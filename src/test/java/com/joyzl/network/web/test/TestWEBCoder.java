package com.joyzl.network.web.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.web.MIMEType;
import com.joyzl.network.web.MultipartFormData;
import com.joyzl.network.web.Part;
import com.joyzl.network.web.WEBRequest;
import com.joyzl.network.web.WEBResponse;
import com.joyzl.network.web.XWWWFormUrlencoded;

/**
 * WEB Coder 相关测试
 * 
 * @author ZhangXi 2023年9月8日
 */
class TestWEBCoder {

	final DataBuffer buffer = DataBuffer.instance();
	final HTTPWriter writer = new HTTPWriter(buffer);
	final HTTPReader reader = new HTTPReader(buffer);

	final WEBRequest request = new WEBRequest();
	final WEBResponse response = new WEBResponse();

	@BeforeEach
	void setUp() throws Exception {
		request.clearParameters();
		request.setContent(null);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testXWWWFormUrlencoded() throws Exception {
		// 测试样本
		request.addParameter("argment1", "A");
		request.addParameter("argment1", "B");
		request.addParameter("argment1", "C");
		request.setParameter("argment2", "");
		request.setParameter("argment3", (String) null);
		request.setParameter("argment4", "123456");
		request.setParameter("argment5", "壹贰叁肆伍陆柒捌玖拾");

		XWWWFormUrlencoded.write(writer, request);
		request.clearParameters();
		XWWWFormUrlencoded.read(reader, request);

		assertTrue(request.hasParameter("argment1"));
		assertTrue(request.hasParameter("argment2"));
		assertTrue(request.hasParameter("argment3"));
		assertTrue(request.hasParameter("argment4"));
		assertTrue(request.hasParameter("argment5"));

		assertEquals(request.getParameter("argment1"), "A");
		assertArrayEquals(request.getParameterValues("argment1"), new String[] { "A", "B", "C" });
		assertEquals(request.getParameter("argment2"), null);
		assertEquals(request.getParameter("argment3"), null);
		assertEquals(request.getParameter("argment4"), "123456");
		assertEquals(request.getParameter("argment5"), "壹贰叁肆伍陆柒捌玖拾");
	}

	@Test
	void testMultipartFormData() throws Exception {
		// 测试样本
		request.setParameter("argment1", "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		request.setParameter("argment2", "壹贰叁肆伍陆柒捌玖拾");
		final List<Part> parts = new ArrayList<>();
		parts.add(new Part("argment3", "POST VALUE"));
		parts.add(new Part("argment4", new File("src/test/java/com/joyzl/network/web/test/TestWEBCoder.java")));
		request.setContent(parts);

		final ContentType contentType = new ContentType(MIMEType.MULTIPART_FORMDATA);
		contentType.setBoundary(ContentType.boundary());

		MultipartFormData.write(writer, request, contentType);
		// System.out.println(writer);
		request.clearParameters();
		request.setContent(null);
		MultipartFormData.read(reader, request, contentType);

		assertTrue(request.hasParameter("argment1"));
		assertTrue(request.hasParameter("argment2"));
		assertTrue(request.hasParameter("argment3"));

		assertEquals(request.getParameter("argment1"), "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		assertEquals(request.getParameter("argment2"), "壹贰叁肆伍陆柒捌玖拾");
		assertEquals(request.getParameter("argment3"), "POST VALUE");
	}
}