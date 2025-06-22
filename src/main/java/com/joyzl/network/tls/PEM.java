/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * RFC 7468 Textual Encodings of PKIX, PKCS, and CMS Structures
 * 
 * @author ZhangXi 2025年2月26日
 */
public class PEM {

	private final String label;
	private final byte[] data;

	public PEM(String label, byte[] data) {
		this.label = label;
		this.data = data;
	}

	public String getLabel() {
		return label;
	}

	public byte[] getData() {
		return data;
	}

	private static final String BEGIN_PREFIX = "-----BEGIN ";
	private static final String END_PREFIX = "-----END ";
	private static final String SUFFIX = "-----";
	private static final String LINE = "\n";

	/**
	 * 读取符合RFC7468规范的文件
	 */
	public static PEM loadFile(String file) throws IOException {
		return loadFile(new File(file));
	}

	/**
	 * 读取符合RFC7468规范的文件
	 */
	public static PEM loadFile(File file) throws IOException {
		return read(new InputStreamReader(new FileInputStream(file)));
	}

	public static PEM loadText(String text) throws IOException {
		return read(new StringReader(text));
	}

	private static PEM read(Reader input) throws IOException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final Base64.Decoder decoder = Base64.getDecoder();
		boolean inContent = false;
		String label = null;
		try (BufferedReader reader = new BufferedReader(input)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(BEGIN_PREFIX)) {
					inContent = true;
					// -----BEGIN CERTIFICATE-----
					// -----BEGIN X509 CRL-----
					// -----BEGIN CERTIFICATE REQUEST-----
					// -----BEGIN PKCS7-----
					// -----BEGIN CMS-----
					// -----BEGIN PRIVATE KEY-----
					// -----BEGIN ENCRYPTED PRIVATE KEY-----
					// -----BEGIN ATTRIBUTE CERTIFICATE-----
					// -----BEGIN PUBLIC KEY-----
					label = line.substring(BEGIN_PREFIX.length(), line.length() - SUFFIX.length());
					continue;
				}
				if (line.startsWith(END_PREFIX)) {
					break;
				}
				if (inContent) {
					String trimmedLine = line.trim();
					if (!trimmedLine.isEmpty()) {
						byte[] decodedPart = decoder.decode(trimmedLine);
						outputStream.write(decodedPart);
					}
				}
			}
		}
		return new PEM(label, outputStream.toByteArray());
	}

	/**
	 * 将PEM编码为符合RFC7468规范的文件
	 */
	public void saveFile(String file) throws IOException {
		saveFile(new File(file));
	}

	/**
	 * 将PEM编码为符合RFC7468规范的文件
	 */
	public void saveFile(File file) throws IOException {
		final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE.getBytes(StandardCharsets.US_ASCII));
		try (FileOutputStream output = new FileOutputStream(file)) {
			// BEGIN
			output.write(BEGIN_PREFIX.getBytes(StandardCharsets.US_ASCII));
			output.write(getLabel().getBytes());
			output.write(SUFFIX.getBytes(StandardCharsets.US_ASCII));
			output.write(LINE.getBytes(StandardCharsets.US_ASCII));

			// Base64 Data
			output.write(encoder.encode(getData()));

			// END
			output.write(LINE.getBytes(StandardCharsets.US_ASCII));
			output.write(END_PREFIX.getBytes(StandardCharsets.US_ASCII));
			output.write(getLabel().getBytes());
			output.write(SUFFIX.getBytes(StandardCharsets.US_ASCII));
		}
	}
}