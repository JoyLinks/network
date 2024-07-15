package com.joyzl.network.ftp;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件信息列表
 * 
 * @author ZhangXi 2024年7月10日
 */
public class LIST extends FTPMessage {

	private String path;
	private final List<FTPFile> files = new ArrayList<>();

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.LIST;
	}

	@Override
	protected String getParameter() {
		return getPath();
	}

	@Override
	protected void setParameter(String value) {
		setPath(value);
	}

	@Override
	protected boolean isSuccess() {
		// 125 数据连接已打开，传输开始
		// 150 文件状态正常，将打开数据连接
		// 226 关闭数据连接
		// 250 请求文件动作完成
		// 425, 426, 451
		// 450
		// 500, 501, 502, 421, 530
		return getCode() == 125 || getCode() == 150 || getCode() == 226 || getCode() == 250;
	}

	@Override
	protected void finish() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String value) {
		path = value;
	}

	public List<FTPFile> getFiles() {
		return files;
	}

	/**
	 * <ul>
	 * <li>Apr April  四月份</li>
	 * <li>Aug August  八月份</li>
	 * <li>Dec December 十二月份</li>
	 * <li>Feb February 二月份</li>
	 * <li>Jan January 一月份</li>
	 * <li>Jul July 七月份</li>
	 * <li>Jun June 六月份</li>
	 * <li>Mar March  三月份</li>
	 * <li>May May 五月份</li>
	 * <li>Nov November 十一月份</li>
	 * <li>Oct October 十月份</li>
	 * <li>SEP September 九月份</li>
	 * </ul>
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return Month / null
	 */
	public final static Month parseMonth(char c1, char c2, char c3) {

		if (c1 == 'a' || c1 == 'A') {
			if (c2 == 'p' || c2 == 'P') {
				if (c3 == 'r' || c3 == 'R') {
					return Month.APRIL;
				}
			} else//
			if (c2 == 'u' || c2 == 'U') {
				if (c3 == 'g' || c3 == 'G') {
					return Month.AUGUST;
				}
			}
		} else //
		if (c1 == 'd' || c1 == 'D') {
			if (c2 == 'e' || c2 == 'E') {
				if (c3 == 'c' || c3 == 'C') {
					return Month.DECEMBER;
				}
			}
		} else //
		if (c1 == 'f' || c1 == 'F') {
			if (c2 == 'e' || c2 == 'E') {
				if (c3 == 'b' || c3 == 'B') {
					return Month.FEBRUARY;
				}
			}
		} else //
		if (c1 == 'j' || c1 == 'J') {
			if (c2 == 'a' || c2 == 'A') {
				if (c3 == 'n' || c3 == 'N') {
					return Month.JANUARY;
				}
			} else //
			if (c2 == 'u' || c2 == 'U') {
				if (c3 == 'l' || c3 == 'L') {
					return Month.JULY;
				} else //
				if (c3 == 'n' || c3 == 'N') {
					return Month.JUNE;
				}
			}
		} else //
		if (c1 == 'm' || c1 == 'M') {
			if (c2 == 'a' || c2 == 'A') {
				if (c3 == 'r' || c3 == 'R') {
					return Month.MARCH;
				} else //
				if (c3 == 'y' || c3 == 'Y') {
					return Month.MAY;
				}
			}
		} else //
		if (c1 == 'n' || c1 == 'N') {
			if (c2 == 'o' || c2 == 'O') {
				if (c3 == 'v' || c3 == 'V') {
					return Month.NOVEMBER;
				}
			}
		} else //
		if (c1 == 'o' || c1 == 'O') {
			if (c2 == 'c' || c2 == 'C') {
				if (c3 == 't' || c3 == 'T') {
					return Month.OCTOBER;
				}
			}
		} else //
		if (c1 == 's' || c1 == 'S') {
			if (c2 == 'e' || c2 == 'E') {
				if (c3 == 'p' || c3 == 'P') {
					return Month.SEPTEMBER;
				}
			}
		}
		return null;
	}
}