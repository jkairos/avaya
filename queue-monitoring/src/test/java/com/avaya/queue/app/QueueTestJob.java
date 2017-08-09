package com.avaya.queue.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hsqldb.util.DatabaseManagerSwing;
import org.springframework.beans.factory.annotation.Autowired;

import com.avaya.queue.dao.CustomerContractDao;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.SR;
import com.avaya.queue.job.ApplicationJob;
import com.avaya.queue.job.QueueJob;
import com.avaya.queue.service.QueueService;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class QueueTestJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	private CustomerContractDao customerContractDao;
	private final static String resDir = "res_queue";
	private QueueService queueService;
	
	public QueueTestJob() {
		siebelReportDownloader = new SiebelReportDownloader(Settings.getString(Constants.QUEUE_MONITORING_URL),
				Constants.QUEUE_FILE_NAME, resDir);
	}

	public CustomerContractDao getCustomerContractDao() {
		return customerContractDao;
	}

	@Autowired
	public void setCustomerContractDao(CustomerContractDao customerContractDao) {
		this.customerContractDao = customerContractDao;
	}

	public void cleanup() {
		File file = new File(userHome + File.separator + Constants.APP_NAME + File.separator + resDir + File.separator
				+ Constants.QUEUE_FILE_NAME);
		logger.info("Deleting file: " + (file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}

	public void processJob() {
		try {
			DatabaseManagerSwing.main(new String[]{"--url","jdbc:hsqldb:mem:dataSource","--user","sa","--password",""});
			logger.info("Begin Process Queue");
			List<SR> queueList = this.getListOfSrs();
			logger.info("Current Queue Size: " + queueList.size());

			if (queueList != null && !queueList.isEmpty()) {
				srDetailsDownloader.getSRDetails(queueList,
						userHome + File.separator + Constants.APP_NAME + File.separator + resDir);
				this.processEmailToSend(queueList);
			}
			logger.info("End Process Queue");
		}catch(RuntimeException re){
			logger.error("ERROR PROCESSING LIST OF SRS IN QUEUE", re);
		}
	}
	
	private List<SR> getListOfSrs(){
		SR sr = new SR();
		sr.setFl("51394673");
		sr.setFlName("HEWLETT-PACKARD MULTIMEDIA SDN BHD");
		sr.setAccount("HEWLETT-PACKARD MULTIMEDIA SDN BHD");
		sr.setParentName("HP Corona");
		sr.setNumber("1-12710095034");
		sr.setSev("BI");
		
		List<SR> queueList = new ArrayList<SR>();
		queueList.add(sr);
		
		return queueList;
	}

	public void processEmailToSend(List<SR> queueList) {
		logger.info("Begin processEmailToSend()");
		QueueMonitoringApp.context=QueueMonitoringTestApp.context;
		queueService=(QueueService) QueueMonitoringTestApp.context.getBean("queueService");
		queueService.processEmailToSend(queueList, velocityEngine, context,"ADV_APP_SUPPORT");
		logger.info("End processEmailToSend");
	}


}
