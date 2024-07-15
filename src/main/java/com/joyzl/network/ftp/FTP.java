package com.joyzl.network.ftp;

/**
 * 文件传输协议 FTP(File Transfer Protocol)
 * <p>
 * RFC959 FILE TRANSFER PROTOCOL (FTP)<br>
 * RFC1700 ASSIGNED NUMBERS<br>
 * </p>
 * 
 * @author ZhangXi 2024年7月4日
 */
public class FTP {

	final static char SPACE = ' ';
	final static char HYPHEN = '-';
	final static char SEPARATOR = '/';
	final static String CRLF = "\r\n";

	public static String codeText(int code) {
		switch (code) {

			// 1XX 预备状态

			case 110:// 重新开始标记响应
				return "Restart marker reply.";
			case 120:// 服务将在稍后准备完成
				return "Service ready in minutes.";
			case 125:// 数据连接已打开，传输开始
				return "Data connection already open; transfer starting.";
			case 150:// 文件状态正常，将打开数据连接
				return "File status okay; about to open data connection.";

			// 完成状态

			case 200:// 命令成功
				return "Command okay.";
			case 202:// 命令没有实现，对本站点冗余
				return "Command not implemented, superfluous at this site.";
			case 211:// 系统状态，或者系统帮助响应
				return "System status, or system help reply.";
			case 212:// 目录状态
				return "Directory status.";
			case 213:// 文件状态
				return "File status.";
			case 214:// 帮助信息
				return "Help message.";
			case 215:// 系统类型名称
				return "NAME system type.";
			case 220:// 接受新用户服务准备完成
				return "Service ready for new user.";
			case 221:// 服务关闭控制连接
				return "Service closing control connection.";
			case 225:// 数据连接打开，没有传输
				return "Data connection open; no transfer in progress.";
			case 226:// 关闭数据连接
				return "Closing data connection.";
			case 227:// 进入被动模式
				return "Entering Passive Mode (h1,h2,h3,h4,p1,p2).";
			case 230:// 用户成功登录，继续
				return "User logged in, proceed.";
			case 250:// 请求文件动作完成
				return "Requested file action okay, completed.";
			case 257:// 创建了目录
				return "PATHNAME created.";

			// 中间状态

			case 331:// 用户名有效，需要密码
				return "User name okay, need password.";
			case 332:// 需要帐户才能登录
				return "Need account for login.";
			case 350:// 请求文件动作需要进一步的信息
				return "Requested file action pending further information.";

			// 暂时状态

			case 421:// 服务不可用，关闭控制连接
				return "Service not available, closing control connection.";
			case 425:// 不能打开数据连接
				return "Can't open data connection.";
			case 426:// 连接关闭，放弃传输
				return "Connection closed; transfer aborted.";
			case 450:// 请求文件动作没有执行
				return "Requested file action not taken.";
			case 451:// 请求动作放弃，处理中发生本地错误
				return "Requested action aborted. Local error in processing.";
			case 452:// 请求动作未执行
				return "Requested action not taken.";

			// 错误状态

			case 500:// 语法错误，命令不能被识别
				return "Syntax error, command unrecognized.";
			case 501:// 参数语法错误
				return "Syntax error in parameters or arguments.";
			case 502:// 命令没有实现
				return "Command not implemented.";
			case 503:// 命令顺序错误
				return "Bad sequence of commands.";
			case 504:// 没有实现这个命令参数
				return "Command not implemented for that parameter.";
			case 530:// 未登录
				return "Not logged in.";
			case 532:// 需要帐户来存储文件
				return "Need account for storing files.";
			case 550:// 请求的动作没有执行
				return "Requested action not taken.";
			case 551:// 请求动作放弃，未知的页面类型
				return "Requested action aborted. Page type unknown.";
			case 552:// 请求文件动作被放弃
				return "Requested file action aborted.";
			case 553:// 请求动作未获得
				return "Requested action not taken.";

			// 自定义

			case 901:// 网络未连接/网络无法连接
				return "Network.";
			case 902:// 网络忙/等待当前命令返回
				return "Network busy.";
			case 999:// 超时
				return "Timeout.";

			default:
				return "Unknown";
		}
	}
}