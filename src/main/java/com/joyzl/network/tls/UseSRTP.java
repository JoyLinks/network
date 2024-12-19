package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 * <pre>
 * uint8 SRTPProtectionProfile[2];
 * 
 * struct {
 *    SRTPProtectionProfiles SRTPProtectionProfiles;
 *    opaque srtp_mki<0..255>;
 * } UseSRTPData;
 * 
 * SRTPProtectionProfile SRTPProtectionProfiles<2..2^16-1>;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class UseSRTP extends Extension {

	private final static SRTPProtectionProfile[] EMPTY = new SRTPProtectionProfile[0];
	private SRTPProtectionProfile[] profiles = EMPTY;
	private byte[] mki;

	@Override
	public ExtensionType type() {
		return ExtensionType.USE_SRTP;
	}

	public byte[] getMKI() {
		return mki;
	}

	public void setMKI(byte[] value) {
		mki = value;
	}

	public SRTPProtectionProfile[] get() {
		return profiles;
	}

	public SRTPProtectionProfile get(int index) {
		return profiles[index];
	}

	public void set(SRTPProtectionProfile... value) {
		if (value == null) {
			profiles = EMPTY;
		} else {
			profiles = value;
		}
	}

	public void add(SRTPProtectionProfile value) {
		if (profiles == EMPTY) {
			profiles = new SRTPProtectionProfile[] { value };
		} else {
			profiles = Arrays.copyOf(profiles, profiles.length + 1);
			profiles[profiles.length - 1] = value;
		}
	}

	public int size() {
		return profiles.length;
	}

	@Override
	public String toString() {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("use_srtp:");
		if (profiles != null && profiles.length > 0) {
			for (int index = 0; index < profiles.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(profiles[index].toString());
			}
		}
		return builder.toString();
	}
}