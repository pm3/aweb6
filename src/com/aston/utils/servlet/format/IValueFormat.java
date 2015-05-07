package com.aston.utils.servlet.format;

import java.util.Locale;

public interface IValueFormat {

	public void init(String style, Locale locale, String conf);

	public String format(Object o, Formatter formatter);
}
