package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 * <pre>
 * RFC 8446
 * 
 * struct {
 *     opaque identity<1..2^16-1>;
 *     uint32 obfuscated_ticket_age;
 * } PskIdentity;
 * 
 * opaque PskBinderEntry<32..255>;
 * 
 * struct {
 *     PskIdentity identities<7..2^16-1>;
 *     PskBinderEntry binders<33..2^16-1>;
 * } OfferedPsks;
 * 
 * struct {
 *     select (Handshake.msg_type) {
 *        case client_hello: OfferedPsks;
 *        case server_hello: uint16 selected_identity_index;
 *     };
 * } PreSharedKeyExtension;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class PreSharedKey extends Extension {

	private final static PskIdentity[] EMPTY = new PskIdentity[0];
	private PskIdentity[] items = EMPTY;
	private int selected;

	@Override
	public short type() {
		return PRE_SHARED_KEY;
	}

	public PskIdentity[] get() {
		return items;
	}

	public PskIdentity get(int index) {
		return items[index];
	}

	public void set(PskIdentity... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(PskIdentity value) {
		if (items == EMPTY) {
			items = new PskIdentity[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int value) {
		selected = value;
	}

	@Override
	public String toString() {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("pre_shared_key:");
		if (items != null && items.length > 0) {
			for (int index = 0; index < items.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(items[index].toString());
			}
		}
		return builder.toString();
	}
}