package org.in.com.controller.web.io;

import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;

import lombok.Data;

@Data
public class NamespaceProfileIO {
	private boolean cancellationCommissionRevokeFlag;
	private boolean cancellationChargeTaxFlag;
	private String smsNotificationFlagCode;
	private boolean emailNotificationFlag;
	private boolean allowExtraCommissionFlag;
	private boolean isAliasNamespaceFlag;
	private String whatsappNotificationFlagCode;
	private String notificationToAlternateMobileFlagCode;
	private int maxSeatPerTransaction;
	private int seatBlockTime;
	private int timeFormat;
	private int advanceBookingDays;
	private int reportingDays;
	private String pnrStartCode;
	private String pnrGenerateTypeCode;
	private String dateFormate;
	private String sendarMailName;
	private String sendarSMSName;
	private String smsProviderCode;
	private String emailCopyAddress;
	private String whatsappProviderCode;
	private String whatsappSenderName;
	private String domainURL;
	private int phoneBookingTicketNotificationMinitues;
	private int boardingReportingMinitues;
	private int instantCancellationMinitues;
	private int travelStatusOpenMinutes;
	private String cancellationTimeType;
	private boolean droppingPointRequriedFlag = true; // while on booking
	private BaseIO seatGendarRestriction;
	private List<FareRuleIO> fareRule;
	private List<BaseIO> dynamicPriceProvider;
	private StateIO state;
	private List<BaseIO> allowApiTripInfo;
	private List<BaseIO> allowApiTicketTransfer;
	private List<BaseIO> allowApiTripChart;
	private List<BaseIO> allowApiTripChartAllPnr;
	private List<BaseIO> cancellationChargeTaxException;
	private String address;
	private String city;
	private String pincode;
	private String supportNumber;
	private String mobileNumberMask;
	private int noFareSMSFlag;
	private List<GroupIO> otpVerifyGroup;
	private List<GroupIO> expirePasswordGroup;
	private List<GroupIO> fareRuleExceptionGroup;
	private List<GroupIO> instantCancellationGroup;
	private List<GroupIO> gstExceptionGroup;
	private int paymentReceiptAcknowledgeProcess;
	private Map<String, String> otaPartnerCode;
	private int trackbusMinutes;
	private String whatsappNumber;
	private String whatsappUrl;
	private String whatsappDatetime;
	private List<String> ticketEventNotificationContact;
	private List<String> ticketAfterTripTimeNotificationContact;
	private List<String> tripNotificationContact;
	private boolean isOtpVerifyGroupEnabled;
	private boolean isExpirePasswordGroupEnabled;
	private int rescheduleOverrideAllowDays;
	private int expirePasswordDays;
	private int ticketRescheduleMaxCount;
	private int searchPastDayCount;
	private int allowDirectLogin = Numeric.ZERO_INT;
	private boolean rechargeAutoApprovalFlag;
	private String job;
}
