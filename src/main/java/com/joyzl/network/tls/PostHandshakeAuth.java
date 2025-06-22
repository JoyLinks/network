/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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
class PostHandshakeAuth extends Extension {

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