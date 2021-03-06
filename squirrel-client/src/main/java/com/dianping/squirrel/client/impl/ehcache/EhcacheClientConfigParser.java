/**
 * Project: avatar
 * 
 * File Created at 2010-10-18
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.squirrel.client.impl.ehcache;

import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.config.StoreClientConfigParser;
import com.dianping.squirrel.common.domain.CacheConfigurationDTO;

/**
 * EhcacheClient Configuration Parser
 * @author danson.liu
 *
 */
public class EhcacheClientConfigParser implements StoreClientConfigParser {

	@Override
	public StoreClientConfig parse(CacheConfigurationDTO detail) {
		//Can extend some ehcache configuration here
	    EhcacheClientConfig config = new EhcacheClientConfig();
	    config.setClientClazz(detail.getClientClazz());
	    return config;
	}

}
