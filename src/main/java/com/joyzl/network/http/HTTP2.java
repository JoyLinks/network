package com.joyzl.network.http;

public class HTTP2 extends HTTP1 {

	// Pseudo-Header Fields

	final static String AUTHORITY = ":authority";
	final static String METHOD = ":method";
	final static String PATH = ":path";
	final static String SCHEME = ":scheme";
	final static String STATUS = ":status";

	// HTTP/2 Settings

	final static short HEADER_TABLE_SIZE = 0x1;
	final static short ENABLE_PUSH = 0x2;
	final static short MAX_CONCURRENT_STREAMS = 0x3;
	final static short INITIAL_WINDOW_SIZE = 0x4;
	final static short MAX_FRAME_SIZE = 0x5;
	final static short MAX_HEADER_LIST_SIZE = 0x6;

	// ERROR Code

	final static int NO_ERROR = 0x0;
	final static int PROTOCOL_ERROR = 0x1;
	final static int INTERNAL_ERROR = 0x2;
	final static int FLOW_CONTROL_ERROR = 0x3;
	final static int SETTINGS_TIMEOUT = 0x4;
	final static int STREAM_CLOSED = 0x5;
	final static int FRAME_SIZE_ERROR = 0x6;
	final static int REFUSED_STREAM = 0x7;
	final static int CANCEL = 0x8;
	final static int COMPRESSION_ERROR = 0x9;
	final static int CONNECT_ERROR = 0xa;
	final static int ENHANCE_YOUR_CALM = 0xb;
	final static int INADEQUATE_SECURITY = 0xc;
	final static int HTTP_1_1_REQUIRED = 0xd;

	public static String errorText(int code) {
		switch (code) {
			case NO_ERROR:
				return "NO ERROR";
			case PROTOCOL_ERROR:
				return "PROTOCOL ERROR";
			case INTERNAL_ERROR:
				return "INTERNAL ERROR";
			case FLOW_CONTROL_ERROR:
				return "FLOW CONTROL ERROR";
			case SETTINGS_TIMEOUT:
				return "SETTINGS TIMEOUT";
			case STREAM_CLOSED:
				return "STREAM CLOSED";
			case FRAME_SIZE_ERROR:
				return "FRAME SIZE ERROR";
			case REFUSED_STREAM:
				return "REFUSED STREAM";
			case CANCEL:
				return "CANCEL";
			case COMPRESSION_ERROR:
				return "COMPRESSION ERROR";
			case CONNECT_ERROR:
				return "CONNECT ERROR";
			case ENHANCE_YOUR_CALM:
				return "ENHANCE YOUR CALM";
			case INADEQUATE_SECURITY:
				return "INADEQUATE SECURITY";
			case HTTP_1_1_REQUIRED:
				return "HTTP 1.1 REQUIRED";
			default:
				return "UNKNOWN";
		}
	}

	/** 响应明文升级 */
	final static Response RESPONSE_SWITCHING_PROTOCOL = new Response();
	/** 客户端明文连接前奏 */
	final static Request REQUEST_PREFACE = new Request();
	static {
		RESPONSE_SWITCHING_PROTOCOL.setStatus(HTTPStatus.SWITCHING_PROTOCOL);
		RESPONSE_SWITCHING_PROTOCOL.addHeader(Connection, Upgrade);
		RESPONSE_SWITCHING_PROTOCOL.addHeader(Upgrade, "h2c");

		// 棱镜计划 PRISM
		REQUEST_PREFACE.setVersion(HTTP1.V20);
		REQUEST_PREFACE.setMethod("PRI");
		REQUEST_PREFACE.setUrl("*");
		REQUEST_PREFACE.setContent("SM");
	}

	// 流标识符零(0x0)用于连接控制消息
	// 客户端发起使用奇数 1357 Request
	// 服务端响应使用奇数 1357 Response
	// 服务端发起使用偶数 2468 Push
	// 流编号只增不减，用尽后GOAWAY

	// 其实就是多个请求或响应同时收发
	// 客户端首先用奇数流发起请求
	// 客户端请求发送完成后用相同奇数流接收响应
	// 服务端用奇数流接收请求
	// 服务端处理完成后用相同奇数流发送响应
	// 服务端推送用偶数流发起
	// 客户端指定服务器可以启动的最大并发流数
	// 服务器指定客户端可以启动的最大并发流数

	// 奇数转换为索引 (odd-1)/2=index
	// 偶数转换为索引 (even-2)/2=index
}