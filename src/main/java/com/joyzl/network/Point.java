/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.regex.Pattern;

/**
 * 通信接点
 * <p>
 * IP接点格式[IP/HOST:PORT],"[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:8000"/"192.168.0.1:8000"/"www.joyzl.net:1030"/"Localhost:8000"
 * </p>
 * 
 * <p>
 * 串口接点格式[NAME:BAUD.DATA.STOP.PARITY],"COM3:9600.8.1.0"/"/dev/ttyS0:19200.8.1.1"<br>
 * 串口值定义<br>
 * 波特率(baud_rate):110,300,600,1200,4800,9600,14400,19200,38400,57600,115200,128000,256000<br>
 * 数据位(data bits):8,7,6,5<br>
 * 停止位(stop bits):1 ONE,2 ONE_FIVE,3 TWO<br>
 * 校验位(parity): 0 NONE,1 ODD,2 EVEN,3 MARK,4 SPACE<br>
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public final class Point {

	final static char SP = ':';
	final static char DOT = '.';
	final static String DOTS = ".";

	/**
	 * IP接点格式 "[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:8000" /
	 * "192.168.0.1:8000" / "www.joyzl.net:1030" / "Localhost:8000"
	 */
	public final static Pattern PATTERN_IP_CLIENT = Pattern.compile("[\\w,.,:,[,]]+:\\d+");
	/** IP接点格式 "8000" */
	public final static Pattern PATTERN_IP_SERVER = Pattern.compile("\\d+");
	/** 串口接点格式 "COM3:9600.8.1.0" / "/dev/ttyS0:19200.8.1.1" */
	public final static Pattern PATTERN_SERIAL_PORT = Pattern.compile("[\\w,//]+:\\d{3,6}.\\d{1}.\\d{1}.\\d{1}");

	public final static boolean isSerialPort(String point) {
		return PATTERN_SERIAL_PORT.matcher(point).matches();
	}

	public final static boolean isIPClient(String point) {
		return PATTERN_IP_CLIENT.matcher(point).matches();
	}

	public final static boolean isIPServer(String point) {
		return PATTERN_IP_SERVER.matcher(point).matches();
	}

	/**
	 * 获取指定接点的唯一标识
	 * 
	 * @param point
	 */
	public final static long getID(String point) {
		// IPv4 255.255.255.255:65535, 4Byte+2Byte
		// IPv6 [ABCD:EF01:2345:6789:ABCD:EF01:2345:6789]:65535, 16Byte+2Byte
		// Server 65535
		// COM 不支持

		return 0;
	}

	/**
	 * 获取主机
	 * 
	 * @param point
	 * @return 如果是客户端节点返回IP/主机名,如果是串口返回串口名,如果是未指定IP的服务端返回null
	 */
	public final static String getHost(String point) {
		int index = point.lastIndexOf(SP);
		if (index >= 0) {
			return point.substring(0, index);
		}
		return null;
	}

	/**
	 * 获取主机
	 * 
	 * @param SocketAddress
	 * @return 返回IP/主机名
	 */
	public final static String getHost(SocketAddress address) {
		if (address instanceof InetSocketAddress) {
			return ((InetSocketAddress) address).getHostString();
		}
		throw new UnsupportedAddressTypeException();
	}

	/**
	 * 获取端口
	 * 
	 * @param point
	 * @return 如果是客户端/服务端返回端口号,如果是串口返回波特率
	 */
	public final static int getPort(String point) {
		int index = point.lastIndexOf(SP);
		if (index > 0) {
			point = point.substring(index + 1);
			// 串口可能存在多个分段
			index = point.indexOf(DOT);
			if (index > 0) {
				point = point.substring(0, index);
			}
		}
		try {
			return Integer.parseInt(point);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * 获取串口接点波特率
	 * 
	 * @param point
	 * @return 如果接点不是串口或未指定则返回0
	 */
	public final static int getBaudRate(String point) {
		int index = point.lastIndexOf(SP);
		if (index > 0) {
			point = point.substring(index + 1);
			String[] items = point.split(DOTS);
			if (items.length == 4) {
				try {
					return Integer.parseInt(items[0]);
				} catch (NumberFormatException e) {
					return 0;
				}
			}
		}
		return 0;
	}

	/**
	 * 获取串口接点数据位
	 * 
	 * @param point
	 * @return 如果接点不是串口或未指定则返回0
	 */
	public final static int getDataBits(String point) {
		int index = point.lastIndexOf(SP);
		if (index > 0) {
			point = point.substring(index + 1);
			String[] items = point.split(DOTS);
			if (items.length == 4) {
				try {
					return Integer.parseInt(items[1]);
				} catch (NumberFormatException e) {
					return 0;
				}
			}
		}
		return 0;
	}

	/**
	 * 获取串口接点停止位
	 * 
	 * @param point
	 * @return 如果接点不是串口或未指定则返回0
	 */
	public final static int getStopBits(String point) {
		int index = point.lastIndexOf(SP);
		if (index > 0) {
			point = point.substring(index + 1);
			String[] items = point.split(DOTS);
			if (items.length == 4) {
				try {
					return Integer.parseInt(items[2]);
				} catch (NumberFormatException e) {
					return 0;
				}
			}
		}
		return 0;
	}

	/**
	 * 获取串口接点校验位
	 * 
	 * @param point
	 * @return 如果接点不是串口或未指定则返回0
	 */
	public final static int getParity(String point) {
		int index = point.lastIndexOf(SP);
		if (index > 0) {
			point = point.substring(index + 1);
			String[] items = point.split(DOTS);
			if (items.length == 4) {
				try {
					return Integer.parseInt(items[3]);
				} catch (NumberFormatException e) {
					return 0;
				}
			}
		}
		return 0;
	}

	public final static String getPoint(byte[] ip, int port) throws UnknownHostException {
		final InetAddress address = InetAddress.getByAddress(ip);
		return address.getHostAddress() + SP + port;
	}

	public final static String getPoint(InetAddress address, int port) {
		return address.getHostAddress() + SP + port;
	}

	public final static String getPoint(SocketAddress address) {
		if (address instanceof InetSocketAddress) {
			return getPoint((InetSocketAddress) address);
		}
		throw new UnsupportedAddressTypeException();
	}

	public final static String getPoint(InetSocketAddress address) {
		return address.getAddress().getHostAddress() + SP + address.getPort();
	}

	public final static String getPoint(String port, int baudrates, int databits, int stopbits, int parities) {
		return port + SP + baudrates + DOT + databits + DOT + stopbits + DOT + parities;
	}

	public final static String getPoint(String host, int port) {
		if (host == null || host.length() == 0) {
			return Integer.toString(port);
		}
		return host + SP + port;
	}

	public final static String getPoint(int port) {
		return Integer.toString(port);
	}
}