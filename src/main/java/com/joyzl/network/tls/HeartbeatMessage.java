/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 心跳消息
 * 
 * <pre>
 * RFC 6520
 * Transport Layer Security (TLS) and Datagram Transport Layer Security (DTLS) Heartbeat Extension
 * 
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
class HeartbeatMessage extends Record {

	// HeartbeatMessageType MAX(255)

	public final static byte HEARTBEAT_REQUEST = 1;
	public final static byte HEARTBEAT_RESPONSE = 2;

	////////////////////////////////////////////////////////////////////////////////

	private byte type = HEARTBEAT_REQUEST;
	private byte[] payload = TLS.EMPTY_BYTES;

	public HeartbeatMessage() {
	}

	public HeartbeatMessage(byte type) {
		this.type = type;
	}

	@Override
	public byte contentType() {
		return HEARTBEAT;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] value) {
		if (value == null) {
			payload = TLS.EMPTY_BYTES;
		} else {
			payload = value;
		}
	}

	public byte getMessageType() {
		return type;
	}

	public void setMessageType(byte value) {
		type = value;
	}
}