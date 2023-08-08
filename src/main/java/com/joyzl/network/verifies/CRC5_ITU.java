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
 * CRC-5/ITU
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC5_ITU extends Verifier {

	// Name: CRC-5/ITU x5+x4+x2+1
	// Poly: 0x15
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
				// 0x15=(reverse 0x15)>>(8-5)
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x15);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc & 0x1F);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}