package com.avaya.queue.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.avaya.queue.email.AsyncEmailer;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringProperties;
import com.avaya.queue.util.Util;

/**
 * BEFORE USING THIS CLASS, PLEASE PROCEED AS FOLLOWS: 
 * 1# DOWNLOAD THE LATEST CONTRACT FILE (https://confluence.forge.avaya.com/download/attachments/74680487/Contracts.
 * xlsb) 
 * 2# OPEN THE XLSB FILE IN AN WINDOWS MACHINE 
 * 3# EXPORT AS CSV (WILL NOT EXPORT AS A CSV FILE, HOWEVER WILL KEEP THE SPACES) 
 * 4# CREATE A NEW SPREADSHEET 
 * 5# COPY THE CONTENT FROM THE CSV FILE CREATED AND PAST IT IN THE NEW SPREADSHEET 
 * 6# SAVE THE FILE AS XLSX
 * 
 * @author jferreira
 *
 */
public class ScriptDBBuilder {
	private final static Logger logger = Logger.getLogger(ScriptDBBuilder.class);
	private static final Map<String, String> MAP_MONTHS;
	private String userHome = System.getProperty("user.home");
	
	static{
		MAP_MONTHS = new HashMap<String, String>();
		MAP_MONTHS.put("JANUARY", "01");
		MAP_MONTHS.put("JAN", "01");
		MAP_MONTHS.put("FEB", "02");
		MAP_MONTHS.put("FEBRUARY", "02");
		MAP_MONTHS.put("MAR", "03");
		MAP_MONTHS.put("MARCH", "03");
		MAP_MONTHS.put("APR", "04");
		MAP_MONTHS.put("APRIL", "04");
		MAP_MONTHS.put("MAY", "05");
		MAP_MONTHS.put("JUN", "06");
		MAP_MONTHS.put("JUNE", "06");
		MAP_MONTHS.put("JUL", "07");
		MAP_MONTHS.put("JULY", "07");
		MAP_MONTHS.put("AUG", "08");
		MAP_MONTHS.put("AUGUST", "08");
		MAP_MONTHS.put("SEPT", "09");
		MAP_MONTHS.put("SEPTEMBER", "09");
		MAP_MONTHS.put("OCT", "10");
		MAP_MONTHS.put("OCTOBER", "10");
		MAP_MONTHS.put("NOV", "11");
		MAP_MONTHS.put("NOVEMBER", "11");
		MAP_MONTHS.put("DEC", "12");
		MAP_MONTHS.put("DECEMBER", "12");
	}
	public void createInsertContractsScript() {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Reading Contract File To CREATE insert-data.sql file");
			}

			Map<String, Object> map = this.getContractsSheetDMLScript();
			StringBuilder data = (StringBuilder) map.get("data");
			// StringBuilder data = new StringBuilder();
			this.getManualContractsSheetDMLScript(data, (Integer) map.get("counter"));
			// StringBuilder data = new StringBuilder();
			// this.getManualContractsSheetDMLScript(data, 1);
			File outputFile = null;
			FileOutputStream fos = null;

			try {
				outputFile = new File(userHome+File.separator+Constants.APP_NAME+File.separator+Constants.INSERT_DATA_FILE);
				fos = new FileOutputStream(outputFile);
			} catch (FileNotFoundException e) {
				outputFile = new File(Constants.PROJECT_PATH + "db" + File.separator + "insert-data.sql");
				fos = new FileOutputStream(outputFile);
			}

			fos.write(data.toString().getBytes());
			fos.close();
			if (logger.isDebugEnabled()) {
				logger.debug("insert-db file created");
			}

		} catch (Exception e) {
			String report = Util.getReport(e);
			String subject = "Error Running QMA - " + Instant.now();
			AsyncEmailer.getInstance(QueueMonitoringProperties.getProperty(Constants.EMAIL_FROM_ADDRESS), QueueMonitoringProperties.getProperty(Constants.EMAIL_TO_SEND_ERRORS), subject, report).start();
			e.printStackTrace();
			logger.error(e);
		}
	}

	private Map<String, Object> getContractsSheetDMLScript() throws FileNotFoundException, IOException {
		StringBuilder data = new StringBuilder();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		FileInputStream file = null;

		try {
			file = new FileInputStream(new File(userHome+File.separator+Constants.APP_NAME+File.separator+Constants.CONTRACTS_XLSX));
		} catch (FileNotFoundException e) {
			file = new FileInputStream(
					new File(Constants.PROJECT_PATH + "contracts" + File.separator + "contracts.xlsx"));
			String report = Util.getReport(e);
			String subject = "Error Running QMA - " + Instant.now();
			AsyncEmailer.getInstance(QueueMonitoringProperties.getProperty(Constants.EMAIL_FROM_ADDRESS), QueueMonitoringProperties.getProperty(Constants.EMAIL_TO_SEND_ERRORS), subject, report).start();
		}

		// Create Workbook instance holding reference to .xlsx file
		XSSFWorkbook workbook = new XSSFWorkbook(file);

		// Get first/desired sheet from the workbook
		XSSFSheet sheet = workbook.getSheetAt(0);

		// Iterate through each rows one by one
		Iterator<Row> rowIterator = sheet.iterator();
		int counter = 1;

		String region = null;
		String country = null;
		String status = null;
		String eProject = null;
		String sapContract = null;
		String fl = null;
		String soldTo = null;
		String shipTo = null;
		String customerName = null;
		String comments = null;
		String sapOrder = null;
		String startDate = null;
		String endDate = null;
		String solutionApplication = null;
		String apsSuppMc = null;
		String apsSuppDesc = null;
		String linkToSap = null;

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			// For each row, iterate through all the columns
			Iterator<Cell> cellIterator = row.cellIterator();
			region = null;
			country = null;
			status = null;
			eProject = null;
			sapContract = null;
			fl = null;
			soldTo = null;
			shipTo = null;
			customerName = null;
			comments = null;
			sapOrder = null;
			startDate = null;
			endDate = null;
			solutionApplication = null;
			apsSuppMc = null;
			apsSuppDesc = null;
			linkToSap = null;

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				if (row.getRowNum() > 1) {// Skip header

					// Set the data to String
					if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						if (!HSSFDateUtil.isCellDateFormatted(cell)) {
							cell.setCellType(CellType.STRING);
						}
					}

					if (cell.getColumnIndex() == 0) {// Region
						region = cell.getStringCellValue();
						region = region.trim();
						region = region.replaceAll(",", "");
						region = region.replaceAll("'", " ");
					} else if (cell.getColumnIndex() == 1) {// country
						country = cell.getStringCellValue();
						country = country.trim();
						country = country.replaceAll(",", "");
						country = country.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 2) {// status
						status = cell.getStringCellValue();
						status = status.trim();
						status = status.replaceAll(",", "");
						status = status.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 3) {// eproject
						eProject = cell.getStringCellValue();
						eProject = eProject.trim();
						eProject = eProject.replaceAll(",", "");
						eProject = eProject.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 4) {// sapContract
						sapContract = cell.getStringCellValue();
						sapContract = sapContract.trim();
						sapContract = sapContract.replaceAll(",", "");
						sapContract = sapContract.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 5) {// fl
						fl = cell.getStringCellValue();
						fl = fl.trim();
						fl = fl.replaceAll(",", "");
						fl = fl.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 6) {// soldTo
						soldTo = cell.getStringCellValue();
						soldTo = soldTo.trim();
						soldTo = soldTo.replaceAll(",", "");
						soldTo = soldTo.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 7) {// shipTo
						shipTo = cell.getStringCellValue();
						shipTo = shipTo.trim();
						shipTo = shipTo.replaceAll(",", "");
						shipTo = shipTo.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 8) {// customerName
						customerName = cell.getStringCellValue();
						customerName = this.replaceCharsInCustomerName(customerName);
					} else if (cell.getColumnIndex() == 9) {// comments
						comments = cell.getStringCellValue();
						comments = comments.trim();
						comments = comments.replaceAll(",", "");
						comments = comments.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 10) {// sapOrder
						sapOrder = cell.getStringCellValue();
						sapOrder = sapOrder.trim();
						sapOrder = sapOrder.replaceAll(",", "");
						sapOrder = sapOrder.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 11) {// start/Last
																// renewed
						DateTime dt = new DateTime(cell.getDateCellValue());
						startDate = fmt.print(dt);
					} else if (cell.getColumnIndex() == 12) {// end date
						DateTime dt = new DateTime(cell.getDateCellValue());
						endDate = fmt.print(dt);
					} else if (cell.getColumnIndex() == 13) {// solution
																// application
						solutionApplication = cell.getStringCellValue();
						solutionApplication = solutionApplication.trim();
						solutionApplication = solutionApplication.replaceAll(",", "");
						solutionApplication = solutionApplication.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 14) {// apsSuppMc
						apsSuppMc = cell.getStringCellValue();
						apsSuppMc = apsSuppMc.trim();
						apsSuppMc = apsSuppMc.replaceAll(",", "");
						apsSuppMc = apsSuppMc.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 15) {// apsSuppMc
						apsSuppDesc = cell.getStringCellValue();
						apsSuppDesc = apsSuppDesc.trim();
						apsSuppDesc = apsSuppDesc.replaceAll(",", "");
						apsSuppDesc = apsSuppDesc.replaceAll("'", " ");

					} else if (cell.getColumnIndex() == 16) {// linkToSap
						linkToSap = cell.getStringCellValue();
						linkToSap = linkToSap.trim();
						linkToSap = linkToSap.replaceAll(",", "");
						linkToSap = linkToSap.replaceAll("'", " ");

					}
				}

			}

			if (row.getRowNum() > 1) {
				StringBuilder currentLine = new StringBuilder();
				currentLine.append("INSERT INTO CONTRACTS(id,region,country,status,eProject,sapContract,fl,soldToName,"
						+ "shipTo,customerNameEndUser,commentsAppsSuppTeam,sapOrder,startLastRenewed,"
						+ "endContract,solutionApplication,apsSuppMc,apsSuppDescription,linkToSapContractDoc)  VALUES(");
				currentLine.append(counter + ",");
				currentLine.append((region != null && !region.equals("")) ? "'" + region + "'," : "NULL,");
				currentLine.append((country != null && !country.equals("")) ? "'" + country + "'," : "NULL,");
				currentLine.append((status != null && !status.equals("")) ? "'" + status + "'," : "NULL,");
				currentLine.append((eProject != null && !eProject.equals("")) ? "'" + eProject + "'," : "NULL,");
				currentLine.append((sapContract != null && !sapContract.equals("")) ? "'" + sapContract + "'," : "NULL,");
				currentLine.append((fl != null && !fl.equals("")) ? "'" + fl + "'," : "NULL,");
				currentLine.append((soldTo != null && !soldTo.equals("")) ? "'" + soldTo + "'," : "NULL,");
				currentLine.append((shipTo != null && !shipTo.equals("")) ? "'" + shipTo + "'," : "NULL,");
				currentLine.append((customerName != null && !customerName.equals("")) ? "'" + customerName + "'," : "NULL,");
				currentLine.append((comments != null && !comments.equals("")) ? "'" + comments + "'," : "NULL,");
				currentLine.append((sapOrder != null && !sapOrder.equals("")) ? "'" + sapOrder + "'," : "NULL,");
				currentLine.append((startDate != null && !startDate.equals("")) ? "TO_DATE('" + startDate + "','DD/MM/YYYY'),"
						: "NULL,");
				currentLine.append((endDate != null && !endDate.equals("")) ? "TO_DATE('" + endDate + "','DD/MM/YYYY'),"
						: "NULL,");
				currentLine.append((solutionApplication != null && !solutionApplication.equals(""))
						? "'" + solutionApplication + "'," : "NULL,");
				currentLine.append((apsSuppMc != null && !apsSuppMc.equals("")) ? "'" + apsSuppMc + "'," : "NULL,");
				currentLine.append((apsSuppDesc != null && !apsSuppDesc.equals("")) ? "'" + apsSuppDesc + "'," : "NULL,");
				currentLine.append((linkToSap != null && !linkToSap.equals("")) ? "'" + linkToSap + "'" : "NULL");

				currentLine.append(");\n");
				data.append(currentLine);
				counter++;

			}
		}

		workbook.close();
		file.close();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", data);
		map.put("counter", counter);

		return map;
	}

	private StringBuilder getManualContractsSheetDMLScript(StringBuilder data, int counter)
			throws FileNotFoundException, IOException {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		FileInputStream file = null;

		try {
			file = new FileInputStream(new File(userHome+File.separator+Constants.APP_NAME+File.separator+Constants.MANUAL_CONTRACTS_XLSX));
		} catch (FileNotFoundException e) {
			file = new FileInputStream(
					new File(Constants.PROJECT_PATH + "contracts" + File.separator + "manual-contracts.xlsx"));
			String report = Util.getReport(e);
			String subject = "Error Running QMA - " + Instant.now();
			AsyncEmailer.getInstance(QueueMonitoringProperties.getProperty(Constants.EMAIL_FROM_ADDRESS), QueueMonitoringProperties.getProperty(Constants.EMAIL_TO_SEND_ERRORS), subject, report).start();
		}

		// Create Workbook instance holding reference to .xlsx file
		XSSFWorkbook workbook = new XSSFWorkbook(file);

		// Get first/desired sheet from the workbook
		XSSFSheet sheet = workbook.getSheetAt(0);

		// Iterate through each rows one by one
		Iterator<Row> rowIterator = sheet.iterator();
		String customerName = null;
		String fl = null;
		String fls[] = null;
		String solutionSold = null;
		String fromDate = null;
		String toDate = null;
		String status = null;
		String commentsAppsSuppTeam = null;

		while (rowIterator.hasNext()) {
			customerName = null;
			fl = null;
			fls = null;
			solutionSold = null;
			fromDate = null;
			toDate = null;
			status = null;
			commentsAppsSuppTeam = null;
			
			Row row = rowIterator.next();
			// For each row, iterate through all the columns
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				if (row.getRowNum() > 1) {// Skip header
					// Set the data to String
					if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						if (!HSSFDateUtil.isCellDateFormatted(cell)) {
							cell.setCellType(CellType.STRING);
						}
					}

					if (cell.getColumnIndex() == 0) {// Customer name
						customerName = cell.getStringCellValue();
						customerName = this.replaceCharsInCustomerName(customerName);
					} else if (cell.getColumnIndex() == 1) {// FL
						fl = cell.getStringCellValue();
						fls = fl.split("/");
					} else if (cell.getColumnIndex() == 2) {// Solution Sold
						solutionSold = cell.getStringCellValue();
						solutionSold = solutionSold.trim();
						solutionSold = solutionSold.replaceAll(",", "");
						solutionSold = solutionSold.replaceAll("'", " ");
					} else if (cell.getColumnIndex() == 5) {// From
						if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
							if (HSSFDateUtil.isCellDateFormatted(cell)) {
								DateTime dt = new DateTime(cell.getDateCellValue());
								fromDate = fmt.print(dt);
							} else {
								fromDate = cell.getStringCellValue();
								fromDate = fromDate.trim();
								fromDate = fromDate.replaceAll(",", "");
								fromDate = fromDate.replaceAll("'", " ");
							}
						} else {
							fromDate = cell.getStringCellValue();
							fromDate = fromDate.trim();
							fromDate = fromDate.replaceAll(",", "");
							fromDate = fromDate.replaceAll("'", " ");
						}
					} else if (cell.getColumnIndex() == 6) {// To
						if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
							if (HSSFDateUtil.isCellDateFormatted(cell)) {
								DateTime dt = new DateTime(cell.getDateCellValue());
								toDate = fmt.print(dt);
							} else {
								toDate = cell.getStringCellValue();
								toDate = toDate.trim();
								toDate = toDate.replaceAll(",", "");
								toDate = toDate.replaceAll("'", " ");
							}
						} else {
							toDate = cell.getStringCellValue();
							toDate = toDate.trim();
							toDate = toDate.replaceAll(",", "");
							toDate = toDate.replaceAll("'", " ");
							toDate = toDate.replaceAll("\\.", " ");
							char charArray[] = toDate.toCharArray();
							String day = "";
							String month="";
							String year = "";
							int i = 0;
							if(charArray!=null && charArray.length > 0){
								while(!Character.isDigit(charArray[i])){
									i++;
								}
								
								int index=0;
								
								while(charArray[index] != ' '){
									month+=charArray[index];
									index++;
								}
								
								month=month.trim();
								if(MAP_MONTHS.get(month.toUpperCase()) == null){
									System.out.println("");
								}
								month=MAP_MONTHS.get(month.toUpperCase());
								
								while(Character.isDigit(charArray[i])){
									day+=charArray[i];
									i++;
								}
								
								if(day.length()<2){
									day="0"+day;
								}
								
								while(!Character.isDigit(charArray[i])){
									i++;
								}
								
								while(i < charArray.length && Character.isDigit(charArray[i])){
									year+=charArray[i];
									i++;
								}
								year=year.trim();
								toDate=day+"/"+month+"/"+year;					
							}else{
								toDate=null;
							}
			
						}

					}else if(cell.getColumnIndex() == 9){//Comments APP 
						commentsAppsSuppTeam= cell.getStringCellValue();
						commentsAppsSuppTeam = commentsAppsSuppTeam.trim();
						commentsAppsSuppTeam = commentsAppsSuppTeam.replaceAll(",", "");
						commentsAppsSuppTeam = commentsAppsSuppTeam.replaceAll("'", " ");
					}

				}

			}

			if (toDate != null && !toDate.isEmpty()) {
				DateTime endContract = null;
				endContract = fmt.parseDateTime(toDate);
				endContract = new DateTime(endContract.year().get(), endContract.monthOfYear().get(),
						endContract.dayOfMonth().get(), 23, 59, 59);
				// Contract Still Valid
				if (endContract.isAfterNow() || endContract.isEqualNow()) {
					status="Open";
				}else{
					status="Closed";
				}
				
			}

			// create Insert scpript
			if (fls != null && fls.length > 0) {
				for (int i = 0; i < fls.length; i++) {
					if ((customerName != null && !customerName.equals("")) || (fls[i] != null && !fls[i].equals(""))
							|| (solutionSold != null && !solutionSold.equals(""))
							|| (fromDate != null && !fromDate.equals("")) || (toDate != null && !toDate.equals(""))) {


						data.append(
								"INSERT INTO CONTRACTS(id,customerNameEndUser,fl,solutionApplication,manualDate,status,commentsAppsSuppTeam) "
										+ "VALUES(" + counter + ",");
						data.append(
								(customerName != null && !customerName.equals("") ? "'" + customerName + "'" : "NULL")
										+ ",");
						fl = fls[i];
						fl = fl.trim();
						data.append((fl != null && !fl.equals("") ? "'" + fl + "'" : "NULL") + ",");
						data.append(
								(solutionSold != null && !solutionSold.equals("") ? "'" + solutionSold + "'" : "NULL")
										+ ",");
						data.append("'" + fromDate + " - " + toDate + "',");
						data.append((status != null && !status.equals("") ? "'" + status + "'," : "NULL,"));
						data.append((commentsAppsSuppTeam != null && !commentsAppsSuppTeam.equals("") ? "'" + commentsAppsSuppTeam + "'" : "NULL"));
						data.append(");\n");
						counter++;
					}
				}
			} else {
				if ((customerName != null && !customerName.equals(""))
						|| (solutionSold != null && !solutionSold.equals(""))
						|| (fromDate != null && !fromDate.equals("")) || (toDate != null && !toDate.equals(""))
						|| (status != null && !status.equals(""))) {
					data.append(
							"INSERT INTO CONTRACTS(id,customerNameEndUser,fl,solutionApplication,manualDate,status,commentsAppsSuppTeam)  "
									+ "VALUES(" + counter + ",");
					data.append((customerName != null && !customerName.equals("") ? "'" + customerName + "'" : "NULL")
							+ ",");
					data.append("NULL,");
					data.append((solutionSold != null && !solutionSold.equals("") ? "'" + solutionSold + "'" : "NULL")
							+ ",");
					data.append("'" + fromDate + " - " + toDate + "',");
					data.append((status != null && !status.equals("") ? "'" + status + "'," : "NULL,"));
					data.append((commentsAppsSuppTeam != null && !commentsAppsSuppTeam.equals("") ? "'" + commentsAppsSuppTeam + "'" : "NULL"));
					data.append(");\n");

				}

			}

			counter++;
		}

		workbook.close();
		file.close();

		return data;
	}
	
	private String replaceCharsInCustomerName(String customerName){
		customerName = customerName.trim();
		customerName = customerName.replaceAll(",", "");
		customerName = customerName.replaceAll("'", " ");
		customerName = customerName.replaceAll("--", " ");
		return customerName;

	}
}
