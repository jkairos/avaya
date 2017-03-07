package com.avaya.queue.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class XlsToCSV {
	private final static Logger logger = Logger.getLogger(XlsToCSV.class);
	public void convertXlsFileToCsv() {
		try {
			StringBuilder data = new StringBuilder();
			data.append("region,country,status,eProject,sapContract,fl,soldToName,"
					+ "shipTo,customerNameEndUser,commentsAppsSuppTeam,sapOrder,startLastRenewed,"
					+ "endContract,solutionApplication,apsSuppMc,apsSuppDescription,linkToSapContractDoc\n");
			
			if(logger.isDebugEnabled()){
				logger.debug("Reading Contract File To Convert to CSV Format");
			}
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
			
			FileInputStream file = new FileInputStream(new File(Constants.CONTRACTS_XLSX));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					
					if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
						if(HSSFDateUtil.isCellDateFormatted(cell)){
							DateTime dt = new DateTime(cell.getDateCellValue());
							data.append("'"+fmt.print(dt)+"',");
						}else{
							cell.setCellType(CellType.STRING);
							String s = cell.getStringCellValue();
							s = s.trim();
							s = s.replaceAll(",", "");
							s = s.replaceAll("'", " ");
							data.append("'" + s + "',");
							
						}
					}else{
						cell.setCellType(CellType.STRING);
						String s = cell.getStringCellValue();
						s = s.trim();
						s = s.replaceAll(",", "");
						s = s.replaceAll("'", " ");
						data.append("'" + s + "',");
					}
					
				}

				if(data.length()>0){
					data.setLength(data.length()-1);
				}

				data.append("\n");
			}

//			File outputFile = new File(Constants.CONTRACTS_CSV);
//			FileOutputStream fos = new FileOutputStream(outputFile);
//			fos.write(data.toString().getBytes());
//			fos.close();

			if(logger.isDebugEnabled()){
				logger.debug("Contract File Converted to CSV Format");
			}
			workbook.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public static void main(String args[]){
		new XlsToCSV().convertXlsFileToCsv();
	}
}
