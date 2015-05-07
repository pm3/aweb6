package com.aston.utils.servlet.format;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Formatter {

	private Locale locale;
	private Map<String, IValueFormat> formatters = new HashMap<String, IValueFormat>();
	private List<ResourceBundle> bundles;
	private FormatterFactory factory;

	public Formatter(Locale locale, Map<String, IValueFormat> formatters, List<ResourceBundle> bundles, FormatterFactory factory) {
		this.locale = locale;
		this.formatters = formatters;
		this.bundles = bundles;
		this.factory = factory;
	}

	public Formatter clone() {
		List<ResourceBundle> base = new ArrayList<>(bundles.size() + 4);
		base.addAll(bundles);
		return new Formatter(locale, formatters, base, factory);
	}

	public void addBundle(ResourceBundle bundle) {
		bundles.add(bundle);
	}

	public void addBundle(String sbundle) {
		bundles.add(factory.getBundle(sbundle, locale));
	}

	public Locale getLocale() {
		return locale;
	}

	public String format(Object o) {
		return format(o, null);
	}

	public String format(Object o, String style) {
		if (o == null)
			return null;
		if (style == null)
			style = o.getClass().getName();
		IValueFormat f = formatters.get(style);
		if (f != null)
			return f.format(o, this);
		if (o instanceof Enum)
			return mb(o.getClass().getName() + "." + ((Enum<?>) o).name());
		return o != null ? o.toString() : null;
	}

	public String mb(String key) {
		for (int i = bundles.size() - 1; i >= 0; i--) {
			ResourceBundle rb = bundles.get(i);
			try {
				return rb.getString(key);
			} catch (Exception e) {
			}
		}
		return key;
	}

	public String mb2(String key, Object... args) {
		String v = mb(key);
		return MessageFormat.format(v, args);
	}
}
