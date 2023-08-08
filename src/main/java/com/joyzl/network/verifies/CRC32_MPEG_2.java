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
 * CRC-32/MPEG-2
 * 
 * @author simon(ZhangXi)
 *
 */
public class CRC32_MPEG_2 extends Verifier {

	// Name: CRC-32/MPEG-2 x32+x26+x23+x22+x16+x12+x11+x10+x8+x7+x5+x4+x2+x+1
	// Poly: 0x4C11DB7
	// Init: 0xFFFFFFF
	// Refin: False
	// Refout: False
	// Xorout: 0x0000000
	// Note:

	private int crc = 0xFFFFFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value << 24;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80000000) != 0) {
				crc = (crc << 1) ^ 0x04C11DB7;
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
		crc = 0xFFFFFFFF;
	}
}