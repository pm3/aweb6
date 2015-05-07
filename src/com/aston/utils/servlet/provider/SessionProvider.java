package com.aston.utils.servlet.provider;

public class SessionProvider<T> implements Provider<T> {
	private Class<T> type;
	private String name;
	private boolean writable;

	public SessionProvider(Class<T> type) {
		this(type, type.getName(), true);
	}

	public SessionProvider(Class<T> type, String name, boolean writable) {
		this.type = type;
		this.name = name;
		this.writable = writable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		Object o = ProviderFilter.REQUEST.get().getSession().getAttribute(name);
		return o != null && type.isAssignableFrom(o.getClass()) ? (T) o : null;
	}

	@Override
	public void set(T o) {
		if (writable)
			ProviderFilter.REQUEST.get().getSession().setAttribute(name, o);
		else
			throw new UnsupportedOperationException("provider isn't writable");
	}

}
