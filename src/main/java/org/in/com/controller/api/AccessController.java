package org.in.com.controller.api;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.NamespaceProfileIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceService;
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
@RequestMapping("/api/{accessToken}")
public class AccessController {

	private final static String ACCESS_TOKEN = "HXXJFEHP79Q69NZP";
	
	@Autowired
	NamespaceService namespaceService;
	
	@RequestMapping(value = "/namespace/whatsapp/{namespaceCode}/verify", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> verifyNamespaceWhatsapp(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @RequestBody NamespaceProfileIO profileIO) throws Exception {
		if (!ACCESS_TOKEN.equals(accessToken) || StringUtil.isNull(namespaceCode)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (!StringUtil.isValidMobileNumber(profileIO.getWhatsappNumber())) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		
		NamespaceProfileDTO profileDTO = new NamespaceProfileDTO();
		profileDTO.setWhatsappNumber(profileIO.getWhatsappNumber());
		profileDTO.setWhatsappUrl(profileIO.getWhatsappUrl());
		namespaceService.updateNamespaceWhatsapp(authDTO, profileDTO, Numeric.ONE_INT);
		return ResponseIO.success();
	}
}
