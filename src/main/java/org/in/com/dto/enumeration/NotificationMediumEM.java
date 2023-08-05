package org.in.com.dto.enumeration;

public enum NotificationMediumEM {
	EZEEBOT_APP("APP", 1, "App"),
	SMS("SMS", 2, "SMS"),
	E_MAIL("MAIL", 3, "e-mail"),
	WHATS_APP("WAPP", 4, "Whats App");

	private final int id;
	private final String code;
	private final String name;

	private NotificationMediumEM(String code, int id, String name) {
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

	public static NotificationMediumEM getNotificationMediumEM(int id) {
		NotificationMediumEM[] values = values();
		for (NotificationMediumEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static NotificationMediumEM getNotificationMediumEM(String code) {
		NotificationMediumEM[] values = values();
		for (NotificationMediumEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}
