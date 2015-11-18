package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.CategoryToApp;

public interface CategoryToAppService {
	void insert(String category,String node);
	void deleteAll();
	List<CategoryToApp> findByCategory(String category);
}
