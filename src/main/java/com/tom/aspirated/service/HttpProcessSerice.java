package com.tom.aspirated.service;

import java.net.URI;

import org.apache.http.client.fluent.Form;

/**
 * @author TommyDeng <250575979@qq.com>
 * @version 创建时间：2016年9月23日 上午11:27:33
 *
 */

public interface HttpProcessSerice {
	/**
	 * 调用httpGet，并记录调用日志
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	String httpGet(URI uri) throws Exception;

	/**
	 * 调用httpPost，Byte Entity 并记录调用日志
	 * 
	 * @param uri
	 * @param form
	 * @return
	 * @throws Exception
	 */
	String httpPost(URI uri, Form form) throws Exception;

	/**
	 * 调用httpPost，String Entity 并记录调用日志
	 * 
	 * @param uri
	 * @param entityStr
	 * @return
	 * @throws Exception
	 */
	String httpPost(URI uri, String entityStr) throws Exception;


}
