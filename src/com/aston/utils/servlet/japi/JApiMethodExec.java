package com.aston.utils.servlet.japi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.jws.WebMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.IWebMethodExec;
import com.aston.utils.servlet.IWebMethodInterceptor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JApiMethodExec implements IWebMethodExec {

	private Object parent;
	private Method method;
	private String name;
	private String description;
	private Type paramType;
	private Gson gson;
	private IWebMethodInterceptor[] interceptors;

	public JApiMethodExec(Object parent, Method method, WebMethod wm, Gson gson, IWebMethodInterceptor[] interceptors) {
		this.parent = parent;
		this.method = method;
		this.name = wm.operationName() != null && wm.operationName().trim().length() > 0 ? wm.operationName() : method.getName();
		ApiDescription ad = method.getAnnotation(ApiDescription.class);
		this.description = ad != null && ad.value().trim().length() > 0 ? ad.value() : null;

		Type[] paramTypes = method.getGenericParameterTypes();
		this.paramType = paramTypes.length > 0 ? paramTypes[0] : null;
		this.gson = gson;
		this.interceptors = interceptors;
	}

	@Override
	public String getPath() {
		return "/japi/" + name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public IWebMethodInterceptor[] getInterceptors() {
		return interceptors;
	}

	@Override
	public Object[] createArgs(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Object[] args = null;
		if (paramType != null) {
			JsonObject paramJson = requestData(request);
			Object paramObj = gson.fromJson(paramJson, paramType);
			args = new Object[] { paramObj };
		} else {
			args = new Object[] {};
		}
		return args;
	}

	@Override
	public void exec(HttpServletRequest request, HttpServletResponse response, Object[] args) throws Exception {

		try {
			Object ret = method.invoke(parent, args);
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			if (ret != null) {
				gson.toJson(ret, response.getWriter());
			}

		} catch (InvocationTargetException ee) {
			if (ee.getTargetException() instanceof Exception)
				throw new Exception("callApi " + name, (Exception) ee.getTargetException());
			throw new Exception("callApi " + name, ee);
		}
	}

	protected JsonObject requestData(HttpServletRequest request) throws UnsupportedEncodingException, IOException {
		JsonObject data = null;
		if (request.getMethod().equalsIgnoreCase("post") && request.getContentType() != null
				&& !request.getContentType().toLowerCase().startsWith("application/x-www-form-urlencoded")) {
			data = (new JsonParser()).parse(request.getReader()).getAsJsonObject();
		} else if (request.getParameter("$json") != null) {
			data = (new JsonParser()).parse(request.getParameter("$json")).getAsJsonObject();
		} else {
			data = new JsonObject();
			for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
				String n = e.nextElement();
				String[] vals = request.getParameterValues(n);
				if (vals.length == 1) {
					data.addProperty(n, vals[0]);
				} else {
					JsonArray a = new JsonArray();
					for (String v : vals)
						a.add(new JsonPrimitive(v));
					data.add(n, a);
				}
			}
		}
		return data;
	}

}
