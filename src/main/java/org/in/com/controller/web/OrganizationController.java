package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.service.OrganizationService;
import org.in.com.utils.StringUtil;
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
@RequestMapping("/{authtoken}/organization")
public class OrganizationController extends BaseController {
	@Autowired
	OrganizationService organizationService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<OrganizationIO>> getAllOrganizations(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		List<OrganizationIO> organizations = new ArrayList<OrganizationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<OrganizationDTO> list = (List<OrganizationDTO>) organizationService.getAll(authDTO);
			for (OrganizationDTO organizationDTO : list) {
				if (activeFlag != -1 && activeFlag != organizationDTO.getActiveFlag()) {
					continue;
				}
				OrganizationIO organizaionio = new OrganizationIO();
				organizaionio.setCode(organizationDTO.getCode());
				organizaionio.setName(organizationDTO.getName());
				organizaionio.setShortCode(organizationDTO.getShortCode());
				organizaionio.setAddress1(organizationDTO.getAddress1());
				organizaionio.setAddress2(organizationDTO.getAddress2());
				organizaionio.setContact(organizationDTO.getContact());
				organizaionio.setPincode(organizationDTO.getPincode());
				organizaionio.setLatitude(organizationDTO.getLatitude());
				organizaionio.setLongitude(organizationDTO.getLongitude());
				organizaionio.setWorkingMinutes(organizationDTO.getWorkingMinutes());
				StationIO stationIO = new StationIO();
				StateIO stateIO = new StateIO();
				stateIO.setCode(organizationDTO.getStation().getState().getCode());
				stateIO.setName(organizationDTO.getStation().getState().getName());
				stationIO.setState(stateIO);
				stationIO.setName(organizationDTO.getStation().getName());
				stationIO.setCode(organizationDTO.getStation().getCode());
				organizaionio.setStation(stationIO);
				organizaionio.setActiveFlag(organizationDTO.getActiveFlag());
				organizaionio.setUserCount(organizationDTO.getUserCount());
				organizations.add(organizaionio);
			}

			// Sort
			Comparator<OrganizationIO> comp = new BeanComparator("name");
			Collections.sort(organizations, comp);
		}
		return ResponseIO.success(organizations);
	}

	@RequestMapping(value = "/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<OrganizationIO>> getOrganizationsUsers(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		List<OrganizationIO> organizations = new ArrayList<OrganizationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			OrganizationDTO dto = new OrganizationDTO();
			dto.setCode(code);
			List<OrganizationDTO> list = (List<OrganizationDTO>) organizationService.get(authDTO, dto);
			for (OrganizationDTO organizationDTO : list) {
				OrganizationIO organizaionio = new OrganizationIO();
				organizaionio.setCode(organizationDTO.getCode());
				organizaionio.setName(organizationDTO.getName());
				organizaionio.setShortCode(organizationDTO.getShortCode());
				organizaionio.setAddress1(organizationDTO.getAddress1());
				organizaionio.setAddress2(organizationDTO.getAddress2());
				organizaionio.setContact(organizationDTO.getContact());
				organizaionio.setPincode(organizationDTO.getPincode());
				organizaionio.setLatitude(organizationDTO.getLatitude());
				organizaionio.setLongitude(organizationDTO.getLongitude());
				organizaionio.setWorkingMinutes(organizationDTO.getWorkingMinutes());
				StationIO stationIO = new StationIO();
				StateIO stateIO = new StateIO();
				stateIO.setCode(organizationDTO.getStation().getState().getCode());
				stateIO.setName(organizationDTO.getStation().getState().getName());
				stationIO.setState(stateIO);
				stationIO.setName(organizationDTO.getStation().getName());
				stationIO.setCode(organizationDTO.getStation().getCode());
				organizaionio.setStation(stationIO);
				organizaionio.setActiveFlag(organizationDTO.getActiveFlag());
				organizations.add(organizaionio);
			}
		}
		return ResponseIO.success(organizations);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<OrganizationIO> getUpdateUID(@PathVariable("authtoken") String authtoken, @RequestBody OrganizationIO organizationIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(organizationIO.getCode());
			organizationDTO.setName(organizationIO.getName());
			organizationDTO.setShortCode(organizationIO.getShortCode());
			organizationDTO.setAddress1(organizationIO.getAddress1());
			organizationDTO.setAddress2(organizationIO.getAddress2());
			organizationDTO.setContact(organizationIO.getContact());
			organizationDTO.setPincode(organizationIO.getPincode());
			organizationDTO.setWorkingMinutes(organizationIO.getWorkingMinutes());
			organizationDTO.setLatLon(StringUtil.isNotNull(organizationIO.getLatitude()) && StringUtil.isNotNull(organizationIO.getLongitude()) ? organizationIO.getLatitude() + Text.COMMA + organizationIO.getLongitude() : Numeric.ZERO + Text.COMMA + Numeric.ZERO);
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(organizationIO.getStation() != null ? organizationIO.getStation().getCode() : null);
			organizationDTO.setStation(stationDTO);
			organizationDTO.setActiveFlag(organizationIO.getActiveFlag());
			organizationService.Update(authDTO, organizationDTO);
			if (organizationDTO.getCode() != null) {
				organizationIO.setCode(organizationDTO.getCode());
				organizationIO.setActiveFlag(organizationDTO.getActiveFlag());
			}
		}

		return ResponseIO.success(organizationIO);
	}

	@RequestMapping(value = "/{organizationCode}/{usercode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> MapUserGroup(@PathVariable("authtoken") String authtoken, @PathVariable("organizationCode") String groupcode, @PathVariable("usercode") String usercode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(groupcode);
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(usercode);
			organizationService.Update(authDTO, organizationDTO, userDTO);
		}
		return ResponseIO.success();
	}

}