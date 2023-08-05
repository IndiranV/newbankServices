package org.in.com.dto.enumeration;

public enum TicketStatusEM {

	CONFIRM_BOOKED_TICKETS("BO",1,"Booked"),
	CONFIRM_CANCELLED_TICKETS("CA",2,"Cancelled"),
	PHONE_BOOKING_CANCELLED("PBC", 3,"Phone Booking Cancelled"),
	TMP_BLOCKED_TICKET("BL",4,"Not Confirmed Ticket"),
	PHONE_BLOCKED_TICKET("PBL", 5,"Phone Blocked Tickets"),
	TICKET_TRANSFERRED("TCKTR",6,"Ticket Transfered"),
	TICKET_NOT_TRAVEL("TNT",7,"Ticket Not Travel"),
 	TRIP_CANCELLED("TCA", 8,"Trip Cancelled"),
	TRIP_CANCEL_INITIATED("TCAI", 9, "Trip Cancel Initiated"),
	TENTATIVE_BLOCK_CANCELLED("BLCA", 10, "Tentative Block Cancelled"),
	TRIP_SEAT_QUOTA("QTA", 11, "Trip Seat Quota"),

	CANCELLATION_ALLOWED("CAL",15,"Cancellation Allowed"),
	CANCELLATION_NOT_ALLOWED("CNAL",16,"Cancellation Not Allowed"),
	
	EDIT_TICKET("EDTK", 17, "Edit Ticket"),
	TICKET_BOARDED("TKBD", 18, "Ticket Boarded"),
	TICKET_NOT_BOARDED("NTBD", 19, "Ticket Not Boarded"),
	TICKET_YET_BOARD("YTBD", 20, "Ticket Yet to board"),
	TICKET_TRAVELED("TRVLD", 21, "Ticket Traveled"),
	TICKET_PARTIAL_CANCELLED("PRLCA", 22, "Ticket Partially Cancelled"),
	TICKET_SEAT_SWAP("STSWP", 22, "Seat Swap"); 
	
	private final int id;
	private final String code;
	private final String description;

	private TicketStatusEM(String code, int id, String description) {
		this.code = code;
		this.id = id;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}
	public String getDescription() {
		return description;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static TicketStatusEM getTicketStatusEM(int id) {
		TicketStatusEM[] values = values();
		for (TicketStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static TicketStatusEM getTicketStatusEM(String value) {
		TicketStatusEM[] values = values();
		for (TicketStatusEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return null;
	}
}
