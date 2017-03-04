package com.avaya.queue.job;

import java.io.File;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringDownloader;
import com.avaya.queue.util.SRDetailsDownloader;

public abstract class ApplicationJob extends QuartzJobBean{
	protected SRDetailsDownloader srDetaildDownloader = new SRDetailsDownloader();
	protected QueueMonitoringDownloader getUrlContent = new QueueMonitoringDownloader();
	protected VelocityEngine velocityEngine;
	protected VelocityContext context;
	
	protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
		this.cleanup();
		this.setupVelocityEngine();
		this.processQueue();
	}

	private void cleanup() {
		File file = new File(Constants.APP_PATH + File.separator + "res");
		if (file.exists()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				file2.delete();
			}
		}
	}
	
	private void setupVelocityEngine(){
		/**
		 * Initialize engine
		 */
		if(velocityEngine==null){
			velocityEngine = new VelocityEngine();
			velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
			velocityEngine.setProperty("file.resource.loader.path",Constants.APP_PATH + "templates" + "," + Constants.PROJECT_PATH + "templates");
			velocityEngine.init();
		}
		
		/**
		 * Prepare context data
		 */
		context = new VelocityContext();
		context.put("css", Constants.CSS);

	}
	
	public abstract void processQueue();
	public abstract void sendEmail(List<SR> queueList);
}
