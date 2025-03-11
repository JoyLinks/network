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

	/** SNI,Queue */
	private final static Map<String, Queue<NewSessionTicket>> M = new ConcurrentHashMap<>();

	/**
	 * 缓存 NewSessionTicket
	 */
	public static void put(String sni, NewSessionTicket newSessionTicket) {
		Queue<NewSessionTicket> q = M.get(sni);
		if (q == null) {
			q = new ConcurrentLinkedQueue<>();
			M.put(sni, q);
		}
		q.add(newSessionTicket);
	}

	/**
	 * 取出 NewSessionTicket
	 */
	public static NewSessionTicket get(String sni) {
		final Queue<NewSessionTicket> q = M.get(sni);
		if (q != null) {
			NewSessionTicket t;
			do {
				t = q.poll();
			} while (t != null && !t.valid());
			return t;
		}
		return null;
	}
}