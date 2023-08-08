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
 * CRC-6/ITU
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC6_ITU extends Verifier {

	// Name: CRC-6/ITU x6+x+1
	// Poly: 0x03
	// Init: 0x00
	// Refin: True
	// Refout: True
	// Xorout: 0x00
	// Note:

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				// 0x30=(reverse 0x03)>>(8-6)
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x30);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc & 0x3F);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}