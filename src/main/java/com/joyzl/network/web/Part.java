/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;

import com.joyzl.network.http.ContentDisposition;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPMessage;

/**
 * WEB HTTP Part<br>
 * Content-Type: multipart/form-data; boundary=BOUNDARY
 * 
 * @author ZhangXi
 * @date 2021年10月13日
 */
public final class Part extends HTTPMessage {

	/** 默认内容类型 */
	public final static ContentType CONTENT_TYPE = new ContentType(MIMEType.TEXT_PLAIN, HTTPCoder.URL_CHARSET_NAME);

	public Part() {
	}

	public Part(FileInputStream input) {
		setContent(input);
	}

	public Part(String name, File file) {
		final ContentDisposition contentDisposition = new ContentDisposition(ContentDisposition.FORM_DATA, name, file.getName());
		addHeader(contentDisposition);
		setContent(file);
	}

	public Part(String name, String value) {
		final ContentDisposition contentDisposition = new ContentDisposition(ContentDisposition.FORM_DATA, name);
		addHeader(contentDisposition);
		addHeader(CONTENT_TYPE);
		setContent(value);
	}
}