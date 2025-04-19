package com.joyzl.network.http;

/**
 * HTTP/2 Settings 通信配置参数
 * 
 * @author ZhangXi 2025年3月31日
 */
class Settings extends Message {

	final static Settings SETTINGS_ACK = new Settings();

	/** 动态头压缩表的最大大小 */
	private int headerTableSize = 4096;
	/** 允许服务器推送 */
	private boolean enablePush = true;
	/** 允许的最大并发流 */
	private int maxConcurrentStreams = 100;
	/** 发送方的初始窗口大小 */
	private int initialWindowSize = 65535;
	/** 最大帧有效负载 */
	private int maxFrameSize = 16384;
	/** 帧头列表的最大数量 */
	private int maxHeaderListSize = Integer.MAX_VALUE;

	public void forServer() {
		headerTableSize = 4096;
		maxConcurrentStreams = 100;
		initialWindowSize = 65535;

		maxFrameSize = 16384;
		maxHeaderListSize = 4096;
	}

	public int getMaxHeaderListSize() {
		return maxHeaderListSize;
	}

	public void setMaxHeaderListSize(int value) {
		maxHeaderListSize = value;
	}

	public int getMaxFrameSize() {
		return maxFrameSize;
	}

	public void setMaxFrameSize(int value) {
		maxFrameSize = value;
	}

	public int getInitialWindowSize() {
		return initialWindowSize;
	}

	public void setInitialWindowSize(int value) {
		initialWindowSize = value;
	}

	public int getMaxConcurrentStreams() {
		return maxConcurrentStreams;
	}

	public void setMaxConcurrentStreams(int value) {
		maxConcurrentStreams = value;
	}

	public boolean isEnablePush() {
		return enablePush;
	}

	public void setEnablePush(boolean value) {
		enablePush = value;
	}

	public int getHeaderTableSize() {
		return headerTableSize;
	}

	public void setHeaderTableSize(int value) {
		headerTableSize = value;
	}
}