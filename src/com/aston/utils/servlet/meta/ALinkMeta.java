package com.aston.utils.servlet.meta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.aston.utils.MethodParamNameParser;
import com.aston.utils.servlet.AServlet;
import com.aston.utils.servlet.path.Path;

public class ALinkMeta implements Comparable<ALinkMeta> {

	private String path;
	private List<ALinkParamMeta> params = new ArrayList<ALinkMeta.ALinkParamMeta>();

	public ALinkMeta(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getId() {
		return path.substring(1).replace('/', '-');
	}

	public List<ALinkParamMeta> getParams() {
		return params;
	}

	@Override
	public int compareTo(ALinkMeta o) {
		return path != null && o != null && o.path != null ? path.compareTo(o.path) : 1;
	}

	public static class ALinkParamMeta {

		private String name;
		private String type;

		public ALinkParamMeta(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

	}

	public static List<ALinkMeta> metaInfo(Class<? extends AServlet> cl) {

		List<ALinkMeta> l = new ArrayList<ALinkMeta>();
		MethodParamNameParser.prepareClass(cl, Path.class);
		for (Method m : cl.getMethods()) {
			Path p = m.getAnnotation(Path.class);
			if (p == null)
				continue;
			ALinkMeta lm = new ALinkMeta(p.name());
			String names[] = MethodParamNameParser.params(m);
			if (names != null) {
				for (int i = 0; i < names.length; i++) {
					lm.params.add(new ALinkParamMeta(names[i], m.getParameterTypes()[i].getSimpleName()));
				}
			}
			l.add(lm);
		}
		return l;
	}
}
