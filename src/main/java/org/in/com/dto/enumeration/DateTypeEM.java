package org.in.com.dto.enumeration;

public enum DateTypeEM {

	TRANSACTION("TXN", 1, "Transaction Date"), TRIP("TRP", 2, "Trip Date");

	private final int id;
	private final String code;
	private final String name;

	private DateTypeEM(String code, int id, String name) {
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

	public String toString() {
		return code + " : " + id + ":" + name;
	}

	public static DateTypeEM getDateTypeEM(int id) {
		DateTypeEM[] values = values();
		for (DateTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Date Type  Not Found: " + id);
		return null;
	}

	public static DateTypeEM getDateTypeEM(String Code) {
		DateTypeEM[] values = values();
		for (DateTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Date Type Not Found: " + Code);
		return null;
	}
}
