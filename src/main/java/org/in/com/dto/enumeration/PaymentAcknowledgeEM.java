package org.in.com.dto.enumeration;

public enum PaymentAcknowledgeEM {

	PAYMENT_INITIATED("INITD", 1, "Initiated"),
	PAYMENT_ACKNOWLEDGED("ACKED",2, "Acknowledged"),
	PAYMENT_REJECT("RJECT",3, "Rejected"),
	PAYMENT_PAID("PAID",4, "Payment Paid"),
	PARTIAL_PAYMENT_PAID("PAPAID",5, "Partial Payment Paid");

	private final int id;
	private final String code;
	private final String name;

	private PaymentAcknowledgeEM(String code, int id, String name) {
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

	public static PaymentAcknowledgeEM getPaymentAcknowledgeDTO(int id) {
		PaymentAcknowledgeEM[] values = values();
		for (PaymentAcknowledgeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static PaymentAcknowledgeEM getPaymentAcknowledgeDTO(String Code) {
		PaymentAcknowledgeEM[] values = values();
		for (PaymentAcknowledgeEM acknowledgeDTO : values) {
			if (acknowledgeDTO.getCode().equalsIgnoreCase(Code)) {
				return acknowledgeDTO;
			}
		}
		return null;
	}

}
