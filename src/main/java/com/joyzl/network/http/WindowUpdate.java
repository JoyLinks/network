/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

public class WindowUpdate extends Message {

	private int increment;

	public WindowUpdate(int id) {
		super(id);
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int value) {
		increment = value;
	}

	@Override
	public String toString() {
		return "WINDOW_UPDATE:increment=" + increment;
	}
}