package org.in.com.aggregator.sms;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailServiceImpl;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.NotificationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NotificationConfigService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
@EnableAsync
public class SMSServiceImpl implements SMSService {
	@Autowired
	BitsService bitsService;
	@Autowired
	NotificationConfigService configService;
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.controller.sms");
	private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("\\d{10}");

	public Future<SmsResponse> sendMTicketSMSNew(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		try {
			if (DeviceMediumEM.API_USER.getId() != authDTO.getDeviceMedium().getId() && NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_BOOKING.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CONFIRM_BOOKING, NotificationMediumEM.SMS);

				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_BOOKING.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				validateSMSContent(ticketDTO);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_BOOKING);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CONFIRM_BOOKING);
			}
			else {
				System.out.println(DateUtil.currentDateAndTime() + NotificationTypeEM.CONFIRM_BOOKING.getCode() + " Unable fire SMS for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.currentDateAndTime() + NotificationTypeEM.CONFIRM_BOOKING.getCode() + authDTO.getDeviceMedium().getId() + " - " + ticketDTO.getDeviceMedium().getId() + " - " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CONFIRM_BOOKING.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Async
	public Future<SmsResponse> sendPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO) {
		SmsResponse smsResponse = null;
		try {
			if (DeviceMediumEM.API_USER.getId() != authDTO.getDeviceMedium().getId() && NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.PHONE_BOOKING) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.PHONE_BOOKING.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.PHONE_BOOKING, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.PHONE_BOOKING.getCode());
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
				dataModel.put("fare", !authDTO.getNamespace().getProfile().isNoFareSMSFlag() ? ticketDTO.getTicketFareWithAddons().toString() : "0.00");
				dataModel.put("busType", StringUtil.substring(BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()), 30));
				dataModel.put("boarding", StringUtil.substring(ticketDTO.getBoardingPoint().getName(), 30));
				dataModel.put("time", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
				dataModel.put("contact", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getAddress(), 15) + " " + StringUtil.substring(ticketDTO.getBoardingPoint().getNumber(), 12)));
				dataModel.put("boardingDate", ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD"));
				dataModel.put("boardingAddress", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getAddress(), 30)));
				dataModel.put("boardingContact", StringUtil.substring(ticketDTO.getBoardingPoint().getNumber(), 30));
				dataModel.put("boardingContactName", authDTO.getNamespace().getName());
				dataModel.put("landmark", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getLandmark(), 30)));
				dataModel.put("serviceNumber", ticketDTO.getServiceNo());
				dataModel.put("landmark", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getLandmark(), 30)));
				dataModel.put("mapurl", StringUtil.isNotNull(ticketDTO.getBoardingPoint().getMapUrl()) ? ticketDTO.getBoardingPoint().getMapUrl() : Text.EMPTY);
				dataModel.put("linkpay", ticketDTO.getTicketExtra() != null ? ticketDTO.getTicketExtra().getLinkPay() : Text.EMPTY);

				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				validateSMSContent(ticketDTO);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.PHONE_BOOKING);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.PHONE_BOOKING);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.PHONE_BOOKING.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Async
	public void sendLinkPaySMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		SmsResponse smsResponse = null;
		try {
			if (DeviceMediumEM.API_USER.getId() != authDTO.getDeviceMedium().getId() && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) && ticketDTO.getTicketExtra() != null && StringUtil.isNotNull(ticketDTO.getTicketExtra().getLinkPay())) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.LINKPAY_BOOKING.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.LINKPAY_BOOKING, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.LINKPAY_BOOKING.getCode());
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
				dataModel.put("fare", !authDTO.getNamespace().getProfile().isNoFareSMSFlag() ? ticketDTO.getTicketFareWithAddons().toString() : "0.00");
				dataModel.put("busType", StringUtil.substring(BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()), 30));
				dataModel.put("boarding", StringUtil.substring(ticketDTO.getBoardingPoint().getName(), 30));
				dataModel.put("time", ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")));
				dataModel.put("serviceNumber", ticketDTO.getServiceNo());
				dataModel.put("paymentLink", ticketDTO.getTicketExtra().getLinkPay());

				String mobile = ticketDTO.getPassengerMobile();

				StringBuilder notes = new StringBuilder();

				if (ticketDTO.getTicketExtra().getBlockReleaseMinutes() < Numeric.ZERO_INT) {
					int diffMinutes = DateUtil.getMinutiesDifferent(ticketDTO.getTicketAt(), DateUtil.NOW());
					int releaseMinutes = Math.abs(ticketDTO.getTicketExtra().getBlockReleaseMinutes()) - diffMinutes;

					notes.append("Payment link expired in next ");
					notes.append(releaseMinutes);
					notes.append(" mins, make the payment before that and confirm your ticket.");
				}
				else if (ticketDTO.getTicketExtra().getBlockReleaseMinutes() > Numeric.ZERO_INT) {
					DateTime releaseDateTime = DateUtil.minusMinituesToDate(ticketDTO.getTripDTO().getTripDateTimeV2(), ticketDTO.getTicketExtra().getBlockReleaseMinutes());

					notes.append("Your ticket expired on ");
					notes.append(releaseDateTime.format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
					notes.append(", make the payment before that and confirm your ticket.");
				}
				else if (ticketDTO.getTicketExtra().getBlockReleaseMinutes() == Numeric.ZERO_INT) {
					notes.append("Your ticket confirmed and you have to pay while boarding or you can pay using this link ");
					notes.append(ticketDTO.getTicketExtra().getLinkPay());
					dataModel.put("paymentLink", Text.EMPTY);
				}

				dataModel.put("notes", notes.toString());

				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				validateSMSContent(ticketDTO);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.LINKPAY_BOOKING);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.PHONE_BOOKING);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.LINKPAY_BOOKING.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public Future<SmsResponse> sendPhoneBookingCancelSMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		SmsResponse smsResponse = null;
		try {
			if (DeviceMediumEM.API_USER.getId() != ticketDTO.getDeviceMedium().getId() && NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.PHONE_BOOKING_CANCEL)) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.PHONE_BOOKING_CANCEL.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.PHONE_BOOKING_CANCEL, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.PHONE_BOOKING_CANCEL.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("name", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("pnr", ticketDTO.getCode());
				dataModel.put("originName", ticketDTO.getFromStation().getName());
				dataModel.put("destinationName", ticketDTO.getToStation().getName());
				dataModel.put("travelDate", DateUtil.parseDateFormat(ticketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
				dataModel.put("seats", ticketDTO.getSeatNames());
				dataModel.put("travelsName", authDTO.getNamespace().getName());

				String mobile = ticketDTO.getPassengerMobile();

				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + " mobile:" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.PHONE_BOOKING_CANCEL);
				notificationDTO.setParticipantAddress(ticketDTO.getPassengerMobile());
				notificationDTO.setTransactionCount((content.length() / 160) + 1);
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.PHONE_BOOKING_CANCEL);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.PHONE_BOOKING_CANCEL.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	private void validateSMSContent(TicketDTO ticketDTO) {
		if (ticketDTO.getBoardingPoint() == null || StringUtil.isNull(ticketDTO.getBoardingPoint().getName()) || StringUtil.isNull(ticketDTO.getSeatNames()) || ticketDTO.getBoardingPoint().getId() == 0) {
			logger.info("ERSMS01 " + ticketDTO.getCode() + " " + ticketDTO.getBoardingPoint().getName() + ticketDTO.getBoardingPoint().getId() + ticketDTO.getBoardingPoint().getCode() + " M" + ticketDTO.getBoardingPoint().getMinitues());
			throw new ServiceException(ErrorCode.INVALID_PASSENGER_NAME);
		}

	}

	@Async
	public Future<SmsResponse> sendTripNotification(AuthDTO authDTO, TripChartDetailsDTO dto, String busNo, String contact) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_NOTIIFICATION, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("TNAME", dto.getTicketCode());
			dataModel.put("FROM", dto.getFromStation().getName());
			dataModel.put("TO", dto.getToStation().getName());
			dataModel.put("BOARDINGPT", StringUtil.substring(dto.getPassengerName(), 30));
			// dataModel.put("BRTIME", dto.getBoardingPoint().get);
			dataModel.put("BUSNO", busNo);
			dataModel.put("CONTACT", contact);
			dataModel.put("MSG", authDTO.getNamespaceCode().toLowerCase());
			String mobile = "";
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
			if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
				throw new ServiceException(100);
			}
			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = client.send(templateConfig, mobile, content);
				logger.info("PNR:" + dto.getTicketCode() + smsResponse.toString());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dto.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Async
	public Future<SmsResponse> sendCancelSMS(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		try {
			if (DeviceMediumEM.API_USER.getId() != ticketDTO.getDeviceMedium().getId() && NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_CANCELLATION)) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());

				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CONFIRM_CANCELLATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + " mobile:" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				// validate and append alternate mobile
				mobile = checkAndAppendAlternateMobile(authDTO, mobile, ticketDTO.getAlternateMobile());

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_CANCELLATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CONFIRM_CANCELLATION);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	private void saveSMSNotification(AuthDTO authDTO, NotificationDTO dto) {
		try {
			NotificationDAO dao = new NotificationDAO();
			dao.insertNotification(authDTO, dto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Future<SmsResponse> tripCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String supportNumber) {
		SmsResponse smsResponse = null;
		try {
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_CANCEL_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_CANCEL_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("reason", reason);
				dataModel.put("supportNumber", supportNumber);
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire SMS for Not confirmed " + ticketDTO.getCode());
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
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	public Future<SmsResponse> tripDelayNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String delayTime, String supportNumber) {

		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_DELAY_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_DELAY_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("reason", reason);
				dataModel.put("delayTime", delayTime);
				dataModel.put("supportNumber", supportNumber);
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);

					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire SMS for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
				smsResponse = new SmsResponse();
				smsResponse.setResponse("Unable fire SMS for Not confirmed");
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_DELAY_NOTIFICATION.getCode());
			}
		}
		catch (

		Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	public Future<SmsResponse> tripStationPointChangeNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String stationPointName, String stationPointTime, String supportNumber) {

		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_STATION_POINT_CHANGE_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_STATION_POINT_CHANGE_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("reason", reason);
				dataModel.put("stationPointName", stationPointName);
				dataModel.put("stationPointTime", stationPointTime);
				dataModel.put("supportNumber", supportNumber);
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire SMS for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
				smsResponse = new SmsResponse();
				smsResponse.setResponse("Unable fire SMS for Not confirmed");
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_STATION_POINT_CHANGE_NOTIFICATION.getCode());
			}
		}
		catch (

		Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);

	}

	public Future<SmsResponse> sendMTicketTransferSMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	public Future<SmsResponse> sendTripJourneyTrackingSMS(AuthDTO authDTO, Map<String, String> dataModel, NotificationTypeEM notificationType) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), notificationType.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, notificationType, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), notificationType.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
			String mobileNumber = dataModel.get("mobileNumber").trim();
			String alternateMobile = dataModel.containsKey("alternateMobile") && StringUtil.isNotNull(dataModel.get("alternateMobile")) ? dataModel.get("alternateMobile").trim() : Text.EMPTY;

			if (StringUtils.isNotBlank(mobileNumber) && MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE) {
				logger.info("mobile Error:" + mobileNumber);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			// validate and append alternate mobile
			mobileNumber = checkAndAppendAlternateMobile(authDTO, mobileNumber, alternateMobile);

			if (StringUtil.isNotNull(mobileNumber)) {
				smsResponse = client.send(templateConfig, mobileNumber, content);
				logger.info(smsResponse.toString());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("ticketCode"));
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(notificationType);
			notificationDTO.setParticipantAddress(mobileNumber);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumber));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, notificationType);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + notificationType.getCode());
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Async
	public Future<SmsResponse> sendTransactionOTP(AuthDTO authDTO, int OTP, String customerNumber) {

		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CUSTOMER_OTP.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CUSTOMER_OTP, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.CUSTOMER_OTP.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("OTPNumber", String.valueOf(OTP));
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("autoreadURL", authDTO.getDeviceMedium().getId() == DeviceMediumEM.MOB_USER.getId() ? authDTO.getNamespace().getProfile().getDomainURL().replace("www.", "m.") : authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("namespaceName", authDTO.getNamespace().getName());
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtil.isNotNull(customerNumber) && MOBILE_NUMBER_PATTERN.matcher(customerNumber).matches() == Boolean.TRUE) {

				smsResponse = client.send(templateConfig, customerNumber, content);
				logger.info("OTP:" + customerNumber + smsResponse.toString());

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(customerNumber);
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.CUSTOMER_OTP);
				notificationDTO.setParticipantAddress(customerNumber);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, customerNumber));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CUSTOMER_OTP);
			}

		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CUSTOMER_OTP.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	@Async
	public Future<SmsResponse> sendRechargeSMS(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {

		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.ONLINE_RECHARGE.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.ONLINE_RECHARGE, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.ONLINE_RECHARGE.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("namespaceName", authDTO.getNamespace().getName());
			dataModel.put("firstName", authDTO.getUser().getName());
			dataModel.put("amount", paymentTransactionDTO.getTransactionAmount().toString());
			dataModel.put("receiptCode", paymentTransactionDTO.getCode());
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtil.isNotNull(authDTO.getUser().getMobile()) && MOBILE_NUMBER_PATTERN.matcher(authDTO.getUser().getMobile()).matches() == Boolean.TRUE) {
				smsResponse = client.send(templateConfig, authDTO.getUser().getMobile(), content);
				logger.info("Recharge:" + paymentTransactionDTO.getCode() + smsResponse.toString());
				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(paymentTransactionDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.ONLINE_RECHARGE);
				notificationDTO.setParticipantAddress(authDTO.getUser().getMobile());
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, authDTO.getUser().getMobile()));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.ONLINE_RECHARGE);
			}

		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.ONLINE_RECHARGE.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	@Async
	public void sendBusbuddyAfterboard(AuthDTO authDTO, String ticketCode, String vehicleNumber, String mobileNumber) {

		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.BUS_BUDDY) && StringUtils.isNotBlank(mobileNumber)) {
				if (MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE) {
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.BUS_BUDDY.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.BUS_BUDDY, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.BUS_BUDDY.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("namespaceName", authDTO.getNamespace().getName());
				dataModel.put("vehicleNumber", vehicleNumber);
				dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtil.isNotNull(content) && StringUtil.isNotNull(mobileNumber) && MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.TRUE) {
					smsResponse = client.send(templateConfig, mobileNumber, content);
					logger.info("PNR:" + ticketCode + smsResponse.toString());
					NotificationDTO notificationDTO = new NotificationDTO();
					notificationDTO.setRefferenceCode(ticketCode);
					notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
					notificationDTO.setNotificationType(NotificationTypeEM.BUS_BUDDY);
					notificationDTO.setParticipantAddress(mobileNumber);
					notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumber));
					notificationDTO.setRequestLog(content);
					notificationDTO.setResponseLog(smsResponse.getResponseV2());
					saveSMSNotification(authDTO, notificationDTO);

					// Failure Email
					sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.BUS_BUDDY);
				}
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.BUS_BUDDY.getCode());
			}
			System.out.format("SMSER01 {1} - {2} - {3}", ticketCode, vehicleNumber, mobileNumber);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Future<SmsResponse> tripEarlyNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String earlyTime, String supportNumber) {
		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_EARLY_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_EARLY_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("passengerName", ticketDTO.getPassengerName());
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("reason", reason);
				dataModel.put("earlyTime", earlyTime);
				dataModel.put("supportNumber", supportNumber);
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire SMS for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
				smsResponse = new SmsResponse();
				smsResponse.setResponse("Unable fire SMS for Not confirmed");
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_EARLY_NOTIFICATION.getCode());
			}
		}
		catch (

		Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Async
	public Future<SmsResponse> eventAlertUtil(AuthDTO authDTO, Map<String, String> dataModel, AuditEventTypeEM namespaceEventType) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.NAMESPACE_EVENT_ALERT.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.NAMESPACE_EVENT_ALERT, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.NAMESPACE_EVENT_ALERT.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String mobile = dataModel.get("mobileNumber");

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = client.send(templateConfig, mobile, content);
				logger.info(smsResponse.toString());
			}
			else {
				logger.info("SMS mobile Empty: " + mobile);
			}

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(namespaceEventType.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.NAMESPACE_EVENT_ALERT);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.NAMESPACE_EVENT_ALERT);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.NAMESPACE_EVENT_ALERT.getCode());
			}
		}
		catch (

		Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Override
	public Future<SmsResponse> sendFaliureTicketSMS(AuthDTO authDTO, TicketDTO ticket) {
		SmsResponse smsResponse = null;
		Map<String, String> dataModel = Maps.newHashMap();
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.FAILIURE_TICKET.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.FAILIURE_TICKET, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.FAILIURE_TICKET.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("mobileNumber", ticket.getPassengerMobile());
			dataModel.put("ticketCode", ticket.getCode());
			dataModel.put("namespaceName", authDTO.getNamespace().getName());

			// Validate mobile number
			if (StringUtils.isNotBlank(dataModel.get("mobileNumber")) && MOBILE_NUMBER_PATTERN.matcher(dataModel.get("mobileNumber")).matches() == Boolean.FALSE) {
				logger.info("mobile Error:" + dataModel.get("mobileNumber"));
				throw new Exception();
			}

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtil.isNotNull(dataModel.get("mobileNumber")) && StringUtil.isNotNull(content)) {
				smsResponse = client.send(templateConfig, dataModel.get("mobileNumber"), content);
				logger.info("PNR:" + ticket.getCode() + smsResponse.toString());
			}

			// Store sms data in ticket_notification table

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("ticketCode"));
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.FAILIURE_TICKET);
			notificationDTO.setParticipantAddress(dataModel.get("mobileNumber"));
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, dataModel.get("mobileNumber")));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.FAILIURE_TICKET);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.FAILIURE_TICKET.getCode());
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			// Failure sms Email
			sendFailureSMSEmail(authDTO, dataModel, NotificationTypeEM.FAILIURE_TICKET);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Future<SmsResponse> sendFaliureBookSMS(AuthDTO authDTO, TicketDTO ticket, String mobileNumbers) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.FAILIURE_TICKET.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.FAILIURE_BOOK, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.FAILIURE_BOOK.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("pnr", ticket.getCode());
			dataModel.put("originName", ticket.getFromStation().getName());
			dataModel.put("destinationName", ticket.getToStation().getName());
			dataModel.put("travelDate", DateUtil.parseDateFormat(ticket.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("seats", ticket.getSeatNames());
			dataModel.put("fare", ticket.getTicketFareWithAddons().toString());
			dataModel.put("boarding", StringUtil.substring(ticket.getBoardingPoint().getName(), 30));
			dataModel.put("passengerName", StringUtil.substring(ticket.getPassengerName(), 30));
			dataModel.put("mobileNumber", ticket.getPassengerMobile());
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtil.isNotNull(mobileNumbers) && StringUtil.isNotNull(content)) {
				smsResponse = client.send(templateConfig, mobileNumbers, content);
				logger.info("PNR:" + ticket.getCode() + smsResponse.toString());
			}

			// Store SMS data in ticket_notification table
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticket.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.FAILIURE_TICKET);
			notificationDTO.setParticipantAddress(mobileNumbers);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumbers));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.FAILIURE_TICKET);
		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.FAILIURE_TICKET.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, String> getSMSStatus(AuthDTO authDTO, String refferenceCode) {
		Map<String, String> statusMap = new HashMap<String, String>();
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), null);

			String response = client.getSMSStatus(refferenceCode);
			JSONObject jsonObject = JSONObject.fromObject(response);

			statusMap.put("status", jsonObject.getString("status"));

			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();

			if (jsonObject.has("data") && jsonObject.getJSONArray("data").size() != 0) {
				JSONArray jsonArray = jsonObject.getJSONArray("data");
				for (Object object : jsonArray) {
					JSONObject dataObject = (JSONObject) object;
					Map<String, String> dataMap = new HashMap<String, String>();
					dataMap.put("id", dataObject.getString("id"));
					dataMap.put("mobile", dataObject.getString("mobile"));
					dataMap.put("status", dataObject.getString("status"));
					dataMap.put("senttime", dataObject.getString("senttime"));
					dataMap.put("deliverytime", dataObject.getString("dlrtime"));
					dataList.add(dataMap);
				}
			}
			statusMap.put("data", dataList.toString());
			statusMap.put("message", jsonObject.getString("message"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return statusMap;
	}

	public String sendVanPickupTracking(AuthDTO authDTO, Map<String, String> dataModel) {
		String responseLog = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.VAN_PICKUP.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.VAN_PICKUP, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.VAN_PICKUP.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtils.isNotBlank(dataModel.get("mobileNumber")) && MOBILE_NUMBER_PATTERN.matcher(dataModel.get("mobileNumber")).matches() == Boolean.FALSE) {
				logger.info("mobile Error:" + dataModel.get("mobileNumber"));
				throw new Exception();
			}
			SmsResponse smsResponse = null;
			if (StringUtil.isNotNull(dataModel.get("mobileNumber"))) {
				smsResponse = client.send(templateConfig, dataModel.get("mobileNumber"), content);
				logger.info(smsResponse.toString());
			}
			responseLog = getResponse(new AsyncResult<SmsResponse>(smsResponse));
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("ticketCode"));
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.VAN_PICKUP);
			notificationDTO.setParticipantAddress(dataModel.get("mobileNumber"));
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, dataModel.get("mobileNumber")));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.VAN_PICKUP);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.VAN_PICKUP.getCode());
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return responseLog;
	}

	public String sendReplyToFeedbackSMS(AuthDTO authDTO, UserFeedbackDTO userFeedback) {
		String responseLog = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.FEEDBACK_REPLY.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.FEEDBACK_REPLY, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.FEEDBACK_REPLY.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("name", userFeedback.getName());
			dataModel.put("mobileNumber", userFeedback.getMobile());
			dataModel.put("content", userFeedback.getReplyContent());
			dataModel.put("domain", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("namespace", authDTO.getNamespace().getName());
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			SmsResponse smsResponse = null;
			if (StringUtil.isNotNull(dataModel.get("mobileNumber"))) {
				smsResponse = client.send(templateConfig, dataModel.get("mobileNumber"), content);
				logger.info(smsResponse.toString());
			}
			responseLog = getResponse(new AsyncResult<SmsResponse>(smsResponse));
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(userFeedback.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.FEEDBACK_REPLY);
			notificationDTO.setParticipantAddress(dataModel.get("mobileNumber"));
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, dataModel.get("mobileNumber")));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.FEEDBACK_REPLY);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.FEEDBACK_REPLY.getCode());
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return responseLog;
	}

	public String sendVehicleChangeSms(AuthDTO authDTO, TicketDTO ticketDTO, TripInfoDTO tripInfo) {
		String responseLog = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.BUS_VEHICLE_CHANGE.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.BUS_VEHICLE_CHANGE, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.BUS_VEHICLE_CHANGE.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("registationNumber", tripInfo.getBusVehicle().getRegistationNumber());
			dataModel.put("supportName", tripInfo.getDriverName());
			dataModel.put("supportContact", tripInfo.getDriverMobile());
			dataModel.put("passengerName", ticketDTO.getPassengerName());
			dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());
			dataModel.put("domain", authDTO.getNamespace().getProfile().getDomainURL());

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
			SmsResponse smsResponse = null;
			if (StringUtil.isNotNull(dataModel.get("mobileNumber"))) {
				smsResponse = client.send(templateConfig, dataModel.get("mobileNumber"), content);
				logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
			}
			responseLog = getResponse(new AsyncResult<SmsResponse>(smsResponse));
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.BUS_VEHICLE_CHANGE);
			notificationDTO.setParticipantAddress(dataModel.get("mobileNumber"));
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, dataModel.get("mobileNumber")));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.BUS_VEHICLE_CHANGE);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.BUS_VEHICLE_CHANGE.getCode());
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return responseLog;
	}

	@Async
	public Future<SmsResponse> sendTicketEventSMS(AuthDTO authDTO, TicketDTO ticketDTO, String ticketStatus) {
		SmsResponse smsResponse = null;
		try {
			List<String> ticketEventContacts = authDTO.getNamespace().getProfile().getTicketEventNotificationContact();
			if (ticketEventContacts != null && !ticketEventContacts.isEmpty()) {

				StringBuilder mobileNumber = new StringBuilder();
				for (String mobile : ticketEventContacts) {
					if (mobileNumber.length() > 0) {
						mobileNumber.append(Text.COMMA);
					}
					mobileNumber.append(mobile);
				}

				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_EVENT.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TICKET_EVENT, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_EVENT.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("pnr", ticketDTO.getCode());
				dataModel.put("originName", ticketDTO.getFromStation().getName());
				dataModel.put("destinationName", ticketDTO.getToStation().getName());
				dataModel.put("bookedBy", ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransactionType() != null && ticketDTO.getTicketXaction().getTransactionType().getId() == Numeric.ONE_INT ? ticketDTO.getTicketUser().getName() : authDTO.getUser().getName());
				dataModel.put("devicemedium", ticketDTO.getDeviceMedium().getName());
				dataModel.put("passengerNumber", ticketDTO.getPassengerMobile());
				dataModel.put("passengerName", ticketDTO.getPassengerName());
				dataModel.put("ticketStatus", ticketStatus);
				dataModel.put("serviceNumber", ticketDTO.getServiceNo());
				dataModel.put("travelDatetime", ticketDTO.getTripDateTime().format("YYYY-MM-DD hh12:mm a", Locale.forLanguageTag("en_IN")));
				dataModel.put("seats", ticketDTO.getSeatNames());
				dataModel.put("fare", ticketDTO.getTicketFareWithAddons().toString());
				dataModel.put("time", ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransactionType() != null && ticketDTO.getTicketXaction().getTransactionType().getId() == Numeric.ONE_INT ? ticketDTO.getTicketAt().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : DateUtil.NOW().format("hh12:mm a", Locale.forLanguageTag("en_IN")));
				dataModel.put("bookedDate", ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransactionType() != null && ticketDTO.getTicketXaction().getTransactionType().getId() == Numeric.ONE_INT ? DateUtil.parseDateFormat(ticketDTO.getTicketAt().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E") : DateUtil.parseDateFormat(DateUtil.NOW().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				validateSMSContent(ticketDTO);

				if (StringUtils.isNotBlank(mobileNumber.toString())) {
					smsResponse = client.send(templateConfig, mobileNumber.toString(), content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TICKET_EVENT);
				notificationDTO.setParticipantAddress(mobileNumber.toString());
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumber.toString()));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TICKET_EVENT);
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TICKET_EVENT.getCode());
			}
			System.out.println(e.getErrorCode() + " unable to SMS");
		}
		catch (Exception e) {
			System.out.println(ticketDTO.getCode() + " unable to SMS");
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Override
	public String sendTicketAfterTripTimeNotify(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> additionalDetails) {
		String responseLog = null;
		try {
			StringBuilder seatNameWithGender = new StringBuilder();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				seatNameWithGender.append(ticketDetailsDTO.getSeatName());
				seatNameWithGender.append("(");
				seatNameWithGender.append(ticketDetailsDTO.getSeatGendar().getCode());
				seatNameWithGender.append(")");
			}

			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_AFTER_TRIP_TIME.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TICKET_AFTER_TRIP_TIME, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_AFTER_TRIP_TIME.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespace().getCode());
			dataModel.put("pnr", ticketDTO.getCode());
			dataModel.put("originName", StringUtil.substring(ticketDTO.getFromStation().getName(), 30));
			dataModel.put("destinationName", StringUtil.substring(ticketDTO.getToStation().getName(), 30));
			dataModel.put("serviceNumber", ticketDTO.getServiceNo());
			dataModel.put("seats", seatNameWithGender.toString());
			dataModel.put("passengerName", StringUtil.substring(ticketDTO.getPassengerName(), 30));
			dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());
			dataModel.put("boardingName", StringUtil.substring(ticketDTO.getBoardingPoint().getName(), 30));
			dataModel.put("boardingDate", DateUtil.parseDateFormat(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("fare", ticketDTO.getTicketFareWithAddons().toString());
			dataModel.put("time", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
			dataModel.put("bookedBy", ticketDTO.getTicketUser().getName());
			dataModel.put("bookedAtDate", DateUtil.parseDateFormat(ticketDTO.getTicketAt().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("bookedAtTime", ticketDTO.getTicketAt().format("hh12:mm a", Locale.forLanguageTag("en_IN")));
			dataModel.put("vehicleNumber", additionalDetails.get("VEHICLE_NUMBER"));
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			validateSMSContent(ticketDTO);

			SmsResponse smsResponse = null;
			if (StringUtils.isNotBlank(additionalDetails.get("MOBILE_NUMBER"))) {
				smsResponse = client.send(templateConfig, additionalDetails.get("MOBILE_NUMBER"), content);

				logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				responseLog = getResponse(new AsyncResult<SmsResponse>(smsResponse));
			}
			else {
				logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.TICKET_AFTER_TRIP_TIME);
			notificationDTO.setParticipantAddress(additionalDetails.get("MOBILE_NUMBER"));
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, additionalDetails.get("MOBILE_NUMBER")));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TICKET_AFTER_TRIP_TIME);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TICKET_AFTER_TRIP_TIME.getCode());
			}
		}
		catch (

		Exception e) {
			e.printStackTrace();
		}
		return responseLog;
	}

	public String sendTicketAfterTripTimeCancelNotify(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> additionalDetails) {
		String responseLog = null;
		try {
			StringBuilder seatNameWithGender = new StringBuilder();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				seatNameWithGender.append(ticketDetailsDTO.getSeatName());
				seatNameWithGender.append("(");
				seatNameWithGender.append(ticketDetailsDTO.getSeatGendar().getCode());
				seatNameWithGender.append(")");
			}

			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_AFTER_TRIP_TIME_CANCEL.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TICKET_AFTER_TRIP_TIME_CANCEL, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_AFTER_TRIP_TIME_CANCEL.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespace().getCode());
			dataModel.put("pnr", ticketDTO.getCode());
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("originName", StringUtil.substring(ticketDTO.getFromStation().getName(), 30));
			dataModel.put("destinationName", StringUtil.substring(ticketDTO.getToStation().getName(), 30));
			dataModel.put("seats", seatNameWithGender.toString());
			dataModel.put("name", StringUtil.substring(ticketDTO.getPassengerName(), 30));
			dataModel.put("travelDate", DateUtil.parseDateFormat(ticketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("refund", String.valueOf(ticketDTO.getRefundAmount()));
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			SmsResponse smsResponse = null;
			if (StringUtils.isNotBlank(additionalDetails.get("MOBILE_NUMBER"))) {
				smsResponse = client.send(templateConfig, additionalDetails.get("MOBILE_NUMBER"), content);

				logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				responseLog = getResponse(new AsyncResult<SmsResponse>(smsResponse));
			}
			else {
				logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.TICKET_AFTER_TRIP_TIME_CANCEL);
			notificationDTO.setParticipantAddress(additionalDetails.get("MOBILE_NUMBER"));
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, additionalDetails.get("MOBILE_NUMBER")));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TICKET_AFTER_TRIP_TIME_CANCEL);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TICKET_AFTER_TRIP_TIME_CANCEL.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return responseLog;
	}

	@Async
	public Future<SmsResponse> sendTicketUpdateSMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_UPDATE.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TICKET_UPDATE, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_UPDATE.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceURL", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("pnr", ticketDTO.getCode());
			dataModel.put("originName", ticketDTO.getFromStation().getName());
			dataModel.put("destinationName", ticketDTO.getToStation().getName());

			dataModel.put("travelDate", DateUtil.parseDateFormat(ticketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("seats", ticketDTO.getSeatNames());
			dataModel.put("fare", ticketDTO.getTicketFareWithAddons().toString());
			dataModel.put("busType", StringUtil.substring(BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()), 30));
			dataModel.put("boarding", StringUtil.substring(ticketDTO.getBoardingPoint().getName(), 30));
			dataModel.put("dropping", StringUtil.substring(ticketDTO.getDroppingPoint().getName(), 30));
			dataModel.put("boardingTime", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
			dataModel.put("droppingTime", ticketDTO.getDroppingPoint().getMinitues() < 1440 ? ticketDTO.getDroppingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getDroppingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
			dataModel.put("contact", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getAddress(), 15) + " " + StringUtil.substring(ticketDTO.getBoardingPoint().getNumber(), 12)));
			dataModel.put("boardingAddress", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getAddress(), 30)));
			dataModel.put("boardingContact", StringUtil.substring(ticketDTO.getBoardingPoint().getNumber(), 30));
			dataModel.put("boardingDate", DateUtil.parseDateFormat(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			String mobile = ticketDTO.getPassengerMobile();
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			validateSMSContent(ticketDTO);

			if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
				System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = client.send(templateConfig, mobile, content);
				logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
			}
			else {
				logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.TICKET_UPDATE);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TICKET_UPDATE);
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TICKET_UPDATE.getCode());
			}
		}
		catch (

		Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	private String getResponse(Future<SmsResponse> futureTask) {
		String message = null;
		if (futureTask != null) {
			while (!futureTask.isDone()) {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
				}
			}
			try {
				message = futureTask.get().getContent();
			}
			catch (InterruptedException | ExecutionException e) {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
		}
		return message;
	}

	private static String removeUnknownSymbol(String response) {
		if (StringUtil.isNotNull(response)) {
			char[] resChars = response.toCharArray();
			for (Character resChar : resChars) {
				if (!(resChar >= 48 && resChar <= 57) && !(resChar >= 65 && resChar <= 90) && !(resChar >= 97 && resChar <= 122) && resChar != 32 && resChar != 38 && !(resChar >= 40 && resChar <= 46)) {
					response = response.replaceAll("\\" + resChar.toString(), Text.DOUBLE_QUOTE);
				}
			}
			response = response.replaceAll("&", "&amp;");
		}
		return response;
	}

	private void sendFailureSMSGatewayEmail(AuthDTO authDTO, SmsResponse smsResponse, NotificationTypeEM notificationType) {
		boolean smsFailed = Text.FALSE;
		try {
			if (smsResponse.getCode().equals(Constants.SMS_PROVIDER_QIKBERRY)) {
				JSONObject jsonObject = JSONObject.fromObject(smsResponse.getResponseV2());
				String status = jsonObject.getString("status");
				if (!"200".equals(status)) {
					smsFailed = Text.TRUE;
				}
			}
			else if (smsResponse.getCode().equals(Constants.SMS_PROVIDER_INFINI)) {
				JSONObject jsonObject = JSONObject.fromObject(smsResponse.getResponseV2());
				String status = jsonObject.getString("status");
				if (!"OK".equalsIgnoreCase(status)) {
					smsFailed = Text.TRUE;
				}
			}
			else if (smsResponse.getCode().equals(Constants.SMS_PROVIDER_KALEYRA)) {
				JSONObject jsonObject = JSONObject.fromObject(smsResponse.getResponseV2());
				String status = jsonObject.getString("error");
				if (status.contains("body")) {
					smsFailed = Text.TRUE;
				}
			}
		}
		catch (Exception e) {
			smsFailed = Text.TRUE;
			System.out.println(smsResponse.getResponseV2());
			e.printStackTrace();
		}
		finally {
			if (smsFailed) {
				Map<String, Object> dataModel = new HashMap<>();
				dataModel.put("url", smsResponse.getUrl());
				dataModel.put("request", smsResponse.getRequest());
				dataModel.put("response", smsResponse.getResponseV2());
				dataModel.put("content", smsResponse.getContent());
				dataModel.put("gatewayName", smsResponse.getCode());
				dataModel.put("namespace", authDTO.getNamespace().getName());
				dataModel.put("notificationType", notificationType.getDescription());

				EmailServiceImpl emailServiceImpl = new EmailServiceImpl();
				emailServiceImpl.sendFailureSMSGatewayEmail(authDTO, dataModel);
			}
		}
	}

	private void sendFailureSMSEmail(AuthDTO authDTO, Map<String, String> dataObject, NotificationTypeEM notificationType) {
		try {
			Map<String, Object> dataModel = new HashMap<>();
			dataModel.put("reason", "Invalid Mobile");
			dataModel.put("request", dataObject.toString());
			dataModel.put("namespace", authDTO.getNamespace().getName());
			dataModel.put("notificationType", notificationType.getDescription());

			EmailServiceImpl emailServiceImpl = new EmailServiceImpl();
			emailServiceImpl.sendFailureSMSGatewayEmail(authDTO, dataModel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendOverallTripSummarySMS(AuthDTO authDTO, Map<String, Object> dataModel, String mobileNumber) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespace().getCode(), NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
			if (StringUtil.isNotNull(mobileNumber) && StringUtil.isNotNull(content)) {
				smsResponse = client.send(templateConfig, mobileNumber, content);
			}
			// Store sms data in ticket_notification table
			logger.info("SMS Log: " + content + " - " + mobileNumber + " - " + smsResponse.getResponseV2() + " - " + content.length() / 160 + 1);

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(Text.OVERALL_OCCUPANCY_SUMMARY);
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY);
			notificationDTO.setParticipantAddress(mobileNumber);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumber));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY);
		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public Future<SmsResponse> busTypeChangeNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String busTypeName, String supportNumber) {
		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.BUS_TYPE_CHANGE_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.BUS_TYPE_CHANGE_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("passengerName", ticketDTO.getPassengerName());
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("reason", reason);
				dataModel.put("busTypeName", busTypeName);
				dataModel.put("supportNumber", supportNumber);
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}

				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire SMS for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
				smsResponse = new SmsResponse();
				smsResponse.setResponse("Unable fire SMS for Not confirmed");
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.BUS_TYPE_CHANGE_NOTIFICATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	public Future<SmsResponse> tripCovidEpassNotification(AuthDTO authDTO, TicketDTO ticketDTO, String registrationNumber, String supportNumber) {
		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_NOTIIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.COVID_E_PASS_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.COVID_E_PASS_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("passengerName", ticketDTO.getPassengerName());
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("fromStation", ticketDTO.getFromStation().getName());
				dataModel.put("toStation", ticketDTO.getToStation().getName());
				dataModel.put("vehicleNumber", registrationNumber);
				dataModel.put("supportNumber", supportNumber);
				dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
				String mobile = ticketDTO.getPassengerMobile();
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
					System.out.println(ticketDTO.getCode() + "mobile :" + mobile);
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				if (StringUtils.isNotBlank(mobile)) {
					smsResponse = client.send(templateConfig, mobile, content);
					logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
				}
				else {
					logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
				}
				/**
				 * Store sms data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
				notificationDTO.setParticipantAddress(mobile);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.TRIP_NOTIIFICATION);
			}
			else {
				System.out.println(DateUtil.NOW().toString() + "Unable fire SMS for Not confirmed " + ticketDTO.getCode());
				System.out.println(DateUtil.NOW().toString() + authDTO.getDeviceMedium().getId() + "@ " + ticketDTO.getDeviceMedium().getId() + "@ " + authDTO.getNamespace().getProfile().getSmsNotificationFlagCode() + "- " + ticketDTO.getTicketStatus().getId() + " =" + TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId());
				smsResponse = new SmsResponse();
				smsResponse.setResponse("Unable fire SMS for Not confirmed");
			}
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.COVID_E_PASS_NOTIFICATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Async
	public void sendTripVanPickupSMS(AuthDTO authDTO, TicketDTO ticketDTO, TripVanInfoDTO tripVanInfoDTO) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.VAN_PICKUP.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.TRIP_VAN_PICKUP, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.TRIP_VAN_PICKUP.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("passengerName", ticketDTO.getPassengerName());
			dataModel.put("supportName", tripVanInfoDTO.getDriver().getName());
			dataModel.put("vanNumber", tripVanInfoDTO.getVehicle().getRegistationNumber());
			dataModel.put("vanContact", tripVanInfoDTO.getMobileNumber());
			String mobile = ticketDTO.getPassengerMobile();
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = client.send(templateConfig, mobile, content);
				logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
			}
			else {
				logger.info("SMS mobile Not Empty: " + ticketDTO.getCode());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.VAN_PICKUP);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.VAN_PICKUP);

		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.TRIP_VAN_PICKUP.getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Async
	public void sendSMS(AuthDTO authDTO, String mobileNumbers, Map<String, String> dataModel, NotificationTypeEM notificationType) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.FREE_TEMPLATE.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, notificationType, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), notificationType.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			if (StringUtil.isNull(mobileNumbers)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (StringUtil.isNull(dataModel)) {
				throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL, "Content cannot be null");
			}
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
			smsResponse = client.send(templateConfig, mobileNumbers, content);
			logger.info(smsResponse.toString());

			// Store SMS data in ticket_notification table
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(Text.NA);
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(notificationType);
			notificationDTO.setParticipantAddress(mobileNumbers);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumbers));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, notificationType);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + notificationType.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public Future<SmsResponse> sendPendingOrderCancelSMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CONFIRM_CANCELLATION.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.PENDING_ORDER_CANCELLATION, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.PENDING_ORDER_CANCELLATION.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("travelsName", authDTO.getNamespaceCode());
			dataModel.put("pnr", ticketDTO.getCode());

			String mobile = ticketDTO.getPassengerMobile();
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = client.send(templateConfig, mobile, content);

				logger.info("PNR:" + ticketDTO.getCode() + smsResponse.toString());
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.PENDING_ORDER_CANCELLATION);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.PENDING_ORDER_CANCELLATION);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.PENDING_ORDER_CANCELLATION.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	@Async
	public void sendFeedbackSMS(AuthDTO authDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.FEEDBACK.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.FEEDBACK, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.FEEDBACK.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);
			String mobile = dataModel.get("mobileNumber");

			if (StringUtils.isNotBlank(mobile) && MOBILE_NUMBER_PATTERN.matcher(mobile).matches() == Boolean.FALSE) {
				System.out.println("mobile Error:" + mobile);
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (StringUtils.isNotBlank(mobile)) {
				smsResponse = client.send(templateConfig, mobile, content);
				logger.info(smsResponse.toString());
			}
			else {
				logger.info("SMS mobile Not Empty: " + dataModel.get("ticketCode"));
			}

			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(dataModel.get("ticketCode"));
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(NotificationTypeEM.FEEDBACK);
			notificationDTO.setParticipantAddress(mobile);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobile));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.FEEDBACK);
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.FEEDBACK.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendCustomerDiscountCoupon(AuthDTO authDTO, DiscountCouponDTO discountCouponDTO, UserCustomerDTO userCustomerDTO) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.CUSTOMER_OTP.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.CUSTOMER_DISCOUNT, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.CUSTOMER_DISCOUNT.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("coupon", discountCouponDTO.getCoupon());
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtil.isNotNull(userCustomerDTO.getMobile()) && MOBILE_NUMBER_PATTERN.matcher(userCustomerDTO.getMobile()).matches() == Boolean.TRUE) {

				smsResponse = client.send(templateConfig, userCustomerDTO.getMobile(), content);
				logger.info("Customer Discount:" + userCustomerDTO.getMobile() + smsResponse.toString());

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(userCustomerDTO.getMobile());
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.CUSTOMER_DISCOUNT);
				notificationDTO.setParticipantAddress(userCustomerDTO.getMobile());
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, userCustomerDTO.getMobile()));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.CUSTOMER_DISCOUNT);
			}

		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.CUSTOMER_DISCOUNT.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Async
	public void sendPaymentSMS(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		SmsResponse smsResponse = null;
		try {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.PAYMENT_NOTIFICATION)) {
				SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.PAYMENT_NOTIFICATION.getCode());
				NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.PAYMENT_NOTIFICATION, NotificationMediumEM.SMS);
				if (StringUtil.isNull(templateConfig.getCode())) {
					logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.PAYMENT_NOTIFICATION.getCode());
					throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
				}

				Map<String, String> dataModel = Maps.newHashMap();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("namespaceName", authDTO.getNamespace().getName());
				dataModel.put("firstName", paymentTransactionDTO.getUser().getName());
				dataModel.put("amount", paymentTransactionDTO.getTransactionAmount().toString());
				dataModel.put("transactionTypeCode", paymentTransactionDTO.getTransactionType().getCode());
				dataModel.put("transactionTypeName", paymentTransactionDTO.getTransactionType().getName());
				dataModel.put("paymentStatusCode", paymentTransactionDTO.getPaymentAcknowledge().getCode());
				dataModel.put("paymentStatusName", paymentTransactionDTO.getPaymentAcknowledge().getName());
				String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

				if (paymentTransactionDTO.getUser() != null && StringUtil.isNotNull(paymentTransactionDTO.getUser().getMobile()) && MOBILE_NUMBER_PATTERN.matcher(paymentTransactionDTO.getUser().getMobile()).matches() == Boolean.TRUE) {
					smsResponse = client.send(templateConfig, paymentTransactionDTO.getUser().getMobile(), content);
					logger.info("Payment SMS:" + paymentTransactionDTO.getCode() + smsResponse.toString());
					NotificationDTO notificationDTO = new NotificationDTO();
					notificationDTO.setRefferenceCode(paymentTransactionDTO.getCode());
					notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
					notificationDTO.setNotificationType(NotificationTypeEM.PAYMENT_NOTIFICATION);
					notificationDTO.setParticipantAddress(paymentTransactionDTO.getUser().getMobile());
					notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, paymentTransactionDTO.getUser().getMobile()));
					notificationDTO.setRequestLog(content);
					notificationDTO.setResponseLog(smsResponse.getResponseV2());
					saveSMSNotification(authDTO, notificationDTO);

					// Failure Email
					sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.PAYMENT_NOTIFICATION);
				}
			}
		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.PAYMENT_NOTIFICATION.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void sendWhatsappNumberVerificationSMS(AuthDTO authDTO, Map<String, String> dataModel) {
		SmsResponse smsResponse = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION.getCode());
			NotificationTemplateConfigDTO templateConfig = configService.getNotificationTemplateConfig(authDTO, NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION, NotificationMediumEM.SMS);
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION.getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String mobileNumber = String.valueOf(dataModel.get("mobileNumber"));
			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.TRUE) {
				smsResponse = client.send(templateConfig, mobileNumber, content);
				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(mobileNumber);
				notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
				notificationDTO.setNotificationType(NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION);
				notificationDTO.setParticipantAddress(mobileNumber);
				notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumber));
				notificationDTO.setRequestLog(content);
				notificationDTO.setResponseLog(smsResponse.getResponseV2());
				saveSMSNotification(authDTO, notificationDTO);

				// Failure Email
				sendFailureSMSGatewayEmail(authDTO, smsResponse, NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION);
			}
		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getCode() + " - " + NotificationTypeEM.WHATSAPP_VERIFICATION_NOTIFICATION.getCode());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public JSONObject getNotificationStatus(AuthDTO authDTO, NotificationDTO notification, Map<String, String> responseParam) {
		JSONObject responseJSON = null;
		try {
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), notification.getNotificationType().getCode());
			String smsResponse = client.getSMSStatus(responseParam.get("cid"));
			responseJSON = JSONObject.fromObject(smsResponse);
		}
		catch (ServiceException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return responseJSON;
	}

	public Future<SmsResponse> sendCustomerTicketEventSMS(AuthDTO authDTO, Map<String, String> dataModel, NotificationTemplateConfigDTO templateConfig) {
		SmsResponse smsResponse = null;
		String ticketCode = dataModel.get("pnr");
		try {
			String mobileNumber = dataModel.get("mobileNumber");
			SmsClient client = SmsClientFactory.getInstance().getSmsClient(authDTO.getNamespace().getProfile().getSmsProvider(), authDTO.getNamespaceCode(), NotificationTypeEM.TICKET_EVENT.getCode());
			if (StringUtil.isNull(templateConfig.getCode())) {
				logger.info("SMS Template config Not Found: {} {} " + authDTO.getNamespaceCode(), templateConfig.getNotificationType().getCode());
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}

			String content = TemplateUtils.getInstance().processDynamicContent(templateConfig, dataModel);

			if (StringUtils.isNotBlank(mobileNumber)) {
				smsResponse = client.send(templateConfig, mobileNumber, content);
				logger.info("PNR:" + ticketCode + smsResponse.toString());
			}
			else {
				logger.info("SMS mobile Not Empty: " + ticketCode);
			}
			/**
			 * Store sms data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketCode);
			notificationDTO.setNotificationMode(NotificationMediumEM.SMS);
			notificationDTO.setNotificationType(templateConfig.getNotificationType());
			notificationDTO.setParticipantAddress(mobileNumber);
			notificationDTO.setTransactionCount(StringUtil.getSMSCount(content, mobileNumber));
			notificationDTO.setRequestLog(content);
			notificationDTO.setResponseLog(smsResponse.getResponseV2());
			saveSMSNotification(authDTO, notificationDTO);

			// Failure Email
			sendFailureSMSGatewayEmail(authDTO, smsResponse, templateConfig.getNotificationType());
		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null && e.getErrorCode().getCode().equals(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND.getCode())) {
				System.out.println(e.getErrorCode().getCode() + " - " + templateConfig.getNotificationType().getCode());
			}
			System.out.println(e.getErrorCode() + " unable to SMS");
		}
		catch (Exception e) {
			System.out.println(ticketCode + " unable to SMS");
			e.printStackTrace();
		}
		return new AsyncResult<SmsResponse>(smsResponse);
	}

	private String checkAndAppendAlternateMobile(AuthDTO authDTO, String mobile, String alternateMobile) {
		if (authDTO.getNamespace().getProfile().isNotificationToAlternateMobile(NotificationMediumEM.SMS) && StringUtils.isNotBlank(alternateMobile) && MOBILE_NUMBER_PATTERN.matcher(alternateMobile).matches() == Boolean.TRUE && !mobile.equals(alternateMobile)) {
			mobile = mobile + Text.COMMA + alternateMobile;
		}
		return mobile;
	}
}
