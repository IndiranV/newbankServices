package org.in.com.dto.enumeration;

public enum UserRoleEM {
	USER_ROLE("USER", 1, "User"), 
	CUST_ROLE("CUST", 2, "Customer"), 
	TABLET_POB_ROLE("TABT", 3, "Tablet"), 
	DRIVER("DIVR", 4, "Driver");

	private final int id;
	private final String code;
	private final String name;

	private UserRoleEM(String code, int id, String name) {
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

	public static UserRoleEM getUserRoleEM(int id) {
		UserRoleEM[] values = values();
		for (UserRoleEM roleDTO : values) {
			if (roleDTO.getId() == id) {
				return roleDTO;
			}
		}
		return null;
	}

	public static UserRoleEM getUserRoleEM(String code) {
		UserRoleEM[] values = values();
		for (UserRoleEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(code)) {
				return errorCode;
			}
		}
		return null;
	}
}
