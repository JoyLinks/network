package com.joyzl.network.tls;

/**
 * 客户端愿意握手后再认证
 * 
 * <pre>
 * struct {} PostHandshakeAuth;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class PostHandshakeAuth extends Extension {

	public final static PostHandshakeAuth INSTANCE = new PostHandshakeAuth();

	@Override
	public short type() {
		return POST_HANDSHAKE_AUTH;
	}

	@Override
	public String toString() {
		return "post_handshake_auth";
	}
}