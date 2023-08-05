package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Cleanup;

import org.apache.commons.lang3.BooleanUtils;
import org.in.com.aggregator.sms.SMSProviderEM;
import org.in.com.aggregator.whatsapp.WhatsappProviderEM;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.PNRGenerateTypeEM;
import org.in.com.dto.enumeration.SeatGenderRestrictionEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import com.google.common.collect.Maps;

public class NamespaceDAO {
	public List<NamespaceDTO> getAllNamespace() {
		List<NamespaceDTO> list = new ArrayList<NamespaceDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT nasp.id,nasp.code,nasp.name,nasp.context_token,nasp.alias,nasp.lookup_id,nasp.active_flag, nset.state_id, nset.job, nset.city FROM namespace nasp LEFT OUTER JOIN namespace_settings  nset ON nset.namespace_id = nasp.id WHERE nasp.active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceDTO dto = new NamespaceDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setContextToken(selectRS.getString("context_token"));
				dto.setAliasCode(selectRS.getString("alias"));
				dto.setLookupId(selectRS.getInt("lookup_id"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				NamespaceProfileDTO profile = new NamespaceProfileDTO();
				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				profile.setCity(selectRS.getString("city"));
				profile.setState(state);
				profile.setJob(selectRS.getString("job"));
				dto.setProfile(profile);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NamespaceDTO NamespaceUID(AuthDTO authDTO, NamespaceDTO NewNamespace) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_IUD(?,?,?,? ,?,?,?)}");
			callableStatement.setString(++pindex, NewNamespace.getCode());
			callableStatement.setString(++pindex, NewNamespace.getName());
			callableStatement.setString(++pindex, NewNamespace.getContextToken());
			callableStatement.setInt(++pindex, NewNamespace.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				NewNamespace.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public NamespaceDTO getNamespaceByCode(String code) {
		NamespaceDTO dto = new NamespaceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,name,context_token,active_flag FROM namespace where active_flag < 2 and code = ?");
			selectPS.setString(1, code);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setContextToken(selectRS.getString("context_token"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public NamespaceDTO getNamespaceByContextToken(String contextToken) {
		NamespaceDTO dto = new NamespaceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,name,context_token,active_flag FROM namespace WHERE context_token = ? AND active_flag = 1");
			selectPS.setString(1, contextToken);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setContextToken(selectRS.getString("context_token"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public boolean updateProfile(AuthDTO authDTO, NamespaceProfileDTO namespaceProfileDTO) {

		boolean status = false;
		try {
			int index = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE namespace_settings SET  cancellation_commission_revoke_flag = ?, cancellation_charge_tax_flag = ?, notification_sms_flag = ?, notification_email_flag = ?, notification_whatsapp_flag = ?, notification_to_alternate_mobile_flag = ?, no_fare_sms_flag = ?, seat_block_time = ?, max_seat_per_transaction = ?, time_format = ?, advance_booking_days = ?, pnr_start_code = ?, date_format = ?, sender_mail_name = ?, sendar_sms_name = ?, whatsapp_sender_name = ?, email_copy_address = ?, sms_provider_id = ?, whatsapp_provider_id = ?, dynamic_price_provider = ?, domain_url = ?, phone_ticket_notification_before_minitues = ?, boarding_reporting_minitues = ?, phone_booking_cancellation_block_minutes = ?, instant_cancellation_minitues = ?, cancellation_time_type = ?, trackbus_minutes = ?, fare_rule_id = ?, state_id = ?, allow_api_trip_chart_all_pnr = ?, allow_api_trip_info = ?, allow_api_ticket_transfer = ?, allow_api_trip_chart = ?, cancellation_charge_tax_exception_user = ?, payment_receipt_acknowledge_process = ?, otp_verify_group = ?, expire_password_group = ?, fare_rule_exception_group = ?, instant_cancellation_user_group = ?, gst_exception_group = ?, ota_partner_code = ?, ticket_event_notification_contact = ?, ticket_after_trip_time_notification_contact = ?, trip_notification_contact = ?, address = ?, city = ?, pincode = ?, support_number = ?, reschedule_override_allow_days = ?, expire_password_days = ?, ticket_reschedule_max_count = ?, search_past_day_count = ?, allow_direct_login = ?, job = ?, recharge_auto_approval_flag = ?, updated_by = ?, updated_at = now() WHERE namespace_id = ?");
			selectPS.setBoolean(++index, namespaceProfileDTO.isCancellationCommissionRevokeFlag());
			selectPS.setBoolean(++index, namespaceProfileDTO.isCancellationChargeTaxFlag());
			selectPS.setString(++index, namespaceProfileDTO.getSmsNotificationFlagCode());
			selectPS.setBoolean(++index, namespaceProfileDTO.isEmailNotificationFlag());
			selectPS.setString(++index, namespaceProfileDTO.getWhatsappNotificationFlagCode());
			selectPS.setString(++index, namespaceProfileDTO.getNotificationToAlternateMobileFlagCode());
			selectPS.setInt(++index, namespaceProfileDTO.getNoFareSMSFlag());
			selectPS.setInt(++index, namespaceProfileDTO.getSeatBlockTime());
			selectPS.setInt(++index, namespaceProfileDTO.getMaxSeatPerTransaction());
			selectPS.setInt(++index, namespaceProfileDTO.getTimeFormat());
			selectPS.setInt(++index, namespaceProfileDTO.getAdvanceBookingDays() != 0 ? namespaceProfileDTO.getAdvanceBookingDays() : 30);
			selectPS.setString(++index, namespaceProfileDTO.getPnrStartCode());
			selectPS.setString(++index, namespaceProfileDTO.getDateFormat());
			selectPS.setString(++index, StringUtil.isNotNull(namespaceProfileDTO.getSendarMailName()) ? namespaceProfileDTO.getSendarMailName() : "Bus Ticket");
			selectPS.setString(++index, StringUtil.isNotNull(namespaceProfileDTO.getSendarSMSName()) ? namespaceProfileDTO.getSendarSMSName() : "BUSTKT");
			selectPS.setString(++index, StringUtil.isNotNull(namespaceProfileDTO.getWhatsappSenderName()) ? namespaceProfileDTO.getWhatsappSenderName() : "Bus Ticket");
			selectPS.setString(++index, StringUtil.isNotNull(namespaceProfileDTO.getEmailCopyAddress()) ? namespaceProfileDTO.getEmailCopyAddress() : "NA");
			selectPS.setInt(++index, namespaceProfileDTO.getSmsProvider().getId());
			selectPS.setInt(++index, namespaceProfileDTO.getWhatsappProvider().getId());
			selectPS.setString(++index, namespaceProfileDTO.getDynamicPriceProvidersIds());
			selectPS.setString(++index, namespaceProfileDTO.getDomainURL());
			selectPS.setInt(++index, namespaceProfileDTO.getPhoneBookingTicketNotificationMinitues());
			selectPS.setInt(++index, namespaceProfileDTO.getBoardingReportingMinitues());
			selectPS.setInt(++index, namespaceProfileDTO.getPhoneBookingCancellationBlockMinutes());
			selectPS.setInt(++index, namespaceProfileDTO.getInstantCancellationMinitues());
			selectPS.setString(++index, namespaceProfileDTO.getCancellationTimeType());
			selectPS.setInt(++index, namespaceProfileDTO.getTrackbusMinutes());
			selectPS.setString(++index, getFareRuleIds(namespaceProfileDTO.getFareRule()));
			selectPS.setInt(++index, namespaceProfileDTO.getState().getId());
			selectPS.setString(++index, namespaceProfileDTO.getApiTripChartAllPnrUserIds());
			selectPS.setString(++index, namespaceProfileDTO.getApiTripInfoUserIds());
			selectPS.setString(++index, namespaceProfileDTO.getApiTicketTransferUserIds());
			selectPS.setString(++index, namespaceProfileDTO.getApiTripChartUserIds());
			selectPS.setString(++index, namespaceProfileDTO.getCancellationChargeTaxExceptionUserIds());
			selectPS.setInt(++index, namespaceProfileDTO.getPaymentReceiptAcknowledgeProcess());
			selectPS.setString(++index, getGroupIds(namespaceProfileDTO.getOtpVerifyGroup()));
			selectPS.setString(++index, getGroupIds(namespaceProfileDTO.getExpirePasswordGroup()));
			selectPS.setString(++index, getGroupIds(namespaceProfileDTO.getFareRuleExceptionGroup()));
			selectPS.setString(++index, getGroupIds(namespaceProfileDTO.getInstantCancellationGroup()));
			selectPS.setString(++index, getGroupIds(namespaceProfileDTO.getGstExceptionGroup()));
			selectPS.setString(++index, getOtaPartnerCode(namespaceProfileDTO.getOtaPartnerCode()));
			selectPS.setString(++index, getNotificationContacts(namespaceProfileDTO.getTicketEventNotificationContact()));
			selectPS.setString(++index, getNotificationContacts(namespaceProfileDTO.getTicketAfterTripTimeNotificationContact()));
			selectPS.setString(++index, getNotificationContacts(namespaceProfileDTO.getTripNotificationContact()));
			selectPS.setString(++index, namespaceProfileDTO.getAddress());
			selectPS.setString(++index, namespaceProfileDTO.getCity());
			selectPS.setString(++index, namespaceProfileDTO.getPincode());
			selectPS.setString(++index, namespaceProfileDTO.getSupportNumber());
			selectPS.setInt(++index, namespaceProfileDTO.getRescheduleOverrideAllowDays());
			selectPS.setInt(++index, namespaceProfileDTO.getExpirePasswordDays());
			selectPS.setInt(++index, namespaceProfileDTO.getTicketRescheduleMaxCount());
			selectPS.setInt(++index, namespaceProfileDTO.getSearchPastDayCount());
			selectPS.setInt(++index, namespaceProfileDTO.getAllowDirectLogin());
			selectPS.setString(++index, namespaceProfileDTO.getJob());
			selectPS.setInt(++index, BooleanUtils.toInteger(namespaceProfileDTO.isRechargeAutoApprovalFlag()));
			selectPS.setInt(++index, authDTO.getUser().getId());
			selectPS.setInt(++index, authDTO.getNamespace().getId());
			status = selectPS.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return status;
	}

	public NamespaceProfileDTO getNamespaceProfile(AuthDTO authDTO) {

		NamespaceProfileDTO profileDTO = new NamespaceProfileDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT  cancellation_commission_revoke_flag , cancellation_charge_tax_flag, notification_sms_flag , notification_email_flag , notification_whatsapp_flag, notification_to_alternate_mobile_flag, no_fare_sms_flag, is_alias_namespace_flag, seat_block_time , max_seat_per_transaction ,seat_gender_restriction, extra_commission_flag , time_format , advance_booking_days, reporting_days,  pnr_start_code, pnr_generate_type_code, date_format , sender_mail_name , sendar_sms_name, whatsapp_sender_name, sms_provider_id, whatsapp_provider_id, dynamic_price_provider, email_copy_address, domain_url , phone_ticket_notification_before_minitues , boarding_reporting_minitues, phone_booking_cancellation_block_minutes, instant_cancellation_minitues, travel_status_open_minutes, cancellation_time_type, trackbus_minutes, whatsapp_number, whatsapp_url, whatsapp_datetime, mobile_number_mask, fare_rule_id, state_id, allow_api_trip_info, allow_api_ticket_transfer, allow_api_trip_chart, allow_api_trip_chart_all_pnr, cancellation_charge_tax_exception_user, payment_receipt_acknowledge_process, otp_verify_group, expire_password_group, fare_rule_exception_group, instant_cancellation_user_group, gst_exception_group, ota_partner_code, ticket_event_notification_contact, ticket_after_trip_time_notification_contact, trip_notification_contact, address, city, pincode, support_number, reschedule_override_allow_days, expire_password_days, ticket_reschedule_max_count, search_past_day_count, allow_direct_login, job, recharge_auto_approval_flag FROM namespace_settings WHERE namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet resultSet = selectPS.executeQuery();
			if (resultSet.next()) {
				profileDTO.setCancellationCommissionRevokeFlag(resultSet.getBoolean("cancellation_commission_revoke_flag"));
				profileDTO.setCancellationChargeTaxFlag(resultSet.getBoolean("cancellation_charge_tax_flag"));
				profileDTO.setAllowExtraCommissionFlag(resultSet.getBoolean("extra_commission_flag"));
				profileDTO.setSmsNotificationFlagCode(resultSet.getString("notification_sms_flag"));
				profileDTO.setEmailNotificationFlag(resultSet.getBoolean("notification_email_flag"));
				profileDTO.setWhatsappNotificationFlagCode(resultSet.getString("notification_whatsapp_flag"));
				profileDTO.setNotificationToAlternateMobileFlagCode(resultSet.getString("notification_to_alternate_mobile_flag"));
				profileDTO.setNoFareSMSFlag(resultSet.getInt("no_fare_sms_flag"));
				profileDTO.setAliasNamespaceFlag(resultSet.getBoolean("is_alias_namespace_flag"));
				profileDTO.setSeatBlockTime(resultSet.getInt("seat_block_time"));
				profileDTO.setMaxSeatPerTransaction(resultSet.getInt("max_seat_per_transaction"));
				profileDTO.setSeatGendarRestriction(SeatGenderRestrictionEM.getSeatGendarRestrictionEM(resultSet.getString("seat_gender_restriction")));
				profileDTO.setTimeFormat(resultSet.getInt("time_format"));
				profileDTO.setAdvanceBookingDays(resultSet.getInt("advance_booking_days"));
				profileDTO.setReportingDays(resultSet.getInt("reporting_days"));
				profileDTO.setPnrStartCode(resultSet.getString("pnr_start_code"));
				profileDTO.setPnrGenerateType(PNRGenerateTypeEM.getPNRGenerateTypeEM(resultSet.getString("pnr_generate_type_code")));
				profileDTO.setDateFormat(resultSet.getString("date_format"));
				profileDTO.setSendarMailName(resultSet.getString("sender_mail_name"));
				profileDTO.setSendarSMSName(resultSet.getString("sendar_sms_name"));
				profileDTO.setWhatsappSenderName(resultSet.getString("whatsapp_sender_name"));
				profileDTO.setSmsProvider(SMSProviderEM.getSMSProviderEM(resultSet.getInt("sms_provider_id")));
				profileDTO.setWhatsappProvider(WhatsappProviderEM.getWhatsappProviderEM(resultSet.getInt("whatsapp_provider_id")));
				profileDTO.setDynamicPriceProviders(getDynamicPriceProviders(resultSet.getString("dynamic_price_provider")));
				profileDTO.setEmailCopyAddress(resultSet.getString("email_copy_address"));
				profileDTO.setDomainURL(resultSet.getString("domain_url"));
				profileDTO.setPhoneBookingTicketNotificationMinitues(resultSet.getInt("phone_ticket_notification_before_minitues"));
				profileDTO.setBoardingReportingMinitues(resultSet.getInt("boarding_reporting_minitues"));
				profileDTO.setPhoneBookingCancellationBlockMinutes(resultSet.getInt("phone_booking_cancellation_block_minutes"));
				profileDTO.setInstantCancellationMinitues(resultSet.getInt("instant_cancellation_minitues"));
				profileDTO.setTravelStatusOpenMinutes(resultSet.getInt("travel_status_open_minutes"));
				profileDTO.setCancellationTimeType(resultSet.getString("cancellation_time_type"));
				profileDTO.setTrackbusMinutes(resultSet.getInt("trackbus_minutes"));
				profileDTO.setMobileNumberMask(resultSet.getString("mobile_number_mask"));
				profileDTO.setWhatsappNumber(resultSet.getString("whatsapp_number"));
				profileDTO.setWhatsappUrl(resultSet.getString("whatsapp_url"));
				profileDTO.setTicketRescheduleMaxCount(resultSet.getInt("ticket_reschedule_max_count"));
				profileDTO.setSearchPastDayCount(resultSet.getInt("search_past_day_count"));

				String whatsappDateTime = resultSet.getString("whatsapp_datetime");
				profileDTO.setWhatsappDatetime(StringUtil.isNotNull(whatsappDateTime) ? DateUtil.getDateTime(whatsappDateTime).format("YYYY-MM-DD hh:mm:ss") : whatsappDateTime);

				StateDTO stateDTO = new StateDTO();
				stateDTO.setId(resultSet.getInt("state_id"));
				profileDTO.setState(stateDTO);

				String fareRuleIds = resultSet.getString("fare_rule_id");
				List<FareRuleDTO> fareRuleList = convertFareRule(fareRuleIds);
				profileDTO.setFareRule(fareRuleList);

				List<UserDTO> apiTripInfoUsers = convertUsers(resultSet.getString("allow_api_trip_info"));
				List<UserDTO> apiTicketTransferUsers = convertUsers(resultSet.getString("allow_api_ticket_transfer"));
				List<UserDTO> apiTripChartUsers = convertUsers(resultSet.getString("allow_api_trip_chart"));
				List<UserDTO> apiTripChartAllPnrUsers = convertUsers(resultSet.getString("allow_api_trip_chart_all_pnr"));
				profileDTO.setAllowApiTripInfo(apiTripInfoUsers);
				profileDTO.setAllowApiTicketTransfer(apiTicketTransferUsers);
				profileDTO.setAllowApiTripChart(apiTripChartUsers);
				profileDTO.setAllowApiTripChartAllPnr(apiTripChartAllPnrUsers);

				List<UserDTO> cancellationChargeTaxExceptionUsers = convertUsers(resultSet.getString("cancellation_charge_tax_exception_user"));
				profileDTO.setCancellationChargeTaxException(cancellationChargeTaxExceptionUsers);

				profileDTO.setPaymentReceiptAcknowledgeProcess(resultSet.getInt("payment_receipt_acknowledge_process"));
				List<GroupDTO> otpVerifyGroups = convertGroup(resultSet.getString("otp_verify_group"));
				List<GroupDTO> expirePasswordGroups = convertGroup(resultSet.getString("expire_password_group"));
				List<GroupDTO> fareRuleExceptionGroups = convertGroup(resultSet.getString("fare_rule_exception_group"));
				List<GroupDTO> instantCancellationGroups = convertGroup(resultSet.getString("instant_cancellation_user_group"));
				List<GroupDTO> gstExceptionGroups = convertGroup(resultSet.getString("gst_exception_group"));
				profileDTO.setFareRuleExceptionGroup(fareRuleExceptionGroups);
				profileDTO.setOtpVerifyGroup(otpVerifyGroups);
				profileDTO.setExpirePasswordGroup(expirePasswordGroups);
				profileDTO.setInstantCancellationGroup(instantCancellationGroups);
				profileDTO.setGstExceptionGroup(gstExceptionGroups);

				Map<String, String> otaPartnerCode = convertOtaPartnerCode(resultSet.getString("ota_partner_code"));
				profileDTO.setOtaPartnerCode(otaPartnerCode);

				List<String> ticketEventNotificationContact = convertNotificationContacts(resultSet.getString("ticket_event_notification_contact"));
				List<String> ticketAfterTripTimeContact = convertNotificationContacts(resultSet.getString("ticket_after_trip_time_notification_contact"));
				List<String> tripNotificationContact = convertNotificationContacts(resultSet.getString("trip_notification_contact"));
				profileDTO.setTicketEventNotificationContact(ticketEventNotificationContact);
				profileDTO.setTicketAfterTripTimeNotificationContact(ticketAfterTripTimeContact);
				profileDTO.setTripNotificationContact(tripNotificationContact);

				profileDTO.setAddress(resultSet.getString("address"));
				profileDTO.setCity(resultSet.getString("city"));
				profileDTO.setPincode(resultSet.getString("pincode"));
				profileDTO.setSupportNumber(resultSet.getString("support_number"));
				profileDTO.setRescheduleOverrideAllowDays(resultSet.getInt("reschedule_override_allow_days"));
				profileDTO.setExpirePasswordDays(resultSet.getInt("expire_password_days"));
				profileDTO.setAllowDirectLogin(resultSet.getInt("allow_direct_login"));
				profileDTO.setJob(resultSet.getString("job"));
				profileDTO.setRechargeAutoApprovalFlag(resultSet.getBoolean("recharge_auto_approval_flag"));
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return profileDTO;
	}

	public void getNamespaceDTO(NamespaceDTO namespaceDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (namespaceDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, context_token, alias, lookup_id, active_flag FROM namespace where active_flag = 1 and id = ?");
				selectPS.setInt(1, namespaceDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, code, name, context_token, alias, lookup_id, active_flag FROM namespace where active_flag = 1 and code = ?");
				selectPS.setString(1, namespaceDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				namespaceDTO.setId(selectRS.getInt("id"));
				namespaceDTO.setCode(selectRS.getString("code"));
				namespaceDTO.setName(selectRS.getString("name"));
				namespaceDTO.setContextToken(selectRS.getString("context_token"));
				namespaceDTO.setAliasCode(selectRS.getString("alias"));
				namespaceDTO.setLookupId(selectRS.getInt("lookup_id"));
				namespaceDTO.setActiveFlag(selectRS.getInt("active_flag"));

				@Cleanup
				PreparedStatement selectProfilePS = connection.prepareStatement("SELECT cancellation_commission_revoke_flag, cancellation_charge_tax_flag, notification_sms_flag , notification_email_flag , notification_whatsapp_flag, notification_to_alternate_mobile_flag, no_fare_sms_flag, is_alias_namespace_flag, seat_block_time , max_seat_per_transaction ,seat_gender_restriction, extra_commission_flag, time_format , pnr_start_code, pnr_generate_type_code, date_format , advance_booking_days, reporting_days, sender_mail_name , sendar_sms_name , whatsapp_sender_name, sms_provider_id, whatsapp_provider_id, dynamic_price_provider, email_copy_address, domain_url , phone_ticket_notification_before_minitues , boarding_reporting_minitues, phone_booking_cancellation_block_minutes, instant_cancellation_minitues, travel_status_open_minutes, cancellation_time_type, trackbus_minutes, whatsapp_number, whatsapp_url, whatsapp_datetime, mobile_number_mask, notification_subscription_type_id, fare_rule_id, state_id, allow_api_trip_info, allow_api_ticket_transfer, allow_api_trip_chart, allow_api_trip_chart_all_pnr, cancellation_charge_tax_exception_user, payment_receipt_acknowledge_process, otp_verify_group, expire_password_group, fare_rule_exception_group, instant_cancellation_user_group, gst_exception_group, ota_partner_code, ticket_event_notification_contact, ticket_after_trip_time_notification_contact, trip_notification_contact, address, city, pincode, support_number, reschedule_override_allow_days, expire_password_days, ticket_reschedule_max_count, search_past_day_count, allow_direct_login, recharge_auto_approval_flag FROM namespace_settings WHERE namespace_id = ?");
				selectProfilePS.setInt(1, namespaceDTO.getId());
				@Cleanup
				ResultSet resultSet = selectProfilePS.executeQuery();
				if (resultSet.next()) {
					NamespaceProfileDTO profileDTO = new NamespaceProfileDTO();
					profileDTO.setCancellationCommissionRevokeFlag(resultSet.getBoolean("cancellation_commission_revoke_flag"));
					profileDTO.setCancellationChargeTaxFlag(resultSet.getBoolean("cancellation_charge_tax_flag"));
					profileDTO.setAllowExtraCommissionFlag(resultSet.getBoolean("extra_commission_flag"));
					profileDTO.setSmsNotificationFlagCode(resultSet.getString("notification_sms_flag"));
					profileDTO.setEmailNotificationFlag(resultSet.getBoolean("notification_email_flag"));
					profileDTO.setWhatsappNotificationFlagCode(resultSet.getString("notification_whatsapp_flag"));
					profileDTO.setNotificationToAlternateMobileFlagCode(resultSet.getString("notification_to_alternate_mobile_flag"));
					profileDTO.setNoFareSMSFlag(resultSet.getInt("no_fare_sms_flag"));
					profileDTO.setAliasNamespaceFlag(resultSet.getBoolean("is_alias_namespace_flag"));
					profileDTO.setSeatBlockTime(resultSet.getInt("seat_block_time"));
					profileDTO.setMaxSeatPerTransaction(resultSet.getInt("max_seat_per_transaction"));
					profileDTO.setSeatGendarRestriction(SeatGenderRestrictionEM.getSeatGendarRestrictionEM(resultSet.getString("seat_gender_restriction")));
					profileDTO.setTimeFormat(resultSet.getInt("time_format"));
					profileDTO.setPnrStartCode(resultSet.getString("pnr_start_code"));
					profileDTO.setPnrGenerateType(PNRGenerateTypeEM.getPNRGenerateTypeEM(resultSet.getString("pnr_generate_type_code")));
					profileDTO.setDateFormat(resultSet.getString("date_format"));
					profileDTO.setAdvanceBookingDays(resultSet.getInt("advance_booking_days"));
					profileDTO.setReportingDays(resultSet.getInt("reporting_days"));
					profileDTO.setSendarMailName(resultSet.getString("sender_mail_name"));
					profileDTO.setSendarSMSName(resultSet.getString("sendar_sms_name"));
					profileDTO.setWhatsappSenderName(resultSet.getString("whatsapp_sender_name"));
					profileDTO.setSmsProvider(SMSProviderEM.getSMSProviderEM(resultSet.getInt("sms_provider_id")));
					profileDTO.setWhatsappProvider(WhatsappProviderEM.getWhatsappProviderEM(resultSet.getInt("whatsapp_provider_id")));
					profileDTO.setDynamicPriceProviders(getDynamicPriceProviders(resultSet.getString("dynamic_price_provider")));
					profileDTO.setEmailCopyAddress(resultSet.getString("email_copy_address"));
					profileDTO.setDomainURL(resultSet.getString("domain_url"));
					profileDTO.setPhoneBookingTicketNotificationMinitues(resultSet.getInt("phone_ticket_notification_before_minitues"));
					profileDTO.setBoardingReportingMinitues(resultSet.getInt("boarding_reporting_minitues"));
					profileDTO.setPhoneBookingCancellationBlockMinutes(resultSet.getInt("phone_booking_cancellation_block_minutes"));
					profileDTO.setInstantCancellationMinitues(resultSet.getInt("instant_cancellation_minitues"));
					profileDTO.setTravelStatusOpenMinutes(resultSet.getInt("travel_status_open_minutes"));
					profileDTO.setCancellationTimeType(resultSet.getString("cancellation_time_type"));
					profileDTO.setTrackbusMinutes(resultSet.getInt("trackbus_minutes"));
					profileDTO.setMobileNumberMask(resultSet.getString("mobile_number_mask"));
					profileDTO.setWhatsappNumber(resultSet.getString("whatsapp_number"));
					profileDTO.setWhatsappUrl(resultSet.getString("whatsapp_url"));
					profileDTO.setTicketRescheduleMaxCount(resultSet.getInt("ticket_reschedule_max_count"));
					profileDTO.setSearchPastDayCount(resultSet.getInt("search_past_day_count"));

					String whatsappDateTime = resultSet.getString("whatsapp_datetime");
					profileDTO.setWhatsappDatetime(StringUtil.isNotNull(whatsappDateTime) ? DateUtil.getDateTime(whatsappDateTime).format("YYYY-MM-DD hh:mm:ss") : whatsappDateTime);

					List<NotificationSubscriptionTypeEM> list = convertSubscriptionType(resultSet.getString("notification_subscription_type_id"));
					profileDTO.setSubscriptionTypes(list);

					StateDTO stateDTO = new StateDTO();
					stateDTO.setId(resultSet.getInt("state_id"));
					profileDTO.setState(stateDTO);

					String fareRuleIds = resultSet.getString("fare_rule_id");
					List<FareRuleDTO> fareRuleList = convertFareRule(fareRuleIds);
					profileDTO.setFareRule(fareRuleList);

					List<UserDTO> apiTripInfoUsers = convertUsers(resultSet.getString("allow_api_trip_info"));
					List<UserDTO> apiTicketTransferUsers = convertUsers(resultSet.getString("allow_api_ticket_transfer"));
					List<UserDTO> apiTripChartUsers = convertUsers(resultSet.getString("allow_api_trip_chart"));
					List<UserDTO> apiTripChartAllPnrUsers = convertUsers(resultSet.getString("allow_api_trip_chart_all_pnr"));
					profileDTO.setAllowApiTripInfo(apiTripInfoUsers);
					profileDTO.setAllowApiTicketTransfer(apiTicketTransferUsers);
					profileDTO.setAllowApiTripChart(apiTripChartUsers);
					profileDTO.setAllowApiTripChartAllPnr(apiTripChartAllPnrUsers);

					List<UserDTO> cancellationChargeTaxExceptionUsers = convertUsers(resultSet.getString("cancellation_charge_tax_exception_user"));
					profileDTO.setCancellationChargeTaxException(cancellationChargeTaxExceptionUsers);

					profileDTO.setPaymentReceiptAcknowledgeProcess(resultSet.getInt("payment_receipt_acknowledge_process"));
					List<GroupDTO> otpVerifyGroups = convertGroup(resultSet.getString("otp_verify_group"));
					List<GroupDTO> expirePasswordGroups = convertGroup(resultSet.getString("expire_password_group"));
					List<GroupDTO> fareRuleExceptionGroups = convertGroup(resultSet.getString("fare_rule_exception_group"));
					List<GroupDTO> instantCancellationGroups = convertGroup(resultSet.getString("instant_cancellation_user_group"));
					List<GroupDTO> gstExceptionGroups = convertGroup(resultSet.getString("gst_exception_group"));
					profileDTO.setFareRuleExceptionGroup(fareRuleExceptionGroups);
					profileDTO.setOtpVerifyGroup(otpVerifyGroups);
					profileDTO.setExpirePasswordGroup(expirePasswordGroups);
					profileDTO.setInstantCancellationGroup(instantCancellationGroups);
					profileDTO.setGstExceptionGroup(gstExceptionGroups);

					Map<String, String> otaPartnerCode = convertOtaPartnerCode(resultSet.getString("ota_partner_code"));
					profileDTO.setOtaPartnerCode(otaPartnerCode);

					List<String> ticketEventNotificationContact = convertNotificationContacts(resultSet.getString("ticket_event_notification_contact"));
					List<String> ticketAfterTripTimeContact = convertNotificationContacts(resultSet.getString("ticket_after_trip_time_notification_contact"));
					List<String> tripNotificationContact = convertNotificationContacts(resultSet.getString("trip_notification_contact"));
					profileDTO.setTicketEventNotificationContact(ticketEventNotificationContact);
					profileDTO.setTicketAfterTripTimeNotificationContact(ticketAfterTripTimeContact);
					profileDTO.setTripNotificationContact(tripNotificationContact);

					profileDTO.setAddress(resultSet.getString("address"));
					profileDTO.setCity(resultSet.getString("city"));
					profileDTO.setPincode(resultSet.getString("pincode"));
					profileDTO.setSupportNumber(resultSet.getString("support_number"));
					profileDTO.setRescheduleOverrideAllowDays(resultSet.getInt("reschedule_override_allow_days"));
					profileDTO.setExpirePasswordDays(resultSet.getInt("expire_password_days"));
					profileDTO.setAllowDirectLogin(resultSet.getInt("allow_direct_login"));
					profileDTO.setRechargeAutoApprovalFlag(resultSet.getBoolean("recharge_auto_approval_flag"));
					namespaceDTO.setProfile(profileDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateUserNamespaceMap(AuthDTO authDTO, NamespaceDTO namespaceDTO, UserDTO userDTO, String action) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL  EZEE_SP_USER_NAMESPACE_MAP_IUD(?,?,?,  ?,?,?)}");
			callableStatement.setString(++pindex, namespaceDTO.getCode());
			callableStatement.setString(++pindex, userDTO.getCode());
			callableStatement.setInt(++pindex, action.equals("add") ? 1 : 0);
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public List<NamespaceDTO> getAllUserNamespaceMap(AuthDTO authDTO, UserDTO userDTO) {
		List<NamespaceDTO> list = new ArrayList<NamespaceDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT namespace_id FROM user_namespace_mapping WHERE user_id = ? AND active_flag = 1");
			selectPS.setInt(1, userDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceDTO dto = new NamespaceDTO();
				dto.setId(selectRS.getInt("namespace_id"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public boolean checkUserNamespaceMapping(AuthDTO authDTO, NamespaceDTO namespaceDTO) {

		boolean checkMapping = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM user_namespace_mapping WHERE namespace_id = ? AND user_id = ? AND active_flag = 1");
			selectPS.setInt(1, namespaceDTO.getId());
			selectPS.setInt(2, authDTO.getUser().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				checkMapping = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return checkMapping;
	}

	public void updateSubscriptionType(AuthDTO authDTO, List<NotificationSubscriptionTypeEM> subscriptionTypeList) {
		int status = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE namespace_settings SET notification_subscription_type_id = ? WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, getSubscriptionTypeIds(subscriptionTypeList));
			selectPS.setInt(2, authDTO.getNamespace().getId());
			status = selectPS.executeUpdate();
			if (status == 0) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateNamespaceWhatsapp(AuthDTO authDTO, NamespaceProfileDTO profileDTO) {
		int status = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE namespace_settings SET whatsapp_number = ?, whatsapp_url = ?, whatsapp_datetime = ? WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, profileDTO.getWhatsappNumber());
			selectPS.setString(2, profileDTO.getWhatsappUrl());
			selectPS.setString(3, profileDTO.getWhatsappDatetime());
			selectPS.setInt(4, authDTO.getNamespace().getId());
			status = selectPS.executeUpdate();
			if (status == 0) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<UserDTO> convertUsers(String users) {
		List<UserDTO> permitAllTicketUsers = new ArrayList<>();
		if (StringUtil.isNotNull(users)) {
			List<String> userIds = Arrays.asList(users.split(Text.COMMA));
			for (String userId : userIds) {
				if (StringUtil.isNull(userId) || Numeric.ZERO.equals(userId)) {
					continue;
				}

				UserDTO userDTO = new UserDTO();
				userDTO.setId(StringUtil.getIntegerValue(userId));
				permitAllTicketUsers.add(userDTO);
			}
		}
		return permitAllTicketUsers;
	}

	private String getSubscriptionTypeIds(List<NotificationSubscriptionTypeEM> subscriptionTypes) {
		StringBuilder subscriptionsType = new StringBuilder();
		for (NotificationSubscriptionTypeEM type : subscriptionTypes) {
			subscriptionsType.append(type.getId());
			subscriptionsType.append(Text.COMMA);
		}
		String subscriptionTypeIds = subscriptionsType.toString();
		if (StringUtil.isNull(subscriptionTypeIds)) {
			subscriptionTypeIds = Text.NA;
		}
		return subscriptionTypeIds;
	}

	private List<NotificationSubscriptionTypeEM> convertSubscriptionType(String subscriptionTypeIds) {
		List<NotificationSubscriptionTypeEM> subscriptionTypes = new ArrayList<>();
		if (StringUtil.isNotNull(subscriptionTypeIds)) {
			for (String subscriptionTypeId : subscriptionTypeIds.split(Text.COMMA)) {
				NotificationSubscriptionTypeEM subscriptionType = NotificationSubscriptionTypeEM.getSubscriptionTypeEM(Integer.valueOf(subscriptionTypeId));
				if (subscriptionType == null) {
					continue;
				}
				subscriptionTypes.add(subscriptionType);
			}
		}
		return subscriptionTypes;
	}

	private String getGroupIds(List<GroupDTO> groupList) {
		StringBuilder group = new StringBuilder();
		for (GroupDTO groupDTO : groupList) {
			group.append(groupDTO.getId());
			group.append(Text.COMMA);
		}
		String groupIds = group.toString();
		if (StringUtil.isNull(groupIds)) {
			groupIds = Text.NA;
		}
		return groupIds;
	}

	private List<GroupDTO> convertGroup(String groupIds) {
		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (StringUtil.isNotNull(groupIds)) {
			for (String groupId : groupIds.split(Text.COMMA)) {
				if (StringUtil.isNull(groupId) || Numeric.ZERO.equals(groupId)) {
					continue;
				}
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(StringUtil.getIntegerValue(groupId));
				groupList.add(groupDTO);
			}
		}
		return groupList;
	}

	private String getOtaPartnerCode(Map<String, String> otaPartnerDetailsMap) {
		StringBuilder otaPartnerCodes = new StringBuilder();
		if (otaPartnerDetailsMap != null) {
			for (Entry<String, String> otaPartnerMap : otaPartnerDetailsMap.entrySet()) {

				UserTagEM userTag = UserTagEM.getUserTagEM(otaPartnerMap.getKey());
				String partnerCode = otaPartnerMap.getValue();
				if (userTag == null || StringUtil.isNull(partnerCode)) {
					continue;
				}
				if (otaPartnerCodes.length() > 0) {
					otaPartnerCodes.append(Text.COMMA);
				}
				otaPartnerCodes.append(userTag.getCode() + Text.COLON + partnerCode);
			}
		}

		String otaPartner = Text.NA;
		if (StringUtil.isNotNull(otaPartnerCodes.toString())) {
			otaPartner = otaPartnerCodes.toString();
		}
		return otaPartner;
	}

	private Map<String, String> convertOtaPartnerCode(String otaPartnerCodes) {
		Map<String, String> otaPartnerDetailsMap = Maps.newHashMap();
		if (StringUtil.isNotNull(otaPartnerCodes)) {
			for (String otaPartner : Arrays.asList(otaPartnerCodes.split(Text.COMMA))) {

				String[] otaPartnerDetails = otaPartner.split("\\:");
				if (otaPartnerDetails.length != 2) {
					continue;
				}
				String userTagCode = otaPartnerDetails[0];
				String partnerCode = otaPartnerDetails[1];
				otaPartnerDetailsMap.put(userTagCode, partnerCode);
			}
		}
		return otaPartnerDetailsMap;
	}

	private String getNotificationContacts(List<String> notificationContact) {
		StringBuilder contacts = new StringBuilder();
		for (String mobileNumber : notificationContact) {
			if (StringUtil.isNull(mobileNumber)) {
				continue;
			}
			if (contacts.length() > 0) {
				contacts.append(Text.COMMA);
			}
			contacts.append(mobileNumber);
		}
		String ticketEventContactNumber = Text.NA;
		if (StringUtil.isNotNull(contacts.toString())) {
			ticketEventContactNumber = contacts.toString();
		}
		return ticketEventContactNumber;
	}

	private List<String> convertNotificationContacts(String notificationContacts) {
		List<String> list = new ArrayList<>();
		if (StringUtil.isNotNull(notificationContacts)) {
			for (String mobileNumber : notificationContacts.split(Text.COMMA)) {
				if (StringUtil.isNull(mobileNumber)) {
					continue;
				}
				list.add(mobileNumber);
			}
		}
		return list;
	}

	private String getFareRuleIds(List<FareRuleDTO> fareRuleList) {
		StringBuilder fareRuleIds = new StringBuilder();
		if (fareRuleList != null) {
			for (FareRuleDTO fareRuleDTO : fareRuleList) {
				if (fareRuleDTO.getId() == 0) {
					continue;
				}
				if (fareRuleIds.length() > 0) {
					fareRuleIds.append(Text.COMMA);
				}
				fareRuleIds.append(fareRuleDTO.getId());
			}
		}
		String fareRules = fareRuleIds.toString();
		if (StringUtil.isNull(fareRules)) {
			fareRules = Text.NA;
		}
		return fareRules;
	}

	private List<FareRuleDTO> convertFareRule(String fareRuleIds) {
		List<FareRuleDTO> fareRuleList = new ArrayList<>();
		if (StringUtil.isNotNull(fareRuleIds)) {
			for (String fareRuleId : fareRuleIds.split(Text.COMMA)) {
				if (StringUtil.isNull(fareRuleId) || fareRuleId.equals(Numeric.ZERO)) {
					continue;
				}
				FareRuleDTO fareRuleDTO = new FareRuleDTO();
				fareRuleDTO.setId(Integer.valueOf(fareRuleId));
				fareRuleList.add(fareRuleDTO);
			}
		}
		return fareRuleList;

	}

	private List<DynamicPriceProviderEM> getDynamicPriceProviders(String dynamicPriceProviderIdString) {
		List<DynamicPriceProviderEM> dynamicPriceProviders = new ArrayList<>();
		if (StringUtil.isNotNull(dynamicPriceProviderIdString)) {
			for (String dynamicPriceProviderId : dynamicPriceProviderIdString.split(Text.COMMA)) {
				if (StringUtil.isNull(dynamicPriceProviderId) || dynamicPriceProviderId.equals(Numeric.ZERO)) {
					continue;
				}
				dynamicPriceProviders.add(DynamicPriceProviderEM.getDynamicPriceProviderEM(Integer.valueOf(dynamicPriceProviderId)));
			}
		}
		return dynamicPriceProviders;

	}
}
