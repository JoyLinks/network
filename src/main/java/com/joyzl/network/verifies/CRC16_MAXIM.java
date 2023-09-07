/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/MAXIM
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_MAXIM extends Verifier {

	// Name: CRC-16/MAXIM x16+x15+x2+1
	// Poly: 0x8005
	// Init: 0x0000
	// Refin: True
	// Refout: True
	// Xorout: 0xFFFF
	// Note:

	short crc = 0;

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
		// crc ^ 0xffff
		return (short) (~crc);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}