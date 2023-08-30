package com.joyzl.network.chain.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.joyzl.network.Executor;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.TCPClient;
import com.joyzl.network.chain.TCPServer;

class TestTCPClient implements ChainHandler<Message> {

	static TCPServer<Message> server;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Executor.initialize(0);
		server = new TCPServer<>(new TCPServerHandler(), null, 1000);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		server.close();
		Executor.shutdown();
	}

	@Test
	void test() throws Exception {
		final TestTCPClient handler = new TestTCPClient();
		final TCPClient<Message> client = new TCPClient<>(handler, "127.0.0.1", 1000);
		client.setHeartbeat(10);
		client.setReconnect(10);

		client.connect();
		synchronized (handler) {
			handler.wait();
		}
		client.close();
	}

	////////////////////////////////////////////////////////////////////////////////

	private int id = 0;

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		System.out.println("LINK:CONNECTED");
		assertTrue(chain.active());
		chain.receive();
	}

	@Override
	public DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		// System.out.println("LINK:ENCODE");
		final DataBuffer writer = DataBuffer.instance();
		writer.writeInt(message.getLength());
		writer.writeInt(message.getId());
		writer.write(message.getBytes());
		// System.out.println(writer);
		return writer;
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		// System.out.println("LINK:SENT");
	}

	@Override
	public void beat(ChainChannel<Message> chain) throws Exception {
		System.out.println("LINK:BEAT");
		// 构造消息
		final Message message = new Message();
		message.setLength(0);
		message.setBytes(new byte[0]);
		message.setId(id++);
		chain.send(message);
	}

	@Override
	public Message decode(ChainChannel<Message> chain, DataBuffer reader) throws Exception {
		// System.out.println("LINK:DECODE");
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
		// System.out.println("LINK:RECEIVED");
		if (message == null) {
		} else {
			if (message.getId() < 5) {
			} else {
				synchronized (this) {
					this.notify();
				}
			}
		}
		chain.receive();
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
		System.out.println("LINK:DISCONNECTED");
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		System.out.println("LINK:ERROR");
		System.out.println(chain.getPoint() + " " + e.getMessage());
		// e.printStackTrace();

		synchronized (this) {
			this.notify();
		}
	}
}