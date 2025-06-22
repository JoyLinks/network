/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;

/**
 * TLS 1.3 Transcript-Hash
 * 
 * @author ZhangXi 2024年12月30日
 */
class V2TranscriptHash {

	// Transcript-Hash("")
	// SHA256=e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855

	// 实例化对象时将立即生成一份空字节的哈希结果
	// 每投递一次用于哈希算法的消息将自动清空当前哈希结果
	// 每获取一次哈希结果将克隆已计算的哈希副本，以便继续后续消息计算
	// 注意：不包括任何 Record Layer 字节

	private MessageDigest digest;

	/** Transcript-Hash("") */
	private byte[] EMPTY_HASH;
	/** 最后获取的哈希值 */
	private byte[] current = TLS.EMPTY_BYTES;

	public void initialize(String name) throws NoSuchAlgorithmException {
		if (digest == null || !name.equals(digest.getAlgorithm())) {
			digest = MessageDigest.getInstance(name);
			EMPTY_HASH = digest.digest();
		} else {
			digest.reset();
		}
		current = TLS.EMPTY_BYTES;
	}

	/** 重置 Transcript-Hash */
	public void hashReset() {
		current = TLS.EMPTY_BYTES;
		digest.reset();
	}

	/** Transcript-Hash(message) */
	public void hash(byte[] message) {
		digest.update(message);
		current = TLS.EMPTY_BYTES;
	}

	/** Transcript-Hash(message) */
	public void hash(DataBuffer message) {
		DataBufferUnit unit = message.head();
		do {
			unit.buffer().mark();
			digest.update(unit.buffer());
			unit.buffer().reset();
			unit = unit.next();
		} while (unit != null);

		// 每次计算新的消息时重置当前哈希结果
		current = TLS.EMPTY_BYTES;
	}

	/** Transcript-Hash(message) */
	public void hash(DataBuffer message, int length) {
		DataBufferUnit unit = message.head();
		while (unit != null && length > 0) {
			unit.buffer().mark();
			if (unit.readable() <= length) {
				length -= unit.readable();
				digest.update(unit.buffer());
			} else {
				length = unit.readable() - length;
				unit.writeIndex(unit.writeIndex() - length);
				digest.update(unit.buffer());
				unit.writeIndex(unit.writeIndex() + length);
				length = 0;
			}
			unit.buffer().reset();
			unit = unit.next();
		}

		// 每次计算新的消息时重置当前哈希结果
		current = TLS.EMPTY_BYTES;
	}

	/** Transcript-Hash(...) */
	public byte[] hash() throws Exception {
		if (current == TLS.EMPTY_BYTES) {
			// 可能会有不支持克隆的加密算法
			final MessageDigest md = (MessageDigest) digest.clone();
			current = digest.digest();
			digest = md;
		}
		return current;
	}

	/** TLS 1.3 HelloRetryRequest */
	public void retry() throws Exception {
		/*-
		 * Transcript-Hash(ClientHello1, HelloRetryRequest, ... Mn) =
		 *       Hash(message_hash       || // Handshake type
		 *            00 00 Hash.length  || // Handshake message length (bytes)
		 *            Hash(ClientHello1) || // Hash of ClientHello1
		 *            HelloRetryRequest  || ... || Mn)
		 * 
		 * SHA256 Hash.length = 32
		 * 00 00 Hash.length = 00 00 20
		 */

		final byte[] hashClientHello1 = digest.digest();
		final byte[] messageHash0000Hashlength = new byte[4];
		messageHash0000Hashlength[0] = Handshake.MESSAGE_HASH;
		messageHash0000Hashlength[1] = 0;
		messageHash0000Hashlength[2] = 0;
		messageHash0000Hashlength[3] = (byte) EMPTY_HASH.length;

		digest.reset();
		digest.update(messageHash0000Hashlength);
		digest.update(hashClientHello1);
		current = TLS.EMPTY_BYTES;
	}

	/** HASH.length */
	public int hashLength() {
		return digest.getDigestLength();
	}

	/** Transcript-Hash("") */
	public byte[] hashEmpty() {
		return EMPTY_HASH;
	}
}