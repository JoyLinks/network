/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * LSB-first、多项式 0xA001 (0x8005 的反射)、初始值 0xFFFF、无最终异或
 * <p>
 * CRC即循环冗余校验码(Cyclic Redundancy Check)<br>
 * 是数据通信领域中最常用的一种查错校验码，其特征是信息字段和校验字段的长度可以任意选定。循环冗余检查(CRC)是一种数据传输检错功能，对数据进行多项式计算，并将得到的结果附在帧的后面，接收设备也执行类似的算法，以保证数据传输的正确性和完整性。
 * </p>
 *
 * @author simon(ZhangXi)
 *
 */
public class CRC16_LSB extends Verifier {

	private int crc = 0xFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value & 0xFF;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 1) {
				crc >>>= 1;
				crc ^= 0xA001;
			} else {
				crc >>>= 1;
			}
		}
		return value;
	}

	@Override
	public int value() {
		return crc & 0xFFFF;
	}

	@Override
	public void reset() {
		crc = 0xFFFF;
	}
}
