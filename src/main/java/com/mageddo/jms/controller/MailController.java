package com.mageddo.jms.controller;

import com.mageddo.jms.service.MailService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by elvis on 21/05/17.
 */
@RestController
@RequestMapping("/mail")
public class MailController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MailService mailService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void mockMail(@RequestBody(required = false) String message){

		if (StringUtils.isBlank(message)){
			mailService.sendMockMail();
		} else {
			mailService.sendMail(message);
		}
		logger.info("status=succes");
	}
}
