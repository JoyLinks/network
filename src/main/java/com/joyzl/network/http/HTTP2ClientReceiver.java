package com.joyzl.network.http;

/**
 * HTTP2 客户端流接收
 * 
 * @author ZhangXi 2025年4月16日
 */
class HTTP2ClientReceiver extends HTTP2Receiver<Response> {

	// 流标识符零(0x0)用于连接控制消息
	// 客户端发起使用奇数 1357 Request
	// 服务端响应使用奇数 1357 Response
	// 服务端发起使用偶数 2468 Push
	// 流编号只增不减，用尽后GOAWAY

	/**
	 * 获取当前并发流数量
	 */
	public int getMaxConcurrent() {
		return capacity();
	}

	/** 最大帧有效负载 */
	private int maxFrameSize;

	public int getMaxFrameSize() {
		return maxFrameSize;
	}

	public void setMaxFrameSize(int value) {
		maxFrameSize = value;
	}
}