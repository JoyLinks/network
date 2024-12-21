package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * struct {
 *     NamedGroup group;
 *     opaque key_exchange<1..2^16-1>;
 * } KeyShareEntry;
 * 
 * struct {
 *     KeyShareEntry client_shares<0..2^16-1>;
 * } KeyShareClientHello;
 * 
 * struct {
 *     NamedGroup selected_group;
 * } KeyShareHelloRetryRequest;
 * 
 * struct {
 *     KeyShareEntry server_share;
 * } KeyShareServerHello;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class KeyShare extends Extension {

	private final static KeyShareEntry[] EMPTY = new KeyShareEntry[0];
	private KeyShareEntry[] items = EMPTY;

	@Override
	public short type() {
		return KEY_SHARE;
	}

	public KeyShareEntry[] get() {
		return items;
	}

	public KeyShareEntry get(int index) {
		return items[index];
	}

	public void set(KeyShareEntry... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(KeyShareEntry value) {
		if (items == EMPTY) {
			items = new KeyShareEntry[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}
}