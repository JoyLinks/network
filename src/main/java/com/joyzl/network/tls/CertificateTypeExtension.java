package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

public abstract class CertificateTypeExtension extends Extension {

	private final static CertificateType[] EMPTY = new CertificateType[0];
	private CertificateType[] items = EMPTY;

	public CertificateType[] get() {
		return items;
	}

	public CertificateType get(int index) {
		return items[index];
	}

	public void set(CertificateType... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(CertificateType value) {
		if (items == EMPTY) {
			items = new CertificateType[] { value };
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
		builder.append(type());
		if (items != null && items.length > 0) {
			for (int index = 0; index < items.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(items[index]);
			}
		}
		return builder.toString();
	}
}