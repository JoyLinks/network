/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * CRC32
 * <ul>
 * <li>名称(Name): CRC-32</li>
 * <li>公式(Formula): x32+x26+x23+x22+x16+x12+x11+x10+x8+x7+x5+x4+x2+x+1</li>
 * <li>多项式(Poly): 0x4C11DB7</li>
 * <li>初始值(Init): 0xFFFFFFF</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0xFFFFFFF</li>
 * <li>别名(Alias): CRC_32/ADCCP</li>
 * <li>备注(Note): WinRAR</li>
 * </ul>
 * 
 * @author simon(ZhangXi)
 *
 */
public class CRC32 extends Verifier {

	private int crc = 0xFFFFFFFF;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) != 0) {
				// 0xEDB88320=reverse 0x04C11DB7
				crc = (crc >> 1) ^ 0xEDB88320;
			} else {
				crc = (crc >> 1);
			}
		}
		return value;
	}

	@Override
	public int value() {
		return ~crc;
	}

	@Override
	public void reset() {
		crc = 0xFFFFFFFF;
	}
}