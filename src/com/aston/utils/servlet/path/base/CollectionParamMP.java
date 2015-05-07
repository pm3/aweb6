package com.aston.utils.servlet.path.base;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.IWebParser;
import com.aston.utils.servlet.path.IMethodParamParser;

public class CollectionParamMP implements IMethodParamParser {
	String name;
	Class<?> type;
	IWebParser parser;
	Class<?> collectionType;

	public CollectionParamMP(String name, Class<?> type, IWebParser parser, Class<?> collectionType) {
		this.name = name;
		this.type = type;
		this.parser = parser;
		this.collectionType = collectionType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Collection<Object> l = null;

		String[] svals = request.getParameterValues(name);
		if (svals == null)
			svals = request.getParameterValues(name + "[]");

		if (svals != null) {
			l = (Collection<Object>) collectionType.newInstance();
			for (String s : svals) {
				l.add(parser.parse(s));
			}
		}

		return l;
	}
}
