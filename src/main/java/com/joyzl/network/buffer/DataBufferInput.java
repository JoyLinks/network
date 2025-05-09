/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.InputStream;

import com.joyzl.codec.BigEndianDataInput;
import com.joyzl.network.codec.BigEndianBCDInput;

/**
 * 包装DataBuffer为InputStream
 * 
 * @author ZhangXi
 * @date 2020年7月28日
 */
public class DataBufferInput extends InputStream implements BigEndianDataInput, BigEndianBCDInput {

	private final DataBuffer buffer;
	private final boolean release;

	public DataBufferInput(DataBuffer buffer) {
		this.buffer = buffer;
		release = false;
	}

	/**
	 * 实例化DataBuffer为输入流
	 * 
	 * @param buffer DataBuffer
	 * @param closeRelease 关闭流时是否释放DataBuffer实例
	 */
	public DataBufferInput(DataBuffer buffer, boolean closeRelease) {
		this.buffer = buffer;
		release = closeRelease;
	}

	@Override
	public int read() throws IOException {
		if (buffer.readable() > 0) {
			return buffer.readUnsignedByte();
		}
		return -1;
	}

	@Override
	public int available() throws IOException {
		return buffer.readable();
	}

	@Override
	public void close() throws IOException {
		if (release) {
			buffer.release();
		}
	}

	@Override
	public void mark(int readlimit) {
		buffer.mark();
	}

	@Override
	public void reset() throws IOException {
		buffer.reset();
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	public DataBuffer buffer() {
		return buffer;
	}

	@Override
	public byte readByte() throws IOException {
		return buffer.readByte();
	}
}