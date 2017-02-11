package com.avaya.queue.entity;

import org.joda.time.DateTime;

public class Notification {
	private String srNumber;
	private DateTime notificationDate;

	public String getSrNumber() {
		return srNumber;
	}
	public void setSrNumber(String srNumber) {
		this.srNumber = srNumber;
	}
	public DateTime getNotificationDate() {
		return notificationDate;
	}
	public void setNotificationDate(DateTime notificationDate) {
		this.notificationDate = notificationDate;
	}

}
