package org.in.com.dto.enumeration;

public enum AttendantCategoryEM {

	ATTENDANT("ATDT", 1, "Attendant"), 
	CAPTAIN("CPTN", 2, "Captain");

	private final int id;
	private final String code;
	private final String name;

	private AttendantCategoryEM(String code, int id, String name) {
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

	public static AttendantCategoryEM getCategoryEM(int id) {
		AttendantCategoryEM[] values = values();
		for (AttendantCategoryEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static AttendantCategoryEM getCategoryEM(String Code) {
		AttendantCategoryEM[] values = values();
		for (AttendantCategoryEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}