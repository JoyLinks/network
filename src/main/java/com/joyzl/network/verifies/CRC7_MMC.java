/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-7/MMC
 * <ul>
 * <li>名称(Name): CRC-7/MMC</li>
 * <li>公式(Formula): x7+x3+1</li>
 * <li>多项式(Poly): 0x09</li>
 * <li>初始值(Init): 0x00</li>
 * <li>输入反转(Refin): False</li>
 * <li>输出反转(Refout): False</li>
 * <li>结果异或(Xorout): 0x00</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note): MultiMediaCard,SD</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC7_MMC extends Verifier {

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) == 0) {
				crc <<= 1;
			} else {
				// 0x12=0x09<<(8-7)
				crc = (byte) ((crc << 1) ^ 0x12);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc >> 1 & 0x7F);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}