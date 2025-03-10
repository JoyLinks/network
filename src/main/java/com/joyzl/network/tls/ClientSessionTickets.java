package com.joyzl.network.tls;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 会话票据缓存
 * 
 * @author ZhangXi 2025年2月18日
 */
public class ClientSessionTickets {

	private final static Map<Key, Queue<NewSessionTicket>> M = new ConcurrentHashMap<>();

	public static void put(String sni, short suite, NewSessionTicket newSessionTicket) {
		final Key key = new Key(sni, suite);
		Queue<NewSessionTicket> q = M.get(key);
		if (q == null) {
			q = new ConcurrentLinkedQueue<>();
			M.put(key, q);
		}
		q.add(newSessionTicket);
	}

	public static NewSessionTicket get(String sni, short suite) {
		final Key key = new Key(sni, suite);
		final Queue<NewSessionTicket> q = M.get(key);
		if (q != null) {
			NewSessionTicket t;
			do {
				t = q.poll();
			} while (t != null && !t.valid());
			return t;
		}
		return null;
	}

	/**
	 * 由SNI(ServerName)+GROUP(NamedGroup)+SUITE(CipherSuite)组成的键
	 * 
	 * @author ZhangXi 2025年2月25日
	 */
	static class Key {
		/** ServerName */
		private final String sni;
		/** CipherSuite */
		private final short suite;

		public Key(String sni, short suite) {
			this.suite = suite;
			this.sni = sni;
		}

		@Override
		public String toString() {
			return sni + " " + CipherSuite.named(suite);
		}

		@Override
		public final boolean equals(Object o) {
			if (o instanceof Key that) {
				if (suite == that.suite) {
					if (sni.equals(that.sni)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public final int hashCode() {
			return sni.hashCode() + suite;
		}
	}
}