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
 * CRC-16/USB
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC16_USB extends Verifier {

	// Name: CRC-16/USB x16+x15+x2+1
	// Poly: 0x8005
	// Init: 0xFFFF
	// Refin: True
	// Refout: True
	// Xorout: 0xFFFF
	// Note:

	short crc = (short) 0xFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (short) (crc >> 1);
			} else {
				// 0xA001 = reverse 0x8005
				crc = (short) ((crc >> 1) ^ 0xA001);
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