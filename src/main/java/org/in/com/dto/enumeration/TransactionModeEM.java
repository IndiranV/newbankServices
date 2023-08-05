package org.in.com.dto.enumeration;

public enum TransactionModeEM {

	PAYMENT_CASH("CASH", 1, "Cash Payment"),
	PAYMENT_NBK("NBK", 2, "Netbanking"),
	PAYMENT_CREDIT_CARD("CCD", 3, "Credit Card"),
	PAYMENT_CHEQUE("CHEQUE", 4, "Cheque"),
	PAYMENT_PAYMENT_GATEWAY("PGWAY", 5, "PG Online"),
	PAYMENT_PREPAID("PPAID", 6, "Pre paid Account"),
	PAYMENT_UPI("PUPI", 7, "Unified Payments Interface"),
	PYAMENT_NEFT("NEFT", 8, "NEFT"),
	PYAMENT_RTGS("RTGS", 9, "RTGS"),
	PYAMENT_IMPS("IMPS", 10, "IMPS"),
	PAYMENT_DEBIT_CARD("DCD", 11, "Debit Card");

	private final int id;
	private final String code;
	private final String name;

	private TransactionModeEM(String code, int id, String name) {
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
		return code + " : " + id + ": " + name;
	}

	public static TransactionModeEM getTransactionModeEM(int id) {
		TransactionModeEM[] values = values();
		for (TransactionModeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static TransactionModeEM getTransactionModeEM(String code) {
		TransactionModeEM[] values = values();
		for (TransactionModeEM modeDTO : values) {
			if (modeDTO.getCode().equalsIgnoreCase(code)) {
				return modeDTO;
			}
		}
		return null;
	}

}
