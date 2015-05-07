package com.aston.utils.servlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.aston.utils.MethodParamNameParser;
import com.aston.utils.servlet.path.Path;

public class PathStore {

	private Map<String, IWebMethodExec> infoPaths = new ConcurrentHashMap<String, IWebMethodExec>();
	private List<IWebMethodExec> infoPathExpressions = new ArrayList<IWebMethodExec>();
	private Map<String, String> redirects = new ConcurrentHashMap<>();

	public void addPath(IWebMethodExec exec) {
		if (exec == null)
			return;
		String p = exec.getPath();
		if (p.startsWith("*") || p.endsWith("*"))
			infoPathExpressions.add(exec);
		else
			infoPaths.put(p, exec);
	}

	public void addPathsFromObj(AConfig aconfig, Object obj) {
		addPathsFromObj(aconfig, obj, null);
	}

	public void addRedirect(String oldUrl, String newUrl) {
		redirects.put(oldUrl, newUrl);
	}

	public void addPathsFromObj(AConfig aconfig, Object obj, Class<?> objType) {
		if (objType == null)
			objType = obj.getClass();
		MethodParamNameParser.prepareClass(objType, Path.class);
		List<IWebMethodExecFactory> execFactories = aconfig.getExecFactories();
		for (Method m : objType.getDeclaredMethods()) {
			for (IWebMethodExecFactory f : execFactories) {
				IWebMethodExec e = f.createMethodExec(obj, m);
				if (e != null) {
					addPath(e);
					break;
				}
			}
		}

	}

	public IWebMethodExec search(HttpServletRequest request, String path) throws HttpStateException {

		String r = redirects.get(path);
		if (r != null) {
			String cp = request.getContextPath();
			if (cp == null)
				cp = "";
			throw new HttpStateException(302, cp + r);
		}

		IWebMethodExec exec = infoPaths.get(path);
		if (exec != null)
			return exec;

		for (IWebMethodExec exec2 : infoPathExpressions) {
			String p2 = exec2.getPath();
			if (p2.endsWith("*") && path.startsWith(p2.substring(p2.length() - 1)))
				return exec2;
			if (p2.startsWith("*") && path.endsWith(p2.substring(1)))
				return exec2;
		}
		return null;
	}
}
