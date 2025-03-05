package com.joyzl.network.tls;

/**
 * <pre>
 * struct {} Empty;

 * struct {
 *     select (Handshake.msg_type) {
 *      case new_session_ticket:   uint32 max_early_data_size;
 *      case client_hello:         Empty;
 *      case encrypted_extensions: Empty;
 *     };
 * } EarlyDataIndication;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class EarlyDataIndication extends Extension {

	public final static EarlyDataIndication EMPTY = new EarlyDataIndication();
	public final static EarlyDataIndication MAX_EARLY_DATA_SIZE = new EarlyDataIndication(Record.PLAINTEXT_MAX);

	private int maxSize = 0;

	public EarlyDataIndication() {
	}

	public EarlyDataIndication(int max) {
		maxSize = max;
	}

	@Override
	public short type() {
		return EARLY_DATA;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int value) {
		maxSize = value;
	}

	@Override
	public String toString() {
		return "early_data:" + maxSize;
	}
}