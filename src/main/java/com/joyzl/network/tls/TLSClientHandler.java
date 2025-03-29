package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * TLSClientHandler
 * 
 * @author ZhangXi 2025年3月10日
 */
public class TLSClientHandler extends TLS implements ChainHandler {

	// 各版本共享
	private final ChainHandler handler;
	private final TLSParameters parameters;
	private final TLSShare share;
	// 各版本实例
	private final V3ClientHandler v3;
	private final V2ClientHandler v2;
	private final V1ClientHandler v1;
	private final V0ClientHandler v0;
	// 当前版本和实例
	private ChainHandler current;
	private short version;

	public TLSClientHandler(ChainHandler handler, TLSParameters parameters) {
		this.share = new TLSShare();
		this.parameters = parameters;
		this.handler = handler;

		v3 = new V3ClientHandler(handler, parameters, share);
		v2 = new V2ClientHandler(handler, parameters, share);
		v1 = new V1ClientHandler(handler, parameters, share);
		v0 = new V0ClientHandler(handler, parameters, share);
		// 默认采用1.3版本
		// 即便版本协商失败也应用最高版本发送失败消息
		current = v3;
	}

	public TLSClientHandler(ChainHandler handler) {
		this(handler, new TLSParameters());
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
	public void disconnected(ChainChannel chain) throws Exception {
		current.disconnected(chain);
		version = 0;
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		current.connected(chain);
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		return current.decode(chain, buffer);
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		return current.encode(chain, message);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (version <= 0) {
			// ClientHello和ServerHello在各个版本兼容
			// 首先协商版本，之后切换为协商版本执行后续握手和通信
			// ***Hello消息待协商密码套件后执行消息摘要计算，由TLSShare对象提供消息缓存
			if (message instanceof ServerHello) {
				final ServerHello hello = (ServerHello) message;
				final SelectedVersion selected = hello.getExtension(Extension.SUPPORTED_VERSIONS);
				if (selected != null) {
					version = selected.get();
				} else {
					version = hello.getVersion();
				}
				if (version == V13) {
					current = v3;
				} else if (version == V12) {
					current = v2;
				} else if (version == V11) {
					current = v1;
				} else if (version == V10) {
					current = v0;
				} else {
					chain.send(new Alert(Alert.PROTOCOL_VERSION));
					return;
				}
			} else {
				chain.send(new Alert(Alert.UNEXPECTED_MESSAGE));
				return;
			}
		}
		current.received(chain, message);
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		current.sent(chain, message);
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		current.beat(chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		current.error(chain, e);
	}

	public TLSParameters getParameters() {
		return parameters;
	}

	public short getVersion() {
		return version;
	}
}