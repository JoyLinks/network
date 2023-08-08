/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.session;

/**
 * 令牌
 * 
 * @author ZhangXi
 * @date 2022年4月14日
 */
final class Token {

	private volatile long time;

	Token(String n) {
		if (n == null) {
			throw new NullPointerException();
		}
		time = System.currentTimeMillis();
	}

	void refresh() {
		time = System.currentTimeMillis();
	}

	boolean isValid(long timeout) {
		return System.currentTimeMillis() - time > timeout;
	}
}