package com.joyzl.network.ftp;

/**
 * 数据端口
 * 
 * @author ZhangXi 2024年7月8日
 */
public class PORT extends FTPMessage {

	private String host;
	private int port1, port2;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.PORT;
	}

	@Override
	protected String getParameter() {
		// 127,0,0,1,196,146
		String host_port = host.replace('.', ',');
		host_port += ',' + port1 + ',' + port2;
		return host_port;
	}

	@Override
	protected void setParameter(String value) {
		// 127,0,0,1,196,146

		// PORT2
		int p2 = value.lastIndexOf(',');
		port2 = Integer.parseInt(value, p2 + 1, value.length(), 10);
		// PORT1
		int p1 = value.lastIndexOf(',', p2 - 1);
		port1 = Integer.parseInt(value, p1 + 1, p2, 10);
		// HOST
		host = value.substring(0, p1);
		host = host.replace(',', '.');
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

	public int getPort1() {
		return port1;
	}

	public void setPort1(int value) {
		port1 = value;
	}

	public int getPort2() {
		return port2;
	}

	public void setPort2(int value) {
		port2 = value;
	}
}