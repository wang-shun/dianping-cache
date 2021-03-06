/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.squirrel.common.config;

import java.util.List;
import java.util.Properties;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public interface ConfigManager {

	public String getEnv();

	public String getConfigServerAddress();

	public String getStringValue(String key);

	public String getStringValue(String key, String defaultValue);

	public Integer getIntValue(String key);

	public int getIntValue(String key, int defaultValue);

	public Long getLongValue(String key);

	public long getLongValue(String key, long defaultValue);

	public Float getFloatValue(String key);

	public float getFloatValue(String key, float defaultValue);

	public Boolean getBooleanValue(String key);

	public boolean getBooleanValue(String key, boolean defaultValue);

	public void init(Properties properties);

	public String getAppName();

	public String doGetProperty(String key) throws Exception;

	public void registerConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception;

	public void unregisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception;
	
	public List<ConfigChangeListener> getConfigChangeListeners();

}
