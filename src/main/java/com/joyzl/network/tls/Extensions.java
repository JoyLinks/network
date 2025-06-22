/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.List;

/**
 * 扩展
 * 
 * @author ZhangXi 2025年2月12日
 */
interface Extensions {

	/** 本实现的特殊方法以协助编码识别扩展所属的握手消息 */
	byte msgType();

	/** 本实现的特殊方法以协助编解码识别握手消息类型 */
	boolean isHelloRetryRequest();

	/** 标识是否包含扩展项 */
	boolean hasExtensions();

	/** 包含的扩展项数量 */
	int extensionSize();

	/** 添加指定的扩展项 */
	void addExtension(Extension extension);

	/** 获取指定索引的扩展项 */
	Extension getExtension(int index);

	/** 获取指定类型的扩展项 */
	@SuppressWarnings("unchecked")
	default <T extends Extension> T getExtension(short type) {
		for (int index = 0; index < extensionSize(); index++) {
			if (getExtension(index).type() == type) {
				return (T) getExtension(index);
			}
		}
		return null;
	}

	/** 获取所有的扩展项 */
	List<Extension> getExtensions();

	/** 设置多个扩展项，已存在的扩展项将被覆盖 */
	void setExtensions(List<Extension> value);
}