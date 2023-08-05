package org.in.com.dto.enumeration;

public enum AuthenticationTypeEM {

	ALL_BITS_DEFAULT("ALL", 0, "Default all users"),
	ALL_REGISTERED_USER("ARU", 1, "All Registed users"),
	BITS_USERNAME_PASSWORD("BITSUP", 2, "Username passowrd"), 
	BITS_API_TOKEN("BITSAPI", 3, "API Token"), 
	SSO_FACEBOOK("FACEBOOK", 4, "Facebook"),
	SSO_GMAIL("GMAIL", 5, "gmail"),
	BITS_GUEST("GUEST", 6, "Operator Guest with out username password"),
	TABLET_POB("TABPOB", 7, "Tablet point of boarding"),
	BITS_CUSTOMER("CUST", 8, "Operator User Customer"),
	TABLET_POB_DRIVER("TABPOBDR", 9, "Driver Tablet point of boarding");

	private final int id;
	private final String code;
	private final String name;

	private AuthenticationTypeEM(String code, int id, String name) {
		this.code = code;
		this.id = id;
		this.name = name;
	}

	public Integer getId() {

		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static AuthenticationTypeEM getAuthenticationTypeEM(int id) {
		AuthenticationTypeEM[] values = values();
		for (AuthenticationTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return BITS_USERNAME_PASSWORD;
	}

	public static AuthenticationTypeEM getAuthenticationTypeEM(String Code) {
		AuthenticationTypeEM[] values = values();
		for (AuthenticationTypeEM typeEM : values) {
			if (typeEM.getCode().equalsIgnoreCase(Code)) {
				return typeEM;
			}
		}
		return BITS_USERNAME_PASSWORD;
	}
}
