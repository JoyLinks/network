package com.joyzl.network.ftp;

import com.joyzl.network.chain.TCPLink;

/**
 * FTP链路
 * 
 * @author ZhangXi 2024年7月5日
 */
public class FTPLink extends TCPLink<FTPMessage> {

	public FTPLink(FTPClientHandler handler, String host, int port) {
		super(handler, host, port);
	}

	private FTPMessage current;

	public FTPMessage getCurrent() {
		return current;
	}

	public void setCurrent(FTPMessage current) {
		this.current = current;
	}
}
