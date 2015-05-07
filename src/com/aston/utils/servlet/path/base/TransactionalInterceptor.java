package com.aston.utils.servlet.path.base;

import com.aston.utils.servlet.IWebMethodInterceptor;
import com.aston.utils.servlet.WebMethodInvoker;
import com.aston.utils.sql.ThreadTrDbc;

public class TransactionalInterceptor implements IWebMethodInterceptor {

	private ThreadTrDbc dbc;

	TransactionalInterceptor(ThreadTrDbc dbc) {
		this.dbc = dbc;
	}

	@Override
	public int order() {
		return 10;
	}

	@Override
	public void process(WebMethodInvoker invoker) throws Exception {

		try {
			dbc.startTransaction();
			invoker.invoke();
			dbc.commit();
		} catch (Throwable e) {
			dbc.rollback();
			throw e;
		}
	}

}
