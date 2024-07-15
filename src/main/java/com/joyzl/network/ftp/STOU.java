package com.joyzl.network.ftp;

/**
 * 唯一保存
 * 
 * @author ZhangXi 2024年7月10日
 */
public class STOU extends FileMessage {

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.STOU;
	}

	@Override
	protected boolean isSuccess() {
		// 125 数据连接已打开，传输开始
		// 150 文件状态正常，将打开数据连接
		// 110 重新开始标记响应
		// 226 关闭数据连接
		// 250 请求文件动作完成
		// 425, 426, 451, 551, 552
		// 532, 450, 452, 553
		// 500, 501, 421, 530
		return false;
	}
}