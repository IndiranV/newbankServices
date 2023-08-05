package org.in.com.dto.enumeration;

public enum AuditEventTypeEM {
	SCHEDULE_EVENT("SCHEVE", 1, "Schedule Update Manipulated"),
	SCHEDULE_FARE("SCHFREVE", 2, "Schedule Fare Update Manipulated"),
	USER_EVENT("USREVE", 3, "User Update Manipulated"),
	USER_PAYMENT_EVENT("USPAYEVE", 4, "User Payment Update Manipulated"),
	TRIP_EVENT("TRPEVE", 5, "Trip Update Manipulated"),
	TICKET_BOOK_EVENT("TCKBOEVE", 6, "Ticket Booking Manipulated"),
	TICKET_CANCEL_EVENT("TCKCAEVE", 7, "Ticket Cancellation Manipulated"),
	BUS_EVENT("BUSEVE", 8, "Bus Update Manipulated"),
	DISCOUNT_EVENT("DISEVE", 9, "Discount Update Manipulated");

	private final int id;
	private final String code;
	private final String description;

	private AuditEventTypeEM(String code, int id, String description) {
		this.code = code;
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public static AuditEventTypeEM getNamespaceEventTypeEM(int id) {
		AuditEventTypeEM[] values = values();
		for (AuditEventTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static AuditEventTypeEM getNamespaceEventTypeEM(String code) {
		AuditEventTypeEM[] values = values();
		for (AuditEventTypeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}
}
