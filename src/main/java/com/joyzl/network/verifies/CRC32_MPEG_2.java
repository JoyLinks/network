/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-32/MPEG-2
 * <ul>
 * <li>名称(Name): CRC-32/MPEG-2</li>
 * <li>公式(Formula): x32+x26+x23+x22+x16+x12+x11+x10+x8+x7+x5+x4+x2+x+1</li>
 * <li>多项式(Poly): 0x4C11DB7</li>
 * <li>初始值(Init): 0xFFFFFFF</li>
 * <li>输入反转(Refin): False</li>
 * <li>输出反转(Refout): False</li>
 * <li>结果异或(Xorout): 0x0000000</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author simon(ZhangXi)
 *
 */
public class CRC32_MPEG_2 extends Verifier {

	private int crc = 0xFFFFFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value << 24;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80000000) != 0) {
				crc = (crc << 1) ^ 0x04C11DB7;
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
		crc = 0xFFFFFFFF;
	}
}