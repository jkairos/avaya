package com.avaya.queue.job;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class OverdueSrsJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(OverdueSrsJob.class);
	private final static String resDir="res_overdue";

	public OverdueSrsJob() {
		siebelReportDownloader = new SiebelReportDownloader(Settings.getString(Constants.OVERDUE_SRS_URL),
				Constants.OVERDUE_SRS_FILE_NAME,resDir);
	}
	
	public void cleanup() {
		File file = new File(userHome+File.separator+"qpc"+File.separator+resDir+File.separator+Constants.OVERDUE_SRS_FILE_NAME);
		logger.info("Deleting file: " +(file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}
	
	public void processQueue() {
		logger.info("Overdue SRS Queue");
		siebelReportDownloader.readUrl();

		List<SR> queueList = siebelReportDownloader.getOverdueSrs(Constants.OVERDUE_SRS_FILE_NAME);
		if (queueList != null && !queueList.isEmpty()) {
			logger.info("Overdue SRs list : " + queueList.size());
			srDetaildDownloader.getSRDetails(queueList,userHome + File.separator + "qpc" + File.separator + resDir);
		}
		this.processEmailToSend(queueList);
		logger.info("End Overdue SRS Queue");
	}

	public void processEmailToSend(List<SR> queueList) {
		if (queueList != null && !queueList.isEmpty()) {
			for (SR sr : queueList) {
				this.sendEmail(sr);
			}
		}
	}

	private void sendEmail(SR sr) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss a");
		DateTime lastUpdate=null;
		logger.info("sendEmail()");
		Template template = null;
		String subject = null;
		StringWriter swOut = new StringWriter();

		if (sr != null) {
			context.put("sr", sr);
			template = velocityEngine.getTemplate("overdue-srs.vm");
			lastUpdate=fmt.parseDateTime(sr.getLastUpdate());
			// Get days between the start date and end date.
	        int days = Days.daysBetween(lastUpdate, new DateTime()).getDays();
	        context.put("days", days);
			subject = Settings.getString(Constants.APP_SHORT_NAME) + (" - OVERDUE SR ")
					+ sr.getNumber() + " / " +sr.getAccount() +" / Last Update On "+sr.getLastUpdate()+ " /  ";
			/**
			 * Merge data and template
			 */
			template.merge(context, swOut);
			String message = swOut.toString();
			String ownerEmailAddress=sr.getOwner();
			ownerEmailAddress=sr.getOwner()+"@avaya.com";
			ownerEmailAddress+=","+Settings.getString(Constants.ARCHITECTS_EMAILS)+","+Settings.getString(Constants.MANAGERS_EMAILS);
			logger.info("ownerEmailAdrress: " + ownerEmailAddress);
			AsyncEmailer.getInstance(Settings.getString("email.from.address"),ownerEmailAddress,subject, message).start();
		}

	}

}
