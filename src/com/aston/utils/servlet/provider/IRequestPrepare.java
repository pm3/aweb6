package com.aston.utils.servlet.provider;

import javax.servlet.http.HttpServletRequest;

import com.aston.utils.servlet.HttpStateException;

public interface IRequestPrepare {

	public void prepare(HttpServletRequest request) throws HttpStateException;
}
