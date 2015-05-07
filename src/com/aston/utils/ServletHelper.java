package com.aston.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletHelper {

	public static String pathName(String path) {
		if (path == null)
			return null;
		if (path.endsWith(delim))
			path = path.substring(0, path.length() - 1);
		return path.substring(path.lastIndexOf('/') + 1);
	}

	public static String pathParent(String path) {
		if (path == null || path.equals(delim))
			return path;
		int pos = path.lastIndexOf('/');
		return pos > 0 ? path.substring(0, pos) : delim;
	}

	// sorted, if change chars, use array.sort();
	private static final char[] specUriChars = "!$&'()+,-.;=@_~".toCharArray();
	private static final char[] dir0 = "/./".toCharArray();
	private static final char[] dir1 = "/../".toCharArray();
	public static final String delim = "/";
	public static final String UTF8 = "utf-8";

	public static String normalizeUri(String uri) throws MalformedURLException {
		if (uri == null || uri.equals(delim))
			return uri;
		if (uri.isEmpty())
			return delim;
		StringBuilder sb0 = new StringBuilder(uri.length() + 2);
		if (uri.charAt(0) != '/')
			sb0.append('/');
		sb0.append(uri);
		if (uri.endsWith("/.") || uri.endsWith("/.."))
			sb0.append('/');

		char[] urichs = new char[sb0.length()];
		sb0.getChars(0, sb0.length(), urichs, 0);
		StringBuilder sb = new StringBuilder(urichs.length);
		for (int i = 0; i < urichs.length; i++) {
			char ch = urichs[i];
			if (ch == '/') {
				if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '/')
					sb.append(ch);
				if (eqchars(urichs, i, dir0, dir0.length))
					i += 1;
				if (eqchars(urichs, i, dir1, dir1.length)) {
					int last = sb.lastIndexOf(delim, sb.length() - 2);
					if (last < 0)
						throw new MalformedURLException("Invalid relative URL reference " + uri);
					sb.setLength(last + 1);
					i += 2;
				}
				continue;
			} else {
				boolean ok = false;
				// ok = Character.isLetterOrDigit(ch);
				ok = ok | (ch >= 'a' && ch <= 'z');
				ok = ok | (ch >= 'A' && ch <= 'Z');
				ok = ok | (ch >= '0' && ch <= '9');
				ok = ok | Arrays.binarySearch(specUriChars, ch) >= 0;
				if (!ok)
					throw new MalformedURLException("URL Illegal character [" + ch + "] " + uri);
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	private static boolean eqchars(char[] chars, int pos, char[] chars2, int len) {
		for (int i = 0; i < len; i++) {
			if (pos + i >= chars.length)
				return false;
			if (chars[pos + i] != chars2[i])
				return false;
		}
		return true;
	}

	public static String etag(long modified, long csize) {
		return modified + "-" + csize;
	}

	private static final ThreadLocal<SimpleDateFormat> threadLocalDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			return sdf;
		}
	};

	public static String webGMTdate(Date d) {
		if (d == null)
			return null;
		return threadLocalDateFormat.get().format(d);
	}

	public static void sendFile(HttpServletRequest request, HttpServletResponse response, ServletContext context, File f) {

		String mt = context.getMimeType(f.getName().toLowerCase());
		if (mt == null)
			mt = "application/octet-stream";
		response.setContentType(mt);

		long len = f.length();
		String etag = f.lastModified() + "-" + len;
		String h1 = request.getHeader("If-None-Match");
		if (etag.equals(h1)) {
			response.setStatus(304);
			return;
		}

		String webdate = ServletHelper.webGMTdate(new Date(f.lastModified()));
		String h2 = request.getHeader("If-Modified-Since");
		if (webdate.equalsIgnoreCase(h2)) {
			response.setStatus(304);
			return;
		}

		response.setHeader("ETag", etag);
		response.setHeader("Last-Modified", webdate);
		response.setContentLength((int) len);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			copy(fis, response.getOutputStream(), 512);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception ee) {
			}
		}
	}

	public static long copy(InputStream is, OutputStream os, int bufSize) throws IOException {
		long size = 0;
		byte[] buf = new byte[bufSize];
		int len = 0;

		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
			size += len;
		}
		return size;
	}

	public static byte[] file2bytea(File f) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		FileInputStream fis = new FileInputStream(f);
		copy(fis, bos, 4096);
		return bos.toByteArray();
	}
}
