package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <pre>
 * opaque ProtocolName<1..2^8-1>;
 * 
 * struct {
 *       ProtocolName protocol_name_list<2..2^16-1>
 * } ProtocolNameList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class ApplicationLayerProtocolNegotiation extends Extension {

	/** http/1.1 */
	public final static byte[] HTTP_1_1 = new byte[] { 0x68, 0x74, 0x74, 0x70, 0x2f, 0x31, 0x2e, 0x31 };
	/** spdy/1 */
	public final static byte[] SPDY_1 = new byte[] { 0x73, 0x70, 0x64, 0x79, 0x2f, 0x31 };
	/** spdy/2 */
	public final static byte[] SPDY_2 = new byte[] { 0x73, 0x70, 0x64, 0x79, 0x2f, 0x32 };
	/** spdy/3 */
	public final static byte[] SPDY_3 = new byte[] { 0x73, 0x70, 0x64, 0x79, 0x2f, 0x33 };
	/** h2 */
	public final static byte[] H2 = new byte[] { 0x68, 0x32 };

	/** ALL */
	public final static byte[][] ALL = new byte[][] { H2, HTTP_1_1 };

	////////////////////////////////////////////////////////////////////////////////

	private byte[][] items = TLS.EMPTY_STRINGS;

	public ApplicationLayerProtocolNegotiation() {
	}

	public ApplicationLayerProtocolNegotiation(byte[]... value) {
		set(value);
	}

	public ApplicationLayerProtocolNegotiation(String... value) {
		set(value);
	}

	@Override
	public short type() {
		return APPLICATION_LAYER_PROTOCOL_NEGOTIATION;
	}

	public byte[][] get() {
		return items;
	}

	public byte[] get(int index) {
		return items[index];
	}

	public String getString(int index) {
		return new String(items[index], StandardCharsets.US_ASCII);
	}

	public void set(byte[]... value) {
		if (value == null) {
			items = TLS.EMPTY_STRINGS;
		} else {
			items = value;
		}
	}

	public void set(String... value) {
		if (value == null) {
			items = TLS.EMPTY_STRINGS;
		} else {
			items = new byte[value.length][];
			for (int index = 0; index < items.length; index++) {
				items[index] = value[index].getBytes(StandardCharsets.US_ASCII);
			}
		}
	}

	public void add(byte[] value) {
		if (items == TLS.EMPTY_STRINGS) {
			items = new byte[][] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public void add(String value) {
		if (items == TLS.EMPTY_STRINGS) {
			items = new byte[][] { value.getBytes(StandardCharsets.US_ASCII) };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value.getBytes(StandardCharsets.US_ASCII);
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
			if (size() > 0) {
				b.append(':');
				for (int index = 0; index < size(); index++) {
					if (index > 0) {
						b.append(',');
					}
					b.append(getString(index));
				}
			}
			return b.toString();
		} else {
			return name() + ":EMPTY";
		}
	}

	/**
	 * 匹配选择，移除扩展中其余协议名称
	 */
	public boolean select(byte[][] others) {
		for (int i = 0; i < items.length; i++) {
			for (int s = 0; s < others.length; s++) {
				if (Arrays.equals(items[i], others[s])) {
					items = new byte[][] { items[i] };
					return true;
				}
			}
		}
		return false;
	}
}