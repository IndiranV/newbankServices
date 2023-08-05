package org.in.com.aggregator.mercservices;

public enum MercEntityTypeEM {

	TICKET_HISTORY("TCKHIS", "Ticket History", "pnrhistroy"),
	USER_LOGIN_HISTORY("LGINHIS", "Login History", "userloginhistroy"),
	FARE_HISTORY("FAREHIS", "Fare History", "farehistroy"),
	MENU_AUDIT_HISTORY("MNUAUL", "Menu Privilege Audit History", "menuprivilegeaudithistory"),
	NAMESPACE_PROFILE_HISTORY("NMSTAL", "Namespace Profile History", "namespaceprofilehistory");

	private final String code;
	private final String name;
	private final String url;

	private MercEntityTypeEM(String code, String name, String url) {
		this.code = code;
		this.name = name;
		this.url = url;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public static MercEntityTypeEM getMercEntityTypeEM(String namespaceCode) {
		MercEntityTypeEM[] values = values();
		for (MercEntityTypeEM code : values) {
			if (code.getCode().equalsIgnoreCase(namespaceCode)) {
				return code;
			}
		}
		return null;
	}

}
