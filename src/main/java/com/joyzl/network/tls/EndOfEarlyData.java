package com.joyzl.network.tls;

/**
 * <pre>
 * struct {} EndOfEarlyData;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class EndOfEarlyData extends Handshake {

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.END_OF_EARLY_DATA;
	}
}