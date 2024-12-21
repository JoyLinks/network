package com.joyzl.network.tls;

public class SignatureAlgorithmsCert extends SignatureAlgorithms {

	@Override
	public short type() {
		return SIGNATURE_ALGORITHMS_CERT;
	}
}