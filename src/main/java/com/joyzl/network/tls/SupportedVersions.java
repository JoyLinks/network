package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 *
 * <pre>
 * struct {
 *     select (Handshake.msg_type) {
 *      case client_hello:
 *        ProtocolVersion versions<2..254>;
 * 
 *      case server_hello: (and HelloRetryRequest)
 *        ProtocolVersion selected_version;
 *     }
 * } SupportedVersions
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class SupportedVersions extends Extension {

	private final static short[] EMPTY = new short[0];
	private short[] items = EMPTY;

	public SupportedVersions() {
	}

	public SupportedVersions(short... versions) {
		items = versions;
	}

	@Override
	public ExtensionType type() {
		return ExtensionType.SUPPORTED_VERSIONS;
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

	@Override
	public String toString() {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("supported_versions:");
		if (items != null && items.length > 0) {
			for (int index = 0; index < items.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(Short.toString(items[index]));
			}
		}
		return builder.toString();
	}
}