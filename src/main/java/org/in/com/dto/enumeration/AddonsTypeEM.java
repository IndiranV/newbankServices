package org.in.com.dto.enumeration;

public enum AddonsTypeEM {

	SCHEDULE_DISCOUNT("SHDS", 1, "Cr", "Schedule discount", true), 
	COUPON_DISCOUNT("COUP", 2, "Cr", "Coupon discount", true),
	DISCOUNT_AMOUNT("DSAMT", 3, "Cr", "Discount Amount", true),
	AGENT_SERVICE_CHARGE("AGSC", 4, "NA", "Agent Service Charge", true),
	OFFLINE_DISCOUNT("OFDS", 5, "Cr", "Offline Discount", true),
	TICKET_TRANSFER_CHARGE("TTCA", 6, "Dr", "Ticket Transfer Charge", false),
	WALLET_COUPON("WACP", 7, "Cr", "Wallet Coupon", true),
	WALLET_REDEEM("WARD", 8, "Cr", "Wallet Redeem", true),
	ADDITIONAL_CHARGE("ADCH", 9, "Dr", "Additional Charge", true),
	GO_GREEN("GGN", 10, "Dr", "Go Green", false),
	TRANSFER_PREVIOUS_TICKET_AMOUNT("TRPTA", 11, "Cr", "Transfer Previous Ticket Amount", false),
	CUSTOMER_ID_PROOF("CSTID", 12, "NA", "Customer Id Proof", false),
	PG_SERVICE_CHARGE("PGSCG", 13, "NA", "Pg Service Charge", false);

	private final int id;
	private final String code;
	private final String name;
	private final String creditDebitFlag;
	private final boolean isRefundable;

	private AddonsTypeEM(String code, int id, String creditDebitFlag, String name, boolean isRefundable) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.creditDebitFlag = creditDebitFlag;
		this.isRefundable = isRefundable;
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

	public String getCreditDebitFlag() {
		return creditDebitFlag;
	}

	public boolean isRefundable() {
		return isRefundable;
	}

	public String toString() {
		return code + " : " + id;
	}

	public static AddonsTypeEM getAddonsTypeEM(int id) {
		AddonsTypeEM[] values = values();
		for (AddonsTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static AddonsTypeEM getAddonsTypeEM(String Code) {
		AddonsTypeEM[] values = values();
		for (AddonsTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}