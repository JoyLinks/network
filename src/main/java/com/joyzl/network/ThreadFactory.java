/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network;

/**
 * 线程创建工厂
 * 
 * @author ZhangXi
 * @date 2021年4月7日
 */
public class ThreadFactory implements java.util.concurrent.ThreadFactory {

	private final String name;
	private int number = 1;

	public ThreadFactory(String prefix) {
		name = prefix;
	}

	private synchronized int nextNumber() {
		return number++;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, name + nextNumber());
	}
}