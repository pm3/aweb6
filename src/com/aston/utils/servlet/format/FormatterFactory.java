package com.aston.utils.servlet.format;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.aston.utils.StringHelper;

public class FormatterFactory {

	private String sconfigBundle = "com.aston.utils.servlet.format.config";
	private List<String> slanguages = new ArrayList<>();
	private List<String> sbaseBundles = new ArrayList<>();
	private List<Formatter> baseFormatters = null;
	private IResorceBundleFactory bundleFactory;

	public FormatterFactory() {
		this.bundleFactory = new IResorceBundleFactory() {

			@Override
			public ResourceBundle getBundle(String baseName, Locale locale) {
				return ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
			}
		};
	}

	public void setBundleFactory(IResorceBundleFactory bundleFactory) {
		this.bundleFactory = bundleFactory;
	}

	public ResourceBundle getBundle(String baseName, Locale locale) {
		return bundleFactory.getBundle(baseName, locale);
	}

	public void setConfigBundle(String sconfigBundle) {
		this.sconfigBundle = sconfigBundle;
	}

	public void setLanguages(String slanguages) {
		this.slanguages = StringHelper.tokenizeTrimString(slanguages, ",");
	}

	public void addLanguage(String lang) {
		this.slanguages.add(lang);
	}

	public void setBaseBundles(String sbaseBundles) {
		this.sbaseBundles.addAll(StringHelper.tokenizeTrimString(sbaseBundles, ","));
	}

	public void addBaseBundle(String baseBundle) {
		this.sbaseBundles.add(baseBundle);
	}

	public Formatter getFormatter(Locale locale) {
		if (baseFormatters == null) {
			synchronized (this) {
				if (baseFormatters == null) {
					init();
				}
			}
		}
		for (Formatter f : baseFormatters)
			if (locale.equals(f.getLocale()))
				return f.clone();
		Formatter f = baseFormatters.get(0);
		return f.clone();
	}

	private static final String formatPref = "format.";

	protected void init() {
		baseFormatters = new ArrayList<Formatter>();
		for (String lang : slanguages) {
			// locale
			Locale locale = new Locale(lang);
			// formatters
			Map<String, IValueFormat> formatters = new HashMap<String, IValueFormat>();
			ResourceBundle rbconf = getBundle(sconfigBundle, locale);
			for (Enumeration<String> e = rbconf.getKeys(); e.hasMoreElements();) {
				String n = e.nextElement();
				if (n.startsWith(formatPref)) {
					String v = rbconf.getString(n);
					n = n.substring(formatPref.length());
					initValueFormatter(formatters, locale, n, v);
				}
			}
			// base bundles
			List<ResourceBundle> bundles = new ArrayList<>(sbaseBundles.size());
			for (String bn : sbaseBundles) {
				bundles.add(getBundle(bn, locale));
			}
			baseFormatters.add(new Formatter(locale, formatters, bundles, this));
		}
	}

	protected void initValueFormatter(Map<String, IValueFormat> formatters, Locale locale, String n, String v) {
		String cl = v;
		String conf = null;
		int pos = v.indexOf('|');
		if (pos > 0) {
			cl = v.substring(0, pos);
			conf = v.substring(pos + 1);
		}
		if (!cl.contains("."))
			cl = this.getClass().getPackage().getName() + "." + cl;
		try {
			IValueFormat f = (IValueFormat) Thread.currentThread().getContextClassLoader().loadClass(cl).newInstance();
			f.init(n, locale, conf);
			formatters.put(n, f);
		} catch (Exception e) {
			System.err.println("init ValueFormatter [" + v + "]: " + e);
		}
	}

	public static void main(String[] args) {
		try {
			FormatterFactory ff = new FormatterFactory();
			ff.setLanguages("sk,en");

			Formatter fsk = ff.getFormatter(new Locale("sk"));
			System.out.println(fsk.format(123, "num3"));
			System.out.println(fsk.format(new Date(), "date12"));
			System.out.println(fsk.format(new Date(), "weekDay"));

			Formatter fen = ff.getFormatter(new Locale("en"));
			System.out.println(fen.format(123, "num3"));
			System.out.println(fen.format(new Date(), "date14"));
			System.out.println(fen.format(7, "weekDay"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
