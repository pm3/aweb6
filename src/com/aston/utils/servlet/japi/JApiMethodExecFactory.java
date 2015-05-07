package com.aston.utils.servlet.japi;

import java.lang.reflect.Method;

import javax.jws.WebMethod;

import com.aston.utils.servlet.AConfig;
import com.aston.utils.servlet.IWebMethodExec;
import com.aston.utils.servlet.IWebMethodExecFactory;
import com.aston.utils.servlet.IWebMethodInterceptor;

public class JApiMethodExecFactory implements IWebMethodExecFactory {

	private AConfig aconfig = null;

	@Override
	public void setAConfig(AConfig aconfig) {
		this.aconfig = aconfig;
	}

	@Override
	public IWebMethodExec createMethodExec(Object parent, Method m) {

		if (m.getParameterTypes().length > 1)
			return null;
		WebMethod wm = m.getAnnotation(WebMethod.class);
		if (wm == null || wm.exclude() == false)
			return null;

		IWebMethodInterceptor[] interceptors = aconfig.createInterceptors(m);
		return new JApiMethodExec(parent, m, wm, aconfig.getGson(), interceptors);
	}

}
