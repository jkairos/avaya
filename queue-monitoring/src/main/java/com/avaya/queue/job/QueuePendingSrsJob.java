package com.avaya.queue.job;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class QueuePendingSrsJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueuePendingSrsJob.class);
	private final static String resDir="res_pending_adv_app_support";
	private final static String resDirAdvImp="res_pending_adv_app_imp";
	
	public QueuePendingSrsJob(){}
	
	public void cleanup() {
		File file = new File(userHome+File.separator+Constants.APP_NAME+File.separator+resDir+File.separator+Constants.QUEUE_PENDING_FILE_NAME);
		logger.info("Deleting file: " +(file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
		
		file = new File(userHome+File.separator+Constants.APP_NAME+File.separator+resDirAdvImp+File.separator+Constants.IMP_QUEUE_PENDING_FILE_NAME);
		logger.info("Deleting file: " +(file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}	
	public void processJob() {
		logger.info("Begin Process QueuePendingSrs");
		String queueName="ADV_APP_SUPPORT";
		try{
			//ADV_APP_SUPPORT Queue
			this.processQueue(Settings.getString(Constants.QUEUE_MONITORING_URL), Constants.QUEUE_PENDING_FILE_NAME, resDir, queueName);
			//ADV_APP_IMP Queue
			queueName="ADV_APP_IMP";
			this.processQueue(Settings.getString(Constants.QUEUE_MONITORING_IMP_URL), Constants.IMP_QUEUE_PENDING_FILE_NAME, resDirAdvImp, queueName);
			logger.info("End Process QueuePendingSrs");
		}catch(RuntimeException re){
			logger.error("ERROR READING PENDING SRS ",re);
		}
	}
	
	private void processQueue(String url, String fileName, String resDir, String queueName){
		List<SR> queueList = this.getQueueList(url, fileName, resDir);
		logger.info("Current Queue Size: " + queueList.size());
		if (queueList != null && !queueList.isEmpty()) {
			srDetailsDownloader.getSRDetails(queueList,userHome + File.separator + Constants.APP_NAME + File.separator + resDir);
		}
		this.processEmailToSend(queueList,queueName);
	}
	
	private List<SR> getQueueList(String url, String fileName, String resDir){
		siebelReportDownloader = new SiebelReportDownloader(url,fileName,resDir);
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
