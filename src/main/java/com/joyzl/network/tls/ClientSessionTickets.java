/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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
	private final static Map<String, Queue<NewSessionTicket1>> M1 = new ConcurrentHashMap<>();
	private final static Map<String, Queue<NewSessionTicket2>> M2 = new ConcurrentHashMap<>();

	/**
	 * 缓存 NewSessionTicket1
	 */
	public static void put(String sni, NewSessionTicket1 newSessionTicket) {
		Queue<NewSessionTicket1> q = M1.get(sni);
		if (q == null) {
			q = new ConcurrentLinkedQueue<>();
			M1.put(sni, q);
		}
		q.add(newSessionTicket);
	}

	/**
	 * 缓存 NewSessionTicket2
	 */
	public static void put(String sni, NewSessionTicket2 newSessionTicket) {
		Queue<NewSessionTicket2> q = M2.get(sni);
		if (q == null) {
			q = new ConcurrentLinkedQueue<>();
			M2.put(sni, q);
		}
		q.add(newSessionTicket);
	}

	/**
	 * 取出 NewSessionTicket1
	 */
	public static NewSessionTicket1 get1(String sni) {
		final Queue<NewSessionTicket1> q = M1.get(sni);
		if (q != null) {
			NewSessionTicket1 t;
			do {
				t = q.poll();
			} while (t != null && !t.valid());
			return t;
		}
		return null;
	}

	/**
	 * 取出 NewSessionTicket2
	 */
	public static NewSessionTicket2 get2(String sni) {
		final Queue<NewSessionTicket2> q = M2.get(sni);
		if (q != null) {
			NewSessionTicket2 t;
			do {
				t = q.poll();
			} while (t != null && !t.valid());
			return t;
		}
		return null;
	}
}