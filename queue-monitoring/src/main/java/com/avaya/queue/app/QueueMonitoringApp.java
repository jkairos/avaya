package com.avaya.queue.app;

import java.io.File;

import org.joda.time.Instant;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.avaya.queue.db.ScriptDBBuilder;
import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.security.PKIXAuthenticator;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringProperties;
import com.avaya.queue.util.Util;

public class QueueMonitoringApp {
	public static ApplicationContext context;

	public static void main(String[] args) {
		try {
			PKIXAuthenticator.authenticate();
			if (Boolean.valueOf(QueueMonitoringProperties.getProperty("create.insert.file"))) {
				ScriptDBBuilder script = new ScriptDBBuilder();
				script.createInsertContractsScript();
			}
		
			if (context == null) {
				if (context == null) {
					try{
						context = new ClassPathXmlApplicationContext("applicationContext.xml");
					}catch(Exception e){
						try{
							String userHome = System.getProperty("user.home");
							context = new FileSystemXmlApplicationContext("file:///"+userHome+File.separator+Constants.APP_NAME+File.separator+"config"+File.separator+"applicationContext.xml");
						}catch(Exception e1){
							context = new FileSystemXmlApplicationContext("file:///"+Constants.APP_PATH+File.separator+"applicationContext.xml");
						}
					}
				}
			}

		} catch (Exception e) {
			String report = Util.getReport(e);
			String subject = "Error Running QMA - " + Instant.now();
			AsyncEmailer.getInstance(QueueMonitoringProperties.getProperty(Constants.EMAIL_FROM_ADDRESS), QueueMonitoringProperties.getProperty(Constants.EMAIL_TO_SEND_ERRORS), subject, report).start();
			e.printStackTrace();
		}

	}
}
