package org.in.com.dto.enumeration;

public enum FrequencyModeEM {

	DAY_TIME("D", 1, "Day Of Time"), DAY_WEEK("W", 2, "Day Of Month"), DAY_MONTH("M", 3, "Day Of Month");

	private final int id;
	private final String code;
	private final String name;

	private FrequencyModeEM(String code, int id, String name) {
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

	public static FrequencyModeEM getFrequencyModeEM(int id) {
		FrequencyModeEM[] values = values();
		for (FrequencyModeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static FrequencyModeEM getFrequencyModeEM(String Code) {
		FrequencyModeEM[] values = values();
		for (FrequencyModeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}

}
