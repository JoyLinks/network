/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 包装DataBuffer为OutputStream
 *
 * @author ZhangXi
 * @date 2020年7月28日
 */
public class DataBufferOutput extends OutputStream {

	private final DataBuffer buffer;

	public DataBufferOutput() {
		this.buffer = DataBuffer.instance();
	}

	public DataBufferOutput(DataBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void write(int b) throws IOException {
		buffer.write(b);
	}

	@Override
	public void flush() throws IOException {
		// DataBuffer 无此操作
	}

	@Override
	public void close() throws IOException {
		// 不会关闭 DataBuffer
	}

	public DataBuffer buffer() {
		return buffer;
	}
}