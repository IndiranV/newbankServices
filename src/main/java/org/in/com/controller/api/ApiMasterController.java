package org.in.com.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.controller.api.io.BaseIO;
import org.in.com.controller.api.io.BusIO;
import org.in.com.controller.api.io.BusVehicleIO;
import org.in.com.controller.api.io.GroupIO;
import org.in.com.controller.api.io.NamespaceTaxIO;
import org.in.com.controller.api.io.OrganizationIO;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.api.io.StationIO;
import org.in.com.controller.api.io.StationPointIO;
import org.in.com.controller.api.io.UserIO;
import org.in.com.controller.api.io.VehicleAttendantIO;
import org.in.com.controller.api.io.VehicleDriverIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AttendantCategoryEM;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.OrganizationService;
import org.in.com.service.StationService;
import org.in.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/json/{operatorCode}/{username}/{apiToken}/master")
public class ApiMasterController extends BaseController {

	@Autowired
	AuthService authService;
	@Autowired
	BusVehicleService busVehicleService;
	@Autowired
	BusVehicleDriverService driverService;
	@Autowired
	UserService userService;
	@Autowired
	StationService stationService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	BusService busService;

	@RequestMapping(value = "/bus/vehicle", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleIO>> getAllBusVehicle(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<BusVehicleIO> vehicleList = new ArrayList<BusVehicleIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<BusVehicleDTO> list = busVehicleService.getAll(authDTO);
		for (BusVehicleDTO bvDTO : list) {
			BusVehicleIO vehicleIO = new BusVehicleIO();
			vehicleIO.setCode(bvDTO.getCode());
			vehicleIO.setName(bvDTO.getName());

			BusIO busIO = new BusIO();
			busIO.setCode(bvDTO.getBus().getCode());
			busIO.setName(bvDTO.getBus().getName());
			busIO.setTotalSeatCount(bvDTO.getBus().getSeatLayoutCount());
			busIO.setBusType(busService.getBusCategoryByCode(bvDTO.getBus().getCategoryCode()));
			vehicleIO.setBus(busIO);

			BaseIO vehicleType = new BaseIO();
			vehicleType.setCode(bvDTO.getVehicleType().getCode());
			vehicleType.setName(bvDTO.getVehicleType().getName());
			vehicleIO.setVehicleType(vehicleType);

			vehicleIO.setRegistrationDate(bvDTO.getRegistrationDate());
			vehicleIO.setRegistationNumber(bvDTO.getRegistationNumber());
			vehicleIO.setLicNumber(bvDTO.getLicNumber());
			vehicleIO.setGpsDeviceCode(bvDTO.getGpsDeviceCode());
			vehicleIO.setMobileNumber(bvDTO.getMobileNumber());
			vehicleIO.setActiveFlag(bvDTO.getActiveFlag());
			vehicleList.add(vehicleIO);
		}
		return ResponseIO.success(vehicleList);
	}

	@RequestMapping(value = "/bus/vehicle/driver", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleDriverIO>> getDrivers(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<VehicleDriverIO> vehicleDrivers = new ArrayList<VehicleDriverIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<BusVehicleDriverDTO> list = driverService.getAll(authDTO);
		for (BusVehicleDriverDTO driverDTO : list) {
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
			vehicleDriver.setRemarks(driverDTO.getRemarks());
			vehicleDriver.setActiveFlag(driverDTO.getActiveFlag());
			vehicleDrivers.add(vehicleDriver);
		}
		return ResponseIO.success(vehicleDrivers);
	}

	@RequestMapping(value = "/bus/vehicle/attendant", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleAttendantIO>> getAttendants(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<VehicleAttendantIO> vehicleAttendantList = new ArrayList<VehicleAttendantIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<BusVehicleAttendantDTO> attendantDTOList = driverService.getAllAttendant(authDTO, AttendantCategoryEM.ATTENDANT);
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
			vehicleAttendantList.add(vehicleAttendant);
		}
		return ResponseIO.success(vehicleAttendantList);
	}

	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getUsers(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<UserIO> users = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<UserDTO> list = userService.getAll(authDTO);
		for (UserDTO userDTO : list) {
			UserIO userio = new UserIO();
			userio.setUsername(userDTO.getUsername());
			userio.setEmail(userDTO.getEmail());
			userio.setCode(userDTO.getCode());
			userio.setName(userDTO.getName());
			userio.setLastname(userDTO.getLastname());
			userio.setMobile(userDTO.getMobile());
			userio.setActiveFlag(userDTO.getActiveFlag());

			BaseIO paymentType = new BaseIO();
			paymentType.setCode(userDTO.getPaymentType().getCode());
			paymentType.setName(userDTO.getPaymentType().getName());
			userio.setPaymentType(paymentType);

			GroupIO groupIO = new GroupIO();
			groupIO.setCode(userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
			groupIO.setName(userDTO.getGroup() != null ? userDTO.getGroup().getName() : null);
			groupIO.setDecription(userDTO.getGroup() != null ? userDTO.getGroup().getDecription() : null);
			userio.setGroup(groupIO);

			OrganizationIO organizationIO = new OrganizationIO();
			organizationIO.setCode(userDTO.getOrganization() != null ? userDTO.getOrganization().getCode() : null);
			organizationIO.setName(userDTO.getOrganization() != null ? userDTO.getOrganization().getName() : null);
			organizationIO.setActiveFlag(Numeric.ONE_INT);
			userio.setOrganization(organizationIO);

			users.add(userio);
		}
		return ResponseIO.success(users);
	}

	@RequestMapping(value = "/station", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<StationIO>> getStations(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<StationDTO> list = stationService.getStationAndStationPoints(authDTO);
		for (StationDTO stationDTO : list) {
			StationIO stationIO = new StationIO();
			stationIO.setCode(stationDTO.getCode());
			stationIO.setName(stationDTO.getName());
			stationIO.setActiveFlag(stationDTO.getActiveFlag());

			BaseIO state = new BaseIO();
			state.setCode(stationDTO.getState().getCode());
			state.setName(stationDTO.getState().getName());
			stationIO.setState(state);

			List<StationPointIO> stationPointList = new ArrayList<StationPointIO>();
			for (StationPointDTO stationPointDTO : stationDTO.getStationPoints()) {
				StationPointIO stationPointIO = new StationPointIO();
				stationPointIO.setCode(stationPointDTO.getCode());
				stationPointIO.setName(stationPointDTO.getName());
				stationPointIO.setAddress(stationPointDTO.getAddress());
				stationPointIO.setLandmark(stationPointDTO.getLandmark());
				stationPointIO.setLatitude(stationPointDTO.getLatitude() == null ? "" : stationPointDTO.getLatitude());
				stationPointIO.setLongitude(stationPointDTO.getLongitude() == null ? "" : stationPointDTO.getLongitude());
				stationPointIO.setNumber(stationPointDTO.getNumber());
				stationPointIO.setActiveFlag(stationPointDTO.getActiveFlag());
				stationPointList.add(stationPointIO);
			}
			stationIO.setStationPoint(stationPointList);
			stations.add(stationIO);
		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<OrganizationIO>> getOrganizations(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<OrganizationIO> organizations = new ArrayList<OrganizationIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<OrganizationDTO> list = organizationService.getAll(authDTO);
		for (OrganizationDTO organizationDTO : list) {
			OrganizationIO organizaionio = new OrganizationIO();
			organizaionio.setCode(organizationDTO.getCode());
			organizaionio.setName(organizationDTO.getName());
			organizaionio.setAddress1(organizationDTO.getAddress1());
			organizaionio.setAddress2(organizationDTO.getAddress2());
			organizaionio.setContact(organizationDTO.getContact());
			organizaionio.setPincode(organizationDTO.getPincode());
			organizaionio.setLatitude(organizationDTO.getLatitude());
			organizaionio.setLongitude(organizationDTO.getLongitude());

			StationIO stationIO = new StationIO();
			stationIO.setName(organizationDTO.getStation().getName());
			stationIO.setCode(organizationDTO.getStation().getCode());
			organizaionio.setStation(stationIO);

			organizaionio.setActiveFlag(organizationDTO.getActiveFlag());
			organizations.add(organizaionio);
		}
		return ResponseIO.success(organizations);
	}

	@RequestMapping(value = "/tax", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceTaxIO>> getAllTax(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<NamespaceTaxIO> list = new ArrayList<NamespaceTaxIO>();
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<NamespaceTaxDTO> taxList = taxService.getAll(authDTO);
		for (NamespaceTaxDTO taxDTO : taxList) {
			if (ProductTypeEM.BITS.getId() != taxDTO.getProductType().getId()) {
				continue;
			}

			NamespaceTaxIO tax = new NamespaceTaxIO();
			tax.setCode(taxDTO.getCode());
			tax.setName(taxDTO.getName());
			tax.setTradeName(taxDTO.getTradeName());
			tax.setGstin(taxDTO.getGstin());
			tax.setCgstValue(taxDTO.getCgstValue());
			tax.setSgstValue(taxDTO.getSgstValue());
			tax.setUgstValue(taxDTO.getUgstValue());
			tax.setIgstValue(taxDTO.getIgstValue());
			tax.setSacNumber(taxDTO.getSacNumber());

			BaseIO state = new BaseIO();
			state.setCode(taxDTO.getState().getCode());
			state.setName(taxDTO.getState().getName());
			tax.setState(state);

			tax.setActiveFlag(taxDTO.getActiveFlag());
			list.add(tax);
		}
		return ResponseIO.success(list);
	}
}
