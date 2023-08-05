package org.in.com.dto.enumeration;

public enum PaymentGatewayTransactionTypeEM {

	PAYMENT("PAY", 1, "Payment for a order"), 
	REFUND("RFD", 2,"payment refund for a cancelled orders");

	private final int id;
	private final String code;
	private final String name;

	private PaymentGatewayTransactionTypeEM(String code, int id, String name) {
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

	public static PaymentGatewayTransactionTypeEM getPaymentGatewayTransactionTypeDTO(
			int id) {
		PaymentGatewayTransactionTypeEM[] values = values();
		for (PaymentGatewayTransactionTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static PaymentGatewayTransactionTypeEM getPaymentGatewayTransactionTypeEM(
			String Code) {
		PaymentGatewayTransactionTypeEM[] values = values();
		for (PaymentGatewayTransactionTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Payment Gateway Status Not Found: " + Code);
		return null;
	}
}
