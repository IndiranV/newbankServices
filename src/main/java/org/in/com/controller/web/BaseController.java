package org.in.com.controller.web;

import org.in.com.constants.Constants;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.impl.BaseImpl;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController extends BaseImpl {
	@Autowired
	protected AuthService authService;

	protected boolean ValidateDeviceMedium(String deviceMedium) {
		if (StringUtil.isNotNull(deviceMedium) && (Constants.DEVICE_MEDIUM_WEB.equals(deviceMedium) || Constants.DEVICE_MEDIUM_MOB.equals(deviceMedium) || Constants.DEVICE_MEDIUM_APP.equals(deviceMedium))) {
			return true;
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_DEVICE_MEDIUM);
		}
	}

	protected boolean ValidateAuthenticationType(String authenticationTypeCode) {
		if (StringUtil.isNotNull(authenticationTypeCode) && AuthenticationTypeEM.getAuthenticationTypeEM(authenticationTypeCode) != null) {
			return true;
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}
	}
}
