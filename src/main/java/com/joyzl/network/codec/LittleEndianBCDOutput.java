/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.codec;

import java.io.IOException;

import com.joyzl.codec.LittleEndianDataOutput;

/**
 * BCD(Binary Coded Decimal)，小端序(LITTLE_ENDIAN)
 * 
 * @author ZhangXi
 * @date 2023年7月29日
 */
public interface LittleEndianBCDOutput extends BCDOutput, LittleEndianDataOutput {

	/** @see #writeBCD8421s(int) */
	default void writeBCDs(int value) throws IOException {
		writeBCD8421s(value);
	}

	/** @see #writeBCD8421s(CharSequence) */
	default void writeBCDs(CharSequence value) throws IOException {
		writeBCD8421s(value);
	}

	/** @see #writeBCD8421s(CharSequence, int, int) */
	default void writeBCDs(CharSequence value, int offset, int length) throws IOException {
		writeBCD8421s(value, 0, value.length());
	}

	/**
	 * 数值编码为BCD8421字节，不足偶数位自动左补零
	 * 
	 * <pre>
	 * VALUE:987654321
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |21|43|65|87|09|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD8421s(int value) throws IOException {
		while (value > 0) {
			writeBCD8421(value % 100);
			value /= 100;
		}
	}

	/** @see #writeBCD8421s(CharSequence, int, int) */
	default void writeBCD8421s(CharSequence value) throws IOException {
		writeBCD8421s(value, 0, value.length());
	}

	/**
	 * 字符串编码为BCD8421字节，字符串数量不足偶数自动补零
	 * 
	 * <pre>
	 * VALUE:"987654321"
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |21|43|65|87|09|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD8421s(CharSequence value, int offset, int length) throws IOException {
		int b, index = offset + length - 1;
		while (index >= offset) {
			b = Character.digit(value.charAt(index--), 10);
			if (index >= offset) {
				b += Character.digit(value.charAt(index--), 10) * 10;
			}
			writeBCD8421(b);
		}
	}

	/**
	 * 数值编码为BCD余3码字节，不足偶数位自动左补零
	 * 
	 * <pre>
	 * VALUE:987654321
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |54|76|98|BA|0C|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD3s(int value) throws IOException {
		while (value > 0) {
			writeBCD3(value % 100);
			value /= 100;
		}
	}

	/** @see #writeBCD3s(CharSequence, int, int) */
	default void writeBCD3s(CharSequence value) throws IOException {
		writeBCD3s(value, 0, value.length());
	}

	/**
	 * 字符串编码为BCD余3码字节，字符串数量不足偶数自动左补零
	 * 
	 * <pre>
	 * VALUE:"987654321"
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |54|76|98|BA|0C|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD3s(CharSequence value, int offset, int length) throws IOException {
		int b, index = offset + length - 1;
		while (index >= offset) {
			b = Character.digit(value.charAt(index--), 10);
			if (index >= offset) {
				b += Character.digit(value.charAt(index--), 10) * 10;
			}
			writeBCD3(b);
		}
	}

	/**
	 * 数值编码为BCD2421字节，不足偶数位自动左补零
	 * 
	 * <pre>
	 * VALUE:987654321
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |21|43|CB|ED|0F|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD2421s(int value) throws IOException {
		while (value > 0) {
			writeBCD2421(value % 100);
			value /= 100;
		}
	}

	/** @see #writeBCD2421s(CharSequence, int, int) */
	default void writeBCD2421s(CharSequence value) throws IOException {
		writeBCD2421s(value, 0, value.length());
	}

	/**
	 * 字符串编码为BCD2421字节，字符串数量不足偶数自动左补零
	 * 
	 * <pre>
	 * VALUE:"987654321"
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |21|43|CB|ED|0F|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD2421s(CharSequence value, int offset, int length) throws IOException {
		int b, index = offset + length - 1;
		while (index >= offset) {
			b = Character.digit(value.charAt(index--), 10);
			if (index >= offset) {
				b += Character.digit(value.charAt(index--), 10) * 10;
			}
			writeBCD2421(b);
		}
	}

	/**
	 * 数值编码为BCD5421字节，不足偶数位自动左补零
	 * 
	 * <pre>
	 * VALUE:987654321
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |21|43|98|BA|0C|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD5421s(int value) throws IOException {
		while (value > 0) {
			writeBCD5421(value % 100);
			value /= 100;
		}
	}

	/** @see #writeBCD5421s(CharSequence, int, int) */
	default void writeBCD5421s(CharSequence value) throws IOException {
		writeBCD5421s(value, 0, value.length());
	}

	/**
	 * 字符串编码为BCD5421字节，字符串数量不足偶数自动左补零
	 * 
	 * <pre>
	 * VALUE:"987654321"
	 * BYTES:
	 *  0  1  2  3  4
	 * +--+--+--+--+--+
	 * |21|43|98|BA|0C|
	 * +--+--+--+--+--+
	 * </pre>
	 */
	default void writeBCD5421s(CharSequence value, int offset, int length) throws IOException {
		int b, index = offset + length - 1;
		while (index >= offset) {
			b = Character.digit(value.charAt(index--), 10);
			if (index >= offset) {
				b += Character.digit(value.charAt(index--), 10) * 10;
			}
			writeBCD5421(b);
		}
	}
}