package com.aston.utils.servlet.provider;

public class RequestProvider<T> implements Provider<T> {

	private Class<T> type;
	private String name;
	private boolean writable;

	public RequestProvider(Class<T> type) {
		this(type, type.getName(), false);
	}

	public RequestProvider(Class<T> type, String name, boolean writable) {
		this.type = type;
		this.name = name;
		this.writable = writable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		Object o = ProviderFilter.REQUEST.get().getAttribute(name);
		return o != null && type.isAssignableFrom(o.getClass()) ? (T) o : null;
	}

	@Override
	public void set(T o) {
		if (writable)
			ProviderFilter.REQUEST.get().setAttribute(name, o);
		else
			throw new UnsupportedOperationException("provider isn't writable");
	}
}
