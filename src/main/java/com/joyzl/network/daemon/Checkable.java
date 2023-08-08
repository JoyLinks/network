/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.daemon;

/**
 * 守护进程检查项
 * 
 * @author ZhangXi
 * @date 2022年4月14日
 */
public interface Checkable {

	/**
	 * 任务开始
	 */
	public void start();

	/**
	 * 检查任务
	 */
	public void check();

	/**
	 * 任务结束
	 */
	public void finish();

	/**
	 * 最后一次执行时间戳
	 * 
	 * @return 如果从未执行则为0
	 */
	public long getTimestamp();

	/**
	 * 任务执行次数
	 * 
	 * @return 0~n
	 */
	public long getCounts();

	/**
	 * 最后一次任务执行耗时(毫秒)
	 */
	public int getSpend();
}