/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.codec;

/**
 * 字节(二进制)转换工具类
 * <p>
 * 默认大端序(big-endian,高位字节在低位字节前面)[76543210]最高有效字节(MSB)~后面跟着最低有效字节(LSB)
 * </p>
 * 
 * @author ZhangXi
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

	/** 读取字节位 1为true,0为fasle,左高右低index[63~0] */
	public static boolean getBit(long source, int index) {
		return (source >> index & 1) == 1;
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

	/** 设置字节位 0为false,1为true,左高右低index[15~0] */
	public static short setBit(short source, boolean value, int index) {
		short mask = (short) (1 << index);
		if (value) {
			source |= mask;
		} else {
			source &= (~mask);
		}
		return source;
	}

	/** 设置字节位 0为false,1为true,左高右低index[31~0] */
	public static int setBit(int source, boolean value, int index) {
		int mask = 1 << index;
		if (value) {
			source |= mask;
		} else {
			source &= (~mask);
		}
		return source;
	}

	/** 设置字节位 0为false,1为true,左高右低index[63~0] */
	public static long setBit(long source, boolean value, int index) {
		long mask = 1L << index;
		if (value) {
			source |= mask;
		} else {
			source &= (~mask);
		}
		return source;
	}

	/** 获取4Bit低四位(3210)表示的无符号整数 */
	public static byte get4BL(byte source) {
		return (byte) (source & 0x0F);
	}

	/** 获取4Bit高四位(7654)表示的无符号整数 */
	public static byte get4BM(byte source) {
		return (byte) ((source & 0xF0) >>> 4);
	}

	/** 设置4Bit低四位(3210)表示的无符号整数 */
	public static byte set4BL(byte source, byte value) {
		return (byte) ((source & 0xF0) | (value & 0x0F));
	}

	/** 设置4Bit高四位(7654)表示的无符号整数 */
	public static byte set4BM(byte source, byte value) {
		return (byte) ((source & 0x0F) | (value << 4));
	}

	/** 获取指定位置字节值[10] */
	public static byte getByte(short source, int index) {
		return (byte) (source >>> index * 8);
	}

	/** 获取指定位置字节值[3210] */
	public static byte getByte(int source, int index) {
		return (byte) (source >>> index * 8);
	}

	/** 获取指定位置字节值[76543210] */
	public static byte getByte(long source, int index) {
		return (byte) (source >>> index * 8);
	}

	/** 设置指定位置字节值[10] */
	public static short setByte(short source, byte value, int index) {
		return (short) ((value & 0xFF) << index * 8 | source);
	}

	/** 设置指定位置字节值[3210] */
	public static int setByte(int source, byte value, int index) {
		return (value & 0xFF) << index * 8 | source;
	}

	/** 设置指定位置字节值[76543210] */
	public static long setByte(long source, byte value, int index) {
		return (value & 0xFFL) << index * 8 | source;
	}

	/** 合并字节[10] */
	public static short join(byte b1, byte b0) {
		return (short) ((b1 & 0xFF) << 8 | b0 & 0xFF);
	}

	/** 合并字节[3210] */
	public static int join(byte b3, byte b2, byte b1, byte b0) {
		return (b3 & 0xFF) << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | b0 & 0xFF;
	}

	/** 合并字节[76543210] */
	public static long join(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
		return (b7 & 0xFFL) << 56 | (b6 & 0xFFL) << 48 | (b5 & 0xFFL) << 40 | (b4 & 0xFF) << 32 | (b3 & 0xFF) << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | b0 & 0xFF;
	}

	/** 分离字节[10] */
	public static byte[] split(short value) {
		final byte[] item = new byte[2];
		item[0] = (byte) (value >>> 8);
		item[1] = (byte) value;
		return item;
	}

	/** 分离字节[3210] */
	public static byte[] split(int value) {
		final byte[] item = new byte[4];
		item[0] = (byte) (value >>> 24);
		item[1] = (byte) (value >>> 16);
		item[2] = (byte) (value >>> 8);
		item[3] = (byte) value;
		return item;
	}

	/** 分离字节[76543210] */
	public static byte[] split(long value) {
		final byte[] item = new byte[8];
		item[0] = (byte) (value >>> 56);
		item[1] = (byte) (value >>> 48);
		item[2] = (byte) (value >>> 40);
		item[3] = (byte) (value >>> 32);
		item[4] = (byte) (value >>> 24);
		item[5] = (byte) (value >>> 16);
		item[6] = (byte) (value >>> 8);
		item[7] = (byte) value;
		return item;
	}

	/** 分离字节到数组指定位置[index:10] */
	public static void put(byte[] target, int index, short value) {
		target[index++] = (byte) (value >>> 8);
		target[index] = (byte) (value);
	}

	/** 分离字节到数组指定位置[index:3210] */
	public static void put(byte[] target, int index, int value) {
		target[index++] = (byte) (value >>> 24);
		target[index++] = (byte) (value >>> 16);
		target[index++] = (byte) (value >>> 8);
		target[index] = (byte) (value);
	}

	/** 分离字节到数组指定位置[index:76543210] */
	public static void put(byte[] target, int index, long value) {
		target[index++] = (byte) (value >>> 56);
		target[index++] = (byte) (value >>> 48);
		target[index++] = (byte) (value >>> 40);
		target[index++] = (byte) (value >>> 32);
		target[index++] = (byte) (value >>> 24);
		target[index++] = (byte) (value >>> 16);
		target[index++] = (byte) (value >>> 8);
		target[index] = (byte) (value);
	}

	/** 从数组指定位置获取值[index:10] */
	public static short getShort(byte[] target, int index) {
		int value = (target[index++] & 0xFF) << 8;
		value |= target[index] & 0xFF;
		return (short) value;
	}

	/** 从数组指定位置获取值[index:3210] */
	public static int getInteger(byte[] target, int index) {
		int value = (target[index++] & 0xFF) << 24;
		value |= (target[index++] & 0xFF) << 16;
		value |= (target[index++] & 0xFF) << 8;
		value |= target[index] & 0xFF;
		return value;
	}

	/** 从数组指定位置获取值[index:76543210] */
	public static long getLong(byte[] target, int index) {
		long value = (target[index++] & 0xFFL) << 56;
		value |= (target[index++] & 0xFFL) << 48;
		value |= (target[index++] & 0xFFL) << 40;
		value |= (target[index++] & 0xFF) << 32;
		value |= (target[index++] & 0xFF) << 24;
		value |= (target[index++] & 0xFF) << 16;
		value |= (target[index++] & 0xFF) << 8;
		value |= target[index] & 0xFF;
		return value;
	}
}