package com.aston.utils.servlet;

import java.lang.reflect.Method;

import com.aston.utils.servlet.path.IMethodParamParser;
import com.aston.utils.servlet.path.IMethodResultParser;

public interface IMethodAspectsFactory {

	void setAConfig(AConfig aconfig);

	IWebParser createWebParser(Class<?> type);

	IWebMethodInterceptor createInterceptor(Object parent, Method method);

	IMethodParamParser createParamParser(Object parent, Method method, int pos, String pname);

	IMethodResultParser createResultParser(Object parent, Method method);
}
