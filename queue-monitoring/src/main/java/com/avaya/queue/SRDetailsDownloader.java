package com.avaya.queue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.avaya.queue.entity.SR;
import com.avaya.queue.security.PKIXAuthenticator;
import com.avaya.queue.util.Constants;

public class SRDetailsDownloader {
	private final static Logger logger = Logger.getLogger(SRDetailsDownloader.class);
	
	public List<SR> processSRDetails(List<SR> queueList) {
		File input = null;
		try {
			this.downloadSrDetails(queueList);
			
			for (SR sr : queueList) {
				input = new File(Constants.SR_FILE+sr.getNumber()+".html");	
				Document doc = Jsoup.parse(input, "UTF-8");
				Element productEntitlement = doc.getElementById(Constants.ID_PRODUCT_ENTITLEMENT);
				Element srDescription = doc.getElementById(Constants.ID_SR_DESCRIPTION);
				Element account = doc.getElementById(Constants.ID_ACCOUNT);
				sr.setProductEntitled(productEntitlement.text());
				sr.setAccount(account.text());
				sr.setDescription(srDescription.text());
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		
		return queueList;
	}

	private void downloadSrDetails(List<SR> srs) {
		URL url;

		try {
			for (SR sr : srs) {
				url = new URL(Constants.SR_DETAILS_URL+sr.getNumber());
				PKIXAuthenticator.authenticate();
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				
				// open the stream and put it into BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String inputLine;
				
				// save to this filename
				File file = new File(Constants.PROJECT_PATH+"res/"+sr.getNumber()+".html");
				
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
				
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

}