package com.aston.utils.servlet;

import java.lang.reflect.Method;

public interface IWebMethodExecFactory {

	public void setAConfig(AConfig aconfig);

	public IWebMethodExec createMethodExec(Object parent, Method m);
}
