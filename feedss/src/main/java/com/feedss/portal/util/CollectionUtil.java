package com.feedss.portal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtil {
	/**
	 * 分段数组
	 * @param list
	 * @param size
	 * @return
	 */
	public static <T> List<List<T>> split(Collection<T> list, int size){
		List<List<T>> result = new ArrayList<>();
		
		ArrayList<T> subList = new ArrayList<>(size);
		for (T t : list) {
			if(subList.size() >= size){
				result.add(subList);
				subList = new ArrayList<>(size);
			}
			subList.add(t);
		}
		result.add(subList);
		return result;
	}
}
