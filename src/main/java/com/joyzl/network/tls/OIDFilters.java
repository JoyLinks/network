/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * struct {
 *       opaque certificate_extension_oid<1..2^8-1>;
 *       opaque certificate_extension_values<0..2^16-1>;
 * } OIDFilter;
 * 
 * struct {
 *       OIDFilter filters<0..2^16-1>;
 * } OIDFilterExtension;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class OIDFilters extends Extension {

	private final static OIDFilter[] EMPTY = new OIDFilter[0];
	private OIDFilter[] items = EMPTY;

	@Override
	public short type() {
		return OID_FILTERS;
	}

	public OIDFilter[] get() {
		return items;
	}

	public OIDFilter get(int index) {
		return items[index];
	}

	public void set(OIDFilter... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(OIDFilter value) {
		if (items == EMPTY) {
			items = new OIDFilter[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}
}