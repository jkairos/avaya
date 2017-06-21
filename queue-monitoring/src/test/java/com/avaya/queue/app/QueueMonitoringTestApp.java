package com.avaya.queue.app;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.avaya.queue.db.ScriptDBBuilder;
import com.avaya.queue.util.QueueMonitoringProperties;

public class QueueMonitoringTestApp {
	public static ApplicationContext context;

	public static void main(String[] args) {
		try {

			if (Boolean.valueOf(QueueMonitoringProperties.getProperty("create.insert.file"))) {
				ScriptDBBuilder script = new ScriptDBBuilder();
				script.createInsertContractsScript();
			}

			if (context == null) {
				if (context == null) {
					context = new FileSystemXmlApplicationContext(
							"file:///Users/jferreira/git/avaya-studies/queue-monitoring/src/test/resources/applicationContext.xml");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
