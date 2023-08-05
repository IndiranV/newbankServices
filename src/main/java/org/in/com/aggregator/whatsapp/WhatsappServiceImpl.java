package org.in.com.aggregator.whatsapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dao.NotificationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NotificationConfigService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class WhatsappServiceImpl implements WhatsappService {
	private static Pattern contentPattern = Pattern.compile("\\$\\{([a-zA-Z]+)\\|?.*?}", Pattern.CASE_INSENSITIVE);

	@Autowired
	EmailService emailService;
	@Autowired
	NotificationConfigService configService;

	private static final Logger LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.whatsapp.WhatsappService");
	private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("\\d{10}");

	public boolean sendTripJourneyTracking(AuthDTO authDTO, Map<String, String> dataModel, NotificationTypeEM notificationType) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			/** Find gateway client and sender code */
			WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, notificationType, NotificationMediumEM.WHATS_APP);
			if (StringUtil.isNull(templateConfig.getTemplateDltCode())) {
				LOGGER.error("whatsapp template not found {} {}", notificationType.getCode());
				throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
			}
			dataModel.put("domainUrl", dataModel.get("domainUrl").replace("www", Text.EMPTY));
			/** Add variable to place holder */
			List<String> placeholders = new ArrayList<String>();
			Matcher matcher = contentPattern.matcher(templateConfig.getContent());
			while (matcher.find()) {
				placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
			}
			String header = dataModel.get("namespaceName");

			/** Validate mobile number */
			String mobileNumber = dataModel.get("mobileNumber").trim();
			String alternateMobile = dataModel.containsKey("alternateMobile") && StringUtil.isNotNull(dataModel.get("alternateMobile")) ? dataModel.get("alternateMobile").trim() : Text.EMPTY;

			if ((!StringUtil.isValidMobileNumber(mobileNumber)) || (StringUtils.isNotBlank(mobileNumber) && MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE)) {
				LOGGER.error("Invalid Mobile Number {}", mobileNumber);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			// validate and append alternate mobile
			mobileNumber = checkAndAppendAlternateMobile(authDTO, mobileNumber, alternateMobile);

			/** Send message */
			smsResponse = whatsappClient.send(templateConfig, mobileNumber, header, placeholders);
			LOGGER.info("res: {} {}", mobileNumber, smsResponse.toString());

			/** Save notification */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("ticketCode"));
			notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
			notificationDTO.setNotificationType(notificationType);
			notificationDTO.setParticipantAddress(mobileNumber);
			notificationDTO.setTransactionCount(mobileNumber.split(Text.COMMA).length);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveNotification(authDTO, notificationDTO);
		}
		catch (ServiceException e) {
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, notificationType);
		}
		return status;
	}

	public boolean sendWhatsappNumberVerification(AuthDTO authDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		boolean status = false;
		NotificationTypeEM notificationType = NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION;
		try {
			/** Find gateway client and sender code */
			WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, notificationType, NotificationMediumEM.WHATS_APP);
			if (StringUtil.isNull(templateConfig.getTemplateDltCode())) {
				LOGGER.error("whatsapp template not found {} {}", notificationType.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}
			/** Add variable to place holder */
			List<String> placeholders = new ArrayList<String>();
			Matcher matcher = contentPattern.matcher(templateConfig.getContent());
			while (matcher.find()) {
				placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
			}
			String header = dataModel.get("namespaceName");

			/** Validate mobile number */
			String mobileNumber = dataModel.get("mobileNumber").trim();
			if ((!StringUtil.isValidMobileNumber(mobileNumber)) || (StringUtils.isNotBlank(mobileNumber) && MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE)) {
				LOGGER.error("Invalid Mobile Number {}", mobileNumber);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			/** Send message */
			smsResponse = whatsappClient.send(templateConfig, mobileNumber, header, placeholders);
			LOGGER.info("res: {} {}", mobileNumber, smsResponse.toString());

			/** Save notification */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(mobileNumber);
			notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
			notificationDTO.setNotificationType(notificationType);
			notificationDTO.setParticipantAddress(mobileNumber);
			notificationDTO.setTransactionCount(mobileNumber.split(Text.COMMA).length);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveNotification(authDTO, notificationDTO);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.FEEDBACK.getCode());
			}
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, notificationType);
		}
		return status;
	}

	public boolean sendCustomerTicketEvent(AuthDTO authDTO, Map<String, String> dataModel, NotificationTemplateConfigDTO templateConfig) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			/** Find gateway client and sender code */
			WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
			if (StringUtil.isNull(templateConfig.getTemplateDltCode())) {
				LOGGER.error("whatsapp template not found {} {}", templateConfig.getNotificationType().getCode());
				throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
			}
			/** Add variable to place holder */
			List<String> placeholders = new ArrayList<String>();
			Matcher matcher = contentPattern.matcher(templateConfig.getContent());
			while (matcher.find()) {
				placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
			}
			String header = dataModel.get("operatorName");

			/** Validate mobile number */
			String mobileNumber = dataModel.get("mobileNumber").trim();
			if ((!StringUtil.isValidMobileNumber(mobileNumber)) || (StringUtils.isNotBlank(mobileNumber) && MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE)) {
				LOGGER.error("Invalid Mobile Number {}", mobileNumber);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			/** Send message */
			smsResponse = whatsappClient.send(templateConfig, mobileNumber, header, placeholders);
			LOGGER.info("res: {} {}", mobileNumber, smsResponse.toString());

			/** Save notification */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("pnr"));
			notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
			notificationDTO.setNotificationType(templateConfig.getNotificationType());
			notificationDTO.setParticipantAddress(mobileNumber);
			notificationDTO.setTransactionCount(mobileNumber.split(Text.COMMA).length);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveNotification(authDTO, notificationDTO);
		}
		catch (ServiceException e) {
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, templateConfig.getNotificationType());
		}
		return status;
	}

	private void saveNotification(AuthDTO authDTO, NotificationDTO dto) {
		try {
			NotificationDAO dao = new NotificationDAO();
			dao.insertNotification(authDTO, dto);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	private boolean checkAndsendFailureMessageGatewayEmail(AuthDTO authDTO, SmsResponse smsResponse, NotificationTypeEM notificationType) {
		boolean isWhatsappMessageSent = Text.TRUE;
		try {
			if (smsResponse != null && smsResponse.getCode().equals(Constants.WHATSAPP_PROVIDER_WIZHCOMM)) {
				if (!smsResponse.getResponseV2().contains("200")) {
					isWhatsappMessageSent = Text.FALSE;
				}
			}
			else if (smsResponse != null && smsResponse.getCode().equals(Constants.WHATSAPP_PROVIDER_QIKBERRY)) {
				if (!smsResponse.getResponseV2().contains("true")) {
					isWhatsappMessageSent = Text.FALSE;
				}
			}
			else if (smsResponse == null) {
				isWhatsappMessageSent = Text.FALSE;
			}
		}
		catch (Exception e) {
			isWhatsappMessageSent = Text.FALSE;
			LOGGER.error("{} - Error {}", smsResponse.getResponseV2(), e);
		}
		finally {
			if (!isWhatsappMessageSent && smsResponse != null) {
				Map<String, Object> dataModel = new HashMap<>();
				dataModel.put("url", smsResponse.getUrl());
				dataModel.put("request", smsResponse.getRequest());
				dataModel.put("response", smsResponse.getResponseV2());
				dataModel.put("content", smsResponse.getContent());
				dataModel.put("gatewayName", smsResponse.getCode());
				dataModel.put("namespace", authDTO.getNamespace().getName());
				dataModel.put("notificationType", notificationType.getDescription());

				emailService.sendFailureSMSGatewayEmail(authDTO, dataModel);
			}
		}
		return isWhatsappMessageSent;
	}

	public boolean sendTicketBookingNotification(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			if (DeviceMediumEM.API_USER.getId() != authDTO.getDeviceMedium().getId() && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_BOOKING.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CONFIRM_BOOKING, NotificationMediumEM.WHATS_APP);

				if (StringUtil.isNull(templateConfig.getCode())) {
					LOGGER.info("whatsapp template not found: {} {} ", authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_BOOKING.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}
				String mobile = ticketDTO.getPassengerMobile();
				dataModel.put("namespaceURL", dataModel.get("namespaceURL").replace("www", Text.EMPTY));

				/** Add variable to place holder */
				List<String> placeholders = new ArrayList<String>();
				Matcher matcher = contentPattern.matcher(templateConfig.getContent());
				while (matcher.find()) {
					placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
				}
				String header = dataModel.get("travelsName");

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
					LOGGER.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					LOGGER.info("Whatsapp mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store data in notification_log table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_BOOKING);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
				notificationDTO.setRequestLog(dataModel.toString());
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveNotification(authDTO, notificationDTO);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CONFIRM_BOOKING.getCode());
			}
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CONFIRM_BOOKING);
		}
		return status;
	}

	@Override
	public boolean sendTicketCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			if (DeviceMediumEM.API_USER.getId() != ticketDTO.getDeviceMedium().getId() && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_CANCELLATION)) {
				WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CONFIRM_CANCELLATION, NotificationMediumEM.WHATS_APP);
				if (StringUtil.isNull(templateConfig.getCode())) {
					LOGGER.info("whatsapp template not found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("name", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("pnr", ticketDTO.getCode());
				dataModel.put("originName", ticketDTO.getFromStation().getName());
				dataModel.put("destinationName", ticketDTO.getToStation().getName());
				dataModel.put("travelDate", DateUtil.parseDateFormat(ticketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
				dataModel.put("seats", ticketDTO.getSeatNames());
				dataModel.put("refund", String.valueOf(ticketDTO.getRefundAmount()));

				/** Add variable to place holder */
				List<String> placeholders = new ArrayList<String>();
				Matcher matcher = contentPattern.matcher(templateConfig.getContent());
				while (matcher.find()) {
					placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
				}
				String header = dataModel.get("travelsName");

				String mobile = ticketDTO.getPassengerMobile();
				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + " mobile:" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
					LOGGER.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					LOGGER.info("Whatsapp mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_CANCELLATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
				notificationDTO.setRequestLog(dataModel.toString());
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveNotification(authDTO, notificationDTO);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
			}
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CONFIRM_CANCELLATION);
		}
		return status;
	}

	private String checkAndAppendAlternateMobile(AuthDTO authDTO, String mobile, String alternateMobile) {
		if (authDTO.getNamespace().getProfile().isNotificationToAlternateMobile(NotificationMediumEM.WHATS_APP) && StringUtils.isNotBlank(alternateMobile) && MOBILE_NUMBER_PATTERN.matcher(alternateMobile).matches() == Boolean.TRUE && !mobile.equals(alternateMobile)) {
			mobile = mobile + Text.COMMA + alternateMobile;
		}
		return mobile;
	}

	@Override
	public Future<SmsResponse> tripDelayNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String delayTime, String supportNumber) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_DELAY_NOTIFICATION, NotificationMediumEM.WHATS_APP);
				if (StringUtil.isNull(templateConfig.getCode())) {
					LOGGER.info("whatsapp template not found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_DELAY_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("reason", reason);
				dataModel.put("delayTime", delayTime);
				dataModel.put("supportNumber", supportNumber);

				/** Add variable to place holder */
				List<String> placeholders = new ArrayList<String>();
				Matcher matcher = contentPattern.matcher(templateConfig.getContent());
				while (matcher.find()) {
					placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
				}
				String header = dataModel.get("travelsName");

				String mobile = ticketDTO.getPassengerMobile();
				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + " mobile:" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
					LOGGER.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					LOGGER.info("Whatsapp mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
				notificationDTO.setRequestLog(dataModel.toString());
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveNotification(authDTO, notificationDTO);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire Whatsapp Message for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_DELAY_NOTIFICATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_DELAY_NOTIFICATION);
			smsResponse.setCode(String.valueOf(status));
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Override
	public Future<SmsResponse> tripCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String supportNumber) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_CANCEL_NOTIFICATION, NotificationMediumEM.WHATS_APP);
				if (StringUtil.isNull(templateConfig.getCode())) {
					LOGGER.info("whatsapp template not found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_CANCEL_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("reason", reason);
				dataModel.put("supportNumber", supportNumber);

				/** Add variable to place holder */
				List<String> placeholders = new ArrayList<String>();
				Matcher matcher = contentPattern.matcher(templateConfig.getContent());
				while (matcher.find()) {
					placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
				}
				String header = dataModel.get("travelsName");

				String mobile = ticketDTO.getPassengerMobile();
				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + " mobile:" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
					LOGGER.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					LOGGER.info("Whatsapp mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
				notificationDTO.setRequestLog(dataModel.toString());
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveNotification(authDTO, notificationDTO);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire Whatsapp Message for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_CANCEL_NOTIFICATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_CANCEL_NOTIFICATION);
			smsResponse.setCode(String.valueOf(status));
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Override
	public Future<SmsResponse> tripEarlyNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String earlyTime, String supportNumber) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_EARLY_NOTIFICATION, NotificationMediumEM.WHATS_APP);
				if (StringUtil.isNull(templateConfig.getCode())) {
					LOGGER.info("whatsapp template not found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_EARLY_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("reason", reason);
				dataModel.put("earlyTime", earlyTime);
				dataModel.put("supportNumber", supportNumber);

				/** Add variable to place holder */
				List<String> placeholders = new ArrayList<String>();
				Matcher matcher = contentPattern.matcher(templateConfig.getContent());
				while (matcher.find()) {
					placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
				}
				String header = dataModel.get("travelsName");

				String mobile = ticketDTO.getPassengerMobile();
				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + " mobile:" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
					LOGGER.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					LOGGER.info("Whatsapp mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
				notificationDTO.setRequestLog(dataModel.toString());
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveNotification(authDTO, notificationDTO);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire Whatsapp Message for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_EARLY_NOTIFICATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_EARLY_NOTIFICATION);
			smsResponse.setCode(String.valueOf(status));
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Override
	public boolean sendTicketCancelNotification(AuthDTO authDTO, TicketDTO repositoryTicketDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			if (DeviceMediumEM.API_USER.getId() != authDTO.getDeviceMedium().getId() && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_CANCELLATION)) {
				WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CONFIRM_CANCELLATION, NotificationMediumEM.WHATS_APP);

				if (StringUtil.isNull(templateConfig.getCode())) {
					LOGGER.info("whatsapp template not found: {} {} ", authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}
				String mobile = repositoryTicketDTO.getPassengerMobile();
				dataModel.put("namespaceURL", dataModel.get("namespaceURL").replace("www", Text.EMPTY));

				/** Add variable to place holder */
				List<String> placeholders = new ArrayList<String>();
				Matcher matcher = contentPattern.matcher(templateConfig.getContent());
				while (matcher.find()) {
					placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
				}
				String header = dataModel.get("travelsName");

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(repositoryTicketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, repositoryTicketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
					LOGGER.info("PNR:" + repositoryTicketDTO.getCode() + smsResponse.toString());
				}
				else {
					LOGGER.info("Whatsapp mobile Not Empty: " + repositoryTicketDTO.getCode());
				}
				/**
				 * Store data in notification_log table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(repositoryTicketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_CANCELLATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
				notificationDTO.setRequestLog(dataModel.toString());
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveNotification(authDTO, notificationDTO);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
			}
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CONFIRM_CANCELLATION);
		}
		return status;
	}

	@Override
	public boolean sendFeedbackSMS(AuthDTO authDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		boolean status = false;
		try {
			WhatsappClient whatsappClient = WhatsappClientFactory.getInstance().getWhatsappClient(authDTO.getNamespace().getProfile().getWhatsappProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.FEEDBACK.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.FEEDBACK, NotificationMediumEM.WHATS_APP);

			if (StringUtil.isNull(templateConfig.getCode())) {
				LOGGER.info("whatsapp template not found: {} {} ", authDTO.getNamespaceCode(), NotificationTypeEM.FEEDBACK.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}
			String mobile = dataModel.get("mobileNumber");

			/** Add variable to place holder */
			List<String> placeholders = new ArrayList<String>();
			Matcher matcher = contentPattern.matcher(templateConfig.getContent());
			while (matcher.find()) {
				placeholders.add(StringUtil.isNull(dataModel.get(matcher.group(1)), Text.HYPHEN));
			}
			String header = dataModel.get("namespaceCode");

			if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
				System.out.println(dataModel.get("ticketCode") + "mobile :" + mobile);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = whatsappClient.send(templateConfig, mobile, header, placeholders);
				LOGGER.info("PNR:" + dataModel.get("ticketCode") + smsResponse.toString());
			}
			else {
				LOGGER.info("Whatsapp mobile Not Empty: " + dataModel.get("ticketCode"));
			}
			/**
			 * Store data in notification_log table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("ticketCode"));
			notificationDTO.setNotificationMode(NotificationMediumEM.WHATS_APP);
			notificationDTO.setNotificationType(NotificationTypeEM.FEEDBACK);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getWhatsappCount(dataModel.toString(), mobile));
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveNotification(authDTO, notificationDTO);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.FEEDBACK.getCode());
			}
			LOGGER.error("", e);
		}
		catch (Exception e) {
			LOGGER.error("", e);
		}
		finally {
			/** Check and send failure email */
			status = checkAndsendFailureMessageGatewayEmail(authDTO, smsResponse, NotificationTypeEM.FEEDBACK);
		}
		return status;
	}
}
