package org.in.com.dto.enumeration;

import org.in.com.utils.StringUtil;

public enum NotificationTypeEM {
	CONFIRM_BOOKING("TCKBO", 1, "Confirm Booking", 1, 1, false),
	CONFIRM_CANCELLATION("TCKCA", 2, "Confirm Cancel", 2, 2, false),
	TRIP_NOTIIFICATION("TRPTN", 3, " Trip Notification", 0, 4, false),
	GPS_TRACKING("GPSTK", 4, "GPS Tracking", 4, 3, false),
	FORGET_PASSWORD("FGPWD", 5, "Forget password", 0, 0, false),
	USER_REGISTER("USRREG", 6, "User Register", 0, 0, false),
	HAPPY_JOURNEY("HPYJY", 7, " Happy Journey", 4, 3, false),
	PHONE_BOOKING("TPHBO", 8, "Phone Booked Ticket", 3, 0, false),
	TRIP_INFO("TINFO", 9, "Trip Driver Informations", 0, 0, false),
	FEEDBACK("FEDBK", 10, "Customer Feedback", 0, 0, false),
	CUSTOMER_OTP("OTP", 13, "OTP Message", 0, 0, false),
	ONLINE_RECHARGE("ONREC", 14, "Online Recharge", 0, 0, false),
	FEEDBACK_AFTER_TRAVEL_DATE("FEBK", 15, "Feedback After Travel Date", 0, 0, false),
	SEAT_VISIBILITY("SEVI",16,"Seat Visibility",0, 0, false),
	SALES_SUMMARY("SASU",17,"Sales Summary", 0, 0, false),
	BUS_BUDDY("BBDY", 18, "Bus Buddy", 5, 0, false),
	FAILIURE_TICKET_BOOKING("FLTKBO", 19, "Failure Ticket Booking Notification", 0, 0, false),
	VAN_PICKUP("VANPC", 20, "Van Pickup", 0, 0, false),
	FEEDBACK_REPLY("FBRP", 21, "Customer Feedback Reply", 0, 0, false),
	BUS_VEHICLE_CHANGE("BVCH", 22, "Bus Vehcile Change", 0, 0, false),
	NAMESPACE_EVENT_ALERT("NAEVAL", 23, "Namespace Event Alert", 0, 0, false),
	FAILIURE_TICKET("FLTCK", 24, "Failure Ticket Notification", 0, 0, false),
	TICKET_EVENT("TCKEV", 25, "Ticket Event Notification", 0, 0, false),
	TICKET_AFTER_TRIP_TIME("TATT", 26, "Ticket After Trip Time Notification", 0, 0, false),
	TICKET_UPDATE("TKUP", 27, "Ticket Update Notification", 0, 0, false),
	SCHEDULE_UPDATE("SCHUP", 28, "Schedule Update Notification", 0, 0, false),
	FAILURE_SMS("FSMS", 29, "Failure SMS Notification", 0, 0, false),
	PHONE_BOOKING_CANCEL("TPHCA", 30, "Phone Book Confirm Cancel", 6, 0, false),
	OVERALL_OCCUPANCY_SUMMARY("OACS", 31, "Overall Occupancy Summary", 0, 0, false),
	TAX_INVOICE("TAXINV", 32, "Tax Invoice", 0, 0, false),
	LINKPAY_BOOKING("LIPBO", 33, "LinkPay Ticket", 0, 0, false),
	FREE_TEMPLATE("FRTMPLT", 34, "Free Template", 0, 0, false),
	PENDING_ORDER_CANCELLATION("POCA", 35, "Pending Order Cancel", 0, 0, false),
	TICKET_RELEASE("TCKRL", 36, "Ticket Release", 0, 0, false),
	CUSTOMER_DISCOUNT("CDIS", 37, "Customer Discount", 0, 0, false),
	PAYMENT_NOTIFICATION("PYMNT", 38, "Payment Notification", 7, 0, false),
	BUS_TYPE_CHANGE_NOTIFICATION("BUSCHG", 39, "Bus Type Change Notification", 0, 0, false),
	TRIP_CANCEL_NOTIFICATION("TRPCNL", 40, "Trip Cancel Notification", 0, 0, false),
	TRIP_DELAY_NOTIFICATION("TRPDLY", 41, "Trip Delay Notification", 0, 0, false),
	TRIP_EARLY_NOTIFICATION("TRPERY", 42, "Trip Early Notification", 0, 0, false),
	TRIP_STATION_POINT_CHANGE_NOTIFICATION("TRPSPC", 43, "Trip Station Point Change Notification", 0, 0, false),
	TRIP_VAN_PICKUP("TRPVNP", 44, "Trip Van Pickup", 0, 0, false),
	FAILIURE_BOOK("FLRBO", 45, "Failure Booking Notification", 0, 0, false),
	BUS_BUDDY_ALERT("BBDAT", 46, "Bus Buddy Alert Notification", 0, 0, false),
	TABLET_OTP("TBOTP", 47, "Tablet OTP", 0, 0, false),
	WHATSAPP_VERIFICATION_NOTIFICATION("WAPV", 48, "Whatsapp Verification Notification", 0, 0, false),
	COVID_E_PASS_NOTIFICATION("EPASS", 49, "Covid Epass Notification", 0, 0, false),
	VEHICLE_NUMBER_NOTIFY_FLAG("VHNF", 50, "Vehicle Number Notify Flag", 8, 0, false),
	COMMON_NOTIFICATION("CMNFN", 51, "Common Notification", 0, 0, true),
	APOLOGY_NOTIFICATION("APLGY", 52, "Apology Notification", 0, 0, true),
	TICKET_AFTER_TRIP_TIME_CANCEL("TATCNL", 53, "Ticket After Trip Time Cancel Notification", 0, 0, false),;

	private final int id;
	private final int flagPos;
	private final String code;
	private final String description;
	private final int whatsappFlagPos;
	private final boolean isDynamic;

	
	private NotificationTypeEM(String code, int id, String description, int flagPos, int whatsappFlagPos, boolean isDynamic) {
		this.code = code;
		this.id = id;
		this.description = description;
		this.flagPos = flagPos;
		this.whatsappFlagPos = whatsappFlagPos;
		this.isDynamic = isDynamic;
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

	public int getFlagPos() {
		return flagPos;
	}
	
	public int getWhatsappFlagPos() {
		return whatsappFlagPos;
	}

	public boolean isDynamic() {
		return isDynamic;
	}
	
	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static NotificationTypeEM getNotificationTypeEM(int id) {
		NotificationTypeEM[] values = values();
		for (NotificationTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static NotificationTypeEM getNotificationTypeEM(String value) {
		NotificationTypeEM[] values = values();
		for (NotificationTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return null;
	}

	public static boolean isNotificationEnabled(String smsNotificationFlagCode, NotificationTypeEM notificationType) {
		if (StringUtil.isNotNull(smsNotificationFlagCode) && (smsNotificationFlagCode.length() == 6 || smsNotificationFlagCode.length() == 7 || smsNotificationFlagCode.length() == 8) && smsNotificationFlagCode.charAt(notificationType.getFlagPos() - 1) == '1') {
			return true;
		}
		return false;
	}

	public static int getNotificationEnabled(String smsNotificationFlagCode, NotificationTypeEM notificationType) {
		if (StringUtil.isNotNull(smsNotificationFlagCode) && (smsNotificationFlagCode.length() == 6 || smsNotificationFlagCode.length() == 7 || smsNotificationFlagCode.length() == 8) && smsNotificationFlagCode.charAt(notificationType.getFlagPos() - 1) == '1') {
			return Integer.parseInt(smsNotificationFlagCode.substring(notificationType.getFlagPos()));
		}
		return 0;
	}
	
	public static boolean isWhatsappNotificationEnabled(String whatsappNotificationFlagCode, NotificationTypeEM notificationType) {
		if (StringUtil.isNotNull(whatsappNotificationFlagCode) && (whatsappNotificationFlagCode.length() == 4) && notificationType.getWhatsappFlagPos() != 0 && whatsappNotificationFlagCode.charAt(notificationType.getWhatsappFlagPos() - 1) == '1') {
			return true;
		}
		return false;
	}
}
