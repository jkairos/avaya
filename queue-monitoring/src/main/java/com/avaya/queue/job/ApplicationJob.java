package com.avaya.queue.job;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SRDetailsDownloader;
import com.avaya.queue.util.SiebelReportDownloader;

public abstract class ApplicationJob extends QuartzJobBean{
	protected SRDetailsDownloader srDetailsDownloader = new SRDetailsDownloader();
	protected SiebelReportDownloader siebelReportDownloader;
	protected VelocityEngine velocityEngine;
	protected VelocityContext context;
	protected String userHome = System.getProperty("user.home");
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	
	protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
		this.cleanup();
		this.setupVelocityEngine();
		this.processJob();
	}

	private void setupVelocityEngine(){
		/**
		 * Initialize engine
		 */
		if(velocityEngine==null){
			velocityEngine = new VelocityEngine();
			velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
			velocityEngine.setProperty("file.resource.loader.path",userHome+File.separator+Constants.APP_NAME+File.separator+"templates"+
					","+Constants.APP_PATH + "templates" + "," + Constants.PROJECT_PATH + "templates");
			logger.info("file.resource.loader.path: " + velocityEngine.getProperty("file.resource.loader.path"));
			velocityEngine.init();
		}
		
		/**
		 * Prepare context data
		 */
		context = new VelocityContext();
		context.put("css", Constants.CSS);

	}
	
	public abstract void cleanup();
	public abstract void processJob();
}
