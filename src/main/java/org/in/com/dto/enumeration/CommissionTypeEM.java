package org.in.com.dto.enumeration;

public enum CommissionTypeEM {

	TICKETS_BOOKING("BO", 1, "Ticket Booking"),
	TICKETS_CANCEL_COMMISSION_ON_CHARGE("CA", 2, "Cancel Commission on Cancel Charge"),
	TICKET_CANCEL_COMMISSION_ON_TICKETFARE("CCA", 3, "Cancel Commission on Ticket Fare");

	private final int id;
	private final String code;
	private final String name;

	private CommissionTypeEM(String code, int id, String name) {
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

	public static CommissionTypeEM getCommissionTypeEM(int id) {
		CommissionTypeEM[] values = values();
		for (CommissionTypeEM commission : values) {
			if (commission.getId() == id) {
				return commission;
			}
		}
		return null;
	}

	public static CommissionTypeEM getCommissionTypeEM(String Code) {
		CommissionTypeEM[] values = values();
		for (CommissionTypeEM commission : values) {
			if (commission.getCode().equalsIgnoreCase(Code)) {
				return commission;
			}
		}
		return null;
	}

}
