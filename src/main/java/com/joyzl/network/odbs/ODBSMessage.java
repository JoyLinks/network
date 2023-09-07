/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
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

	private int tag;
	private Chain chain;

	int tag() {
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
	public Chain getChain() {
		return chain;
	}

	/**
	 * 接收数据并解析完成后，设置消息的来源链路，此链路表示此消息来源
	 */
	void setChain(Chain value) {
		chain = value;
	}
}