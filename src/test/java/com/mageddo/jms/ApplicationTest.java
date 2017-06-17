package com.mageddo.jms;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by elvis on 16/06/17.
 */
@Import(Application.class)
@ImportResource("classpath:activemq.xml")
@Configuration
public class ApplicationTest {

}
