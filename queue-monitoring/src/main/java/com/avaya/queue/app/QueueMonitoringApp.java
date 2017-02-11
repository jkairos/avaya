package com.avaya.queue.app;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.avaya.queue.db.ScriptUtil;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringProperties;

public class QueueMonitoringApp {
	public static ApplicationContext context;

	public static void main(String[] args) {
		try {
			// if(Boolean.valueOf(QueueMonitoringProperties.getProperty("convert.to.csv"))){
			// XlsToCSV xlsToCsv = new XlsToCSV();
			// xlsToCsv.convertXlsFileToCsv();
			// }
			
			if (Boolean.valueOf(QueueMonitoringProperties.getProperty("create.insert.file"))) {
				ScriptUtil script = new ScriptUtil();
				script.createInsertContractsScript();
			}
			if (context == null) {
				if (context == null) {
					context = new FileSystemXmlApplicationContext("file:///"+Constants.APP_PATH+File.separator+"applicationContext.xml");
				}
			}

			while (true) {
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
