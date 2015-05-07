package com.aston.utils.exec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aston.utils.exec.cron.CronScheduler;

public class TaskQueueFactory implements HasShutdown {

	private ConcurrentHashMap<String, Executor> executorsMap = new ConcurrentHashMap<String, Executor>();
	private CronScheduler cronScheduler = new CronScheduler();

	public void addTaskGroup(String group, Executor executor) {
		if (executorsMap.containsKey(group))
			throw new IllegalStateException("task group exist: " + group);
		this.executorsMap.put(group, executor);
	}

	public void createGroupThreadPool(String group, int maximumPoolSize, int maxQueueSize) {
		ThreadPoolExecutor e = new ThreadPoolExecutor(0, maximumPoolSize, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(maxQueueSize));
		addTaskGroup(group, e);
	}

	public void createGroupThreadPoolExpire(String group, int maximumPoolSize, int maxQueueSize, int expire, TimeUnit unit) {
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(0, maximumPoolSize, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(maxQueueSize));
		addTaskGroup(group, new ExpireThreadPoolExecutor(tpe, expire, unit));
	}

	public void createGroupSingleBlocked(String group, IExecSemaphore semaphore) {
		addTaskGroup(group, new SingleBlockedExecutor(semaphore));
	}

	public void run(String group, Runnable task) {
		Executor e = executorsMap.get(group);
		if (e == null)
			throw new IllegalStateException("undifined task group: " + group);
		e.execute(task);
	}

	public void addCronTask(String group, String expression, Runnable task) {
		Executor e = executorsMap.get(group);
		if (e == null)
			throw new IllegalStateException("undifined task group: " + group);
	}

	public void start() {
		cronScheduler.start();
	}

	public void shutdown() {
		cronScheduler.stop();
		for (Executor e : executorsMap.values()) {
			if (e instanceof ExecutorService)
				((ExecutorService) e).shutdownNow();
			else if (e instanceof HasShutdown)
				((HasShutdown) e).shutdown();
		}
	}
}
