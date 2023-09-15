/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-6/ITU
 * <ul>
 * <li>名称(Name): CRC-6/ITU</li>
 * <li>公式(Formula): x6+x+1</li>
 * <li>多项式(Poly): 0x03</li>
 * <li>初始值(Init): 0x00</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0x00</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC6_ITU extends Verifier {

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				// 0x30=(reverse 0x03)>>(8-6)
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x30);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc & 0x3F);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}