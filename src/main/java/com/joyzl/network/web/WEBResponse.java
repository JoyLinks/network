/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Response;

/**
 * WEN HTTP Response
 * 
 * @author ZhangXi
 * @date 2021年10月12日
 */
public final class WEBResponse extends Response {

	public WEBResponse() {
		setStatus(HTTPStatus.OK);
		setVersion("HTTP/1.1");
	}
}
