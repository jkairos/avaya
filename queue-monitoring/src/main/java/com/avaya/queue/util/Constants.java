package com.avaya.queue.util;

import java.io.File;

public class Constants {
	private static File JAR_PATH = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	public static final String APP_PATH = JAR_PATH.getParentFile().getAbsolutePath()+File.separator;

	public static final String URL_XLSB = "https://confluence.forge.avaya.com/download/attachments/74680487/Contracts.xlsb";
	public static final String QUEUE_MONITORING_URL="https://report.avaya.com/siebelreports/employeedrill.aspx?UserHandle=ADV_APP_SUPPORT";
	public static final String SR_DETAILS_URL="https://report.avaya.com/siebelreports/casedetails.aspx?case_id=";

	public static final String CONTRACTS_XLSB=APP_PATH+"contracts"+File.separator+"contracts.xlsb";
	public static final String QUEUE_FILE=APP_PATH+"res"+File.separator+"queue.html";
//	public static final String QUEUE_FILE=PROJECT_PATH+"res"+File.separator+"queue.html";
//	public static final String SR_FILE=PROJECT_PATH+"res"+File.separator;
	public static final String RES=APP_PATH+"res"+File.separator;
	public static final String ID_OPEN_SRS="lOpenSrs";
	public static final String ID_OPEN_ACTS="lOpenActs";
	public static final String ID_CASE_ENTRIES="tCaseEntries";
	public static final String ID_PRODUCT_ENTITLEMENT="lProduct";
	public static final String ID_SR_DESCRIPTION="lCASEDESCRIPTION";
	public static final String ID_ACCOUNT="lSiteName";
	public static final String ID_SECURITY_RESTRICTED="SecurityRestricted";
	public static final String ID_SEVERITY="lSeverity";
	public static final String ID_PARENT_NAME="lParentName";
	public static final String CONTRACTS_CSV=APP_PATH+"contracts"+File.separator+"only-contractsv3.csv";
//	public static final String CONTRACTS_XLSX=PROJECT_PATH+"contracts"+File.separator+"contracts.xlsx";
	public static final String CONTRACTS_XLSX=APP_PATH+"contracts"+File.separator+"contracts.xlsx";
//	public static final String INSERT_DATA_FILE=PROJECT_PATH+"res"+File.separator+"insert-data.sql";
	public static final String INSERT_DATA_FILE=APP_PATH+"db"+File.separator+"insert-data.sql";
	public static final String SBI_INTERVAL="interval.to.resend.email.sbi";
	public static final String BI_INTERVAL="interval.to.resend.email.bi";
	public static final String NSI_INTERVAL="interval.to.resend.email.nsi";
	public static final String SBI="SBI";
	public static final String BI="BI";
	public static final String NSI="NSI";
	
			
}
