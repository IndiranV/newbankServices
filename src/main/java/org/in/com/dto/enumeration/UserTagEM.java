package org.in.com.dto.enumeration;

public enum UserTagEM {
	ONLINE_USER("ONUSR", 1, "Online user"),
	OFFLINE_USER("OFFUSR", 2, "Offline user"), 
	API_USER_RB("APIRB", 3, "Redbus"),
/*	API_USER_TY("APITY", 4, "Travelyaari"),*/
	API_USER_PT("APIPT", 5, "Paytm"),
	API_USER_AB("APIAB", 6, "Abhibus"),
	API_USER_EZ("APIEZ", 7, "Ezeeinfo"),
	API_USER_CB("APICB", 8, "Cobota"),
	API_USER_MT("APIMT", 9, "Maven Techlabs"),
/* 	API_USER_HM("APIHM", 10, "Herms"), */
	TRIP_INFO("TRPINF", 11, "Trip Info"),
	TRIP_ALL_CUSTOMER("ALLCUST", 12, "All Customer"),
	TRIP_OWN_CUSTOMER("OWNCUST", 13, "Own Customer"),
	BRANCH_USER("BRUSR",14,"Branch User"),
	OTA_USER("OTAUSR",15,"OTA Agent"),
	AGENT_USER("AGUSR",16,"Agent");

	private final int id;
	private final String code;
	private final String name;

	private UserTagEM(String code, int id, String name) {
		this.code = code;
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static UserTagEM getUserTagEM(int id) {
		UserTagEM[] values = values();
		for (UserTagEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static UserTagEM getUserTagEM(String code) {
		UserTagEM[] values = values();
		for (UserTagEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}
