/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * WEB Socket 消息
 * 
 * @author ZhangXi 2024年12月11日
 */
public class WEBSocketMessage extends Message {

	// OPCODE

	/** 接续消息 */
	public final static byte CONTINUATION = 0x00;
	/** 字节消息 */
	public final static byte BINARY = 0x02;
	/** 控制消息 */
	public final static byte CLOSE = 0x08;
	/** 控制消息 */
	public final static byte PING = 0x09;
	/** 控制消息 */
	public final static byte PONG = 0x0A;
	/** 文本消息(UTF-8) */
	public final static byte TEXT = 0x01;

	// STATUS

	/** 正常关闭 */
	public final static short NORMAL_CLOSURE = 1000;
	/** 服务器关闭或用户离开浏览 */
	public final static short GOING_AWAY = 1001;
	/** 协议错误 */
	public final static short PROTOCOL_ERROR = 1002;
	/** 数据类型错误 */
	public final static short DATA_TYPE_ERROR = 1003;
	/** 未定义的保留状态 */
	public final static short RESERVED = 1004;
	/** 应设置状态代码 */
	public final static short EXPECTING_STATUS = 1005;
	/** 异常关闭（未发送或收到关闭帧） */
	public final static short ABNORMALLY_CLOSED = 1006;
	/** 数据编码错误 */
	public final static short ENCODING_ERROR = 1007;
	/** 违反策略 */
	public final static short VIOLATES_POLICY = 1008;
	/** 消息太大 */
	public final static short TOO_BIG = 1009;
	/** 扩展错误 */
	public final static short EXTENSION_ERROR = 1010;
	/** 服务异常 */
	public final static short ENCOUNTERED_UNEXPECTED = 1011;
	/** 证书校验失败 */
	public final static short TLS_FAILURE = 1015;

	private byte opcode;
	private short status;

	/**
	 * 获取消息类型
	 * 
	 * @return {@link #CONTINUATION}, {@link #TEXT}, {@link #BINARY},
	 *         {@link #CLOSE}, {@link #PING}, {@link #PONG}
	 */
	public byte getType() {
		return opcode;
	}

	/**
	 * 设置消息类型
	 * 
	 * @param value {@link #CONTINUATION}, {@link #TEXT}, {@link #BINARY},
	 *            {@link #CLOSE}, {@link #PING}, {@link #PONG}
	 */
	public void setType(byte value) {
		opcode = value;
	}

	/**
	 * 指示消息内容是否文本
	 */
	public boolean isText() {
		return opcode == TEXT;
	}

	/**
	 * 指示消息内容是否二进（字节）
	 */
	public boolean isBinary() {
		return opcode == BINARY;
	}

	/**
	 * 获取连接关闭原因代码，仅在CLOSE控制帧有效
	 */
	public short getStatus() {
		return status;
	}

	/**
	 * 设置连接关闭原因代码，仅在CLOSE控制帧有效
	 */
	public void setStatus(short value) {
		status = value;
	}
}