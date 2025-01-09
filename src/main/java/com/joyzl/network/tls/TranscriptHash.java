package com.joyzl.network.tls;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import com.joyzl.network.buffer.DataBuffer;

/**
 * Transcript-Hash
 * 
 * @author ZhangXi 2024年12月30日
 */
public class TranscriptHash extends CipherSuiter {

	// Transcript-Hash("")
	// SHA256=e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855

	// 实例化对象时将立即生成一份空字节的哈希结果
	// 每投递一次用于哈希算法的消息将自动清空当前哈希结果
	// 每获取一次哈希结果将克隆已计算的哈希副本，以便继续后续消息计算

	private final byte[] EMPTY_HASH;
	private byte[] current = TLS.EMPTY_BYTES;

	public TranscriptHash(short code) throws Exception {
		super(code);
		EMPTY_HASH = digest.digest();
	}

	/** Transcript-Hash(message) */
	public void hash(byte[] message) {
		digest.update(message);
		current = TLS.EMPTY_BYTES;
	}

	/** Transcript-Hash(message) */
	public void hash(DataBuffer message) {
		final ByteBuffer[] buffers = message.reads();
		for (int index = 0; index < buffers.length; index++) {
			buffers[index].mark();
			if (index == 0) {
				// 排除RecordLayer的5个字节
				buffers[index].position(buffers[index].position() + 5);
			}
			digest.update(buffers[index]);
			buffers[index].reset();
		}
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

	/** Transcript-Hash("") */
	public byte[] hashEmpty() {
		return EMPTY_HASH;
	}

	public void hashReset() {
		current = TLS.EMPTY_BYTES;
		digest.reset();
	}
}