package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *     opaque renegotiated_connection<0..255>;
 * } RenegotiationInfo;
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class RenegotiationInfo extends Extension {

	public final static byte[] TLS_EMPTY_RENEGOTIATION_INFO_SCSV = new byte[] { 0x00, (byte) 0xFF };

	private byte[] value = TLS.EMPTY_BYTES;

	@Override
	public short type() {
		return RENEGOTIATION_INFO;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		if (value == null) {
			this.value = TLS.EMPTY_BYTES;
		} else {
			this.value = value;
		}
	}
}