/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * RFC 8879
 * 
 * enum {
 *       zlib(1),
 *       brotli(2),
 *       zstd(3),
 *       (65535)
 * } CertificateCompressionAlgorithm;
 * 
 * struct {
 *       CertificateCompressionAlgorithm algorithms<2..2^8-2>;
 * } CertificateCompressionAlgorithms;
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class CompressCertificate extends Extension {

	// CertificateCompressionAlgorithm MAX(65535)

	public final static short ZLIB = 1;
	public final static short BROTLI = 2;
	public final static short ZSTD = 3;

	public final static short[] ALL = new short[] { BROTLI, ZLIB, ZSTD };

	////////////////////////////////////////////////////////////////////////////////

	private short[] items = TLS.EMPTY_SHORTS;

	public CompressCertificate() {
	}

	public CompressCertificate(short... value) {
		set(value);
	}

	@Override
	public short type() {
		return COMPRESS_CERTIFICATE;
	}

	public short[] get() {
		return items;
	}

	public short get(int index) {
		return items[index];
	}

	public void set(short... value) {
		if (value == null) {
			items = TLS.EMPTY_SHORTS;
		} else {
			items = value;
		}
	}

	public void add(short value) {
		if (items == TLS.EMPTY_SHORTS) {
			items = new short[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
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
				if (get(i) == ZLIB) {
					b.append("ZLIB");
				} else if (get(i) == BROTLI) {
					b.append("BROTLI");
				} else if (get(i) == ZSTD) {
					b.append("ZSTD");
				} else {
					b.append("UNKNOWN");
				}
			}
			return b.toString();
		} else {
			return name() + ":EMPTY";
		}
	}
}