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
 * CRC32 校验(长度4字节)
 * <p>
 * CRC即循环冗余校验码(Cyclic Redundancy Check)<br>
 * 是数据通信领域中最常用的一种查错校验码，其特征是信息字段和校验字段的长度可以任意选定。循环冗余检查(CRC)是一种数据传输检错功能，对数据进行多项式计算，并将得到的结果附在帧的后面，接收设备也执行类似的算法，以保证数据传输的正确性和完整性。
 * </p>
 * 
 * @author simon(ZhangXi)
 *
 */
public class CRC32 extends Verifier {

	// Name: CRC-32 x32+x26+x23+x22+x16+x12+x11+x10+x8+x7+x5+x4+x2+x+1
	// Poly: 0x4C11DB7
	// Init: 0xFFFFFFF
	// Refin: True
	// Refout: True
	// Xorout: 0xFFFFFFF
	// Alias: CRC_32/ADCCP
	// Use: WinRAR,ect.

	private int crc = 0xFFFFFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) != 0) {
				// 0xEDB88320=reverse 0x04C11DB7
				crc = (crc >> 1) ^ 0xEDB88320;
			} else {
				crc = (crc >> 1);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return ~crc;
	}

	@Override
	public void reset() {
		crc = 0xFFFFFFFF;
	}
}