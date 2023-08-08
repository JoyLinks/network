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
 * CRC-16/X25
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC16_X25 extends Verifier {

	// Name: CRC-16/X25 x16+x12+x5+1
	// Poly: 0x1021
	// Init: 0xFFFF
	// Refin: True
	// Refout: True
	// Xorout: 0XFFFF
	// Note:

	short crc = (short) 0xFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) != 0) {
				// 0x8408 = reverse 0x1021
				crc = (short) ((crc >> 1) ^ 0x8408);
			} else {
				crc = (short) (crc >> 1);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (short) (~crc);
	}

	@Override
	public void reset() {
		crc = (short) 0xFFFF;
	}
}