package org.in.com.dto.enumeration;

public enum EventNotificationEM {
	TICKET_BOOKING("BO", 1, "Booked"),
	TICKET_CANCEL("CA", 2, "Cancelled"),
	PHONE_TICKET_CANCEL("PBC", 3, "Phone Booking Cancelled"),
	TRACKBUS_NOTIFICATION("TRNT", 4, "Tracking Notification"),
	PHONE_TICKET_BOOKING("PBL", 5, "Phone Blocked Tickets");

	private final int id;
	private final String code;
	private final String name;

	private EventNotificationEM(String code, int id, String name) {
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

	public static EventNotificationEM getNotificationEventEM(int id) {
		EventNotificationEM[] values = values();
		for (EventNotificationEM notificationEventEM : values) {
			if (notificationEventEM.getId() == id) {
				return notificationEventEM;
			}
		}
		return null;
	}

	public static EventNotificationEM getNotificationEventEM(String code) {
		EventNotificationEM[] values = values();
		for (EventNotificationEM notificationEventEM : values) {
			if (notificationEventEM.getCode().equalsIgnoreCase(code)) {
				return notificationEventEM;
			}
		}
		return null;
	}

}
