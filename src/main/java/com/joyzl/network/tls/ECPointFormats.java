package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：椭圆曲线是否压缩
 * 
 * <pre>
 * enum {
 *     elliptic_curves(10),
 *     ec_point_formats(11)
 * } ExtensionType;
 * 
 * enum {
 *     deprecated(1..22),
 *     secp256r1 (23), 
 *     secp384r1 (24), 
 *     secp521r1 (25),
 *     x25519(29), 
 *     x448(30),
 *     reserved (0xFE00..0xFEFF),
 *     deprecated(0xFF01..0xFF02),
 *     (0xFFFF)
 * } NamedCurve;
 * struct {
 *     NamedCurve named_curve_list<2..2^16-1>
 * } NamedCurveList;
 * 
 * enum {
 *     uncompressed (0),
 *     deprecated (1..2),
 *     reserved (248..255)
 * } ECPointFormat;
 * struct {
 *     ECPointFormat ec_point_format_list<1..2^8-1>
 * } ECPointFormatList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class ECPointFormats extends Extension {

	// ECPointFormat MAX(255);

	public final static byte UNCOMPRESSED = 0;

	////////////////////////////////////////////////////////////////////////////////

	private byte[] items = TLS.EMPTY_BYTES;

	public ECPointFormats() {
	}

	public ECPointFormats(byte... value) {
		set(value);
	}

	@Override
	public short type() {
		return EC_POINT_FORMATS;
	}

	public byte[] get() {
		return items;
	}

	public byte get(int index) {
		return items[index];
	}

	public void set(byte... value) {
		if (value == null) {
			items = TLS.EMPTY_BYTES;
		} else {
			items = value;
		}
	}

	public void add(byte value) {
		if (items == TLS.EMPTY_BYTES) {
			items = new byte[] { value };
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
				if (get(i) == 0) {
					b.append("uncompressed");
				} else {
					b.append(get(i));
				}
			}
			return b.toString();
		}
		return name() + ":EMPTY";
	}
}