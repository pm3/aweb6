package com.aston.utils.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebMethodInvoker {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private int level;
	private IWebMethodExec exec;
	private Object[] args = null;
	private IWebMethodInterceptor[] interceptors;

	public WebMethodInvoker(HttpServletRequest request, HttpServletResponse response, IWebMethodExec exec) {
		this.request = request;
		this.response = response;
		this.level = 0;
		this.exec = exec;
		this.interceptors = exec.getInterceptors();
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public Object[] createArgs() throws Exception {
		if (args == null) {
			args = exec.createArgs(request, response);
		}
		return args;
	}

	public void invoke() throws Exception {
		if (interceptors != null && level < interceptors.length) {
			interceptors[level++].process(this);
		} else {
			exec.exec(request, response, createArgs());
		}
	}
}
