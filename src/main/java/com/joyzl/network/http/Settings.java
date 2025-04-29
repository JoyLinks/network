package com.joyzl.network.http;

/**
 * HTTP/2 Settings 通信配置参数
 * 
 * @author ZhangXi 2025年3月31日
 */
class Settings extends Message {

	/** 默认动态头压缩表大小 */
	public final static int DEFAULT_HEADER_TABLE_SIZE = 4096;
	/** 默认窗口大小 */
	public final static int DEFAULT_WINDOW_SIZE = 65535;
	/** 最大窗口大小 */
	public final static int MAX_WINDOW_SIZE = Integer.MAX_VALUE;
	/** 帧默认最大长度 16k */
	public final static int DEFAULT_FRAME_SIZE = 16384;
	/** 帧允许最大长度(2^24-1) 16m */
	final static int MAX_FRAME_SIZE = 16777215;

	private boolean ack = false;

	/** 动态头压缩表大小 */
	private int headerTableSize = -1;
	/** 允许服务器推送 */
	private int enablePush = -1;
	/** 允许的最大并发流 */
	private int maxConcurrentStreams = -1;
	/** 发送方的初始窗口大小 */
	private int initialWindowSize = -1;
	/** 最大帧有效负载 */
	private int maxFrameSize = -1;
	/** 帧头列表的最大数量 */
	private int maxHeaderListSize = -1;

	public Settings() {
		super(0, COMPLETE);
	}

	public Settings(boolean ack) {
		super(0, COMPLETE);
		this.ack = ack;
	}

	public Settings forServer() {
		headerTableSize = 4096;
		maxConcurrentStreams = 100;
		initialWindowSize = 65535;
		maxFrameSize = 16384;
		maxHeaderListSize = 4096;
		return this;
	}

	public Settings forACK() {
		ack = true;
		return this;
	}

	public boolean isACK() {
		return ack;
	}

	public boolean valid() {
		if (hasInitialWindowSize()) {
			if (validInitialWindowSize()) {
			} else {
				return false;
			}
		}
		if (hasMaxFrameSize()) {
			if (validMaxFrameSize()) {
			} else {
				return false;
			}
		}
		if (hasEnablePush()) {
			if (validEnablePush()) {
			} else {
				return false;
			}
		}
		return true;
	}

	public int getMaxHeaderListSize() {
		return maxHeaderListSize;
	}

	public void setMaxHeaderListSize(int value) {
		maxHeaderListSize = value;
	}

	public boolean hasMaxHeaderListSize() {
		return maxHeaderListSize >= 0;
	}

	public int getMaxFrameSize() {
		return maxFrameSize;
	}

	public void setMaxFrameSize(int value) {
		maxFrameSize = value;
	}

	public boolean hasMaxFrameSize() {
		return maxFrameSize >= 0;
	}

	public boolean validMaxFrameSize() {
		if (maxFrameSize < DEFAULT_FRAME_SIZE) {
			return false;
		}
		if (maxFrameSize > MAX_FRAME_SIZE) {
			return false;
		}
		return true;
	}

	public int getInitialWindowSize() {
		return initialWindowSize;
	}

	public void setInitialWindowSize(int value) {
		initialWindowSize = value;
	}

	public boolean hasInitialWindowSize() {
		return initialWindowSize != -1;
	}

	public boolean validInitialWindowSize() {
		if (initialWindowSize < 0) {
			return false;
		}
		return true;
	}

	public int getMaxConcurrentStreams() {
		return maxConcurrentStreams;
	}

	public void setMaxConcurrentStreams(int value) {
		maxConcurrentStreams = value;
	}

	public boolean hasMaxConcurrentStreams() {
		return maxConcurrentStreams >= 0;
	}

	public int getEnablePush() {
		return enablePush;
	}

	public void setEnablePush(int value) {
		enablePush = value;
	}

	public boolean isEnablePush() {
		return enablePush == 1;
	}

	public boolean hasEnablePush() {
		return enablePush >= 0;
	}

	public boolean validEnablePush() {
		if (enablePush < 0 || enablePush > 1) {
			return false;
		}
		return true;
	}

	public int getHeaderTableSize() {
		return headerTableSize;
	}

	public void setHeaderTableSize(int value) {
		headerTableSize = value;
	}

	public boolean hasHeaderTableSize() {
		return headerTableSize >= 0;
	}

	@Override
	public String toString() {
		if (ack) {
			return "SETTINGS:ACK";
		} else {
			final StringBuilder builder = new StringBuilder();
			builder.append("SETTINGS:");
			if (hasHeaderTableSize()) {
				builder.append("headerTableSize=");
				builder.append(headerTableSize);
			}
			if (hasEnablePush()) {
				if (builder.length() > 9) {
					builder.append(',');
				}
				builder.append("enablePush=");
				builder.append(enablePush);
			}
			if (hasMaxConcurrentStreams()) {
				if (builder.length() > 9) {
					builder.append(',');
				}
				builder.append("maxConcurrentStreams=");
				builder.append(maxConcurrentStreams);
			}
			if (hasInitialWindowSize()) {
				if (builder.length() > 9) {
					builder.append(',');
				}
				builder.append("initialWindowSize=");
				builder.append(initialWindowSize);
			}
			if (hasMaxFrameSize()) {
				if (builder.length() > 9) {
					builder.append(',');
				}
				builder.append("maxFrameSize=");
				builder.append(maxFrameSize);
			}
			if (hasMaxHeaderListSize()) {
				if (builder.length() > 9) {
					builder.append(',');
				}
				builder.append("maxHeaderListSize=");
				builder.append(maxHeaderListSize);
			}
			return builder.toString();
		}
	}
}