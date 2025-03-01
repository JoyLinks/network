/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接组，所有连接创建后将自动添加到连接组，断开后将自动移除连接组
 *
 * @author simon(ZhangXi TEL : 13883833982) 2019年7月9日
 *
 */
public final class ChainGroup {

	private final static Map<String, Server> SERVERS = new ConcurrentHashMap<>();
	private final static Map<String, Client> CLIENTS = new ConcurrentHashMap<>();

	private ChainGroup() {
		// 此类无需实例化
	}

	public final static void add(Server chain) {
		Server old = SERVERS.put(chain.key(), chain);
		if (old != null) {
			old.close();
		}
	}

	public final static void remove(Server chain) {
		Server old = SERVERS.remove(chain.key());
		if (old != null) {
			old.close();
		}
	}

	public final static void add(Client chain) {
		Client old = CLIENTS.put(chain.key(), chain);
		if (old != null) {
			old.close();
		}
	}

	public final static void remove(Client chain) {
		Client old = CLIENTS.remove(chain.key());
		if (old != null) {
			old.close();
		}
	}

	public static boolean hasServer() {
		return SERVERS.isEmpty();
	}

	public static boolean hasClient() {
		return CLIENTS.isEmpty();
	}

	public final static Server getServer(String key) {
		return SERVERS.get(key);
	}

	public final static Client getClient(String key) {
		return CLIENTS.get(key);
	}

	public final static Collection<Server> getServers() {
		return SERVERS.values();
	}

	public final static Collection<Client> getClients() {
		return CLIENTS.values();
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 群发给所有指定服务端的子链路,不判断是否有登录标识
	 * <p>
	 * 注意：群发的消息对象不应当有与链路以及发送相关的状态
	 * </p>
	 * 
	 * @param server
	 * @param message
	 * @return 投递数量
	 */
	public final static int sendSlaves(ChainType server, Object message) {
		int size = 0;
		for (Server s : SERVERS.values()) {
			if (s.type() == server) {
				if (s.active()) {
					for (Slave slave : s.getSlaves()) {
						if (slave.active()) {
							slave.send(message);
							size++;
						}
					}
				}
			}
		}
		return size;
	}

	/**
	 * 群发给指定服务端的子链路，排除无用户标识的链路
	 * <p>
	 * 注意：群发的消息对象不应当有与链路以及发送相关的状态
	 * </p>
	 * 
	 * @param server
	 * @param message
	 * @return 投递数量
	 */
	public final static int sendLogonSlaves(ChainType server, Object message) {
		int size = 0;
		for (Server s : SERVERS.values()) {
			if (s.type() == server) {
				if (s.active()) {
					for (Slave slave : s.getSlaves()) {
						if (slave.active() && slave.getToken() != null) {
							slave.send(message);
							size++;
						}
					}
				}
			}
		}
		return size;
	}

	/**
	 * 群发给指定服务端的子链路，排除无用户标识的链路，排除指定链路
	 * <p>
	 * 注意：群发的消息对象不应当有与链路以及发送相关的状态
	 * </p>
	 * 
	 * @param server
	 * @param exclude
	 * @param message
	 * @return 投递数量
	 */
	public final static int sendLogonSlaves(ChainType server, Chain exclude, Object message) {
		int size = 0;
		for (Server s : SERVERS.values()) {
			if (s.type() == server) {
				if (s.active()) {
					for (Slave slave : s.getSlaves()) {
						if (slave.active() && slave != exclude && slave.getToken() != null) {
							slave.send(message);
							size++;
						}
					}
				}
			}
		}
		return size;
	}

	/**
	 * 群发给指定客户端类型的所有链路
	 * <p>
	 * 注意：群发的消息对象不应当有与链路以及发送相关的状态
	 * </p>
	 * 
	 * @param type
	 * @param message
	 * @return 投递数量
	 */
	public final static int sendClients(ChainType type, Object message) {
		int size = 0;
		for (Client client : CLIENTS.values()) {
			if (client.type() == type) {
				if (client.active()) {
					client.send(message);
					size++;
				}
			}
		}
		return size;
	}
}