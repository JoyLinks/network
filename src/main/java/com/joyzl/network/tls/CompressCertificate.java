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
public class CompressCertificate extends Extension {

	// CertificateCompressionAlgorithm MAX(65535)

	public final static short ZLIB = 1;
	public final static short BROTLI = 2;
	public final static short ZSTD = 3;

	public final static short[] ALL = new short[] { BROTLI, ZLIB, ZSTD };

	////////////////////////////////////////////////////////////////////////////////

	private final static short[] EMPTY = new short[0];
	private short[] items = EMPTY;

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