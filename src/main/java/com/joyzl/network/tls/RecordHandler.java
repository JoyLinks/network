package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;

abstract class RecordHandler extends RecordCoder implements ChainHandler {

	protected abstract ChainHandler handler();

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		if (message instanceof Record) {
			return encode((Record) message);
		} else {
			// APPLICATION DATA
			final DataBuffer data = handler().encode(chain, message);
			return encodeCiphertext(ApplicationData.INSTANCE, data);
		}
	}

	@Override
	public final Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
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
	public void received(ChainChannel chain, Object message) throws Exception {
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
	public void disconnected(ChainChannel chain) throws Exception {
		handler().disconnected(chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		handler().error(chain, e);
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		final HeartbeatMessage heartbeat = new HeartbeatMessage();
		heartbeat.setMessageType(HeartbeatMessage.HEARTBEAT_REQUEST);
		heartbeat.setPayload(Binary.split(chain.hashCode()));
		chain.send(heartbeat);
	};

	protected void heartbeat(ChainChannel chain, HeartbeatMessage message) {
		if (message.getMessageType() == HeartbeatMessage.HEARTBEAT_RESPONSE) {
			if (message.getPayload().length == 4) {
				int hashCode = Binary.getInteger(message.getPayload(), 0);
				if (hashCode == chain.hashCode()) {
					// OK
				}
			}
		} else {
			chain.send(new Alert(Alert.UNEXPECTED_MESSAGE));
		}
	}
}