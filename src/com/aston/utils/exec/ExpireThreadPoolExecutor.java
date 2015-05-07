package com.aston.utils.exec;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExpireThreadPoolExecutor implements Executor, HasShutdown {

	private int ttl;
	private TimeUnit ttlUnit;
	private ThreadPoolExecutor poolExecutor;

	public ExpireThreadPoolExecutor(ThreadPoolExecutor poolExecutor, int ttl, TimeUnit ttlUnit) {
		this.poolExecutor = poolExecutor;
		this.ttl = ttl;
		this.ttlUnit = ttlUnit;
	}

	@Override
	public void execute(Runnable command) {
		long now = System.currentTimeMillis();
		for (Iterator<Runnable> it = poolExecutor.getQueue().iterator(); it.hasNext();) {
			if (((ExpireRunnable) it.next()).expire < now)
				it.remove();
		}
		long expire = now + ttlUnit.toMillis(ttl);
		poolExecutor.execute(new ExpireRunnable(expire, command));
	}

	@Override
	public void shutdown() {
		poolExecutor.shutdownNow();
	}

	public static class ExpireRunnable implements Runnable {
		private long expire;
		private Runnable command;

		public ExpireRunnable(long expire, Runnable command) {
			this.expire = expire;
			this.command = command;
		}

		@Override
		public void run() {
			if (expire > System.currentTimeMillis())
				command.run();
		}
	}

}
