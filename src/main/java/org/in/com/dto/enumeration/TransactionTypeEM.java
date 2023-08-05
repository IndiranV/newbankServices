package org.in.com.dto.enumeration;

public enum TransactionTypeEM {

	TICKETS_BOOKING("TICKBO", 1, "Dr", "Ticket Booking"),
	TICKETS_CANCEL("TICKCA", 2, "Cr", "Ticket Cancel"),
	RECHARGE("RECHG", 3, "Cr", "Recharge"),
	OPERATOR_PAYMENT("OPPAY", 4, "Cr", "Payment to Opeator Account"), 
	PAYMENT_VOUCHER("PAVR", 5, "Cr", "Payment Voucher"),
	OTHER_CHARGES("CGBO", 6, "Dr", "Other Charges"),
	//Cargo
	CARGO_CANCEL("CGCA", 7, "Cr", "Cargo Cancel"),
	CARGO_DELIVERY("CGDL", 9, "Cr", "Cargo Delivery"), 
	CARGO_PAYMENT_VOUCHER("CGPAVR", 10, "Cr", "Cargo Payment Voucher"),
	CARGO_PAYMENT_INVOICE("CGINVPA", 11, "Cr", "Cargo Invoice Payment"),
	PAYMENT_RECEIPT("CGPART", 12, "Cr", "Payment Receipt"),
	
	REVOKE_RECEIPT("RVREPT", 8, "Dr", "Revoke Receipt");

	private final int id;
	private final String code;
	private final String creditDebitFlag;
	private final String name;

	private TransactionTypeEM(String code, int id, String creditDebitFlag, String name) {
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

	public static TransactionTypeEM getTransactionTypeEM(int id) {
		TransactionTypeEM[] values = values();
		for (TransactionTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static TransactionTypeEM getTransactionTypeEM(String Code) {
		TransactionTypeEM[] values = values();
		for (TransactionTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}

	public static TransactionTypeEM getTransactionTypeDTOFlag(String value) {
		TransactionTypeEM[] values = values();
		for (TransactionTypeEM errorCode : values) {
			if (errorCode.getCreditDebitFlag().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return null;
	}
}
