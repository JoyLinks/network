package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * TLSClientHandler
 * 
 * @author ZhangXi 2025年3月10日
 */
public class TLSClientHandler implements ChainHandler {

	// 各版本共享对象
	private final TLSParameters parameters;
	private final TLSShare share;

	private final V3ClientHandler v3;
	private final V2ClientHandler v2;
	private final V0ClientHandler v1;
	private ChainHandler tls;
	private short version;

	public TLSClientHandler(ChainHandler handler, TLSParameters parameters) {
		this.share = new TLSShare();
		this.parameters = parameters;

		v3 = new V3ClientHandler(handler, parameters, share);
		v2 = new V2ClientHandler(handler, parameters, share);
		v1 = new V0ClientHandler(handler, parameters, share);
		// 默认采用1.3版本
		tls = v3;
	}

	public TLSClientHandler(ChainHandler handler) {
		this(handler, new TLSParameters());
	}

	@Override
	public long getTimeoutRead() {
		return tls.getTimeoutRead();
	}

	@Override
	public long getTimeoutWrite() {
		return tls.getTimeoutWrite();
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		tls.disconnected(chain);
		version = 0;
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		tls.connected(chain);
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		return tls.decode(chain, buffer);
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		return tls.encode(chain, message);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		if (version <= 0) {
			if (message instanceof Handshake handshake) {
				if (handshake.msgType() == Handshake.SERVER_HELLO) {
					final ServerHello hello = (ServerHello) handshake;
					final SelectedVersion selected = hello.getExtension(Extension.SUPPORTED_VERSIONS);
					if (selected != null) {
						version = selected.get();
					} else {
						version = hello.getVersion();
					}
					if (version == TLS.V13) {
						tls = v3;
					} else if (version == TLS.V12) {
						tls = v2;
					} else if (version == TLS.V11 || version == TLS.V10) {
						tls = v1;
					} else {
						chain.send(new Alert(Alert.PROTOCOL_VERSION));
						return;
					}
				}
			}
		}
		tls.received(chain, message);
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		tls.sent(chain, message);
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		tls.beat(chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		tls.error(chain, e);
	}

	public TLSParameters getParameters() {
		return parameters;
	}

	public short getVersion() {
		return version;
	}
}