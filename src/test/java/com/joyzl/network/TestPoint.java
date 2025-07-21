package com.joyzl.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

/**
 * 测试接点表示
 * 
 * @author ZhangXi 2025年7月21日
 */
class TestPoint {

	@Test
	void testGetHost() {
		assertEquals(Point.getHost("[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:8000"), "[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]");
		assertEquals(Point.getHost("[5135:2419:8210:3011::5000]:8000"), "[5135:2419:8210:3011::5000]");
		assertEquals(Point.getHost("192.168.0.1:8000"), "192.168.0.1");
		assertEquals(Point.getHost("www.joyzl.net:1030"), "www.joyzl.net");
		assertEquals(Point.getHost("Localhost:8000"), "Localhost");

		assertEquals(Point.getHost("COM3:9600.8.1.0"), "COM3");
		assertEquals(Point.getHost("/dev/ttyS0:19200.8.1.1"), "/dev/ttyS0");
	}

	@Test
	void testGetPort() {
		assertEquals(Point.getPort("[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:8000"), 8000);
		assertEquals(Point.getPort("192.168.0.1:8000"), 8000);
		assertEquals(Point.getPort("www.joyzl.net:1030"), 1030);
		assertEquals(Point.getPort("Localhost:8000"), 8000);

		assertEquals(Point.getPort("COM3:9600.8.1.0"), 9600);
		assertEquals(Point.getPort("/dev/ttyS0:19200.8.1.1"), 19200);
	}

	@Test
	void testSerialPort() {
		assertEquals(Point.getBaudRate("Localhost:8000"), 0);
		assertEquals(Point.getBaudRate("COM3:9600.8.1.0"), 9600);
		assertEquals(Point.getBaudRate("/dev/ttyS0:19200.8.1.1"), 19200);

		assertEquals(Point.getDataBits("Localhost:8000"), 0);
		assertEquals(Point.getDataBits("COM3:9600.8.1.0"), 8);
		assertEquals(Point.getDataBits("/dev/ttyS0:19200.8.1.1"), 8);

		assertEquals(Point.getStopBits("Localhost:8000"), 0);
		assertEquals(Point.getStopBits("COM3:9600.8.1.0"), 1);
		assertEquals(Point.getStopBits("/dev/ttyS0:19200.8.1.1"), 1);

		assertEquals(Point.getParity("Localhost:8000"), 0);
		assertEquals(Point.getParity("COM3:9600.8.1.0"), 0);
		assertEquals(Point.getParity("/dev/ttyS0:19200.8.1.1"), 1);
	}

	@Test
	void testGetPoint() throws Exception {
		assertEquals(Point.getPoint(8000), "8000");
		assertEquals(Point.getPoint("localhost", 8000), "localhost:8000");
		assertEquals(Point.getPoint(new byte[] { 10, 10, 10, 1 }, 8000), "10.10.10.1:8000");
		assertEquals(Point.getPoint("COM3", 9600, 8, 1, 1), "COM3:9600.8.1.1");

		assertEquals(Point.getPoint(InetAddress.getLoopbackAddress(), 8000), "127.0.0.1:8000");

		InetSocketAddress a = new InetSocketAddress("127.0.0.1", 80);
		assertEquals(Point.getPoint(a), "127.0.0.1:80");
	}
}