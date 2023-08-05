package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.UserTaxDetailsIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTaxDetailsDTO;
import org.in.com.service.UserTaxDetailsService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/user/tax/details")
public class UserTaxDetailsController extends BaseController {

	@Autowired
	UserTaxDetailsService userTaxService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateUserTaxDetails(@PathVariable("authtoken") String authtoken, @RequestBody UserTaxDetailsIO userTaxDetailsIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserTaxDetailsDTO userTaxDetailsDTO = new UserTaxDetailsDTO();

		userTaxDetailsDTO.setFromDate(DateUtil.getDateTime(userTaxDetailsIO.getFromDate()));
		userTaxDetailsDTO.setTdsTaxValue(userTaxDetailsIO.getTdsTaxValue());
		userTaxDetailsDTO.setPanCardCode(userTaxDetailsIO.getPanCardCode());

		UserDTO userDTO = new UserDTO();
		if (userTaxDetailsIO.getUser() != null) {
			userDTO.setCode(userTaxDetailsIO.getUser().getCode());
		}
		userTaxDetailsDTO.setUser(userDTO);

		userTaxDetailsDTO.setActiveFlag(userTaxDetailsIO.getActiveFlag());
		userTaxService.Update(authDTO, userTaxDetailsDTO);

		return ResponseIO.success();
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserTaxDetailsIO>> getAllUserTaxDetails(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<UserTaxDetailsIO> userTaxList = new ArrayList<UserTaxDetailsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<UserTaxDetailsDTO> list = userTaxService.getAll(authDTO);
		for (UserTaxDetailsDTO userTaxDetailsDTO : list) {
			if (activeFlag != -1 && activeFlag != userTaxDetailsDTO.getActiveFlag()) {
				continue;
			}
			UserTaxDetailsIO userTaxDetailsIO = new UserTaxDetailsIO();
			userTaxDetailsIO.setFromDate(DateUtil.convertDate(userTaxDetailsDTO.getFromDate()));
			userTaxDetailsIO.setTdsTaxValue(userTaxDetailsDTO.getTdsTaxValue());
			userTaxDetailsIO.setPanCardCode(userTaxDetailsDTO.getPanCardCode());

			UserIO userIO = new UserIO();
			userIO.setCode(userTaxDetailsDTO.getUser().getCode());
			userIO.setName(userTaxDetailsDTO.getUser().getName());
			userTaxDetailsIO.setUser(userIO);

			userTaxDetailsIO.setActiveFlag(userTaxDetailsDTO.getActiveFlag());
			userTaxList.add(userTaxDetailsIO);
		}
		return ResponseIO.success(userTaxList);
	}

	@RequestMapping(value = "/{userCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserTaxDetailsIO> getUserTaxDetails(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		UserTaxDetailsIO userTaxDetailsIO = new UserTaxDetailsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userCode);
		UserTaxDetailsDTO userTaxDetailsDTO = userTaxService.getUserTaxDetails(authDTO, userDTO);

		userTaxDetailsIO.setFromDate(DateUtil.convertDate(userTaxDetailsDTO.getFromDate()));
		userTaxDetailsIO.setTdsTaxValue(userTaxDetailsDTO.getTdsTaxValue());
		userTaxDetailsIO.setPanCardCode(userTaxDetailsDTO.getPanCardCode());

		UserIO userIO = new UserIO();
		if (userTaxDetailsDTO.getUser() != null) {
			userIO.setCode(userTaxDetailsDTO.getUser().getCode());
			userIO.setName(userTaxDetailsDTO.getUser().getName());
		}
		userTaxDetailsIO.setUser(userIO);

		userTaxDetailsIO.setActiveFlag(userTaxDetailsDTO.getActiveFlag());

		return ResponseIO.success(userTaxDetailsIO);

	}
}
