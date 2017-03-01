package com.tom.aspirated.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tom.aspirated.service.CommonService;

/**
 * @author TommyDeng <250575979@qq.com>
 * @version 创建时间：2016年9月28日 下午5:30:03
 *
 */
@Component
public class BaseController {

	@Autowired
	protected CommonService commonService;

}
