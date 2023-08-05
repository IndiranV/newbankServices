package org.in.com.controller.web;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationAreaIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.UserDetailsIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserDetailsDTO;
import org.in.com.service.UserDetailsService;
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
@RequestMapping("/{authtoken}/user/details")
public class UserDetailsController extends BaseController {

	@Autowired
	UserDetailsService userDetailsService;

	@RequestMapping(value = "/{userCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserDetailsIO> getUserDetails(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		UserDetailsIO userDetailsIO = new UserDetailsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userCode);
		UserDetailsDTO userDetailsDTO = userDetailsService.getUserDetails(authDTO, userDTO);
		if (StringUtil.isNotNull(userDetailsDTO.getCode())) {
			userDetailsIO.setCode(userDetailsDTO.getCode());
			userDetailsIO.setAddress1(userDetailsDTO.getAddress1());
			userDetailsIO.setAddress2(userDetailsDTO.getAddress2());
			userDetailsIO.setLandmark(userDetailsDTO.getLandmark());
			userDetailsIO.setPincode(userDetailsDTO.getPincode());
			userDetailsIO.setActiveFlag(userDetailsDTO.getActiveFlag());

			UserIO userIO = new UserIO();
			userIO.setCode(userDTO.getCode());
			userIO.setName(userDTO.getName());
			userDetailsIO.setUser(userIO);

			StationIO stationIO = new StationIO();
			stationIO.setCode(userDetailsDTO.getStation().getCode());
			stationIO.setName(userDetailsDTO.getStation().getName());
			userDetailsIO.setStation(stationIO);

			StateIO stateIO = new StateIO();
			stateIO.setCode(userDetailsDTO.getState().getCode());
			stateIO.setName(userDetailsDTO.getState().getName());
			userDetailsIO.setState(stateIO);

			StationAreaDTO stationAreaDTO = userDetailsDTO.getStationArea();
			StationAreaIO stationAreaIO = new StationAreaIO();
			if (stationAreaDTO != null && stationAreaDTO.getId() != 0) {
				stationAreaIO.setCode(stationAreaDTO.getCode());
				stationAreaIO.setName(stationAreaDTO.getName());
				stationAreaIO.setLatitude(StringUtil.isNull(stationAreaDTO.getLatitude(), Text.EMPTY));
				stationAreaIO.setLongitude(StringUtil.isNull(stationAreaDTO.getLongitude(), Text.EMPTY));
				stationAreaIO.setRadius(stationAreaDTO.getRadius());
				stationAreaIO.setActiveFlag(stationAreaDTO.getActiveFlag());
			}
			userDetailsIO.setStationArea(stationAreaIO);
		}
		return ResponseIO.success(userDetailsIO);

	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserDetailsIO> getUpdateUserDetails(@PathVariable("authtoken") String authtoken, @RequestBody UserDetailsIO userDetailsIO) throws Exception {
		UserDetailsIO userDetails = new UserDetailsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDetailsDTO userDetilsDTO = new UserDetailsDTO();
		userDetilsDTO.setCode(userDetailsIO.getCode());
		userDetilsDTO.setAddress1(userDetailsIO.getAddress1());
		userDetilsDTO.setAddress2(userDetailsIO.getAddress2());
		userDetilsDTO.setLandmark(userDetailsIO.getLandmark());
		userDetilsDTO.setPincode(userDetailsIO.getPincode());
		userDetilsDTO.setActiveFlag(userDetailsIO.getActiveFlag());

		UserDTO userDTO = new UserDTO();
		if (userDetailsIO.getUser() != null) {
			userDTO.setCode(userDetailsIO.getUser().getCode());
		}
		userDetilsDTO.setUser(userDTO);

		StationDTO stationDTO = new StationDTO();
		if (userDetailsIO.getStation() != null) {
			stationDTO.setCode(userDetailsIO.getStation().getCode());
		}
		userDetilsDTO.setStation(stationDTO);

		StateDTO stateDTO = new StateDTO();
		if (userDetailsIO.getState() != null) {
			stateDTO.setCode(userDetailsIO.getState().getCode());
		}
		userDetilsDTO.setState(stateDTO);

		StationAreaDTO stationAreaDTO = new StationAreaDTO();
		if (userDetailsIO.getStationArea() != null) {
			stationAreaDTO.setCode(userDetailsIO.getStationArea().getCode());
		}
		userDetilsDTO.setStationArea(stationAreaDTO);

		UserDetailsDTO userDetailsDTO2 = userDetailsService.Update(authDTO, userDetilsDTO);
		if (userDetailsDTO2 != null) {
			userDetails.setCode(userDetailsDTO2.getCode());
			userDetails.setActiveFlag(userDetailsDTO2.getActiveFlag());
		}
		return ResponseIO.success(userDetails);
	}
}
