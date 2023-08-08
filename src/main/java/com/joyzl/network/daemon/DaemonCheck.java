/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.daemon;

/**
 * 守护检查默认实现
 * 
 * @author ZhangXi
 * @date 2022年4月14日
 */
public abstract class DaemonCheck implements Checkable {

	private long timestamp = 0;
	private long counts = 0;
	private int spend = 0;

	public DaemonCheck() {
		Daemones.register(this);
	}

	public void start() {
		timestamp = System.currentTimeMillis();
		counts++;
	}

	public void finish() {
		spend = (int) (System.currentTimeMillis() - timestamp);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getCounts() {
		return counts;
	}

	public int getSpend() {
		return spend;
	}
}