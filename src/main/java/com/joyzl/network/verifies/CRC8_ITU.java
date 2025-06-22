/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * CRC-8/ITU
 * <ul>
 * <li>名称(Name): CRC-8/ITU</li>
 * <li>公式(Formula): x8+x2+x+1</li>
 * <li>多项式(Poly): 0x07</li>
 * <li>初始值(Init): 0x00</li>
 * <li>输入反转(Refin): False</li>
 * <li>输出反转(Refout): False</li>
 * <li>结果异或(Xorout): 0x55</li>
 * <li>别名(Alias): CRC-8/ATM</li>
 * <li>备注(Note):</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC8_ITU extends Verifier {

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 0x80) == 0) {
				crc <<= 1;
			} else {
				crc = (byte) ((crc << 1) ^ 0x07);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return (byte) (crc ^ 0x55);
	}

	@Override
	public void reset() {
		crc = 0;
	}
}