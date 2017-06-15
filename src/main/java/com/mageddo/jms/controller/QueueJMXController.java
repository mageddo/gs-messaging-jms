package com.mageddo.jms.controller;

import com.mageddo.jms.entity.DestinationParameterEntity;
import com.mageddo.jms.service.QueueService;
import com.mageddo.jms.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by elvis on 22/05/17.
 */
@RestController
@RequestMapping("/api/jmx/queue")
public class QueueJMXController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private QueueService queueService;

	@RequestMapping(method = RequestMethod.PUT)
	public void changeConsumers(@RequestBody DestinationParameterEntity entity){
		queueService.changeConsumersAndSave(entity);
	}

	@ExceptionHandler
	@ResponseBody
	public ResponseEntity<PropertiesUtils.InnerProperties> exception(Throwable e){
		logger.error("status=begin, msg={}", e.getMessage(), e);
		return ResponseEntity
			.badRequest()
			.body(PropertiesUtils.prop().put("msg", e.getMessage()));
	}
}
