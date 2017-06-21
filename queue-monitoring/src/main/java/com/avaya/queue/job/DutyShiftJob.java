package com.avaya.queue.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.DutyShift;
import com.avaya.queue.entity.Engineer;
import com.avaya.queue.security.PKIXAuthenticator;
import com.avaya.queue.util.Constants;

public class DutyShiftJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(DutyShiftJob.class);
	private static Map<String, Engineer> engineersMap;
	private static List<Engineer> managers;

	protected String userHome = System.getProperty("user.home");

	public DutyShiftJob() {
		//Engineers
		setEngineersManagers(1);
		//Managers
		setEngineersManagers(2);
	}

	private static void setEngineersManagers(int tableIndex) {
		try {
			switch (tableIndex) {
			case 1:
				engineersMap = new HashMap<String, Engineer>();
				break;
			case 2:
				managers = new ArrayList<Engineer>();
				break;
			default:
				engineersMap = new HashMap<String, Engineer>();
				managers = new ArrayList<Engineer>();
				break;
			}
			Document doc = null;
			try {
				URL url = new URL(Settings.getString(Constants.DUTY_SHIFT_URL));
				PKIXAuthenticator.authenticate();
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				System.setProperty("http.maxRedirects", "100");
				conn.setReadTimeout(7000);
				doc = Jsoup.parse(url, 7000);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// Strip the table from the page
			Element table = doc.select("table[class=confluenceTable]").get(tableIndex);
			// SRs
			// Table
			// Strip the rows from the table
			Elements tbRows = table.select("tr");
			int i = 1;// Skips Table's header
			Engineer engineer = null;
			String name = null;
			String handle = null;
			String voice = null;
			String usExtension = null;
			String cellPhone = null;
			String country = null;

			while (i < tbRows.size()) {
				Element row = tbRows.get(i);
				Elements tds = row.select("td");
				engineer = new Engineer();

				name = tds.get(0).text().trim();
				handle = tds.get(1).text().trim();
				voice = tds.get(2).text().trim();
				usExtension = tds.get(3).text().trim();
				cellPhone = tds.get(4).text().trim();
				country = tds.get(5).text().trim();

				engineer.setName(name);
				engineer.setHandle(handle);
				engineer.setVoice(voice);
				engineer.setUsExtension(usExtension);
				engineer.setCellPhone(cellPhone);
				engineer.setCountry(country);
				if(tableIndex==1){
					engineersMap.put(name, engineer);
				}else{
					managers.add(engineer);
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

	public void processJob() {
		try {
			logger.info("Duty Shift Job");
			DutyShift dutyShift = this.getDutyShift();
			this.sendEmail(dutyShift);
			logger.info("End Duty Shift Job");
		} catch (RuntimeException re) {
			logger.error("ERROR PROCESSING DUTY SHIFT", re);
		}
	}

	private DutyShift getDutyShift() {
		DutyShift dutyShift = new DutyShift();
		try {
			Document doc = null;
			try {
				URL url = new URL(Settings.getString(Constants.DUTY_SHIFT_URL));
				PKIXAuthenticator.authenticate();
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				System.setProperty("http.maxRedirects", "100");
				conn.setReadTimeout(7000);
				doc = Jsoup.parse(url, 7000);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// Strip the table from the page
			Element table = doc.select("table[class=confluenceTable]").get(0);
			// SRs
			// Table
			// Strip the rows from the table
			Elements tbRows = table.select("tr");
			int i = 4;// Skips Table's header
			LocalDate now = new LocalDate();

			String year = String.valueOf(now.getYear());
			String monthStr = now.monthOfYear().getAsText(); // gets the month
																// name
			String currentMonth = monthStr.substring(0, 3) + "-" + year.substring(2);
			String monthTable = null;
			String monthTableAnt = null;
			int dutyShiftStartingDay = 0;
			int nextFridayDay = 0;

			while (i < tbRows.size()) {
				Element row = tbRows.get(i);
				Elements tds = row.select("td");
				monthTable = tds.get(0).text();
				monthTable = monthTable.trim();
				if (monthTable != null && !monthTable.equals(" ")) {
					monthTableAnt = monthTable;
				} else {
					monthTable = monthTableAnt;
				}
				String coverageWeek = tds.get(1).text();
				coverageWeek = coverageWeek.trim();
				String days[] = coverageWeek.split("-");
				if (days.length > 1) {
					String startDay = days[0].substring(0, days[0].indexOf("/"));
					if (startDay.charAt(0) == '0') {
						startDay = startDay.substring(1);
					}
					dutyShiftStartingDay = Integer.valueOf(startDay);
					nextFridayDay = this.calcNextFriday(now).getDayOfMonth();
				}

				if (monthTable.equalsIgnoreCase(currentMonth) && (nextFridayDay == dutyShiftStartingDay)) {
					dutyShift.setMonth(currentMonth);
					dutyShift.setCoverageWeek(coverageWeek);
					dutyShift.setWestEngineer(engineersMap.get(tds.get(2).text()));
					dutyShift.setEastEngineer(engineersMap.get(tds.get(3).text()));
					dutyShift.setBackupSeniorEngineer(engineersMap.get(tds.get(4).text()));
					break;
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return dutyShift;
	}

	private LocalDate calcNextFriday(LocalDate d) {
		if (d.getDayOfWeek() == DateTimeConstants.FRIDAY) {
			return d;
		} else {
			return d.isBefore(d.dayOfWeek().setCopy(5)) ? d.dayOfWeek().setCopy(5)
					: d.plusWeeks(1).dayOfWeek().setCopy(5);
		}
	}

	private void sendEmail(DutyShift dutyShift) {
		logger.info("sendEmail()");
		Template template = null;
		String subject = null;
		StringWriter swOut = new StringWriter();
		String dutyProcessPath = userHome + File.separator + Constants.APP_NAME + File.separator + "templates"
				+ File.separator + "dutyProcess.jpeg";

		context.put("dutyShift", dutyShift);
		context.put("managers", managers);
		context.put("dutyProcessKey", dutyProcessPath);

		template = velocityEngine.getTemplate(Constants.DUTY_SHIFT_CONTRACT_INFORMATION);
		LocalDate now = new LocalDate();
		boolean isFriday=now.getDayOfWeek() == DateTimeConstants.FRIDAY;
		
		subject = Settings.getString(Constants.APP_SHORT_NAME)
				+ ((isFriday ? " - Duty Shift Starts Today ("+dutyShift.getCoverageWeek()+")" : " - Application Support Engineers On Duty This Weekend(" + dutyShift.getCoverageWeek() + ")"));
		/**
		 * Merge data and template
		 */
		template.merge(context, swOut);
		String message = swOut.toString();
		String emailAddress = dutyShift.getBackupSeniorEngineer().getHandle() + "@avaya.com,"
				+ dutyShift.getEastEngineer().getHandle() + "@avaya.com," + dutyShift.getWestEngineer().getHandle()
				+ "@avaya.com," + Settings.getString(Constants.MANAGERS_EMAILS) + ","
				+ Settings.getString(Constants.ARCHITECTS_EMAILS);
		logger.info("ownerEmailAdrress: " + emailAddress);
		AsyncEmailer emailer = AsyncEmailer.getInstance(Settings.getString("email.from.address"), emailAddress, subject,
				message);
		emailer.addImage(dutyProcessPath, dutyProcessPath);
		emailer.start();
	}

	@Override
	public void cleanup() {
		logger.info("No cleanup needed");
	}

}
