package org.in.com.constants;

import java.util.regex.Pattern;

import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.utils.DateUtil;

import hirondelle.date4j.DateTime;

public class Constants {
	public static final String MYSQL_JDBC = "java:comp/env/jdbc/MySQLDB";
	public static final String MYSQL_REPORT_JDBC = "java:comp/env/jdbc/REPORTMYSQLDB";

	/* Name space Mode */
	public static String DEVICE_MEDIUM_WEB = "WEB";
	public static String DEVICE_MEDIUM_MOB = "MOB";
	public static String DEVICE_MEDIUM_APP = "APP";
	public static String NAMESPACE_STATION = "_STATION";
	public static String NAMESPACE_ROUTE = "_ROUTE";
	public static String TOP_ROUTE = "_TOP_ROUTE";
	public static String PREVIOUS_PNR_COUPEN = "PREPNR";
	public static String NAMESPACE_CARGO_ORGANIZATION = "_CARGO_ORG";

	public static String SMS_DEFAULT_PROVIDER = "INFINI";
	public static String SMS_PROVIDER_ACL = "ACL";
	public static String SMS_PROVIDER_INFINI = "INFINI";
	public static String SMS_PROVIDER_QIKBERRY = "QIKBERRY";
	public static String SMS_PROVIDER_SPARK = "SPARK";
	public static String SMS_PROVIDER_QIKBERRY_OTP = "QIKBERRYOTP";
	public static String SMS_PROVIDER_PAY4SMS = "PAY4SMS";
	public static String SMS_PROVIDER_AAKASHSMS = "AAKASHSMS";
	public static final String SMS_PROVIDER_KALEYRA = "KALEYRASMS";

	public static String WHATSAPP_PROVIDER_WIZHCOMM = "WIZHCOMM";
	public static String WHATSAPP_PROVIDER_QIKBERRY = "QIKBERY";
	public static String WHATSAPP_PROVIDER_MYOPERATOR = "MYOPERATOR";

	public static String SERVER_ENV_WINDOWS = "WIN";
	public static String SERVER_ENV_LINUX = "LIX";
	public static String[] SUPER_REGIONS_ZONE = { "bits", "devbits" };
	public static String[] REGIONS = { "bits", "r2bits", "ybmbits", "tatbits", "sbltbits", "svrtbits", "tranzkingbits", "parveenbits", "srmbits", "rajeshbits", "rmtbits", "devbits", "gotourbits" };
	public static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("\\d{10}");

	// Allow Mask mobile number only approved OTA
	public static final UserTagEM[] MASK_MOBILE_NUMBER_TAG = { UserTagEM.API_USER_EZ, UserTagEM.API_USER_RB, UserTagEM.API_USER_PT, UserTagEM.API_USER_AB };
	/** Exclude GST */
	public static final UserTagEM[] GST_EXCEPTION_TAG = { UserTagEM.API_USER_EZ, UserTagEM.API_USER_RB, UserTagEM.API_USER_PT, UserTagEM.API_USER_AB, UserTagEM.API_USER_MT };

	public static String TICKET_EVENT = ",applebus,appletravels,sgstravels,holidayappeal,yatrabustrip,itcbus,sahilbus,mehartravels,samjhauta,shubhamholidays,mountain" + Text.COMMA;
	public static String TICKET_EVENT_FAILURE_PASSENGER = ",thangamayil,ashwintravels,vairamtravels,neeveetravels,nnltravels,samantha,thangamtravels,ramstravels,wintravels,psstransport,ivartravels,srimvttravels,veekaytravels,gokultravels,suryatravelsap,iratravels,vikramtravels,royalvoyage" + Text.COMMA;
	public static String TICKET_EVENT_FAILURE_OPERATOR = ",bits,thangamayil,nagatravels" + Text.COMMA;
	public static String TICKET_EVENT_CANCEL = ",nagatravels" + Text.COMMA;
	public static String TICKET_AFTER_TRAVEL_WALLET = ",srivenkataramana" + Text.COMMA;
	public static String FEEDBACK = ",r2bits,bits,tranzking,tattravels,srisrinivasa,veekaytravels,gokultravels,ammantravels" + Text.COMMA;
	public static String MERC_SERVICE_FARE_INDEX = ",ybmtravels,rajeshtransports" + Text.COMMA;

	// Link Pay
	public static String LINK_PAY = "https://linkpay.ezeebits.com";

	// Whatsapp Link
	public static final String WHATSAPP_LINK = "https://api.whatsapp.com/send";

	// Default OTP
	public static String DEFAULT_OTP_MOBILE_NUMBER = ",9940379373" + Text.COMMA;

	// Default Fuel Cost
	public static String DEFAULT_FUEL_COST = "94";

	public static String OPEN_SEARCH_KEY = "OPENSRCH";

	/** Customer Discount Coupon */
	public static String CUSTOMER_DISCOUNT_COUPON = "CUSTCOUPON";
	public static String CUSTOMER_DISCOUNT = ",bits,demo" + Text.COMMA;

	public static final String STAGE = "STAGE";
	public static final String CANCELLATION_DATETIME = "CANCELLATION_DATETIME";

	public static final String DUPLICATE_SESSION_FLAG = "DUPLICATE_SESSION_FLAG";

	public static final String ALIAS_NAMESPACE = "ALIAS_NAMESPACE";
	// delete after this date
	public static final DateTime CGST_EFFECTIVE_DATE = DateUtil.getDateTime("2022-01-01 00:00:00");
	public static final String REPORT_DETAILS_DEFAULT_STATUS = "INITIAL";
	
	// Driver App default close Minutes
	public static int BUSBUDDY_DRIVER_TRIP_OPEN_MINUTES = 180;
	public static int BUSBUDDY_DRIVER_TRIP_CLOSE_MINUTES = 180;

}
