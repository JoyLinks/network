/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;
import com.joyzl.network.tls.CertificateStatusRequest.OCSPStatusRequest;
import com.joyzl.network.tls.KeyShare.KeyShareEntry;
import com.joyzl.network.tls.PreSharedKey.PskIdentity;
import com.joyzl.network.tls.SessionCertificates.LocalCache;
import com.joyzl.network.tls.SessionCertificates.RemoteCache;

/**
 * TLSClientHandler
 * 
 * @author ZhangXi 2025年3月10日
 */
public class V3ClientHandler implements ChainHandler {

	private final ChainHandler handler;
	private final TLSParameters parameters;
	private final TLSShare share;

	private final V3CipherSuiter cipher = new V3CipherSuiter();
	private final V3SecretCache secret = new V3SecretCache();
	private Signaturer signaturer;
	private V3KeyExchange key;

	public V3ClientHandler(ChainHandler handler, TLSParameters parameters, TLSShare share) {
		this.parameters = parameters;
		this.handler = handler;
		this.share = share;
	}

	public V3ClientHandler(ChainHandler handler, TLSParameters parameters) {
		this(handler, parameters, new TLSShare());
	}

	public V3ClientHandler(ChainHandler handler) {
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
		final ClientHello hello = createClientHello();

		// 获取缓存的预共享密钥(PSK)
		final NewSessionTicket2 ticket = ClientSessionTickets.get2(share.getServerName());
		if (ticket != null) {
			// 0-RTT
			cipher.initialize(ticket.getSuite());

			// Key Share
			key = new V3KeyExchange(NamedGroup.X25519);
			hello.addExtension(new KeyShareClientHello(new KeyShareEntry(NamedGroup.X25519, key.publicKey())));

			// PSK 注意此扩展必须位于最后
			final PreSharedKeys psk = new PreSharedKeys();
			final PskIdentity pskIdentity = new PskIdentity();
			pskIdentity.setTicketAge(ticket.obfuscatedAgeAdd());
			pskIdentity.setIdentity(ticket.getTicket());
			psk.add(pskIdentity);
			hello.addExtension(psk);
			psk.setHashLength(secret.hmacLength());
			secret.reset(ticket.getResumption());

			// early_data
		} else {
			// 1-RTT
			key = new V3KeyExchange(NamedGroup.X25519);
			// key = new KeyExchange(NamedGroup.SECP256R1);
			hello.addExtension(new KeyShareClientHello(new KeyShareEntry(key.group(), key.publicKey())));

			// Request HelloRetry
			// hello.addExtension(new KeyShareClientHello());
		}

		chain.send(hello);
		chain.receive();
	}

	/**
	 * 创建并设置ClientHello公共参数
	 */
	private ClientHello createClientHello() {
		final ClientHello hello = new ClientHello();
		hello.setCompressionMethods(TLS.COMPRESSION_METHODS);
		hello.setCipherSuites(parameters.cipherSuites());
		hello.setVersion(TLS.V12);
		hello.makeRandom();

		// 1.3
		hello.addExtension(new SupportedVersions(parameters.versions()));
		hello.addExtension(new SupportedGroups(parameters.namedGroups()));
		hello.addExtension(new SignatureAlgorithmsCert(parameters.signatureSchemes()));
		hello.addExtension(new PskKeyExchangeModes(PskKeyExchangeModes.PSK_DHE_KE));
		hello.addExtension(new Heartbeat(Heartbeat.PEER_NOT_ALLOWED_TO_SEND));

		// 1.2
		hello.addExtension(new ServerNames(new ServerName(share.getServerName())));
		hello.addExtension(new SignatureAlgorithms(parameters.signatureSchemes()));
		hello.addExtension(new ApplicationLayerProtocolNegotiation(parameters.alpns()));
		hello.addExtension(ExtendedMasterSecret.INSTANCE);
		hello.addExtension(new CompressCertificate(CompressCertificate.BROTLI));
		hello.addExtension(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		hello.addExtension(new OCSPStatusRequest());
		hello.addExtension(new RenegotiationInfo());
		hello.addExtension(new SessionTicket());

		return hello;
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
		} else if (type == Record.HEARTBEAT) {
			return RecordCoder.decodeHeartbeat(share.buffer());
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
			} else if (record.contentType() == Record.HEARTBEAT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder.encode((HeartbeatMessage) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeCiphertext(cipher, record, data, buffer);
				data.release();
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
		// System.out.println("H:" + type);
		if (type == Handshake.SERVER_HELLO) {
			// 等待握手协商时执行首条消息哈希
			if (share.getServerHello() == null) {
				share.setServerHello(DataBuffer.instance());
			}
			share.getServerHello().replicate(buffer, 0, length);
			return HandshakeCoder.decodeV3(buffer);
		} else if (type == Handshake.FINISHED) {
			// 服务端完成消息校验码
			final byte[] local = secret.serverFinished();
			secret.hash(buffer, length);

			final Finished finished = (Finished) HandshakeCoder.decodeV3(buffer);
			finished.setLocalData(local);
			return finished;
		} else {
			secret.hash(buffer, length);
			return HandshakeCoder.decodeV3(buffer);
		}
	}

	private void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		if (handshake.msgType() == Handshake.CLIENT_HELLO) {
			final ClientHello hello = (ClientHello) handshake;
			HandshakeCoder.encodeV3(handshake, buffer);
			if (hello.hasExtensions() && hello.lastExtension() instanceof PreSharedKeys) {
				// 0-RTT BinderKey
				final PreSharedKeys psks = (PreSharedKeys) hello.lastExtension();

				// 1移除填充的零值
				// Transcript-Hash(Truncate(ClientHello1))
				// binders Length uint16 Short 2Byte
				buffer.backSkip(psks.bindersLength() + 2);
				secret.hash(buffer);

				// 2计算BinderKey
				PskIdentity identity;
				for (int index = 0; index < psks.size(); index++) {
					identity = psks.get(index);
					identity.setBinder(secret.resumptionBinderKey());
				}
				ExtensionCoder.encodeBinders(psks, buffer);

				// 3重置消息哈希以包含BinderKey部分
				secret.hashReset();
			} else {
				// 等待握手协商时执行首条消息哈希
				if (share.getClientHello() == null) {
					share.setClientHello(DataBuffer.instance());
				}
				share.getClientHello().replicate(buffer);
				return;
			}
		} else if (handshake.msgType() == Handshake.CERTIFICATE_VERIFY) {
			// 生成客户端证书消息签名
			final CertificateVerifyV3 verify = (CertificateVerifyV3) handshake;
			verify.setSignature(signaturer.singClient(secret.hash()));
			HandshakeCoder.encodeV3(handshake, buffer);
		} else if (handshake.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) handshake;
			// 握手完成消息编码之前构造验证码
			finished.setVerifyData(secret.clientFinished());
			HandshakeCoder.encodeV3(handshake, buffer);
		} else {
			HandshakeCoder.encodeV3(handshake, buffer);
		}
		secret.hash(buffer);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (message == null) {
			if (secret.isApplication()) {
				handler.received(chain, message);
			} else {
				chain.reset();
			}
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
			} else if (record.contentType() == Record.HEARTBEAT) {
				heartbeat(chain, (HeartbeatMessage) record);
			} else if (record.contentType() == Record.INVALID) {
				chain.reset();
			} else if (record.contentType() == Record.ALERT) {
				chain.reset();
			} else {
				chain.reset();
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
				chain.reset();
			} else if (message instanceof Finished) {
				if (secret.isHandshaked()) {
					// 重置为应用加密套件
					cipher.encryptReset(secret.clientApplicationWriteKey(cipher.type()), secret.clientApplicationWriteIV(cipher.type()));
					// 生成恢复密钥
					secret.resumptionMasterSecret();
					// 握手成功
					handler.connected(chain);
				}
			} else if (message instanceof KeyUpdate) {
				final KeyUpdate update = (KeyUpdate) message;
				if (update.get() == KeyUpdate.UPDATE_NOT_REQUESTED) {
					// 响应密钥更新，重置加解密套件
					secret.serverApplicationTrafficSecret();
					secret.clientApplicationTrafficSecret();
					cipher.encryptReset(secret.serverApplicationWriteKey(cipher.type()), secret.serverApplicationWriteIV(cipher.type()));
					cipher.decryptReset(secret.clientApplicationWriteKey(cipher.type()), secret.clientApplicationWriteIV(cipher.type()));
				}
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
			// 确认版本
			if (hello.getVersion() != TLS.V12) {
				return new Alert(Alert.PROTOCOL_VERSION);
			}
			final SelectedVersion sv = hello.getExtension(Extension.SUPPORTED_VERSIONS);
			if (sv == null) {
				return new Alert(Alert.PROTOCOL_VERSION);
			}
			if (sv.get() != TLS.V13) {
				return new Alert(Alert.PROTOCOL_VERSION);
			}

			if (hello.isHelloRetryRequest()) {
				// 只有支持1.3的服务端才会发送此消息

				if (hello.getCompressionMethod() != 0) {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				if (hello.hasSessionId()) {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				if (hello.getCipherSuite() > 0) {
					cipher.initialize(hello.getCipherSuite());
					secret.initialize(cipher.type());
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}

				final ClientHello retry = createClientHello();
				final KeyShareHelloRetryRequest ksr = hello.getExtension(Extension.KEY_SHARE);
				if (ksr != null) {
					// 生成新的共享密钥
					key.initialize(ksr.getSelectedGroup());
					retry.addExtension(new KeyShareClientHello(new KeyShareEntry(key.group(), key.publicKey())));
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				final Cookie cookie = hello.getExtension(Extension.COOKIE);
				if (cookie != null) {
					// Cookie由Server发送并原样送回
					retry.addExtension(cookie);
				}

				// 特殊合成处理
				secret.hash(share.getClientHello());
				secret.retry();
				secret.hash(share.getServerHello());
				share.setClientHello(null);
				share.setServerHello(null);
				return retry;
			} else {
				final EarlyDataIndication ed = hello.getExtension(Extension.EARLY_DATA);
				final PreSharedKeySelected psk = hello.getExtension(Extension.PRE_SHARED_KEY);
				if (psk != null) {
					if (ed != null) {
						if (psk.getSelected() != 0) {
							// 早期数据时PSK必须选择首个
							// 否则加密的的早期数据如何解密呢
							return new Alert(Alert.ILLEGAL_PARAMETER);
						} else {
							// 已选择0-RTT握手
							// 早期数据已被接受
							// 无须重置密码套件
						}
					} else {
						// 已选择0-RTT握手
						// 无须重置密码套件
					}
				} else {
					if (ed != null) {
						// 未选择PSK时不应有早期数据扩展
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
					// 继续常规握手
					cipher.initialize(hello.getCipherSuite());
					secret.initialize(cipher.type());
				}

				if (share.getClientHello() != null) {
					secret.hash(share.getClientHello());
					share.setClientHello(null);
				}
				if (share.getServerHello() != null) {
					secret.hash(share.getServerHello());
					share.setServerHello(null);
				}

				final KeyShareServerHello share = hello.getExtension(Extension.KEY_SHARE);
				if (share != null) {
					if (share.getServerShare() != null) {
						if (share.getServerShare().getGroup() == key.group()) {
							secret.sharedKey(key.sharedKey(share.getServerShare().getKeyExchange()));
							secret.clientHandshakeTrafficSecret();
							secret.serverHandshakeTrafficSecret();
							cipher.encryptReset(secret.clientHandshakeWriteKey(cipher.type()), secret.clientHandshakeWriteIV(cipher.type()));
							cipher.decryptReset(secret.serverHandshakeWriteKey(cipher.type()), secret.serverHandshakeWriteIV(cipher.type()));
						} else {
							return new Alert(Alert.ILLEGAL_PARAMETER);
						}
					} else {
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
				} else {
					// TODO 待验证
				}
			}
		} else if (record.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			final EncryptedExtensions extensions = (EncryptedExtensions) record;
			final ApplicationLayerProtocolNegotiation alpn = extensions.getExtension(Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION);
			if (alpn != null) {

			}
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
					// 没有匹配的算法
				}
			} else {
				// 没有任何证书时也应返回空消息
			}
			return certificate;
		} else if (record.msgType() == Handshake.CERTIFICATE) {
			final CertificateV3 certificate = (CertificateV3) record;
			if (certificate.size() > 0) {
				final RemoteCache remote = SessionCertificates.loadCertificate(share.getServerName(), certificate);
				// OCSP
				// SignedCertificateTimestamp
				// MD5/SHA-1 bad_certificate

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
				if (signaturer == null) {
					// 构建证书签名验证对象
					// 签名算法待证书验证消息指定
					signaturer = new Signaturer();
					signaturer.setPublicKey(remote.getPublicKey());
					signaturer.setHash(secret.hash());
				} else {
					return new Alert(Alert.UNEXPECTED_MESSAGE);
				}
			} else {
				// 不允许服务端发送空的证书消息
				return new Alert(Alert.DECODE_ERROR);
			}
		} else if (record.msgType() == Handshake.CERTIFICATE_VERIFY) {
			final CertificateVerifyV3 verify = (CertificateVerifyV3) record;
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
		} else if (record.msgType() == Handshake.NEW_SESSION_TICKET) {
			final NewSessionTicket2 ticket = (NewSessionTicket2) record;
			ticket.setResumption(secret.resumptionSecret(ticket.getNonce()));
			ticket.setSuite(cipher.type().code());
			ClientSessionTickets.put(share.getServerName(), ticket);
		} else if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.validate()) {
				// 导出应用流量密钥
				secret.serverApplicationTrafficSecret();
				// 加密套件等待握手完成消息发送后重置
				secret.clientApplicationTrafficSecret();
				// 重置解密套件
				cipher.decryptReset(secret.serverApplicationWriteKey(cipher.type()), secret.serverApplicationWriteIV(cipher.type()));

				// cipher.exporterMaster();
				// EndOfEarlyData
				// chain.send(EndOfEarlyData.INSTANCE);
				// 握手消息完成验证码待编码时生成
				return finished;
			} else {
				return new Alert(Alert.DECRYPT_ERROR);
			}
		}
		return null;
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		if (secret.isApplication()) {
			final HeartbeatMessage heartbeat = new HeartbeatMessage();
			heartbeat.setMessageType(HeartbeatMessage.HEARTBEAT_REQUEST);
			heartbeat.setPayload(Binary.split(chain.hashCode()));
			chain.send(heartbeat);
		}
	}

	private void heartbeat(ChainChannel chain, HeartbeatMessage message) {
		if (message.getMessageType() == HeartbeatMessage.HEARTBEAT_RESPONSE) {
			if (message.getPayload().length == 4) {
				int hashCode = Binary.getInteger(message.getPayload(), 0);
				if (hashCode == chain.hashCode()) {
					// OK
				} else {
					// 无额外处理
				}
			}
		} else {
			chain.send(new Alert(Alert.UNEXPECTED_MESSAGE));
		}
	}
}