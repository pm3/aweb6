package com.aston.utils.servlet.format;

import java.text.DecimalFormat;
import java.util.Locale;

public class FormatNumber implements IValueFormat {

	private DecimalFormat[] formats;
	private int[] locks;

	@Override
	public void init(String style, Locale locale, String conf) {
		int max = 3;
		formats = new DecimalFormat[max];
		locks = new int[max];
		for (int i = 0; i < max; i++) {
			formats[i] = new DecimalFormat(conf);
			locks[i] = 0;
		}
	}

	@Override
	public String format(Object o, Formatter formatter) {
		String s = null;
		if (o instanceof Number) {
			for (int i = 1; i < formats.length; i++) {
				if (++locks[i] == 1) {
					s = formats[i].format(o);
					locks[i] = 0;
					return s;
				}
			}
			synchronized (this) {
				s = formats[0].format(o);
			}
		}
		return s;
	}
}
