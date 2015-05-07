package com.aston.utils.servlet;


public interface IWebMethodInterceptor {

	public int order();

	public void process(WebMethodInvoker invoker) throws Exception;
}
