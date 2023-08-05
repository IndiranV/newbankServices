package org.in.com.dto.enumeration;

public enum EnRouteTypeEM {
	OPEN_ON_PREVIOUS_STAGE("OPST", 1, "Open On Previous Stage"), OPEN_ON_TRIP_TIME("OOTT", 2, "Open On Trip Time");

	private final int id;
	private final String code;
	private final String name;

	private EnRouteTypeEM(String code, int id, String name) {
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

	public static EnRouteTypeEM getEnRouteTypeEM(int id) {
		EnRouteTypeEM[] values = values();
		for (EnRouteTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static EnRouteTypeEM getEnRouteTypeEM(String code) {
		EnRouteTypeEM[] values = values();
		for (EnRouteTypeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}
