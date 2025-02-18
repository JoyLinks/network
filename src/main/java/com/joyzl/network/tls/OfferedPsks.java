package com.joyzl.network.tls;

import java.util.Arrays;

public class OfferedPsks extends PreSharedKey {

	private final static PskIdentity[] EMPTY = new PskIdentity[0];
	private PskIdentity[] identities = EMPTY;
	private int hashLength;

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