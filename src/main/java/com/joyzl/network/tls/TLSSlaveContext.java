/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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