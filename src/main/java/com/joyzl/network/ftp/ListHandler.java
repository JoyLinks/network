package com.joyzl.network.ftp;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public abstract class ListHandler implements ChainHandler<FTPMessage> {

	@Override
	public void connected(ChainChannel<FTPMessage> chain) throws Exception {
		chain.receive();
	}

	@Override
	public void received(ChainChannel<FTPMessage> chain, FTPMessage message) throws Exception {
		if (message == null) {
			final ListClient client = (ListClient) chain;
			message = client.getLIST();
			message.setCode(999);
			message.finish();
		} else {
			message.finish();
		}
	}

	@Override
	public DataBuffer encode(ChainChannel<FTPMessage> chain, FTPMessage message) throws Exception {
		return null;
	}

	@Override
	public void sent(ChainChannel<FTPMessage> chain, FTPMessage message) throws Exception {
	}

	@Override
	public void disconnected(ChainChannel<FTPMessage> chain) throws Exception {
		final ListClient client = (ListClient) chain;
		client.getLIST().finish();
		client.close();
	}

	@Override
	public void error(ChainChannel<FTPMessage> chain, Throwable e) {
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