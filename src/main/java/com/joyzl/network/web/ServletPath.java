/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Servlet 注解
 * 
 * @author ZhangXi
 * @date 2020年11月10日
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServletPath {

	/**
	 * 完全匹配 "/action.html"<br>
	 * 部分匹配 "/action/*","/action/*.do"<br>
	 * 后缀匹配 "*.do"
	 */
	String path();
}