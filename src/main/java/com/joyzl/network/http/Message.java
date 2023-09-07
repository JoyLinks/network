/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import com.joyzl.network.CaseInsensitiveMap;
import com.joyzl.network.buffer.DataBuffer;

/**
 * Request和Response的父类
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

	private final Map<String, String> headers = new CaseInsensitiveMap<>();

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addHeader(Header value) {
		headers.put(value.getHeaderName(), value.getHeaderValue());
	}

	public boolean hasHeader(String name) {
		return headers.containsKey(name);
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public Map<String, String> getHeaders() {
		return headers;
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
	 * 计算消息内容长度(字节数量)
	 * 
	 * @return null返回0,-1表示无法识别的内容类型
	 * @throws IOException
	 */
	public int contentSize() throws IOException {
		if (content == null) {
			return 0;
		} else if (content instanceof DataBuffer) {
			return ((DataBuffer) content).readable();
		} else if (content instanceof InputStream) {
			return ((InputStream) content).available();
		} else if (content instanceof File) {
			return (int) ((File) content).length();
		} else if (content instanceof byte[]) {
			return ((byte[]) content).length;
		} else {
			return -1;
		}
	}

	/**
	 * 关闭消息携带的实体内容
	 */
	public static void close(Object value) {
		if (value == null) {
			return;
		} else if (value instanceof DataBuffer) {
			((DataBuffer) value).release();
		} else if (value instanceof Message) {
			close(((Message) value).getContent());
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