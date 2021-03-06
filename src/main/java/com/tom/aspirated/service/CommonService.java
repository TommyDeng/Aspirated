package com.tom.aspirated.service;

/**
 * @author TommyDeng <250575979@qq.com>
 * @version 创建时间：2016年9月14日 上午11:30:12
 *
 */

public interface CommonService {

	/**
	 * 记录用户登陆日志
	 * 
	 * @param name
	 * @param deviceType
	 * @throws Exception
	 */
	void logVisit(String name, String deviceType) throws Exception;

}
