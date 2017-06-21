package com.avaya.queue.entity;

public class DutyShift {
	private String month;
	private String coverageWeek;
	private Engineer westEngineer;
	private Engineer eastEngineer;
	private Engineer backupSeniorEngineer;
	
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getCoverageWeek() {
		return coverageWeek;
	}
	public void setCoverageWeek(String coverageWeek) {
		this.coverageWeek = coverageWeek;
	}
	public Engineer getWestEngineer() {
		return westEngineer;
	}
	public void setWestEngineer(Engineer westEngineer) {
		this.westEngineer = westEngineer;
	}
	public Engineer getEastEngineer() {
		return eastEngineer;
	}
	public void setEastEngineer(Engineer eastEngineer) {
		this.eastEngineer = eastEngineer;
	}
	public Engineer getBackupSeniorEngineer() {
		return backupSeniorEngineer;
	}
	public void setBackupSeniorEngineer(Engineer backupSeniorEngineer) {
		this.backupSeniorEngineer = backupSeniorEngineer;
	}
	
	
}
