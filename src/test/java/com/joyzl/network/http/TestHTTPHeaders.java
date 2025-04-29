/**
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved. 
 */
package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

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
		for (int i = 0; i < HTTP1.HEADERS.size(); i++) {
			samples.add(new StringBuilder(HEADERS[i]));
		}

		String name;
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP1.HEADERS.get(samples.get(i));
			assertTrue(name == HEADERS[i]);
		}
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP1.HEADERS.get(samples.get(i).toString().toLowerCase());
			assertTrue(name == HEADERS[i]);
		}
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP1.HEADERS.get(samples.get(i).toString().toUpperCase());
			assertTrue(name == HEADERS[i]);
		}
		for (int i = 0; i < samples.size(); i++) {
			name = HTTP1.HEADERS.get(samples.get(i).substring(0, samples.get(i).length() - 1));
			assertTrue(name != HEADERS[i]);
		}

		// 无匹配情形
		name = HTTP1.HEADERS.get("X");
		assertTrue(name.equals("X"));
		name = HTTP1.HEADERS.get("XX");
		assertTrue(name.equals("XX"));

		// MAP
		final Map<String, String> map = new HashMap<>();
		for (int i = 0; i < HTTP1.HEADERS.size(); i++) {
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
				name = HTTP1.HEADERS.get(samples.get(i));
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
			HTTP1.Accept, //
			HTTP1.Accept_Additions, //
			HTTP1.Accept_Charset, //
			HTTP1.Accept_Encoding, //
			HTTP1.Accept_Features, //
			HTTP1.Accept_Language, //
			HTTP1.Accept_Ranges, //
			HTTP1.Access_Control_Allow_Credentials, //
			HTTP1.Access_Control_Allow_Headers, //
			HTTP1.Access_Control_Allow_Methods, //
			HTTP1.Access_Control_Allow_Origin, //
			HTTP1.Access_Control_Expose_Headers, //
			HTTP1.Access_Control_Max_Age, //
			HTTP1.Access_Control_Request_Headers, //
			HTTP1.Access_Control_Request_Method, //
			HTTP1.Age, //
			HTTP1.A_IM, //
			HTTP1.Allow, //
			HTTP1.Alternates, //
			HTTP1.Alt_Svc, //
			HTTP1.Authentication_Info, //
			HTTP1.Authorization, //
			HTTP1.Cache_Control, //
			HTTP1.C_Ext, //
			HTTP1.Clear_Site_Data, //
			HTTP1.C_Man, //
			HTTP1.Connection, //
			HTTP1.Content_Base, //
			HTTP1.Content_Disposition, //
			HTTP1.Content_Encoding, //
			HTTP1.Content_ID, //
			HTTP1.Content_Language, //
			HTTP1.Content_Length, //
			HTTP1.Content_Location, //
			HTTP1.Content_MD5, //
			HTTP1.Content_Range, //
			HTTP1.Content_Script_Type, //
			HTTP1.Content_Security_Policy, //
			HTTP1.Content_Security_Policy_Report_Only, //
			HTTP1.Content_Style_Type, //
			HTTP1.Content_Type, //
			HTTP1.Content_Version, //
			HTTP1.Cookie, //
			HTTP1.Cookie2, //
			HTTP1.C_Opt, //
			HTTP1.C_PEP, //
			HTTP1.C_PEP_Info, //
			HTTP1.Cross_Origin_Embedder_Policy, //
			HTTP1.Cross_Origin_Opener_Policy, //
			HTTP1.Cross_Origin_Resource_Policy, //
			HTTP1.Date, //
			HTTP1.DAV, //
			HTTP1.Default_Style, //
			HTTP1.Delta_Base, //
			HTTP1.Depth, //
			HTTP1.Derived_From, //
			HTTP1.Destination, //
			HTTP1.Differential_ID, //
			HTTP1.Digest, //
			HTTP1.Downlink, //
			HTTP1.ECT, //
			HTTP1.ETag, //
			HTTP1.Expect, //
			HTTP1.Expect_CT, //
			HTTP1.Expires, //
			HTTP1.Ext, //
			HTTP1.Forwarded, //
			HTTP1.From, //
			HTTP1.GetProfile, //
			HTTP1.Host, //
			HTTP1.If, //
			HTTP1.If_Match, //
			HTTP1.If_Modified_Since, //
			HTTP1.If_None_Match, //
			HTTP1.If_Range, //
			HTTP1.If_Unmodified_Since, //
			HTTP1.IM, //
			HTTP1.Keep_Alive, //
			HTTP1.Label, //
			HTTP1.Last_Event_ID, //
			HTTP1.Last_Modified, //
			HTTP1.Link, //
			HTTP1.Location, //
			HTTP1.Lock_Token, //
			HTTP1.Man, //
			HTTP1.Max_Forwards, //
			HTTP1.Meter, //
			HTTP1.MIME_Version, //
			HTTP1.Negotiate, //
			HTTP1.Opt, //
			HTTP1.Ordering_Type, //
			HTTP1.Origin, //
			HTTP1.Overwrite, //
			HTTP1.P3P, //
			HTTP1.PEP, //
			HTTP1.Pep_Info, //
			HTTP1.Permissions_Policy, //
			HTTP1.PICS_Label, //
			HTTP1.Ping_From, //
			HTTP1.Ping_To, //
			HTTP1.Position, //
			HTTP1.Pragma, //
			HTTP1.ProfileObject, //
			HTTP1.Protocol, //
			HTTP1.Protocol_Info, //
			HTTP1.Protocol_Query, //
			HTTP1.Protocol_Request, //
			HTTP1.Proxy_Authenticate, //
			HTTP1.Proxy_Authentication_Info, //
			HTTP1.Proxy_Authorization, //
			HTTP1.Proxy_Features, //
			HTTP1.Proxy_Instruction, //
			HTTP1.Public, //
			HTTP1.Range, //
			HTTP1.Referer, //
			HTTP1.Referrer_Policy, //
			HTTP1.Refresh, //
			HTTP1.Report_To, //
			HTTP1.Retry_After, //
			HTTP1.RTT, //
			HTTP1.Safe, //
			HTTP1.Sec_Fetch_Dest, //
			HTTP1.Sec_Fetch_Mode, //
			HTTP1.Sec_Fetch_Site, //
			HTTP1.Sec_Fetch_User, //
			HTTP1.Security_Scheme, //
			HTTP1.Sec_WebSocket_Accept, //
			HTTP1.Sec_WebSocket_Extensions, //
			HTTP1.Sec_WebSocket_Key, //
			HTTP1.Sec_WebSocket_Protocol, //
			HTTP1.Sec_WebSocket_Version, //
			HTTP1.Server, //
			HTTP1.Server_Timing, //
			HTTP1.Service_Worker_Navigation_Preload, //
			HTTP1.Set_Cookie, //
			HTTP1.Set_Cookie2, //
			HTTP1.SetProfile, //
			HTTP1.SoapAction, //
			HTTP1.SourceMap, //
			HTTP1.Status_URI, //
			HTTP1.Strict_Transport_Security, //
			HTTP1.Surrogate_Capability, //
			HTTP1.Surrogate_Control, //
			HTTP1.TCN, //
			HTTP1.TE, //
			HTTP1.Timeout, //
			HTTP1.Timing_Allow_Origin, //
			HTTP1.Trailer, //
			HTTP1.Transfer_Encoding, //
			HTTP1.Upgrade, //
			HTTP1.Upgrade_Insecure_Requests, //
			HTTP1.URI, //
			HTTP1.User_Agent, //
			HTTP1.Variant_Vary, //
			HTTP1.Vary, //
			HTTP1.Via, //
			HTTP1.Want_Digest, //
			HTTP1.Warning, //
			HTTP1.WWW_Authenticate,//
	};
}