package com.joyzl.network.tls;

import java.security.MessageDigest;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;

/**
 * Transcript-Hash
 * 
 * @author ZhangXi 2024年12月30日
 */
class TranscriptHash {

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

	/** 指定哈希算法 */
	public void digest(String name) throws Exception {
		digest = MessageDigest.getInstance(name);
		EMPTY_HASH = digest.digest();
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

	/** Transcript-Hash("") */
	public byte[] hashEmpty() {
		return EMPTY_HASH;
	}
}