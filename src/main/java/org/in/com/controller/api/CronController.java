package org.in.com.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CalendarAnnouncementIO;
import org.in.com.controller.web.io.NamespaceIO;
import org.in.com.controller.web.io.NamespaceProfileIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.TransactionModeIO;
import org.in.com.controller.web.io.TransactionTypeIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.UserTransactionIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.CalendarAnnouncementService;
import org.in.com.service.FareRuleService;
import org.in.com.service.NamespaceService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.PendingOrderService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.SearchService;
import org.in.com.service.TicketPhoneBookAutoReleaseService;
import org.in.com.service.TicketService;
import org.in.com.service.UserFeedbackService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.report.ExportReportService;
import org.in.com.service.report.UtilityReportServiceImpl;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/api/json/{accessToken}/cron")
public class CronController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	AuthService authService;
	@Autowired
	ExportReportService querservice;
	@Autowired
	TicketPhoneBookAutoReleaseService autoReleaseService;
	@Autowired
	TicketService ticketService;
	@Autowired
	UserTransactionService transactionService;
	@Autowired
	PendingOrderService pendingOrderService;
	@Autowired
	UtilityReportServiceImpl reportService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	UserFeedbackService feedbackService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	FareRuleService fareRuleService;
	@Autowired
	ScheduleDynamicStageFareService scheduleDynamicStageFareService;
	@Autowired
	SearchService searchService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	BusVehicleDriverService vehicleDriverService;
	@Autowired
	CalendarAnnouncementService calendarAnnouncementService;

	@RequestMapping(value = "/{namespaceCode}/report/generation", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> generateReport(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @RequestBody JSONObject jsonObject) throws Exception {
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);
		authDTO.setUser(new UserDTO());
		if (jsonObject != null && (jsonObject.has("reportName") && jsonObject.has("reportCode") && StringUtil.isNull(jsonObject.getString("datePeriod")) || StringUtil.isNull(jsonObject.getString("reportImpl")))) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		querservice.exportReport(authDTO, jsonObject);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/phone/auto/confirm/release/tripdate/v2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> confirmAndReleasePhoneBlockTicket(@PathVariable("accessToken") String accessToken, String tripDate, String namespaceCode) throws Exception {
		validateMandatory(accessToken);
		hirondelle.date4j.DateTime releaseDate = DateUtil.NOW();
		if (StringUtil.isNotNull(tripDate) && DateUtil.isValidDate(tripDate)) {
			releaseDate = DateUtil.getDateTime(tripDate);
		}

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = namespaceService.getAll(authDTO);
		for (NamespaceDTO namespace : namespaceList) {
			if (!namespace.getProfile().isPhoneBlockReleaseConfirmJobEnabled() || namespace.getLookupId() != 0 || (StringUtil.isNotNull(namespaceCode) && !namespaceCode.equals(namespace.getCode()))) {
				continue;
			}
			authService.switchNamespace(authDTO, namespace);
			authDTO = authService.getAuthDTO(authDTO.getAuthToken());

			authDTO.getAdditionalAttribute().put(Text.PHONE_BOOK_AUTO_CANCEL, "Auto Cancel");
			autoReleaseService.confirmAndReleasePhoneBlockTicket(authDTO, releaseDate);
		}

		return ResponseIO.success();
	}

	@RequestMapping(value = "/{namespaceCode}/after/travel/coupon", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> addAfterTravelCoupon(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @RequestBody List<String> tickets) throws Exception {
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);

		ticketService.addAfterTravelCoupon(authDTO, tickets);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{namespaceCode}/user/balance/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserTransactionIO>> getUserBalanceMismatch(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, String fromDate, String toDate) throws Exception {
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);

		List<UserTransactionIO> transactionList = new ArrayList<UserTransactionIO>();

		if (StringUtil.isNull(fromDate) || StringUtil.isNull(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		List<UserTransactionDTO> userTrasnactions = transactionService.validateUserBalanceMismatch(authDTO, new DateTime(fromDate), new DateTime(toDate));

		for (UserTransactionDTO transactionDTO : userTrasnactions) {
			UserTransactionIO userTransactionIO = new UserTransactionIO();
			TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
			transactionTypeIO.setName(transactionDTO.getTransactionType().getName());
			transactionTypeIO.setCreditDebitFlag(transactionDTO.getTransactionType().getCreditDebitFlag());
			transactionTypeIO.setCode(transactionDTO.getTransactionType().getCode());
			userTransactionIO.setTransactionType(transactionTypeIO);

			if (transactionDTO.getTransactionMode() != null) {
				TransactionModeIO transactionModeIO = new TransactionModeIO();
				transactionModeIO.setCode(transactionDTO.getTransactionMode().getCode());
				transactionModeIO.setName(transactionDTO.getTransactionMode().getName());
				userTransactionIO.setTransactionMode(transactionModeIO);
			}

			userTransactionIO.setRefferenceCode(transactionDTO.getRefferenceCode());
			userTransactionIO.setCreditAmount(transactionDTO.getCreditAmount());
			userTransactionIO.setDebitAmount(transactionDTO.getDebitAmount());
			userTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
			userTransactionIO.setClosingBalance(transactionDTO.getClosingBalanceAmount());
			userTransactionIO.setTdsTax(transactionDTO.getTdsTax());
			userTransactionIO.setTransactionDate(transactionDTO.getTransactionDate());

			// User
			UserIO userIO = new UserIO();
			userIO.setCode(transactionDTO.getUser().getCode());
			userIO.setName(transactionDTO.getUser().getName());
			userIO.setActiveFlag(transactionDTO.getId());
			userTransactionIO.setUser(userIO);

			transactionList.add(userTransactionIO);
		}
		return ResponseIO.success(transactionList);
	}

	@RequestMapping(value = "/pending/order/refund", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> refundCancelPendingOrder(@PathVariable("accessToken") String accessToken) throws Exception {
		validateMandatory(accessToken);
		pendingOrderService.processPendingOrderAutoRefund();
		return ResponseIO.success();
	}

	@RequestMapping(value = "/notification/feedback/{travelDate}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> sendFeedBackSMS(@PathVariable("accessToken") String accessToken, @PathVariable("travelDate") String travelDate) throws Exception {
		validateMandatory(accessToken);
		if (!DateUtil.isValidDate(travelDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = namespaceService.getAll(authDTO);
		for (NamespaceDTO namespace : namespaceList) {
			if ((namespace.getLookupId() != 0) || (!Constants.FEEDBACK.contains(namespace.getCode()))) {
				continue;
			}
			authService.switchNamespace(authDTO, namespace);
			authDTO = authService.getAuthDTO(authDTO.getAuthToken());
			hirondelle.date4j.DateTime tripDate = DateUtil.getDateTime(travelDate);
			feedbackService.sendFeedBackSMS(authDTO, tripDate);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{namespaceCode}/notification/trackbus/{tripCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> sendTripJourneyTrackingSMS(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("tripCode") String tripCode) throws Exception {
		validateMandatory(accessToken, namespaceCode);
		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);

		NamespaceDTO namespace = new NamespaceDTO();
		namespace.setCode(namespaceCode);

		authService.switchNamespace(authDTO, namespace);
		authDTO = authService.getAuthDTO(authDTO.getAuthToken());

		if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.GPS_TRACKING) || (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getWhatsappNotificationFlagCode(), NotificationTypeEM.GPS_TRACKING))) {
			TripDTO trip = new TripDTO();
			trip.setCode(tripCode);
			notificationService.sendTripJourneyTrackingSMS(authDTO, trip);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/notification/{notificationType}/namespace", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceIO>> getNotificationEnabledNamespacelist(@PathVariable("accessToken") String accessToken, @PathVariable("notificationType") String notificationType) throws Exception {
		validateMandatory(accessToken, ApplicationConfig.getServerZoneCode());
		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);

		NotificationTypeEM notification = NotificationTypeEM.getNotificationTypeEM(notificationType);
		List<NamespaceDTO> namespaceList = namespaceService.getNotificationEnabledNamespace(authDTO, notification);
		List<NamespaceIO> list = new ArrayList<>();
		for (NamespaceDTO namespace : namespaceList) {
			NamespaceIO namespaceIO = new NamespaceIO();
			namespaceIO.setCode(namespace.getCode());

			NamespaceProfileIO namespaceProfile = new NamespaceProfileIO();
			namespaceProfile.setTrackbusMinutes(namespace.getProfile().getTrackbusMinutes());
			namespaceIO.setNamespaceProfile(namespaceProfile);
			list.add(namespaceIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/notification/sales/summary/{tripDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> sendSalesSummary(@PathVariable("accessToken") String accessToken, @PathVariable("tripDate") String tripDate) throws Exception {
		validateMandatory(accessToken);
		if (!DateUtil.isValidDate(tripDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		DateTime now = DateUtil.NowV2();
		if (now.getHourOfDay() != 23) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = namespaceService.getAll(authDTO);
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());
				if (!authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.DAILY_SALES_SUMMARY)) {
					continue;
				}
				Map<String, Object> dataModel = reportService.getSalesSummaryDetails(authDTO, DateUtil.getDateTime(tripDate));
				dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
				dataModel.put("time", DateUtil.parseDateFormat(DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd hh:mm:ss", "MMM dd E hh:mm a"));
				int tripSeatCount = Integer.valueOf(dataModel.get("TOTAL_TRIP_CNT").toString());
				int tripBookedCount = Integer.valueOf(dataModel.get("TOTAL_BO_CNT").toString());
				if (tripSeatCount > 0 && tripBookedCount > 0) {
					// notificationService.sendOverallTripSummarySMS(authDTO,
					// dataModel);
					// push flutter notification
					notificationPushService.pushDailySalesNotification(authDTO, dataModel);
				}
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/fare/rule/{ruleCode}/sync", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> syncFareRule(@PathVariable("accessToken") String accessToken, @PathVariable("ruleCode") String ruleCode, String syncDate) throws Exception {
		if (!accessToken.equals("H5BUL9H73ZWMBYQF")) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		if (!ruleCode.equals("FR3A85294M") && !ruleCode.equals("FR188B392G") && !ruleCode.equals("FR388C1277I")) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		AuthDTO authDTO = new AuthDTO();
		authDTO.setUser(new UserDTO());

		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		fareRuleDTO.setCode(ruleCode);

		hirondelle.date4j.DateTime date = StringUtil.isNotNull(syncDate) ? DateUtil.getDateTime(syncDate) : DateUtil.NOW();
		fareRuleService.syncVertexFareRule(authDTO, fareRuleDTO, DateUtil.minusDaysToDate(date, Numeric.ONE_INT));
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{namespaceCode}/dynamic/stage/fare/refresh", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> processDynamicStageFare(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		if (BitsUtil.getDynamicPriceProvider(authDTO.getNamespace().getProfile().getDynamicPriceProviders(), DynamicPriceProviderEM.REDBUS) != null) {
			scheduleDynamicStageFareService.openChart(authDTO, DateUtil.NOW());
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/active/trips/push", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> pushTripsDetails(@PathVariable("accessToken") String accessToken, String startDate, int days) throws Exception {
		String jobName = "active_trips_push";
		try {
			checkConcurrentRequests(jobName, startDate + "+" + days);

			validateMandatory(accessToken);

			AuthDTO authDTO = new AuthDTO();
			List<NamespaceDTO> namespaces = namespaceService.getAll(authDTO);

			if (!DateUtil.isValidDate(startDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}

			if (days > 40) {
				throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
			}
			hirondelle.date4j.DateTime pushDate = new hirondelle.date4j.DateTime(startDate);
			searchService.pushTripsDetails(authDTO, namespaces, pushDate, days);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseConcurrentRequests(jobName);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/routes/refine", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> disableInActiveRoutes(@PathVariable("accessToken") String accessToken) throws Exception {
		String jobName = "refine_routes";
		try {
			checkConcurrentRequests(jobName, Text.EMPTY);

			validateMandatory(accessToken);

			AuthDTO authDTO = new AuthDTO();
			List<NamespaceDTO> namespaces = namespaceService.getAll(authDTO);

			searchService.disableInActiveRoutes(authDTO, namespaces);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseConcurrentRequests(jobName);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/notification/occupancy/status", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> tripOccupancyStatusNotification(@PathVariable("accessToken") String accessToken) throws Exception {
		validateMandatory(accessToken);
		DateTime now = DateUtil.NowV2();
		if (now.getHourOfDay() < 8 && now.getHourOfDay() > 22) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = namespaceService.getAll(authDTO);
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());
				if (!authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.OCCUPANCY_STATUS)) {
					continue;
				}
				// push flutter notification
				notificationPushService.pushTripOccupancyStatusNotification(authDTO);
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/revenue/migration/{tripDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> migrateTicketNetRevenue(@PathVariable("accessToken") String accessToken, @PathVariable("tripDate") String tripDate, String namespaceCode) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = new ArrayList<>();
		if (StringUtil.isNotNull(namespaceCode)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			namespaceList.add(namespaceDTO);
		}
		else {
			namespaceList = namespaceService.getAll(authDTO);
		}
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());

				ticketService.migrateTicketNetRevenue(authDTO, DateUtil.getDateTime(tripDate));
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/top/route/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> topRouteUpdate(@PathVariable("accessToken") String accessToken, String namespaceCode) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = new ArrayList<>();
		if (StringUtil.isNotNull(namespaceCode)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			namespaceList.add(namespaceDTO);
		}
		else {
			namespaceList = namespaceService.getAll(authDTO);
		}
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());

				ticketService.updateTopRoute(authDTO);
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/deactivate/{namespaceCode}/ticket/status/tentative/block", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateTicketStatusToBlock(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @RequestParam(required = true) String groupCode, String statusCode, int numberOfDays) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		authDTO.setDeviceMedium(DeviceMediumEM.WEB_USER);
		authDTO.setUser(new UserDTO());

		List<GroupDTO> groups = new ArrayList<>();
		for (String code : groupCode.split(Text.COMMA)) {
			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(code);
			groups.add(groupDTO);
		}

		List<TicketStatusEM> ticketStatusList = new ArrayList<>();
		if (StringUtil.isNotNull(statusCode)) {
			for (String code : statusCode.split(Text.COMMA)) {
				TicketStatusEM ticketStatusEM = TicketStatusEM.getTicketStatusEM(code);
				if (ticketStatusEM != null) {
					ticketStatusList.add(ticketStatusEM);
				}
			}
		}
		if (numberOfDays < 10) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
		if (ticketStatusList.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		if (groups.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_GROUP);
		}
		ticketService.updateTicketStatusToBlock(authDTO, groups, ticketStatusList, numberOfDays);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/driver/assign/trip/count/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateAssignedTripsCount(@PathVariable("accessToken") String accessToken, String namespaceCode, @RequestParam(required = true, defaultValue = "20") int days) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = new ArrayList<>();
		if (StringUtil.isNotNull(namespaceCode)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			namespaceList.add(namespaceDTO);
		}
		else {
			namespaceList = namespaceService.getAll(authDTO);
		}
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());

				vehicleDriverService.updateAssignedTripsCount(authDTO, days);
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/trip/data/count/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateTripDataCount(@PathVariable("accessToken") String accessToken, String namespaceCode, @RequestParam(required = true, defaultValue = "10") int days) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = new ArrayList<>();
		if (StringUtil.isNotNull(namespaceCode)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			namespaceList.add(namespaceDTO);
		}
		else {
			namespaceList = namespaceService.getAll(authDTO);
		}
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());
				searchService.updateTripDataCount(authDTO, days);
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/dynamicprice/fare/notify/queue/process", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> processFareChangeQueue(@PathVariable("accessToken") String accessToken) throws Exception {
		String jobName = "notify_fare_change_queue";
		try {
			checkConcurrentRequests(jobName, "");

			validateMandatory(accessToken);

			AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
			scheduleDynamicStageFareService.processFareChangeQueueJob(authDTO);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseConcurrentRequests(jobName);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/calendar/announcement/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CalendarAnnouncementIO>> getCalendarAnnouncementForZoneSync(@PathVariable("accessToken") String accessToken, String syncDate) {
		validateMandatory(accessToken);
		List<CalendarAnnouncementIO> announcementList = new ArrayList<>();
		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<CalendarAnnouncementDTO> list = calendarAnnouncementService.getAllCalendarAnnouncementForZoneSync(authDTO, syncDate);
		for (CalendarAnnouncementDTO calendatAnouncementDTO : list) {
			CalendarAnnouncementIO calendarAnouncementIO = new CalendarAnnouncementIO();
			calendarAnouncementIO.setCode(calendatAnouncementDTO.getCode());
			calendarAnouncementIO.setName(calendatAnouncementDTO.getName());
			calendarAnouncementIO.setActiveFrom(calendatAnouncementDTO.getActiveFrom());
			calendarAnouncementIO.setActiveTo(calendatAnouncementDTO.getActiveTo());
			calendarAnouncementIO.setDayOfWeek(calendatAnouncementDTO.getDayOfWeek());

			BaseIO categoryIO = new BaseIO();
			categoryIO.setCode(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getCode() : null);
			categoryIO.setName(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getName() : null);
			calendarAnouncementIO.setCategory(categoryIO);

			List<StateIO> stateList = new ArrayList<StateIO>();
			for (StateDTO stateDTO : calendatAnouncementDTO.getStates()) {
				StateIO stateIO = new StateIO();
				stateIO.setCode(stateDTO.getCode());
				stateIO.setName(stateDTO.getName());
				stateList.add(stateIO);
			}
			calendarAnouncementIO.setStates(stateList);

			List<String> dateList = new ArrayList<>();
			for (hirondelle.date4j.DateTime date : calendatAnouncementDTO.getDates()) {
				dateList.add(DateUtil.convertDate(date));
			}
			calendarAnouncementIO.setDates(dateList);

			calendarAnouncementIO.setActiveFlag(calendatAnouncementDTO.getActiveFlag());
			announcementList.add(calendarAnouncementIO);
		}
		return ResponseIO.success(announcementList);
	}

	private AuthDTO validateMandatory(String accessToken, String namespaceCode) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (StringUtil.isNull(namespaceCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		return authDTO;
	}

	private void validateMandatory(String accessToken) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}

	public static synchronized boolean checkConcurrentRequests(String jobName, String data) {
		if (ConcurrentRequests.get(jobName) != null && ConcurrentRequests.get(jobName) >= 1) {
			System.out.println(DateUtil.NOW() + " CRONJOB01 - " + jobName + " - reached Max Concurrent Request - " + data);
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		if (ConcurrentRequests.get(jobName) != null) {
			ConcurrentRequests.put(jobName, ConcurrentRequests.get(jobName) + 1);
		}
		else {
			ConcurrentRequests.put(jobName, 1);
		}
		return true;
	}

	public static synchronized boolean releaseConcurrentRequests(String jobName) {
		if (ConcurrentRequests.get(jobName) != null) {
			if (ConcurrentRequests.get(jobName) > 0) {
				ConcurrentRequests.put(jobName, ConcurrentRequests.get(jobName) - 1);
			}
		}
		return true;
	}

	@RequestMapping(value = "/notification/travel/status/summary", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> sendTravelStatusNotification(@PathVariable("accessToken") String accessToken) throws Exception {
		validateMandatory(accessToken);

		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		List<NamespaceDTO> namespaceList = namespaceService.getAll(authDTO);
		for (NamespaceDTO namespace : namespaceList) {
			if (namespace.getLookupId() != 0) {
				continue;
			}
			try {
				authService.switchNamespace(authDTO, namespace);
				authDTO = authService.getAuthDTO(authDTO.getAuthToken());
				if (!authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TRAVEL_STATUS)) {
					continue;
				}
				List<String> travelStatusSummary = reportService.getTravelStatusSummary(authDTO);
				if (!travelStatusSummary.isEmpty()) {
					notificationPushService.pushDailyTravelStatusNotification(authDTO, travelStatusSummary);
				}
			}
			catch (ServiceException e) {
				System.out.println(e.getErrorCode().getCode());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/export/report/details", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> exportReportDetails(@PathVariable("accessToken") String accessToken) throws Exception {
		validateMandatory(accessToken);
		AuthDTO authDTO = authService.getApiAuthendtication(ApplicationConfig.getServerZoneCode(), ApplicationConfig.getZoneUsername(), ApplicationConfig.getZoneAPIToken(), DeviceMediumEM.API_USER);
		querservice.exportReportV2(authDTO);
		return ResponseIO.success();
	}
}
