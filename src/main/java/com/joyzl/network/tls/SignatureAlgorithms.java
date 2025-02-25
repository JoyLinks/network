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
public class SignatureAlgorithms extends Extension implements SignatureScheme {

	// CertificateVerify 消息中的签名算法
	// 如果没有 "signature_algorithms_cert" 扩展，
	// 则 "signature_algorithms" 扩展同样适用于证书中的签名

	private final static short[] EMPTY = new short[0];
	private short[] items = EMPTY;

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
		return items;
	}

	public short get(int index) {
		return items[index];
	}

	public void set(short... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(short value) {
		if (items == EMPTY) {
			items = new short[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}
}