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
 * CRC-16/CCITT-FALSE
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC16_CCITT_FALSE extends Verifier {

	// Name: CRC-16/CCITT-FALSE x16+x12+x5+1
	// Poly: 0x1021
	// Init: 0xFFFF
	// Refin: False
	// Refout: False
	// Xorout: 0x0000
	// Note:

	short crc = (short) 0xFFFF;

	@Override
	public byte check(byte value) {
		crc ^= (short) value << 8;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x8000) != 0) {
				crc = (short) ((crc << 1) ^ 0x1021);
			} else {
				crc <<= 1;
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
		crc = (short) 0xFFFF;
	}
}