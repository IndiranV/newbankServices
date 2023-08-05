package org.in.com.aggregator.payment.enumeration;

public enum ICICIFirstDataCredentialEM {

	BITS("bits", "WS3396045174._.1", "X49#zDU^VJ"),
	LAXMITRAVELS("laxmitravels", "WS3396045174._.1", "X49#zDU^VJ"),
	WINTRAVELS("wintravels", "WS3396073242._.1", "U\\4yx;7Ukn");

	private final String operatorCode;
	// API userID (User id)
	private final String ksFile;
	// Password(password), Not Certificate password
	private final String ksPassword;

	private ICICIFirstDataCredentialEM(String operatorCode, String ksFile, String ksPassword) {
		this.operatorCode = operatorCode;
		this.ksFile = ksFile;
		this.ksPassword = ksPassword;
	}

	public String getOperatorCode() {
		return operatorCode;
	}

	public String getKsFile() {
		return ksFile;
	}

	public String getKsPassword() {
		return ksPassword;
	}

	public static ICICIFirstDataCredentialEM get(String code) {
		for (ICICIFirstDataCredentialEM icicienum : ICICIFirstDataCredentialEM.values()) {
			if (icicienum.getOperatorCode().equals(code)) {
				return icicienum;
			}
		}
		return null;
	}
}
