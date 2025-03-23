package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 记录层编码解码
 * 
 * @author ZhangXi 2025年2月10日
 */
class RecordCoder extends TLS {

	/**
	 * 解码记录层数据
	 */
	static int v13Decode(CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		int type = buffer.readByte();
		// ProtocolVersion 2Byte
		buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		if (type == Record.APPLICATION_DATA) {
			// CIPHERTEXT
			if (length > Record.CIPHERTEXT_MAX) {
				buffer.clear();
				throw new TLSException(Alert.RECORD_OVERFLOW);
			}
			// 解密
			try {
				if (buffer.readable() == length) {
					cipher.v13DecryptAEAD(length);
					cipher.decryptFinal(buffer, data);
				} else if (buffer.readable() > length) {
					cipher.v13DecryptAEAD(length);
					cipher.decryptFinal(buffer, data, length);
				} else {
					buffer.reset();
					return -1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new TLSException(Alert.BAD_RECORD_MAC);
			}
			// 删除 zeros 查找 ContentType
			type = data.backByte();
			while (type == 0) {
				type = data.backByte();
			}
			return type;
		} else {
			// PLAINTEXT
			if (length > Record.PLAINTEXT_MAX) {
				buffer.clear();
				throw new TLSException(Alert.RECORD_OVERFLOW);
			}
			if (buffer.readable() >= length) {
				buffer.transfer(data, length);
				return type;
			} else {
				buffer.reset();
				return -1;
			}
		}
	}

	/**
	 * 编码明文记录，如有必要执行分块
	 */
	static void encodePlaintext(Record record, DataBuffer data, DataBuffer buffer) throws IOException {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX);
			// opaque
			data.transfer(buffer, Record.PLAINTEXT_MAX);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable());
		// opaque
		data.transfer(buffer);
	}

	static int decodePlaintext(DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		int type = buffer.readByte();
		// ProtocolVersion 2Byte
		buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		// PLAINTEXT
		if (length > Record.PLAINTEXT_MAX) {
			buffer.clear();
			throw new TLSException(Alert.RECORD_OVERFLOW);
		}
		if (buffer.readable() >= length) {
			buffer.transfer(data, length);
			return type;
		} else {
			buffer.reset();
			return -1;
		}
	}

	/**
	 * 编码APPLICATION_DATA的记录消息，始终加密消息，如有必要执行分块和填充
	 */
	static void v13EncodeCiphertext(CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(Record.APPLICATION_DATA);
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + 1/* ContentType */ + cipher.tagLength());

			// encrypted_record: DATA + ContentType
			// 负载已满时无填充数据(padding)
			cipher.v13EncryptAEAD(Record.PLAINTEXT_MAX + 1/* ContentType */ + cipher.tagLength());
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			cipher.encryptUpdate(new byte[] { record.contentType() }, buffer);
			cipher.encryptFinal(buffer);
		}

		// encrypted_record: DATA + ContentType + PADDING(n)
		// ContentType 1Byte
		data.writeByte(record.contentType());
		v13Padding(data, 0);

		// ContentType 1Byte
		buffer.writeByte(Record.APPLICATION_DATA);
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.tagLength());
		// encrypted_record
		cipher.v13EncryptAEAD(data.readable() + cipher.tagLength());
		cipher.encryptFinal(data, buffer);
	}

	/**
	 * 零长度应用数据，此方法始终执行完整的记录编码
	 */
	static void encode(ApplicationData message, DataBuffer buffer) throws Exception {
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(0);
	}

	/**
	 * 此消息始终明文编码，此方法始终执行完整的记录编码
	 */
	static void encode(ChangeCipherSpec message, DataBuffer buffer) throws Exception {
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(1);
		// 0x01 1Byte
		buffer.writeByte(ChangeCipherSpec.ONE);
	}

	static Record decodeChangeCipherSpec(DataBuffer buffer) {
		if (buffer.readByte() == ChangeCipherSpec.ONE) {
			return ChangeCipherSpec.INSTANCE;
		} else {
			return new Alert(Alert.UNEXPECTED_MESSAGE);
		}
	}

	static void encode(HeartbeatMessage message, DataBuffer buffer) throws IOException {
		// HeartbeatMessageType 1Byte
		buffer.writeByte(message.getMessageType());
		// payload_length 2Byte(uint16)
		// opaque payload nByte
		buffer.writeShort(message.getPayload().length);
		buffer.write(message.getPayload());
		// opaque padding >= 16Byte
		v13Padding(buffer, 16);
	}

	static HeartbeatMessage decodeHeartbeat(DataBuffer buffer) throws IOException {
		final HeartbeatMessage message = new HeartbeatMessage();
		// HeartbeatMessageType 1Byte
		message.setMessageType(buffer.readByte());
		// payload_length 2Byte(uint16)
		message.setPayload(new byte[buffer.readUnsignedShort()]);
		// opaque payload nByte
		buffer.readFully(message.getPayload());
		// ignored opaque padding
		buffer.skipBytes(buffer.readable());
		return message;
	}

	static void encode(Alert message, DataBuffer buffer) {
		// AlertLevel 1Byte
		buffer.writeByte(message.getLevel());
		// AlertDescription 1Byte
		buffer.writeByte(message.getDescription());
	}

	static Alert decodeAlert(DataBuffer buffer) throws IOException {
		final Alert alert = new Alert();
		// AlertLevel 1Byte
		alert.setLevel(buffer.readByte());
		// AlertDescription 1Byte
		alert.setDescription(buffer.readByte());
		return alert;
	}

	/** PADDING(zero*) */
	static void v13Padding(DataBuffer data, int min) {
		int size = Record.PLAINTEXT_MAX - data.readable();
		if (size > min) {
			if (size > 32) {
				size = 32 - (size % 32);
			} else if (size > 16) {
				size = 16 - (size % 16);
			} else if (size > 8) {
				size = 8 - (size % 8);
			} else if (size > 4) {
				size = 4 - (size % 4);
			} else {
				size = 4 - size;
			}
			while (size-- > 0) {
				data.writeByte(0);
			}
		}
	}

	/*-
	 * struct {
	 *       ContentType type;
	 *       ProtocolVersion version;
	 *       uint16 length;
	 *       select (SecurityParameters.cipher_type) {
	 *             case stream: GenericStreamCipher;
	 *             case block:  GenericBlockCipher;
	 *             case aead:   GenericAEADCipher;
	 *       } fragment;
	 * } TLSCiphertext;
	 * 
	 * stream-ciphered struct {
	 *       opaque content[TLSCompressed.length];
	 *       opaque MAC[SecurityParameters.mac_length];
	 * } GenericStreamCipher;
	 * 
	 * struct {
	 *       opaque IV[SecurityParameters.record_iv_length];
	 *       block-ciphered struct {
	 *             opaque content[TLSCompressed.length];
	 *             opaque MAC[SecurityParameters.mac_length];
	 *             uint8 padding[GenericBlockCipher.padding_length];
	 *             uint8 padding_length;
	 *       };
	 * } GenericBlockCipher;
	 * 
	 * struct {
	 *       opaque nonce_explicit[SecurityParameters.record_iv_length];
	 *       aead-ciphered struct {
	 *             opaque content[TLSCompressed.length];
	 *       };
	 * } GenericAEADCipher;
	 * 
	 * MAC(MAC_write_key, seq_num +
	 *                    TLSCompressed.type +
	 *                    TLSCompressed.version +
	 *                    TLSCompressed.length +
	 *                    TLSCompressed.fragment);
	 */

	static int v12Decode(CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		if (cipher.decryptReady()) {
			if (cipher.isAEAD()) {
				return v12DecodeAEAD(cipher, buffer, data);
			} else if (cipher.isBlock()) {
				return v12DecodeBlock(cipher, buffer, data);
			} else if (cipher.isStream()) {
				return v12DecodeStream(cipher, buffer, data);
			} else {
				throw new TLSException(Alert.BAD_RECORD_MAC);
			}
		} else {
			return decodePlaintext(buffer, data);
		}
	}

	static void v12EncodeCiphertext(CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		if (cipher.isAEAD()) {
			v12EncodeAEAD(cipher, record, data, buffer);
		} else if (cipher.isBlock()) {
			v12EncodeBlock(cipher, record, data, buffer);
		} else if (cipher.isStream()) {
			v12EncodeStream(cipher, record, data, buffer);
		} else {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}
	}

	/** 1.2 TLSCiphertext(GenericStreamCipher) */
	static void v12EncodeStream(CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + cipher.hmacLength());

			final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, Record.PLAINTEXT_MAX);
			cipher.encryptStream();
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// opaque MAC[SecurityParameters.mac_length];
			cipher.encryptUpdate(mac, buffer);
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.hmacLength());

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, data.readable());

		cipher.encryptStream();
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		cipher.encryptFinal(buffer);
	}

	/** 1.2 TLSCiphertext(GenericStreamCipher) */
	static int v12DecodeStream(CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		byte type = buffer.readByte();
		// ProtocolVersion 2Byte
		short version = buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		// CIPHERTEXT
		if (length > Record.CIPHERTEXT_MAX) {
			buffer.clear();
			throw new TLSException(Alert.RECORD_OVERFLOW);
		}

		// 解密
		try {
			if (buffer.readable() == length) {
				cipher.decryptStream();
				cipher.decryptFinal(buffer, data);
			} else if (buffer.readable() > length) {
				cipher.decryptStream();
				cipher.decryptFinal(buffer, data, length);
			} else {
				buffer.reset();
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		// MAC
		length -= cipher.hmacLength();
		final byte[] mac = cipher.decryptMAC(type, version, data, length);
		// 验证MAC
		version = 0;
		length = mac.length;
		while (--length >= 0) {
			if (mac[length] == data.backByte()) {
				version++;
			}
		}
		if (version < cipher.hmacLength()) {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		return type;
	}

	/** 1.2 TLSCiphertext(GenericBlockCipher) */
	static void v12EncodeBlock(CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());

			// 计算长度和填充数量
			int length = Record.PLAINTEXT_MAX + cipher.hmacLength();
			int padding = cipher.blockLength() - length % cipher.blockLength();
			length += padding + cipher.ivLength();

			// length 2Byte(uint16)
			buffer.writeShort(length);

			// opaque IV[SecurityParameters.record_iv_length];
			final byte[] temp = new byte[cipher.ivLength()];
			TLS.RANDOM.nextBytes(temp);
			buffer.write(temp);

			// padding value
			for (int i = 0; i < padding; i++) {
				temp[i] = (byte) (padding - 1);
			}

			// MAC value
			final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, Record.PLAINTEXT_MAX);

			cipher.encryptBlock();
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// opaque MAC[SecurityParameters.mac_length];
			cipher.encryptUpdate(mac, buffer);
			// uint8 padding[GenericBlockCipher.padding_length];
			cipher.encryptUpdate(temp, buffer, padding);
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());

		// 计算长度和填充数量
		int length = data.readable() + cipher.hmacLength();
		int padding = cipher.blockLength() - length % cipher.blockLength();
		length += padding + cipher.ivLength();

		// length 2Byte(uint16)
		buffer.writeShort(length);

		// opaque IV[SecurityParameters.record_iv_length];
		final byte[] temp = new byte[cipher.ivLength()];
		TLS.RANDOM.nextBytes(temp);
		buffer.write(temp);

		// padding value
		for (int i = 0; i < padding; i++) {
			temp[i] = (byte) (padding - 1);
		}

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, data.readable());

		cipher.encryptBlock();
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		// uint8 padding[GenericBlockCipher.padding_length];
		cipher.encryptUpdate(temp, buffer, padding);
		cipher.encryptFinal(buffer);
	}

	/** 1.2 TLSCiphertext(GenericBlockCipher) */
	static int v12DecodeBlock(CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		byte type = buffer.readByte();
		// ProtocolVersion 2Byte
		short version = buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		// CIPHERTEXT
		if (length > Record.CIPHERTEXT_MAX) {
			buffer.clear();
			throw new TLSException(Alert.RECORD_OVERFLOW);
		}

		// IV
		buffer.skipBytes(cipher.ivLength());
		length -= cipher.ivLength();

		// 解密
		try {
			if (buffer.readable() == length) {
				cipher.decryptStream();
				cipher.decryptFinal(buffer, data);
			} else if (buffer.readable() > length) {
				cipher.decryptStream();
				cipher.decryptFinal(buffer, data, length);
			} else {
				buffer.reset();
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		// PADDING
		buffer.backSkip(buffer.backByte());

		// MAC
		length -= cipher.hmacLength();
		final byte[] mac = cipher.decryptMAC(type, version, data, length);
		// 验证MAC
		version = 0;
		length = mac.length;
		while (--length >= 0) {
			if (mac[length] == data.backByte()) {
				version++;
			}
		}
		if (version < cipher.hmacLength()) {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		return type;
	}

	/** 1.2 TLSCiphertext(GenericAEADCipher) */
	static void v12EncodeAEAD(CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + cipher.ivLength() + cipher.tagLength());

			// opaque nonce_explicit[SecurityParameters.record_iv_length];
			final byte[] temp = new byte[cipher.ivLength()];
			TLS.RANDOM.nextBytes(temp);
			buffer.write(temp);

			cipher.v12EncryptAEAD(record.contentType(), record.getProtocolVersion(), Record.PLAINTEXT_MAX + cipher.tagLength());
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.ivLength() + cipher.tagLength());

		// opaque nonce_explicit[SecurityParameters.record_iv_length];
		final byte[] temp = new byte[cipher.ivLength()];
		TLS.RANDOM.nextBytes(temp);
		buffer.write(temp);

		cipher.v12EncryptAEAD(record.contentType(), record.getProtocolVersion(), data.readable() + cipher.tagLength());
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		cipher.encryptFinal(buffer);
	}

	/** 1.2 TLSCiphertext(GenericAEADCipher) */
	static int v12DecodeAEAD(CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		byte type = buffer.readByte();
		// ProtocolVersion 2Byte
		short version = buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		// CIPHERTEXT
		if (length > Record.CIPHERTEXT_MAX) {
			buffer.clear();
			throw new TLSException(Alert.RECORD_OVERFLOW);
		}

		// nonce_explicit
		buffer.skipBytes(cipher.ivLength());
		length -= cipher.ivLength();

		// 解密
		try {
			if (buffer.readable() == length) {
				cipher.v12DecryptAEAD(type, version, length);
				cipher.decryptFinal(buffer, data);
			} else if (buffer.readable() > length) {
				cipher.v12DecryptAEAD(type, version, length);
				cipher.decryptFinal(buffer, data, length);
			} else {
				buffer.reset();
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		return type;
	}

	/** PADDING(block) */
	static void v12Padding(DataBuffer data, int block) {
		int length = data.readable();
		length = block - length % block;
		length -= 1;
		for (int i = 0; i < length; i++) {
			data.writeByte(length);
		}
		data.writeByte(length);
	}
}