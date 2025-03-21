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