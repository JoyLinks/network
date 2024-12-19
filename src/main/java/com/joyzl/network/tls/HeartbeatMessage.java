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
public class HeartbeatMessage {

	private HeartbeatMessageType type;
	private byte[] payload;

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] value) {
		payload = value;
	}

	public HeartbeatMessageType getType() {
		return type;
	}

	public void setType(HeartbeatMessageType value) {
		type = value;
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