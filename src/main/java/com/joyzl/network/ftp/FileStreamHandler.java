package com.joyzl.network.ftp;

import java.nio.channels.FileChannel;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;

/**
 * 文件流传输
 * 
 * @author ZhangXi 2024年7月13日
 */
public class FileStreamHandler extends FileHandler {

	final static FileStreamHandler INSTANCE = new FileStreamHandler();

	// decode() received() 用于接收文件数据

	@Override
	public FileMessage decode(ChainChannel<FileMessage> chain, DataBuffer reader) throws Exception {
		final FileClient client = (FileClient) chain;
		final FileChannel channel = client.getChannel();

		if (reader.readable() > 0) {
			client.getCommand().setTransferred(client.getCommand().getTransferred() + reader.readable());
			reader.read(channel);
		}
		return client.getCommand();
	}

	@Override
	public void received(ChainChannel<FileMessage> chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		if (message == null) {
			message = client.getCommand();
			message.setCode(999);
			message.finish();
		} else {
			chain.receive();
		}
	}

	// encode() sent() 用于发送文件数据

	@Override
	public DataBuffer encode(ChainChannel<FileMessage> chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		final FileChannel channel = client.getChannel();

		final DataBuffer buffer = DataBuffer.instance();
		long length = channel.size() - channel.position();
		if (length > BUFFER_SIZE) {
			length = BUFFER_SIZE;
			length = buffer.write(channel, (int) length);
			message.setTransferred(message.getTransferred() + length);
		} else {
			length = buffer.write(channel, (int) length);
			message.setTransferred(message.getTransferred() + length);
			// CLOSE
			client.closeChannel();
		}
		return buffer;
	}

	@Override
	public void sent(ChainChannel<FileMessage> chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		if (message == null) {
			message = client.getCommand();
			message.setCode(999);
			message.finish();
		} else {
			if (client.getChannel() == null) {
				client.close();
			} else {
				client.send(client.getCommand());
			}
		}
	}
}