package org.in.com.aggregator.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;

import javax.activation.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TemplateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

public class EmailClient {
	private static final Logger logger = LoggerFactory.getLogger(EmailClient.class);
	private static ModelMapper mapper = new ModelMapper();
	private static SmtpInfo smtpInfo = null;
	private static Properties emailProps = null;
	private HtmlEmail email;
	static {
		try {
			emailProps = new Properties();
			try {
				File configDir = new File(System.getProperty("catalina.base"), "conf");
				File configFile = new File(configDir, "email.properties");
				if (configFile.exists()) {
					InputStream stream = new FileInputStream(configFile);
					emailProps.load(stream);
					logger.info("Email Properties loaded from Server");
				}
				else {
					emailProps.load(EmailClient.class.getResourceAsStream("/email.properties"));
					logger.info("Email Properties loaded from Application");
				}
			}
			catch (FileNotFoundException fe) {
				logger.info("unable to load SMS Properties");
			}
			catch (Exception e) {
				logger.info("unable to load SMS Properties");
			}

			smtpInfo = mapper.map(emailProps, SmtpInfo.class);
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid properties file, could not initialize email client");
		}
	}

	public static EmailClient getDefaultMailClient(String senderName, String replyToName) throws Exception {
		return new EmailClient(smtpInfo, senderName, replyToName);
	}

	public static EmailClient getCustomMailClient(SmtpInfo smtpInfo, String senderName, String replyToName) throws EmailException {
		EmailClient emailer = new EmailClient(smtpInfo, senderName, replyToName);
		return emailer;
	}

	private EmailClient(SmtpInfo smtpInfo, String senderName, String replyToName) throws EmailException {
		email = new HtmlEmail();
		email.setHostName(smtpInfo.getHost());
		email.setSmtpPort(smtpInfo.getPort());
		email.addHeader("Priority", "1");
		email.setAuthenticator(new DefaultAuthenticator(smtpInfo.getSmtpusername(), smtpInfo.getSmtppassword()));
		email.setSSLOnConnect(smtpInfo.getSslOnConnect());
		email.setStartTLSEnabled(smtpInfo.getStartTLSEnabled());
		email.setFrom(smtpInfo.getEmail(), (StringUtil.isNotNull(senderName) ? senderName : smtpInfo.getName()));
		email.setDebug(smtpInfo.getDebug());
		email.addReplyTo(smtpInfo.getReplyEmail(), replyToName);
		email.setBounceAddress(smtpInfo.getEmail());
	}

	public void addTo(String emailAddress, String name) throws EmailException {
		if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(name)) {
			throw new EmailException("Invalid email address");
		}
		email.addTo(emailAddress, name);
	}

	public void addTo(String... emailAddress) throws EmailException {
		if (emailAddress == null || emailAddress.length < 1) {
			throw new EmailException("Invalid email address");
		}
		email.addTo(emailAddress);
	}

	public void addCc(String emailAddress, String name) throws EmailException {
		if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(name)) {
			throw new EmailException("Invalid email address");
		}
		email.addCc(emailAddress, name);
	}

	public void addBcc(String emailAddress, String name) throws EmailException {
		if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(name)) {
			throw new EmailException("Invalid email address");
		}
		email.addBcc(emailAddress, name);
	}

	public void addTo(Map<String, String> map) throws EmailException {
		if (map == null) {
			throw new EmailException("Invalid Map");
		}
		for (Entry<String, String> entry : map.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getKey())) {
				addTo(entry.getKey(), entry.getValue());
			}
		}
	}

	public void addCc(Map<String, String> map) throws EmailException {
		if (map == null) {
			throw new EmailException("Invalid Map");
		}
		for (Entry<String, String> entry : map.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getKey())) {
				addCc(entry.getKey(), entry.getValue());
			}
		}
	}

	public void addBcc(Map<String, String> map) throws EmailException {
		if (map == null) {
			throw new EmailException("Invalid Map");
		}
		for (Entry<String, String> entry : map.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getKey())) {
				addBcc(entry.getKey(), entry.getValue());
			}
		}

	}

	public void addAttachment(String url) throws EmailException, MalformedURLException {
		EmailAttachment attachment = new EmailAttachment();
		attachment.setURL(new URL(url));
		email.attach(attachment);
	}

	public void addAttachment(File file) throws EmailException {
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(file.getAbsolutePath());
		email.attach(attachment);
	}

	public void addAttachment(DataSource ds, String name, String description) throws EmailException {
		email.attach(ds, name, description);
	}

	public String sendHtmlEmail(String subject, String content) throws EmailException {
		email.setSubject(subject);
		email.setHtmlMsg(content);
		email.setTextMsg("Your email client does not support html content");
		return send();
	}

	public String setTextEmail(String subject, String content) throws EmailException {
		email.setSubject(subject);
		email.setTextMsg(content);
		return send();
	}

	public String sendEmail(String subject, String htmlContent, String textContent) throws EmailException {
		email.setSubject(subject);
		email.setHtmlMsg(htmlContent);
		email.setTextMsg(textContent);
		return send();
	}

	private String send() throws EmailException {
		String sentId = email.send();
		if (StringUtils.isNotBlank(sentId)) {
			logger.debug("Email sent successfully");
		}
		return sentId;
	}

	@Async
	public Future<String> sendAsyncHtmlEmail(String subject, String content) throws EmailException {
		String id = sendHtmlEmail(subject, content);
		return new AsyncResult<String>(id);
	}

	public void sendHtmlMail(String template, Map<String, Object> bodyContentData) throws Exception {
		String subject = TemplateUtils.getInstance().processEmailSubject(template);

		String body = TemplateUtils.getInstance().processEmailContent(template, bodyContentData);

		sendAsyncHtmlEmail(subject, body);
	}

	public void sendHtmlMail(String template, Map<String, Object> bodyContentData, Map<String, String> subjectData) throws Exception {
		String subject = TemplateUtils.getInstance().processEmailSubject(template, subjectData);

		String body = TemplateUtils.getInstance().processEmailContent(template, bodyContentData);

		sendAsyncHtmlEmail(subject, body);

	}

	public static void main(String a[]) {
		try {
			HtmlEmail email = new HtmlEmail();
			email.setHostName("email-smtp.us-west-2.amazonaws.com");
			email.setSmtpPort(465);
			email.addReplyTo("ezeebus@ezeeinfosolutions.com", "Support Desk");
			email.setAuthenticator(new DefaultAuthenticator("AKIAUVUGFZLSH7QZYID6", "YJ5SfW7EUWlSQkDfcuHyCVcKaFI7tOqykrO6Bnf9"));
			email.setSSLOnConnect(true);
			email.setStartTLSEnabled(true);
			email.setFrom("noreply@parveentravels.in", "Testing");
			email.setDebug(false);
			email.addTo("ramasamy@ezeeinfosolutions.com");
			email.setBounceAddress("noreply@parveentravels.in");
			email.setMsg("testing");
			email.setSubject("testing");
			email.send();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
