package org.in.com.aggregator.mail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.constants.Text;
import org.in.com.dao.NotificationDAO;
import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.UserRegistrationDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.NumberToWordConvertor;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

@Service
public class EmailServiceImpl implements EmailService {
	@Autowired
	BitsService bitsService;

	private static final String ETICKET_TEMPLATE = "eticketcheck";
	private static final String ETICKET_FORGOT_PASWORD = "forgetpassword";
	private static final String NEW_ACCOUNT_REGISTER = "register";
	private static final String LOW_BALANCE = "lowbalance";
	private static final String CUSTOMER_FEEDBACK = "feedback";
	private static final String DEVICE_REGISTER = "deviceregister";
	private static final String PASSENGER_FEEDBACK_REPLY = "replyfeedback";
	private static final String TAX_INVOICE_TEMPLATE = "taxinvoice";
	private static final String CANCEL_TICKET_TEMPLATE = "cancelticket";
	private static final String TRANSACTION_TEMPLATE = "userbalancevalidation";
	private static final String REPORT_TEMPLATE = "report";
	private static final String TICKET_TRANSACTION_TEMPLATE = "mismatchtransaction";

	@Async
	public void sendBookingEmail(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			// Mail will send to only customer and WEB user
			if (DeviceMediumEM.API_USER.getId() != ticketDTO.getDeviceMedium().getId() && authDTO.getNamespace().getProfile().isEmailNotificationFlag() && StringUtil.isNotNull(ticketDTO.getPassengerEmailId())) {
				Map<String, Object> dataModel = Maps.newHashMap();
				List<Map<String, String>> ticketDetails = new ArrayList<Map<String, String>>();
				for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {
					Map<String, String> passDetails = new HashMap<String, String>();
					passDetails.put("passengerName", dto.getPassengerName());
					passDetails.put("seatName", dto.getSeatName());
					passDetails.put("gender", dto.getSeatGendar().getName());
					passDetails.put("passengerAge", "" + dto.getPassengerAge());
					ticketDetails.add(passDetails);
				}

				dataModel.put("name", ticketDTO.getPassengerName());
				dataModel.put("website", authDTO.getNamespace().getProfile().getDomainURL());
				dataModel.put("origin", ticketDTO.getFromStation().getName());
				dataModel.put("destination", ticketDTO.getToStation().getName());
				dataModel.put("travelDate", ticketDTO.getTripDate().format(Text.DATE_DATE4J));
				dataModel.put("travelTime", ticketDTO.getTripTime());
				dataModel.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
				dataModel.put("boardingPointTime", DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format(Text.DATE_TIME_DATE4J));
				dataModel.put("contact", ticketDTO.getBoardingPoint().getNumber());
				dataModel.put("fare", ticketDTO.getTotalFare().toString());
				dataModel.put("ticketDetails", ticketDetails);
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("ticketurl", authDTO.getNamespace().getProfile().getDomainURL() + "/print-ticket?pnr=" + ticketDTO.getCode());
				dataModel.put("operator", authDTO.getNamespace().getName());
				validateSMSContent(ticketDTO);
				String body = TemplateUtils.getInstance().processEmailContent(ETICKET_TEMPLATE, dataModel);
				String subject = authDTO.getNamespace().getName() + " Bus Ticket " + ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName() + " on " + ticketDTO.getTripDate().format(Text.DATE_DATE4J);
				EmailClient mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName(), Text.EMPTY);
				mailClient.addTo(ticketDTO.getPassengerEmailId(), StringUtils.isNotBlank(ticketDTO.getPassengerName()) ? ticketDTO.getPassengerName() : "Passenger");
				String ccString = authDTO.getNamespace().getProfile().getEmailCopyAddress();
				String ccArray[] = (StringUtil.isNotNull(ccString) ? ccString.split(",") : null);
				int mailCount = 1;
				if (ccArray != null) {
					for (String cc : ccArray) {
						if (StringUtil.isNotNull(cc)) {
							mailClient.addCc(cc, "Support");
							mailCount++;
						}
					}
				}
				// ByteArrayOutputStream baos = new
				// PDFBuilder().buildPdfA4Document(authDTO, ticketDTO, null);
				// mailClient.addAttachment(new
				// ByteArrayDataSource(baos.toByteArray(), "application/pdf"),
				// "e-ticket-copy.pdf", "e-ticket");

				Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

				/**
				 * Store Mail data in ticket_notification table
				 */
				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_BOOKING);
				notificationDTO.setParticipantAddress(ticketDTO.getPassengerEmailId() + "," + ccString);
				notificationDTO.setTransactionCount(mailCount);
				notificationDTO.setRequestLog(subject + " " + dataModel.toString());
				notificationDTO.setResponseLog(response.get());
				saveMailNotification(authDTO, notificationDTO);
			}
		}
		catch (Exception e) {
			System.out.println(ticketDTO.getPassengerEmailId() + " " + ticketDTO.getCode());
			e.printStackTrace();
		}
	}

	private void saveMailNotification(AuthDTO authDTO, NotificationDTO dto) throws Exception {
		try {
			NotificationDAO dao = new NotificationDAO();
			dao.insertNotification(authDTO, dto);
		}
		catch (Exception e) {
			throw e;
		}
	}

	@Async
	public void sendForgetPasswordEmail(AuthDTO authDTO, UserDTO userDTO) {
		if (authDTO.getNamespace().getProfile().isEmailNotificationFlag()) {
			EmailClient mailClient;
			String body = null, subject = null;
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("username", userDTO.getUsername());
			dataModel.put("namespace", authDTO.getNamespace().getName());
			dataModel.put("forgettoken", userDTO.getForgetToken());
			dataModel.put("domainUrl", authDTO.getDomainUrl());
			try {
				body = TemplateUtils.getInstance().processEmailContent(ETICKET_FORGOT_PASWORD, dataModel);
				subject = authDTO.getNamespace().getName() + " account forgot password request";
				mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName() + " Admin", Text.EMPTY);
				mailClient.addTo(userDTO.getEmail(), StringUtils.isNotBlank(userDTO.getUsername()) ? userDTO.getUsername() : "Passenger");
				String ccString = authDTO.getNamespace().getProfile().getEmailCopyAddress();
				String ccArray[] = (StringUtil.isNotNull(ccString) ? ccString.split(",") : null);
				int mailCount = 1;
				if (ccArray != null) {
					for (String cc : ccArray) {
						if (StringUtil.isNotNull(cc)) {
							mailClient.addCc(cc, "Support");
							mailCount++;
						}
					}
				}
				Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

				/**
				 * Store Mail data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(userDTO.getUsername());
				notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
				notificationDTO.setNotificationType(NotificationTypeEM.FORGET_PASSWORD);
				notificationDTO.setParticipantAddress(userDTO.getEmail() + "," + ccString);
				notificationDTO.setTransactionCount(mailCount);
				notificationDTO.setRequestLog(subject + " " + dataModel.toString());
				notificationDTO.setResponseLog(response.get());
				saveMailNotification(authDTO, notificationDTO);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Async
	public void sendRegisterEmail(AuthDTO authDTO, UserDTO userDTO) {
		if (authDTO.getNamespace().getProfile().isEmailNotificationFlag()) {
			String body = null, subject = null;
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("username", userDTO.getUsername());
			dataModel.put("namespace", authDTO.getNamespace().getName());
			dataModel.put("namespaceCode", authDTO.getNamespace().getCode());
			dataModel.put("domainUrl", authDTO.getDomainUrl());
			try {
				body = TemplateUtils.getInstance().processEmailContent(NEW_ACCOUNT_REGISTER, dataModel);
				subject = authDTO.getNamespace().getName() + " Welcome you";
				EmailClient mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName() + " Admin", Text.EMPTY);
				mailClient.addTo(userDTO.getEmail(), StringUtils.isNotBlank(userDTO.getUsername()) ? userDTO.getUsername() : "Passenger");
				String ccString = authDTO.getNamespace().getProfile().getEmailCopyAddress();
				String ccArray[] = (StringUtil.isNotNull(ccString) ? ccString.split(",") : null);
				int mailCount = 1;
				if (ccArray != null) {
					for (String cc : ccArray) {
						if (StringUtil.isNotNull(cc)) {
							mailClient.addCc(cc, "Support");
							mailCount++;
						}
					}
				}
				Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);
				/**
				 * Store Mail data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(userDTO.getUsername());
				notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
				notificationDTO.setNotificationType(NotificationTypeEM.USER_REGISTER);
				notificationDTO.setParticipantAddress(userDTO.getEmail() + "," + ccString);
				notificationDTO.setTransactionCount(mailCount);
				notificationDTO.setRequestLog(subject + " " + dataModel.toString());
				notificationDTO.setResponseLog(response.get());
				saveMailNotification(authDTO, notificationDTO);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Async
	public void sendLowBalanceEmail(AuthDTO authDTO, BigDecimal amount) {
		if (authDTO.getNamespace().getProfile().isEmailNotificationFlag()) {
			String body = null, subject = null;
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("username", authDTO.getUser().getUsername());
			dataModel.put("accountName", authDTO.getUser().getName());
			dataModel.put("namespace", authDTO.getNamespace().getName());
			dataModel.put("namespaceCode", authDTO.getNamespace().getCode());
			dataModel.put("amount", amount);
			try {
				body = TemplateUtils.getInstance().processEmailContent(LOW_BALANCE, dataModel);
				subject = authDTO.getNamespace().getName() + ", " + authDTO.getUser().getName() + " Low Balance Alert";
				EmailClient mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName() + " Admin", Text.EMPTY);
				mailClient.addTo(authDTO.getUser().getEmail(), StringUtils.isNotBlank(authDTO.getUser().getUsername()) ? authDTO.getUser().getUsername() : "Passenger");
				String ccString = authDTO.getNamespace().getProfile().getEmailCopyAddress() + ",ezeebus@ezeeinfosolutions.com,";
				String ccArray[] = (StringUtil.isNotNull(ccString) ? ccString.split(",") : null);

				int mailCount = 1;
				if (ccArray != null) {
					for (String cc : ccArray) {
						if (StringUtil.isNotNull(cc)) {
							mailClient.addCc(cc, "Support");
							mailCount++;
						}
					}
				}
				Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);
				/**
				 * Store Mail data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(authDTO.getUser().getUsername());
				notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
				notificationDTO.setNotificationType(NotificationTypeEM.USER_REGISTER);
				notificationDTO.setParticipantAddress(authDTO.getUser().getEmail() + "," + ccString);
				notificationDTO.setTransactionCount(mailCount);
				notificationDTO.setRequestLog(subject + " " + dataModel.toString());
				notificationDTO.setResponseLog(response.get());
				saveMailNotification(authDTO, notificationDTO);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Async
	public void sendFeedbackEmail(AuthDTO authDTO, UserFeedbackDTO feedbackDTO, String toEmail) {
		try {
			String body = null;
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("mobile", feedbackDTO.getMobile());
			dataModel.put("email", feedbackDTO.getEmail());
			dataModel.put("ticketCode", feedbackDTO.getTicketCode());
			dataModel.put("comments", feedbackDTO.getComments());
			body = TemplateUtils.getInstance().processEmailContent(CUSTOMER_FEEDBACK, dataModel);
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Support", Text.EMPTY);
			String emailIds[] = (StringUtil.isNotNull(toEmail) ? toEmail.split(",") : null);
			int mailCount = 1;
			if (emailIds != null) {
				for (String email : emailIds) {
					if (StringUtil.isNotNull(email)) {
						mailClient.addTo(email.trim(), "Passenger");
						mailCount++;
					}
				}
			}
			Future<String> response = mailClient.sendAsyncHtmlEmail("Passenger online Feedback", body);
			/**
			 * Store Mail data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(authDTO.getUser().getUsername());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FEEDBACK);
			notificationDTO.setParticipantAddress(toEmail);
			notificationDTO.setTransactionCount(mailCount);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void validateSMSContent(TicketDTO ticketDTO) {
		if (ticketDTO.getBoardingPoint() == null || StringUtil.isNull(ticketDTO.getBoardingPoint().getName()) || StringUtil.isNull(ticketDTO.getSeatNames()) || ticketDTO.getBoardingPoint().getMinitues() == 0 || ticketDTO.getBoardingPoint().getId() == 0) {
			System.out.println("EREMAIL01 " + ticketDTO.getCode() + " " + ticketDTO.getBoardingPoint().getName() + ticketDTO.getBoardingPoint().getId() + ticketDTO.getBoardingPoint().getCode());
		}

	}

	@Override
	public void sendRegistrationRequestEmail(AuthDTO authDTO, UserRegistrationDTO registrationDTO) {
		// TODO Auto-generated method stub

	}

	@Async
	public void sendDeviceRegistration(AuthDTO authDTO, NamespaceDeviceDTO deviceDTO) {

		try {
			String body = null;
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("deviceCode", deviceDTO.getCode());
			dataModel.put("token", deviceDTO.getToken());
			dataModel.put("generatedBy", authDTO.getUser().getName());
			body = TemplateUtils.getInstance().processEmailContent(DEVICE_REGISTER, dataModel);
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Support", Text.EMPTY);
			mailClient.addTo(authDTO.getUser().getEmail(), authDTO.getUser().getName());
			mailClient.addCc("tech@ezeeinfosolutions.com", "Tech");
			mailClient.sendAsyncHtmlEmail("New Device Register" + deviceDTO.getCode(), body);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Async
	public void sendAuditEventAlertEmail(AuthDTO authDTO, Map<String, String> dataModel, AuditEventDTO auditEvent) {
		try {
			String body = null;
			String email = dataModel.get("emailId");
			Map<String, Object> dataModelMap = new HashMap<String, Object>();
			dataModelMap.putAll(dataModel);

			body = TemplateUtils.getInstance().processEmailContent("auditeventalert", dataModelMap);
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Support", Text.EMPTY);
			mailClient.addTo(email, "Team");
			Future<String> response = mailClient.sendAsyncHtmlEmail("Audit Event Alert", body);
			/**
			 * Store Mail data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(authDTO.getUser().getUsername());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FEEDBACK);
			notificationDTO.setParticipantAddress(email);
			notificationDTO.setTransactionCount(1);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Async
	public void sendFailureOrderBookingEmail(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> orderStatusMap) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("name", ticketDTO.getPassengerName());
			dataModel.put("origin", ticketDTO.getFromStation().getName());
			dataModel.put("destination", ticketDTO.getToStation().getName());
			dataModel.put("travelDate", ticketDTO.getTripDate().toString());
			dataModel.put("travelTime", ticketDTO.getTripTime());
			dataModel.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
			dataModel.put("boardingPointTime", DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format(Text.DATE_TIME_DATE4J));
			dataModel.put("fare", ticketDTO.getTotalFare().toString());
			dataModel.put("ticketCode", ticketDTO.getCode());
			dataModel.put("operator", authDTO.getNamespace().getName());
			dataModel.put("bookedBy", authDTO.getUser().getName());
			dataModel.put("statusMap", orderStatusMap.toString());
			validateSMSContent(ticketDTO);

			String body = TemplateUtils.getInstance().processEmailContent("failurebook", dataModel);
			String subject = authDTO.getNamespace().getName() + " Failure Ticket Confirmed on " + DateUtil.NOW().format(Text.DATE_DATE4J);

			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Ramasamy");

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			/**
			 * Store Mail data in ticket_notification table
			 */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FAILIURE_TICKET);
			notificationDTO.setParticipantAddress("ramasamy@ezeeinfosolutions.com");
			notificationDTO.setTransactionCount(1);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendTripCancellEmail(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("refund", ticketDTO.getRefundAmount());
			dataModel.put("discount", ticketDTO.getAddonsValue().compareTo(BigDecimal.ZERO) != 0 ? String.valueOf(ticketDTO.getAddonsValue()) : Text.EMPTY);
			dataModel.put("tripCode", ticketDTO.getTripDTO().getCode());
			dataModel.put("bookingId", ticketDTO.getCode());
			dataModel.put("fromStation", ticketDTO.getFromStation().getName());
			dataModel.put("toStation", ticketDTO.getToStation().getName());

			JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);

			String email = null;
			if (jsonObject != null && jsonObject.has("email") && StringUtil.isNotNull(jsonObject.getString("email"))) {
				email = jsonObject.getString("email");
			}

			if (StringUtil.isNull(email)) {
				email = "ezeebus@ezeeinfosolutions.com";
			}
			String ccArray[] = (StringUtil.isNotNull(email) ? email.split(",") : null);

			String body = TemplateUtils.getInstance().processEmailContent("tripcancel", dataModel);
			String subject = "Ticket Trip Cancel";
			EmailClient mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName(), Text.EMPTY);
			mailClient.addTo(email, "Admin");

			int mailCount = 1;
			if (ccArray != null) {
				for (String cc : ccArray) {
					if (StringUtil.isNotNull(cc) && !ccArray[0].equals(cc)) {
						mailClient.addCc(cc, "Support");
						mailCount++;
					}
				}
			}

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.TRIP_NOTIIFICATION);
			notificationDTO.setParticipantAddress(email);
			notificationDTO.setTransactionCount(mailCount);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);

		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode() + " unable to Trip cancel mail");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendReplyToFeedbackEmail(AuthDTO authDTO, UserFeedbackDTO userFeedback) {
		String responseLog = null;
		try {
			String body = null;
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("name", userFeedback.getName());
			dataModel.put("email", userFeedback.getEmail());
			dataModel.put("content", userFeedback.getReplyContent());
			dataModel.put("domain", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("namespace", authDTO.getNamespace().getName());

			body = TemplateUtils.getInstance().processEmailContent(PASSENGER_FEEDBACK_REPLY, dataModel);
			EmailClient mailClient = EmailClient.getDefaultMailClient("Travel Feedback", authDTO.getNamespace().getProfile().getEmailCopyAddress());
			String emailIds[] = (StringUtil.isNotNull(userFeedback.getEmail()) ? userFeedback.getEmail().split(",") : null);
			int mailCount = 1;
			if (emailIds != null) {
				for (String email : emailIds) {
					if (StringUtil.isNotNull(email)) {
						mailClient.addTo(email.trim(), "Passenger");
						mailCount++;
					}
				}
			}
			Future<String> response = mailClient.sendAsyncHtmlEmail("Feedback response from " + authDTO.getNamespace().getName(), body);
			responseLog = response.get();
			/**
			 * Store Mail data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(authDTO.getUser().getUsername());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FEEDBACK_REPLY);
			notificationDTO.setParticipantAddress(userFeedback.getEmail());
			notificationDTO.setTransactionCount(mailCount);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(responseLog);
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return responseLog;
	}

	@Async
	public void sendScheduleUpdateEmail(AuthDTO authDTO, List<Map<String, String>> finalScheduleChanges, String referenceCode, List<String> toEmailIds, List<String> ccEmailIds) {
		try {
			StringBuilder participantAddress = new StringBuilder();

			// Mail will send to only customer and WEB user
			Map<String, Object> dataModel = Maps.newHashMap();
			String headerContent = finalScheduleChanges.get(0).get("header");
			dataModel.put("namespaceName", authDTO.getNamespace().getName());
			dataModel.put("headerContent", headerContent);
			dataModel.put("logDetails", finalScheduleChanges);

			String body = TemplateUtils.getInstance().processEmailContent("schedulenotification", dataModel);
			String subject = "[" + authDTO.getNamespace().getName() + "] " + headerContent;
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
			for (String toEmail : toEmailIds) {
				mailClient.addTo(toEmail, authDTO.getNamespace().getName());
				participantAddress.append(toEmail);
				participantAddress.append(Text.COMMA);
			}
			for (String ccEmail : ccEmailIds) {
				mailClient.addCc(ccEmail, "Support");
				participantAddress.append(ccEmail);
				participantAddress.append(Text.COMMA);
			}

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);
			/**
			 * Store Mail data in ticket_notification table
			 */

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(referenceCode);
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.SCHEDULE_UPDATE);
			notificationDTO.setParticipantAddress(participantAddress.toString());
			notificationDTO.setTransactionCount(toEmailIds.size() + ccEmailIds.size());
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendFailureSMSGatewayEmail(AuthDTO authDTO, Map<String, Object> dataModel) {
		try {
			String body = null;
			Map<String, Object> dataModelMap = new HashMap<String, Object>();
			dataModelMap.putAll(dataModel);

			body = TemplateUtils.getInstance().processEmailContent("failuresms", dataModelMap);
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Support", Text.EMPTY);
			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Team");
			Future<String> response = mailClient.sendAsyncHtmlEmail("[" + dataModel.get("namespace") + "] " + "Failure SMS", body);

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode("FAILSMS");
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FAILURE_SMS);
			notificationDTO.setParticipantAddress("ramasamy@ezeeinfosolutions.com");
			notificationDTO.setTransactionCount(1);
			notificationDTO.setRequestLog(dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Async
	public void sendS2SFailureBookingEmail(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("ticketCode", ticketDTO.getCode());
			dataModel.put("operator", authDTO.getNamespace().getName());
			dataModel.put("origin", ticketDTO.getFromStation().getName());
			dataModel.put("destination", ticketDTO.getToStation().getName());
			dataModel.put("name", ticketDTO.getPassengerName());
			dataModel.put("fare", ticketDTO.getTotalFare().toString());
			dataModel.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
			dataModel.put("boardingDate", DateUtil.parseDateFormat(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("time", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
			dataModel.put("website", authDTO.getNamespace().getProfile().getDomainURL());

			String body = TemplateUtils.getInstance().processEmailContent("s2sfailureticketbook", dataModel);

			String subject = "[" + authDTO.getNamespace().getName() + "]" + "Ticket Pending Order" + ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName() + " on " + ticketDTO.getTripDate().format(Text.DATE_DATE4J);
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
			mailClient.addTo("ezeebus@ezeeinfosolutions.com", "Devops Team");
//			mailClient.addCc("ramasamy@ezeeinfosolutions.com", "Support Team");

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			/**
			 * Store Mail data in ticket_notification table
			 */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FAILIURE_TICKET_BOOKING);
			notificationDTO.setParticipantAddress("arun@ezeeinfo.in");
			notificationDTO.setTransactionCount(1);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			System.out.println(ticketDTO.getPassengerEmailId() + " " + ticketDTO.getCode());
			e.printStackTrace();
		}
	}

	@Async
	public void sendPaymentOrderVerificationFailureEmail(AuthDTO authDTO, OrderDTO orderDTO, Map<String, String> orderStatusMap) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("orderCode", orderDTO.getOrderCode());
			dataModel.put("operator", authDTO.getNamespace().getName());
			dataModel.put("orderType", orderDTO.getOrderType().getName());
			dataModel.put("statusMap", orderStatusMap != null ? orderStatusMap.toString() : Text.EMPTY);

			String body = TemplateUtils.getInstance().processEmailContent("orderverificationfailure", dataModel);
			String subject = "[" + authDTO.getNamespace().getName() + "]" + " Payment Mismatch On Internal Verification";

			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
			mailClient.addTo("arun@ezeeinfo.in", "Arun");

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			/**
			 * Store Mail data in ticket_notification table
			 */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(orderDTO.getOrderCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.FAILIURE_TICKET_BOOKING);
			notificationDTO.setParticipantAddress("arun@ezeeinfo.in");
			notificationDTO.setTransactionCount(1);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendBitsShortURLFailureEmail(Map<String, Object> dataModel) {
		try {
			String body = TemplateUtils.getInstance().processEmailContent("shorturlfailure", dataModel);
			String subject = "Bits Short URL Failure";

			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
			mailClient.addTo("arun@ezeeinfo.in", "Arun");

			mailClient.sendAsyncHtmlEmail(subject, body);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendTaxInvoiceEmail(AuthDTO authDTO, TicketDTO ticketDTO, TicketTaxDTO ticketTaxDTO) {
		try {
			String emailId = StringUtil.isNotNull(ticketTaxDTO.getEmail()) ? ticketTaxDTO.getEmail() : ticketDTO.getPassengerEmailId();
			
			final String BOLD_OPEN_TAG = "<b>";
			final String BOLD_CLOSE_TAG = "</b>";
			final String BREAK_TAG = "<br/>";
			
			StringBuilder description = new StringBuilder();
			description.append(BOLD_OPEN_TAG + "PNR No. : " + BOLD_CLOSE_TAG).append(ticketDTO.getCode()).append(BREAK_TAG);
			description.append(BOLD_OPEN_TAG + "Route : " + BOLD_CLOSE_TAG).append(ticketDTO.getFromStation().getName()).append(" to ").append(ticketDTO.getToStation().getName()).append(BREAK_TAG);
			description.append(BOLD_OPEN_TAG + "Service No. : " + BOLD_CLOSE_TAG).append(ticketDTO.getServiceNo()).append(BREAK_TAG);
			description.append(BOLD_OPEN_TAG + "Name : " + BOLD_CLOSE_TAG).append(ticketDTO.getPassengerName()).append(BREAK_TAG);
			description.append(BOLD_OPEN_TAG + "Travel Date : " + BOLD_CLOSE_TAG).append(DateUtil.parseDateFormat(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd hh:mm:ss", "dd/MM/yyyy hh:mm a")).append(BREAK_TAG);
			description.append(BOLD_OPEN_TAG + "Seat Name : " + BOLD_CLOSE_TAG).append(ticketDTO.getSeatNames()).append(BREAK_TAG);
			description.append(BOLD_OPEN_TAG + "Bus Type : " + BOLD_CLOSE_TAG).append(BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode())).append(BREAK_TAG);

			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("description", description.toString());
			dataModel.put("taxableValue", ticketDTO.getTotalSeatFare().setScale(2));
			dataModel.put("ticketCode", ticketDTO.getCode());
			dataModel.put("cgst", ticketDTO.getCgst().setScale(2));
			dataModel.put("sgst", ticketDTO.getSgst().setScale(2));
			dataModel.put("customerTradeName", ticketTaxDTO != null && StringUtil.isNotNull(ticketTaxDTO.getTradeName()) ? ticketTaxDTO.getTradeName().toUpperCase() : Text.HYPHEN);
			dataModel.put("customerGstin", ticketTaxDTO != null && StringUtil.isNotNull(ticketTaxDTO.getGstin()) ? ticketTaxDTO.getGstin().toUpperCase() : Text.HYPHEN);
			dataModel.put("date", DateUtil.parseDateFormat(DateUtil.NOW().format("YYYY-MM-DD"), "yyyy-MM-dd", "dd/MM/yyyy"));
			dataModel.put("seatCount", ticketDTO.getTicketDetails().size());
			dataModel.put("route", ticketDTO.getFromStation().getName() + " - " + ticketDTO.getToStation().getName());
			dataModel.put("busType", BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()));
			dataModel.put("boarding", ticketDTO.getBoardingPoint().getAddress());
			dataModel.put("boardingTime", DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("DD-MM-YYYY hh12:mm a", Locale.ENGLISH));
			dataModel.put("mobile", ticketDTO.getPassengerMobile());
			dataModel.put("dropping", ticketDTO.getDroppingPoint().getAddress());
			dataModel.put("namespaceTradeName", StringUtil.isNotNull(ticketDTO.getTax().getTradeName()) ? ticketDTO.getTax().getTradeName().toUpperCase() : Text.HYPHEN);
			dataModel.put("namespaceGstin", StringUtil.isNotNull(ticketDTO.getTax().getGstin()) ? ticketDTO.getTax().getGstin().toUpperCase() : Text.HYPHEN);
			dataModel.put("cgstPercentage", ticketDTO.getTax().getCgstValue());
			dataModel.put("sgstPercentage", ticketDTO.getTax().getSgstValue());
			dataModel.put("sacCode", ticketDTO.getTax().getSacNumber());
			dataModel.put("discount", ticketDTO.getAddonsValue().setScale(2));
			dataModel.put("total", ticketDTO.getTicketFareWithAddons().setScale(2));
			dataModel.put("totalInWords", StringUtils.capitalize(NumberToWordConvertor.convert(ticketDTO.getTicketFareWithAddons().doubleValue())));

			String body = TemplateUtils.getInstance().processEmailContent(TAX_INVOICE_TEMPLATE, dataModel);
			String subject = "Tax Invoice";
			EmailClient mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName(), Text.EMPTY);
			mailClient.addTo(emailId, StringUtils.isNotBlank(ticketDTO.getPassengerName()) ? ticketDTO.getPassengerName() : "Passenger");

			String ccString = authDTO.getNamespace().getProfile().getEmailCopyAddress();
			String ccArray[] = (StringUtil.isNotNull(ccString) ? ccString.split(Text.COMMA) : null);
			int mailCount = 1;
			if (ccArray != null) {
				for (String cc : ccArray) {
					if (StringUtil.isNotNull(cc)) {
						mailClient.addCc(cc, "Support");
						mailCount++;
					}
				}
			}

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			/**
			 * Store Mail data in ticket_notification table
			 */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.TAX_INVOICE);
			notificationDTO.setParticipantAddress(ticketDTO.getPassengerEmailId() + Text.COMMA + ccString);
			notificationDTO.setTransactionCount(mailCount);
			notificationDTO.setRequestLog(subject + Text.SINGLE_SPACE + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			System.out.println(ticketDTO.getPassengerEmailId() + Text.SINGLE_SPACE + ticketDTO.getCode());
			e.printStackTrace();
		}

	}

	@Async
	public void sendCancelEmailV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			if (ticketDTO.getTicketUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId() && DeviceMediumEM.API_USER.getId() != ticketDTO.getDeviceMedium().getId() && authDTO.getNamespace().getProfile().isEmailNotificationFlag()) {
				Map<String, Object> dataModel = Maps.newHashMap();
				dataModel.put("passengerName", ticketDTO.getPassengerName());
				dataModel.put("travelsName", authDTO.getNamespace().getName());
				dataModel.put("pnr", ticketDTO.getCode());
				dataModel.put("originName", ticketDTO.getFromStation().getName());
				dataModel.put("destinationName", ticketDTO.getToStation().getName());
				dataModel.put("boardingPointName", ticketDTO.getBoardingPoint().getName());
				dataModel.put("boardingPointTime", DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format(Text.TWELVE_HOURS_FORMAT, Locale.ENGLISH));
				dataModel.put("travelDate", ticketDTO.getTripDate().format("DD/MM/YYYY"));
				dataModel.put("seats", ticketDTO.getSeatNames());
				dataModel.put("busType", BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()));
				dataModel.put("paidAmount", ticketDTO.getTotalFare());
				dataModel.put("cancellationCharges", ticketDTO.getCancellationCharges());
				dataModel.put("discount", ticketDTO.getAddonsValue().compareTo(BigDecimal.ZERO) != 0 ? String.valueOf(ticketDTO.getAddonsValue()) : Text.EMPTY);
				dataModel.put("refund", String.valueOf(ticketDTO.getRefundAmount()));
				dataModel.put("cancellationTime", DateUtil.NOW().format("DD/MM/YYYY hh12:mm a", Locale.ENGLISH));
				dataModel.put("totalSeatFare", ticketDTO.getTotalSeatFare());
				dataModel.put("contact", ticketDTO.getBoardingPoint().getNumber());
				dataModel.put("website", authDTO.getNamespace().getProfile().getDomainURL());

				String body = TemplateUtils.getInstance().processEmailContent(CANCEL_TICKET_TEMPLATE, dataModel);
				String subject = authDTO.getNamespace().getName() + Text.SINGLE_SPACE + "Ticket Cancelled" + Text.HYPHEN + ticketDTO.getCode();
				EmailClient mailClient = EmailClient.getDefaultMailClient(authDTO.getNamespace().getProfile().getSendarMailName(), Text.EMPTY);
				mailClient.addTo(ticketDTO.getPassengerEmailId(), StringUtils.isNotBlank(ticketDTO.getPassengerName()) ? ticketDTO.getPassengerName() : "Passenger");
				String ccString = authDTO.getNamespace().getProfile().getEmailCopyAddress();
				String ccArray[] = (StringUtil.isNotNull(ccString) ? ccString.split(",") : null);
				int mailCount = 1;
				if (ccArray != null) {
					for (String cc : ccArray) {
						if (StringUtil.isNotNull(cc)) {
							mailClient.addCc(cc, "Support");
							mailCount++;
						}
					}
				}

				Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

				/**
				 * Store Mail data in ticket_notification table
				 */

				NotificationDTO notificationDTO = new NotificationDTO();
				notificationDTO.setRefferenceCode(ticketDTO.getCode());
				notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
				notificationDTO.setNotificationType(NotificationTypeEM.CONFIRM_CANCELLATION);
				notificationDTO.setParticipantAddress(ticketDTO.getPassengerEmailId() + "," + ccString);
				notificationDTO.setTransactionCount(mailCount);
				notificationDTO.setRequestLog(subject + " " + dataModel.toString());
				notificationDTO.setResponseLog(response.get());
				saveMailNotification(authDTO, notificationDTO);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendTransactionEmail(AuthDTO authDTO, List<UserTransactionDTO> userTransactionlist) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			List<Map<String, Object>> transactionDetails = new ArrayList<Map<String, Object>>();
			for (UserTransactionDTO userTransactionDTO : userTransactionlist) {
				Map<String, Object> subDataModel = Maps.newHashMap();
				subDataModel.put("pnr", userTransactionDTO.getRefferenceCode());
				subDataModel.put("transactionDate", new DateTime(userTransactionDTO.getTransactionDate()).format("DD/MM/YYYY hh12:mm a", Locale.ENGLISH));
				subDataModel.put("transactionType", userTransactionDTO.getTransactionType().getName());
				subDataModel.put("transactionMode", userTransactionDTO.getTransactionMode().getName());
				subDataModel.put("transactionAmount", userTransactionDTO.getTransactionAmount());
				subDataModel.put("commissionAmount", userTransactionDTO.getCommissionAmount());
				subDataModel.put("tdsTax", userTransactionDTO.getTdsTax());
				subDataModel.put("creditAmount", userTransactionDTO.getCreditAmount());
				subDataModel.put("debitAmount", userTransactionDTO.getDebitAmount());
				subDataModel.put("closingBalance", userTransactionDTO.getClosingBalanceAmount());
				subDataModel.put("userCode", userTransactionDTO.getUser().getCode());
				subDataModel.put("userName", userTransactionDTO.getUser().getName());
				transactionDetails.add(subDataModel);
			}
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("transactionDetails", transactionDetails);

			String body = TemplateUtils.getInstance().processEmailContent(TRANSACTION_TEMPLATE, dataModel);
			String subject = authDTO.getNamespace().getName() + Text.SINGLE_SPACE + "Balance Mismatch Transactions";
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info", Text.EMPTY);
			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Support Team");

			mailClient.sendAsyncHtmlEmail(subject, body);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendReportEmail(AuthDTO authDTO, String mailIds, String fileName, String url) {
		try {

			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("url", url);
			dataModel.put("fileName", fileName);

			String body = TemplateUtils.getInstance().processEmailContent(REPORT_TEMPLATE, dataModel);
			String subject = authDTO.getNamespace().getName() + Text.SINGLE_SPACE + fileName;
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info", Text.EMPTY);
			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Support Team");
			String ccArray[] = (StringUtil.isNotNull(mailIds) ? mailIds.split(",") : null);
			int mailCount = 1;
			if (ccArray != null) {
				for (String cc : ccArray) {
					if (StringUtil.isNotNull(cc)) {
						mailClient.addCc(cc, "Support");
						mailCount++;
					}
				}
			}
			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(Text.NA);
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.OVERALL_OCCUPANCY_SUMMARY);
			notificationDTO.setParticipantAddress(mailIds);
			notificationDTO.setTransactionCount(mailCount);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendDenialTicketEmail(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("ticketCode", ticketDTO.getCode());
			dataModel.put("namespace", authDTO.getNamespace().getName());
			dataModel.put("travelDate", ticketDTO.getTripDate());
			dataModel.put("seats", ticketDTO.getSeatCodeNames());
			dataModel.put("serviceNumber", ticketDTO.getServiceNo());
			dataModel.put("name", ticketDTO.getPassengerName());
			dataModel.put("origin", ticketDTO.getFromStation().getName());
			dataModel.put("destination", ticketDTO.getToStation().getName());
			dataModel.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
			dataModel.put("boardingPointTime", DateUtil.parseDateFormat(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("fare", ticketDTO.getTotalSeatFare());

			String body = TemplateUtils.getInstance().processEmailContent("denialticket", dataModel);
			String subject = "[" + authDTO.getNamespace().getName() + "] " + ticketDTO.getCode() + " Denial Ticket";

			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
//			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Ramasamy");
			mailClient.addCc("ezeebus@ezeeinfosolutions.com", "Ezee Support");

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			/**
			 * Store Mail data in ticket_notification table
			 */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.TICKET_RELEASE);
			notificationDTO.setParticipantAddress("ramasamy@ezeeinfosolutions.com");
			notificationDTO.setTransactionCount(2);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendPendingOrderCancelMail(AuthDTO authDTO, RefundDTO refundDTO, TicketDTO ticketDTO) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("travelsName", authDTO.getNamespaceCode());
			dataModel.put("pnr", ticketDTO.getCode());
			dataModel.put("amount", refundDTO.getAmount());

			String body = TemplateUtils.getInstance().processEmailContent(Text.SMS_DENIAL_ORDER_EMAIL, dataModel);
			String subject = "[" + authDTO.getNamespace().getName() + "] " + ticketDTO.getCode() + " Denial Ticket";

			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info Tech", Text.EMPTY);
			mailClient.addTo(ticketDTO.getPassengerEmailId(), ticketDTO.getPassengerName());
			mailClient.addCc("ezeebus@ezeeinfosolutions.com", "Ezee Support");

			Future<String> response = mailClient.sendAsyncHtmlEmail(subject, body);

			/**
			 * Store Mail data in ticket_notification table
			 */
			NotificationDTO notificationDTO = new NotificationDTO();
			notificationDTO.setRefferenceCode(ticketDTO.getCode());
			notificationDTO.setNotificationMode(NotificationMediumEM.E_MAIL);
			notificationDTO.setNotificationType(NotificationTypeEM.PENDING_ORDER_CANCELLATION);
			notificationDTO.setParticipantAddress(ticketDTO.getPassengerEmailId());
			notificationDTO.setTransactionCount(2);
			notificationDTO.setRequestLog(subject + " " + dataModel.toString());
			notificationDTO.setResponseLog(response.get());
			saveMailNotification(authDTO, notificationDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendMismatchTransactionEmail(AuthDTO authDTO, List<TicketTransactionDTO> ticketTransactionlist) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			List<Map<String, Object>> transactionDetails = new ArrayList<Map<String, Object>>();
			for (TicketTransactionDTO ticketTransactionDTO : ticketTransactionlist) {
				Map<String, Object> subDataModel = Maps.newHashMap();
				subDataModel.put("code", ticketTransactionDTO.getCode());
				subDataModel.put("transactionType", ticketTransactionDTO.getTransactionType().getName());
				subDataModel.put("transactionMode", ticketTransactionDTO.getTransactionMode().getName());
				subDataModel.put("transactionAmount", ticketTransactionDTO.getTransactionAmount().toString());
				subDataModel.put("commissionAmount", ticketTransactionDTO.getCommissionAmount().toString());
				subDataModel.put("tdsTax", ticketTransactionDTO.getTdsTax().toString());
				subDataModel.put("acBusTax", ticketTransactionDTO.getAcBusTax().toString());
				subDataModel.put("addonAmount", ticketTransactionDTO.getAddonsAmount().toString());
				subDataModel.put("status", ticketTransactionDTO.getRemarks());
				transactionDetails.add(subDataModel);
			}
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("transactionDetails", transactionDetails);

			String body = TemplateUtils.getInstance().processEmailContent(TICKET_TRANSACTION_TEMPLATE, dataModel);
			String subject = authDTO.getNamespace().getName() + Text.SINGLE_SPACE + "Ticket Transaction Mismatch";
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info", Text.EMPTY);
			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Support Team");

			mailClient.sendAsyncHtmlEmail(subject, body);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendEzeebotFeedbackMail(Map<String, String> dataModel) {
		try {
			String body = dataModel.toString();
			String subject = "ezeebot feedback";
			EmailClient mailClient = EmailClient.getDefaultMailClient("Ezee Info", Text.EMPTY);
			mailClient.addTo("ramasamy@ezeeinfosolutions.com", "Ramasmay");
			mailClient.addCc("deepak@ezeeinfosolutions.com", "Deepak");
			mailClient.addCc("kathir@ezeeinfosolutions.com", "Kathir");
			mailClient.sendAsyncHtmlEmail(subject, body);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
