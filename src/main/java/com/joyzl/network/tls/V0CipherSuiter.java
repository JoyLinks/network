package com.joyzl.network.tls;

import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;
import com.joyzl.network.codec.Binary;

/**
 * 密码套件执行消息加密与解密
 * 
 * @author ZhangXi 2024年12月24日
 */
class V0CipherSuiter extends CipherSuiter {

	private Mac mac;

	public void initialize(CipherSuiteType type) throws Exception {
		super.initialize(type);
		// 消息签名使用密码套件算法
		// SecurityParameters.mac_algorithm
		// SecurityParameters.mac_length
		// SecurityParameters.mac_key_length
		if (type.macAlgorithm() == null) {
			mac = null;
		} else if (mac == null || !type.macAlgorithm().equals(mac.getAlgorithm())) {
			mac = Mac.getInstance(type.macAlgorithm());
		} else {
			mac.reset();
		}
	}

	/**
	 * 开始加密
	 */
	public void encryptBlock() throws Exception {
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, new IvParameterSpec(encryptIV), TLS.RANDOM);
	}

	/**
	 * 开始解密
	 */
	public void decryptBlock() throws Exception {
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, new IvParameterSpec(decryptIV), TLS.RANDOM);
	}

	/**
	 * 开始加密
	 */
	public void encryptStream() throws Exception {
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, TLS.RANDOM);
	}

	/**
	 * 开始解密
	 */
	public void decryptStream() throws Exception {
		decryptCipher.init(Cipher.ENCRYPT_MODE, decryptKey, TLS.RANDOM);
	}

	public void encryptUpdateIV(DataBuffer out) {
		// 首次使用密钥参数导出的IV
		// 后续记录使用之前加密后的最后块用作IV
		out.mark();
		int length = encryptIV.length;
		while (--length >= 0) {
			encryptIV[length] = out.backByte();
		}
		out.reset();
	}

	public void decryptUpdateIV(DataBuffer in, int length) {
		// 首次使用密钥参数导出的IV
		// 后续记录使用之前加密后的最后块用作IV
		int index = decryptIV.length;
		while (--index >= 0) {
			decryptIV[index] = in.get(--length);
		}
	}

	byte[] encryptMACKey;

	public void encryptMACKey(byte[] key) {
		encryptMACKey = key;
	}

	public byte[] encryptMAC(byte type, short version, DataBuffer data, int length) throws Exception {
		// System.out.println("encryptSequence" + encryptSequence);
		// 经过实际验证序列号从加密消息开始自增
		return MAC(encryptMACKey, encryptSequence, type, version, length, data);
	}

	byte[] decryptMACKey;

	public void decryptMACKey(byte[] key) {
		decryptMACKey = key;
	}

	public byte[] decryptMAC(byte type, short version, DataBuffer data, int length) throws Exception {
		// System.out.println("decryptSequence" + decryptSequence);
		// 经过实际验证序列号从加密消息开始自增
		// 由于解密后才能执行验证，因此解密序列号须减一
		return MAC(decryptMACKey, decryptSequence - 1, type, version, length, data);
	}

	/**
	 * TLS 1.0
	 * 
	 * <pre>
	 * MAC(MAC_write_key, seq_num +
	 *                    TLSCompressed.type +
	 *                    TLSCompressed.version +
	 *                    TLSCompressed.length +
	 *                    TLSCompressed.fragment);
	 * 由于压缩未启用TLSCompressed = TLSPlaintext
	 * </pre>
	 */
	protected final byte[] MAC(byte[] key, long seqnum, byte type, short version, int length, DataBuffer fragment) throws InvalidKeyException {
		mac.init(new SecretKeySpec(key, mac.getAlgorithm()));

		final byte[] temp = new byte[8];
		Binary.put(temp, 0, seqnum);
		mac.update(temp);

		mac.update(type);

		Binary.put(temp, 0, version);
		mac.update(temp, 0, 2);

		Binary.put(temp, 0, (short) length);
		mac.update(temp, 0, 2);

		// fragment
		DataBufferUnit unit = fragment.head();
		while (unit != null && length > 0) {
			unit.buffer().mark();
			if (unit.readable() <= length) {
				length -= unit.readable();
				mac.update(unit.buffer());
			} else {
				length = unit.readable() - length;
				unit.writeIndex(unit.writeIndex() - length);
				mac.update(unit.buffer());
				unit.writeIndex(unit.writeIndex() + length);
				length = 0;
			}
			unit.buffer().reset();
			unit = unit.next();
		}

		return mac.doFinal();
	}
}