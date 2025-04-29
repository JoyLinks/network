/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 消息
 * 
 * @author ZhangXi
 * @date 2021年10月8日
 */
public abstract class Message {

	// 编解码状态，表示当前编解码正在处理的部分

	final static int COMMAND = 1;
	final static int HEADERS = 2;
	final static int CONTENT = 3;
	final static int COMPLETE = 4;

	private int state = 0;
	private int id = -1;

	public Message() {
	}

	public Message(int id) {
		this.id = id;
	}

	public Message(int id, int state) {
		this.state = state;
		this.id = id;
	}

	/**
	 * 获取消息状态
	 */
	public int state() {
		return state;
	}

	/**
	 * 设置消息状态
	 */
	void state(int value) {
		state = value;
	}

	/** 获取流标识 */
	public int id() {
		return id;
	}

	/** 设置流标识 */
	void id(int value) {
		id = value;
	}

	////////////////////////////////////////////////////////////////////////////////

	private Object content;

	/**
	 * 消息是否携带内容实体
	 */
	public boolean hasContent() {
		return content != null;
	}

	/**
	 * 获取消息内容实体
	 * 
	 * @return 可能是DataBuffer, InputStream, File, byte[]
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * 获取消息内容大小（估计字节数）
	 */
	public int getContentSize() throws IOException {
		if (content == null) {
			return 0;
		}
		if (content instanceof DataBuffer) {
			return ((DataBuffer) content).readable();
		}
		if (content instanceof InputStream) {
			return ((InputStream) content).available();
		}
		if (content instanceof CharSequence) {
			return ((CharSequence) content).length();
		}
		if (content instanceof byte[]) {
			return ((byte[]) content).length;
		}
		return -1;
	}

	/**
	 * 设置消息的内容
	 * 
	 * @param value 可以是DataBuffer, InputStream, File, byte[]
	 */
	public void setContent(Object value) {
		content = value;
	}

	public void clearContent() throws Exception {
		if (content != null) {
			close(content);
			content = null;
		}
	}

	/**
	 * 关闭/释放消息携带的实体内容
	 */
	public static void close(Object value) throws Exception {
		if (value == null) {
			return;
		} else if (value instanceof Message) {
			((Message) value).clearContent();
		} else if (value instanceof DataBuffer) {
			((DataBuffer) value).release();
		} else if (value instanceof Closeable) {
			((AutoCloseable) value).close();
		} else if (value instanceof Collection<?>) {
			for (Object item : (Collection<?>) value) {
				close(item);
			}
		}
	}

	public void reset() throws Exception {
		close(content);
		state = COMMAND;
	}
}