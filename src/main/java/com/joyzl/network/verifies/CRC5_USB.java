/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-5/USB
 * <ul>
 * <li>名称(Name): CRC-5/USB</li>
 * <li>公式(Formula): x5+x2+1</li>
 * <li>多项式(Poly): 0x05</li>
 * <li>初始值(Init): 0x1F</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0x1F</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC5_USB extends Verifier {

	byte crc = 0x1F;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				// 0x14=(reverse 0x05)>>(8-5)
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x14);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc ^ 0x1F & 0x1F);
	}

	@Override
	public void reset() {
		crc = 0x1F;
	}
}