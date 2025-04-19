package com.joyzl.network.http;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.joyzl.network.http.HPACK.Item;

public class TestHPACK {

	@Test
	void testStaticTable() {
		final HPACK hpack = new HPACK();

		assertNull(hpack.getName(0));
		assertNull(hpack.getValue(0));

		Item item;
		for (int index = 0; index < HPACK.STATIC_TABLE_SIZE; index++) {
			item = HPACK.STATIC_TABLE[index];
			assertEquals(hpack.getName(index), item.name);
			assertEquals(hpack.getValue(index), item.value);
		}

		assertEquals(hpack.findName(HPACK.AUTHORITY), 1);
		assertEquals(hpack.findValue(1, null), 1);

		assertEquals(hpack.findName(HPACK.METHOD), 2);
		assertEquals(hpack.findValue(2, HTTP.POST), 3);

		assertEquals(hpack.findName(HPACK.PATH), 4);
		assertEquals(hpack.findValue(4, "/"), 4);

		assertEquals(hpack.findName(HPACK.SCHEME), 6);
		assertEquals(hpack.findValue(7, "https"), 7);

		assertEquals(hpack.findName(HPACK.STATUS), 8);
		assertEquals(hpack.findValue(8, "500"), 14);

		assertEquals(hpack.findName(HPACK.Accept_Encoding), 16);
		assertEquals(hpack.findValue(16, "gzip, deflate"), 16);

	}

	@Test
	void testDynamic() {
		final HPACK hpack = new HPACK();

		hpack.add("cache-control", "a");
		// [a62]

		assertEquals(hpack.getName(62), "cache-control");
		assertEquals(hpack.getValue(62), "a");

		assertEquals(hpack.findName("cache-control"), 62);
		assertEquals(hpack.findValue(62, "a"), 62);

		hpack.add("cache-control", "b");
		hpack.add("cache-control", "c");
		// [c62 b63]

		assertEquals(hpack.getName(62), "cache-control");
		assertEquals(hpack.getName(63), "cache-control");
		assertEquals(hpack.getValue(62), "c");
		assertEquals(hpack.getValue(63), "b");

		assertEquals(hpack.findName("cache-control"), 62);
		assertEquals(hpack.findValue(62, "c"), 62);
		assertEquals(hpack.findValue(62, "b"), 63);
		assertEquals(hpack.findValue(63, "c"), 0);
		assertEquals(hpack.findValue(63, "b"), 63);

		hpack.add("Accept_Charset", null);
		// [n62 c63 b64 a65]

		assertEquals(hpack.getName(62), "Accept_Charset");
		assertEquals(hpack.getName(63), "cache-control");
		assertEquals(hpack.getValue(62), null);
		assertEquals(hpack.getValue(63), "c");

		assertEquals(hpack.findName("Accept_Charset"), 62);
		assertEquals(hpack.findValue(62, null), 62);
	}
}