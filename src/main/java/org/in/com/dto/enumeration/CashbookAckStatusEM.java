package org.in.com.dto.enumeration;

public enum CashbookAckStatusEM {

	INITIATED("INIT", 1, "Initiated"), 
	APPROVED("APRD", 2, "Approved"), 
	REJECTED("REJT", 3, "Rejected");

	private final int id;
	private final String code;
	private final String name;

	private CashbookAckStatusEM(String code, int id, String name) {
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

	public static CashbookAckStatusEM getCashbookAckStatusEM(int id) {
		CashbookAckStatusEM[] values = values();
		for (CashbookAckStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Cashbook Ack Status Not Found: " + id);
		return null;
	}

	public static CashbookAckStatusEM getCashbookAckStatusEM(String Code) {
		CashbookAckStatusEM[] values = values();
		for (CashbookAckStatusEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Cashbook Ack Status Not Found: " + Code);
		return null;
	}
}
