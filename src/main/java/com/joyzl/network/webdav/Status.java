package com.joyzl.network.webdav;

import com.joyzl.network.http.HTTPStatus;

public enum Status {

	// 207 多状态
	// 422 不可处理的实体
	// 423 锁定
	// 424 依赖失败
	// 507 存储空间不足

	;
	////////////////////////////////////////////////////////////////////////////////

	private final int code;
	private final String text;

	private Status(int c, String t) {
		code = c;
		text = t;
	}

	public int code() {
		return code;
	}

	public String text() {
		return text;
	}

	public final static HTTPStatus fromCode(int code) {
		int length = HTTPStatus.values().length;
		for (int index = 0; index < length; index++) {
			if (HTTPStatus.values()[index].code() == code) {
				return HTTPStatus.values()[index];
			}
		}
		return null;
	}
}