/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.session;

/**
 * 用于会话的时效性接口
 * 
 * @author ZhangXi 2025年2月16日
 * @param <T>
 */
public interface Timely<T> {

	/**
	 * 根据当前时间戳检查是否有效期内
	 * 
	 * @param timestamp 检查者传入的当前时间戳
	 * @return true 表示有效，false 已超时
	 */
	boolean valid(long timestamp);

	/**
	 * 获取关联值对象
	 */
	T value();
}