package com.aston.utils.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IRow<T> {
	T row(ResultSet rs) throws SQLException;
}
