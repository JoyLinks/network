package com.joyzl.network.session;

/**
 * 倒计时时间戳，超过时间未使用则过期移除
 * 
 * @author ZhangXi 2025年2月17日
 * @param <T>
 */
public class SessionTimely<T extends Timely<T>> extends Session<T> {

	@Override
	protected Timely<T> wrap(T t) {
		return t;
	}
}