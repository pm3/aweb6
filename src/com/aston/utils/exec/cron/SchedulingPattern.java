package com.aston.utils.exec.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SchedulingPattern {
	private String m_expression;
	private List<IValueMatcher> m_matchers = new ArrayList<IValueMatcher>();

	public SchedulingPattern(String expression) {
		this.m_expression = expression;
		if (expression == null || expression.trim().length() == 0) {
			throw new IllegalArgumentException("expression is empty");
		}
		parse(expression.toLowerCase());
	}

	public boolean isTime(Calendar c) {
		int[] timeArray = new int[] { c.get(Calendar.MINUTE), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_WEEK),
				c.get(Calendar.YEAR) };
		boolean[] res = new boolean[] { false, false, false, false, false, false };

		for (IValueMatcher m : m_matchers) {
			if (m.match(timeArray[m.getArrayPosition()]) == true)
				res[m.getArrayPosition()] = true;
		}
		for (int i = 0; i < res.length; i++) {
			if (res[i] == false)
				return false;
		}
		return true;
	}

	private void parse(String expression) {
		String[] compositeItems = expression.trim().split("\\s+");
		if (compositeItems.length < 5 || compositeItems.length > 6)
			throw new IllegalArgumentException("undefined pattern(min hour day month week year): " + expression);

		// minute
		parseCompositeItem(compositeItems, 0, 0, 59, null);
		// hour
		parseCompositeItem(compositeItems, 1, 0, 23, null);
		// day
		if ("?".equals(compositeItems[2])) {
			m_matchers.add(new AllValueMatcher(2));
		} else {
			parseCompositeItem(compositeItems, 2, 1, 31, null);
		}
		// month
		parseCompositeItem(compositeItems, 3, 1, 12, new String[] { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" });
		// week
		if ("?".equals(compositeItems[4])) {
			m_matchers.add(new AllValueMatcher(4));
		} else {
			parseCompositeItem(compositeItems, 4, 1, 7, new String[] { "sun", "mon", "tue", "wed", "thu", "fri", "sat" });
		}
		// year
		if (compositeItems.length == 6) {
			parseCompositeItem(compositeItems, 5, 2000, 2099, null);
		} else {
			m_matchers.add(new AllValueMatcher(5));
		}
	}

	private void parseCompositeItem(String[] xstrArray, int arrayPos, int xmin, int xmax, String[] aliases) {
		String xstr = xstrArray[arrayPos];
		if ("*".equals(xstr) == true) {
			m_matchers.add(new AllValueMatcher(arrayPos));
			return;
		}
		String[] localItems = xstr.split(",");
		for (String item : localItems) {
			int pos = item.indexOf('-');
			if (pos == 0) {
				// -max
				int max = parseItem(item.substring(pos + 1), xmin, aliases);
				if (max < 0 || max > xmax)
					throw new IllegalArgumentException("1bad expression [pos=" + (arrayPos + 1) + "] [" + item + "]: " + m_expression);
				m_matchers.add(new MinMaxValueMatcher(arrayPos, xmin, max, 1));
			} else if (pos == item.length() - 1) {
				// min-
				int min = parseItem(item.substring(0, pos), xmin, aliases);
				if (min < 0 || min < xmin)
					throw new IllegalArgumentException("2bad expression [pos=" + (arrayPos + 1) + "] [" + item + "]: " + m_expression);
				m_matchers.add(new MinMaxValueMatcher(arrayPos, min, xmax, 1));
			} else if (pos > 0) {
				// min-max
				int min = parseItem(item.substring(0, pos), xmin, aliases);
				int max = parseItem(item.substring(pos + 1), xmin, aliases);
				if (min < xmin || min > max)
					throw new IllegalArgumentException("3bad expression [pos=" + (arrayPos + 1) + "] [" + item + "]: " + m_expression);
				if (max > xmax)
					throw new IllegalArgumentException("4bad expression [pos=" + (arrayPos + 1) + "] [" + item + "]: " + m_expression);
				m_matchers.add(new MinMaxValueMatcher(arrayPos, min, max, 1));
			} else {
				// pos<0
				int pos2 = item.indexOf('/');
				if (pos2 > 0) {
					// min/step
					int min = parseItem(item.substring(0, pos2), xmin, aliases);
					int step = parseItem(item.substring(pos2 + 1), 0, null);
					if (min < xmin)
						throw new IllegalArgumentException("5bad expression [pos=" + (arrayPos + 1) + "] [" + item + "]: " + m_expression);
					if (step < 1 || step > xmax - min)
						throw new IllegalArgumentException("6bad expression [pos=" + (arrayPos + 1) + "] [" + item + "]: " + m_expression);
					m_matchers.add(new MinMaxValueMatcher(arrayPos, min, xmax, step));
				} else {
					// simple value
					int v = parseItem(item, xmin, aliases);
					if (v < xmin || v > xmax)
						throw new IllegalArgumentException("7bad expression [pos=" + (arrayPos + 1) + "] [" + item + "] : " + m_expression);
					m_matchers.add(new SingleValueMatcher(arrayPos, v));
				}
			}
		}
	}

	private int parseItem(String str, int alias_pref, String[] aliases) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			if (aliases != null) {
				for (int i = 0; i < aliases.length; i++)
					if (aliases[i].equals(str))
						return alias_pref + i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(m_expression).append("]: ");
		for (IValueMatcher vm : m_matchers) {
			sb.append(vm.toString()).append(", ");
		}
		return sb.toString();
	}

	public static interface IValueMatcher {
		public int getArrayPosition();

		public boolean match(int value);
	}

	public static class AllValueMatcher implements IValueMatcher {
		int arrayPosition;

		public AllValueMatcher(int arrayPosition) {
			this.arrayPosition = arrayPosition;
		}

		public int getArrayPosition() {
			return arrayPosition;
		}

		public boolean match(int value) {
			return true;
		}

		@Override
		public String toString() {
			return "{" + arrayPosition + ":all}";
		}
	}

	public static class SingleValueMatcher implements IValueMatcher {
		int arrayPosition;
		int value;

		public SingleValueMatcher(int arrayPosition, int value) {
			this.arrayPosition = arrayPosition;
			this.value = value;
		}

		public int getArrayPosition() {
			return arrayPosition;
		}

		public boolean match(int value) {
			return this.value == value;
		}

		@Override
		public String toString() {
			return "{" + arrayPosition + ":" + value + "}";
		}
	}

	public static class MinMaxValueMatcher implements IValueMatcher {
		int arrayPosition;
		int min, max, step;

		public MinMaxValueMatcher(int arrayPosition, int min, int max, int step) {
			this.arrayPosition = arrayPosition;
			this.min = min;
			this.max = max;
			this.step = step;
		}

		public int getArrayPosition() {
			return arrayPosition;
		}

		public boolean match(int value) {
			if (step == 1) {
				return value >= min && value <= max;
			}
			for (int i = min; i <= max; i += step) {
				if (i == value)
					return true;
			}
			return false;
		}

		@Override
		public String toString() {
			if (step == 1) {
				return "{" + arrayPosition + ":" + min + "-" + max + "}";
			}
			StringBuffer sb = new StringBuffer();
			for (int i = min; i <= max; i += step) {
				sb.append(i).append(",");
			}
			return "{" + arrayPosition + ":" + sb.toString() + "}";
		}
	}
}
