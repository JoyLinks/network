package com.joyzl.network.ftp;

import com.joyzl.network.chain.TCPLink;

/**
 * 文件列表数据客户端
 * 
 * @author ZhangXi 2024年7月11日
 */
public class ListClient extends TCPLink<FTPMessage> {

	private final LIST list;

	public ListClient(LIST cmd, String host, int port) {
		super(ListUNIXHandler.INSTANCES, host, port);
		list = cmd;
	}

	public LIST getLIST() {
		return list;
	}

	/**
	 * 接收文件列表数据
	 * 
	 * @param cmd 命令，必须是LIST
	 * @param host 被动主机
	 * @param port 被动端口
	 */
	final static void receive(LIST cmd, String host, int port) {
		final ListClient transfer = new ListClient(cmd, host, port);
		transfer.connect();
	}
}