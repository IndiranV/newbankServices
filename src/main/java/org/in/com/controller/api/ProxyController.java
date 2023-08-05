package org.in.com.controller.api;

import org.in.com.config.ApplicationConfig;
import org.in.com.dto.ProxyDTO;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ProxyService;
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
@RequestMapping(value = "/api/json/{accessToken}/proxy")
public class ProxyController {

	@Autowired
	ProxyService proxyService;

	@RequestMapping(value = "/request", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String registrationAccount(@PathVariable("accessToken") String accessToken, @RequestBody ProxyDTO proxy) throws Exception {

		validateMandatory(accessToken);
		String response = proxyService.processRequest(proxy);

		return response;
	}

	private void validateMandatory(String accessToken) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}
}
