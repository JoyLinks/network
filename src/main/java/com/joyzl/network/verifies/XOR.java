package com.joyzl.network.verifies;

/**
 * XOR（异或）
 * 
 * @author ZhangXi 2026年7月16日
 */
public final class XOR extends Verifier {

	private final byte seed;

	public XOR(byte seed) {
		if (seed == 0) {
			throw new IllegalArgumentException("种子不能为零");
		}
		this.seed = seed;
	}

	@Override
	public byte check(byte value) {
		return (byte) (value ^ seed);
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public void reset() {
	}
}