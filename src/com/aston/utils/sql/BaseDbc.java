package com.aston.utils.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class BaseDbc extends Dbc {

	private DataSource ds = null;

	public BaseDbc(DataSource ds) {
		this.ds = ds;
	}

	public DataSource getDataSource() {
		return ds;
	}

	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	@Override
	protected void closeConnection(Connection c, PreparedStatement ps, ResultSet rs) {
		super.closeConnection(c, ps, rs);
		try {
			c.close();
		} catch (SQLException e) {
		}
	}
}
