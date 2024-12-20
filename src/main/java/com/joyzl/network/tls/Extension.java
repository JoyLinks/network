package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 5246 TLSv1.2
 * 
 * struct {
 *     ExtensionType extension_type;
 *     opaque extension_data<0..2^16-1>;
 * } Extension;

 * enum {
 *     signature_algorithms(13), (65535)
 * } ExtensionType;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
public abstract class Extension {

	public abstract ExtensionType type();

}