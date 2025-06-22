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
class V0TranscriptHash {

	private MessageDigest md5;
	private MessageDigest sha;

	/** 最后获取的哈希值 */
	private byte[] current1 = TLS.EMPTY_BYTES;
	private byte[] current2 = TLS.EMPTY_BYTES;

	public void initialize() throws NoSuchAlgorithmException {
		if (md5 == null || sha == null) {
			md5 = MessageDigest.getInstance("MD5");
			sha = MessageDigest.getInstance("SHA-1");
		}
	}

	/** 重置 Transcript-Hash */
	public void hashReset() {
		current1 = TLS.EMPTY_BYTES;
		current2 = TLS.EMPTY_BYTES;
		md5.reset();
		sha.reset();
	}

	/** Transcript-Hash(message) */
	public void hash(byte[] message) {
		current1 = TLS.EMPTY_BYTES;
		current2 = TLS.EMPTY_BYTES;
		md5.update(message);
		sha.update(message);
	}

	/** Transcript-Hash(message) */
	public void hash(DataBuffer message) {
		DataBufferUnit unit = message.head();
		do {
			unit.buffer().mark();
			md5.update(unit.buffer());
			unit.buffer().reset();

			unit.buffer().mark();
			sha.update(unit.buffer());
			unit.buffer().reset();

			unit = unit.next();
		} while (unit != null);

		// 每次计算新的消息时重置当前哈希结果
		current1 = TLS.EMPTY_BYTES;
		current2 = TLS.EMPTY_BYTES;
	}

	/** Transcript-Hash(message) */
	public void hash(DataBuffer message, int length) {
		int position;
		DataBufferUnit unit = message.head();
		while (unit != null && length > 0) {
			unit.buffer().mark();
			if (unit.readable() <= length) {
				length -= unit.readable();

				position = unit.readIndex();
				md5.update(unit.buffer());
				unit.readIndex(position);
				sha.update(unit.buffer());
			} else {
				length = unit.readable() - length;
				unit.writeIndex(unit.writeIndex() - length);

				position = unit.readIndex();
				md5.update(unit.buffer());
				unit.readIndex(position);
				sha.update(unit.buffer());

				unit.writeIndex(unit.writeIndex() + length);
				length = 0;
			}
			unit.buffer().reset();
			unit = unit.next();
		}

		// 每次计算新的消息时重置当前哈希结果
		current1 = TLS.EMPTY_BYTES;
		current2 = TLS.EMPTY_BYTES;
	}

	public byte[] hashMD5() throws Exception {
		if (current1 == TLS.EMPTY_BYTES) {
			// 可能会有不支持克隆的加密算法
			final MessageDigest md = (MessageDigest) md5.clone();
			current1 = md5.digest();
			md5 = md;
		}
		return current1;
	}

	public byte[] hashSHA() throws Exception {
		if (current2 == TLS.EMPTY_BYTES) {
			// 可能会有不支持克隆的加密算法
			final MessageDigest md = (MessageDigest) sha.clone();
			current2 = sha.digest();
			sha = md;
		}
		return current2;
	}
}