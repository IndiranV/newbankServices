package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.AppStoreDetailsIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NotificationSubscriptionIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NotificationSubscriptionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.TicketService;
import org.in.com.service.TransactionOTPService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/{authtoken}/notification")
public class NotificationController extends BaseController {
	@Autowired
	TransactionOTPService otpService;
	@Autowired
	TicketService ticketService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationPushService notificationPushService;

	@RequestMapping(value = "/{tripCode}/delay/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> busDelay(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String reason, String delayTime, String supportNumber) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.tripDelayNotification(authDTO, ticketCode, reason, delayTime, supportNumber);
			base.setName(response);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/early/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> busEarly(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String reason, String earlyTime, String supportNumber) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.tripEarlyNotification(authDTO, ticketCode, reason, earlyTime, supportNumber);
			base.setName(response);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/cancelled/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> tripCancelled(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String reason, String supportNumber) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.tripCancelNotification(authDTO, ticketCode, reason, supportNumber);
			base.setName(response);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/stationPointChanged/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> stationPointChanged(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String reason, String stationPointName, String stationPointTime, String supportNumber) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.tripStationPointChangeNotification(authDTO, ticketCode, reason, stationPointName, stationPointTime, supportNumber);
			base.setName(response);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/bustypechange/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> busTypeChanged(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String reason, String busTypeName, String supportNumber) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.busTypeChangeNotification(authDTO, ticketCode, reason, busTypeName, supportNumber);
			base.setName(response);
		}
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/trip/send", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> sendTripSms(@PathVariable("authtoken") String authtoken, @RequestBody Map<String, String> notificationDetails) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		notificationService.sendTripSms(authDTO, notificationDetails);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/busbuddy/{ticketCode}/afterboard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> busbuddyAfterboard(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String vehicleNumber, String mobileNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(mobileNumber) || StringUtil.isNull(vehicleNumber)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (EhcacheManager.getFreshRequestEhCache().get("afterboard" + authDTO.getUserCode() + ticketCode) == null) {
				notificationService.firebusbuddyAfterboard(authDTO, ticketCode, vehicleNumber, mobileNumber);
				Element element = new Element("afterboard" + authDTO.getUserCode() + ticketCode, ticketCode);
				EhcacheManager.getFreshRequestEhCache().put(element);
			}
			else {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/response/{refferencecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Map<String, String>> getResponse(@PathVariable("authtoken") String authtoken, @PathVariable("refferencecode") String refferencecode) throws Exception {
		Map<String, String> response = new HashMap<String, String>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			response = notificationService.getSMSStatus(authDTO, refferencecode);
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/vehicle/change/notify/{ticketcode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> sendVehicleChangeSms(@PathVariable("authtoken") String authtoken, @PathVariable("ticketcode") String ticketcode) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.sendVehicleChangeSms(authDTO, ticketcode);
			base.setName(response);
		}
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/van/pickup/notify/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> vanPickupNotify(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String registrationNumber, String supportName, String supportNumber, String trackUrl) throws Exception {
		BaseIO base = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String response = notificationService.vanPickupNotify(authDTO, ticketCode, registrationNumber, supportName, supportNumber, trackUrl);
			base.setName(response);
		}
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/otp/generate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> generateTransactionOTP(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (StringUtil.isNull(ticketCode)) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}
		if (authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketService.getTicketStatus(authDTO, ticketDTO);

		if (ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
		}
		otpService.generateOTP(authDTO, ticketCode, ticketDTO.getPassengerMobile(), true);
		baseIO.setCode(ticketDTO.getPassengerMobile());
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/otp/{otpNumber}/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> validateTransactionOTP(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @PathVariable("otpNumber") int otpNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (StringUtil.isNull(ticketCode)) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}
		if (authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketService.getTicketStatus(authDTO, ticketDTO);

		if (!otpService.validateOTP(authDTO, ticketDTO.getCode(), ticketDTO.getPassengerMobile(), otpNumber)) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}

		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/mobile/otp/generate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> generateTransactionOTPV2(@PathVariable("authtoken") String authtoken, String mobileNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (StringUtil.isNull(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}

		otpService.generateOTP(authDTO, mobileNumber, mobileNumber, true);
		baseIO.setCode(mobileNumber);
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/mobile/otp/{otpNumber}/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> validateTransactionOTPV2(@PathVariable("authtoken") String authtoken, @PathVariable("otpNumber") int otpNumber, String mobileNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (StringUtil.isNull(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}

		if (!otpService.validateOTP(authDTO, mobileNumber, mobileNumber, otpNumber)) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}

		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/user/{mobileNumber}/push/fcm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> pushFCMNotification(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber, @RequestBody JSONObject jsonObject) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (jsonObject.has("title") && jsonObject.has("content") && StringUtil.isNull(jsonObject.getString("title")) || StringUtil.isNull(jsonObject.getString("content"))) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
		userCustomerDTO.setMobile(mobileNumber);

		String image = jsonObject.has("image") ? jsonObject.getString("image") : Text.EMPTY;

		notificationPushService.pushNotification(authDTO, userCustomerDTO, jsonObject.getString("title"), jsonObject.getString("content"), image);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/van/pickup/notify/{tripCode}/v2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> sendTripVanPickupSMS(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody List<String> ticketCodes) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		if (ticketCodes.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}

		TripVanInfoDTO tripInfo = new TripVanInfoDTO();
		tripInfo.setCode(tripCode);

		notificationService.sendVanPickupSMS(authDTO, tripInfo, ticketCodes);

		return ResponseIO.success();

	}

	@RequestMapping(value = "/subscription", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<NotificationSubscriptionIO>> getAllSubscription(@PathVariable("authtoken") String authtoken) throws Exception {
		List<NotificationSubscriptionIO> alertSubscriptions = new ArrayList<NotificationSubscriptionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<NotificationSubscriptionDTO> alertSubscriptionsList = notificationService.getAllSubscription(authDTO);
		for (NotificationSubscriptionDTO alertSubscriptionsDTO : alertSubscriptionsList) {
			if (alertSubscriptionsDTO.getSubscriptionType().getLevel() != 0) {
				continue;
			}
			NotificationSubscriptionIO notificationSubscription = new NotificationSubscriptionIO();
			notificationSubscription.setCode(alertSubscriptionsDTO.getCode());

			BaseIO eventType = new BaseIO();
			eventType.setCode(alertSubscriptionsDTO.getSubscriptionType().getCode());
			eventType.setName(alertSubscriptionsDTO.getSubscriptionType().getName());
			notificationSubscription.setSubscriptionType(eventType);

			List<UserIO> userList = new ArrayList<UserIO>();
			for (UserDTO userDTO : alertSubscriptionsDTO.getUserList()) {
				UserIO user = new UserIO();
				user.setCode(userDTO.getCode());
				user.setName(userDTO.getName());
				userList.add(user);
			}
			notificationSubscription.setUserList(userList);

			List<GroupIO> groupList = new ArrayList<GroupIO>();
			for (GroupDTO groupDTO : alertSubscriptionsDTO.getGroupList()) {
				GroupIO group = new GroupIO();
				group.setCode(groupDTO.getCode());
				group.setName(groupDTO.getName());
				groupList.add(group);
			}
			notificationSubscription.setGroupList(groupList);

			List<BaseIO> notificationMediumList = new ArrayList<BaseIO>();
			for (NotificationMediumEM notificationMedium : alertSubscriptionsDTO.getNotificationMediumList()) {
				BaseIO notification = new BaseIO();
				notification.setCode(notificationMedium.getCode());
				notification.setName(notificationMedium.getName());
				notificationMediumList.add(notification);
			}
			notificationSubscription.setMediumList(notificationMediumList);
			notificationSubscription.setActiveFlag(alertSubscriptionsDTO.getActiveFlag());
			alertSubscriptions.add(notificationSubscription);
		}
		return ResponseIO.success(alertSubscriptions);
	}

	@RequestMapping(value = "/subscription/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<NotificationSubscriptionIO> updateSubscription(@PathVariable("authtoken") String authtoken, @RequestBody NotificationSubscriptionIO alertSubscriptionsIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NotificationSubscriptionDTO subscriptionsDTO = new NotificationSubscriptionDTO();
		subscriptionsDTO.setCode(alertSubscriptionsIO.getCode());
		subscriptionsDTO.setSubscriptionType(alertSubscriptionsIO.getSubscriptionType() != null ? NotificationSubscriptionTypeEM.getSubscriptionTypeEM(alertSubscriptionsIO.getSubscriptionType().getCode()) : null);

		List<NotificationMediumEM> notificationMediums = new ArrayList<NotificationMediumEM>();
		if (alertSubscriptionsIO.getMediumList() != null) {
			for (BaseIO notification : alertSubscriptionsIO.getMediumList()) {
				notificationMediums.add(NotificationMediumEM.getNotificationMediumEM(notification.getCode()));
			}
		}
		subscriptionsDTO.setNotificationMediumList(notificationMediums);

		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (alertSubscriptionsIO.getGroupList() != null) {
			for (GroupIO group : alertSubscriptionsIO.getGroupList()) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(group.getCode());
				groupList.add(groupDTO);
			}
		}
		subscriptionsDTO.setGroupList(groupList);

		List<UserDTO> userList = new ArrayList<UserDTO>();
		if (alertSubscriptionsIO.getUserList() != null) {
			for (UserIO user : alertSubscriptionsIO.getUserList()) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(user.getCode());
				userList.add(userDTO);
			}
		}
		subscriptionsDTO.setUserList(userList);

		subscriptionsDTO.setActiveFlag(alertSubscriptionsIO.getActiveFlag());
		subscriptionsDTO = notificationService.updateSubscription(authDTO, subscriptionsDTO);
		NotificationSubscriptionIO subscriptionIO = new NotificationSubscriptionIO();
		subscriptionIO.setCode(subscriptionsDTO.getCode());
		subscriptionIO.setActiveFlag(subscriptionsDTO.getActiveFlag());
		return ResponseIO.success(subscriptionIO);
	}

	@RequestMapping(value = "/subscription/user/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateUserSubscription(@PathVariable("authtoken") String authtoken, @RequestBody List<BaseIO> subscriptionList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<NotificationSubscriptionDTO> subscriptionsDTOList = new ArrayList<NotificationSubscriptionDTO>();

		for (BaseIO subscription : subscriptionList) {
			NotificationSubscriptionTypeEM notificationSubcriptionType = NotificationSubscriptionTypeEM.getSubscriptionTypeEM(subscription.getCode());
			if (notificationSubcriptionType == null) {
				continue;
			}
			NotificationSubscriptionDTO subscriptionsDTO = new NotificationSubscriptionDTO();
			subscriptionsDTO.setSubscriptionType(NotificationSubscriptionTypeEM.getSubscriptionTypeEM(subscription.getCode()));
			subscriptionsDTO.setGroupList(new ArrayList<GroupDTO>());

			List<UserDTO> userList = new ArrayList<UserDTO>();
			userList.add(authDTO.getUser());
			subscriptionsDTO.setUserList(userList);

			List<NotificationMediumEM> notificationMediums = new ArrayList<NotificationMediumEM>();
			notificationMediums.add(NotificationMediumEM.EZEEBOT_APP);
			subscriptionsDTO.setNotificationMediumList(notificationMediums);

			subscriptionsDTO.setActiveFlag(subscription.getActiveFlag());
			subscriptionsDTOList.add(subscriptionsDTO);
		}
		notificationService.updateUserSubscription(authDTO, subscriptionsDTOList);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/subscription/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<BaseIO>> updateUserSubscription(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BaseIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<NotificationSubscriptionDTO> userAlertSubscriptionsList = notificationService.getUserSubscription(authDTO);
		for (NotificationSubscriptionDTO subscription : userAlertSubscriptionsList) {
			BaseIO subcriptionIO = new BaseIO();
			subcriptionIO.setCode(subscription.getSubscriptionType().getCode());
			subcriptionIO.setName(subscription.getSubscriptionType().getName());
			subcriptionIO.setActiveFlag(subscription.getActiveFlag());
			list.add(subcriptionIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/subscription/type", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getAllEventType(@PathVariable("authtoken") String authtoken) throws Exception {
		List<Map<String, String>> subscriptionsTypeList = new ArrayList<Map<String, String>>();
		authService.getAuthDTO(authtoken);
		for (NotificationSubscriptionTypeEM eventType : NotificationSubscriptionTypeEM.values()) {
			Map<String, String> subscriptionEvent = new HashMap<String, String>();
			subscriptionEvent.put("code", eventType.getCode());
			subscriptionEvent.put("name", eventType.getName());
			subscriptionEvent.put("level", String.valueOf(eventType.getLevel()));
			subscriptionsTypeList.add(subscriptionEvent);
		}
		return ResponseIO.success(subscriptionsTypeList);
	}

	@RequestMapping(value = "/subscription/authenticate/{deviceMedium}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> authenticateSubscription(@PathVariable("authtoken") String authtoken, @PathVariable("deviceMedium") String deviceMedium) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO authenticate = new BaseIO();
		String otp = authService.generateBotOTP(authDTO);
		authenticate.setCode(otp);
		return ResponseIO.success(authenticate);
	}

	@RequestMapping(value = "/subscription/authorize/revoke/{authenticateCode}/{deviceMedium}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> revokeAuthenticate(@PathVariable("authtoken") String authtoken, @PathVariable("authenticateCode") String authenticateCode, @PathVariable("deviceMedium") String deviceMedium) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		AppStoreDetailsDTO detailsDTO = new AppStoreDetailsDTO();
		detailsDTO.setCode(authenticateCode);
		detailsDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(deviceMedium));
		notificationService.revokeAuthenticateSubscription(authDTO, detailsDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/subscription/authorize/{deviceMedium}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<AppStoreDetailsIO>> getAuthenticateDetails(@PathVariable("authtoken") String authtoken, @PathVariable("deviceMedium") String deviceMedium) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<AppStoreDetailsIO> appStoreList = new ArrayList<AppStoreDetailsIO>();
		DeviceMediumEM deviceMediumEM = DeviceMediumEM.getDeviceMediumEM(deviceMedium);
		List<AppStoreDetailsDTO> list = notificationService.getAuthenticateSubscriptionDetails(authDTO, authDTO.getUser(), deviceMediumEM);
		for (AppStoreDetailsDTO storeDetails : list) {
			AppStoreDetailsIO appStore = new AppStoreDetailsIO();
			appStore.setCode(storeDetails.getCode());
			appStore.setUdid(storeDetails.getUdid());
			appStore.setGcmToken(storeDetails.getGcmToken());
			appStore.setModel(storeDetails.getModel());
			appStore.setOs(storeDetails.getOs());
			appStore.setActiveFlag(storeDetails.getActiveFlag());
			appStore.setCreatedAt(storeDetails.getAudit().getUpdatedAt());
			BaseIO device = new BaseIO();
			device.setCode(storeDetails.getDeviceMedium().getCode());
			appStore.setDeviceMedium(device);
			appStoreList.add(appStore);
		}
		return ResponseIO.success(appStoreList);
	}

	@RequestMapping(value = "/subscription/push/{subscriptionType}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> pushNotificationSubscriptions(@PathVariable("authtoken") String authtoken, @PathVariable("subscriptionType") String subscriptionType, @RequestBody Map<String, String> content) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NotificationSubscriptionTypeEM subscriptionTypeEM = NotificationSubscriptionTypeEM.getSubscriptionTypeEM(subscriptionType);
		if (subscriptionTypeEM == null || !authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(subscriptionTypeEM)) {
			throw new ServiceException(ErrorCode.NOTIFICATION_SUBSCRIPTION_NOT_ENABLED);
		}
		notificationPushService.pushNotificationSubscriptions(authDTO, subscriptionTypeEM, content);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/subscription/report", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> notificationSubscriptionsReport(@PathVariable("authtoken") String authtoken, String subscriptionTypeCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NotificationSubscriptionTypeEM subscriptionTypeEM = StringUtil.isNotNull(subscriptionTypeCode) ? NotificationSubscriptionTypeEM.getSubscriptionTypeEM(subscriptionTypeCode) : null;
		List<Map<String, String>> list = notificationService.notificationSubscriptionReport(authDTO, subscriptionTypeEM);
		return ResponseIO.success(list);
	}
}
