package com.joyzl.network.tls;

import java.util.Arrays;

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

	// SRTPProtectionProfile MAX(65535)

	public final static short SRTP_AES128_CM_HMAC_SHA1_80 = 0x0001;
	public final static short SRTP_AES128_CM_HMAC_SHA1_32 = 0x0002;
	public final static short SRTP_NULL_HMAC_SHA1_80 = 0x0005;
	public final static short SRTP_NULL_HMAC_SHA1_32 = 0x0006;

	////////////////////////////////////////////////////////////////////////////////

	private final static short[] EMPTY = new short[0];
	private short[] profiles = EMPTY;
	private byte[] mki;

	@Override
	public short type() {
		return USE_SRTP;
	}

	public byte[] getMKI() {
		return mki;
	}

	public void setMKI(byte[] value) {
		mki = value;
	}

	public short[] get() {
		return profiles;
	}

	public short get(int index) {
		return profiles[index];
	}

	public void set(short... value) {
		if (value == null) {
			profiles = EMPTY;
		} else {
			profiles = value;
		}
	}

	public void add(short value) {
		if (profiles == EMPTY) {
			profiles = new short[] { value };
		} else {
			profiles = Arrays.copyOf(profiles, profiles.length + 1);
			profiles[profiles.length - 1] = value;
		}
	}

	public int size() {
		return profiles.length;
	}
}