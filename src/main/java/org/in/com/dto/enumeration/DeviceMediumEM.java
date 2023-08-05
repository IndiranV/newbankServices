package org.in.com.dto.enumeration;

public enum DeviceMediumEM {
	ALL_USER("ALL", 0, "All user"),
	WEB_USER("WEB", 1, "Online user"),
	API_USER("API", 2, "Api user"), 
	MOB_USER("MOB", 3, "Mobile user"),
	APP_USER("APP", 4, "App user"), 
	APP_IOS("IOS", 5, "IOS User"), 
	APP_AND("AND", 6, "Android User"),
	APP_TABLET_POB("TBPOB", 7, "Tablet POB"),
	EZEEBOT_APP("EZBOT", 8, "Ezee Bot App"),
	WEB_API_USER("WEBAPI", 9, "Web API User");

	private final int id;
	private final String code;
	private final String name;

	private DeviceMediumEM(String code, int id, String name) {
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

	public static DeviceMediumEM getDeviceMediumEM(int id) {
		DeviceMediumEM[] values = values();
		for (DeviceMediumEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return ALL_USER;
	}

	public static DeviceMediumEM getDeviceMediumEM(String code) {
		DeviceMediumEM[] values = values();
		for (DeviceMediumEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return ALL_USER;
	}

}
