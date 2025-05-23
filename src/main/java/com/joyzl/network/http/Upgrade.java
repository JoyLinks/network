/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
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