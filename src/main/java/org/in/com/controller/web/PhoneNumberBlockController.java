package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.PhoneNumberBlockIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PhoneNumberBlockDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.PhoneNumberBlockService;
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
@RequestMapping(value = "/{authtoken}/ticket/phone/number/block")
public class PhoneNumberBlockController extends BaseController {

	@Autowired
	PhoneNumberBlockService phoneNumberBlockService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PhoneNumberBlockIO>> getAllMobileBlock(@PathVariable("authtoken") String authtoken) throws Exception {
		List<PhoneNumberBlockIO> phoneNumberBlockList = new ArrayList<PhoneNumberBlockIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<PhoneNumberBlockDTO> list = phoneNumberBlockService.getAll(authDTO);
		for (PhoneNumberBlockDTO mobileBlock : list) {
			PhoneNumberBlockIO mobileBlockIO = new PhoneNumberBlockIO();
			mobileBlockIO.setMobile(mobileBlock.getMobile());
			mobileBlockIO.setRemarks(mobileBlock.getRemarks());
			mobileBlockIO.setActiveFlag(mobileBlock.getActiveFlag());
			phoneNumberBlockList.add(mobileBlockIO);
		}
		return ResponseIO.success(phoneNumberBlockList);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updatePhoneNumberBlock(@PathVariable("authtoken") String authtoken, @RequestBody PhoneNumberBlockIO phoneNumberBlock) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		if (!StringUtil.isValidMobileNumber(phoneNumberBlock.getMobile())) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}

		PhoneNumberBlockDTO phoneNumberBlockDTO = new PhoneNumberBlockDTO();
		phoneNumberBlockDTO.setMobile(phoneNumberBlock.getMobile());
		phoneNumberBlockDTO.setRemarks(phoneNumberBlock.getRemarks());
		phoneNumberBlockDTO.setActiveFlag(phoneNumberBlock.getActiveFlag());
		phoneNumberBlockService.Update(authDTO, phoneNumberBlockDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{mobile}/validate", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> validatePhoneNumberBlock(@PathVariable("authtoken") String authtoken, @PathVariable("mobile") String mobile) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (!StringUtil.isValidMobileNumber(mobile)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		PhoneNumberBlockDTO phoneNumberBlockDTO = new PhoneNumberBlockDTO();
		phoneNumberBlockDTO.setMobile(mobile);
		phoneNumberBlockService.validatePhoneNumberBlock(authDTO, phoneNumberBlockDTO);
		return ResponseIO.success();
	}
}
