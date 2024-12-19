package com.joyzl.network.tls;

import com.joyzl.network.Utility;

public class SignatureAlgorithmsCert extends SignatureAlgorithms {

	@Override
	public ExtensionType type() {
		return ExtensionType.SIGNATURE_ALGORITHMS_CERT;
	}

	@Override
	public String toString() {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("signature_algorithms_cert:");
		for (int index = 0; index < size(); index++) {
			if (index > 0) {
				builder.append(',');
			}
			builder.append(get(index).toString());
		}
		return builder.toString();
	}
}