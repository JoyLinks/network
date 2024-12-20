package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 * <pre>
 * struct {
 *        SignatureScheme supported_signature_algorithms<2..2^16-2>;
 * } SignatureSchemeList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class SignatureAlgorithms extends Extension {

	private final static SignatureScheme[] EMPTY = new SignatureScheme[0];
	private SignatureScheme[] items = EMPTY;

	public SignatureAlgorithms() {
	}

	public SignatureAlgorithms(SignatureScheme... value) {
		set(value);
	}

	@Override
	public ExtensionType type() {
		return ExtensionType.SIGNATURE_ALGORITHMS;
	}

	public SignatureScheme[] get() {
		return items;
	}

	public SignatureScheme get(int index) {
		return items[index];
	}

	public void set(SignatureScheme... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(SignatureScheme value) {
		if (items == EMPTY) {
			items = new SignatureScheme[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}

	@Override
	public String toString() {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("signature_algorithms:");
		for (int index = 0; index < size(); index++) {
			if (index > 0) {
				builder.append(',');
			}
			builder.append(get(index).toString());
		}
		return builder.toString();
	}
}