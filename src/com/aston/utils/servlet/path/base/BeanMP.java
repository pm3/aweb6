package com.aston.utils.servlet.path.base;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.aston.utils.servlet.AConfig;
import com.aston.utils.servlet.IWebParser;
import com.aston.utils.servlet.path.IMethodParamParser;
import com.aston.utils.servlet.path.base.BaseFactory.PartMP;

public class BeanMP implements IMethodParamParser {

	String name;
	Class<?> type;
	List<Object[]> props = new ArrayList<>();

	public BeanMP(String name, Class<?> type, AConfig aconfig) throws Exception {
		this.name = name;
		this.type = type;

		type.getConstructor();

		for (Method m : type.getDeclaredMethods()) {
			String n = m.getName();
			if (n.startsWith("set") && n.length() > 3 && m.getParameterTypes().length == 1) {
				try {
					String pn = n.substring(3, 4).toLowerCase() + n.substring(4);
					Class<?> pt = m.getParameterTypes()[0];
					IWebParser p = aconfig.getWebParser(pt.isArray() ? pt.getComponentType() : pt);
					if (p != null) {
						props.add(new Object[] { m, new ParamMP(pn, pt, p) });
					} else if (pt.equals(Part.class)) {
						props.add(new Object[] { m, new PartMP(pn) });
					} else if (pt.equals(List.class)) {
						Class<?> generic = (Class<?>) ((ParameterizedType) m.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
						IWebParser p2 = aconfig.getWebParser(generic);
						if (p2 != null)
							props.add(new Object[] { m, new CollectionParamMP(pn, generic, p2, ArrayList.class) });
					}
					if (pt.equals(Set.class)) {
						Class<?> generic = (Class<?>) ((ParameterizedType) m.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
						IWebParser p2 = aconfig.getWebParser(generic);
						if (p2 != null)
							props.add(new Object[] { m, new CollectionParamMP(pn, generic, p2, TreeSet.class) });
					}
				} catch (Exception ee) {
				}
			}
		}
		if (props.size() == 0)
			throw new Exception("bean without setters");
	}

	@Override
	public Object parse(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Object o = type.newInstance();
		for (Object[] row : props) {
			try {
				Object pv = ((IMethodParamParser) row[1]).parse(request, response);
				if (pv != null)
					((Method) row[0]).invoke(o, pv);
			} catch (Exception e) {
			}
		}
		return o;
	}
}
