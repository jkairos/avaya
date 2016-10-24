package com.avaya.queue.job;

import java.io.File;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.avaya.queue.Constants;
import com.avaya.queue.QueueMonitoringDownloader;
import com.avaya.queue.entity.SR;

public class QueueJob extends QuartzJobBean {
	
	@Override
	protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
//		Downloader downloader = new Downloader();
//		downloader.downloadContractFile();
		this.clean();
		this.processQueue();

	}
	
	private void clean(){
		File file = new File(Constants.QUEUE_FILE);
		if(file.exists()){
			file.delete();
		}
	}
	
	private void processQueue(){
		QueueMonitoringDownloader getUrlContent = new QueueMonitoringDownloader();
		getUrlContent.readUrl();
		List<SR> queueList = getUrlContent.processFile();
		System.out.println("Queue List size: " + queueList.size());
		for (SR sr : queueList) {
			System.out.println(sr.toString());
		}
	}

}
