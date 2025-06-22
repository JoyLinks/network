/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 心跳扩展
 * 
 * <pre>
 * RFC 6520
 * Transport Layer Security (TLS) and Datagram Transport Layer Security (DTLS) Heartbeat Extension
 * 
 * enum {
 *     peer_allowed_to_send(1),
 *     peer_not_allowed_to_send(2),
 *     (255)
 * } HeartbeatMode;

 * struct {
 *     HeartbeatMode mode;
 * } HeartbeatExtension;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class Heartbeat extends Extension {

	// HeartbeatMode MAX(255)

	public final static byte PEER_ALLOWED_TO_SEND = 1;
	public final static byte PEER_NOT_ALLOWED_TO_SEND = 2;

	////////////////////////////////////////////////////////////////////////////////

	private byte mode;

	public Heartbeat() {
	}

	public Heartbeat(byte mode) {
		this.mode = mode;
	}

	@Override
	public short type() {
		return HEARTBEAT;
	}

	public byte getMode() {
		return mode;
	}

	public void setMode(byte value) {
		mode = value;
	}

	@Override
	public String toString() {
		if (getMode() == PEER_ALLOWED_TO_SEND) {
			return name() + ":PEER_ALLOWED_TO_SEND";
		}
		if (getMode() == PEER_NOT_ALLOWED_TO_SEND) {
			return name() + ":PEER_NOT_ALLOWED_TO_SEND";
		}
		return name() + ":EMPTY";
	}
}