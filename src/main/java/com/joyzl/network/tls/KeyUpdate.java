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
public class KeyUpdate extends Handshake {

	private KeyUpdateRequest request;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.KEY_UPDATE;
	}

	public KeyUpdateRequest getRequest() {
		return request;
	}

	public void setRequest(KeyUpdateRequest value) {
		request = value;
	}

	public void setRequest(int value) {
		request = KeyUpdateRequest.code(value);
	}
}