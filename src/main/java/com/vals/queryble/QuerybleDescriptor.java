package com.vals.queryble;

import java.util.AbstractMap;
import java.util.Map;

public class QuerybleDescriptor {
	public Long pageSize;
	public Long pageNumber;
	public Long totalCount;
	public Map<Integer,
				AbstractMap.SimpleEntry<String, String>> 
					sortMap;
}