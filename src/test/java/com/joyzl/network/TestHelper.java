package com.joyzl.network;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

public class TestHelper {

	/**
	 * 多行字符串转换为字节数据
	 */
	public static DataBuffer buffer(String data) throws IOException {
		data = data.replaceAll("\\s*", "");
		final DataBuffer buffer = DataBuffer.instance();
		buffer.write(Utility.hex(data));
		return buffer;
	}

	/**
	 * 多行字符串转换为字节数据
	 */
	public static byte[] bytes(String data) {
		data = data.replaceAll("\\s*", "");
		return Utility.hex(data);
	}
}