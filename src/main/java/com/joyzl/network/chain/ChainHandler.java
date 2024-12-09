/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 连接对象的业务逻辑处理接口
 * 
 * <p>
 * 发送流程:
 * <ol>
 * <li>Chain.send(M)</li>
 * <li>ChainHandler.send(Chain,M)</li>
 * <li>ChainHandler.encode(Chain,M,DataBuffer)</li>
 * <li>Chain.write(DataBuffer)</li>
 * <li>ChainHandler.sent(Chain,M)</li>
 * </ol>
 * 接收流程:
 * <ol>
 * <li>Chain.receive()</li>
 * <li>ChainHandler.receive(Chain)</li>
 * <li>Chain.read(DataBuffer)</li>
 * <li>ChainHandler.decode(Chain,DataBuffer)</li>
 * <li>ChainHandler.received(Chain,M)</li>
 * </ol>
 * </p>
 * 
 * <p>
 * 对象重用与回收，原则：谁用完谁负责释放；<br>
 * DataBuffer对象在接收数据后，如果业务逻辑持有并使用数据对象，必须用完后释放；<br>
 * DataBuffer在发送数据后，Chain会用完后自动将其释放；<br>
 * 接收的消息对象由ChainHandler.decode()方法创建，业务逻辑必须用完后释放；<br>
 * 发送的消息对象在ChainHandler.sent()方法中释放或直接重用；<br>
 * </p>
 * 
 * <p>
 * 消息处理实现建议：<br>
 * connected()方法中通常情况下需要开始接收数据，应调用Chain.receive()方法；
 * 但对于HTTP客户端则不同，必须在请求发送之后才能接收到数据；减少无用的网络数据读取等待能够提高IO线程利用率。<br>
 * receive()方法为接收数据提供合适的数据缓存对象DataBuffer，根据链路和协议类型选择最合适的数据缓存大小；
 * 例如UDP链路其特性决定了能够发送和接收的数据包大小限制，在ModbusTCP协议中消息也都设计的尽可能短小。<br>
 * decode()方法将接收到的数据转换为消息对象(解码)，应当设计可重复使用的消息对象避免频繁通信产生垃圾对象；
 * 解码必须考虑网络数据流特性：断帧、粘连、多帧等情况；解码方法应当快速完成避免处理业务逻辑。<br>
 * received()方法接收解码后的消息对象执行业务处理，如果业务逻辑需要耗费较长时间应当考虑投递到业务线程执行；
 * 根据消息对象此方法应调用Chain.receive()方法继续接收后续数据或停止接收网络数据。<br>
 * beat()方法用于空闲时监测网络链路连通性(心跳)；在没有数据发送和接收一段时间后此方法会被调用；
 * 应当调用Chain.send()发送极少字节的消息到服务端，服务端通常直接回复相同消息给客户端；
 * 链路心跳监测由客户端主动发起，服务端被动接收，因此所有的从链路不会调用此方法。<br>
 * send()方法处理链路消息对象排队逻辑，以及决定如何排队(例如WebSocket的控制消息可插队发送)；
 * 需要优先发送的消息应当插入队列首部，普通消息排列在队列尾部。<br>
 * encode()方法处理消息对象编码逻辑，此方法应当快速完成并视情况对消息进行分片处理；
 * 一个较大的消息如果不分片可能会导致链路长时间和较多内存占用<br>
 * sent()方法处理已经发送的消息对象，应当在此方法对消息对象进行状态进行判断是继续发送还是回收或重用；
 * 消息分片发送时此方法会被多次调用，业务逻辑应当能够区分此情况；<br>
 * disconnected()方法在连接断开时调用，无论是客户端主动断开还是服务端主动断开均被调用；
 * 链路断开时应当处理一些收尾工作，此后链路对象将被释放。<br>
 * error()方法在出现异常时被调用，ChainHandler中的方法除error()以外所有异常都会被捕获并触发此方法；
 * 通常情况下发送和接收已停止，因此应当在error()方法实现中调用Chain.close()方法关闭链路；
 * 如果需要尝试恢复链路业务应当根据实际情况重新尝试发送或接收数据。
 * </p>
 * 
 * <p>
 * 链路提供消息队列Chain.messages()，默认情况下通过Chain.send()方法发送消息对象将自动添加到消息队列，
 * 如果当前没有排队的消息也没有发送中的消息，则会立即发送，发送完成后需要根据业务逻辑手动移除完成的消息，并调用Chain.send(null)发送其余排队消息。
 * 如果有消息需要优先发送，可以在ChainHandler.send()方法中手动添加到消息头部；
 * 如果消息需要等待远程回复之后才能移除，可以在ChainHandler.sent()中判断是否移除，并且暂停发送其余消息。
 * 
 * @author ZhangXi 2019年7月12日
 *
 */
public interface ChainHandler<M> {

	/**
	 * 获取读取数据超时时间（毫秒）
	 * 
	 * @return 默认3秒超时限制
	 */
	default long getTimeoutRead() {
		return 3000L;
	}

	/**
	 * 获取发送数据超时时间（毫秒）
	 * 
	 * @return 默认3秒超时限制
	 */
	default long getTimeoutWrite() {
		return 3000L;
	}

	/**
	 * 连接建立
	 * <p>
	 * 如果要立即开始接收,应当在此方法中调用chain.receive()
	 */
	void connected(ChainChannel<M> chain) throws Exception;

	/**
	 * 数据接收
	 * <p>
	 * 此方法实现应当根据链路业务特性返回合适的用于接收数据的DataBuffer实例,
	 * 例如:{@link com.joyzl.network.buffer.DataBuffer#getB2048()} ,
	 * {@link com.joyzl.network.buffer.DataBuffer#getB65536()}
	 */
	// DataBuffer receive(ChainChannel<M> chain) throws Exception;

	/**
	 * 字节解码为对象
	 * <p>
	 * 此方法将字节解码为业务对象,如果解码成功应当返回对象实例; 如果返回null通常情况下表示已接收数据不完整,需要继续接收更多数据完成解码;
	 * 此方法实现必须考虑各种数据帧情况:断帧、粘连、多帧等，可能需要进行多次数据接收才能完成解码。
	 * <p>
	 * <p>
	 * 处理数据粘连或接收不足时解码的建议： <br>
	 * 1.判断数据包所需的最小长度，如果不足返回null以便继续接收数据；<br>
	 * 2.判断数据包最大长度，如果超过最大长度应当丢弃数据并返回null；<br>
	 * 3.对于具有前导长度标识的数据包可根据长度判断是否接收到足够的数据，然后开始解码转换为对象； <br>
	 * 4.为了处理多个数据包，如果解码完成还有残留数据，将再次调用此方法尝试继续解包。
	 * 
	 * @param chain 链路
	 * @param reader 如果接收数据超时可能为null
	 */
	M decode(ChainChannel<M> chain, DataBuffer reader) throws Exception;

	/**
	 * 对象已接收
	 * <p>
	 * 此方法应快速完成,否则请考虑投递到其它线程运行。
	 * 
	 * @param chain
	 * @param message 如果接收数据超时可能为null
	 */
	void received(ChainChannel<M> chain, M message) throws Exception;

	/**
	 * 心跳
	 * 
	 * @param chain 链路
	 */
	default void beat(ChainChannel<M> chain) throws Exception {
	};

	/**
	 * 对象编码为字节
	 * <p>
	 * 链路会多次调用此方法，直至消息对象编码返回null表示消息对象编码完成；
	 * 消息需要分包以及多次编码发送的情形，例如大文件，可分多次编码，每次返回的编码数据会被立即发送。
	 * 消息对象设计应当考虑编码状态，以明确对象编码是否完成。
	 * 
	 * @param chain 链路
	 * @param message 要编码的对象
	 */
	DataBuffer encode(ChainChannel<M> chain, M message) throws Exception;

	/**
	 * 对象和数据已发送
	 * <p>
	 * 数据已投递到网络，但不能确保数据已经全部发送完成。
	 * 
	 * @param chain 链路
	 * @param message 已发送的对象
	 */
	void sent(ChainChannel<M> chain, M message) throws Exception;

	/**
	 * 连接断开
	 * 
	 * @param chain 链路
	 */
	void disconnected(ChainChannel<M> chain) throws Exception;

	/**
	 * 异常捕获
	 * <p>
	 * 此方法实现不应当抛出任何异常
	 */
	void error(ChainChannel<M> chain, Throwable e);
}