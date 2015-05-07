package com.aston.utils.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private PathStore pathStore = null;

	public AServlet() {
	}

	public AServlet(PathStore pathStore) {
		this.pathStore = pathStore;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		if (pathStore == null) {
			this.pathStore = (PathStore) config.getServletContext().getAttribute(PathStore.class.getName());
		}
		if (pathStore == null)
			throw new ServletException("require initialized PathStore");
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			String path = request.getRequestURI();
			if (path == null)
				path = "/";
			String context = request.getContextPath();
			if (context != null && context.length() > 1)
				path = path.substring(context.length());

			service2(request, response, path);

		} catch (HttpStateException e) {
			e.defineStatus(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			response.getWriter().print(e.getMessage());
		}
	}

	protected void service2(HttpServletRequest request, HttpServletResponse response, String path) throws Exception, IOException, ServletException {

		IWebMethodExec e = pathStore.search(request, path);
		if (e != null) {
			WebMethodInvoker invoker = new WebMethodInvoker(request, response, e);
			invoker.invoke();
		} else {
			throw new HttpStateException(404, path);
		}
	}

	@Override
	public void destroy() {
	}
}
