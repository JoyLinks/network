package com.joyzl.network.http;

/**
 * HTTP2 流优先级
 * 
 * @author ZhangXi 2025年4月24日
 */
public class Priority extends Message {

	private boolean exclusive;
	private int dependency;
	private byte weight;

	public Priority(int id) {
		super(id, COMPLETE);
	}

	public byte getWeight() {
		return weight;
	}

	public void setWeight(byte value) {
		weight = value;
	}

	public int getDependency() {
		return dependency;
	}

	public void setDependency(int value) {
		dependency = value;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean value) {
		exclusive = value;
	}

	@Override
	public String toString() {
		return "PRIORITY:exclusive=" + exclusive + ",dependency=" + dependency + ",weight=" + weight;
	}
}