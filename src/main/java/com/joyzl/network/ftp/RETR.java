package com.joyzl.network.ftp;

/**
 * 获得
 * 
 * @author ZhangXi 2024年7月10日
 */
public class RETR extends FileMessage {

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.RETR;
	}

	@Override
	protected boolean isSuccess() {
		// 125 数据连接已打开，传输开始
		// 150 文件状态正常，将打开数据连接
		// 110 重新开始标记响应
		// 226 关闭数据连接
		// 250 请求文件动作完成
		// 425, 426, 451
		// 450, 550
		// 500, 501, 421, 530
		return false;
	}
}