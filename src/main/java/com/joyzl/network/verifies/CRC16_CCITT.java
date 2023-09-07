/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/CCITT
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_CCITT extends Verifier {

	// Name: CRC-16/CCITT x16+x12+x5+1
	// Poly: 0x1021
	// Init: 0x0000
	// Refin: True
	// Refout: True
	// Xorout: 0x0000
	// Alias: CRC-CCITT,CRC-16/CCITT-TRUE,CRC-16/KERMIT

	short crc = 0;

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
		return crc;
	}

	@Override
	public void reset() {
		crc = 0;
	}
}