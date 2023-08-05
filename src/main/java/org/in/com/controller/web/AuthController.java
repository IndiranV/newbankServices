package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.AuthIO;
import org.in.com.controller.web.io.AuthVechicleIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.CollaborationIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.MenuEventIO;
import org.in.com.controller.web.io.MenuIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.UserSessionAuditIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.UserCustomerAuthDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserSessionAuditDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.MenuService;
import org.in.com.service.NamespaceService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsEnDecrypt;
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
@RequestMapping(value = "/auth")
public class AuthController<T> extends BaseController {
	@Autowired
	MenuService menuService;
	@Autowired
	UserService userService;
	@Autowired
	NamespaceService namespaceService;

	@RequestMapping(value = "/getAuthToken", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> getAuthToken(String namespaceCode, String username, String password, String devicemedium, @RequestParam(value = "authenticationTypeCode", required = true, defaultValue = "BITSUP") String authenticationTypeCode, @RequestParam(value = "userFirstName", required = true, defaultValue = "NA") String userFirstName) throws Exception {
		AuthIO authIO = new AuthIO();
		if (StringUtil.isNotNull(namespaceCode) && StringUtil.isNotNull(username) && StringUtil.isNotNull(password) && ValidateDeviceMedium(devicemedium)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			AuthDTO authDTO = authService.CheckAuthendtication(namespaceDTO, AuthenticationTypeEM.getAuthenticationTypeEM(authenticationTypeCode), username, password, DeviceMediumEM.getDeviceMediumEM(devicemedium), userFirstName);
			if (authDTO != null) {
				RoleIO role = new RoleIO();
				role.setCode(authDTO.getUser().getUserRole().getCode());
				role.setName(authDTO.getUser().getUserRole().getName());
				authIO.setRole(role);
				authIO.setAuthToken(authDTO.getAuthToken());
			}
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/getGuestAuthToken", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> getGuestAuthToken(String namespaceCode, String devicemedium) throws Exception {
		AuthIO authIO = new AuthIO();
		String authToken = null;
		StringBuilder error = new StringBuilder();
		try {
			if (StringUtil.isNotNull(namespaceCode) && ValidateDeviceMedium(devicemedium)) {
				AuthDTO authDTO = null;
				String guestKey = StringUtil.removeSymbol(namespaceCode + devicemedium);
				Element guestElement = EhcacheManager.getGuestAuthTokenEhCache().get(guestKey);
				if (guestElement != null) {
					error.append("guestElement");
					authToken = (String) guestElement.getObjectValue();
					authDTO = authService.getAuthDetails(authToken, guestKey);
					error.append("guestElement != null(106)");
				}
				if (authDTO == null) {
					error.append(",authDTO == null - 1(109)");
					authDTO = authService.getGuestAuthendtication(namespaceCode, DeviceMediumEM.getDeviceMediumEM(devicemedium));
					error.append(",authDTO == null - 2(111)");
				}
				error.append(",authDTO != null (114)");
				RoleIO role = new RoleIO();
				role.setCode(UserRoleEM.CUST_ROLE.getCode());
				role.setName(UserRoleEM.CUST_ROLE.getName());
				authIO.setRole(role);
				authIO.setAuthToken(authDTO.getAuthToken());
			}
		}
		catch (ServiceException e) {
			System.out.println("GUEST01 " + e.getErrorCode().getCode() + namespaceCode + devicemedium + " - " + error);
			throw e;
		}
		catch (Exception e) {
			System.out.println("GUEST02 " + namespaceCode + devicemedium);
			e.printStackTrace();
			throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/token/authenticate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> getApiToken(String namespaceCode, String username, String apiToken) throws Exception {
		AuthIO authIO = new AuthIO();
		if (StringUtil.isNotNull(namespaceCode) && StringUtil.isNotNull(username) && StringUtil.isNotNull(apiToken)) {
			AuthDTO authDTO = authService.getApiAuthendtication(namespaceCode, username, apiToken, DeviceMediumEM.API_USER);
			if (authDTO != null) {
				RoleIO role = new RoleIO();
				role.setCode(authDTO.getUser().getUserRole().getCode());
				role.setName(authDTO.getUser().getUserRole().getName());
				authIO.setRole(role);
				authIO.setAuthToken(authDTO.getAuthToken());
			}
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/switch/{namespaceCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> switchNamespace(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null && StringUtil.isNotNull(namespaceCode)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(namespaceCode);
			authService.switchNamespace(authDTO, namespaceDTO);
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/switch/user/{usercode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> switchUser(@PathVariable("authtoken") String authtoken, @PathVariable("usercode") String usercode) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null && StringUtil.isNotNull(usercode)) {
			UserDTO user = new UserDTO();
			user.setCode(usercode);
			authService.switchUser(authDTO, user);
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/verify", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> verify(@PathVariable("authtoken") String authtoken) throws Exception {
		authService.getAuthDTO(authtoken);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{authtoken}/verify/password", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> verifyPassword(@PathVariable("authtoken") String authtoken, String password) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		authService.getVerifyPassword(authDTO, password);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{authtoken}/verify/profile", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> verifyToken(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO userDTO = getUserDTO(authDTO.getNamespaceCode(), authDTO.getUserCode());
		authIO.setUsername(userDTO.getUsername());
		authIO.setNamespaceCode(authDTO.getNamespaceCode());
		authIO.setDeviceMediumCode(authDTO.getDeviceMedium().getCode());
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/account/{accountCode}/integrationtype/{typeCode}/verify", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> verifyToken(@PathVariable("authtoken") String authtoken, @PathVariable("accountCode") String accountCode, @PathVariable("typeCode") String typeCode) throws Exception {
		AuthIO authIO = new AuthIO();

		IntegrationDTO integrationDTO = new IntegrationDTO();
		integrationDTO.setAccount(accountCode);
		integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(typeCode));

		AuthDTO authDTO = authService.verifyAuthToken(integrationDTO, authtoken);
		if (authDTO != null) {
			authIO.setAuthToken(authDTO.getAuthToken());
			authIO.setUsername(authDTO.getUser().getUsername());
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/privileges", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<MenuIO>> getPrivileges(@PathVariable("authtoken") String authtoken) throws Exception {
		List<MenuIO> menuList = new ArrayList<MenuIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<MenuDTO> list = authDTO.getPrivileges();
			// List<MenuDTO> list = menuService.getUserPrivileges(authDTO);
			if (list == null) {
				list = new ArrayList<MenuDTO>();
			}
			for (MenuDTO menuDTO : list) {
				MenuIO menuio = new MenuIO();
				menuio.setCode(menuDTO.getCode());
				menuio.setName(menuDTO.getName());
				menuio.setDisplayFlag(menuDTO.getDisplayFlag());
				menuio.setLink(menuDTO.getLink());
				BaseIO productIO = new BaseIO();
				productIO.setCode(menuDTO.getProductType().getCode());
				productIO.setName(menuDTO.getProductType().getName());
				productIO.setActiveFlag(1);
				menuio.setProductType(productIO);
				if (menuDTO.getLookup() != null) {
					MenuIO lookup = new MenuIO();
					lookup.setCode(menuDTO.getLookup().getCode());
					lookup.setName(menuDTO.getLookup().getName());
					lookup.setLink(menuDTO.getLookup().getLink());
					menuio.setLookup(lookup);
				}
				List<MenuEventIO> eventList = new ArrayList<MenuEventIO>();
				for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
					if (eventDTO != null) {
						MenuEventIO eventIO = new MenuEventIO();
						eventIO.setCode(eventDTO.getCode());
						eventIO.setActiveFlag(eventDTO.getActiveFlag());
						eventIO.setName(eventDTO.getName());
						eventIO.setOperationCode(eventDTO.getOperationCode() != null ? eventDTO.getOperationCode() : "NA");
						eventIO.setPermissionType(MenuEventDTO.getPermission(eventDTO.getPermissionFlag()));
						eventList.add(eventIO);
					}
				}
				menuio.setEventList(eventList);
				menuio.setActiveFlag(menuDTO.getActiveFlag());
				menuList.add(menuio);
			}

		}
		return ResponseIO.success(menuList);
	}

	@RequestMapping(value = "/{authtoken}/privileges/{productCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<MenuIO>> getPrivilegesV2(@PathVariable("authtoken") String authtoken, @PathVariable("productCode") String productCode) throws Exception {
		List<MenuIO> menuList = new ArrayList<MenuIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<MenuDTO> list = authDTO.getPrivileges();
			if (list == null) {
				list = new ArrayList<MenuDTO>();
			}
			ProductTypeEM productType = ProductTypeEM.getProductTypeEM(productCode);

			for (MenuDTO menuDTO : list) {
				MenuIO menuio = new MenuIO();
				menuio.setCode(menuDTO.getCode());
				menuio.setName(menuDTO.getName());
				menuio.setDisplayFlag(menuDTO.getDisplayFlag());
				menuio.setLink(menuDTO.getLink());

				BaseIO productIO = new BaseIO();
				productIO.setCode(menuDTO.getProductType().getCode());
				productIO.setName(menuDTO.getProductType().getName());
				productIO.setActiveFlag(1);
				menuio.setProductType(productIO);

				// Only product specific menus
				if (menuDTO.getProductType().getId() != ProductTypeEM.ACCOUNT.getId() && productType.getId() != menuDTO.getProductType().getId()) {
					continue;
				}
				if (menuDTO.getLookup() != null) {
					MenuIO lookup = new MenuIO();
					lookup.setCode(menuDTO.getLookup().getCode());
					lookup.setName(menuDTO.getLookup().getName());
					lookup.setLink(menuDTO.getLookup().getLink());
					menuio.setLookup(lookup);
				}
				List<MenuEventIO> eventList = new ArrayList<MenuEventIO>();
				for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
					if (eventDTO != null) {
						MenuEventIO eventIO = new MenuEventIO();
						eventIO.setCode(eventDTO.getCode());
						eventIO.setActiveFlag(eventDTO.getActiveFlag());
						eventIO.setName(eventDTO.getName());
						eventIO.setOperationCode(eventDTO.getOperationCode() != null ? eventDTO.getOperationCode() : "NA");
						eventIO.setPermissionType(MenuEventDTO.getPermission(eventDTO.getPermissionFlag()));
						eventList.add(eventIO);
					}
				}
				menuio.setEventList(eventList);
				menuio.setActiveFlag(menuDTO.getActiveFlag());
				menuList.add(menuio);
			}

		}
		return ResponseIO.success(menuList);
	}

	@RequestMapping(value = "/{authtoken}/collaboration", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CollaborationIO>> getCollaboration(@PathVariable("authtoken") String authtoken) throws Exception {
		List<CollaborationIO> List = new ArrayList<CollaborationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			Map<String, String> filer = new HashMap<String, String>();
			for (Object key : EhcacheManager.getAuthTokenEhCache().getKeys()) {
				Element element = EhcacheManager.getAuthTokenEhCache().get(key);
				if (element != null) {
					AuthDTO dto = (AuthDTO) element.getObjectValue();
					if (dto.getNamespace().getId() == authDTO.getNamespace().getId() && filer.get(dto.getUser().getCode()) == null) {
						CollaborationIO collaborationIO = new CollaborationIO();
						RoleIO role = new RoleIO();
						role.setCode(dto.getUser().getUserRole().getCode());
						role.setName(dto.getUser().getUserRole().getName());
						collaborationIO.setRole(role);

						GroupIO groupIO = new GroupIO();
						groupIO.setName(dto.getGroup().getName());
						groupIO.setCode(dto.getGroup().getCode());
						collaborationIO.setGroup(groupIO);

						UserIO userIO = new UserIO();
						userIO.setName(dto.getUser().getName());
						userIO.setCode(dto.getUser().getCode());
						collaborationIO.setUser(userIO);

						collaborationIO.setAuthToken(key.toString());
						filer.put(dto.getUser().getCode(), "1");
						List.add(collaborationIO);
					}
				}
			}
		}
		return ResponseIO.success(List);
	}

	@RequestMapping(value = "/{authtoken}/forgotPassword", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> forgotPassword(@PathVariable("authtoken") String authtoken, String username) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserIO userIO = new UserIO();
		if (authDTO != null && StringUtil.isNotNull(username)) {
			UserDTO userDTO = authService.forgetPassword(authDTO, username);
			userIO.setCode(userDTO.getCode());
			userIO.setName(userDTO.getName());
			userIO.setEmail(userDTO.getEmail());
		}
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> resetPassword(String namespaceCode, String username, String forgetToken, String newPassword) throws Exception {
		UserIO userIO = new UserIO();
		if (StringUtil.isNotNull(namespaceCode) && StringUtil.isNotNull(username) && StringUtil.isNotNull(forgetToken) && StringUtil.isNotNull(newPassword)) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO = getNamespaceDTO(namespaceCode);
			AuthDTO authDTO = new AuthDTO();
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			UserDTO userDTO = new UserDTO();
			userDTO.setUsername(username);
			userDTO = userService.getUserDTO(authDTO, userDTO);
			if (userDTO.getId() != 0 && userDTO.getForgetToken() != null) {
				if (userDTO.getForgetToken().equals(forgetToken)) {
					userDTO.setNewPassword(newPassword);
					userService.resetProfilePassword(authDTO, userDTO);
				}
				else {
					throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
				}
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_RESET_PASSWORD);
			}
			userIO.setCode(userDTO.getCode());
			userIO.setName(userDTO.getName());
			userIO.setEmail(userDTO.getEmail());
		}
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/device/getAuthToken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AuthIO> getDeviceOAuthToken(@RequestBody AuthIO auth) throws Exception {
		AuthIO authIO = new AuthIO();
		if (StringUtil.isNotNull(auth.getNamespaceCode()) && StringUtil.isNotNull(auth.getUsername()) && StringUtil.isNotNull(auth.getPassword()) && StringUtil.isNotNull(auth.getDeviceCode()) && StringUtil.isNotNull(auth.getDeviceToken())) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(auth.getNamespaceCode());
			NamespaceDeviceDTO deviceDTO = new NamespaceDeviceDTO();
			deviceDTO.setCode(auth.getDeviceCode());
			deviceDTO.setToken(auth.getAuthToken());
			BitsEnDecrypt decrypt = new BitsEnDecrypt();
			deviceDTO.setToken(decrypt.decrypt(auth.getDeviceToken()));
			AuthDTO authDTO = authService.CheckDeviceAuthendtication(namespaceDTO, deviceDTO, AuthenticationTypeEM.BITS_USERNAME_PASSWORD, auth.getUsername(), auth.getPassword(), DeviceMediumEM.WEB_USER);
			if (authDTO != null) {
				RoleIO role = new RoleIO();
				role.setCode(authDTO.getUser().getUserRole().getCode());
				role.setName(authDTO.getUser().getUserRole().getName());
				authIO.setRole(role);
				authIO.setAuthToken(authDTO.getAuthToken());
			}
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/tablet/getAuthToken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AuthVechicleIO> getTabletAuthToken(@RequestBody AuthIO auth, @RequestParam(value = "authenticationTypeCode", required = true, defaultValue = "TABPOB") String authenticationTypeCode) throws Exception {
		AuthVechicleIO vechicle = new AuthVechicleIO();
		if (StringUtil.isNotNull(auth.getNamespaceCode()) && StringUtil.isNotNull(auth.getUsername()) && StringUtil.isNotNull(auth.getDeviceCode()) && StringUtil.isNotNull(auth.getDeviceToken())) {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setCode(auth.getNamespaceCode());
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(auth.getDeviceCode());
			AuthDTO authDTO = authService.CheckTabletAuthendtication(namespaceDTO, tabletDTO, AuthenticationTypeEM.getAuthenticationTypeEM(authenticationTypeCode), auth.getUsername(), auth.getDeviceToken(), DeviceMediumEM.APP_TABLET_POB);
			if (authDTO != null) {
				AuthIO authIO = new AuthIO();
				RoleIO role = new RoleIO();
				role.setCode(authDTO.getUser().getUserRole().getCode());
				role.setName(authDTO.getUser().getUserRole().getName());
				authIO.setRole(role);
				authIO.setAuthToken(authDTO.getAuthToken());
				vechicle.setAuth(authIO);

				BusVehicleIO vehicle = new BusVehicleIO();
				if (AuthenticationTypeEM.TABLET_POB.getCode().equals(authenticationTypeCode)) {
					vehicle.setCode(tabletDTO.getBusVehicle().getCode());
					vehicle.setName(tabletDTO.getBusVehicle().getName());

					BusIO busIO = new BusIO();
					busIO.setCode(tabletDTO.getBusVehicle().getBus().getCode());
					busIO.setName(tabletDTO.getBusVehicle().getBus().getName());
					busIO.setDisplayName(tabletDTO.getName());
					busIO.setCategoryCode(tabletDTO.getBusVehicle().getBus().getCategoryCode());
					vehicle.setBus(busIO);

					vehicle.setRegistrationDate(tabletDTO.getBusVehicle().getRegistrationDate());
					vehicle.setRegistationNumber(tabletDTO.getBusVehicle().getRegistationNumber());
					vehicle.setLicNumber(tabletDTO.getBusVehicle().getLicNumber());
					vehicle.setGpsDeviceCode(tabletDTO.getBusVehicle().getGpsDeviceCode());

					BaseIO deviceVendor = new BaseIO();
					deviceVendor.setCode(tabletDTO.getBusVehicle().getDeviceVendor().getCode());
					deviceVendor.setName(tabletDTO.getBusVehicle().getDeviceVendor().getName());
					vehicle.setGpsDeviceVendor(deviceVendor);

					vehicle.setMobileNumber(tabletDTO.getBusVehicle().getMobileNumber());
					vehicle.setActiveFlag(tabletDTO.getBusVehicle().getActiveFlag());
				}
				vechicle.setVehicle(vehicle);
			}
		}
		return ResponseIO.success(vechicle);
	}

	@RequestMapping(value = "/{authtoken}/session/audit/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> sessionAuditUpdate(@PathVariable("authtoken") String authtoken, @RequestBody UserSessionAuditIO sessionAudit) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserSessionAuditDTO sessionAuditDTO = new UserSessionAuditDTO();
			if (StringUtil.isNull(sessionAudit.getIpAddress())) {
				sessionAudit.setIpAddress(Text.NA);
			}
			if (StringUtil.isNull(sessionAudit.getLatitude())) {
				sessionAudit.setLatitude(Text.NA);
			}
			if (StringUtil.isNull(sessionAudit.getLongitude())) {
				sessionAudit.setLongitude(Text.NA);
			}
			sessionAuditDTO.setIpAddress(sessionAudit.getIpAddress());
			sessionAuditDTO.setLatitude(sessionAudit.getLatitude());
			sessionAuditDTO.setLongitude(sessionAudit.getLongitude());
			userService.sessionAuditUpdate(authDTO, sessionAuditDTO);
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/session/recent", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserSessionAuditIO>> getUserRecentSession(@PathVariable("authtoken") String authtoken) throws Exception {
		List<UserSessionAuditIO> sessionList = new ArrayList<UserSessionAuditIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<UserSessionAuditDTO> list = userService.getUserRecentSession(authDTO, authDTO.getUser());
			for (UserSessionAuditDTO auditDTO : list) {
				UserSessionAuditIO sessionAudit = new UserSessionAuditIO();
				sessionAudit.setIpAddress(auditDTO.getIpAddress());
				sessionAudit.setLatitude(auditDTO.getLatitude());
				sessionAudit.setLongitude(auditDTO.getLongitude());
				sessionAudit.setSessionStartAt(auditDTO.getSessionStartAt());
				sessionAudit.setSessionEndAt(auditDTO.getSessionEndAt());
				BaseIO deviceMedium = new BaseIO();
				deviceMedium.setCode(auditDTO.getDeviceMedium().getCode());
				deviceMedium.setName(auditDTO.getDeviceMedium().getName());
				sessionAudit.setDeviceMedium(deviceMedium);
				BaseIO sessionStatus = new BaseIO();
				sessionStatus.setCode(auditDTO.getSessionStatus().getCode());
				sessionStatus.setName(auditDTO.getSessionStatus().getName());
				sessionAudit.setSessionStatus(sessionStatus);
				sessionList.add(sessionAudit);
			}
		}
		return ResponseIO.success(sessionList);
	}

	@RequestMapping(value = "/{authtoken}/namespace", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getNamespace(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BaseIO> list = new ArrayList<BaseIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<NamespaceDTO> namespaceList = namespaceService.getAllUserNamespaceMap(authDTO, authDTO.getUser());
		for (NamespaceDTO dto : namespaceList) {
			if (dto.getActiveFlag() != 1) {
				continue;
			}
			BaseIO namespace = new BaseIO();
			namespace.setCode(dto.getCode());
			namespace.setName(dto.getName());
			namespace.setActiveFlag(dto.getActiveFlag());
			list.add(namespace);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{authtoken}/customer/{mobileNumber}/otp/generate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> generateCustomerOTP(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (!StringUtil.isValidMobileNumber(mobileNumber)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			authService.generateCustomerOTP(authDTO, mobileNumber);
			baseIO.setCode(mobileNumber);
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/{authtoken}/subscription/authenticate/{deviceMedium}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> authenticateSubscription(@PathVariable("authtoken") String authtoken, @PathVariable("deviceMedium") String deviceMedium) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		String otp = authService.generateBotOTP(authDTO);
		baseIO.setCode(otp);
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/{authtoken}/customer/{mobileNumber}/validate/otp/{otpNumber}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> validateCustomerOTP(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber, @PathVariable("otpNumber") int otpNumber) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (!StringUtil.isValidMobileNumber(mobileNumber)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}

			AuthDTO authCache = authService.getCustomerAuthendticationV2(authDTO, mobileNumber, otpNumber);
			authIO.setAuthToken(authCache.getAuthToken());
			authIO.setSessionToken(authCache.getUserCustomer().getUserCustomerAuth().getSessionToken());
		}
		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/customer/{mobileNumber}/validate/sessiontoken/{sessionToken}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuthIO> getAuthBySessiontoken(@PathVariable("authtoken") String authtoken, @PathVariable("mobileNumber") String mobileNumber, @PathVariable("sessionToken") String sessionToken) throws Exception {
		AuthIO authIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
		userCustomerDTO.setMobile(mobileNumber);
		UserCustomerAuthDTO customerAuthDTO = new UserCustomerAuthDTO();
		customerAuthDTO.setDeviceMedium(authDTO.getDeviceMedium());
		customerAuthDTO.setSessionToken(sessionToken);
		userCustomerDTO.setUserCustomerAuth(customerAuthDTO);

		AuthDTO authCache = authService.getCustomerAuthendticationV3(authDTO, userCustomerDTO);
		authIO.setAuthToken(authCache.getAuthToken());
		authIO.setSessionToken(authCache.getUserCustomer().getUserCustomerAuth().getSessionToken());

		return ResponseIO.success(authIO);
	}

	@RequestMapping(value = "/{authtoken}/profile/generate/otp", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, String>> generateProfileOTP(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		String mobileNumber = authDTO.getUser().getMobile();
		if (!StringUtil.isValidMobileNumber(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}

		Map<String, String> dataMap = userService.generateProfileOTPV2(authDTO, mobileNumber);

		return ResponseIO.success(dataMap);
	}

	@RequestMapping(value = "/{authtoken}/profile/validate/otp/{otpNumber}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> validateProfileOTP(@PathVariable("authtoken") String authtoken, @PathVariable("otpNumber") int otpNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		String mobileNumber = authDTO.getUser().getMobile();
		if (!StringUtil.isValidMobileNumber(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		userService.validateProfileOTPV2(authDTO, mobileNumber, otpNumber);

		return ResponseIO.success();
	}
}
