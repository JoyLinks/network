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
3. 支持WEBSocket实现自定义消息处理；
4. 
5. 
6. 支持WEBServlet实现自定义业务和功能扩展。

注意：与Tomcat等容器不同，不支持JSP和WAR的发布运行。

基于当前框架实现的企业级WEB容器，支持集群部署和负载均衡，请转到[JOYZL HTTP WEB](www.hoyzl.com)获取更多信息。

#### 使用说明

添加 Maven 依赖，在项目的pom.xml文件中

```xml
<dependency>
	<groupId>com.joyzl</groupId>
	<artifactId>network</artifactId>
	<version>2.0.1</version>
</dependency>
```


#### 参与贡献

中翌智联 www.joyzl.com

华腾智联 www.huatens.com

张希 ZhangXi


---


中国制造，智造中国

Made in China, Intelligent China

