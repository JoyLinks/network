package com.joyzl.network.ftp;

/**
 * FTP Client
 * 
 * @author ZhangXi 2024年7月8日
 */
public class FTPClient extends FTPClientHandler {

	private FTPLink link;

	public boolean isActive() {
		return link != null && link.active();
	}

	public void connect(String host, int port) {
		link = new FTPLink(this, host, port);
		link.connect();
	}

	public void invoke(FTPMessage command) {
		link.send(command);
	}
}