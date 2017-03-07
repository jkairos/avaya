package com.avaya.queue.job;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
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

public class QueueJob extends ApplicationJob {
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	private CustomerContractDao customerContractDao;
	
	public CustomerContractDao getCustomerContractDao() {
		return customerContractDao;
	}

	@Autowired
	public void setCustomerContractDao(CustomerContractDao customerContractDao) {
		this.customerContractDao = customerContractDao;
	}

	public void processQueue() {
		logger.info("Begin Process Queue");
		getUrlContent.readUrl();

		List<SR> queueList = getUrlContent.getQueueList();
		if (logger.isDebugEnabled()) {
			logger.debug("Current Queue Size: " + queueList.size());
		}
		if (queueList != null && !queueList.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("QUEUE IS NOT EMPTY");
			}
			srDetaildDownloader.getSRDetails(queueList);
			this.sendEmail(queueList);
		}
		logger.info("End Process Queue");
	}

	private void setSRCustomerContractsDb(SR sr) {
		customerContractDao = (CustomerContractDao) QueueMonitoringApp.context.getBean("customerContractDao");
		List<CustomerContract> customerContracts = customerContractDao.findByFl(sr.getFl());
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");

		context.put("contractFound", "BY FL (" + sr.getFl() + ")");
		if (customerContracts == null || customerContracts.isEmpty()) {
			// Could not find by FL, tries FL in shipTo column
			customerContracts = customerContractDao.findByShipTo(sr.getFl());
			context.put("contractFound", "BY SHIP TO (" + sr.getFl() + ")");
		}

		if (customerContracts == null || customerContracts.isEmpty()) {
			// Could not find by FL, then tries by customer name, status and
			// solution @TODO
			// needs to be implemented
			customerContracts = customerContractDao.findByName(sr);
			context.put("contractFound",
					"BY NAME (soldToName, name, customerNameEndUser, parentName, commentsAppsSuppTeam)");
		}

		List<CustomerContract> customerContractsToProcess = new ArrayList<CustomerContract>();

		if (customerContracts != null && !customerContracts.isEmpty()) {
			DateTime endContract = null;
			String manualDate = null;

			for (CustomerContract cc : customerContracts) {
				if (cc.getEndContract() != null && !cc.getEndContract().isEmpty()) {
					endContract = fmt.parseDateTime(cc.getEndContract());
				} else {
					manualDate = cc.getManualDate();
					manualDate = manualDate.substring(manualDate.indexOf("-" + 1), manualDate.length());
					manualDate = manualDate.trim();
					endContract = fmt.parseDateTime(manualDate);
				}
				endContract = new DateTime(endContract.year().get(), endContract.monthOfYear().get(),
						endContract.dayOfMonth().get(), 23, 59, 59);
				if (endContract.isAfterNow() || endContract.isEqualNow()) {// Contract
																			// still
																			// valid
					customerContractsToProcess.add(cc);
				}

			}
		}

		sr.setCustomerContracts(customerContractsToProcess);
	}

	public void sendEmail(List<SR> queueList) {
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
					this.sendEmail(sr, 0);
					noticiationDao.insert(srNumber);
				} else {// Weekend
						// On the Weekend sends email only for SBI or OUTG
					if (sr.getSev().equalsIgnoreCase(Constants.SBI) || sr.getSev().equalsIgnoreCase(Constants.OUTG)) {
						this.sendEmail(sr, 0);
						noticiationDao.insert(srNumber);
					}
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("EMAIL HAS ALREADY BEEN SENT");
				}
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
		Template template = null;
		boolean contractFound = false;

		if (sr != null && !sr.getCustomerContracts().isEmpty()) {
			template = velocityEngine.getTemplate("customer-contracts.vm");
			contractFound = true;
		} else if (sr != null && sr.isSentBackToQueueByAccountTeam()) {
			template = velocityEngine.getTemplate("sr-sent-back-to-queue.vm");
		} else if (sr != null && sr.getCustomerContracts().isEmpty()) {
			template = velocityEngine.getTemplate("no-contracts-found.vm");
			contractFound = false;
		} else {
			template = velocityEngine.getTemplate("queue-empty.vm");
		}

		if (sr != null) {
			context.put("sr", sr);
			context.put("reminder", reminder);
			context.put("customerContracts", sr.getCustomerContracts());
			context.put("caseEntries", sr.getCaseEntries());
		}

		/**
		 * Merge data and template
		 */
		StringWriter swOut = new StringWriter();
		template.merge(context, swOut);

		String message = swOut.toString();
		String subject = null;
		if (sr != null) {
			if (sr.isSentBackToQueueByAccountTeam()) {
				subject = "QPC " + (sr.getType().equalsIgnoreCase("Collaboration") ? " - COLLABORATION - " : " - ")
						+ " Sent Back to Our Queue By " + sr.getSentBackToQueueHandle() + " / " + sr.getNumber() + " / "
						+ sr.getAccount() + " / " + sr.getProductEntitled() + " / " + sr.getDescription();
			} else {
				subject = "QPC " + (sr.getType().equalsIgnoreCase("Collaboration") ? " - COLLABORATION - " : " - ")
						+ (contractFound ? "Contract Found / " : "No Valid Contract Found / ") + sr.getNumber() + " / "
						+ sr.getAccount() + " / " + sr.getProductEntitled() + " / " + sr.getDescription();
			}

		} else {
			subject = "QPC  - Queue is Empty";
		}

		AsyncEmailer.getInstance(subject, message).start();

	}

}
