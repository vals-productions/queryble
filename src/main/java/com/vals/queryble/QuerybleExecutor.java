package com.vals.queryble;

public abstract class QuerybleExecutor<P> {
	
	protected QuerybleDescriptor querybleDescriptor;
	
	protected Queryble<P> queryble;
	
	public Queryble<P> gueryble() {
		return queryble;
	}

	public QuerybleExecutor<P> withQuerybleDescriptor(QuerybleDescriptor querybleDescriptor) {
		this.querybleDescriptor = querybleDescriptor;
		return this;
	}

	protected boolean isPaginatedQuery() {
		return querybleDescriptor != null  && querybleDescriptor.pageNumber != null && querybleDescriptor.pageSize != null;
	}
	
	protected void print(String text) {
		System.out.println(text);
	}

}
