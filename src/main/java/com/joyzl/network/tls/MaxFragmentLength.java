package com.joyzl.network.tls;

/**
 * <pre>
 * enum{
 *        2^9(1), 2^10(2), 2^11(3), 2^12(4), (255)
 * } MaxFragmentType;
 * </pre>
 * 
 * @author ZhangXi 2024年12月18日
 */
public class MaxFragmentLength extends Extension {

	private final MaxFragmentType type;

	public MaxFragmentLength(MaxFragmentType type) {
		this.type = type;
	}

	@Override
	public ExtensionType type() {
		return ExtensionType.MAX_FRAGMENT_LENGTH;
	}

	public MaxFragmentType getType() {
		return type;
	}

	public int getMaxFragment() {
		if (type == MaxFragmentType.MAX_4096) {
			return 4096;
		}
		if (type == MaxFragmentType.MAX_2048) {
			return 2048;
		}
		if (type == MaxFragmentType.MAX_1024) {
			return 1024;
		}
		if (type == MaxFragmentType.MAX_512) {
			return 512;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "max_fragment_length:" + getMaxFragment();
	}
}