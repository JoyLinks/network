package com.joyzl.network.tls;

import java.security.SecureRandom;
import java.util.Arrays;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public class TLSClientHandler extends RecordHandler {

	private final ChainHandler<Object> handler;
	private CipherSuiter cipher;
	private KeyExchange key;

	public TLSClientHandler(ChainHandler<Object> handler) {
		this.handler = handler;
	}

	@Override
	protected ChainHandler<Object> handler() {
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
	protected DataBuffer encrypt(DataBuffer buffer) throws Exception {
		cipher.encryptFinal(buffer);
		return buffer;
	}

	@Override
	protected void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		HandshakeCoder.encode(handshake, buffer);
		cipher.hash(buffer);
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
	public void sent(ChainChannel<Object> chain, Object message) throws Exception {
		if (message instanceof Record) {
			if (message instanceof Finished) {
				cipher.encryptReset(cipher.clientApplicationTraffic());
				handler().connected(chain);
			} else {
				chain.receive();
			}
		} else {
			handler().sent(chain, message);
		}
	}

	@Override
	protected void received(ChainChannel<Object> chain, Handshake message) throws Exception {
		System.out.print('\t');
		System.out.println(message);

		if (message.msgType() == Handshake.SERVER_HELLO) {
			final ServerHello serverHello = (ServerHello) message;
			if (serverHello.isHelloRetryRequest()) {
				System.out.println("HelloRetryRequest");
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
									// OK
									continue;
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
							final KeyShareServerHello keyShare = (KeyShareServerHello) extension;
							if (keyShare.serverShare().group() == key.group()) {
								cipher.sharedKey(key.sharedKey(keyShare.serverShare().getKeyExchange()));
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
		if (message.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			final EncryptedExtensions extensions = (EncryptedExtensions) message;
			for (Extension extension : extensions.getExtensions()) {
				System.out.print('\t');
				System.out.print('\t');
				System.out.println(extension);

				if (extension.type() == Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
					final ApplicationLayerProtocolNegotiation alpn = (ApplicationLayerProtocolNegotiation) extension;

				}
			}
		} else
		//
		if (message.msgType() == Handshake.CERTIFICATE) {
			final Certificate certificate = (com.joyzl.network.tls.Certificate) message;

		} else
		//
		if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {

		} else
		//
		if (message.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) message;
			if (finished.getVerifyData() == Finished.OK) {
				finished.setVerifyData(cipher.clientFinished());
				cipher.decryptReset(cipher.serverApplicationTraffic());
			}
			chain.send(finished);
		}
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
		hello.getExtensions().add(new KeyShareClientHello(//
			// new KeyShareEntry((short) 0x2A2A, new byte[] { 0 }), //
			// new KeyShareEntry((short) 0x11EC, SecureRandom.getSeed(1216)), //
			new KeyShareEntry(NamedGroup.X25519, key.publicKey())));
		// hello.getExtensions().add(new KeyShare());

		cipher = new CipherSuiter(CipherSuite.TLS_AES_128_GCM_SHA256);

		chain.send(hello);
	}

	static ClientHello defaultClientHello() {
		return null;
	}
}