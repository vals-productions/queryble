package com.vals.queryble.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.vals.queryble.Queryble;
import com.vals.queryble.QuerybleExecutor;

public class QuerybleJdbc<R> extends QuerybleExecutor<Integer> {
	
	private PreparedStatement ps;
	private Connection connection;
	
	private QuerybleJdbc() {
		super();
	}
	
	public static <R> QuerybleJdbc<R> create(Connection connection) {
		QuerybleJdbc<R> querybleJdbc = new QuerybleJdbc<>();
		querybleJdbc.connection = connection;
		querybleJdbc.queryble = new Queryble<Integer>();
		querybleJdbc.queryble.usePositionalParams(true);
		return querybleJdbc;
	}
	
	public void build() throws Exception {
		queryble.build();		
	}
	
	@SuppressWarnings("unchecked")
	public R result() throws Exception {
		queryble.build();
		String query = queryble.getQueryString();
		ps = connection.prepareStatement(query);
		bindParameters();
		Date dateBefore = new Date();
		ps.execute();
		ResultSet resultSet = ps.getResultSet();
		Date dateAfter = new Date();
		print("Execution time: " + (dateAfter.getTime() - dateBefore.getTime()) + ", query: " + query);
		return (R)resultSet;
	}
	
	private void bindParameters() throws SQLException {
		for (int i = 1; i <= queryble.getParameters().size(); i++) {
			Object parameter = queryble.getParameters().get(i);
			ps.setObject(i, parameter);
		}
	}

}
