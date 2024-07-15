package com.joyzl.network.ftp;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;

import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.TCPLink;

/**
 * 文件传输客户端
 * 
 * @author ZhangXi 2024年7月12日
 */
public class FileClient extends TCPLink<FileMessage> {

	private FileMessage command;
	private FileChannel channel;

	public FileClient(ChainHandler<FileMessage> handler, String host, int port) {
		super(handler, host, port);
	}

	public FileMessage getCommand() {
		return command;
	}

	protected void setCommand(FileMessage value) {
		command = value;
	}

	protected FileChannel getChannel() {
		return channel;
	}

	protected void openChannel(OpenOption... options) throws IOException {
		if (channel == null) {
			channel = FileChannel.open(command.getFile().toPath(), options);
		} else {
			throw new IllegalStateException("不能重复打开文件读写通道");
		}
	}

	protected void closeChannel() throws IOException {
		if (channel != null) {
			channel.close();
			channel = null;
		}
	}

	/**
	 * 发送文件数据
	 * 
	 * @param cmd 必须是 STOR / STOU / APPE
	 * @param host 被动主机
	 * @param port 被动端口
	 */
	final static void send(FileMessage cmd, String host, int port) {
		// STOR STOU APPE
		if (cmd.getCommand() == FTPCommand.STOR || cmd.getCommand() == FTPCommand.STOU || cmd.getCommand() == FTPCommand.APPE) {
			final FileClient client = new FileClient(FileStreamHandler.INSTANCE, host, port);
			client.setCommand(cmd);
			client.connect();
		}
	}

	/**
	 * 接收文件数据
	 * 
	 * @param cmd 必须是 RETR
	 * @param host 被动主机
	 * @param port 被动端口
	 */
	final static void receive(RETR cmd, String host, int port) {
		final FileClient client = new FileClient(FileStreamHandler.INSTANCE, host, port);
		client.setCommand(cmd);
		client.connect();
	}
}