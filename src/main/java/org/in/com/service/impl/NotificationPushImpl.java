package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.aggregator.fcm.FCMService;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.TicketHelperCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleSeatVisibilityDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NotificationSubscriptionDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.SearchService;
import org.in.com.service.SectorService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StreamUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TemplateUtils;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class NotificationPushImpl extends CacheCentral implements NotificationPushService {
	@Autowired
	UserService userService;
	@Autowired
	GroupService groupService;
	@Autowired
	TripService tripService;
	@Autowired
	FCMService fcmService;
	@Autowired
	SearchService searchService;
	// @Autowired
	// UserCustomerService userCustomerService;
	@Autowired
	SectorService sectorService;
	@Autowired
	StationService stationService;

	public void pushTicketAfterTripTime(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.AFTER_DEPARTURE_BOOKING)) {
				TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, ticketDTO.getTripDTO());
				Map<String, Object> content = Maps.newHashMap();
				content.put("title", "ABD - " + ticketDTO.getServiceNo() + ", " + ticketDTO.getSeatNames());
				content.put("notificationType", NotificationSubscriptionTypeEM.AFTER_DEPARTURE_BOOKING.getCode());
				content.put("pnr", ticketDTO.getCode());
				content.put("seats", ticketDTO.getSeatNames());
				content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
				content.put("serviceNo", ticketDTO.getServiceNo());
				content.put("mobile", ticketDTO.getPassengerMobile());
				content.put("bookedBy", ticketDTO.getTicketUser().getName());
				content.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
				content.put("boardingTime", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
				content.put("driverMobile", tripInfo != null ? tripInfo.getDriverMobile() : Text.EMPTY);

				List<StationDTO> stations = new ArrayList<>();
				stations.add(ticketDTO.getFromStation());
				stations.add(ticketDTO.getToStation());

				BusVehicleDTO busVehicleDTO = null;
				if (tripInfo != null && tripInfo.getBusVehicle() != null) {
					busVehicleDTO = tripInfo.getBusVehicle();
				}

				pushNotification(authDTO, NotificationSubscriptionTypeEM.AFTER_DEPARTURE_BOOKING, content, stations, ticketDTO.getTripDTO().getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void pushUserNotification(AuthDTO authDTO, UserDTO userDTO, NotificationSubscriptionTypeEM subscriptionType) {
		try {
			if ((userDTO.getUserRole() == null || StringUtil.isNull(userDTO.getName())) && StringUtil.isNotNull(userDTO.getCode())) {
				getUser(authDTO, userDTO);
			}
			if (userDTO.getUserRole() != null && userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
				Map<String, Object> content = null;
				if (NotificationSubscriptionTypeEM.ALL_USER_LOGIN.getId() == subscriptionType.getId()) {
					GroupDTO group = getGroupDTOById(authDTO, userDTO.getGroup());
					if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.ALL_USER_LOGIN) && group != null && group.getName().equals("Office")) {
						content = Maps.newHashMap();
						content.put("notificationType", NotificationSubscriptionTypeEM.ALL_USER_LOGIN.getCode());
						content.put("title", "Office User login");
						content.put("userFirstName", userDTO.getName());
						content.put("ipAddress", Text.EMPTY);

						pushNotification(authDTO, NotificationSubscriptionTypeEM.ALL_USER_LOGIN, content, null, null, null);
					}
					else if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.ALL_USER_LOGIN)) {
						content = Maps.newHashMap();
						content.put("notificationType", NotificationSubscriptionTypeEM.ALL_USER_LOGIN.getCode());
						content.put("title", "All User login");
						content.put("userFirstName", userDTO.getName());

						pushNotification(authDTO, NotificationSubscriptionTypeEM.ALL_USER_LOGIN, content, null, null, null);
					}
					// My User Login
					if (isUserSubscriptionEnabled(authDTO, userDTO, NotificationSubscriptionTypeEM.USER_LOGIN)) {
						content = Maps.newHashMap();
						content.put("notificationType", NotificationSubscriptionTypeEM.USER_LOGIN.getCode());
						content.put("title", "My User login");
						content.put("userFirstName", userDTO.getName());

						pushNotificationV2(authDTO, NotificationSubscriptionTypeEM.USER_LOGIN, userDTO, content, null, null, null);
					}
					boolean isDuplicatelogin = authDTO.getAdditionalAttribute() != null && authDTO.getAdditionalAttribute().containsKey(Constants.DUPLICATE_SESSION_FLAG) && authDTO.getAdditionalAttribute().get(Constants.DUPLICATE_SESSION_FLAG).equals(Numeric.ONE) ? true : false;
					// Duplicate session
					if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.DUPLICATE_USER_LOGIN) && isDuplicatelogin) {
						content = Maps.newHashMap();
						content.put("notificationType", NotificationSubscriptionTypeEM.DUPLICATE_USER_LOGIN.getCode());
						content.put("title", "Duplicate User Login");
						content.put("userFirstName", userDTO.getName());

						pushNotification(authDTO, NotificationSubscriptionTypeEM.DUPLICATE_USER_LOGIN, content, null, null, null);
					}
					if (isUserSubscriptionEnabled(authDTO, userDTO, NotificationSubscriptionTypeEM.USER_LOGIN) && isDuplicatelogin) {
						content = Maps.newHashMap();
						content.put("notificationType", NotificationSubscriptionTypeEM.DUPLICATE_USER_LOGIN.getCode());
						content.put("title", "Duplicate User Login");
						content.put("userFirstName", userDTO.getName());

						pushNotificationV2(authDTO, NotificationSubscriptionTypeEM.DUPLICATE_USER_LOGIN, userDTO, content, null, null, null);
					}
				}
				else if (NotificationSubscriptionTypeEM.NEW_USER_UPDATE.getId() == subscriptionType.getId() && userDTO.getActiveFlag() == Numeric.ONE_INT && authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.NEW_USER_UPDATE)) {
					content = Maps.newHashMap();
					content.put("notificationType", NotificationSubscriptionTypeEM.NEW_USER_UPDATE.getCode());
					content.put("title", "New user added");
					content.put("userFirstName", userDTO.getName());
					content.put("groupName", groupService.getGroup(authDTO, userDTO.getGroup()).getName());
					content.put("updatedBy", authDTO.getUser().getName());

					pushNotification(authDTO, NotificationSubscriptionTypeEM.NEW_USER_UPDATE, content, null, null, null);
				}
				else if (NotificationSubscriptionTypeEM.USER_DELETE.getId() == subscriptionType.getId() && userDTO.getActiveFlag() == Numeric.TWO_INT && authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.USER_DELETE)) {
					content = Maps.newHashMap();
					content.put("notificationType", NotificationSubscriptionTypeEM.USER_DELETE.getCode());
					content.put("title", "User Deleted");
					content.put("userFirstName", userDTO.getName());
					content.put("groupName", groupService.getGroup(authDTO, userDTO.getGroup()).getName());
					content.put("updatedBy", authDTO.getUser().getName());

					pushNotification(authDTO, NotificationSubscriptionTypeEM.USER_DELETE, content, null, null, null);
				}
				else if (NotificationSubscriptionTypeEM.USER_RESET_PASSWORD.getId() == subscriptionType.getId() && authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.USER_RESET_PASSWORD)) {
					content = Maps.newHashMap();
					content.put("notificationType", NotificationSubscriptionTypeEM.USER_RESET_PASSWORD.getCode());
					content.put("title", "Password resetted");
					content.put("userFirstName", userDTO.getName());
					content.put("updatedBy", authDTO.getUser().getName());

					pushNotification(authDTO, NotificationSubscriptionTypeEM.USER_RESET_PASSWORD, content, null, null, null);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getUser(AuthDTO authDTO, UserDTO userDTO) {
		UserDTO user = new UserDTO();
		user.setCode(userDTO.getCode());
		user = getUserDTO(authDTO, user);
		userDTO.setName(user.getName());
		userDTO.setGroup(user.getGroup());
		userDTO.setUserRole(user.getUserRole());
	}

	@Async
	public void pushOTPNotification(AuthDTO authDTO, int otp) {
		UserDTO userDTO = authDTO.getUser();
		if (userDTO.getUserRole() != null && userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && isUserSubscriptionEnabled(authDTO, userDTO, NotificationSubscriptionTypeEM.OTP_LOGIN)) {
			Map<String, Object> content = Maps.newHashMap();
			content.put("notificationType", NotificationSubscriptionTypeEM.OTP_LOGIN.getCode());
			content.put("title", "OTP for Login - " + String.valueOf(otp));
			content.put("otpNumber", String.valueOf(otp));

			pushNotificationV2(authDTO, NotificationSubscriptionTypeEM.OTP_LOGIN, userDTO, content, null, null, null);
		}
	}

	private void pushAdvanceTicketBookingNotification(AuthDTO authDTO, TicketDTO ticketDTO, List<StationDTO> stations, ScheduleDTO scheduleDTO, BusVehicleDTO vehicleDTO, UserDTO userDTO) {
		try {
			Map<String, Object> content = Maps.newHashMap();
			content.put("title", "Advance Ticket Booked - " + DateUtil.parseDateFormat(DateUtil.convertDate(ticketDTO.getTripDate()), "yyyy-MM-dd", "dd/MMM/yyyy"));
			content.put("notificationType", NotificationSubscriptionTypeEM.ADVANCE_TRAVEL_DATE_BOOKING.getCode());
			content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
			content.put("serviceNo", ticketDTO.getServiceNo());
			content.put("travelDate", ticketDTO.getTripDate().format("DD-MM-YYYY"));
			content.put("fare", ticketDTO.getTicketFareWithAddons().toString());
			content.put("updatedBy", authDTO.getUser().getName());

			pushNotification(authDTO, NotificationSubscriptionTypeEM.ADVANCE_TRAVEL_DATE_BOOKING, content, stations, scheduleDTO, vehicleDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private void pushServiceFirstTicketNotification(AuthDTO authDTO,
	// TicketDTO ticketDTO) {
	// try {
	// Map<String, String> content = Maps.newHashMap();
	// content.put("notificationType",
	// NotificationSubscriptionTypeEM.SERVICE_FIRST_TICKET.getCode());
	// content.put("title", "Service First Ticket Booked");
	// content.put("pnr", ticketDTO.getCode());
	// content.put("seats", ticketDTO.getSeatNames());
	// content.put("serviceNo", ticketDTO.getServiceNo());
	// content.put("bookedUser", ticketDTO.getTicketUser().getName());
	// content.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
	//
	// pushNotification(authDTO,
	// NotificationSubscriptionTypeEM.SERVICE_FIRST_TICKET, content);
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	@Async
	public void pushTicketBlockConfirmNotification(AuthDTO authDTO, TicketDTO ticketDTO, NotificationSubscriptionTypeEM subscriptionType) {
		try {
			Map<String, Object> content = Maps.newHashMap();
			if (NotificationSubscriptionTypeEM.TICKET_BLOCK.getId() == subscriptionType.getId() && authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TICKET_BLOCK)) {
				content.put("title", "New Ticket Blocked for " + ticketDTO.getTripDate().format("DD-MM-YYYY") + " by " + ticketDTO.getTicketUser().getName());
				content.put("notificationType", NotificationSubscriptionTypeEM.TICKET_BLOCK.getCode());
			}
			else if (NotificationSubscriptionTypeEM.TICKET_CONFIRM.getId() == subscriptionType.getId() && authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TICKET_CONFIRM)) {
				content.put("title", "New Ticket Booked for " + ticketDTO.getTripDate().format("DD-MM-YYYY") + " by " + ticketDTO.getTicketUser().getName());
				content.put("notificationType", NotificationSubscriptionTypeEM.TICKET_CONFIRM.getCode());
			}

			TripDTO existTripDTO = new TripDTO();
			existTripDTO.setCode(ticketDTO.getTripDTO().getCode());
			existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

			List<StationDTO> stations = new ArrayList<>();
			stations.add(ticketDTO.getFromStation());
			stations.add(ticketDTO.getToStation());

			TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, ticketDTO.getTripDTO());
			BusVehicleDTO busVehicleDTO = null;
			if (tripInfo != null && tripInfo.getBusVehicle() != null) {
				busVehicleDTO = tripInfo.getBusVehicle();
			}

			if (!content.isEmpty()) {
				getBookingDetailsMap(authDTO, ticketDTO, content);

				pushNotification(authDTO, subscriptionType, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}

			UserDTO userDTO = ticketDTO.getTicketUser();
			if (userDTO.getUserRole() != null && userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && NotificationSubscriptionTypeEM.TICKET_CONFIRM.getId() == subscriptionType.getId() && isUserSubscriptionEnabled(authDTO, userDTO, NotificationSubscriptionTypeEM.MY_TICKET_BOOKING)) {
				if (content.isEmpty()) {
					getBookingDetailsMap(authDTO, ticketDTO, content);
				}
				content.put("title", "New Ticket Booked for " + ticketDTO.getTripDate().format("DD-MM-YYYY") + " by " + ticketDTO.getTicketUser().getName());
				content.put("notificationType", NotificationSubscriptionTypeEM.MY_TICKET_BOOKING.getCode() + Text.UNDER_SCORE + NotificationSubscriptionTypeEM.TICKET_CONFIRM.getCode());

				pushNotificationV2(authDTO, NotificationSubscriptionTypeEM.MY_TICKET_BOOKING, userDTO, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}

			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.ADVANCE_TRAVEL_DATE_BOOKING) && DateUtil.getDayDifferent(DateUtil.minusDaysToDate(DateUtil.NOW(), 1), ticketDTO.getTripDate()) >= 7) {
				pushAdvanceTicketBookingNotification(authDTO, ticketDTO, stations, existTripDTO.getSchedule(), busVehicleDTO, ticketDTO.getTicketUser());
			}

			// if
			// (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.SERVICE_FIRST_TICKET))
			// {
			// boolean isFirstTicket =
			// ticketService.isServiceFirstTicket(authDTO, ticketDTO);
			// if (isFirstTicket) {
			// pushServiceFirstTicketNotification(authDTO, ticketDTO);
			// }
			// }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getBookingDetailsMap(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, Object> content) {
		content.put("pnr", ticketDTO.getCode());
		content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
		content.put("serviceNo", ticketDTO.getServiceNo());
		content.put("travelDate", ticketDTO.getTripDate().format("DD-MM-YYYY"));
		content.put("seats", ticketDTO.getSeatNames());
		content.put("fare", ticketDTO.getTicketFareWithAddons().toString());
		content.put("mobile", ticketDTO.getPassengerMobile());
		content.put("passenger", ticketDTO.getPassengerName());
		content.put("updatedBy", authDTO.getUser().getName());
	}

	@Async
	public void pushVehicleNotAssignedNotification(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.VEHICLE_NOT_ASSIGNED)) {

				Map<String, Object> content = Maps.newHashMap();
				content.put("notificationType", NotificationSubscriptionTypeEM.VEHICLE_NOT_ASSIGNED.getCode());
				content.put("title", "Vehicle Not Assinged - " + tripDTO.getSchedule().getServiceNumber());
				content.put("serviceNo", tripDTO.getSchedule().getServiceNumber());

				pushNotification(authDTO, NotificationSubscriptionTypeEM.VEHICLE_NOT_ASSIGNED, content, null, tripDTO.getSchedule(), null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pushDailySalesNotification(AuthDTO authDTO, Map<String, Object> salesMap) {
		try {
			if (!salesMap.isEmpty()) {
				Map<String, Object> content = Maps.newHashMap();
				content.put("notificationType", NotificationSubscriptionTypeEM.DAILY_SALES_SUMMARY.getCode());
				content.put("title", "Sales Summary - " + DateUtil.NOW().format("DD-MM-YYYY"));
				content.put("header", String.valueOf(salesMap.get("header")).split(Text.COMMA)[0].replace("[", ""));
				content.put("booking", String.valueOf(salesMap.get("header")).split(Text.COMMA)[1].replace("]", ""));
				content.put("content", salesMap.get("content"));
				content.put("summary", salesMap.get("summary"));
				if (StringUtil.getIntegerValue(String.valueOf(salesMap.get("TOTAL_CA_CNT"))) != 0) {
					content.put("cancel", String.valueOf(salesMap.get("TOTAL_CA_CNT")) + " Seats /" + String.valueOf(salesMap.get("cancel")));
				}
				content.put("tripCount", String.valueOf(salesMap.get("TOTAL_TRIP_CNT")));
				content.put("bookingCount", String.valueOf(salesMap.get("TOTAL_BO_CNT")));
				content.put("time", String.valueOf(salesMap.get("time")));
				content.put("domainUrl", String.valueOf(salesMap.get("domainUrl")));

				pushNotification(authDTO, NotificationSubscriptionTypeEM.DAILY_SALES_SUMMARY, content, null, null, null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void pushTripNotification(AuthDTO authDTO, Map<String, String> notificationDetails) {
		try {
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.MANUAL_TRIP_SMS)) {
				String notificationType = notificationDetails.get("SMS_TYPE");

				Map<String, Object> content = Maps.newHashMap();
				content.put("notificationType", NotificationSubscriptionTypeEM.MANUAL_TRIP_SMS.getCode());
				if (notificationType.equals("TRIP_CANCEL")) {
					content.put("title", "Trip Notification sent - Trip Cancel Notification");
					content.put("type", "Trip Cancel");
				}
				else if (notificationType.equals("TRIP_DELAY")) {
					content.put("title", "Trip Notification sent - Trip Delay Notification");
					content.put("time", notificationDetails.get("DELAY_TIME"));
					content.put("type", "Trip Delay");
				}
				else if (notificationType.equals("TRIP_EARLY")) {
					content.put("title", "Trip Notification sent - Trip Early Notification");
					content.put("time", notificationDetails.get("DELAY_TIME"));
					content.put("type", "Trip Early");
				}
				else if (notificationType.equals("STATION_POINT_CHANGE")) {
					content.put("title", "Trip Notification sent - Station Point Change Notification");
					content.put("stationPoint", notificationDetails.get("STATION_POINT_NAME"));
					content.put("time", notificationDetails.get("STATION_POINT_TIME"));
					content.put("type", "Station Point Change");
				}
				else if (notificationType.equals("RESEND_TRACKING")) {
					content.put("title", "Trip Notification sent - Resend Tracking Notification");
					content.put("type", "Resend Tracking");
				}
				else if (notificationType.equals("COVID_EPASS")) {
					content.put("title", "Trip Notification sent - Covid Epass Notification");
					content.put("type", "Covid Epass");
				}
				else if (notificationType.equals("APOLOGY_NOTIFICATION")) {
					content.put("title", "Trip Notification sent - Apology Notification");
					content.put("type", notificationDetails.get("REASON"));
				}

				String ticketCode = notificationDetails.get("TICKETS").split(Text.COMMA)[0];
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketCode);
				TicketDAO dao = new TicketDAO();
				dao.getTicketStatus(authDTO, ticketDTO);
				TripDTO tripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, ticketDTO.getTripDTO());

				content.put("pnrCount", notificationDetails.get("TICKETS").split(Text.COMMA).length);
				content.put("travelDate", tripDTO.getTripDate().format("DD-MM-YYYY"));
				content.put("serviceNo", StringUtil.isNotNull(tripDTO.getSchedule().getServiceNumber()) ? tripDTO.getSchedule().getServiceNumber() : Text.EMPTY);
				content.put("updatedBy", authDTO.getUser().getName());

				BusVehicleDTO busVehicleDTO = null;
				TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, tripDTO);
				if (tripInfo != null && tripInfo.getBusVehicle() != null) {
					busVehicleDTO = tripInfo.getBusVehicle();
				}

				pushNotification(authDTO, NotificationSubscriptionTypeEM.MANUAL_TRIP_SMS, content, null, tripDTO.getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Async
	public void pushNotification(AuthDTO authDTO, UserCustomerDTO userCustomerDTO, String title, String content, String image) {
		UserCustomerServiceImpl userServiceImpl = new UserCustomerServiceImpl();
		userServiceImpl.getUserCustomer(authDTO, userCustomerDTO);
		List<AppStoreDetailsDTO> appStoreDetailsList = userServiceImpl.getAppStoreDetails(authDTO, userCustomerDTO);
		for (AppStoreDetailsDTO appStoreDetailsDTO : appStoreDetailsList) {
			JSONObject data = new JSONObject();
			data.put("title", title);
			data.put("body", content);
			data.put("datetime", DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"));

			fcmService.pushNotification(authDTO, authDTO.getNamespaceCode(), appStoreDetailsDTO.getGcmToken(), title, content, image, data);
		}
	}

	@Async
	public void pushFareChangeNotification(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleTripStageFareDTO> fareList) {
		try {
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.FARE_CHANGE)) {
				Map<String, Object> content = Maps.newHashMap();
				content.put("title", "Fare Changed - " + tripDTO.getSchedule().getServiceNumber());
				content.put("notificationType", NotificationSubscriptionTypeEM.FARE_CHANGE.getCode());
				content.put("serviceNo", tripDTO.getSchedule().getServiceNumber());
				content.put("tripDate", tripDTO.getTripDate().format("DD-MM-YYYY"));
				content.put("updatedBy", authDTO.getUser().getName());

				Map<String, String> fareMap = Maps.newHashMap();
				Map<String, String> routeMap = Maps.newHashMap();
				Map<Integer, StationDTO> stations = Maps.newHashMap();
				Iterable<List<ScheduleTripStageFareDTO>> batchFarelist = Iterables.partition(fareList, 10);
				for (List<ScheduleTripStageFareDTO> list : batchFarelist) {
					for (ScheduleTripStageFareDTO tripStagefare : list) {
						RouteDTO routeDTO = tripStagefare.getRoute();
						if (routeDTO.getFromStation().getId() == 0 || routeDTO.getToStation().getId() == 0) {
							continue;
						}
						String minMaxFare = null;
						for (StageFareDTO busSeatTypeFare : routeDTO.getStageFare()) {
							if (StringUtil.isNotNull(minMaxFare) && minMaxFare.contains("-")) {
								String[] fares = minMaxFare.split("\\-");

								BigDecimal minFare = StringUtil.getBigDecimalValue(fares[0].trim());
								BigDecimal maxFare = StringUtil.getBigDecimalValue(fares[1].trim());

								if (minFare.compareTo(busSeatTypeFare.getFare()) > 0) {
									minFare = busSeatTypeFare.getFare();
								}
								else if (maxFare.compareTo(busSeatTypeFare.getFare()) < 0) {
									maxFare = busSeatTypeFare.getFare();
								}
								minMaxFare = String.valueOf(minFare) + " - " + String.valueOf(maxFare);
							}
							else {
								if (StringUtil.isNull(minMaxFare)) {
									minMaxFare = String.valueOf(busSeatTypeFare.getFare());
								}
								else if (StringUtil.getBigDecimalValue(minMaxFare).compareTo(busSeatTypeFare.getFare()) > 0) {
									minMaxFare = String.valueOf(busSeatTypeFare.getFare()) + " - " + minMaxFare;
								}
								else if (StringUtil.getBigDecimalValue(minMaxFare).compareTo(busSeatTypeFare.getFare()) < 0) {
									minMaxFare = minMaxFare + " - " + String.valueOf(busSeatTypeFare.getFare());
								}
							}
						}
						String key = minMaxFare;
						if (fareMap.get(key) != null || routeMap.get(String.valueOf(routeDTO.getFromStation().getId())) != null) {
							String routeFareDetails = routeMap.get(String.valueOf(routeDTO.getFromStation().getId()));
							if (StringUtil.isNull(routeDTO.getToStation().getName())) {
								getStationDTObyId(routeDTO.getToStation());
							}
							if (StringUtil.isNotNull(routeFareDetails) && !isRouteExist(routeDTO.getToStation().getName(), routeFareDetails)) {

								StringBuilder existRouteFareDetails = new StringBuilder();
								existRouteFareDetails.append(routeFareDetails);
								existRouteFareDetails.append(Text.COMMA + Text.SINGLE_SPACE);
								existRouteFareDetails.append(routeDTO.getToStation().getName());
								fareMap.put(key, existRouteFareDetails.toString());
								routeMap.put(String.valueOf(routeDTO.getFromStation().getId()), existRouteFareDetails.toString());
							}
							routeFareDetails = routeMap.get(String.valueOf(routeDTO.getFromStation().getId()));
							if (StringUtil.isNotNull(routeFareDetails) && !isFareExist(minMaxFare, routeFareDetails)) {
								String existFareDetails = composeFareDetails(minMaxFare, routeFareDetails);
								fareMap.put(key, existFareDetails);
								routeMap.put(String.valueOf(routeDTO.getFromStation().getId()), existFareDetails);
							}
						}
						else {
							StringBuilder fareDetails = new StringBuilder();
							fareDetails.append(minMaxFare + "</br>" + routeDTO.getFromStation().getName() + " -> " + routeDTO.getToStation().getName());
							fareMap.put(key, fareDetails.toString());
							routeMap.put(String.valueOf(routeDTO.getFromStation().getId()), fareDetails.toString());

						}
						stations.put(routeDTO.getFromStation().getId(), routeDTO.getFromStation());
						stations.put(routeDTO.getToStation().getId(), routeDTO.getToStation());
					}
				}

				List<String> stageFareList = new ArrayList<>(routeMap.values());
				content.put("fareDetails", stageFareList);

				BusVehicleDTO busVehicleDTO = null;
				TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, tripDTO);
				if (tripInfo != null && tripInfo.getBusVehicle() != null) {
					busVehicleDTO = tripInfo.getBusVehicle();
				}

				pushNotification(authDTO, NotificationSubscriptionTypeEM.FARE_CHANGE, content, new ArrayList<>(stations.values()), tripDTO.getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isRouteExist(String station, String existFareDetails) {
		boolean isFound = false;
		String[] details = existFareDetails.split("\\->");
		if (details.length == 2) {
			for (String existStation : details[1].split(Text.COMMA)) {
				if (station.trim().equals(existStation.trim())) {
					isFound = true;
					break;
				}
			}
		}
		return isFound;
	}

	private boolean isFareExist(String fare, String existFareDetails) {
		boolean isFound = false;
		String[] details = existFareDetails.split("\\</br>");
		if (details.length == 2) {
			for (String existFare : details[0].split(Text.COMMA)) {
				if (fare.trim().equals(existFare.trim())) {
					isFound = true;
					break;
				}
			}
		}
		return isFound;
	}

	private String composeFareDetails(String minMaxFare, String existFareDetails) {
		String[] routeFaredetails = existFareDetails.split("\\</br>");
		StringBuilder details = new StringBuilder();
		if (routeFaredetails.length == 2) {
			String routeDetails = routeFaredetails[1];
			for (String existFare : routeFaredetails[0].split(Text.COMMA)) {
				details.append(existFare);
				details.append(Text.COMMA + Text.SINGLE_SPACE);
			}
			details.append(minMaxFare);
			details.append("</br>" + routeDetails);
		}
		return details.toString();

	}

	@Override
	public void pushTicketFailureNotification(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.FAILURE_DROPOUT_TICKET)) {
				Map<String, Object> content = Maps.newHashMap();
				content.put("notificationType", NotificationSubscriptionTypeEM.FAILURE_DROPOUT_TICKET.getCode());
				content.put("title", "Failure / Dropout Ticket - " + ticketDTO.getTripDate().format("DD-MM-YYYY") + ", " + ticketDTO.getDeviceMedium().getCode());
				content.put("route", ticketDTO.getFromStation().getName() + " - " + ticketDTO.getToStation().getName());
				content.put("travelDate", ticketDTO.getTripDate().format("DD-MM-YYYY"));
				content.put("seatCount", String.valueOf(ticketDTO.getTicketDetails().size()));
				content.put("seats", ticketDTO.getSeatNames());
				content.put("serviceNo", StringUtil.isNotNull(ticketDTO.getServiceNo()) ? ticketDTO.getServiceNo() : Text.HYPHEN);
				content.put("busType", BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()));
				content.put("boardingPoint", ticketDTO.getBoardingPoint().getName());
				content.put("boardingTime", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));

				StringBuilder passengerDetails = new StringBuilder();
				for (TicketDetailsDTO ticketdetails : ticketDTO.getTicketDetails()) {
					if (passengerDetails.length() > 0) {
						passengerDetails.append(Text.COMMA + Text.SINGLE_SPACE);
					}
					passengerDetails.append(ticketdetails.getPassengerName() + " - ");
					passengerDetails.append(ticketdetails.getPassengerAge() + " - ");
					passengerDetails.append(ticketdetails.getSeatGendar().getCode());
				}
				content.put("customerDetails", passengerDetails.toString());
				content.put("customerMobile", ticketDTO.getPassengerMobile());
				content.put("updatedBy", ticketDTO.getTicketUser().getName());

				TripDTO existTripDTO = new TripDTO();
				existTripDTO.setCode(ticketDTO.getTripDTO().getCode());
				existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

				List<StationDTO> stations = new ArrayList<>();
				stations.add(ticketDTO.getFromStation());
				stations.add(ticketDTO.getToStation());

				BusVehicleDTO busVehicleDTO = null;
				TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
				if (tripInfo != null && tripInfo.getBusVehicle() != null) {
					busVehicleDTO = tripInfo.getBusVehicle();
				}

				pushNotification(authDTO, NotificationSubscriptionTypeEM.FAILURE_DROPOUT_TICKET, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Async
	public void pushNotificationSubscriptions(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType, Map<String, String> content) {
		Map<String, NotificationSubscriptionDTO> subscriptionsMap = new HashMap<String, NotificationSubscriptionDTO>();
		String title = subscriptionType.getName();
		String body = StringUtil.isNull(content.get("body"), subscriptionType.getName());
		List<NotificationSubscriptionDTO> subscriptionList = getSubscriptionByType(authDTO, subscriptionType);

		// Grouping with Notification medium
		for (NotificationSubscriptionDTO subscriptionDTO : subscriptionList) {
			List<UserDTO> userList = getUsers(authDTO, subscriptionDTO);
			for (NotificationMediumEM notificationMedium : subscriptionDTO.getNotificationMediumList()) {
				NotificationSubscriptionDTO notificationSubscriptionDTO = new NotificationSubscriptionDTO();
				if (subscriptionsMap.get(notificationMedium.getCode()) != null) {
					notificationSubscriptionDTO = subscriptionsMap.get(notificationMedium.getCode());
					userList.addAll(notificationSubscriptionDTO.getUserList());
				}
				notificationSubscriptionDTO.setUserList(userList);
				subscriptionsMap.put(notificationMedium.getCode(), notificationSubscriptionDTO);
			}
		}

		JSONObject jsonData = JSONObject.fromObject(content);
		for (Map.Entry<String, NotificationSubscriptionDTO> sub : subscriptionsMap.entrySet()) {
			String notificationMedium = sub.getKey();
			if (!notificationMedium.equals(NotificationMediumEM.EZEEBOT_APP.getCode())) {
				continue;
			}
			NotificationSubscriptionDTO subscriptionDTO = sub.getValue();
			for (UserDTO subscribeUser : subscriptionDTO.getUserList()) {
				List<AppStoreDetailsDTO> userAppList = getAppstoreByUserId(authDTO, subscribeUser);
				if (userAppList.isEmpty()) {
					continue;
				}
				List<String> gcmTokenList = userAppList.stream().map(user -> user.getGcmToken()).collect(Collectors.toList());
				JSONArray alertArray = new JSONArray();
				for (String userKey : gcmTokenList) {
					JSONObject alertdata = new JSONObject();
					alertdata.put("priority", "COLD");
					alertdata.put("to", userKey);
					alertdata.put("collapseKey", StringUtil.removeSymbol(title));
					alertdata.put("category", subscriptionType.getCategory());
					alertdata.put("product", "ezeebus");
					alertdata.put("time", DateUtil.NowV2().toString(DateUtil.JODA_DATE_TIME_FORMATE));
					alertdata.put("namespace", authDTO.getNamespaceCode());
					alertdata.put("userCode", userKey);
					alertdata.put("notificationTitle", title);
					alertdata.put("notificationBody", content);
					alertdata.put("mid", TokenGenerator.generateCode("BITS", 20));
					alertArray.add(alertdata);
				}
				fcmService.alertzService(authDTO, "ezeebotfcm", alertArray);
			}
		}
	}

	@Async
	public void pushServiceUpdateNotification(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.SERVICE_UPDATE)) {
				TripDTO existTripDTO = new TripDTO();
				existTripDTO.setCode(tripDTO.getCode());
				existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);
				List<TicketDetailsDTO> list = tripService.getBookedBlockedSeats(authDTO, existTripDTO);

				Map<String, Object> content = Maps.newHashMap();
				content.put("notificationType", NotificationSubscriptionTypeEM.SERVICE_UPDATE.getCode());
				if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CLOSED.getId()) {
					content.put("title", "Service Closed " + existTripDTO.getTripDate().format("DD-MM-YYYY") + ", " + existTripDTO.getSchedule().getServiceNumber());
					content.put("status", "CLOSE");
				}
				else if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId()) {
					content.put("title", "Service Cancelled " + existTripDTO.getTripDate().format("DD-MM-YYYY") + ", " + existTripDTO.getSchedule().getServiceNumber());
					content.put("status", "CANCEL");
					content.put("seatCount", list.size());
				}
				else if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_OPEN.getId()) {
					content.put("title", "Booking Opened " + existTripDTO.getTripDate().format("DD-MM-YYYY") + ", " + existTripDTO.getSchedule().getServiceNumber());
					content.put("status", "OPEN");
				}
				content.put("serviceNo", existTripDTO.getSchedule().getServiceNumber());
				content.put("tripDate", existTripDTO.getTripDate().format("DD-MM-YYYY"));
				content.put("updatedBy", authDTO.getUser().getName());

				BusVehicleDTO busVehicleDTO = null;
				TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
				if (tripInfo != null && tripInfo.getBusVehicle() != null) {
					busVehicleDTO = tripInfo.getBusVehicle();
				}

				pushNotification(authDTO, NotificationSubscriptionTypeEM.SERVICE_UPDATE, content, null, existTripDTO.getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void pushPhoneBookTicketCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			TripDTO existTripDTO = new TripDTO();
			existTripDTO.setCode(ticketDTO.getTripDTO().getCode());
			existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

			BusVehicleDTO busVehicleDTO = null;
			TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
			if (tripInfo != null && tripInfo.getBusVehicle() != null) {
				busVehicleDTO = tripInfo.getBusVehicle();
			}

			List<StationDTO> stations = new ArrayList<>();
			stations.add(ticketDTO.getFromStation());
			stations.add(ticketDTO.getToStation());

			Map<String, Object> content = null;
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.PHONE_TCIKET_CANCEL)) {
				content = getPhoneBookCancelMap(authDTO, ticketDTO, content);

				pushNotification(authDTO, NotificationSubscriptionTypeEM.PHONE_TCIKET_CANCEL, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}

			UserDTO userDTO = authDTO.getUser();
			if (userDTO.getUserRole() != null && userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && isUserSubscriptionEnabled(authDTO, userDTO, NotificationSubscriptionTypeEM.MY_TICKET_BOOKING)) {
				if (content == null) {
					content = getPhoneBookCancelMap(authDTO, ticketDTO, content);
				}
				content.put("title", "Phone Ticket Cancelled - " + DateUtil.parseDateFormat(DateUtil.convertDate(ticketDTO.getTripDate()), "yyyy-MM-dd", "dd/MMM/yyyy") + ", " + ticketDTO.getTicketDetails().size() + " Seats");
				content.put("notificationType", NotificationSubscriptionTypeEM.MY_TICKET_BOOKING.getCode() + Text.UNDER_SCORE + NotificationSubscriptionTypeEM.PHONE_TCIKET_CANCEL.getCode());

				pushNotificationV2(authDTO, NotificationSubscriptionTypeEM.MY_TICKET_BOOKING, userDTO, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> getPhoneBookCancelMap(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, Object> content) {
		content = Maps.newHashMap();
		try {
			content.put("title", "Phone Ticket Cancelled - " + DateUtil.parseDateFormat(DateUtil.convertDate(ticketDTO.getTripDate()), "yyyy-MM-dd", "dd/MMM/yyyy") + ", " + ticketDTO.getTicketDetails().size() + " Seats");
			content.put("notificationType", NotificationSubscriptionTypeEM.PHONE_TCIKET_CANCEL.getCode());
			content.put("pnr", ticketDTO.getCode());
			content.put("serviceNo", ticketDTO.getServiceNo());
			content.put("travelDate", ticketDTO.getTripDate().format("DD-MM-YYYY"));
			content.put("seats", ticketDTO.getTicketDetails().size());
			content.put("fare", ticketDTO.getTicketFareWithAddons().toString());
			content.put("bookedBy", ticketDTO.getTicketUser().getName());
			content.put("bookedAtDate", ticketDTO.getTicketAt().format("DD-MM-YYYY"));
			content.put("bookedAtTime", ticketDTO.getTicketAt().format("hh12:mm a", Locale.forLanguageTag("en_IN")));
			content.put("updatedBy", authDTO.getUser().getName());
			content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	@Async
	public void pushConfirmTicketCancelNotification(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			TripDTO existTripDTO = new TripDTO();
			existTripDTO.setCode(ticketDTO.getTripDTO().getCode());
			existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

			BusVehicleDTO busVehicleDTO = null;
			TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
			if (tripInfo != null && tripInfo.getBusVehicle() != null) {
				busVehicleDTO = tripInfo.getBusVehicle();
			}

			List<StationDTO> stations = new ArrayList<>();
			stations.add(ticketDTO.getFromStation());
			stations.add(ticketDTO.getToStation());

			Map<String, Object> content = null;
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TICKET_CANCEL)) {
				content = getCancelTicketMap(authDTO, ticketDTO, content);

				pushNotification(authDTO, NotificationSubscriptionTypeEM.TICKET_CANCEL, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}

			UserDTO userDTO = ticketDTO.getTicketUser();
			if (userDTO.getUserRole() != null && userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && isUserSubscriptionEnabled(authDTO, userDTO, NotificationSubscriptionTypeEM.MY_TICKET_BOOKING)) {
				if (content == null) {
					content = getCancelTicketMap(authDTO, ticketDTO, content);
				}
				content.put("notificationType", NotificationSubscriptionTypeEM.MY_TICKET_BOOKING.getCode() + Text.UNDER_SCORE + NotificationSubscriptionTypeEM.TICKET_CANCEL.getCode());

				pushNotificationV2(authDTO, NotificationSubscriptionTypeEM.MY_TICKET_BOOKING, userDTO, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, Object> getCancelTicketMap(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, Object> content) {
		try {
			content = Maps.newHashMap();
			content.put("notificationType", NotificationSubscriptionTypeEM.TICKET_CANCEL.getCode());
			content.put("title", "Ticket Cancelled - " + DateUtil.parseDateFormat(DateUtil.convertDate(ticketDTO.getTripDate()), "yyyy-MM-dd", "dd/MMM/yyyy") + ", " + ticketDTO.getTicketDetails().size() + " Seats");
			content.put("pnr", ticketDTO.getCode());
			content.put("travelDate", ticketDTO.getTripDate().format("DD-MM-YYYY"));
			content.put("serviceNo", ticketDTO.getServiceNo());
			content.put("seats", ticketDTO.getSeatNames());
			content.put("fare", ticketDTO.getTicketFareWithAddons().toString());
			content.put("mobileNumber", ticketDTO.getPassengerMobile());
			content.put("charges", ticketDTO.getCancellationCharges().toString());
			content.put("bookedBy", ticketDTO.getTicketUser().getName());
			content.put("cancelledBy", authDTO.getUser().getName());
			content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	@Async
	public void pushTicketNotBoardedNotification(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketHelperCache ticketHelperCache = new TicketHelperCache();
		TicketDTO ticketCache = ticketHelperCache.getTicketNotBoarded(authDTO, ticketDTO.getCode());
		if (ticketCache != null) {
			TravelStatusEM travelStatus = Iterables.getFirst(ticketCache.getTicketDetails(), null).getTravelStatus();
			Map<String, Object> content = Maps.newHashMap();
			if ((authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TICKET_NOT_BOARDED) && travelStatus.getId() == TravelStatusEM.NOT_BOARDED.getId()) || (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TICKET_NOT_TRAVELED) && travelStatus.getId() == TravelStatusEM.NOT_TRAVELED.getId())) {

				NotificationSubscriptionTypeEM notificationType = null;
				if (travelStatus.getId() == TravelStatusEM.NOT_BOARDED.getId()) {
					notificationType = NotificationSubscriptionTypeEM.TICKET_NOT_BOARDED;
					content.put("title", "Not Boarded - " + ticketCache.getServiceNo() + ", " + ticketCache.getSeatNames());
					content.put("notificationType", NotificationSubscriptionTypeEM.TICKET_NOT_BOARDED.getCode());
				}
				else if (travelStatus.getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
					notificationType = NotificationSubscriptionTypeEM.TICKET_NOT_TRAVELED;
					content.put("title", "Not Travelled - " + ticketCache.getServiceNo() + ", " + ticketCache.getSeatNames());
					content.put("notificationType", NotificationSubscriptionTypeEM.TICKET_NOT_TRAVELED.getCode());
				}
				content.put("pnr", ticketCache.getCode());
				content.put("seats", ticketCache.getSeatNames());
				content.put("mobile", ticketCache.getPassengerMobile());
				content.put("serviceNo", ticketCache.getServiceNo());
				StationPointDTO stationPoint = getStationPointDTO(authDTO, ticketCache.getBoardingPoint());
				content.put("boardingPoint", stationPoint.getName());
				content.put("boardingTime", stationPoint.getMinitues() < 1440 ? ticketCache.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketCache.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
				content.put("bookedBy", getUserDTOById(authDTO, ticketCache.getTicketUser()).getName());

				if (notificationType != null) {
					TripDTO existTripDTO = new TripDTO();
					existTripDTO.setCode(ticketDTO.getTripDTO().getCode());
					existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

					BusVehicleDTO busVehicleDTO = null;
					TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
					if (tripInfo != null && tripInfo.getBusVehicle() != null) {
						busVehicleDTO = tripInfo.getBusVehicle();
					}

					List<StationDTO> stations = new ArrayList<>();
					stations.add(ticketDTO.getFromStation());
					stations.add(ticketDTO.getToStation());

					pushNotification(authDTO, notificationType, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
				}
			}
		}
	}

	// public void pushScheduleNotification(AuthDTO authDTO, ScheduleDTO
	// scheduleDTO, Map<String, String> schedule) {
	// try {
	// if
	// (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.NEW_SCHEDULE))
	// {
	//
	// ScheduleDTO existScheduleDTO = new ScheduleDTO();
	// existScheduleDTO.setCode(scheduleDTO.getCode());
	// existScheduleDTO = getSchedule(authDTO, existScheduleDTO);
	// ScheduleBusDTO scheduleBus = scheduleBusService.getByScheduleId(authDTO,
	// existScheduleDTO);
	//
	// Map<String, String> content = Maps.newHashMap();
	// content.put("notificationType",
	// NotificationSubscriptionTypeEM.NEW_SCHEDULE.getCode());
	// content.put("title", "New Schedule Created");
	// content.put("serviceNo", existScheduleDTO.getServiceNumber());
	// content.put("fromDate",
	// DateUtil.getDateTime(existScheduleDTO.getActiveFrom()).format("DD-MM-YYYY"));
	// content.put("toDate",
	// DateUtil.getDateTime(existScheduleDTO.getActiveTo()).format("DD-MM-YYYY"));
	// if (scheduleBus != null && scheduleBus.getBus() != null) {
	// content.put("busType",
	// BitsUtil.getBusCategoryUsingEM(scheduleBus.getBus().getCategoryCode()));
	// }
	// content.put("updatedBy", schedule.get("updatedBy"));
	//
	// pushNotification(authDTO, NotificationSubscriptionTypeEM.NEW_SCHEDULE,
	// content);
	// }
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	@Override
	public void pushScheduleEditNotification(AuthDTO authDTO, String scheduleCode, List<Map<String, String>> scheduleMaps) {
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.SCHEDULE_EDIT)) {
			Map<String, Object> content = Maps.newHashMap();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);
			scheduleDTO = getSchedule(authDTO, scheduleDTO);

			content.put("notificationType", NotificationSubscriptionTypeEM.SCHEDULE_EDIT.getCode());
			content.put("title", "Schedule Edited - " + scheduleDTO.getServiceNumber());

			Map<String, String> schedule = Iterables.getFirst(scheduleMaps, null);
			content.put("header", "Schedule details for the service number " + scheduleDTO.getServiceNumber() + " has been modified by " + schedule.get("updatedBy"));
			content.put("updatedBy", schedule.get("updatedBy"));

			StringBuilder messageBody = new StringBuilder();
			int index = 0;
			for (Map<String, String> scheduleMap : scheduleMaps) {
				if (messageBody.length() > 0) {
					messageBody.append("</br>");
				}
				messageBody.append((++index) + "." + scheduleMap.get("keyword") + Text.SINGLE_SPACE);
				messageBody.append(!String.valueOf(scheduleMap.get("oldContent")).equals(Text.HYPHEN) ? scheduleMap.get("oldContent") + Text.SINGLE_SPACE + " to " : Text.EMPTY);
				messageBody.append(!String.valueOf(scheduleMap.get("newContent")).equals(Text.HYPHEN) ? scheduleMap.get("newContent") : Text.EMPTY);
			}
			content.put("changes", messageBody.toString());

			pushNotification(authDTO, NotificationSubscriptionTypeEM.SCHEDULE_EDIT, content, null, scheduleDTO, null);
		}
	}

	private void pushNotification(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType, Map<String, Object> dataModel, List<StationDTO> stations, ScheduleDTO scheduleDTO, BusVehicleDTO vehicleDTO) {
		try {
			Map<String, NotificationSubscriptionDTO> subscriptionsMap = new HashMap<String, NotificationSubscriptionDTO>();
			String title = String.valueOf(dataModel.get("title"));
			List<NotificationSubscriptionDTO> subscriptionList = getSubscriptionByType(authDTO, subscriptionType);

			// Grouping with Notification medium
			for (NotificationSubscriptionDTO subscriptionDTO : subscriptionList) {
				List<UserDTO> userList = getUsers(authDTO, subscriptionDTO);
				for (NotificationMediumEM notificationMedium : subscriptionDTO.getNotificationMediumList()) {
					NotificationSubscriptionDTO notificationSubscriptionDTO = new NotificationSubscriptionDTO();
					if (subscriptionsMap.get(notificationMedium.getCode()) != null) {
						notificationSubscriptionDTO = subscriptionsMap.get(notificationMedium.getCode());
						userList.addAll(notificationSubscriptionDTO.getUserList());
					}
					notificationSubscriptionDTO.setUserList(userList);
					subscriptionsMap.put(notificationMedium.getCode(), notificationSubscriptionDTO);
				}
			}

			Map<String, List<String>> userGCMToken = new HashMap<String, List<String>>();
			for (Map.Entry<String, NotificationSubscriptionDTO> sub : subscriptionsMap.entrySet()) {
				String notificationMedium = sub.getKey();
				if (!notificationMedium.equals(NotificationMediumEM.EZEEBOT_APP.getCode())) {
					continue;
				}
				NotificationSubscriptionDTO subscriptionDTO = sub.getValue();
				for (UserDTO subscribeUser : subscriptionDTO.getUserList()) {
					List<AppStoreDetailsDTO> userAppList = getAppstoreByUserId(authDTO, subscribeUser);
					if (userAppList.isEmpty()) {
						continue;
					}
					boolean isSectorAuthorized = applySector(authDTO, subscribeUser, stations, scheduleDTO, vehicleDTO);
					if (!isSectorAuthorized) {
						continue;
					}
					List<String> gcmTokenList = userAppList.stream().map(user -> user.getGcmToken()).collect(Collectors.toList());
					userGCMToken.put(subscribeUser.getCode(), gcmTokenList);
				}
			}

			if (!userGCMToken.isEmpty()) {
				String content = TemplateUtils.getInstance().processFileContent(Text.NOTIFICATION_EZEEBOT, dataModel);
				JSONArray alertArray = new JSONArray();
				for (Map.Entry<String, List<String>> entry : userGCMToken.entrySet()) {
					for (String userKey : entry.getValue()) {
						JSONObject alertdata = new JSONObject();
						alertdata.put("priority", "COLD");
						alertdata.put("to", userKey);
						alertdata.put("collapseKey", StringUtil.removeSymbol(title));
						alertdata.put("category", subscriptionType.getCategory());
						alertdata.put("product", "ezeebus");
						alertdata.put("time", DateUtil.NowV2().toString(DateUtil.JODA_DATE_TIME_FORMATE));
						alertdata.put("namespace", authDTO.getNamespaceCode());
						alertdata.put("userCode", entry.getKey());
						alertdata.put("notificationTitle", title);
						alertdata.put("notificationBody", content);
						alertdata.put("mid", TokenGenerator.generateCode("BITS", 20));
						alertArray.add(alertdata);
					}
				}
				fcmService.alertzService(authDTO, "ezeebotfcm", alertArray);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void pushNotificationV2(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType, UserDTO userDTO, Map<String, Object> dataModel, List<StationDTO> stations, ScheduleDTO scheduleDTO, BusVehicleDTO vehicleDTO) {
		try {
			String title = String.valueOf(dataModel.get("title"));
			Map<String, List<String>> userGCMToken = new HashMap<String, List<String>>();

			UserDTO subscribeUser = userDTO;

			List<AppStoreDetailsDTO> userAppList = getAppstoreByUserId(authDTO, subscribeUser);
			boolean isSectorAuthorized = applySector(authDTO, subscribeUser, stations, scheduleDTO, vehicleDTO);
			if (!userAppList.isEmpty() && isSectorAuthorized) {
				List<String> gcmTokenList = userAppList.stream().map(user -> user.getGcmToken()).collect(Collectors.toList());
				userGCMToken.put(subscribeUser.getCode(), gcmTokenList);
			}

			if (!userGCMToken.isEmpty()) {
				String content = TemplateUtils.getInstance().processFileContent(Text.NOTIFICATION_EZEEBOT, dataModel);

				JSONArray alertArray = new JSONArray();
				for (Map.Entry<String, List<String>> entry : userGCMToken.entrySet()) {
					for (String userKey : entry.getValue()) {
						JSONObject alertdata = new JSONObject();
						alertdata.put("priority", "COLD");
						alertdata.put("to", userKey);
						alertdata.put("collapseKey", StringUtil.removeSymbol(title));
						alertdata.put("category", subscriptionType.getCategory());
						alertdata.put("product", "ezeebus");
						alertdata.put("time", DateUtil.NowV2().toString(DateUtil.JODA_DATE_TIME_FORMATE));
						alertdata.put("namespace", authDTO.getNamespaceCode());
						alertdata.put("userCode", entry.getKey());
						alertdata.put("notificationTitle", title);
						alertdata.put("notificationBody", content);
						alertdata.put("mid", TokenGenerator.generateCode("BITS", 20));
						alertArray.add(alertdata);
					}
				}
				fcmService.alertzService(authDTO, "ezeebotfcm", alertArray);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<UserDTO> getUsers(AuthDTO authDTO, NotificationSubscriptionDTO subscriptionDTO) {
		List<UserDTO> finalUserList = new ArrayList<UserDTO>();
		for (GroupDTO groupDTO : subscriptionDTO.getGroupList()) {
			groupDTO = getGroupDTO(authDTO, groupDTO);
			List<UserDTO> userList = groupService.getUserDTO(authDTO, groupDTO);
			finalUserList.addAll(userList);
		}
		for (UserDTO userDTO : subscriptionDTO.getUserList()) {
			UserDTO user = userService.getUser(authDTO, userDTO);
			if (StringUtil.isNotNull(user.getCode())) {
				finalUserList.add(user);
			}
		}
		return finalUserList.stream().filter(StreamUtil.distinctByKey(p -> p.getCode())).filter(p -> p.getActiveFlag() == 1).collect(Collectors.toList());
	}

	private ScheduleDTO getSchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCache scheduleCache = new ScheduleCache();
		return scheduleCache.getScheduleDTObyId(authDTO, scheduleDTO);
	}

	@Async
	public void pushSeatVisibiltyNotification(AuthDTO authDTO, ScheduleSeatVisibilityDTO visibilityDTO) {
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.SEAT_VISIBILITY)) {
			Map<String, Object> content = Maps.newHashMap();

			ScheduleSeatVisibilityDTO scheduleSeatVisibilty = Iterables.getFirst(visibilityDTO.getList(), null);

			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleSeatVisibilty.getSchedule().getCode());
			scheduleDTO = getSchedule(authDTO, scheduleDTO);

			for (ScheduleSeatVisibilityDTO visibilty : visibilityDTO.getList()) {
				String visibiltyType = Text.EMPTY;
				if (visibilty.getActiveFlag() == 1 && visibilty.getVisibilityType().equals("ACAT") && visibilty.getReleaseMinutes() != 0) {
					content.put("title", "Seat Allocated - " + scheduleDTO.getServiceNumber() + ", " + DateUtil.convertDate(DateUtil.getDateTime(visibilty.getActiveFrom())));
					content.put("activityType", "Seat Allocated");
					visibiltyType = "Allocated To : ";
				}
				else if (visibilty.getActiveFlag() == 1 && visibilty.getVisibilityType().equals("HIDE") && visibilty.getReleaseMinutes() != 0) {
					content.put("title", "Seat BLocked - " + scheduleDTO.getServiceNumber() + ", " + DateUtil.convertDate(DateUtil.getDateTime(visibilty.getActiveFrom())));
					content.put("activityType", "Seat Blocked");
					visibiltyType = "Blocked To : ";
				}
				else if ((visibilty.getActiveFlag() != 1 || visibilty.getReleaseMinutes() == 0) && (StringUtil.isNotNull(visibilty.getCode()) || StringUtil.isNotNull(visibilty.getLookupCode()))) {
					content.put("title", "Seat Released - " + scheduleDTO.getServiceNumber() + ", " + DateUtil.convertDate(DateUtil.getDateTime(visibilty.getActiveFrom())));
					content.put("activityType", "Seat Released");
					visibiltyType = "Released To : ";

					ScheduleSeatVisibilityDTO existSeatVisibility = new ScheduleSeatVisibilityDTO();
					existSeatVisibility.setCode(StringUtil.isNotNull(visibilty.getLookupCode()) ? visibilty.getLookupCode() : visibilty.getCode());
					existSeatVisibility = getScheduleSeatVisibility(authDTO, existSeatVisibility);
					BusDTO busDTO = visibilty.getBus();
					if (existSeatVisibility != null) {
						visibilty = existSeatVisibility;
						visibilty.setBus(busDTO);
					}
				}

				StringBuilder groups = new StringBuilder();
				if (visibilty.getGroupList() != null) {
					for (GroupDTO groupDTO : visibilty.getGroupList()) {
						if (groups.length() > 0) {
							groups.append(Text.COMMA + Text.SINGLE_SPACE);
						}
						groups.append(groupDTO.getName());
					}
				}
				StringBuilder users = new StringBuilder();
				if (visibilty.getUserList() != null) {
					for (UserDTO userDTO : visibilty.getUserList()) {
						if (users.length() > 0) {
							users.append(Text.COMMA + Text.SINGLE_SPACE);
						}
						users.append(userDTO.getName());
					}
				}
				BusDTO busDTO = new BusDTO();
				busDTO.setCode(scheduleSeatVisibilty.getBus().getCode());
				busDTO = getBusDTO(authDTO, busDTO);
				Map<String, BusSeatLayoutDTO> seatMap = busDTO.getBusSeatLayoutMapFromList();

				StringBuilder seatCodes = new StringBuilder();
				for (BusSeatLayoutDTO layoutDTO : visibilty.getBus().getBusSeatLayoutDTO().getList()) {
					if (StringUtil.isNull(layoutDTO.getCode())) {
						continue;
					}
					if (seatCodes.length() > 0) {
						seatCodes.append(",");
					}
					seatCodes.append(seatMap.get(layoutDTO.getCode()).getName());
				}

				if (!content.isEmpty() && content.containsKey("title")) {
					content.put("notificationType", NotificationSubscriptionTypeEM.SEAT_VISIBILITY.getCode());
					content.put("minutes", visibilty.getReleaseMinutes() > 0 ? getTime(visibilty.getReleaseMinutes()) + " before" : "Never Release");
					content.put("groups", StringUtil.isNotNull(groups.toString()) ? visibiltyType + groups.toString() : Text.EMPTY);
					content.put("users", StringUtil.isNotNull(users.toString()) ? visibiltyType + users.toString() : Text.EMPTY);
					content.put("seats", seatCodes.toString());
					content.put("updatedBy", authDTO.getUser().getName());

					pushNotification(authDTO, NotificationSubscriptionTypeEM.SEAT_VISIBILITY, content, null, scheduleDTO, null);
				}
			}
		}
	}

	private String getTime(int minutes) {
		int day = minutes / 24 / 60;
		int hour = minutes / 60 % 24;

		StringBuilder timeConverion = new StringBuilder();
		timeConverion.append(day > 1 ? day + " days" : day == 1 ? day + " day" : Text.EMPTY);
		timeConverion.append(hour > 1 ? hour + " hours" : hour == 1 ? hour + " hour" : Text.EMPTY);
		return timeConverion.toString();
	}

	@Override
	public void pushTripOccupancyStatusNotification(AuthDTO authDTO) {
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.OCCUPANCY_STATUS)) {
			Map<String, Object> content = null;

			DateTime now = DateUtil.NOW();
			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(DateUtil.getDateTime(now.format("YYYY-MM-DD")));
			List<TripDTO> tripList = searchService.getAllTrips(authDTO, searchDTO);

			if (!tripList.isEmpty()) {
				List<String> tripDetailsList = new ArrayList<>();
				content = Maps.newHashMap();
				content.put("title", "Occupancy Status - " + now.format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
				content.put("notificationType", NotificationSubscriptionTypeEM.OCCUPANCY_STATUS.getCode());

				Collections.sort(tripList, new Comparator<TripDTO>() {
					@Override
					public int compare(TripDTO t1, TripDTO t2) {
						return new CompareToBuilder().append(DateUtil.addMinituesToDate(t1.getTripDate().getStartOfDay(), t1.getTripMinutes()), DateUtil.addMinituesToDate(t2.getTripDate().getStartOfDay(), t2.getTripMinutes())).toComparison();
					}
				});

				int openCount = 0;
				int closedCount = 0;
				int tripCanceledCount = 0;

				int totalbookedCount = 0;
				int totalSeatCount = 0;

				for (TripDTO tripDTO : tripList) {
					DateTime originDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate().getStartOfDay(), tripDTO.getTripMinutes());

					StringBuilder tripDetails = new StringBuilder();
					tripDetails.append(StringUtil.isNotNull(tripDTO.getSchedule().getServiceNumber()) ? tripDTO.getSchedule().getServiceNumber() : Text.HYPHEN);
					tripDetails.append(tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getBusVehicle() != null ? "(" + tripDTO.getTripInfo().getBusVehicle().getRegistationNumber() + ")" : Text.EMPTY);
					tripDetails.append(Text.COMMA + Text.SINGLE_SPACE);
					tripDetails.append(originDateTime.format("hh12:mm a", Locale.forLanguageTag("en_IN")));
					tripDetails.append(Text.COLON);
					tripDetails.append(tripDTO.getBookedSeatCount());
					tripDetails.append(Text.COMMA);

					int bookedSeatCount = tripDTO.getBookedSeatCount();
					int seatCount = tripDTO.getBus().getReservableLayoutSeatCount();

					BigDecimal occupancyPercentage = BigDecimal.valueOf(bookedSeatCount * 100).divide(BigDecimal.valueOf(seatCount), 2, RoundingMode.HALF_UP);
					tripDetails.append(occupancyPercentage.setScale(0, RoundingMode.HALF_UP) + " %" + Text.COMMA + Text.SINGLE_SPACE);
					BigDecimal amount = tripDTO.getRevenueAmount();
					String revenueAmount = getRevenueAmount(amount);
					tripDetails.append(revenueAmount);

					totalbookedCount = totalbookedCount + bookedSeatCount;
					totalSeatCount = totalSeatCount + seatCount;

					if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_OPEN.getId() || tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_YET_OPEN.getId()) {
						tripDetails.append(now.gt(originDateTime) ? "DEPARTED" : "YET TO DEPART");
						++openCount;
					}

					if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CLOSED.getId()) {
						tripDetails.append(Text.COMMA + Text.SINGLE_SPACE);
						tripDetails.append("CLOSED");
						++closedCount;
					}
					else if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId()) {
						tripDetails.append(Text.COMMA + Text.SINGLE_SPACE);
						tripDetails.append("CANCELLED");
						++tripCanceledCount;
					}
					tripDetailsList.add(tripDetails.toString());
				}

				double totalBookPercentage = (Float.valueOf(totalbookedCount) * 100) / totalSeatCount;
				totalBookPercentage = Math.round(totalBookPercentage);

				content.put("tripCount", tripList.size() + ", " + totalbookedCount + "/" + totalSeatCount + Text.SINGLE_SPACE + totalBookPercentage + " %");
				content.put("tripCountDetails", "Open - " + openCount + ", Close - " + closedCount + ", TCA - " + tripCanceledCount);
				content.put("summary", "");
				content.put("tripDetails", tripDetailsList);
			}

			if (content != null) {
				pushNotification(authDTO, NotificationSubscriptionTypeEM.OCCUPANCY_STATUS, content, null, null, null);
			}
		}
	}

	private boolean isUserSubscriptionEnabled(AuthDTO authDTO, UserDTO userDTO, NotificationSubscriptionTypeEM subscriptionType) {
		boolean isEnabled = false;
		List<NotificationSubscriptionDTO> subscriptionList = getSubscriptionByType(authDTO, subscriptionType);
		for (NotificationSubscriptionDTO subscriptionDTO : subscriptionList) {
			for (UserDTO existUser : subscriptionDTO.getUserList()) {
				if (existUser.getId() == userDTO.getId()) {
					isEnabled = true;
					break;
				}
			}
			if (isEnabled) {
				break;
			}
		}
		return isEnabled;
	}

	private ScheduleSeatVisibilityDTO getScheduleSeatVisibility(AuthDTO authDTO, ScheduleSeatVisibilityDTO seatVisibilityDTO) {
		ScheduleSeatVisibilityDTO existSeatVisibilty = null;
		ScheduleSeatVisibilityDAO visibilityDAO = new ScheduleSeatVisibilityDAO();
		seatVisibilityDTO = visibilityDAO.getScheduleSeatVisibility(authDTO, seatVisibilityDTO);
		if (seatVisibilityDTO.getId() != 0) {
			existSeatVisibilty = new ScheduleSeatVisibilityDTO();
			if (seatVisibilityDTO.getGroupList() != null) {
				for (GroupDTO group : seatVisibilityDTO.getGroupList()) {
					groupService.getGroup(authDTO, group);
				}
				existSeatVisibilty.setGroupList(seatVisibilityDTO.getGroupList());
			}
			if (seatVisibilityDTO.getUserList() != null) {
				for (UserDTO user : seatVisibilityDTO.getUserList()) {
					userService.getUser(authDTO, user);
				}
				existSeatVisibilty.setUserList(seatVisibilityDTO.getUserList());
			}
			BusDTO busDTO = new BusDTO();
			BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
			busSeatTypeDTO.setList(seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList());
			busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
			existSeatVisibilty.setBus(busDTO);
			existSeatVisibilty.setReleaseMinutes(seatVisibilityDTO.getReleaseMinutes());
		}
		return existSeatVisibilty;
	}

	private boolean applySector(AuthDTO authDTO, UserDTO userDTO, List<StationDTO> stations, ScheduleDTO scheduleDTO, BusVehicleDTO vehicleDTO) {
		boolean isSectorAuthorized = Text.FALSE;
		SectorDTO sector = sectorService.getUserActiveSector(authDTO, userDTO);
		if (sector.getActiveFlag() == Numeric.ONE_INT) {
			if (stations != null && !stations.isEmpty() && BitsUtil.isStationExistsV2(sector.getStation(), stations) != null) {
				isSectorAuthorized = Text.TRUE;
			}
			else if (scheduleDTO != null && BitsUtil.isScheduleExists(sector.getSchedule(), scheduleDTO) != null) {
				isSectorAuthorized = Text.TRUE;
			}
			else if (vehicleDTO != null && BitsUtil.isVehicleExist(sector.getVehicle(), vehicleDTO) != null) {
				isSectorAuthorized = Text.TRUE;
			}
			else if (userDTO.getOrganization() != null && BitsUtil.isOrganizationExists(sector.getOrganization(), userDTO.getOrganization()) != null) {
				isSectorAuthorized = Text.TRUE;
			}
			else if (stations == null && scheduleDTO == null && vehicleDTO == null && (sector.getOrganization() == null || sector.getOrganization().isEmpty())) {
				isSectorAuthorized = Text.TRUE;
			}
		}
		else {
			isSectorAuthorized = Text.TRUE;
		}
		return isSectorAuthorized;
	}

	@Override
	public void pushCustomerFeedbackNotification(AuthDTO authDTO, UserFeedbackDTO userFeedBack) {
		Map<String, Object> content = Maps.newHashMap();
		content.put("notificationType", NotificationSubscriptionTypeEM.CUSTOMER_FEEDBACK.getCode());
		content.put("title", "New feedback received from " + userFeedBack.getName());
		content.put("mobile", userFeedBack.getMobile());
		content.put("email", userFeedBack.getEmail());
		content.put("comments", userFeedBack.getComments());
		pushNotification(authDTO, NotificationSubscriptionTypeEM.CUSTOMER_FEEDBACK, content, null, null, null);
	}

	@Override
	public void pushSeatEditNotification(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		Map<String, Object> content = Maps.newHashMap();
		content.put("title", "Seat Edit");
		content.put("notificationType", NotificationSubscriptionTypeEM.SEAT_EDIT.getCode());
		content.put("pnr", ticketDTO.getCode());
		content.put("seat(s)", ticketDTO.getSeatNames());
		content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
		content.put("travelDate", ticketDTO.getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
		content.put("updatedBy", authDTO.getUser().getUsername());
		content.put("updatedAt", ticketDTO.getUpdatedAt().format("YYYY-MM-DD hh:mm:ss"));
		content.put("seatStatus", event);
		TripDTO existTripDTO = new TripDTO();
		existTripDTO.setCode(ticketDTO.getTripDTO().getCode());
		existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

		List<StationDTO> stations = new ArrayList<>();
		stations.add(ticketDTO.getFromStation());
		stations.add(ticketDTO.getToStation());

		BusVehicleDTO busVehicleDTO = null;
		TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
		if (tripInfo != null && tripInfo.getBusVehicle() != null) {
			busVehicleDTO = tripInfo.getBusVehicle();
		}
		pushNotification(authDTO, NotificationSubscriptionTypeEM.SEAT_EDIT, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
	}

	@Override
	public void pushTicketRescheduleNotification(AuthDTO authDTO, TicketDTO transferDTO, Map<String, Boolean> additionalAttribute) {
		TicketDTO ticketDTO = new TicketDTO();
		TicketDAO dao = new TicketDAO();
		TicketAddonsDetailsDTO ticketAddonsDetailDTO = transferDTO.getTicketAddonsDetails().stream().filter(p -> p.getAddonsType().getCode().equals("TTCA")).findFirst().orElse(null);
		TicketAddonsDetailsDTO ticketAddonsDetailsDTO = transferDTO.getTicketAddonsDetails().stream().filter(p -> p.getAddonsType().getCode().equals("TRPTA")).findFirst().orElse(null);
		ticketDTO.setCode(ticketAddonsDetailsDTO.getRefferenceCode());
		dao.showTicket(authDTO, ticketDTO);
		ticketDTO.setFromStation(stationService.getStation(ticketDTO.getFromStation()));
		ticketDTO.setToStation(stationService.getStation(ticketDTO.getToStation()));
		UserDTO userDTO = ticketDTO.getTicketUser();
		userService.getUserDTO(authDTO, userDTO);
		Map<String, Object> content = Maps.newHashMap();
		content.put("title", "Ticket Reschedule");
		content.put("notificationType", NotificationSubscriptionTypeEM.RESCHEDULE.getCode());
		content.put("oldPnr", ticketDTO.getCode());
		content.put("oldFare", ticketDTO.getTicketFareWithAddons().toString());
		content.put("oldDoj", ticketDTO.getTripDateTime().format("DD-MM-YYYY hh:mm"));
		content.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());
		content.put("bookedBy", userDTO.getUsername());
		content.put("pnr", transferDTO.getCode());
		content.put("doj", transferDTO.getTripDateTime().format("DD-MM-YYYY hh:mm"));
		content.put("fare", transferDTO.getTicketFareWithAddons().toString());
		content.put("transferedBy", authDTO.getUser().getUsername());
		content.put("transferedAt", transferDTO.getTicketAt().format("DD-MM-YYYY hh:mm:ss"));
		if (additionalAttribute.get("captureTransferCharge") != null && additionalAttribute.get("captureTransferCharge") == true && ticketAddonsDetailDTO != null) {
			content.put("rescheduleCharges", ticketAddonsDetailDTO.getValue());
		}
		else {
			content.put("rescheduleCharges", Text.HYPHEN);
		}
		TripDTO existTripDTO = new TripDTO();
		existTripDTO.setCode(transferDTO.getTripDTO().getCode());
		existTripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, existTripDTO);

		List<StationDTO> stations = new ArrayList<>();
		stations.add(transferDTO.getFromStation());
		stations.add(transferDTO.getToStation());

		BusVehicleDTO busVehicleDTO = null;
		TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, existTripDTO);
		if (tripInfo != null && tripInfo.getBusVehicle() != null) {
			busVehicleDTO = tripInfo.getBusVehicle();
		}
		pushNotification(authDTO, NotificationSubscriptionTypeEM.RESCHEDULE, content, stations, existTripDTO.getSchedule(), busVehicleDTO);
	}

	@Override
	public void pushDailyTravelStatusNotification(AuthDTO authDTO, List<String> travelStatusSummary) {
		try {
			Map<String, Object> content = Maps.newHashMap();
			content.put("notificationType", NotificationSubscriptionTypeEM.TRAVEL_STATUS.getCode());
			content.put("title", "Travel Date - " + DateUtil.NOW().format("DD-MM-YYYY"));
			content.put("travelStatus", travelStatusSummary);

			pushNotification(authDTO, NotificationSubscriptionTypeEM.TRAVEL_STATUS, content, null, null, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String getRevenueAmount(BigDecimal revenueAmount) {
		String suffix;
		if (revenueAmount.compareTo(new BigDecimal("100000")) >= 0) {
			revenueAmount = revenueAmount.divide(new BigDecimal("100000"), 2, RoundingMode.HALF_UP);
			suffix = "L";
		}
		else if (revenueAmount.compareTo(new BigDecimal("1000")) >= 0) {
			revenueAmount = revenueAmount.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
			suffix = "K";
		}
		else {
			return revenueAmount.toString();
		}
		return revenueAmount.stripTrailingZeros().toPlainString() + suffix;
	}

	@Override
	public void pushVehicleAssignedNotification(AuthDTO authDTO, TripDTO tripDTO, DateTime updatedAt) {
		Map<String, Object> content = Maps.newHashMap();
		content.put("notificationType", NotificationSubscriptionTypeEM.VEHICLE_ASSIGNED.getCode());
		content.put("title", "Vehicle Assigned-" + tripDTO.getSchedule().getServiceNumber());
		content.put("tripName", tripDTO.getSchedule().getName());
		content.put("serviceNumber", tripDTO.getSchedule().getServiceNumber());
		content.put("vehicleNumber", tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
		content.put("driverName", tripDTO.getTripInfo().getDriverName());
		content.put("driverNumber", tripDTO.getTripInfo().getDriverMobile());
		content.put("updatedBy", authDTO.getUser().getName());
		content.put("updatedAt", updatedAt.format("DD-MM-YYYY hh:mm:ss"));
		pushNotification(authDTO, NotificationSubscriptionTypeEM.VEHICLE_ASSIGNED, content, null, tripDTO.getSchedule(), null);
	}
}
