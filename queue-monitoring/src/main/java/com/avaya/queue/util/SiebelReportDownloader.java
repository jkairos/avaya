package com.avaya.queue.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.avaya.queue.entity.SR;
import com.avaya.queue.security.PKIXAuthenticator;

public class SiebelReportDownloader {
	private final static Logger logger = Logger.getLogger(SiebelReportDownloader.class);
	private String userHome = System.getProperty("user.home");
	private String urlSiebel;
	private String fileName;
	private String resPath;

	public SiebelReportDownloader(String urlSiebel, String fileName, String resPath) {
		this.urlSiebel = urlSiebel;
		this.fileName = fileName;
		this.resPath=resPath;
	}

	public List<SR> getOverdueSrs(String fileName) {
		List<SR> queueList = new ArrayList<SR>();
		File input = new File(userHome+File.separator+"qpc"+File.separator+resPath+File.separator + fileName);
		SR sr = null;
		try {
			Document doc = null;
			try {
				doc = Jsoup.parse(input, "UTF-8");
			} catch (FileNotFoundException e) {
				input = new File(Constants.PROJECT_PATH + resPath + File.separator + fileName);
				doc = Jsoup.parse(input, "UTF-8");
			}
			// Strip the table from the page
			Element table = doc.select("table[class=tableBorder]").get(1);
			// SRs
			// Table
			// Strip the rows from the table
			Elements tbRows = table.select("tr");
			String owner="";
			String lastUpdate="";
			int i = 1;// Skips Table's header
			while (i < tbRows.size()) {
				Element row = tbRows.get(i);
				Elements tds = row.select("td");
				sr = new SR();
				// SRS
				sr.setNumber(tds.get(0).text());
				sr.setSev(tds.get(1).text());
				sr.setTscs(tds.get(2).text());
				sr.setNcs(tds.get(3).text());
				sr.setAge(tds.get(4).text());
				char ownerCharA[]=tds.get(6).text().toCharArray();
				for (char c : ownerCharA) {
					if(c!= ' '){
						owner+=c;
					}
				}
				
				owner=owner.trim();
				sr.setOwner(owner);
				owner="";
				sr.setStatus(tds.get(7).text());
				sr.setLastStatusNote(tds.get(11).html());
				//Last Update
				char lastUpdateA[]=tds.get(41).text().toCharArray();
				for (char c : lastUpdateA) {
					if(c!= ' '){
						lastUpdate+=c;
					}
				}
				
				lastUpdate=lastUpdate.trim();
				sr.setLastUpdate(lastUpdate);
				lastUpdate="";
				queueList.add(sr);
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return queueList;
	}

	public List<SR> getQueueList(String fileName) {
		List<SR> queueList = new ArrayList<SR>();
		this.getQueueList(Constants.ID_OPEN_SRS, queueList, fileName);
		this.getQueueList(Constants.ID_OPEN_ACTS, queueList, fileName);
		return queueList;
	}

	private List<SR> getQueueList(String spanId, List<SR> queueList, String fileName) {
		File input = new File(userHome+File.separator+"qpc"+File.separator+resPath+File.separator + fileName);
		SR sr = null;
		try {
			Document doc = null;
			try {
				doc = Jsoup.parse(input, "UTF-8");
			} catch (FileNotFoundException e) {
				input = new File(Constants.PROJECT_PATH + resPath + File.separator + fileName);
				doc = Jsoup.parse(input, "UTF-8");
			}
			// Strip the table from the page
			Element table = doc.select("table[class=tableBorder]").get(spanId.equals(Constants.ID_OPEN_SRS) ? 1 : 2);// Open
			// SRs
			// Table
			// Strip the rows from the table
			Elements tbRows = table.select("tr");

			int i = 2;// Skips Table's header
			while (i < tbRows.size()) {
				Element row = tbRows.get(i);
				if (row.id().isEmpty() && !row.hasAttr("class")) {
					Elements tds = row.select("td");
					sr = new SR();
					if (spanId.equals(Constants.ID_OPEN_SRS)) {// Open
						// SRS
						sr.setNumber(tds.get(1).text());
						sr.setSev(tds.get(2).text());
						sr.setType(tds.get(3).text());
						sr.setStatus(tds.get(4).text());
						sr.setAge(tds.get(5).text());
						sr.setTscs(tds.get(6).text());
						sr.setNcs(tds.get(7).text());
						sr.setBacklogState(tds.get(11).text());
						sr.setMea(tds.get(12).text().equals("N") ? false : true);
						sr.setDispOrPart(tds.get(13).text().equals("N") ? false : true);
						sr.setProductSkill(tds.get(14).text());
						String fl = tds.get(15).text();
						if (!fl.isEmpty()) {
							fl = fl.replaceFirst("^0+(?!$)", "");
							fl = fl.trim();
						}
						sr.setFl(fl);
						sr.setFlName(tds.get(16).text());
						sr.setCountry(tds.get(17).text());
						try{
							sr.setOtherSrs(Integer.valueOf(tds.get(18).text()));
						}catch(NumberFormatException nfe){
							sr.setType(tds.get(4).text());
							sr.setStatus(tds.get(5).text());
							sr.setAge(tds.get(6).text());
							sr.setTscs(tds.get(7).text());
							sr.setNcs(tds.get(8).text());
							sr.setBacklogState(tds.get(12).text());
							sr.setMea(tds.get(13).text().equals("N") ? false : true);
							sr.setDispOrPart(tds.get(14).text().equals("N") ? false : true);
							sr.setProductSkill(tds.get(15).text());
							fl = tds.get(16).text();
							if (!fl.isEmpty()) {
								fl = fl.replaceFirst("^0+(?!$)", "");
								fl = fl.trim();
							}
							sr.setFl(fl);
							sr.setFlName(tds.get(17).text());
							sr.setCountry(tds.get(18).text());
							sr.setOtherSrs(Integer.valueOf(tds.get(19).text()));
						}
					} else {// Open Collaborations
						sr.setNumber(tds.get(0).text());
						sr.setSev(tds.get(1).text());
						sr.setMea(tds.get(2).text().equals("N") ? false : true);
						sr.setType(tds.get(4).text());
						sr.setStatus(tds.get(5).text());
						sr.setAge(tds.get(6).text());
						sr.setBacklogState(tds.get(8).text());
						sr.setProductSkill(tds.get(9).text());
						String fl = tds.get(10).text();
						if (!fl.isEmpty()) {
							fl = fl.replaceFirst("^0+(?!$)", "");
							fl = fl.trim();
						}
						sr.setFl(fl);
						sr.setFlName(tds.get(11).text());
						sr.setCountry(tds.get(12).text());
					}
					queueList.add(sr);
					i++;
				} else {
					i += 3;
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}

		return queueList;
	}

	public void readUrl() {
		URL url;

		try {
			url = new URL(urlSiebel);
			PKIXAuthenticator.authenticate();
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			System.setProperty("http.maxRedirects", "100");
			conn.setReadTimeout(5000);

			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine;
			File file = null;

			try {
				// save to this filename
				file = new File(userHome+File.separator+"qpc"+File.separator+resPath+File.separator + fileName);

				if (!file.exists()) {
					file.createNewFile();
				}

			} catch (IOException ioe) {
				file = new File(Constants.PROJECT_PATH + resPath + File.separator + fileName);
				if (!file.exists()) {
					file.createNewFile();
				}
			}

			// use FileWriter to write file
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			while ((inputLine = br.readLine()) != null) {
				bw.write(inputLine + "\n");
			}

			bw.close();
			br.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		} catch (SocketTimeoutException se){
			se.printStackTrace();
			logger.error(se);
			throw new RuntimeException(se);
		}catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		}

	}

}