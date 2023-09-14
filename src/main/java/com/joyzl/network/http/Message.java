/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.Closeable;
import java.util.Collection;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 消息
 * 
 * @author ZhangXi
 * @date 2021年10月8日
 */
public abstract class Message {

	public final static int COMMAND = 1;
	public final static int HEADERS = 2;
	public final static int CONTENT = 3;
	public final static int COMPLETE = 100;

	private int state = COMMAND;

	public int state() {
		return state;
	}

	public void state(int value) {
		state = value;
	}

	////////////////////////////////////////////////////////////////////////////////

	private Object content;

	public boolean hasContent() {
		return content != null;
	}

	public Object getContent() {
		return content;
	}

	/**
	 * 设置消息的内容
	 * 
	 * @param value 可以是DataBuffer, InputStream, File, byte[]
	 */
	public void setContent(Object value) {
		if (value == null) {
			close(content);
		}
		content = value;
	}

	/**
	 * 关闭消息携带的实体内容
	 */
	public static void close(Object value) {
		if (value == null) {
			return;
		} else if (value instanceof Message) {
			close(((Message) value).getContent());
		} else if (value instanceof DataBuffer) {
			((DataBuffer) value).release();
		} else if (value instanceof Closeable) {
			try {
				((AutoCloseable) value).close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (value instanceof Collection<?>) {
			for (Object item : (Collection<?>) value) {
				close(item);
			}
		}
	}
}