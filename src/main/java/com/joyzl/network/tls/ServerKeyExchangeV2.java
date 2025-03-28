package com.joyzl.network.tls;

/**
 * <pre>
 * TLS 1.2
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
 */
class ServerKeyExchangeV2 extends Handshake {

	/** struct {} */
	final static ServerKeyExchangeV2 EMPTY = new ServerKeyExchangeV2();

	private ServerKeyExchangeV2() {
	}

	@Override
	public byte msgType() {
		return SERVER_KEY_EXCHANGE;
	}

	static class ServerDHParams extends ServerKeyExchangeV2 {
		/** prime modulus */
		private byte[] p = TLS.EMPTY_BYTES;
		/** generator */
		private byte[] g = TLS.EMPTY_BYTES;
		/** public value (g^X mod p) */
		private byte[] Ys = TLS.EMPTY_BYTES;

		/** digitally-signed */
		private byte[] clientRandom = TLS.EMPTY_BYTES;
		private byte[] serverRandom = TLS.EMPTY_BYTES;

		public ServerDHParams() {
		}

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

		public byte[] getClientRandom() {
			return clientRandom;
		}

		public void setClientRandom(byte[] value) {
			if (value == null) {
				clientRandom = TLS.EMPTY_BYTES;
			} else {
				clientRandom = value;
			}
		}

		public byte[] getServerRandom() {
			return serverRandom;
		}

		public void setServerRandom(byte[] value) {
			if (value == null) {
				serverRandom = TLS.EMPTY_BYTES;
			} else {
				serverRandom = value;
			}
		}
	}
}