package org.in.com.dto.enumeration;

public enum PaymentOrderEM {
	SUCCESS("SUCCESS", 1, "Payment Success"),
	PAYMENT_DECLINED("DECLINED", 2, "Payment declined");

	private final int id;
	private final String code;
	private final String name;

	private PaymentOrderEM(String code, int id, String name) {
		this.code = code;
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static PaymentOrderEM getPaymentOrderEM(int id) {
		PaymentOrderEM[] values = values();
		for (PaymentOrderEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static PaymentOrderEM getPaymentOrderEM(String code) {
		PaymentOrderEM[] values = values();
		for (PaymentOrderEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}
