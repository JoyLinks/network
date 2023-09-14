/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.websocket;

import com.joyzl.network.http.Message;

public class WebSocketMessage extends Message {

	// OPCODE
	final public static int CONTINUATION = 0x00;
	final public static int TEXT = 0x01;
	final public static int BINARY = 0x02;
	final public static int CLOSE = 0x08;
	final public static int PING = 0x09;
	final public static int PONG = 0x0A;

	private int opcode;
	private int length;
	private boolean finish;
	private boolean mask;
	private final byte[] mask_keys = new byte[4];

	/**
	 * 获取消息类型
	 * 
	 * @return {@link #CONTINUATION}, {@link #TEXT}, {@link #BINARY},
	 *         {@link #CLOSE}, {@link #PING}, {@link #PONG}
	 */
	public int getType() {
		return opcode;
	}

	/**
	 * 设置消息类型
	 * 
	 * @param value {@link #CONTINUATION}, {@link #TEXT}, {@link #BINARY},
	 *            {@link #CLOSE}, {@link #PING}, {@link #PONG}
	 */
	public void setType(int value) {
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

	protected boolean isFinish() {
		return finish;
	}

	protected void setFinish(boolean value) {
		finish = value;
	}

	protected int getLength() {
		return length;
	}

	protected void setLength(int value) {
		length = value;
	}

	protected boolean isMask() {
		return mask;
	}

	protected void setMask(boolean mask) {
		this.mask = mask;
	}

	protected byte[] getMaskKeys() {
		return mask_keys;
	}

	public void clear() {
		opcode = 0;
		length = 0;
		finish = false;
		mask = false;
	}
}
