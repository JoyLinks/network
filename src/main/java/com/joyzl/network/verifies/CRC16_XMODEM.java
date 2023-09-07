/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/XMODEM
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_XMODEM extends Verifier {

	// Name: CRC-16/XMODEM x16+x12+x5+1
	// Poly: 0x1021
	// Init: 0x0000
	// Refin: False
	// Refout: False
	// Xorout: 0x0000
	// Alias: CRC-16/ZMODEM,CRC-16/ACORN

	short crc = 0;

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
		crc = 0;
	}
}