package com.avaya.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

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
	public void convertXlsFileToCsv() {
		try {
			StringBuilder data = new StringBuilder();
			System.out.println("Reading Contracts File");
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
							System.out.print(fmt.print(dt));
						}else{
							cell.setCellType(CellType.STRING);
							String s = cell.getStringCellValue();
							s = s.trim();
							s = s.replaceAll(",", "");
							s = s.replaceAll("'", " ");
							data.append("'" + s + "',");
							System.out.print(s + "\t");
							
						}
					}else{
						cell.setCellType(CellType.STRING);
						String s = cell.getStringCellValue();
						s = s.trim();
						s = s.replaceAll(",", "");
						s = s.replaceAll("'", " ");
						data.append("'" + s + "',");
						System.out.print(s + "\t");
						
					}
					
				}

				if(data.length()>0){
					data.setLength(data.length()-1);
				}

				System.out.println("");
				data.append("\n");
			}

			File outputFile = new File(Constants.CONTRACTS_CSV);
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(data.toString().getBytes());
			fos.close();

			System.out.println("End Read Contracts File");
			workbook.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
