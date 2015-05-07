package com.aston.utils.servlet.path.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.aston.utils.servlet.AConfig;
import com.aston.utils.servlet.IMethodAspectsFactory;
import com.aston.utils.servlet.IWebMethodInterceptor;
import com.aston.utils.servlet.IWebParser;
import com.aston.utils.servlet.path.IMethodParamParser;
import com.aston.utils.servlet.path.IMethodResultParser;
import com.aston.utils.servlet.path.Path;
import com.google.gson.Gson;

public class BaseFactory implements IMethodAspectsFactory {

	AConfig aconfig = null;

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

		Class<?> type = method.getParameterTypes()[pos];
		if (HttpServletRequest.class.equals(type))
			return new HttpRequestMP();
		if (HttpServletResponse.class.equals(type))
			return new HttpResponseMP();
		if (InputStream.class.equals(type) || ServletInputStream.class.equals(type))
			return new InputStreamMP();
		if (Reader.class.equals(type))
			return new ReaderMP();
		if (OutputStream.class.equals(type) || ServletOutputStream.class.equals(type))
			return new OutputStreamMP();
		if (PrintWriter.class.equals(type))
			return new PrintWriterMP();
		if (Part.class.equals(type))
			return new PartMP(pname);

		Annotation[] as = method.getParameterAnnotations()[pos];
		for (Annotation a : as) {
			if (a.annotationType().equals(Param.class)) {
				String an = ((Param) a).value().trim();
				if (an.length() > 0)
					pname = an;
			}
			if (a.annotationType().equals(Header.class)) {
				IWebParser parser = aconfig.getWebParser(type);
				if (parser == null)
					throw new IllegalStateException("undefined header type " + type.getName());
				return new HeaderMP((Header) a, type, parser);
			}
		}
		if (pname != null) {
			IWebParser parser = aconfig.getWebParser(type.isArray() ? type.getComponentType() : type);
			if (parser != null)
				return new ParamMP(pname, type, parser);
			if (type.equals(List.class)) {
				try {
					Class<?> generic = (Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[pos]).getActualTypeArguments()[0];
					IWebParser p2 = aconfig.getWebParser(generic);
					if (p2 != null)
						return new CollectionParamMP(pname, generic, p2, ArrayList.class);
				} catch (Exception ee) {
				}
			}
			if (type.equals(Set.class)) {
				try {
					Class<?> generic = (Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[pos]).getActualTypeArguments()[0];
					IWebParser p2 = aconfig.getWebParser(generic);
					if (p2 != null)
						return new CollectionParamMP(pname, generic, p2, TreeSet.class);
				} catch (Exception ee) {
				}
			}
		}

		try {
			if (!type.isArray() && !Collection.class.isAssignableFrom(type)) {
				return new BeanMP(pname, type, aconfig);
			}
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public IMethodResultParser createResultParser(Object parent, Method method) {

		Class<?> type = method.getReturnType();
		if (type.equals(Void.TYPE))
			return new VoidRP();

		if (String.class.equals(type))
			return new BasicRP();

		if (byte[].class.equals(type))
			return new ByteaRP();

		Path http = method.getAnnotation(Path.class);
		if (http.contentType() != null && http.contentType().endsWith("/json"))
			return new JsonRP(aconfig.getGson());

		return new BasicRP();
	}

	@Override
	public IWebParser createWebParser(Class<?> type) {
		if (type.isEnum())
			return new EnumWP(type);
		return null;
	}

	public static class EnumWP implements IWebParser {

		private Class<?> type;

		public EnumWP(Class<?> type) {
			this.type = type;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object parse(String sval) {
			return Enum.valueOf((Class<? extends Enum>) type, sval);
		}

	}

	public static class HttpRequestMP implements IMethodParamParser {

		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) {
			return request;
		}
	}

	public static class HttpResponseMP implements IMethodParamParser {
		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) {
			return response;
		}
	}

	public static class HttpSessionMP implements IMethodParamParser {
		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) {
			return request.getSession();
		}
	}

	public static class InputStreamMP implements IMethodParamParser {
		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
			return request.getInputStream();
		}
	}

	public static class ReaderMP implements IMethodParamParser {
		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
			return request.getReader();
		}
	}

	public static class OutputStreamMP implements IMethodParamParser {
		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
			return response.getOutputStream();
		}
	}

	public static class PrintWriterMP implements IMethodParamParser {
		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
			return response.getWriter();
		}
	}

	public static class PartMP implements IMethodParamParser {

		String name;

		public PartMP(String name) {
			this.name = name;
		}

		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
			Object o = null;
			try {
				o = request.getPart(name);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			return o;
		}
	}

	public static class HeaderMP implements IMethodParamParser {

		Header header;
		Class<?> type;
		IWebParser parser;

		public HeaderMP(Header header, Class<?> type, IWebParser parser) {
			this.header = header;
			this.type = type;
			this.parser = parser;
		}

		@Override
		public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
			String sval = request.getParameter(header.name());
			if (type.isPrimitive() && sval == null)
				throw new Exception("primitive type is null " + header.name());
			return sval != null ? parser.parse(sval) : null;
		}
	}

	public static class VoidRP implements IMethodResultParser {

		@Override
		public void parse(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
		}

	}

	public static class BasicRP implements IMethodResultParser {
		@Override
		public void parse(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
			response.getWriter().print(result);
		}
	}

	public static class ByteaRP implements IMethodResultParser {
		@Override
		public void parse(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
			if (result != null)
				response.getOutputStream().write((byte[]) result);
		}
	}

	public static class JsonRP implements IMethodResultParser {

		Gson gson;

		public JsonRP(Gson gson) {
			this.gson = gson;
		}

		@Override
		public void parse(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
			if (result == null)
				return;
			gson.toJson(result, response.getWriter());
		}
	}
}
