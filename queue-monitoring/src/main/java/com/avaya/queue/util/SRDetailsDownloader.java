package com.avaya.queue.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.avaya.queue.email.Settings;
import com.avaya.queue.entity.Activity;
import com.avaya.queue.entity.SR;
import com.avaya.queue.security.PKIXAuthenticator;

public class SRDetailsDownloader {
	private final static Logger logger = Logger.getLogger(SRDetailsDownloader.class);
	private String userHome = System.getProperty("user.home");

	public List<SR> getSRDetails(List<SR> queueList, String path) {
		logger.info("Setting SR Details");
		File input = null;
		Document doc = null;
		try {
			this.downloadSrDetails(queueList,path);

			for (SR sr : queueList) {
				try {
					input = new File(path+ File.separator+ sr.getNumber() +".html");
					doc = Jsoup.parse(input, "UTF-8");
				} catch (FileNotFoundException e) {
					input = new File(Constants.PROJECT_PATH + "res" + File.separator + sr.getNumber() + ".html");
					doc = Jsoup.parse(input, "UTF-8");
				}
				Element productEntitlement = doc.getElementById(Constants.ID_PRODUCT_ENTITLEMENT);
				Element srDescription = doc.getElementById(Constants.ID_SR_DESCRIPTION);
				Element account = doc.getElementById(Constants.ID_ACCOUNT);
				Element securityRestricted = doc.getElementById(Constants.ID_SECURITY_RESTRICTED);
				Element severity = doc.getElementById(Constants.ID_SEVERITY);
				Element parentName = doc.getElementById(Constants.ID_PARENT_NAME);
				Element contactName = doc.getElementById(Constants.ID_SR_CONTACT_NAME);
				Element contactPhone = doc.getElementById(Constants.ID_SR_CONTACT_PHONE);
				Element contactEmail = doc.getElementById(Constants.ID_SR_CONTACT_EMAIL);
				Element prefLanguage = doc.getElementById(Constants.ID_SR_CONTACT_PREF_LANGUAGE);
				Element type = doc.getElementById(Constants.ID_TYPE);
				sr.setProductEntitled(productEntitlement.text());
				sr.setAccount(account.text());
				sr.setDescription(srDescription.text());
				sr.setSeverity(severity.text());
				sr.setParentName(parentName.text());
				String str = securityRestricted.text();
				sr.setSecurityRestricted(str.equals("Y") ? Boolean.TRUE : Boolean.FALSE);
				sr.setCaseEntries(this.getCaseEntries(sr,path));
				sr.setNameContact(contactName.text());
				sr.setPhoneContact(contactPhone.text());
				sr.setEmailContact(contactEmail.text());
				sr.setPrefLanguage(prefLanguage.text());
				sr.setType(type.text());
				// Checks whether the SR has been sent back to queue or not by
				// the contract team
				sr.setSentBackToQueueByAccountTeam(this.isSrSentBackToQueueByAccountTeam(doc, sr));
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		}

		return queueList;
	}

	private boolean isSrSentBackToQueueByAccountTeam(Document doc, SR sr) {
		boolean isSentBack = false;
		// Strip the table from the page
		Element table = doc.getElementById(Constants.ID_CONREF_ENTRIES);// Open
		// SRs
		// Table
		// Strip the rows from the table
		Elements tbRows = table.select("tr");
		String previousOwner = null;
		String currentOwner = null;
		String allowedHandlers = Settings.getString(Constants.CONTRACT_TEAM_HANDLERS);
		allowedHandlers = allowedHandlers.toLowerCase();
		String queueHandle = Settings.getString(Constants.QUEUE_OWNER);
		queueHandle = queueHandle.toLowerCase();

		int i = 2;// Skips Table's header
		while (i < tbRows.size()) {
			Element row = tbRows.get(i);
			Elements tds = row.select("td");
			previousOwner = tds.get(2).text();
			previousOwner = previousOwner.toLowerCase();
			currentOwner = tds.get(4).text();
			currentOwner = currentOwner.toLowerCase();
			break;
		}

		if (previousOwner != null && allowedHandlers.contains(previousOwner)) {
			if (currentOwner != null && currentOwner.equalsIgnoreCase(queueHandle)) {
				isSentBack = true;
				sr.setSentBackToQueueHandle(previousOwner.toUpperCase());
			}
		}

		return isSentBack;
	}

	public List<Activity> getCaseEntries(SR sr, String path) {
		logger.info("Getting Case Entries For SR " + sr.getNumber());
		File input = new File(path + File.separator  + sr.getNumber() + ".html");
		boolean isGetContent = false;
		int trCountForDescription = 0;
		List<Activity> caseEntriesList = new ArrayList<Activity>();
		Activity activity = null;
		try {

			Document doc = null;
			try {
				doc = Jsoup.parse(input, "UTF-8");
			} catch (FileNotFoundException ioe) {
				input = new File(
						Constants.PROJECT_PATH + File.separator + "res" + File.separator + sr.getNumber() + ".html");
				doc = Jsoup.parse(input, "UTF-8");
			}
			Element e = doc.getElementById(Constants.ID_CASE_ENTRIES);
			Node n = e.parentNode();

			for (Node childNode : n.childNodes()) {
				if (childNode instanceof Element) {
					Element elementNode = (Element) childNode;
					if (elementNode.tagName().equals("table")) {
						isGetContent = false;
						if (logger.isDebugEnabled()) {
							logger.debug("Case Entries Table:\n");
						}

						if (logger.isDebugEnabled()) {
							logger.debug(elementNode.toString());
						}

						outer: {
							for (Element row : elementNode.select("tr")) {
								if (logger.isDebugEnabled()) {
									logger.debug(row.toString());
								}
								Elements tds = row.select("td");

								for (int i = 0; i < tds.size(); i++) {
									if (isGetContent) {
										if (trCountForDescription % 2 == 0) {
											String desc = tds.get(0).html();
											desc = desc.substring(desc.indexOf("<pre>"), desc.indexOf("</span>"));
											activity.setDescription(desc);
											caseEntriesList.add(activity);
											trCountForDescription = 1;
										} else {
											if (!tds.toString().contains("</table>")
													&& !tds.toString().contains("Generated By")) {
												activity = new Activity();
												activity.setType(tds.get(1).text());
												activity.setCreatedBy(tds.get(2).text());
												activity.setDateCreated(tds.get(3).text());
												activity.setStatus(tds.get(4).text());
												activity.setPrivateNote(tds.get(5).text());
												activity.setAssignmentTime(tds.get(6).text());
												activity.setOwner(tds.get(7).text());
												trCountForDescription++;
											} else {
												break outer;
											}
											break;
										}
									} else {
										if (tds.get(i).text().equals("Owner")) {
											isGetContent = true;
											trCountForDescription++;
										}
									}

								}
							}

						}

					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("END PROCESS FILE");
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}

		return caseEntriesList;
	}

	private void downloadSrDetails(List<SR> srs, String path) {
		URL url;

		try {
			PKIXAuthenticator.authenticate();

			for (SR sr : srs) {
				url = new URL(Settings.getString(Constants.SR_DETAILS_URL) + sr.getNumber());
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

				// open the stream and put it into BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String inputLine;

				// save to this filename
				File file = null;

				try {
					logger.info("File to Download: " + path+ File.separator+ File.separator + sr.getNumber() + ".html");
					file = new File(path+ File.separator+sr.getNumber() + ".html");

					if (!file.exists()) {
						file.createNewFile();
					}

				} catch (IOException ioe) {
					file = new File(Constants.PROJECT_PATH + "res" + File.separator + sr.getNumber() + ".html");

					if (!file.exists()) {
						file.createNewFile();
					}

				}

				// use FileWriter to write file
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				while ((inputLine = br.readLine()) != null) {
					// System.out.println(inputLine);
					bw.write(inputLine + "\n");
				}

				bw.close();
				br.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new RuntimeException(e);
		}

	}

}