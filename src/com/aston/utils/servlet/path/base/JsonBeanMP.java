package com.aston.utils.servlet.path.base;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.servlet.path.IMethodParamParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonBeanMP implements IMethodParamParser {

	private static final String JSON_ATT = "request.json";
	String name;
	Type type;
	Gson gson;

	public JsonBeanMP(String name, Type type, Gson gson) {
		this.name = name;
		this.type = type;
		this.gson = gson;
	}

	@Override
	public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Object oval = null;

		JsonObject jobj = (JsonObject) request.getAttribute(JSON_ATT);
		if (jobj == null) {
			JsonObject o = new JsonObject();
			for (Entry<String, String[]> e : request.getParameterMap().entrySet()) {
				String[] v = e.getValue();
				if (v.length == 1) {
					o.addProperty(e.getKey(), v[0]);
				} else {
					JsonArray a = new JsonArray();
					for (String s : v)
						a.add(new JsonPrimitive(s));
					o.add(e.getKey(), a);
				}
			}
			jobj = o;
			request.setAttribute(JSON_ATT, jobj);
		}
		oval = gson.fromJson(jobj, type);
		return oval;
	}
}
