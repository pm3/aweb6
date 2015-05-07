package com.aston.utils.exec;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleBlockedExecutor implements Executor, HasShutdown {
	private final ExecutorService exec;
	private final IExecSemaphore semaphore;

	public SingleBlockedExecutor() {
		this(new JvmSemaphore());
	}

	public SingleBlockedExecutor(IExecSemaphore execSemaphore) {
		this.exec = new ThreadPoolExecutor(0, 1, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1));
		this.semaphore = execSemaphore;
	}

	@Override
	public void execute(final Runnable command) {
		try {
			semaphore.acquire();
			try {
				exec.execute(new Runnable() {
					public void run() {
						try {
							command.run();
						} finally {
							semaphore.release();
						}
					}
				});
			} catch (RejectedExecutionException e) {
				semaphore.release();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void shutdown() {
		exec.shutdownNow();
	}

	public static class JvmSemaphore implements IExecSemaphore {

		private Semaphore jvm = new Semaphore(1);

		@Override
		public void acquire() throws InterruptedException {
			jvm.acquire();
		}

		@Override
		public void release() {
			jvm.release();
		}
	}

}
