/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.codec.Binary;

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
	final static int BLOCK = 1024 * 1024;

	/**
	 * 读取WebSocket消息
	 * 
	 * @return true /false
	 * @throws IOException
	 */
	protected final boolean readHead(WebSocketMessage message, DataBuffer reader) throws IOException {
		reader.mark();

		// FIRST Byte
		byte value = reader.readByte();
		// 左边第一位表示数据是否最后帧
		message.setFinish(Binary.getBit(value, 7));
		// OPCODE 表示数据帧类型
		message.setType(Binary.get4BL(value));

		// SECOND Byte
		value = reader.readByte();
		// 左边第一位表示是否有掩码
		message.setMask(Binary.getBit(value, 7));
		// 剩余位数表示长度标识
		value = Binary.setBit(value, false, 7);

		// 根据长度标识计算最终长度
		int length = 0;
		if (value <= 125) {
			// 1字节表示长度
			length = value;
		} else if (value == 126) {
			// 2字节表示长度
			if (reader.readable() > 2) {
				length = reader.readUnsignedShort();
			} else {
				// 长度不足,继续接收
				reader.reset();
				return false;
			}
		} else if (value == 127) {
			// 8字节表示长度
			if (reader.readable() > 8) {
				length = (int) reader.readLong();
			} else {
				// 长度不足,继续接收
				reader.reset();
				return false;
			}
		} else {
			throw new IOException("WebSocket数据包长度异常" + value);
		}

		if (message.isMask()) {
			if (reader.readable() >= 4) {
				reader.readFully(message.getMaskKeys());
			} else {
				// 长度不足,继续接收
				reader.reset();
				return false;
			}
		}
		message.setLength(length);
		return true;
	}

	/**
	 * 读取WebSocket消息
	 * 
	 * @throws IOException
	 */
	protected final boolean readContent(WebSocketMessage message, DataBuffer reader) throws IOException {
		if (message.getLength() > 0) {
			if (message.isMask()) {
				// 有掩码
				// 客户端发给服务器 mask 必须设置为1。
				// 服务器发送给客户端 mask 必须设置为0。
				// 此情形数据必须进行一次遍历还原

				DataBuffer writer;
				if (message.getContent() instanceof DataBuffer) {
					writer = (DataBuffer) message.getContent();
				} else {
					message.setContent(writer = DataBuffer.instance());
				}

				// MASK DATA
				// j = i MOD 4
				// transformed-octet-i=original-octet-i XOR masking-key-octet-j

				final byte[] keys = message.getMaskKeys();
				int i = writer.readable();
				int length = message.getLength() - i;
				length = length > reader.readable() ? length : reader.readable();
				while (length-- > 0) {
					writer.write(reader.readByte() ^ keys[i % 4]);
					i++;
				}
			} else {
				// 无掩码
				if (reader.readable() >= message.getLength()) {
					// 等待链路的DataBuffer接收到足够的字节后直接绑定给消息对象
					reader.bounds(message.getLength());
					message.setContent(reader);
				} else {
					// 长度不足,继续接收
					reader.reset();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 编码WEWBSocket帧
	 * 
	 * @throws IOException
	 * @throws HTTPException
	 */
	protected boolean write(WebSocketMessage message, DataBuffer writer) throws IOException {
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
		int length = message.contentSize();

		// FIRST Byte [ FIN | RSV 1~3 默认0 | OPCODE]
		if (message.getLength() > BLOCK) {
			writer.writeByte(Binary.setBit((byte) message.getType(), false, 7));
			// 第一帧发送消息类型，后续帧标记为续帧
			message.setType(WebSocketMessage.CONTINUATION);
			length = BLOCK;
		} else {
			writer.writeByte(Binary.setBit((byte) message.getType(), true, 7));
		}

		// SECOND Byte [MASK | LENGTH]
		if (message.isMask()) {
			// 0x80=[10000000]=128
			if (length <= 125) {
				writer.write(0x80 | length);
			} else if (length <= 65536) {
				// 2字节长度
				writer.write(0x80 | 126);
				writer.writeShort(length);
			} else {
				// 8字节长度
				writer.write(0x80 | 127);
				writer.writeLong(length);
			}
			// [MASKING-KEY]
			writer.write(message.getMaskKeys());
		} else {
			if (length <= 125) {
				writer.write(length);
			} else if (length <= 65536) {
				// 2字节长度
				writer.write(126);
				writer.writeShort(length);
			} else {
				// 8字节长度
				writer.write(127);
				writer.writeLong(length);
			}
		}

		// DATA
		if (length > 0) {
			if (message.getContent() instanceof DataBuffer) {
				final DataBuffer buffer = (DataBuffer) message.getContent();
				if (message.isMask()) {
					for (int i = 0; i < length; i++) {
						writer.write(buffer.readByte() ^ message.getMaskKeys()[i % 4]);
					}
				} else {
					while (length-- > 0) {
						writer.writeByte(buffer.readByte());
					}
				}
				return buffer.readable() <= 0;
			} else if (message.getContent() instanceof File) {
				final File file = (File) message.getContent();
				final InputStream input = new FileInputStream(file);
				message.setContent(input);
				if (message.isMask()) {
					for (int i = 0; i < length; i++) {
						writer.write(input.read() ^ message.getMaskKeys()[i % 4]);
					}
				} else {
					while (length-- > 0) {
						writer.write(input.read());
					}
				}
				return input.available() <= 0;
			} else if (message.getContent() instanceof InputStream) {
				final InputStream input = (InputStream) message.getContent();
				if (message.isMask()) {
					for (int i = 0; i < length; i++) {
						writer.write(input.read() ^ message.getMaskKeys()[i % 4]);
					}
				} else {
					while (length-- > 0) {
						writer.write(input.read());
					}
				}
				return input.available() <= 0;
			} else {
				throw new IOException("不支持的消息消息内容");
			}
		} else {
			return true;
		}
	}
}