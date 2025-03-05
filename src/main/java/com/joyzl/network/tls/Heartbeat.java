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
}