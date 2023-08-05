package org.in.com.dto.enumeration;

public enum PaymentReceiptTypeEM {

	COLLECTION("COLN", 1, "Collection"),
	PAYMENT("PAY", 2, "Payment");

	private final int id;
	private final String code;
	private final String name;

	private PaymentReceiptTypeEM(String code, int id, String name) {
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

	public static PaymentReceiptTypeEM getPaymentReceiptType(int id) {
		PaymentReceiptTypeEM[] values = values();
		for (PaymentReceiptTypeEM paymentReceiptTypeEM : values) {
			if (paymentReceiptTypeEM.getId() == id) {
				return paymentReceiptTypeEM;
			}
		}
		return null;
	}

	public static PaymentReceiptTypeEM getPaymentReceiptType(String Code) {
		PaymentReceiptTypeEM[] values = values();
		for (PaymentReceiptTypeEM paymentReceiptTypeEM : values) {
			if (paymentReceiptTypeEM.getCode().equalsIgnoreCase(Code)) {
				return paymentReceiptTypeEM;
			}
		}
		return null;
	}

}
