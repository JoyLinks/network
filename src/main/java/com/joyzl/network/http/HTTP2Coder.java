package com.joyzl.network.http;

import java.io.IOException;
import java.util.Base64;
import java.util.Map.Entry;

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

	/** 帧默认最大长度 */
	final static int MAX_FRAME_SIZE1 = 16384;
	/** 帧允许最大长度 */
	final static int MAX_FRAME_SIZE2 = 16777215;

	/** 霍夫曼编码字符串 */
	final static byte HUFFMAN = (byte) 0b10000000;
	/** 常规编码字符串 */
	final static byte STRING = (byte) 0b00000000;

	/** 完整索引(1+7) */
	final static byte INDEXED = (byte) 0b10000000;
	/** 增加索引(2+6) */
	final static byte INCREMENTAL = (byte) 0b01000000;
	/** 无须索引(4+4) */
	final static byte NO_INDEXING = (byte) 0b00000000;
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

	/** Client */
	static Object readResponse(HPACK hpack, HTTP2Sender streams, DataBuffer buffer) throws IOException {
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
			int stream = buffer.readInt();

			int pad = 0;
			if (type == DATA) {
				if (isPadded(flag)) {
					pad = buffer.readUnsignedByte();
					// 1 = Pad length 1Byte
					length -= pad + 1;
				}

				final Response response = streams.response(stream);
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
					stream = buffer.readInt();
					// Weight? (8)
					buffer.readUnsignedByte();
				}

				final Response response = streams.response(stream);
				readHeaders(hpack, buffer, response, length);

				if (isEndHeaders(flag)) {

				}
				if (isEndStream(flag)) {
					return response;
				}
			} else if (type == CONTINUATION) {
				final Response response = streams.response(stream);
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
				final Response response = streams.response(stream);
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
					return Settings.SETTINGS_ACK;
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
	static Object readRequest(HPACK hpack, HTTP2Sender streams, DataBuffer buffer) throws IOException {
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
			int stream = buffer.readInt();

			int pad = 0;
			if (type == DATA) {
				if (isPadded(flag)) {
					pad = buffer.readUnsignedByte();
					// 1 = Pad length 1Byte
					length -= pad + 1;
				}

				final Request request = streams.request(stream);
				readData(buffer, request, length);

				if (isEndStream(flag)) {
					return request;
				}
			} else if (type == HEADERS) {
				if (isPadded(flag)) {
					pad = buffer.readUnsignedByte();
					// 1 = Pad length 1Byte
					length -= pad + 1;
				}

				final Request request = streams.request(stream);
				if (isPriority(flag)) {
					// E|Stream Dependency? (31)
					request.setDependency(buffer.readInt());
					// Weight? (8)
					request.setWeight(buffer.readUnsignedByte());
				}

				readHeaders(hpack, buffer, request, length);

				if (isEndHeaders(flag)) {

				}
				if (isEndStream(flag)) {
					return request;
				}
			} else if (type == CONTINUATION) {
				final Request request = streams.request(stream);
				readHeaders(hpack, buffer, request, length);

				if (isEndHeaders(flag)) {

				}
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
					return Settings.SETTINGS_ACK;
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

	static void readData(DataBuffer buffer, HTTPMessage message, int length) throws IOException {
		final DataBuffer content;
		if (message.hasContent()) {
			content = (DataBuffer) message.getContent();
		} else {
			content = DataBuffer.instance();
			message.setContent(content);
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
					name = HTTP.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
					hpack.add(name, value);
				}
			} else if (isNoIndexing(flag)) {
				index = readVarint(buffer, flag, 4);
				if (index == 0) {
					readString(buffer, builder);
					name = HTTP.HEADERS.get(builder);
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
					name = HTTP.HEADERS.get(builder);
					readString(buffer, builder);
					value = builder.toString();
					hpack.add(name, value);
				}
			} else if (isNoIndexing(flag)) {
				index = readVarint(buffer, flag, 4);
				if (index == 0) {
					readString(buffer, builder);
					name = HTTP.HEADERS.get(builder);
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
				settings.setEnablePush(value > 0);
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
		int id;
		length = buffer.readable() - length;
		while (buffer.readable() > length) {
			// Identifier (16) |Value (32)
			id = buffer.readShort();
			if (id == HEADER_TABLE_SIZE) {
				settings.setHeaderTableSize(buffer.readInt());
			} else if (id == ENABLE_PUSH) {
				settings.setEnablePush(buffer.readInt() > 0);
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

	static void writeHeaders(HPACK hpack, DataBuffer buffer, Request request) throws IOException {
		int position = buffer.readable();
		// Length (24)
		buffer.writeMedium(0);
		// Type (8)
		buffer.writeByte(HEADERS);
		// Flags (8)
		// END_STREAM(0x1)/END_HEADERS(0x4)/PADDED(0x8)/PRIORITY(0x20)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(request.getStream());

		// 伪头
		writeHeader(hpack, buffer, METHOD, request.getMethod());
		writeHeader(hpack, buffer, SCHEME, request.getScheme());
		writeHeader(hpack, buffer, PATH, request.getPath());
		writeHeader(hpack, buffer, AUTHORITY, request.getAuthority());
		if (buffer.readable() > hpack.getMaxHeaderListSize()) {
			// 请求行不能超过帧限制
			throw new HTTP2Exception(FRAME_SIZE_ERROR);
		}
		request.state(Request.HEADERS);

		// 标准头
		for (Entry<String, String> header : request.getHeaders().entrySet()) {
			if (header.getKey() != null && header.getValue() != null) {
				writeHeader(hpack, buffer, header.getKey(), header.getValue());
			}
		}
		request.state(Request.CONTENT);

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
		} else {
			flag = FLAG_END_HEADERS | FLAG_END_STREAM;
			request.state(Request.COMPLETE);
		}
		buffer.set(position, flag);
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
		buffer.writeInt(response.getStream());

		// 伪头
		writeHeader(hpack, buffer, STATUS, Integer.toString(response.getStatus()));
		response.state(Request.HEADERS);
		// 标准头
		for (Entry<String, String> header : response.getHeaders().entrySet()) {
			if (header.getKey() != null && header.getValue() != null) {
				writeHeader(hpack, buffer, header.getKey(), header.getValue());
			}
		}
		response.state(Request.CONTENT);

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
		} else {
			flag = FLAG_END_HEADERS | FLAG_END_STREAM;
			response.state(Request.COMPLETE);
		}
		buffer.set(position, flag);
		return true;
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
	static void writeData(HTTPMessage message, DataBuffer buffer, Settings setting) throws IOException {
		final DataBuffer content = (DataBuffer) message.getContent();
		// 10 = Frame header 9 + Pad Length 1
		int length = setting.getMaxFrameSize() - 10;
		if (content.readable() > length) {
			// Length (24)
			buffer.writeMedium(length);
			// Type (8)
			buffer.writeByte(DATA);
			// Flags (8)
			buffer.writeByte(0);
			// R|Stream Identifier (32)
			buffer.writeInt(0);

			// Pad Length? (8)
			buffer.writeByte(0);
			// Data (*)
			buffer.append(content, length);
		} else {
			int pad = length - content.readable();
			if (pad > 8) {
				pad = 8 - pad % 8;
			} else {
				pad = 8 - pad;
			}

			// Length (24)
			buffer.writeMedium(length);
			// Type (8)
			buffer.writeByte(DATA);
			// Flags (8)
			if (pad > 0) {
				buffer.writeByte(FLAG_END_STREAM | FLAG_PADDED);
			} else {
				buffer.writeByte(FLAG_END_STREAM);
			}
			// R|Stream Identifier (32)
			buffer.writeInt(0);

			// Pad Length? (8)
			buffer.writeByte(pad);
			// Data (*)
			buffer.append(content);
			// Padding (*)
			while (pad-- > 0) {
				buffer.writeByte(0);
			}
		}
	}

	static void encodePriority(DataBuffer buffer, boolean e, int stream, int weight) throws IOException {
		// Length (24)
		buffer.writeMedium(5);
		// Type (8)
		buffer.writeByte(PRIORITY);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(stream);

		// E|Stream Dependency (32)
		if (e) {
			Binary.setBit(stream, e, 31);
		}
		buffer.writeByte(stream);
		// Weight (8)
		buffer.writeByte(weight);
	}

	static void encodeRST_STREAM(DataBuffer buffer, int stream, int code) throws IOException {
		// Length (24)
		buffer.writeMedium(4);
		// Type (8)
		buffer.writeByte(RST_STREAM);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(stream);
		// Error Code (32)
		buffer.writeInt(code);
	}

	/** Frame HEAD | SETTINGS */
	static void write(DataBuffer buffer, Settings settings) throws IOException {
		// Length (24)
		buffer.writeMedium(36);
		// Type (8)
		buffer.writeByte(SETTINGS);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(0);

		// Identifier (16) | Value (32)
		buffer.writeShort(HEADER_TABLE_SIZE);
		buffer.writeInt(settings.getHeaderTableSize());
		buffer.writeShort(ENABLE_PUSH);
		buffer.writeInt(settings.isEnablePush() ? 1 : 0);
		buffer.writeShort(MAX_CONCURRENT_STREAMS);
		buffer.writeInt(settings.getMaxConcurrentStreams());
		buffer.writeShort(INITIAL_WINDOW_SIZE);
		buffer.writeInt(settings.getInitialWindowSize());
		buffer.writeShort(MAX_FRAME_SIZE);
		buffer.writeInt(settings.getMaxFrameSize());
		buffer.writeShort(MAX_HEADER_LIST_SIZE);
		buffer.writeInt(settings.getMaxHeaderListSize());

		settings.state(Message.COMPLETE);
	}

	/** Frame HEAD | PING */
	static void write(DataBuffer buffer, Ping ping) throws IOException {
		// Length (24)
		buffer.writeMedium(8);
		// Type (8)
		buffer.writeByte(PING);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(0);

		// Opaque Data (64)
		buffer.writeLong(ping.getValue());

		ping.state(Message.COMPLETE);
	}

	/** Frame HEAD | GOAWAY */
	static void write(DataBuffer buffer, Goaway goaway) throws IOException {
		// Length (24)
		buffer.writeMedium(8);
		// Type (8)
		buffer.writeByte(PING);
		// Flags (8)
		buffer.writeByte(0);
		// R|Stream Identifier (32)
		buffer.writeInt(0);

		// R|Last-Stream-ID (31)
		buffer.writeInt(goaway.getLastStreamID());
		// Error Code (32)
		buffer.writeInt(goaway.getError());
		// Additional Debug Data (*)

		goaway.state(Message.COMPLETE);
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
}