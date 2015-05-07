package com.aston.utils.servlet.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.HttpStateException;
import com.aston.utils.servlet.format.Formatter;
import com.aston.utils.servlet.format.FormatterFactory;

public class ProviderFilter implements Filter {

	private FormatterFactory formatterFactory = null;
	private List<IRequestPrepare> prepares = new ArrayList<>();

	public static ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();
	public static final String FKEY = Formatter.class.getName();

	public void setFormatterFactory(FormatterFactory formatterFactory) {
		this.formatterFactory = formatterFactory;
	}

	public void addRequestPrepare(IRequestPrepare prepare) {
		if (prepare != null)
			prepares.add(prepare);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (formatterFactory == null)
			this.formatterFactory = (FormatterFactory) filterConfig.getServletContext().getAttribute(FormatterFactory.class.getName());

		Object ol = filterConfig.getServletContext().getAttribute(ProviderFilter.class.getName() + ".prepares");
		if (ol instanceof List) {
			for (Object op : (List<Object>) ol) {
				if (op instanceof IRequestPrepare && !prepares.contains(op))
					prepares.add((IRequestPrepare) op);
			}
		}
	}

	@Override
	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) _request;

		// prepare request
		try {
			for (IRequestPrepare prepare : prepares)
				prepare.prepare(request);
		} catch (HttpStateException e) {
			e.defineStatus(request, (HttpServletResponse) _response);
			return;
		}

		// check formatter
		if (formatterFactory != null) {
			Object o = request.getAttribute(FKEY);
			if (o == null) {
				Locale l = request.getLocale();
				o = formatterFactory.getFormatter(l);
				request.setAttribute(FKEY, o);
			}
		}

		try {
			REQUEST.set((HttpServletRequest) request);
			chain.doFilter(_request, _response);
		} finally {
			REQUEST.set(null);
		}
	}

	public void destroy() {
	}
}
