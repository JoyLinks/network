package com.joyzl.network.tls;

/**
 * <pre>
 * struct {} EndOfEarlyData;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class EndOfEarlyData extends Handshake {

	public final static EndOfEarlyData INSTANCE = new EndOfEarlyData();

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.END_OF_EARLY_DATA;
	}
}