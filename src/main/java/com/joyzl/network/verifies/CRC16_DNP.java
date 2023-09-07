/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/DNP
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_DNP extends Verifier {

	// Name: CRC-16/DNP x16+x13+x12+x11+x10+x8+x6+x5+x2+1
	// Poly: 0x3D65
	// Init: 0x0000
	// Refin: True
	// Refout: True
	// Xorout: 0xFFFF
	// Use: M-Bus,ect.

	short crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) != 0) {
				// 0xA6BC = reverse 0x3D65
				crc = (short) ((crc >> 1) ^ 0xA6BC);
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
		crc = 0;
	}
}