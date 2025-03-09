package com.joyzl.network.tls;

/**
 * <pre>
 * TLS 1.2
 * 
 * enum {
 *       dhe_dss, dhe_rsa, dh_anon, // 发送 ServerKeyExchange
 *       rsa, dh_dss, dh_rsa        // 不发 ServerKeyExchange
 * } KeyExchangeAlgorithm;
 * 
 * struct {
 *       opaque dh_p<1..2^16-1>;
 *       opaque dh_g<1..2^16-1>;
 *       opaque dh_Ys<1..2^16-1>;
 * } ServerDHParams;     // Ephemeral DH parameters
 * 
 * struct {
 *       select (KeyExchangeAlgorithm) {
 *             case dh_anon:
 *                   ServerDHParams params;
 *             case dhe_dss:
 *             case dhe_rsa:
 *                   ServerDHParams params;
 *                   digitally-signed struct {
 *                         opaque client_random[32];
 *                         opaque server_random[32];
 *                         ServerDHParams params;
 *                   } signed_params;
 *             case rsa:
 *             case dh_dss:
 *             case dh_rsa:
 *                   struct {};
 *       };
 * } ServerKeyExchange;
 * </pre>
 * 
 * <pre>
 * RFC2246 TLS 1.0
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
class ServerKeyExchange extends Handshake {

	private ServerDHParams params;
	private byte[] signed = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return SERVER_KEY_EXCHANGE;
	}

	public ServerDHParams getParams() {
		return params;
	}

	public void setParams(ServerDHParams value) {
		params = value;
	}

	public byte[] getSigned() {
		return signed;
	}

	public void setSigned(byte[] value) {
		if (value == null) {
			signed = TLS.EMPTY_BYTES;
		} else {
			signed = value;
		}
	}

	/**
	 * Ephemeral DH parameters
	 */
	static class ServerDHParams {
		/** prime modulus */
		private byte[] p = TLS.EMPTY_BYTES;
		/** generator */
		private byte[] g = TLS.EMPTY_BYTES;
		/** public value (g^X mod p) */
		private byte[] Ys = TLS.EMPTY_BYTES;

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

		public int length() {
			return p.length + g.length + Ys.length;
		}
	}

	/**
	 * TLS 1.1 1.0
	 */
	static class ServerRSAParams {
		/** rsa_modulus */
		private byte[] modulus;
		/** rsa_exponent */
		private byte[] exponent;

		public byte[] getExponent() {
			return exponent;
		}

		public void setExponent(byte[] value) {
			exponent = value;
		}

		public byte[] getModulus() {
			return modulus;
		}

		public void setModulus(byte[] value) {
			modulus = value;
		}
	}
}