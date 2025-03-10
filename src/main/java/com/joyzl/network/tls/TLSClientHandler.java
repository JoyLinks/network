package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;
import com.joyzl.network.tls.KeyShare.KeyShareEntry;
import com.joyzl.network.tls.PreSharedKey.PskIdentity;
import com.joyzl.network.tls.SessionCertificates.LocalCache;
import com.joyzl.network.tls.SessionCertificates.RemoteCache;

/**
 * TLSClientHandler
 * 
 * @author ZhangXi 2025年3月10日
 */
public class TLSClientHandler implements ChainHandler {

	private final ChainHandler handler;

	private final CipherSuiter cipher = new CipherSuiter();
	private Signaturer signaturer;
	private KeyExchange key;
	private String sn;

	private final DataBuffer data = DataBuffer.instance();

	public TLSClientHandler(ChainHandler handler) {
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
		final ServerName name = new ServerName(chain.getRemoteAddress());
		sn = name.getNameString();

		// 构建握手请求消息
		final ClientHello hello = new ClientHello();
		hello.setVersion(TLS.V12);
		hello.makeSessionId();
		hello.makeRandom();
		hello.setCipherSuites(CipherSuite.V13);
		hello.setCompressionMethods(TLS.COMPRESSION_METHODS);

		// Extensions

		hello.addExtension(new ServerNames(name));
		hello.addExtension(new SupportedVersions(TLS.ALL_VERSIONS));
		hello.addExtension(new SignatureAlgorithms(SignatureAlgorithms.ALL));
		hello.addExtension(new Heartbeat(Heartbeat.PEER_NOT_ALLOWED_TO_SEND));
		hello.addExtension(new ApplicationLayerProtocolNegotiation("http/1.1"));

		// TLS 1.2
		hello.addExtension(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		hello.addExtension(new ExtendedMasterSecret());
		hello.addExtension(new RenegotiationInfo());
		hello.addExtension(new SessionTicket());

		// Key Exchange
		hello.addExtension(new PskKeyExchangeModes(PskKeyExchangeModes.ALL));
		hello.addExtension(new SupportedGroups(NamedGroup.ALL));

		key = new KeyExchange(NamedGroup.X25519);
		hello.addExtension(new KeyShareClientHello(new KeyShareEntry(NamedGroup.X25519, key.publicKey())));
		cipher.suite(CipherSuite.TLS_AES_128_GCM_SHA256);

		// 获取缓存的PSK
		final NewSessionTicket ticket = ClientSessionTickets.get(sn, CipherSuite.TLS_AES_128_GCM_SHA256);
		if (ticket != null) {
			// 0-RTT PSK
			// 注意此扩展必须位于最后
			final PreSharedKeys psk = new PreSharedKeys();
			final PskIdentity pskIdentity = new PskIdentity();
			pskIdentity.setTicketAge(ticket.obfuscatedAgeAdd());
			pskIdentity.setIdentity(ticket.getTicket());
			psk.add(pskIdentity);
			hello.addExtension(psk);
			psk.setHashLength(cipher.hashLength());
			cipher.reset(ticket.getResumption());

			// early_data
		}

		chain.send(hello);
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final int type;
		try {
			type = RecordCoder.decode(cipher, buffer, data);
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
			return RecordCoder.decodeChangeCipherSpec(data);
		} else if (type == Record.HEARTBEAT) {
			return RecordCoder.decodeHeartbeat(data);
		} else if (type == Record.ALERT) {
			return RecordCoder.decodeAlert(data);
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
						if (cipher.handshaked()) {
							RecordCoder.encodeCiphertext(cipher, record, data, buffer);
						} else {
							RecordCoder.encodePlaintext(record, data, buffer);
						}
					}
					data.release();
					return buffer;
				} else {
					final DataBuffer data = DataBuffer.instance();
					encode((Handshake) record, data);
					final DataBuffer buffer = DataBuffer.instance();
					if (cipher.handshaked()) {
						RecordCoder.encodeCiphertext(cipher, record, data, buffer);
					} else {
						RecordCoder.encodePlaintext(record, data, buffer);
					}
					data.release();
					return buffer;
				}
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encode((ChangeCipherSpec) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.APPLICATION_DATA) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encode((ApplicationData) record, buffer);
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
				if (cipher.handshaked()) {
					RecordCoder.encodeCiphertext(cipher, record, data, buffer);
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
		if (type == Handshake.FINISHED) {
			// 服务端完成消息校验码
			final byte[] local = cipher.serverFinished();
			cipher.hash(buffer, length);

			final Finished finished = (Finished) HandshakeCoder.decode(buffer);
			finished.setLocalData(local);
			return finished;
		} else {
			cipher.hash(buffer, length);
			return HandshakeCoder.decode(buffer);
		}
	}

	private void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		if (handshake.msgType() == Handshake.CLIENT_HELLO) {
			final ClientHello hello = (ClientHello) handshake;
			HandshakeCoder.encode(handshake, buffer);
			if (hello.hasExtensions() && hello.lastExtension() instanceof PreSharedKeys) {
				// 0-RTT BinderKey
				final PreSharedKeys psks = (PreSharedKeys) hello.lastExtension();

				// 1移除填充的零值
				// Transcript-Hash(Truncate(ClientHello1))
				// binders Length uint16 Short 2Byte
				buffer.backSkip(psks.bindersLength() + 2);
				cipher.hash(buffer);

				// 2计算BinderKey
				PskIdentity identity;
				for (int index = 0; index < psks.size(); index++) {
					identity = psks.get(index);
					identity.setBinder(cipher.resumptionBinderKey());
				}
				ExtensionCoder.encodeBinders(psks, buffer);

				// 3重置消息哈希以包含BinderKey部分
				cipher.hashReset();
			}
		} else if (handshake.msgType() == Handshake.CERTIFICATE_VERIFY) {
			// 生成客户端证书消息签名
			final CertificateVerify verify = (CertificateVerify) handshake;
			verify.setSignature(signaturer.singClient(cipher.hash()));
			HandshakeCoder.encode(handshake, buffer);
		} else {
			HandshakeCoder.encode(handshake, buffer);
		}
		cipher.hash(buffer);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (message == null) {
			if (cipher.application()) {
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
				message = handshake((Handshake) record);
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
			if (cipher.decryptLimit()) {
				chain.send(new KeyUpdate());
			}
			handler.received(chain, message);
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record) {
			System.out.println(message);
			if (message instanceof Alert) {
				chain.close();
			} else if (message instanceof Finished) {
				if (cipher.handshaked()) {
					// 重置为应用加密套件
					cipher.encryptReset(cipher.clientApplicationTraffic());
					// cipher.resumptionMaster();
					handler.connected(chain);
				}
			} else if (message instanceof KeyUpdate) {
				final KeyUpdate update = (KeyUpdate) message;
				if (update.get() == KeyUpdate.UPDATE_NOT_REQUESTED) {
					// 响应密钥更新，重置加解密套件
					cipher.encryptReset(cipher.serverApplicationTraffic());
					cipher.decryptReset(cipher.clientApplicationTraffic());
				}
				chain.receive();
			} else {
				chain.receive();
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
			if (hello.isHelloRetryRequest()) {
				// 只有支持1.3的服务端才会发送此消息

				if (hello.getCompressionMethod() != 0) {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				if (hello.hasSessionId()) {
					// TODO
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				if (hello.getCipherSuite() > 0) {
					cipher.suite(hello.getCipherSuite());
					// TODO
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}

				// 确认版本
				if (hello.getVersion() != TLS.V12) {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				final SelectedVersion version = hello.getExtension(Extension.SUPPORTED_VERSIONS);
				if (version == null) {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
				if (version.get() == TLS.V13) {
					final ClientHello retry = new ClientHello();
					final KeyShareHelloRetryRequest ksr = hello.getExtension(Extension.KEY_SHARE);
					if (ksr != null) {
						// 生成新的共享密钥
						key.initialize(ksr.getSelectedGroup());
						retry.addExtension(new PskKeyExchangeModes(PskKeyExchangeModes.ALL));
						retry.addExtension(new SupportedGroups(ksr.getSelectedGroup()));
						retry.addExtension(new KeyShareClientHello(new KeyShareEntry(key.group(), key.publicKey())));
					} else {
						return new Alert(Alert.ILLEGAL_PARAMETER);
					}
					final Cookie cookie = hello.getExtension(Extension.COOKIE);
					if (cookie != null) {
						// Cookie由Server发送并原样送回
						retry.addExtension(cookie);
					}
					return retry;
				} else if (version.get() == TLS.V12) {
					// DOWNGRD
					// TODO
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
			} else {
				final short version;
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
				if (version == TLS.V13) {
					final EarlyDataIndication ed = hello.getExtension(Extension.EARLY_DATA);
					final PreSharedKeySelected psk = hello.getExtension(Extension.PRE_SHARED_KEY);
					if (psk != null) {
						if (ed != null) {
							if (psk.getSelected() != 0) {
								// 早期数据时PSK必须选择首个
								// 否则加密的的早期数据如何解密呢
								return new Alert(Alert.ILLEGAL_PARAMETER);
							}
						} else {
							// 已选择0-RTT握手
						}
					} else {
						if (ed != null) {
							// 未选择PSK时不应有早期数据扩展
							return new Alert(Alert.ILLEGAL_PARAMETER);
						}
					}

					final KeyShareServerHello share = hello.getExtension(Extension.KEY_SHARE);
					if (share != null) {
						if (share.getServerShare() != null) {
							if (share.getServerShare().getGroup() == key.group()) {
								cipher.sharedKey(key.sharedKey(share.getServerShare().getKeyExchange()));
								cipher.encryptReset(cipher.clientHandshakeTraffic());
								cipher.decryptReset(cipher.serverHandshakeTraffic());
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
			}
		} else if (record.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			final EncryptedExtensions extensions = (EncryptedExtensions) record;
			final ApplicationLayerProtocolNegotiation alpn = extensions.getExtension(Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION);
			if (alpn != null) {

			}
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
				final CertificateVerify certificateVerify = new CertificateVerify();
				certificateVerify.setAlgorithm(local.getScheme());
				certificate.set(local.getEntries());
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
				if (signaturer == null) {
					try {
						SessionCertificates.check(remote.getCertificates());
					} catch (Exception e) {
						return new Alert(Alert.UNSUPPORTED_CERTIFICATE);
					}
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
		} else if (record.msgType() == Handshake.NEW_SESSION_TICKET) {
			final NewSessionTicket ticket = (NewSessionTicket) record;
			ticket.setResumption(cipher.resumption(ticket.getNonce()));
			ClientSessionTickets.put(sn, cipher.suite(), ticket);
		} else if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.validate()) {
				cipher.decryptReset(cipher.serverApplicationTraffic());
				cipher.clientApplicationTraffic();
				cipher.exporterMaster();
				// EndOfEarlyData
				// chain.send(EndOfEarlyData.INSTANCE);
				finished.setVerifyData(cipher.clientFinished());
				return finished;
			} else {
				return new Alert(Alert.DECRYPT_ERROR);
			}
		}
		return null;
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		final HeartbeatMessage heartbeat = new HeartbeatMessage();
		heartbeat.setMessageType(HeartbeatMessage.HEARTBEAT_REQUEST);
		heartbeat.setPayload(Binary.split(chain.hashCode()));
		chain.send(heartbeat);
	};

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