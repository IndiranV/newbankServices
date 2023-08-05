package org.in.com.controller.web;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.AppStoreDetailsIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserCustomerIO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.UserCustomerService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/user/customer")
public class UserCustomerController extends BaseController {
	@Autowired
	UserCustomerService userCustomerService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserCustomerIO> updateUserCustomer(@PathVariable("authtoken") String authtoken, @RequestBody UserCustomerIO userCustomerIO) throws Exception {
		UserCustomerIO userCustomer = new UserCustomerIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
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

		}
		return ResponseIO.success(userCustomer);
	}

	@RequestMapping(value = "/app/store/details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AppStoreDetailsIO> appStoreUpdate(@PathVariable("authtoken") String authtoken, @RequestBody AppStoreDetailsIO storeDetails) throws Exception {
		AppStoreDetailsIO userAppDetails = new AppStoreDetailsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
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
		}
		return ResponseIO.success(userAppDetails);
	}

}
