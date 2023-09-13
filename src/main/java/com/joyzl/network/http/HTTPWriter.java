/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;

/**
 * 超文本写入(ASCII)
 * 
 * @author ZhangXi
 * @date 2021年10月11日
 */
public final class HTTPWriter {

	private final DataBuffer buffer;

	public HTTPWriter(DataBuffer b) {
		buffer = b;
	}

	public DataBuffer buffer() {
		return buffer;
	}

	public void write(char c) throws IOException {
		buffer.write(c);
	}

	public void write(CharSequence chars) throws IOException {
		buffer.writeASCIIs(chars);
	}

	public void write(CharSequence chars, Charset charset) throws IOException {
		buffer.write(chars.toString().getBytes(charset));
	}

	@Override
	public final String toString() {
		StringBuilder writer = new StringBuilder();
		try (Reader reader = new InputStreamReader(new DataBufferInput(buffer, false), HTTPCoder.URL_CHARSET)) {
			int value;
			buffer.mark();
			while ((value = reader.read()) >= 0) {
				if (value == HTTPCoder.CR) {
					writer.append("CR");
					continue;
				}
				if (value == HTTPCoder.LF) {
					writer.append("LF");
				}
				writer.append((char) value);
			}
			buffer.reset();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
}