/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-8/ROHC
 * <ul>
 * <li>名称(Name): CRC-8/ROHC</li>
 * <li>公式(Formula): x8+x2+x+1</li>
 * <li>多项式(Poly): 0x07</li>
 * <li>初始值(Init): 0xFF</li>
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
public final class CRC8_ROHC extends Verifier {

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0xE0);
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