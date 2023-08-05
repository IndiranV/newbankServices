package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.aggregator.sms.SMSProviderEM;
import org.in.com.aggregator.whatsapp.WhatsappProviderEM;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.FareRuleIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NamespaceIO;
import org.in.com.controller.web.io.NamespaceProfileIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/namespace")
public class NamespaceController extends BaseController {
	@Autowired
	NamespaceService namespaceService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceIO>> getAllNamespace(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<NamespaceIO> namespaceIOList = new ArrayList<NamespaceIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<NamespaceDTO> namespaceList = (List<NamespaceDTO>) namespaceService.getAll(authDTO);
			for (NamespaceDTO dto : namespaceList) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				NamespaceIO namespaceIO = new NamespaceIO();
				namespaceIO.setCode(dto.getCode());
				namespaceIO.setName(dto.getName());
				namespaceIO.setActiveFlag(dto.getActiveFlag());

				NamespaceProfileIO profile = new NamespaceProfileIO();
				StateIO state = new StateIO();

				state.setCode(dto.getProfile().getState().getCode());
				state.setName(dto.getProfile().getState().getName());
				profile.setCity(dto.getProfile().getCity());
				profile.setState(state);
				namespaceIO.setNamespaceProfile(profile);
				namespaceIOList.add(namespaceIO);
			}
			// Sorting
			Comparator<NamespaceIO> comp = new BeanComparator("name");
			Collections.sort(namespaceIOList, comp);

		}
		return ResponseIO.success(namespaceIOList);
	}

	@RequestMapping(value = "/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceIO>> getNamespace(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		ResponseIO<List<NamespaceIO>> io = new ResponseIO<List<NamespaceIO>>();

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<NamespaceDTO> namespaceList = (List<NamespaceDTO>) namespaceService.getAll(authDTO);
			List<NamespaceIO> namespaceIOList = new ArrayList<NamespaceIO>();
			for (NamespaceDTO dto : namespaceList) {
				NamespaceIO namespaceIO = new NamespaceIO();
				namespaceIO.setCode(dto.getCode());
				namespaceIO.setName(dto.getName());
				namespaceIO.setActiveFlag(dto.getActiveFlag());
				namespaceIOList.add(namespaceIO);
			}
			io.setStatus(1);
			io.setData(namespaceIOList);
		}
		return io;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceIO> getNameSpaceUID(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceIO namespaceIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceIO.getCode().toLowerCase());
			namespaceDTO.setName(namespaceIO.getName());
			namespaceDTO.setActiveFlag(namespaceIO.getActiveFlag());
			namespaceService.Update(authDTO, namespaceDTO);
			namespaceIO.setCode(namespaceDTO.getCode());
			namespaceIO.setActiveFlag(namespaceDTO.getActiveFlag());
		}
		return ResponseIO.success(namespaceIO);
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceProfileIO> getNamespaceProfile(@PathVariable("authtoken") String authtoken) throws Exception {
		NamespaceProfileIO profileIO = new NamespaceProfileIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceProfileDTO profileDTO = namespaceService.getProfile(authDTO);
		profileIO.setBoardingReportingMinitues(profileDTO.getBoardingReportingMinitues());
		profileIO.setCancellationCommissionRevokeFlag(profileDTO.isCancellationCommissionRevokeFlag());
		profileIO.setCancellationChargeTaxFlag(profileDTO.isCancellationChargeTaxFlag());
		profileIO.setDateFormate(profileDTO.getDateFormat());
		profileIO.setDomainURL(profileDTO.getDomainURL());
		profileIO.setEmailNotificationFlag(profileDTO.isEmailNotificationFlag());
		profileIO.setMaxSeatPerTransaction(profileDTO.getMaxSeatPerTransaction());
		profileIO.setAliasNamespaceFlag(profileDTO.isAliasNamespaceFlag());
		profileIO.setAdvanceBookingDays(profileDTO.getAdvanceBookingDays());
		profileIO.setReportingDays(profileDTO.getReportingDays());
		profileIO.setPhoneBookingTicketNotificationMinitues(profileDTO.getPhoneBookingTicketNotificationMinitues());
		profileIO.setTrackbusMinutes(profileDTO.getTrackbusMinutes());
		profileIO.setTimeFormat(profileDTO.getTimeFormat());
		profileIO.setPnrStartCode(profileDTO.getPnrStartCode());
		profileIO.setPnrGenerateTypeCode(profileDTO.getPnrGenerateType().getCode());
		profileIO.setSeatBlockTime(profileDTO.getSeatBlockTime());
		profileIO.setSendarMailName(profileDTO.getSendarMailName());
		profileIO.setSendarSMSName(profileDTO.getSendarSMSName());
		profileIO.setSmsProviderCode(profileDTO.getSmsProvider().getCode());
		profileIO.setNoFareSMSFlag(profileDTO.getNoFareSMSFlag());
		profileIO.setSmsNotificationFlagCode(profileDTO.getSmsNotificationFlagCode());
		profileIO.setNotificationToAlternateMobileFlagCode(profileDTO.getNotificationToAlternateMobileFlagCode());
		profileIO.setEmailCopyAddress(profileDTO.getEmailCopyAddress());
		profileIO.setInstantCancellationMinitues(profileDTO.getInstantCancellationMinitues());
		profileIO.setTravelStatusOpenMinutes(profileDTO.getTravelStatusOpenMinutes());
		profileIO.setCancellationTimeType(profileDTO.getCancellationTimeType());
		profileIO.setAllowExtraCommissionFlag(profileDTO.isAllowExtraCommissionFlag());
		profileIO.setMobileNumberMask(profileDTO.getMobileNumberMask());
		profileIO.setWhatsappNotificationFlagCode(profileDTO.getWhatsappNotificationFlagCode());
		profileIO.setWhatsappProviderCode(profileDTO.getWhatsappProvider().getCode());
		profileIO.setWhatsappSenderName(profileDTO.getWhatsappSenderName());
		profileIO.setWhatsappNumber(profileDTO.getWhatsappNumber());
		profileIO.setWhatsappUrl(profileDTO.getWhatsappUrl());
		profileIO.setWhatsappDatetime(profileDTO.getWhatsappDatetime());
		profileIO.setTicketRescheduleMaxCount(profileDTO.getTicketRescheduleMaxCount());
		profileIO.setSearchPastDayCount(profileDTO.getSearchPastDayCount());
		profileIO.setAllowDirectLogin(profileDTO.getAllowDirectLogin());

		List<String> tickEventContacts = new ArrayList<>();
		List<String> tickAfterTripTimeContacts = new ArrayList<>();
		List<String> tripNotificationContacts = new ArrayList<>();

		for (String mobileNumber : profileDTO.getTicketEventNotificationContact()) {
			tickEventContacts.add(mobileNumber);
		}

		for (String mobileNumber : profileDTO.getTicketAfterTripTimeNotificationContact()) {
			tickAfterTripTimeContacts.add(mobileNumber);
		}

		for (String mobileNumber : profileDTO.getTripNotificationContact()) {
			tripNotificationContacts.add(mobileNumber);
		}

		profileIO.setTicketEventNotificationContact(tickEventContacts);
		profileIO.setTicketAfterTripTimeNotificationContact(tickAfterTripTimeContacts);
		profileIO.setTripNotificationContact(tripNotificationContacts);

		StateIO state = new StateIO();
		state.setCode(profileDTO.getState().getCode());
		state.setName(profileDTO.getState().getName());
		profileIO.setState(state);

		List<FareRuleIO> fareRuleList = new ArrayList<>();
		for (FareRuleDTO fareRuleDTO : profileDTO.getFareRule()) {
			FareRuleIO fareRule = new FareRuleIO();
			fareRule.setCode(fareRuleDTO.getCode());
			fareRule.setName(fareRuleDTO.getName());
			fareRuleList.add(fareRule);
		}
		profileIO.setFareRule(fareRuleList);

		BaseIO seatGendarRestriction = new BaseIO();
		seatGendarRestriction.setCode(profileDTO.getSeatGendarRestriction().getCode());
		seatGendarRestriction.setName(profileDTO.getSeatGendarRestriction().getName());
		profileIO.setSeatGendarRestriction(seatGendarRestriction);

		List<BaseIO> dynamicPriceProviders = new ArrayList<>();
		for (DynamicPriceProviderEM dynamicPriceProviderEM : profileDTO.getDynamicPriceProviders()) {
			BaseIO dynamicPriceProvider = new BaseIO();
			dynamicPriceProvider.setCode(dynamicPriceProviderEM.getCode());
			dynamicPriceProvider.setName(dynamicPriceProviderEM.getName());
			dynamicPriceProviders.add(dynamicPriceProvider);
		}
		profileIO.setDynamicPriceProvider(dynamicPriceProviders);

		List<BaseIO> apiTripChartAllPnrUsers = new ArrayList<>();
		List<BaseIO> apiTripInfoUsers = new ArrayList<>();
		List<BaseIO> apiTicketTransferUsers = new ArrayList<>();
		List<BaseIO> apiTripChartUsers = new ArrayList<>();

		for (UserDTO userDTO : profileDTO.getAllowApiTripChartAllPnr()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTripChartAllPnrUsers.add(userio);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripInfo()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTripInfoUsers.add(userio);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTicketTransfer()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTicketTransferUsers.add(userio);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripChart()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTripChartUsers.add(userio);
		}
		profileIO.setAllowApiTripChartAllPnr(apiTripChartAllPnrUsers);
		profileIO.setAllowApiTripInfo(apiTripInfoUsers);
		profileIO.setAllowApiTicketTransfer(apiTicketTransferUsers);
		profileIO.setAllowApiTripChart(apiTripChartUsers);

		List<BaseIO> cancellationChargeTaxExceptionUsers = new ArrayList<>();

		for (UserDTO userDTO : profileDTO.getCancellationChargeTaxException()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			cancellationChargeTaxExceptionUsers.add(userio);
		}
		profileIO.setCancellationChargeTaxException(cancellationChargeTaxExceptionUsers);

		List<GroupIO> otpVerifyGroups = new ArrayList<GroupIO>();
		List<GroupIO> expirePasswordGroups = new ArrayList<GroupIO>();
		List<GroupIO> fareRuleExceptionGroups = new ArrayList<GroupIO>();
		List<GroupIO> instantCancellationGroup = new ArrayList<GroupIO>();
		List<GroupIO> gstExceptionGroups = new ArrayList<GroupIO>();

		for (GroupDTO groupDTO : profileDTO.getOtpVerifyGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			otpVerifyGroups.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getExpirePasswordGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			expirePasswordGroups.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getFareRuleExceptionGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			fareRuleExceptionGroups.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getInstantCancellationGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			instantCancellationGroup.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getGstExceptionGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			gstExceptionGroups.add(groupIO);
		}

		profileIO.setFareRuleExceptionGroup(fareRuleExceptionGroups);
		profileIO.setOtpVerifyGroup(otpVerifyGroups);
		profileIO.setExpirePasswordGroup(expirePasswordGroups);
		profileIO.setInstantCancellationGroup(instantCancellationGroup);
		profileIO.setGstExceptionGroup(gstExceptionGroups);

		profileIO.setOtaPartnerCode(profileDTO.getOtaPartnerCode());
		profileIO.setPaymentReceiptAcknowledgeProcess(profileDTO.getPaymentReceiptAcknowledgeProcess());
		profileIO.setAddress(profileDTO.getAddress());
		profileIO.setCity(profileDTO.getCity());
		profileIO.setPincode(profileDTO.getPincode());
		profileIO.setSupportNumber(profileDTO.getSupportNumber());
		profileIO.setRescheduleOverrideAllowDays(profileDTO.getRescheduleOverrideAllowDays());
		profileIO.setJob(profileDTO.getJob());
		profileIO.setExpirePasswordDays(profileDTO.getExpirePasswordDays());
		profileIO.setAllowDirectLogin(profileDTO.getAllowDirectLogin());
		profileIO.setRechargeAutoApprovalFlag(profileDTO.isRechargeAutoApprovalFlag());
		return ResponseIO.success(profileIO);
	}

	@RequestMapping(value = "/auth/profile", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceIO> getNamespaceProfileV2(@PathVariable("authtoken") String authtoken) throws Exception {
		NamespaceIO namespaceIO = new NamespaceIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceProfileDTO profileDTO = namespaceService.getProfile(authDTO);
		NamespaceDTO namespaceDTO = authDTO.getNamespace();
		namespaceIO.setCode(namespaceDTO.getCode());
		namespaceIO.setName(namespaceDTO.getName());
		namespaceIO.setContextToken(namespaceDTO.getContextToken());
		namespaceIO.setActiveFlag(namespaceDTO.getActiveFlag());
		NamespaceProfileIO profileIO = new NamespaceProfileIO();
		profileIO.setBoardingReportingMinitues(profileDTO.getBoardingReportingMinitues());
		profileIO.setCancellationCommissionRevokeFlag(profileDTO.isCancellationCommissionRevokeFlag());
		profileIO.setCancellationChargeTaxFlag(profileDTO.isCancellationChargeTaxFlag());
		profileIO.setDateFormate(profileDTO.getDateFormat());
		profileIO.setDomainURL(profileDTO.getDomainURL());
		profileIO.setEmailNotificationFlag(profileDTO.isEmailNotificationFlag());
		profileIO.setMaxSeatPerTransaction(profileDTO.getMaxSeatPerTransaction());
		profileIO.setAdvanceBookingDays(profileDTO.getAdvanceBookingDays());
		profileIO.setReportingDays(profileDTO.getReportingDays());
		profileIO.setPhoneBookingTicketNotificationMinitues(profileDTO.getPhoneBookingTicketNotificationMinitues());
		profileIO.setTrackbusMinutes(profileDTO.getTrackbusMinutes());
		profileIO.setTimeFormat(profileDTO.getTimeFormat());
		profileIO.setPnrStartCode(profileDTO.getPnrStartCode());
		profileIO.setPnrGenerateTypeCode(profileDTO.getPnrGenerateType().getCode());
		profileIO.setSeatBlockTime(profileDTO.getSeatBlockTime());
		profileIO.setSendarMailName(profileDTO.getSendarMailName());
		profileIO.setSendarSMSName(profileDTO.getSendarSMSName());
		profileIO.setSmsProviderCode(profileDTO.getSmsProvider().getCode());
		profileIO.setNoFareSMSFlag(profileDTO.getNoFareSMSFlag());
		profileIO.setSmsNotificationFlagCode(profileDTO.getSmsNotificationFlagCode());
		profileIO.setNotificationToAlternateMobileFlagCode(profileDTO.getNotificationToAlternateMobileFlagCode());
		profileIO.setEmailCopyAddress(profileDTO.getEmailCopyAddress());
		profileIO.setInstantCancellationMinitues(profileDTO.getInstantCancellationMinitues());
		profileIO.setTravelStatusOpenMinutes(profileDTO.getTravelStatusOpenMinutes());
		profileIO.setCancellationTimeType(profileDTO.getCancellationTimeType());
		profileIO.setAllowExtraCommissionFlag(profileDTO.isAllowExtraCommissionFlag());
		profileIO.setMobileNumberMask(profileDTO.getMobileNumberMask());
		profileIO.setWhatsappNotificationFlagCode(profileDTO.getWhatsappNotificationFlagCode());
		profileIO.setWhatsappProviderCode(profileDTO.getWhatsappProvider().getCode());
		profileIO.setWhatsappSenderName(profileDTO.getWhatsappSenderName());
		profileIO.setWhatsappNumber(profileDTO.getWhatsappNumber());
		profileIO.setWhatsappUrl(profileDTO.getWhatsappUrl());
		profileIO.setWhatsappDatetime(profileDTO.getWhatsappDatetime());
		profileIO.setAliasNamespaceFlag(profileDTO.isAliasNamespaceFlag());
		profileIO.setTicketRescheduleMaxCount(profileDTO.getTicketRescheduleMaxCount());
		profileIO.setSearchPastDayCount(profileDTO.getSearchPastDayCount());
		profileIO.setAllowDirectLogin(profileDTO.getAllowDirectLogin());

		List<String> tickEventContacts = new ArrayList<>();
		List<String> tickAfterTripTimeContacts = new ArrayList<>();
		List<String> tripNotificationContacts = new ArrayList<>();

		for (String mobileNumber : profileDTO.getTicketEventNotificationContact()) {
			tickEventContacts.add(mobileNumber);
		}

		for (String mobileNumber : profileDTO.getTicketAfterTripTimeNotificationContact()) {
			tickAfterTripTimeContacts.add(mobileNumber);
		}

		for (String mobileNumber : profileDTO.getTripNotificationContact()) {
			tripNotificationContacts.add(mobileNumber);
		}

		profileIO.setTicketEventNotificationContact(tickEventContacts);
		profileIO.setTicketAfterTripTimeNotificationContact(tickAfterTripTimeContacts);
		profileIO.setTripNotificationContact(tripNotificationContacts);

		StateIO state = new StateIO();
		if (profileDTO.getState().getId() != 0) {
			state.setCode(profileDTO.getState().getCode());
			state.setName(profileDTO.getState().getName());
		}
		profileIO.setState(state);

		List<FareRuleIO> fareRuleList = new ArrayList<>();
		for (FareRuleDTO fareRuleDTO : profileDTO.getFareRule()) {
			FareRuleIO fareRule = new FareRuleIO();
			fareRule.setCode(fareRuleDTO.getCode());
			fareRule.setName(fareRuleDTO.getName());
			fareRuleList.add(fareRule);
		}
		profileIO.setFareRule(fareRuleList);

		BaseIO seatGendarRestriction = new BaseIO();
		seatGendarRestriction.setCode(profileDTO.getSeatGendarRestriction().getCode());
		seatGendarRestriction.setName(profileDTO.getSeatGendarRestriction().getName());
		profileIO.setSeatGendarRestriction(seatGendarRestriction);

		List<BaseIO> dynamicPriceProviders = new ArrayList<>();
		for (DynamicPriceProviderEM dynamicPriceProviderEM : profileDTO.getDynamicPriceProviders()) {
			BaseIO dynamicPriceProvider = new BaseIO();
			dynamicPriceProvider.setCode(dynamicPriceProviderEM.getCode());
			dynamicPriceProvider.setName(dynamicPriceProviderEM.getName());
			dynamicPriceProviders.add(dynamicPriceProvider);
		}
		profileIO.setDynamicPriceProvider(dynamicPriceProviders);

		List<BaseIO> apiTripChartAllPnrUsers = new ArrayList<>();
		List<BaseIO> apiTripInfoUsers = new ArrayList<>();
		List<BaseIO> apiTicketTransferUsers = new ArrayList<>();
		List<BaseIO> apiTripChartUsers = new ArrayList<>();

		for (UserDTO userDTO : profileDTO.getAllowApiTripChartAllPnr()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTripChartAllPnrUsers.add(userio);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripInfo()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTripInfoUsers.add(userio);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTicketTransfer()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTicketTransferUsers.add(userio);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripChart()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			apiTripChartUsers.add(userio);
		}
		profileIO.setAllowApiTripChartAllPnr(apiTripChartAllPnrUsers);
		profileIO.setAllowApiTripInfo(apiTripInfoUsers);
		profileIO.setAllowApiTicketTransfer(apiTicketTransferUsers);
		profileIO.setAllowApiTripChart(apiTripChartUsers);

		List<BaseIO> cancellationChargeTaxExceptionUsers = new ArrayList<>();

		for (UserDTO userDTO : profileDTO.getCancellationChargeTaxException()) {
			BaseIO userio = new BaseIO();
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setActiveFlag(userDTO.getActiveFlag());
			cancellationChargeTaxExceptionUsers.add(userio);
		}
		profileIO.setCancellationChargeTaxException(cancellationChargeTaxExceptionUsers);

		List<GroupIO> otpVerifyGroups = new ArrayList<GroupIO>();
		List<GroupIO> expirePasswordGroups = new ArrayList<GroupIO>();
		List<GroupIO> fareRuleExceptionGroups = new ArrayList<GroupIO>();
		List<GroupIO> instantCancellationGroup = new ArrayList<GroupIO>();
		List<GroupIO> gstExceptionGroups = new ArrayList<GroupIO>();

		for (GroupDTO groupDTO : profileDTO.getOtpVerifyGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			otpVerifyGroups.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getExpirePasswordGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			expirePasswordGroups.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getFareRuleExceptionGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			fareRuleExceptionGroups.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getInstantCancellationGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			instantCancellationGroup.add(groupIO);
		}
		for (GroupDTO groupDTO : profileDTO.getGstExceptionGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			gstExceptionGroups.add(groupIO);
		}

		profileIO.setFareRuleExceptionGroup(fareRuleExceptionGroups);
		profileIO.setOtpVerifyGroup(otpVerifyGroups);
		profileIO.setExpirePasswordGroup(expirePasswordGroups);
		profileIO.setInstantCancellationGroup(instantCancellationGroup);
		profileIO.setGstExceptionGroup(gstExceptionGroups);

		profileIO.setOtaPartnerCode(profileDTO.getOtaPartnerCode());
		profileIO.setPaymentReceiptAcknowledgeProcess(profileDTO.getPaymentReceiptAcknowledgeProcess());
		profileIO.setAddress(profileDTO.getAddress());
		profileIO.setCity(profileDTO.getCity());
		profileIO.setPincode(profileDTO.getPincode());
		profileIO.setSupportNumber(profileDTO.getSupportNumber());
		profileIO.setRescheduleOverrideAllowDays(profileDTO.getRescheduleOverrideAllowDays());
		profileIO.setExpirePasswordDays(profileDTO.getExpirePasswordDays());
		profileIO.setAllowDirectLogin(profileDTO.getAllowDirectLogin());
		profileIO.setJob(profileDTO.getJob());
		profileIO.setRechargeAutoApprovalFlag(profileDTO.isRechargeAutoApprovalFlag());
		namespaceIO.setNamespaceProfile(profileIO);
		return ResponseIO.success(namespaceIO);
	}

	@RequestMapping(value = "/profile/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceProfileIO> namespaceProfileUID(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceProfileIO profileIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			NamespaceProfileDTO profileDTO = new NamespaceProfileDTO();
			profileDTO.setBoardingReportingMinitues(profileIO.getBoardingReportingMinitues());
			profileDTO.setCancellationCommissionRevokeFlag(profileIO.isCancellationCommissionRevokeFlag());
			profileDTO.setCancellationChargeTaxFlag(profileIO.isCancellationChargeTaxFlag());
			profileDTO.setDateFormat(profileIO.getDateFormate());
			profileDTO.setDomainURL(profileIO.getDomainURL());
			profileDTO.setEmailNotificationFlag(profileIO.isEmailNotificationFlag());
			profileDTO.setMaxSeatPerTransaction(profileIO.getMaxSeatPerTransaction());
			profileDTO.setAdvanceBookingDays(profileIO.getAdvanceBookingDays());
			profileDTO.setPhoneBookingTicketNotificationMinitues(profileIO.getPhoneBookingTicketNotificationMinitues());
			profileDTO.setTrackbusMinutes(profileIO.getTrackbusMinutes());
			profileDTO.setTimeFormat(profileIO.getTimeFormat());
			profileDTO.setPnrStartCode(profileIO.getPnrStartCode());
			profileDTO.setSeatBlockTime(profileIO.getSeatBlockTime());
			profileDTO.setSendarMailName(profileIO.getSendarMailName());
			profileDTO.setSendarSMSName(profileIO.getSendarSMSName());
			profileDTO.setSmsProvider(SMSProviderEM.getSMSProviderEM(profileIO.getSmsProviderCode()));
			profileDTO.setEmailCopyAddress(profileIO.getEmailCopyAddress());
			profileDTO.setNoFareSMSFlag(profileIO.getNoFareSMSFlag());
			profileDTO.setSmsNotificationFlagCode(profileIO.getSmsNotificationFlagCode());
			profileDTO.setNotificationToAlternateMobileFlagCode(profileIO.getNotificationToAlternateMobileFlagCode());
			profileDTO.setInstantCancellationMinitues(profileIO.getInstantCancellationMinitues());
			profileDTO.setCancellationTimeType(StringUtil.isNotNull(profileIO.getCancellationTimeType()) ? profileIO.getCancellationTimeType() : Constants.STAGE);
			profileDTO.setWhatsappNotificationFlagCode(profileIO.getWhatsappNotificationFlagCode());
			profileDTO.setWhatsappProvider(WhatsappProviderEM.getWhatsappProviderEM(profileIO.getWhatsappProviderCode()));
			profileDTO.setWhatsappSenderName(profileIO.getWhatsappSenderName());

			List<String> tickEventContacts = new ArrayList<>();
			List<String> tickAfterTripTimeContacts = new ArrayList<>();
			List<String> tripNotificationContacts = new ArrayList<>();

			if (profileIO.getTicketEventNotificationContact() != null) {
				for (String mobileNumber : profileIO.getTicketEventNotificationContact()) {
					tickEventContacts.add(mobileNumber);
				}
			}
			if (profileIO.getTicketAfterTripTimeNotificationContact() != null) {
				for (String mobileNumber : profileIO.getTicketAfterTripTimeNotificationContact()) {
					tickAfterTripTimeContacts.add(mobileNumber);
				}
			}
			if (profileIO.getTripNotificationContact() != null) {
				for (String mobileNumber : profileIO.getTripNotificationContact()) {
					tripNotificationContacts.add(mobileNumber);
				}
			}
			profileDTO.setTicketEventNotificationContact(tickEventContacts);
			profileDTO.setTicketAfterTripTimeNotificationContact(tickAfterTripTimeContacts);
			profileDTO.setTripNotificationContact(tripNotificationContacts);

			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(StringUtil.isNotNull(profileIO.getState()) ? profileIO.getState().getCode() : Text.NA);
			profileDTO.setState(stateDTO);

			List<FareRuleDTO> fareRuleList = new ArrayList<>();
			if (profileIO.getFareRule() != null) {
				for (FareRuleIO fareRuleIO : profileIO.getFareRule()) {
					if (StringUtil.isNull(fareRuleIO.getCode())) {
						continue;
					}
					FareRuleDTO fareRuleDTO = new FareRuleDTO();
					fareRuleDTO.setCode(fareRuleIO.getCode());
					fareRuleList.add(fareRuleDTO);
				}
			}
			profileDTO.setFareRule(fareRuleList);

			List<UserDTO> apiTripChartAllPnrUsers = new ArrayList<>();
			List<UserDTO> apiTripInfoUsers = new ArrayList<>();
			List<UserDTO> apiTicketTransferUsers = new ArrayList<>();
			List<UserDTO> apiTripChartUsers = new ArrayList<>();

			if (profileIO.getAllowApiTripChartAllPnr() != null) {
				for (BaseIO apiTripChartAllPnrUser : profileIO.getAllowApiTripChartAllPnr()) {
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(apiTripChartAllPnrUser.getCode());
					apiTripChartAllPnrUsers.add(userDTO);
				}
			}
			if (profileIO.getAllowApiTripInfo() != null) {
				for (BaseIO apiTripInfoUser : profileIO.getAllowApiTripInfo()) {
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(apiTripInfoUser.getCode());
					apiTripInfoUsers.add(userDTO);
				}
			}
			if (profileIO.getAllowApiTicketTransfer() != null) {
				for (BaseIO apiTicketTransferUser : profileIO.getAllowApiTicketTransfer()) {
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(apiTicketTransferUser.getCode());
					apiTicketTransferUsers.add(userDTO);
				}
			}
			if (profileIO.getAllowApiTripChart() != null) {
				for (BaseIO apiTripChartUser : profileIO.getAllowApiTripChart()) {
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(apiTripChartUser.getCode());
					apiTripChartUsers.add(userDTO);
				}
			}

			List<UserDTO> cancellationChargeTaxExceptionUsers = new ArrayList<>();

			if (profileIO.getCancellationChargeTaxException() != null) {
				for (BaseIO exceptionUser : profileIO.getCancellationChargeTaxException()) {
					if (StringUtil.isNull(exceptionUser.getCode())) {
						continue;
					}
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(exceptionUser.getCode());
					cancellationChargeTaxExceptionUsers.add(userDTO);
				}
			}
			profileDTO.setCancellationChargeTaxException(cancellationChargeTaxExceptionUsers);

			List<GroupDTO> otpVerifyGroups = new ArrayList<GroupDTO>();
			List<GroupDTO> expirePasswordGroups = new ArrayList<GroupDTO>();
			List<GroupDTO> fareRuleExceptionGroups = new ArrayList<GroupDTO>();
			List<GroupDTO> instantCancellationGroups = new ArrayList<GroupDTO>();
			List<GroupDTO> gstExceptionGroups = new ArrayList<GroupDTO>();

			if (profileIO.getOtpVerifyGroup() != null) {
				for (GroupIO otpVerifyGroup : profileIO.getOtpVerifyGroup()) {
					if (StringUtil.isNull(otpVerifyGroup.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(otpVerifyGroup.getCode());
					otpVerifyGroups.add(groupDTO);
				}
			}
			if (profileIO.getExpirePasswordGroup() != null) {
				for (GroupIO expirePasswordGroup : profileIO.getExpirePasswordGroup()) {
					if (StringUtil.isNull(expirePasswordGroup.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(expirePasswordGroup.getCode());
					expirePasswordGroups.add(groupDTO);
				}
			}
			if (profileIO.getFareRuleExceptionGroup() != null) {
				for (GroupIO fareRuleExceptionGroup : profileIO.getFareRuleExceptionGroup()) {
					if (StringUtil.isNull(fareRuleExceptionGroup.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(fareRuleExceptionGroup.getCode());
					fareRuleExceptionGroups.add(groupDTO);
				}
			}
			if (profileIO.getInstantCancellationGroup() != null) {
				for (GroupIO instantCancellationGroup : profileIO.getInstantCancellationGroup()) {
					if (StringUtil.isNull(instantCancellationGroup.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(instantCancellationGroup.getCode());
					instantCancellationGroups.add(groupDTO);
				}
			}
			if (profileIO.getGstExceptionGroup() != null) {
				for (GroupIO gstExceptionGroup : profileIO.getGstExceptionGroup()) {
					if (StringUtil.isNull(gstExceptionGroup.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(gstExceptionGroup.getCode());
					gstExceptionGroups.add(groupDTO);
				}
			}
			if (profileDTO.getTrackbusMinutes() < 30) {
				profileDTO.setTrackbusMinutes(30);
			}
			profileDTO.setOtpVerifyGroup(otpVerifyGroups);
			profileDTO.setExpirePasswordGroup(expirePasswordGroups);
			profileDTO.setFareRuleExceptionGroup(fareRuleExceptionGroups);
			profileDTO.setInstantCancellationGroup(instantCancellationGroups);
			profileDTO.setGstExceptionGroup(gstExceptionGroups);

			List<DynamicPriceProviderEM> dynamicPriceProviders = new ArrayList<>();
			if (profileIO.getDynamicPriceProvider() != null) {
				for (BaseIO dynamicPriceProviderIO : profileIO.getDynamicPriceProvider()) {
					DynamicPriceProviderEM dynamicPriceProviderEM = DynamicPriceProviderEM.getDynamicPriceProviderEM(dynamicPriceProviderIO.getCode());
					if (dynamicPriceProviderEM != null) {
						dynamicPriceProviders.add(dynamicPriceProviderEM);
					}
				}
			}
			if (profileIO.getDynamicPriceProvider() == null || dynamicPriceProviders.isEmpty()) {
				dynamicPriceProviders.add(DynamicPriceProviderEM.NOT_AVAILABLE);
			}

			profileDTO.setPaymentReceiptAcknowledgeProcess(profileIO.getPaymentReceiptAcknowledgeProcess());
			profileDTO.setDynamicPriceProviders(dynamicPriceProviders);
			profileDTO.setAllowApiTripChartAllPnr(apiTripChartAllPnrUsers);
			profileDTO.setAllowApiTripInfo(apiTripInfoUsers);
			profileDTO.setAllowApiTicketTransfer(apiTicketTransferUsers);
			profileDTO.setAllowApiTripChart(apiTripChartUsers);
			profileDTO.setAddress(profileIO.getAddress());
			profileDTO.setCity(profileIO.getCity());
			profileDTO.setPincode(profileIO.getPincode());
			profileDTO.setSupportNumber(profileIO.getSupportNumber());
			profileDTO.setOtaPartnerCode(profileIO.getOtaPartnerCode());
			profileDTO.setRescheduleOverrideAllowDays(profileIO.getRescheduleOverrideAllowDays());
			profileDTO.setExpirePasswordDays(profileIO.getExpirePasswordDays());
			profileDTO.setTicketRescheduleMaxCount(profileIO.getTicketRescheduleMaxCount());
			profileDTO.setSearchPastDayCount(profileIO.getSearchPastDayCount());
			profileDTO.setAllowDirectLogin(profileIO.getAllowDirectLogin());
			profileDTO.setJob(profileIO.getJob());
			profileDTO.setRechargeAutoApprovalFlag(profileIO.isRechargeAutoApprovalFlag());
			namespaceService.updateProfile(authDTO, profileDTO);
		}
		return ResponseIO.success(profileIO);
	}

	@RequestMapping(value = "/mapping/user/{userCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceIO>> getAllUserNamespaceMap(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		List<NamespaceIO> list = new ArrayList<NamespaceIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);

			List<NamespaceDTO> list2 = namespaceService.getAllUserNamespaceMap(authDTO, userDTO);
			for (NamespaceDTO dto : list2) {
				if (dto.getActiveFlag() != 1) {
					continue;
				}
				NamespaceIO namespace = new NamespaceIO();
				namespace.setCode(dto.getCode());
				namespace.setName(dto.getName());
				namespace.setActiveFlag(dto.getActiveFlag());

				if (dto.getProfile() != null) {
					NamespaceProfileIO profile = new NamespaceProfileIO();
					profile.setAddress(dto.getProfile().getAddress());
					profile.setCity(dto.getProfile().getCity());
					profile.setPincode(dto.getProfile().getPincode());
					profile.setSupportNumber(dto.getProfile().getSupportNumber());

					StateIO state = new StateIO();
					if (dto.getProfile().getState().getId() != 0) {
						state.setCode(dto.getProfile().getState().getCode());
						state.setName(dto.getProfile().getState().getName());
					}
					profile.setState(state);
					namespace.setNamespaceProfile(profile);
				}
				list.add(namespace);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/mapping/{namespaceCode}/user/{userCode}/{action}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> getUpdateUID(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("userCode") String userCode, @PathVariable("action") String action) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			namespaceService.updateUserNamespaceMap(authDTO, namespaceDTO, userDTO, action);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/notification/subscription/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateNotificationSubscriptionType(@PathVariable("authtoken") String authtoken, @RequestBody(required = true) List<String> subscriptionType) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(subscriptionType)) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		List<NotificationSubscriptionTypeEM> subscriptionTypeList = new ArrayList<>();
		for (String subscriptionCode : subscriptionType) {
			NotificationSubscriptionTypeEM subscription = NotificationSubscriptionTypeEM.getSubscriptionTypeEM(subscriptionCode);
			if (subscription == null) {
				throw new ServiceException(ErrorCode.INVALID_CODE, "Not found " + subscriptionCode);
			}
			subscriptionTypeList.add(subscription);
		}
		namespaceService.updateNotificationSubscriptionType(authDTO, subscriptionTypeList);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/notification/subscription", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getNotificationSubscriptionType(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<BaseIO> subscriptionTypeList = new ArrayList<BaseIO>();
		List<NotificationSubscriptionTypeEM> list = authDTO.getNamespace().getProfile().getSubscriptionTypes();
		if (list != null) {
			for (NotificationSubscriptionTypeEM notificationSubscriptionType : list) {
				BaseIO subscriptionType = new BaseIO();
				subscriptionType.setCode(notificationSubscriptionType.getCode());
				subscriptionType.setName(notificationSubscriptionType.getName());
				subscriptionType.setActiveFlag(notificationSubscriptionType.getLevel());
				subscriptionTypeList.add(subscriptionType);
			}
		}
		return ResponseIO.success(subscriptionTypeList);
	}

	@RequestMapping(value = "/sms/provider", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getSMSProvider(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BaseIO> providerList = new ArrayList<BaseIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			for (SMSProviderEM smsProviderEM : SMSProviderEM.values()) {
				BaseIO smsProviderIO = new BaseIO();
				smsProviderIO.setCode(smsProviderEM.getCode());
				smsProviderIO.setName(smsProviderEM.getImpl());
				providerList.add(smsProviderIO);
			}
		}
		return ResponseIO.success(providerList);
	}

	@RequestMapping(value = "/whatsapp/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateNamespaceWhatsapp(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceProfileIO profileIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (!StringUtil.isValidMobileNumber(profileIO.getWhatsappNumber())) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		if (StringUtil.isNull(profileIO.getWhatsappUrl())) {
			throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING, "Invalid URL");
		}
		NamespaceProfileDTO profileDTO = new NamespaceProfileDTO();
		profileDTO.setWhatsappNumber(profileIO.getWhatsappNumber());
		profileDTO.setWhatsappUrl(profileIO.getWhatsappUrl());
		namespaceService.updateNamespaceWhatsapp(authDTO, profileDTO, Numeric.ZERO_INT);
		return ResponseIO.success();
	}

}
