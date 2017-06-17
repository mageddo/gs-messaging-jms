package com.mageddo.jms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;

/**
 * Created by elvis on 16/06/17.
 */

@EnableJms
@ImportResource("classpath:activemq.xml")
@SpringBootApplication
@EnableAutoConfiguration
@Configuration
public class ApplicationTest {}
