package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.joyzl.network.Utility;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;
import com.joyzl.network.http.WWWAuthenticate;

/**
 * Basic 身份验证 RFC 7617
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateBasic extends Authenticate {

	public final static String TYPE = "Basic";

	private final Map<String, String> USERS = new HashMap<>();
	private String users;
	private String www;

	public AuthenticateBasic(String path) {
		super(path);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		// Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
		// Authorization: Basic name:password

		String a = request.getHeader(Authorization.NAME);
		if (a != null && a.length() > 6) {
			if (a.startsWith(TYPE)) {
				a = a.substring(TYPE.length() + 1);
				a = new String(Base64.getDecoder().decode(a), StandardCharsets.UTF_8);
				int colon = a.indexOf(HTTPCoder.COLON);
				if (colon > 0) {
					final String password = USERS.get(a.substring(0, colon));
					if (password != null) {
						a = a.substring(colon + 1);
						if (getAlgorithm() == null) {
							// 未指定密码加密方式，采用明文密码
							if (a.contains(password)) {
								return true;
							}
						} else {
							try {
								final MessageDigest md = MessageDigest.getInstance(getAlgorithm());
								md.update(a.getBytes(StandardCharsets.UTF_8));
								a = Utility.hex(md.digest());
							} catch (NoSuchAlgorithmException e) {
								throw new RuntimeException(e);
							}
							if (Utility.same(a, password)) {
								return true;
							}
						}
					}
				}
			}
		}
		response.setStatus(HTTPStatus.UNAUTHORIZED);
		response.addHeader(WWWAuthenticate.NAME, www);
		// TEST cs531a5
		response.addHeader(ContentType.NAME, "text/html");
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		return false;
	}

	public void setRealm(String vlaue) {
		super.setRealm(vlaue);
		www = TYPE + " realm=\"" + vlaue + HTTPCoder.QUOTE;
	}

	public void setUsers(String value) throws IOException {
		users = value;
		load(new File(value));
	}

	public String getUsers() {
		return users;
	}

	public void load(File file) throws IOException {
		// # ***
		// name=value
		// # ***
		// name:MD5/SHA(password)

		USERS.clear();

		int colon = 0, c;
		boolean ignore = false;
		final StringBuilder builder = Utility.getStringBuilder();
		try (final FileInputStream input = new FileInputStream(file);
			final InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);) {
			while ((c = reader.read()) >= 0) {
				if (c == HTTPCoder.NUM) {
					ignore = builder.length() == 0;
					continue;
				}
				if (c == HTTPCoder.COLON) {
					if (ignore) {
						continue;
					} else {
						colon = builder.length();
					}
				} else if (c == HTTPCoder.CR || c == HTTPCoder.LF) {
					if (!ignore) {
						if (builder.length() > 0 && colon > 0) {
							USERS.put(builder.substring(0, colon), builder.substring(colon + 1));
						}
					}
					builder.setLength(0);
					ignore = false;
					colon = 0;
					continue;
				}
				builder.append((char) c);
			}
		}
	}

	public void save(File file) throws IOException {
		try (final FileOutputStream input = new FileOutputStream(file, true);
			final OutputStreamWriter writer = new OutputStreamWriter(input, StandardCharsets.UTF_8);) {
			writer.write("# ");
			writer.write(LocalDateTime.now().toString());
			writer.write(HTTPCoder.CR);
			for (Entry<String, String> user : USERS.entrySet()) {
				writer.write(user.getKey());
				writer.write(HTTPCoder.COLON);
				writer.write(user.getValue());
				writer.write(HTTPCoder.CR);
			}
		}
	}
}