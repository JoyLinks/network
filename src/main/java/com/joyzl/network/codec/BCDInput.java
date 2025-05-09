/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.codec;

import java.io.IOException;

import com.joyzl.codec.DataInput;

/**
 * BCD(Binary Coded Decimal)单个字节解码读取，与字节序无关
 * 
 * @author ZhangXi
 * @date 2023年7月29日
 */
public interface BCDInput extends DataInput {
	/**
	 * BCD 2421
	 * 
	 * <pre>
	 * VALUE  BCD
	 * +-+----+--+
	 * |0|0000| 0|
	 * |1|0001| 1|
	 * |2|0010| 2|
	 * |3|0011| 3|
	 * |4|0100| 4|
	 * |5|1011|11|
	 * |6|1100|12|
	 * |7|1101|13|
	 * |8|1110|14|
	 * |9|1111|15|
	 * +-+----+--+
	 * </pre>
	 */
	final byte[] BCD2421 = new byte[] { 0, 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 5, 6, 7, 8, 9 };
	/**
	 * BCD 5421
	 * 
	 * <pre>
	 * VALUE  BCD
	 * +-+----+--+
	 * |0|0000| 0|
	 * |1|0001| 1|
	 * |2|0010| 2|
	 * |3|0011| 3|
	 * |4|0100| 4|
	 * |5|1000| 8|
	 * |6|1001| 9|
	 * |7|1010|10|
	 * |8|1011|11|
	 * |9|1100|12|
	 * +-+----+--+
	 * </pre>
	 */
	final byte[] BCD5421 = new byte[] { 0, 1, 2, 3, 4, 0, 0, 0, 5, 6, 7, 8, 9 };
	/**
	 * BCD 5211
	 * 
	 * <pre>
	 * VALUE  BCD
	 * +-+----+--+
	 * |0|0000| 0|
	 * |1|0001| 1|
	 * |2|0100| 4|
	 * |3|0101| 5|
	 * |4|0111| 7|
	 * |5|1000| 8|
	 * |6|1001| 9|
	 * |7|1100|12|
	 * |8|1101|13|
	 * |9|1111|15|
	 * +-+----+--+
	 * </pre>
	 */
	final byte[] BCD5211 = new byte[] { 0, 1, 0, 0, 2, 3, 0, 4, 5, 6, 0, 0, 7, 8, 0, 9 };
	/**
	 * BCD 余3循环
	 * 
	 * <pre>
	 * VALUE  BCD
	 * +-+----+--+
	 * |0|0010| 2|
	 * |1|0110| 6|
	 * |2|0111| 7|
	 * |3|0101| 5|
	 * |4|0100| 4|
	 * |5|1100|12|
	 * |6|1101|13|
	 * |7|1111|15|
	 * |8|1110|14|
	 * |9|1010|10|
	 * +-+----+--+
	 * </pre>
	 */
	final byte[] BCD3C = new byte[] { 0, 0, 0, 0, 4, 3, 1, 2, 0, 0, 9, 0, 5, 6, 8, 7 };

	/** @see #readBCD8421(int) */
	default int readBCD() throws IOException {
		return readBCD8421();
	}

	/**
	 * BCD8421字节解码为数值，范围0~99
	 */
	default int readBCD8421() throws IOException {
		int value = readUnsignedByte();
		return (value >>> 4) * 10 + (value & 0x0F);
	}

	/**
	 * BCD余3码字节解码为数值，范围0~99
	 */
	default int readBCD3() throws IOException {
		int value = readUnsignedByte();
		return ((value >>> 4) - 3) * 10 + (value & 0x0F) - 3;
	}

	/**
	 * BCD2421字节解码为数值，范围0~99
	 */
	default int readBCD2421() throws IOException {
		int value = readUnsignedByte();
		int a = value >>> 4;
		int b = value & 0x0F;
		if (a > 10) {
			a -= 6;
		}
		if (b > 10) {
			b -= 6;
		}
		return a * 10 + b;
	}

	/**
	 * BCD5421字节解码为数值，范围0~99
	 */
	default int readBCD5421() throws IOException {
		int value = readUnsignedByte();
		int a = value >>> 4;
		int b = value & 0x0F;
		if (a > 7) {
			a -= 3;
		}
		if (b > 7) {
			b -= 3;
		}
		return a * 10 + b;
	}

	/**
	 * BCD5211字节解码为数值，范围0~99
	 */
	default int readBCD5211() throws IOException {
		int value = readUnsignedByte();
		return BCD5211[value >>> 4] * 10 + BCD5211[value & 0x0F];
	}

	/**
	 * BCD余3循环字节解码为数值，范围0~99
	 */
	default int readBCD3C() throws IOException {
		int value = readUnsignedByte();
		return BCD3C[value >>> 4] * 10 + BCD3C[value & 0x0F];
	}
}