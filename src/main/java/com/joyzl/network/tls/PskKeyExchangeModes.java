package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

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

	private final static PskKeyExchangeMode[] EMPTY = new PskKeyExchangeMode[0];
	private PskKeyExchangeMode[] items = EMPTY;

	public PskKeyExchangeModes() {
	}

	public PskKeyExchangeModes(PskKeyExchangeMode... modes) {
		set(modes);
	}

	@Override
	public ExtensionType type() {
		return ExtensionType.PSK_KEY_EXCHANGE_MODES;
	}

	public PskKeyExchangeMode[] get() {
		return items;
	}

	public PskKeyExchangeMode get(int index) {
		return items[index];
	}

	public void set(PskKeyExchangeMode... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(PskKeyExchangeMode value) {
		if (items == EMPTY) {
			items = new PskKeyExchangeMode[] { value };
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
		builder.append("psk_key_exchange_modes:");
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