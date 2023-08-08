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
 * CRC16 Modbus 校验(长度2字节)
 * <p>
 * CRC即循环冗余校验码(Cyclic Redundancy Check)<br>
 * 是数据通信领域中最常用的一种查错校验码，其特征是信息字段和校验字段的长度可以任意选定。循环冗余检查(CRC)是一种数据传输检错功能，对数据进行多项式计算，并将得到的结果附在帧的后面，接收设备也执行类似的算法，以保证数据传输的正确性和完整性。
 * </p>
 *
 * @author simon(ZhangXi)
 *
 */
public class CRC16 extends Verifier {

	private int crc = 0x0000FFFF;

	@Override
	public byte check(byte value) {
		crc ^= value & 0x000000FF;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x00000001) == 1) {
				crc >>= 1;
				crc ^= 0x0000A001;
			} else {
				crc >>= 1;
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
		crc = 0x0000FFFF;
	}
}
