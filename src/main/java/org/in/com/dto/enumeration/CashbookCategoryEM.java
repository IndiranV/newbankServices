package org.in.com.dto.enumeration;

public enum CashbookCategoryEM {

	VEHICLE("VEH", 1, "Vehicle"), 
	TRIP("TRP", 2, "Trip"), 
	BRANCH("BRN", 3, "Branch"), 
	PNR("PNR", 4, "PNR");

	private final int id;
	private final String code;
	private final String name;

	private CashbookCategoryEM(String code, int id, String name) {
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

	public static CashbookCategoryEM getCashbookCategoryEM(int id) {
		CashbookCategoryEM[] values = values();
		for (CashbookCategoryEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Cashbook Category Not Found: " + id);
		return null;
	}

	public static CashbookCategoryEM getCashbookCategoryEM(String Code) {
		CashbookCategoryEM[] values = values();
		for (CashbookCategoryEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Cashbook Category Not Found: " + Code);
		return null;
	}
}
