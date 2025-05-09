/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.Chain;

/**
 * ODBS Message 提供消息状态
 * 
 * @author ZhangXi
 * @date 2021年10月20日
 */
public abstract class ODBSMessage {

	private int tag = 0, status = EXECUTE;
	private Chain chain = null;

	/** 消息标识 */
	public int tag() {
		return tag;
	}

	void tag(int value) {
		tag = value;
	}

	/**
	 * 获取消息的来源链路，当消息需要在多个链路发送时，可通过来源链路判断消息来源；
	 * 主动请求方发送的消息携带着消息的本地序列标识，回复给请求者时需要原样返回序列标识；
	 * 如果消息需要同时发送给其它链路（群发给非发起方），此时本地序列应忽略。
	 */
	public Chain chain() {
		return chain;
	}

	/**
	 * 接收数据并解析完成后，设置消息的来源链路，此链路表示此消息来源
	 */
	void chain(Chain value) {
		chain = value;
	}

	/** 状态：待执行 */
	public static final int EXECUTE = 0;
	/** 状态：已转发 */
	public static final int FORWARD = 1;
	/** 状态：成功 */
	public static final int SUCCESS = 2;
	/** 状态：网络 */
	public static final int NETWORK = 3;
	/** 状态：超时 */
	public final static int TIMEOUT = 4;
	/** 状态：失败 */
	public static final int FAILURE = 5;

	public void setStatus(int value) {
		status = value;
	}

	public int getStatus() {
		return status;
	}

	/**
	 * 是否待执行，EXECUTE和FORWARD均为待执行
	 */
	public final boolean execution() {
		return status == EXECUTE || status == FORWARD;
	}

	/**
	 * 是否已转发，消息已转发其它端执行
	 */
	public final boolean forwarded() {
		return status == FORWARD;
	}

	/**
	 * 是否成功，仅表示消息成功，业务状态须额外判断
	 */
	public final boolean success() {
		return status == SUCCESS;
	}
}