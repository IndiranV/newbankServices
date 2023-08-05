package org.in.com.aggregator.mail;

public class SmtpInfo {
	private String email;
	private String password;
	private String host;
	private String smtpusername;
	private String smtppassword;
	private Integer port;
	private String name;
	private String bounceAddress;
	private Boolean debug;
	private Boolean sslOnConnect;
	private Boolean startTLSEnabled;
	private String replyEmail;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBounceAddress() {
		return bounceAddress;
	}

	public void setBounceAddress(String bounceAddress) {
		this.bounceAddress = bounceAddress;
	}

	public Boolean getDebug() {
		return debug;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	public Boolean getSslOnConnect() {
		return sslOnConnect;
	}

	public void setSslOnConnect(Boolean sslOnConnect) {
		this.sslOnConnect = sslOnConnect;
	}

	public Boolean getStartTLSEnabled() {
		return startTLSEnabled;
	}

	public void setStartTLSEnabled(Boolean startTLSEnabled) {
		this.startTLSEnabled = startTLSEnabled;
	}

	public String getSmtpusername() {
		return smtpusername;
	}

	public void setSmtpusername(String smtpusername) {
		this.smtpusername = smtpusername;
	}

	public String getSmtppassword() {
		return smtppassword;
	}

	public void setSmtppassword(String smtppassword) {
		this.smtppassword = smtppassword;
	}

	public String getReplyEmail() {
		return replyEmail;
	}

	public void setReplyemail(String replyEmail) {
		this.replyEmail = replyEmail;
	}

}
