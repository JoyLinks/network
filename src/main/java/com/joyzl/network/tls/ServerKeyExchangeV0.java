/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * <pre>
 * TLS 1.0
 * 
 * enum { rsa, diffie_hellman } KeyExchangeAlgorithm;
 * 
 * struct {
 *       opaque rsa_modulus<1..2^16-1>;
 *       opaque rsa_exponent<1..2^16-1>;
 * } ServerRSAParams;
 * 
 * struct {
 *       opaque dh_p<1..2^16-1>;
 *       opaque dh_g<1..2^16-1>;
 *       opaque dh_Ys<1..2^16-1>;
 * } ServerDHParams;     // Ephemeral DH parameters
 * 
 * struct {
 *       select (KeyExchangeAlgorithm) {
 *             case diffie_hellman:
 *                   ServerDHParams params;
 *                   Signature signed_params;
 *             case rsa:
 *                   ServerRSAParams params;
 *                   Signature signed_params;
 *       };
 * } ServerKeyExchange;
 * 
 * enum { anonymous, rsa, dsa } SignatureAlgorithm;
 * 
 * select (SignatureAlgorithm) {
 *       case anonymous: struct { };
 *       case rsa:
 *             digitally-signed struct {
 *                   opaque md5_hash[16];
 *                   opaque sha_hash[20];
 *             };
 *       case dsa:
 *             digitally-signed struct {
 *                   opaque sha_hash[20];
 *             };
 * } Signature;
 * </pre>
 */
class ServerKeyExchangeV0 extends Handshake {

	// 由于编码时缺乏上下文，此消息需要二次解码
	// 既将整个消息读取为字节数组，根据密钥和签名算法二次拆分
	// 服务端构建消息时无须二次步骤

	/** struct {} */
	final static ServerKeyExchangeV0 EMPTY = new ServerKeyExchangeV0();

	@Override
	public byte msgType() {
		return SERVER_KEY_EXCHANGE;
	}

	static class ServerParams extends ServerKeyExchangeV0 {
		private byte[] params;

		public byte[] getParams() {
			return params;
		}

		public void setParams(byte[] params) {
			this.params = params;
		}

		public ServerDHParams toServerDHParams() {
			final ServerDHParams params = new ServerDHParams();
			// TODO
			return params;
		}

		public ServerRSAParams toServerRSAParams() {
			final ServerRSAParams params = new ServerRSAParams();
			// TODO
			return params;
		}
	}

	static class ServerDHParams extends ServerKeyExchangeV0 {
		/** prime modulus */
		private byte[] p = TLS.EMPTY_BYTES;
		/** generator */
		private byte[] g = TLS.EMPTY_BYTES;
		/** public value (g^X mod p) */
		private byte[] Ys = TLS.EMPTY_BYTES;

		/** digitally-signed */
		private byte[] signedMD5 = TLS.EMPTY_BYTES;
		private byte[] signedSHA = TLS.EMPTY_BYTES;

		public byte[] getYs() {
			return Ys;
		}

		public void setYs(byte[] value) {
			if (value == null) {
				Ys = TLS.EMPTY_BYTES;
			} else {
				Ys = value;
			}
		}

		public byte[] getG() {
			return g;
		}

		public void setG(byte[] value) {
			if (value == null) {
				g = TLS.EMPTY_BYTES;
			} else {
				g = value;
			}
		}

		public byte[] getP() {
			return p;
		}

		public void setP(byte[] value) {
			if (value == null) {
				p = TLS.EMPTY_BYTES;
			} else {
				p = value;
			}
		}

		public int paramsLength() {
			return p.length + g.length + Ys.length;
		}

		public byte[] getSignedMD5() {
			return signedMD5;
		}

		public void setSignedMD5(byte[] value) {
			if (value == null) {
				signedMD5 = TLS.EMPTY_BYTES;
			} else {
				signedMD5 = value;
			}
		}

		public byte[] getSignedSHA() {
			return signedSHA;
		}

		public void setSignedSHA(byte[] value) {
			if (value == null) {
				signedSHA = TLS.EMPTY_BYTES;
			} else {
				signedSHA = value;
			}
		}
	}

	static class ServerRSAParams extends ServerKeyExchangeV0 {
		/** rsa_modulus */
		private byte[] modulus = TLS.EMPTY_BYTES;
		/** rsa_exponent */
		private byte[] exponent = TLS.EMPTY_BYTES;

		/** digitally-signed */
		private byte[] signedMD5 = TLS.EMPTY_BYTES;
		private byte[] signedSHA = TLS.EMPTY_BYTES;

		public byte[] getExponent() {
			return exponent;
		}

		public void setExponent(byte[] value) {
			if (value == null) {
				exponent = TLS.EMPTY_BYTES;
			} else {
				exponent = value;
			}
		}

		public byte[] getModulus() {
			return modulus;
		}

		public void setModulus(byte[] value) {
			if (value == null) {
				modulus = TLS.EMPTY_BYTES;
			} else {
				modulus = value;
			}
		}

		public int paramsLength() {
			return exponent.length + modulus.length;
		}

		public byte[] getSignedMD5() {
			return signedMD5;
		}

		public void setSignedMD5(byte[] value) {
			if (value == null) {
				signedMD5 = TLS.EMPTY_BYTES;
			} else {
				signedMD5 = value;
			}
		}

		public byte[] getSignedSHA() {
			return signedSHA;
		}

		public void setSignedSHA(byte[] value) {
			if (value == null) {
				signedSHA = TLS.EMPTY_BYTES;
			} else {
				signedSHA = value;
			}
		}
	}
}