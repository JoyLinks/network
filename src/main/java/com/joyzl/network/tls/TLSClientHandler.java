package com.joyzl.network.tls;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;

import javax.crypto.KeyAgreement;

import com.joyzl.network.Executor;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.TCPClient;

public class TLSClientHandler implements ChainHandler<Record> {

	public static void main(String[] argments) throws Exception {
		// final URL url = new URL("https://developer.mozilla.org/");
		// url.getContent();

		Executor.initialize(0);

		final TLSClientHandler handler = new TLSClientHandler();
		final TCPClient<?> client = new TCPClient<>(handler, "34.111.97.67", 443);

		client.connect();

		System.in.read();
		Executor.shutdown();
	}

	private KeyPair key;

	@Override
	public void connected(ChainChannel<Record> chain) throws Exception {
		final ClientHello hello = new ClientHello();
		hello.setVersion(TLS.V12);
		hello.setRandom(SecureRandom.getSeed(32));
		hello.setSessionId(SecureRandom.getSeed(32));
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
		hello.getExtensions().add(new PskKeyExchangeModes(PskKeyExchangeModes.PSK_DHE_KE));
		hello.getExtensions().add(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		// ENCRYPTED_CLIENT_HELLO
		hello.getExtensions().add(new SupportedGroups(//
			// (short) 0x2A2A, (short) 0x11EC, //
			SupportedGroups.X25519, //
			SupportedGroups.SECP256R1, //
			SupportedGroups.SECP384R1));
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

		hello.getExtensions().add(new KeyShare(//
			// new KeyShareEntry((short) 0x2A2A, new byte[] { 0 }), //
			// new KeyShareEntry((short) 0x11EC, SecureRandom.getSeed(1216)), //
			new KeyShareEntry(SupportedGroups.X25519, make(SupportedGroups.X25519))));
		// hello.getExtensions().add(new KeyShare());
		chain.send(hello);
	}

	@Override
	public Record decode(ChainChannel<Record> chain, DataBuffer reader) throws Exception {
		return TLSCoder.decodeByClient(reader);
	}

	@Override
	public void received(ChainChannel<Record> chain, Record message) throws Exception {
		System.out.println(message);
	}

	@Override
	public DataBuffer encode(ChainChannel<Record> chain, Record message) throws Exception {
		return TLSCoder.encodeByClient(message);
	}

	@Override
	public void sent(ChainChannel<Record> chain, Record message) throws Exception {
		chain.receive();
	}

	@Override
	public void disconnected(ChainChannel<Record> chain) throws Exception {

	}

	@Override
	public void error(ChainChannel<Record> chain, Throwable e) {
		e.printStackTrace();
	}

	public final byte[] make(short named_group) throws Exception {
		final String name = SupportedGroups.named(named_group);
		if (name != null) {
			final KeyPairGenerator generator = KeyPairGenerator.getInstance(name);
			final KeyAgreement agreement = KeyAgreement.getInstance(name);
			final KeyPair key = generator.generateKeyPair();

		}

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
		NamedParameterSpec paramSpec = new NamedParameterSpec("X25519");
		// equivalent to kpg.initialize(255)
		kpg.initialize(paramSpec);
		// alternatively: kpg = KeyPairGenerator.getInstance("X25519")
		KeyPair kp = kpg.generateKeyPair();

		KeyFactory kf = KeyFactory.getInstance("XDH");
		XECPublicKeySpec pubSpec = new XECPublicKeySpec(paramSpec, BigInteger.TEN);
		PublicKey pubKey = kf.generatePublic(pubSpec);

		KeyAgreement ka = KeyAgreement.getInstance("XDH");
		ka.init(kp.getPrivate());
		ka.doPhase(pubKey, true);
		byte[] secret = ka.generateSecret();
		return secret;
	}
}