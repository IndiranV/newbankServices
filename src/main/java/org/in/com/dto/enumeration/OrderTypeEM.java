package org.in.com.dto.enumeration;

public enum OrderTypeEM {

	TICKET("TICKBO", 1, "Dr", "Ticket"),
	RECHARGE("RECHG", 2, "Cr", "Recharge");

	private final int id;
	private final String code;
	private final String creditDebitFlag;
	private final String name;

	private OrderTypeEM(String code, int id, String creditDebitFlag, String name) {
		this.code = code;
		this.id = id;
		this.creditDebitFlag = creditDebitFlag;
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

	public String getCreditDebitFlag() {
		return creditDebitFlag;
	}

	public String toString() {
		return code + " : " + id + ": " + creditDebitFlag;
	}

	public static OrderTypeEM getOrderTypeEM(int id) {
		OrderTypeEM[] values = values();
		for (OrderTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return TICKET;
	}

	public static OrderTypeEM getOrderTypeEM(String Code) {
		OrderTypeEM[] values = values();
		for (OrderTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return TICKET;
	}

	public static OrderTypeEM getTransactionTypeDTOFlag(String value) {
		OrderTypeEM[] values = values();
		for (OrderTypeEM errorCode : values) {
			if (errorCode.getCreditDebitFlag().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return TICKET;
	}
}
