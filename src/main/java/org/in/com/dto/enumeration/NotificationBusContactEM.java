package org.in.com.dto.enumeration;

public enum NotificationBusContactEM {
	DRIVER_1("DRV1", 1, "Driver 1"), 
	DRIVER_2("DRV2", 2, "Driver 2"),
	ATTENDER("ATNDR", 3, "Attender"),
	CAPTAIN("CPTN", 4, "Captain");

	private final int id;
	private final String code;
	private final String name;

	private NotificationBusContactEM(String code, int id, String name) {
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

	public static NotificationBusContactEM getTypeEM(int id) {
		NotificationBusContactEM[] values = values();
		for (NotificationBusContactEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return DRIVER_1;
	}

	public static NotificationBusContactEM getTypeEM(String Code) {
		NotificationBusContactEM[] values = values();
		for (NotificationBusContactEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return DRIVER_1;
	}
}