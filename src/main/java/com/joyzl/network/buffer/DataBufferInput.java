/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 包装DataBuffer为InputStream
 * 
 * @author ZhangXi
 * @date 2020年7月28日
 */
public class DataBufferInput extends InputStream {

	private final DataBuffer buffer;

	public DataBufferInput(DataBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int read() throws IOException {
		return buffer.readUnsignedByte();
	}

	@Override
	public int available() throws IOException {
		return buffer.readable();
	}

	@Override
	public void close() throws IOException {
		buffer.release();
	}

	@Override
	public synchronized void mark(int readlimit) {
		buffer.mark();
	}

	@Override
	public synchronized void reset() throws IOException {
		buffer.reset();
	}

	@Override
	public boolean markSupported() {
		return true;
	}
}