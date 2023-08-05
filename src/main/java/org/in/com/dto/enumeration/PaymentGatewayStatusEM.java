package org.in.com.dto.enumeration;

public enum PaymentGatewayStatusEM {

	ORDER_INITIATED("ORIN", 1, "Order being processes "), 
	ORDER_PG_RESPONSE("ORRE", 2, "Received response from PG"), 
	SUCCESS("SUSS", 3, "Payment Successfully"), 
	FAILURE("FARE", 4, "Order fail"), 
	ORDER_CANCELLED("ORCA", 5, "Order cancelled"), 
	PENDING_ORDER_SUCCESS("PSUSS", 6, "Payment Success By User"), 
	PENDING_ORDER_CANCELLED("PORCA", 7, "Order cancelled By User");

	private final int id;
	private final String code;
	private final String name;

	private PaymentGatewayStatusEM(String code, int id, String name) {
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

	public static PaymentGatewayStatusEM getStatusDTO(int id) {
		PaymentGatewayStatusEM[] values = values();
		for (PaymentGatewayStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static PaymentGatewayStatusEM getStatusDTO(String Code) {
		PaymentGatewayStatusEM[] values = values();
		for (PaymentGatewayStatusEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Payment Gateway Status Not Found: " + Code);
		return null;
	}
}
