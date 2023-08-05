package org.in.com.dto.enumeration;

public enum PaymentTypeEM {

	PAYMENT_UNLIMITED("ULTED", -1, "Payment Unlimited"), 
	PAYMENT_PRE_PAID("PRE", 1, "Payment Pre Paid"), 
	PAYMENT_POST_PAID("POT", 2, "Payment Post Paid"), 
	PAYMENT_GATEWAY_PAID("PGP", 3, "Payment Gateway Pay"),
	
	/* cargo*/
	PAYMENT_TO_PAY("TOP", 4, "To Pay"),
	PAYMENT_INVOICE_PAY("INP", 5, "Ac - To Pay"),
	PAYMENT_PAID("PAD", 6, "Paid"),
	PAYMENT_TO_PAY_PAID("TPPAD", 7, "To Pay Paid"),
	FREE_SERVICE("FS", 8, "Free Service"),
	PAYMENT_WAY_TO_PAY("WTOP", 9, "Way To Pay");

	private final int id;
	private final String code;
	private final String name;

	private PaymentTypeEM(String code, int id, String name) {
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

	public static PaymentTypeEM getPaymentTypeEM(int id) {
		PaymentTypeEM[] values = values();
		for (PaymentTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Payment Type  Not Found: " + id);
		return PAYMENT_GATEWAY_PAID;
	}

	public static PaymentTypeEM getPaymentTypeEM(String Code) {
		PaymentTypeEM[] values = values();
		for (PaymentTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Payment Type  Not Found: " + Code);
		return PAYMENT_GATEWAY_PAID;
	}
}
