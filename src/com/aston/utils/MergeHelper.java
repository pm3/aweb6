package com.aston.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MergeHelper {

	public static <T1, T2> void merge(List<T1> newl, List<T2> oldl, IMergeLists<T1, T2> m) throws Exception {

		Map<Object, T2> map = new HashMap<Object, T2>(oldl.size());
		Set<Object> usedId = new TreeSet<Object>();

		for (T2 t2 : oldl)
			map.put(m.getOldId(t2), t2);
		for (T1 t1 : newl) {
			Object id = m.getNewId(t1);
			T2 t2 = map.get(id);
			if (t2 != null) {
				m.updateItem(t1, t2);
				usedId.add(id);
			} else {
				m.newItem(t1);
			}
		}
		for (T2 t2 : oldl) {
			if (!usedId.contains(m.getOldId(t2))) {
				m.deleteItem(t2);
			}
		}
	}

	public static interface IMergeLists<T1, T2> {
		Object getNewId(T1 newItem) throws Exception;

		Object getOldId(T2 oldItem) throws Exception;

		void newItem(T1 newItem) throws Exception;

		void updateItem(T1 newItem, T2 oldItem) throws Exception;

		void deleteItem(T2 oldItem) throws Exception;
	}

	public static <K, T1, T2> void merge(Map<K, T1> newm, Map<K, T2> oldm, IMergeMap<T1, T2> m) throws Exception {
		Set<K> usedId = new TreeSet<K>();
		for (Map.Entry<K, T1> e1 : newm.entrySet()) {
			T2 t2 = oldm.get(e1.getKey());
			if (t2 != null) {
				m.updateItem(e1.getValue(), t2);
				usedId.add(e1.getKey());
			} else {
				m.newItem(e1.getValue());
			}
		}
		for (Map.Entry<K, T2> e2 : oldm.entrySet()) {
			if (!usedId.contains(e2.getKey())) {
				m.deleteItem(e2.getValue());
			}
		}
	}

	public static interface IMergeMap<T1, T2> {
		void newItem(T1 newItem) throws Exception;

		void updateItem(T1 newItem, T2 oldItem) throws Exception;

		void deleteItem(T2 oldItem) throws Exception;
	}

}
