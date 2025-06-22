/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * CRC-5/EPC
 * <ul>
 * <li>名称(Name): CRC-5/EPC</li>
 * <li>公式(Formula): x5+x3+1</li>
 * <li>多项式(Poly): 0x09</li>
 * <li>初始值(Init): 0x09</li>
 * <li>输入反转(Refin): False</li>
 * <li>输出反转(Refout): False</li>
 * <li>结果异或(Xorout): 0x00</li>
 * <li>别名(Alias):</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC5_EPC extends Verifier {

	byte crc = 0x48;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) != 0) {
				// 0x48=0x09<<(8-5)
				crc = (byte) ((crc << 1) ^ 0x48);
			} else {
				crc <<= 1;
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc >> 3 & 0x1F);
	}

	@Override
	public void reset() {
		// 0x48=0x09<<(8-5)
		crc = 0x48;
	}
}