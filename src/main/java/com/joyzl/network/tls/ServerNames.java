/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：服务器名称标识，通常为域名或主机名；服务器可通过此名称匹配证书。
 * 
 * <pre>
 * RFC 6066
 * SNI(Server Name Identification)
 * 
 * struct {
 *    NameType name_type;
 *    select (name_type) {
 *        case host_name: HostName;
 *    } name;
 * } ServerName;
 * 
 * enum {
 *    host_name(0), (255)
 * } NameType;
 * 
 * opaque HostName<1..2^16-1>;
 * 
 * struct {
 *    ServerName server_name_list<1..2^16-1>
 * } ServerNameList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月18日
 */
class ServerNames extends Extension {

	private final static ServerName[] EMPTY_NAMES = new ServerName[0];
	private ServerName[] items = EMPTY_NAMES;

	public ServerNames() {
	}

	public ServerNames(ServerName... value) {
		set(value);
	}

	@Override
	public short type() {
		return SERVER_NAME;
	}

	public ServerName[] get() {
		return items;
	}

	public ServerName get(int index) {
		return items[index];
	}

	public void set(ServerName... value) {
		if (value == null) {
			items = EMPTY_NAMES;
		} else {
			items = value;
		}
	}

	public void add(ServerName value) {
		if (items == EMPTY_NAMES) {
			items = new ServerName[] { value };
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
		final StringBuilder builder = new StringBuilder();
		builder.append("server_name:");
		if (items != null && items.length > 0) {
			for (int index = 0; index < items.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(items[index].toString());
			}
		}
		return builder.toString();
	}
}