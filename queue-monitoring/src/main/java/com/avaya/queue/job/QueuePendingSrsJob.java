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
	private final static String resDir="res_pending";
	
	public QueuePendingSrsJob(){
		siebelReportDownloader = new SiebelReportDownloader(Settings.getString(Constants.QUEUE_MONITORING_URL),Constants.QUEUE_PENDING_FILE_NAME,resDir);
	}
	
	public void cleanup() {
		File file = new File(userHome+File.separator+"qpc"+File.separator+resDir+File.separator+Constants.QUEUE_PENDING_FILE_NAME);
		logger.info("Deleting file: " +(file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}	
	public void processQueue() {
		logger.info("Begin Process Queue");
		siebelReportDownloader.readUrl();

		List<SR> queueList = siebelReportDownloader.getQueueList(Constants.QUEUE_PENDING_FILE_NAME);
		if (logger.isDebugEnabled()) {
			logger.debug("Current Queue Size: " + queueList.size());
		}
		if (queueList != null && !queueList.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("QUEUE IS NOT EMPTY");
			}
			srDetaildDownloader.getSRDetails(queueList,userHome + File.separator + "qpc" + File.separator + resDir);
		}
		this.processEmailToSend(queueList);
		logger.info("End Process Queue");
	}

	public void processEmailToSend(List<SR> queueList) {
		Template template = null;

		if (queueList != null && !queueList.isEmpty()) {
			template = velocityEngine.getTemplate("pending-in-queue.vm");
		} else {
			template = velocityEngine.getTemplate("queue-empty.vm");
		}

		/**
		 * Prepare context data
		 */
		if (queueList != null && !queueList.isEmpty()) {
			context.put("pendingSRs", queueList);
		}

		/**
		 * Merge data and template
		 */
		StringWriter swOut = new StringWriter();
		template.merge(context, swOut);

		String message = swOut.toString();
		String subject = null;
		if (queueList != null) {
			subject = Settings.getString(Constants.APP_SHORT_NAME) + " - PENDING =  " +queueList.size();
		} else {
			subject = Settings.getString(Constants.APP_SHORT_NAME) + " - Queue is Empty";
		}

		AsyncEmailer.getInstance(subject, message).start();
	}

}
