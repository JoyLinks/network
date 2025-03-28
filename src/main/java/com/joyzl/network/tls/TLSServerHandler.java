package com.joyzl.network.tls;

import java.io.Closeable;
import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;
import com.joyzl.network.tls.KeyShare.KeyShareEntry;
import com.joyzl.network.tls.PreSharedKey.PskIdentity;
import com.joyzl.network.tls.SessionCertificates.LocalCache;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class TLSServerHandler extends TLSParameters implements ChainHandler {

	private final ChainHandler handler;

	public TLSServerHandler(ChainHandler handler) {
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
	public void connected(ChainChannel chain) throws Exception {
		chain.setContext(new TLSContext());
		chain.receive();
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
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		final int type;
		try {
			type = RecordCoder.v13Decode(context.cipher, buffer, context.data);
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
			Object message = handler.decode(chain, context.data);
			while (message != null && context.data.readable() > 0) {
				handler.received(chain, message);
				message = handler.decode(chain, context.data);
			}
			return message;
		} else if (type == Record.HANDSHAKE) {
			// 解码后可能会保留数据用于消息哈希
			// 因此context.length表示保留的数据
			Handshake handshake = decode(context, context.data);
			while (handshake != null && context.data.readable() > context.length) {
				received(chain, handshake);
				handshake = decode(context, context.data);
			}
			return handshake;
		} else if (type == Record.CHANGE_CIPHER_SPEC) {
			return RecordCoder.decodeChangeCipherSpec(context.data);
		} else if (type == Record.HEARTBEAT) {
			return RecordCoder.decodeHeartbeat(context.data);
		} else if (type == Record.ALERT) {
			return RecordCoder.decodeAlert(context.data);
		} else {
			return new TLSException(Alert.UNEXPECTED_MESSAGE);
		}
	}

	private Handshake decode(TLSContext context, DataBuffer buffer) throws Exception {
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
		if (type == Handshake.CLIENT_HELLO) {
			// 握手消息应保留数据用于TranscriptHash
			buffer.mark();
			final Handshake handshake = V3HandshakeCoder.decodeV3(buffer);
			// 待协商后执行消息哈希
			context.length = length;
			buffer.reset();
			return handshake;
		} else if (type == Handshake.FINISHED) {
			// 客户端完成消息校验码
			final byte[] local = context.cipher.clientFinished();
			context.cipher.hash(buffer, length);

			final Finished finished = (Finished) V3HandshakeCoder.decodeV3(buffer);
			finished.setLocalData(local);
			return finished;
		} else if (type == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			return V3HandshakeCoder.decodeV3(buffer);
		} else {
			context.cipher.hash(buffer, length);
			return V3HandshakeCoder.decodeV3(buffer);
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
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
							RecordCoder.encodePlaintext(record, data, buffer);
							RecordCoder.encodeV0(ChangeCipherSpec.INSTANCE_V12, buffer);
						} else {
							RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
						}
					}
					data.release();
					return buffer;
				} else {
					final DataBuffer data = DataBuffer.instance();
					encode(context, (Handshake) record, data);
					final DataBuffer buffer = DataBuffer.instance();
					if (context.cipher.isHandshaked()) {
						RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
					} else {
						RecordCoder.encodePlaintext(record, data, buffer);
					}
					data.release();
					return buffer;
				}
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeV0((ChangeCipherSpec) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.APPLICATION_DATA) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeV0((ApplicationData) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.HEARTBEAT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder.encode((HeartbeatMessage) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
				data.release();
				return buffer;
			} else if (record.contentType() == Record.ALERT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder.encode((Alert) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				if (context.cipher.isHandshaked()) {
					RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
				} else {
					RecordCoder.encodePlaintext(record, data, buffer);
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
			RecordCoder.encodeCiphertext(context.cipher, ApplicationData.INSTANCE, data, buffer);
			data.release();
			return buffer;
		}
	}

	private void encode(TLSContext context, Handshake handshake, DataBuffer buffer) throws Exception {
		if (handshake.msgType() == Handshake.FINISHED) {
			// 握手完成消息编码之前构造验证码
			final Finished finished = (Finished) handshake;
			finished.setVerifyData(context.cipher.serverFinished());
		} else if (handshake.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			// 这是最先加密的握手消息
			// 导出密钥并重置握手加密和解密套件
			context.cipher.encryptReset(context.cipher.serverHandshakeTrafficSecret());
			// 因早期数据还须解密，此时不能重置解密套件
			context.cipher.clientHandshakeTrafficSecret();
		} else if (handshake.msgType() == Handshake.CERTIFICATE_VERIFY) {
			// 生成服务端证书消息签名
			final CertificateVerifyV3 verify = (CertificateVerifyV3) handshake;
			verify.setSignature(context.signaturer.singServer(context.cipher.hash()));
		} else if (handshake.msgType() == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			V3HandshakeCoder.encodeV3(handshake, buffer);
			return;
		}
		V3HandshakeCoder.encodeV3(handshake, buffer);
		context.cipher.hash(buffer);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		if (message == null) {
			if (context.cipher.isApplication()) {
				handler.received(chain, message);
			} else {
				chain.close();
			}
		} else if (message instanceof Record) {
			System.out.println(message);
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
			if (context.cipher.decryptLimit()) {
				chain.send(new KeyUpdate());
			}
			handler.received(chain, message);
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		if (message instanceof Record) {
			System.out.println(message);
			if (message instanceof Alert) {
				chain.close();
				return;
			} else if (message instanceof Handshakes handshakes) {
				if (handshakes.last().msgType() == Handshake.FINISHED) {
					if (context.cipher.isHandshaked()) {
						// 重置握手数据解密套件
						context.cipher.decryptReset(context.cipher.clientHandshakeTrafficSecret());
						// 导出应用数据加密密钥并重置加密套件
						context.cipher.encryptReset(context.cipher.serverApplicationTrafficSecret());
						// 应用数据解密密钥导出，等待客户端完成消息后重置解密套件
						context.cipher.clientApplicationTrafficSecret();
						// context.cipher.exporterMaster();
						handler.connected(chain);
					}
				}
			} else if (message instanceof KeyUpdate) {
				final KeyUpdate update = (KeyUpdate) message;
				if (update.get() == KeyUpdate.UPDATE_NOT_REQUESTED) {
					// 响应密钥更新，重置加解密套件
					context.cipher.encryptReset(context.cipher.serverApplicationTrafficSecret());
					context.cipher.decryptReset(context.cipher.clientApplicationTrafficSecret());
				}
			}
		} else {
			if (context.cipher.encryptLimit()) {
				chain.send(new KeyUpdate());
			}
			handler.sent(chain, message);
		}
	}

	/**
	 * 握手，本实现所有握手消息(Handshake)继承自记录层(Record)；<br>
	 * 收到的握手消息即便在记录层已合并，在解码后也逐消息调用此方法；
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
			final SupportedVersions sv = hello.getExtension(Extension.SUPPORTED_VERSIONS);
			if (sv != null && sv.size() > 0) {
				context.version = sv.match(versions());
			} else {
				context.version = hello.matchVersion(versions());
			}
			if (context.version == 0) {
				// 未能匹配TLS版本
				return new Alert(Alert.PROTOCOL_VERSION);
			}

			// TLS 1.3
			if (context.version == TLS.V13) {
				// 严禁重协商
				if (context.cipher.isHandshaked()) {
					return new Alert(Alert.UNEXPECTED_MESSAGE);
				}
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
					suite = CipherSuite.match(context.version, hello.getCipherSuites());
					if (suite > 0) {
						// 设置加密套件
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
				server.addExtension(new SelectedVersion(context.version));

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
					// 这是个特殊标记以避开PSK_DHE_KE和PSK_KE
					context.mode = Byte.MIN_VALUE;
				}

				PreSharedKeySelected psk = null;
				final EarlyDataIndication earlyData = hello.getExtension(Extension.EARLY_DATA);
				final PreSharedKeys psks = hello.getExtension(Extension.PRE_SHARED_KEY);
				if (psks != null) {
					if (psks != hello.lastExtension()) {
						// pre_shared_key 扩展必须位于最后
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
					if (pskm == null) {
						// 未指定密钥交换模式扩展
						// psk_key_exchange_modes,pre_shared_key
						// 以上两个扩展同时有效才能执行0-RTT握手
						return new Alert(Alert.HANDSHAKE_FAILURE);
					}
					if (psks.size() > 0) {
						int i = 0;
						PskIdentity identity = null;
						NewSessionTicket2 ticket = null;
						// 1 筛选有效票据
						for (; i < psks.size(); i++) {
							identity = psks.get(i);
							ticket = ServerSessionTickets.get(identity);
							if (ticket != null) {
								if (ticket.valid()) {
									if (ticket.checkAgeAdd(identity.getTicketAge())) {
										if (ticket.getSuite() == suite) {
											// 未尝试匹配密码套件与HASH算法的兼容性
											// 本实现执行严格的匹配
											break;
										}
									} else {
										ticket = null;
									}
								}
							}
						}
						if (ticket != null) {
							// 2 验证BinderKey
							// Transcript-Hash(Truncate(ClientHello1))
							// binders Length uint16 Short 2Byte
							int length = psks.bindersLength() + 2;
							length = context.length - length;
							context.cipher.suite(ticket.getSuite());
							context.cipher.reset(ticket.getResumption());
							context.cipher.hash(context.data, length);
							// 丢弃已计算的数据
							context.data.skipBytes(length);
							length = context.length -= length;
							if (identity.check(context.cipher.resumptionBinderKey())) {
								// 3 0-RTT
								// 补充消息哈希，导出早期流量密钥
								context.cipher.hash(context.data, length);
								context.data.skipBytes(length);
								context.length = 0;
								if (earlyData != null) {
									context.cipher.decryptReset(context.cipher.clientEarlyTrafficSecret());
									// context.cipher.earlyExporterMaster();
								} else {
									// 未有早期数据
								}
								// pre_shared_key selected
								server.addExtension(psk = new PreSharedKeySelected(i));
							} else {
								// 补充消息哈希
								context.cipher.hash(context.data, length);
								context.data.skipBytes(length);
								context.length = 0;
								// 票据验证失败
								// 不阻止此情形继续常规握手
							}
						}
						// 未匹配票据或票据过期
						// 不阻止此情形继续常规握手
					} else {
						// 扩展未能提供任何票据
						// 不阻止此情形继续常规握手
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
									if (NamedGroup.check(entry.getGroup(), namedGroups())) {
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

								// 设置密钥算法
								context.key.initialize(entry.getGroup());
								// 设置对端共享密钥（公钥）
								context.cipher.sharedKey(context.key.sharedKey(entry.getKeyExchange()));
								// TranscriptHash:client_hello
								if (context.length > 0) {
									context.cipher.hash(context.data, context.length);
									context.data.skipBytes(context.length);
								}
								// key_share
								entry.setKeyExchange(context.key.publicKey());
								server.addExtension(new KeyShareServerHello(entry));
							} else {
								// 未能匹配已提供共享密钥的算法
								return new Alert(Alert.ILLEGAL_PARAMETER);
							}
						} else {
							final short group = groups.match(namedGroups());
							if (group > 0) {
								// Hello Retry Request
								// 请求客户端提供对应算法的密钥后重试
								server.makeHelloRetryRequest();
								server.addExtension(new KeyShareHelloRetryRequest(group));
								server.addExtension(new Cookie(context.cipher.hash()));
								context.cipher.retry();
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
				server.makeRandom(context.version);
				records.add(server);

				// encrypted_extensions

				final EncryptedExtensions extensions = new EncryptedExtensions();
				records.add(extensions);

				// 应用协议
				final ApplicationLayerProtocolNegotiation alpn = hello.getExtension(Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION);
				if (alpn != null) {
					if (alpn.select(alpns())) {
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
					final CertificateRequestV3 cr = new CertificateRequestV3();
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
					LocalCache cpk = null;
					final CertificateAuthorities cas = hello.getExtension(Extension.CERTIFICATE_AUTHORITIES);
					final ServerNames sns = hello.getExtension(Extension.SERVER_NAME);
					if (sns != null) {
						cpk = SessionCertificates.getLocal(sns);
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
							for (LocalCache p : SessionCertificates.allLocals()) {
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
						final CertificateV3 certificate = new CertificateV3();
						certificate.set(cpk.getEntries());
						certificate.setContext(null);
						records.add(certificate);

						// certificate_verify
						final CertificateVerifyV3 verify = new CertificateVerifyV3();
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
						context.signaturer.setPrivateKey(cpk.getPrivateKey());
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
			if (context.version == TLS.V12) {
				// trusted_ca_keys
				// CertificateStatusRequest

				final RenegotiationInfo ri = hello.getExtension(Extension.RENEGOTIATION_INFO);
				if (ri != null) {
					if (ri.isEmpty() || ri.isSCSV()) {
						// OK
					} else {
						return new Alert(Alert.HANDSHAKE_FAILURE);
					}
				}
			}
			return new Alert(Alert.PROTOCOL_VERSION);
		} else

		if (record.msgType() == Handshake.END_OF_EARLY_DATA) {
			// ClientHello + PSK -> decryptReset(client_early_traffic_secret)
			// Early Application Data - EndOfEarlyData
			context.cipher.decryptReset(context.cipher.clientHandshakeTrafficSecret());
			return null;
		} else

		if (record.msgType() == Handshake.KEY_UPDATE) {
			final KeyUpdate update = (KeyUpdate) record;
			if (update.get() == KeyUpdate.UPDATE_REQUESTED) {
				// 来自对方请求，发送后重置密钥
				update.set(KeyUpdate.UPDATE_NOT_REQUESTED);
				context.cipher.nextApplicationTrafficSecret();
				return update;
			} else if (update.get() == KeyUpdate.UPDATE_NOT_REQUESTED) {
				// 来自对方响应，立即重置密钥
				context.cipher.nextApplicationTrafficSecret();
				context.cipher.encryptReset(context.cipher.serverApplicationTrafficSecret());
				context.cipher.decryptReset(context.cipher.clientApplicationTrafficSecret());
				return null;
			} else {
				return new Alert(Alert.ILLEGAL_PARAMETER);
			}
		} else

		if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.validate()) {
				// 客户端握手完成
				// 重置应用数据解密套件
				context.cipher.decryptReset(context.cipher.clientApplicationTrafficSecret());
				context.cipher.resumptionMasterSecret();
				if (context.mode >= 0) {
					// NewSessionTicket
					final Handshakes records = new Handshakes();
					final NewSessionTicket2 ticket1 = ServerSessionTickets.make((byte) 0);
					final NewSessionTicket2 ticket2 = ServerSessionTickets.make((byte) 1);
					ticket1.setResumption(context.cipher.resumptionSecret(ticket1.getNonce()));
					ticket2.setResumption(context.cipher.resumptionSecret(ticket2.getNonce()));
					ticket1.setSuite(context.cipher.suite());
					ticket2.setSuite(context.cipher.suite());
					ticket1.setGroup(context.key.group());
					ticket2.setGroup(context.key.group());
					records.add(ticket1);
					records.add(ticket2);
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
		// 服务端仅响应心跳检查
		if (message.getMessageType() == HeartbeatMessage.HEARTBEAT_REQUEST) {
			message.setMessageType(HeartbeatMessage.HEARTBEAT_RESPONSE);
			chain.send(message);
		} else {
			chain.send(new Alert(Alert.UNEXPECTED_MESSAGE));
		}
	}

	private class TLSContext implements Closeable {
		final Signaturer signaturer;
		final CipherSuiter cipher;
		final V3KeyExchange key;
		short version;
		byte mode;

		private byte[] clientVerifyData, serverVerifyData;
		private byte[] sessionId;

		public TLSContext() {
			signaturer = new Signaturer();
			cipher = new CipherSuiter();
			key = new V3KeyExchange();
		}

		final DataBuffer data = DataBuffer.instance();
		int length = 0;

		@Override
		public void close() throws IOException {
			data.release();
		}
	}
}