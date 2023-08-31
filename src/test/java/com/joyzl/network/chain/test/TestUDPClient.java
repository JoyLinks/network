package com.joyzl.network.chain.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.joyzl.network.Executor;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.UDPClient;
import com.joyzl.network.chain.UDPServer;

class TestUDPClient implements ChainHandler<Message> {

	static UDPServer<Message> server;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Executor.initialize(0);
		server = new UDPServer<>(new ServerHandler(), null, 1000);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		server.close();
		Executor.shutdown();
	}

	@Test
	@Disabled
	void test1() throws Exception {
		final TestUDPClient handler = new TestUDPClient();
		final UDPClient<Message> link = new UDPClient<>(handler, "", 0);
		// FIRST
		link.connect();
		// 127.0.0.1:0 Can't connect to port 0
		assertFalse(link.active());

		// SECOND
		link.connect();
		assertFalse(link.active());

		// CLOSE
		link.close();
	}

	@Test
	@Disabled
	void test2() throws Exception {
		final TestUDPClient handler = new TestUDPClient();
		final UDPClient<Message> link = new UDPClient<>(handler, "", 1024);
		// FIRST
		link.connect();
		assertTrue(link.active());

		// SECOND
		link.connect();
		assertTrue(link.active());

		// CLOSE
		link.close();
	}

	@Test
	@Disabled
	void test3() throws Exception {
		final TestUDPClient handler = new TestUDPClient();
		final UDPClient<Message> link = new UDPClient<>(handler, "192.168.0.2", 1024);
		// FIRST
		link.connect();
		assertTrue(link.active());

		// SECOND
		link.connect();
		assertTrue(link.active());

		// CLOSE
		link.close();
	}

	@Test
	void test4() throws Exception {
		final TestUDPClient handler = new TestUDPClient();
		final UDPClient<Message> link = new UDPClient<>(handler, "127.0.0.1", 1000);
		// FIRST
		link.connect();
		synchronized (handler) {
			handler.wait();
		}
		link.close();
	}

	////////////////////////////////////////////////////////////////////////////////

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		System.out.println("LINK:CONNECTED");
		assertTrue(chain.active());

		// 构造消息
		final Message message = new Message();
		final byte[] bytes = new byte[512];
		for (int index = 0; index < bytes.length; index++) {
			bytes[index] = (byte) index;
		}
		message.setLength(bytes.length);
		message.setBytes(bytes);
		message.setId(1);
		chain.send(message);
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
		System.out.println("LINK:SENT");
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
			for (int index = 0; index < message.getLength(); index++) {
				assertEquals(message.getBytes()[index], (byte) index);
			}
			if (message.getId() < 10) {
				message.setId(message.getId() + 1);
				chain.send(message);
			} else {
				chain.close();
				return;
			}
		}
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
		System.out.println("LINK:DISCONNECTED");
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		System.out.println("LINK:ERROR");
		System.out.println(chain.getPoint() + " " + e.getMessage());
		e.printStackTrace();

		synchronized (this) {
			this.notify();
		}
	}
}