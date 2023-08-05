package org.in.com.dto.enumeration;

public enum TravelStatusEM {
	YET_BOARD("YETBOARD", 1, "Yet to board"), 
	NOT_BOARDED("NOTBOARDED", 2, "Not boarded"), 
	BOARDED("BOARDED", 3, "Boarded"), 
	TRAVELED("TRAVELED", 4, "Traveled"), 
	NOT_TRAVELED("NOTTRAVELED", 5, "Not traveled");

	private final int id;
	private final String code;
	private final String name;

	private TravelStatusEM(String code, int id, String name) {
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

	public static TravelStatusEM getTravelStatusEM(int id) {
		TravelStatusEM[] values = values();
		for (TravelStatusEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return YET_BOARD;
	}

	public static TravelStatusEM getTravelStatusEM(String code) {
		TravelStatusEM[] values = values();
		for (TravelStatusEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return YET_BOARD;
	}
}
