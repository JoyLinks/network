package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：支持的签名算法
 * 
 * <pre>
 * TLS 1.3
 * 
 * struct {
 *        SignatureScheme supported_signature_algorithms<2..2^16-2>;
 * } SignatureSchemeList;
 * </pre>
 * 
 * <pre>
 * TLS 1.2
 * 
 * enum {
 *       none(0), md5(1), sha1(2), sha224(3), sha256(4), sha384(5), sha512(6), (255)
 * } HashAlgorithm;
 * enum {
 *       anonymous(0), rsa(1), dsa(2), ecdsa(3), (255)
 * } SignatureAlgorithm;
 * 
 * struct {
 *       HashAlgorithm hash;
 *       SignatureAlgorithm signature;
 * } SignatureAndHashAlgorithm;
 * 
 * SignatureAndHashAlgorithm
 *       supported_signature_algorithms<2..2^16-2>;
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

	@Override
	public String toString() {
		if (size() > 0) {
			final StringBuilder b = new StringBuilder();
			b.append(name());
			b.append(':');
			for (int i = 0; i < size(); i++) {
				if (i > 0) {
					b.append(',');
				}
				b.append(SignatureScheme.named(get(i)));
			}
			return b.toString();
		} else {
			return name() + ":EMPTY";
		}
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