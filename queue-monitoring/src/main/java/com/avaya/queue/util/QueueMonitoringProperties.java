package com.avaya.queue.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class QueueMonitoringProperties {
	private static InputStream inputStream;
	private static Properties props = new Properties();

	public static String getProperty(String key){
		String value=null;
		
		if(inputStream == null){
			inputStream = QueueMonitoringProperties.class.getClassLoader().getSystemResourceAsStream("config.properties");
			try {
				props.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		value=props.getProperty(key);
		return value;
	}
}
