/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

class PreSharedKeySelected extends PreSharedKey {

	private int selected;

	public PreSharedKeySelected() {
	}

	public PreSharedKeySelected(int selected) {
		this.selected = selected;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int value) {
		selected = value;
	}

	@Override
	public String toString() {
		return name() + ":" + selected;
	}
}