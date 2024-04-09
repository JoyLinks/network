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
}