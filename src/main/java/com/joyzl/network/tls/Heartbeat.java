package com.joyzl.network.tls;

/**
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

	private HeartbeatMode mode;

	@Override
	public ExtensionType type() {
		return ExtensionType.HEARTBEAT;
	}

	public HeartbeatMode getMode() {
		return mode;
	}

	public void setMode(HeartbeatMode value) {
		mode = value;
	}
}