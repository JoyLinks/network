package com.joyzl.network.ftp;

/**
 * FTP 命令
 * 
 * @author ZhangXi 2024年7月5日
 */
public enum FTPCommand {

	// 访问控制

	/** 用户 USER SP username CRLF */
	USER,
	/** 密码 PASS SP password CRLF */
	PASS,
	/** 账户 ACCT SP account-information CRLF */
	ACCT,
	/** 改变工作目录 CWD SP pathname CRLF */
	CWD,
	/** 返回上层目录 CDUP CRLF */
	CDUP,
	/** 结构装备 SMNT SP pathname CRLF */
	SMNT,
	/** 注销 QUIT CRLF */
	QUIT,
	/** 重新初始化 REIN CRLF */
	REIN,

	// 传输参数

	/** 主动模式（服务器连接本地端口），设定数据端口 PORT SP host-port CRLF */
	PORT,
	/** 被动模式（本地连接服务器端口），获取数据端口 PASV CRLF */
	PASV,
	/** 表示类型 TYPE SP type-code CRLF */
	TYPE,
	/** 文件结构 STRU SP structure-code CRLF */
	STRU,
	/** 传输模式 MODE SP mode-code CRLF */
	MODE,

	// 服务

	/** 获得 RETR SP pathname CRLF */
	RETR,
	/** 保存 STOR SP pathname CRLF */
	STOR,
	/** 唯一保存 STOU CRLF */
	STOU,
	/** 追加 APPE SP pathname CRLF */
	APPE,
	/** 分配 ALLO SP decimal-integer [SP R SP decimal-integer] CRLF */
	ALLO,
	/** 重新开始 REST SP marker CRLF */
	REST,
	/** 重命名开始 RNFR SP pathname CRLF */
	RNFR,
	/** 重命名为 RNTO SP pathname CRLF */
	RNTO,
	/** 放弃 ABOR CRLF */
	ABOR,
	/** 删除 DELE SP pathname CRLF */
	DELE,
	/** 删除目录 RMD SP pathname CRLF */
	RMD,
	/** 新建目录 MKD SP pathname CRLF */
	MKD,
	/** 打印工作目录 PWD CRLF */
	PWD,
	/** 列表 LIST [SP pathname] CRLF */
	LIST,
	/** 名字列表 NLST [SP pathname] CRLF */
	NLST,
	/** 站点参数 SITE SP string CRLF */
	SITE,
	/** 系统 SYST CRLF */
	SYST,
	/** 状态 STAT [SP pathname] CRLF */
	STAT,
	/** 帮助 HELP [SP string] CRLF */
	HELP,
	/** 空操作 NOOP CRLF */
	NOOP,
}
