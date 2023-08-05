package org.in.com.controller.api;

import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.AppStoreDetailsIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserFeedbackIO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.NotificationService;
import org.in.com.service.UserService;
import org.in.com.utils.StringUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

@Controller
@RequestMapping("/api/bot/notification")
public class BotAPIController extends BaseController {
	@Autowired
	AuthService authService;
	@Autowired
	UserService userService;
	@Autowired
	NotificationService notificatinoService;

	@RequestMapping(value = "/authenticate/otp/{otp}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONObject> subscriptionOTPDetails(@PathVariable("otp") String otp) throws Exception {
		JSONObject content = authService.getBotOTP(otp);
		return ResponseIO.success(content);
	}

	@RequestMapping(value = "/authenticate/subscription/{authtoken}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> subscriptionStoreUpdate(@PathVariable("authtoken") String authtoken, @RequestBody AppStoreDetailsIO storeDetails) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (storeDetails == null || StringUtil.isNull(storeDetails.getUdid()) || StringUtil.isNull(storeDetails.getGcmToken()) || StringUtil.isNull(storeDetails.getCode())) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		storeDetails.setCode(storeDetails.getCode().replace(authDTO.getNamespaceCode(), Text.EMPTY));
		if (!authDTO.getUserCode().equals(storeDetails.getCode())) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		if (!authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		UserDTO userDTO = new UserDTO();
		userDTO.setCode(authDTO.getUserCode());
		AppStoreDetailsDTO appStoreDetails = new AppStoreDetailsDTO();
		appStoreDetails.setUdid(storeDetails.getUdid());
		appStoreDetails.setGcmToken(storeDetails.getGcmToken());
		appStoreDetails.setModel(storeDetails.getModel());
		appStoreDetails.setOs(storeDetails.getOs());
		appStoreDetails.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(storeDetails.getDeviceMedium().getCode()));
		appStoreDetails.setActiveFlag(storeDetails.getActiveFlag());
		userDTO.setAppStoreDetails(appStoreDetails);
		userService.appStoreUpdate(authDTO, userDTO);
		BaseIO base = new BaseIO();
		base.setCode(userDTO.getAppStoreDetails().getCode());
		base.setActiveFlag(userDTO.getAppStoreDetails().getActiveFlag());
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/revoke/subscription/{subscriptionCode}/namespace/{namespaceCode}/user/{userCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> revokeSubscription(@PathVariable("subscriptionCode") String subscriptionCode, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("userCode") String userCode) throws Exception {
		// System.out.println("ezeebot: " + subscriptionCode + " - " +
		// namespaceCode + " - " + userCode);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/message/{messageCode}/namespace/{namespaceCode}/user/{userCode}/{status}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> messageStatus(@PathVariable("messageCode") String messageCode, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("userCode") String userCode, @PathVariable("status") String status) throws Exception {
		BaseIO base = new BaseIO();
		// System.out.println("ezeebot: " + messageCode + " - " + namespaceCode
		// + " - " + userCode);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/feedback/namespace/{namespaceCode}/user/{userCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> feedback(@PathVariable("namespaceCode") String namespaceCode, @PathVariable("userCode") String userCode, @RequestBody UserFeedbackIO feedback) throws Exception {

		Map<String, String> dataModel = Maps.newHashMap();
		dataModel.put("namespace", namespaceCode);
		dataModel.put("userFirstName", userCode);
		dataModel.put("feedbackName", feedback.getName());
		dataModel.put("comments", feedback.getComments());

		notificatinoService.sendEzeebotFeedbackMail(dataModel);
		return ResponseIO.success();
	}

}
