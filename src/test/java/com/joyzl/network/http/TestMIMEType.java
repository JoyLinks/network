package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestMIMEType {

	@Test
	void test() {
		final String mime = MIMEType.getByFilename("PEF１１２Ｃ０２－1130：05-06-20-251119282 20251122 0307..csv");
		assertEquals(MIMEType.TEXT_CSV, mime);
	}

}