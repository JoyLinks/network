/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.joyzl.network.Executor;

/**
 * 基于TCP连接的客户端，提供心跳，中断重连和超时检查机制
 * <p>
 * 工作机制：
 * <ol>
 * <li>connect()请求连接（异步）;</li>
 * <li>connected()连接返回（成功/失败）;</li>
 * <li>receive()请求接收数据（异步）；</li>
 * <li>received()接收数据返回（成功/失败）；</li>
 * <li>send()请求发送数据（异步）；</li>
 * <li>sent()发送数据返回（成功/失败）；</li>
 * <li>close()关闭链路；</li>
 * </ol>
 * 使用者根据通信协议确定发送和接收数据的时机和机制；
 * 发送和接收数据可同时进行，但不能同时请求多个发送或多个接收，应等待上一次发送或接收返回后才能再次请求发送或接收数据。
 * 链路关闭后可再次请求连接。对象不是多线程安全的，在多线程收发情形下使用者应实现消息排队。
 * <p>
 *
 * @author ZhangXi 2019年7月12日
 *
 */
public class TCPClient extends TCPLink {

	private final ScheduledFuture<?> future;
	/** 数据收发时间戳 */
	private volatile long timestamp;
	/** 重新连接间隔时间(秒) */
	private int reconnect = 6;
	/** 连接空闲心跳间隔时间(秒) */
	private int heartbeat = 60 * 3;

	/**
	 * 创建TCPClient由接点标识指定连接信息
	 * <p>
	 * 自动生成接点标识 "192.168.0.1:1030"
	 *
	 * @param handler {@link ChainHandler}
	 * @param host 主机
	 * @param port 端口
	 */
	public TCPClient(ChainHandler handler, String host, int port) {
		super(handler, host, port);

		// 每秒触发检查重连和心跳以及超时
		future = Executor.scheduleAtFixedRate(TASK, 1, 1, TimeUnit.SECONDS);
		ChainGroup.add(this);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_CLIENT;
	}

	@Override
	public void connect() {
		if (future.isCancelled()) {
			throw new IllegalStateException("客户端链路已关闭");
		} else {
			super.connect();
		}
	}

	@Override
	protected void received(int size) {
		timestamp = System.currentTimeMillis();
		super.received(size);
	}

	@Override
	protected void sent(int size) {
		timestamp = System.currentTimeMillis();
		super.sent(size);
	}

	@Override
	public void close() {
		super.close();
		ChainGroup.remove(this);
		future.cancel(false);
	}

	/**
	 * 定时任务检查心跳和重连
	 */
	final Runnable TASK = new Runnable() {
		private long current;

		@Override
		public void run() {
			if (future.isCancelled()) {
				return;
			}
			current = System.currentTimeMillis();
			if (timestamp > 0) {
				if (active()) {
					// 心跳检查
					if (getHeartbeat() - (current - timestamp) / 1000 <= 0) {
						try {
							handler().beat(TCPClient.this);
						} catch (Exception e) {
							handler().error(TCPClient.this, e);
						}
					}
				} else {
					// 重连检查
					if (getReconnect() - (current - timestamp) / 1000 <= 0) {
						timestamp = current;
						connect();
					}
				}
				// 用户检查
				check(current);
			} else {
				timestamp = current;
			}
		}
	};

	protected void check(long timestamp) {
		// 实现者可重载此方法检查指令超时
		// System.out.println(active() + "," + timestamp);
	}

	/**
	 * 获取自动重新连接间隔时间
	 *
	 * @return 秒
	 */
	public int getReconnect() {
		return reconnect;
	}

	/**
	 * 设置自动重新连接间隔时间，不建议低于10秒
	 *
	 * @param interval 10~n秒
	 */
	public void setReconnect(int interval) {
		reconnect = interval;
	}

	/**
	 * 获取连接空闲心跳间隔时间
	 *
	 * @return 秒
	 */
	public int getHeartbeat() {
		return heartbeat;
	}

	/**
	 * 设置连接空闲心跳间隔时间，不建议低于60秒
	 *
	 * @param time 60~n秒
	 */
	public void setHeartbeat(int time) {
		heartbeat = time;
	}
}