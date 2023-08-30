package com.joyzl.network.chain.test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public class TCPServerHandler implements ChainHandler<Message> {

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		System.out.println("SERVER:CONNECTED " + chain.getPoint());
		chain.receive();
	}

	@Override
	public Message decode(ChainChannel<Message> chain, DataBuffer reader) throws Exception {
		// System.out.println("SERVER:DECODE");
		// System.out.println(reader);
		reader.mark();
		final int length = reader.readInt();
		if (reader.readable() >= length) {
			final Message message = new Message();
			message.setLength(length);
			message.setId(reader.readInt());
			final byte[] bytes = new byte[length];
			for (int index = 0; index < length; index++) {
				bytes[index] = reader.readByte();
			}
			message.setBytes(bytes);
			return message;
		} else {
			reader.reset();
		}
		return null;
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		// System.out.println("SERVER:RECEIVED");
		if (message == null) {
		} else {
			chain.send(message);
		}
		chain.receive();
	}

	@Override
	public DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		final DataBuffer writer = DataBuffer.instance();
		writer.writeInt(message.getLength());
		writer.writeInt(message.getId());
		writer.write(message.getBytes());
		return writer;
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		// System.out.println("SERVER:SENT");
		if (message.getId() == 0) {
			chain.close();
		}
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
		System.out.println("SERVER:DISCONNECTED " + chain.getPoint());
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		System.out.println("SERVER:ERROR");
		e.printStackTrace();
	}
}