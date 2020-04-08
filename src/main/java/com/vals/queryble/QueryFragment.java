package com.vals.queryble;

public class QueryFragment<P> {
	
	public static String TYPE_DEFAULT = "d";
	public static String TYPE_FROM = "f";
	public static String TYPE_WHERE = "w";
	public static String TYPE_REQUIRED = "r";
	
	public static String TYPE_COUNT_ONLY = "c";

	public String queryFragment;
	public Object param;
	public P paramId;
	public boolean isParamFragment = false;
	public String charFlags = "d";
	public String stage = "select";
	
	public boolean is(String type) {
		return charFlags.contains(type);
	}

	public static <P> QueryFragment<P> build(String queryStr, Object parameter, String flags) {
		QueryFragment<P> qf = new QueryFragment<>();
		qf.queryFragment = queryStr;
		qf.param = parameter;
		qf.charFlags = flags;
		qf.isParamFragment = true;
		return qf;
	}
	
	public static <P> QueryFragment<P> build(String queryStr, Object parameter, P paramId) {
		QueryFragment<P> qf = new QueryFragment<>();
		qf.queryFragment = queryStr;
		qf.param = parameter;
		qf.paramId = paramId;
		qf.isParamFragment = true;
		return qf;
	}
	
	public static <P> QueryFragment<P> build(String queryStr, Object parameter) {
		QueryFragment<P> qf = new QueryFragment<>();
		qf.queryFragment = queryStr;
		qf.param = parameter;
		qf.isParamFragment = true;
		return qf;
	}

	public static <P> QueryFragment<P> build(String queryStr) {
		QueryFragment<P> qf = new QueryFragment<P>();
		qf.queryFragment = queryStr;
		qf.isParamFragment = false;
		return qf;
	}
	
}
