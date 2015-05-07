package com.aston.utils.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class ThreadTrDbc extends Dbc {

	private DataSource ds = null;

	public ThreadTrDbc(DataSource ds) {
		this.ds = ds;
	}

	public DataSource getDataSource() {
		return ds;
	}

	private static ThreadLocal<LazzyConnection> threadConnection = new ThreadLocal<LazzyConnection>();

	public Connection getConnection() throws SQLException {
		if (threadConnection.get() != null)
			return threadConnection.get().connection();
		return ds.getConnection();
	}

	public void startTransaction() throws SQLException {
		if (threadConnection.get() != null)
			throw new SQLException("double open transaction");
		threadConnection.set(new LazzyConnection(ds));
	}

	@Override
	protected void closeConnection(Connection c, PreparedStatement ps, ResultSet rs) {
		super.closeConnection(c, ps, rs);
		if (threadConnection.get() == null) {
			try {
				close(c, null);
			} catch (SQLException e) {
			}
		}
	}

	protected void close(boolean commit) throws SQLException {
		LazzyConnection lc = threadConnection.get();
		if (lc == null)
			throw new SQLException("not started transaction");
		threadConnection.remove();
		if (lc.initialized()) {
			Connection c = lc.connection();
			close(c, commit);
		}
	}

	protected void close(Connection c, Boolean commit) throws SQLException {
		try {
			if (commit != null) {
				if (commit.booleanValue())
					c.commit();
				else
					c.rollback();
			}
		} finally {
			try {
				c.close();
			} catch (Exception e) {
			}
		}
	}

	public void commit() throws SQLException {
		close(true);
	}

	public void rollback() throws SQLException {
		close(false);
	}

	public static class LazzyConnection {
		private DataSource dataSource;
		private Connection connection = null;

		public LazzyConnection(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		public boolean initialized() {
			return connection != null;
		}

		public Connection connection() throws SQLException {
			if (connection == null) {
				dataSource.getConnection();
			}
			return connection;
		}
	}
}
