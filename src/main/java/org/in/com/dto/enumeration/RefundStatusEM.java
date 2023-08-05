package org.in.com.dto.enumeration;

public enum RefundStatusEM {

	INITIAL("INI", 1, "Refund Initial"),
	REQUEST_TO_BANK("REQ", 2, "Refund Request to Bank"),
	PROCESSED_BY_BANK("PRO", 3, "Refund Processed"), 
	REFUND_TO_ACCOUNT("RTAC", 4, "Refund To Your Account");

	private final int id;
	private final String code;
	private final String description;

	private RefundStatusEM(String code, int id, String description) {
		this.code = code;
		this.id = id;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static RefundStatusEM getRefundStatusEM(int id) {
		RefundStatusEM[] values = values();
		for (RefundStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static RefundStatusEM getRefundStatusEM(String value) {
		RefundStatusEM[] values = values();
		for (RefundStatusEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return null;
	}
}
