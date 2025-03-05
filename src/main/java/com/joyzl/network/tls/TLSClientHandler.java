package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.tls.SessionCertificates.CPK;

/**
 * TLSClientHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class TLSClientHandler extends RecordHandler {

	private final ChainHandler handler;

	private Signaturer signaturer;
	private CipherSuiter cipher;
	private KeyExchange key;

	private long time;
	private String sni;

	public TLSClientHandler(ChainHandler handler) {
		this.handler = handler;
	}

	@Override
	protected ChainHandler handler() {
		return handler;
	}

	@Override
	protected boolean handshaked() {
		return cipher.handshaked();
	}

	@Override
	protected DataBuffer decrypt(DataBuffer buffer, int length) throws Exception {
		final DataBuffer data = DataBuffer.instance();
		if (buffer.readable() == length) {
			cipher.decryptAdditional(length);
			cipher.decryptFinal(buffer, data);
		} else {
			cipher.decryptAdditional(length);
			cipher.decryptFinal(buffer, data, length);
		}
		return data;
	}

	@Override
	protected DataBuffer encrypt(DataBuffer data) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		cipher.encryptAdditional(data.readable() + cipher.tagLength());
		cipher.encryptFinal(data, buffer);
		data.release();
		return buffer;
	}

	@Override
	protected void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		HandshakeCoder.encode(handshake, buffer);
		if (handshake.msgType() == Handshake.CLIENT_HELLO) {
			final ClientHello clientHello = (ClientHello) handshake;
			final Extension extension = clientHello.lastExtension();
			if (extension != null && extension instanceof OfferedPsks) {
				// 0-RTT BinderKey
				final OfferedPsks offeredPsks = (OfferedPsks) extension;
				// Transcript-Hash(Truncate(ClientHello1))
				// 2 = uint16 Short Length
				buffer.backSkip(offeredPsks.bindersLength() + 2);
				cipher.hash(buffer);

				// PskBinderEntry
				PskIdentity identity;
				for (int index = 0; index < offeredPsks.size(); index++) {
					identity = offeredPsks.get(index);
					identity.setBinder(cipher.resumptionBinderKey());
				}
				ExtensionCoder.encodeBinders(offeredPsks, buffer);

				// 重新计算消息哈希以包含BinderKey部分
				// TODO hash(buffer,offset,length);
				cipher.hashReset();
				cipher.hash(buffer);
			} else {
				cipher.hash(buffer);
			}
		} else {
			cipher.hash(buffer);
		}
	}

	@Override
	protected Handshake decode(DataBuffer buffer) throws Exception {
		buffer.mark();
		final int type = buffer.readByte();
		final int length = buffer.readUnsignedMedium();
		buffer.reset();

		if (type == Handshake.FINISHED) {
			// 校验服务端的完成哈希
			// 如果本地校验与服务端相同则置为OK

			final byte[] hash = cipher.serverFinished();
			cipher.hash(buffer, length + 4);

			final Finished finished = (Finished) HandshakeCoder.decode(buffer);
			if (Arrays.equals(hash, finished.getVerifyData())) {
				finished.setVerifyData(Finished.OK);
			}
			return finished;
		} else {
			cipher.hash(buffer, length + 4);
			return HandshakeCoder.decode(buffer);
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record) {
			System.out.println(message);
			if (message instanceof Finished) {
				cipher.encryptReset(cipher.clientTraffic());
				cipher.resumptionMaster();

				time = System.currentTimeMillis() - time;
				System.out.println("握手完成，耗时：" + time);

				handler().connected(chain);
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
	protected void received(ChainChannel chain, Handshake message) throws Exception {
		System.out.print('\t');
		System.out.println(message);

		if (message.msgType() == Handshake.SERVER_HELLO) {
			final ServerHello serverHello = (ServerHello) message;
			if (serverHello.isHelloRetryRequest()) {
				System.out.println("HelloRetryRequest");
				// Cookie 由Server发送并原样送回
			} else {
				if (serverHello.getVersion() == TLS.V12) {
					for (Extension extension : serverHello.getExtensions()) {
						System.out.print('\t');
						System.out.print('\t');
						System.out.println(extension);
						if (extension.type() == Extension.SUPPORTED_VERSIONS) {
							final SupportedVersions supportedVersions = (SupportedVersions) extension;
							if (supportedVersions.size() == 1) {
								if (supportedVersions.get(0) == TLS.V13) {
									// OK 1.3
									continue;
								} else {
									chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
									return;
								}
							} else {
								chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
								return;
							}
						} else if (extension.type() == Extension.PRE_SHARED_KEY) {
							final PreSharedKeySelected preSharedKeySelected = (PreSharedKeySelected) extension;
							if (preSharedKeySelected.getSelected() != 0) {
								chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
								return;
							}
						} else if (extension.type() == Extension.KEY_SHARE) {
							final KeyShareServerHello keyShare = (KeyShareServerHello) extension;
							if (keyShare.getServerShare().getGroup() == key.group()) {
								cipher.sharedKey(key.sharedKey(keyShare.getServerShare().getKeyExchange()));
							} else {
								chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
								return;
							}
						}
					}
					if (serverHello.getCipherSuite() == cipher.suite()) {
						cipher.encryptReset(cipher.clientHandshakeTraffic());
						cipher.decryptReset(cipher.serverHandshakeTraffic());
					} else {

					}
				}

			}
		} else if (message.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			final EncryptedExtensions extensions = (EncryptedExtensions) message;
			for (Extension extension : extensions.getExtensions()) {
				System.out.print('\t');
				System.out.print('\t');
				System.out.println(extension);

				if (extension.type() == Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
					final ApplicationLayerProtocolNegotiation alpn = (ApplicationLayerProtocolNegotiation) extension;

				}
			}
		} else if (message.msgType() == Handshake.CERTIFICATE_REQUEST) {
			final CertificateRequest certificateRequest = (com.joyzl.network.tls.CertificateRequest) message;
			// OID Filters
			// signature_algorithms
			// certificate_authorities
			final Certificate certificate = new Certificate();
			certificate.setContext(certificateRequest.getContext());
			final CPK entry = SessionCertificates.get(sni);
			if (entry != null) {
				certificate.setCertificates(entry.getEntries());
				final CertificateVerify certificateVerify = new CertificateVerify();
				certificateVerify.setAlgorithm(entry.getScheme());
			}
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			final Certificate certificate = (Certificate) message;
			if (certificate.size() > 0) {
				CertificateEntry entry;
				for (int index = 0; index < certificate.size(); index++) {
					entry = certificate.get(index);
					for (Extension extension : entry.getExtensions()) {
						System.out.print('\t');
						System.out.print('\t');
						System.out.println(extension);
					}
				}

				// OCSP
				// SignedCertificateTimestamp
				// MD5/SHA-1 bad_certificate

				signaturer = new Signaturer();
				signaturer.setEntries(certificate.getCertificates());
				signaturer.setHash(cipher.hash());
			} else {
				// 不允许服务端发送空的证书消息
				chain.send(new Alert(Alert.DECODE_ERROR));
			}
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			final CertificateVerify certificateVerify = (CertificateVerify) message;
			signaturer.scheme(certificateVerify.getAlgorithm());
			if (signaturer.verifyServer(certificateVerify.getSignature())) {
				// OK
			} else {
				// 证书消息签名验证失败
				chain.send(new Alert(Alert.DECRYPT_ERROR));
			}
		} else if (message.msgType() == Handshake.NEW_SESSION_TICKET) {
			final NewSessionTicket newSessionTicket = (NewSessionTicket) message;
			newSessionTicket.setNonce(cipher.resumption(newSessionTicket.getNonce()));
			ClientSessionTickets.put(sni, key.group(), cipher.suite(), newSessionTicket);
			chain.receive();
		} else if (message.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) message;
			if (finished.getVerifyData() == Finished.OK) {
				cipher.decryptReset(cipher.serverApplicationTraffic());
				cipher.clientApplicationTraffic();
				cipher.exporterMaster();
				// EndOfEarlyData
				// chain.send(EndOfEarlyData.INSTANCE);
				// ClientFinished
				finished.setVerifyData(cipher.clientFinished());
				chain.send(finished);
			} else {
				chain.send(new Alert(Alert.DECRYPT_ERROR));
			}
		}
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		time = System.currentTimeMillis();
		sni = ServerName.findServerName(chain.getRemoteAddress());

		final ClientHello hello = new ClientHello();
		hello.setVersion(TLS.V12);
		hello.makeSessionId();
		hello.makeRandom();

		// AEAD HKDF
		hello.setCipherSuites(CipherSuite.V13);
		hello.setCompressionMethods(CompressionMethod.METHODS);
		// Extensions
		hello.addExtension(new ServerNames(new ServerName(sni)));
		hello.addExtension(new StatusRequest(new OCSPStatusRequest()));
		hello.addExtension(new SignatureAlgorithms(SignatureAlgorithms.ALL));
		hello.addExtension(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		hello.addExtension(new SessionTicket());
		// hello.addExtension(new ApplicationLayerProtocolNegotiation(//
		// ApplicationLayerProtocolNegotiation.H2, //
		// ApplicationLayerProtocolNegotiation.HTTP_1_1));
		// hello.addExtension(new
		// ApplicationSettings(ApplicationSettings.HTTP_1_1));
		hello.addExtension(new SupportedVersions(TLS.ALL_VERSIONS));
		hello.addExtension(new ExtendedMasterSecret());
		hello.addExtension(new RenegotiationInfo());
		hello.addExtension(new SignedCertificateTimestamp());
		hello.addExtension(new CompressCertificate(CompressCertificate.BROTLI));
		hello.addExtension(new Heartbeat(Heartbeat.PEER_NOT_ALLOWED_TO_SEND));

		// Certificate Extensions
		hello.addExtension(PostHandshakeAuth.INSTANCE);

		// Key Exchange Extensions
		key = new KeyExchange(NamedGroup.X25519);
		hello.addExtension(new SupportedGroups(NamedGroup.ALL));
		hello.addExtension(new KeyShareClientHello(new KeyShareEntry(NamedGroup.X25519, key.publicKey())));
		hello.addExtension(new PskKeyExchangeModes(PskKeyExchangeModes.ALL));
		// 获取缓存的PSK
		final NewSessionTicket ticket = ClientSessionTickets.get(sni, NamedGroup.X25519, CipherSuite.TLS_AES_128_GCM_SHA256);
		if (ticket != null) {
			// early_data

			// 0-RTT PSK
			final OfferedPsks psk = new OfferedPsks();
			final PskIdentity pskIdentity = new PskIdentity();
			pskIdentity.setTicketAge(ticket.obfuscatedAgeAdd());
			pskIdentity.setIdentity(ticket.getTicket());
			psk.add(pskIdentity);
			hello.addExtension(psk);
			psk.setHashLength(cipher.hashLength());
			cipher.reset(ticket.getNonce());
		} else {
			// 1-RTT KEY SHARE
			cipher = new CipherSuiter(CipherSuite.TLS_AES_128_GCM_SHA256);
		}

		chain.send(hello);
	}
}