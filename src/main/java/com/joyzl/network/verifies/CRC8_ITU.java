/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-8/ITU
 * 
 * @author ZhangXi
 *
 */
public final class CRC8_ITU extends Verifier {

	// Name: CRC-8/ITU x8+x2+x+1
	// Poly: 0x07
	// Init: 0x00
	// Refin: False
	// Refout: False
	// Xorout: 0x55
	// Alias: CRC-8/ATM

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) == 0) {
				crc <<= 1;
			} else {
				crc = (byte) ((crc << 1) ^ 0x07);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc ^ 0x55);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}