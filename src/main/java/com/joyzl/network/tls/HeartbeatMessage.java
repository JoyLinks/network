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

	private HeartbeatMessageType type = HeartbeatMessageType.HEARTBEAT_REQUEST;
	private byte[] payload = TLS.EMPTY_BYTES;

	@Override
	public ContentType contentType() {
		return ContentType.HEARTBEAT;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] value) {
		payload = value;
	}

	public HeartbeatMessageType getMessageType() {
		return type;
	}

	public void setMessageType(HeartbeatMessageType value) {
		type = value;
	}

	public void setMessageType(int value) {
		type = HeartbeatMessageType.code(value);
	}

	/**
	 * REQUEST -> RESPONSE
	 */
	public HeartbeatMessage exchange() {
		if (type == HeartbeatMessageType.HEARTBEAT_REQUEST) {
			type = HeartbeatMessageType.HEARTBEAT_RESPONSE;
		}
		return this;
	}
}