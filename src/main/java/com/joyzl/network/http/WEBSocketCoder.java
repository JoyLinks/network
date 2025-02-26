/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

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
	// WebSocket帧不具备消息标识，因此必须将一个消息发送完成后才能发送另外的消息
	// 特别是当单个消息字节数太大时需要分批发送的时候，需要仔细考虑消息排队

	// 控制消息不能分片,控制消息可能混入在分片消息序列中
	// 控制消息附带数据不能大于125Byte（单字节长度）
	// 控制帧不可能被群发

	// 输入流作为数据对象时由于无法被多线程同时读取，因此不能使用此对象进行群发
	// 如果要群发必须构建单独的消息对象

	// 消息未分片FIN=1并且opcode!=0(CONTINUATION)
	// 消息分片第一帧FIN=0并且opcode!=0(CONTINUATION)
	// 消息分片后续帧FIN=0并且opcode==0(CONTINUATION)
	// 消息分片结束帧FIN=1并且opcode==0(CONTINUATION)

	// 客户端发给服务器 mask 必须设置为1。
	// 服务器发送给客户端 mask 必须设置为0。

	/**
	 * 读取WebSocket消息
	 */
	public static WEBSocketMessage read(WEBSocketMessage message, DataBuffer buffer) throws IOException {
		// 分块发送的数据最终长度无法预知
		// 因此解码逻辑禁止积攒数据
		// 每分片应交由应用层处理

		buffer.mark();

		// FIRST Byte
		// 左边第一位表示数据是否最后帧
		// OPCODE 表示数据帧类型
		final byte first = buffer.readByte();
		// SECOND Byte
		// 左边第一位表示是否有掩码
		byte second = buffer.readByte();

		// 长度标识
		int length = Binary.setBit(second, false, 7);
		// 根据长度标识计算最终长度
		if (length <= 125) {
			// 1字节表示长度
			// length = length;
		} else if (length == 126) {
			// 2字节表示长度
			if (buffer.readable() > 2) {
				length = buffer.readUnsignedShort();
			} else {
				// 长度不足,继续接收
				buffer.reset();
				return null;
			}
		} else if (length == 127) {
			// 8字节表示长度
			if (buffer.readable() > 8) {
				final long l = buffer.readLong();
				if (l > Integer.MAX_VALUE) {
					throw new IOException("WebSocket数据包长度异常" + l);
				}
				length = (int) l;
			} else {
				// 长度不足,继续接收
				buffer.reset();
				return null;
			}
		} else {
			throw new IOException("WebSocket数据包长度异常" + second);
		}

		// DATA
		if (length > 0) {
			// 有数据
			if (Binary.getBit(second, 7)) {
				// 有掩码
				if (length + 4 > buffer.readable()) {
					// 长度不足,继续接收
					buffer.reset();
					return null;
				}

				// 遍历还原掩码数据

				// MASK DATA
				// j = i MOD 4
				// transformed-octet-i=original-octet-i XOR
				// masking-key-octet-j

				final byte[] keys = new byte[4];
				keys[0] = buffer.readByte();
				keys[1] = buffer.readByte();
				keys[2] = buffer.readByte();
				keys[3] = buffer.readByte();
				for (int index = 0; index < length; index++) {
					buffer.set(index, (byte) (buffer.get(index) ^ keys[index % 4]));
				}
			} else {
				// 无掩码
				if (length > buffer.readable()) {
					// 长度不足,继续接收
					buffer.reset();
					return null;
				}
			}

			// 或帧类型
			second = (byte) Binary.get4BL(first);
			if (message == null) {
				message = new WEBSocketMessage();
			}
			if (second == WEBSocketMessage.CLOSE) {
				// 如果是CLOSE控制帧则进一步解析
				// STATUS 2Byte
				message.setStatus(buffer.readShort());
				// UTF-8
				if (length > 2) {
					length -= 2;
					byte[] reason = new byte[length];
					buffer.readFully(reason);
					message.setContent(new String(reason, 0, length, StandardCharsets.UTF_8));
				}
			} else {
				// 接收到足够的字节后转移给消息对象
				final DataBuffer content;
				if (message.getContent() == null) {
					message.setContent(content = DataBuffer.instance());
				} else {
					content = (DataBuffer) message.getContent();
				}
				buffer.transfer(content, length);
			}
		} else {
			if (Binary.getBit(second, 7)) {
				if (4 > buffer.readable()) {
					// 长度不足,继续接收
					buffer.reset();
					return null;
				} else {
					// 丢弃掩码
					buffer.skipBytes(4);
				}
			}
			if (message == null) {
				message = new WEBSocketMessage();
			}
		}
		// 左边第一位表示数据是否最后帧
		if (Binary.getBit(first, 7)) {
			message.state(HTTPMessage.COMPLETE);
		} else {
			message.state(HTTPMessage.CONTENT);
		}
		// OPCODE 表示数据帧类型
		message.setType((byte) Binary.get4BL(first));
		// 返回消息
		return message;
	}

	/**
	 * 编码WEBSocket帧，有掩码，用于客户端发给服务器
	 * 
	 * @param mask 客户端发给服务器设置为true，服务器发送给客户端设置为false。
	 */
	public static boolean writeMask(WEBSocketMessage message, DataBuffer buffer) throws IOException {
		if (message.getType() == WEBSocketMessage.TEXT || //
				message.getType() == WEBSocketMessage.BINARY || //
				message.getType() == WEBSocketMessage.CONTINUATION) {
			// 数据缓存对象必须是空，既不能有任何数据
			// 以下编码方式必须从缓存对象0位置开始

			// 消息头和长度预留2字节
			buffer.writeByte(0);
			buffer.writeByte(0);
			// 预留8字节用于扩展长度
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);

			int length = HTTPCoder.BLOCK_BYTES - buffer.readable();
			final boolean complete = writeContentMasked(message, buffer, length);
			length = buffer.readable() - 14;
			if (length <= 125) {
				buffer.skipBytes(8);
				buffer.set(0, Binary.setBit(message.getType(), complete, 7));
				buffer.set(1, (byte) (0x80 | length));
			} else if (length <= 65536) {
				buffer.skipBytes(6);
				buffer.set(0, Binary.setBit(message.getType(), complete, 7));
				buffer.set(1, (byte) (0x80 | 126));
				// 2字节长度
				buffer.set(2, (byte) (length >>> 8));
				buffer.set(3, (byte) length);
			} else {
				buffer.set(0, Binary.setBit(message.getType(), complete, 7));
				buffer.set(1, (byte) (0x80 | 127));
				// 8字节长度
				buffer.set(2, (byte) (length >>> 56));
				buffer.set(3, (byte) (length >>> 48));
				buffer.set(4, (byte) (length >>> 40));
				buffer.set(5, (byte) (length >>> 32));
				buffer.set(6, (byte) (length >>> 24));
				buffer.set(7, (byte) (length >>> 16));
				buffer.set(8, (byte) (length >>> 8));
				buffer.set(9, (byte) length);
			}
			if (complete) {
				return true;
			} else {
				message.setType(WEBSocketMessage.CONTINUATION);
				return false;
			}
		} else {
			// 控制帧
			buffer.writeByte(Binary.setBit(message.getType(), true, 7));
			// 长度1Byte
			int position = buffer.readable();
			buffer.writeByte(0x80);
			writeContentMasked(message, buffer, 125);
			int length = buffer.readable() - position - 5;
			if (length > 0) {
				buffer.set(position, (byte) (0x80 | length));
			}
			return true;
		}
	}

	/**
	 * 编码WEBSocket帧，无掩码，用于服务器发送给客户端
	 * 
	 * @return true消息已全部编码完成，false消息仅编码部分（分片）
	 */
	public static boolean write(WEBSocketMessage message, DataBuffer buffer) throws IOException {
		if (message.getType() == WEBSocketMessage.TEXT || //
				message.getType() == WEBSocketMessage.BINARY || //
				message.getType() == WEBSocketMessage.CONTINUATION) {
			// 数据缓存对象必须是空，既不能有任何数据
			// 以下编码方式必须从缓存对象0位置开始

			// 消息头和长度预留2字节
			buffer.writeByte(0);
			buffer.writeByte(0);
			// 预留8字节用于扩展长度
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(0);

			int length = HTTPCoder.BLOCK_BYTES - buffer.readable();
			final boolean complete = writeContent(message, buffer, length);
			length = buffer.readable() - 10;
			if (length <= 125) {
				buffer.skipBytes(8);
				buffer.set(0, Binary.setBit(message.getType(), complete, 7));
				buffer.set(1, (byte) length);
			} else if (length <= 65536) {
				buffer.skipBytes(6);
				buffer.set(0, Binary.setBit(message.getType(), complete, 7));
				buffer.set(1, (byte) 126);
				// 2字节长度
				buffer.set(2, (byte) (length >>> 8));
				buffer.set(3, (byte) length);
			} else {
				buffer.set(0, Binary.setBit(message.getType(), complete, 7));
				buffer.set(1, (byte) 127);
				// 8字节长度
				buffer.set(2, (byte) (length >>> 56));
				buffer.set(3, (byte) (length >>> 48));
				buffer.set(4, (byte) (length >>> 40));
				buffer.set(5, (byte) (length >>> 32));
				buffer.set(6, (byte) (length >>> 24));
				buffer.set(7, (byte) (length >>> 16));
				buffer.set(8, (byte) (length >>> 8));
				buffer.set(9, (byte) length);
			}
			if (complete) {
				return true;
			} else {
				message.setType(WEBSocketMessage.CONTINUATION);
				return false;
			}
		} else {
			// 控制帧
			buffer.writeByte(Binary.setBit(message.getType(), true, 7));
			// 长度1Byte
			int position = buffer.readable();
			buffer.writeByte(0);
			writeContent(message, buffer, 125);
			int length = buffer.readable() - position - 1;
			if (length > 0) {
				buffer.set(position, (byte) length);
			}
			return true;
		}
	}

	static boolean writeContentMasked(Message message, DataBuffer buffer, int max) throws IOException {
		// [MASKING-KEY]
		final byte[] masks = marks();
		buffer.write(masks);
		max -= 4;
		// CONTENT DATA
		if (message.getContent() == null) {
			return true;
		}
		if (message.getContent() instanceof DataBuffer) {
			final DataBuffer content = (DataBuffer) message.getContent();
			if (content.readable() < max) {
				max = content.readable();
			}
			for (int i = 0; i < max; i++) {
				buffer.writeByte(content.readByte() ^ masks[i % 4]);
			}
			return content.readable() <= 0;
		} else //
		if (message.getContent() instanceof InputStream) {
			final InputStream content = (InputStream) message.getContent();
			int i = 0, value;
			while (i < max) {
				value = content.read();
				if (value < 0) {
					content.close();
					return true;
				}
				buffer.writeByte(value ^ masks[i % 4]);
				i++;
			}
		} else //
		if (message.getContent() instanceof CharSequence) {
			final CharSequence content = (CharSequence) message.getContent();
			if (content.length() > 0) {
				// 字符串不分段
				final int position = buffer.readable();
				buffer.append(CharBuffer.wrap(content), StandardCharsets.UTF_8);
				final int length = buffer.readable() - position;
				int i = 0;
				while (i < length) {
					buffer.set(position + i, (byte) (buffer.get(position + i) ^ masks[i % 4]));
				}
			}
		}
		return false;
	}

	static boolean writeContent(Message message, DataBuffer buffer, int max) throws IOException {
		if (message.getContent() == null) {
			return true;
		}
		if (message.getContent() instanceof DataBuffer) {
			final DataBuffer content = (DataBuffer) message.getContent();
			if (content.readable() > 0) {
				if (content.readable() > max) {
					content.transfer(buffer, max);
					return false;
				} else {
					content.transfer(buffer);
				}
			}
		} else //
		if (message.getContent() instanceof InputStream) {
			final InputStream content = (InputStream) message.getContent();
			int length = buffer.write(content, max);
			if (length == max) {
				return false;
			}
		} else //
		if (message.getContent() instanceof CharSequence) {
			final CharSequence content = (CharSequence) message.getContent();
			if (content.length() > 0) {
				// 字符串不分段
				buffer.append(CharBuffer.wrap(content), StandardCharsets.UTF_8);
			}
		}
		return true;
	}

	/**
	 * 生成掩码
	 */
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