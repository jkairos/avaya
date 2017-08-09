package com.avaya.queue.job;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.avaya.queue.app.QueueMonitoringApp;
import com.avaya.queue.entity.SR;
import com.avaya.queue.service.QueueService;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class QueueJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	private QueueService queueService;
	private String resDir;
	private String queueName;
	private String fileName;
	
	public QueueJob(){}
	
	public QueueJob(String url, String queueName, String fileName,String resDir) {
		this.queueName=queueName;
		this.fileName=fileName;
		this.resDir=resDir;
		siebelReportDownloader = new SiebelReportDownloader(url,this.fileName, this.resDir);
	}

	public void cleanup() {
		File file = new File(userHome + File.separator + Constants.APP_NAME + File.separator + resDir + File.separator+fileName);
		logger.info("Deleting file: " + (file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}

	public void processJob() {
		try {
			logger.info("Begin Process Queue - " +queueName);
			siebelReportDownloader.readUrl();

			List<SR> queueList = siebelReportDownloader.getQueueList(fileName);
			logger.info("Current Queue Size: " + queueList.size() +" in "+queueName);

			if (queueList != null && !queueList.isEmpty()) {
				srDetailsDownloader.getSRDetails(queueList,
						userHome + File.separator + Constants.APP_NAME + File.separator + resDir);
				this.processEmailToSend(queueList);
			}
			logger.info("End Process Queue - "+queueName);
		} catch (RuntimeException re) {
			logger.error("ERROR PROCESSING LIST OF SRS IN QUEUE", re);
		}
	}


	public void processEmailToSend(List<SR> queueList) {
		logger.info("Begin processEmailToSend()");
		queueService=(QueueService) QueueMonitoringApp.context.getBean("queueService");
		queueService.processEmailToSend(queueList, velocityEngine, context,this.queueName);
		logger.info("End processEmailToSend");
	}


}
