package com.mageddo.jms.parser;

/**
 * Created by elvis on 04/06/17.
 */
public interface JsonParser<From, To> {

	To parse(From from);
	From format(To to);
}
