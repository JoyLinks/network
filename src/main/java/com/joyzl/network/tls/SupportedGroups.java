package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * struct {
 *     NamedGroup named_group_list<2..2^16-1>;
 * } NamedGroupList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class SupportedGroups extends Extension {

	// NamedGroup MAX (0xFFFF)

	/** Elliptic Curve Groups (ECDHE) */
	public final static short SECP256R1 = 0X0017;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short SECP384R1 = 0X0018;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short SECP521R1 = 0X0019;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short X25519 = 0X001D;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short X448 = 0X001E;
	/** Elliptic Curve Groups (ECDHE) */

	/** Finite Field Groups (DHE) */
	public final static short FFDHE2048 = 0X0100;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE3072 = 0X0101;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE4096 = 0X0102;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE6144 = 0X0103;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE8192 = 0X0104;

	/* Reserved Code Points */
	// ffdhe_private_use(0x01FC..0x01FF),
	// ecdhe_private_use(0xFE00..0xFEFF),

	public final static short[] ALL = new short[] { //
			X25519, //
			X448, //

			SECP256R1, //
			SECP384R1, //
			SECP521R1, //

			FFDHE2048, //
			FFDHE3072, //
			FFDHE4096, //
			FFDHE6144, //
			FFDHE8192,//
	};

	////////////////////////////////////////////////////////////////////////////////

	private final static short[] EMPTY = new short[0];
	private short[] items = EMPTY;

	public SupportedGroups() {
	}

	public SupportedGroups(short... value) {
		set(value);
	}

	@Override
	public short type() {
		return SUPPORTED_GROUPS;
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