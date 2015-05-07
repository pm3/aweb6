package com.aston.utils.servlet.provider;

public interface Provider<T> {

	public T get();

	public void set(T o);
}
