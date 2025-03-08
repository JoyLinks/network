package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：预共享密钥交换模式
 * 
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
class PskKeyExchangeModes extends Extension {

	// PskKeyExchangeMode MAX(255)

	/** 仅PSK密钥建立。在这种模式下Server不能提供"key_share" */
	public final static byte PSK_KE = 0;
	/** PSK和(EC)DHE建立。在这种模式下，Client和Server必须提供"key_share" */
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

	public boolean has(byte value) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == value) {
				return true;
			}
		}
		return false;
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
				if (get(i) == PSK_KE) {
					b.append("PSK_KE");
				} else if (get(i) == PSK_DHE_KE) {
					b.append("PSK_DHE_KE");
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