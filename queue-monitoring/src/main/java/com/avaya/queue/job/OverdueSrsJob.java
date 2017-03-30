package com.avaya.queue.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.IntervalUpdate;
import com.avaya.queue.entity.SR;
import com.avaya.queue.security.PKIXAuthenticator;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class OverdueSrsJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(OverdueSrsJob.class);
	private final static String resDir = "res_overdue";

	public OverdueSrsJob() {
		siebelReportDownloader = new SiebelReportDownloader(Settings.getString(Constants.OVERDUE_SRS_URL),
				Constants.OVERDUE_SRS_FILE_NAME, resDir);
	}

	public void cleanup() {
		File file = new File(userHome + File.separator + "qpc" + File.separator + resDir + File.separator
				+ Constants.OVERDUE_SRS_FILE_NAME);
		logger.info("Deleting file: " + (file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}

	public void processQueue() {
		try{
			logger.info("Overdue SRS Queue");
			siebelReportDownloader.readUrl();
	
			List<SR> queueList = siebelReportDownloader.getOverdueSrs(Constants.OVERDUE_SRS_FILE_NAME);
			if (queueList != null && !queueList.isEmpty()) {
				logger.info("Overdue SRs list : " + queueList.size());
				srDetailsDownloader.getSRDetails(queueList, userHome + File.separator + "qpc" + File.separator + resDir);
			}
			this.processEmailToSend(queueList);
			logger.info("End Overdue SRS Queue");
		}catch(RuntimeException re){
			logger.error("ERROR PROCESSING LIST OF OVERDUE SRS", re);
		}
	}

	public void processEmailToSend(List<SR> queueList) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss a");
		DateTime lastUpdate = null;
		if (queueList != null && !queueList.isEmpty()) {
			Map<String, List<SR>> mapSrsByOwner = new HashMap<String, List<SR>>();
			for (SR sr : queueList) {
				lastUpdate = fmt.parseDateTime(sr.getLastUpdate());
				// Get days between the start date and end date.
				int interval = Days.daysBetween(lastUpdate, new DateTime()).getDays();
				sr.setLastUpdateInterval(interval);
				if (interval != 0) {
					sr.setIntervalUpdate(IntervalUpdate.DAY);
				} else {
					sr.setLastUpdateInterval(Hours.hoursBetween(lastUpdate, new DateTime()).getHours());
					sr.setIntervalUpdate(IntervalUpdate.HOUR);
				}

				if (mapSrsByOwner.containsKey(sr.getOwner())) {
					mapSrsByOwner.get(sr.getOwner()).add(sr);
				} else {
					List<SR> list = new ArrayList<SR>();
					list.add(sr);
					sr.setOwnerName(this.getOwnerName(sr.getOwner()));
					mapSrsByOwner.put(sr.getOwner(), list);
				}
			}
			this.sendOverdueEmailReminder(mapSrsByOwner);
		}
	}

	private String getOwnerName(String owner) {
		String ownerName = null;
		try {
			URL url = new URL(Settings.getString(Constants.USER_DETAILS) + owner);
			PKIXAuthenticator.authenticate();
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			System.setProperty("http.maxRedirects", "100");
			conn.setReadTimeout(5000);

			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuilder htmlContent = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				// System.out.println(inputLine);
				htmlContent.append(inputLine + "\n");
			}

			Document doc = Jsoup.parse(htmlContent.toString(), "UTF-8");
			Element username = doc.getElementById(Constants.ID_USERNAME);
			ownerName = username.text();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		}

		return ownerName;
	}

	private void sendOverdueEmailReminder(Map<String, List<SR>> mapSrsByOwner) {
		for (Map.Entry<String, List<SR>> entry : mapSrsByOwner.entrySet()) {
			this.sendEmail(entry.getValue());
		}
	}

	private void sendEmail(List<SR> srs) {
		logger.info("sendEmail()");
		Template template = null;
		String subject = null;
		StringWriter swOut = new StringWriter();
		SR sr = null;
		if (srs.size() > 1) {
			context.put("srList", srs);
			context.put("ownerName", srs.get(0).getOwnerName());
			template = velocityEngine.getTemplate("list-overdue-srs-by-owner.vm");
			subject = Settings.getString(Constants.APP_SHORT_NAME) + (" - Following The List Of OVERDUE SRs For ")
					+ srs.get(0).getOwnerName() + " - Please Enter the Latest Statuses ";
		} else {
			sr = srs.get(0);
			context.put("sr", sr);
			template = velocityEngine.getTemplate("overdue-srs.vm");
			subject = Settings.getString(Constants.APP_SHORT_NAME) + (" - OVERDUE SR ") + sr.getNumber() + " / "
					+ sr.getAccount() + " / Last Update On " + sr.getLastUpdate() + " /  ";
		}
		/**
		 * Merge data and template
		 */
		template.merge(context, swOut);
		String message = swOut.toString();
		String ownerEmailAddress = sr == null ? srs.get(0).getOwner() : sr.getOwner();
		ownerEmailAddress = ownerEmailAddress + "@avaya.com";
		ownerEmailAddress += "," + Settings.getString(Constants.ARCHITECTS_EMAILS) + ","
				+ Settings.getString(Constants.MANAGERS_EMAILS);
		logger.info("ownerEmailAdrress: " + ownerEmailAddress);
		AsyncEmailer.getInstance(Settings.getString("email.from.address"), ownerEmailAddress, subject, message).start();
		// AsyncEmailer.getInstance(Settings.getString("email.from.address"),
		// "jferreira@avaya.com", subject, message)
		// .start();
	}

}
