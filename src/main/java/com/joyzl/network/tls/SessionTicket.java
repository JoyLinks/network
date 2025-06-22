/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * RFC 5077
 * 
 * <pre>
 * 旧格式
 * 00 23          Ticket Extension type 35
 * 01 02          Length of extension contents
 * 01 00          Length of ticket
 * FF FF .. ..    Actual ticket
 * 新格式
 * 00 23          Extension type 35
 * 01 00          Length of extension contents (ticket)
 * FF FF .. ..    Actual ticket
 * </pre>
 * 
 * @author ZhangXi 2025年3月5日
 */
class SessionTicket extends Extension {

	private byte[] ticket = TLS.EMPTY_BYTES;

	@Override
	public short type() {
		return SESSION_TICKET;
	}

	public boolean hasTicket() {
		return ticket.length > 0;
	}

	public byte[] getTicket() {
		return ticket;
	}

	public void setTicket(byte[] value) {
		if (value == null) {
			ticket = TLS.EMPTY_BYTES;
		} else {
			ticket = value;
		}
	}

	@Override
	public String toString() {
		return name() + ":" + ticket.length;
	}
}