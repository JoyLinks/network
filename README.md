# JOYZL Network

#### 介绍

JOYZL network 是高性能网络通信框架，实现 HTTP, ODBS, TLS 通信协议。

更多详细信息和应用请访问 [www.joyzl.com](http://www.joyzl.com) [www.huatens.com](http://www.huatens.com)


JOYZL network 是NIO服务端和客户端通信框架，可以快速轻松的开发网络应用程序，极大的简化了网络应用编程。
底层实现TCP基于NIO 2异步逻辑，UDP基于NIO 1选择器逻辑，缓存基于ByteBuffer获得最少物理读写次数（零复制），
没有繁重的封装，重点在于足够简单轻量、性能优异、稳定灵活。

可在同一个运行实例中可同时提供C/S和B/S服务，极大的简化了业务开发，这在物联网(IoT)应用和同时提供C/S和B/S服务的应用中尤为重要。

二进制和JSON序列化使用[ODBS](https://github.com/JoyLinks/odbs)组件，没有其它第三方依赖。

**ODBS**

ODBS通信主要用于C/S通信场景，客户端通过单个连接实现与服务端的长连接通信。
1. 采用[ODBS](https://github.com/JoyLinks/odbs)二进制序列化通信协议；
2. 基本服务端(ODBSServer)和客户端(ODBSClient)实现；
3. 客户端实现链路检测、自动重连、消息超时机制；
4. 支持多端消息广播和多路复用。

**HTTP**

用于B/S的HTTP超文本传输协议。
1. 实现 HTTP1.0 HTTP1.1 HTTP2；
2. 基本服务端(HTTPServer)和客户端(HTTPClient)实现；
3. WEBSocket协议实现；


**TLS**

1. 实现 TLS 1.0 TLS 1.1 TLS 1.2 TLS 1.3；
2. 支持Java默认提供的加密套件；
3. 支持第三方加密套件，须额外加载；
4. 支持Java KeyStore和PEM格式存储的证书；


#### 感谢以下网站贡献的资源

[MDN Web Docs](https://developer.mozilla.org/zh-CN/docs/Web)

[www.ip33.com](http://www.ip33.com)
的在线工具
[CRC](http://www.ip33.com/crc.html)
[LRC](http://www.ip33.com/lrc.html)
[BCC](http://www.ip33.com/bcc.html)
为我们提供了有力的帮助。



中国制造，智造中国

Made in China, Intelligent China

