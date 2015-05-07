package com.aston.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

	public static String lastToken(String str, char delim) {
		if (str == null)
			return null;
		int i = str.lastIndexOf(delim);
		if (i < 0)
			return str;
		if (i == str.length() - 1)
			return "";
		return str.substring(i + 1);
	}

	public static List<String> tokenizeTrimString(String text, String delimiter) {
		List<String> l = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(text, delimiter);
		while (st.hasMoreElements()) {
			String el = st.nextToken();
			if (el == null)
				continue;
			el = el.trim();
			if (el.length() > 0)
				l.add(el);
		}
		return l;
	}

	public static String[] tokenizeTrimStringA(String text, String delimiter) {
		List<String> l = tokenizeTrimString(text, delimiter);
		return (String[]) l.toArray(new String[l.size()]);
	}

	public static String join(List<?> items, String delim) {
		StringBuffer sb = new StringBuffer();
		Iterator<?> i = items.iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			if (i.hasNext())
				sb.append(delim);
		}
		return sb.toString();
	}

	public static String joinA(Object[] items, String delim) {
		if (items == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < items.length; i++) {
			sb.append(items[i]);
			if (i + 1 < items.length)
				sb.append(delim);
		}
		return sb.toString();
	}

	public static boolean contains(String[] sarray, String value) {
		if (sarray == null || value == null)
			return false;
		for (int i = 0; i < sarray.length; i++)
			if (value.equals(sarray[i]))
				return true;
		return false;
	}

	public static String contains(String[] items1, String[] items2) {
		if (items1 == null || items2 == null)
			return null;
		for (int i1 = 0; i1 < items1.length; i1++) {
			for (int i2 = 0; i2 < items2.length; i2++) {
				if (items1[i1].equals(items2[i2]))
					return items1[i1];
			}
		}
		return null;
	}

	public static List<String[]> matchList(String exp, String content) {
		return matchList(exp, content, Pattern.DOTALL);
	}

	public static List<String[]> matchList(String exp, String content, int flags) {
		List<String[]> res = new ArrayList<String[]>();
		Pattern p = Pattern.compile(exp, flags);

		Matcher m = p.matcher(content);
		while (m.find()) {
			String[] row = new String[m.groupCount()];
			for (int i = 0; i < row.length; i++) {
				row[i] = m.group(i + 1);
			}
			res.add(row);
		}
		return res;
	}
}
