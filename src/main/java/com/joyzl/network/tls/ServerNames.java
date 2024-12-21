package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 * <pre>
 * RFC 6066
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
public class ServerNames extends Extension {

	private final static ServerName[] EMPTY = new ServerName[0];
	private ServerName[] items = EMPTY;

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
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(ServerName value) {
		if (items == EMPTY) {
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
		final StringBuilder builder = Utility.getStringBuilder();
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