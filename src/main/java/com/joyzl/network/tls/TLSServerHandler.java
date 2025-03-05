package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.tls.SessionCertificates.CPK;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class TLSServerHandler implements ChainHandler {

	private final ChainHandler handler;
	/** 启用的密钥算法 */
	private short[] namedGroups = NamedGroup.ALL;
	/** 启用的版本 */
	private short[] versions = TLS.ALL_VERSIONS;
	/** 启用的协议 */
	private byte[][] protocols;

	public TLSServerHandler(ChainHandler handler) {
		this.handler = handler;
	}

	protected ChainHandler handler() {
		return handler;
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.setContext(new TLSContext());
		chain.receive();
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final TLSContext context = chain.getContext();
		final int type;
		try {
			type = RecordCoder2.decode(context.cipher, buffer, context.data);
		} catch (Exception e) {
			if (e instanceof TLSException) {
				return new Alert((TLSException) e);
			} else {
				throw e;
			}
		}
		if (type < 0) {
			return null;
		} else if (type == Record.APPLICATION_DATA) {
			Object message = handler().decode(chain, context.data);
			while (message != null && context.data.readable() > 0) {
				handler().received(chain, message);
				message = handler().decode(chain, context.data);
			}
			return message;
		} else if (type == Record.HANDSHAKE) {
			Handshake handshake = decodeHandshake(context, context.data);
			while (handshake != null && context.data.readable() > 0) {
				received(chain, handshake);
				handshake = decodeHandshake(context, context.data);
			}
			return handshake;
		} else if (type == Record.CHANGE_CIPHER_SPEC) {
			return RecordCoder2.decodeChangeCipherSpec(context.data);
		} else if (type == Record.HEARTBEAT) {
			return RecordCoder2.decodeHeartbeat(context.data);
		} else if (type == Record.ALERT) {
			return RecordCoder2.decodeAlert(context.data);
		} else {
			return new TLSException(Alert.UNEXPECTED_MESSAGE);
		}
	}

	private Handshake decodeHandshake(TLSContext context, DataBuffer buffer) throws IOException {
		return HandshakeCoder.decode(buffer);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext();
		if (message == null) {
			if (context.finished) {
				handler().received(chain, message);
			} else {
				chain.close();
			}
		} else if (message instanceof Record) {
			final Record record = (Record) message;
			if (record.contentType() == Record.APPLICATION_DATA) {
				// 忽略空的应用消息
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				// 忽略兼容性消息
			} else if (record.contentType() == Record.HANDSHAKE) {
				message = handshake(context, (Handshake) record);
				if (message != null) {
					chain.send(message);
				}
				if (context.finished) {
					handler().connected(chain);
				}
			} else if (record.contentType() == Record.HEARTBEAT) {
				heartbeat(chain, (HeartbeatMessage) record);
			} else if (record.contentType() == Record.INVALID) {
				chain.close();
			} else if (record.contentType() == Record.ALERT) {
				chain.close();
			} else {
				chain.close();
			}
		} else {
			handler().received(chain, message);
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext();
		if (message instanceof Record record) {
			// TLS RECORD
			if (record.contentType() == Record.HANDSHAKE) {
				if (record instanceof Handshakes handshakes) {
					// 握手消息不与其他消息交错
					// 握手消息不能跨越密钥更改
					final DataBuffer data = DataBuffer.instance();
					final DataBuffer buffer = DataBuffer.instance();
					for (int i = 0; i < handshakes.size(); i++) {
						encode(context, handshakes.get(i), data);
						if (handshakes.get(i).msgType() == Handshake.SERVER_HELLO) {
							RecordCoder2.encodePlaintext(record, data, buffer);
							RecordCoder2.encode((ChangeCipherSpec) record, buffer);
						} else {
							RecordCoder2.encodeCiphertext(context.cipher, record, data, buffer);
						}
					}
					data.release();
					return buffer;
				} else {
					final DataBuffer data = DataBuffer.instance();
					encode(context, (Handshake) record, data);
					final DataBuffer buffer = DataBuffer.instance();
					if (context.cipher.handshaked()) {
						RecordCoder2.encodeCiphertext(context.cipher, record, data, buffer);
					} else {
						RecordCoder2.encodePlaintext(record, data, buffer);
					}
					data.release();
					return buffer;
				}
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder2.encode((ChangeCipherSpec) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.APPLICATION_DATA) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder2.encode((ApplicationData) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.HEARTBEAT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder2.encode((HeartbeatMessage) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder2.encodeCiphertext(context.cipher, record, data, buffer);
				data.release();
				return buffer;
			} else if (record.contentType() == Record.ALERT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder2.encode((Alert) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				if (context.cipher.handshaked()) {
					RecordCoder2.encodeCiphertext(context.cipher, record, data, buffer);
				} else {
					RecordCoder2.encodePlaintext(record, data, buffer);
				}
				data.release();
				return buffer;
			} else {
				throw new UnsupportedOperationException("TLS:意外的记录类型" + record.contentType());
			}
		} else {
			// APPLICATION DATA
			final DataBuffer data = handler().encode(chain, message);
			final DataBuffer buffer = DataBuffer.instance();
			RecordCoder2.encodeCiphertext(context.cipher, ApplicationData.INSTANCE, data, buffer);
			data.release();
			return buffer;
		}
	}

	private void encode(TLSContext context, Handshake handshake, DataBuffer buffer) throws IOException {
		HandshakeCoder.encode(handshake, buffer);
		// 0-RTT
		// binder
		context.cipher.hash(buffer);
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record) {
			if (message instanceof Finished) {

			} else if (message instanceof Alert) {
				chain.close();
			} else {
				chain.receive();
			}
		} else {
			handler().sent(chain, message);
		}
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		final TLSContext context = chain.getContext();
		context.data.release();
		handler().disconnected(chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		handler().error(chain, e);
	}

	/**
	 * 握手
	 */
	private Record handshake(TLSContext context, Handshake record) throws Exception {
		if (record.msgType() == Handshake.CLIENT_HELLO) {
			final ClientHello hello = (ClientHello) record;

			// TLS Version
			// 如果有SupportedVersions则匹配此扩展中的版本
			// 反之由ClientHello.version确定版本

			if (hello.getVersion() <= TLS.SSL30) {
				// ClientHello,ServerHello
				// 已禁止0x0300或更低版本
				return new Alert(Alert.PROTOCOL_VERSION);
			}
			final short version;
			final SupportedVersions sv = hello.getExtension(Extension.SUPPORTED_VERSIONS);
			if (sv != null && sv.size() > 0) {
				version = sv.select(versions);
			} else {
				version = hello.matchVersion(versions);
			}
			if (version == 0) {
				// 未能匹配TLS版本
				return new Alert(Alert.PROTOCOL_VERSION);
			}

			// TLS 1.3
			if (version == TLS.V13) {
				// 压缩模式
				if (hello.hasCompressionMethods()) {
					if (hello.getCompressionMethods()[0] == 0) {
						// TLS 1.3 不支持任何压缩
					} else {
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}

				// 加密套件
				final short suite;
				if (hello.hasCipherSuites()) {
					suite = CipherSuite.match(version, hello.getCipherSuites());
					if (suite > 0) {
						context.cipher.suite(suite);
					} else {
						// 未能匹配加密套件
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
				} else {
					// 未指定加密套件
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}

				// server_hello
				final ServerHello server = new ServerHello();
				server.setSessionId(hello.getSessionId());
				server.setCipherSuite(suite);
				// selected_versions
				server.addExtension(sv);

				// 密钥交换
				final SupportedGroups groups = hello.getExtension(Extension.SUPPORTED_GROUPS);
				if (groups == null || groups.size() <= 0) {
					// 未指定密钥交换算法组
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}

				final PskKeyExchangeModes pskm = hello.getExtension(Extension.PSK_KEY_EXCHANGE_MODES);
				if (pskm != null) {
					if (pskm.has(PskKeyExchangeModes.PSK_DHE_KE)) {
						context.mode = PskKeyExchangeModes.PSK_DHE_KE;
					} else if (pskm.has(PskKeyExchangeModes.PSK_KE)) {
						context.mode = PskKeyExchangeModes.PSK_KE;
					} else {
						// 指定的密钥交换模式无效
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
				} else {
					// 未指定密钥交换模式
					// 服务端将不会发送NewSessionTicket
					context.mode = Byte.MIN_VALUE;
				}

				PreSharedKeySelected psk = null;
				final OfferedPsks psks = hello.getExtension(Extension.PRE_SHARED_KEY);
				if (psks != null) {
					if (pskm != null) {
						if (psks.size() > 0) {
							PskIdentity identity;
							NewSessionTicket ticket = null;
							for (int i = 0; i < psks.size(); i++) {
								identity = psks.get(i);
								ticket = ServerSessionTickets.get(identity);
								if (ticket != null) {
									if (ticket.valid()) {
										if (ticket.checkAgeAdd(identity.getTicketAge())) {
											identity.getBinder();
											// 0-RTT
											// pre_shared_key
											server.addExtension(psk = new PreSharedKeySelected(i));
											break;
										}
									}
								}
							}
							// 未匹配票据或票据过期
							// 不阻止此情形继续常规握手
						} else {
							// PSK扩展未提供有效票据
							// 不阻止此情形继续常规握手
						}
					} else {
						// 未指定密钥交换模式扩展
						// psk_key_exchange_modes+pre_shared_key扩展同时有效才能执行0-RTT握手
						return new Alert(Alert.HANDSHAKE_FAILURE);
					}
				}
				if (context.mode != 0) {
					final KeyShareClientHello shares = hello.getExtension(Extension.KEY_SHARE);
					if (shares != null) {
						if (shares.size() > 0) {
							int i;
							KeyShareEntry entry = null;
							for (i = 0; i < shares.size(); i++) {
								entry = shares.get(i);
								if (groups.check(entry.getGroup())) {
									if (NamedGroup.check(entry.getGroup(), namedGroups)) {
										break;
									} else {
										continue;
									}
								} else {
									// 共享密钥的算法不在支持组
									return new Alert(Alert.ILLEGAL_PARAMETER);
								}
							}
							if (entry != null && i < shares.size()) {
								// 1-RTT
								context.key.initialize(entry.getGroup());
								context.cipher.sharedKey(context.key.sharedKey(entry.getKeyExchange()));
								// key_share
								entry.setKeyExchange(context.key.publicKey());
								server.addExtension(new KeyShareServerHello(entry));
							} else {
								// 未能匹配已提供共享密钥的算法
								return new Alert(Alert.ILLEGAL_PARAMETER);
							}
						} else {
							final short group = groups.match(namedGroups);
							if (group > 0) {
								// Hello Retry Request
								// 请求客户端提供对应算法的密钥后重试
								server.setRandom(ServerHello.HELLO_RETRY_REQUEST_RANDOM);
								server.addExtension(new KeyShareHelloRetryRequest(group));
								server.addExtension(new Cookie());
								return server;
							} else {
								// 没有匹配的密钥算法
								return new Alert(Alert.HANDSHAKE_FAILURE);
							}
						}
					} else {
						// 未提供共享密钥扩展
						return new Alert(Alert.HANDSHAKE_FAILURE);
					}
				}

				final Handshakes records = new Handshakes();
				server.makeRandom(version);
				records.add(server);

				// encrypted_extensions

				final EncryptedExtensions extensions = new EncryptedExtensions();
				records.add(extensions);

				// 应用协议
				final ApplicationLayerProtocolNegotiation alpn = hello.getExtension(Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION);
				if (alpn != null) {
					if (alpn.select(protocols)) {
						extensions.addExtension(alpn);
					} else {
						// 没有匹配的应用协议
						return new Alert(Alert.NO_APPLICATION_PROTOCOL);
					}
				} else {
					// 默认
				}
				// 心跳指示
				extensions.addExtension(new Heartbeat(Heartbeat.PEER_ALLOWED_TO_SEND));

				// 0-RTT 时无须证书交换
				if (psk == null) {

					// 客户端证书
					final PostHandshakeAuth pha = hello.getExtension(Extension.POST_HANDSHAKE_AUTH);
					final CertificateRequest cr = new CertificateRequest();
					if (pha != null) {
						cr.setContext(new byte[32]);
						TLS.RANDOM.nextBytes(cr.getContext());
					}
					// signature_algorithms
					cr.addExtension(new SignatureAlgorithms(SignatureScheme.ALL));
					// certificate_authorities
					cr.addExtension(SessionCertificates.makeCASExtension());
					// oid_filters
					cr.addExtension(SessionCertificates.makeOIDFiltersExtension());

					// 服务端证书
					CPK cpk = null;
					final CertificateAuthorities cas = hello.getExtension(Extension.CERTIFICATE_AUTHORITIES);
					final ServerNames sns = hello.getExtension(Extension.SERVER_NAME);
					if (sns != null) {
						cpk = SessionCertificates.get(sns.get());
						if (cpk != null) {
							if (cas != null) {
								if (cpk.check(cas)) {
									// OK
								} else {
									// 主机名称对应的证书与CA不匹配
									return new Alert(Alert.UNKNOWN_CA);
								}
							} else {
								// 未指定CA时不执行检查
							}
						} else {
							// 主机名称没有对应的证书
							return new Alert(Alert.UNRECOGNIZED_NAME);
						}
					} else {
						if (cas != null) {
							for (CPK p : SessionCertificates.all()) {
								if (p.check(cas)) {
									cpk = p;
									break;
								}
							}
							if (cpk == null) {
								// 未能匹配指定CA证书
								return new Alert(Alert.UNKNOWN_CA);
							}
						} else {
							// 未提供主机名扩展
							return new Alert(Alert.MISSING_EXTENSION);
						}
					}

					final SignatureAlgorithms sas = hello.getExtension(Extension.SIGNATURE_ALGORITHMS);
					if (sas != null && sas.size() > 0) {
						// certificate
						final Certificate certificate = new Certificate();
						certificate.setCertificates(cpk.getEntries());
						certificate.setContext(null);
						records.add(certificate);

						// certificate_verify
						final CertificateVerify verify = new CertificateVerify();
						records.add(verify);
						verify.setAlgorithm(sas.match(cpk.getScheme()));
						if (verify.getAlgorithm() <= 0) {
							verify.setAlgorithm(SignatureScheme.match(sas.get()));
							if (verify.getAlgorithm() <= 0) {
								// 没有匹配的签名算法
								return new Alert(Alert.HANDSHAKE_FAILURE);
							}
						}
						// signaturer
						context.signaturer.scheme(verify.getAlgorithm());
						// 证书消息签名在编码时生成
					} else {
						// 未提供有效的签名算法扩展
						return new Alert(Alert.MISSING_EXTENSION);
					}
				}

				// finished
				final Finished finished = new Finished();
				// 握手消息完成签名在编码时生成
				records.add(finished);

				return records;
			} else
			// TLS 1.2
			if (version == TLS.V12) {
				// trusted_ca_keys
			}
			return new Alert(Alert.PROTOCOL_VERSION);
		} else

		if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.getVerifyData() == Finished.OK) {
				// cipher.decryptReset(cipher.serverApplicationTraffic());
				// cipher.clientApplicationTraffic();
				// cipher.exporterMaster();
				context.finished = true;
				if (context.mode >= 0) {
					// NewSessionTicket
					final Handshakes records = new Handshakes();
					records.add(ServerSessionTickets.make((byte) 0));
					records.add(ServerSessionTickets.make((byte) 1));
					return records;
				} else {
					return null;
				}
			} else {
				return new Alert(Alert.DECRYPT_ERROR);
			}
		} else {
			return new Alert(Alert.UNEXPECTED_MESSAGE);
		}
	}

	private void heartbeat(ChainChannel chain, HeartbeatMessage message) {
		if (message.getMessageType() == HeartbeatMessage.HEARTBEAT_REQUEST) {
			message.setMessageType(HeartbeatMessage.HEARTBEAT_RESPONSE);
			chain.send(message);
		}
	}

	private class TLSContext {
		final Signaturer signaturer;
		final CipherSuiter cipher;
		final KeyExchange key;
		boolean finished;
		byte mode;

		public TLSContext() {
			signaturer = new Signaturer();
			cipher = new CipherSuiter();
			key = new KeyExchange();
		}

		final DataBuffer data = DataBuffer.instance();
	}
}