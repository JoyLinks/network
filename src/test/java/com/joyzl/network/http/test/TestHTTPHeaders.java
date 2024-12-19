/**
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved. 
 */
package com.joyzl.network.http.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.joyzl.network.http.Accept;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentDisposition;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentRange;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Cookie;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.Range;
import com.joyzl.network.http.SetCookie;

/**
 * HTTP Header 相关测试
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年5月25日
 */
class TestHTTPHeaders {

	@Test
	void testAa() {
		// a(97) - A(65) = 32
		// [a(97) + A(65)] / 2 = 81
		// [z(122) + Z(90)] / 2 = 82
		// 大小写字母异或为32
		assertEquals('a' ^ 'A', 32);
		assertEquals('z' ^ 'Z', 32);

		assertNotEquals('a' ^ 'B', 32);
		assertNotEquals('z' ^ 'Y', 32);

		System.out.println('a' ^ 'A');// 32
		System.out.println('b' ^ 'A');// 35
		System.out.println('c' ^ 'A');// 34
	}

	@Test
	void testIfString() {
		final List<StringBuilder> samples = new ArrayList<>();
		for (int i = 0; i < HTTP.HEADERS.size(); i++) {
			samples.add(new StringBuilder(HEADERS[i]));
		}

		String name;
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP.HEADERS.get(samples.get(i));
			assertTrue(name == HEADERS[i]);
		}
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP.HEADERS.get(samples.get(i).toString().toLowerCase());
			assertTrue(name == HEADERS[i]);
		}
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP.HEADERS.get(samples.get(i).toString().toUpperCase());
			assertTrue(name == HEADERS[i]);
		}
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP.HEADERS.get(samples.get(i).substring(0, samples.get(i).length() - 1));
			assertTrue(name != HEADERS[i]);
		}

		// 无匹配情形
		name = HTTP.HEADERS.get("X");
		assertTrue(name.equals("X"));
		name = HTTP.HEADERS.get("XX");
		assertTrue(name.equals("XX"));

		// MAP
		final Map<String, String> map = new HashMap<>();
		for (int i = 0; i < HTTP.HEADERS.size(); i++) {
			map.put(HEADERS[i], HEADERS[i]);
		}

		// 效率比较
		int size = 10000;
		long time = System.currentTimeMillis();
		while (size-- > 0) {
			for (int i = 0; i < samples.size(); i++) {
				name = map.get(samples.get(i).toString().toUpperCase());
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("MAP:" + time);

		size = 10000;
		time = System.currentTimeMillis();
		while (size-- > 0) {
			for (int i = 0; i < samples.size(); i++) {
				name = HTTP.HEADERS.get(samples.get(i));
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("SEK:" + time);
	}

	/**
	 * <pre>
	 * WWW-Authenticate: Digest qop="auth", realm="DS-2CD2310FD-I", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d"
	 * Authorization: Digest username="admin", realm="DS-2CD2310FD-I", qop="auth", algorithm="MD5", uri="/onvif/device_service", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d", nc=00000001, cnonce="0EE3ED23BFD9A00B2AB542E3BAB85BDB", response="518fd6d1666f9f00a5c5097359188c4e"
	 * </pre>
	 * 
	 */

	@Test
	void testSplit1() {
		// 值无引号
		String test = " name1=value1; name2=value2;  name3=value3;name4=value4 ;name5=value5  ;name6 =value6; name7= value7; name8 = value8;name9;";

		String name = null, value = null;
		for (int start = 0, end = 0, index = 0; index <= test.length(); index++) {
			if (index >= test.length() || test.charAt(index) == ';') {
				if (name == null) {
					if (start < end) {
						name = test.substring(start, end);

						System.out.print('[');
						System.out.print(name);
						System.out.print('=');
						System.out.println(']');
					}
				} else {
					value = test.substring(start, end);

					System.out.print('[');
					System.out.print(name);
					System.out.print('=');
					System.out.print(value);
					System.out.println(']');
				}

				name = null;
				end = start = index + 1;
			} else if (test.charAt(index) == '=') {
				name = test.substring(start, end);
				end = start = index + 1;
			} else if (Character.isWhitespace(test.charAt(index))) {
				if (end <= start) {
					start = index + 1;
				}
			} else {
				end = index + 1;
			}
		}

	}

	@Test
	void testSplit2() {
		// 值可能有引号
		String test = " name1=value1; name2=value2;  name3=value3;name4=\"value4\" ;name5=\"value5\"  ;name6 =\"value6\"; name7= value7; name8 = value8;";

		String name = null, value = null;
		for (int start = 0, end = 0, index = 0; index <= test.length(); index++) {
			if (index >= test.length() || test.charAt(index) == ';') {
				if (name == null) {
					break;
				} else {
					value = test.substring(start, end);
					System.out.print('[');
					System.out.print(name);
					System.out.print('=');
					System.out.print(value);
					System.out.println(']');
				}
				name = null;
				end = start = index + 1;
			} else if (test.charAt(index) == '=') {
				name = test.substring(start, end);

				end = start = index + 1;
			} else if (test.charAt(index) == '"' || Character.isWhitespace(test.charAt(index))) {
				if (end <= start) {
					start = index + 1;
				}
			} else {
				end = index + 1;
			}
		}
	}

	@Test
	void testAccept() {
		// 常规带权重值有空格分隔
		final String sample1 = "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8";
		// 常规带权重值无空格分隔
		final String sample2 = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

		Accept accept = Accept.parse(sample1);
		assertEquals(accept.size(), 4);
		assertEquals(accept.getValue(), "text/html");
		assertEquals(accept.getValue(0), "text/html");
		assertEquals(accept.getValue(1), "application/xhtml+xml");
		assertEquals(accept.getValue(2), "application/xml");
		assertEquals(accept.getValue(3), "*/*");
		assertEquals(accept.getHeaderValue(), sample1);

		accept = Accept.parse(sample2);
		assertEquals(accept.size(), 4);
		assertEquals(accept.getValue(), "text/html");
		assertEquals(accept.getValue(0), "text/html");
		assertEquals(accept.getValue(1), "application/xhtml+xml");
		assertEquals(accept.getValue(2), "application/xml");
		assertEquals(accept.getValue(3), "*/*");
		assertEquals(accept.getHeaderValue(), sample1);
	}

	@Test
	void testAcceptEncoding() {
		final String value = "deflate, gzip;q=1.0, *;q=0.5";
		AcceptEncoding ae = AcceptEncoding.parse(value);
		assertEquals(ae.size(), 3);
		assertEquals(ae.getValue(), "deflate");
		assertEquals(ae.getValue(0), "deflate");
		assertEquals(ae.getValue(1), "gzip");
		assertEquals(ae.getValue(2), "*");
	}

	@Test
	void testCacheControl() {
		CacheControl c1 = CacheControl.parse("no-cache");
		assertEquals(c1.getControl(), CacheControl.NO_CACHE);
		assertEquals(c1.getSeconds(), 0);
		assertEquals(c1.getHeaderValue(), "no-cache");
		System.out.println(c1);

		CacheControl c2 = CacheControl.parse("max-age=100");
		assertEquals(c2.getControl(), CacheControl.MAX_AGE);
		assertEquals(c2.getSeconds(), 100);
		assertEquals(c2.getHeaderValue(), "max-age=100");
		System.out.println(c2);
	}

	@Test
	void testContentRange() {
		ContentRange c1 = ContentRange.parse("bytes 200-1000/67589");
		assertEquals(c1.getStart(), 200);
		assertEquals(c1.getEnd(), 1000);
		assertEquals(c1.getLength(), 67589);
		assertEquals(c1.getHeaderValue(), "bytes 200-1000/67589");
		System.out.println(c1);
	}

	@Test
	void testContentType() {
		ContentType c1 = ContentType.parse("text/html");
		assertEquals(c1.getType(), "text/html");
		assertEquals(c1.getHeaderValue(), "text/html");
		System.out.println(c1);

		ContentType c2 = ContentType.parse("text/html; charset=utf-8");
		assertEquals(c2.getType(), "text/html");
		assertEquals(c2.getCharset(), "utf-8");
		assertEquals(c2.getHeaderValue(), "text/html; charset=utf-8");
		System.out.println(c2);

		ContentType c3 = ContentType.parse("multipart/form-data; boundary=something");
		assertEquals(c3.getType(), "multipart/form-data");
		assertEquals(c3.getBoundary(), "something");
		assertEquals(c3.getHeaderValue(), "multipart/form-data; boundary=something");
		System.out.println(c3);
	}

	@Test
	void testContentLength() {
		ContentLength content_length = ContentLength.parse("2");
		assertEquals(content_length.getLength(), 2);
	}

	@Test
	void testCookie() {
		Cookie c1 = Cookie.parse("name=value");
		assertEquals(c1.getArguments().size(), 1);
		assertEquals(c1.getValue("name"), "value");
		System.out.println(c1);

		Cookie c2 = Cookie.parse("name=value; name2=value2; name3=value3");
		assertEquals(c2.getArguments().size(), 3);
		System.out.println(c2);

		Cookie c3 = Cookie.parse("PHPSESSID=298zf09hf012fh2; csrftoken=u32t4o3tb3gg43; _gat=1;");
		assertEquals(c3.getArguments().size(), 3);
		System.out.println(c3);
	}

	@Test
	void testContentDisposition() {
		ContentDisposition c1 = ContentDisposition.parse("inline");
		assertEquals(c1.getDisposition(), "inline");
		assertEquals(c1.getHeaderValue(), "inline");
		System.out.println(c1);

		ContentDisposition c2 = ContentDisposition.parse("attachment; filename=\"filename.jpg\"");
		assertEquals(c2.getDisposition(), "attachment");
		assertEquals(c2.getFilename(), "filename.jpg");
		System.out.println(c2);

		ContentDisposition c3 = ContentDisposition.parse("form-data; name=\"fieldName\"; filename=\"filename.jpg\"");
		assertEquals(c3.getDisposition(), "form-data");
		assertEquals(c3.getField(), "fieldName");
		assertEquals(c3.getFilename(), "filename.jpg");
		System.out.println(c3);

		ContentDisposition c4 = ContentDisposition.parse("attachment; filename*=\"UTF-8''%E4%B8%AD%E5%90%8E%E7%AB%AF%E6%A1%86%E6%9E%B6.txt\"");
		assertEquals(c4.getDisposition(), "attachment");
		assertEquals(c4.getFilename(), "中后端框架.txt");
		System.out.println(c4);
	}

	@Test
	void testRange() {
		Range c1 = Range.parse("bytes=200-1000");
		assertEquals(c1.getRanges().size(), 1);
		System.out.println(c1);

		Range c2 = Range.parse("bytes=200-1000, 2000-6576, 19000-");
		assertEquals(c2.getRanges().size(), 3);
		System.out.println(c2);
	}

	@Test
	void testSetCookie() {
		SetCookie c1 = SetCookie.parse("qwerty=219ffwef9w0f; Domain=somecompany.com; Path=/; Expires=Wed, 21 Oct 2015 07:28:00 GMT");
		assertEquals(c1.getName(), "qwerty");
		assertEquals(c1.getValue(), "219ffwef9w0f");
		assertEquals(c1.getDomain(), "somecompany.com");
		assertEquals(c1.getPath(), "/");
		assertEquals(DateTimeFormatter.RFC_1123_DATE_TIME.format(c1.getExpires()), "Wed, 21 Oct 2015 07:28:00 GMT");
		System.out.println(c1);

		SetCookie c2 = SetCookie.parse("qwerty=219ffwef9w0f; HttpOnly; Path=/; Secure");
		assertEquals(c2.getName(), "qwerty");
		assertEquals(c2.getValue(), "219ffwef9w0f");
		assertEquals(c2.isHttpOnly(), true);
		assertEquals(c2.isSecure(), true);
		assertEquals(c2.getPath(), "/");
		System.out.println(c2);
	}

	final static String[] HEADERS = new String[] { //
			HTTP.Accept, //
			HTTP.Accept_Additions, //
			HTTP.Accept_Charset, //
			HTTP.Accept_Encoding, //
			HTTP.Accept_Features, //
			HTTP.Accept_Language, //
			HTTP.Accept_Ranges, //
			HTTP.Access_Control_Allow_Credentials, //
			HTTP.Access_Control_Allow_Headers, //
			HTTP.Access_Control_Allow_Methods, //
			HTTP.Access_Control_Allow_Origin, //
			HTTP.Access_Control_Expose_Headers, //
			HTTP.Access_Control_Max_Age, //
			HTTP.Access_Control_Request_Headers, //
			HTTP.Access_Control_Request_Method, //
			HTTP.Age, //
			HTTP.A_IM, //
			HTTP.Allow, //
			HTTP.Alternates, //
			HTTP.Alt_Svc, //
			HTTP.Authentication_Info, //
			HTTP.Authorization, //
			HTTP.Cache_Control, //
			HTTP.C_Ext, //
			HTTP.Clear_Site_Data, //
			HTTP.C_Man, //
			HTTP.Connection, //
			HTTP.Content_Base, //
			HTTP.Content_Disposition, //
			HTTP.Content_Encoding, //
			HTTP.Content_ID, //
			HTTP.Content_Language, //
			HTTP.Content_Length, //
			HTTP.Content_Location, //
			HTTP.Content_MD5, //
			HTTP.Content_Range, //
			HTTP.Content_Script_Type, //
			HTTP.Content_Security_Policy, //
			HTTP.Content_Security_Policy_Report_Only, //
			HTTP.Content_Style_Type, //
			HTTP.Content_Type, //
			HTTP.Content_Version, //
			HTTP.Cookie, //
			HTTP.Cookie2, //
			HTTP.C_Opt, //
			HTTP.C_PEP, //
			HTTP.C_PEP_Info, //
			HTTP.Cross_Origin_Embedder_Policy, //
			HTTP.Cross_Origin_Opener_Policy, //
			HTTP.Cross_Origin_Resource_Policy, //
			HTTP.Date, //
			HTTP.DAV, //
			HTTP.Default_Style, //
			HTTP.Delta_Base, //
			HTTP.Depth, //
			HTTP.Derived_From, //
			HTTP.Destination, //
			HTTP.Differential_ID, //
			HTTP.Digest, //
			HTTP.Downlink, //
			HTTP.ECT, //
			HTTP.ETag, //
			HTTP.Expect, //
			HTTP.Expect_CT, //
			HTTP.Expires, //
			HTTP.Ext, //
			HTTP.Forwarded, //
			HTTP.From, //
			HTTP.GetProfile, //
			HTTP.Host, //
			HTTP.If, //
			HTTP.If_Match, //
			HTTP.If_Modified_Since, //
			HTTP.If_None_Match, //
			HTTP.If_Range, //
			HTTP.If_Unmodified_Since, //
			HTTP.IM, //
			HTTP.Keep_Alive, //
			HTTP.Label, //
			HTTP.Last_Event_ID, //
			HTTP.Last_Modified, //
			HTTP.Link, //
			HTTP.Location, //
			HTTP.Lock_Token, //
			HTTP.Man, //
			HTTP.Max_Forwards, //
			HTTP.Meter, //
			HTTP.MIME_Version, //
			HTTP.Negotiate, //
			HTTP.Opt, //
			HTTP.Ordering_Type, //
			HTTP.Origin, //
			HTTP.Overwrite, //
			HTTP.P3P, //
			HTTP.PEP, //
			HTTP.Pep_Info, //
			HTTP.Permissions_Policy, //
			HTTP.PICS_Label, //
			HTTP.Ping_From, //
			HTTP.Ping_To, //
			HTTP.Position, //
			HTTP.Pragma, //
			HTTP.ProfileObject, //
			HTTP.Protocol, //
			HTTP.Protocol_Info, //
			HTTP.Protocol_Query, //
			HTTP.Protocol_Request, //
			HTTP.Proxy_Authenticate, //
			HTTP.Proxy_Authentication_Info, //
			HTTP.Proxy_Authorization, //
			HTTP.Proxy_Features, //
			HTTP.Proxy_Instruction, //
			HTTP.Public, //
			HTTP.Range, //
			HTTP.Referer, //
			HTTP.Referrer_Policy, //
			HTTP.Refresh, //
			HTTP.Report_To, //
			HTTP.Retry_After, //
			HTTP.RTT, //
			HTTP.Safe, //
			HTTP.Sec_Fetch_Dest, //
			HTTP.Sec_Fetch_Mode, //
			HTTP.Sec_Fetch_Site, //
			HTTP.Sec_Fetch_User, //
			HTTP.Security_Scheme, //
			HTTP.Sec_WebSocket_Accept, //
			HTTP.Sec_WebSocket_Extensions, //
			HTTP.Sec_WebSocket_Key, //
			HTTP.Sec_WebSocket_Protocol, //
			HTTP.Sec_WebSocket_Version, //
			HTTP.Server, //
			HTTP.Server_Timing, //
			HTTP.Service_Worker_Navigation_Preload, //
			HTTP.Set_Cookie, //
			HTTP.Set_Cookie2, //
			HTTP.SetProfile, //
			HTTP.SoapAction, //
			HTTP.SourceMap, //
			HTTP.Status_URI, //
			HTTP.Strict_Transport_Security, //
			HTTP.Surrogate_Capability, //
			HTTP.Surrogate_Control, //
			HTTP.TCN, //
			HTTP.TE, //
			HTTP.Timeout, //
			HTTP.Timing_Allow_Origin, //
			HTTP.Trailer, //
			HTTP.Transfer_Encoding, //
			HTTP.Upgrade, //
			HTTP.Upgrade_Insecure_Requests, //
			HTTP.URI, //
			HTTP.User_Agent, //
			HTTP.Variant_Vary, //
			HTTP.Vary, //
			HTTP.Via, //
			HTTP.Want_Digest, //
			HTTP.Warning, //
			HTTP.WWW_Authenticate,//
	};
}