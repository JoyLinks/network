package com.joyzl.network.tls;

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
public abstract class PreSharedKey extends Extension {

	@Override
	public short type() {
		return PRE_SHARED_KEY;
	}
}