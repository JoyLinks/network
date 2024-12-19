package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.joyzl.network.Utility;

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
	public final byte[] HTTP_1_1 = new byte[] { 0x68, 0x74, 0x74, 0x70, 0x2f, 0x31, 0x2e, 0x31 };
	/** spdy/1 */
	public final byte[] SPDY_1 = new byte[] { 0x73, 0x70, 0x64, 0x79, 0x2f, 0x31 };
	/** spdy/2 */
	public final byte[] SPDY_2 = new byte[] { 0x73, 0x70, 0x64, 0x79, 0x2f, 0x32 };

	private final static byte[][] EMPTY = new byte[0][];
	private byte[][] items = EMPTY;

	@Override
	public ExtensionType type() {
		return ExtensionType.APPLICATION_LAYER_PROTOCOL_NEGOTIATION;
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
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(byte[] value) {
		if (items == EMPTY) {
			items = new byte[][] { value };
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
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("application_layer_protocol_negotiation:");
		if (items != null && items.length > 0) {
			for (int index = 0; index < items.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(getString(index));
			}
		}
		return builder.toString();
	}
}