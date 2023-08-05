package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.NotificationSubscriptionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;

public interface NotificationService {

	public String tripCancelNotification(AuthDTO authDTO, String ticketCode, String reason, String supportNumber);

	public String tripDelayNotification(AuthDTO authDTO, String ticketCode, String reason, String delayTime, String supportNumber);

	public String tripStationPointChangeNotification(AuthDTO authDTO, String ticketCode, String reason, String boardingPlaceName, String boardingTime, String ContactNumber);

	public void firebusbuddyAfterboard(AuthDTO authDTO, String ticketCode, String vehicleNumber, String mobileNumber);

	public String tripEarlyNotification(AuthDTO authDTO, String ticketCode, String reason, String earlyTime, String supportNumber);

	public Map<String, String> getSMSStatus(AuthDTO authDTO, String refferenceCode);

	public String sendVehicleChangeSms(AuthDTO authDTO, String ticketCode);

	public String vanPickupNotify(AuthDTO authDTO, String ticketCode, String registrationNumber, String supportName, String supportNumber, String trackUrl);

	public void sendTripJourneyTrackingSMS(AuthDTO authDTO, TripDTO tripDTO);

	public void sendTripJourneyTrackingSMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendTicketAfterTripTimeNotify(AuthDTO authDTO, TicketDTO ticketDTO, String boardingMobileNumber);
	
	public void sendTicketAfterTripTimeCancelNotify(AuthDTO authDTO, TicketDTO ticketDTO, String boardingMobileNumber);

	public void sendTicketUpdateSMS(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendFaliureBookSMS(AuthDTO authDTO, TicketDTO ticket);

	public void sendScheduleUpdateEmail(AuthDTO authDTO, List<Map<String, String>> finalScheduleChanges, String referenceCode);

	public void ticketAfterTripTimeFCM(AuthDTO authDTO, TripDTO tripDTO, TicketDTO ticketDTO);

	public void sendOverallTripSummarySMS(AuthDTO authDTO, Map<String, Object> dataModel);

	public String busTypeChangeNotification(AuthDTO authDTO, String ticketCode, String reason, String BusTypeName, String ContactNumber);

	public void sendVanPickupSMS(AuthDTO authDTO, TripVanInfoDTO tripInfo, List<String> ticketCode);

	public void sendBusBuddyAlert(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendPendingOrderCancelSMS(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO);

	public void sendDenialTicketEmail(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendEzeebotFeedbackMail(Map<String, String> dataModel);

	public void sendTripSms(AuthDTO authDTO, Map<String, String> notificationDetails);

	public List<NotificationSubscriptionDTO> getAllSubscription(AuthDTO authDTO);

	public NotificationSubscriptionDTO updateSubscription(AuthDTO authDTO, NotificationSubscriptionDTO subscriptionsDTO);

	public NotificationSubscriptionDTO updateUserSubscription(AuthDTO authDTO, List<NotificationSubscriptionDTO> subscriptionsList);

	public List<NotificationSubscriptionDTO> getUserSubscription(AuthDTO authDTO);

	public List<NotificationSubscriptionDTO> getSubscriptionByType(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionsType);

	public void revokeAuthenticateSubscription(AuthDTO authDTO, AppStoreDetailsDTO appStoreDetails);

	public List<AppStoreDetailsDTO> getAuthenticateSubscriptionDetails(AuthDTO authDTO, UserDTO user, DeviceMediumEM deviceMedium);

	public List<Map<String, String>> notificationSubscriptionReport(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionTypeEM);

	public void checkAndUpdateNotificationStatus(AuthDTO authDTO, NotificationDTO notification, Map<String, String> responseParam);
	
	public void sendWhatsappVerificationNotification(AuthDTO authDTO, NamespaceProfileDTO profileDTO);
	
	public void sendCustomerTicketEvent(AuthDTO authDTO, TicketDTO ticketDTO, EventNotificationEM notificationEventEM);
	
	public void sendTicketBookingNotification(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendTicketCancelNotification(AuthDTO authDTO, TicketDTO repositoryTicketDTO);
}
