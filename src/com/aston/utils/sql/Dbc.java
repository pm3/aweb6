package com.aston.utils.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Dbc {

	protected abstract Connection getConnection() throws SQLException;

	protected void closeConnection(Connection c, PreparedStatement ps, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
		}
		try {
			if (ps != null)
				ps.close();
		} catch (Exception e) {
		}
	}

	protected void fillPs(PreparedStatement ps, Object[] params) throws SQLException {
		if (params != null)
			for (int i = 0; i < params.length; i++) {
				Object v = params[i];
				if (v instanceof Date) {
					v = new Timestamp(((Date) v).getTime());
				} else if (v != null && v.getClass().isArray()) {
					v = ps.getConnection().createArrayOf("text", (Object[]) v);
				}

				ps.setObject(i + 1, v);
			}
	}

	protected static ConcurrentMap<Class<?>, EntityInfo<?>> _entityInfos = new ConcurrentHashMap<Class<?>, EntityInfo<?>>();
	protected Map<Class<?>, IConverter> _converters = new HashMap<Class<?>, IConverter>();

	public void addConverter(Class<?> type, IConverter converter) {
		if (type != null && converter != null)
			_converters.put(type, converter);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> EntityInfo<T> createEntityInfo(Class<T> cl) throws SQLException {
		EntityInfo<?> ei = _entityInfos.get(cl);
		if (ei == null) {
			synchronized (this) {
				ei = _entityInfos.get(cl);
				if (ei == null) {
					ei = new EntityInfo(cl, _converters);
					_entityInfos.put(cl, ei);
				}
			}
		}
		return (EntityInfo<T>) ei;
	}

	protected <T> IRow<T> getEntityRow(Class<T> cl) throws SQLException {
		EntityInfo<T> ei = createEntityInfo(cl);
		return ei.createRow();
	}

	public <T> T load(Class<T> type, Object id) throws SQLException {
		EntityInfo<T> ei = createEntityInfo(type);
		return select1(ei.createRow(), "select * from " + ei.tableName + " where " + ei.id.dbname + "=?", id);
	}

	public int save(Object entity) throws SQLException {
		int count = 0;
		EntityInfo<?> ei = createEntityInfo(entity.getClass());
		Object[] a = null;
		try {
			Object oid = ei.id.get(entity);
			if (emptyId(oid)) {
				a = ei.createInsertData(entity);
				oid = insert(ei.insertSql, a);
				ei.id.set(entity, oid);
				count = 1;
			} else {
				a = ei.createUpdateData(entity);
				count = update(ei.updateSql, a);
			}
		} catch (Exception e) {
			throw new SQLException(e.getMessage(), e);
		}
		return count;
	}

	public int delete(Object entity) throws SQLException {
		EntityInfo<?> ei = createEntityInfo(entity.getClass());
		Object[] a = null;
		try {
			a = ei.createDeleteData(entity);
		} catch (Exception e) {
			throw new SQLException(e.getMessage(), e);
		}
		return update(ei.deleteSql, a);
	}

	public int delete(Class<?> type, Object id) throws SQLException {
		EntityInfo<?> ei = createEntityInfo(type);
		Object[] a = null;
		try {
			a = ei.createDeleteIdData(id);
		} catch (Exception e) {
			throw new SQLException(e.getMessage(), e);
		}
		return update(ei.deleteSql, a);
	}

	protected boolean emptyId(Object oid) {
		if (oid == null)
			return true;
		if (oid instanceof Long && ((Number) oid).longValue() == 0)
			return true;
		if (oid instanceof Integer && ((Number) oid).intValue() == 0)
			return true;
		return false;
	}

	public Object insert(String sql, Object... params) throws SQLException {
		long l1 = System.nanoTime();
		Object oid = null;
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = getConnection();
			ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			fillPs(ps, params);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				oid = rs.getObject(1);
		} finally {
			closeConnection(c, ps, rs);
		}
		long l2 = System.nanoTime();
		System.out.println(sql + " " + ((l2 - l1) / 1000));
		return oid;
	}

	public int update(String sql, Object... params) throws SQLException {
		// System.out.println(sql);
		long l1 = System.nanoTime();
		int r = 0;
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = getConnection();
			ps = c.prepareStatement(sql);
			fillPs(ps, params);
			r = ps.executeUpdate();
		} finally {
			closeConnection(c, ps, null);
		}
		long l2 = System.nanoTime();
		System.out.println(sql + " " + ((l2 - l1) / 1000));
		return r;
	}

	public <T> List<T> select(Class<T> type, String sql, Object... params) throws SQLException {
		return select(getEntityRow(type), sql, params);
	}

	public <T> T select1(Class<T> type, String sql, Object... params) throws SQLException {
		return select1(getEntityRow(type), sql, params);
	}

	public <T> List<T> select(IRow<T> row, String sql, Object... params) throws SQLException {
		return select0(new ResultRows<T>(row), sql, params);
	}

	public <T> T select1(IRow<T> row, String sql, Object... params) throws SQLException {
		return select0(new ResultRow1<T>(row), sql, params);
	}

	public <T> T select0(IResult<T> result, String sql, Object... params) throws SQLException {
		long l1 = System.nanoTime();
		T val = null;
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = getConnection();
			ps = c.prepareStatement(sql);
			fillPs(ps, params);
			rs = ps.executeQuery();
			val = result.result(rs);
		} finally {
			closeConnection(c, ps, rs);
		}
		long l2 = System.nanoTime();
		System.out.println(sql + " " + ((l2 - l1) / 1000));
		return val;
	}

	protected static class ResultRows<T> implements IResult<List<T>> {
		private IRow<T> row;

		protected ResultRows(IRow<T> row) {
			this.row = row;
		}

		@Override
		public List<T> result(ResultSet rs) throws SQLException {
			List<T> l = new ArrayList<T>();
			while (rs.next()) {
				l.add(row.row(rs));
			}
			return l;
		}
	}

	protected static class ResultRow1<T> implements IResult<T> {
		private IRow<T> row;

		protected ResultRow1(IRow<T> row) {
			this.row = row;
		}

		@Override
		public T result(ResultSet rs) throws SQLException {
			T o = null;
			if (rs.next()) {
				o = row.row(rs);
			}
			return o;
		}
	}

	public static IRow<Integer> singleInt = new IRow<Integer>() {

		@Override
		public Integer row(ResultSet rs) throws SQLException {
			Object o = rs.getObject(1);
			if (o instanceof Integer)
				return (Integer) o;
			if (o instanceof Number)
				return new Integer(((Number) o).intValue());
			return null;
		}
	};

	public static IRow<Long> singleLong = new IRow<Long>() {

		@Override
		public Long row(ResultSet rs) throws SQLException {
			Object o = rs.getObject(1);
			if (o instanceof Long)
				return (Long) o;
			if (o instanceof Number)
				return new Long(((Number) o).longValue());
			return null;
		}
	};

	public static IRow<String> singleString = new IRow<String>() {

		@Override
		public String row(ResultSet rs) throws SQLException {
			return rs.getString(1);
		}
	};

	public static IRow<Boolean> singleBoolean = new IRow<Boolean>() {

		@Override
		public Boolean row(ResultSet rs) throws SQLException {
			Object o = rs.getObject(1);
			if (o instanceof Boolean)
				return (Boolean) o;
			if (o instanceof Number)
				return ((Number) o).intValue() != 0;
			return null;
		}
	};

	public static IRow<Object[]> array = new IRow<Object[]>() {

		@Override
		public Object[] row(ResultSet rs) throws SQLException {
			int max = rs.getMetaData().getColumnCount();
			Object[] a = new Object[max];
			for (int i = 0; i < max; i++)
				a[i] = rs.getObject(i + 1);
			return a;
		}
	};
}
