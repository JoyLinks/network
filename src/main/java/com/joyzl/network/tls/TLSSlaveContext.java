package com.joyzl.network.tls;

import java.io.Closeable;
import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

abstract class TLSSlaveContext implements Closeable {

	V3ServerHandler handler;
	final DataBuffer data = DataBuffer.instance();
	int length = 0;

	@Override
	public void close() throws IOException {
		data.release();
	}
}