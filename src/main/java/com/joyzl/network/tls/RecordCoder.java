package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 记录层编码解码
 * 
 * @author ZhangXi 2025年2月10日
 */
class RecordCoder extends TLS {

	static void encodePlaintextV0(Record record, DataBuffer data, DataBuffer buffer) throws IOException {
		encodePlaintext(record, data, buffer, TLS.V10);
	}

	static void encodePlaintextV1(Record record, DataBuffer data, DataBuffer buffer) throws IOException {
		encodePlaintext(record, data, buffer, TLS.V11);
	}

	static void encodePlaintextV2(Record record, DataBuffer data, DataBuffer buffer) throws IOException {
		encodePlaintext(record, data, buffer, TLS.V12);
	}

	static void encodeV0(ApplicationData message, DataBuffer buffer) throws Exception {
		encode(message, buffer, TLS.V10);
	}

	static void encodeV1(ApplicationData message, DataBuffer buffer) throws Exception {
		encode(message, buffer, TLS.V11);
	}

	static void encodeV2(ApplicationData message, DataBuffer buffer) throws Exception {
		encode(message, buffer, TLS.V12);
	}

	static void encodeV0(ChangeCipherSpec message, DataBuffer buffer) throws Exception {
		encode(message, buffer, TLS.V10);
	}

	static void encodeV1(ChangeCipherSpec message, DataBuffer buffer) throws Exception {
		encode(message, buffer, TLS.V11);
	}

	static void encodeV2(ChangeCipherSpec message, DataBuffer buffer) throws Exception {
		encode(message, buffer, TLS.V12);
	}

	/*-
	 * TLS 1.0
	 * struct {
	 *       uint8 major, minor;
	 * } ProtocolVersion;
	 * 
	 * enum {
	 *       change_cipher_spec(20), 
	 *       alert(21), 
	 *       handshake(22),
	 *       application_data(23), (255)
	 * } ContentType;
	 * 
	 * struct {
	 *       ContentType type;
	 *       ProtocolVersion version;
	 *       uint16 length;
	 *       opaque fragment[TLSPlaintext.length];
	 * } TLSPlaintext;
	 * 
	 * struct {
	 *       ContentType type;       // same as TLSPlaintext.type 
	 *       ProtocolVersion version;// same as TLSPlaintext.version 
	 *       uint16 length;
	 *       opaque fragment[TLSCompressed.length];
	 * } TLSCompressed;
	 */

	/**
	 * 编码明文记录，如有必要执行分块
	 */
	static void encodePlaintext(Record record, DataBuffer data, DataBuffer buffer, short version) throws IOException {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX);
			// opaque
			data.transfer(buffer, Record.PLAINTEXT_MAX);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(version);
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
	static void encode(ApplicationData message, DataBuffer buffer, short version) throws Exception {
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(version);
		// length 2Byte(uint16)
		buffer.writeShort(0);
	}

	/**
	 * 此消息始终明文编码，此方法始终执行完整的记录编码
	 */
	static void encode(ChangeCipherSpec message, DataBuffer buffer, short version) throws Exception {
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(version);
		// length 2Byte(uint16)
		buffer.writeShort(1);
		// 0x01 1Byte
		buffer.writeByte(ChangeCipherSpec.ONE);
	}

	static Record decodeChangeCipherSpec(DataBuffer buffer) {
		buffer.readByte();
		return ChangeCipherSpec.INSTANCE;
	}

	static void encode(HeartbeatMessage message, DataBuffer buffer) throws IOException {
		// HeartbeatMessageType 1Byte
		buffer.writeByte(message.getMessageType());
		// payload_length 2Byte(uint16)
		// opaque payload nByte
		buffer.writeShort(message.getPayload().length);
		buffer.write(message.getPayload());
		// opaque padding >= 16Byte
		paddingZeros(buffer, 16);
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

	/*-
	 * TLS 1.0
	 * struct {
	 *       ContentType type;
	 *       ProtocolVersion version;
	 *       uint16 length;
	 *       select (SecurityParameters.cipher_type) {
	 *             case stream: GenericStreamCipher;
	 *             case block:  GenericBlockCipher;
	 *       } fragment;
	 * } TLSCiphertext;
	 * 
	 * stream-ciphered struct {
	 *       opaque content[TLSCompressed.length];
	 *       opaque MAC[CipherSpec.hash_size];
	 * } GenericStreamCipher;
	 * 
	 * block-ciphered struct {
	 *       opaque content[TLSCompressed.length];
	 *       opaque MAC[CipherSpec.hash_size];
	 *       uint8 padding[GenericBlockCipher.padding_length];
	 *       uint8 padding_length;
	 * } GenericBlockCipher;
	 * 
	 * MAC(MAC_write_secret, seq_num +
	 *                    TLSCompressed.type +
	 *                    TLSCompressed.version +
	 *                    TLSCompressed.length +
	 *                    TLSCompressed.fragment);
	 */

	static int decode(V0CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		if (cipher.decryptReady()) {
			if (cipher.isBlock()) {
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

	static void encodeCiphertext(V0CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		if (cipher.isBlock()) {
			encodeBlock(cipher, record, data, buffer, TLS.V10);
		} else if (cipher.isStream()) {
			encodeStream(cipher, record, data, buffer, TLS.V10);
		} else {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}
	}

	/** TLSCiphertext(GenericStreamCipher) */
	static void encodeStream(V0CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer, short version) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + cipher.macLength());

			// MAC value
			final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, Record.PLAINTEXT_MAX);

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
		buffer.writeShort(version);
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.macLength());

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, data.readable());

		cipher.encryptStream();
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		// encrypt end output
		cipher.encryptFinal(buffer);
	}

	/** TLSCiphertext(GenericStreamCipher) */
	static int decodeStream(V0CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
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

	/** TLSCiphertext(GenericBlockCipher) */
	static void encodeBlock(V0CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer, short version) throws Exception {
		int length, padding;
		final byte[] temp = new byte[cipher.blockLength()];

		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);

			// 计算长度和填充数量
			// 尾部 padding_length 占用1字节
			length = Record.PLAINTEXT_MAX + cipher.macLength() + 1;
			padding = cipher.blockLength() - length % cipher.blockLength();
			length += padding;

			// length 2Byte(uint16)
			buffer.writeShort(length);

			// MAC value
			final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, Record.PLAINTEXT_MAX);

			cipher.encryptBlock();
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// opaque MAC[SecurityParameters.mac_length];
			cipher.encryptUpdate(mac, buffer);
			// uint8 padding[GenericBlockCipher.padding_length];
			for (length = 0; length < padding; length++) {
				temp[length] = (byte) (padding);
			}
			cipher.encryptUpdate(temp, buffer, padding);
			// uint8 padding_length;
			cipher.encryptUpdate(temp, buffer, 1);
			// encrypt end output
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(version);

		// 计算长度和填充数量
		// 尾部 padding_length 占用1字节
		length = data.readable() + cipher.macLength() + 1;
		padding = cipher.blockLength() - length % cipher.blockLength();
		length += padding;

		// length 2Byte(uint16)
		buffer.writeShort(length);

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, data.readable());

		cipher.encryptBlock();
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		// uint8 padding[GenericBlockCipher.padding_length];
		for (length = 0; length < padding; length++) {
			temp[length] = (byte) (padding);
		}
		cipher.encryptUpdate(temp, buffer, padding);
		// uint8 padding_length;
		cipher.encryptUpdate(temp, buffer, 1);
		// encrypt end output
		cipher.encryptFinal(buffer);

		// 更新IV
		cipher.encryptUpdateIV(buffer);
	}

	/** TLSCiphertext(GenericBlockCipher) */
	static int decodeBlock(V0CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
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

		// 解密
		try {
			if (buffer.readable() == length) {
				cipher.decryptBlock();
				cipher.decryptUpdateIV(buffer, length);
				cipher.decryptFinal(buffer, data);
			} else if (buffer.readable() > length) {
				cipher.decryptBlock();
				cipher.decryptUpdateIV(buffer, length);
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

	/** 1.0 PADDING(block) */
	static void paddingBlock(DataBuffer data, int block) {
		int length = data.readable();
		length = block - length % block;
		length -= 1;
		for (int i = 0; i < length; i++) {
			data.writeByte(length);
		}
		data.writeByte(length);
	}

	/*-
	 * TLS 1.1
	 * struct {
	 *       ContentType type;
	 *       ProtocolVersion version;
	 *       uint16 length;
	 *       select (SecurityParameters.cipher_type) {
	 *             case stream: GenericStreamCipher;
	 *             case block:  GenericBlockCipher;
	 *       } fragment;
	 * } TLSCiphertext;
	 * 
	 * stream-ciphered struct {
	 *       opaque content[TLSCompressed.length];
	 *       opaque MAC[CipherSpec.hash_size];
	 * } GenericStreamCipher;
	 * 
	 * block-ciphered struct {
	 *       opaque IV[CipherSpec.block_length];
	 *       opaque content[TLSCompressed.length];
	 *       opaque MAC[CipherSpec.hash_size];
	 *       uint8 padding[GenericBlockCipher.padding_length];
	 *       uint8 padding_length;
	 * } GenericBlockCipher;
	 * 
	 * MAC(MAC_write_secret, seq_num +
	 *                    TLSCompressed.type +
	 *                    TLSCompressed.version +
	 *                    TLSCompressed.length +
	 *                    TLSCompressed.fragment);
	 * 
	 * 相比1.0调整了GenericBlockCipher的IV参数和编码方式
	 */

	static int decode(V1CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		if (cipher.decryptReady()) {
			if (cipher.isBlock()) {
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

	static void encodeCiphertext(V1CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		if (cipher.isBlock()) {
			encodeBlock(cipher, record, data, buffer, TLS.V11);
		} else if (cipher.isStream()) {
			encodeStream(cipher, record, data, buffer, TLS.V11);
		} else {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}
	}

	/** TLSCiphertext(GenericBlockCipher) */
	static void encodeBlock(V1CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer, short version) throws Exception {
		int length, padding;
		final byte[] iv = new byte[cipher.ivLength()];

		// 由于IVLength与BlockLength相同，并且填充不超过BlockLength
		// 因此填充不会超过IV数组空间，以下无须创建新数组，使用IV用于填充

		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);

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
			final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, Record.PLAINTEXT_MAX);

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
		buffer.writeShort(version);

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
		final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, data.readable());

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

	/** TLSCiphertext(GenericBlockCipher) */
	static int decodeBlock(V1CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
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

	/*-
	 * 1.2
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
	 * 
	 * 相比1.1增加了AEAD加解密算法
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
			encodeAEAD(cipher, record, data, buffer, TLS.V12);
		} else if (cipher.isBlock()) {
			encodeBlock(cipher, record, data, buffer, TLS.V12);
		} else if (cipher.isStream()) {
			encodeStream(cipher, record, data, buffer, TLS.V12);
		} else {
			throw new TLSException(Alert.BAD_RECORD_MAC);
		}
	}

	/** TLSCiphertext(GenericStreamCipher) */
	static void encodeStream(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer, short version) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + cipher.macLength());

			// MAC value
			final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, Record.PLAINTEXT_MAX);

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
		buffer.writeShort(version);
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.macLength());

		// MAC value
		final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, data.readable());

		cipher.encryptStream();
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// opaque MAC[SecurityParameters.mac_length];
		cipher.encryptUpdate(mac, buffer);
		// encrypt end output
		cipher.encryptFinal(buffer);
	}

	/** TLSCiphertext(GenericStreamCipher) */
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

	/** TLSCiphertext(GenericBlockCipher) */
	static void encodeBlock(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer, short version) throws Exception {
		int length, padding;
		final byte[] iv = new byte[cipher.ivLength()];

		// 由于IVLength与BlockLength相同，并且填充不超过BlockLength
		// 因此填充不会超过IV数组空间，以下无须创建新数组，使用IV用于填充

		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);

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
			final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, Record.PLAINTEXT_MAX);

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
		buffer.writeShort(version);

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
		final byte[] mac = cipher.encryptMAC(record.contentType(), version, data, data.readable());

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

	/** TLSCiphertext(GenericBlockCipher) */
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

	/** TLSCiphertext(GenericAEADCipher) */
	static void encodeAEAD(V2CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer, short version) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(version);
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + cipher.ivLength() + cipher.tagLength());

			// opaque nonce_explicit[SecurityParameters.record_iv_length];
			final byte[] temp = new byte[cipher.ivLength()];
			TLS.RANDOM.nextBytes(temp);
			buffer.write(temp);

			cipher.encryptAEAD(record.contentType(), version, Record.PLAINTEXT_MAX + cipher.tagLength());
			// opaque content[TLSCompressed.length];
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			// encrypt end output
			cipher.encryptFinal(buffer);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(version);
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.ivLength() + cipher.tagLength());

		// opaque nonce_explicit[SecurityParameters.record_iv_length];
		final byte[] temp = new byte[cipher.ivLength()];
		TLS.RANDOM.nextBytes(temp);
		buffer.write(temp);

		cipher.encryptAEAD(record.contentType(), version, data.readable() + cipher.tagLength());
		// opaque content[TLSCompressed.length];
		cipher.encryptUpdate(data, buffer, data.readable());
		// encrypt end output
		cipher.encryptFinal(buffer);
	}

	/** TLSCiphertext(GenericAEADCipher) */
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

	/*-
	 * TLS 1.3
	 * enum {
	 *       invalid(0),
	 *       change_cipher_spec(20),
	 *       alert(21),
	 *       handshake(22),
	 *       application_data(23),
	 *       heartbeat(24),  // RFC 6520
	 *       (255)
	 * } ContentType;
	 * 
	 * struct {
	 *       ContentType type;
	 *       ProtocolVersion legacy_record_version;
	 *       uint16 length;
	 *       opaque fragment[TLSPlaintext.length];
	 * } TLSPlaintext;
	 * 
	 * struct {
	 *       opaque content[TLSPlaintext.length];
	 *       ContentType type;
	 *       uint8 zeros[length_of_padding];
	 * } TLSInnerPlaintext;
	 * 
	 * struct {
	 *       ContentType opaque_type = application_data; // 23 
	 *       ProtocolVersion legacy_record_version = 0x0303; // TLS 1.2 
	 *       uint16 length;
	 *       opaque encrypted_record[TLSCiphertext.length];
	 * } TLSCiphertext;
	 * 
	 * 相比1.2仅使用AEAD加解密算法，且附加信息处理方式不同
	 * 加密结构也有变化，实际记录类型被加密
	 */

	/**
	 * 解码记录层数据
	 */
	static int decode(V3CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
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
					cipher.decryptAEAD(length);
					cipher.decryptFinal(buffer, data);
				} else if (buffer.readable() > length) {
					cipher.decryptAEAD(length);
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
	 * 编码APPLICATION_DATA的记录消息，始终加密消息，如有必要执行分块和填充
	 */
	static void encodeCiphertext(V3CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(Record.APPLICATION_DATA);
			// ProtocolVersion 2Byte
			buffer.writeShort(TLS.V12);
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + 1/* ContentType */ + cipher.tagLength());

			// encrypted_record: DATA + ContentType
			// 负载已满时无填充数据(padding)
			cipher.encryptAEAD(Record.PLAINTEXT_MAX + 1/* ContentType */ + cipher.tagLength());
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			cipher.encryptUpdate(new byte[] { record.contentType() }, buffer);
			cipher.encryptFinal(buffer);
		}

		// encrypted_record: DATA + ContentType + PADDING(n)
		// ContentType 1Byte
		data.writeByte(record.contentType());
		paddingZeros(data, 0);

		// ContentType 1Byte
		buffer.writeByte(Record.APPLICATION_DATA);
		// ProtocolVersion 2Byte
		buffer.writeShort(TLS.V12);
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.tagLength());
		// encrypted_record
		cipher.encryptAEAD(data.readable() + cipher.tagLength());
		cipher.encryptFinal(data, buffer);
	}

	/** 1.3 PADDING(zeros*) */
	static void paddingZeros(DataBuffer data, int min) {
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
}