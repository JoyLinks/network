/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.io.File;
import java.util.ArrayList;

/**
 * 多部分内容的文件形式
 * 
 * @author ZhangXi 2024年11月28日
 */
public class MultipartFile {

	private final File file;
	private final String filename;
	private String contentType;
	private String field;

	public MultipartFile(File file) {
		this.file = file;
		this.filename = file.getName();
	}

	public MultipartFile(File file, String filename) {
		this.file = file;
		this.filename = filename;
	}

	public String getField() {
		return field;
	}

	public void setField(String value) {
		field = value;
	}

	public String getFilename() {
		return filename;
	}

	public File getFile() {
		return file;
	}

	public long getLength() {
		return file.length();
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String value) {
		contentType = value;
	}

	public static class MultipartFiles extends ArrayList<MultipartFile> {
		private static final long serialVersionUID = 1L;

		public MultipartFiles() {
			super();
		}

		public MultipartFiles(int i) {
			super(i);
		}

		public final static MultipartFiles EMPTY = new MultipartFiles(0);
	}
}