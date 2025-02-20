package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public abstract class RecordHandler extends RecordCoder implements ChainHandler<Object> {

	protected abstract ChainHandler<Object> handler();

	@Override
	public DataBuffer encode(ChainChannel<Object> chain, Object message) throws Exception {
		if (message instanceof Record) {
			return encode((Record) message);
		} else {
			// APPLICATION DATA
			final DataBuffer data = handler().encode(chain, message);
			return encodeCiphertext(ApplicationData.INSTANCE, data);
		}
	}

	@Override
	public final Object decode(ChainChannel<Object> chain, DataBuffer buffer) throws Exception {
		Object message = super.decode(chain, buffer);
		if (message instanceof DataBuffer data) {
			while (true) {
				message = handler().decode(chain, data);
				if (message == null) {
					//// 断报，粘报
					return null;
				}
				if (data.readable() > 0) {
					handler().received(chain, message);
				} else {
					data.release();
					return message;
				}
			}
		}
		return message;
	}

	@Override
	public void received(ChainChannel<Object> chain, Object message) throws Exception {
		if (message == null) {
			// TIMEOUT
			handler().received(chain, message);
		} else if (message instanceof Record) {
			final Record record = (Record) message;
			if (record.contentType() == Record.APPLICATION_DATA) {
				// 空的应用消息
				System.out.println(message);
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				// 忽略兼容性消息
				System.out.println(message);
			} else if (record.contentType() == Record.HANDSHAKE) {
				received(chain, (Handshake) record);
			} else if (record.contentType() == Record.HEARTBEAT) {
				heartbeat(chain, (HeartbeatMessage) record);
			} else if (record.contentType() == Record.INVALID) {
				// 无效消息
				System.out.println(message);
			} else if (record.contentType() == Record.ALERT) {
				// 告警消息
				System.out.println(message);
			} else {
				System.out.println(message);
			}
			// System.out.println("TLS:RECEIVE");
			// chain.receive();
		} else {
			handler().received(chain, message);
		}
	}

	@Override
	public void disconnected(ChainChannel<Object> chain) throws Exception {
		handler().disconnected(chain);
	}

	@Override
	public void error(ChainChannel<Object> chain, Throwable e) {
		handler().error(chain, e);
	}

	@Override
	public void beat(ChainChannel<Object> chain) throws Exception {
		chain.send(new HeartbeatMessage(HeartbeatMessage.HEARTBEAT_REQUEST));
	};

	protected void heartbeat(ChainChannel<Object> chain, HeartbeatMessage message) {
		if (message.getMessageType() == HeartbeatMessage.HEARTBEAT_REQUEST) {
			message.setMessageType(HeartbeatMessage.HEARTBEAT_RESPONSE);
			chain.send(message);
		}
	}
}