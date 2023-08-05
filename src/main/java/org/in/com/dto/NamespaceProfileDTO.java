package org.in.com.dto;

import java.util.List;
import java.util.Map;

import org.in.com.aggregator.sms.SMSProviderEM;
import org.in.com.aggregator.whatsapp.WhatsappProviderEM;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.PNRGenerateTypeEM;
import org.in.com.dto.enumeration.SeatGenderRestrictionEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceProfileDTO extends BaseDTO<NamespaceProfileDTO> {
	private boolean cancellationCommissionRevokeFlag;
	// Add Tax to Cancellation charge amount
	private boolean cancellationChargeTaxFlag;
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
	// booking/cancel/phoneBook/boardingPoint/GPS/SALES
	private String smsNotificationFlagCode;
	private String pnrStartCode;
	private PNRGenerateTypeEM pnrGenerateType;
	private String dateFormat;
	private String sendarSMSName;
	private String sendarMailName;
	private SMSProviderEM smsProvider;
	private String emailCopyAddress;
	private WhatsappProviderEM whatsappProvider;
	private String whatsappSenderName;
	private String domainURL;
	private int phoneBookingTicketNotificationMinitues;
	private int boardingReportingMinitues;
	private int instantCancellationMinitues;
	private String cancellationTimeType;
	private int phoneBookingCancellationBlockMinutes;
	private int travelStatusOpenMinutes;
	private int allowDirectLogin;
	private SeatGenderRestrictionEM seatGendarRestriction;
	private List<DynamicPriceProviderEM> dynamicPriceProviders;
	private List<FareRuleDTO> fareRule;
	private StateDTO state;
	private List<UserDTO> allowApiTripInfo;
	private List<UserDTO> allowApiTicketTransfer;
	private List<UserDTO> allowApiTripChart;
	private List<UserDTO> allowApiTripChartAllPnr;
	private List<UserDTO> cancellationChargeTaxException;
	private String address;
	private String city;
	private String pincode;
	private String supportNumber;
	private String mobileNumberMask;
	private int noFareSMSFlag;
	private List<NotificationSubscriptionTypeEM> subscriptionTypes;
	private List<GroupDTO> otpVerifyGroup;
	private List<GroupDTO> expirePasswordGroup;
	private List<GroupDTO> fareRuleExceptionGroup;
	private List<GroupDTO> instantCancellationGroup;
	private List<GroupDTO> gstExceptionGroup;
	private int paymentReceiptAcknowledgeProcess;
	private Map<String, String> otaPartnerCode;
	private int trackbusMinutes;
	private String whatsappNumber;
	private String whatsappUrl;
	private String whatsappDatetime;
	private List<String> ticketEventNotificationContact;
	private List<String> ticketAfterTripTimeNotificationContact;
	private List<String> tripNotificationContact;
	private int rescheduleOverrideAllowDays;
	private int expirePasswordDays;
	private int ticketRescheduleMaxCount;
	private int searchPastDayCount;
	private boolean rechargeAutoApprovalFlag;
	private String job;

	public String getApiTripChartAllPnrUserIds() {
		String userIds = Text.NA;
		if (allowApiTripChartAllPnr != null) {
			StringBuilder user = new StringBuilder();
			for (UserDTO userDTO : allowApiTripChartAllPnr) {
				if (userDTO.getId() == 0) {
					continue;
				}
				user.append(userDTO.getId());
				user.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(user)) {
				userIds = user.toString();
			}
		}
		return userIds;
	}

	public String getApiTripInfoUserIds() {
		String userIds = Text.NA;
		if (allowApiTripInfo != null) {
			StringBuilder user = new StringBuilder();
			for (UserDTO userDTO : allowApiTripInfo) {
				if (userDTO.getId() == 0) {
					continue;
				}
				user.append(userDTO.getId());
				user.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(user)) {
				userIds = user.toString();
			}
		}
		return userIds;
	}

	public String getApiTicketTransferUserIds() {
		String userIds = Text.NA;
		if (allowApiTicketTransfer != null) {
			StringBuilder user = new StringBuilder();
			for (UserDTO userDTO : allowApiTicketTransfer) {
				if (userDTO.getId() == 0) {
					continue;
				}
				user.append(userDTO.getId());
				user.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(user)) {
				userIds = user.toString();
			}
		}
		return userIds;
	}

	public String getApiTripChartUserIds() {
		String userIds = Text.NA;
		if (allowApiTripChart != null) {
			StringBuilder user = new StringBuilder();
			for (UserDTO userDTO : allowApiTripChart) {
				if (userDTO.getId() == 0) {
					continue;
				}
				user.append(userDTO.getId());
				user.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(user)) {
				userIds = user.toString();
			}
		}
		return userIds;
	}

	public String getCancellationChargeTaxExceptionUserIds() {
		String userIds = Text.NA;
		if (cancellationChargeTaxException != null) {
			StringBuilder user = new StringBuilder();
			for (UserDTO userDTO : cancellationChargeTaxException) {
				if (userDTO.getId() == 0) {
					continue;
				}
				user.append(userDTO.getId());
				user.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(user)) {
				userIds = user.toString();
			}
		}
		return userIds;
	}

	public boolean isMobileNumberMaskingEnabled() {
		boolean isEnable = Text.FALSE;
		if (StringUtil.isNotNull(mobileNumberMask)) {
			char allowMobileNumberMasking = mobileNumberMask.charAt(0);
			if (allowMobileNumberMasking == '1') {
				isEnable = Text.TRUE;
			}
		}
		return isEnable;
	}

	public boolean isNoFareSMSFlag() {
		boolean isNoFareFlag = Text.FALSE;
		if (noFareSMSFlag == 1) {
			isNoFareFlag = Text.TRUE;
		}
		return isNoFareFlag;
	}

	public boolean isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM subscriptionTypeEM) {
		if (subscriptionTypes != null) {
			for (NotificationSubscriptionTypeEM notificationSubscription : subscriptionTypes) {
				if (notificationSubscription.getCode().equals(subscriptionTypeEM.getCode())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isOtpVerifyGroupEnabled(GroupDTO userGroup) {
		if (otpVerifyGroup != null && userGroup != null) {
			for (GroupDTO groupDTO : otpVerifyGroup) {
				if (groupDTO.getId() == userGroup.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isExpirePasswordGroupEnabled(GroupDTO userGroup) {
		if (expirePasswordGroup != null && userGroup != null) {
			for (GroupDTO groupDTO : expirePasswordGroup) {
				if (groupDTO.getId() == userGroup.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isFareRuleExceptionGroupEnabled(List<GroupDTO> userGroup) {
		if (fareRuleExceptionGroup != null && userGroup != null && !fareRuleExceptionGroup.isEmpty()) {
			for (GroupDTO group : userGroup) {
				for (GroupDTO groupDTO : fareRuleExceptionGroup) {
					if (groupDTO.getId() == group.getId()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean isGstExceptionGroup(GroupDTO userGroup) {
		if (gstExceptionGroup != null && userGroup != null && !gstExceptionGroup.isEmpty()) {
			for (GroupDTO exceptionGroup : gstExceptionGroup) {
				if (exceptionGroup.getId() == userGroup.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public String getDynamicPriceProvidersIds() {
		StringBuilder dynamicPriceProviderIds = new StringBuilder();
		if (dynamicPriceProviders != null && !dynamicPriceProviders.isEmpty()) {
			for (DynamicPriceProviderEM dynamicPriceProviderEM : dynamicPriceProviders) {
				if (StringUtil.isNotNull(dynamicPriceProviderIds)) {
					dynamicPriceProviderIds.append(Text.COMMA);
				}
				dynamicPriceProviderIds.append(dynamicPriceProviderEM.getId());
			}
		}
		else {
			dynamicPriceProviderIds.append(Text.NA);
		}
		return dynamicPriceProviderIds.toString();
	}

	public boolean isPhoneBlockReleaseConfirmJobEnabled() {
		return StringUtil.isNotNull(job) && job.charAt(Numeric.ZERO_INT) == '1' ? Text.TRUE : Text.FALSE;
	}
	
	public boolean isNotificationToAlternateMobile(NotificationMediumEM medium) {
		if (StringUtil.isNotNull(notificationToAlternateMobileFlagCode) && notificationToAlternateMobileFlagCode.charAt(0) == '1' && medium.getId() == NotificationMediumEM.SMS.getId()) {
			return true;
		}
		if (StringUtil.isNotNull(notificationToAlternateMobileFlagCode) && notificationToAlternateMobileFlagCode.length() > 1 && notificationToAlternateMobileFlagCode.charAt(1) == '1' && medium.getId() == NotificationMediumEM.WHATS_APP.getId()) {
			return true;
		}
		return false;
	}
}
