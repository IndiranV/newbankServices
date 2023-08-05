package org.in.com.aggregator.sms;

import java.util.Map;
import java.util.concurrent.Future;

import net.sf.json.JSONObject;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;

public interface SMSService {

	public Future<SmsResponse> sendMTicketSMSNew(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataModel);

	public Future<SmsResponse> sendMTicketTransferSMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public Future<SmsResponse> sendCancelSMS(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataModel);

	public Future<SmsResponse> sendPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO);

	public Future<SmsResponse> sendPhoneBookingCancelSMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public Future<SmsResponse> sendTripJourneyTrackingSMS(AuthDTO authDTO, Map<String, String> dataModel, NotificationTypeEM notificationType);

	public Future<SmsResponse> sendTripNotification(AuthDTO authDTO, TripChartDetailsDTO dto, String busNo, String contact);

	public Future<SmsResponse> tripCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String supportNumber);

	public Future<SmsResponse> tripDelayNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String delayTime, String supportNumber);

	public Future<SmsResponse> tripStationPointChangeNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String stationPointName, String stationPointTime, String contactNumber);

	public Future<SmsResponse> tripCovidEpassNotification(AuthDTO authDTO, TicketDTO ticketDTO, String registrationNumber, String supportNumber);

	public Future<SmsResponse> sendTransactionOTP(AuthDTO authDTO, int OTP, String contactNumber);

	public Future<SmsResponse> sendRechargeSMS(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO);

	public void sendBusbuddyAfterboard(AuthDTO authDTO, String ticketCode, String vehicleNumber, String mobileNumber);

	public Future<SmsResponse> tripEarlyNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String earlyTime, String supportNumber);

	public Future<SmsResponse> eventAlertUtil(AuthDTO authDTO, Map<String, String> dataModel, AuditEventTypeEM namespaceEventType);

	public Future<SmsResponse> sendFaliureTicketSMS(AuthDTO authDTO, TicketDTO ticket);

	public Map<String, String> getSMSStatus(AuthDTO authDTO, String refferenceCode);

	public String sendVanPickupTracking(AuthDTO authDTO, Map<String, String> dataModel);

	public String sendReplyToFeedbackSMS(AuthDTO authDTO, UserFeedbackDTO userFeedback);

	public String sendVehicleChangeSms(AuthDTO authDTO, TicketDTO ticketDTO, TripInfoDTO tripInfo);

	public Future<SmsResponse> sendTicketEventSMS(AuthDTO authDTO, TicketDTO ticketDTO, String ticketStatus);

	public String sendTicketAfterTripTimeNotify(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> additionalDetails);

	public String sendTicketAfterTripTimeCancelNotify(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> additionalDetails);

	public Future<SmsResponse> sendTicketUpdateSMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public Future<SmsResponse> sendFaliureBookSMS(AuthDTO authDTO, TicketDTO ticket, String mobileNumbers);

	public void sendOverallTripSummarySMS(AuthDTO authDTO, Map<String, Object> dataModel, String mobileNumber);

	public Future<SmsResponse> busTypeChangeNotification(AuthDTO authDTO, TicketDTO ticketDTO, String reason, String busTypeName, String supportNumber);

	public void sendLinkPaySMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendTripVanPickupSMS(AuthDTO authDTO, TicketDTO ticketDTO, TripVanInfoDTO tripVanInfoDTO);

	public void sendSMS(AuthDTO authDTO, String mobileNumber, Map<String, String> dataModel, NotificationTypeEM notificationType);

	public Future<SmsResponse> sendPendingOrderCancelSMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendFeedbackSMS(AuthDTO authDTO, Map<String, String> dataModel);

	public void sendCustomerDiscountCoupon(AuthDTO authDTO, DiscountCouponDTO discountCouponDTO, UserCustomerDTO userCustomerDTO);

	public void sendPaymentSMS(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO);

	public JSONObject getNotificationStatus(AuthDTO authDTO, NotificationDTO notification, Map<String, String> responseParam);

	public void sendWhatsappNumberVerificationSMS(AuthDTO authDTO, Map<String, String> dataModel);

	public Future<SmsResponse> sendCustomerTicketEventSMS(AuthDTO authDTO, Map<String, String> dataModel, NotificationTemplateConfigDTO templateConfig);

}
