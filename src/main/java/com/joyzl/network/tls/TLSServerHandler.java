package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class TLSServerHandler extends TLS implements ChainHandler {

	// 各版本共享
	private final ChainHandler handler;
	private final TLSParameters parameters;
	// 各版本实例
	private final V3ServerHandler v3;
	private final V2ServerHandler v2;
	private final V1ServerHandler v1;
	private final V0ServerHandler v0;

	public TLSServerHandler(ChainHandler handler, TLSParameters parameters) {
		this.parameters = parameters;
		this.handler = handler;

		v3 = new V3ServerHandler(handler, parameters);
		v2 = new V2ServerHandler(handler, parameters);
		v1 = new V1ServerHandler(handler, parameters);
		v0 = new V0ServerHandler(handler, parameters);
	}

	@Override
	public long getTimeoutRead() {
		return handler.getTimeoutRead();
	}

	@Override
	public long getTimeoutWrite() {
		return handler.getTimeoutWrite();
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.receive();
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		handler.disconnected(chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		handler.error(chain, e);
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final TLSSlaveContext context = chain.getContext(TLSSlaveContext.class);
		if (context != null) {
			return context.handler.decode(chain, buffer);
		}
		return v3.decode(chain, buffer);
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final TLSSlaveContext context = chain.getContext(TLSSlaveContext.class);
		if (context != null) {
			return context.handler.encode(chain, message);
		}
		return v3.encode(chain, message);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (chain.hasContext()) {
			final TLSSlaveContext context = chain.getContext(TLSSlaveContext.class);
			context.handler.received(chain, message);
		} else {
			if (message instanceof ClientHello) {
				final ClientHello hello = (ClientHello) message;
				if (hello.getVersion() <= TLS.SSL30) {
					// ClientHello,ServerHello
					// 已禁止0x0300或更低版本
					chain.send(new Alert(Alert.PROTOCOL_VERSION));
				} else {
					// 如果有SupportedVersions则匹配此扩展中的版本
					// 反之由ClientHello.version确定版本

					final SupportedVersions versions = hello.getExtension(Extension.SUPPORTED_VERSIONS);
					if (versions != null && versions.size() > 0) {
						hello.setVersion(versions.match(parameters.versions()));
					}
					if (hello.getVersion() == V13) {
						v3.connected(chain);
						v3.received(chain, message);
					} else if (hello.getVersion() == V12) {
						v2.connected(chain);
						v2.received(chain, message);
					} else if (hello.getVersion() == V11) {
						v2.connected(chain);
						v1.received(chain, message);
					} else if (hello.getVersion() == V10) {
						v2.connected(chain);
						v0.received(chain, message);
					} else {
						chain.send(new Alert(Alert.PROTOCOL_VERSION));
					}
				}
			} else {
				chain.send(new Alert(Alert.UNEXPECTED_MESSAGE));
			}
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		final TLSSlaveContext context = chain.getContext(TLSSlaveContext.class);
		if (context != null) {
			context.handler.sent(chain, message);
		} else {
			v3.encode(chain, message);
		}
	}

	public TLSParameters getParameters() {
		return parameters;
	}
}