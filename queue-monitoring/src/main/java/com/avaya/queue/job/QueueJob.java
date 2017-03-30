package com.avaya.queue.job;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.avaya.queue.app.QueueMonitoringApp;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class QueueJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	private QueueService queueService;
	private final static String resDir = "res_queue";
	
	public QueueJob() {
		siebelReportDownloader = new SiebelReportDownloader(Settings.getString(Constants.QUEUE_MONITORING_URL),
				Constants.QUEUE_FILE_NAME, resDir);
	}

	public void cleanup() {
		File file = new File(userHome + File.separator + "qpc" + File.separator + resDir + File.separator
				+ Constants.QUEUE_FILE_NAME);
		logger.info("Deleting file: " + (file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}

	public void processQueue() {
		try {
			logger.info("Begin Process Queue");
			siebelReportDownloader.readUrl();

			List<SR> queueList = siebelReportDownloader.getQueueList(Constants.QUEUE_FILE_NAME);
			logger.info("Current Queue Size: " + queueList.size());

			if (queueList != null && !queueList.isEmpty()) {
				srDetailsDownloader.getSRDetails(queueList,
						userHome + File.separator + "qpc" + File.separator + resDir);
				this.processEmailToSend(queueList);
			}
			logger.info("End Process Queue");
		} catch (RuntimeException re) {
			logger.error("ERROR PROCESSING LIST OF SRS IN QUEUE", re);
		}
	}


	public void processEmailToSend(List<SR> queueList) {
		logger.info("Begin processEmailToSend()");
		queueService=(QueueService) QueueMonitoringApp.context.getBean("queueService");
		queueService.processEmailToSend(queueList, velocityEngine, context);
		logger.info("End processEmailToSend");
	}


}
