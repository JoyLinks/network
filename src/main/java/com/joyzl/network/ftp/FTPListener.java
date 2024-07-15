package com.joyzl.network.ftp;

/**
 * 命令执行监听 FTP Listener
 * 
 * @author ZhangXi 2024年7月11日
 */
public interface FTPListener {

	/**
	 * 空的不执行任何动作的监听器实例
	 */
	final static FTPListener EMPTY = new FTPListener() {

		@Override
		public void finish(FTPMessage message) {
		}
	};

	void finish(FTPMessage message);
}