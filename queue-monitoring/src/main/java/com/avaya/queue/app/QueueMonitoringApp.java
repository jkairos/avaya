package com.avaya.queue.app;

import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.avaya.queue.XlsToCSV;

public class QueueMonitoringApp {

	public static void main(String[] args) throws Exception {
		InputStream inputStream = QueueMonitoringApp.class.getResourceAsStream("/config.properties");

		//now can use this input stream as usually, i.e. to load as properties
		Properties props = new Properties();
		props.load(inputStream);
		
		if(Boolean.valueOf(props.getProperty("convert.to.csv"))){
			XlsToCSV xlsToCsv = new XlsToCSV();
			xlsToCsv.convertXlsFileToCsv();
		}
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"springQuartzSchedulerCronContext.xml");
		
		
		while(true){}
		
//		try {
//			Thread.sleep(6000);
//		} finally {
//			context.close();
//		}
	}
}
