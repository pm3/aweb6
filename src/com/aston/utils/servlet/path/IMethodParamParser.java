package com.aston.utils.servlet.path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IMethodParamParser {

	public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
