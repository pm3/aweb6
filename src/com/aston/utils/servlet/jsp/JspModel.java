package com.aston.utils.servlet.jsp;

import java.util.Map;

import com.google.gson.internal.LinkedTreeMap;

public class JspModel {

	String view = null;
	Map<String, Object> data = null;

	public JspModel() {
	}

	public JspModel(String view) {
		this.view = view;
	}

	public JspModel view(String view) {
		this.view = view;
		return this;
	}

	public JspModel put(String name, Object val) {
		if (data == null)
			data = new LinkedTreeMap<String, Object>();
		data.put(name, val);
		return this;
	}

	public JspModel putAll(Map<String, Object> map) {
		if (data == null)
			data = new LinkedTreeMap<String, Object>();
		data.putAll(map);
		return this;
	}

	public static JspModel create() {
		return new JspModel();
	}

	public static JspModel create(String view) {
		return new JspModel(view);
	}
}
