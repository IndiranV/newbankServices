package org.in.com.aggregator.whatsapp;

import java.util.Map;
import java.util.concurrent.Future;

import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.NotificationTypeEM;

public interface WhatsappService {

	public boolean sendTripJourneyTracking(AuthDTO authDTO, Map<String, String> dataModel, NotificationTypeEM notificationType);
	
	public boolean sendWhatsappNumberVerification(AuthDTO authDTO, Map<String, String> dataModel);
	
	public boolean sendCustomerTicketEvent(AuthDTO authDTO, Map<String, String> dataModel, NotificationTemplateConfigDTO templateConfig);
	
	public boolean sendTicketBookingNotification(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataModel);
	
	public boolean sendTicketCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO);

	public Future<SmsResponse> tripDelayNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String delayTime, String supportNumber);

	public Future<SmsResponse> tripCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String supportNumber);

	public Future<SmsResponse> tripEarlyNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String earlyTime, String supportNumber);

	public boolean sendTicketCancelNotification(AuthDTO authDTO, TicketDTO repositoryTicketDTO, Map<String, String> dataModel);

	public boolean sendFeedbackSMS(AuthDTO authDTO, Map<String, String> dataModel);
}
