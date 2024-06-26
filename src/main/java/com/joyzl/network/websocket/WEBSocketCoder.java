/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.websocket;

import java.io.IOException;
import java.io.InputStream;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.codec.Binary;
import com.joyzl.network.web.WEBContentCoder;

/**
 * WEB Socket 消息编解码 RFC6455 The WebSocket Protocol
 * 
 * <pre>
 * WebSocket帧结构
 * +----------1B-----------+--------1B-------+-----------------------+--------------+----+
 * |FIN 1T RSV 3T OPCODE 4T|MASK 1T LENGTH 7T|EXTENDED LENGTH 16B/64B|MASKING-KEY 4B|DATA|
 * +-----------------------+-----------------+-----------------------+--------------+----+
 * 文本默认UNICODE(UTF-8)编码
 * </pre>
 * 
 * @author ZhangXi
 * @date 2023年9月5日
 */
public class WEBSocketCoder {

	/** 数据分块 */
	final static int BLOCK = 1024 * 8;

	/**
	 * 读取WebSocket消息
	 */
	static WebSocketMessage read(DataBuffer reader) throws IOException {
		reader.mark();

		// FIRST Byte
		final byte first = reader.readByte();
		// 左边第一位表示数据是否最后帧
		// message.setFinish(Binary.getBit(first, 7));
		// OPCODE 表示数据帧类型
		// message.setType(Binary.get4BL(first));

		// SECOND Byte
		final byte second = reader.readByte();
		// 左边第一位表示是否有掩码
		final boolean mark = Binary.getBit(second, 7);

		// 剩余位数表示长度标识
		int length = Binary.setBit(second, false, 7);
		// 根据长度标识计算最终长度
		if (length <= 125) {
			// 1字节表示长度
			// length = length;
		} else if (length == 126) {
			// 2字节表示长度
			if (reader.readable() > 2) {
				length = reader.readUnsignedShort();
			} else {
				// 长度不足,继续接收
				reader.reset();
				return null;
			}
		} else if (length == 127) {
			// 8字节表示长度
			if (reader.readable() > 8) {
				length = (int) reader.readLong();
			} else {
				// 长度不足,继续接收
				reader.reset();
				return null;
			}
		} else {
			throw new IOException("WebSocket数据包长度异常" + second);
		}

		WebSocketMessage message;
		// DATA
		if (length > 0) {
			if (mark) {
				if (length + 4 <= reader.readable()) {
					message = new WebSocketMessage();
					// 有掩码
					// 客户端发给服务器 mask 必须设置为1。
					// 服务器发送给客户端 mask 必须设置为0。
					// 此情形数据必须进行一次遍历还原

					// MASK DATA
					// j = i MOD 4
					// transformed-octet-i=original-octet-i XOR
					// masking-key-octet-j

					final byte[] keys = new byte[4];
					keys[0] = reader.readByte();
					keys[1] = reader.readByte();
					keys[2] = reader.readByte();
					keys[3] = reader.readByte();
					for (int index = 0; index < length; index++) {
						reader.set(index, (byte) (reader.get(index) ^ keys[index % 4]));
					}
				} else {
					// 长度不足,继续接收
					reader.reset();
					return null;
				}
			} else {
				if (length <= reader.readable()) {
					message = new WebSocketMessage();
				} else {
					// 长度不足,继续接收
					reader.reset();
					return null;
				}
			}
			// 接收到足够的字节后绑定给消息对象
			final DataBuffer buffer = DataBuffer.instance();
			reader.transfer(buffer, length);
			message.setContent(buffer);
		} else {
			message = new WebSocketMessage();
		}
		// 左边第一位表示数据是否最后帧
		message.setFinish(Binary.getBit(first, 7));
		// OPCODE 表示数据帧类型
		message.setType(Binary.get4BL(first));
		return message;
	}

	/**
	 * 编码WEWBSocket帧
	 * 
	 * @throws IOException
	 * @throws HTTPException
	 */
	static boolean write(WebSocketMessage message, DataBuffer writer, boolean mark) throws IOException {
		// WebSocket帧不具备消息标识，因此必须将一个消息发送完成后才能发送另外的消息
		// 特别是当单个消息字节数太大时需要分批发送的时候，需要仔细考虑消息排队

		// 控制消息不能分片,控制消息可能混入在分片消息序列中
		// 控制消息附带数据不能大于125
		// 控制帧不可能被群发

		// 输入流作为数据对象时由于无法被多线程同时读取，因此不能使用此对象进行群发
		// 如果要群发必须构建单独的消息对象

		// 消息未分片FIN=1并且opcode!=0(CONTINUATION)
		// 消息分片第一帧FIN=0并且opcode!=0(CONTINUATION)
		// 消息分片后续帧FIN=0并且opcode==0(CONTINUATION)
		// 消息分片结束帧FIN=1并且opcode==0(CONTINUATION)

		// 获取消息长度
		long length = WEBContentCoder.size(message);

		// FIRST Byte [ FIN | RSV 1~3 默认0 | OPCODE]
		if (length > BLOCK) {
			// 消息分片发送
			writer.writeByte(Binary.setBit((byte) message.getType(), false, 7));
			// 第一帧发送消息类型，后续帧标记为续帧
			message.setType(WebSocketMessage.CONTINUATION);
			length = BLOCK;
		} else {
			// 消息整体发送
			writer.writeByte(Binary.setBit((byte) message.getType(), true, 7));
		}

		// SECOND Byte [MASK | LENGTH]
		if (length > 0) {
			if (mark) {
				// 0x80=[10000000]=128
				if (length <= 125) {
					writer.write(0x80 | (int) length);
				} else if (length <= 65536) {
					// 2字节长度
					writer.write(0x80 | 126);
					writer.writeShort((int) length);
				} else {
					// 8字节长度
					writer.write(0x80 | 127);
					writer.writeLong(length);
				}
				// [MASKING-KEY]
				final byte[] masks = marks();
				writer.write(masks);
				// [DATA]
				try (final InputStream input = WEBContentCoder.input(message)) {
					for (int i = 0; i < length; i++) {
						writer.write(input.read() ^ masks[i % 4]);
					}
					return input.available() <= 0;
				}
			} else {
				if (length <= 125) {
					writer.write((int) length);
				} else if (length <= 65536) {
					// 2字节长度
					writer.write(126);
					writer.writeShort((int) length);
				} else {
					// 8字节长度
					writer.write(127);
					writer.writeLong(length);
				}
				// [DATA]
				try (final InputStream input = WEBContentCoder.input(message)) {
					while (length-- > 0) {
						writer.writeByte(input.read());
					}
					return input.available() <= 0;
				}
			}
		} else {
			writer.write(0);
		}
		return true;
	}

	static byte[] marks() {
		final byte[] masks = new byte[4];
		long nano = System.nanoTime();
		masks[0] = (byte) (nano >> 8);
		masks[1] = (byte) (nano >> 16);
		masks[2] = (byte) (nano >> 24);
		masks[3] = (byte) (nano >> 32);
		return masks;
	}
}