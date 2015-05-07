package com.aston.utils.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IWebMethodExec {

	public String getPath();

	public IWebMethodInterceptor[] getInterceptors();

	public Object[] createArgs(HttpServletRequest request, HttpServletResponse response) throws Exception;

	public void exec(HttpServletRequest request, HttpServletResponse response, Object[] args) throws Exception;
}
