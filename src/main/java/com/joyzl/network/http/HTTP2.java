package com.joyzl.network.http;

public class HTTP2 extends HTTP {

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

	/** 响应明文升级 */
	final static Response RESPONSE_SWITCHING_PROTOCOL = new Response();
	/** 客户端明文连接前奏 */
	final static Request REQUEST_PREFACE = new Request();
	static {
		RESPONSE_SWITCHING_PROTOCOL.setStatus(HTTPStatus.SWITCHING_PROTOCOL);
		RESPONSE_SWITCHING_PROTOCOL.addHeader(Connection, Upgrade);
		RESPONSE_SWITCHING_PROTOCOL.addHeader(Upgrade, "h2c");

		// 棱镜计划 PRISM
		REQUEST_PREFACE.setVersion(HTTP.V20);
		REQUEST_PREFACE.setMethod("PRI");
		REQUEST_PREFACE.setUrl("*");
		REQUEST_PREFACE.setContent("SM");
	}
}