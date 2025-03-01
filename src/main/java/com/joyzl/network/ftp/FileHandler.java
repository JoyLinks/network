package com.joyzl.network.ftp;

import java.nio.file.StandardOpenOption;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public abstract class FileHandler implements ChainHandler {

	/** 每次发送的字节数(MAX_LENGTH=64K) */
	final static int BUFFER_SIZE = 1024 * 64;

	@Override
	public void connected(ChainChannel chain) throws Exception {
		final FileClient client = (FileClient) chain;
		if (client.getCommand().getCommand() == FTPCommand.RETR) {
			// 接收文件数据
			client.openChannel(StandardOpenOption.WRITE);
			chain.receive();
		} else//
		if (client.getCommand().getCommand() == FTPCommand.STOR) {
			// 发送文件数据
			client.openChannel(StandardOpenOption.READ);
			chain.send(client.getCommand());
		} else//
		if (client.getCommand().getCommand() == FTPCommand.STOU) {
			// 发送文件数据
			client.openChannel(StandardOpenOption.READ);
			chain.send(client.getCommand());
		} else//
		if (client.getCommand().getCommand() == FTPCommand.APPE) {
			// 发送文件数据
			client.openChannel(StandardOpenOption.READ);
			chain.send(client.getCommand());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		return encode(chain, (FileMessage) message);
	}

	protected abstract DataBuffer encode(ChainChannel chain, FileMessage message) throws Exception;

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		received(chain, (FileMessage) message);
	}

	protected abstract void received(ChainChannel chain, FileMessage message) throws Exception;

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		sent(chain, (FileMessage) message);
	}

	protected abstract void sent(ChainChannel chain, FileMessage message) throws Exception;

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		final FileClient client = (FileClient) chain;
		client.closeChannel();
		client.close();
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		e.printStackTrace();
	}
}