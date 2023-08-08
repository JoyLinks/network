/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 2018年4月23日
 */
package com.joyzl.network.codec;

/**
 * 字节(二进制)转换工具类
 * <p>
 * 默认大端序(big-endian,高位字节在低位字节前面)[76543210]最高有效字节(MSB)~后面跟着最低有效字节(LSB)
 * </p>
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class Binary {

	/** 读取字节位 1为true,0为fasle,左高右低index[76543210] */
	public static boolean getBit(byte source, int index) {
		return (source >> index & 1) == 1;
	}

	/** 读取字节位 1为true,0为fasle,左高右低index[15~0] */
	public static boolean getBit(short source, int index) {
		return (source >> index & 1) == 1;
	}

	/** 读取字节位 1为true,0为fasle,左高右低index[31~0] */
	public static boolean getBit(int source, int index) {
		return (source >> index & 1) == 1;
	}

	/** 读取字节位 1为true,0为fasle,左高右低index[01234567] */
	public static boolean getBitLE(byte source, int index) {
		return getBit(source, 7 - index);
	}

	/** 读取字节位 1为true,0为fasle,左高右低index[0~15] */
	public static boolean getBitLE(short source, int index) {
		return getBit(source, 7 - index);
	}

	/** 读取字节位 1为true,0为fasle,左高右低index[0~31] */
	public static boolean getBitLE(int source, int index) {
		return getBit(source, 7 - index);
	}

	/** 设置字节位 0为false,1为true,左高右低index[76543210] */
	public static byte setBit(byte source, boolean value, int index) {
		byte mask = (byte) (1 << index);
		if (value) {
			source |= mask;
		} else {
			source &= (~mask);
		}
		return source;
	}

	/** 设置字节位 0为false,1为true,左高右低index[01234567] */
	public static byte setBitLE(byte source, boolean value, int index) {
		return setBit(source, value, 7 - index);
	}

	/** 获取4Bit低四位(3210)表示的无符号整数 */
	public static int get4BL(byte source) {
		return source & 0x0F;
	}

	/** 获取4Bit高四位(7654)表示的无符号整数 */
	public static int get4BM(byte source) {
		return (source & 0xF0) >>> 4;
	}

	/** 设置4Bit低四位(3210)表示的无符号整数 */
	public static int set4BL(byte source, byte value) {
		return (source & 0xF0) | (value & 0x0F);
	}

	/** 设置4Bit高四位(7654)表示的无符号整数 */
	public static int set4BM(byte source, byte value) {
		return (source & 0x0F) | (value << 4);
	}

	/** 获取指定位置字节值[10] */
	public static byte getByte(short source, int index) {
		switch (index) {
			case 0:
				return (byte) (source & 0xFF);
			case 1:
				return (byte) (source >>> 8);
			default:
				throw new IndexOutOfBoundsException(index);
		}
	}

	/** 设置字节[10] */
	public static short setByte(byte b1, byte b0) {
		return (short) ((b1 & 0xFF) << 8 | b0 & 0xFF);
	}

	/** 获取指定位置字节值[3210] */
	public static byte getByte(int source, int index) {
		switch (index) {
			case 0:
				return (byte) (source & 0xFF);
			case 1:
				return (byte) (source >>> 8);
			case 2:
				return (byte) (source >>> 16);
			case 3:
				return (byte) (source >>> 24);
			default:
				throw new IndexOutOfBoundsException(index);
		}
	}

	/** 设置字节[3210] */
	public static int setByte(byte b3, byte b2, byte b1, byte b0) {
		return (b3 & 0xFF) << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | b0 & 0xFF;
	}

	/** 获取指定位置字节值[76543210] */
	public static byte getByte(long source, int index) {
		switch (index) {
			case 0:
				return (byte) (source & 0xFF);
			case 1:
				return (byte) (source >>> 8);
			case 2:
				return (byte) (source >>> 16);
			case 3:
				return (byte) (source >>> 24);
			case 4:
				return (byte) (source >>> 32);
			case 5:
				return (byte) (source >>> 40);
			case 6:
				return (byte) (source >>> 48);
			case 7:
				return (byte) (source >>> 56);
			default:
				throw new IndexOutOfBoundsException(index);
		}
	}

	/** 设置字节[76543210] */
	public static long setByte(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
		return (b7 & 0x00000000000000FF) << 56 | (b6 & 0x00000000000000FF) << 48 | (b5 & 0x00000000000000FF) << 40 | (b4 & 0x00000000000000FF) << 32 | (b3 & 0xFF) << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | b0 & 0xFF;
	}
}