/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * CRC-8/MAXIM
 * <ul>
 * <li>名称(Name): CRC-8/MAXIM</li>
 * <li>公式(Formula): x8+x5+x4+1</li>
 * <li>多项式(Poly): 0x31</li>
 * <li>初始值(Init): 0x00</li>
 * <li>输入反转(Refin): True</li>
 * <li>输出反转(Refout): True</li>
 * <li>结果异或(Xorout): 0x00</li>
 * <li>别名(Alias): DOW-CRC,CRC-8/IBUTTON</li>
 * <li>备注(Note): Maxim(Dallas)'s some devices,e.g. DS18B20</li>
 * </ul>
 * 
 * @author ZhangXi
 *
 */
public final class CRC8_MAXIM extends Verifier {

	byte crc = 0;

	@Override
	public byte check(byte value) {
		crc ^= value;
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 0) {
				crc = (byte) ((crc & 0xFF) >> 1);
			} else {
				crc = (byte) (((crc & 0xFF) >> 1) ^ 0x8C);
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