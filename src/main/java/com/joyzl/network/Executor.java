/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池执行器
 *
 * @author ZhangXi 2019年7月8日
 *
 */
public final class Executor {

	// 为业务提供执行线程池
	private static ScheduledThreadPoolExecutor WORK_THREAD_POOL;
	// 为NIO.2提供统一的线程组
	private static AsynchronousChannelGroup CHANNEL_GROUP;

	// 初始化后线程数量
	private static int size = 0;

	private Executor() {
		// 禁止实例化
	}

	public static final void initialize(int thead_size) {
		// 如果业务非常简单，执行时间非常短，不需要与外部网元交互、访问数据库和磁盘，不需要等待其它资源，则建议直接在业务ChannelHandler中执行，不需要再启业务的线程或者线程池。避免线程上下文切换，也不存在线程并发问题。
		// 使用自己的线程池的时候注意限流，不然容易高并发情况下容易引起内存泄露。线程池提交任务是异步无阻塞的。高并发情况下可能造成大量的请求积压在线程池的队列里，耗完内存。
		// tomcat也使用了线程池，但是他有限制连接数。所以使用自己线程池的时候要么也限流，要么实现自己线程池，当任务超过一定量的提交任务时阻塞。

		if (size > 0) {
			throw new IllegalStateException("执行器已经初始化");
		}
		if (thead_size <= 0) {
			// 自动计算最佳线程数
			thead_size = Runtime.getRuntime().availableProcessors() * 128;
		}

		size = thead_size;

		try {
			// 初始化NIO.1线程
			ChainSelector.initialize(2);

			thead_size -= 1;

			// 初始化NIO.2线程
			CHANNEL_GROUP = AsynchronousChannelGroup.withFixedThreadPool(thead_size / 2, new ThreadFactory("nio.2-"));
			thead_size -= thead_size / 2;

			// 初始化业务线程池
			// WORK_THREAD_POOL = Executors.newScheduledThreadPool(thead_size,
			// new ThreadFactory("work-"));
			WORK_THREAD_POOL = new ScheduledThreadPoolExecutor(thead_size, new ThreadFactory("work-"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static final void shutdown() {
		ChainSelector.shutdown();

		if (CHANNEL_GROUP != null) {
			// NIO.2 终止并关闭
			CHANNEL_GROUP.shutdown();
			try {
				CHANNEL_GROUP.awaitTermination(10, TimeUnit.SECONDS);
				CHANNEL_GROUP.shutdownNow();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
			CHANNEL_GROUP = null;
		}

		if (WORK_THREAD_POOL != null) {
			WORK_THREAD_POOL.shutdown();
			try {
				WORK_THREAD_POOL.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			WORK_THREAD_POOL.shutdownNow();
			WORK_THREAD_POOL = null;
		}
		size = 0;
	}

	public static int getThreadSize() {
		return size;
	}

	////////////////////////////////////////////////////////////////////////////////

	public final static AsynchronousChannelGroup channelGroup() {
		return CHANNEL_GROUP;
	}

	public final static ThreadPoolExecutor pool() {
		return WORK_THREAD_POOL;
	}

	////////////////////////////////////////////////////////////////////////////////

	public static final boolean isActive() {
		return WORK_THREAD_POOL != null && !WORK_THREAD_POOL.isShutdown();
	}

	/**
	 * 在业务线程池立即执行指定任务
	 *
	 * @param command 任务
	 */
	public static final void execute(Runnable command) {
		WORK_THREAD_POOL.execute(command);
	}

	public static final Future<?> submit(Runnable command) {
		return WORK_THREAD_POOL.submit(command);
	}

	public static final <T> Future<T> submit(Callable<T> command) {
		return WORK_THREAD_POOL.submit(command);
	}

	/**
	 * 在业务线程延迟执行指定任务
	 *
	 * @param command 任务
	 * @param delay 延迟
	 * @param unit 延迟单位
	 * @return ScheduledFuture
	 */
	public static final ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return WORK_THREAD_POOL.schedule(command, delay, unit);
	}

	/**
	 * 在业务线程定时循环执行任务,不考虑任务执行花费的时间
	 *
	 * @param command 任务
	 * @param initial 第一次执行延迟时间
	 * @param period 间隔时间
	 * @param unit 时间单位
	 * @return ScheduledFuture
	 */
	public static final ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initial, long period, TimeUnit unit) {
		return WORK_THREAD_POOL.scheduleAtFixedRate(command, initial, period, unit);
	}

	/**
	 * 在业务线程定时循环执行任务,任务执行完成推迟间隔时间执行下一次
	 *
	 * @param command 任务
	 * @param initial 第一次执行延迟时间
	 * @param delay 间隔时间
	 * @param unit 时间单位
	 * @return ScheduledFuture
	 */
	public static final ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initial, long delay, TimeUnit unit) {
		return WORK_THREAD_POOL.scheduleWithFixedDelay(command, initial, delay, unit);
	}
}