package com.avaya.queue.util;

public class Constants {
	public static final String URL_XLSB = "https://confluence.forge.avaya.com/download/attachments/74680487/Contracts.xlsb";
	public static final String PROJECT_PATH=(Constants.class.getProtectionDomain().getCodeSource().getLocation()).getFile();
	public static final String CONTRACTS_XLSB=PROJECT_PATH+"contracts/contracts.xlsb";
	public static final String QUEUE_MONITORING_URL="https://report.avaya.com/siebelreports/employeedrill.aspx?UserHandle=ADV_APP_SUPPORT";
	public static final String QUEUE_FILE=PROJECT_PATH+"res/queue.html";
	public static final String SR_FILE=PROJECT_PATH+"res/";
	public static final String ID_OPEN_SRS="lOpenSrs";
	public static final String ID_PRODUCT_ENTITLEMENT="lProduct";
	public static final String ID_SR_DESCRIPTION="lCASEDESCRIPTION";
	public static final String ID_ACCOUNT="lSiteName";
	public static final String CONTRACTS_CSV=PROJECT_PATH+"contracts/only-contractsv2.csv";
	public static final String CONTRACTS_XLSX=PROJECT_PATH+"contracts/only-contractsv2.xlsx";
	public static final String SR_DETAILS_URL="https://report.avaya.com/siebelreports/casedetails.aspx?case_id=";
//	public static final String APACHE_SOLR_URL="http://localhost:8983/solr/avaya";
}
