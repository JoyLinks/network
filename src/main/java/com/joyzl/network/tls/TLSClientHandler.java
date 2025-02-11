package com.joyzl.network.tls;

import java.security.SecureRandom;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public class TLSClientHandler implements ChainHandler<Object> {

	private final ChainHandler<Object> handler;
	private KeyExchange key;
	private CipherSuiter cipher;

	public TLSClientHandler(ChainHandler<Object> handler) {
		this.handler = handler;
	}

	@Override
	public void connected(ChainChannel<Object> chain) throws Exception {
		final ClientHello hello = new ClientHello();
		hello.setVersion(TLS.V12);
		hello.setRandom(SecureRandom.getSeed(32));
		hello.setSessionId(SecureRandom.getSeed(32));
		// AEAD HKDF
		hello.setCipherSuites(CipherSuite.V13);
		hello.setCompressionMethods(CompressionMethod.METHODS);
		// Extensions
		// hello.getExtensions().add(new Reserved((short) 0x0A0A));
		hello.getExtensions().add(new ServerNames(new ServerName("developer.mozilla.org")));
		hello.getExtensions().add(new StatusRequest(new OCSPStatusRequest()));
		hello.getExtensions().add(new SignatureAlgorithms(//
			SignatureAlgorithms.ECDSA_SECP256R1_SHA256, //
			SignatureAlgorithms.RSA_PSS_RSAE_SHA256, //
			SignatureAlgorithms.RSA_PKCS1_SHA256, //
			SignatureAlgorithms.ECDSA_SECP384R1_SHA384, //
			SignatureAlgorithms.RSA_PSS_PSS_SHA384, //
			SignatureAlgorithms.RSA_PKCS1_SHA384, //
			SignatureAlgorithms.RSA_PSS_RSAE_SHA512, //
			SignatureAlgorithms.RSA_PKCS1_SHA512));
		hello.getExtensions().add(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		// ENCRYPTED_CLIENT_HELLO
		hello.getExtensions().add(new SupportedGroups(//
			// (short) 0x2A2A, (short) 0x11EC, //
			NamedGroup.X25519, //
			NamedGroup.SECP256R1, //
			NamedGroup.SECP384R1));
		hello.getExtensions().add(new SessionTicket());
		hello.getExtensions().add(new ApplicationLayerProtocolNegotiation(//
			ApplicationLayerProtocolNegotiation.H2, //
			ApplicationLayerProtocolNegotiation.HTTP_1_1));
		hello.getExtensions().add(new ApplicationSettings(ApplicationSettings.H2));
		hello.getExtensions().add(new SupportedVersions(//
			// (short) 0x0A0A, //
			TLS.V13, //
			TLS.V12));
		hello.getExtensions().add(new ExtendedMasterSecret());
		hello.getExtensions().add(new RenegotiationInfo());
		hello.getExtensions().add(new SignedCertificateTimestamp());
		hello.getExtensions().add(new CompressCertificate(CompressCertificate.BROTLI));
		// hello.getExtensions().add(new Reserved((short) 0x9A9A));

		hello.getExtensions().add(new PskKeyExchangeModes(PskKeyExchangeModes.PSK_DHE_KE));

		key = new KeyExchange(NamedGroup.X25519);
		hello.getExtensions().add(new KeyShare(//
			// new KeyShareEntry((short) 0x2A2A, new byte[] { 0 }), //
			// new KeyShareEntry((short) 0x11EC, SecureRandom.getSeed(1216)), //
			new KeyShareEntry(NamedGroup.X25519, key.publicKey())));
		// hello.getExtensions().add(new KeyShare());

		cipher = new CipherSuiter(CipherSuite.TLS_AES_128_GCM_SHA256);
		chain.send(hello);
	}

	@Override
	public DataBuffer encode(ChainChannel<Object> chain, Object message) throws Exception {
		if (message instanceof Record) {
			return RecordCoder.encode((Record) message, cipher);
		} else {
			// APPLICATION DATA
			final DataBuffer data = handler.encode(chain, message);
			return RecordCoder.encodeCiphertext(ApplicationData.DATA, data, cipher);
		}
	}

	@Override
	public void sent(ChainChannel<Object> chain, Object message) throws Exception {
		chain.receive();
	}

	private DataBuffer b;

	@Override
	public Object decode(ChainChannel<Object> chain, DataBuffer buffer) throws Exception {
		Object message = RecordCoder.decode(buffer, cipher);
		if (message instanceof DataBuffer) {
			// TODO 多次解码消息如何返回
			final DataBuffer data = (DataBuffer) message;
			while (data.readable() > 0) {
				message = handler.decode(chain, data);
				if (message != null) {
					handler.received(chain, message);
				} else {
					break;
				}
			}
			data.release();
		}
		return message;
	}

	@Override
	public void received(ChainChannel<Object> chain, Object message) throws Exception {
		System.out.println(message);
		if (message == null) {
			// TIMEOUT
			// handler.received(chain, message);
		} else {
			if (message instanceof Record) {
				final Record record = (Record) message;
				if (record.contentType() == Record.HANDSHAKE) {
					final Handshake handshake = (Handshake) message;
					if (handshake.msgType() == Handshake.SERVER_HELLO) {
						final ServerHello serverHello = (ServerHello) handshake;
						if (serverHello.isHelloRetryRequest()) {

						} else {
							if (serverHello.getVersion() == TLS.V12) {
								for (Extension extension : serverHello.getExtensions()) {
									System.out.println(extension);
									if (extension.type() == Extension.SUPPORTED_VERSIONS) {
										final SupportedVersions supportedVersions = (SupportedVersions) extension;
										if (supportedVersions.size() == 1) {
											if (supportedVersions.get(0) == TLS.V13) {
												// OK
											} else {
												chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
												return;
											}
										} else {
											chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
											return;
										}
									} else
									//
									if (extension.type() == Extension.KEY_SHARE) {
										final KeyShare keyShare = (KeyShare) extension;
										if (keyShare.size() == 1) {
											final KeyShareEntry entry = keyShare.get(0);
											if (entry.group() == key.group()) {
												if (entry.getKeyExchange() != null) {
													cipher.sharedKey(key.sharedKey(entry.getKeyExchange()));
												} else {
													chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
													return;
												}
											} else {
												chain.send(new Alert(Alert.ILLEGAL_PARAMETER));
												return;
											}
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
					} else
					//
					if (handshake.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
						final EncryptedExtensions extensions = (EncryptedExtensions) handshake;
						for (Extension extension : extensions.getExtensions()) {
							System.out.println(extension);
						}
					} else
					//
					if (handshake.msgType() == Handshake.CERTIFICATE) {

					} else
					//
					if (handshake.msgType() == Handshake.CERTIFICATE_VERIFY) {

					} else
					//
					if (handshake.msgType() == Handshake.FINISHED) {
						// final Finished finished = new Finished();
						// finished.setVerifyData(cipher.clientFinished());
						// chain.send(finished);
					}
					chain.receive();
				} else
				//
				if (record.contentType() == Record.APPLICATION_DATA) {
					chain.receive();
				} else
				//
				if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
					chain.receive();
				} else
				//
				if (record.contentType() == Record.HEARTBEAT) {
				} else
				//
				if (record.contentType() == Record.INVALID) {
				} else
				//
				if (record.contentType() == Record.ALERT) {
				}
			} else {
				// handler.received(chain, message);
			}
		}
	}

	@Override
	public void disconnected(ChainChannel<Object> chain) throws Exception {

	}

	@Override
	public void error(ChainChannel<Object> chain, Throwable e) {
		e.printStackTrace();
	}

	static ClientHello defaultClientHello() {
		return null;
	}
}