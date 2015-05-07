package com.aston.utils.servlet.provider;

import com.aston.utils.servlet.format.Formatter;

public class FormatterProvider extends RequestProvider<Formatter> {

	public FormatterProvider() {
		super(Formatter.class, ProviderFilter.FKEY, false);
	}
}
