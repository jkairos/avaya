package com.avaya.queue.job;

import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;

public class QueuePendingSrsJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueuePendingSrsJob.class);

	public void processQueue() {
		logger.info("Begin Process Queue");
		getUrlContent.readUrl();

		List<SR> queueList = getUrlContent.getQueueList();
		if (logger.isDebugEnabled()) {
			logger.debug("Current Queue Size: " + queueList.size());
		}
		if (queueList != null && !queueList.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("QUEUE IS NOT EMPTY");
			}
			srDetaildDownloader.getSRDetails(queueList);
		}
		this.sendEmail(queueList);
		logger.info("End Process Queue");
	}

	public void sendEmail(List<SR> queueList) {
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
