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
 * CRC-8/MAXIM
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC8_MAXIM extends Verifier {

	// Name: CRC-8/MAXIM x8+x5+x4+1
	// Poly: 0x31
	// Init: 0x00
	// Refin: True
	// Refout: True
	// Xorout: 0x00
	// Alias: DOW-CRC,CRC-8/IBUTTON
	// Use: Maxim(Dallas)'s some devices,e.g. DS18B20

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x8C);
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