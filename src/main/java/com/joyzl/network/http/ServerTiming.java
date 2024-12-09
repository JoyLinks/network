package com.joyzl.network.http;

/**
 * 服务端性能参数收集
 * 
 * @author ZhangXi 2024年11月26日
 */
public class ServerTiming extends Header {

	public final static String NAME = HTTP.Server_Timing;
	public final static String total = "total";
	public final static String cache = "cache";
	public final static String miss = "miss";
	public final static String app = "app";
	public final static String db = "db";

	// Server-Timing: miss, db;dur=53, app;dur=47.2
	// Server-Timing: cache;desc="Cache Read";dur=23.2
	// Trailer: Server-Timing
	// Server-Timing: total;dur=123.4

	private String name;
	private long start;
	private long done;

	public ServerTiming(String name) {
		this.name = name;
	}

	@Override
	public String getHeaderName() {
		return HTTP.Server_Timing;
	}

	@Override
	public String getHeaderValue() {
		return name + ';' + "dur=" + duration();
	}

	@Override
	public void setHeaderValue(String value) {
		// TODO Auto-generated method stub
	}

	public int duration() {
		return (int) (done - start);
	}

	public void start() {
		start = System.currentTimeMillis();
	}

	public void done() {
		done = System.currentTimeMillis();
	}

	public long getStart() {
		return start;
	}

	public long getDone() {
		return done;
	}
}