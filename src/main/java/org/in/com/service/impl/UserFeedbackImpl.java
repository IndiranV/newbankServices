package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.aggregator.whatsapp.WhatsappService;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.UserRegistrationDTO;
import org.in.com.dto.enumeration.NamespaceEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.service.NotificationPushService;
import org.in.com.service.TicketService;
import org.in.com.service.UserFeedbackService;
import org.in.com.utils.BitsShortURL;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

@Service
public class UserFeedbackImpl implements UserFeedbackService {
	@Autowired
	EmailService emailService;
	@Autowired
	BitsService bitsService;
	@Autowired
	SMSService smsService;
	@Autowired
	TicketService ticketService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	WhatsappService whatsappService;

	public List<UserFeedbackDTO> getAll(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		UserDAO dao = new UserDAO();
		List<UserFeedbackDTO> list = dao.getAllUserFeedback(authDTO, fromDate, toDate);
		return list;
	}

	public UserFeedbackDTO Update(AuthDTO authDTO, UserFeedbackDTO feedbackDTO) {
		UserDAO dao = new UserDAO();
		try {
			// Email - Bits Configure
			JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);
			if (jsonObject != null && jsonObject.has("email") && StringUtil.isNotNull(jsonObject.getString("email"))) {
				String emailId = jsonObject.getString("email");
				emailService.sendFeedbackEmail(authDTO, feedbackDTO, emailId);
			}
		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode() + " unable to feedback mail");
		}
		feedbackDTO = dao.updateUserFeedback(authDTO, feedbackDTO);
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.CUSTOMER_FEEDBACK)) {
			notificationPushService.pushCustomerFeedbackNotification(authDTO, feedbackDTO);
		}
		return feedbackDTO;
	}

	public List<UserRegistrationDTO> getUserRegistrationRequest(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		UserDAO dao = new UserDAO();
		return dao.getUserRegistrationRequest(authDTO, fromDate, toDate);

	}

	public UserRegistrationDTO addUserRegistrationRequest(AuthDTO authDTO, UserRegistrationDTO registrationDTO) {

		UserDAO dao = new UserDAO();
		if (authDTO.getNamespace().getCode().equals(NamespaceEM.TATTRAVELS.getCode())) {
			emailService.sendRegistrationRequestEmail(authDTO, registrationDTO);
		}
		return dao.addUserRegistrationRequest(authDTO, registrationDTO);
	}

	@Override
	public String sendReplyToUserFeedback(AuthDTO authDTO, UserFeedbackDTO userFeedback) {
		UserFeedbackDTO dbFeedbackDTO = new UserFeedbackDTO();
		dbFeedbackDTO.setCode(userFeedback.getCode());
		dbFeedbackDTO.setReplyContent(userFeedback.getReplyContent());

		UserDAO dao = new UserDAO();
		dao.getUserFeedback(authDTO, dbFeedbackDTO);
		userFeedback.setName(dbFeedbackDTO.getName());

		String response = null;
		if (StringUtil.isNotNull(userFeedback.getMobile())) {
			response = smsService.sendReplyToFeedbackSMS(authDTO, userFeedback);
		}
		else if (StringUtil.isNotNull(userFeedback.getEmail())) {
			response = emailService.sendReplyToFeedbackEmail(authDTO, userFeedback);
		}

		// Update Comment
		dao.updateUserFeedbackComment(authDTO, dbFeedbackDTO);
		return response;
	}

	public void sendFeedBackSMS(AuthDTO authDTO, DateTime tripDate) {
		TicketDTO ticket = new TicketDTO();
		ticket.setTripDate(tripDate);
		List<TicketDTO> ticketList = ticketService.getTicketsForFeedback(authDTO, ticket);
		ticketList = getUniqueMobileTicketList(ticketList);
		for (TicketDTO ticketDTO : ticketList) {
			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("namespaceCode", authDTO.getNamespace().getCode());
			dataModel.put("namespaceName", authDTO.getNamespace().getName());
			dataModel.put("ticketCode", ticketDTO.getCode());
			dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());
			dataModel.put("devicemedium", ticketDTO.getDeviceMedium() != null ? ticketDTO.getDeviceMedium().getCode() : Text.NA);
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
			if (StringUtil.isNotNull(authDTO.getNamespace().getProfile().getWhatsappUrl())) {
				String link = Constants.WHATSAPP_LINK + "?phone=" + "91" + authDTO.getNamespace().getProfile().getWhatsappNumber();
				String shortURL = BitsShortURL.getUrlshortener(link, BitsShortURL.TYPE.PER);
				dataModel.put("whatsappUrl", shortURL);
			}
			else {
				dataModel.put("whatsappUrl", Text.HYPHEN);
			}
			if (!authDTO.getNamespace().getCode().equals(NamespaceEM.TRANZKING.getCode())) {
				String url = "http://" + authDTO.getNamespace().getProfile().getDomainURL().replace("www", "m") + "/feedback?pnr=" + ticketDTO.getCode() + "&zone=" + ApplicationConfig.getServerZoneCode();
				dataModel.put("feedbackUrl", BitsShortURL.getUrlshortener(url, BitsShortURL.TYPE.TMP));
			}
			dataModel.put("template", Text.SMS_FEEDBACK);

			boolean status = whatsappService.sendFeedbackSMS(authDTO, dataModel);
			if (!status) {
				smsService.sendFeedbackSMS(authDTO, dataModel);
			}
		}

	}

	private List<TicketDTO> getUniqueMobileTicketList(List<TicketDTO> nonUniqueAccountList) {
		Map<String, TicketDTO> uniqueAccountsMapList = new HashMap<String, TicketDTO>();
		if (nonUniqueAccountList != null && !nonUniqueAccountList.isEmpty()) {
			for (TicketDTO nprDto : nonUniqueAccountList) {
				uniqueAccountsMapList.put(nprDto.getPassengerMobile(), nprDto);
			}
		}
		return new ArrayList<TicketDTO>(uniqueAccountsMapList.values());
	}
}
