package com.joyzl.network.http;

/**
 * HTTP 2 Header 压缩表
 * 
 * @author ZhangXi 2025年4月3日
 */
class HPACK extends HTTP2 {

	/** 帧头列表的最大数量 */
	private int maxHeaderListSize = Integer.MAX_VALUE;

	// User-Agent 应索引
	// Cookie 应永不索引

	private final Table table = new Table();
	private int max = Integer.MAX_VALUE;

	/** 获取指定索引头名 */
	public String getName(int index) {
		if (index > STATIC_TABLE_SIZE) {
			return table.getName(index - STATIC_TABLE_SIZE);
		}
		return STATIC_TABLE[index].name;
	}

	/** 获取指定索引头值 */
	public String getValue(int index) {
		if (index > STATIC_TABLE_SIZE) {
			return table.getValue(index - STATIC_TABLE_SIZE);
		}
		return STATIC_TABLE[index].value;
	}

	/** 获取索引的名称，第一个匹配的名称 */
	public int findName(String name) {
		int index = getStaticName(name);
		if (index > 0) {
			return index;
		}
		index = table.find(name);
		return index > 0 ? index + STATIC_TABLE_SIZE : 0;
	}

	/** 获取名称对应的值索引 */
	public int findValue(int index, String value) {
		if (index < 61) {
			return getStaticValue(index, value);
		}
		index = table.find(index - STATIC_TABLE_SIZE, value);
		return index > 0 ? index + STATIC_TABLE_SIZE : 0;
	}

	/** 添加动态索引 */
	public void add(String name, String value) {
		final Item item = new Item(name, value);

		// 超过限制需要逐出
		while (table.size() + item.size() > max) {
			table.remove();
		}

		table.add(item);
	}

	/** 更新动态表大小(估计字节) */
	public void update(int value) {
		max = value;

		// 超过限制需要逐出
		while (table.size() > max) {
			table.remove();
		}
	}

	public int getMaxHeaderListSize() {
		return maxHeaderListSize;
	}

	public void setMaxHeaderListSize(int value) {
		maxHeaderListSize = value;
	}

	final static int STATIC_TABLE_SIZE = 61;

	final static int getStaticName(String name) {
		switch (name) {
			case AUTHORITY:
				return 1;
			case METHOD:
				return 2;
			// case METHOD:
			// return 3;
			case PATH:
				return 4;
			// case PATH:
			// return 5;
			case SCHEME:
				return 6;
			// case SCHEME:
			// return 7;
			case STATUS:
				return 8;
			// case STATUS:
			// return 9;
			// case STATUS:
			// return 10;
			// case STATUS:
			// return 11;
			// case STATUS:
			// return 12;
			// case STATUS:
			// return 13;
			// case STATUS:
			// return 14;
			case HTTP.Accept_Charset:
				return 15;
			case HTTP.Accept_Encoding:
				return 16;
			case HTTP.Accept_Language:
				return 17;
			case HTTP.Accept_Ranges:
				return 18;
			case HTTP.Accept:
				return 19;
			case HTTP.Access_Control_Allow_Origin:
				return 20;
			case HTTP.Age:
				return 21;
			case HTTP.Allow:
				return 22;
			case HTTP.Authorization:
				return 23;
			case HTTP.Cache_Control:
				return 24;
			case HTTP.Content_Disposition:
				return 25;
			case HTTP.Content_Encoding:
				return 26;
			case HTTP.Content_Language:
				return 27;
			case HTTP.Content_Length:
				return 28;
			case HTTP.Content_Location:
				return 29;
			case HTTP.Content_Range:
				return 30;
			case HTTP.Content_Type:
				return 31;
			case HTTP.Cookie:
				return 32;
			case HTTP.Date:
				return 33;
			case HTTP.ETag:
				return 34;
			case HTTP.Expect:
				return 35;
			case HTTP.Expires:
				return 36;
			case HTTP.From:
				return 37;
			case HTTP.Host:
				return 38;
			case HTTP.If_Match:
				return 39;
			case HTTP.If_Modified_Since:
				return 40;
			case HTTP.If_None_Match:
				return 41;
			case HTTP.If_Range:
				return 42;
			case HTTP.If_Unmodified_Since:
				return 43;
			case HTTP.Last_Modified:
				return 44;
			case HTTP.Link:
				return 45;
			case HTTP.Location:
				return 46;
			case HTTP.Max_Forwards:
				return 47;
			case HTTP.Proxy_Authenticate:
				return 48;
			case HTTP.Proxy_Authorization:
				return 49;
			case HTTP.Range:
				return 50;
			case HTTP.Referer:
				return 51;
			case HTTP.Refresh:
				return 52;
			case HTTP.Retry_After:
				return 53;
			case HTTP.Server:
				return 54;
			case HTTP.Set_Cookie:
				return 55;
			case HTTP.Strict_Transport_Security:
				return 56;
			case HTTP.Transfer_Encoding:
				return 57;
			case HTTP.User_Agent:
				return 58;
			case HTTP.Vary:
				return 59;
			case HTTP.Via:
				return 60;
			case HTTP.WWW_Authenticate:
				return 61;
			default:
				return 0;
		}
	}

	final static int getStaticValue(int name, String value) {
		if (value == null) {
			return name;
		}
		switch (value) {
			case GET:
				return 2;
			case POST:
				return 3;
			case "/":
				return 4;
			case "/index.html":
				return 5;
			case "http":
				return 6;
			case "https":
				return 7;
			case "200":
				return 8;
			case "204":
				return 9;
			case "206":
				return 10;
			case "304":
				return 11;
			case "400":
				return 12;
			case "404":
				return 13;
			case "500":
				return 14;
			case "":
				return 15;
			case "gzip, deflate":
				return 16;
			default:
				return 0;
		}
	}

	final static Item[] STATIC_TABLE = new Item[] { // 静态头表
			new Item(null, null), //
			new Item(AUTHORITY, null), //
			new Item(METHOD, "GET"), //
			new Item(METHOD, "POST"), //
			new Item(PATH, "/"), //
			new Item(PATH, "/index.html"), //
			new Item(SCHEME, "http"), //
			new Item(SCHEME, "https"), //
			new Item(STATUS, "200"), //
			new Item(STATUS, "204"), //
			new Item(STATUS, "206"), //
			new Item(STATUS, "304"), //
			new Item(STATUS, "400"), //
			new Item(STATUS, "404"), //
			new Item(STATUS, "500"), //
			new Item(HTTP.Accept_Charset, null), //
			new Item(HTTP.Accept_Encoding, "gzip, deflate"), //
			new Item(HTTP.Accept_Language, null), //
			new Item(HTTP.Accept_Ranges, null), //
			new Item(HTTP.Accept, null), //
			new Item(HTTP.Access_Control_Allow_Origin, null), //
			new Item(HTTP.Age, null), //
			new Item(HTTP.Allow, null), //
			new Item(HTTP.Authorization, null), //
			new Item(HTTP.Cache_Control, null), //
			new Item(HTTP.Content_Disposition, null), //
			new Item(HTTP.Content_Encoding, null), //
			new Item(HTTP.Content_Language, null), //
			new Item(HTTP.Content_Length, null), //
			new Item(HTTP.Content_Location, null), //
			new Item(HTTP.Content_Range, null), //
			new Item(HTTP.Content_Type, null), //
			new Item(HTTP.Cookie, null), //
			new Item(HTTP.Date, null), //
			new Item(HTTP.ETag, null), //
			new Item(HTTP.Expect, null), //
			new Item(HTTP.Expires, null), //
			new Item(HTTP.From, null), //
			new Item(HTTP.Host, null), //
			new Item(HTTP.If_Match, null), //
			new Item(HTTP.If_Modified_Since, null), //
			new Item(HTTP.If_None_Match, null), //
			new Item(HTTP.If_Range, null), //
			new Item(HTTP.If_Unmodified_Since, null), //
			new Item(HTTP.Last_Modified, null), //
			new Item(HTTP.Link, null), //
			new Item(HTTP.Location, null), //
			new Item(HTTP.Max_Forwards, null), //
			new Item(HTTP.Proxy_Authenticate, null), //
			new Item(HTTP.Proxy_Authorization, null), //
			new Item(HTTP.Range, null), //
			new Item(HTTP.Referer, null), //
			new Item(HTTP.Refresh, null), //
			new Item(HTTP.Retry_After, null), //
			new Item(HTTP.Server, null), //
			new Item(HTTP.Set_Cookie, null), //
			new Item(HTTP.Strict_Transport_Security, null), //
			new Item(HTTP.Transfer_Encoding, null), //
			new Item(HTTP.User_Agent, null), //
			new Item(HTTP.Vary, null), //
			new Item(HTTP.Via, null), //
			new Item(HTTP.WWW_Authenticate, null), //
	};

	static class Item {
		final String name;
		final String value;

		public Item(String name, String value) {
			this.name = name;
			this.value = value;
		}

		int size() {
			if (value == null) {
				return 32 + name.length();
			}
			return 32 + name.length() + value.length();
		}
	}

	class Table {
		// 环形模式：尾部插入，头部移除，避免整体移动
		// 未翻转时：ABCD代表添加顺序，P代表自增位置
		// 索引逻辑：[C62 B63 A64]
		// 实际存储：[A=0 B=1 C=2]
		// 索引换算：C(62)-61=1 P(3)-1=2 2%3=2
		// 索引换算：B(63)-61=2 P(3)-2=1 1%3=1
		// 索引换算：A(64)-61=3 P(3)-3=0 0%3=0
		// 已翻转时：A被覆盖（逐出）
		// 索引逻辑：[D62 C63 B64] A-
		// 实际存储：[D=0 B=1 C=2]
		// 索引换算：D(62)-61=1 P(4)-1=3 3%3=0
		// 索引换算：C(63)-61=2 P(4)-2=2 2%3=2
		// 索引换算：B(64)-61=3 P(4)-3=1 1%3=1
		// 注意：foot head 始终在增加直至溢出

		private Item[] items;
		private int foot, head;
		private int size;

		Table() {
			items = new Item[100];
			head = 0;
			foot = 0;
		}

		int size() {
			return size;
		}

		/** index:1~n */
		Item get(int index) {
			index = (head - index) % items.length;
			return items[index];
		}

		/** index:1~n */
		String getName(int index) {
			index = (head - index) % items.length;
			return items[index].name;
		}

		/** index:1~n */
		String getValue(int index) {
			index = (head - index) % items.length;
			return items[index].value;
		}

		// 查找
		// START P2-1=1 ~ END P2-L3=-1;
		// (S1)%3=1 P2-1+61=62
		// (S0)%3=0 P2-0+61=63
		// START P3-1=2 ~ END P3-L3=0;
		// (S2)%3=2 P3-2+61=62
		// (S1)%3=1 P3-1+61=63
		// (S0)%3=0 P3-0+61=64
		// START P4-1=3 ~ END P4-L3=1
		// (S3)%3=0 P4-3+61=62
		// (S2)%3=2 P4-2+61=63
		// (S1)%3=1 P4-1+61=64

		int find(String name) {
			Item item;
			int index = head - 1;
			for (; index >= foot; index--) {
				item = items[index % items.length];
				if (item.name.equalsIgnoreCase(name)) {
					return head - index;
				}
			}
			return 0;
		}

		int find(String name, String value) {
			Item item;
			int index = head - 1;
			for (; index >= foot; index--) {
				item = items[index % items.length];
				if (item.name.equalsIgnoreCase(name)) {
					if (value == null) {
						if (item.value == null) {
							return head - index;
						}
					} else {
						if (item.value != null) {
							if (item.value.equalsIgnoreCase(value)) {
								return head - index;
							}
						}
					}
				}
			}
			return 0;
		}

		int find(int index, String value) {
			// 首先比对之前查找的名称索引
			// 如果不匹配后续需要判断名称，因此此步骤无法省略
			Item item = items[(head - index) % items.length];
			if (value == null) {
				if (item.value == null) {
					return index;
				}
			} else {
				if (item.value != null) {
					if (item.value.equals(value)) {
						return index;
					}
				}
			}

			String name = item.name;
			index = head - index - 1;
			for (; index >= foot; index--) {
				item = items[index % items.length];
				if (item.name.equalsIgnoreCase(name)) {
					if (value == null) {
						if (item.value == null) {
							return head - index;
						}
					} else {
						if (item.value != null) {
							if (item.value.equalsIgnoreCase(value)) {
								return head - index;
							}
						}
					}
				}
			}
			return 0;
		}

		void add(Item item) {
			extend();

			items[head % items.length] = item;
			size += item.size();
			head++;
		}

		void remove() {
			final Item item = items[foot % items.length];
			size -= item.size();
			foot++;
		}

		void extend() {
			// 检查并扩展数组
			if (head - foot == items.length) {
				final Item[] news = new Item[items.length + 32];
				for (int index = 0; index < head - foot; index++) {
					news[index] = items[(foot + index) % items.length];
				}
				items = news;
			}
		}
	}
}