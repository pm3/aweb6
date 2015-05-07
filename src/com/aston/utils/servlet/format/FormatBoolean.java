package com.aston.utils.servlet.format;

import java.util.Locale;

public class FormatBoolean implements IValueFormat {

	private String[] names;

	@Override
	public void init(String style, Locale locale, String conf) {
		names = conf.split(",");
	}

	@Override
	public String format(Object o, Formatter formatter) {
		if (o instanceof Boolean) {
			return Boolean.TRUE.equals(o) ? names[0] : names[1];
		}
		return null;
	}

}
