package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <pre>
 * opaque ProtocolName<1..2^8-1>;
 * 
 * struct {
 *       ProtocolName protocol_name_list<2..2^16-1>
 * } ProtocolNameList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class ApplicationLayerProtocolNegotiation extends Extension {

	public final static String HTTP_0_9 = "http/0.9";
	public final static String HTTP_1_0 = "http/1.0";
	public final static String HTTP_1_1 = "http/1.1";
	public final static String SPDY_1 = "spdy/1";
	public final static String SPDY_2 = "spdy/2";
	public final static String SPDY_3 = "spdy/3";
	/** Traversal Using Relays around NAT (TURN) */
	public final static String STUN_TURN = "stun.turn";
	/** NAT discovery using Session Traversal Utilities for NAT (STUN) */
	public final static String STUN_TURN_DISCOVERY = "stun.nat-discovery";
	/** HTTP/2 over TLS */
	public final static String H2 = "h2";
	/** HTTP/2 over TCP */
	public final static String H2C = "h2c";
	/** WebRTC Media and Data */
	public final static String WEBRTC = "webrtc";
	/** Confidential WebRTC Media and Data */
	public final static String C_WEBRTC = "c-webrtc";
	/** Confidential WebRTC Media and Data */
	public final static String FTP = "ftp";
	public final static String IMAP = "imap";
	public final static String POP3 = "pop3";
	public final static String MANAGE_SIEVE = "managesieve";
	public final static String XMPP_CLIENT = "xmpp-client";
	public final static String XMPP_SERVER = "xmpp-server";

	////////////////////////////////////////////////////////////////////////////////

	private byte[][] items = TLS.EMPTY_STRINGS;

	public ApplicationLayerProtocolNegotiation() {
	}

	public ApplicationLayerProtocolNegotiation(byte[]... value) {
		set(value);
	}

	public ApplicationLayerProtocolNegotiation(String... value) {
		set(value);
	}

	@Override
	public short type() {
		return APPLICATION_LAYER_PROTOCOL_NEGOTIATION;
	}

	public byte[][] get() {
		return items;
	}

	public byte[] get(int index) {
		return items[index];
	}

	public String getString(int index) {
		return new String(items[index], StandardCharsets.US_ASCII);
	}

	public void set(byte[]... value) {
		if (value == null) {
			items = TLS.EMPTY_STRINGS;
		} else {
			items = value;
		}
	}

	public void set(String... value) {
		if (value == null) {
			items = TLS.EMPTY_STRINGS;
		} else {
			items = new byte[value.length][];
			for (int index = 0; index < items.length; index++) {
				items[index] = value[index].getBytes(StandardCharsets.US_ASCII);
			}
		}
	}

	public void add(byte[] value) {
		if (items == TLS.EMPTY_STRINGS) {
			items = new byte[][] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public void add(String value) {
		if (items == TLS.EMPTY_STRINGS) {
			items = new byte[][] { value.getBytes(StandardCharsets.US_ASCII) };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value.getBytes(StandardCharsets.US_ASCII);
		}
	}

	public int size() {
		return items.length;
	}

	@Override
	public String toString() {
		if (size() > 0) {
			final StringBuilder b = new StringBuilder();
			b.append(name());
			if (size() > 0) {
				b.append(':');
				for (int index = 0; index < size(); index++) {
					if (index > 0) {
						b.append(',');
					}
					b.append(getString(index));
				}
			}
			return b.toString();
		} else {
			return name() + ":EMPTY";
		}
	}

	/**
	 * 匹配选择，移除扩展中其余协议名称
	 */
	public boolean select(byte[][] others) {
		for (int i = 0; i < items.length; i++) {
			for (int s = 0; s < others.length; s++) {
				if (Arrays.equals(items[i], others[s])) {
					items = new byte[][] { items[i] };
					return true;
				}
			}
		}
		return false;
	}
}