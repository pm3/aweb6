package com.aston.utils.servlet.format;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FormatWeekDay implements IValueFormat {

	private String[] names;

	@Override
	public void init(String style, Locale locale, String conf) {
		names = conf.split(",");
	}

	@Override
	public String format(Object o, Formatter formatter) {
		int day = 0;
		if (o instanceof Number) {
			day = ((Number) o).intValue();
			if (day < 1)
				day = 1;
			if (day > 7)
				day = 7;
		} else if (o instanceof Date) {
			Calendar c = Calendar.getInstance();
			c.setTime((Date) o);
			day = c.get(Calendar.DAY_OF_WEEK);
		} else if (o instanceof Calendar) {
			Calendar c = (Calendar) o;
			day = c.get(Calendar.DAY_OF_WEEK);
		}
		return day >= 1 ? names[day - 1] : null;
	}

}
