package com.avaya.queue.email;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AsyncEmailer extends Thread {

	protected final static Log log = LogFactory.getLog("queueMonitoring");

	public String LAST_SENT_TEXT = "nothing yet";
	public String LAST_SENT_HTML = "nothing yet";

	private String subject = null;
	private String to = null;
	private String cc = null;
	private String bcc = null;
	private String fromName = null;
	private String fromAddress = null;
	private String replyTo = null;
	private String html;
	private String text;
	private List<Attachment> attachments = new LinkedList<Attachment>();

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean getHasSubject() {
		return subject != null && subject.trim().length() > 0;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public boolean getHasTo() {
		return to != null && to.trim().length() > 0;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public boolean getHasFromAddress() {
		return fromAddress != null && fromAddress.trim().length() > 0;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public boolean getHasHtml() {
		return html != null && html.trim().length() > 0;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean getHasText() {
		return text != null && text.trim().length() > 0;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public static class Attachment {
		public byte[] data = null;
		public String mime = null;
		public String filename = null;
		public String url = null;

		public Attachment(byte[] data, String mime, String filename) {
			this.data = data;
			this.mime = mime;
			this.filename = filename;
		}

		public Attachment(String url) {
			this.url = url;
		}

		public void grabUrl() throws IOException {
			InputStream is = null;
			try {
				URL urlObj = new URL(url);
				filename = urlObj.getFile();
				filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
				URLConnection urlConnection = urlObj.openConnection();
				is = urlConnection.getInputStream();
				data = IOUtils.toByteArray(is);
				mime = urlConnection.getContentType();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception ignored) {
					}
				}
			}
		}
	}

	public void addAttachment(byte[] data, String mime, String filename) {
		attachments.add(new Attachment(data, mime, filename));
	}

	public void addAttachment(String url) {
		attachments.add(new Attachment(url));
	}

	static {
		log.info("setting up security provider");
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	private AsyncEmailer() {
	}

	public static AsyncEmailer getInstance(String subject, String text) {
		AsyncEmailer build = new AsyncEmailer();
		build.setSubject(subject);
		build.setText(text);
		build.setHtml(text);
		// build.setHtml("<html><body><pre>\n" + text +
		// "\n</pre></body></html>");
		return build;
	}

	public static AsyncEmailer getInstance(String fromAddress, String to, String subject, String text) {
		AsyncEmailer build = new AsyncEmailer();
		build.setFromAddress(fromAddress);
		build.setTo(to);
		build.setSubject(subject);
		build.setText(text);
		build.setHtml(text);
		// build.setHtml("<html><body><pre>\n" + text +
		// "\n</pre></body></html>");
		return build;
	}

	@Override
	public void run() {
		this.fromAddress = Settings.getString("email.from.address");
		this.fromName = Settings.getString("email.name");
		this.to = Settings.getString("email.to.address");

		if (!(getHasHtml() || getHasText()))
			throw new RuntimeException("Need some content");
		if (!getHasSubject())
			throw new RuntimeException("Need a subject");
		if (!getHasTo())
			throw new RuntimeException("Need a to address");
		if (to.endsWith("@example.com")) {
			log.info("ignoring email request to: " + to);
			return;
		}

		/*
		 * if (Util.unsubscribed(to)) { log.info("ingoring on unsub: " + to);
		 * return; }
		 */
		if (!getHasFromAddress()) {
			log.warn("FROM is blank. to: " + to + " subject:" + subject + " falling back to default.");
			fromAddress = Settings.getString("email.default.from.address");
		}
		setName("AsyncEmailerThread-" + to);
		log.info("Setting up email. from:" + fromAddress + " to:" + to + " subject:" + subject);
		try {

			Properties properties = new Properties();

			properties.put("mail.smtp.host", Settings.getString("mail.smtp.host"));
			properties.put("mail.smtp.socketFactory.class", Settings.getString("mail.smtp.socketFactory.class"));
			properties.put("mail.smtp.socketFactory.port", Settings.getString("mail.smtp.socketFactory.port"));
			properties.put("mail.smtp.auth", Settings.getString("mail.smtp.auth"));
			properties.put("mail.smtp.port", Settings.getString("mail.smtp.port"));

			Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(Settings.getString("email.username"),
							Settings.getString("email.password"));
				}
			});

			MimeMessage message = new MimeMessage(session);
			List<InternetAddress> toAddresses = new LinkedList<InternetAddress>();
			boolean toIncludesFrom = false;
			for (String tosub : to.split(",| ")) {
				tosub = tosub.trim();
				if (tosub.equals(""))
					continue;
				if (tosub.equals(fromAddress))
					toIncludesFrom = true;
				if (!checkEmail(tosub))
					throw new RuntimeException("Bad to address : " + tosub);
				toAddresses.add(new InternetAddress(tosub));
			}
			if (toAddresses.size() == 0)
				throw new RuntimeException("no to addresses : " + to);
			message.setRecipients(Message.RecipientType.TO,
					toAddresses.toArray(new InternetAddress[toAddresses.size()]));

			if (cc != null) {
				if (!checkEmail(cc))
					throw new RuntimeException("Bad cc address : " + cc);
				message.setRecipients(Message.RecipientType.CC, new InternetAddress[] { new InternetAddress(cc) });
				if (cc.equals(fromAddress))
					toIncludesFrom = true;
			}
			if (bcc != null) {
				if (!checkEmail(bcc))
					throw new RuntimeException("Bad bcc address : " + bcc);
				message.setRecipients(Message.RecipientType.BCC, new InternetAddress[] { new InternetAddress(bcc) });
				if (bcc.equals(fromAddress))
					toIncludesFrom = true;
			}
			if (toIncludesFrom) {
				log.info("Using default from as to includes from : " + fromAddress + " => " + to + " / " + cc + " / "
						+ bcc);
				if (replyTo == null)
					replyTo = fromAddress;
				fromAddress = Settings.getString("email.default.from.address");
			}
			if (fromAddress.indexOf("@yahoo.") > -1 || fromAddress.indexOf("@aol.") > -1
					|| fromAddress.indexOf("@netflix.") > -1 || fromAddress.indexOf("@cs.") > -1
					|| fromAddress.indexOf("@emsil.") > -1 || fromAddress.indexOf("@aim.") > -1) {
				log.info("Not using from address b/c of DMARC issues : " + fromAddress);
				fromAddress = Settings.getString("email.default.from.address");
			}
			InternetAddress fromIA = new InternetAddress(fromAddress, fromName, "UTF-8");
			message.setFrom(fromIA);
			if (replyTo == null) {
				message.setReplyTo(new InternetAddress[] { fromIA });
			} else {
				message.setReplyTo(new InternetAddress[] { new InternetAddress(replyTo) });
			}

			message.setSubject(MimeUtility.encodeText(subject, "UTF-8", null));

			List<MimeBodyPart> attachmentParts = new LinkedList<MimeBodyPart>();
			for (Attachment attachment : attachments) {
				if (attachment.data == null && attachment.url != null) {
					try {
						attachment.grabUrl();
					} catch (Exception e) {
						log.warn("Problem downloading attachment.", e);
						continue;
					}
				}
				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				ByteArrayDataSource dataSource = new ByteArrayDataSource(attachment.data, attachment.mime);
				attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
				if (attachment.filename != null)
					attachmentBodyPart.setFileName(attachment.filename);
				attachmentParts.add(attachmentBodyPart);
			}

			if (attachmentParts.size() == 0 && html == null) {
				message.setContent(text, "text/plain; charset=UTF-8");
			} else {
				// MimePart toAddTo = message;
				MimeMultipart alternative = new MimeMultipart("alternative");
				MimeBodyPart textPart = new MimeBodyPart();
				textPart.setText(text, "UTF-8");
				alternative.addBodyPart(textPart);
				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setContent(html, "text/html; charset=UTF-8");
				alternative.addBodyPart(htmlPart);
				if (attachmentParts.size() == 0) {
					message.setContent(alternative);
				} else {
					MimeMultipart mixed = new MimeMultipart("mixed");
					message.setContent(mixed);
					MimeBodyPart wrap = new MimeBodyPart();
					wrap.setContent(alternative);
					mixed.addBodyPart(wrap);
					for (MimeBodyPart attachmentPart : attachmentParts) {
						mixed.addBodyPart(attachmentPart);
					}
				}
			}

			message.setSentDate(new Date());
			if (html != null)
				LAST_SENT_HTML = html;
			if (text != null)
				LAST_SENT_TEXT = text;

			if (Settings.getBoolean("production")) {
				// URLName urln = new URLName("smtp",
				// Settings.getString("mail.smtp.host"), 587, "",
				// Settings.getString("email.username"),
				// Settings.getString("email.password"));
				// SMTPSSLTransport trans = new SMTPSSLTransport(mailSession,
				// urln);
				// trans.setStartTLS(true);
				// SMTPSSLTransport.send(message);
				// trans.close();
				Transport.send(message);
				log.info("Email sent. from:" + fromAddress + " to:" + to + " subject:" + subject);
			} else {
				StringBuffer report = new StringBuffer("");
				report.append("========================\n");
				for (Enumeration e = message.getAllHeaderLines(); e.hasMoreElements();)
					report.append(e.nextElement() + "\n");
				report.append("========================\n");
				report.append(IOUtils.toString(message.getInputStream(), "UTF-8"));
				report.append("========================\n");
				log.info("Not sending email...\n" + report);
			}
		} catch (MessagingException e) {
			log.error("problem sending email: " + to + " / " + subject, e);
			// Util.errorEmail("AsyncEmailer failed : " + e);
			// throw new RuntimeException("problem sending email: " + to + " / "
			// + subject, e);
		} catch (IOException e) {
			log.error("problem sending email: " + to + " / " + subject, e);
			// Util.errorEmail("AsyncEmailer failed : " + e);
			// throw new RuntimeException("problem sending email: " + to + " / "
			// + subject, e);
		} catch (Exception e) {
			log.error("problem sending email: " + to + " / " + subject, e);
			// Util.errorEmail("AsyncEmailer failed : " + e);
			// throw new RuntimeException("problem sending email: " + to + " / "
			// + subject, e);
		}
	}

	public static boolean checkEmail(String s) {
		return ONE_EMAIL_PATTERN.matcher(s).matches();
	}

	public static String EMAIL_PATTERN_STRING = "[-a-z0-9~!$%^&*_=+}{'?]+(\\.[-a-z0-9~!$%^&*_=+}{'?]+)*@([a-z0-9_][-a-z0-9_]*(\\.[-a-z0-9_]+)*\\.([a-z]{2,})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,5})?";
	public static Pattern ONE_EMAIL_PATTERN = Pattern.compile("\\A" + EMAIL_PATTERN_STRING + "\\z",
			Pattern.CASE_INSENSITIVE);

}