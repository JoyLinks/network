package com.joyzl.network.http;

import com.joyzl.network.IndexMap;

/**
 * HTTP2 标识消息集合
 * 
 * @author ZhangXi 2025年4月25日
 * @param <V>
 */
public class HTTP2Index<V extends Message> extends IndexMap<V> {

	private int lastOpen = 0, lastClose = 0;
	private boolean lastContinue = false;

	public HTTP2Index(int capacity) {
		super(capacity);
	}

	@Override
	public void put(int key, V m) {
		super.put(lastOpen = key, m);
		lastContinue = true;
	}

	@Override
	public V remove(int key) {
		return super.remove(lastClose = key);
	}

	public void endHeaders() {
		lastContinue = false;
	}

	public boolean lastContinue() {
		return lastContinue;
	}

	public int lastOpen() {
		return lastOpen;
	}

	/** FLAG:END_STREAM */
	public int lastClose() {
		return lastClose;
	}
}