/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 为ChainHandler提供泛型支持
 * 
 * @author ZhangXi 2025年4月27日
 * @param <C> 特定实现的链路
 * @param <M> 特定实现的消息
 */
public interface ChainGenericsHandler<C, M> extends ChainHandler {

	@Override
	@SuppressWarnings("unchecked")
	default void connected(ChainChannel chain) throws Exception {
		connected((C) chain);
	}

	@Override
	@SuppressWarnings("unchecked")
	default Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		return decode((C) chain, buffer);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void received(ChainChannel chain, Object message) throws Exception {
		received((C) chain, (M) message);
	}

	@Override
	@SuppressWarnings("unchecked")
	default DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		return encode((C) chain, (M) message);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void sent(ChainChannel chain, Object message) throws Exception {
		sent((C) chain, (M) message);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void disconnected(ChainChannel chain) throws Exception {
		disconnected((C) chain);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void beat(ChainChannel chain) throws Exception {
		beat((C) chain);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void error(ChainChannel chain, Throwable e) {
		error((C) chain, e);
	}

	abstract void connected(C chain) throws Exception;

	abstract Object decode(C chain, DataBuffer buffer) throws Exception;

	abstract void received(C chain, M message) throws Exception;

	abstract DataBuffer encode(C chain, M message) throws Exception;

	abstract void sent(C slave, M message) throws Exception;

	abstract void disconnected(C chain) throws Exception;

	abstract void beat(C chain) throws Exception;

	abstract void error(C chain, Throwable e);
}