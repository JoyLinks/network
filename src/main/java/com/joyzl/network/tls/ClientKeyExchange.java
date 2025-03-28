package com.joyzl.network.tls;

/**
 * <pre>
 * TLS 1.0 1.1 1.2
 * 
 * struct {
 *      select (KeyExchangeAlgorithm) {
 *           case rsa: EncryptedPreMasterSecret;
 *           case diffie_hellman: ClientDiffieHellmanPublic;
 *      } exchange_keys;
 * } ClientKeyExchange;
 * 
 * struct {
 *      ProtocolVersion client_version; // newest
 *      opaque random[46]; // 46 securely-generated random bytes
 * } PreMasterSecret;
 * 
 * struct {
 *      public-key-encrypted PreMasterSecret pre_master_secret;
 * } EncryptedPreMasterSecret;
 * 
 * 
 * enum { implicit, explicit } PublicValueEncoding;
 * 
 * struct {
 *      select (PublicValueEncoding) {
 *           case implicit: struct { };
 *           case explicit: opaque dh_Yc<1..2^16-1>;
 *      } dh_public;
 * } ClientDiffieHellmanPublic;
 * 
 * dh_Yc The client's Diffie-Hellman public value (Yc).
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
class ClientKeyExchange extends Handshake {

	/**
	 * EncryptedPreMasterSecret:pre_master_secret;
	 * ClientDiffieHellmanPublic:implicit: struct {};
	 * ClientDiffieHellmanPublic:explicit: opaque dh_Yc<1..2^16-1>;
	 */
	private byte[] exchangeKeys = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CLIENT_KEY_EXCHANGE;
	}

	public byte[] get() {
		return exchangeKeys;
	}

	public void set(byte[] value) {
		if (value == null) {
			exchangeKeys = TLS.EMPTY_BYTES;
		} else {
			exchangeKeys = value;
		}
	}

	@Override
	public String toString() {
		return name() + ":" + exchangeKeys.length + "byte";
	}
}