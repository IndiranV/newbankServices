package org.in.com.dto.enumeration;


public enum FareOverrideTypeEM {

	INCREASE_FARE("CRFA", 1, "Increase Fare"),
	DECREASE_FARE("DRFA", 2, "Decrease Fare"), 
	FINAL_FARE("FLFA", 3, "Final Fare");

	private final int id;
	private final String code;
	private final String name;

	private FareOverrideTypeEM(String code, int id, String name) {
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

	public static FareOverrideTypeEM getFareOverrideTypeEM(int id) {
		FareOverrideTypeEM[] values = values();
		for (FareOverrideTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static FareOverrideTypeEM getFareOverrideTypeEM(String Code) {
		FareOverrideTypeEM[] values = values();
		for (FareOverrideTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}
