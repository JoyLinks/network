/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：预共享密钥，表明支持的对称密钥标识
 * 
 * <pre>
 * RFC 8446
 * 
 * struct {
 *     opaque identity<1..2^16-1>;
 *     uint32 obfuscated_ticket_age;
 * } PskIdentity;
 * 
 * opaque PskBinderEntry<32..255>;
 * 
 * struct {
 *     PskIdentity identities<7..2^16-1>;
 *     PskBinderEntry binders<33..2^16-1>;
 * } OfferedPsks;
 * 
 * struct {
 *     select (Handshake.msg_type) {
 *        case client_hello: OfferedPsks;
 *        case server_hello: uint16 selected_identity_index;
 *     };
 * } PreSharedKeyExtension;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
abstract class PreSharedKey extends Extension {

	@Override
	public short type() {
		return PRE_SHARED_KEY;
	}

	static class PskIdentity {

		/** ticket */
		private byte[] identity = TLS.EMPTY_BYTES;
		/** obfuscated */
		private int ticket_age = 0;
		/** binder */
		private byte[] binder = TLS.EMPTY_BYTES;

		public PskIdentity() {
		}

		public PskIdentity(int ticket_age, byte[] identity) {
			setTicketAge(ticket_age);
			setIdentity(identity);
		}

		public int getTicketAge() {
			return ticket_age;
		}

		public void setTicketAge(int value) {
			ticket_age = value;
		}

		public byte[] getIdentity() {
			return identity;
		}

		public void setIdentity(byte[] value) {
			if (value == null) {
				identity = TLS.EMPTY_BYTES;
			} else {
				identity = value;
			}
		}

		public byte[] getBinder() {
			return binder;
		}

		public void setBinder(byte[] value) {
			if (value == null) {
				binder = TLS.EMPTY_BYTES;
			} else {
				binder = value;
			}
		}

		public boolean check(byte[] key) {
			if (binder != key) {
				return Arrays.equals(binder, key);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(identity);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof PskIdentity p) {
				return Arrays.equals(identity, p.identity);
			}
			return false;
		}

		@Override
		public String toString() {
			return "ticket_age=" + ticket_age + " identity=" + identity.length + "byte";
		}
	}
}