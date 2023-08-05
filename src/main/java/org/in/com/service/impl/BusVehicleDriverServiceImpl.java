package org.in.com.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.dao.BusVehicleDriverDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AttendantCategoryEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.GroupService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class BusVehicleDriverServiceImpl extends CacheCentral implements BusVehicleDriverService {

	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;
	@Lazy
	@Autowired
	TripService tripService;

	public List<BusVehicleDriverDTO> getAll(AuthDTO authDTO, BusVehicleDriverDTO driverDTO) {
		return null;
	}

	public BusVehicleDriverDTO getBusVehicleDriver(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver) {
		BusVehicleDriverDTO driver = null;
		if (busVehicleDriver.getId() != 0) {
			driver = getVehicleDriverDTOById(authDTO, busVehicleDriver);
		}
		else if (StringUtil.isNotNull(busVehicleDriver.getCode())) {
			driver = getVehicleDriverDTO(authDTO, busVehicleDriver);

		}
		return driver;
	}

	public BusVehicleDriverDTO Update(AuthDTO authDTO, BusVehicleDriverDTO driverDTO) {
		BusVehicleDriverDAO dao = new BusVehicleDriverDAO();
		dao.updateDriver(authDTO, driverDTO);
		return driverDTO;

	}

	public List<BusVehicleDriverDTO> get(AuthDTO authDTO, BusVehicleDriverDTO dto) {
		BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
		driverDAO.getBusVehicleDriver(authDTO, dto);
		if (dto.getUser() != null && dto.getUser().getId() != 0) {
			dto.setUser(userService.getUser(authDTO, dto.getUser()));
		}
		return null;
	}

	public List<BusVehicleDriverDTO> getAll(AuthDTO authDTO) {
		BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
		List<BusVehicleDriverDTO> list = driverDAO.getAllDriver(authDTO);
		for (BusVehicleDriverDTO driver : list) {
			if (driver.getUser().getId() == 0) {
				continue;
			}
			driver.setUser(userService.getUser(authDTO, driver.getUser()));
		}
		return list;
	}

	public void updateVehicleAttendant(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO) {
		BusVehicleDriverDAO attendantDAO = new BusVehicleDriverDAO();
		attendantDAO.updateAttendant(authDTO, attendantDTO);
	}

	public List<BusVehicleAttendantDTO> getAllAttendant(AuthDTO authDTO, AttendantCategoryEM category) {
		BusVehicleDriverDAO attendantDAO = new BusVehicleDriverDAO();
		return attendantDAO.getAllAttendant(authDTO, category);
	}

	public BusVehicleAttendantDTO getAttendant(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO) {
		BusVehicleDriverDAO attendantDAO = new BusVehicleDriverDAO();
		return attendantDAO.getAttendant(authDTO, attendantDTO);
	}

	@Override
	public UserDTO updateVehicleDriverUser(AuthDTO authDTO, UserDTO userDTO) {
		BusVehicleDriverDTO busVehicleDriver = new BusVehicleDriverDTO();
		busVehicleDriver.setCode(userDTO.getCode());
		busVehicleDriver = getBusVehicleDriver(authDTO, busVehicleDriver);
		if (busVehicleDriver == null || busVehicleDriver.getId() == 0) {
			throw new ServiceException(ErrorCode.DRIVER_NOT_FOUND);
		}
		userDTO.setName(StringUtil.substring(busVehicleDriver.getName(), 45));
		userDTO.setLastname(StringUtil.substring(busVehicleDriver.getLastName(), 45));
		userDTO.setMobile(busVehicleDriver.getMobileNumber());
		userDTO.setUserRole(UserRoleEM.DRIVER);
		GroupDTO groupDTO = groupService.checkAndUpdateGroup(authDTO);
		userDTO.setGroup(groupDTO);

		if (userDTO.getOrganization() == null) {
			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(getOrganizationDTObyId(authDTO, authDTO.getUser().getOrganization()).getCode());
			userDTO.setOrganization(organizationDTO);
		}
		userDTO.setPaymentType(PaymentTypeEM.PAYMENT_PRE_PAID);
		userService.saveVehicleDriverUser(authDTO, userDTO);
		updateDriverUserMap(authDTO, busVehicleDriver, userDTO);
		return userDTO;
	}

	@Override
	public void updateDriverUserMap(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver, UserDTO userDTO) {
		BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
		driverDAO.updateDriverUserMap(authDTO, busVehicleDriver, userDTO);
	}

	@Override
	public void updateUser(AuthDTO authDTO, UserDTO userDTO, BusVehicleDriverDTO driverDTO) {
		BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
		driverDAO.updateUser(authDTO, userDTO, driverDTO);
	}

	@Override
	public void updateLastAssignedDate(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver) {
		BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
		driverDAO.updateLastAssignedDate(authDTO, busVehicleDriver);
	}

	@Override
	public void updateAssignedTripsCount(AuthDTO authDTO, int days) {
		DateTime startDate = DateUtil.minusDaysToDate(DateUtil.NOW(), days);
		List<DateTime> tripDates = DateUtil.getDateListV3(startDate, DateUtil.NOW(), "1111111");
		Map<String, Integer> driverAssignedTripsCountMap = new HashMap<>();
		for (DateTime tripDate : tripDates) {
			List<TripDTO> trips = tripService.getTripInfoByTripDate(authDTO, tripDate);
			for (TripDTO tripDTO : trips) {
				/** Primary Driver */
				if (StringUtil.isNotNull(tripDTO.getTripInfo().getPrimaryDriver().getCode()) && driverAssignedTripsCountMap.get(tripDTO.getTripInfo().getPrimaryDriver().getCode()) == null) {
					driverAssignedTripsCountMap.put(tripDTO.getTripInfo().getPrimaryDriver().getCode(), 1);
				}
				else if (StringUtil.isNotNull(tripDTO.getTripInfo().getPrimaryDriver().getCode()) && driverAssignedTripsCountMap.get(tripDTO.getTripInfo().getPrimaryDriver().getCode()) != null) {
					int count = driverAssignedTripsCountMap.get(tripDTO.getTripInfo().getPrimaryDriver().getCode());
					driverAssignedTripsCountMap.put(tripDTO.getTripInfo().getPrimaryDriver().getCode(), count + 1);
				}
				/** Secondary Driver */
				if (StringUtil.isNotNull(tripDTO.getTripInfo().getSecondaryDriver().getCode()) && driverAssignedTripsCountMap.get(tripDTO.getTripInfo().getSecondaryDriver().getCode()) == null) {
					driverAssignedTripsCountMap.put(tripDTO.getTripInfo().getSecondaryDriver().getCode(), 1);
				}
				else if (StringUtil.isNotNull(tripDTO.getTripInfo().getSecondaryDriver().getCode()) && driverAssignedTripsCountMap.get(tripDTO.getTripInfo().getSecondaryDriver().getCode()) != null) {
					int count = driverAssignedTripsCountMap.get(tripDTO.getTripInfo().getSecondaryDriver().getCode());
					driverAssignedTripsCountMap.put(tripDTO.getTripInfo().getSecondaryDriver().getCode(), count + 1);
				}
			}
		}

		BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
		List<BusVehicleDriverDTO> drivers = driverDAO.getAllDriver(authDTO);
		for (BusVehicleDriverDTO driver : drivers) {
			if (driver.getActiveFlag() != 1) {
				continue;
			}
			driver.setAssignedTripsCount(Numeric.ZERO_INT);
			if (driverAssignedTripsCountMap.get(driver.getCode()) != null) {
				driver.setAssignedTripsCount(driverAssignedTripsCountMap.get(driver.getCode()));
			}
		}

		/** Update Driver Assigned Trips Count */
		driverDAO.updateAssignedTripsCount(authDTO, drivers);
	}
}