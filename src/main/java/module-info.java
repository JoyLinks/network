/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
module com.joyzl.network {
	requires transitive com.joyzl.odbs;

	exports com.joyzl.network;
	exports com.joyzl.network.codec;
	exports com.joyzl.network.chain;
	exports com.joyzl.network.buffer;
	exports com.joyzl.network.verifies;

	exports com.joyzl.network.session;
	exports com.joyzl.network.odbs;
	exports com.joyzl.network.http;
	exports com.joyzl.network.web;
	exports com.joyzl.network.websocket;
}
