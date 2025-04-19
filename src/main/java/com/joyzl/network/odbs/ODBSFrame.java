/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

/**
 * ODBS 编解码
 * 
 * <pre>
 * 帧结构
 * +--------+----------+-------+-------------+
 * | HEAD 1 | LENGTH 4 | TAG 4 | DATA ODBS n |
 * +--------+----------+-------+-------------+
 * </pre>
 * 
 * @author ZhangXi 2019年7月15日
 *
 */
public abstract class ODBSFrame {

	/** 帧最小长度(字节) */
	public final static int MIN_FRAME = 1 + 4 + 4;
	/** 帧最大长度(64M 字节) */
	public final static int MAX_FRAME = 1024 * 1024 * 64;
	/** 帧数据最大长度(字节) */
	public final static int MAX_LENGTH = MAX_FRAME - MIN_FRAME;
	/** 帧开始标记 */
	public final static byte HEAD = (byte) 0xFE;
	/** 帧结束标记 */
	public final static byte FOOT = (byte) 0xEF;

}