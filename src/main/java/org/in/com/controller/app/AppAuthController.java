package org.in.com.controller.app;

import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.AuthIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.dto.AuthDTO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.ehcache.Element;

//@Controller
//@RequestMapping("/api/3.0/app/authentication/{accesstoken}")
public class AppAuthController extends BaseController {

	@Autowired
	UserCustomerService userCustomerService;
	private static final Logger applogger = LoggerFactory.getLogger(AppAuthController.class);

	@RequestMapping(value = "/authtoken/{namespaceCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AuthIO> getAppGuestAuthToken(@PathVariable("accesstoken") String contextToken, @PathVariable("namespaceCode") String namespaceCode, @RequestParam(value = "devicemedium", required = true, defaultValue = "WEB") String devicemedium) throws Exception {
		validateMandatory(contextToken);
		if (StringUtil.isNull(namespaceCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		DeviceMediumEM medium = DeviceMediumEM.getDeviceMediumEM(devicemedium);
		if (medium == null || medium.getId() == DeviceMediumEM.ALL_USER.getId()) {
			throw new ServiceException(ErrorCode.INVALID_DEVICE_MEDIUM);
		}
		if (medium.getId() != DeviceMediumEM.WEB_USER.getId() && medium.getId() != DeviceMediumEM.MOB_USER.getId() && medium.getId() != DeviceMediumEM.APP_USER.getId() && medium.getId() != DeviceMediumEM.APP_IOS.getId() && medium.getId() != DeviceMediumEM.APP_AND.getId()) {
			throw new ServiceException(ErrorCode.INVALID_DEVICE_MEDIUM);
		}
		if (medium.getId() == DeviceMediumEM.APP_IOS.getId() || medium.getId() == DeviceMediumEM.APP_AND.getId()) {
			medium = DeviceMediumEM.APP_USER;
		}
		AuthIO authIO = new AuthIO();
		String authToken = null;
		AuthDTO authDTO = null;
		String guestKey = StringUtil.removeSymbol(namespaceCode + medium.getCode());
		Element guestElement = EhcacheManager.getGuestAuthTokenEhCache().get(guestKey);
		if (guestElement != null) {
			authToken = (String) guestElement.getObjectValue();
			authDTO = authService.getAuthDTO(authToken);
		}
		if (authDTO == null) {
			authDTO = authService.getGuestAuthendticationV2(namespaceCode, DeviceMediumEM.APP_USER, contextToken);
		}
		if (authDTO != null) {
			authIO.setAuthToken(authDTO.getAuthToken());
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/verify/{authtoken}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> switchNamespace(@PathVariable("accesstoken") String accessToken, @PathVariable("authtoken") String authtoken) throws Exception {
		validateMandatory(accessToken);
		authService.getAuthDTO(authtoken);
		return ResponseIO.success();
	}

	private void validateMandatory(String contextToken) {
		if (StringUtil.isNull(contextToken)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}
}
