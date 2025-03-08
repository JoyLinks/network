package com.joyzl.network.tls;

import java.util.Arrays;

class KeyShareClientHello extends KeyShare {

	private final static KeyShareEntry[] EMPTY = new KeyShareEntry[0];
	private KeyShareEntry[] items = EMPTY;

	public KeyShareClientHello() {
	}

	public KeyShareClientHello(KeyShareEntry... value) {
		set(value);
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
				b.append(get(i));
			}
			return b.toString();
		} else {
			return name() + ":EMPTY";
		}
	}
}