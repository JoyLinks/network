package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.joyzl.network.http.MultipartFile.MultipartFiles;

class TestFormDataCoder {

	// 含有中文的测试文件
	static File tempFile;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		tempFile = File.createTempFile("中文文件名", "测试");
		Files.writeString(tempFile.toPath(), "中华人民共和国");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		tempFile.delete();
	}

	@Test
	void testXWWWForm() throws IOException {
		final Request request = new Request();
		request.addHeader(ContentType.NAME, MIMEType.X_WWW_FORM_URLENCODED);
		request.addParameter("name1", "value 1");
		request.addParameter("name2", "value2.1");
		request.addParameter("name2", "value2.2");
		request.addParameter("name3", "中华人民共和国");
		FormDataCoder.write(request);
		assertTrue(request.hasContent());

		request.clearParameters();
		assertFalse(request.hasParameters());

		FormDataCoder.read(request);
		assertTrue(request.hasParameters());
		assertEquals(request.getParameter("name1"), "value 1");
		assertEquals(request.getParameter("name2"), "value2.1");
		assertEquals(request.getParameter("name3"), "中华人民共和国");
		assertArrayEquals(request.getParameterValues("name2"), new String[] { "value2.1", "value2.2" });

		assertTrue(request.hasContent());
	}

	@Test
	void testFormData() throws IOException {
		final Request request = new Request();
		request.addHeader(new ContentType(MIMEType.MULTIPART_FORMDATA));

		request.addParameter("name1", "value 1");
		request.addParameter("name2", "value2.1");
		request.addParameter("name2", "value2.2");
		request.addParameter("name3", "中华人民共和国");

		final MultipartFiles files = new MultipartFiles();
		final MultipartFile file1 = new MultipartFile(new File("src\\test\\java\\com\\joyzl\\network\\http\\TestFormDataCoder.java"));
		final MultipartFile file2 = new MultipartFile(tempFile);
		files.add(file1);
		files.add(file2);
		request.setContent(files);

		FormDataCoder.write(request);
		assertTrue(request.hasContent());

		// System.out.println(HTTP1Coder.toString((DataBuffer)
		// request.getContent()));
		// System.out.println(System.currentTimeMillis());

		request.clearParameters();
		assertFalse(request.hasParameters());

		FormDataCoder.read(request);
		assertTrue(request.hasParameters());
		assertEquals(request.getParameter("name1"), "value 1");
		assertEquals(request.getParameter("name2"), "value2.1");
		assertEquals(request.getParameter("name3"), "中华人民共和国");
		assertArrayEquals(request.getParameterValues("name2"), new String[] { "value2.1", "value2.2" });

		assertTrue(request.hasContent());
		final MultipartFiles items = (MultipartFiles) request.getContent();
		assertFalse(items.isEmpty());

		final MultipartFile item1 = items.get(0);
		assertEquals(item1.getFilename(), file1.getFilename());
		assertEquals(item1.getLength(), file1.getLength());

		final MultipartFile item2 = items.get(1);
		assertEquals(item2.getFilename(), file2.getFilename());
		assertEquals(item2.getLength(), file2.getLength());
	}
}