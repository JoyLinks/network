package com.joyzl.network.http;

import com.joyzl.network.chain.ChainType;

/**
 * WEB Socket Handler
 * 
 * @author ZhangXi 2024年12月12日
 */
public interface WEBSocketHandler {

	/**
	 * HTTP Chain 内部使用的默认实例，用户不应使用此实例
	 */
	final static WEBSocketHandler DEFAULT_SLAVE = new WEBSocketHandler() {
		@Override
		public ChainType type() {
			return ChainType.TCP_HTTP_SLAVE;
		}

		@Override
		public void received(HTTPSlave slave, WEBSocketMessage message) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void connected(HTTPSlave slave) throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public void disconnected(HTTPSlave slave) throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public void sent(HTTPSlave slave, WEBSocketMessage message) throws Exception {
			throw new UnsupportedOperationException();
		}
	};

	default ChainType type() {
		return ChainType.TCP_HTTP_SLAVE_WEB_SOCKET;
	};

	void connected(HTTPSlave slave) throws Exception;

	void received(HTTPSlave slave, WEBSocketMessage message) throws Exception;

	void sent(HTTPSlave slave, WEBSocketMessage message) throws Exception;

	void disconnected(HTTPSlave slave) throws Exception;
}