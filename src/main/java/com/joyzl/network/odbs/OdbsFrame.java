/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.ChainHandler;

/**
 * ODBS 编解码
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月15日
 *
 */
public abstract class OdbsFrame<M extends ODBSMessage> implements ChainHandler<M> {

	/** 帧最小长度(字节) */
	public final static int MIN_FRAME = 1 + 3 + 1;
	/** 帧最大长度(字节) */
	public final static int MAX_FRAME = 65536 * 256;// 16384 Kb / 16Mb
	/** 帧数据最大长度(字节) */
	public final static int MAX_LENGTH = MAX_FRAME - MIN_FRAME;
	/** 帧开始标记 */
	public final static byte HEAD = (byte) 0xFE;
	/** 帧结束标记 */
	public final static byte FOOT = (byte) 0xEF;

}