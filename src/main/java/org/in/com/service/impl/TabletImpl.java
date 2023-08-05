package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.dao.TabletDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.GroupService;
import org.in.com.service.TabletService;
import org.in.com.service.TransactionOTPService;
import org.in.com.service.UserService;
import org.in.com.utils.NumericGenerator;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

@Service
public class TabletImpl extends CacheCentral implements TabletService {
	@Autowired
	BusVehicleService busVehicleService;
	@Autowired
	GroupService groupService;
	public static Map<String, TabletDTO> expireMap = new HashMap<String, TabletDTO>();
	@Autowired
	TransactionOTPService transactionOTPService;
	@Autowired
	UserService userService;
	@Autowired
	SMSService smsService;
	@Autowired
	BusVehicleDriverService driverService;

	@Override
	public TabletDTO Update(AuthDTO authDTO, TabletDTO tabletDTO) {
		TabletDAO tabletDAO = new TabletDAO();
		if (expireMap.get(authDTO.getNamespaceCode() + "_" + tabletDTO.getCode()) == null) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		TabletDTO mapDTO = expireMap.get(authDTO.getNamespaceCode() + "_" + tabletDTO.getCode());
		if (mapDTO == null || !tabletDTO.getUser().getToken().equals(mapDTO.getUser().getToken()) || tabletDTO.getActiveFlag() != 1) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}
		UserDTO userDTO = mapDTO.getUser();
		userDTO.setAppStoreDetails(tabletDTO.getUser().getAppStoreDetails());
		userDTO.getAppStoreDetails().setDeviceMedium(DeviceMediumEM.APP_TABLET_POB);

		userDTO.setUsername(tabletDTO.getUser().getAppStoreDetails().getUdid());
		UserDTO user = userService.getUserDTO(authDTO, userDTO);

		if (user.getId() != 0) {
			List<GroupDTO> groupList = groupService.getUserRoleGroup(authDTO, UserRoleEM.TABLET_POB_ROLE);
			if (groupList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_GROUP);
			}
			userDTO.setToken(TokenGenerator.generateToken(20));
			userDTO.setActiveFlag(1);
			userDTO.setUserRole(UserRoleEM.TABLET_POB_ROLE);
			tabletDTO.setName(mapDTO.getName());
			userDTO.setEmail(mapDTO.getCode() + "@" + authDTO.getNamespaceCode() + ".com");
			userDTO.setGroup(Iterables.getFirst(groupList, null));
			userService.updateUserDetails(authDTO, userDTO);

			tabletDAO.releaseUserFromTablet(authDTO, userDTO);
			tabletDTO.setUser(userDTO);
		}
		else {
			userDTO.setToken(TokenGenerator.generateToken(20));
			userDTO.setUserRole(UserRoleEM.TABLET_POB_ROLE);
			userDTO.setEmail(mapDTO.getCode() + "@" + authDTO.getNamespaceCode() + ".com");
			userDTO.setPaymentType(PaymentTypeEM.PAYMENT_GATEWAY_PAID);
			List<GroupDTO> groupList = groupService.getUserRoleGroup(authDTO, UserRoleEM.TABLET_POB_ROLE);
			if (groupList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_GROUP);
			}
			userDTO.setGroup(Iterables.getFirst(groupList, null));
			userDTO.setMobile(mapDTO.getMobileNumber());
			userService.Update(authDTO, userDTO);
			userService.resetProfilePassword(authDTO, userDTO);

			userService.getUserDTO(authDTO, userDTO);

			userDTO.setAppStoreDetails(tabletDTO.getUser().getAppStoreDetails());
			tabletDTO.setUser(userDTO);
			tabletDTO.setName(mapDTO.getName());
		}
		tabletDTO.setMobileNumber(StringUtil.isNull(tabletDTO.getMobileNumber(), mapDTO.getMobileNumber()));
		tabletDTO.setModel(StringUtil.isNull(tabletDTO.getModel(), mapDTO.getModel()));
		tabletDTO.setVersion(StringUtil.isNull(tabletDTO.getModel(), mapDTO.getVersion()));
		tabletDAO.saveTablet(authDTO, tabletDTO);
		expireMap.remove(authDTO.getNamespaceCode() + "_" + tabletDTO.getCode());
		return tabletDTO;
	}

	@Override
	public String generateAuthorizePIN(AuthDTO authDTO, String tabletCode) {
		if (expireMap.get(authDTO.getNamespaceCode() + "_" + tabletCode) == null) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		int newPIN = NumericGenerator.randInt();
		TabletDTO tablet = expireMap.get(authDTO.getNamespaceCode() + "_" + tabletCode);
		tablet.getUser().setToken(String.valueOf(newPIN));
		expireMap.put(authDTO.getNamespaceCode() + "_" + tabletCode, tablet);
		return String.valueOf(newPIN);
	}

	public String generateDriverAuthorizePIN(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver) {
		busVehicleDriver = driverService.getBusVehicleDriver(authDTO, busVehicleDriver);
		if (busVehicleDriver == null || busVehicleDriver.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}

		int newPIN = NumericGenerator.randInt();
		TabletDTO tabletDTO = new TabletDTO();
		tabletDTO.setCode(busVehicleDriver.getCode());
		tabletDTO.setName("DRIVER_APP");

		UserDTO userDTO = new UserDTO();
		userDTO.setActiveFlag(1);
		userDTO.setToken(String.valueOf(newPIN));
		tabletDTO.setUser(userDTO);

		expireMap.put(authDTO.getNamespaceCode() + "_" + newPIN, tabletDTO);
		return String.valueOf(newPIN);
	}

	public TabletDTO saveDriverApp(AuthDTO authDTO, TabletDTO tabletDTO) {
		TabletDTO mapDTO = expireMap.get(authDTO.getNamespaceCode() + "_" + tabletDTO.getUser().getToken());
		if (mapDTO == null) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}

		BusVehicleDriverDTO busVehicleDriver = new BusVehicleDriverDTO();
		busVehicleDriver.setCode(mapDTO.getCode());

		busVehicleDriver = driverService.getBusVehicleDriver(authDTO, busVehicleDriver);
		if (busVehicleDriver == null || busVehicleDriver.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}

		UserDTO userDTO = mapDTO.getUser();
		tabletDTO.setCode(busVehicleDriver.getCode());
		tabletDTO.setName(busVehicleDriver.getName());
		userDTO.setAppStoreDetails(tabletDTO.getUser().getAppStoreDetails());
		userDTO.getAppStoreDetails().setDeviceMedium(DeviceMediumEM.APP_TABLET_POB);

		userDTO.setUsername(tabletDTO.getUser().getAppStoreDetails().getUdid());
		UserDTO user = userService.getUserDTO(authDTO, userDTO);

		if (user.getId() != 0) {
			List<GroupDTO> groupList = groupService.getUserRoleGroup(authDTO, UserRoleEM.DRIVER);
			if (groupList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_GROUP);
			}
			userDTO.setToken(TokenGenerator.generateToken(20));
			userDTO.setActiveFlag(1);
			userDTO.setUserRole(UserRoleEM.DRIVER);
			userDTO.setEmail(mapDTO.getCode() + "@" + authDTO.getNamespaceCode() + ".com");
			userDTO.setGroup(Iterables.getFirst(groupList, null));
			userService.updateUserDetails(authDTO, userDTO);

			driverService.updateUser(authDTO, new UserDTO(), busVehicleDriver);
			tabletDTO.setUser(userDTO);
		}
		else {
			userDTO.setToken(TokenGenerator.generateToken(20));
			userDTO.setUserRole(UserRoleEM.DRIVER);
			userDTO.setEmail(mapDTO.getCode() + "@" + authDTO.getNamespaceCode() + ".com");
			userDTO.setPaymentType(PaymentTypeEM.PAYMENT_GATEWAY_PAID);
			List<GroupDTO> groupList = groupService.getUserRoleGroup(authDTO, UserRoleEM.DRIVER);
			if (groupList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_GROUP);
			}
			userDTO.setGroup(Iterables.getFirst(groupList, null));
			userDTO.setMobile(mapDTO.getMobileNumber());
			userDTO.setName(busVehicleDriver.getName());
			userService.Update(authDTO, userDTO);
			userService.resetProfilePassword(authDTO, userDTO);

			userService.getUserDTO(authDTO, userDTO);

			userDTO.setAppStoreDetails(tabletDTO.getUser().getAppStoreDetails());
			tabletDTO.setUser(userDTO);
		}

		driverService.updateUser(authDTO, userDTO, busVehicleDriver);
		expireMap.remove(authDTO.getNamespaceCode() + "_" + tabletDTO.getUser().getToken());
		return tabletDTO;
	}

	@Override
	public Collection<TabletDTO> getRegisterPending(AuthDTO authDTO) {
		List<TabletDTO> list = new ArrayList<TabletDTO>();
		for (String tabletCode : expireMap.keySet()) {
			if (tabletCode.contains(authDTO.getNamespaceCode())) {
				TabletDTO tabletDTO = expireMap.get(tabletCode);
				if (StringUtil.isNull(tabletDTO.getName()) || !"DRIVER_APP".equals(tabletDTO.getName())) {
					list.add(tabletDTO);
				}
			}
		}
		return list;
	}

	@Override
	public List<TabletDTO> get(AuthDTO authDTO, TabletDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TabletDTO> getAll(AuthDTO authDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TabletDTO registerNewTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		tabletDTO.setCode(TokenGenerator.generateCode("TAB"));
		expireMap.put(authDTO.getNamespaceCode() + "_" + tabletDTO.getCode(), tabletDTO);
		return tabletDTO;
	}

	@Override
	public Collection<TabletDTO> getAllTablets(AuthDTO authDTO) {
		TabletDAO tabletDAO = new TabletDAO();
		Collection<TabletDTO> list = tabletDAO.getAll(authDTO);
		for (TabletDTO tabletDTO : list) {
			if (tabletDTO.getBusVehicle() != null && tabletDTO.getBusVehicle().getId() > 0) {
				BusVehicleDTO vehicleDTO = busVehicleService.getBusVehicles(authDTO, tabletDTO.getBusVehicle());
				tabletDTO.setBusVehicle(vehicleDTO);
			}
		}
		return list;
	}

	@Override
	public void tabletVehicleMapping(AuthDTO authDTO, TabletDTO tabletDTO) {
		if (tabletDTO.getBusVehicle() != null && StringUtil.isNotNull(tabletDTO.getBusVehicle().getCode())) {
			BusVehicleDTO vehicleDTO = busVehicleService.getBusVehicles(authDTO, tabletDTO.getBusVehicle());
			tabletDTO.setBusVehicle(vehicleDTO);
		}
		TabletDAO tabletDAO = new TabletDAO();
		tabletDAO.tabletVehicleMapping(authDTO, tabletDTO);

		TabletDTO tablet = getTablet(authDTO, tabletDTO.getCode());
		UserDTO userDTO = tablet.getUser();
		userDTO.setName(tablet.getBusVehicle().getRegistationNumber());
		userService.Update(authDTO, userDTO);
	}

	@Override
	public void deleteTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		TabletDAO tabletDAO = new TabletDAO();
		tabletDAO.deleteTablet(authDTO, tabletDTO);
	}

	@Override
	public void updateTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		TabletDAO tabletDAO = new TabletDAO();
		tabletDAO.updateTablet(authDTO, tabletDTO);
	}

	@Override
	public TabletDTO getTablet(AuthDTO authDTO, String tabletCode) {
		TabletDAO tabletDAO = new TabletDAO();
		TabletDTO tabletDTO = tabletDAO.getTablet(authDTO, tabletCode);
		if (tabletDTO == null || tabletDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Tablet not found");
		}
		if (tabletDTO.getBusVehicle() != null && tabletDTO.getBusVehicle().getId() != 0) {
			BusVehicleDTO vehicleDTO = busVehicleService.getBusVehicles(authDTO, tabletDTO.getBusVehicle());
			vehicleDTO.setBus(getBusDTObyId(authDTO, vehicleDTO.getBus()));
			tabletDTO.setBusVehicle(vehicleDTO);
		}
		if (tabletDTO.getUser().getId() != 0) {
			tabletDTO.setUser(getUserDTOById(authDTO, tabletDTO.getUser()));
		}
		return tabletDTO;
	}

	@Override
	public TabletDTO deRegisterTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		TabletDAO tabletDAO = new TabletDAO();
		tabletDTO = tabletDAO.getTablet(authDTO, tabletDTO.getCode());

		UserDTO userDTO = new UserDTO();
		userDTO.setId(Numeric.ZERO_INT);
		userDTO.setName(tabletDTO.getName());
		userDTO.setActiveFlag(Numeric.ONE_INT);
		tabletDTO.setUser(userDTO);

		expireMap.put(authDTO.getNamespaceCode() + "_" + tabletDTO.getCode(), tabletDTO);
		return tabletDTO;
	}

	@Override
	public void generateOTP(AuthDTO authDTO, TabletDTO tabletDTO) {
		TabletDAO tabletDAO = new TabletDAO();
		tabletDTO = tabletDAO.getTablet(authDTO, tabletDTO.getCode());

		int newOTP = transactionOTPService.generateOTP(authDTO, tabletDTO.getCode(), tabletDTO.getMobileNumber(), false);

		Map<String, String> dataModel = new HashMap<>();
		dataModel.put("otp", String.valueOf(newOTP));
		dataModel.put("tabletCode", tabletDTO.getCode());

		smsService.sendSMS(authDTO, tabletDTO.getMobileNumber(), dataModel, NotificationTypeEM.TABLET_OTP);
	}

	@Override
	public void verifyDeviceMobile(AuthDTO authDTO, TabletDTO tabletDTO, int otp) {
		TabletDAO tabletDAO = new TabletDAO();
		tabletDTO = tabletDAO.getTablet(authDTO, tabletDTO.getCode());
		boolean isValid = transactionOTPService.validateOTP(authDTO, tabletDTO.getCode(), tabletDTO.getMobileNumber(), otp);
		if (!isValid) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}

		tabletDAO.verifyMobile(authDTO, tabletDTO);
	}

	@Override
	public List<TabletDTO> getTablet(AuthDTO authDTO, BusVehicleDTO busVehicle) {
		TabletDAO tabletDAO = new TabletDAO();
		List<TabletDTO> tabletList = tabletDAO.getTablet(authDTO, busVehicle);
		for (TabletDTO tabletDTO : tabletList) {
			if (tabletDTO.getUser().getId() != 0) {
				tabletDTO.setUser(userService.getUser(authDTO, tabletDTO.getUser()));
			}
		}
		return tabletList;
	}
}
