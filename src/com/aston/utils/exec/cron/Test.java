package com.aston.utils.exec.cron;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;

public class Test {
	public static void main(String[] args) {
		main1(args);
		main2(args);
		// main3(args);
	}

	public static void main1(String[] args) {
		Calendar c = GregorianCalendar.getInstance();
		SchedulingPattern p1 = new SchedulingPattern("1,3  1 1 1 ? 2010");
		System.out.println(p1.toString());
		c.set(2010, 0, 1, 1, 1, 0);
		System.out.println(p1.isTime(c));
		c.set(2010, 0, 1, 1, 3, 0);
		System.out.println(p1.isTime(c));
		c.set(2010, 0, 1, 1, 5, 0);
		System.out.println(p1.isTime(c));

		SchedulingPattern p2 = new SchedulingPattern("2/11 1,3,4 * 1 ?");
		System.out.println(p2.toString());
		c.set(2010, 0, 1, 3, 24, 10);
		System.out.println(p2.isTime(c));

		SchedulingPattern p3 = new SchedulingPattern("2/11 1,3,4 1 feb,mar mon");
		System.out.println(p3.toString());

		SchedulingPattern p4 = new SchedulingPattern("0 23 * * mon,tue,wed,thu,fri,sat");
		System.out.println(p4.toString());
	}

	public static void main2(String[] args) {
		Executor main = new Executor() {

			@Override
			public void execute(Runnable command) {
				command.run();
			}
		};
		CronScheduler s = new CronScheduler();
		s.addTrigger("0/5 * * * *", main, new TestRunner("1"));
		s.addTrigger("20 * * * *", main, new TestRunner("2"));
		s.addTrigger("30-45 * * * *", main, new TestRunner("3"));
		s.addTrigger("5,10 * * * *", main, new TestRunner("4"));
		s.start();
		try {
			Thread.sleep(60 * 1000);
		} catch (Exception ex) {

		}
		s.stop();
	}

	static class TestRunner implements Runnable {
		String id;

		TestRunner(String id) {
			this.id = id;
		}

		public void run() {
			Calendar c = GregorianCalendar.getInstance();
			System.out.println("run:" + id + " " + c.get(Calendar.SECOND) + " -- " + c.get(Calendar.MILLISECOND));
		}

	}
}
