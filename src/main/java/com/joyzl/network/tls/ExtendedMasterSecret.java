package com.joyzl.network.tls;

/**
 * <pre>
 * { EMPTY }
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class ExtendedMasterSecret extends Extension {

	public final static ExtendedMasterSecret INSTANCE = new ExtendedMasterSecret();

	@Override
	public short type() {
		return EXTENDED_MASTER_SECRET;
	}
}