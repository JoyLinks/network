package com.joyzl.network.tls;

/**
 * <pre>
 * enum {
 *     update_not_requested(0), update_requested(1), (255)
 * } KeyUpdateRequest;
 * 
 * struct {
 *     KeyUpdateRequest request_update;
 * } KeyUpdate;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class KeyUpdate extends Handshake {

	// KeyUpdateRequest MAX(255)

	public final static byte UPDATE_NOT_REQUESTED = 0;
	public final static byte UPDATE_REQUESTED = 1;

	////////////////////////////////////////////////////////////////////////////////

	private byte request = UPDATE_REQUESTED;

	public KeyUpdate() {
	}

	public KeyUpdate(byte request) {
		this.request = request;
	}

	@Override
	public byte msgType() {
		return KEY_UPDATE;
	}

	public byte get() {
		return request;
	}

	public void set(byte value) {
		request = value;
	}

	@Override
	public String toString() {
		return name() + ":" + request;
	}
}