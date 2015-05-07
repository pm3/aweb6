package com.aston.utils.sql;

public interface IConverter {

	public void setFormat(String format);

	public Object bean2sql(Object val, Class<?> type) throws Exception;

	public Object sql2bean(Object val, Class<?> type) throws Exception;
}
