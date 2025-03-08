package com.joyzl.network.tls;

/**
 * 扩展：共享密钥
 * 
 * <pre>
 * struct {
 *     NamedGroup group;
 *     opaque key_exchange<1..2^16-1>;
 * } KeyShareEntry;
 * 
 * struct {
 *     KeyShareEntry client_shares<0..2^16-1>;
 * } KeyShareClientHello;
 * 
 * struct {
 *     NamedGroup selected_group;
 * } KeyShareHelloRetryRequest;
 * 
 * struct {
 *     KeyShareEntry server_share;
 * } KeyShareServerHello;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
abstract class KeyShare extends Extension {

	@Override
	public short type() {
		return KEY_SHARE;
	}

	/**
	 * <pre>
	 * key_exchange:Diffie-Hellman (DHE)
	 * key_exchange:Elliptic Curve Diffie-Hellman (ECDHE)
	 * 
	 * struct {
	 *     uint8 legacy_form = 4;
	 *     opaque X[coordinate_length];
	 *     opaque Y[coordinate_length];
	 * } UncompressedPointRepresentation;
	 * </pre>
	 * 
	 * @author ZhangXi 2024年12月19日
	 */
	static class KeyShareEntry {

		private short group;
		private byte[] key_exchange = TLS.EMPTY_BYTES;

		public KeyShareEntry() {
		}

		public KeyShareEntry(short group) {
			this.group = group;
		}

		public KeyShareEntry(short group, byte[] key_exchange) {
			this.group = group;
			this.key_exchange = key_exchange;
		}

		public short getGroup() {
			return group;
		}

		public void setGroup(short value) {
			group = value;
		}

		public byte[] getKeyExchange() {
			return key_exchange;
		}

		public void setKeyExchange(byte[] value) {
			if (value == null) {
				key_exchange = TLS.EMPTY_BYTES;
			} else {
				key_exchange = value;
			}
		}

		@Override
		public String toString() {
			return NamedGroup.named(getGroup()) + "(" + key_exchange.length + "byte)";
		}
	}
}