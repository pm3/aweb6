package com.aston.utils.exec.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class CronScheduler extends TimerTask {
	private List<CronTask> tasks = new ArrayList<CronTask>();
	private Timer timer = null;

	public void addTrigger(String expression, Executor executor, Runnable runnable) {
		addTrigger(null, expression, executor, runnable);
	}

	public void addTrigger(String name, String expression, Executor executor, Runnable runnable) {
		SchedulingPattern pattern = new SchedulingPattern(expression);
		System.out.println("name " + pattern.toString());
		tasks.add(new CronTask(name, pattern, executor, runnable));
	}

	public void changeExpression(String name, String expression) {
		SchedulingPattern pattern = new SchedulingPattern(expression);
		System.out.println("name " + pattern.toString());
		for (CronTask t : tasks)
			if (t.getName() != null && t.getName().equals(name))
				t.setPattern(pattern);
	}

	@Override
	public void run() {
		try {
			Calendar c = Calendar.getInstance();
			for (CronTask t : tasks) {
				if (t.getPattern().isTime(c)) {
					try {
						t.getExecutor().execute(t.getRunnable());
					} catch (Throwable tt) {
						System.err.println("error execute [" + t.getPattern() + "] " + tt.getMessage());
					}
				}
			}
		} catch (Throwable t) {
		}
	}

	public void start() {
		long now = System.currentTimeMillis();
		// start next minute
		long start = now - now % 60000 + 60000;
		// if next minute is too soon, delay start to next minute
		if (start - now < 10000)
			start += 60000;
		this.timer = new Timer("aweb.cron", true);
		this.timer.scheduleAtFixedRate(this, 60000, 60000);
	}

	public void stop() {
		this.timer.cancel();
		this.timer = null;
	}

	public boolean isRunning() {
		return timer != null;
	}

	public static class CronTask {
		private String name;
		private SchedulingPattern pattern;
		private Executor executor;
		private Runnable runnable;

		public CronTask(String name, SchedulingPattern pattern, Executor executor, Runnable runnable) {
			this.name = null;
			this.pattern = pattern;
			this.executor = executor;
			this.runnable = runnable;
		}

		public String getName() {
			return name;
		}

		public SchedulingPattern getPattern() {
			return pattern;
		}

		public void setPattern(SchedulingPattern pattern) {
			this.pattern = pattern;
		}

		public Executor getExecutor() {
			return executor;
		}

		public Runnable getRunnable() {
			return runnable;
		}
	}

}
