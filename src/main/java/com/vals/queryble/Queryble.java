package com.vals.queryble;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 
 * @author Vlad S
 *
 */
public class Queryble<P> {
	
	public static final String SELECT_COUNT = "SELECT COUNT(*) ";
	public static final String paramPrevfix = "__param_";
	public static final String PARAM_PLACEHOLDER = "{}";
	
	protected String customSelectClause;
	protected LinkedList<QueryFragment<P>> queryFragments = new LinkedList<>();
	protected String queryString;
	private Map<P, Object> parameterMap = new HashMap<>();
	private boolean isUsePositionalParams = false;
	protected boolean isBuilt = false;
	private int paramIndex = 1;	
	private List<String> sortList = new LinkedList<String>(); 
	private boolean isFirstWhere = true;

	protected Function<QueryFragment<P>, Boolean> addDecisionFunction = new Function<>() {
		@Override
		public Boolean apply(QueryFragment<P> queryFragment) {
			if (queryFragment.isParamFragment == false) {
				return true;
			}
			if (queryFragment.isParamFragment && queryFragment.param != null) {
				return true;
			}
			return false;
		}
	};
	
	protected Function<QueryFragment<P>, String> checkFragmentFunction = new Function<>() {

		@Override
		public String apply(QueryFragment<P> qf) {
			return qf.queryFragment;
		}
		
	};
	
	public Queryble<P> withCheckFragmentFunction(Function<QueryFragment<P>, String> checkFragmentFunction) {
		this.checkFragmentFunction = checkFragmentFunction;
		return this;
	}

	protected String currentStage = "select";
	
	public Queryble<P> withFlags(String flags) {
		QueryFragment<P> qf = queryFragments.getLast();
		qf.charFlags = flags;
		return this;
	}
	
	/**
	 * Not tested yet
	 * @param queryPart
	 * @return
	 */
	public Queryble<P> addGroupBy(String queryPart) {
		currentStage = "group by";
		return add(queryPart);
	}
	
	/**
	 * Not tested yet
	 * @param queryPart
	 * @return
	 */
	public Queryble<P> addHaving(String queryPart) {
		currentStage = "having";
		return add(queryPart);
	}
	
	public Queryble<P> add(String queryPart) {
		QueryFragment<P> qf = QueryFragment.build(queryPart);
		qf.stage = currentStage;
		queryFragments.add(qf);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Queryble<P> add(String queryPart, Object param) {
		QueryFragment<P> qf = QueryFragment.build(queryPart, param);
		if (!isUsePositionalParams) {
			qf.paramId = (P) getParamName();
		} else {
			qf.paramId = (P) getParamIndex();
		}
		qf.stage = currentStage;
		queryFragments.add(qf);
		return this;
	}
	
	public Queryble<P> addWhere() {
		currentStage = "where";
		QueryFragment<P> qf = QueryFragment.build("WHERE");
		qf.charFlags += 'w';
		qf.stage = currentStage;
		queryFragments.add(qf);
		return this;
	}
	
	public Queryble<P> addFrom() {
		currentStage = "from";
		QueryFragment<P> qf = QueryFragment.build("FROM");
		qf.charFlags += QueryFragment.TYPE_FROM;
		qf.stage = currentStage;
		queryFragments.add(qf);
		return this;
	}

	public Queryble<P> usePositionalParams(boolean isUsePositionalParams) {
		this.isUsePositionalParams = isUsePositionalParams;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	protected void ensureParam(StringBuilder sb, QueryFragment<P> qf) {
		if (qf.param != null && qf.isParamFragment) {
			if (!isUsePositionalParams) {
				String name = getParamName();
				applyParamPlaceholder(name, sb, qf);
				parameterMap.put((P)name, qf.param);
			} else {
				Integer index = getParamIndex();
				applyParamPlaceholder(null, sb, qf);
				parameterMap.put((P)index, qf.param);
			}
		}
	}
	
	protected void applyParamPlaceholder(String name, StringBuilder sb, QueryFragment<P> qf) {
		int position = sb.indexOf(PARAM_PLACEHOLDER);
		if (position != -1) {
			if (!isUsePositionalParams) {
				sb.replace(position, position + 2, ":" + name);
			} else {
				sb.replace(position, position + 2, " ? ");
			}
		} else {
			if (!isUsePositionalParams) {
				sb.append(":" + name + " ");
			} else {
				sb.append(" ? ");
			}
		}
	}
	
	private String getParamName() {
		return paramPrevfix + (paramIndex++);
	}
	
	private Integer getParamIndex() {
		return (paramIndex++);
	}
	
	public void build() throws Exception {
		if (!isBuilt) {
			paramIndex = 1;
			StringBuilder sb = new StringBuilder();
			buildSelect(sb);
			buildFrom(sb);
			buildWhere(sb);
			buildGroupBy(sb);
			buildHaving(sb);
			buildOrderBy(sb);
			queryString = sb.toString();
//			print("Query: " + queryString);
			isBuilt = true;
		}
	}

	protected void buildWhere(StringBuilder sb ) throws Exception {
		QueryFragment<P> whereQueryFragment = null;
		for (QueryFragment<P> qf: queryFragments) {
			if ("where".equals(qf.stage)) {
				if ("where".equalsIgnoreCase(qf.queryFragment)) {
					whereQueryFragment = qf;
					continue;
				}
				if (addDecisionFunction.apply(qf)) {
					if (whereQueryFragment != null) {
						sb.append(whereQueryFragment.queryFragment + " ");
						whereQueryFragment = null;
					}
					String originalFragment = qf.queryFragment;
					qf.queryFragment = checkFirstWhere(qf);
					qf.queryFragment = checkFragment(qf);
					sb.append(qf.queryFragment + " ");
					qf.queryFragment = originalFragment;
					ensureParam(sb, qf);
				} else {
					if (qf.is(QueryFragment.TYPE_REQUIRED)) {
						throw new Exception("Required " + qf.queryFragment + " was not included in the query");
					}
				}
			}
		}
	}

	protected void buildFrom(StringBuilder sb ) {
		buildStage("from", sb);
//		for (QueryFragment<P> qf: queryFragments) {
//			if ("from".equals(qf.stage)) {
//				if (addDecisionFunction.apply(qf)) {
//					String fragment = qf.queryFragment;
//					fragment = checkFragment(qf);
//					sb.append(fragment + " ");
//					ensureParam(sb, qf);
//				}
//			}
//		}
	}

	protected void buildSelect(StringBuilder sb) {
		buildStage("select", sb);
	}
	
	protected void buildStage(String stage, StringBuilder sb) {
		for (QueryFragment<P> qf: queryFragments) {
			if (stage.equals(qf.stage)) {
				if (addDecisionFunction.apply(qf)) {
					String fragment = qf.queryFragment;
					fragment = checkFragment(qf);
					sb.append(fragment + " ");
					ensureParam(sb, qf);
				}
			}
		}
	}
	
	protected void buildGroupBy(StringBuilder stringBuffer) {
		buildStage("group by", stringBuffer);
	}
	
	protected void buildHaving(StringBuilder stringBuffer) {
		buildStage("having", stringBuffer);
	}
	
	protected void buildOrderBy(StringBuilder stringBuffer) {
		if (sortList != null && !sortList.isEmpty()) {
			String orderBy = String.join(",", sortList);
			stringBuffer.append(" ORDER BY ").append(orderBy);
		}
	}
	
	protected String checkFragment(QueryFragment<P> qf) {
		return checkFragmentFunction.apply(qf);
	}
	
	protected String checkFirstWhere(QueryFragment<P> qf) {
		String result = qf.queryFragment;
		if ("WHERE".equalsIgnoreCase(qf.stage) &&
				!"WHERE".equals(qf.queryFragment) &&
				isFirstWhere) {
			isFirstWhere = false;
			if (qf.queryFragment.toLowerCase().stripLeading().startsWith("and")) {
				result = qf.queryFragment.trim().substring(3);
			} else if (qf.queryFragment.toLowerCase().stripLeading().startsWith("or")) {
				result = qf.queryFragment.trim().substring(2);
			} else {
				result = qf.queryFragment;
			}
			return result;
		}
		return result;
	}
	
	public String getQueryString() {
		return queryString;
	}
	
	public Map<P, Object> getParameters() {
		return parameterMap;
	}
	
	public Queryble<P> clone() {
		Queryble<P> q = new Queryble<>();
		q.queryFragments.addAll(queryFragments);
		q.usePositionalParams(isUsePositionalParams);
		return q;
	}
	
	public void cloneInto(Queryble<P> anotherQueryble) {
		anotherQueryble.queryFragments.addAll(queryFragments);
		anotherQueryble.usePositionalParams(isUsePositionalParams);
	}
	
	public Queryble<P> addSort(String columnAscDesc) {
		sortList.add(columnAscDesc);
		return this;
	}
	
	/**
	 * 
	 * @param sortMap - 1 based map
	 * @return
	 */
	public Queryble<P> setSort(Map<Integer, AbstractMap.SimpleEntry<String, String>> sortMap) {
		sortList.clear();
		if (sortMap != null) {
			for (int i = 1; i <= sortMap.size(); i++) {
				AbstractMap.SimpleEntry<String, String> entry = sortMap.get(i);
				sortList.add(entry.getKey() + " " + entry.getValue());
			}
		}
		return this;
	}

	public  Queryble<P> withAddDecisionFunction(Function<QueryFragment<P>, Boolean> addDecisionFunction) {
		this.addDecisionFunction = addDecisionFunction;
		return this;
	}

	public  Queryble<P> withSelectClause(String customSelectClause) {
		this.customSelectClause = customSelectClause;
		return this;
	}
	
	public Queryble<P> add(QueryFragment<P> qf) {
		queryFragments.add(qf);
		return this;
	}
	
	protected void print(String text) {
		System.out.println(text);
	}

	
}
