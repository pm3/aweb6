package com.aston.utils.servlet.path;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.IWebMethodExec;
import com.aston.utils.servlet.IWebMethodInterceptor;

public class PathMethodExec implements IWebMethodExec {

	Path http;
	Object parent;
	Method method;
	IMethodParamParser[] paramParsers;
	IMethodResultParser resultParser;
	IWebMethodInterceptor[] interceptors;
	String[] httpMethods;

	public PathMethodExec(Path http, Object parent, Method method, IMethodParamParser[] paramParsers, IMethodResultParser resultParser, IWebMethodInterceptor[] interceptors) {
		this.http = http;
		this.parent = parent;
		this.method = method;
		this.resultParser = resultParser;
		this.paramParsers = paramParsers;
		this.interceptors = interceptors;
	}

	@Override
	public String getPath() {
		return http.name();
	}

	@Override
	public IWebMethodInterceptor[] getInterceptors() {
		return interceptors;
	}

	@Override
	public Object[] createArgs(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Object[] args = new Object[paramParsers.length];
		for (int i = 0; i < args.length; i++)
			args[i] = paramParsers[i].parse(request, response);
		return args;
	}

	@Override
	public void exec(HttpServletRequest request, HttpServletResponse response, Object[] args) throws Exception {

		if (http.contentType() != null) {
			String ct = http.contentType();
			response.setContentType(ct);
			response.setCharacterEncoding("utf-8");
		}

		try {
			Object result = method.invoke(parent, args);
			if (resultParser != null)
				resultParser.parse(request, response, result);

		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof Exception)
				throw (Exception) e.getTargetException();
			throw new IllegalStateException("invoke method exception " + method.getName() + " " + e.getMessage(), e);
		} catch (Exception e) {
			throw new IllegalStateException("invoke method exception " + method.getName() + " " + e.getMessage(), e);
		}

	}

	@Override
	public String toString() {
		return "AMethodInfo [http=" + http + ", parent=" + parent + ", method=" + method + ", paramParsers=" + Arrays.toString(paramParsers) + ", resultParser=" + resultParser
				+ ", interceptors=" + Arrays.toString(interceptors) + ", httpMethods=" + Arrays.toString(httpMethods) + "]";
	}

}
