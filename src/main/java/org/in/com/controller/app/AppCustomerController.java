package org.in.com.controller.app;

import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.AppStoreDetailsIO;
import org.in.com.controller.web.io.AuthIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.UserCustomerIO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserCustomerAuthDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.UserCustomerService;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

//@Controller
//@RequestMapping("/api/3.0/app/{authtoken}/customer")
public class AppCustomerController extends BaseController {

	@Autowired
	UserCustomerService userCustomerService;
	private static final Logger applogger = LoggerFactory.getLogger(AppCustomerController.class);

	@RequestMapping(value = "/{mobileNumber}/otp/generate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> generateCustomerOTP(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (!StringUtil.isValidMobileNumber(mobileNumber)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			authService.generateCustomerOTP(authDTO, mobileNumber);
			baseIO.setCode(mobileNumber);
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/{mobileNumber}/validate/otp/{otpNumber}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> validateCustomerOTP(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber, @PathVariable("otpNumber") int otpNumber) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (!StringUtil.isValidMobileNumber(mobileNumber)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			AuthDTO authCache = authService.getCustomerAuthendticationV2(authDTO, mobileNumber, otpNumber);
			authIO.setAuthToken(authCache.getAuthToken());
			authIO.setSessionToken(authCache.getUserCustomer().getUserCustomerAuth().getSessionToken());
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{mobileNumber}/validate/sessiontoken/{sessionToken}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> getAuthBySessiontoken(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber, @PathVariable("sessionToken") String sessionToken) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
		userCustomerDTO.setMobile(mobileNumber);
		UserCustomerAuthDTO customerAuthDTO = new UserCustomerAuthDTO();
		customerAuthDTO.setDeviceMedium(authDTO.getDeviceMedium());
		customerAuthDTO.setSessionToken(sessionToken);
		userCustomerDTO.setUserCustomerAuth(customerAuthDTO);

		AuthDTO authCache = authService.getCustomerAuthendticationV3(authDTO, userCustomerDTO);
		authIO.setAuthToken(authCache.getAuthToken());
		authIO.setSessionToken(authCache.getUserCustomer().getUserCustomerAuth().getSessionToken());

		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/profile/details/{mobileNumber}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserCustomerIO> getUserCustomer(@PathVariable("authtoken") String authtoken) throws Exception {
		UserCustomerIO userIO = new UserCustomerIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO.getUserCustomer() == null || authDTO.getUserCustomer().getId() == Numeric.ZERO_INT) {
			throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
		}
		UserCustomerDTO userCustomerDTO = userCustomerService.getUserCustomer(authDTO, authDTO.getUserCustomer());

		if (!userCustomerDTO.getMobile().equals(authDTO.getUserCustomer().getMobile())) {
			throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
		}

		userIO.setName(userCustomerDTO.getName());
		userIO.setEmail(userCustomerDTO.getEmail());
		userIO.setLastname(userCustomerDTO.getLastname());
		userIO.setMobile(userCustomerDTO.getMobile());
		userIO.setCode(userCustomerDTO.getCode());
		userIO.setActiveFlag(userCustomerDTO.getActiveFlag());

		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/profile/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserCustomerIO> updateUserCustomer(@PathVariable("authtoken") String authtoken, @RequestBody UserCustomerIO userCustomerIO) throws Exception {
		UserCustomerIO userCustomer = new UserCustomerIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (!StringUtil.isValidMobileNumber(userCustomerIO.getMobile())) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		if (StringUtil.isNotNull(userCustomerIO.getName()) && userCustomerIO.getName().length() < 3) {
			throw new ServiceException(ErrorCode.USER_INVALID_USERNAME, "Invalid User First Name, length should be more than 2 characters.");
		}
		UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
		userCustomerDTO.setCode(userCustomerIO.getCode());
		userCustomerDTO.setName(userCustomerIO.getName());
		userCustomerDTO.setLastname(userCustomerIO.getLastname());
		userCustomerDTO.setEmail(userCustomerIO.getEmail());
		userCustomerDTO.setMobile(userCustomerIO.getMobile());
		userCustomerDTO.setActiveFlag(userCustomerIO.getActiveFlag());

		userCustomerService.updateUserCustomer(authDTO, userCustomerDTO);
		userCustomer.setCode(userCustomerDTO.getCode());
		userCustomer.setName(userCustomerDTO.getName());
		userCustomer.setActiveFlag(userCustomerDTO.getActiveFlag());
		return ResponseIO.success(userCustomer);
	}

	@RequestMapping(value = "/store/details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AppStoreDetailsIO> appStoreUpdate(@PathVariable("authtoken") String authtoken, @RequestBody AppStoreDetailsIO storeDetails) throws Exception {
		AppStoreDetailsIO userAppDetails = new AppStoreDetailsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (storeDetails == null || StringUtil.isNull(storeDetails.getUdid()) || StringUtil.isNull(storeDetails.getGcmToken())) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		if (authDTO.getUserCustomer() == null || authDTO.getUserCustomer().getId() == Numeric.ZERO_INT) {
			throw new ServiceException(ErrorCode.UPDATE_FAIL);
		}
		UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
		userCustomerDTO.setId(authDTO.getUserCustomer().getId());

		AppStoreDetailsDTO appStoreDetails = new AppStoreDetailsDTO();
		appStoreDetails.setUdid(storeDetails.getUdid());
		appStoreDetails.setGcmToken(storeDetails.getGcmToken());
		appStoreDetails.setModel(storeDetails.getModel());
		appStoreDetails.setOs(storeDetails.getOs());
		appStoreDetails.setDeviceMedium(DeviceMediumEM.APP_USER);
		appStoreDetails.setActiveFlag(storeDetails.getActiveFlag());
		userCustomerDTO.setAppStoreDetails(appStoreDetails);

		userCustomerService.appStoreUpdate(authDTO, userCustomerDTO);
		userAppDetails.setCode(userCustomerDTO.getAppStoreDetails().getCode());
		userAppDetails.setActiveFlag(userCustomerDTO.getAppStoreDetails().getActiveFlag());
		return ResponseIO.success(userAppDetails);
	}

	private void validateMandatory(String accessToken) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}
}
