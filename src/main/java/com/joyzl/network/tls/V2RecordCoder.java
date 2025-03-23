package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 记录层编码解码
 * 
 * @author ZhangXi 2025年2月10日
 */
class V2RecordCoder extends TLS {

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

	static int decode(V2CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		if (cipher.decryptReady()) {
			if (cipher.isAEAD()) {
				return decodeAEAD(cipher, buffer, data);
			} else if (cipher.isBlock()) {
				return decodeBlock(cipher, buffer, data);
			} else if (cipher.isStream()) {
				return decodeStream(cipher, buffer, data);
			} else {
				throw new TLSException(Alert.BAD_RECORD_MAC);
			}
		} else {
			return decodePlaintext(buffer, data);
		}
	}

	static void encodeCiphertext(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		if (cipher.isAEAD()) {
			encodeAEAD(cipher, record, data, buffer);
		} else if (cipher.isBlock()) {
			encodeBlock(cipher, record, data, buffer);
		} else if (cipher.isStream()) {
			encodeStream(cipher, record, data, buffer);
		} else {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}
	}

	/** 1.2 TLSCiphertext(GenericStreamCipher) */
	static void encodeStream(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + cipher.macLength());

			// MAC value
			final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, Record.PLAINTEXT_MAX);

			cipher.encryptStream();
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// opaque MAC[SecurityParameters.mac_length];
			cipher.encryptUpdate(mac, buffer);
			// encrypt end output
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.macLength());

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, data.readable());

		cipher.encryptStream();
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		// encrypt end output
		cipher.encryptFinal(buffer);
	}

	/** 1.2 TLSCiphertext(GenericStreamCipher) */
	static int decodeStream(V2CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		final byte type = buffer.readByte();
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
		length -= cipher.macLength();
		final byte[] mac = cipher.decryptMAC(type, version, data, length);

		// 验证MAC
		version = 0;
		length = mac.length;
		while (--length >= 0) {
			if (mac[length] == data.backByte()) {
				version++;
			}
		}
		if (version < cipher.macLength()) {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		return type;
	}

	/** 1.2 TLSCiphertext(GenericBlockCipher) */
	static void encodeBlock(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		int length, padding;
		final byte[] iv = new byte[cipher.ivLength()];

		// 由于IVLength与BlockLength相同，并且填充不超过BlockLength
		// 因此填充不会超过IV数组空间，以下无须创建新数组，使用IV用于填充

		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());

			// 计算长度和填充数量
			// 尾部 padding_length 占用1字节
			length = Record.PLAINTEXT_MAX + cipher.macLength() + 1;
			padding = cipher.blockLength() - length % cipher.blockLength();
			length += padding + cipher.ivLength();

			// length 2Byte(uint16)
			buffer.writeShort(length);

			// opaque IV[SecurityParameters.record_iv_length];
			TLS.RANDOM.nextBytes(iv);
			buffer.write(iv);

			// MAC value
			final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, Record.PLAINTEXT_MAX);

			cipher.encryptBlock(iv);
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// opaque MAC[SecurityParameters.mac_length];
			cipher.encryptUpdate(mac, buffer);
			// uint8 padding[GenericBlockCipher.padding_length];
			for (length = 0; length < padding; length++) {
				iv[length] = (byte) (padding);
			}
			cipher.encryptUpdate(iv, buffer, padding);
			// uint8 padding_length;
			cipher.encryptUpdate(iv, buffer, 1);
			// encrypt end output
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());

		// 计算长度和填充数量
		// 尾部 padding_length 占用1字节
		length = data.readable() + cipher.macLength() + 1;
		padding = cipher.blockLength() - length % cipher.blockLength();
		length += padding + cipher.ivLength();

		// length 2Byte(uint16)
		buffer.writeShort(length);

		// opaque IV[SecurityParameters.record_iv_length];
		TLS.RANDOM.nextBytes(iv);
		buffer.write(iv);

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), record.getProtocolVersion(), data, data.readable());

		cipher.encryptBlock(iv);
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		// uint8 padding[GenericBlockCipher.padding_length];
		for (length = 0; length < padding; length++) {
			iv[length] = (byte) (padding);
		}
		cipher.encryptUpdate(iv, buffer, padding);
		// uint8 padding_length;
		cipher.encryptUpdate(iv, buffer, 1);
		// encrypt end output
		cipher.encryptFinal(buffer);
	}

	/** 1.2 TLSCiphertext(GenericBlockCipher) */
	static int decodeBlock(V2CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		final byte type = buffer.readByte();
		// ProtocolVersion 2Byte
		short version = buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		// CIPHERTEXT
		if (length > Record.CIPHERTEXT_MAX) {
			buffer.clear();
			throw new TLSException(Alert.RECORD_OVERFLOW);
		}
		// 数据是否收完
		if (length > buffer.readable()) {
			buffer.reset();
			return -1;
		}

		// IV
		final byte[] iv = new byte[cipher.ivLength()];
		length -= cipher.ivLength();
		buffer.readFully(iv);

		// 解密
		try {
			if (buffer.readable() == length) {
				cipher.decryptBlock(iv);
				cipher.decryptFinal(buffer, data);
			} else if (buffer.readable() > length) {
				cipher.decryptBlock(iv);
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
		int padding = data.backByte() & 0xFF;
		data.backSkip(padding);
		length -= padding + 1;

		// MAC
		length -= cipher.macLength();
		final byte[] mac = cipher.decryptMAC(type, version, data, length);

		// 验证MAC
		version = 0;
		length = mac.length;
		while (--length >= 0) {
			if (mac[length] == data.backByte()) {
				version++;
			}
		}
		if (version < cipher.macLength()) {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}

		return type;
	}

	/** 1.2 TLSCiphertext(GenericAEADCipher) */
	static void encodeAEAD(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
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

			cipher.encryptAEAD(record.contentType(), record.getProtocolVersion(), Record.PLAINTEXT_MAX + cipher.tagLength());
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// encrypt end output
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

		cipher.encryptAEAD(record.contentType(), record.getProtocolVersion(), data.readable() + cipher.tagLength());
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// encrypt end output
		cipher.encryptFinal(buffer);
	}

	/** 1.2 TLSCiphertext(GenericAEADCipher) */
	static int decodeAEAD(V2CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		final byte type = buffer.readByte();
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
				cipher.decryptAEAD(type, version, length);
				cipher.decryptFinal(buffer, data);
			} else if (buffer.readable() > length) {
				cipher.decryptAEAD(type, version, length);
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
	static void padding(DataBuffer data, int block) {
		int length = data.readable();
		length = block - length % block;
		length -= 1;
		for (int i = 0; i < length; i++) {
			data.writeByte(length);
		}
		data.writeByte(length);
	}
}