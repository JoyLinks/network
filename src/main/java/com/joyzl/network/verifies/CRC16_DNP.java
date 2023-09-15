/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/DNP
 * <ul>
 * <li>名称(Name): CRC-16/DNP</li>
 * <li>公式(Formula): x16+x13+x12+x11+x10+x8+x6+x5+x2+1</li>
 * <li>多项式(Poly): 0x3D65</li>
 * <li>初始值(Init): 0x0000</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0xFFFF</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note): M-Bus</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_DNP extends Verifier {

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