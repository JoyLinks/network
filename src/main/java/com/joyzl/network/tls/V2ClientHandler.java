package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;
import com.joyzl.network.tls.CertificateStatusRequest.OCSPStatusRequest;
import com.joyzl.network.tls.SessionCertificates.LocalCache;
import com.joyzl.network.tls.SessionCertificates.RemoteCache;

/**
 * TLSClientHandler
 * 
 * @author ZhangXi 2025年3月10日
 */
public class V2ClientHandler implements ChainHandler {

	private final ChainHandler handler;
	private final TLSParameters parameters;
	private final TLSShare share;

	private final V2CipherSuiter cipher = new V2CipherSuiter();
	private final V2SecretCache secret = new V2SecretCache();
	private final Signaturer signaturer = new Signaturer();

	private boolean extendedMasterSecret;
	private byte[] clientVerify, serverVerify;
	private byte[] sessionId;

	public V2ClientHandler(ChainHandler handler, TLSParameters parameters, TLSShare share) {
		this.parameters = parameters;
		this.handler = handler;
		this.share = share;
	}

	public V2ClientHandler(ChainHandler handler, TLSParameters parameters) {
		this(handler, parameters, new TLSShare());
	}

	public V2ClientHandler(ChainHandler handler) {
		this(handler, new TLSParameters(), new TLSShare());
	}

	@Override
	public long getTimeoutRead() {
		return handler.getTimeoutRead();
	}

	@Override
	public long getTimeoutWrite() {
		return handler.getTimeoutWrite();
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		handler.disconnected(chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		handler.error(chain, e);
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		// 从远端地址获取服务名称
		if (share.getServerName() == null) {
			share.setServerName(ServerName.from(chain.getRemoteAddress()));
		}

		// 构建握手请求消息
		final ClientHello hello = new ClientHello();
		hello.setCompressionMethods(TLS.COMPRESSION_METHODS);
		hello.setCipherSuites(parameters.cipherSuites());
		hello.setVersion(TLS.V12);
		hello.makeRandom();

		hello.addExtension(new ServerNames(new ServerName(share.getServerName())));
		hello.addExtension(new SignatureAlgorithms(parameters.signatureSchemes()));
		hello.addExtension(new ApplicationLayerProtocolNegotiation(parameters.alpns()));
		hello.addExtension(ExtendedMasterSecret.INSTANCE);

		hello.addExtension(new CompressCertificate(CompressCertificate.BROTLI));
		hello.addExtension(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		hello.addExtension(new OCSPStatusRequest());
		hello.addExtension(new RenegotiationInfo());
		hello.addExtension(new SessionTicket());

		// SESSION TICKET
		final NewSessionTicket1 ticket = ClientSessionTickets.get1(share.getServerName());
		if (ticket != null) {
			final SessionTicket st = hello.getExtension(Extension.SESSION_TICKET);
			st.setTicket(ticket.getTicket());
			cipher.suite(ticket.getSuite());
			secret.master(ticket.getResumption());
		} else {
			// SESSION ID
			hello.setSessionId(sessionId);
			if (hello.hasSessionId()) {
				cipher.suite(cipher.type().code());
				secret.master(secret.master());
			} else {
				cipher.suite(CipherSuiter.TLS_NULL_WITH_NULL_NULL);
				secret.pms(null);
			}
		}
		clientVerify = hello.getRandom();

		chain.send(hello);
		chain.receive();
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final int type;
		try {
			type = RecordCoder.decode(cipher, buffer, share.buffer());
		} catch (Exception e) {
			if (e instanceof TLSException) {
				e.printStackTrace();
				return new Alert((TLSException) e);
			} else {
				throw e;
			}
		}
		if (type < 0) {
			return null;
		} else if (type == Record.APPLICATION_DATA) {
			Object message = handler.decode(chain, share.buffer());
			while (message != null && share.buffer().readable() > 0) {
				handler.received(chain, message);
				message = handler.decode(chain, share.buffer());
			}
			return message;
		} else if (type == Record.HANDSHAKE) {
			Handshake handshake = decode(share.buffer());
			while (handshake != null && share.buffer().readable() > 0) {
				received(chain, handshake);
				handshake = decode(share.buffer());
			}
			return handshake;
		} else if (type == Record.CHANGE_CIPHER_SPEC) {
			return RecordCoder.decodeChangeCipherSpec(share.buffer());
		} else if (type == Record.ALERT) {
			return RecordCoder.decodeAlert(share.buffer());
		} else {
			return new TLSException(Alert.UNEXPECTED_MESSAGE);
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record record) {
			// TLS RECORD
			if (record.contentType() == Record.HANDSHAKE) {
				if (record instanceof Handshakes handshakes) {
					// 握手消息不与其他消息交错
					// 握手消息不能跨越密钥更改
					final DataBuffer data = DataBuffer.instance();
					final DataBuffer buffer = DataBuffer.instance();
					for (int i = 0; i < handshakes.size(); i++) {
						encode(handshakes.get(i), data);
						if (handshakes.get(i).msgType() == Handshake.FINISHED) {
							RecordCoder.encodeV2(ChangeCipherSpec.INSTANCE, buffer);
						}
						if (cipher.encryptReady()) {
							RecordCoder.encodeCiphertext(cipher, record, data, buffer);
						} else {
							RecordCoder.encodePlaintextV2(record, data, buffer);
						}
					}
					data.release();
					return buffer;
				} else {
					final DataBuffer data = DataBuffer.instance();
					encode((Handshake) record, data);
					final DataBuffer buffer = DataBuffer.instance();
					if (((Handshake) record).msgType() == Handshake.FINISHED) {
						RecordCoder.encodeV2(ChangeCipherSpec.INSTANCE, buffer);
					}
					if (cipher.encryptReady()) {
						RecordCoder.encodeCiphertext(cipher, record, data, buffer);
					} else {
						RecordCoder.encodePlaintextV2(record, data, buffer);
					}
					data.release();
					return buffer;
				}
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeV2((ChangeCipherSpec) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.APPLICATION_DATA) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeV2((ApplicationData) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.ALERT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder.encode((Alert) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				if (cipher.encryptReady()) {
					RecordCoder.encodeCiphertext(cipher, record, data, buffer);
				} else {
					RecordCoder.encodePlaintextV2(record, data, buffer);
				}
				data.release();
				return buffer;
			} else {
				throw new UnsupportedOperationException("TLS:意外的记录类型" + record.contentType());
			}
		} else {
			// APPLICATION DATA
			final DataBuffer data = handler.encode(chain, message);
			final DataBuffer buffer = DataBuffer.instance();
			RecordCoder.encodeCiphertext(cipher, ApplicationData.INSTANCE, data, buffer);
			data.release();
			return buffer;
		}
	}

	private Handshake decode(DataBuffer buffer) throws Exception {
		if (buffer.readable() < 4) {
			return null;
		}
		// 获取消息类型
		final byte type = buffer.get(0);
		// 获取消息字节数(type 1byte + length uint24 + data)
		final int length = Binary.join((byte) 0, buffer.get(1), buffer.get(2), buffer.get(3)) + 4;
		// 校验数据长度
		if (buffer.readable() < length) {
			return null;
		}

		if (type == Handshake.SERVER_HELLO) {
			// 等待握手协商时执行首条消息哈希
			if (share.getServerHello() == null) {
				share.setServerHello(DataBuffer.instance());
			}
			share.getServerHello().replicate(buffer, 0, length);
			return HandshakeCoder.decodeV2(buffer);
		} else if (type == Handshake.FINISHED) {
			// 服务端完成消息校验码
			final byte[] local = secret.serverFinished();
			secret.hash(buffer, length);

			final Finished finished = (Finished) HandshakeCoder.decodeV2(buffer);
			finished.setLocalData(local);
			return finished;
		} else if (type == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			return HandshakeCoder.decodeV2(buffer);
		} else {
			secret.hash(buffer, length);
			return HandshakeCoder.decodeV2(buffer);
		}
	}

	private void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		if (handshake.msgType() == Handshake.CLIENT_HELLO) {
			HandshakeCoder.encodeV2(handshake, buffer);

			// 等待握手协商时执行首条消息哈希
			if (share.getClientHello() == null) {
				share.setClientHello(DataBuffer.instance());
			}
			share.getClientHello().replicate(buffer);
		} else if (handshake.msgType() == Handshake.CERTIFICATE_VERIFY) {
			final CertificateVerifyV3 verify = (CertificateVerifyV3) handshake;
			// 生成客户端证书消息签名
			verify.setSignature(signaturer.singClient(secret.hash()));

			HandshakeCoder.encodeV2(handshake, buffer);
			secret.hash(buffer);
		} else if (handshake.msgType() == Handshake.CLIENT_KEY_EXCHANGE) {
			HandshakeCoder.encodeV2(handshake, buffer);
			secret.hash(buffer);

			// 导出客户端密钥
			if (extendedMasterSecret) {
				secret.masterSecret();
				secret.keyBlock(cipher.type(), serverVerify, clientVerify);
			} else {
				secret.masterSecret(clientVerify, serverVerify);
				secret.keyBlock(cipher.type(), serverVerify, clientVerify);
			}
		} else if (handshake.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) handshake;
			// 握手完成消息编码之前构造验证码
			finished.setVerifyData(secret.clientFinished());
			clientVerify = finished.getVerifyData();

			HandshakeCoder.encodeV2(handshake, buffer);
			secret.hash(buffer);

			// 重置加密密钥
			cipher.encryptReset(secret.clientWriteKey(cipher.type()), secret.clientWriteIV(cipher.type()));
			cipher.encryptMACKey(secret.clientWriteMACKey(cipher.type()));
		} else if (handshake.msgType() == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			HandshakeCoder.encodeV2(handshake, buffer);
		} else {
			HandshakeCoder.encodeV2(handshake, buffer);
			secret.hash(buffer);
		}
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (message == null) {
			handler.received(chain, message);
		} else if (message instanceof Record) {
			System.out.println("RECV \n" + message);
			final Record record = (Record) message;
			if (record.contentType() == Record.APPLICATION_DATA) {
				// 忽略空的应用消息
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				// 重置解密密钥
				cipher.decryptReset(secret.serverWriteKey(cipher.type()), secret.serverWriteIV(cipher.type()));
				cipher.decryptMACKey(secret.serverWriteMACKey(cipher.type()));
			} else if (record.contentType() == Record.HANDSHAKE) {
				message = handshake((Handshake) record);
				if (message != null) {
					chain.send(message);
				}
			} else if (record.contentType() == Record.ALERT) {
				chain.reset();
			} else {
				chain.reset();
			}
		} else {
			handler.received(chain, message);
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record) {
			System.out.println("SENT \n" + message);
			if (message instanceof Alert) {
				chain.reset();
			} else if (message instanceof Finished) {
				handler.connected(chain);
			} else if (message instanceof Handshakes handshakes) {
				if (handshakes.last().msgType() == Handshake.FINISHED) {
					handler.connected(chain);
				}
			}
		} else {
			handler.sent(chain, message);
		}
	}

	/**
	 * 握手，本实现所有握手消息(Handshake)继承自记录层(Record)；<br>
	 * 收到的握手消息即便在记录层已合并，在解码后也逐消息调用此方法；
	 */
	private Record handshake(Handshake record) throws Exception {
		if (record.msgType() == Handshake.SERVER_HELLO) {
			final ServerHello hello = (ServerHello) record;
			// 确认版本
			if (hello.getVersion() != TLS.V12) {
				return new Alert(Alert.PROTOCOL_VERSION);
			}

			cipher.suite(hello.getCipherSuite());
			secret.initialize(cipher.type());
			serverVerify = hello.getRandom();

			if (share.getClientHello() != null) {
				secret.hash(share.getClientHello());
				share.setClientHello(null);
			}
			if (share.getServerHello() != null) {
				secret.hash(share.getServerHello());
				share.setServerHello(null);
			}

			final RenegotiationInfo ri = hello.getExtension(Extension.RENEGOTIATION_INFO);
			if (ri != null) {
				if (ri.isEmpty()) {
					// OK
				} else {
					return new Alert(Alert.HANDSHAKE_FAILURE);
				}
			}

			// 使用增强型主密钥
			final ExtendedMasterSecret ems = hello.getExtension(Extension.EXTENDED_MASTER_SECRET);
			extendedMasterSecret = ems != null;

			if (hello.hasSessionId()) {
				if (Arrays.equals(sessionId, hello.getSessionId())) {
					// 通过会话标识恢复
					if (secret.master() != null) {
						// 导出客户端密钥
						secret.keyBlock(cipher.type(), serverVerify, clientVerify);
						return null;
					} else {
						// 继续常规握手
					}
				} else {
					// 缓存会话标识
					sessionId = hello.getSessionId();
					// 继续常规握手
				}
			} else {
				// 服务端未提供会话标识
				// 通常有会话票据扩展

				final SessionTicket st = hello.getExtension(Extension.SESSION_TICKET);
				if (st != null) {
					if (st.hasTicket()) {
						return new Alert(Alert.ILLEGAL_PARAMETER);
					} else {
						// 继续常规握手
					}
				} else {
					// 执行票据恢复
					if (secret.master() != null) {
						// 导出客户端密钥
						secret.keyBlock(cipher.type(), serverVerify, clientVerify);
						return null;
					} else {
						// 继续常规握手
					}
				}
			}

			// 生成密钥
			// RSA
			secret.pms(V2DeriveSecret.preMasterSecret(hello.getVersion()));
			// Diffie-Hellman
		} else if (record.msgType() == Handshake.HELLO_REQUEST) {
			// 服务端要求重新协商

		} else if (record.msgType() == Handshake.CERTIFICATE_REQUEST) {
			final CertificateRequestV3 request = (CertificateRequestV3) record;
			final OIDFilters filters = request.getExtension(Extension.OID_FILTERS);
			final SignatureAlgorithms algorithms = request.getExtension(Extension.SIGNATURE_ALGORITHMS);
			final CertificateAuthorities authorities = request.getExtension(Extension.CERTIFICATE_AUTHORITIES);
			final SignatureAlgorithmsCert sac = request.getExtension(Extension.SIGNATURE_ALGORITHMS_CERT);

			final CertificateV3 certificate = new CertificateV3();
			certificate.setContext(request.getContext());
			final LocalCache local = SessionCertificates.filters(authorities, filters);
			if (local != null) {
				final short scheme = algorithms.match(local.getScheme());
				if (scheme > 0) {
					final CertificateVerifyV3 certificateVerify = new CertificateVerifyV3();
					certificateVerify.setAlgorithm(scheme);
					certificate.set(local.getEntries());
				} else {

				}
			} else {
				// 没有任何证书时也应返回空消息
			}
			return certificate;
		} else if (record.msgType() == Handshake.CERTIFICATE) {
			final CertificateV0 certificate = (CertificateV0) record;
			if (certificate.size() > 0) {
				final RemoteCache remote = SessionCertificates.loadCertificate(share.getServerName(), certificate);
				if (parameters.isIgnoreCertificate()) {
					// 不检查证书
				} else {
					try {
						// 验证证书有效性
						SessionCertificates.check(remote.getCertificates());
					} catch (Exception e) {
						return new Alert(Alert.UNSUPPORTED_CERTIFICATE);
					}
				}

				// 设置证书签名验证对象
				// 证书可能不具备签名算法
				signaturer.scheme(remote.getScheme());
				signaturer.setPublicKey(remote.getPublicKey());
				signaturer.setHash(secret.hash());
			} else {
				// 不允许服务端发送空的证书消息
				return new Alert(Alert.DECODE_ERROR);
			}
		} else if (record.msgType() == Handshake.SERVER_HELLO_DONE) {
			final Handshakes handshakes = new Handshakes();

			final ClientKeyExchange exchange = new ClientKeyExchange();
			exchange.set(signaturer.encryptPKCS1(secret.pms()));
			handshakes.add(exchange);

			final Finished finished = new Finished();
			// 握手消息完成验证码待编码时生成
			handshakes.add(finished);
			return handshakes;
		} else if (record.msgType() == Handshake.NEW_SESSION_TICKET) {
			final NewSessionTicket1 ticket = (NewSessionTicket1) record;
			// 将主密钥和密码套件关联到会话恢复票据
			ticket.setResumption(secret.master());
			ticket.setSuite(cipher.type().code());
			ClientSessionTickets.put(share.getServerName(), ticket);
		} else if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.validate()) {
				// 计算并保留消息完成验证码
				// 用于旧版本1.2重协商
				serverVerify = finished.getVerifyData();

				if (secret.pms() == null) {
					// 票据恢复时
					return finished;
				}
			} else {
				return new Alert(Alert.DECRYPT_ERROR);
			}
		}
		return null;
	}
}