package org.in.com.dto.enumeration;

public enum NotificationSubscriptionTypeEM {
	AFTER_DEPARTURE_BOOKING("AFTDP", 1, "After Departure Booking", "After Departure Booking", 0),

	TICKET_NOT_BOARDED("TCKNB", 2, "Customer Not Boarded", "Travel Status", 0),

	DAILY_SALES_SUMMARY("SALES", 3, "Daily Sales Summary", "Daily Sales Summary", 0),

	FAILURE_DROPOUT_TICKET("FLRTCK", 4, "Failure / Dropout Ticket", "Dropout Ticket", 0),

	PHONE_TCIKET_CANCEL("PBCNL", 5, "Phone Ticket Cancel", "Ticket", 0),

	TICKET_CANCEL("TCKCNL", 6, "Ticket Cancel", "Ticket", 0),

	FARE_CHANGE("FRCNG", 7, "Fare Change", "Fare", 0),

	TICKET_NOT_TRAVELED("NOTTRVL", 8, "Customer Not Traveled", "Travels Status", 0),

	SERVICE_UPDATE("SRVUP", 9, "Service Open / Close / Cancel", "Service", 0),

	VEHICLE_NOT_ASSIGNED("VHNTASN", 10, "Vehicle not Assigned", "Trip", 0),

	MANUAL_TRIP_SMS("TRPSMS", 11, "Manual trip SMS", "Trip", 0),

//	SERVICE_FIRST_TICKET("FRTCK", 12, "Service First Ticket", "First Ticket"),

	ADVANCE_TRAVEL_DATE_BOOKING("ADVBO", 13, "Advance Travel Date Booking", "Advance Booking", 0),

	SCHEDULE_EDIT("SCHEDT", 14, "Schedule Edit", "Schedule", 0),

	NEW_USER_UPDATE("NEWUSR", 15, "New User Update", "User", 0),

	USER_LOGIN("USRLGN", 16, "My User Login", "Login", 1),

	USER_RESET_PASSWORD("RSTPWD", 17, "User Reset Password", "User", 0),

	USER_DELETE("USRDLT", 18, "User Delete", "User", 0),

	TICKET_BLOCK("TCKBL", 19, "Ticket Blocking", "Ticket", 0),

	TICKET_CONFIRM("TCKCNF", 20, "Ticket Confirm", "Ticket", 0),
	
	ALL_USER_LOGIN("ALUSRLN", 21, "All User Login", "Login", 0),
	
	OTP_LOGIN("OTPLGN", 22, "OTP for Login", "Login", 1),
	
	SEAT_VISIBILITY("STVSB", 23, "Seat Visibilty", "Ticket", 0),
	
	OCCUPANCY_STATUS("OCCSTS", 24, "Occupancy Status", "Trip", 0),
	
	MY_TICKET_BOOKING("MYTCK", 25, "My Tickets", "My Ticket", 1),
	
	DUPLICATE_USER_LOGIN("DUPLGN", 26, "Duplicate User Login", "Duplicate Login", 0),

	CUSTOMER_FEEDBACK("CUSFEB", 27, "Customer FeedBack", "Feedback", 0),

	SEAT_EDIT("STEDT", 28, "Seat Edit", "Ticket", 0),

	RESCHEDULE("RESHL", 29, "Ticket Reschedule", "Ticket", 0),

	TRAVEL_STATUS("TRSTS", 30, "Travel Status", "Trip", 0),
	
	VEHICLE_ASSIGNED("VHCASG", 31, "Vehicle Assigned", "Trip", 0);


	private final int id;
	private final String code;
	private final String name;
	private final String category;
	private final int level;

	private NotificationSubscriptionTypeEM(String code, int id, String name, String category, int level) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.category = category;
		this.level = level;
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
	public String getCategory() {
		return category;
	}
	
	public int getLevel() {
		return level;
	}

	public static NotificationSubscriptionTypeEM getSubscriptionTypeEM(int id) {
		NotificationSubscriptionTypeEM[] values = values();
		for (NotificationSubscriptionTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static NotificationSubscriptionTypeEM getSubscriptionTypeEM(String code) {
		NotificationSubscriptionTypeEM[] values = values();
		for (NotificationSubscriptionTypeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}
}
