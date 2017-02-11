package com.avaya.queue.db;

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

import com.avaya.queue.util.Constants;

/**
 * BEFORE USING THIS CLASS, PLEASE PROCEED AS FOLLOWS:
 * 1# DOWNLOAD THE LATEST CONTRACT FILE (https://confluence.forge.avaya.com/download/attachments/74680487/Contracts.xlsb)
 * 2# OPEN THE XLSB FILE IN AN WINDOWS MACHINE
 * 3# EXPORT AS CSV (WILL NOT EXPORT AS A CSV FILE, HOWEVER WILL KEEP THE SPACES)
 * 4# CREATE A NEW SPREADSHEET
 * 5# COPY THE CONTENT FROM THE CSV FILE CREATED AND PAST IT IN THE NEW SPREADSHEET
 * 6# SAVE THE FILE AS XLSX
 * @author jferreira
 *
 */
public class ScriptUtil {
	private final static Logger logger = Logger.getLogger(ScriptUtil.class);
	private int threshold=18;//max number of columns
	public void createInsertContractsScript() {
		try {
			StringBuilder data = new StringBuilder();
			
			if(logger.isDebugEnabled()){
				logger.debug("Reading Contract File To CREATE insert-data.sql file");
			}
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
			
			FileInputStream file = new FileInputStream(new File(Constants.CONTRACTS_XLSX));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			int counter=1;
			int counterRow=0;
			
			while (rowIterator.hasNext()) {
				data.append("INSERT INTO CONTRACTS(id,region,country,status,eProject,sapContract,fl,soldToName,"
						+ "shipTo,customerNameEndUser,commentsAppsSuppTeam,sapOrder,startLastRenewed,"
						+ "endContract,solutionApplication,apsSuppMc,apsSuppDescription,linkToSapContractDoc) \n VALUES("+counter+",");

				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();
				counterRow=0;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					counterRow++;
					if(counterRow<threshold){
						if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
							if(HSSFDateUtil.isCellDateFormatted(cell)){
								DateTime dt = new DateTime(cell.getDateCellValue());
								data.append("TO_DATE('"+fmt.print(dt)+"','DD/MM/YYYY'),");
							}else{
								cell.setCellType(CellType.STRING);
								String s = cell.getStringCellValue();
								s = s.trim();
								if((counterRow == 12 || counterRow == 13) && s.isEmpty() || s.equals("")){
									data.append("NULL,");
								}else{
									s = s.replaceAll(",", "");
									s = s.replaceAll("'", " ");
									data.append("'" + s + "',");
								}
								
							}
						}else{
							cell.setCellType(CellType.STRING);
							String s = cell.getStringCellValue();
							s = s.trim();
							if((counterRow == 12 || counterRow == 13) && s.isEmpty() || s.equals("")){
								data.append("NULL,");
							}else{
								s = s.replaceAll(",", "");
								s = s.replaceAll("'", " ");
								data.append("'" + s + "',");
							}
						}
					}
				}

				if(data.length()>0){
					data.setLength(data.length()-1);
				}

				data.append(");\n");
				counter++;
			}

			File outputFile = new File(Constants.INSERT_DATA_FILE);
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(data.toString().getBytes());
			fos.close();

			if(logger.isDebugEnabled()){
				logger.debug("insert-db file created");
			}
			workbook.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
}
