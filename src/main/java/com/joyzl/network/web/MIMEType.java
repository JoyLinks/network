/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.File;

/**
 * MIME类型 RFC2046
 * 
 * <p>
 * 所有注册的MIME-Type由IANA管理
 * https://www.iana.org/assignments/media-types/media-types.xhtml
 * </p>
 * 
 * @author ZhangXi
 * @date 2020年7月31日
 */
public final class MIMEType {

	// https://www.file-extensions.org/extensions/common-file-extension-list

	/** 所有 */
	public final static String ALL = "*/*";

	/** 文本 */
	public final static String TEXT = "text";
	/** 图像 */
	public final static String IMAGE = "image";
	/** 音频 */
	public final static String AUDIO = "audio";
	/** 视频 */
	public final static String VIDEO = "video";
	/** 应用 */
	public final static String APPLICATION = "application";
	/** 消息 */
	public final static String MESSAGE = "message";
	/** 多部 */
	public final static String MULTIPART = "multipart";
	/** 浏览器发送信息给服务器,作为多部分文档格式,混合 */
	public final static String MULTIPART_MIXED = "multipart/mixed";
	/** 浏览器发送信息给服务器,作为多部分文档格式 */
	public final static String MULTIPART_FORMDATA = "multipart/form-data";
	/** 部分的响应报文发送回浏览器 */
	public final static String MULTIPART_BYTERANGES = "multipart/byteranges";
	/** POST请求键值对数据 */
	public final static String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public final static String X_FORM_WWW_URLENCODED = "application/x-form-www-urlencoded";

	/** 应用程序文件 */
	public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";
	/** JavaScript */
	public final static String APPLICATION_JAVA_SCRIPT = "application/javascript";
	/** JavaScript */
	public final static String APPLICATION_ECMA_SCRIPT = "application/ecmascript";
	/** JavaScript JSON */
	public final static String APPLICATION_JSON = "application/json";
	/** xml */
	public final static String APPLICATION_XML = "application/xml";

	/** 文本文件 */
	public final static String TEXT_PLAIN = "text/plain";
	/** CSS文件 */
	public final static String TEXT_CSS = "text/css";
	/** HTML文件 */
	public final static String TEXT_HTML = "text/html";
	/** XML文件 */
	public final static String TEXT_XML = "text/xml";

	/** GIF图像文件 */
	public final static String IMAGE_GIF = "image/gif";
	/** JPEG图像文件 */
	public final static String IMAGE_JPEG = "image/jpeg";
	/** PNG位图文件 */
	public final static String IMAGE_PNG = "image/png";
	/** SVG矢量图形文件 */
	public final static String IMAGE_SVG_XML = "image/svg+xml";

	/** HTTP TRACE DEBUG */
	public final static String MESSAGE_HTTP = "message/http";

	public final static String getByFile(File file) {
		return getByFilename(file.getName());
	}

	public final static String getByFilename(String filename) {
		if (filename == null || filename.length() <= 0) {
			return APPLICATION_OCTET_STREAM;
		}

		String type;
		int index = filename.lastIndexOf('.');
		int end = filename.length();
		while (index > 0) {
			type = getByExtension(filename.substring(index + 1, end));
			if (type == null) {
				index = filename.lastIndexOf('.', end = index - 1);
			} else {
				return type;
			}
		}
		return APPLICATION_OCTET_STREAM;
	}

	public static String getByExtension(String extension) {
		switch (extension.toLowerCase()) {
			case "gif":
				return IMAGE_GIF;
			case "jpeg":
				return IMAGE_JPEG;
			case "png":
				return IMAGE_PNG;
			case "svg":
				return IMAGE_SVG_XML;
			case "css":
				return TEXT_CSS;
			case "htm":
			case "html":
				return TEXT_HTML;
			case "xml":
				return TEXT_XML;
			case "txt", "log":
				return TEXT_PLAIN;
			case "js":
				return APPLICATION_JAVA_SCRIPT;
			case "json":
				return APPLICATION_JSON;
			default:
				return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	private String type;
	private float quality;
	/** 默认权重 */
	public final static float DEFAULT_QUALITY = 1;

	public MIMEType(String t) {
		this(t, DEFAULT_QUALITY);
	}

	public MIMEType(String t, float q) {
		type = t;
		quality = q;
	}

	public String getType() {
		return type;
	}

	public float getQuality() {
		return quality;
	}

	public void setQuality(float value) {
		quality = value;
	}
}