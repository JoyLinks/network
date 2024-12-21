package com.joyzl.network.tls;

/**
 * <pre>
 * struct {} Empty;

 * struct {
 *     select (Handshake.msg_type) {
 *      case new_session_ticket:   uint32 max_early_data_size;
 *      case client_hello: *    Empty;
 *      case encrypted_extensions: Empty;
 *     };
 * } EarlyDataIndication;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class EarlyData extends Extension {

	public final static EarlyData EMPTY = new EarlyData();
	private int maxSize = 0;

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