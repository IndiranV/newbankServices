package org.in.com.dto.enumeration;

public enum MenuEventEM {

	BOOKING_SCHEDULE_ADD("", "SCH-ADD", "Add"),
	BOOKING_SCHEDULE_EDIT("", "SCH-EDIT", "EDIT"), 
	BOOKING_SCHEDULE_DELETE("", "SCH-DELETE", "DELETE"), 
	BOOKING_SCHEDULE_VISIBILITY_RIGHTS("SCHEDULE", "SCH-VISIBILITY-RIGHTS", "Schedule Visibility Rights"), 
	BOOKING_SCHEDULE_VIEW_RIGHTS_ALL("SCHEDULE", "SCH-VIEW-RIGHTS-APPLY", "Schedule View Rights Apply"), 
	BOOKING_CANCEL_OVERRIDE_AMOUNT("", "OVER-REF-AMT", "Over Ref Amount"),
	BOOKING_CANCEL_OVERRIDE_AS_POLICY("", "OVER-AS-POLICY", "Override As Per Policy"),
	BOOKING_AFTER_TRIP_TIME("TCK_SEARCH_BOOK", "BOOK-AFTER-TRIP-TIME", "Book After Trip Time/Offline Booking"),
	BOOKING_PHONE_ALLOW("TCK_SEARCH_BOOK", "PHB-ALLOWED", "Allow Phone Book"),
	BOOKING_SPECIAL_DISCOUNT("TCK_SEARCH_BOOK", "SPL-DISCOUNT", "Special Discount"),
	BOOKING_FREE_SERVICE_TICKET("TCK_SEARCH_BOOK", "ALLOW-FREE-SERVICE-TCK", "Free Service Tickets"),
	BOOKING_FIND_EDIT_ALL_TCK("BOOKING_TCK_FEDIT", "ALLOW-ALL-USER-TCK", "Find and edit all ticket"),
	BOOKING_PHONE_ALL_USER_TCK("BOOKING_PH_ALL_TCK", "ALLOW-ALL-USER-TCK", "Confirm All user tickets"),
	BOOKING_ON_OTP("TCK_SEARCH_BOOK", "BOOK-ON-OTP", "Book on OTP"),
	REPORT_TRIP_CHART_RIGHTS_30("REPORT-TRP-CRT", "TRP-VIEW-RIGHTS-30", "Trip View Rights Apply 30 Minutes"), 
	REPORT_TRIP_CHART_RIGHTS_3HR("REPORT-TRP-CRT", "TRP-VIEW-RIGHTS-3HR", "Trip View Rights Apply 3 Hours"),
	REPORT_TRIP_CHART_RIGHTS_ALL("REPORT-TRP-CRT", "TRP-VIEW-RIGHTS-APPLY", "Trip View Rights Apply"),
	REPORT_TRIP_CHART_VIEW_CANCELLED_SEATS("REPORT-TRP-CRT", "TRP-VIEW-CANCEL-SEAT", "Trip Chart View Cancelled Seats"),
	TICKET_PAYMENT_VOUCHER("TCK_PAYMENT_VOUCHER", "VOC-AUTO-GENET", "Ticket Payment Voucher"),
	BOOKING_SHOW_TICKET_INFO("TCK_SEARCH_BOOK", "PHB-SHOW-TKT-INFO", "Show Ticket Informations"),
	SEAT_VISIBILITY_EDIT_RIGHTS_USER("SCHEDULE", "SEAT-VISBLTY-USER-RIGHTS", "Seat Visibilty Edit Rights"),
	SECTOR("ACCOUNT", "USR-APLY-SECTOR", "Sector Rights"),
	TICKET_MULTIPLE_TRANSFER("ACCOUNT", "USR-MULTI-TRANSFER", "Ticket Multiple Reschedule"), 
	BOOKING_CANCEL_SAME_GROUP("", "CA-ALL-USR-TCK", " Allow Cancellation To Same Group Tickets"), 
	ALLOW_BOOKING_CANCEL_SAME_DAY("", "TCK-CA-SAME_DAY", "Allow Cancellation To Same Day Travel Date Tickets"), 
	TRANSFER_BOOKING_CANCEL("", "TCK-CA-TRANSFER", "Allow Cancellation To Transferred Tickets"),
	PRIVILEGE_DEFAULT("MENU-PRIVILEGE", "EBL-DEFAULT", "Privilege Enable Default"),
	PRIVILEGE_MAJOR("MENU-PRIVILEGE", "EBL-MAJOR", "Privilege Enable Major"),
	PRIVILEGE_CRITICAL("MENU-PRIVILEGE", "EBL-CRITICAL", "Privilege Enable Critical"),
	PRIVILEGE_BLOCKER("MENU-PRIVILEGE", "EBL-BLOCKER", "Privilege Enable Blocker"),
	NO_CANCEL_AFTER_6PM("", "NO-CA-AFTER-6PM", "No Cancel After 6 PM"),
	ALLOW_BLOCKED_SEAT_BOOKING("TCK_SEARCH_BOOK", "ALLOW-BOOK-BLOCK-SEAT", "Allow Blocked seats Booking");

	private final String name;
	private final String operationCode;
	private final String actionCode;

	private MenuEventEM(String actionCode, String operationCode, String name) {
		this.actionCode = actionCode;
		this.operationCode = operationCode;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getOperationCode() {
		return operationCode;
	}

	public String getActionCode() {
		return actionCode;
	}

	public static MenuEventEM getErrorCode(String value) {
		MenuEventEM[] values = values();
		for (MenuEventEM eventEM : values) {
			if (eventEM.getOperationCode().equalsIgnoreCase(value)) {
				return eventEM;
			}
		}
		return null;
	}
}
