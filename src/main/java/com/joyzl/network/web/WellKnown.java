package com.joyzl.network.web;

import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * RFC 5785 8615
 * <p>
 * https://www.iana.org/assignments/well-known-uris/
 * </p>
 * 
 * @author ZhangXi 2024年11月21日
 */
@ServletURI(uri = "/.well-known/*")
public class WellKnown extends WEBServlet {

	@Override
	protected void options(Request request, Response response) throws Exception {
		response.addHeader("Allow", "OPTIONS, GET");
		response.setStatus(HTTPStatus.OK);
	}

	@Override
	protected void get(Request request, Response response) throws Exception {
		execute(request, response);
	}

	private void execute(Request request, Response response) {

	}
}