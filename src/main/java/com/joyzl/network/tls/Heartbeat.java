package com.joyzl.network.tls;

/**
 * RFC 6520
 * 
 * <pre>
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
public class Heartbeat extends Extension {

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