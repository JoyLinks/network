package com.joyzl.network.tls;

import java.util.Arrays;

public abstract class CertificateTypes extends Extension {

	// CertificateType MAX(255)

	public final static byte X509 = 0;
	public final static byte RAW_PUBLIC_KEY = 2;

	////////////////////////////////////////////////////////////////////////////////

	private final static byte[] EMPTY = new byte[0];
	private byte[] items = EMPTY;

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