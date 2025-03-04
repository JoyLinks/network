package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：支持的签名算法
 * 
 * <pre>
 * struct {
 *        SignatureScheme supported_signature_algorithms<2..2^16-2>;
 * } SignatureSchemeList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class SignatureAlgorithms extends Extension implements SignatureScheme {

	// CertificateVerify 消息中的签名算法
	// 如果没有 "signature_algorithms_cert" 扩展，
	// 则 "signature_algorithms" 扩展同样适用于证书中的签名

	private short[] algorithms = TLS.EMPTY_SHORTS;

	public SignatureAlgorithms() {
	}

	public SignatureAlgorithms(short... value) {
		set(value);
	}

	@Override
	public short type() {
		return SIGNATURE_ALGORITHMS;
	}

	public short[] get() {
		return algorithms;
	}

	public short get(int index) {
		return algorithms[index];
	}

	public void set(short... value) {
		if (value == null) {
			algorithms = TLS.EMPTY_SHORTS;
		} else {
			algorithms = value;
		}
	}

	public void add(short value) {
		if (algorithms == TLS.EMPTY_SHORTS) {
			algorithms = new short[] { value };
		} else {
			algorithms = Arrays.copyOf(algorithms, algorithms.length + 1);
			algorithms[algorithms.length - 1] = value;
		}
	}

	public int size() {
		return algorithms.length;
	}

	/**
	 * 匹配签名算法
	 */
	public short match(short other) {
		for (int i = 0; i < algorithms.length; i++) {
			if (algorithms[i] == other) {
				return other;
			}
		}
		return 0;
	}
}