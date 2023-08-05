package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.in.com.aggregator.mercservices.MercService;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.UserCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.NamespaceDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.FareRuleService;
import org.in.com.service.GroupService;
import org.in.com.service.NamespaceService;
import org.in.com.service.NotificationService;
import org.in.com.service.StateService;
import org.in.com.service.UserService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.JSONUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class NamespaceImpl extends UserCache implements NamespaceService {
	private static String ALL_NAMESPACE_KEY = "ALL_NAMESPACE_LIST_KEY";

	@Autowired
	UserService userService;
	@Autowired
	StateService stateService;
	@Autowired
	FareRuleService fareRuleService;
	@Autowired
	GroupService groupService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	MercService mercService;

	public NamespaceDTO getNamespace(NamespaceDTO namespace) {
		NamespaceDTO namespaceDTO = null;
		if (namespace.getId() != 0) {
			namespaceDTO = getNamespaceDTObyId(namespace);
		}
		else if (StringUtil.isNotNull(namespace.getCode())) {
			namespaceDTO = getNamespaceDTO(namespace.getCode());
		}
		return namespaceDTO;
	}

	public NamespaceDTO getNamespace(String namespaceCode) {
		if (StringUtil.isNull(namespaceCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		NamespaceDTO namespaceDTO = getNamespaceDTO(namespaceCode);
		return namespaceDTO;
	}

	public List<NamespaceDTO> get(AuthDTO authDTO, NamespaceDTO DTO) {
		return null;// dao.getNamespaceByCode(code);
	}

	public List<NamespaceDTO> getAll(AuthDTO authDTO) {
		List<NamespaceDTO> namespaceList = null;
		Element namespaceElement = EhcacheManager.getNamespaceEhCache().get(ALL_NAMESPACE_KEY);
		if (namespaceElement != null) {
			namespaceList = (List<NamespaceDTO>) namespaceElement.getObjectValue();
		}
		else {
			NamespaceDAO dao = new NamespaceDAO();
			namespaceList = dao.getAllNamespace();
			EhcacheManager.getNamespaceEhCache().put(new Element(ALL_NAMESPACE_KEY, namespaceList));
		}
		for (NamespaceDTO namespace : namespaceList) {
			namespace.getProfile().setState(stateService.getState(namespace.getProfile().getState()));
		}
		return namespaceList;
	}

	public NamespaceDTO Update(AuthDTO authDTO, NamespaceDTO Newnamespace) {
		NamespaceDAO dao = new NamespaceDAO();
		NamespaceDTO repoNamespace = dao.getNamespaceByCode(Newnamespace.getCode());
		String contextToken = StringUtil.isNull(repoNamespace.getContextToken()) ? TokenGenerator.generateToken(20, true) : repoNamespace.getContextToken();
		Newnamespace.setContextToken(contextToken);
		dao.NamespaceUID(authDTO, Newnamespace);
		removeNamespaceDTO(authDTO, Newnamespace);
		return Newnamespace;
	}

	public NamespaceProfileDTO getProfile(AuthDTO authDTO) {
		NamespaceDAO dao = new NamespaceDAO();
		NamespaceProfileDTO profileDTO = dao.getNamespaceProfile(authDTO);
		profileDTO.setState(stateService.getState(profileDTO.getState()));
		// get fare rule
		if (!profileDTO.getFareRule().isEmpty()) {
			for (FareRuleDTO fareRuleDTO : profileDTO.getFareRule()) {
				getFareRuleDTO(authDTO, fareRuleDTO);
			}
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripInfo()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTicketTransfer()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripChart()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripChartAllPnr()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getCancellationChargeTaxException()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getOtpVerifyGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getExpirePasswordGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getFareRuleExceptionGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getInstantCancellationGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getGstExceptionGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		return profileDTO;
	}

	public boolean updateProfile(AuthDTO authDTO, NamespaceProfileDTO profileDTO) {
		for (UserDTO userDTO : profileDTO.getAllowApiTripInfo()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTicketTransfer()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripChart()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getAllowApiTripChartAllPnr()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		for (UserDTO userDTO : profileDTO.getCancellationChargeTaxException()) {
			userDTO = userService.getUserDTO(authDTO, userDTO);
		}
		if (StringUtil.isNotNull(profileDTO.getState().getCode())) {
			profileDTO.setState(stateService.getState(profileDTO.getState()));
		}
		for (FareRuleDTO fareRuleDTO : profileDTO.getFareRule()) {
			fareRuleService.getFareRule(authDTO, fareRuleDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getOtpVerifyGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getExpirePasswordGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getFareRuleExceptionGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getInstantCancellationGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (GroupDTO groupDTO : profileDTO.getGstExceptionGroup()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		NamespaceProfileDTO repoProfile = getProfile(authDTO);

		NamespaceDAO dao = new NamespaceDAO();
		boolean status = dao.updateProfile(authDTO, profileDTO);
		removeNamespaceDTO(authDTO, authDTO.getNamespace());

		/** Push to Merc */
		JSONArray namespaceHistory = getNamespaceSettingHistory(repoProfile, profileDTO);
		mercService.indexNamespaceProfileHistory(authDTO, profileDTO, namespaceHistory);
		return status;
	}

	private JSONArray getNamespaceSettingHistory(NamespaceProfileDTO profile, NamespaceProfileDTO profileDTO) {
		JSONArray namespaceSettingHistory = new JSONArray();
		if (profile.isCancellationCommissionRevokeFlag() != profileDTO.isCancellationCommissionRevokeFlag()) {
			JSONObject cancellationcommissionrevokeflag = new JSONObject();
			cancellationcommissionrevokeflag.put("keyword", "cancellationcommissionrevokeflag");
			cancellationcommissionrevokeflag.put("olddata", profile.isCancellationCommissionRevokeFlag());
			cancellationcommissionrevokeflag.put("newdata", profileDTO.isCancellationCommissionRevokeFlag());
			namespaceSettingHistory.add(cancellationcommissionrevokeflag);
		}
		if (profile.isCancellationChargeTaxFlag() != profileDTO.isCancellationChargeTaxFlag()) {
			JSONObject cancellationchargetaxflag = new JSONObject();
			cancellationchargetaxflag.put("keyword", "cancellationchargetaxflag");
			cancellationchargetaxflag.put("olddata", profile.isCancellationChargeTaxFlag());
			cancellationchargetaxflag.put("newdata", profileDTO.isCancellationChargeTaxFlag());
			namespaceSettingHistory.add(cancellationchargetaxflag);
		}
		if (!profile.getSmsNotificationFlagCode().equals(profileDTO.getSmsNotificationFlagCode())) {
			JSONObject smsnotificationflagcode = new JSONObject();
			smsnotificationflagcode.put("keyword", "smsnotificationflagcode");
			smsnotificationflagcode.put("olddata", profile.getSmsNotificationFlagCode());
			smsnotificationflagcode.put("newdata", profileDTO.getSmsNotificationFlagCode());
			namespaceSettingHistory.add(smsnotificationflagcode);
		}
		if (profile.isEmailNotificationFlag() != profileDTO.isEmailNotificationFlag()) {
			JSONObject emailnotificationflag = new JSONObject();
			emailnotificationflag.put("keyword", "emailnotificationflag");
			emailnotificationflag.put("olddata", profile.isEmailNotificationFlag());
			emailnotificationflag.put("newdata", profileDTO.isEmailNotificationFlag());
			namespaceSettingHistory.add(emailnotificationflag);
		}
		if (!profile.getWhatsappNotificationFlagCode().equals(profileDTO.getWhatsappNotificationFlagCode())) {
			JSONObject whatsappnotificationflagCode = new JSONObject();
			whatsappnotificationflagCode.put("keyword", "whatsappnotificationflagCode");
			whatsappnotificationflagCode.put("olddata", profile.getWhatsappNotificationFlagCode());
			whatsappnotificationflagCode.put("newdata", profileDTO.getWhatsappNotificationFlagCode());
			namespaceSettingHistory.add(whatsappnotificationflagCode);
		}
		if (profile.getNoFareSMSFlag() != profileDTO.getNoFareSMSFlag()) {
			JSONObject nofaresmsflag = new JSONObject();
			nofaresmsflag.put("keyword", "nofaresmsflag");
			nofaresmsflag.put("olddata", profile.getNoFareSMSFlag());
			nofaresmsflag.put("newdata", profileDTO.getNoFareSMSFlag());
			namespaceSettingHistory.add(nofaresmsflag);
		}
		if (profile.getSeatBlockTime() != profileDTO.getSeatBlockTime()) {
			JSONObject seatblocktime = new JSONObject();
			seatblocktime.put("keyword", "seatblocktime");
			seatblocktime.put("olddata", profile.getSeatBlockTime());
			seatblocktime.put("newdata", profileDTO.getSeatBlockTime());
			namespaceSettingHistory.add(seatblocktime);
		}
		if (profile.getMaxSeatPerTransaction() != profileDTO.getMaxSeatPerTransaction()) {
			JSONObject maxseatpertransaction = new JSONObject();
			maxseatpertransaction.put("keyword", "maxseatpertransaction");
			maxseatpertransaction.put("olddata", profile.getMaxSeatPerTransaction());
			maxseatpertransaction.put("newdata", profileDTO.getMaxSeatPerTransaction());
			namespaceSettingHistory.add(maxseatpertransaction);
		}
		if (profile.getTimeFormat() != profileDTO.getTimeFormat()) {
			JSONObject timeformat = new JSONObject();
			timeformat.put("keyword", "timeformat");
			timeformat.put("olddata", profile.getTimeFormat());
			timeformat.put("newdata", profileDTO.getTimeFormat());
			namespaceSettingHistory.add(timeformat);
		}
		if (profile.getAdvanceBookingDays() != profileDTO.getAdvanceBookingDays()) {
			JSONObject advancebookingdays = new JSONObject();
			advancebookingdays.put("keyword", "advancebookingdays");
			advancebookingdays.put("olddata", profile.getAdvanceBookingDays());
			advancebookingdays.put("newdata", profileDTO.getAdvanceBookingDays());
			namespaceSettingHistory.add(advancebookingdays);
		}
		if (!profile.getPnrStartCode().equals(profileDTO.getPnrStartCode())) {
			JSONObject pnrstartcode = new JSONObject();
			pnrstartcode.put("keyword", "pnrstartcode");
			pnrstartcode.put("olddata", profile.getPnrStartCode());
			pnrstartcode.put("newdata", profileDTO.getPnrStartCode());
			namespaceSettingHistory.add(pnrstartcode);
		}
		if (!profile.getDateFormat().equals(profileDTO.getDateFormat())) {
			JSONObject dateformat = new JSONObject();
			dateformat.put("keyword", "dateformat");
			dateformat.put("olddata", profile.getDateFormat());
			dateformat.put("newdata", profileDTO.getDateFormat());
			namespaceSettingHistory.add(dateformat);
		}
		if (!profile.getSendarMailName().equals(profileDTO.getSendarMailName())) {
			JSONObject sendarmailname = new JSONObject();
			sendarmailname.put("keyword", "sendarmailname");
			sendarmailname.put("olddata", profile.getSendarMailName());
			sendarmailname.put("newdata", profileDTO.getSendarMailName());
			namespaceSettingHistory.add(sendarmailname);
		}
		if (!profile.getSendarSMSName().equals(profileDTO.getSendarSMSName())) {
			JSONObject sendarsmsname = new JSONObject();
			sendarsmsname.put("keyword", "sendarsmsname");
			sendarsmsname.put("olddata", profile.getSendarSMSName());
			sendarsmsname.put("newdata", profileDTO.getSendarSMSName());
			namespaceSettingHistory.add(sendarsmsname);
		}
		if (!profile.getWhatsappSenderName().equals(profileDTO.getWhatsappSenderName())) {
			JSONObject whatsappsendername = new JSONObject();
			whatsappsendername.put("keyword", "whatsappsendername");
			whatsappsendername.put("olddata", profile.getWhatsappSenderName());
			whatsappsendername.put("newdata", profileDTO.getWhatsappSenderName());
			namespaceSettingHistory.add(whatsappsendername);
		}
		if (!profile.getEmailCopyAddress().equals(profileDTO.getEmailCopyAddress())) {
			JSONObject emailcopyaddress = new JSONObject();
			emailcopyaddress.put("keyword", "emailcopyaddress");
			emailcopyaddress.put("olddata", profile.getEmailCopyAddress());
			emailcopyaddress.put("newdata", profileDTO.getEmailCopyAddress());
			namespaceSettingHistory.add(emailcopyaddress);
		}
		if (profile.getSmsProvider().getId() != profileDTO.getSmsProvider().getId()) {
			JSONObject smsprovider = new JSONObject();
			smsprovider.put("keyword", "smsprovider");
			smsprovider.put("olddata", profile.getSmsProvider().getCode());
			smsprovider.put("newdata", profileDTO.getSmsProvider().getCode());
			namespaceSettingHistory.add(smsprovider);
		}

		if (profile.getWhatsappProvider().getId() != profileDTO.getWhatsappProvider().getId()) {
			JSONObject whatsappprovider = new JSONObject();
			whatsappprovider.put("keyword", "whatsappprovider");
			whatsappprovider.put("olddata", profile.getWhatsappProvider().getCode());
			whatsappprovider.put("newdata", profileDTO.getWhatsappProvider().getCode());
			namespaceSettingHistory.add(whatsappprovider);
		}
		if (profile.getDynamicPriceProviders() != null && profileDTO.getDynamicPriceProviders() != null) {
			String dynamicProviderCodes = getDynamicPriceProvidersCodes(profile.getDynamicPriceProviders());
			String providerCodes = getDynamicPriceProvidersCodes(profileDTO.getDynamicPriceProviders());
			if (!dynamicProviderCodes.equals(providerCodes)) {
				JSONObject dynamicpriceprovider = new JSONObject();
				dynamicpriceprovider.put("keyword", "dynamicpriceprovider");
				dynamicpriceprovider.put("olddata", dynamicProviderCodes);
				dynamicpriceprovider.put("newdata", providerCodes);
				namespaceSettingHistory.add(dynamicpriceprovider);
			}
		}

		if (!profile.getDomainURL().equals(profileDTO.getDomainURL())) {
			JSONObject domainurl = new JSONObject();
			domainurl.put("keyword", "domainurl");
			domainurl.put("olddata", profile.getDomainURL());
			domainurl.put("newdata", profileDTO.getDomainURL());
			namespaceSettingHistory.add(domainurl);
		}
		if (profile.getPhoneBookingTicketNotificationMinitues() != profileDTO.getPhoneBookingTicketNotificationMinitues()) {
			JSONObject phonebookingticketnotificationminitues = new JSONObject();
			phonebookingticketnotificationminitues.put("keyword", "phonebookingticketnotificationminitues");
			phonebookingticketnotificationminitues.put("olddata", profile.getPhoneBookingTicketNotificationMinitues());
			phonebookingticketnotificationminitues.put("newdata", profileDTO.getPhoneBookingTicketNotificationMinitues());
			namespaceSettingHistory.add(phonebookingticketnotificationminitues);
		}
		if (profile.getBoardingReportingMinitues() != profileDTO.getBoardingReportingMinitues()) {
			JSONObject boardingreportingminitues = new JSONObject();
			boardingreportingminitues.put("keyword", "boardingreportingminitues");
			boardingreportingminitues.put("olddata", profile.getBoardingReportingMinitues());
			boardingreportingminitues.put("newdata", profileDTO.getBoardingReportingMinitues());
			namespaceSettingHistory.add(boardingreportingminitues);
		}
		if (profile.getPhoneBookingCancellationBlockMinutes() != profileDTO.getPhoneBookingCancellationBlockMinutes()) {
			JSONObject phonebookingcancellationblockminutes = new JSONObject();
			phonebookingcancellationblockminutes.put("keyword", "phonebookingcancellationblockminutes");
			phonebookingcancellationblockminutes.put("olddata", profile.getPhoneBookingCancellationBlockMinutes());
			phonebookingcancellationblockminutes.put("newdata", profileDTO.getPhoneBookingCancellationBlockMinutes());
			namespaceSettingHistory.add(phonebookingcancellationblockminutes);
		}
		if (profile.getInstantCancellationMinitues() != profileDTO.getInstantCancellationMinitues()) {
			JSONObject instantcancellationminitues = new JSONObject();
			instantcancellationminitues.put("keyword", "instantcancellationminitues");
			instantcancellationminitues.put("olddata", profile.getInstantCancellationMinitues());
			instantcancellationminitues.put("newdata", profileDTO.getInstantCancellationMinitues());
			namespaceSettingHistory.add(instantcancellationminitues);
		}
		if (!profile.getCancellationTimeType().equals(profileDTO.getCancellationTimeType())) {
			JSONObject cancellationtimetype = new JSONObject();
			cancellationtimetype.put("keyword", "cancellationtimetype");
			cancellationtimetype.put("olddata", profile.getCancellationTimeType());
			cancellationtimetype.put("newdata", profileDTO.getCancellationTimeType());
			namespaceSettingHistory.add(cancellationtimetype);
		}
		if (profile.getTrackbusMinutes() != profileDTO.getTrackbusMinutes()) {
			JSONObject trackbusminutes = new JSONObject();
			trackbusminutes.put("keyword", "trackbusminutes");
			trackbusminutes.put("olddata", profile.getTrackbusMinutes());
			trackbusminutes.put("newdata", profileDTO.getTrackbusMinutes());
			namespaceSettingHistory.add(trackbusminutes);
		}

		if (profile.getFareRule() != null && profileDTO.getFareRule() != null) {
			String fareRule = getFareRuleCodes(profile.getFareRule());
			String fareRules = getFareRuleCodes(profileDTO.getFareRule());
			if (!fareRule.equals(fareRules)) {
				JSONObject farerule = new JSONObject();
				farerule.put("keyword", "farerule");
				farerule.put("olddata", fareRule);
				farerule.put("newdata", fareRules);
				namespaceSettingHistory.add(farerule);
			}
		}

		if (profile.getState().getId() != profileDTO.getState().getId()) {
			JSONObject state = new JSONObject();
			state.put("keyword", "state");
			state.put("olddata", profile.getState().getCode());
			state.put("newdata", profileDTO.getState().getCode());
			namespaceSettingHistory.add(state);
		}

		if (profile.getAllowApiTripChartAllPnr() != null && profileDTO.getAllowApiTripChartAllPnr() != null) {
			String userCode = getUserCodes(profile.getAllowApiTripChartAllPnr());
			String userCodes = getUserCodes(profileDTO.getAllowApiTripChartAllPnr());
			if (!userCode.equals(userCodes)) {
				JSONObject allowapitripchartallpnr = new JSONObject();
				allowapitripchartallpnr.put("keyword", "allowapitripchartallpnr");
				allowapitripchartallpnr.put("olddata", userCode);
				allowapitripchartallpnr.put("newdata", userCodes);
				namespaceSettingHistory.add(allowapitripchartallpnr);
			}
		}

		if (profile.getAllowApiTripInfo() != null && profileDTO.getAllowApiTripInfo() != null) {
			String userCode = getUserCodes(profile.getAllowApiTripInfo());
			String userCodes = getUserCodes(profileDTO.getAllowApiTripInfo());
			if (!userCode.equals(userCodes)) {
				JSONObject allowapitripinfo = new JSONObject();
				allowapitripinfo.put("keyword", "allowapitripinfo");
				allowapitripinfo.put("olddata", userCode);
				allowapitripinfo.put("newdata", userCodes);
				namespaceSettingHistory.add(allowapitripinfo);
			}
		}

		if (profile.getAllowApiTicketTransfer() != null && profileDTO.getAllowApiTicketTransfer() != null) {
			String userCode = getUserCodes(profile.getAllowApiTicketTransfer());
			String userCodes = getUserCodes(profileDTO.getAllowApiTicketTransfer());
			if (!userCode.equals(userCodes)) {
				JSONObject allowapitickettransfer = new JSONObject();
				allowapitickettransfer.put("keyword", "allowapitickettransfer");
				allowapitickettransfer.put("olddata", userCode);
				allowapitickettransfer.put("newdata", userCodes);
				namespaceSettingHistory.add(allowapitickettransfer);
			}
		}

		if (profile.getAllowApiTripChart() != null && profileDTO.getAllowApiTripChart() != null) {
			String userCode = getUserCodes(profile.getAllowApiTripChart());
			String userCodes = getUserCodes(profileDTO.getAllowApiTripChart());
			if (!userCode.equals(userCodes)) {
				JSONObject allowapitripchart = new JSONObject();
				allowapitripchart.put("keyword", "allowapitripchart");
				allowapitripchart.put("olddata", userCode);
				allowapitripchart.put("newdata", userCodes);
				namespaceSettingHistory.add(allowapitripchart);
			}
		}

		if (profile.getCancellationChargeTaxException() != null && profileDTO.getCancellationChargeTaxException() != null) {
			String userCode = getUserCodes(profile.getCancellationChargeTaxException());
			String userCodes = getUserCodes(profileDTO.getCancellationChargeTaxException());
			if (!userCode.equals(userCodes)) {
				JSONObject cancellationchargetaxexception = new JSONObject();
				cancellationchargetaxexception.put("keyword", "cancellationchargetaxexception");
				cancellationchargetaxexception.put("olddata", userCode);
				cancellationchargetaxexception.put("newdata", userCodes);
				namespaceSettingHistory.add(cancellationchargetaxexception);
			}
		}

		if (profile.getPaymentReceiptAcknowledgeProcess() != profileDTO.getPaymentReceiptAcknowledgeProcess()) {
			JSONObject paymentreceiptacknowledgeprocess = new JSONObject();
			paymentreceiptacknowledgeprocess.put("keyword", "paymentreceiptacknowledgeprocess");
			paymentreceiptacknowledgeprocess.put("olddata", profile.getPaymentReceiptAcknowledgeProcess());
			paymentreceiptacknowledgeprocess.put("newdata", profileDTO.getPaymentReceiptAcknowledgeProcess());
			namespaceSettingHistory.add(paymentreceiptacknowledgeprocess);
		}

		if (profile.getOtaPartnerCode() != null && profileDTO.getOtaPartnerCode() != null) {
			String partnerCode = getOtaPartnerCode(profile.getOtaPartnerCode());
			String partnerCodes = getOtaPartnerCode(profileDTO.getOtaPartnerCode());
			if (!partnerCode.equals(partnerCodes)) {
				JSONObject otapartnercode = new JSONObject();
				otapartnercode.put("keyword", "otapartnercode");
				otapartnercode.put("olddata", partnerCode);
				otapartnercode.put("newdata", partnerCodes);
				namespaceSettingHistory.add(otapartnercode);
			}
		}

		if (profile.getOtpVerifyGroup() != null && profileDTO.getOtpVerifyGroup() != null) {
			String groupCode = getGroupCodes(profile.getOtpVerifyGroup());
			String groupCodes = getGroupCodes(profileDTO.getOtpVerifyGroup());
			if (!groupCode.equals(groupCodes)) {
				JSONObject otpverifygroup = new JSONObject();
				otpverifygroup.put("keyword", "otpverifygroup");
				otpverifygroup.put("olddata", groupCode);
				otpverifygroup.put("newdata", groupCodes);
				namespaceSettingHistory.add(otpverifygroup);
			}
		}

		if (profile.getExpirePasswordGroup() != null && profileDTO.getExpirePasswordGroup() != null) {
			String groupCode = getGroupCodes(profile.getExpirePasswordGroup());
			String groupCodes = getGroupCodes(profileDTO.getExpirePasswordGroup());
			if (!groupCode.equals(groupCodes)) {
				JSONObject expirepasswordgroup = new JSONObject();
				expirepasswordgroup.put("keyword", "expirepasswordgroup");
				expirepasswordgroup.put("olddata", groupCode);
				expirepasswordgroup.put("newdata", groupCodes);
				namespaceSettingHistory.add(expirepasswordgroup);
			}
		}

		if (profile.getFareRuleExceptionGroup() != null && profileDTO.getFareRuleExceptionGroup() != null) {
			String groupCode = getGroupCodes(profile.getFareRuleExceptionGroup());
			String groupCodes = getGroupCodes(profileDTO.getFareRuleExceptionGroup());
			if (!groupCode.equals(groupCodes)) {
				JSONObject fareruleexceptiongroup = new JSONObject();
				fareruleexceptiongroup.put("keyword", "fareruleexceptiongroup");
				fareruleexceptiongroup.put("olddata", groupCode);
				fareruleexceptiongroup.put("newdata", groupCodes);
				namespaceSettingHistory.add(fareruleexceptiongroup);
			}
		}

		if (profile.getInstantCancellationGroup() != null && profileDTO.getInstantCancellationGroup() != null) {
			String groupCode = getGroupCodes(profile.getInstantCancellationGroup());
			String groupCodes = getGroupCodes(profileDTO.getInstantCancellationGroup());
			if (!groupCode.equals(groupCodes)) {
				JSONObject instantcancellationgroup = new JSONObject();
				instantcancellationgroup.put("keyword", "instantcancellationgroup");
				instantcancellationgroup.put("olddata", groupCode);
				instantcancellationgroup.put("newdata", groupCodes);
				namespaceSettingHistory.add(instantcancellationgroup);
			}
		}

		if (profile.getGstExceptionGroup() != null && profileDTO.getGstExceptionGroup() != null) {
			String groupCode = getGroupCodes(profile.getGstExceptionGroup());
			String groupCodes = getGroupCodes(profileDTO.getGstExceptionGroup());
			if (!groupCode.equals(groupCodes)) {
				JSONObject gstexceptiongroup = new JSONObject();
				gstexceptiongroup.put("keyword", "gstexceptiongroup");
				gstexceptiongroup.put("olddata", groupCode);
				gstexceptiongroup.put("newdata", groupCodes);
				namespaceSettingHistory.add(gstexceptiongroup);
			}
		}

		if (profile.getTicketEventNotificationContact() != null && profileDTO.getTicketEventNotificationContact() != null) {
			String notificationContact = getNotificationContacts(profile.getTicketEventNotificationContact());
			String notificationContacts = getNotificationContacts(profileDTO.getTicketEventNotificationContact());
			if (!notificationContact.equals(notificationContacts)) {
				JSONObject ticketeventnotificationcontact = new JSONObject();
				ticketeventnotificationcontact.put("keyword", "ticketeventnotificationcontact");
				ticketeventnotificationcontact.put("olddata", notificationContact);
				ticketeventnotificationcontact.put("newdata", notificationContacts);
				namespaceSettingHistory.add(ticketeventnotificationcontact);
			}
		}

		if (profile.getTicketAfterTripTimeNotificationContact() != null && profileDTO.getTicketAfterTripTimeNotificationContact() != null) {
			String notificationContact = getNotificationContacts(profile.getTicketAfterTripTimeNotificationContact());
			String notificationContacts = getNotificationContacts(profileDTO.getTicketAfterTripTimeNotificationContact());
			if (!notificationContact.equals(notificationContacts)) {
				JSONObject ticketaftertriptimenotificationcontact = new JSONObject();
				ticketaftertriptimenotificationcontact.put("keyword", "ticketaftertriptimenotificationcontact");
				ticketaftertriptimenotificationcontact.put("olddata", notificationContact);
				ticketaftertriptimenotificationcontact.put("newdata", notificationContacts);
				namespaceSettingHistory.add(ticketaftertriptimenotificationcontact);
			}
		}

		if (profile.getTripNotificationContact() != null && profileDTO.getTripNotificationContact() != null) {
			String notificationContact = getNotificationContacts(profile.getTripNotificationContact());
			String notificationContacts = getNotificationContacts(profileDTO.getTripNotificationContact());
			if (!notificationContact.equals(notificationContacts)) {
				JSONObject tripnotificationcontact = new JSONObject();
				tripnotificationcontact.put("keyword", "tripnotificationcontact");
				tripnotificationcontact.put("olddata", notificationContact);
				tripnotificationcontact.put("newdata", notificationContacts);
				namespaceSettingHistory.add(tripnotificationcontact);
			}
		}

		if (profile.getSubscriptionTypes() != null && profileDTO.getSubscriptionTypes() != null) {
			String subscriptionType = getSubscriptionTypeCodes(profile.getSubscriptionTypes());
			String subscriptionTypes = getSubscriptionTypeCodes(profileDTO.getSubscriptionTypes());
			if (!subscriptionType.equals(subscriptionTypes)) {
				JSONObject subscriptiontypes = new JSONObject();
				subscriptiontypes.put("keyword", "subscriptiontypes");
				subscriptiontypes.put("olddata", subscriptionType);
				subscriptiontypes.put("newdata", subscriptionTypes);
				namespaceSettingHistory.add(subscriptiontypes);
			}
		}

		if (!profile.getAddress().equals(profileDTO.getAddress())) {
			JSONObject address = new JSONObject();
			address.put("keyword", "address");
			address.put("olddata", profile.getAddress());
			address.put("newdata", profileDTO.getAddress());
			namespaceSettingHistory.add(address);
		}
		if (!profile.getCity().equals(profileDTO.getCity())) {
			JSONObject city = new JSONObject();
			city.put("keyword", "city");
			city.put("olddata", profile.getCity());
			city.put("newdata", profileDTO.getCity());
			namespaceSettingHistory.add(city);
		}
		if (!profile.getPincode().equals(profileDTO.getPincode())) {
			JSONObject pincode = new JSONObject();
			pincode.put("keyword", "pincode");
			pincode.put("olddata", profile.getPincode());
			pincode.put("newdata", profileDTO.getPincode());
			namespaceSettingHistory.add(pincode);
		}
		if (!profile.getSupportNumber().equals(profileDTO.getSupportNumber())) {
			JSONObject supportnumber = new JSONObject();
			supportnumber.put("keyword", "supportnumber");
			supportnumber.put("olddata", profile.getSupportNumber());
			supportnumber.put("newdata", profileDTO.getSupportNumber());
			namespaceSettingHistory.add(supportnumber);
		}
		if (profile.getRescheduleOverrideAllowDays() != profileDTO.getRescheduleOverrideAllowDays()) {
			JSONObject rescheduleoverrideallowdays = new JSONObject();
			rescheduleoverrideallowdays.put("keyword", "rescheduleoverrideallowdays");
			rescheduleoverrideallowdays.put("olddata", profile.getRescheduleOverrideAllowDays());
			rescheduleoverrideallowdays.put("newdata", profileDTO.getRescheduleOverrideAllowDays());
			namespaceSettingHistory.add(rescheduleoverrideallowdays);
		}
		if (profile.getTicketRescheduleMaxCount() != profileDTO.getTicketRescheduleMaxCount()) {
			JSONObject ticketreschedulemaxount = new JSONObject();
			ticketreschedulemaxount.put("keyword", "ticketreschedulemaxount");
			ticketreschedulemaxount.put("olddata", profile.getTicketRescheduleMaxCount());
			ticketreschedulemaxount.put("newdata", profileDTO.getTicketRescheduleMaxCount());
			namespaceSettingHistory.add(ticketreschedulemaxount);
		}
		if (profile.getAllowDirectLogin() != profileDTO.getAllowDirectLogin()) {
			JSONObject allowdirectlogin = new JSONObject();
			allowdirectlogin.put("keyword", "allowdirectlogin");
			allowdirectlogin.put("olddata", profile.getAllowDirectLogin());
			allowdirectlogin.put("newdata", profileDTO.getAllowDirectLogin());
			namespaceSettingHistory.add(allowdirectlogin);
		}
		if (!profile.getJob().equals(profileDTO.getJob())) {
			JSONObject job = new JSONObject();
			job.put("keyword", "job");
			job.put("olddata", profile.getJob());
			job.put("newdata", profileDTO.getJob());
			namespaceSettingHistory.add(job);
		}
		if (profile.isRechargeAutoApprovalFlag() != profileDTO.isRechargeAutoApprovalFlag()) {
			JSONObject rechargeautoapprovalFlag = new JSONObject();
			rechargeautoapprovalFlag.put("keyword", "rechargeautoapprovalFlag");
			rechargeautoapprovalFlag.put("olddata", profile.isRechargeAutoApprovalFlag());
			rechargeautoapprovalFlag.put("newdata", profileDTO.isRechargeAutoApprovalFlag());
			namespaceSettingHistory.add(rechargeautoapprovalFlag);
		}
		return namespaceSettingHistory;

	}

	public List<NamespaceDTO> getAllUserNamespaceMap(AuthDTO authDTO, UserDTO userDTO) {
		NamespaceDAO dao = new NamespaceDAO();
		userDTO = getUserDTO(authDTO, userDTO);
		if (userDTO.getId() == Numeric.ZERO_INT) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		List<NamespaceDTO> list = dao.getAllUserNamespaceMap(authDTO, userDTO);
		for (NamespaceDTO namespaceDTO : list) {
			NamespaceDTO namespace = getNamespaceDTObyId(namespaceDTO);
			namespaceDTO.setName(namespace.getName());
			namespaceDTO.setActiveFlag(namespace.getActiveFlag());
			namespaceDTO.setProfile(namespace.getProfile());
			if (namespace.getProfile() != null && namespace.getProfile().getState() != null) {
				namespaceDTO.getProfile().setState(stateService.getState(namespace.getProfile().getState()));
			}
		}
		return list;
	}

	public void updateUserNamespaceMap(AuthDTO authDTO, NamespaceDTO namespaceDTO, UserDTO userDTO, String action) {
		NamespaceDAO dao = new NamespaceDAO();
		dao.updateUserNamespaceMap(authDTO, namespaceDTO, userDTO, action);
	}

	public void checkUserNamespaceMapping(AuthDTO authDTO, NamespaceDTO namespaceDTO) {
		NamespaceDAO dao = new NamespaceDAO();
		boolean checkMapping = dao.checkUserNamespaceMapping(authDTO, namespaceDTO);
		if (!checkMapping && !authDTO.getNativeNamespaceCode().equals(namespaceDTO.getCode())) {
			System.out.println("unauthorized NS access: " + authDTO.getNativeNamespaceCode() + "-" + authDTO.getNamespaceCode() + " - " + authDTO.getUser().getUsername() + " - " + namespaceDTO.getCode() + "-" + authDTO.getDeviceMedium().getCode());
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}

	@Override
	public void updateNotificationSubscriptionType(AuthDTO authDTO, List<NotificationSubscriptionTypeEM> subsciptionTypeList) {
		NamespaceDAO dao = new NamespaceDAO();
		dao.updateSubscriptionType(authDTO, subsciptionTypeList);
		removeNamespaceDTO(authDTO, authDTO.getNamespace());
	}

	public List<NamespaceDTO> getNotificationEnabledNamespace(AuthDTO authDTO, NotificationTypeEM notificationType) {
		List<NamespaceDTO> namespaceList = new ArrayList<NamespaceDTO>();
		List<NamespaceDTO> list = getAll(authDTO);
		for (NamespaceDTO namespace : list) {
			// No need to process for alias namespace
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				NamespaceProfileDTO namespaceProfile = getNamespace(namespace).getProfile();
				if (NotificationTypeEM.isNotificationEnabled(namespaceProfile.getSmsNotificationFlagCode(), notificationType)) {
					namespace.setProfile(namespaceProfile);
					namespaceList.add(namespace);
				}
			}
			catch (Exception e) {
				System.out.println(JSONUtil.objectToJson(namespace));
				e.printStackTrace();
			}
		}
		return namespaceList;
	}

	@Override
	public void updateNamespaceWhatsapp(AuthDTO authDTO, NamespaceProfileDTO profileDTO, int status) {
		NamespaceDAO dao = new NamespaceDAO();
		if (status == Numeric.ZERO_INT) {
			profileDTO.setWhatsappDatetime(Text.NA);
			dao.updateNamespaceWhatsapp(authDTO, profileDTO);
			// send notification
			notificationService.sendWhatsappVerificationNotification(authDTO, profileDTO);
		}
		else if (status == Numeric.ONE_INT) {
			if (StringUtil.isNotNull(authDTO.getNamespace().getProfile().getWhatsappDatetime())) {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, "mobile number verified");
			}
			if (!authDTO.getNamespace().getProfile().getWhatsappNumber().equals(profileDTO.getWhatsappNumber())) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			profileDTO.setWhatsappUrl(StringUtil.isNull(profileDTO.getWhatsappUrl(), authDTO.getNamespace().getProfile().getWhatsappUrl()));
			profileDTO.setWhatsappDatetime(DateUtil.NowV2().toString(DateUtil.JODA_DATE_TIME_FORMATE));
			dao.updateNamespaceWhatsapp(authDTO, profileDTO);
		}
		removeNamespaceDTO(authDTO, authDTO.getNamespace());
	}

	@Override
	public NamespaceDTO getNamespaceByContextToken(String contextToken) {
		NamespaceDAO dao = new NamespaceDAO();
		return dao.getNamespaceByContextToken(contextToken);
	}

	@Override
	public NamespaceTabletSettingsDTO getNamespaceTabletSettings(AuthDTO authDTO) {
		return getNamespaceTabletSettingsCache(authDTO);
	}

	@Override
	public void putNamespaceTabletSettings(AuthDTO authDTO, NamespaceTabletSettingsDTO namespaceTabletSettingsDTO) {
		putNamespaceTabletSettingsCache(authDTO, namespaceTabletSettingsDTO);
	}

	@Override
	public void removeNamespaceTabletSettingsCache(AuthDTO authDTO) {
		removeNamespaceTabletSettings(authDTO);
	}

	private String getGroupCodes(List<GroupDTO> groupList) {
		StringBuilder group = new StringBuilder();
		for (GroupDTO groupDTO : groupList) {
			group.append(groupDTO.getCode());
			group.append(Text.COMMA);
		}
		return group.toString();
	}

	private String getUserCodes(List<UserDTO> userList) {
		StringBuilder user = new StringBuilder();
		for (UserDTO userDTO : userList) {
			user.append(userDTO.getCode());
			user.append(Text.COMMA);
		}
		return user.toString();
	}

	private String getFareRuleCodes(List<FareRuleDTO> fareRuleList) {
		StringBuilder fareRuleIds = new StringBuilder();
		for (FareRuleDTO fareRuleDTO : fareRuleList) {
			fareRuleIds.append(fareRuleDTO.getCode());
			fareRuleIds.append(Text.COMMA);
		}
		return fareRuleIds.toString();
	}

	private String getNotificationContacts(List<String> notificationContact) {
		StringBuilder contacts = new StringBuilder();
		for (String mobileNumber : notificationContact) {
			contacts.append(mobileNumber);
			contacts.append(Text.COMMA);
		}
		return contacts.toString();
	}

	private String getOtaPartnerCode(Map<String, String> otaPartnerDetailsMap) {
		StringBuilder otaPartnerCodes = new StringBuilder();
		if (otaPartnerDetailsMap != null) {
			for (Entry<String, String> otaPartnerMap : otaPartnerDetailsMap.entrySet()) {
				UserTagEM userTag = UserTagEM.getUserTagEM(otaPartnerMap.getKey());
				String partnerCode = otaPartnerMap.getValue();
				otaPartnerCodes.append(userTag.getCode() + Text.COLON + partnerCode);
				otaPartnerCodes.append(Text.COMMA);

			}
		}
		return otaPartnerCodes.toString();
	}

	private String getSubscriptionTypeCodes(List<NotificationSubscriptionTypeEM> subscriptionTypes) {
		StringBuilder subscriptionsType = new StringBuilder();
		for (NotificationSubscriptionTypeEM type : subscriptionTypes) {
			subscriptionsType.append(type.getCode());
			subscriptionsType.append(Text.COMMA);
		}
		return subscriptionsType.toString();
	}

	private String getDynamicPriceProvidersCodes(List<DynamicPriceProviderEM> dynamicPriceProviders) {
		StringBuilder dynamicPriceProviderIds = new StringBuilder();
		if (dynamicPriceProviders != null && !dynamicPriceProviders.isEmpty()) {
			for (DynamicPriceProviderEM dynamicPriceProviderEM : dynamicPriceProviders) {
				dynamicPriceProviderIds.append(dynamicPriceProviderEM.getCode());
				dynamicPriceProviderIds.append(Text.COMMA);
			}
		}
		return dynamicPriceProviderIds.toString();
	}
}
