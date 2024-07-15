package com.joyzl.network.ftp;

/**
 * 被动
 * 
 * @author ZhangXi 2024年7月8日
 */
public class PASV extends PORT {

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.PASV;
	}

	@Override
	protected void setText(String value) {
		super.setText(value);
		// 227 Entering Passive Mode (127,0,0,1,196,146)
		int begin = value.indexOf('(');
		if (begin > 0) {
			int end = value.lastIndexOf(')');
			if (end > begin) {
				setParameter(value.substring(begin + 1, end));
			}
		}
	}

	@Override
	protected boolean isSuccess() {
		// 227 进入被动模式
		// 500, 501, 502, 421, 530
		return getCode() == 227;
	}
}