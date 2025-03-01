package com.joyzl.network.ftp;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public abstract class ListHandler implements ChainHandler {

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.receive();
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		FTPMessage command = (FTPMessage) message;
		if (command == null) {
			final ListClient client = (ListClient) chain;
			command = client.getLIST();
			command.setCode(999);
			command.finish();
		} else {
			command.finish();
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		return null;
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		final ListClient client = (ListClient) chain;
		client.getLIST().finish();
		client.close();
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		e.printStackTrace();
	}

	void skipSpaces(DataBuffer reader) throws IOException {
		int c;
		while (reader.readable() > 0) {
			c = reader.get(0);
			if (c == ' ' || c == '\t') {
				reader.readByte();
				continue;
			} else {
				break;
			}
		}
	}

	int readInteger(DataBuffer reader) throws IOException {
		char c;
		int value = 0;
		while (reader.readable() > 0) {
			c = reader.readASCII();
			if (c == ' ' || c == ':' || c == '\t') {
				break;
			}
			value *= 10;
			value += Character.digit(c, 10);
		}
		return value;
	}

	String readString(DataBuffer reader) throws IOException {
		char c;
		final StringBuilder builder = new StringBuilder();
		while (reader.readable() > 0) {
			c = reader.readASCII();
			if (c == ' ' || c == '\t' || c == '\r') {
				break;
			}
			builder.append(c);
		}
		return builder.toString();
	}
}