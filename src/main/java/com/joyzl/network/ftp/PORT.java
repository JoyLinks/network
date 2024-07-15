package com.joyzl.network.ftp;

/**
 * 数据端口
 * 
 * @author ZhangXi 2024年7月8日
 */
public class PORT extends FTPMessage {

	private String host;
	private int port;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.PORT;
	}

	@Override
	protected String getParameter() {
		// 127,0,0,1,196,146
		String host_port = host.replace('.', ',');
		host_port += ',' + (port >>> 8);
		host_port += ',' + (port & 0x0F);
		return host_port;
	}

	@Override
	protected void setParameter(String value) {
		// 127,0,0,1,196,146

		// PORT2
		int p2 = value.lastIndexOf(',');
		// PORT1
		int p1 = value.lastIndexOf(',', p2 - 1);
		// HOST
		host = value.substring(0, p1);
		host = host.replace(',', '.');

		p1 = Integer.parseInt(value, p1 + 1, p2, 10);
		p2 = Integer.parseInt(value, p2 + 1, value.length(), 10);
		port = (p1 << 8) | p2;
	}

	@Override
	protected boolean isSuccess() {
		// 200
		// 500, 501, 421, 530
		return getCode() == 200;
	}

	@Override
	protected void finish() {
	}

	public String getHost() {
		return host;
	}

	public void setHost(String value) {
		host = value;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int value) {
		port = value;
	}
}