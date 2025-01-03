package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * enum { psk_ke(0), psk_dhe_ke(1), (255) } PskKeyExchangeMode;
 * 
 * struct {
 *     PskKeyExchangeMode ke_modes<1..255>;
 * } PskKeyExchangeModes;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class PskKeyExchangeModes extends Extension {

	// PskKeyExchangeMode MAX(255)

	public final static byte PSK_KE = 0;
	public final static byte PSK_DHE_KE = 1;

	public final static byte[] ALL = new byte[] { PSK_DHE_KE, PSK_KE };

	////////////////////////////////////////////////////////////////////////////////

	private final static byte[] EMPTY = new byte[0];
	private byte[] items = EMPTY;

	public PskKeyExchangeModes() {
	}

	public PskKeyExchangeModes(byte... modes) {
		set(modes);
	}

	@Override
	public short type() {
		return PSK_KEY_EXCHANGE_MODES;
	}

	public byte[] get() {
		return items;
	}

	public byte get(int index) {
		return items[index];
	}

	public void set(byte... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(byte value) {
		if (items == EMPTY) {
			items = new byte[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}
}