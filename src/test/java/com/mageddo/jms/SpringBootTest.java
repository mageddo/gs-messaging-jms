package com.mageddo.jms;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by elvis on 16/06/17.
 */
@org.springframework.boot.test.context.SpringBootTest
@ContextConfiguration(classes = {ApplicationTest.class}, loader = SpringBootContextLoader.class)
public @interface SpringBootTest {
}
