package com.aston.utils.servlet.jsp;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.AConfig;
import com.aston.utils.servlet.IMethodAspectsFactory;
import com.aston.utils.servlet.IWebMethodInterceptor;
import com.aston.utils.servlet.IWebParser;
import com.aston.utils.servlet.path.IMethodParamParser;
import com.aston.utils.servlet.path.IMethodResultParser;

public class JspFactory implements IMethodAspectsFactory {

	private String jspPrefix;
	private String jspSufix;
	private AConfig aconfig = null;

	public JspFactory(String jspPrefix, String jspSufix) {
		this.jspPrefix = jspPrefix;
		this.jspSufix = jspSufix;
	}

	@Override
	public void setAConfig(AConfig aconfig) {
		this.aconfig = aconfig;
	}

	@Override
	public IWebMethodInterceptor createInterceptor(Object parent, Method method) {
		return null;
	}

	@Override
	public IMethodParamParser createParamParser(Object parent, Method method, int pos, String pname) {
		return null;
	}

	@Override
	public IMethodResultParser createResultParser(Object parent, Method method) {
		if (method.getReturnType().equals(JspModel.class))
			return new JspModelRP(aconfig.getServletContext(), jspPrefix, jspSufix);
		return null;
	}

	@Override
	public IWebParser createWebParser(Class<?> type) {
		return null;
	}

	public static class JspModelRP implements IMethodResultParser {

		private ServletContext servletContext;
		private String jspPrefix;
		private String jspSufix;

		public JspModelRP(ServletContext servletContext, String jspPrefix, String jspSufix) {
			this.servletContext = servletContext;
			this.jspPrefix = jspPrefix;
			this.jspSufix = jspSufix;
		}

		@Override
		public void parse(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
			JspModel model = (JspModel) result;
			if (model == null)
				return;
			if (model.view == null)
				throw new IllegalStateException("JspModel.view is null");
			if (model.data != null) {
				for (Entry<String, Object> e : model.data.entrySet())
					request.setAttribute(e.getKey(), e.getValue());
			}
			String path = null;
			if (jspPrefix != null && jspSufix != null)
				path = jspPrefix + model.view + jspSufix;
			else if (jspPrefix != null)
				path = jspPrefix + model.view;
			else if (jspSufix != null)
				path = model.view + jspSufix;
			else
				path = model.view;

			servletContext.getRequestDispatcher(path).include(request, response);
		}

	}

}
