package com.avaya.queue.job;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.joda.time.Instant;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringProperties;
import com.avaya.queue.util.SiebelReportDownloader;
import com.avaya.queue.util.Util;

public class QueuePendingSrsJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueuePendingSrsJob.class);
	private String resDir;
	private String queueName;
	private String fileName;
	
	public QueuePendingSrsJob(){}

	public QueuePendingSrsJob(String url, String queueName, String fileName,String resDir) {
		this.queueName=queueName;
		this.fileName=fileName;
		this.resDir=resDir;
		siebelReportDownloader = new SiebelReportDownloader(url,this.fileName, this.resDir);
	}

	public void cleanup() {
		File file = new File(userHome+File.separator+Constants.APP_NAME+File.separator+resDir+File.separator+fileName);
		logger.info("Deleting file: " +(file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}	
	public void processJob() {
		logger.info("Begin Process QueuePendingSrs - " + this.queueName);
		try{
			this.processQueue();
			logger.info("End Process QueuePendingSrs - " + this.queueName);
		}catch(RuntimeException re){
			String report = Util.getReport(re);
			String subject = "Error Running QMA - " + Instant.now();
			AsyncEmailer.getInstance(QueueMonitoringProperties.getProperty(Constants.EMAIL_FROM_ADDRESS), QueueMonitoringProperties.getProperty(Constants.EMAIL_TO_SEND_ERRORS), subject, report).start();
			logger.error("ERROR READING PENDING SRS ",re);
		}
	}
	
	private void processQueue(){
		List<SR> queueList = this.getQueueList();
		logger.info("Current Queue Size: " + queueList.size());
		if (queueList != null && !queueList.isEmpty()) {
			srDetailsDownloader.getSRDetails(queueList,userHome + File.separator + Constants.APP_NAME + File.separator + resDir);
		}
		this.processEmailToSend(queueList,queueName);
	}
	
	private List<SR> getQueueList(){
		siebelReportDownloader.readUrl();
		List<SR> queueList = siebelReportDownloader.getQueueList(fileName);
		return queueList;
	}
	
	public void processEmailToSend(List<SR> queueList,String queueName) {
		Template template = null;

		if (queueList != null && !queueList.isEmpty()) {
			template = velocityEngine.getTemplate(Constants.PENDING_IN_QUEUE_TEMPLATE);
		} else {
			template = velocityEngine.getTemplate(Constants.QUEUE_EMPTY_TEMPLATE);
		}

		/**
		 * Prepare context data
		 */
		if (queueList != null && !queueList.isEmpty()) {
			context.put("pendingSRs", queueList);
		}
		context.put("queueName", queueName);
		
		/**
		 * Merge data and template
		 */
		StringWriter swOut = new StringWriter();
		template.merge(context, swOut);

		String message = swOut.toString();
		String subject = null;
		if (queueList != null) {
			subject = Settings.getString(Constants.APP_SHORT_NAME) + " - PENDING =  " +queueList.size() + " IN QUEUE - " + queueName;
		} else {
			subject = Settings.getString(Constants.APP_SHORT_NAME) + " - Queue is Empty";
		}

		AsyncEmailer.getInstance(subject, message).start();
	}

}
