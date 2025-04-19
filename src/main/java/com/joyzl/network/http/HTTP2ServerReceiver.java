package com.joyzl.network.http;

/**
 * HTTP2 服务端流接收
 * 
 * @author ZhangXi 2025年4月16日
 */
class HTTP2ServerReceiver extends HTTP2Receiver<Request> {

	// 流标识符零(0x0)用于连接控制消息
	// 客户端发起使用奇数 1357 Request
	// 服务端响应使用奇数 1357 Response
	// 服务端发起使用偶数 2468 Push
	// 流编号只增不减，用尽后GOAWAY

	/** 奇数流编号转换为存储索引 */
	static int oddIndex(int odd) {
		// 奇数转换为索引 (odd-1)/2=index
		// 服务端只会接收到奇数编号
		return (odd - 1) / 2;
	}

	/** 存储索引转换为奇数流编号 */
	static int indexOdd(int index) {
		return index * 2 + 1;
	}

	@Override
	public Request get(int i) {
		return super.get(oddIndex(i));
	}

	@Override
	public void add(Request m, int i) {
		super.add(m, oddIndex(i));
	}

	@Override
	public Request remove(int i) {
		return super.remove(oddIndex(i));
	}

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