package com.avaya.queue.util;

import java.io.File;

public class Constants {
	private static File JAR_PATH = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	public static final String APP_PATH = JAR_PATH.getParentFile().getAbsolutePath()+File.separator;
	public static final String PROJECT_PATH=(Constants.class.getProtectionDomain().getCodeSource().getLocation()).getFile();
	
	public static final String URL_XLSB = "https://confluence.forge.avaya.com/download/attachments/74680487/Contracts.xlsb";
	public static final String QUEUE_MONITORING_URL="https://report.avaya.com/siebelreports/employeedrill.aspx?UserHandle=ADV_APP_SUPPORT";
	public static final String SR_DETAILS_URL="https://report.avaya.com/siebelreports/casedetails.aspx?case_id=";

	public static final String CONTRACTS_XLSB=APP_PATH+"contracts"+File.separator+"contracts.xlsb";
	public static final String QUEUE_FILE=APP_PATH+"res"+File.separator+"queue.html";
	public static final String RES=APP_PATH+"res"+File.separator;
	public static final String ID_OPEN_SRS="lOpenSrs";
	public static final String ID_OPEN_ACTS="lOpenActs";
	public static final String ID_CONREF_ENTRIES="tConrefEntries";
	public static final String ID_CASE_ENTRIES="tCaseEntries";
	public static final String ID_PRODUCT_ENTITLEMENT="lProduct";
	public static final String ID_SR_DESCRIPTION="lCASEDESCRIPTION";
	public static final String ID_ACCOUNT="lSiteName";
	public static final String ID_SECURITY_RESTRICTED="SecurityRestricted";
	public static final String ID_SEVERITY="lSeverity";
	public static final String ID_PARENT_NAME="lParentName";
	public static final String ID_SR_CONTACT_NAME="lReportedByName";
	public static final String ID_SR_CONTACT_PHONE="lReportedByPhone";
	public static final String ID_SR_CONTACT_EMAIL="hlReportedByEmail";
	public static final String ID_SR_CONTACT_PREF_LANGUAGE="lReportedByPrefLang";
	public static final String CONTRACTS_XLSX=APP_PATH+"contracts"+File.separator+"contracts.xlsx";
	public static final String MANUAL_CONTRACTS_XLSX=APP_PATH+"contracts"+File.separator+"manual-contracts.xlsx";
	public static final String INSERT_DATA_FILE=APP_PATH+"db"+File.separator+"insert-data.sql";
	public static final String SBI_INTERVAL="interval.to.resend.email.sbi";
	public static final String BI_INTERVAL="interval.to.resend.email.bi";
	public static final String NSI_INTERVAL="interval.to.resend.email.nsi";
	public static final String OUTG_INTERVAL="interval.to.resend.email.outg";
	public static final String CONTRACT_TEAM_HANDLERS="contract.team.handlers";
	public static final String QUEUE_OWNER="queue.owner";
	public static final String SBI="SBI";
	public static final String BI="BI";
	public static final String NSI="NSI";
	public static final String OUTG="OUTG";
	public static final String CSS="body { "
+"	color: #ffffff; "
+"	margin: 0; "
+"	padding: 0; "
+ " } "

+" table.messageContainer { "
+"	width: 560px; "
+"	margin: 0; "
+"	padding: 0; "
+ " } "

+" table.employeeMessageContainer { "
+"	width: 100%; "
+"	padding: 0; "
+"	border: 0; "
+"	border-collapse: collapse; "
+ " } "

+" td.spacerContainer { "
+"	width: 35px; "
+"	margin: 0; "
+"	padding: 0; "
+ " } "

+" td.fullNoBorderContainer { "
+"	width: 100%; "
+"	margin: 0; "
+"	padding: 0; "
+"	border: none; "
+"	color: #000000;" 
+ " } "

+" td.fullNoBorderContainer { "
+"	text-align: center; "
+"	vertical-align: middle; "
+ " } "

+" td.fullNoBorderContainer { "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+ " } "

+" td.fullBorderContainer { "
+"	width: 100%; "
+"	margin: 0; "
+"	padding: 0; "
+"	border: ridge; "
+"	color: #000000; "
+ " } "

+" td.fullBorderContainer { "
+"	text-align: left; "
+"	vertical-align: middle; "
+ " } "

+" td.fullBorderContainer { "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+ " } "

+" td.meaFullBorderContainer { "
+"	width: 100%; "
+"	margin: 0; "
+"	padding: 0; "
+"	border: ridge; "
+"	background-color: #CC3300; "
+"	color: #eeeee0; "
+ " } "

+" td.meaFullBorderContainer { "
+"	text-align: left; "
+"	vertical-align: middle; "
+ " } "

+" td.meaFullBorderContainer { "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+ " } "

+" td.Container { "
+"	width: 100%; "
+"	margin: 0; "
+"	padding: 0; "
+"	border: ridge; "
+ " } "

+" td.bodyContainer { "
+"	width: 525px; "
+"	margin: 0; "
+"	padding: 0; "
+ " } "

+" table.bodyContainer { "
+"	width: 525px; "
+"	margin: 0; "
+"	padding: 0; "
+ " } "

+" tr.bannerContainer { "
+"	width: 525px; "
+"	margin: 0; "
+"	padding: 0; "
+ " } "

+" td.logoContainer { "
+"	width: 205px; "
+"	margin: 0; "
+"	padding: 0 0 10px 0; "
+"	height: 110px; "
+"	vertical-align: bottom; "
+ " } "

+" td.typeContainer { "
+"	width: 305px; "
+"	margin: 0; "
+"	padding: 0 0 10px 15px; "
+"	height: 110px; "
+"	vertical-align: bottom; "
+ " } "

+" td.employeeLabelContainer { "
+"	width: 33%; "
+"	margin: 0; "
+"	padding: 0; "
+"	text-align: left; "
+"	vertical-align: middle; "
+"	border-style: ridge; "
+ " } "

+" td.employeeLabelContainer { "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" td.employeeValueContainer { "
+"	width: 67%; "
+"	margin: 0; "
+"	padding: 0; "
+"	text-align: left; "
+"	vertical-align: middle; "
+"	border-style: ridge; "
+ " } "

+" td.employeeValueContainer { "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" span.legalHeader { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	text-decoration: underline; "
+"	font-weight: bold; "
+"	color: #000000; "
+ " } "

+" span.legalFooter { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 8pt; "
+"	font-weight: bold; "
+"	color: #000000; "
+ " } "

+" span.respond { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" span.TopFive { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 14pt; "
+"	font-weight: bold; "
+"	color: #c60808; "
+ " } "

+" span.passwordLegalHeader { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	font-weight: bold; "
+"	color: #000000; "
+ " } "

+" span.passwordBold { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	font-weight: bold; "
+"	color: #000000; "
+ " } "

+" span.passwordNormal { "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" td.typeContainer { "
+"	background-color: #ff660A; "
+ " } "

+" td.fullBorderContainer { "
+"	background-color: #ff660A; "
+"	color: #eeeee0; "
+ " } "

+" tr.contentContainer { "
+"	width: 525px; "
+"	font-family: Arial; "
+"	font-size: 11px; "
+"	size: 1; "
+ " } "

+" td.preAmbleContainer { "
+"	width: 525px; "
+"	padding: 35px 20px 15px 0px; "
+"	color: #666666; "
+ " } "

+" p.preAmbleContainer { "
+"	width: 525px; "
+"	font-family: Arial; "
+"	font-size: 9px; "
+"	padding: 35px 20px 15px 0px; "
+"	color: #666666; "
+ " } "

+" table.dataContainer { "
+"	width: 525px; "
+"	padding-bottom: 25px; "
+"	color: #333333; "
+"	border: 1px; "
+"	border-style: solid; "
+"	border-color: #e8e8e8; "
+"	border-collapse: collapse; "
+ " } "

+" tr.dataElementContainer { "
+"	display: table-row; "
+"	width: 525px; "
+"	vertical-align: middle; "
+"	font-family: Arial; "
+"	font-size: 11px; "
+ " } "

+" td.dataWrapperContainer { "
+"	padding-bottom: 25px; "
+ " } "

+" td.dataLabelContainer { "
+"	width: 185px; "
+"	padding: 8px 0px 8px 20px; "
+"	border: 1px; "
+"	border-style: solid; "
+"	border-color: #e8e8e8; "
+"	border-collapse: collapse; "
+"	vertical-align: middle; "
+ " } "

+" td.dataValueContainer { "
+"	width: 298px; "
+"	padding: 8px 0px 8px 20px; "
+"	border: 1px; "
+"	border-style: solid; "
+"	border-color: #e8e8e8; "
+"	border-collapse: collapse; "
+"	vertical-align: middle; "
+ " } "

+" td.dataNoteContainer { "
+"	width: 525px; "
+"	padding: 8px 0px 8px 20px; "
+"	border: 1px; "
+"	border-style: solid; "
+"	border-color: #e8e8e8; "
+"	border-collapse: collapse; "
+"	vertical-align: middle; "
+ " } "

+" td.generationLabelContainer { "
+"	width: 525px; "
+"	padding: 8px 0px 8px 20px; "
+"	border: 1px; "
+"	border-style: solid; "
+"	border-color: #e8e8e8; "
+"	border-bottom: 0px none; "
+"	border-collapse: collapse; "
+"	vertical-align: middle; "
+ " } "

+" td.generationValueContainer { "
+"	width: 525px; "
+"	padding: 0px 0px 8px 50px; "
+"	border: 1px; "
+"	border-style: solid; "
+"	border-color: #e8e8e8; "
+"	border-top: 0px none; "
+"	border-collapse: collapse; "
+"	vertical-align: middle; "
+ " } "

+" td.notificationNoteContainer { "
+"	width: 525px; "
+"	padding-bottom: 15px; "
+"	size: 2; "
+"	font-family: Arial; "
+"	font-size: 11px; "
+"	color: #666666; "
+ " } "

+" td.doNotReplyNoteContainer { "
+"	width: 525px; "
+"	padding-bottom: 10px; "
+"	size: 2; "
+"	font-family: Arial; "
+"	font-size: 11px; "
+"	color: #999999; "
+ " } "

+" td.empSubLabelContainer { "
+"	width: 100%; "
+"	margin: 0; "
+"	padding: 0 0 8px 0; "
+"	border: ridge; "
+"	color: #000000; "
+"	border-collapse: collapse; "
+ " } "

+" td.empSubLabelContainer { "
+"	text-align: left; "
+"	vertical-align: middle; "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 8pt; "
+ " } "

+" td.empSubValueContainer { "
+"	width: 100%; "
+"	margin: 0; "
+"	padding: 0 0 0 50px; "
+"	border: ridge; "
+"	border-top: 0px none; "
+"	color: #000000; "
+"	border-collapse: collapse; "
+ " } "

+" td.empSubValueContainer { "
+"	text-align: left; "
+"	vertical-align: middle; "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 8pt; "
+ " } "

+" td.form { "
+"	text-align: left; "
+"	font-weight: normal; "
+"	padding-left: 20px; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" td.formcenter { "
+"	text-align: center; "
+"	font-weight: normal; "
+"	padding-left: 20px; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" input.button { "
+"	font-weight: normal; "
+"	font-family: Verdana, Arial, Helvetica, sans-serif; "
+"	font-size: 10pt; "
+"	color: #000000; "
+ " } "

+" img.avayaLogo { "
+"	width: 119px; "
+"	height: 35px; "
+"	display: block; "
+"	border: none; "
+ " } "

+" img.spacer { "
+"	width: 35px; "
+"	height: 35px; "
+"	_vertical-align: bottom; "
+ " } "

+" font.serviceRequestTitle { "
+"	font-family: Arial; "
+"	font-size: 15px; "
+"	color: #ffffff; "
+"	font-weight: bold; "
+ " } "

+" font.notificationType { "
+"	font-family: Arial; "
+"	font-size: 28px; "
+"	font-weight: normal; "
+ " } "

+" font.specialNotes { "
+"	font-family: Arial; "
+"	font-size: 11px; "
+"	font-weight: normal; "
+"	color: #666666; "
+ " } "

+" a { "
+"	text-decoration: underline; "
+"	color: #486580; "
+"	font-weight: bold; "
+ " } "

+" a.specialLinks { "
+"	font-weight: bold; "
+"	font-size: 16px; "
+ " } "

+" element.style { "
+"	width: 10%; "
+ " } "

+" .tableBorder { "
+"	border-color: black; "
+"	border-style: solid; "
+"	border-width: 1px 2px 2px 1px; "
+"	color: black; "
+"	font-family: Verdana; "
+"	font-size: 11px; "
+"	font-weight: normal; "
+"	padding: 1px; "
+ " } "

+" .headRowV3 { "
+"	background-color: #4b80b6; "
+"	color: White; "
+"	font-family: Verdana; "
+"	font-size: 11px; "
+"	font-weight: bold; "
+"	padding: 1px; "
+"	vertical-align: top; "
+ " } "

+" .blacktext { "
+"	color: #000000; "
+"	font-family: verdana; "
+"	font-size: 11px; "
+"	font-weight: normal; "
+ " } ";
	
			
}
