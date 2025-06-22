/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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