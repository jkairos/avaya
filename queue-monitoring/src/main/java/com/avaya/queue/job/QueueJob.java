package com.avaya.queue.job;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import com.avaya.queue.app.QueueMonitoringApp;
import com.avaya.queue.dao.CustomerContractDao;
import com.avaya.queue.dao.NotificationDao;
import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.CustomerContract;
import com.avaya.queue.entity.Notification;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.SiebelReportDownloader;

public class QueueJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	private CustomerContractDao customerContractDao;
	private final static String resDir = "res_queue";

	public QueueJob() {
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
		File file = new File(userHome + File.separator + "qpc" + File.separator + resDir + File.separator
				+ Constants.QUEUE_FILE_NAME);
		logger.info("Deleting file: " + (file.getAbsolutePath()));
		if (file.exists()) {
			file.delete();
		}
	}

	public void processQueue() {
		try {
			logger.info("Begin Process Queue");
			siebelReportDownloader.readUrl();

			List<SR> queueList = siebelReportDownloader.getQueueList(Constants.QUEUE_FILE_NAME);
			logger.info("Current Queue Size: " + queueList.size());

			if (queueList != null && !queueList.isEmpty()) {
				srDetaildDownloader.getSRDetails(queueList,
						userHome + File.separator + "qpc" + File.separator + resDir);
				this.processEmailToSend(queueList);
			}
			logger.info("End Process Queue");
		} catch (Exception e) {
			logger.error(e.getStackTrace(), e);
			System.exit(0);
		}
	}

	private void setSRCustomerContractsDb(SR sr) {
		String foundBy = null;
		boolean foundByName = false;
		customerContractDao = (CustomerContractDao) QueueMonitoringApp.context.getBean("customerContractDao");
		Set<String> customerContractIdsMap = new HashSet<String>();

		// Searches for FL
		List<CustomerContract> customerContracts = customerContractDao.findByFl(sr.getFl());

		if (customerContracts != null && !customerContracts.isEmpty()) {
			foundBy = "BY FL (" + sr.getFl() + ")";
		} else {
			// Could not find by FL, tries FL in shipTo column
			customerContracts = customerContractDao.findByShipTo(sr.getFl());
			if (customerContracts != null && !customerContracts.isEmpty()) {
				foundBy = "BY SHIP TO (" + sr.getFl() + ")";
			}
		}

		if (customerContracts != null && !customerContracts.isEmpty()) {
			for (CustomerContract customerContract : customerContracts) {
				customerContractIdsMap.add(customerContract.getId());
			}
		}

		// Always checks by name even it has been found by Ship To or FL
		List<CustomerContract> customerContractsByName = customerContractDao.findByName(sr);
		if (customerContractsByName != null && !customerContractsByName.isEmpty()) {
			if (!customerContractIdsMap.isEmpty()) {
				customerContracts = customerContracts == null ? new ArrayList<CustomerContract>() : customerContracts;
				for (CustomerContract cc : customerContractsByName) {
					if (!customerContractIdsMap.contains(cc.getId())) {
						customerContracts.add(cc);
						foundByName = true;
					}
				}

			}
		}

		if (foundByName) {
			foundBy = foundBy != null ? foundBy + " AND BY <u>NAME</u> " : " BY NAME";
		}

		if (foundBy != null) {
			context.put("contractFound", foundBy);
		}

		sr.setCustomerContracts(this.getCustomerContractToProcess(customerContracts));
	}

	private List<CustomerContract> getCustomerContractToProcess(List<CustomerContract> customerContracts) {
		List<CustomerContract> customerContractsToProcess = new ArrayList<CustomerContract>();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		String manualDate = null;
		LocalDate endContract = null;

		if (customerContracts != null && !customerContracts.isEmpty()) {

			for (CustomerContract cc : customerContracts) {
				if (cc.getEndContract() != null && !cc.getEndContract().isEmpty()) {
					endContract = fmt.parseLocalDate(cc.getEndContract());
				} else {
					manualDate = cc.getManualDate();
					logger.info("manual date: " + manualDate);
					manualDate = manualDate.substring(manualDate.indexOf("-" + 1), manualDate.length());
					manualDate = manualDate.trim();
					endContract = fmt.parseLocalDate(manualDate);
				}
				DateTime endContractDt = new DateTime(endContract.year().get(), endContract.monthOfYear().get(),
						endContract.dayOfMonth().get(), 23, 59, 59);

				// Contract still valid
				if (endContractDt.isAfterNow() || endContractDt.isEqualNow()) {
					customerContractsToProcess.add(cc);
				}

			}
		}

		return customerContractsToProcess;
	}

	public void processEmailToSend(List<SR> queueList) {
		logger.info("Begin processEmailToSend()");
		Notification notification = null;
		NotificationDao noticiationDao = (NotificationDao) QueueMonitoringApp.context.getBean("notificationDao");

		for (SR sr : queueList) {
			String srNumber = sr.getNumber();
			notification = noticiationDao.findBySr(srNumber);
			DateTime now = new DateTime();

			if (notification == null) {// 1st time the notification is being
										// sent
				this.setSRCustomerContractsDb(sr);
				if (now.getDayOfWeek() < DateTimeConstants.SATURDAY) {// Weekdays
					logger.info("Weekday :" + now.getDayOfWeek());
					this.sendEmail(sr, 0);
					noticiationDao.insert(srNumber);
				} else {// Weekend
						// On the Weekend sends email only for SBI or OUTG
					logger.info("Weekend :" + now.getDayOfWeek());
					if (sr.getSev().equalsIgnoreCase(Constants.SBI) || sr.getSev().equalsIgnoreCase(Constants.OUTG)) {
						logger.info("Weekend :" + now.getDayOfWeek() + " SBI OR OUTG");
						this.sendEmail(sr, 0);
						noticiationDao.insert(srNumber);
					}
				}
			} else {
				logger.info("Email has already been sent for SR " + sr.getNumber());
				// Notification has been sent already, however no one has
				// owned the SR yet, so we wait an interval to re-send the
				// email
				DateTime notifcationDateSent = notification.getNotificationDate();
				int minutes = Minutes.minutesBetween(notifcationDateSent, now).getMinutes();
				int configIntervalToResendEmail = 1440;// 24 hours
				try {
					if (sr.getSev().equalsIgnoreCase(Constants.SBI)) {
						configIntervalToResendEmail = Integer.valueOf(Settings.getString(Constants.SBI_INTERVAL));
					} else if (sr.getSev().equalsIgnoreCase(Constants.BI)) {
						configIntervalToResendEmail = Integer.valueOf(Settings.getString(Constants.BI_INTERVAL));
					} else if (sr.getSev().equalsIgnoreCase(Constants.NSI)) {
						configIntervalToResendEmail = Integer.valueOf(Settings.getString(Constants.NSI_INTERVAL));
					} else if (sr.getSev().equalsIgnoreCase(Constants.OUTG)) {
						configIntervalToResendEmail = Integer.valueOf(Settings.getString(Constants.OUTG_INTERVAL));
					}

				} catch (Exception e) {
					configIntervalToResendEmail = 1440;// 24 hours
				}

				if (minutes > configIntervalToResendEmail) {
					int reminder = notification.getReminder() + 1;
					this.setSRCustomerContractsDb(sr);
					if (now.getDayOfWeek() < DateTimeConstants.SATURDAY) {// Weekdays
						this.sendEmail(sr, reminder);
						noticiationDao.update(srNumber, reminder);
					} else {// Weekend
							// On the Weekend sends email only for SBI or OUTG
						if (sr.getSev().equalsIgnoreCase(Constants.SBI)
								|| sr.getSev().equalsIgnoreCase(Constants.OUTG)) {
							this.sendEmail(sr, reminder);
							noticiationDao.update(srNumber, reminder);
						}
					}
				}
			}
		}
	}

	private void sendEmail(SR sr, Integer reminder) {
		logger.info("sendEmail()");
		Template template = null;
		boolean contractFound = false;
		String subject = null;
		StringWriter swOut = new StringWriter();

		if (sr != null) {
			context.put("sr", sr);
			context.put("reminder", reminder);
			context.put("customerContracts", sr.getCustomerContracts());
			context.put("caseEntries", sr.getCaseEntries());

			if (sr.isSentBackToQueueByAccountTeam()) {
				template = velocityEngine.getTemplate("sr-sent-back-to-queue.vm");
				if (sr.getCustomerContracts() != null && !sr.getCustomerContracts().isEmpty()) {
					contractFound = true;
				}
				subject = Settings.getString(Constants.APP_SHORT_NAME)
						+ (sr.getType().equalsIgnoreCase("Collaboration") ? " - COLLABORATION - " : " - ")
						+ " Sent Back to Our Queue By " + sr.getSentBackToQueueHandle() + " / " + sr.getNumber() + " / "
						+ sr.getAccount() + " / " + sr.getProductEntitled() + " / " + sr.getDescription();
			} else {
				if (!sr.getCustomerContracts().isEmpty()) {
					template = velocityEngine.getTemplate("customer-contracts.vm");
					contractFound = true;
				} else if (sr.getCustomerContracts().isEmpty()) {
					template = velocityEngine.getTemplate("no-contracts-found.vm");
					contractFound = false;
				} else {
					template = velocityEngine.getTemplate("queue-empty.vm");
				}
				subject = Settings.getString(Constants.APP_SHORT_NAME)
						+ (sr.getType().equalsIgnoreCase("Collaboration") ? " - COLLABORATION - " : " - ")
						+ (contractFound ? "Contract Found / " : "No Valid Contract Found / ") + sr.getNumber() + " / "
						+ sr.getAccount() + " / " + sr.getProductEntitled() + " / " + sr.getDescription();
			}
		} else {
			subject = Settings.getString(Constants.APP_SHORT_NAME) + " - Queue is Empty";
		}

		/**
		 * Merge data and template
		 */
		template.merge(context, swOut);
		String message = swOut.toString();
		logger.info("subject: " + subject);
		AsyncEmailer.getInstance(subject, message).start();

	}

}
