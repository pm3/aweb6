package com.aston.utils.servlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AConfig {

	public AConfig(ServletContext servletContext) {
		this.servletContext = servletContext;
		servletContext.setAttribute(AConfig.class.getName(), this);
	}

	private ServletContext servletContext;
	private Gson gson = new GsonBuilder().create();
	private List<IWebMethodExecFactory> execFactories = new ArrayList<IWebMethodExecFactory>();
	private Map<Class<?>, IWebParser> webParsers = new HashMap<Class<?>, IWebParser>();
	private List<IMethodAspectsFactory> pathFctories = new ArrayList<IMethodAspectsFactory>();

	public ServletContext getServletContext() {
		return servletContext;
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public List<IWebMethodExecFactory> getExecFactories() {
		return this.execFactories;
	}

	public void addExecFactory(IWebMethodExecFactory factory) {
		if (factory != null) {
			factory.setAConfig(this);
			execFactories.add(factory);
		}
	}

	public List<IMethodAspectsFactory> getPathFactories() {
		return pathFctories;
	}

	public void addPathFactory(IMethodAspectsFactory factory) {
		if (factory != null) {
			factory.setAConfig(this);
			this.pathFctories.add(factory);
		}
	}

	public IWebMethodInterceptor[] createInterceptors(Method m) {

		List<IWebMethodInterceptor> interceptors = null;
		for (int i = pathFctories.size() - 1; i >= 0; i--) {
			IWebMethodInterceptor h = pathFctories.get(i).createInterceptor(this, m);
			if (h != null) {
				if (interceptors == null)
					interceptors = new ArrayList<IWebMethodInterceptor>();
				interceptors.add(h);
			}
		}
		if (interceptors == null)
			return null;

		Collections.sort(interceptors, new Comparator<IWebMethodInterceptor>() {
			@Override
			public int compare(IWebMethodInterceptor o1, IWebMethodInterceptor o2) {
				return o1.order() - o2.order();
			}
		});
		return interceptors.toArray(new IWebMethodInterceptor[interceptors.size()]);
	}

	public void addWebParser(Class<?> type, IWebParser parser) {
		if (type != null && parser != null)
			webParsers.put(type, parser);
	}

	public void addWebParser(Class<?> type, Class<?> type2, IWebParser parser) {
		if (type != null && parser != null)
			webParsers.put(type, parser);
		if (type2 != null && parser != null)
			webParsers.put(type2, parser);
	}

	public IWebParser getWebParser(Class<?> type) {
		if (type == null)
			return null;
		IWebParser p = webParsers.get(type);
		if (p == null) {
			for (int i = pathFctories.size() - 1; i >= 0; i--) {
				p = pathFctories.get(i).createWebParser(type);
				if (p != null)
					break;
			}
			if (p == null)
				p = empty;
			webParsers.put(type, p);
		}
		return !empty.equals(p) ? p : null;
	}

	private static final IWebParser empty = new IWebParser() {
		@Override
		public Object parse(String sval) {
			return null;
		}
	};

}
