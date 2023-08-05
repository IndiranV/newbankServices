package org.in.com.dto.enumeration;


public enum MinutesTypeEM {

	MINUTES("MIN", 0, "Minutes"),
	AM("AM", 1, "AM"),
	PM("PM", 2, "PM"); 
	
	private final int id;
	private final String code;
	private final String name;

	private MinutesTypeEM(String code, int id, String name) {
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

	public static MinutesTypeEM getMinutesTypeEM(int id) {
		MinutesTypeEM[] values = values();
		for (MinutesTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static MinutesTypeEM getMinutesTypeEM(String Code) {
		MinutesTypeEM[] values = values();
		for (MinutesTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}
