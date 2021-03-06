/**
 * Project: cache-server
 * 
 * File Created at 2011-9-18
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.cache.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.cache.entity.OperationLog;
import com.dianping.cache.service.condition.OperationLogSearchCondition;
import com.dianping.core.type.PageModel;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author danson.liu
 *
 */
public interface OperationLogDao extends GenericDao {
	
	void create(OperationLog log);
	
	PageModel paginate(PageModel paginater, OperationLogSearchCondition searchCondition);
	
	List<OperationLog> paginate(@Param("pageId") int pageId, @Param("cond") OperationLogSearchCondition searchCondition);
	int paginate_COUNT(@Param("cond") OperationLogSearchCondition searchCondition);

	void delete(Date before);

	List<OperationLog> findByContent(String content);
}
