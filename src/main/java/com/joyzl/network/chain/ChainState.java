/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

/**
 * 链路状态
 * 
 * @author ZhangXi
 * @date 2021年4月18日
 */
public enum ChainState {
	FREE,

	CONNECTING,
	CONNECTED,

	READING,
	WRITING,

	TIMEOUT_WRITE,
	TIMEOUT_READ,

	DISCONNECTING,
	DISCONNECTED,

	CLOSING,
	CLOSED
}