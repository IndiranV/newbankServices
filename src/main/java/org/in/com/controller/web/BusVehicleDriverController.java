package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.VehicleAttendantIO;
import org.in.com.controller.web.io.VehicleDriverIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AttendantCategoryEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusVehicleDriverService;
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
@RequestMapping("{authtoken}/bus/vehicle")
public class BusVehicleDriverController extends BaseController {
	@Autowired
	AuthService authService;
	@Autowired
	BusVehicleDriverService driverService;

	@RequestMapping(value = "/driver", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleDriverIO>> getAllDrivers(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		List<VehicleDriverIO> vehicleDrivers = new ArrayList<VehicleDriverIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<BusVehicleDriverDTO> list = driverService.getAll(authDTO);
			for (BusVehicleDriverDTO driverDTO : list) {
				if (activeFlag != -1 && activeFlag != driverDTO.getActiveFlag()) {
					continue;
				}
				VehicleDriverIO vehicleDriver = new VehicleDriverIO();
				vehicleDriver.setCode(driverDTO.getCode());
				vehicleDriver.setName(driverDTO.getName());
				vehicleDriver.setLastName(driverDTO.getLastName());
				vehicleDriver.setDateOfBirth(driverDTO.getDateOfBirth());
				vehicleDriver.setBloodGroup(driverDTO.getBloodGroup());
				vehicleDriver.setLicenseNumber(driverDTO.getLicenseNumber());
				vehicleDriver.setBadgeNumber(driverDTO.getBadgeNumber());
				vehicleDriver.setLicenseExpiryDate(driverDTO.getLicenseExpiryDate());
				vehicleDriver.setQualification(driverDTO.getQualification());
				vehicleDriver.setEmployeeCode(driverDTO.getEmployeeCode());
				vehicleDriver.setMobileNumber(driverDTO.getMobileNumber());
				vehicleDriver.setEmergencyContactNumber(driverDTO.getEmergencyContactNumber());
				vehicleDriver.setAadharNo(driverDTO.getAadharNo());
				vehicleDriver.setJoiningDate(driverDTO.getJoiningDate());
				vehicleDriver.setLastAssignedDate(driverDTO.getLastAssignedDateToString());
				vehicleDriver.setAssignedTripsCount(driverDTO.getAssignedTripsCount());
				vehicleDriver.setRemarks(driverDTO.getRemarks());
				vehicleDriver.setActiveFlag(driverDTO.getActiveFlag());

				UserIO userIO = new UserIO();
				if (driverDTO.getUser().getId() != 0) {
					userIO.setCode(driverDTO.getUser().getCode());
					userIO.setName(driverDTO.getUser().getName());
				}
				vehicleDriver.setUser(userIO);

				vehicleDrivers.add(vehicleDriver);
			}
		}
		return ResponseIO.success(vehicleDrivers);
	}

	@RequestMapping(value = "/driver/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<VehicleDriverIO> updateDriver(@PathVariable("authtoken") String authtoken, @RequestBody VehicleDriverIO driver) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		VehicleDriverIO vehicleDriver = new VehicleDriverIO();
		if (authDTO != null) {
			BusVehicleDriverDTO driverDTO = new BusVehicleDriverDTO();
			driverDTO.setCode(driver.getCode());
			driverDTO.setName(driver.getName());
			driverDTO.setLastName(driver.getLastName());
			driverDTO.setDateOfBirth(driver.getDateOfBirth());
			driverDTO.setBloodGroup(driver.getBloodGroup());
			driverDTO.setLicenseNumber(driver.getLicenseNumber());
			driverDTO.setBadgeNumber(driver.getBadgeNumber());
			driverDTO.setLicenseExpiryDate(driver.getLicenseExpiryDate());
			driverDTO.setQualification(driver.getQualification());
			driverDTO.setEmployeeCode(driver.getEmployeeCode());
			driverDTO.setMobileNumber(driver.getMobileNumber());
			driverDTO.setEmergencyContactNumber(driver.getEmergencyContactNumber());
			driverDTO.setAadharNo(driver.getAadharNo());
			driverDTO.setJoiningDate(driver.getJoiningDate());
			driverDTO.setRemarks(driver.getRemarks());
			driverDTO.setActiveFlag(driver.getActiveFlag());
			driverService.Update(authDTO, driverDTO);
			vehicleDriver.setCode(driverDTO.getCode());
		}
		return ResponseIO.success(vehicleDriver);
	}

	@RequestMapping(value = "/attendant/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<VehicleAttendantIO> updateAttendant(@PathVariable("authtoken") String authtoken, @RequestBody VehicleAttendantIO attendantIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		VehicleAttendantIO vehicleAttendant = new VehicleAttendantIO();
		BusVehicleAttendantDTO vehicleAttendantDTO = new BusVehicleAttendantDTO();

		vehicleAttendantDTO.setCode(attendantIO.getCode());
		vehicleAttendantDTO.setName(attendantIO.getName());
		vehicleAttendantDTO.setAge(attendantIO.getAge());
		vehicleAttendantDTO.setMobile(attendantIO.getMobile());
		vehicleAttendantDTO.setAlternateMobile(attendantIO.getAlternateMobile());
		vehicleAttendantDTO.setJoiningDate(attendantIO.getJoiningDate());
		vehicleAttendantDTO.setAddress(attendantIO.getAddress());
		vehicleAttendantDTO.setRemarks(attendantIO.getRemarks());
		vehicleAttendantDTO.setCategory(attendantIO.getCategory() != null ? AttendantCategoryEM.getCategoryEM(attendantIO.getCategory().getCode()) : AttendantCategoryEM.ATTENDANT);
		vehicleAttendantDTO.setActiveFlag(attendantIO.getActiveFlag());

		driverService.updateVehicleAttendant(authDTO, vehicleAttendantDTO);
		vehicleAttendant.setCode(vehicleAttendantDTO.getCode());
		vehicleAttendant.setActiveFlag(vehicleAttendantDTO.getActiveFlag());

		return ResponseIO.success(vehicleAttendant);
	}

	@RequestMapping(value = "/attendant", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleAttendantIO>> getAllAttendant(@PathVariable("authtoken") String authtoken, @RequestParam(value = "categoryCode", required = false, defaultValue = "ATDT") String categoryCode) throws Exception {
		List<VehicleAttendantIO> vehicleAttendantList = new ArrayList<VehicleAttendantIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		AttendantCategoryEM categoryEM = StringUtil.isNotNull(categoryCode) ? AttendantCategoryEM.getCategoryEM(categoryCode) : null;
		List<BusVehicleAttendantDTO> attendantDTOList = driverService.getAllAttendant(authDTO, categoryEM);

		for (BusVehicleAttendantDTO vehicleAttendantDTO : attendantDTOList) {
			VehicleAttendantIO vehicleAttendant = new VehicleAttendantIO();
			vehicleAttendant.setCode(vehicleAttendantDTO.getCode());
			vehicleAttendant.setName(vehicleAttendantDTO.getName());
			vehicleAttendant.setAge(vehicleAttendantDTO.getAge());
			vehicleAttendant.setMobile(vehicleAttendantDTO.getMobile());
			vehicleAttendant.setAlternateMobile(vehicleAttendantDTO.getAlternateMobile());
			vehicleAttendant.setJoiningDate(vehicleAttendantDTO.getJoiningDate());
			vehicleAttendant.setAddress(vehicleAttendantDTO.getAddress());
			vehicleAttendant.setRemarks(vehicleAttendantDTO.getRemarks());
			vehicleAttendant.setActiveFlag(vehicleAttendantDTO.getActiveFlag());
			
			BaseIO categoryIO = new BaseIO();
			if (vehicleAttendantDTO.getCategory() != null) {
				categoryIO.setCode(vehicleAttendantDTO.getCategory().getCode());
				categoryIO.setName(vehicleAttendantDTO.getCategory().getName());
			}
			vehicleAttendant.setCategory(categoryIO);
			vehicleAttendantList.add(vehicleAttendant);
		}
		return ResponseIO.success(vehicleAttendantList);
	}

	@RequestMapping(value = "/driver/user/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> updateDriverUser(@PathVariable("authtoken") String authtoken, @RequestBody UserIO user) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserIO userIO = new UserIO();
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(user.getCode());
			userDTO.setUsername(user.getUsername());
			userDTO.setEmail(user.getEmail());
			userDTO.setActiveFlag(user.getActiveFlag());
			if (user.getOrganization() != null && StringUtil.isNotNull(user.getOrganization().getCode())) {
				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setCode(user.getOrganization().getCode());
				userDTO.setOrganization(organizationDTO);
			}
			if (StringUtil.isNull(userDTO.getUsername())) {
				throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
			}
			else if (!StringUtil.isValidUsername(userDTO.getUsername())) {
				throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
			}

			userDTO = driverService.updateVehicleDriverUser(authDTO, userDTO);
			userIO.setCode(userDTO.getCode());
		}
		return ResponseIO.success(userIO);
	}

}
