package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.fcm.FCMServerKeyEM;
import org.in.com.aggregator.fcm.FCMService;
import org.in.com.aggregator.gps.TrackBusService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.aggregator.whatsapp.WhatsappProviderEM;
import org.in.com.aggregator.whatsapp.WhatsappService;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.BaseDAO;
import org.in.com.dao.NotificationDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.EventNotificationConfigDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.NotificationSubscriptionDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.EventNotificationConfigService;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationConfigService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.SearchService;
import org.in.com.service.StationPointService;
import org.in.com.service.TabletService;
import org.in.com.service.TicketService;
import org.in.com.service.TravelStopsService;
import org.in.com.service.TripService;
import org.in.com.service.TripVanInfoService;
import org.in.com.service.UserCustomerService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsShortURL;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.CollectionsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StreamUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class NotificationImpl extends CacheCentral implements NotificationService {
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.TripImpl");
	private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("\\d{10}");

	@Autowired
	TicketService ticketService;
	@Autowired
	SMSService smsService;
	@Autowired
	EmailService emailService;
	@Autowired
	TripService tripService;
	@Autowired
	SearchService searchService;
	@Autowired
	BitsService bitsService;
	@Autowired
	UserCustomerService userCustomerService;
	@Autowired
	FCMService fcmService;
	@Autowired
	UserService userService;
	@Autowired
	TripVanInfoService tripVanInfoService;
	@Autowired
	TabletService tabletService;
	@Autowired
	GroupService groupService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	TrackBusService trackbusService;
	@Autowired
	TravelStopsService travelStopsService;
	@Autowired
	WhatsappService whatsappService;
	@Autowired
	EventNotificationConfigService eventNotificationConfigService;
	@Autowired
	NotificationConfigService notificationConfigService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	StationPointService stationPointService;

	public String tripCancelNotification(AuthDTO authDTO, String ticketCode, String reason, String supportNumber) {
		String message = null;
		Future<SmsResponse> futureTask = null;
		TicketDTO ticketDTO = new TicketDTO();
		try {
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING)) {
				futureTask = whatsappService.tripCancelNotification(authDTO, ticketDTO, reason, supportNumber);
			}
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) || futureTask == null || !futureTask.get().getCode().equals("true")) {
				futureTask = smsService.tripCancelNotification(authDTO, ticketDTO, reason, supportNumber);
			}
		}
		catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		finally {
			if (futureTask != null) {
				while (!futureTask.isDone()) {
					try {
						Thread.sleep(2000);
					}
					catch (InterruptedException e) {
					}
				}
				try {
					message = futureTask.get() != null ? futureTask.get().getContent() : Text.EMPTY;
				}
				catch (InterruptedException | ExecutionException e) {
					throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
				}

				ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: " + reason + ", Support Number: " + supportNumber);
				SaveTripHistory(authDTO, ticketDTO);
			}
		}
		return message;

	}

	public String tripDelayNotification(AuthDTO authDTO, String ticketCode, String reason, String delayTime, String supportNumber) {
		String message = null;
		Future<SmsResponse> futureTask = null;
		TicketDTO ticketDTO = new TicketDTO();
		try {
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING)) {
				futureTask = whatsappService.tripDelayNotification(authDTO, ticketDTO, reason, delayTime, supportNumber);
			}
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) || !futureTask.get().getCode().equals("true")) {
				futureTask = smsService.tripDelayNotification(authDTO, ticketDTO, reason, delayTime, supportNumber);
			}
		}
		catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		finally {
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

				ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: " + reason + ", Delay Time: " + delayTime + ", Support Number: " + supportNumber);
				SaveTripHistory(authDTO, ticketDTO);
			}
		}
		return message;

	}

	public String tripStationPointChangeNotification(AuthDTO authDTO, String ticketCode, String reason, String stationPointName, String stationPointTime, String supportNumber) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		Future<SmsResponse> futureTask = smsService.tripStationPointChangeNotification(authDTO, ticketDTO, reason, stationPointName, stationPointTime, supportNumber);

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
			ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: " + reason + ", Station Point:" + stationPointName + " - " + stationPointTime + ", Support Number: " + supportNumber);
			SaveTripHistory(authDTO, ticketDTO);
		}
		return message;
	}

	@Override
	public void firebusbuddyAfterboard(AuthDTO authDTO, String ticketCode, String vehicleNumber, String mobileNumber) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

		if (ticketDTO.getId() == 0 || ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		if (StringUtils.isNotBlank(mobileNumber) && MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		smsService.sendBusbuddyAfterboard(authDTO, ticketCode, vehicleNumber, mobileNumber);

	}

	@Override
	public String tripEarlyNotification(AuthDTO authDTO, String ticketCode, String reason, String earlyTime, String supportNumber) {
		String message = null;
		Future<SmsResponse> futureTask = null;
		TicketDTO ticketDTO = new TicketDTO();
		try {
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING)) {
				futureTask = whatsappService.tripEarlyNotification(authDTO, ticketDTO, reason, earlyTime, supportNumber);
			}

			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) || !futureTask.get().getCode().equals("true")) {
				futureTask = smsService.tripEarlyNotification(authDTO, ticketDTO, reason, earlyTime, supportNumber);
			}
		}
		catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		finally {
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

				ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: " + reason + ", Early Time: " + earlyTime + ", Support Number: " + supportNumber);
				SaveTripHistory(authDTO, ticketDTO);
			}
		}
		return message;
	}

	private void SaveTripHistory(AuthDTO authDTO, TicketDTO ticketDTO) {
		BaseDAO base = new BaseDAO();
		base.addAuditLog(authDTO, ticketDTO.getTripDTO().getCode(), "trip", "Trip Notification", ticketDTO.getRemarks());
	}

	@Override
	public Map<String, String> getSMSStatus(AuthDTO authDTO, String refferenceCode) {
		Map<String, String> statusMap = smsService.getSMSStatus(authDTO, refferenceCode);
		return statusMap;
	}

	@Override
	public String sendVehicleChangeSms(AuthDTO authDTO, String ticketCode) {
		String message = null;
		try {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			if (TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() == ticketDTO.getTicketStatus().getId() || TicketStatusEM.TRIP_CANCELLED.getId() == ticketDTO.getTicketStatus().getId()) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TripInfoDTO tripInfo = tripService.getTripInfo(authDTO, ticketDTO.getTripDTO());
			if (tripInfo != null && tripInfo.getBusVehicle() != null) {
				message = smsService.sendVehicleChangeSms(authDTO, ticketDTO, tripInfo);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	@Override
	public String vanPickupNotify(AuthDTO authDTO, String ticketCode, String registrationNumber, String supportName, String supportNumber, String trackUrl) {
		String message = null;
		try {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("travelDatetime", DateUtil.parseDateFormat(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd MMM hh:mm a"));
			dataModel.put("passengerName", WordUtils.capitalize(ticketDTO.getPassengerName()));
			dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());
			dataModel.put("ticketCode", ticketDTO.getCode());
			dataModel.put("vanNumber", registrationNumber);
			dataModel.put("vanContact", supportNumber);
			dataModel.put("supportName", supportName);
			dataModel.put("trackBus", trackUrl);
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
			message = smsService.sendVanPickupTracking(authDTO, dataModel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	@Async
	public void sendTripJourneyTrackingSMS(AuthDTO authDTO, TripDTO tripDTO) throws ServiceException {
		try {
			TRIP_INFO_LOGGER.info("Start Tracking SMS - {} {} {}", authDTO.getNamespaceCode(), authDTO.getUser().getUsername(), tripDTO.getCode());
			if (tripDTO.getId() == 0) {
				tripDTO = tripService.getTrip(authDTO, tripDTO);
			}
			if (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId() || tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_YET_OPEN.getId()) {
				TRIP_INFO_LOGGER.error("TRIP Cancelled SMS - {} {} {}", tripDTO.getCode(), tripDTO.getTripStatus().getCode(), ErrorCode.TRIP_CANCELLED.getCode());
				System.out.println(tripDTO.getCode() + " " + tripDTO.getTripStatus().getCode() + " Exit Happy Journey SMS");
				throw new ServiceException(ErrorCode.TRIP_CANCELLED);
			}

			List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
			Map<String, StageStationDTO> stationMap = new HashMap<String, StageStationDTO>();
			for (StageStationDTO stationDTO : stageList) {
				stationMap.put(stationDTO.getStation().getCode(), stationDTO);
			}

			DateTime originDateTime = BitsUtil.getOriginStationPointDateTime(stageList, tripDTO.getTripDate());
			int diffMinitues = DateUtil.getMinutiesDifferent(originDateTime, DateUtil.NOW());
			int trackbusMinutes = -authDTO.getNamespace().getProfile().getTrackbusMinutes();
			// Before 30 mins to 4 hrs
			if (diffMinitues < trackbusMinutes || diffMinitues > 240) {
				TRIP_INFO_LOGGER.error("Time Validation Fail TC: {} TS:{} DT:{}TM:{} EC:{}", tripDTO.getCode(), tripDTO.getTripStatus().getCode(), diffMinitues, trackbusMinutes, ErrorCode.GPS_TRIP_TRACKING_TIME_NOT_IN_PERIOD.getCode());
				throw new ServiceException(ErrorCode.GPS_TRIP_TRACKING_TIME_NOT_IN_PERIOD);
			}
			TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, tripDTO);
			if (tripInfoDTO == null || tripInfoDTO.getBusVehicle() == null || tripInfoDTO.getBusVehicle().getId() == 0) {
				notificationPushService.pushVehicleNotAssignedNotification(authDTO, tripDTO);
			}
			if (tripInfoDTO == null) {
				TRIP_INFO_LOGGER.error(" No trip Info - {} {}", tripDTO.getCode(), ErrorCode.TRIP_INFO_INVALID.getCode());
				throw new ServiceException(ErrorCode.TRIP_INFO_INVALID);
			}
			NotificationTypeEM notificationType = NotificationTypeEM.HAPPY_JOURNEY;
			if (tripInfoDTO.getBusVehicle() != null && (tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.EZEEGPS.getId() || ((tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.JTECH.getId() || tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.SENSEL.getId() || tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.PLAYGPS.getId())) && tripInfoDTO.getBusVehicle().getDeviceVendor().getNamespace().contains(authDTO.getNamespaceCode()))) {
				notificationType = NotificationTypeEM.GPS_TRACKING;
			}
			if (!authDTO.getAdditionalAttribute().containsKey("OVERRIDE") && tripInfoDTO.checkNoficationTypeExists(notificationType)) {
				TRIP_INFO_LOGGER.error("Already Sent SMS - {} {} {} {} {}", tripDTO.getCode(), tripInfoDTO.getNotificationStatusCodes(), notificationType, authDTO.getAdditionalAttribute().containsKey("OVERRIDE"), ErrorCode.TRIP_NOTIFICATION_ALREADY_SENT.getCode());
				throw new ServiceException(ErrorCode.TRIP_NOTIFICATION_ALREADY_SENT, " : " + notificationType.getCode());
			}

			TripChartDTO tripChartDTO = tripService.getTripChart(authDTO, tripDTO);

			// Update Trip details in geo
			if (tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null && (tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.EZEEGPS.getId() || ((tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.JTECH.getId() || tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.SENSEL.getId() || tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.PLAYGPS.getId())) && tripInfoDTO.getBusVehicle().getDeviceVendor().getNamespace().contains(authDTO.getNamespaceCode()))) {
				TRIP_INFO_LOGGER.info(" Push to Geo - {}", tripDTO.getCode());
				tripChartDTO.setVendorList(getVendorList(authDTO));

				List<TravelStopsDTO> travelStopList = travelStopsService.getByScheduleCode(authDTO, tripDTO.getSchedule());
				tripDTO.getSchedule().setTravelStopsList(travelStopList);

				trackbusService.updateGeoTripDetails(authDTO, GPSDeviceVendorEM.EZEEGPS, tripChartDTO);
			}
			List<TripChartDetailsDTO> uniqueList = new ArrayList<>();
			if (tripDTO.getTicketDetailsList() == null || tripDTO.getTicketDetailsList().isEmpty()) {
				List<TripChartDetailsDTO> finalList = getUniqueTicketList(tripChartDTO.getTicketDetailsList());
				uniqueList.addAll(finalList);
			}
			else if (tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
				List<TripChartDetailsDTO> finalList = getUniqueTicketList(tripDTO.getTicketDetailsList(), tripChartDTO.getTicketDetailsList());
				uniqueList.addAll(finalList);
			}

			for (TripChartDetailsDTO chartDetailsDTO : uniqueList) {
				// Apply recent timings from schedule data
				if (stationMap.get(chartDetailsDTO.getFromStation().getCode()) != null) {
					ScheduleDTO scheduleDTO = scheduleService.getSchedule(authDTO, tripDTO.getSchedule());
					List<ScheduleStationPointDTO> scheduleStationPointList = scheduleStationPointService.getScheduleStationPoint(authDTO, scheduleDTO);

					Map<Integer, String> stationPointAddressMap = new HashMap<Integer, String>();
					for (ScheduleStationPointDTO scheduleStationPoint : scheduleStationPointList) {
						stationPointAddressMap.put(scheduleStationPoint.getStationPoint().getId(), scheduleStationPoint.getAddress());
					}

					StageStationDTO stationDTO = stationMap.get(chartDetailsDTO.getFromStation().getCode());
					StationPointDTO stationPointDTO = CollectionsUtil.getStationPoint(stationDTO.getStationPoint(), chartDetailsDTO.getBoardingPoint());

					if (stationPointDTO == null) {
						continue;
					}
					String boardingAddress = stationPointAddressMap.get(chartDetailsDTO.getBoardingPoint().getId());
					chartDetailsDTO.getBoardingPoint().setAddress(StringUtil.isNotNull(boardingAddress) ? boardingAddress : stationPointDTO.getAddress());
					chartDetailsDTO.getBoardingPoint().setMinitues(stationDTO.getMinitues() + stationPointDTO.getMinitues());
					chartDetailsDTO.getBoardingPoint().setName(stationPointDTO.getName());
					chartDetailsDTO.getBoardingPoint().setNumber(stationPointDTO.getNumber());
				}
				else {
					continue;
				}
				Map<String, String> dataModel = new HashMap<String, String>();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("namespaceName", authDTO.getNamespace().getName());
				dataModel.put("fromStationName", WordUtils.capitalize(chartDetailsDTO.getFromStation().getName()));
				dataModel.put("toStationName", WordUtils.capitalize(chartDetailsDTO.getToStation().getName()));
				dataModel.put("travelDatetime", DateUtil.parseDateFormat(DateUtil.addMinituesToDate(new DateTime(chartDetailsDTO.getTripDate()), chartDetailsDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd MMM hh:mm a"));
				dataModel.put("passengerName", WordUtils.capitalize(StringUtil.substring(chartDetailsDTO.getPassengerName(), 30)));
				dataModel.put("ticketCode", chartDetailsDTO.getTicketCode());
				dataModel.put("serviceNumber", chartDetailsDTO.getServiceNumber());
				dataModel.put("mobileNumber", chartDetailsDTO.getPassengerMobile());
				dataModel.put("alternateMobile", chartDetailsDTO.getAlternateMobile());
				dataModel.put("busNumber", tripInfoDTO.getBusVehicle() != null ? tripInfoDTO.getBusVehicle().getRegistationNumber() : Text.HYPHEN);
				String seatNames = getSeatNames(tripChartDTO.getTicketDetailsList(), chartDetailsDTO.getTicketCode());
				dataModel.put("seats", seatNames);
				dataModel.put("boarding", StringUtil.substring(chartDetailsDTO.getBoardingPoint().getName(), 30));
				dataModel.put("boardingAddress", removeUnknownSymbol(StringUtil.substring(chartDetailsDTO.getBoardingPoint().getAddress(), 30)));
				dataModel.put("boardingContact", StringUtil.substring(chartDetailsDTO.getBoardingPoint().getNumber(), 30));
				dataModel.put("busContact", tripInfoDTO.getBusContactMobileNumber());
				dataModel.put("busContact2", StringUtil.isNotNull(tripInfoDTO.getDriverMobile2()) ? tripInfoDTO.getDriverMobile2() : Text.HYPHEN);
				dataModel.put("attenderMobile", StringUtil.isNotNull(tripInfoDTO.getAttenderMobile()) ? tripInfoDTO.getAttenderMobile() : Text.HYPHEN);
				dataModel.put("wappNumber", authDTO.getNamespace().getProfile().getWhatsappNumber());
				dataModel.put("wappChatURL", authDTO.getNamespace().getProfile().getWhatsappUrl());
				if (notificationType.getId() == NotificationTypeEM.GPS_TRACKING.getId()) {
					String trackUrl = "http://m.trackbus.in?p=" + chartDetailsDTO.getTicketCode() + "&z=" + ApplicationConfig.getServerZoneCode() + "&zone=" + ApplicationConfig.getServerZoneCode() + "&n=" + authDTO.getNamespaceCode();
					dataModel.put("trackBus", BitsShortURL.getUrlshortener(trackUrl, BitsShortURL.TYPE.TMP));
				}
				else {
					dataModel.put("trackBus", Text.HYPHEN);
				}
				if (StringUtil.isNotNull(authDTO.getNamespace().getProfile().getWhatsappUrl())) {
					String link = Constants.WHATSAPP_LINK + "?phone=" + "91" + authDTO.getNamespace().getProfile().getWhatsappNumber();
					String shortURL = BitsShortURL.getUrlshortener(link, BitsShortURL.TYPE.PER);
					dataModel.put("whatsappUrl", shortURL);
				}
				else {
					dataModel.put("whatsappUrl", Text.HYPHEN);
				}
				dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());

				// WHATSAPP
				boolean whatsappMsgStatus = false;
				if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), notificationType)) {
					whatsappMsgStatus = whatsappService.sendTripJourneyTracking(authDTO, dataModel, notificationType);
				}
				// SMS
				if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), notificationType) && !whatsappMsgStatus) {
					smsService.sendTripJourneyTrackingSMS(authDTO, dataModel, notificationType);

					/** Send Event Notification */
					sendCustomerEventNotification(authDTO, tripDTO, chartDetailsDTO.getFromStation(), chartDetailsDTO.getToStation(), EventNotificationEM.TRACKBUS_NOTIFICATION, chartDetailsDTO.getTicketCode(), chartDetailsDTO.getPassengerMobile());
				}
			}
			TRIP_INFO_LOGGER.info("Success Tracking SMS - {} {} size:{}", tripDTO.getCode(), notificationType.getCode(), uniqueList.size());

			/** Send Tracking SMS To Trip Notification Contact */
			if (authDTO.getNamespace().getProfile().getTripNotificationContact() != null && !authDTO.getNamespace().getProfile().getTripNotificationContact().isEmpty()) {
				sendTripJourneyTrackingNamespaceConfig(authDTO, tripDTO, tripInfoDTO, stageList, notificationType);
				TRIP_INFO_LOGGER.info("Success01 Tracking SMS - {} {} size:{}", tripDTO.getCode(), notificationType.getCode(), authDTO.getNamespace().getProfile().getTripNotificationContact().size());
			}

			// update Job Status
			if (!tripInfoDTO.checkNoficationTypeExists(notificationType)) {
				tripInfoDTO.addNoficationType(notificationType);
				tripDTO.setTripInfo(tripInfoDTO);
				tripService.updateTripJobStatus(authDTO, tripDTO);
			}
			// Save Trip Activity Log
			StringBuilder activityLog = new StringBuilder();
			activityLog.append((authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() ? "Job" : "Manual") + " - " + notificationType.getDescription());
			activityLog.append(" SMS Sent");
			addTripAuditLog(authDTO, tripDTO.getCode(), activityLog.toString());
		}
		catch (ServiceException e) {
			TRIP_INFO_LOGGER.error("Fail Tracking SMS - {} ECODE:{} {} attr: {}", tripDTO.getCode(), e.getErrorCode().getCode(), String.valueOf(e.getData()), authDTO.getAdditionalAttribute());
			if (e.getErrorCode().getCode().equals(ErrorCode.TRIP_CANCELLED.getCode())) {
				TRIP_INFO_LOGGER.error(DateUtil.NOW() + " Fail due to Trip Cancelled " + tripDTO.getCode());
				addTripAuditLog(authDTO, tripDTO.getCode(), "Fail due to Trip Cancelled/Yet Open");
			}
			else if (e.getErrorCode().getCode().equals(ErrorCode.TRIP_INFO_INVALID.getCode())) {
				TRIP_INFO_LOGGER.error(DateUtil.NOW() + " Fail due to No Vehicle Info " + tripDTO.getCode());
				addTripAuditLog(authDTO, tripDTO.getCode(), "Fail due to No Vehicle Info");
			}
			else if (e.getErrorCode().getCode().equals(ErrorCode.GPS_TRIP_TRACKING_NOT_ALLOWED.getCode())) {
				TRIP_INFO_LOGGER.error(DateUtil.NOW() + " Fail due to Already Sent " + tripDTO.getCode() + " - " + e.getErrorCode());
				addTripAuditLog(authDTO, tripDTO.getCode(), "Fail due to Already Sent");
			}
			else if (e.getErrorCode().getCode().equals(ErrorCode.GPS_TRIP_TRACKING_TIME_NOT_IN_PERIOD.getCode())) {
				TRIP_INFO_LOGGER.error(DateUtil.NOW() + " Send tracking time not with in time period " + tripDTO.getCode() + " - " + e.getErrorCode());
			}
			else if (e.getErrorCode().getCode().equals(ErrorCode.TRIP_NOTIFICATION_ALREADY_SENT.getCode())) {
				addTripAuditLog(authDTO, tripDTO.getCode(), "Already sent " + e.getData());
			}
			else {
				TRIP_INFO_LOGGER.error(DateUtil.NOW() + "Fail due to" + e.getErrorCode() + e.getData() + tripDTO.getCode());
				addTripAuditLog(authDTO, tripDTO.getCode(), "Fail due to " + e.getErrorCode() + e.getData());
			}
		}
		catch (Exception e) {
			System.out.println("ERROR TRACKBUS02" + authDTO.getNamespaceCode() + " - " + tripDTO.getCode() + " - " + e.getMessage());
			TRIP_INFO_LOGGER.error("Fail Tracking SMS - {}", tripDTO.getCode());
			addTripAuditLog(authDTO, tripDTO.getCode(), "Fail due to unknown reason");
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
	}

	private void sendTripJourneyTrackingNamespaceConfig(AuthDTO authDTO, TripDTO tripDTO, TripInfoDTO tripInfoDTO, List<StageStationDTO> stageList, NotificationTypeEM notificationType) {
		try {
			StageStationDTO originStageStation = BitsUtil.getOriginStageStation(stageList);
			StageStationDTO destinationStageStation = BitsUtil.getDestinationStageStation(stageList);
			tripDTO.setSchedule(scheduleService.getSchedule(authDTO, tripDTO.getSchedule()));

			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("namespaceName", authDTO.getNamespace().getName());
			dataModel.put("fromStationName", WordUtils.capitalize(originStageStation.getStation().getName()));
			dataModel.put("toStationName", WordUtils.capitalize(destinationStageStation.getStation().getName()));
			dataModel.put("travelDatetime", DateUtil.parseDateFormat(tripDTO.getTripDateTimeV2().format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd MMM hh:mm a"));
			dataModel.put("passengerName", WordUtils.capitalize(StringUtil.substring(authDTO.getNamespace().getName(), 30)));
			dataModel.put("ticketCode", Text.HYPHEN);
			dataModel.put("serviceNumber", tripDTO.getSchedule().getServiceNumber());
			dataModel.put("busNumber", tripInfoDTO.getBusVehicle() != null ? tripInfoDTO.getBusVehicle().getRegistationNumber() : Text.HYPHEN);
			dataModel.put("busContact", tripInfoDTO.getBusContactMobileNumber());
			dataModel.put("busContact2", StringUtil.isNotNull(tripInfoDTO.getDriverMobile2()) ? tripInfoDTO.getDriverMobile2() : Text.HYPHEN);
			dataModel.put("attenderMobile", StringUtil.isNotNull(tripInfoDTO.getAttenderMobile()) ? tripInfoDTO.getAttenderMobile() : Text.HYPHEN);
			dataModel.put("wappNumber", authDTO.getNamespace().getProfile().getWhatsappNumber());
			dataModel.put("wappChatURL", authDTO.getNamespace().getProfile().getWhatsappUrl());
			if (notificationType.getId() == NotificationTypeEM.GPS_TRACKING.getId()) {
				String trackUrl = "http://m.trackbus.in?t=" + tripDTO.getCode() + "&z=" + ApplicationConfig.getServerZoneCode() + "&zone=" + ApplicationConfig.getServerZoneCode() + "&n=" + authDTO.getNamespaceCode();
				dataModel.put("trackBus", BitsShortURL.getUrlshortener(trackUrl, BitsShortURL.TYPE.TMP));
			}
			else {
				dataModel.put("trackBus", Text.HYPHEN);
			}
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());

			for (String mobile : authDTO.getNamespace().getProfile().getTripNotificationContact()) {
				dataModel.put("mobileNumber", mobile);

				// WHATSAPP
				boolean whatsappMsgStatus = false;
				if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), notificationType)) {
					whatsappMsgStatus = whatsappService.sendTripJourneyTracking(authDTO, dataModel, notificationType);
				}
				// SMS
				if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), notificationType) && !whatsappMsgStatus) {
					smsService.sendTripJourneyTrackingSMS(authDTO, dataModel, notificationType);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
	}

	private List<String> getVendorList(AuthDTO authDTO) {
		Map<String, String> partnerMap = new HashMap<>();
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTripInfo()) {
			userDTO = userService.getUser(authDTO, userDTO);
			if (userDTO.getUserTags() == null || userDTO.getUserTags().isEmpty()) {
				continue;
			}
			for (UserTagEM userTag : userDTO.getUserTags()) {
				partnerMap.put(userTag.getCode(), userTag.getCode());
			}
		}
		return new ArrayList<String>(partnerMap.values());
	}

	@Async
	public void sendTripJourneyTrackingSMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDTO tripDTO = ticketDTO.getTripDTO();
		// Sorting
		TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, tripDTO);
		if (tripInfoDTO != null && tripInfoDTO.getBusVehicle() != null) {
			NotificationTypeEM notificationType = NotificationTypeEM.HAPPY_JOURNEY;
			if (tripInfoDTO.getBusVehicle() != null && (tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.EZEEGPS.getId() || ((tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.JTECH.getId() || tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.SENSEL.getId() || tripInfoDTO.getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.PLAYGPS.getId())) && tripInfoDTO.getBusVehicle().getDeviceVendor().getNamespace().contains(authDTO.getNamespaceCode()))) {
				notificationType = NotificationTypeEM.GPS_TRACKING;
			}
			try {
				Map<String, String> dataModel = new HashMap<String, String>();
				dataModel.put("namespaceCode", authDTO.getNamespaceCode());
				dataModel.put("namespaceName", authDTO.getNamespace().getName());
				dataModel.put("fromStationName", WordUtils.capitalize(ticketDTO.getFromStation().getName()));
				dataModel.put("toStationName", WordUtils.capitalize(ticketDTO.getToStation().getName()));
				dataModel.put("travelDatetime", DateUtil.parseDateFormat(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd MMM hh:mm a"));
				dataModel.put("passengerName", WordUtils.capitalize(StringUtil.substring(ticketDTO.getPassengerName(), 30)));
				dataModel.put("ticketCode", ticketDTO.getCode());
				dataModel.put("serviceNumber", ticketDTO.getServiceNo());
				dataModel.put("seatName", ticketDTO.getSeatNames());
				dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());
				dataModel.put("busNumber", tripInfoDTO.getBusVehicle() != null ? tripInfoDTO.getBusVehicle().getRegistationNumber() : "-");
				dataModel.put("busContact", tripInfoDTO.getBusContactMobileNumber());
				dataModel.put("busContact2", StringUtil.isNotNull(tripInfoDTO.getDriverMobile2()) ? tripInfoDTO.getDriverMobile2() : Text.EMPTY);
				dataModel.put("attenderMobile", StringUtil.isNotNull(tripInfoDTO.getAttenderMobile()) ? tripInfoDTO.getAttenderMobile() : Text.EMPTY);
				dataModel.put("wappNumber", authDTO.getNamespace().getProfile().getWhatsappNumber());
				dataModel.put("wappChatURL", authDTO.getNamespace().getProfile().getWhatsappUrl());
				if (notificationType.getId() == NotificationTypeEM.GPS_TRACKING.getId()) {
					String trackUrl = "http://m.trackbus.in?p=" + ticketDTO.getCode() + "&z=" + ApplicationConfig.getServerZoneCode() + "&zone=" + ApplicationConfig.getServerZoneCode() + "&n=" + authDTO.getNamespaceCode();
					dataModel.put("trackBus", BitsShortURL.getUrlshortener(trackUrl, BitsShortURL.TYPE.TMP));
				}
				else {
					dataModel.put("trackBus", Text.HYPHEN);
				}
				dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());

				// Whatsapp
				boolean whatsappMsgStatus = false;
				if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), notificationType)) {
					whatsappMsgStatus = whatsappService.sendTripJourneyTracking(authDTO, dataModel, notificationType);
				}
				// SMS
				if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), notificationType) && !whatsappMsgStatus || authDTO.getAdditionalAttribute().get(Text.TICKET_AFTER_TRIP_TIME) != null) {
					smsService.sendTripJourneyTrackingSMS(authDTO, dataModel, notificationType);
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void addTripAuditLog(AuthDTO authDTO, String tripCode, String activityLog) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.addAuditLog(authDTO, tripCode, "trip", "Trip Notification", activityLog);
	}

	public static synchronized void validateFreshRequest(TripDTO tripDTO) {
		String key = "RESEND_HP_GT" + tripDTO.getCode();

		if (EhcacheManager.getFreshRequestEhCache().get(key) == null) {
			Element element = new Element(key, tripDTO.getCode());
			EhcacheManager.getFreshRequestEhCache().put(element);
		}
		else {
			throw new ServiceException(ErrorCode.PARALLEL_SAME_TRANSACTION_OCCUR);
		}
	}

	@Override
	public void sendTicketAfterTripTimeNotify(AuthDTO authDTO, TicketDTO ticketDTO, String boardingMobileNumber) {
		Map<String, String> additionalDetails = new HashMap<>();

		String auditLog = getTicketAfterTripTimeDetails(authDTO, ticketDTO, boardingMobileNumber, additionalDetails);

		if (StringUtil.isNotNull(additionalDetails.get("MOBILE_NUMBER"))) {
			String response = smsService.sendTicketAfterTripTimeNotify(authDTO, ticketDTO, additionalDetails);
			if (StringUtil.isNotNull(response)) {
				tripService.SaveTripHistory(authDTO, ticketDTO.getTripDTO(), "Ticket After Trip Time Notify SMS Sent", auditLog);
			}
		}
	}

	private String getTicketAfterTripTimeDetails(AuthDTO authDTO, TicketDTO ticketDTO, String boardingMobileNumber, Map<String, String> additionalDetails) {
		StringBuilder mobileNumber = new StringBuilder();
		StringBuilder auditLog = new StringBuilder();

		List<String> tickAfterTripTimeNotifyContacts = authDTO.getNamespace().getProfile().getTicketAfterTripTimeNotificationContact();

		if (tickAfterTripTimeNotifyContacts != null && !tickAfterTripTimeNotifyContacts.isEmpty()) {
			for (String mobile : tickAfterTripTimeNotifyContacts) {
				if (mobileNumber.length() > 0) {
					mobileNumber.append(Text.COMMA);
				}
				mobileNumber.append(mobile);
			}

			auditLog.append(ticketDTO.getCode());
			auditLog.append(Text.COLON);
			auditLog.append(mobileNumber);
		}

		TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, ticketDTO.getTripDTO());
		if (tripInfoDTO != null) {
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.VEHICLE_NUMBER_NOTIFY_FLAG)) {
				if (StringUtil.isNotNull(mobileNumber.toString())) {
					mobileNumber.append(Text.COMMA);
				}
				mobileNumber.append(tripInfoDTO.getDriverMobile());
				auditLog.append(ticketDTO.getCode());
				auditLog.append(Text.COLON);
				auditLog.append(tripInfoDTO.getDriverName());
				auditLog.append(Text.HYPHEN);
				auditLog.append(tripInfoDTO.getDriverMobile());
			}
		}
		if (StringUtil.isNotNull(boardingMobileNumber)) {
			mobileNumber.append(Text.COMMA);
			mobileNumber.append(boardingMobileNumber);
			auditLog.append(Text.COMMA);
			auditLog.append(boardingMobileNumber);
		}
		additionalDetails.put("VEHICLE_NUMBER", tripInfoDTO != null && tripInfoDTO.getBusVehicle() != null ? tripInfoDTO.getBusVehicle().getRegistationNumber() : null);
		additionalDetails.put("MOBILE_NUMBER", mobileNumber.toString());

		return boardingMobileNumber;
	}

	@Override
	public void sendTicketAfterTripTimeCancelNotify(AuthDTO authDTO, TicketDTO ticketDTO, String boardingMobileNumber) {
		Map<String, String> additionalDetails = new HashMap<>();
		String auditLog = getTicketAfterTripTimeDetails(authDTO, ticketDTO, boardingMobileNumber, additionalDetails);
		if (StringUtil.isNotNull(additionalDetails.get("MOBILE_NUMBER"))) {
			String response = smsService.sendTicketAfterTripTimeCancelNotify(authDTO, ticketDTO, additionalDetails);
			if (StringUtil.isNotNull(response)) {
				tripService.SaveTripHistory(authDTO, ticketDTO.getTripDTO(), "Ticket After Trip Time Cancel Notify SMS Sent", auditLog);
			}
		}
	}

	@Override
	public void sendTicketUpdateSMS(AuthDTO authDTO, TicketDTO ticketDTO) {
		smsService.sendTicketUpdateSMS(authDTO, ticketDTO);
	}

	private List<TripChartDetailsDTO> getUniqueTicketList(List<TripChartDetailsDTO> nonUniqueAccountList) {
		Map<String, TripChartDetailsDTO> selectedTickets = new HashMap<String, TripChartDetailsDTO>();
		for (TripChartDetailsDTO ticketDetailsDTO : nonUniqueAccountList) {
			selectedTickets.put(ticketDetailsDTO.getTicketCode(), ticketDetailsDTO);
		}

		return new ArrayList<TripChartDetailsDTO>(selectedTickets.values());
	}

	private String getSeatNames(List<TripChartDetailsDTO> nonUniqueAccountList, String ticketCode) {
		StringBuilder ticketBuilder = new StringBuilder();
		for (TripChartDetailsDTO ticketDetailsDTO : nonUniqueAccountList) {
			if (ticketCode.equals(ticketDetailsDTO.getTicketCode())) {
				if (!ticketBuilder.toString().isEmpty()) {
					ticketBuilder.append(",");
				}
				ticketBuilder.append(ticketDetailsDTO.getSeatName());
			}
		}
		return ticketBuilder.toString();
	}

	@Override
	public void sendFaliureBookSMS(AuthDTO authDTO, TicketDTO ticket) {
		JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);

		if (jsonObject != null && jsonObject.has("notificationSMS") && jsonObject.getJSONObject("notificationSMS") != null) {
			JSONObject notificationJSON = jsonObject.getJSONObject("notificationSMS");

			StringBuilder mobileNumber = new StringBuilder();
			if (notificationJSON.has("failureBook") && notificationJSON.getJSONArray("failureBook") != null && !notificationJSON.getJSONArray("failureBook").isEmpty()) {
				JSONArray numberArray = notificationJSON.getJSONArray("failureBook");
				for (int i = 0; i < numberArray.size(); i++) {
					mobileNumber.append(numberArray.get(i));
					if (i < numberArray.size() - 1) {
						mobileNumber.append(",");
					}
				}
				if (StringUtil.isNotNull(mobileNumber.toString())) {
					smsService.sendFaliureBookSMS(authDTO, ticket, mobileNumber.toString());
				}
			}
		}
	}

	@Override
	public void sendScheduleUpdateEmail(AuthDTO authDTO, List<Map<String, String>> finalScheduleChanges, String referenceCode) {
		try {
			JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);

			List<String> toEmailIds = new ArrayList<>();
			List<String> ccEmailIds = new ArrayList<>();
			if (jsonObject != null && jsonObject.has("notificationEmail") && jsonObject.getJSONObject("notificationEmail") != null) {
				JSONObject notificationJSON = jsonObject.getJSONObject("notificationEmail");
				if (notificationJSON.has("scheduleNotificationTo") && StringUtil.isNotNull(notificationJSON.getString("scheduleNotificationTo"))) {
					String emailIds = notificationJSON.getString("scheduleNotificationTo");
					toEmailIds.addAll(Arrays.asList(emailIds.split(Text.COMMA)));
				}
				if (notificationJSON != null && notificationJSON.has("scheduleNotificationCc") && StringUtil.isNotNull(notificationJSON.getString("scheduleNotificationCc"))) {
					String emailIds = notificationJSON.getString("scheduleNotificationCc");
					ccEmailIds.addAll(Arrays.asList(emailIds.split(Text.COMMA)));
				}
			}
			if (toEmailIds.isEmpty()) {
				toEmailIds.add("ezeebus@ezeeinfosolutions.com");
			}
			if (!toEmailIds.isEmpty()) {
				emailService.sendScheduleUpdateEmail(authDTO, finalScheduleChanges, referenceCode, toEmailIds, ccEmailIds);
			}
		}
		catch (ServiceException e) {
			System.out.println("NE001 " + authDTO.getNamespaceCode() + " send Schedule Update Email Fail");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void ticketAfterTripTimeFCM(AuthDTO authDTO, TripDTO tripDTO, TicketDTO ticketDTO) {
		try {
			TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, tripDTO);
			if (tripInfoDTO == null || tripInfoDTO.getBusVehicle() == null) {
				throw new ServiceException(ErrorCode.TRIP_INFO_INVALID);
			}

			List<TabletDTO> tabletList = tabletService.getTablet(authDTO, tripInfoDTO.getBusVehicle());
			for (TabletDTO tabletDTO : tabletList) {
				if (tabletDTO.getUser().getId() == 0) {
					continue;
				}

				List<AppStoreDetailsDTO> userAppDetailsList = userService.getAppStoreDetails(authDTO, tabletDTO.getUser());
				if (!userAppDetailsList.isEmpty()) {
					String title = "New Seat Booking [" + ticketDTO.getSeatCodeNames() + "]";

					JSONObject data = new JSONObject();
					data.put("trc", ticketDTO.getTripDTO().getCode());
					data.put("tc", ticketDTO.getCode());
					data.put("bpc", ticketDTO.getBoardingPoint().getCode());
					data.put("dpc", ticketDTO.getDroppingPoint().getCode());
					data.put("fsc", ticketDTO.getFromStation().getCode());
					data.put("tsc", ticketDTO.getToStation().getCode());
					data.put("m", ticketDTO.getPassengerMobile());
					data.put("t", ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
					data.put("buc", ticketDTO.getTicketUser().getCode());
					data.put("trm", ticketDTO.getRemarks());
					data.put("trm", Text.EMPTY);
					data.put("bun", ticketDTO.getTicketUser().getName());
					data.put("pt", ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() ? "To Pay" : "Online");
					data.put("tf", ticketDTO.getTicketFareWithAddons());
					JSONArray pdetails = new JSONArray();
					for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
						JSONObject object = new JSONObject();
						object.put("n", ticketDetails.getPassengerName());
						object.put("a", ticketDetails.getPassengerAge());
						object.put("g", ticketDetails.getSeatGendar().getCode());
						object.put("sc", ticketDetails.getSeatCode());
						object.put("sn", ticketDetails.getSeatName());
						object.put("ssc", ticketDetails.getTicketStatus().getCode());
						object.put("trsc", ticketDetails.getTravelStatus().getCode());
						object.put("sf", ticketDetails.getSeatFare());
						object.put("ad", ticketDTO.getAddonsValue(ticketDetails));
						object.put("tax", ticketDetails.getAcBusTax());
						pdetails.add(object);
					}
					data.put("td", pdetails);

					JSONObject masterData = new JSONObject();
					masterData.put("data", data);

					String content = ticketDTO.getTripDTO().getCode();
					for (AppStoreDetailsDTO appStore : userAppDetailsList) {
						fcmService.pushNotification(authDTO, FCMServerKeyEM.BUS_BUDDY.getCode(), appStore.getGcmToken(), title, content, Text.EMPTY, masterData);
					}
				}
			}
		}
		catch (ServiceException e) {
			System.out.println("FCMER01" + authDTO.getNamespaceCode() + Text.HYPHEN + tripDTO.getCode() + Text.HYPHEN + ticketDTO.getCode() + Text.HYPHEN + e.getErrorCode().getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void sendOverallTripSummarySMS(AuthDTO authDTO, Map<String, Object> dataModel) {
		JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);
		String[] numbers = null;
		if (jsonObject != null && jsonObject.has("notificationSMS") && jsonObject.getJSONObject("notificationSMS").has("DailySaleSummary")) {
			JSONArray numberArray = jsonObject.getJSONObject("notificationSMS").getJSONArray("DailySaleSummary");
			numbers = new String[numberArray.size()];
			for (int i = 0; i < numberArray.size(); i++) {
				numbers[i] = numberArray.get(i).toString();
			}
		}

		if (numbers != null) {
			smsService.sendOverallTripSummarySMS(authDTO, dataModel, StringUtils.join(Text.COMMA, numbers));
		}
	}

	@Override
	public String busTypeChangeNotification(AuthDTO authDTO, String ticketCode, String reason, String BusTypeName, String contactNumber) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

		if (ticketDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		Future<SmsResponse> futureTask = smsService.busTypeChangeNotification(authDTO, ticketDTO, reason, BusTypeName, contactNumber);
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

			ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: " + reason + ", Bus Type: " + BusTypeName + ", Support Number: " + contactNumber);
			SaveTripHistory(authDTO, ticketDTO);
		}
		return message;
	}

	@Override
	public void sendVanPickupSMS(AuthDTO authDTO, TripVanInfoDTO tripInfo, List<String> ticketCodes) {
		tripInfo = tripVanInfoService.getTripVanInfoByCode(authDTO, tripInfo);

		for (String ticketCode : ticketCodes) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);

			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			// Send SMS
			smsService.sendTripVanPickupSMS(authDTO, ticketDTO, tripInfo);
		}

		// Update Notification Status
		tripInfo.setNotificationType(NotificationTypeEM.HAPPY_JOURNEY);
		tripVanInfoService.updateNotitficationStatus(authDTO, tripInfo);
	}

	@Async
	public void sendBusBuddyAlert(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDTO tripDTO = new TripDTO();
		try {
			tripDTO = tripService.getTripDTO(authDTO, ticketDTO.getTripDTO());

			List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
			DateTime originDateTime = BitsUtil.getOriginStationPointDateTime(stageList, tripDTO.getTripDate());

			int tripMinuties = DateUtil.getMinutiesDifferent(originDateTime, DateUtil.NOW());

			if (tripMinuties > -30) {
				TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, tripDTO);
				if (tripInfoDTO == null || tripInfoDTO.getBusVehicle() == null) {
					throw new ServiceException(ErrorCode.TRIP_INFO_INVALID);
				}

				List<TabletDTO> tabletlist = tabletService.getTablet(authDTO, tripInfoDTO.getBusVehicle());
				for (TabletDTO tabletDTO : tabletlist) {
					String key = Text.BUS_BUDDY_SYNC + Text.UNDER_SCORE + authDTO.getNamespaceCode() + Text.UNDER_SCORE + tabletDTO.getCode();
					Element element = EhcacheManager.getBusBuddyEhCache().get(key);
					if (element != null) {
						DateTime syncTime = DateUtil.getDateTime((String) element.getObjectValue());
						System.out.println("BBSMS01 " + tabletDTO.getCode() + syncTime + " - " + DateUtil.NOW() + " : " + DateUtil.getMinutiesDifferent(syncTime, DateUtil.NOW()));
						if (DateUtil.getMinutiesDifferent(syncTime, DateUtil.NOW()) < 5) {
							System.out.println("BBSMS02 exception: " + tabletDTO.getCode());
							continue;
						}
					}
					if (tabletDTO.getMobileVerifyFlag() != Numeric.ONE_INT) {
						System.out.println("BBSMS03 exception: Mobile not verified" + tabletDTO.getCode());
						continue;
					}
					Map<String, String> dataModel = new HashMap<>();
					dataModel.put("tripCode", ticketDTO.getTripDTO().getCode());
					dataModel.put("ticketCode", ticketDTO.getCode());
					dataModel.put("seatNames", ticketDTO.getSeatNames());
					dataModel.put("passengerGender", ticketDTO.getTicketDetails().get(0).getSeatGendar().getCode().toLowerCase());
					dataModel.put("passengerAge", String.valueOf(ticketDTO.getTicketDetails().get(0).getPassengerAge()));
					dataModel.put("passengerName", ticketDTO.getTicketDetails().get(0).getPassengerName());
					dataModel.put("seatFare", String.valueOf(ticketDTO.getTicketFareWithAddons().intValue()));
					dataModel.put("passengerMobile", ticketDTO.getPassengerMobile());
					dataModel.put("boardingPointCode", ticketDTO.getBoardingPoint().getCode());
					dataModel.put("droppingPointCode", ticketDTO.getDroppingPoint().getCode());
					dataModel.put("fromStationCode", ticketDTO.getFromStation().getCode());
					dataModel.put("toStationCode", ticketDTO.getToStation().getCode());
					dataModel.put("travelStatus", String.valueOf(ticketDTO.getTicketDetails().get(0).getTravelStatus().getId()));
					dataModel.put("ticketStatus", ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() ? "pb" : "ob");
					dataModel.put("bookedUser", userService.getUser(authDTO, ticketDTO.getTicketUser()).getName());
					System.out.println("BBSMS04 sent SMS: " + tabletDTO.getCode() + tabletDTO.getMobileNumber() + "\n" + dataModel.toString());
					smsService.sendSMS(authDTO, tabletDTO.getMobileNumber(), dataModel, NotificationTypeEM.BUS_BUDDY_ALERT);
				}
			}
		}
		catch (ServiceException e) {
			System.out.println("BBER01" + authDTO.getNamespaceCode() + Text.HYPHEN + tripDTO.getCode() + Text.HYPHEN + ticketDTO.getCode() + Text.HYPHEN + e.getErrorCode().getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendPendingOrderCancelSMS(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(preTransactionDTO.getOrderCode());
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

		smsService.sendPendingOrderCancelSMS(authDTO, ticketDTO);
	}

	@Override
	public void sendDenialTicketEmail(AuthDTO authDTO, TicketDTO ticketDTO) {
		emailService.sendDenialTicketEmail(authDTO, ticketDTO);
	}

	@Override
	public void sendEzeebotFeedbackMail(Map<String, String> dataModel) {
		emailService.sendEzeebotFeedbackMail(dataModel);
	}

	@Override
	public void sendTripSms(AuthDTO authDTO, Map<String, String> notificationDetails) {
		List<String> tickets = Arrays.asList(notificationDetails.get("TICKETS").split(Text.COMMA));
		if (tickets.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		if (StringUtil.isNull(notificationDetails.get("SMS_TYPE"))) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}

		switch (notificationDetails.get("SMS_TYPE")) {
			case "TRIP_CANCEL":
				for (String ticketCode : tickets) {
					tripCancelNotification(authDTO, ticketCode, notificationDetails.get("REASON"), notificationDetails.get("SUPPORT_NUMBER"));
				}
				break;

			case "TRIP_DELAY":
				for (String ticketCode : tickets) {
					tripDelayNotification(authDTO, ticketCode, notificationDetails.get("REASON"), notificationDetails.get("DELAY_TIME"), notificationDetails.get("SUPPORT_NUMBER"));
				}
				break;

			case "TRIP_EARLY":
				for (String ticketCode : tickets) {
					tripEarlyNotification(authDTO, ticketCode, notificationDetails.get("REASON"), notificationDetails.get("DELAY_TIME"), notificationDetails.get("SUPPORT_NUMBER"));
				}
				break;

			case "STATION_POINT_CHANGE":
				for (String ticketCode : tickets) {
					tripStationPointChangeNotification(authDTO, ticketCode, notificationDetails.get("REASON"), notificationDetails.get("STATION_POINT_NAME"), notificationDetails.get("STATION_POINT_TIME"), notificationDetails.get("SUPPORT_NUMBER"));
				}
				break;

			case "RESEND_TRACKING":
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(notificationDetails.get("TRIP_CODE"));
				authDTO.getAdditionalAttribute().put("OVERRIDE", Numeric.ONE);

				List<TicketDetailsDTO> ticketDetailsDTOList = new ArrayList<TicketDetailsDTO>();

				for (String ticketCode : tickets) {
					if (StringUtil.isNull(ticketCode)) {
						continue;
					}
					TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
					ticketDetailsDTO.setTicketCode(ticketCode);
					ticketDetailsDTOList.add(ticketDetailsDTO);
				}
				if (ticketDetailsDTOList.isEmpty()) {
					throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
				}
				tripDTO.setTicketDetailsList(ticketDetailsDTOList);

				sendTripJourneyTrackingSMS(authDTO, tripDTO);
				break;

			case "COVID_EPASS":
				String registrationNumber = getRegistrationNumber(authDTO, notificationDetails.get("TRIP_CODE"));
				for (String ticketCode : tickets) {
					tripCovidEpassNotification(authDTO, ticketCode, registrationNumber, notificationDetails.get("SUPPORT_NUMBER"));
				}
				break;

			case "APOLOGY_NOTIFICATION":
				NotificationTemplateConfigDTO templateConfig = getNotificationConfigTemplate(authDTO, notificationDetails.get("TEMPLATE_CODE"));
				for (String ticketCode : tickets) {
					sendApologyNotification(authDTO, ticketCode, templateConfig);
				}
				break;

			default:
				break;
		}
		// push flutter notification
		notificationPushService.pushTripNotification(authDTO, notificationDetails);
	}

	private List<TripChartDetailsDTO> getUniqueTicketList(List<TicketDetailsDTO> ticketDetails, List<TripChartDetailsDTO> nonUniqueAccountList) {
		Map<String, String> selectedTickets = new HashMap<String, String>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			selectedTickets.put(ticketDetailsDTO.getTicketCode(), ticketDetailsDTO.getTicketCode());
		}

		Map<String, TripChartDetailsDTO> uniqueAccountsMapList = new HashMap<String, TripChartDetailsDTO>();
		if (nonUniqueAccountList != null && !nonUniqueAccountList.isEmpty()) {
			for (TripChartDetailsDTO nprDto : nonUniqueAccountList) {
				if (selectedTickets.isEmpty() || selectedTickets.get(nprDto.getTicketCode()) != null) {
					uniqueAccountsMapList.put(nprDto.getTicketCode(), nprDto);
				}
			}
		}
		return new ArrayList<TripChartDetailsDTO>(uniqueAccountsMapList.values());
	}

	@Override
	public List<NotificationSubscriptionDTO> getAllSubscription(AuthDTO authDTO) {

		NotificationDAO dao = new NotificationDAO();
		List<NotificationSubscriptionDTO> list = dao.getAllAlertSubscriptions(authDTO);
		for (NotificationSubscriptionDTO subscriptionsDTO : list) {
			for (UserDTO userDTO : subscriptionsDTO.getUserList()) {
				UserDTO user = userService.getUser(authDTO, userDTO);
				userDTO.setCode(user.getCode());
				userDTO.setName(user.getName());
			}
			for (GroupDTO groupDTO : subscriptionsDTO.getGroupList()) {
				GroupDTO group = groupService.getGroup(authDTO, groupDTO);
				groupDTO.setCode(group.getCode());
				groupDTO.setName(group.getName());
			}
		}
		return list;
	}

	@Override
	public NotificationSubscriptionDTO updateSubscription(AuthDTO authDTO, NotificationSubscriptionDTO subscriptionsDTO) {

		NotificationDAO dao = new NotificationDAO();
		for (UserDTO userDTO : subscriptionsDTO.getUserList()) {
			UserDTO user = userService.getUser(authDTO, userDTO);
			userDTO.setId(user.getId());
		}
		for (GroupDTO groupDTO : subscriptionsDTO.getGroupList()) {
			GroupDTO group = groupService.getGroup(authDTO, groupDTO);
			groupDTO.setId(group.getId());
		}
		dao.updateNotificationSubscription(authDTO, subscriptionsDTO);
		// clear subscription cache
		removeSubscriptionCache(authDTO, subscriptionsDTO.getSubscriptionType());
		return subscriptionsDTO;
	}

	@Override
	public NotificationSubscriptionDTO updateUserSubscription(AuthDTO authDTO, List<NotificationSubscriptionDTO> subscriptionsList) {
		NotificationDAO dao = new NotificationDAO();
		UserDTO userDTO = authDTO.getUser();
		for (NotificationSubscriptionDTO subscriptionsDTO : subscriptionsList) {
			List<NotificationSubscriptionDTO> existSubscriptionList = getSubscriptionByType(authDTO, subscriptionsDTO.getSubscriptionType());
			for (NotificationSubscriptionDTO existSubscription : existSubscriptionList) {
				if (subscriptionsDTO.getActiveFlag() == Numeric.ONE_INT) {
					subscriptionsDTO.setUserList(addRemoveUser(userDTO, existSubscription.getUserList(), subscriptionsDTO.getActiveFlag()));
				}
				else if (subscriptionsDTO.getActiveFlag() == Numeric.ZERO_INT && BitsUtil.isUserExists(existSubscription.getUserList(), userDTO) != null) {
					subscriptionsDTO.setUserList(addRemoveUser(userDTO, existSubscription.getUserList(), subscriptionsDTO.getActiveFlag()));
				}
				subscriptionsDTO.setCode(existSubscription.getCode());
				subscriptionsDTO.setActiveFlag(Numeric.ONE_INT);
			}
		}
		dao.updateUserNotificationSubscription(authDTO, subscriptionsList);
		// clear subscription cache
		for (NotificationSubscriptionDTO subscriptionsDTO : subscriptionsList) {
			removeSubscriptionCache(authDTO, subscriptionsDTO.getSubscriptionType());
		}
		return null;
	}

	public List<NotificationSubscriptionDTO> getUserSubscription(AuthDTO authDTO) {
		NotificationDAO dao = new NotificationDAO();
		Map<String, NotificationSubscriptionDTO> userSubscriptionMap = dao.getUserSubscriptions(authDTO);
		for (NotificationSubscriptionTypeEM subscriptionType : NotificationSubscriptionTypeEM.values()) {
			if (subscriptionType.getLevel() != Numeric.ONE_INT || userSubscriptionMap.get(subscriptionType.getCode()) != null) {
				continue;
			}
			NotificationSubscriptionDTO subscriptionDTO = new NotificationSubscriptionDTO();
			subscriptionDTO.setSubscriptionType(subscriptionType);
			subscriptionDTO.setActiveFlag(Numeric.ZERO_INT);

			userSubscriptionMap.put(subscriptionType.getCode(), subscriptionDTO);
		}
		return new ArrayList<NotificationSubscriptionDTO>(userSubscriptionMap.values());
	}

	private List<UserDTO> addRemoveUser(UserDTO userDTO, List<UserDTO> list, int activeFlag) {
		Map<Integer, UserDTO> userMap = Maps.newHashMap();
		for (UserDTO existUser : list) {
			userMap.put(existUser.getId(), existUser);
		}
		if (activeFlag == Numeric.ONE_INT) {
			userMap.put(userDTO.getId(), userDTO);
		}
		else {
			userMap.remove(userDTO.getId());
		}
		return new ArrayList<UserDTO>(userMap.values());
	}

	@Override
	public List<NotificationSubscriptionDTO> getSubscriptionByType(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionsType) {
		NotificationDAO dao = new NotificationDAO();
		return dao.getSubscriptionsByType(authDTO, subscriptionsType);
	}

	@Override
	public void revokeAuthenticateSubscription(AuthDTO authDTO, AppStoreDetailsDTO appStoreDetails) {
		UserDTO userDTO = authDTO.getUser();
		appStoreDetails.setActiveFlag(2);
		userDTO.setAppStoreDetails(appStoreDetails);
		UserDAO userDAO = new UserDAO();
		userDAO.appStoreUpdate(authDTO, userDTO);
		// clear appstore cache
		removeAppStoreDetailsCache(authDTO, userDTO);
	}

	@Override
	public List<AppStoreDetailsDTO> getAuthenticateSubscriptionDetails(AuthDTO authDTO, UserDTO user, DeviceMediumEM deviceMedium) {
		UserDAO userDAO = new UserDAO();
		return userDAO.getAppStoreDetailsV2(authDTO, user, deviceMedium);
	}

	public List<Map<String, String>> notificationSubscriptionReport(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionTypeEM) {
		List<Map<String, String>> subscriptionDetails = new ArrayList<>();
		Map<String, NotificationSubscriptionDTO> subscriptionMap = Maps.newHashMap();
		NotificationDAO dao = new NotificationDAO();
		List<NotificationSubscriptionDTO> subscriptionList = dao.getAllAlertSubscriptions(authDTO);
		for (NotificationSubscriptionDTO subscriptionDTO : subscriptionList) {
			if (subscriptionTypeEM != null && subscriptionTypeEM.getId() != subscriptionDTO.getSubscriptionType().getId()) {
				continue;
			}
			for (NotificationMediumEM medium : subscriptionDTO.getNotificationMediumList()) {
				String subscriptionTypeCode = subscriptionDTO.getSubscriptionType().getCode() + Text.UNDER_SCORE + medium.getCode();
				if (subscriptionMap.get(subscriptionTypeCode) != null) {
					NotificationSubscriptionDTO existSubscriptionDTO = subscriptionMap.get(subscriptionTypeCode);
					existSubscriptionDTO.getGroupList().addAll(subscriptionDTO.getGroupList());
					existSubscriptionDTO.getUserList().addAll(subscriptionDTO.getUserList());
					subscriptionMap.put(subscriptionTypeCode, existSubscriptionDTO);
				}
				else {
					subscriptionMap.put(subscriptionTypeCode, subscriptionDTO);
				}
			}
		}
		for (Map.Entry<String, NotificationSubscriptionDTO> entrySet : subscriptionMap.entrySet()) {
			NotificationSubscriptionTypeEM subcriptionType = NotificationSubscriptionTypeEM.getSubscriptionTypeEM(entrySet.getKey().split("\\_")[0]);
			NotificationMediumEM medium = NotificationMediumEM.getNotificationMediumEM(entrySet.getKey().split("\\_")[1]);
			NotificationSubscriptionDTO subscriptionDTO = entrySet.getValue();

			List<UserDTO> userList = getUsers(authDTO, subscriptionDTO);

			for (UserDTO userDTO : userList) {
				Map<String, String> userSubscriptionMap = Maps.newHashMap();
				userSubscriptionMap.put("notification_type_code", subcriptionType.getCode());
				userSubscriptionMap.put("notification_type_name", subcriptionType.getName());
				userSubscriptionMap.put("notification_medium_code", medium.getCode());
				userSubscriptionMap.put("notification_medium_name", medium.getName());
				userSubscriptionMap.put("user_code", userDTO.getCode());
				userSubscriptionMap.put("user_name", userDTO.getName());
				userSubscriptionMap.put("group_code", userDTO.getGroup().getCode());
				userSubscriptionMap.put("group_name", userDTO.getGroup().getName());
				subscriptionDetails.add(userSubscriptionMap);
			}

		}
		return subscriptionDetails;
	}

	private List<UserDTO> getUsers(AuthDTO authDTO, NotificationSubscriptionDTO subscriptionDTO) {
		List<UserDTO> finalUserList = new ArrayList<UserDTO>();
		for (GroupDTO groupDTO : subscriptionDTO.getGroupList()) {
			groupDTO = getGroupDTO(authDTO, groupDTO);
			List<UserDTO> userList = groupService.getUserDTO(authDTO, groupDTO);
			for (UserDTO userDTO : userList) {
				userDTO.setGroup(groupDTO);
			}
			finalUserList.addAll(userList);
		}
		for (UserDTO userDTO : subscriptionDTO.getUserList()) {
			UserDTO user = userService.getUser(authDTO, userDTO);
			user.setGroup(getGroupDTOById(authDTO, user.getGroup()));
			finalUserList.add(user);
		}
		return finalUserList.stream().filter(StreamUtil.distinctByKey(p -> p.getCode())).filter(p -> p.getActiveFlag() == 1).collect(Collectors.toList());
	}

	@Override
	public void checkAndUpdateNotificationStatus(AuthDTO authDTO, NotificationDTO notification, Map<String, String> responseParam) {
		smsService.getNotificationStatus(authDTO, notification, responseParam);

	}

	public void sendWhatsappVerificationNotification(AuthDTO authDTO, NamespaceProfileDTO profileDTO) {
		try {
			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("url", profileDTO.getWhatsappUrl());
			dataModel.put("mobileNumber", profileDTO.getWhatsappNumber());

			boolean whatsappMsgStatus = false;
			if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0) {
				whatsappMsgStatus = whatsappService.sendWhatsappNumberVerification(authDTO, dataModel);
			}
			// SMS
			if (!whatsappMsgStatus) {
				smsService.sendWhatsappNumberVerificationSMS(authDTO, dataModel);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getRegistrationNumber(AuthDTO authDTO, String tripCode) {
		String registrationNumber = null;
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		TripInfoDTO infoDTO = tripService.getTripInfo(authDTO, tripDTO);
		if (infoDTO != null && infoDTO.getBusVehicle() != null && StringUtil.isNotNull(infoDTO.getBusVehicle().getRegistationNumber())) {
			registrationNumber = infoDTO.getBusVehicle().getRegistationNumber();
		}
		if (StringUtil.isNull(registrationNumber)) {
			throw new ServiceException(ErrorCode.TRIP_INFO_INVALID);
		}
		return registrationNumber;
	}

	public String tripCovidEpassNotification(AuthDTO authDTO, String ticketCode, String registrationNumber, String supportNumber) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		Future<SmsResponse> futureTask = smsService.tripCovidEpassNotification(authDTO, ticketDTO, registrationNumber, supportNumber);
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

			ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: Covid Epass, Support Number: " + supportNumber);
			SaveTripHistory(authDTO, ticketDTO);
		}
		return message;

	}

	@Async
	public void sendCustomerTicketEvent(AuthDTO authDTO, TicketDTO ticketDTO, EventNotificationEM notificationEventEM) {
		try {
			if (StringUtil.isValidMobileNumber(ticketDTO.getPassengerMobile())) {
				EventNotificationConfigDTO notificationConfigDTO = eventNotificationConfigService.getActiveNotificationConfig(authDTO, ticketDTO.getTripDTO(), ticketDTO.getFromStation(), ticketDTO.getToStation(), notificationEventEM, ticketDTO.getCode());
				if (notificationConfigDTO != null && notificationConfigDTO.getTemplateConfig() != null && notificationConfigDTO.getTemplateConfig().getNotificationType() != null) {
					if (notificationConfigDTO.getTemplateConfig().getNotificationSMSConfig().isPromotionalSMSType() && !DateUtil.isHourWithinRange(10, 21, DateUtil.NOW())) {
						throw new ServiceException(ErrorCode.TRIP_DATE_OVER, "Promotional SMS service only allowed between 10AM and 8:55PM");
					}
					Map<String, String> dataModel = Maps.newHashMap();
					dataModel.put("pnr", ticketDTO.getCode());
					dataModel.put("name", StringUtil.substring(ticketDTO.getPassengerName(), 30));
					dataModel.put("originName", ticketDTO.getFromStation().getName());
					dataModel.put("destinationName", ticketDTO.getToStation().getName());
					dataModel.put("bookedBy", ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransactionType() != null && ticketDTO.getTicketXaction().getTransactionType().getId() == Numeric.ONE_INT ? ticketDTO.getTicketUser().getName() : authDTO.getUser().getName());
					dataModel.put("devicemedium", ticketDTO.getDeviceMedium().getName());
					dataModel.put("ticketStatus", notificationEventEM.getName());
					dataModel.put("serviceNumber", ticketDTO.getServiceNo());
					dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());
					dataModel.put("operatorName", authDTO.getNamespace().getName());
					dataModel.put("travelDate", DateUtil.parseDateFormat(ticketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
					dataModel.put("seats", ticketDTO.getSeatNames());
					dataModel.put("fare", ticketDTO.getTicketFareWithAddons().toString());
					dataModel.put("time", ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransactionType() != null && ticketDTO.getTicketXaction().getTransactionType().getId() == Numeric.ONE_INT ? ticketDTO.getTicketAt().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : DateUtil.NOW().format("hh12:mm a", Locale.forLanguageTag("en_IN")));
					dataModel.put("bookedDate", ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransactionType() != null && ticketDTO.getTicketXaction().getTransactionType().getId() == Numeric.ONE_INT ? DateUtil.parseDateFormat(ticketDTO.getTicketAt().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E") : DateUtil.parseDateFormat(DateUtil.NOW().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
					dataModel.put("linkpay", ticketDTO.getTicketExtra() != null ? ticketDTO.getTicketExtra().getLinkPay() : Text.EMPTY);
					dataModel.put("namespaceURL", authDTO.getNamespace().getProfile().getDomainURL());

					// send SMS Notification
					if (notificationConfigDTO.getNotificationMediumMap().containsKey(NotificationMediumEM.SMS.getCode())) {
						smsService.sendCustomerTicketEventSMS(authDTO, dataModel, notificationConfigDTO.getTemplateConfig());
					}
					// send whatsapp Notification
					if (notificationConfigDTO.getNotificationMediumMap().containsKey(NotificationMediumEM.WHATS_APP.getCode()) && authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != WhatsappProviderEM.NOT_AVAILABLE.getId()) {
						whatsappService.sendCustomerTicketEvent(authDTO, dataModel, notificationConfigDTO.getTemplateConfig());
					}
				}
			}
		}
		catch (ServiceException e) {
			TRIP_INFO_LOGGER.error("Customer promotion SMS Blocked {} {} {}", ticketDTO.getCode(), ticketDTO.getPassengerMobile(), e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendCustomerEventNotification(AuthDTO authDTO, TripDTO tripDTO, StationDTO fromStation, StationDTO toStation, EventNotificationEM notificationEventEM, String ticketCode, String mobileNumber) {
		try {
			if (StringUtil.isValidMobileNumber(mobileNumber)) {
				EventNotificationConfigDTO notificationConfigDTO = eventNotificationConfigService.getActiveNotificationConfig(authDTO, tripDTO, fromStation, toStation, notificationEventEM, ticketCode);
				if (notificationConfigDTO != null && notificationConfigDTO.getTemplateConfig() != null && notificationConfigDTO.getTemplateConfig().getNotificationType() != null) {
					Map<String, String> dataModel = Maps.newHashMap();
					dataModel.put("url", authDTO.getNamespace().getProfile().getDomainURL());
					dataModel.put("operatorName", authDTO.getNamespace().getName());
					dataModel.put("mobileNumber", mobileNumber);

					/** Send SMS Notification */
					if (notificationConfigDTO.getNotificationMediumMap().containsKey(NotificationMediumEM.SMS.getCode())) {
						smsService.sendCustomerTicketEventSMS(authDTO, dataModel, notificationConfigDTO.getTemplateConfig());
					}
					/** Send Whatsapp Notification */
					if (notificationConfigDTO.getNotificationMediumMap().containsKey(NotificationMediumEM.WHATS_APP.getCode()) && authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != WhatsappProviderEM.NOT_AVAILABLE.getId()) {
						whatsappService.sendCustomerTicketEvent(authDTO, dataModel, notificationConfigDTO.getTemplateConfig());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendApologyNotification(AuthDTO authDTO, String ticketCode, NotificationTemplateConfigDTO templateConfig) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

		if (ticketDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		if (StringUtils.isNotBlank(ticketDTO.getPassengerMobile()) && MOBILE_NUMBER_PATTERN.matcher(ticketDTO.getPassengerMobile()).matches() == Boolean.FALSE) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		String message = null;
		if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("passengerName", ticketDTO.getPassengerName());
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("namespaceURL", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("pnr", ticketDTO.getCode());
			dataModel.put("mobileNumber", ticketDTO.getPassengerMobile());

			Future<SmsResponse> futureTask = smsService.sendCustomerTicketEventSMS(authDTO, dataModel, templateConfig);
			message = null;
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
				ticketDTO.setRemarks("PNR: " + ticketCode + ", Reason: " + templateConfig.getName());
				SaveTripHistory(authDTO, ticketDTO);
			}
		}
		return message;
	}

	private NotificationTemplateConfigDTO getNotificationConfigTemplate(AuthDTO authDTO, String templateCode) {
		NotificationTemplateConfigDTO templateConfig = new NotificationTemplateConfigDTO();
		templateConfig.setCode(templateCode);
		templateConfig = notificationConfigService.getNotificationTemplateConfigByCode(authDTO, templateConfig);
		if (templateConfig.getId() == 0) {
			throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
		}
		return templateConfig;
	}

	@Async
	public void sendTicketBookingNotification(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			Map<String, String> dataModel = Maps.newHashMap();
			dataModel.put("namespaceCode", authDTO.getNamespaceCode());
			dataModel.put("namespaceURL", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("name", StringUtil.substring(ticketDTO.getPassengerName(), 30));
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("pnr", ticketDTO.getCode());
			dataModel.put("originName", ticketDTO.getFromStation().getName());
			dataModel.put("destinationName", ticketDTO.getToStation().getName());
			dataModel.put("route", ticketDTO.getFromStation().getName() + " to " + ticketDTO.getToStation().getName());

			dataModel.put("travelDate", DateUtil.parseDateFormat(ticketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("seats", ticketDTO.getSeatNames());
			dataModel.put("fare", !authDTO.getNamespace().getProfile().isNoFareSMSFlag() ? ticketDTO.getTicketFareWithAddons().toString() : "0.00");
			dataModel.put("busType", StringUtil.substring(BitsUtil.getBusCategoryUsingEM(ticketDTO.getTripDTO().getBus().getCategoryCode()), 30));
			dataModel.put("boarding", StringUtil.substring(ticketDTO.getBoardingPoint().getName(), 30));
			dataModel.put("time", ticketDTO.getBoardingPoint().getMinitues() < 1440 ? ticketDTO.getBoardingPointDateTime().format("hh12:mm a", Locale.forLanguageTag("en_IN")) : ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
			dataModel.put("contact", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getAddress(), 15) + " " + StringUtil.substring(ticketDTO.getBoardingPoint().getNumber(), 12)));
			dataModel.put("boardingDate", DateUtil.parseDateFormat(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("boardingAddress", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getAddress(), 30)));
			dataModel.put("boardingContact", StringUtil.substring(ticketDTO.getBoardingPoint().getNumber(), 30));

			if (StringUtil.isNull(ticketDTO.getBoardingPoint().getNumber())) {
				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setCode(ticketDTO.getBoardingPoint().getCode());
				stationPointDTO = stationPointService.getStationPoint(authDTO, stationPointDTO);
				dataModel.put("boardingContact", StringUtil.substring(stationPointDTO.getNumber(), 30));
			}

			dataModel.put("boardingLandmark", StringUtil.substring(ticketDTO.getBoardingPoint().getLandmark(), 30));
			dataModel.put("boardingContactName", authDTO.getNamespace().getName());
			dataModel.put("serviceNumber", ticketDTO.getServiceNo());
			dataModel.put("landmark", removeUnknownSymbol(StringUtil.substring(ticketDTO.getBoardingPoint().getLandmark(), 30)));
			dataModel.put("mapurl", StringUtil.isNotNull(ticketDTO.getBoardingPoint().getMapUrl()) ? ticketDTO.getBoardingPoint().getMapUrl() : "");
			dataModel.put("travelDatetime", ticketDTO.getBoardingPointDateTime().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
			if (StringUtil.isNotNull(authDTO.getNamespace().getProfile().getWhatsappUrl())) {
				String link = Constants.WHATSAPP_LINK + "?phone=" + "91" + authDTO.getNamespace().getProfile().getWhatsappNumber();
				String shortURL = BitsShortURL.getUrlshortener(link, BitsShortURL.TYPE.PER);
				dataModel.put("whatsappUrl", shortURL);
			}
			else {
				dataModel.put("whatsappUrl", Text.HYPHEN);
			}

			// WHATSAPP
			boolean whatsappMsgStatus = false;
			if (authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING)) {
				whatsappMsgStatus = whatsappService.sendTicketBookingNotification(authDTO, ticketDTO, dataModel);
			}
			// SMS
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && !whatsappMsgStatus) {
				smsService.sendMTicketSMSNew(authDTO, ticketDTO, dataModel);
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

	@Async
	public void sendTicketCancelNotification(AuthDTO authDTO, TicketDTO repositoryTicketDTO) {
		try {
			Map<String, String> dataModel = Maps.newHashMap();

			dataModel.put("namespaceURL", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("name", StringUtil.substring(repositoryTicketDTO.getPassengerName(), 30));
			dataModel.put("travelsName", authDTO.getNamespace().getName());
			dataModel.put("pnr", repositoryTicketDTO.getCode());
			dataModel.put("originName", repositoryTicketDTO.getFromStation().getName());
			dataModel.put("destinationName", repositoryTicketDTO.getToStation().getName());
			dataModel.put("route", repositoryTicketDTO.getFromStation().getName() + " to " + repositoryTicketDTO.getToStation().getName());
			dataModel.put("travelDate", DateUtil.parseDateFormat(repositoryTicketDTO.getTripDate().format("YYYY-MM-DD"), "yyyy-MM-dd", "MMM dd E"));
			dataModel.put("seats", repositoryTicketDTO.getSeatNames());
			dataModel.put("whatsappUrl", authDTO.getNamespace().getProfile().getWhatsappUrl());

			// WHATSAPP
			boolean whatsappMsgStatus = false;
			if (DeviceMediumEM.API_USER.getId() != repositoryTicketDTO.getDeviceMedium().getId() && authDTO.getNamespace().getProfile().getWhatsappProvider().getId() != 0 && NotificationTypeEM.isWhatsappNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.CONFIRM_CANCELLATION)) {
				whatsappMsgStatus = whatsappService.sendTicketCancelNotification(authDTO, repositoryTicketDTO, dataModel);
			}
			// SMS
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_CANCELLATION) && !whatsappMsgStatus) {
				smsService.sendCancelSMS(authDTO, repositoryTicketDTO, dataModel);
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
	}

}
