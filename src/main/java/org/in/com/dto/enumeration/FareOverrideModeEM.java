package org.in.com.dto.enumeration;


public enum FareOverrideModeEM {

	SCHEDULE_FARE("SCH", 1, "Schedule Fare"), 
	SEARCH_FARE("SERH", 2, "Search Fare"),
	SCHEDULE_FARE_V2("SCHV2", 3, "Schedule Fare V2");

	private final int id;
	private final String code;
	private final String name;

	private FareOverrideModeEM (String code, int id, String name) {
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

	public static FareOverrideModeEM getFareOverrideModeEM(int id) {
		FareOverrideModeEM[] values = values();
		for (FareOverrideModeEM fareOverrideMode : values) {
			if (fareOverrideMode.getId() == id) {
				return fareOverrideMode;
			}
		}
		return null;
	}

	public static FareOverrideModeEM getFareOverrideModeEM(String Code) {
		FareOverrideModeEM[] values = values();
		for (FareOverrideModeEM fareOverrideMode : values) {
			if (fareOverrideMode.getCode().equalsIgnoreCase(Code)) {
				return fareOverrideMode;
			}
		}
		return null;
	}
}
