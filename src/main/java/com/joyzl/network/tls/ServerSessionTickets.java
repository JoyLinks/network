/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.tls.PreSharedKey.PskIdentity;

/**
 * 服务端发出的恢复会话票据缓存
 * 
 * @author ZhangXi 2025年3月4日
 */
public class ServerSessionTickets {

	private final static Map<PskIdentity, NewSessionTicket2> TICKETS = new ConcurrentHashMap<>();

	public static NewSessionTicket2 get(PskIdentity identity) {
		return TICKETS.get(identity);
	}

	/**
	 * 构造新的票据
	 */
	public static NewSessionTicket2 make(byte nonce) {
		final NewSessionTicket2 ticket = new NewSessionTicket2();
		ticket.setLifetime(172800);
		ticket.setNonce(new byte[] { nonce });
		ticket.setTicket(new byte[256]);
		ticket.setAgeAdd(TLS.RANDOM.nextInt());
		TLS.RANDOM.nextBytes(ticket.getTicket());
		ticket.addExtension(EarlyDataIndication.MAX_EARLY_DATA_SIZE);
		TICKETS.put(new PskIdentity(ticket.getAgeAdd(), ticket.getTicket()), ticket);
		return ticket;
	}
}