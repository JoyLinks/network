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
 * CRC-7/MMC
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC7_MMC extends Verifier {

	// Name: CRC-7/MMC x7+x3+1
	// Poly: 0x09
	// Init: 0x00
	// Refin: False
	// Refout: False
	// Xorout: 0x00
	// Use: MultiMediaCard,SD,ect.

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) == 0) {
				crc <<= 1;
			} else {
				// 0x12=0x09<<(8-7)
				crc = (byte) ((crc << 1) ^ 0x12);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc >> 1 & 0x7F);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}