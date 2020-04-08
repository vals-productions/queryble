package com.vals.queryble.hibernate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;

import com.vals.queryble.QueryFragment;
import com.vals.queryble.Queryble;
import com.vals.queryble.QuerybleDescriptor;
import com.vals.queryble.QuerybleExecutor;

public class QuerybleHibernate<R> extends QuerybleExecutor<String> {
	
	private Query <?> query;
	private Session session;
	
	private ResultTransformer resultTransformer;
	
	// “HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!”
	private String entityIdFor3StepPagination;
	
	private List<Object> idList;
	protected Queryble<String> querybleForCount;
	protected Queryble<String> querybleForIds;
	protected Queryble<String> querybleByIds;
	
	static protected Function<QueryFragment<String>, String> noCollectionFetchCheckFragmentFunction = new Function<>() {
		@Override
		public String apply(QueryFragment<String> qf) {
			if ("from".equalsIgnoreCase(qf.stage)) {
				if (qf.is(QueryFragment.TYPE_COUNT_ONLY)) {
					return "";
				}
				String result = qf.queryFragment.replaceAll("(?i)fetch", "");
				return result;
			}
			return qf.queryFragment;
		}
	};
	
	public static <R> QuerybleHibernate<R> create(Session session) {
		QuerybleHibernate<R> querybleHibernate = new QuerybleHibernate<>();
		querybleHibernate.session = session;
		querybleHibernate.queryble = new Queryble<>();
//		querybleHibernate.queryble.usePositionalParams(false);
		return querybleHibernate;
	}
	
	public QuerybleHibernate<R> withDescriptor(QuerybleDescriptor querybleDescriptor) {
		this.querybleDescriptor = querybleDescriptor;
		return this;
	}
	
	public void build() throws Exception {
		queryble.build();		
	}
	
	public void buildCount() throws Exception {
		queryble.build();
	}
	
	public void queryCounts() throws Exception {
		if (isPaginatedQuery()) {
			querybleForCount = new Queryble<String>() {
				@Override
				protected void buildSelect(StringBuilder sb) {
					sb.append(Queryble.SELECT_COUNT);
				}
			};
			queryble.cloneInto(querybleForCount);
			querybleForCount.withCheckFragmentFunction(noCollectionFetchCheckFragmentFunction);
			querybleForCount.build();
			String countQueryString = querybleForCount.getQueryString();
			@SuppressWarnings("unchecked")
			Query<Long> countQuery = session.createQuery(countQueryString);
			Date dateBefore = new Date();
			bindParameters(countQuery, querybleForCount);
			List<Long> counts = countQuery.list();
			Date dateAfter = new Date();
			print("Execution time: " + (dateAfter.getTime() - dateBefore.getTime()) + " ms, query: " + countQueryString);		
			if (counts != null && counts.size() == 1) {
				Long count = counts.get(0);
				querybleDescriptor.totalCount = count;
			}
		}
	}

	/**
	 * Ids are retrieved only, pagination specified, but no fetching.
	 * 
	 * @throws Exception
	 */
	public void queryIds() throws Exception {
		if (isPaginatedQuery()) {
			querybleForIds = new Queryble<String>() {
				@Override
				protected void buildSelect(StringBuilder sb) {
					sb.append("SELECT " + entityIdFor3StepPagination  + " ");
				}
			};
			queryble.cloneInto(querybleForIds);
			querybleForIds.withCheckFragmentFunction(noCollectionFetchCheckFragmentFunction);
			querybleForIds.setSort(querybleDescriptor.sortMap);
			querybleForIds.build();
			String queryString = querybleForIds.getQueryString();
			@SuppressWarnings("unchecked")
			Query<Object> idQuery = session.createQuery(queryString);
			if(is3StepPagination()) {
				applyPagination(idQuery);
			}
			Date dateBefore = new Date();
			bindParameters(idQuery, querybleForIds);
			idList = idQuery.list();
			Date dateAfter = new Date();
			print("Execution time: " + (dateAfter.getTime() - dateBefore.getTime()) + " ms, query: " + queryString);		
		}
	}
	
	private boolean is3StepPagination() {
		return entityIdFor3StepPagination != null;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public R result() throws Exception {
		/*
		 * Optional record count
		 */
		if (isPaginatedQuery()) {
			queryCounts();
		}
		/*
		 * Optional 3 step pagination id retrieval step
		 */
		if (is3StepPagination() && entityIdFor3StepPagination != null) {
			queryIds();
			/*
			 * Apply id list filter to the next query.
			 */
			QueryFragment<String> qf = new QueryFragment<String>();
			qf.queryFragment = " AND " + entityIdFor3StepPagination + " IN ";
			qf.param = idList;
			qf.stage = "where";
			qf.paramId = "entityIdFor3StepPagination";
			qf.isParamFragment = true;
			queryble.add(qf);
		}
		/*
		 * Data retrieval
		 */
		queryble.setSort(querybleDescriptor.sortMap);
		queryble.build();
		String queryString = queryble.getQueryString();
		query = session.createQuery(queryString);
		if (querybleDescriptor != null && querybleDescriptor.pageNumber != null && querybleDescriptor.pageSize != null) {
			if (!is3StepPagination()) {
				/*
				 * We apply pagination to 2 step pagination.
				 * 3 step pagination will be implemented by id list instead.
				 */
				applyPagination(query);
			}
		}
		Date dateBefore = new Date();
		bindParameters(query, queryble);
		if (resultTransformer != null) {
			query.setResultTransformer(resultTransformer);
		}
		R r = (R) query.list();
		Date dateAfter = new Date();
		print("Execution time: " + (dateAfter.getTime() - dateBefore.getTime()) + " ms, query: " + queryString);		
		return r;
	}
	
	private void applyPagination(Query<?> query) {
		query.setFirstResult((int)(querybleDescriptor.pageSize * querybleDescriptor.pageNumber));
		query.setMaxResults(querybleDescriptor.pageSize.intValue());		
	}
	
	private void bindParameters(Query<?> qry, Queryble<String> qryble) throws SQLException {
		for (String name: qryble.getParameters().keySet()) {
			Object parameter = qryble.getParameters().get(name);
			if (parameter instanceof Collection) {
				qry.setParameterList(name, (Collection<?>)parameter);
			} else {
				qry.setParameter(name, parameter);
			}
		}
	}

	public QuerybleHibernate<R> withResultTransformer(ResultTransformer resultTransformer) {
		this.resultTransformer = resultTransformer;
		return this;
	}

	public QuerybleHibernate<R> withEntityIdFor3StepPagination(String entityIdFor3StepPagination) {
		this.entityIdFor3StepPagination = entityIdFor3StepPagination;
		return this;
	}
	
}