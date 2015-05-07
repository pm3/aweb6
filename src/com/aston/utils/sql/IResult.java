package com.aston.utils.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IResult<T> {
	T result(ResultSet rs) throws SQLException;
}
