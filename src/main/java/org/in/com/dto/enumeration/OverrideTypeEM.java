package org.in.com.dto.enumeration;

public enum OverrideTypeEM {

	INCREASE_VALUE("CRVU", 1, "Increase Attribute Value"),
	DECREASE_VALUE("DRVU", 2, "Decrease Attribute Value"),
	FINAL_VALUE("FLVU", 3, "Final Attribute Value");

	private final int id;
	private final String code;
	private final String name;

	private OverrideTypeEM(String code, int id, String name) {
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

	public static OverrideTypeEM getOverrideTypeEM(int id) {
		OverrideTypeEM[] values = values();
		for (OverrideTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static OverrideTypeEM getOverrideTypeEM(String Code) {
		OverrideTypeEM[] values = values();
		for (OverrideTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}
