package com.joyzl.network.tls;

import java.security.SecureRandom;

import com.joyzl.network.Executor;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.TCPClient;

public class TLSClientHandler implements ChainHandler<Record> {

	public static void main(String[] argments) throws Exception {
		Executor.initialize(0);

		final TLSClientHandler handler = new TLSClientHandler();
		final TCPClient<?> client = new TCPClient<>(handler, "103.235.46.96", 443);

		client.connect();

		System.in.read();
		Executor.shutdown();
	}

	@Override
	public void connected(ChainChannel<Record> chain) throws Exception {
		final ClientHello hello = new ClientHello();
		hello.setVersion(TLS.V12);
		hello.setRandom(SecureRandom.getSeed(32));
		hello.setSessionId(TLS.EMPTY_BYTES);
		hello.setCipherSuites(CipherSuite.V13);
		hello.setCompressionMethods(CompressionMethod.METHODS);
		// Extensions
		hello.getExtensions().add(new SupportedVersions(TLS.ALL_VERSIONS));
		hello.getExtensions().add(new PskKeyExchangeModes(PskKeyExchangeModes.ALL));
		hello.getExtensions().add(new SupportedGroups(SupportedGroups.ALL));
		hello.getExtensions().add(new SignedCertificateTimestamp());
		hello.getExtensions().add(new StatusRequest(new OCSPStatusRequest()));
		hello.getExtensions().add(new SignatureAlgorithms(SignatureAlgorithms.ALL));
		hello.getExtensions().add(new ApplicationLayerProtocolNegotiation(//
			ApplicationLayerProtocolNegotiation.H2, //
			ApplicationLayerProtocolNegotiation.HTTP_1_1));
		hello.getExtensions().add(new ApplicationSettings(ApplicationSettings.H2));
		hello.getExtensions().add(new CompressCertificate(CompressCertificate.ALL));
		hello.getExtensions().add(new ECPointFormats(ECPointFormats.UNCOMPRESSED));
		hello.getExtensions().add(new ExtendedMasterSecret());
		hello.getExtensions().add(new RenegotiationInfo());
		hello.getExtensions().add(new SessionTicket());
		hello.getExtensions().add(new KeyShare());

		chain.send(hello);
	}

	@Override
	public Record decode(ChainChannel<Record> chain, DataBuffer reader) throws Exception {
		return TLSCoder.decode(reader);
	}

	@Override
	public void received(ChainChannel<Record> chain, Record message) throws Exception {
		System.out.println(message);
	}

	@Override
	public DataBuffer encode(ChainChannel<Record> chain, Record message) throws Exception {
		return TLSCoder.encode(message);
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
}