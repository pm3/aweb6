package com.aston.utils.sql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aston.utils.sql.anot.Column;
import com.aston.utils.sql.anot.Table;
import com.aston.utils.sql.convert.EnumStringConverter;

public class EntityInfo<T> {
	Class<T> type;
	String tableName;
	EntityProp id;
	List<EntityProp> props;
	String insertSql;
	String updateSql;
	String deleteSql;
	Map<String, EntityProp> dbnameProps;

	public EntityInfo(Class<T> type, Map<Class<?>, IConverter> converters) throws SQLException {

		this.type = type;
		Table t = type.getAnnotation(Table.class);
		boolean camelize = t != null ? t.camelize() : false;
		tableName = t != null ? t.name() : null;
		if (tableName == null || tableName.isEmpty())
			tableName = camelize ? camelize(type.getSimpleName()) : type.getSimpleName();
		this.props = createEntityProps(type, camelize, converters);
		String idName = t != null ? t.id() : null;
		if (idName == null || idName.isEmpty())
			idName = "id";
		this.dbnameProps = new HashMap<String, EntityInfo.EntityProp>(props.size());
		for (EntityProp ep : props) {
			if (ep.name.equals(idName))
				this.id = ep;
			dbnameProps.put(ep.dbname, ep);
		}
		if (id == null)
			throw new SQLException("entity does id column " + type.getName());
		props.remove(id);

		this.insertSql = createInsertSql();
		this.updateSql = createUpdateSql();
		this.deleteSql = createDeleteSql();
	}

	protected String createInsertSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("insert into ").append(tableName).append(" (");
		for (EntityProp ep : props)
			sb.append(ep.dbname).append(',');
		sb.setLength(sb.length() - 1);
		sb.append(") values (");
		for (int i = 0; i < props.size(); i++)
			sb.append("?,");
		sb.setLength(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}

	protected String createUpdateSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("update ").append(tableName).append(" set ");
		for (EntityProp ep : props)
			sb.append(ep.dbname).append("=?,");
		sb.setLength(sb.length() - 1);
		sb.append(" where ").append(id.dbname).append("=?");
		return sb.toString();
	}

	protected String createDeleteSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("delete from ").append(tableName);
		sb.append(" where ").append(id.dbname).append("=?");
		return sb.toString();
	}

	protected List<EntityProp> createEntityProps(Class<?> type, boolean camelize, Map<Class<?>, IConverter> converters) {
		List<EntityProp> l = new ArrayList<EntityProp>();
		for (Method m : type.getDeclaredMethods()) {
			if (!m.getReturnType().equals(Void.class) && m.getParameterTypes().length == 0) {
				String mn = m.getName();
				String pn = null;
				if (mn.startsWith("get"))
					pn = mn.substring(3);
				else if (mn.startsWith("is"))
					pn = mn.substring(2);

				if (pn != null) {
					try {
						EntityProp ep = createEntityProp(type, camelize, m, pn, converters);
						l.add(ep);
					} catch (Exception e) {
					}
				}
			}
		}
		return l;
	}

	protected EntityProp createEntityProp(Class<?> type, boolean camelize, Method m, String pn, Map<Class<?>, IConverter> converters) throws NoSuchMethodException,
			InstantiationException, IllegalAccessException {
		Method setter = type.getDeclaredMethod("set" + pn, m.getReturnType());
		String n1 = pn.substring(0, 1).toLowerCase() + pn.substring(1);
		String sqlName = null;
		IConverter converter = null;
		Column column = seachColumnAnnotation(m, type, n1);
		if (column != null) {
			if (column.name() != null && !column.name().isEmpty())
				sqlName = column.name();
			if (column.convert() != null && !IConverter.class.equals(column.convert())) {
				converter = column.convert().newInstance();
				converter.setFormat(column.format());
			}
		}
		if (sqlName == null)
			sqlName = camelize ? camelize(n1) : n1;
		if (converter == null) {
			converter = converters.get(m.getReturnType());
		}
		EntityProp ep = new EntityProp(m.getReturnType(), n1, sqlName, m, setter, converter);
		return ep;
	}

	protected Column seachColumnAnnotation(Method getter, Class<?> parent, String name) {
		Column c = getter.getAnnotation(Column.class);
		if (c == null) {
			try {
				Field f = parent.getDeclaredField(name);
				c = f.getAnnotation(Column.class);
			} catch (Exception e) {
			}
		}
		return c;
	}

	public Object[] createInsertData(Object e) throws Exception {
		Object[] a = new Object[props.size()];
		for (int i = 0; i < props.size(); i++)
			a[i] = props.get(i).getToSql(e);
		return a;
	}

	public Object[] createUpdateData(Object e) throws Exception {
		Object[] a = new Object[props.size() + 1];
		for (int i = 0; i < props.size(); i++)
			a[i] = props.get(i).getToSql(e);
		a[a.length - 1] = id.getToSql(e);
		return a;
	}

	public Object[] createDeleteData(Object e) throws Exception {
		Object[] a = new Object[1];
		a[0] = id.getToSql(e);
		return a;
	}

	public Object[] createDeleteIdData(Object idval) throws Exception {
		Object[] a = new Object[1];
		a[0] = idval;
		return a;
	}

	public static String camelize(String s) {
		StringBuffer sb = new StringBuffer(s.length() + 10);
		char[] buf = s.toCharArray();
		for (int i = 0; i < buf.length; i++) {
			char ch = buf[i];
			if (i > 0 && Character.isUpperCase(ch))
				sb.append('_');
			sb.append(Character.toLowerCase(ch));
		}
		return sb.toString();
	}

	public static class EntityProp {
		String name;
		String dbname;
		Class<?> type;
		Method getter;
		Method setter;
		IConverter converter;

		public EntityProp(Class<?> type, String name, String dbname, Method getter, Method setter, IConverter converter) {
			this.type = type;
			this.name = name;
			this.dbname = dbname;
			this.getter = getter;
			this.setter = setter;
			this.converter = converter;
			if (type.isEnum() && converter == null)
				this.converter = new EnumStringConverter();
		}

		public Object get(Object obj) throws Exception {
			return getter.invoke(obj);
		}

		public Object getToSql(Object obj) throws Exception {
			Object val = getter.invoke(obj);
			if (converter != null)
				val = converter.bean2sql(val, type);
			return val;
		}

		public void set(Object obj, Object val) throws Exception {
			if (converter != null)
				val = converter.sql2bean(val, type);
			setter.invoke(obj, val);
		}

		public void setFromSql(Object obj, Object val) throws Exception {
			try {
				if (converter != null)
					val = converter.sql2bean(val, type);
				setter.invoke(obj, val);
			} catch (Exception e) {
				throw new Exception("set value [type=" + type.getSimpleName() + "]" + name + " = " + val, e);
			}
		}
	}

	public IRow<T> createRow() {
		return new EntityRow<T>(type, dbnameProps);
	}

	public static class EntityRow<T> implements IRow<T> {

		private Class<T> type;
		private Map<String, EntityProp> dbnameProps;
		private EntityProp[] columns;

		public EntityRow(Class<T> type, Map<String, EntityProp> dbnameProps) {
			this.type = type;
			this.dbnameProps = dbnameProps;
		}

		@Override
		public T row(ResultSet rs) throws SQLException {

			if (columns == null) {
				ResultSetMetaData mi = rs.getMetaData();
				columns = new EntityProp[mi.getColumnCount()];
				for (int i = 0; i < mi.getColumnCount(); i++) {
					String dbn = mi.getColumnLabel(i + 1);
					columns[i] = dbnameProps.get(dbn);
				}
			}

			T obj = null;
			try {
				obj = type.newInstance();
				for (int i = 0; i < columns.length; i++) {
					try {
						Object sqlval = rs.getObject(i + 1);
						EntityProp ep = columns[i];
						if (ep != null)
							ep.setFromSql(obj, sqlval);
					} catch (Exception e) {
						System.err.println(type.getSimpleName() + " " + e.getMessage());
					}
				}
			} catch (Exception e) {
				throw new SQLException("row " + type.getSimpleName() + " " + e.getMessage(), e);
			}
			return obj;
		}
	};

}
