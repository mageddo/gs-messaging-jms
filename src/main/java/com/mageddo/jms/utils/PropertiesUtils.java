package com.mageddo.jms.utils;

import java.util.Properties;

/**
 * Created by elvis on 21/05/17.
 */
public class PropertiesUtils {


	public static InnerProperties prop(){
		return new InnerProperties();
	}

	public static class InnerProperties extends Properties {

		@Override
		public synchronized InnerProperties put(Object key, Object value) {
			super.put(key, value);
			return this;
		}
	}

}
