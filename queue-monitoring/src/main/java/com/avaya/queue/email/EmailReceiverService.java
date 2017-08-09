package com.avaya.queue.email;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.avaya.queue.app.QueueMonitoringApp;
import com.avaya.queue.entity.SR;
import com.avaya.queue.service.QueueService;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringProperties;
import com.avaya.queue.util.SRDetailsDownloader;

public class EmailReceiverService{
	private final static Logger logger = Logger.getLogger(EmailReceiverService.class);
	private QueueService queueService;
	private VelocityEngine velocityEngine;
	private VelocityContext context;
	private final static String resDir = "res_queue";
	private String userHome = System.getProperty("user.home");
	private SRDetailsDownloader srDetailsDownloader = new SRDetailsDownloader();
	
	public void receive(MimeMessage mimeMessage) {
		try {
			logger.info("Message Received");
			Address[] froms = mimeMessage.getFrom();
			String email = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
			if(email!=null && !email.isEmpty()){
				if(email.equals(QueueMonitoringProperties.getProperty(Constants.SIEBEL_EMAIL_ADDRESS))){
					logger.info("SIEBEL Message Received");
					MimeMessageParser parser = new MimeMessageParser(mimeMessage);
					parser.parse();

					String htmlContent = parser.getHtmlContent();
					String subject = mimeMessage.getSubject();
					String srAssignment="Siebel SR Assignment:";
					String srActivity="Siebel Activity Assignment:";
					srAssignment=srAssignment.toUpperCase();
					srActivity=srActivity.toUpperCase();
					subject=subject.toUpperCase();
					
					logger.info("Sender: " + email);
					logger.info("Subject: " +mimeMessage.getSubject());
					logger.info("HTML Message Body: " +htmlContent);
					if(subject.contains(srAssignment) || subject.contains(srActivity)){
						String srNumber = null;
						if(subject.contains("SR#")){
							srNumber=subject.substring(subject.indexOf("SR# ")+4);
						}else{
							srNumber=subject.substring(subject.indexOf(": ")+2, subject.indexOf("SEVERITY"));
						}
						srNumber=srNumber.trim();
						SR sr = new SR();
						sr.setNumber(srNumber);
						List<SR> listOfSrs = new ArrayList<SR>();
						listOfSrs.add(sr);
						this.processEmailToSend(listOfSrs);
					}
				}
			}
			logger.info("END Message Received");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processEmailToSend(List<SR> queueList) {
		logger.info("Begin processEmailToSend()");
		if(queueService == null){
			queueService=(QueueService) QueueMonitoringApp.context.getBean("queueService");
		}

		
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
		if(context == null){
			context = new VelocityContext();
			context.put("css", Constants.CSS);
		}
		queueService=(QueueService) QueueMonitoringApp.context.getBean("queueService");
		srDetailsDownloader.getSRDetails(queueList,userHome + File.separator + Constants.APP_NAME + File.separator + resDir);
		queueService.processEmailToSend(queueList, velocityEngine, context,"ADV_APP_SUPPORT");
		logger.info("End processEmailToSend");
	}

	
}
