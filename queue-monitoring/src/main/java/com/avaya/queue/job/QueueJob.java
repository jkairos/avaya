package com.avaya.queue.job;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		try {
			logger.info("Begin Process Queue");
			getUrlContent.readUrl();

			List<SR> queueList = getUrlContent.getQueueList();
			if (logger.isDebugEnabled()) {
				logger.debug("Current Queue Size: " + queueList.size());
			}

			// List<SR> queueList = new ArrayList<SR>();
			// SR sr = new SR();
			// sr.setFl("51565952");
			// sr.setNumber("1-12674024943");
			// sr.setNameContact("WESTPAC BANKING CORP - WSDC PRE-PROD -");
			// sr.setFlName("WESTPAC BANKING CORP - WSDC PRE-PROD -");
			// sr.setParentName("WESTPAC");
			// sr.setType("OUTG");
			// sr.setSentBackToQueueHandle("DDVIEIRA");
			// sr.setProductEntitled("TEst");
			// sr.setProductSkill("test");
			// sr.setDescription(" fasdaf sdasafdsafsadfsdaf");
			// queueList.add(sr);
			if (queueList != null && !queueList.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("QUEUE IS NOT EMPTY");
				}
				srDetaildDownloader.getSRDetails(queueList);
				this.sendEmail(queueList);
			}
			logger.info("End Process Queue");
		} catch (Exception e) {
			logger.error(e.getStackTrace(), e);
			System.exit(0);
		}
	}

	private void setSRCustomerContractsDb(SR sr) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		String foundBy = null;
		boolean foundByName = false;
		DateTime endContract = null;
		String manualDate = null;
		customerContractDao = (CustomerContractDao) QueueMonitoringApp.context.getBean("customerContractDao");
		Set<String> customerContractIdsMap = new HashSet<String>();

		// Searches for FL
		List<CustomerContract> customerContracts = customerContractDao.findByFl(sr.getFl());

		if (customerContracts != null && !customerContracts.isEmpty()) {
			foundBy = "BY FL (" + sr.getFl() + ")";
		} else {
			// Could not find by FL, tries FL in shipTo column
			customerContracts = customerContractDao.findByShipTo(sr.getFl());
			if(customerContracts!=null && !customerContracts.isEmpty()){
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
				customerContracts = customerContracts == null ? new ArrayList<CustomerContract>() :customerContracts;
				for (CustomerContract cc : customerContractsByName) {
					if (!customerContractIdsMap.contains(cc.getId())) {
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
						
						// Contract still valid
						if (endContract.isAfterNow() || endContract.isEqualNow()) {
							customerContracts.add(cc);
							foundByName = true;
						}
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

		List<CustomerContract> customerContractsToProcess = new ArrayList<CustomerContract>();

		if (customerContracts != null && !customerContracts.isEmpty()) {

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
				
				// Contract still valid
				if (endContract.isAfterNow() || endContract.isEqualNow()) {
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

		if (sr != null && sr.isSentBackToQueueByAccountTeam()) {
			template = velocityEngine.getTemplate("sr-sent-back-to-queue.vm");
			if (sr.getCustomerContracts() != null && !sr.getCustomerContracts().isEmpty()) {
				contractFound = true;
			}
		} else if (sr != null && !sr.getCustomerContracts().isEmpty()) {
			template = velocityEngine.getTemplate("customer-contracts.vm");
			contractFound = true;
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
				subject = Settings.getString(Constants.APP_SHORT_NAME) + (sr.getType().equalsIgnoreCase("Collaboration") ? " - COLLABORATION - " : " - ")
						+ " Sent Back to Our Queue By " + sr.getSentBackToQueueHandle() + " / " + sr.getNumber() + " / "
						+ sr.getAccount() + " / " + sr.getProductEntitled() + " / " + sr.getDescription();
			} else {
				subject = Settings.getString(Constants.APP_SHORT_NAME) + (sr.getType().equalsIgnoreCase("Collaboration") ? " - COLLABORATION - " : " - ")
						+ (contractFound ? "Contract Found / " : "No Valid Contract Found / ") + sr.getNumber() + " / "
						+ sr.getAccount() + " / " + sr.getProductEntitled() + " / " + sr.getDescription();
			}

		} else {
			subject = Settings.getString(Constants.APP_SHORT_NAME) + " - Queue is Empty";
		}

		AsyncEmailer.getInstance(subject, message).start();

	}

}
