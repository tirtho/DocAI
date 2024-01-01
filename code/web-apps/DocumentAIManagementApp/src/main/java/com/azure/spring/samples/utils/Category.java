package com.azure.spring.samples.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Category {

	public static List<String> getCategoryList(String categoryJsonString) {
  		Map<String, Object> categoryMap = new HashMap<>();
  		List<Map<String, Object>> categories = new ArrayList<>();
  		categories.add(categoryMap);
  		Gson gson = new Gson();
  		categories = gson.fromJson(categoryJsonString.replace("\\\"", "\'"), categories.getClass());

  		List<String> categoryList = new ArrayList<>();
  		for (Map<String, Object> category : categories) {
  			String categoryName = (String) category.get("category");
  			categoryList.add(categoryName);
  		}
		return categoryList;
	}
	
	/**
	 * This method returns true if ALL substrings from actualList are part of expectedList. 
	 * Otherwise, it returns false.
	 * @param actualList
	 * @param expectedList
	 * @return
	 */
	public static boolean hasAll(List<String> actualList, List<String> expectedList) {
	    return actualList.stream().allMatch(s1 -> expectedList.stream().anyMatch(s2 -> s1.contains(s2)));
	}

	/**
	 * This method returns true if ANY substrings from actualList are part of expectedList. 
	 * Otherwise, it returns false.
	 * @param actualList
	 * @param expectedList
	 * @return
	 */
	public static boolean hasAny(List<String> actualList, List<String> expectedList) {
	    return actualList.stream().anyMatch(s1 -> expectedList.stream().anyMatch(s2 -> s1.contains(s2)));
	}

}
