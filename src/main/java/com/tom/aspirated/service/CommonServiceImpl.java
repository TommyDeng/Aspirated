package com.tom.aspirated.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author TommyDeng <250575979@qq.com>
 * @version 创建时间：2016年9月14日 上午11:31:23
 *
 */
@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	DataAccessService dataAccessService;

	@Override
	@Transactional
	public void logVisit(String visitorName, String deviceType) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("name".toUpperCase(), visitorName);
		paramMap.put("remark".toUpperCase(), deviceType);
		dataAccessService.insertSingle("LOG_VISIT", paramMap);
	}

}
