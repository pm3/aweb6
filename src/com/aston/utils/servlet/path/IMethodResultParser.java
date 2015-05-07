package com.aston.utils.servlet.path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IMethodResultParser {

	public void parse(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception;
}
