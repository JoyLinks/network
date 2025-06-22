/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

/**
 * Upgrade<br>
 * HTTP/2 明确禁止使用此机制；这个机制只属于 HTTP/1.1
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class Upgrade {

	public final static String H2C = "h2c";

	public final static String NAME = HTTP1.Upgrade;

}