package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *      HeartbeatMessageType type;
 *      uint16 payload_length;
 *      opaque payload[HeartbeatMessage.payload_length];
 *      opaque padding[padding_length];
 * } HeartbeatMessage;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class HeartbeatMessage extends TLSPlaintext {

	// HeartbeatMessageType MAX(255)

	public final static byte HEARTBEAT_REQUEST = 1;
	public final static byte HEARTBEAT_RESPONSE = 2;

	////////////////////////////////////////////////////////////////////////////////

	private byte type = HEARTBEAT_REQUEST;
	private byte[] payload = TLS.EMPTY_BYTES;

	@Override
	public byte contentType() {
		return HEARTBEAT;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] value) {
		payload = value;
	}

	public byte getMessageType() {
		return type;
	}

	public void setMessageType(byte value) {
		type = value;
	}

	/**
	 * REQUEST -> RESPONSE
	 */
	public HeartbeatMessage exchange() {
		if (type == HEARTBEAT_REQUEST) {
			type = HEARTBEAT_RESPONSE;
		}
		return this;
	}
}