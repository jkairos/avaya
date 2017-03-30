package com.avaya.queue.job;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SRDetailsDownloader;
import com.avaya.queue.util.SiebelReportDownloader;

public abstract class ApplicationJob extends QuartzJobBean{
	protected SRDetailsDownloader srDetailsDownloader = new SRDetailsDownloader();
	protected SiebelReportDownloader siebelReportDownloader;
	protected VelocityEngine velocityEngine;
	protected VelocityContext context;
	protected String userHome = System.getProperty("user.home");

	protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
		this.cleanup();
		this.setupVelocityEngine();
		this.processQueue();
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
	
	public abstract void cleanup();
	public abstract void processQueue();
	public abstract void processEmailToSend(List<SR> queueList);
}
