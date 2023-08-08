/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 2018年9月18日
 */
package com.joyzl.network.verifies;

/**
 * CRC-8/ROHC
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC8_ROHC extends Verifier {

	// Name: CRC-8/ROHC x8+x2+x+1
	// Poly: 0x07
	// Init: 0xFF
	// Refin: True
	// Refout: True
	// Xorout: 0x00
	// Note:

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0xE0);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return crc;
	}

	@Override
	public void reset() {
		crc = 0;
	}
}