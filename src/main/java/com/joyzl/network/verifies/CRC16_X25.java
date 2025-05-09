/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/X25
 * <ul>
 * <li>名称(Name): CRC-16/X25</li>
 * <li>公式(Formula): x16+x12+x5+1</li>
 * <li>多项式(Poly): 0x1021</li>
 * <li>初始值(Init): 0xFFFF</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0XFFFF</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_X25 extends Verifier {

	short crc = (short) 0xFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) != 0) {
				// 0x8408 = reverse 0x1021
				crc = (short) ((crc >> 1) ^ 0x8408);
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
		crc = (short) 0xFFFF;
	}
}