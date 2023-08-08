/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.daemon;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 守护进程检查
 * 
 * @author ZhangXi
 * @date 2022年4月14日
 */
public final class Daemones extends Thread {

	private static Daemones INSTANCE;
	private final static List<Checkable> CHECKS = new CopyOnWriteArrayList<>();

	public static Collection<Checkable> checks() {
		return Collections.unmodifiableCollection(CHECKS);
	}

	public static void register(Checkable check) {
		if (CHECKS.contains(check)) {
		} else {
			CHECKS.add(check);
			// System.out.println(check);
		}
	}

	public static void unregister(Checkable check) {
		if (CHECKS.contains(check)) {
			CHECKS.remove(check);
		}
	}

	public static final void initialize() {
		if (INSTANCE == null) {
			INSTANCE = new Daemones();
			INSTANCE.start();
		}
	}

	public static final void shutdown() {
		if (INSTANCE != null) {
			CHECKS.clear();
			INSTANCE = null;
		}
	}

	public Daemones() {
		setDaemon(true);
		setName("DAEMONES");
	}

	@Override
	public void run() {
		if (CHECKS.isEmpty()) {
		} else {
			for (int index = 0; index < CHECKS.size(); index++) {
				try {
					CHECKS.get(index).start();
					CHECKS.get(index).check();
					CHECKS.get(index).finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			sleep(60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
