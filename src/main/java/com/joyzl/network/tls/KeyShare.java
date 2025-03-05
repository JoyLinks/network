package com.joyzl.network.tls;

/**
 * 扩展：共享密钥
 * 
 * <pre>
 * struct {
 *     NamedGroup group;
 *     opaque key_exchange<1..2^16-1>;
 * } KeyShareEntry;
 * 
 * struct {
 *     KeyShareEntry client_shares<0..2^16-1>;
 * } KeyShareClientHello;
 * 
 * struct {
 *     NamedGroup selected_group;
 * } KeyShareHelloRetryRequest;
 * 
 * struct {
 *     KeyShareEntry server_share;
 * } KeyShareServerHello;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
abstract class KeyShare extends Extension {

	@Override
	public short type() {
		return KEY_SHARE;
	}
}