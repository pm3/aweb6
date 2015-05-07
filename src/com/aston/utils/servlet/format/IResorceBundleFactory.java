package com.aston.utils.servlet.format;

import java.util.Locale;
import java.util.ResourceBundle;

public interface IResorceBundleFactory {

	public ResourceBundle getBundle(String baseName, Locale locale);
}
