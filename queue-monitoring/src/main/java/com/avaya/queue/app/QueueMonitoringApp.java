package com.avaya.queue.app;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.avaya.queue.XlsToCSV;
import com.avaya.queue.util.QueueMonitoringProperties;

public class QueueMonitoringApp {

	public static void main(String[] args) throws Exception {
		
		if(Boolean.valueOf(QueueMonitoringProperties.getProperty("convert.to.csv"))){
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
