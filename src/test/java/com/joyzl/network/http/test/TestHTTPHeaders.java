/**
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved. 
 */
package com.joyzl.network.http.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.joyzl.network.http.Accept;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentDisposition;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentRange;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Cookie;
import com.joyzl.network.http.Range;
import com.joyzl.network.http.SetCookie;
import com.joyzl.network.http.WWWAuthenticate;

/**
 * HTTP Header 相关测试
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年5月25日
 */
class TestHTTPHeaders {

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
	void testWWWAuthenticate() {
		WWWAuthenticate a1 = WWWAuthenticate.parse(" Basic");
		assertEquals(a1.getType(), "Basic");
		assertEquals(a1.getHeaderValue(), "Basic");
		System.out.println(a1);

		WWWAuthenticate a2 = WWWAuthenticate.parse("Basic realm=\"Access to the staging site\"");
		assertEquals(a2.getType(), "Basic");
		assertEquals(a2.getValue("realm"), "Access to the staging site");
		assertEquals(a2.getHeaderValue(), "Basic realm=\"Access to the staging site\"");
		System.out.println(a2);

		WWWAuthenticate a3 = WWWAuthenticate.parse("Digest qop=\"auth\", realm=\"DS-2CD2310FD-I\", nonce=\"4e555130516a6b304e546f784e7a49334e7a417a59513d3d\"");
		assertEquals(a3.getType(), "Digest");
		assertEquals(a3.getValue("qop"), "auth");
		assertEquals(a3.getValue("realm"), "DS-2CD2310FD-I");
		assertEquals(a3.getValue("nonce"), "4e555130516a6b304e546f784e7a49334e7a417a59513d3d");
		assertEquals(a3.getHeaderValue(), "Digest qop=\"auth\", realm=\"DS-2CD2310FD-I\", nonce=\"4e555130516a6b304e546f784e7a49334e7a417a59513d3d\"");
		System.out.println(a3);
	}

	@Test
	void testAuthorization() {
		Authorization a1 = Authorization.parse(" Basic YWxhZGRpbjpvcGVuc2VzYW1l");
		assertEquals(a1.getType(), "Basic");
		assertEquals(a1.getCredentials(), "YWxhZGRpbjpvcGVuc2VzYW1l");
		assertEquals(a1.getHeaderValue(), "Basic YWxhZGRpbjpvcGVuc2VzYW1l");
		System.out.println(a1);

		Authorization a3 = Authorization.parse("Digest username=\"admin\", realm=\"DS-2CD2310FD-I\", qop=\"auth\", algorithm=\"MD5\", uri=\"/onvif/device_service\", nonce=\"4e555130516a6b304e546f784e7a49334e7a417a59513d3d\", nc=00000001, cnonce=\"0EE3ED23BFD9A00B2AB542E3BAB85BDB\", response=\"518fd6d1666f9f00a5c5097359188c4e\"");
		assertEquals(a3.getType(), "Digest");
		assertEquals(a3.getValue("username"), "admin");
		assertEquals(a3.getValue("realm"), "DS-2CD2310FD-I");
		assertEquals(a3.getValue("qop"), "auth");
		assertEquals(a3.getValue("algorithm"), "MD5");
		assertEquals(a3.getValue("uri"), "/onvif/device_service");
		assertEquals(a3.getValue("qop"), "auth");
		assertEquals(a3.getValue("nonce"), "4e555130516a6b304e546f784e7a49334e7a417a59513d3d");
		assertEquals(a3.getValue("nc"), "00000001");
		assertEquals(a3.getValue("cnonce"), "0EE3ED23BFD9A00B2AB542E3BAB85BDB");
		assertEquals(a3.getValue("response"), "518fd6d1666f9f00a5c5097359188c4e");
		assertEquals(a3.getHeaderValue(), "Digest username=\"admin\", realm=\"DS-2CD2310FD-I\", qop=\"auth\", algorithm=\"MD5\", uri=\"/onvif/device_service\", nonce=\"4e555130516a6b304e546f784e7a49334e7a417a59513d3d\", nc=00000001, cnonce=\"0EE3ED23BFD9A00B2AB542E3BAB85BDB\", response=\"518fd6d1666f9f00a5c5097359188c4e\"");
		System.out.println(a3);
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
		Range c1 = Range.parse("Range: bytes=200-1000");
		assertEquals(c1.getRanges().size(), 1);
		System.out.println(c1);

		Range c2 = Range.parse("Range: bytes=200-1000, 2000-6576, 19000-");
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
}