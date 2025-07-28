package com.joyzl.network.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestSessionRefreshable {

	final static Session<String> SESSION = new SessionRefreshable<>(1000);

	@Test
	void test() throws Exception {
		SESSION.set("TOKEN", "VALUE");
		assertEquals(SESSION.get("TOKEN"), "VALUE");

		Thread.sleep(500);
		assertEquals(SESSION.get("TOKEN"), "VALUE");

		Thread.sleep(1000);
		assertEquals(SESSION.get("TOKEN"), null);
	}
}