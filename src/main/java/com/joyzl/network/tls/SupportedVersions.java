package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 *
 * <pre>
 * struct {
 *     select (Handshake.msg_type) {
 *      case client_hello:
 *        ProtocolVersion versions<2..254>;
 * 
 *      case server_hello: (and HelloRetryRequest)
 *        ProtocolVersion selected_version;
 *     }
 * } SupportedVersions
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class SupportedVersions extends Extension {

	private short[] versions = TLS.EMPTY_SHORTS;

	public SupportedVersions() {
	}

	public SupportedVersions(short... versions) {
		this.versions = versions;
	}

	@Override
	public short type() {
		return SUPPORTED_VERSIONS;
	}

	public short[] get() {
		return versions;
	}

	public short get(int index) {
		return versions[index];
	}

	public void set(short... value) {
		if (value == null) {
			versions = TLS.EMPTY_SHORTS;
		} else {
			versions = value;
		}
	}

	public void add(short value) {
		if (versions == TLS.EMPTY_SHORTS) {
			versions = new short[] { value };
		} else {
			versions = Arrays.copyOf(versions, versions.length + 1);
			versions[versions.length - 1] = value;
		}
	}

	public int size() {
		return versions.length;
	}

	@Override
	public String toString() {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("supported_versions:");
		if (versions != null && versions.length > 0) {
			for (int index = 0; index < versions.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(Short.toString(versions[index]));
			}
		}
		return builder.toString();
	}

	/**
	 * 选择版本，匹配成功之后内部数组将缩减为仅包含选择项
	 */
	public short select(short[] others) {
		for (int i = 0; i < versions.length; i++) {
			for (int s = 0; s < others.length; s++) {
				if (versions[i] == others[s]) {
					versions = new short[] { others[s] };
					return others[s];
				}
			}
		}
		return 0;
	}

	/**
	 * 匹配版本
	 */
	public short match(short[] others) {
		for (int i = 0; i < versions.length; i++) {
			for (int s = 0; s < others.length; s++) {
				if (versions[i] == others[s]) {
					return others[s];
				}
			}
		}
		return 0;
	}
}