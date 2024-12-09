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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.joyzl.network.Utility;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;

/**
 * Digest 身份验证 RFC 7616
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateDigest extends Authenticate {

	public final static String TYPE = "Digest";
	/** 完整保护 qop="auth-int" */
	final static String AUTH_INT = "auth-int";
	/** 身份验证 qop="auth" */
	final static String AUTH = "auth";

	final static byte[] COLON = new byte[] { ':' };

	private final Map<String, String> USERS = new HashMap<>();
	private String users;

	public AuthenticateDigest(String path) {
		super(path);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		// Authorization: Digest realm="",response="",username="",cnonce="",nc=
		// Authorization: Digest
		// realm="Colonial Place",
		// response="4b92e1b584783d2a234ffc1786a32e86"
		// username="mln",
		// uri="http://192.168.2.19/a4-test/limited2/foo/bar.txt",
		// cnonce="014a54548c61ba03827ef6a4dc2f7b4c",
		// qop=auth,
		// nc=00000001,
		// userhash,
		// nonce="",
		// 不带引号:algorithm qop nc stale userhash

		String a = request.getHeader(Authorization.NAME);
		if (a != null && a.length() > 64) {
			if (a.startsWith(TYPE)) {
				String respons = null, username = null, cnonce = null, nc = null;
				String qop = null, nonce = null, opaque = null;
				// 解析参数
				int end, equal, start = TYPE.length() + 1;
				do {
					equal = a.indexOf(HTTPCoder.EQUAL, start);
					if (equal > 0) {
						end = a.indexOf(HTTPCoder.COMMA, equal + 1);
						if (end < 0) {
							end = a.length();
						}
						if (equal("response", a, start, equal)) {
							respons = a.substring(equal + 2, end - 1);
						} else if (equal("username", a, start, equal)) {
							username = a.substring(equal + 2, end - 1);
						} else if (equal("cnonce", a, start, equal)) {
							cnonce = a.substring(equal + 2, end - 1);
						} else if (equal("opaque", a, start, equal)) {
							opaque = a.substring(equal + 2, end - 1);
						} else if (equal("nonce", a, start, equal)) {
							nonce = a.substring(equal + 2, end - 1);
						} else if (equal("qop", a, start, equal)) {
							qop = a.substring(equal + 1, end);
						} else if (equal("nc", a, start, equal)) {
							nc = a.substring(equal + 1, end);
						}
						start = end + 1;
						while (start < a.length()) {
							if (Character.isWhitespace(a.charAt(start))) {
								start++;
								continue;
							}
							break;
						}
					} else {
						break;
					}
				} while (start < a.length());

				// System.out.println("response=" + respons);
				// System.out.println("username=" + username);
				// System.out.println("cnonce=" + cnonce);
				// System.out.println("opaque=" + opaque);
				// System.out.println("nonce=" + nonce);
				// System.out.println("qop=" + qop);
				// System.out.println("nc=" + nc);

				if (username != null && respons != null && cnonce != null && nonce != null && nc != null) {
					// 校验随机数
					if (check(nonce, opaque)) {
						// 校验参数 auth+md5
						if (qop == null || AUTH.equalsIgnoreCase(qop)) {

							// response=MD5(MD5(A1):nonce:nc:cnonce:qop:MD5(A2))
							// A1=username:realm:passwd
							// A2=Method:request-uri

							String A1, A2;
							// 此时A1为存储的密码
							A1 = USERS.get(username);
							if (A1 != null) {
								try {
									final MessageDigest md;
									// A1
									if (getAlgorithm() == null) {
										// 未指定加密算法，密码明文存储
										// 其余验证采用默认 MD5
										md = MessageDigest.getInstance("MD5");

										md.update(username.getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(getRealm().getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(A1.getBytes(StandardCharsets.UTF_8));
										A1 = Utility.hex(md.digest());
									} else {
										// 指定加密算法，密码加密存储
										md = MessageDigest.getInstance(getAlgorithm());

										// 由于机制限制密码必须连同 name:realm:password 加密
										// 存储的密码已经是 A1 值，此处无须额外加密
									}
									// A2
									md.reset();
									md.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
									md.update(COLON);
									md.update(request.getURL().getBytes(StandardCharsets.UTF_8));
									A2 = Utility.hex(md.digest());
									// RESPONSE
									md.reset();
									md.update(A1.getBytes(StandardCharsets.UTF_8));
									md.update(COLON);
									md.update(nonce.getBytes(StandardCharsets.UTF_8));
									md.update(COLON);
									md.update(nc.getBytes(StandardCharsets.UTF_8));
									md.update(COLON);
									md.update(cnonce.getBytes(StandardCharsets.UTF_8));
									md.update(COLON);
									md.update(AUTH.getBytes(StandardCharsets.UTF_8));
									md.update(COLON);
									md.update(A2.getBytes(StandardCharsets.UTF_8));
									a = Utility.hex(md.digest());

									if (Utility.same(a, respons)) {
										// A2
										md.reset();
										md.update(COLON);
										md.update(request.getURL().getBytes(StandardCharsets.UTF_8));
										A2 = Utility.hex(md.digest());
										// RSPAUTH
										md.reset();
										md.update(A1.getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(nonce.getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(nc.getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(cnonce.getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(AUTH.getBytes(StandardCharsets.UTF_8));
										md.update(COLON);
										md.update(A2.getBytes(StandardCharsets.UTF_8));
										a = Utility.hex(md.digest());

										authenticationInfo(request, response, a, cnonce, nc);
										return true;
									}
								} catch (NoSuchAlgorithmException e) {
									throw new RuntimeException(e);
								}
							} else {
								// 暂未支持 auth-int
								// response=MD5(H(username:realm:passwd):nonce:nc:cnonce:qop:H(Method:request-uri:H(entity-body)))
							}
						}
					}
				}
			}
		}
		wwwAuthenticate(request, response);
		return false;
	}

	boolean check(String nonce, String opaque) {
		if (opaque != null) {
			int start = opaque.length() / 2;
			long time = Long.parseUnsignedLong(nonce, start, nonce.length(), start);
			long current = System.currentTimeMillis();
			if (time < current && current - time < 60000) {
				return true;
			}
		} else {
			// 暂未校验
			return true;
		}
		return false;
	}

	/**
	 * 响应 Authentication-Info
	 */
	void authenticationInfo(Request request, Response response, String rspauth, String cnonce, String nc) {
		final String nano = Long.toUnsignedString(System.nanoTime(), Character.MAX_RADIX);
		final String time = Long.toUnsignedString(System.currentTimeMillis(), Character.MAX_RADIX);
		final StringBuilder builder = Utility.getStringBuilder();

		builder.append(TYPE);
		// realm
		builder.append(" realm=\"");
		builder.append(getRealm());
		builder.append("\"");
		// nextnonce
		builder.append(",nextnonce=\"");
		builder.append(nano);
		builder.append(time);
		builder.append("\"");
		// qop
		builder.append(",qop=");
		builder.append(AUTH);
		// rspauth
		builder.append(",rspauth=\"");
		builder.append(rspauth);
		builder.append("\"");
		// cnonce
		builder.append(",cnonce=\"");
		builder.append(cnonce);
		builder.append("\"");
		// nc
		builder.append(",nc=");
		builder.append(nc);
		response.addHeader(HTTP.Authentication_Info, builder.toString());
	}

	/**
	 * 响应 WWW-Authenticate
	 */
	void wwwAuthenticate(Request request, Response response) {
		response.setStatus(HTTPStatus.UNAUTHORIZED);

		final String nano = Long.toUnsignedString(System.nanoTime(), Character.MAX_RADIX);
		final String time = Long.toUnsignedString(System.currentTimeMillis(), Character.MAX_RADIX);
		final StringBuilder builder = Utility.getStringBuilder();

		builder.append(TYPE);
		// realm
		builder.append(" realm=\"");
		builder.append(getRealm());
		builder.append("\"");
		// domain
		builder.append(",domain=\"");
		builder.append(getPath());
		builder.append("\"");
		// nonce
		builder.append(",nonce=\"");
		builder.append(nano);
		builder.append(time);
		builder.append("\"");
		// opaque
		builder.append(",opaque=\"");
		int size = nano.length() * 2;
		final Random random = new Random();
		while (size-- > 0) {
			builder.append(Character.forDigit(random.nextInt(Character.MAX_RADIX), Character.MAX_RADIX));
		}
		builder.append("\"");
		// algorithm
		if (getAlgorithm() != null) {
			builder.append(",algorithm=");
			builder.append(getAlgorithm());
		}
		// qop
		builder.append(",qop=");
		builder.append(AUTH);
		// stale 不设置，服务端始终设置true
		// charset 不设置，默认UTF-8
		// userhash 不设置，默认 false
		response.addHeader(HTTP.WWW_Authenticate, builder.toString());
		// TEST cs531a5
		response.addHeader(ContentType.NAME, "text/html");
		response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
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
		// # 明文
		// name:password
		// # 密文
		// name:realm:MD5/SHA(name:realm:password)

		USERS.clear();

		int c, c1 = 0, c2 = 0;
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
					}
					if (c1 <= 0) {
						c1 = builder.length();
					} else if (c2 <= 0) {
						c2 = builder.length();
					}
				} else if (c == HTTPCoder.CR || c == HTTPCoder.LF) {
					if (!ignore) {
						if (c1 > 0) {
							if (c2 > 0) {
								USERS.put(builder.substring(0, c1), builder.substring(c2 + 1));
							} else {
								USERS.put(builder.substring(0, c1), builder.substring(c1 + 1));
							}
						}
					}
					builder.setLength(0);
					ignore = false;
					c1 = c2 = 0;
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

	boolean equal(String a, CharSequence b, int start, int done) {
		if (done - start == a.length() && done <= b.length()) {
			for (int index = 0; index < a.length(); index++, start++) {
				if (a.charAt(index) != b.charAt(start)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}