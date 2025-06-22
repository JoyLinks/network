/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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