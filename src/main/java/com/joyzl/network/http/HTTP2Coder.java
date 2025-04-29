package com.joyzl.network.http;

import java.io.IOException;
import java.util.Base64;
import java.util.Map.Entry;

import com.joyzl.network.IndexMap;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.codec.Binary;

public class HTTP2Coder extends HTTP2 {

	/*-
	 * RFC 7540
	 * 
	 * 帧头
	 * +-----------------------------------------------+
	 * |                 Length (24)                   |
	 * +---------------+---------------+---------------+
	 * |   Type (8)    |   Flags (8)   |
	 * +-+-------------+---------------+-------------------------------+
	 * |R|                 Stream Identifier (31)                      |
	 * +=+=============================================================+
	 * |                   Frame Payload (0...)                      ...
	 * +---------------------------------------------------------------+
	 * 
	 * DATA Frame
	 * +---------------+
	 * |Pad Length? (8)|
	 * +---------------+-----------------------------------------------+
	 * |                            Data (*)                         ...
	 * +---------------------------------------------------------------+
	 * |                           Padding (*)                       ...
	 * +---------------------------------------------------------------+
	 * END_STREAM (0x1)
	 * PADDED (0x8)
	 * 
	 * HEADERS Frame
	 * +---------------+
	 * |Pad Length? (8)|
	 * +-+-------------+-----------------------------------------------+
	 * |E|                 Stream Dependency? (31)                     |
	 * +-+-------------+-----------------------------------------------+
	 * |  Weight? (8)  |
	 * +-+-------------+-----------------------------------------------+
	 * |                   Header Block Fragment (*)                 ...
	 * +---------------------------------------------------------------+
	 * |                           Padding (*)                       ...
	 * +---------------------------------------------------------------+
	 * END_STREAM (0x1)
	 * END_HEADERS (0x4)
	 * PADDED (0x8)
	 * PRIORITY (0x20)
	 * 
	 * PRIORITY Frame
	 * +-+-------------------------------------------------------------+
	 * |E|                  Stream Dependency (31)                     |
	 * +-+-------------+-----------------------------------------------+
	 * |   Weight (8)  |
	 * +-+-------------+
	 * 
	 * RST_STREAM Frame
	 * +---------------------------------------------------------------+
	 * |                        Error Code (32)                        |
	 * +---------------------------------------------------------------+
	 * 
	 * SETTINGS Frame
	 * +-------------------------------+
	 * |       Identifier (16)         |
	 * +-------------------------------+-------------------------------+
	 * |                        Value (32)                             |
	 * +---------------------------------------------------------------+
	 * ACK (0x1)
	 * 
	 * PUSH_PROMISE Frame
	 * +---------------+
	 * |Pad Length? (8)|
	 * +-+-------------+-----------------------------------------------+
	 * |R|                  Promised Stream ID (31)                    |
	 * +-+-----------------------------+-------------------------------+
	 * |                   Header Block Fragment (*)                 ...
	 * +---------------------------------------------------------------+
	 * |                           Padding (*)                       ...
	 * +---------------------------------------------------------------+
	 * END_HEADERS (0x4)
	 * PADDED (0x8)
	 * 
	 * PING Frame
	 * +---------------------------------------------------------------+
	 * |                      Opaque Data (64)                         |
	 * +---------------------------------------------------------------+
	 * 
	 * GOAWAY Frame
	 * +-+-------------------------------------------------------------+
	 * |R|                  Last-Stream-ID (31)                        |
	 * +-+-------------------------------------------------------------+
	 * |                      Error Code (32)                          |
	 * +---------------------------------------------------------------+
	 * |                  Additional Debug Data (*)                    |
	 * +---------------------------------------------------------------+
	 * 
	 * WINDOW_UPDATE Frame
	 * +-+-------------------------------------------------------------+
	 * |R|              Window Size Increment (31)                     |
	 * +-+-------------------------------------------------------------+
	 * 
	 * CONTINUATION Frame
	 * +---------------------------------------------------------------+
	 * |                   Header Block Fragment (*)                 ...
	 * +---------------------------------------------------------------+
	 * END_HEADERS (0x4)
	 */

	// Frame Type

	final static byte DATA = 0x0;
	final static byte HEADERS = 0x1;
	final static byte PRIORITY = 0x2;
	final static byte RST_STREAM = 0x3;
	final static byte SETTINGS = 0x4;
	final static byte PUSH_PROMISE = 0x5;
	final static byte PING = 0x6;
	final static byte GOAWAY = 0x7;
	final static byte WINDOW_UPDATE = 0x8;
	final static byte CONTINUATION = 0x9;

	// Frame Flag

	final static byte FLAG_END_STREAM = 0x1;
	final static byte FLAG_END_HEADERS = 0x4;
	final static byte FLAG_PADDED = 0x8;
	final static byte FLAG_PRIORITY = 0x20;
	final static byte FLAG_ACK = 0x1;

	/** 霍夫曼编码字符串 */
	final static byte HUFFMAN = (byte) 0b10000000;
	/** 常规编码字符串 */
	final static byte STRING = (byte) 0b00000000;

	/** 完整索引(1+7) */
	final static byte INDEXED = (byte) 0b10000000;
	/** 增加索引(2+6) */
	final static byte INCREMENTAL = (byte) 0b01000000;
	/** 不带索引(4+4) */
	final static byte NO_INDEXING = (byte) 0b00000000;
	/** 从不索引(4+4) */
	final static byte NAVER_INDEXING = (byte) 0b00010000;
	/** 动态表更新(3+5) */
	final static byte TABLE_SIZE = (byte) 0b00100000;

	// 字节掩码，用于分离左位

	final static byte MASK1 = (byte) 0b10000000;
	final static byte MASK2 = (byte) 0b11000000;
	final static byte MASK3 = (byte) 0b11100000;
	final static byte MASK4 = (byte) 0b11110000;

	static boolean isHuffman(byte value) {
		return (value & MASK1) == HUFFMAN;
	}

	static boolean isIndexed(byte value) {
		return (value & MASK1) == INDEXED;
	}

	static boolean isIncremental(byte value) {
		return (value & MASK2) == INCREMENTAL;
	}

	static boolean isNoIndexing(byte value) {
		return (value & MASK4) == NO_INDEXING;
	}

	static boolean isNaverIndexing(byte value) {
		return (value & MASK4) == NAVER_INDEXING;
	}

	static boolean isTableSize(byte value) {
		return (value & MASK3) == TABLE_SIZE;
	}

	static boolean isEndStream(byte value) {
		return (value & FLAG_END_STREAM) == FLAG_END_STREAM;
	}

	static boolean isEndHeaders(byte value) {
		return (value & FLAG_END_HEADERS) == FLAG_END_HEADERS;
	}

	static boolean isPadded(byte value) {
		return (value & FLAG_PADDED) == FLAG_PADDED;
	}

	static boolean isPriority(byte value) {
		return (value & FLAG_PRIORITY) == FLAG_PRIORITY;
	}

	static boolean isAck(byte value) {
		return (value & FLAG_ACK) == FLAG_ACK;
	}

	static boolean isPseudo(CharSequence value) {
		return value.charAt(0) == ':';
	}

	static boolean isOdd(int id) {
		return (id & 1) == 1;
	}

	static boolean isEven(int id) {
		return (id & 1) == 0;
	}

	/** Client */
	static Object readResponse(HPACK hpack, IndexMap<Response> im, DataBuffer buffer) throws IOException {
		buffer.mark();
		// Length (24)
		int length = buffer.readUnsignedMedium();
		// 6 = type 1 + flags 1 + Stream Identifier 4
		if (buffer.readable() >= length + 6) {
			// Type (8)
			byte type = buffer.readByte();
			// Flags (8)
			byte flag = buffer.readByte();
			// R|Stream Identifier (32)
			int id = buffer.readInt();

			int pad = 0;
			if (type == DATA) {
				if (isPadded(flag)) {
					pad = buffer.readUnsignedByte();
					// 1 = Pad length 1Byte
					length -= pad + 1;
				}

				final Response response = im.get(id);
				readData(buffer, response, length);

				if (isEndStream(flag)) {
					return response;
				}
			} else if (type == HEADERS) {
				if (isPadded(flag)) {
					pad = buffer.readUnsignedByte();
					// 1 = Pad length 1Byte
					length -= pad + 1;
				}
				if (isPriority(flag)) {
					// E|Stream Dependency? (31)
					id = buffer.readInt();
					// Weight? (8)
					buffer.readUnsignedByte();
				}

				final Response response = im.get(id);
				readHeaders(hpack, buffer, response, length);

				if (isEndHeaders(flag)) {

				}
				if (isEndStream(flag)) {
					return response;
				}
			} else if (type == CONTINUATION) {
				final Response response = im.get(id);
				readHeaders(hpack, buffer, response, length);

				if (isEndHeaders(flag)) {

				}
			} else if (type == PUSH_PROMISE) {
				if (isPadded(flag)) {
					pad = buffer.readUnsignedByte();
					// 1 = Pad length 1Byte
					length -= pad + 1;
				}

				// R|Promised Stream ID (31)
				final Response response = im.get(id);
				readHeaders(hpack, buffer, response, length);
			} else if (type == WINDOW_UPDATE) {
				// R|Window Size Increment (31)
				buffer.readInt();
			} else if (type == RST_STREAM) {
				// Error Code (32)
				buffer.readInt();
			} else if (type == PRIORITY) {
				// |E| Stream Dependency (31)
				buffer.readInt();
				// Weight (8)
				buffer.readByte();
			} else if (type == SETTINGS) {
				if (isAck(flag)) {
					if (length > 0) {
						throw new HTTP2Exception(FRAME_SIZE_ERROR);
					}
					return new Settings(true);
				} else {
					return readSettings(buffer, length);
				}
			} else if (type == GOAWAY) {
				return readGoaway(buffer, length);
			} else if (type == PING) {
				// Opaque Data (64)
				return new Ping(buffer.readLong());
			} else {
				throw new HTTP2Exception(PROTOCOL_ERROR);
			}
			if (pad > 0) {
				buffer.skipBytes(pad);
			}
		} else {
			buffer.reset();
		}
		return null;
	}

	/** Server */
	static Object read(HPACK hpack, HTTP2Index<Request> im, DataBuffer buffer) throws IOException {
		// 帧头9字节
		if (buffer.readable() < 9) {
			return null;
		}
		// 超过允许的最大帧
		if (buffer.readable() > Settings.MAX_FRAME_SIZE) {
			buffer.clear();
			return new Goaway(FRAME_SIZE_ERROR);
		}

		buffer.mark();
		// Length (24)
		int length = buffer.readUnsignedMedium();
		// 6 = type 1 + flags 1 + Stream Identifier 4
		if (buffer.readable() >= length + 6) {
			// Type 1Byte
			byte type = buffer.readByte();
			// Flags 1Byte
			byte flag = buffer.readByte();
			// R|Stream Identifier 4Byte
			int id = buffer.readInt();
			if (id < 0) {
				// 忽略非零R
				id = Binary.setBit(id, false, 31);
			}
			if (length > hpack.getMaxFrameSize()) {
				// 超过设定最大帧
				buffer.skipBytes(length);
				// return new ResetStream(id, FRAME_SIZE_ERROR);
				return new Goaway(id, FRAME_SIZE_ERROR);
			}

			System.out.println("ID:" + id + " TYPE:" + frameName(type) + " LENGTH:" + length + " S:" + im.size());

			if (id > 0) {
				if (isEven(id)) {
					// 客户端仅使用奇数流标识
					buffer.skipBytes(length);
					return new Goaway(id, PROTOCOL_ERROR);
				}
				if (type == DATA) {
					int pad = 0;
					if (isPadded(flag)) {
						pad = buffer.readUnsignedByte();
						// 1 = Pad length 1Byte
						length -= pad + 1;
					}
					final Request request = im.get(id);
					if (request == null) {
						buffer.skipBytes(length + pad);
						if (id <= im.lastClose()) {
							// 已关闭的流标识
							return new Goaway(id, STREAM_CLOSED);
						}
						if (id > im.lastOpen()) {
							// 错误的流标识
							return new Goaway(id, PROTOCOL_ERROR);
						}
					} else {
						if (request.getContentSize() > hpack.getWindowSize()) {
							buffer.skipBytes(length + pad);
							// 流数据超过窗口大小
							return new ResetStream(id, REFUSED_STREAM);
						}
					}
					readData(buffer, request, length);
					if (pad > 0) {
						buffer.skipBytes(pad);
					}
					if (isEndStream(flag)) {
						request.state(Message.COMPLETE);
						im.remove(id);
					}
					return request;
				} else if (type == HEADERS) {
					int pad = 0;
					if (isPadded(flag)) {
						pad = buffer.readUnsignedByte();
						// 1 = Pad length 1Byte
						length -= pad + 1;
					}
					Request request = im.get(id);
					if (request == null) {
						if (id <= im.lastClose()) {
							buffer.skipBytes(length + pad);
							// 已关闭的流标识
							return new Goaway(id, STREAM_CLOSED);
						}
						if (id <= im.lastOpen()) {
							buffer.skipBytes(length + pad);
							// 重复打开流标识
							return new Goaway(id, PROTOCOL_ERROR);
						}
						if (im.lastContinue()) {
							buffer.skipBytes(length + pad);
							// 之前消息未结束头帧
							return new Goaway(id, PROTOCOL_ERROR);
						}
						if (im.size() > 100) {
							buffer.skipBytes(length + pad);
							// 超过并发数
							return new Goaway(id, REFUSED_STREAM);
						}
						im.put(id, request = new Request(id, HTTP1.V20));
					}
					if (isPriority(flag)) {
						length -= 5;
						// E|Stream Dependency? (31)
						request.setDependency(buffer.readInt());
						// Weight? (8)
						request.setWeight(buffer.readUnsignedByte());
					}
					readHeaders(hpack, buffer, request, length);
					if (pad > 0) {
						buffer.skipBytes(pad);
					}
					if (isEndHeaders(flag)) {
						request.state(Message.CONTENT);
						im.endHeaders();
					}
					if (isEndStream(flag)) {
						request.state(Message.COMPLETE);
						im.remove(id);
					}
					return request;
				} else if (type == CONTINUATION) {
					final Request request = im.get(id);
					if (request == null) {
						buffer.skipBytes(length);
						// 错误的流标识
						return new Goaway(id, PROTOCOL_ERROR);
					}
					readHeaders(hpack, buffer, request, length);
					if (isEndHeaders(flag)) {
						request.state(Message.CONTENT);
					}
					return request;
				} else if (type == WINDOW_UPDATE) {
					final WindowUpdate wu = new WindowUpdate(id);
					// R|Window Size Increment (31)
					wu.setIncrement(buffer.readInt());
					return wu;
				} else if (type == RST_STREAM) {
					final ResetStream resetStream = new ResetStream(id);
					// Error Code (32)
					resetStream.setError(buffer.readInt());
					return resetStream;
				} else if (type == PRIORITY) {
					final Priority priority = new Priority(id);
					// |E| Stream Dependency (31)
					priority.setDependency(buffer.readInt());
					// Weight (8)
					priority.setWeight(buffer.readByte());
					return priority;
				} else {
					buffer.skipBytes(length);
					// throw new HTTP2Exception(PROTOCOL_ERROR);
					return new Goaway(id, PROTOCOL_ERROR);
				}
			} else if (id == 0) {
				if (type == SETTINGS) {
					if (isAck(flag)) {
						if (length > 0) {
							// throw new HTTP2Exception(FRAME_SIZE_ERROR);
							return new Goaway(FRAME_SIZE_ERROR);
						}
						return new Settings(true);
					} else {
						return readSettings(buffer, length);
					}
				} else if (type == WINDOW_UPDATE) {
					final WindowUpdate wu = new WindowUpdate(id);
					// R|Window Size Increment (31)
					wu.setIncrement(buffer.readInt());
					return wu;
				} else if (type == PING) {
					// Opaque Data (64)
					return new Ping(isAck(flag), buffer.readLong());
				} else if (type == GOAWAY) {
					return readGoaway(buffer, length);
				} else {
					buffer.skipBytes(length);
					// throw new HTTP2Exception(PROTOCOL_ERROR);
					return new Goaway(PROTOCOL_ERROR);
				}
			} else {
				buffer.skipBytes(length);
				// throw new HTTP2Exception(PROTOCOL_ERROR);
				return new Goaway(PROTOCOL_ERROR);
			}
		} else {
			// System.out.println(length);
			buffer.reset();
		}
		return null;
	}

	static void readData(DataBuffer buffer, HTTPMessage message, int length) throws IOException {
		final DataBuffer content;
		if (message.hasContent()) {
			content = (DataBuffer) message.getContent();
		} else {
			message.setContent(content = DataBuffer.instance());
		}
		content.append(buffer, length);
	}

	/**
	 * Response Pseudo-Header Fields<br>
	 * Header Block Fragment (*)
	 */
	static void readHeaders(HPACK hpack, DataBuffer buffer, Response response, int length) throws IOException {
		final StringBuilder builder = getStringBuilder();

		String name, value;
		int index;
		byte flag;

		length = buffer.readable() - length;
		while (buffer.readable() > length) {
			flag = buffer.readByte();
			if (isIndexed(flag)) {
				index = readVarint(buffer, flag, 7);
				if (index > 0) {
					name = hpack.getName(index);
					value = hpack.getValue(index);
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isIncremental(flag)) {
				index = readVarint(buffer, flag, 6);
				if (index > 0) {
					name = hpack.getName(index);
					readString(buffer, builder);
					value = builder.toString();
					hpack.add(name, value);
				} else {
					readString(buffer, builder);
					name = HTTP1.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
					hpack.add(name, value);
				}
			} else if (isNoIndexing(flag)) {
				index = readVarint(buffer, flag, 4);
				if (index == 0) {
					readString(buffer, builder);
					name = HTTP1.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isNaverIndexing(flag)) {
				index = readVarint(buffer, flag, 4);
				if (index == 0) {
					readString(buffer, builder);
					name = HTTP1.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isTableSize(flag)) {
				hpack.update(readVarint(buffer, flag, 5));
				continue;
			} else {
				throw new HTTP2Exception(PROTOCOL_ERROR);
			}

			if (isPseudo(name)) {
				// :status
				// 不含版本和原因短语
				if (name.equals(STATUS)) {
					response.setStatus(Integer.parseInt(value));
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else {
				response.getHeaders().put(name, value);
			}
		}
	}

	/**
	 * Request Pseudo-Header Fields<br>
	 * Header Block Fragment (*)
	 */
	static void readHeaders(HPACK hpack, DataBuffer buffer, Request request, int length) throws IOException {
		// 实测发现CONTINUATION会导致字段任意位置分割为后续帧
		// CONTINUATION Flood 攻击：发送无限制的续帧消耗内存

		final StringBuilder builder = getStringBuilder();

		String name, value;
		int index;
		byte flag;

		length = buffer.readable() - length;
		while (buffer.readable() > length) {
			flag = buffer.readByte();
			if (isIndexed(flag)) {
				index = readVarint(buffer, flag, 7);
				if (index > 0) {
					name = hpack.getName(index);
					value = hpack.getValue(index);
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isIncremental(flag)) {
				index = readVarint(buffer, flag, 6);
				if (index > 0) {
					name = hpack.getName(index);
					readString(buffer, builder);
					value = builder.toString();
					hpack.add(name, value);
				} else if (index == 0) {
					readString(buffer, builder);
					name = HTTP1.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
					hpack.add(name, value);
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isNoIndexing(flag)) {
				index = readVarint(buffer, flag, 4);
				if (index == 0) {
					readString(buffer, builder);
					name = HTTP1.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isNaverIndexing(flag)) {
				index = readVarint(buffer, flag, 4);
				if (index == 0) {
					readString(buffer, builder);
					name = HTTP1.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else if (isTableSize(flag)) {
				hpack.update(readVarint(buffer, flag, 5));
				continue;
			} else {
				throw new HTTP2Exception(PROTOCOL_ERROR);
			}

			if (isPseudo(name)) {
				// :method,:scheme,:authority,:path
				// 请求伪头不含版本
				if (name.equals(METHOD)) {
					request.setMethod(value);
				} else if (name.equals(SCHEME)) {
					request.setScheme(value);
				} else if (name.equals(PATH)) {
					request.setUrl(value);
				} else if (name.equals(AUTHORITY)) {
					request.setAuthority(value);
				} else {
					throw new HTTP2Exception(PROTOCOL_ERROR);
				}
			} else {
				request.getHeaders().put(name, value);
			}
		}
	}

	static Settings readSettings(String text) {
		final byte[] data = Base64.getUrlDecoder().decode(text);
		final Settings settings = new Settings();
		int index = 0, id, value;
		while (index + 6 > data.length) {
			// Identifier (16) |Value (32)
			id = Binary.getShort(data, index);
			index += 2;
			value = Binary.getInteger(data, index);
			index += 4;
			if (id == HEADER_TABLE_SIZE) {
				settings.setHeaderTableSize(value);
			} else if (id == ENABLE_PUSH) {
				settings.setEnablePush(value);
			} else if (id == MAX_CONCURRENT_STREAMS) {
				settings.setMaxConcurrentStreams(value);
			} else if (id == INITIAL_WINDOW_SIZE) {
				settings.setInitialWindowSize(value);
			} else if (id == MAX_FRAME_SIZE) {
				settings.setMaxFrameSize(value);
			} else if (id == MAX_HEADER_LIST_SIZE) {
				settings.setMaxHeaderListSize(value);
			} else {
				// 忽略
			}
		}
		return settings;
	}

	static Settings readSettings(DataBuffer buffer, int length) throws IOException {
		final Settings settings = new Settings();
		length = buffer.readable() - length;
		while (buffer.readable() > length) {
			// Identifier (16) |Value (32)
			int id = buffer.readShort();
			if (id == HEADER_TABLE_SIZE) {
				settings.setHeaderTableSize(buffer.readInt());
			} else if (id == ENABLE_PUSH) {
				settings.setEnablePush(buffer.readInt());
			} else if (id == MAX_CONCURRENT_STREAMS) {
				settings.setMaxConcurrentStreams(buffer.readInt());
			} else if (id == INITIAL_WINDOW_SIZE) {
				settings.setInitialWindowSize(buffer.readInt());
			} else if (id == MAX_FRAME_SIZE) {
				settings.setMaxFrameSize(buffer.readInt());
			} else if (id == MAX_HEADER_LIST_SIZE) {
				settings.setMaxHeaderListSize(buffer.readInt());
			} else {
				buffer.skipBytes(4);
			}
		}
		return settings;
	}

	static Goaway readGoaway(DataBuffer buffer, int length) throws IOException {
		final Goaway goaway = new Goaway();
		// R| Last-Stream-ID (31)
		goaway.setLastStreamID(buffer.readInt());
		// Error Code (32)
		goaway.setError(buffer.readInt());
		// Additional Debug Data (*)
		length -= 8;
		if (length > 0) {
			buffer.skipBytes(length);
		}
		return goaway;
	}

	static boolean writeHeaders(HPACK hpack, DataBuffer buffer, Request request) throws IOException {
		int position = buffer.readable();
		// Length (24)
		buffer.writeMedium(0);
		// Type (8)
		buffer.writeByte(HEADERS);
		// Flags (8)
		// END_STREAM(0x1)/END_HEADERS(0x4)/PADDED(0x8)/PRIORITY(0x20)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(request.id());

		// 伪头
		writeHeader(hpack, buffer, METHOD, request.getMethod());
		writeHeader(hpack, buffer, SCHEME, request.getScheme());
		writeHeader(hpack, buffer, PATH, request.getPath());
		writeHeader(hpack, buffer, AUTHORITY, request.getAuthority());
		if (buffer.readable() > hpack.getMaxFrameSize()) {
			// 请求行不能超过帧限制
			throw new HTTP2Exception(COMPRESSION_ERROR);
		}

		// 标准头
		for (Entry<String, String> header : request.getHeaders().entrySet()) {
			if (header.getKey() != null) {
				writeHeader(hpack, buffer, header.getKey(), header.getValue());
			}
		}

		// SET LENGTH
		int length = buffer.readable() - position - 9;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position++, (byte) (length));

		// SKIP Type
		position++;

		// SET Flags
		byte flag = 0;
		if (request.hasContent()) {
			flag = FLAG_END_HEADERS;
			buffer.set(position, flag);
			return false;
		} else {
			flag = FLAG_END_HEADERS | FLAG_END_STREAM;
			buffer.set(position, flag);
			return true;
		}
	}

	static boolean writeHeaders(HPACK hpack, DataBuffer buffer, Response response) throws IOException {
		int position = buffer.readable();
		// Length (24)
		buffer.writeMedium(0);
		// Type (8)
		buffer.writeByte(HEADERS);
		// Flags (8)
		// END_STREAM(0x1)/END_HEADERS(0x4)/PADDED(0x8)/PRIORITY(0x20)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(response.id());

		// 伪头
		writeHeader(hpack, buffer, STATUS, Integer.toString(response.getStatus()));

		// 标准头
		for (Entry<String, String> header : response.getHeaders().entrySet()) {
			if (header.getKey() != null) {
				writeHeader(hpack, buffer, header.getKey(), header.getValue());
			}
		}

		// SET LENGTH
		int length = buffer.readable() - position - 9;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position++, (byte) (length));

		// SKIP Type
		position++;

		// SET Flags
		byte flag = 0;
		if (response.hasContent()) {
			flag = FLAG_END_HEADERS;
			buffer.set(position, flag);
			return false;
		} else {
			flag = FLAG_END_HEADERS | FLAG_END_STREAM;
			buffer.set(position, flag);
			return true;
		}
	}

	/** HPACK Encode */
	static void writeHeader(HPACK hpack, DataBuffer buffer, String name, String value) throws IOException {
		int n = hpack.findName(name);
		if (n > 0) {
			int v = hpack.findValue(n, value);
			if (v > 0) {
				// 名称和值已索引
				writeVarint(buffer, INDEXED, v, 7);
			} else {
				// 名称已索引值未索引
				// 应添加到动态表
				writeVarint(buffer, INCREMENTAL, n, 6);
				writeHuffman(value, buffer);
				// 此时添加索引将改变??
				hpack.add(name, value);
			}
		} else {
			// 名称和值未索引
			// 不添加到动态表
			writeVarint(buffer, NO_INDEXING, 0, 4);
			writeHuffman(name, buffer);
			writeHuffman(value, buffer);
		}

		// 完整索引
		// buffer.writeVarint(INDEXED, index, 7);

		// 增加索引（已索引名称）
		// buffer.writeVarint(INCREMENTAL, index, 6);
		// writeHuffman(header.getHeaderValue(), buffer);

		// 增加索引（名称和值均未索引）
		// buffer.writeByte(INCREMENTAL);
		// writeHuffman(header.getHeaderName(), buffer);
		// writeHuffman(header.getHeaderValue(), buffer);

		// 无须索引（名称已索引）
		// buffer.writeByte(NO_INDEXING, index, 4);
		// writeHuffman(header.getHeaderValue(), buffer);

		// 无须索引（名称和值均未索引）
		// buffer.writeByte(NO_INDEXING);
		// writeHuffman(header.getHeaderName(), buffer);
		// writeHuffman(header.getHeaderValue(), buffer);
	}

	/** Frame HEAD | DATA */
	static boolean writeData(HTTPMessage message, DataBuffer buffer, int length) throws IOException {
		if (message.hasContent()) {
			if (message.getContent() instanceof DataBuffer content) {
				if (content.readable() > length) {
					// Length (24)
					buffer.writeMedium(length);
					// Type (8)
					buffer.writeByte(DATA);
					// Flags (8)
					buffer.writeByte(0);
					// R|Stream Identifier (32)
					buffer.writeInt(message.id());
					// Data (*)
					buffer.append(content, length);
					return false;
				} else {
					int pad = length - content.readable();
					if (pad > 8) {
						pad = 8 - pad % 8;
					}
					if (pad > 0) {
						length = content.readable() + pad + 1;
						// Length (24)
						buffer.writeMedium(length);
						// Type (8)
						buffer.writeByte(DATA);
						// Flags (8)
						buffer.writeByte(FLAG_END_STREAM | FLAG_PADDED);
						// R|Stream Identifier (32)
						buffer.writeInt(message.id());
						// Pad Length? (8)
						buffer.writeByte(pad);
						// Data (*)
						buffer.append(content);
						// Padding (*)
						while (pad-- > 0) {
							buffer.writeByte(0);
						}
					} else {
						// Length (24)
						buffer.writeMedium(content.readable());
						// Type (8)
						buffer.writeByte(DATA);
						// Flags (8)
						buffer.writeByte(FLAG_END_STREAM);
						// R|Stream Identifier (32)
						buffer.writeInt(message.id());
						// Data (*)
						buffer.append(content);
					}
					return true;
				}
			}
			if (message.getContent() instanceof CharSequence content) {
				// Length (24)
				buffer.writeMedium(content.length());
				// Type (8)
				buffer.writeByte(DATA);
				// Flags (8)
				buffer.writeByte(FLAG_END_STREAM);
				// R|Stream Identifier (32)
				buffer.writeInt(message.id());
				// Data (*)
				buffer.writeASCIIs(content);
				return true;
			}
			return true;
		} else {
			return true;
		}
	}

	static void write(DataBuffer buffer, Priority priority) throws IOException {
		// Length (24)
		buffer.writeMedium(5);
		// Type (8)
		buffer.writeByte(PRIORITY);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(priority.id());

		// E|Stream Dependency (32)
		if (priority.isExclusive()) {
			buffer.writeByte(Binary.setBit(priority.getDependency(), true, 31));
		} else {
			buffer.writeByte(priority.getDependency());
		}
		// Weight (8)
		buffer.writeByte(priority.getWeight());
	}

	static void write(DataBuffer buffer, ResetStream reset) throws IOException {
		// Length (24)
		buffer.writeMedium(4);
		// Type (8)
		buffer.writeByte(RST_STREAM);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(reset.id());
		// Error Code (32)
		buffer.writeInt(reset.getError());
	}

	/** Frame HEAD | SETTINGS */
	static void write(DataBuffer buffer, Settings settings) throws IOException {
		if (settings.isACK()) {
			// Length (24)
			buffer.writeMedium(0);
			// Type (8)
			buffer.writeByte(SETTINGS);
			// Flags (8)
			buffer.writeByte(FLAG_ACK);
			// R|Stream Identifier (32)
			buffer.writeInt(0);
		} else {
			int length = 0;
			if (settings.hasHeaderTableSize()) {
				length += 6;
			}
			if (settings.hasEnablePush()) {
				length += 6;
			}
			if (settings.hasMaxConcurrentStreams()) {
				length += 6;
			}
			if (settings.hasInitialWindowSize()) {
				length += 6;
			}
			if (settings.hasMaxFrameSize()) {
				length += 6;
			}
			if (settings.hasMaxHeaderListSize()) {
				length += 6;
			}

			// Length (24)
			buffer.writeMedium(length);
			// Type (8)
			buffer.writeByte(SETTINGS);
			// Flags (8)
			buffer.writeByte(0);
			// R|Stream Identifier (32)
			buffer.writeInt(0);

			// Identifier (16) | Value (32)
			if (settings.hasHeaderTableSize()) {
				buffer.writeShort(HEADER_TABLE_SIZE);
				buffer.writeInt(settings.getHeaderTableSize());
			}
			if (settings.hasEnablePush()) {
				buffer.writeShort(ENABLE_PUSH);
				buffer.writeInt(settings.getEnablePush());
			}
			if (settings.hasMaxConcurrentStreams()) {
				buffer.writeShort(MAX_CONCURRENT_STREAMS);
				buffer.writeInt(settings.getMaxConcurrentStreams());
			}
			if (settings.hasInitialWindowSize()) {
				buffer.writeShort(INITIAL_WINDOW_SIZE);
				buffer.writeInt(settings.getInitialWindowSize());
			}
			if (settings.hasMaxFrameSize()) {
				buffer.writeShort(MAX_FRAME_SIZE);
				buffer.writeInt(settings.getMaxFrameSize());
			}
			if (settings.hasMaxHeaderListSize()) {
				buffer.writeShort(MAX_HEADER_LIST_SIZE);
				buffer.writeInt(settings.getMaxHeaderListSize());
			}
		}
	}

	/** Frame HEAD | PING */
	static void write(DataBuffer buffer, Ping ping) throws IOException {
		// Length (24)
		buffer.writeMedium(8);
		// Type (8)
		buffer.writeByte(PING);
		// Flags (8)
		if (ping.isACK()) {
			buffer.writeByte(FLAG_ACK);
		} else {
			buffer.writeByte(0);
		}
		// R|Stream Identifier (32)
		buffer.writeInt(0);
		// Opaque Data (64)
		buffer.writeLong(ping.getValue());
	}

	/** Frame HEAD | GOAWAY */
	static void write(DataBuffer buffer, Goaway goaway) throws IOException {
		// Length (24)
		buffer.writeMedium(8);
		// Type (8)
		buffer.writeByte(GOAWAY);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(0);

		// R|Last-Stream-ID (31)
		buffer.writeInt(goaway.getLastStreamID());
		// Error Code (32)
		buffer.writeInt(goaway.getError());
		// Additional Debug Data (*)
	}

	////////////////////////////////////////////////////////////////////////////////

	static void writeString(CharSequence value, DataBuffer buffer) throws IOException {
		writeVarint(buffer, STRING, value.length(), 7);
		buffer.writeASCIIs(value);
	}

	static void writeHuffman(CharSequence value, DataBuffer buffer) throws IOException {
		writeVarint(buffer, HUFFMAN, HuffmanCoder.byteSize(value), 7);
		HuffmanCoder.encode(buffer, value);
	}

	static void readString(DataBuffer buffer, StringBuilder builder) throws IOException {
		byte tag = buffer.readByte();
		int length = readVarint(buffer, tag, 7);

		if (isHuffman(tag)) {
			HuffmanCoder.decode(buffer, builder, length);
		} else {
			while (length-- > 0) {
				builder.append(buffer.readASCII());
			}
		}
	}

	/**
	 * 编码指定前缀的变长整数无符号值，前缀表示第一个字节有多少位用于整数，剩余位用于标记
	 */
	static void writeVarint(DataBuffer buffer, byte tag, int value, int prefix) {
		final int mask = (1 << prefix) - 1;
		if (value < mask) {
			if (prefix == 8) {
				buffer.writeByte(value);
			} else {
				buffer.writeByte(value | tag);
			}
		} else {
			if (prefix == 8) {
				buffer.writeByte(mask);
			} else {
				buffer.writeByte(mask | tag);
			}
			value -= mask;
			while (value >= 128) {
				buffer.writeByte(value % 128 + 128);
				value = value / 128;
			}
			buffer.writeByte(value);
		}
	}

	/**
	 * 解码具有前缀的变长整数无符号值，初始值从第一个字节剥离
	 */
	static int readVarint(DataBuffer buffer, byte tag, int prefix) throws IOException {
		int mask = (1 << prefix) - 1;
		// 清除初始值中的tag
		int value = tag & (0b11111111 >>> (8 - prefix));
		if (value >= mask) {
			mask = 1;
			do {
				prefix = buffer.readByte();
				value = value + (prefix & 127) * mask;
				mask = mask << 7;
			} while ((prefix & 128) == 128);
		}
		return value;
	}

	static String frameName(byte type) {
		switch (type) {
			case DATA:
				return "DATA";
			case HEADERS:
				return "HEADERS";
			case PRIORITY:
				return "PRIORITY";
			case RST_STREAM:
				return "RST_STREAM";
			case SETTINGS:
				return "SETTINGS";
			case PUSH_PROMISE:
				return "PUSH_PROMISE";
			case PING:
				return "PING";
			case GOAWAY:
				return "GOAWAY";
			case WINDOW_UPDATE:
				return "WINDOW_UPDATE";
			case CONTINUATION:
				return "CONTINUATION";
			default:
				return "UNKNOWN";
		}
	}
}