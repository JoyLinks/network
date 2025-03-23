package com.joyzl.network.tls;

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
public class V2ClientHandler extends TLSParameters implements ChainHandler {

	private final ChainHandler handler;

	private final V2CipherSuiter cipher = new V2CipherSuiter();
	private Signaturer signaturer;
	private KeyExchange key;
	private String sn;

	private final DataBuffer data = DataBuffer.instance();
	private DataBuffer clientHello, serverHello;
	private byte[] clientVerify, serverVerify;
	private byte[] sessionId;
	private short version;

	public V2ClientHandler(ChainHandler handler) {
		this.handler = handler;
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
		sn = ServerName.from(chain.getRemoteAddress());

		// 构建握手请求消息
		final ClientHello hello = new ClientHello();
		clientHello(hello);
		hello.setSessionId(sessionId);
		clientVerify = hello.getRandom();

		chain.send(hello);
		chain.receive();
	}

	/**
	 * 创建并设置ClientHello公共参数
	 */
	private void clientHello(ClientHello hello) {
		hello.setCompressionMethods(TLS.COMPRESSION_METHODS);
		hello.setCipherSuites(cipherSuites());
		// hello.setCipherSuites(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256);
		hello.setVersion(TLS.V12);
		hello.makeRandom();

		hello.addExtension(new ServerNames(new ServerName(sn)));
		hello.addExtension(new SupportedGroups(namedGroups()));
		hello.addExtension(new SignatureAlgorithms(signatureSchemes()));
		hello.addExtension(new SignatureAlgorithmsCert(signatureSchemes()));
		hello.addExtension(new ApplicationLayerProtocolNegotiation(alpns()));
		hello.addExtension(ExtendedMasterSecret.INSTANCE);

		hello.addExtension(new CompressCertificate(CompressCertificate.BROTLI));
		hello.addExtension(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		// hello.addExtension(new SignedCertificateTimestamp());
		// hello.addExtension(new CertificateStatusRequestListV2());
		hello.addExtension(new OCSPStatusRequest());
		hello.addExtension(new RenegotiationInfo());
		hello.addExtension(new SessionTicket());
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final int type;
		try {
			type = V2RecordCoder.decode(cipher, buffer, data);
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
			Object message = handler.decode(chain, data);
			while (message != null && data.readable() > 0) {
				handler.received(chain, message);
				message = handler.decode(chain, data);
			}
			return message;
		} else if (type == Record.HANDSHAKE) {
			Handshake handshake = decode(data);
			while (handshake != null && data.readable() > 0) {
				received(chain, handshake);
				handshake = decode(data);
			}
			return handshake;
		} else if (type == Record.CHANGE_CIPHER_SPEC) {
			return V2RecordCoder.decodeChangeCipherSpec(data);
		} else if (type == Record.ALERT) {
			return V2RecordCoder.decodeAlert(data);
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
							V2RecordCoder.encode(ChangeCipherSpec.INSTANCE, buffer);
						}
						if (cipher.encryptReady()) {
							V2RecordCoder.encodeCiphertext(cipher, record, data, buffer);
						} else {
							V2RecordCoder.encodePlaintext(record, data, buffer);
						}
					}
					data.release();
					return buffer;
				} else {
					final DataBuffer data = DataBuffer.instance();
					encode((Handshake) record, data);
					final DataBuffer buffer = DataBuffer.instance();
					if (((Handshake) record).msgType() == Handshake.FINISHED) {
						V2RecordCoder.encode(ChangeCipherSpec.INSTANCE, buffer);
					}
					if (cipher.encryptReady()) {
						V2RecordCoder.encodeCiphertext(cipher, record, data, buffer);
					} else {
						V2RecordCoder.encodePlaintext(record, data, buffer);
					}
					data.release();
					return buffer;
				}
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				final DataBuffer buffer = DataBuffer.instance();
				V2RecordCoder.encode((ChangeCipherSpec) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.APPLICATION_DATA) {
				final DataBuffer buffer = DataBuffer.instance();
				V2RecordCoder.encode((ApplicationData) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.ALERT) {
				final DataBuffer data = DataBuffer.instance();
				V2RecordCoder.encode((Alert) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				if (cipher.encryptReady()) {
					V2RecordCoder.encodeCiphertext(cipher, record, data, buffer);
				} else {
					V2RecordCoder.encodePlaintext(record, data, buffer);
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
			V2RecordCoder.encodeCiphertext(cipher, ApplicationData.INSTANCE, data, buffer);
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
		// System.out.println("H:" + type);
		if (type == Handshake.SERVER_HELLO) {
			serverHello = DataBuffer.instance();
			serverHello.replicate(buffer, 0, length);
			return HandshakeCoder.decode(version, buffer);
		} else if (type == Handshake.FINISHED) {
			// 服务端完成消息校验码
			final byte[] local = cipher.serverFinished();
			cipher.hash(buffer, length);
			System.out.println("HASH:" + Handshake.name(type));

			final Finished finished = (Finished) HandshakeCoder.decode(version, buffer);
			finished.setLocalData(local);

			// 重置解密密钥
			cipher.decryptReset(cipher.serverWriteKey(), cipher.serverWriteIV());
			cipher.decryptMACKey(cipher.serverWriteMACKey());

			return finished;
		} else if (type == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			return HandshakeCoder.decode(version, buffer);
		} else {
			cipher.hash(buffer, length);
			System.out.println("HASH:" + Handshake.name(type));
			return HandshakeCoder.decode(version, buffer);
		}
	}

	private void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		if (handshake.msgType() == Handshake.CLIENT_HELLO) {
			HandshakeCoder.encode(handshake, buffer);

			// 等待握手协商时执行首条消息哈希
			clientHello = DataBuffer.instance();
			clientHello.replicate(buffer);
			return;
		} else if (handshake.msgType() == Handshake.CERTIFICATE_VERIFY) {
			// 生成客户端证书消息签名
			final CertificateVerify verify = (CertificateVerify) handshake;
			verify.setSignature(signaturer.singClient(cipher.hash()));
			HandshakeCoder.encode(handshake, buffer);
		} else if (handshake.msgType() == Handshake.FINISHED) {
			// 握手完成消息编码之前构造验证码
			final Finished finished = (Finished) handshake;
			finished.setVerifyData(cipher.clientFinished());
			clientVerify = finished.getVerifyData();

			// 重置加密密钥
			cipher.encryptReset(cipher.clientWriteKey(), cipher.clientWriteIV());
			cipher.encryptMACKey(cipher.clientWriteMACKey());

			HandshakeCoder.encode(handshake, buffer);
		} else if (handshake.msgType() == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			HandshakeCoder.encode(handshake, buffer);
			return;
		} else {
			HandshakeCoder.encode(handshake, buffer);
		}
		System.out.println("HASH:" + handshake.name());
		cipher.hash(buffer);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (message == null) {
			handler.received(chain, message);
		} else if (message instanceof Record) {
			System.out.println("RECV " + message);
			final Record record = (Record) message;
			if (record.contentType() == Record.APPLICATION_DATA) {
				// 忽略空的应用消息
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				// 忽略兼容性消息
			} else if (record.contentType() == Record.HANDSHAKE) {
				message = handshake((Handshake) record);
				if (message != null) {
					chain.send(message);
				}
			} else if (record.contentType() == Record.INVALID) {
				// chain.close();
			} else if (record.contentType() == Record.ALERT) {
				// chain.close();
			} else {
				// chain.close();
			}
		} else {
			if (cipher.decryptLimit()) {
				chain.send(new KeyUpdate());
			}
			handler.received(chain, message);
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record) {
			System.out.println("SENT " + message);
			if (message instanceof Alert) {
				// chain.close();
			} else if (message instanceof Finished) {

			}
		} else {
			if (cipher.encryptLimit()) {
				chain.send(new KeyUpdate());
			}
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

			final SelectedVersion selected = hello.getExtension(Extension.SUPPORTED_VERSIONS);
			if (selected != null) {
				version = selected.get();
			} else {
				version = hello.getVersion();
			}
			if (version == 0) {
				// 未能匹配TLS版本
				return new Alert(Alert.PROTOCOL_VERSION);
			}

			if (version == TLS.V12) {
				cipher.suite(hello.getCipherSuite());
				serverVerify = hello.getRandom();
				sessionId = hello.getSessionId();

				// DOWNGRD
				// TODO

				if (clientHello != null) {
					cipher.hash(clientHello);
					System.out.println("HASH:clientHello");
					clientHello.release();
					clientHello = null;
				}
				if (serverHello != null) {
					cipher.hash(serverHello);
					System.out.println("HASH:serverHello");
					serverHello.release();
					serverHello = null;
				}

				// RSA
				cipher.pms(V2DeriveSecret.preMasterSecret(hello.getVersion()));
				// Diffie-Hellman

				final ExtendedMasterSecret ems = hello.getExtension(Extension.EXTENDED_MASTER_SECRET);
				if (ems != null) {
					cipher.masterSecret();
					cipher.keyBlock(serverVerify, clientVerify);
				} else {
					cipher.masterSecret(clientVerify, serverVerify);
					cipher.keyBlock(serverVerify, clientVerify);
				}

				final RenegotiationInfo ri = hello.getExtension(Extension.RENEGOTIATION_INFO);
				if (ri != null) {
					if (ri.isEmpty()) {
						// OK
					} else {
						return new Alert(Alert.HANDSHAKE_FAILURE);
					}
				}
			}
		} else if (record.msgType() == Handshake.HELLO_REQUEST) {
			// 服务端要求重新协商

		} else if (record.msgType() == Handshake.CERTIFICATE_REQUEST) {
			final CertificateRequest request = (CertificateRequest) record;
			final OIDFilters filters = request.getExtension(Extension.OID_FILTERS);
			final SignatureAlgorithms algorithms = request.getExtension(Extension.SIGNATURE_ALGORITHMS);
			final CertificateAuthorities authorities = request.getExtension(Extension.CERTIFICATE_AUTHORITIES);
			final SignatureAlgorithmsCert sac = request.getExtension(Extension.SIGNATURE_ALGORITHMS_CERT);

			final Certificate certificate = new Certificate();
			certificate.setContext(request.getContext());
			final LocalCache local = SessionCertificates.filters(authorities, filters);
			if (local != null) {
				final short scheme = algorithms.match(local.getScheme());
				if (scheme > 0) {
					final CertificateVerify certificateVerify = new CertificateVerify();
					certificateVerify.setAlgorithm(scheme);
					certificate.set(local.getEntries());
				} else {

				}
			} else {
				// 没有任何证书时也应返回空消息
			}
			return certificate;
		} else if (record.msgType() == Handshake.CERTIFICATE) {
			final Certificate certificate = (Certificate) record;
			if (certificate.size() > 0) {
				final RemoteCache remote = SessionCertificates.loadCertificate(sn, certificate);
				// OCSP
				// SignedCertificateTimestamp
				// MD5/SHA-1 bad_certificate

				if (isIgnoreCertificate()) {
					// 不检查证书
				} else {
					try {
						// 验证证书有效性
						SessionCertificates.check(remote.getCertificates());
					} catch (Exception e) {
						return new Alert(Alert.UNSUPPORTED_CERTIFICATE);
					}
				}
				if (signaturer == null) {
					// 构建证书签名验证对象
					// 签名算法待证书验证消息指定
					signaturer = new Signaturer();
					signaturer.setPublicKey(remote.getPublicKey());
					signaturer.setHash(cipher.hash());
				} else {
					return new Alert(Alert.UNEXPECTED_MESSAGE);
				}
			} else {
				// 不允许服务端发送空的证书消息
				return new Alert(Alert.DECODE_ERROR);
			}
		} else if (record.msgType() == Handshake.CERTIFICATE_VERIFY) {
			final CertificateVerify verify = (CertificateVerify) record;
			if (signaturer != null) {
				signaturer.scheme(verify.getAlgorithm());
				if (signaturer.verifyServer(verify.getSignature())) {
					signaturer = null;
					// OK
				} else {
					signaturer = null;
					// 证书消息签名验证失败
					return new Alert(Alert.DECRYPT_ERROR);
				}
			} else {
				return new Alert(Alert.UNEXPECTED_MESSAGE);
			}
		} else if (record.msgType() == Handshake.SERVER_HELLO_DONE) {
			final Handshakes handshakes = new Handshakes();

			final ClientKeyExchange exchange = new ClientKeyExchange();
			exchange.set(signaturer.encryptPKCS1(cipher.pms()));
			handshakes.add(exchange);

			final Finished finished = new Finished();
			// 握手消息完成验证码待编码时生成
			handshakes.add(finished);

			return handshakes;
		} else if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.validate()) {
				// 计算并保留消息完成验证码
				// 用于旧版本1.2重协商
				serverVerify = finished.getVerifyData();
				return finished;
			} else {
				return new Alert(Alert.DECRYPT_ERROR);
			}
		}
		return null;
	}
}