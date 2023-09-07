/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-5/USB
 * 
 * @author ZhangXi
 *
 */
public final class CRC5_USB extends Verifier {

	// Name: CRC-5/USB x5+x2+1
	// Poly: 0x05
	// Init: 0x1F
	// Refin: True
	// Refout: True
	// Xorout: 0x1F
	// Note:

	byte crc = 0x1F;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				// 0x14=(reverse 0x05)>>(8-5)
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x14);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc ^ 0x1F & 0x1F);
	}

	@Override
	public void reset() {
		crc = 0x1F;
	}
}