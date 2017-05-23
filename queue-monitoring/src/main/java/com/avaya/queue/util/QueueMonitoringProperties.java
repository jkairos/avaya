package com.avaya.queue.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class QueueMonitoringProperties {
	private static Properties props = new Properties();
	private static String userHome = System.getProperty("user.home");
	
	public static String getProperty(String key) {
		String value = null;

		try {
			props.load(new FileInputStream(userHome+File.separator+Constants.APP_NAME+File.separator+"config"+File.separator+"config.properties"));
		} catch (FileNotFoundException e) {
			try {
				props.load(QueueMonitoringProperties.class.getClassLoader().getSystemResourceAsStream("config.properties"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		value = props.getProperty(key);
		return value;
	}
}
