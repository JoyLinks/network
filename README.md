# network

#### 介绍

JOYZL.network 是高性能网络服务框架，提供 B/S HTTP WEB 服务和 C/S ODBS 网络服务框架；
可用于快速开发C/S和B/S服务器和客户端。

#### 软件架构

JOYZL.network 是NIO服务端和客户端框架，可以快速轻松的开发网络应用程序，极大的简化了网络应用编程。
底层实现TCP基于NIO 2异步逻辑，UDP基于NIO 1选择器逻辑，缓存基于ByteBuffer获得最少物理读写次数（零复制），
没有繁重的封装，重点在于足够简单轻量、性能优异、稳定灵活。

在同一个运行实例中可同时提供C/S和B/S服务，极大的简化了业务开发，这在物联网(IoT)应用和同时提供C/S和B/S服务的应用中尤为重要。

二进制和JSON序列化使用[ODBS](https://github.com/JoyLinks/odbs)组件，除外没有其它第三方依赖；

**设计原则**

1. 更少的资源消耗；
2. 更高的吞吐量、更低的延迟；
3. 零复制、最小化内存复制和协议解析一次性读取。

**ODBS NET**

1. 采用[ODBS](https://github.com/JoyLinks/odbs)二进制序列化通信协议；
2. 基本服务端(ODBSServer)和客户端(ODBSClient)实现；
3. 单个客户端实例支持最大127并行消息请求；
4. 客户端实现链路检测、自动重连、消息超时机制；
5. 支持多端消息广播。

**HTTP WEB**

1. 基本HTTP WEB服务端(WEBServer)和客户端(WEBClient)实现；
2. 基本HTTP资源服务(FileServlet)可作为前端容器发布网站；
3. 支持常规静态资源文件的自动压缩(Gzip和Deflate)；
4. 支持WEBSocket实现自定义消息处理；
5. 支持WEBServlet实现自定义业务和功能扩展。
6. 支持Servlet通配符匹配URI;

注意：与Tomcat等容器不同，不支持JSP和WAR的发布运行。

#### 使用说明

添加 Maven 依赖，在项目的pom.xml文件中

```xml
<dependency>
	<groupId>com.joyzl</groupId>
	<artifactId>network</artifactId>
	<version>2.1.1</version>
</dependency>
```

##### 创建BS静态网站

```java
// 初始化线程池
Executor.initialize(0);
// 创建WEB服务端实例
// "\web"为网站资源目录
WEBServerBase server = new WEBServerBase("\web");
server.start(null, 80);
...
// 关闭停止服务
server.close();
Executor.shutdown();
```

通过浏览器请求http://192.168.0.1地址，即可呈现网页；其中IP地址应为服务运行所在服务器地址。

##### 创建具有Servlet功能扩展的BS网站

首先在Java项目中创建用于实现Servlet类的package，并在其中继承Servlet类实现扩展功能；

```java
package com.joyzl.network.web.test;

public class TestServlet extends WEBServlet {

	protected void get(WEBRequest request, WEBResponse response) throws Exception {
		response.setContent("TEST Servlet");
		...
		response.setStatus(HTTPStatus.OK);
	}
}
```

```java
// 初始化线程池
Executor.initialize(0);
// 创建WEB服务端实例
// "\web"为网站资源目录
WEBServerBase server = new WEBServerBase("\web");
server.getServlets().bind("/test", new TestServlet());
server.start(null, 80);
...
```

通过浏览器请求http://192.168.0.1/test地址，即可转由TestServlet实例处理请求；其中IP地址应为服务运行所在服务器地址。

##### 创建只有Servlet扩展的BS服务

```java
// 初始化线程池
Executor.initialize(0);
// 创建WEB服务端实例
WEBServerBase server = new WEBServerBase();
server.getServlets().bind("/test/*", new TestServlet());
server.start(null, 80);
...
```

可通过扫描功能添加包中的多个Servlet，每个Servlet须通过ServletURI注解设定URI；

```java
package com.joyzl.network.web.test;
@ServletURI(uri = "/test/*")
public class TestServlet extends WEBServlet {

	protected void get(WEBRequest request, WEBResponse response) throws Exception {
		...
	}
}
```

```java
// 初始化线程池
Executor.initialize(0);
// 创建WEB服务端实例
WEBServerBase server = new WEBServerBase();
server.scan("com.joyzl.network.web.test");
server.start(null, 80);
...
```

以下类提供基本的Servlet结构：

WEBServlet 提供基本的请求方法，继承此类重写对应的请求方法实现业务逻辑；

CROSServlet 提供基本的跨越逻辑，继承此类重新对应的请求方法实现可跨域请求的业务逻辑；

WEBFileServlet 提供基本的文件资源请求响应逻辑实现。

##### 创建CS服务端


首先应有单独的工程定义实体对象，服务端和客户端均需要引用相同的定义。

```java
package com.joyzl.network.odbs.test;

import com.joyzl.network.odbs.ODBSMessage;

public class Action extends ODBSMessage {

	private int state;
	private String error;

	public int getState() {
		return state;
	}

	public void setState(int value) {
		state = value;
	}

	public String getError() {
		return error;
	}

	public void setError(String value) {
		error = value;
	}

	@Override
	public String toString() {
		return state + ":" + error;
	}
}
```


创建服务端

```java
Executor.initialize(0);
ODBS odbs = ODBS.initialize("com.joyzl.network.test");
ODBSServer<Action> server = new ODBSServer<Action>(
	new ODBSServerHandler<Action>(odbs){
		
		public ServerHandler(ODBS o) {
			super(o);
		}
	
		@Override
		public void connected(ChainChannel<Action> chain) throws Exception {
			super.connected(chain);
			...
		}
	
		@Override
		public void received(ChainChannel<Action> chain, Action message) throws Exception {
			super.received(chain, message);
	
			if (message == null) {
			} else {
				...
				chain.send(message);
			}
		}
	
		@Override
		public void sent(ChainChannel<Action> chain, Action message) throws Exception {
			super.sent(chain, message);
		}
	
		@Override
		public void disconnected(ChainChannel<Action> chain) throws Exception {
			...
		}
	
		@Override
		public void error(ChainChannel<Action> chain, Throwable e) {
			e.printStackTrace();
		}
	}
	, null, 1200);

...

// 关闭服务
Executor.shutdown();
```

其中1230为监听端口

##### 创建CS客户端


```java
Executor.initialize(0);
ODBS odbs = ODBS.initialize("com.joyzl.network.test");
ODBSClient<Action> client = new ODBSClient<>(
	new ODBSClientHandler<Action>(){

		public TestODBSClient() {
			super(odbs);
		}
	
		@Override
		public void beat(ChainChannel<Action> chain) throws Exception {	
			chain.send(new Action());
		}
	
		@Override
		public void connected(ChainChannel<Action> chain) throws Exception {
			super.connected(chain);

			...

		}
	
		@Override
		public void received(ChainChannel<Action> chain, Action message) throws Exception {
			if (message == null) {
				super.received(chain, message);
			} else {

				...

				super.received(chain, message);
			}
		}
	
		@Override
		public void sent(ChainChannel<Action> chain, Action message) throws Exception {
			super.sent(chain, message);

			...

		}
	
		@Override
		public void disconnected(ChainChannel<Action> chain) throws Exception {
			...
		}
	
		@Override
		public void error(ChainChannel<Action> chain, Throwable e) {
			e.printStackTrace();
		}
	}
	, "192.168.0.1", 1200);

...

// 关闭服务
Executor.shutdown();
```

其中"192.168.0.1"和1200为服务端所在地址和端口。

#### 参与贡献

中翌智联 www.joyzl.com

华腾智联 www.huatens.com

张希 ZhangXi


感谢以下网站贡献的资源

[MDN Web Docs](https://developer.mozilla.org/zh-CN/docs/Web)

[www.ip33.com](http://www.ip33.com)
的在线工具
[CRC](http://www.ip33.com/crc.html)
[LRC](http://www.ip33.com/lrc.html)
[BCC](http://www.ip33.com/bcc.html)
为我们提供了有力的帮助。

---


中国制造，智造中国

Made in China, Intelligent China

