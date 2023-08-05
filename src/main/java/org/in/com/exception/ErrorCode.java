package org.in.com.exception;


public enum ErrorCode {

	/*
	 * User Exception code
	 */
	USER_INVALID_AUTH_TOKEN("100", "Invalid Auth Token"), 
	USER_INVALID_DEVICE_AUTH_TOKEN("100A", "Invalid Device Auth Token"), 
	USER_INVALID_EMAIL_PASSWORD("101", "Invalid Email and Password"), 
	USER_INVALID_EMAIL("102", "Email ID not exists"),
	USER_INVALID_USERNAME("102A", "Invalid Username"),
	USER_CUSTOMER_AUTH_FAILURE("102B", "User customer token generate failed!"),
	INVALID_DEVICE_MEDIUM("103", "Invalid Device Medium Should be WEB/APP/MOB"),
	INVALID_AUTH_TYPE("103A", "Invalid Auth Type Should be BITSUP/BITSAT/FACEBOOK/GMAIL"),
	INVALID_NAMESPACE("104", "Invalid Namespace"),
	INVALID_TRIP_CODE("105", "Invalid Trip Code"),
	INVALID_TRIP_STAGE_CODE("106", "Invalid Trip Stage Code"),
	INVALID_CODE("107", "Invalid Code"),
	INVALID_DAYOFFWEEK("107A", "Invalid Day of Week"),
	INVALID_ORDER_DETAILS("107B", "Invalid Order Details"),
	INVALID_OPERATOR("108", "Invalid Operator Code"),
	INVALID_DEVICE_CODE("109", "Invalid Device Code"),
	INVALID_USER_CODE("109A", "Invalid User Code"),
	CODE_INVALID("200", "Code not in Cache Memory"),
	UPDATE_FAIL("201", "Update fail"),
	MISMATCH_PASSWORD("202", "Current password is not matched"),
	MISMATCH_SEATCODENAME("202A", "Seat code and name are mismatch"),
	MISMATCH_ROLE("203", "Invalid Role/not Null"),
	MISMATCH_TOKEN("204", "Invalid given user token"),
	INVALID_STATION("205", "Invalid Station"),
	INVALID_ORGANIZATION("205A", "Invalid organization, not found"),
	INVALID_STATE("205B", "Invalid State"),
	INVALID_APPLICATION_ZONE("206", "Invalid Application zone"),
	INVALID_GSTIN("207", "Invalid GST Identification Number"),
	INVALID_GROUP("208", "Invalid Group, Group not found"),
	NAME_SHOULD_NOT_NULL("300", "Name Should not be null.."),
	STATION_POINT_CODE_INVALID("301", "Station Point Code not found"),
	REQURIED_FIELD_SHOULD_NOT_NULL("302", "Name Should not be null.."),
	REQURIED_SCHEDULE_DATA("304", "Some requried schedule data are not updated"),
	REQURIED_SCHEDULE_CODE("304A", "schedule code is requried"),
	SCHEDULE_NOT_ACTIVE("304B", "schedule is not active"),
	TRIP_STATGE_CODE("305", "Invalid TripStageCode"),
	TRIP_CODE("305A", "Invalid Trip Code"),
	STATION_POINT("306", "Invalid Boarding/Dropping Points"),
	SEAT_NOT_AVAILABLE("307", "Seat Not available"),
	SEAT_NOT_ALLOWED_ABOVE_20("307A", "More than 20 seats Not allowed"),
	SELECTED_STEAT_NOT_FOR_RESERVATION("308", "Invalid seat, please select reservable seat"),
	SELECTED_SEAT_BLOCK_TIME_OVER("309", "Block Time Over, please try block seat"),
	TRIP_DATE_OVER("309A", "Expired travel date"),
	SELECTED_STEAT_FARE_ZERO("310", "Selected seat fare is Zero"),
	TRANSACTION_AMOUNT_EXCEED("311", "Transaction amount exceed on crore"),
	TRANSACTION_AMOUNT_INVALID("311A", "Transaction amount should be valid"),
	STATION_POINT_USED_SCHEDULE("312", "can not Delete!,Station point used in schedule"),
	STATION_USED_SCHEDULE("313", "can not Delete!,Station used in schedule"),
	SCHEDULE_STATION_USED_TICKET("313A", "Can not Delete!, Ticket booked in this station"),
	VAN_PICKUP_STATION_POINT_USED_SCHEDULE("313B", "Can not delete!, van pickup station point used in schedule"),
	CANCELLATION_USED_SCHEDULE("314", "can not Delete!,Cancellation Term used in schedule"),
	BUSMAP_USED_SCHEDULE("315", "can not Delete!,Busmap used in schedule"),
	SCHEDULE_BUSMAP_USED_TICKET("315A", "can not Change bus type!, Ticket booked in this bus type"),
	MAX_SEAT_PER_TRANSACTION("316", "Max seat per transaction exceed"),
	MAX_ADVANCE_BOOKING_DAYS("317", "Given request is behind advance booking days"),
	ROUTE_NOT_FOUND("318", "route not found"),
	PREPOSTPONE_NOT_ALLOWED("319", "Prepond/Postpond not allowed to this PNR"),
	PREPOSTPONE_NOT_ALLOWED_TO_GROUP("320", "Prepond/Postpond not allowed to the channel"),
	OTP_ALREADY_SENT("321", "OTP code sent to your mobile number, please wait 1 minutes"),
	BUSMAP_MISSED_MATCHED("322", "Busmap Type is mis-matched in schedule"),
	ALREADY_REGISTERED_MOBILE("323", "Mobile is already registered"),
	UNABLE_PROCESS("324", "unable to process your request, please try after some time!"),
	ROUTE_FARE_OUT_OF_RANGE("325", "Route Fare Out Of Range"),
	NOTIFICATION_SUBSCRIPTION_NOT_ENABLED("326", "This Notification Subscription not enabled"),
	NOTIFICATION_CONFIG_NOT_FOUND("327", "Notification config not found"),
	NO_PROVISION_TO_SEND_NOTIFICATION("328", "No provision to send notification"),
	POLICY_TIME_CONTINUITY_MISMATCHED("329", "Policy time continuity mismatched"),
	MIN_THREE_POLICY_ALLOWED("330", "Min 3 policy should be allowed"),
	DRIVER_NOT_FOUND("331", "Driver Not found"),

	/**Payment gateway error codes*/
	
	TRANSACTION_ENQUIRY_NOT_AVAILABLE("PG01","This PG does not support enquiry API call"),
	AUTO_REFUND_IMPLEMENTATION_NOT_AVAILABLE("PG02","Auto Refund API is not implemented for this PG"),
	TRANSACTION_ALREADY_INITIATED("PG03","ALREADY_INITIATED"),
	API_TICKET_CODE("AE01","API Reference Ticket Number is required"),
	NO_GATEWAY_FOUND("PG04","Requested gateway not found"),
	SUCCESS("PG05","Success"),
	MISMATCH_IN_TRANSACTION_AMOUNT("PG06","Mismatch in transaction amount"),
	INVALID_TRANSACTION_ID("PG07","Invalid transaction id"),
	PAYMENT_DECLINED("PG08","Payment declined"),
	INVALID_TICKET_ID("PG10","Invalid Ticket ID"),
	TRANSACTION_ALREADY_SUCCESS("PG11","Your Transaction Already Success"),
	TRANSACTION_REACHED_MAX_RETRY("PG12","Your Transaction Reached Max Retry"),
	TRANSACTION_RETRY_TIMEOUT("PG13","Your Transaction Retry Timeout"),
	MANDATORY_PARAMETERS_MISSING("PG14","Mandatory parameters are missing"),
	INVALID_CREDENTIALS("PG15","Invalid credentials"),
	INVALID_USERNAME("PG15A","Invalid Username"),
	INVALID_REFUND_AMOUNT("PG16","Invalid refund amount"),
	TRANSACTION_CANCELLED_BY_USER("PG17","Transaction Cancelled by User"),
	PAYMENT_GATEWAY_TRANSACTION_FAIL_NOT_COMPLETED("PG18","Payment failure"),
	PAYMENT_GATEWAY_TRANSACTION_AMOUNT_TICKET_AMOUNT_MISMATCH("PG19","Transaction amount differ from Ticket amount"),
	LOW_AVAILABLE_BALANCE("PG20","User don't have sufficient balance"),
	UNABLE_TO_CONFIRM_TICKET("PG21","Unable to confirm Ticket"),
	TRANSACTION_ALREADY_ACK("PG22","Your Transaction Already Ack"),
	TRANSACTION_INVALID_ACK("PG23","Your Transaction Invalid Ack"),

	/**DAO Error codes*/
	NOT_NULL_DATA_FOR_PERSITS("DA01","Invalid data for persists"),
	
	/** Trip Error Codes*/
	TRIP_CLOSED_NOT_ALLOW_BOOKING("TR01","Trip has been closed, not allow booking"),
	TRIP_STAGE_CLOSED_NOT_ALLOW_BOOKING("TR04","Trip stage has been closed, not allow booking"),
	TRIP_YET_OPEN_NOT_ALLOW_BOOKING("TR03","Trip yet open, not allow booking"),
	TRIP_NOT_AVAILABLE("TR02","Trip has been closed, not allow booking"),
	TRIP_CANCELLED("TR05","Trip has been cancelled"),
	TRIP_INVALID("TR06","Invalid Trip status"),
	TRIP_INFO_INVALID("TR07","Trip information not available"),
	TRIP_DATA_CHANGED("TR08","Trip data has Changed"),
	TRIP_NOTIFICATION_ALREADY_SENT("TR09","Trip notification already sent"),

	/** Booking Error Codes*/
	SEAT_ALREADY_BLOCKED("BO01","Selected seat(s) already blocked, please select other seat(s)"),
	SEAT_ALREADY_BLOOKED("BO02","Selected seat(s) already confirm booked ticket"),
	SEAT_ALREADY_CANCELLED("BO03","Selected seat(s) already cancelled"),
	PHONE_BOOKED_TICKET("BO04","Phone booked ticket, Can not confirm"),
	PHONE_BOOK_TICKET_NOT_ALLOW("BO04A","Phone booking not allowed at this time"),
	BOOK_TICKET_NOT_ALLOW_LIMITS("BO04B","Ticket booking not allowed, beyond limits"),
	NOT_CONFIRM_BOOKED_TICKET("BO05","Not confirmed Tickets"),
 	NOT_PHONE_BOOKED_TICKET("BO06","Confirmed ticket, Not cancel in phone booking cancel"),
	CANCELLATION_TERM_NOT_FOUND_BOOKING_NOT_ALLOW("BO07","Booking not allowed, Cancellation term not found"),
	ALREADY_CONFIRM_BOOKED_TICKET("BO08","Given ticket is already confirmed"),
	FREE_TICKET_NOT_ALLOWED("BO09","Free ticket is not allow on phone book"),
	TRANSFERED_TICKET_NOT_ALLOW_CANCEL("BO10","Transfered ticket is not allowed cancel"),
	TENTATIVE_BLOCK_TICKET_ALREADY_RELEASED("BO11","Ticket already released"),
	TENTATIVE_BLOCK_TICKET_ALREADY_CONFIRMED ("BO12","Ticket already confirmed, unable to release"),
	OTP_REQUIRED_TRANSCTION("BO13","OTP is Required on this Transaction"),
	/**Pending order*/
	PENDING_ORDER_CONFIRMATION_SEAT_COUNT_MISMATCH("PO01","Order not confirmed, Seat count mismatch"),
/** Cancellation Related*/
	CANCELLATION_TERMS_NOT_FOUND("CA01","Cancellation Terms not found"),
	PASSENGER_DETAILS_IS_EMPTY("400", "Passenger details is empty"),
	INVALID_EMAIL_ID("401", "Invalid email id"),
	INVALID_EMAIL_DOMAIN("401A", "Invalid email domain"),
	INVALID_MOBLIE_NUMBER("402", "Invalid mobile number"),
	INVALID_PASSENGER_NAME("403", "Invalid passender name or empty"),
	INVALID_PASSENGER_AGE("404", "Invalid Age"),
	INVALID_PASSENGER_GENDER("405", "Invalid Gender"),
	INVALID_PASSENGER_GENDER_PREFERENCE("405A", "Invalid Gender for Preferenced Seat gender"),
	PASSENGER_GENDER_RESTRICT("405B", "Gender restricted for selected seat, please select other available seat(s) based on gender"),
	INVALID_SEAT_CODE("406", "Invalid Seat code"),
	INVALID_SEAT_NAME("406B", "Invalid Seat name"),
	INVALID_SEAT_FARE("406C", "Invalid Seat fare"),
	DUPLICATE_SEAT_CODE("406A", "Duplicate Seat code"),
	UNDEFINE_EXCEPTION("500","Un Known Exception"),
	INVALID_TICKET_CODE("501","Invalid ticket code"),
	UNABLE_TO_BLOCK_TICKET("502","Unable to Block Ticket"),
	UNABLE_TO_TRANSFER_TICKET("502A","Unable to Transfer Ticket"),
	INVALID_DATE("503","Invalid Date"),
	INVALID_DATE_RANGE("504","Invalid date Range"),
	INVALID_DISCOUNT_CODE("505","Invalid discount code"),
	DISCOUNT_CODE_USED_ALREDY("505A","PNR used already!"),
	INVALID_VEHICLE_CODE("506","Invalid vehicle not found"),
	INVALID_TRIP_SEAT_QUOTA("507","Invalid Trip seat quota"),
	TRANSACTION_FAIL_BALANCE_ISSUES("610","Transaction is fail due to invalid balance"),
	DOUBLE_ENTRY_VALIDATION_FAIL("611","Identifed Seat double entry, Please retry the transaction"),
	DOUPLICATE_ENTRY_VALIDATION_FAIL("612","Identifed ticket code is already exists"),
	UNABLE_TO_GENERATE_PDF("601","Unable to Generate PDF"),
	UNABLE_TO_RESET_PASSWORD("601A","Unable to Reset Password"),
	UNABLE_TO_GENERATE_VOUCHER("602","Unable to Generate Voucher"),
	UNABLE_TO_GENERATE_SEQUENCE("602A","Unable to Generate Sequence"),
	UNABLE_TO_UPDATE("602","Unable to update the details"),
	UNABLE_TO_PROVIDE_DATA("603","Unable to provide data"),
	PARALLEL_SAME_TRANSACTION_OCCUR("TR01A","Parallel same transaction occurrence identified, not a fresh transaction!"),
	OTP_TRANSACTION_COUNT_EXCEED("TR01B","OTP exceeds the allowable limit!"),
	TRANSACTION_INPROGRESS("TR02","Transaction In Progress"),
	INVAID_IMAGE_CATEGORY("604","Invalid Image Category !"),

	/** Cancellation Flow*/
	CANCELLATION_VERIFICATION_USER_FAIL("600","Permission denied to cancel, this ticket booked by office/agent"),
	CANCELLATION_VERIFICATION_EMAIL_FAIL("600A","PNR and Email address does not matched"),
	CANCELLATION_VERIFICATION_MOBILE_FAIL("600B","PNR and mobile number does not matched"),
	CANCELLATION_TIME_OVER("601","Cancellation time over"),
	CANCELLATION_NOT_ALLOWED("602","Cancellation not allowed"),
	INVAILD_TRANSACTION_OTP("603","Invalid OTP code, Please enter correct code"),
	REACHED_MAX_CONCURRENT_REQUESTS("800","User reached Max Concurrent Request"),
	
	/** GPS */
	
	GPS_TRIP_TRACKING_NOT_ALLOWED("GP01","Trip tracking not allowed, time over"),
	GPS_TRIP_JOURNEY_NOT_STARTED("GP02","Your trip tracking not allowed now, Please try after "),
	GPS_TRIP_JOURNEY_COMPLETED("GER03","Trip journey completed"),
	GPS_VENDOR_NOT_FOUND("GER04","Vendor is not found"),
	GPS_DEVICE_LOCATION_NOT_FOUND("GER05","Device communication error"),
	GPS_NO_SCHEDULE_NOT_FOUND("GER06","Schedule(s) not enalbed"),
	GPS_VECHILE_NOT_ALLOCATED("GER07","Vechile not allocated"),
	GPS_TRIP_CHART_SHARING_NOT_ALLOWED("GER07A","Requested data is not available"),
	GPS_TRIP_TRACKING_TIME_NOT_IN_PERIOD("GER08","Send tracking time not with in time period"),

	UNAUTHORIZED("901","unauthorized access"),
	
	SAME_GST_IS_EXIST("902","Same GST is Exist"),
	REQUEST_ALREADY_PROCESSED("903", "Request Already Processed"), 
	REQUIRED_PARAMAETER_CANNOT_BE_NULL("904","Required Paramter Can't be Null");
	
	
	private final String code;
	private final String message;

	private ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	public String getCode() {
		return code;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	
	public String toString() {
		return code + ": " + message;
	}

	public static ErrorCode getErrorCode(String value) {
		ErrorCode[] values = values();
		for (ErrorCode errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return null;
	}
}
