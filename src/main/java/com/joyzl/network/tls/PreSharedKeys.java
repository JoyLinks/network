package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 
 * 扩展：OfferedPsks 用于 ClientHello 0-RTT PSK
 * 
 * @author ZhangXi 2025年3月8日
 */
class PreSharedKeys extends PreSharedKey {

	private final static PskIdentity[] EMPTY = new PskIdentity[0];
	private PskIdentity[] identities = EMPTY;

	public PskIdentity[] get() {
		return identities;
	}

	public PskIdentity get(int index) {
		return identities[index];
	}

	public void set(PskIdentity... value) {
		if (value == null) {
			identities = EMPTY;
		} else {
			identities = value;
		}
	}

	public void add(PskIdentity value) {
		if (identities == EMPTY) {
			identities = new PskIdentity[] { value };
		} else {
			identities = Arrays.copyOf(identities, identities.length + 1);
			identities[identities.length - 1] = value;
		}
	}

	public int size() {
		return identities.length;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(':');
		if (size() > 0) {
			for (int i = 0; i < size(); i++) {
				if (i > 0) {
					b.append(',');
				}
				b.append(get(i));
			}
		}
		return b.toString();
	}

	/** 必须设置值用于编码填充BinderKey */
	private int hashLength;

	public int getHashLength() {
		return hashLength;
	}

	public void setHashLength(int value) {
		hashLength = value;
	}

	public int bindersLength() {
		return identities.length * (hashLength + 1);
	}
}