package com.vals.queryble;

import java.util.function.Supplier;

public class QuerybleUtils {
	
	public static <T> T callSafe(Supplier <T> supplier) {
		try {
			T value = supplier.get();
			return value;
		} catch (Throwable t) {
			return null;
		}
	}

}
