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

	private final static Map<PskIdentity, NewSessionTicket> TICKETS = new ConcurrentHashMap<>();

	public static NewSessionTicket get(PskIdentity identity) {
		return TICKETS.get(identity);
	}

	/**
	 * 构造新的票据
	 */
	public static NewSessionTicket make(byte nonce) {
		final NewSessionTicket ticket = new NewSessionTicket();
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