package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;

import hirondelle.date4j.DateTime;

public interface NotificationPushService {
	public void pushNotification(AuthDTO authDTO, UserCustomerDTO userCustomerDTO, String title, String content, String image);

	public void pushPhoneBookTicketCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO);

	public void pushConfirmTicketCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO);

	public void pushTicketNotBoardedNotification(AuthDTO authDTO, TicketDTO ticketDTO);

	public void pushNotificationSubscriptions(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType, Map<String, String> content);

	public void pushUserNotification(AuthDTO authDTO, UserDTO userDTO, NotificationSubscriptionTypeEM subscriptionType);

	public void pushTicketAfterTripTime(AuthDTO authDTO, TicketDTO ticketDTO);

	public void pushTicketBlockConfirmNotification(AuthDTO authDTO, TicketDTO ticketDTO, NotificationSubscriptionTypeEM subscriptionType);

	public void pushVehicleNotAssignedNotification(AuthDTO authDTO, TripDTO tripDTO);

	public void pushDailySalesNotification(AuthDTO authDTO, Map<String, Object> salesMap);

	public void pushTripNotification(AuthDTO authDTO, Map<String, String> tripDetailMap);

	public void pushFareChangeNotification(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleTripStageFareDTO> fareList);

	public void pushServiceUpdateNotification(AuthDTO authDTO, TripDTO tripDTO);

	public void pushTicketFailureNotification(AuthDTO authDTO, TicketDTO ticketDTO);

	public void pushScheduleEditNotification(AuthDTO authDTO, String scheduleCode, List<Map<String, String>> scheduleMap);

	public void pushSeatVisibiltyNotification(AuthDTO authDTO, ScheduleSeatVisibilityDTO visibilityDTO);

	public void pushOTPNotification(AuthDTO authDTO, int otp);

	public void pushTripOccupancyStatusNotification(AuthDTO authDTO);

	public void pushCustomerFeedbackNotification(AuthDTO authDTO, UserFeedbackDTO userFeedBack);

	public void pushSeatEditNotification(AuthDTO authDTO, TicketDTO ticketDTO, String event);

	public void pushTicketRescheduleNotification(AuthDTO authDTO, TicketDTO transferDTO, Map<String, Boolean> additionalAttribute);

	public void pushDailyTravelStatusNotification(AuthDTO authDTO, List<String> travelStatusSummary);

	public void pushVehicleAssignedNotification(AuthDTO authDTO, TripDTO tripDTO, DateTime updatedAt);

}
