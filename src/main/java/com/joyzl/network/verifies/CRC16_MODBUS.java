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
 * CRC-16/MODBUS
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class CRC16_MODBUS extends Verifier {

	// Name: CRC-16/MODBUS x16+x15+x2+1
	// Poly: 0x8005
	// Init: 0xFFFF
	// Refin: True
	// Refout: True
	// Xorout: 0x0000
	// Note:

	int crc = 0xFFFF;

	@Override
	public byte check(byte value) {
		crc = (crc ^ (value & 0xFF)) & 0xFFFF;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = crc >> 1;
			} else {
				// 0xA001 = reverse 0x8005
				crc = ((crc >> 1) ^ 0xA001) & 0xFFFF;
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
		crc = 0xFFFF;
	}
}