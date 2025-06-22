/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/MODBUS
 * <ul>
 * <li>名称(Name): CRC-16/MODBUS</li>
 * <li>公式(Formula): x16+x15+x2+1</li>
 * <li>多项式(Poly): 0x8005</li>
 * <li>初始值(Init): 0xFFFF</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0x0000</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_MODBUS extends Verifier {

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