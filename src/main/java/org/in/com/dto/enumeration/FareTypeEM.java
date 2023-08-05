package org.in.com.dto.enumeration;


public enum FareTypeEM {

	FLAT("FLT", 1, "Flat Fare"),
	PERCENTAGE("PER", 2, "Percentage Fare");

	private final int id;
	private final String code;
	private final String name;

	private FareTypeEM(String code, int id, String name) {
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

	public static FareTypeEM getFareTypeEM(int id) {
		FareTypeEM[] values = values();
		for (FareTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static FareTypeEM getFareTypeEM(String Code) {
		FareTypeEM[] values = values();
		for (FareTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}
