package com.aston.utils.servlet.path;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import com.aston.utils.MethodParamNameParser;
import com.aston.utils.servlet.AConfig;
import com.aston.utils.servlet.IMethodAspectsFactory;
import com.aston.utils.servlet.IWebMethodExec;
import com.aston.utils.servlet.IWebMethodExecFactory;
import com.aston.utils.servlet.IWebMethodInterceptor;
import com.aston.utils.servlet.IWebParser;
import com.aston.utils.servlet.path.base.BaseFactory;
import com.google.gson.internal.LazilyParsedNumber;

public class PathMethodExecFactory implements IWebMethodExecFactory {

	private AConfig aconfig;

	@Override
	public void setAConfig(AConfig aconfig) {
		this.aconfig = aconfig;
		aconfig.addWebParser(String.class, new StringWP());
		aconfig.addWebParser(boolean.class, Boolean.class, new BooleanWP());
		aconfig.addWebParser(int.class, Integer.class, new IntWP());
		aconfig.addWebParser(long.class, Long.class, new LongWP());
		aconfig.addWebParser(Number.class, new NumberWP());
		aconfig.addWebParser(BigDecimal.class, new BigDecimalWP());
		aconfig.addPathFactory(new BaseFactory());
	}

	@Override
	public IWebMethodExec createMethodExec(Object parent, Method m) {
		Path http = m.getAnnotation(Path.class);
		if (http != null) {
			return createMethodExec(parent, m, http);
		}
		return null;
	}

	protected IWebMethodExec createMethodExec(Object parent, Method m, Path http) {

		// param parsers
		IMethodParamParser[] paramParsers = new IMethodParamParser[m.getParameterTypes().length];
		List<IMethodAspectsFactory> factories = aconfig.getPathFactories();
		String[] paramNames = MethodParamNameParser.params(m);
		for (int pos = 0; pos < paramParsers.length; pos++) {
			String pn = paramNames != null ? paramNames[pos] : null;
			for (int i = factories.size() - 1; i >= 0; i--) {
				paramParsers[pos] = factories.get(i).createParamParser(this, m, pos, pn);
				if (paramParsers[pos] != null)
					break; // break factories

			}
		}

		// result parser
		IMethodResultParser resultParser = null;
		for (int i = factories.size() - 1; i >= 0; i--) {
			resultParser = factories.get(i).createResultParser(this, m);
			if (resultParser != null)
				break; // break factories
		}

		// interceptors
		IWebMethodInterceptor[] interceptors = aconfig.createInterceptors(m);
		return new PathMethodExec(http, parent, m, paramParsers, resultParser, interceptors);
	}

	public static class StringWP implements IWebParser {
		@Override
		public Object parse(String sval) {
			return sval;
		}
	}

	public static class BooleanWP implements IWebParser {
		@Override
		public Object parse(String sval) {
			return Boolean.parseBoolean(sval);
		}
	}

	public static class IntWP implements IWebParser {
		@Override
		public Object parse(String sval) {
			return Integer.parseInt(sval);
		}
	}

	public static class LongWP implements IWebParser {
		@Override
		public Object parse(String sval) {
			return Long.parseLong(sval);
		}
	}

	public static class NumberWP implements IWebParser {
		@Override
		public Object parse(String sval) {
			return new LazilyParsedNumber(sval);
		}
	}

	public static class BigDecimalWP implements IWebParser {
		@Override
		public Object parse(String sval) {
			return new BigDecimal(sval);
		}
	}

}
