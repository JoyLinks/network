package com.joyzl.network.ftp;

import java.io.File;

import com.joyzl.network.chain.TCPLink;

/**
 * FTP Client
 * 
 * @author ZhangXi 2024年7月8日
 */
public class FTPClient extends TCPLink {

	/** 当前消息，等待回复的消息，涉及数据传输的消息可能会收到多次回复 */
	private FTPMessage current;
	/** 消息监听器 */
	private FTPListener listener = FTPListener.EMPTY;

	// 以下为交互参数

	private String system;
	private String directory;
	private String passiveHost;
	private int passivePort;

	public FTPClient(String host, int port) {
		super(FTPClientHandler.INSTANCE, host, port);
	}

	/**
	 * 登录，涉及命令：USER PASS
	 * 
	 * @param username 用户名
	 * @param password 密码
	 * @see {@link #login(String, String, String)}
	 */
	public void login(String user, String password) {
		login(user, password, null);
	}

	/**
	 * 登录，涉及命令：USER PASS ACCT
	 * 
	 * @param username 用户名
	 * @param password 密码
	 * @param account 账户
	 */
	public void login(String username, String password, String account) {
		final USER user = new USER() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (getCode() == 331) {
					// 需求密码
					final PASS pass = new PASS() {
						@Override
						protected void finish() {
							listener.finish(this);
							if (getCode() == 332) {
								// 332 需要帐户
								final ACCT acct = new ACCT() {
									@Override
									protected void finish() {
										listener.finish(this);
										if (isSuccess()) {
											// 成功
										} else {
											// 失败
										}
									}
								};
								acct.setAccount(account);
								send(acct);
							}
						}
					};
					pass.setPassword(password);
					send(pass);
				} else//
				if (getCode() == 332) {
					// 需求账户
					final ACCT acct = new ACCT() {
						@Override
						protected void finish() {
							listener.finish(this);
							if (isSuccess()) {
								// 成功
							} else {
								// 失败
							}
						}
					};
					acct.setAccount(account);
					send(acct);
				} else//
				if (getCode() == 230) {
					// 成功
				} else {
					// 无效
				}
			}
		};
		user.setUsername(username);
		send(user);
	}

	/**
	 * 注销，涉及命令：QUIT
	 */
	public void logout() {
		final QUIT quit = new QUIT() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功
				} else {
					// 失败
				}
			}
		};
		send(quit);
	};

	/**
	 * 准备，涉及命令：SYST HELP STAT
	 */
	public void ready() {
		final STAT stat = new STAT() {
			@Override
			protected void finish() {
				listener.finish(this);
			}
		};

		final HELP help = new HELP() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					send(stat);
				}
			}
		};

		final SYST syst = new SYST() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					system = getSystem();
					send(help);
				}
			}
		};
		send(syst);
	}

	/**
	 * 初始化，涉及命令：REIN
	 */
	public void initialize() {
		final REIN rein = new REIN() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功
				} else {
					// 失败
				}
			}
		};
		send(rein);
	};

	/**
	 * 被动模式
	 */
	public void passive() {
		final PASV pasv = new PASV() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					passiveHost = getHost();
					passivePort = getPort();
				}
			}
		};
		send(pasv);
	}

	/**
	 * 切换为上级目录，涉及命令：CDUP
	 */
	public void parent() {
		final CDUP cdup = new CDUP() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功
				} else {
					// 失败
				}
			}
		};
		send(cdup);
	};

	/**
	 * 指定当前目录，涉及命令：CWD
	 * 
	 * @param path 远程目录路径
	 */
	public void directory(String path) {
		final CWD cwd = new CWD() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					directory = getPath();
				}
			}
		};
		cwd.setPath(path);
		send(cwd);
	}

	/**
	 * 装载指定目录，涉及命令：SMNT
	 * 
	 * @param path 远程目录路径
	 */
	public void mount(String path) {
		final SMNT smnt = new SMNT() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功
				} else {
					// 失败
				}
			}
		};
		smnt.setPath(path);
		send(smnt);
	}

	/**
	 * 重命名，涉及命令：RNFR RNTO
	 * 
	 * @param from 远程目录路径
	 * @param to 远程目录路径
	 */
	public void rename(String from, String to) {
		final RNFR rnfr = new RNFR() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (getCode() == 350) {
					final RNTO rnto = new RNTO() {
						@Override
						protected void finish() {
							listener.finish(this);
							if (getCode() == 250) {
								// 成功
							} else {
								// 失败
							}
						}
					};
					rnto.setPath(to);
					send(rnto);
				} else {
					// 失败
				}
			}
		};
		rnfr.setPath(from);
		send(rnfr);
	}

	/**
	 * 删除文件或目录，涉及命令：LIST DELE RMD
	 * 
	 * @param path 远程目录路径
	 */
	public void delete(String path) {
		final LIST list = new LIST() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功

					// TODO 判断路径是目录还是文件

					final DELE dele = new DELE() {
						@Override
						protected void finish() {
							listener.finish(this);
							if (isSuccess()) {
								// 成功
							} else {
								// 失败
							}
						}
					};
					dele.setPath(path);
					send(dele);

					final RMD rmd = new RMD() {
						@Override
						protected void finish() {
							listener.finish(this);
							if (isSuccess()) {
								// 成功
							} else {
								// 失败
							}
						}
					};
					rmd.setPath(path);
					send(rmd);
				} else {
					// 失败
				}
			}
		};
		list.setPath(path);
		send(list);
	}

	/**
	 * 创建目录，涉及命令：MKD
	 * 
	 * @param path 远程目录路径
	 */
	public void make(String path) {
		final MKD mkd = new MKD() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功
				} else {
					// 失败
				}
			}
		};
		mkd.setPath(path);
		send(mkd);
	}

	/**
	 * 获取当前目录，涉及命令：PWD
	 */
	public void directory() {
		final PWD pwd = new PWD() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					directory = getPath();
				}
			}
		};
		send(pwd);
	}

	/**
	 * 浏览，涉及命令：LIST
	 * 
	 * @param path 远程目录路径
	 */
	public void browse(String path) {
		final LIST list = new LIST() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (getCode() == 150) {
					// 150 文件状态正常，将打开数据连接
					ListClient.receive(this, passiveHost, passivePort);
				} else //
				if (getCode() == 125) {
					// 125 数据连接已打开，传输开始
				} else //
				if (getCode() == 226) {
					// 226 关闭数据连接
				} else //
				if (getCode() == 250) {
					// 250 请求文件动作完成
				}
			}
		};
		list.setPath(path);
		send(list);
	}

	/**
	 * 上传，涉及命令：ALLO STOR STOU APPE
	 * 
	 * @param local 本地文件绝对路径
	 * @param path 远程文件路径
	 * @param append 追加
	 */
	public void upload(String local, String path, boolean append) {
		upload(new File(local), path, append);
	}

	/**
	 * 上传，涉及命令：ALLO STOR STOU APPE
	 * 
	 * @param local 本地文件
	 * @param path 远程文件路径
	 * @param append 追加
	 */
	public void upload(File local, String path, boolean append) {
		if (local.exists()) {
			if (local.canRead()) {
				// 1 申请空间
				final ALLO allo = new ALLO() {
					@Override
					protected void finish() {
						listener.finish(this);
						if (isSuccess()) {
							if (path == null || path.length() == 0) {
								// 2A 唯一保存，由服务器生成文件名
								final STOU stou = new STOU() {
									@Override
									protected void finish() {
										listener.finish(this);
										if (isSuccess()) {
											// 传输文件
										}
									}
								};
								send(stou);
							} else if (append) {
								// 2B 追加文件数据
								final APPE appe = new APPE() {
									@Override
									protected void finish() {
										listener.finish(this);
										if (isSuccess()) {
											// 传输文件
										}
									}
								};
								appe.setPath(path);
								send(appe);
							} else {
								// 2C 保存指定路径
								final STOR stor = new STOR() {
									@Override
									protected void finish() {
										listener.finish(this);
										if (isSuccess()) {
											// 传输文件
										}
									}
								};
								stor.setPath(path);
								send(stor);
							}
						}
					}
				};
				allo.setSize(local.length());
				allo.setOccupy(0);
				send(allo);
			} else {
				throw new IllegalArgumentException("本地文件不可读");
			}
		} else {
			throw new IllegalArgumentException("本地文件不存在");
		}
	}

	/**
	 * 下载，涉及命令：RETR
	 * 
	 * @param path 远程文件路径
	 * @param local 本地文件路径
	 */
	public void download(String path, String local) {
		download(path, new File(local));
	}

	/**
	 * 下载，涉及命令：RETR
	 * 
	 * @param path 远程文件路径
	 * @param local 本地文件
	 */
	public void download(String path, File local) {
		if (local.canWrite()) {
			final RETR retr = new RETR() {
				@Override
				protected void finish() {
					listener.finish(this);
					if (isSuccess()) {
						// 成功
						// 文件传输
					} else {
						// 失败
					}
				}
			};
			retr.setPath(path);
			send(retr);
		}
	}

	/**
	 * 终止，涉及命令：ABOR
	 */
	public void abort() {
		final ABOR abor = new ABOR() {
			@Override
			protected void finish() {
				listener.finish(this);
				if (isSuccess()) {
					// 成功
				} else {
					// 失败
				}
			}
		};
		send(abor);
	}

	/**
	 * 发送FTP命令
	 * 
	 * @param cmd {@link FTPMessage}
	 */
	public void send(FTPMessage cmd) {
		if (current != null) {
			if (current.getCode() < 100) {
				cmd.setCode(902);
				cmd.finish();
				return;
			}
		}
		if (active()) {
			super.send(current = cmd);
		} else {
			cmd.setCode(901);
			cmd.finish();
		}
	}

	public FTPMessage getCurrent() {
		return current;
	}

	public FTPListener getListener() {
		return listener;
	}

	public void setListener(FTPListener value) {
		if (value == null) {
			listener = FTPListener.EMPTY;
		} else {
			listener = value;
		}
	}

	public String getSystem() {
		return system;
	}

	public String getDirectory() {
		return directory;
	}
}