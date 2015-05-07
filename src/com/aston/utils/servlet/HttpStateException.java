package com.aston.utils.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpStateException extends Exception {
	private static final long serialVersionUID = 1L;

	private final int status;

	public HttpStateException(int status, String message, Throwable throwable) {
		super(message, throwable);
		this.status = status;
	}

	public HttpStateException(int status, String message) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void defineStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (response.isCommitted()) {
			System.err.println("commited before http status exception");
			return;
		}
		response.resetBuffer();

		response.setStatus(status);
		if (status == 301 || status == 302) {
			String absolute = toAbsolute(getMessage(), request);
			response.setHeader("Location", absolute);
		} else if (status == 401) {
			response.addHeader("WWW-Authenticate", "Basic realm=\"" + getMessage() + "\"");
			response.getWriter().write("<html><head><title>401 Authorization Required</title></head><body><h1>Authorization Required</h1></body></html>");
		}

		if (status > 401)
			System.err.println(status + " - " + getMessage());
	}

	private static String toAbsolute(String location, HttpServletRequest request) {
		if (location == null)
			return (location);

		if (location.startsWith("//")) {
			// Scheme relative
			StringBuilder sb = new StringBuilder();
			// Add the scheme
			sb.append(request.getScheme());
			sb.append(':');
			sb.append(location);
			return sb.toString();
		}
		URL url = null;
		try {
			url = new URL(location);
			if (url.getAuthority() == null)
				return location;
		} catch (MalformedURLException e1) {
			String requrl = request.getRequestURL().toString();
			try {
				url = new URL(new URL(requrl), location);
			} catch (MalformedURLException e2) {
				throw new IllegalArgumentException(location);
			}
		}
		return (url.toExternalForm());
	}

}
