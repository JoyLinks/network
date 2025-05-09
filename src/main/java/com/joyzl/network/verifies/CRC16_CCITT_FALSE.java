/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-16/CCITT-FALSE
 * <ul>
 * <li>名称(Name): CRC-16/CCITT-FALSE
 * <li>公式(Formula): x16+x12+x5+1</li>
 * <li>多项式(Poly): 0x1021</li>
 * <li>初始值(Init): 0xFFFF</li>
 * <li>输入反转(Refin): False</li>
 * <li>输出反转(Refout): False</li>
 * <li>结果异或(Xorout): 0x0000</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC16_CCITT_FALSE extends Verifier {

	short crc = (short) 0xFFFF;

	@Override
	public byte check(byte value) {
		crc ^= (short) value << 8;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x8000) != 0) {
				crc = (short) ((crc << 1) ^ 0x1021);
			} else {
				crc <<= 1;
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
		crc = (short) 0xFFFF;
	}
}