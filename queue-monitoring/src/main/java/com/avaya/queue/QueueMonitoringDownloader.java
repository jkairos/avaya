package com.avaya.queue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.avaya.queue.entity.SR;
import com.avaya.queue.security.PKIXAuthenticator;

public class QueueMonitoringDownloader {

	public List<SR> processFile() {
		File input = new File(Constants.QUEUE_FILE);
		boolean isGetContent = false;
		List<SR> queueList = new ArrayList<SR>();
		SR sr = null;
		try {
			Document doc = Jsoup.parse(input, "UTF-8");
			Element e = doc.getElementById(Constants.ID_OPEN_SRS);
			Node n = e.parentNode();

			for (Node childNode : n.childNodes()) {
				if (childNode instanceof Element) {
					Element elementNode = (Element) childNode;
					if (elementNode.tagName().equals("table")) {
						isGetContent = false;
						for (Element row : elementNode.select("tr")) {
							Elements tds = row.select("td");
							
							for (int i = 0; i < tds.size(); i++) {
								if (isGetContent) {
									sr = new SR();
									sr.setNumber(tds.get(1).text());
									sr.setSev(tds.get(2).text());
									sr.setType(tds.get(3).text());
									sr.setStatus(tds.get(4).text());
									sr.setAge(tds.get(5).text());
									sr.setTscs(tds.get(6).text());
									sr.setNcs(tds.get(7).text());
									sr.setBacklogState(tds.get(11).text());
									sr.setMea(tds.get(12).text().equals("N") ? false :true);
									sr.setDispOrPart(tds.get(13).text().equals("N") ? false :true);
									sr.setProductSkill(tds.get(14).text());
									String fl = tds.get(15).text();
									if(!fl.isEmpty()){
										fl=fl.replaceFirst("^0+(?!$)", "");
										fl=fl.trim();
									}
									sr.setFl(fl);
									sr.setFlName(tds.get(16).text());
									sr.setCountry(tds.get(17).text());
									sr.setOtherSrs(Integer.valueOf(tds.get(18).text()));
									
									queueList.add(sr);
									break;
								} else {
									if (tds.get(i).text().equals("Manager")) {
										isGetContent = true;
									}
								}
							}
						}

					}
				}
			}

			System.out.println(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return queueList;
	}

	public void readUrl() {
		URL url;

		try {
			url = new URL(Constants.QUEUE_MONITORING_URL);
			PKIXAuthenticator.authenticate();
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine;

			// save to this filename
			File file = new File(Constants.QUEUE_FILE);

			if (!file.exists()) {
				file.createNewFile();
			}

			// use FileWriter to write file
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			while ((inputLine = br.readLine()) != null) {
				System.out.println(inputLine);
				bw.write(inputLine + "\n");
			}

			bw.close();
			br.close();

			System.out.println("Done");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}