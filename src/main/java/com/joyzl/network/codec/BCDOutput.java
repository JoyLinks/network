/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.codec;

import java.io.IOException;

import com.joyzl.codec.DataOutput;

/**
 * BCD(Binary Coded Decimal)单个字节编码写入，与字节序无关
 * 
 * @author ZhangXi
 * @date 2023年7月29日
 */
public interface BCDOutput extends DataOutput {
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
	final byte[] BCD2421 = new byte[] { 0, 1, 2, 3, 4, 11, 12, 13, 14, 15 };
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
	final byte[] BCD5421 = new byte[] { 0, 1, 2, 3, 4, 8, 9, 10, 11, 12 };
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
	final byte[] BCD5211 = new byte[] { 0, 1, 4, 5, 7, 8, 9, 12, 13, 15 };
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
	final byte[] BCD3C = new byte[] { 0, 1, 4, 5, 7, 8, 9, 12, 13, 15 };

	/** @see #writeBCD8421(int) */
	default void writeBCD(int value) throws IOException {
		writeBCD8421(value);
	}

	/**
	 * 数值编码为BCD8421字节，范围0~99，不足2位值自动左补零
	 * 
	 * <pre>
	 * VALUE:98
	 * BYTES:
	 * +--+
	 * |98|
	 * +--+
	 * </pre>
	 */
	default void writeBCD8421(int value) throws IOException {
		writeByte((value / 10) << 4 | (value % 10));
	}

	/**
	 * 数值编码为BCD余3码字节，范围0~99，不足2位值自动左补零
	 * 
	 * <pre>
	 * VALUE:98
	 * BYTES:
	 * +--+
	 * |CB|
	 * +--+
	 * </pre>
	 */
	default void writeBCD3(int value) throws IOException {
		writeByte((value / 10 + 3) << 4 | (value % 10 + 3));
	}

	/**
	 * 数值编码为BCD2421字节，范围0~99，不足2位值自动左补零
	 * 
	 * <pre>
	 * VALUE:98
	 * BYTES:
	 * +--+
	 * |FE|
	 * +--+
	 * </pre>
	 */
	default void writeBCD2421(int value) throws IOException {
		writeByte(BCD2421[value / 10] << 4 | BCD2421[value % 10]);
	}

	/**
	 * 数值编码为BCD5421字节，范围0~99，不足2位值自动左补零
	 * 
	 * <pre>
	 * VALUE:98
	 * BYTES:
	 * +--+
	 * |CB|
	 * +--+
	 * </pre>
	 */
	default void writeBCD5421(int value) throws IOException {
		writeByte(BCD5421[value / 10] << 4 | BCD5421[value % 10]);
	}

	/**
	 * 数值编码为BCD5211字节，范围0~99，不足2位值自动左补零
	 * 
	 * <pre>
	 * VALUE:98
	 * BYTES:
	 * +--+
	 * |FD|
	 * +--+
	 * </pre>
	 */
	default void writeBCD5211(int value) throws IOException {
		writeByte(BCD5211[value / 10] << 4 | BCD5211[value % 10]);
	}

	/**
	 * 数值编码为BCD余3循环字节，范围0~99，不足2位值自动左补零
	 * 
	 * <pre>
	 * VALUE:98
	 * BYTES:
	 * +--+
	 * |FD|
	 * +--+
	 * </pre>
	 */
	default void writeBCD3C(int value) throws IOException {
		writeByte(BCD3C[value / 10] << 4 | BCD3C[value % 10]);
	}
}