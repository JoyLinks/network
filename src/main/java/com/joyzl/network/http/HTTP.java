package com.joyzl.network.http;

import java.util.function.Supplier;

import com.joyzl.network.StringSeeker;

/**
 * HTTP
 * 
 * @author ZhangXi 2024年11月18日
 */
public class HTTP {

	// VERSIONS

	public final static String V10 = "HTTP/1.0";
	public final static String V11 = "HTTP/1.1";
	public final static String V20 = "HTTP/2";
	public final static String WS = "WS";
	public final static StringSeeker VERSIONS = new StringSeeker(new String[] { V10, V11, V20 });

	// METHODS

	/** 请求创建隧道 */
	public final static String CONNECT = "CONNECT";
	/** 请求删除资源 */
	public final static String DELETE = "DELETE";
	/** 请求获取资源 */
	public final static String GET = "GET";
	/** 请求资源标头 */
	public final static String HEAD = "HEAD";
	/** 询问支持的请求方法 */
	public final static String OPTIONS = "OPTIONS";
	/** 请求修改资源 */
	public final static String PATCH = "PATCH";
	/** 请求提交数据 */
	public final static String POST = "POST";
	/** 请求创建资源 */
	public final static String PUT = "PUT";
	/** 请求回环测试 */
	public final static String TRACE = "TRACE";
	/** 尝试解析HTTP/2 */
	public final static String PRI = "PRI";

	// WEB DAV

	/** 请求检索资源属性 */
	public final static String PROPFIND = "PROPFIND";
	/** 请求设置资源属性 */
	public final static String PROPPATCH = "PROPPATCH";
	/** 请求创建资源集合 */
	public final static String MKCOL = "MKCOL";
	/** 请求拷贝资源 */
	public final static String COPY = "COPY";
	/** 请求移动资源 */
	public final static String MOVE = "MOVE";
	/** 请求锁定资源 */
	public final static String LOCK = "LOCK";
	/** 请求解锁资源 */
	public final static String UNLOCK = "UNLOCK";

	public final static StringSeeker METHODS = new StringSeeker(new String[] {
			// HTTP BASE
			CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE, PRI,
			// WEB DAV
			PROPFIND, PROPPATCH, MKCOL, COPY, MOVE, LOCK, UNLOCK });

	// HRADERS HTTP + WEBDAV

	public final static String Accept = "Accept";
	public final static String Accept_Additions = "Accept-Additions";
	public final static String Accept_Charset = "Accept-Charset";
	public final static String Accept_Encoding = "Accept-Encoding";
	public final static String Accept_Features = "Accept-Features";
	public final static String Accept_Language = "Accept-Language";
	public final static String Accept_Ranges = "Accept-Ranges";
	public final static String Access_Control_Allow_Credentials = "Access-Control-Allow-Credentials";
	public final static String Access_Control_Allow_Headers = "Access-Control-Allow-Headers";
	public final static String Access_Control_Allow_Methods = "Access-Control-Allow-Methods";
	public final static String Access_Control_Allow_Origin = "Access-Control-Allow-Origin";
	public final static String Access_Control_Expose_Headers = "Access-Control-Expose-Headers";
	public final static String Access_Control_Max_Age = "Access-Control-Max-Age";
	public final static String Access_Control_Request_Headers = "Access-Control-Request-Headers";
	public final static String Access_Control_Request_Method = "Access-Control-Request-Method";
	public final static String Age = "Age";
	public final static String A_IM = "A-IM";
	public final static String Allow = "Allow";
	public final static String Alternates = "Alternates";
	public final static String Alt_Svc = "Alt-Svc";
	public final static String Authentication_Info = "Authentication-Info";
	public final static String Authorization = "Authorization";
	public final static String Cache_Control = "Cache-Control";
	public final static String C_Ext = "C-Ext";
	public final static String Clear_Site_Data = "Clear-Site-Data";
	public final static String C_Man = "C-Man";
	public final static String Connection = "Connection";
	public final static String Content_Base = "Content-Base";
	public final static String Content_Disposition = "Content-Disposition";
	public final static String Content_Encoding = "Content-Encoding";
	public final static String Content_ID = "Content-ID";
	public final static String Content_Language = "Content-Language";
	public final static String Content_Length = "Content-Length";
	public final static String Content_Location = "Content-Location";
	public final static String Content_MD5 = "Content-MD5";
	public final static String Content_Range = "Content-Range";
	public final static String Content_Script_Type = "Content-Script-Type";
	public final static String Content_Security_Policy = "Content-Security-Policy";
	public final static String Content_Security_Policy_Report_Only = "Content-Security-Policy-Report-Only";
	public final static String Content_Style_Type = "Content-Style-Type";
	public final static String Content_Type = "Content-Type";
	public final static String Content_Version = "Content-Version";
	public final static String Cookie = "Cookie";
	public final static String Cookie2 = "Cookie2";
	public final static String C_Opt = "C-Opt";
	public final static String C_PEP = "C-PEP";
	public final static String C_PEP_Info = "C-PEP-Info";
	public final static String Cross_Origin_Embedder_Policy = "Cross-Origin-Embedder-Policy";
	public final static String Cross_Origin_Opener_Policy = "Cross-Origin-Opener-Policy";
	public final static String Cross_Origin_Resource_Policy = "Cross-Origin-Resource-Policy";
	public final static String Date = "Date";
	public final static String DAV = "DAV";
	public final static String Default_Style = "Default-Style";
	public final static String Delta_Base = "Delta-Base";
	public final static String Depth = "Depth";
	public final static String Derived_From = "Derived-From";
	public final static String Destination = "Destination";
	public final static String Differential_ID = "Differential-ID";
	public final static String Digest = "Digest";
	public final static String Downlink = "Downlink";
	public final static String ECT = "ECT";
	public final static String ETag = "ETag";
	public final static String Expect = "Expect";
	public final static String Expect_CT = "Expect-CT";
	public final static String Expires = "Expires";
	public final static String Ext = "Ext";
	public final static String Forwarded = "Forwarded";
	public final static String From = "From";
	public final static String GetProfile = "GetProfile";
	public final static String Host = "Host";
	public final static String HTTP2_Settings = "HTTP2-Settings";
	public final static String If = "If";
	public final static String If_Match = "If-Match";
	public final static String If_Modified_Since = "If-Modified-Since";
	public final static String If_None_Match = "If-None-Match";
	public final static String If_Range = "If-Range";
	public final static String If_Unmodified_Since = "If-Unmodified-Since";
	public final static String IM = "IM";
	public final static String Keep_Alive = "Keep-Alive";
	public final static String Label = "Label";
	public final static String Last_Event_ID = "Last-Event-ID";
	public final static String Last_Modified = "Last-Modified";
	public final static String Link = "Link";
	public final static String Location = "Location";
	public final static String Lock_Token = "Lock-Token";
	public final static String Man = "Man";
	public final static String Max_Forwards = "Max-Forwards";
	public final static String Meter = "Meter";
	public final static String MIME_Version = "MIME-Version";
	public final static String Negotiate = "Negotiate";
	public final static String Opt = "Opt";
	public final static String Ordering_Type = "Ordering-Type";
	public final static String Origin = "Origin";
	public final static String Overwrite = "Overwrite";
	public final static String P3P = "P3P";
	public final static String PEP = "PEP";
	public final static String Pep_Info = "Pep-Info";
	public final static String Permissions_Policy = "Permissions-Policy";
	public final static String PICS_Label = "PICS-Label";
	public final static String Ping_From = "Ping-From";
	public final static String Ping_To = "Ping-To";
	public final static String Position = "Position";
	public final static String Pragma = "Pragma";
	public final static String ProfileObject = "ProfileObject";
	public final static String Protocol = "Protocol";
	public final static String Protocol_Info = "Protocol-Info";
	public final static String Protocol_Query = "Protocol-Query";
	public final static String Protocol_Request = "Protocol-Request";
	public final static String Proxy_Authenticate = "Proxy-Authenticate";
	public final static String Proxy_Authentication_Info = "Proxy-Authentication-Info";
	public final static String Proxy_Authorization = "Proxy-Authorization";
	public final static String Proxy_Features = "Proxy-Features";
	public final static String Proxy_Instruction = "Proxy-Instruction";
	public final static String Public = "Public";
	public final static String Range = "Range";
	public final static String Referer = "Referer";
	public final static String Referrer_Policy = "Referrer-Policy";
	public final static String Refresh = "Refresh";
	public final static String Report_To = "Report-To";
	public final static String Retry_After = "Retry-After";
	public final static String RTT = "RTT";
	public final static String Safe = "Safe";
	public final static String Sec_Fetch_Dest = "Sec-Fetch-Dest";
	public final static String Sec_Fetch_Mode = "Sec-Fetch-Mode";
	public final static String Sec_Fetch_Site = "Sec-Fetch-Site";
	public final static String Sec_Fetch_User = "Sec-Fetch-User";
	public final static String Security_Scheme = "Security-Scheme";
	public final static String Sec_WebSocket_Accept = "Sec-WebSocket-Accept";
	public final static String Sec_WebSocket_Extensions = "Sec-WebSocket-Extensions";
	public final static String Sec_WebSocket_Key = "Sec-WebSocket-Key";
	public final static String Sec_WebSocket_Protocol = "Sec-WebSocket-Protocol";
	public final static String Sec_WebSocket_Version = "Sec-WebSocket-Version";
	public final static String Server = "Server";
	public final static String Server_Timing = "Server-Timing";
	public final static String Service_Worker_Navigation_Preload = "Service-Worker-Navigation-Preload";
	public final static String Set_Cookie = "Set-Cookie";
	public final static String Set_Cookie2 = "Set-Cookie2";
	public final static String SetProfile = "SetProfile";
	public final static String SoapAction = "SoapAction";
	public final static String SourceMap = "SourceMap";
	public final static String Status_URI = "Status-URI";
	public final static String Strict_Transport_Security = "Strict-Transport-Security";
	public final static String Surrogate_Capability = "Surrogate-Capability";
	public final static String Surrogate_Control = "Surrogate-Control";
	public final static String TCN = "TCN";
	public final static String TE = "TE";
	public final static String Timeout = "Timeout";
	public final static String Timing_Allow_Origin = "Timing-Allow-Origin";
	public final static String Trailer = "Trailer";
	public final static String Transfer_Encoding = "Transfer-Encoding";
	public final static String Upgrade = "Upgrade";
	public final static String Upgrade_Insecure_Requests = "Upgrade-Insecure-Requests";
	public final static String URI = "URI";
	public final static String User_Agent = "User-Agent";
	public final static String Variant_Vary = "Variant-Vary";
	public final static String Vary = "Vary";
	public final static String Via = "Via";
	public final static String Want_Digest = "Want-Digest";
	public final static String Warning = "Warning";
	public final static String WWW_Authenticate = "WWW-Authenticate";
	public final static StringSeeker HEADERS = new StringSeeker(new String[] { //
			Accept, //
			Accept_Additions, //
			Accept_Charset, //
			Accept_Encoding, //
			Accept_Features, //
			Accept_Language, //
			Accept_Ranges, //
			Access_Control_Allow_Credentials, //
			Access_Control_Allow_Headers, //
			Access_Control_Allow_Methods, //
			Access_Control_Allow_Origin, //
			Access_Control_Expose_Headers, //
			Access_Control_Max_Age, //
			Access_Control_Request_Headers, //
			Access_Control_Request_Method, //
			Age, //
			A_IM, //
			Allow, //
			Alternates, //
			Alt_Svc, //
			Authentication_Info, //
			Authorization, //
			Cache_Control, //
			C_Ext, //
			Clear_Site_Data, //
			C_Man, //
			Connection, //
			Content_Base, //
			Content_Disposition, //
			Content_Encoding, //
			Content_ID, //
			Content_Language, //
			Content_Length, //
			Content_Location, //
			Content_MD5, //
			Content_Range, //
			Content_Script_Type, //
			Content_Security_Policy, //
			Content_Security_Policy_Report_Only, //
			Content_Style_Type, //
			Content_Type, //
			Content_Version, //
			Cookie, //
			Cookie2, //
			C_Opt, //
			C_PEP, //
			C_PEP_Info, //
			Cross_Origin_Embedder_Policy, //
			Cross_Origin_Opener_Policy, //
			Cross_Origin_Resource_Policy, //
			Date, //
			DAV, //
			Default_Style, //
			Delta_Base, //
			Depth, //
			Derived_From, //
			Destination, //
			Differential_ID, //
			Digest, //
			Downlink, //
			ECT, //
			ETag, //
			Expect, //
			Expect_CT, //
			Expires, //
			Ext, //
			Forwarded, //
			From, //
			GetProfile, //
			Host, //
			If, //
			If_Match, //
			If_Modified_Since, //
			If_None_Match, //
			If_Range, //
			If_Unmodified_Since, //
			IM, //
			Keep_Alive, //
			Label, //
			Last_Event_ID, //
			Last_Modified, //
			Link, //
			Location, //
			Lock_Token, //
			Man, //
			Max_Forwards, //
			Meter, //
			MIME_Version, //
			Negotiate, //
			Opt, //
			Ordering_Type, //
			Origin, //
			Overwrite, //
			P3P, //
			PEP, //
			Pep_Info, //
			Permissions_Policy, //
			PICS_Label, //
			Ping_From, //
			Ping_To, //
			Position, //
			Pragma, //
			ProfileObject, //
			Protocol, //
			Protocol_Info, //
			Protocol_Query, //
			Protocol_Request, //
			Proxy_Authenticate, //
			Proxy_Authentication_Info, //
			Proxy_Authorization, //
			Proxy_Features, //
			Proxy_Instruction, //
			Public, //
			Range, //
			Referer, //
			Referrer_Policy, //
			Refresh, //
			Report_To, //
			Retry_After, //
			RTT, //
			Safe, //
			Sec_Fetch_Dest, //
			Sec_Fetch_Mode, //
			Sec_Fetch_Site, //
			Sec_Fetch_User, //
			Security_Scheme, //
			Sec_WebSocket_Accept, //
			Sec_WebSocket_Extensions, //
			Sec_WebSocket_Key, //
			Sec_WebSocket_Protocol, //
			Sec_WebSocket_Version, //
			Server, //
			Server_Timing, //
			Service_Worker_Navigation_Preload, //
			Set_Cookie, //
			Set_Cookie2, //
			SetProfile, //
			SoapAction, //
			SourceMap, //
			Status_URI, //
			Strict_Transport_Security, //
			Surrogate_Capability, //
			Surrogate_Control, //
			TCN, //
			TE, //
			Timeout, //
			Timing_Allow_Origin, //
			Trailer, //
			Transfer_Encoding, //
			Upgrade, //
			Upgrade_Insecure_Requests, //
			URI, //
			User_Agent, //
			Variant_Vary, //
			Vary, //
			Via, //
			Want_Digest, //
			Warning, //
			WWW_Authenticate,//
	});

	/**
	 * 由于HTTP大量依赖字符编码因此采用线程缓存的StringBuilder辅助
	 */
	private static final ThreadLocal<StringBuilder> THREAD_LOCAL_STRING_BUILDER = ThreadLocal.withInitial(new Supplier<>() {
		@Override
		public StringBuilder get() {
			return new StringBuilder(512);
		}
	});

	/**
	 * 获取线程缓存字符串构建类实例
	 */
	protected static StringBuilder getStringBuilder() {
		final StringBuilder b = THREAD_LOCAL_STRING_BUILDER.get();
		b.setLength(0);
		return b;
	}
}