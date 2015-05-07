package com.aston.utils.servlet.path.base;

import java.lang.reflect.Array;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.IWebParser;
import com.aston.utils.servlet.path.IMethodParamParser;

public class ParamMP implements IMethodParamParser {

	String name;
	Class<?> type;
	IWebParser parser;

	public ParamMP(String name, Class<?> type, IWebParser parser) {
		this.name = name;
		this.type = type;
		this.parser = parser;
	}

	@Override
	public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Object oval = null;

		if (type.isArray()) {
			String[] svals = request.getParameterValues(name);
			if (svals == null)
				svals = request.getParameterValues(name + "[]");
			oval = parseArray(svals, type);
		} else {
			String sval = request.getParameter(name);
			if (type.isPrimitive() && sval == null)
				throw new Exception("primitive type is null " + name);
			oval = sval != null ? parser.parse(sval) : null;
		}

		return oval;
	}

	protected Object parseArray(String[] svals, Class<?> arrType) {
		Object oval;
		Class<?> outputType = arrType.getComponentType();
		if (svals == null)
			return Array.newInstance(outputType, 0);
		oval = Array.newInstance(outputType, svals.length);
		for (int i = 0; i < svals.length; i++) {
			Object ival = parser.parse(svals[i]);
			Array.set(oval, i, ival);
		}
		return oval;
	}
}
