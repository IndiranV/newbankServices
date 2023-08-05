package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.slack.SlackService;
import org.in.com.cache.AuthCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.dto.AuthCacheDTO;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.PrivilegeDAO;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.SessionStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.CommissionService;
import org.in.com.service.GroupService;
import org.in.com.service.IntegrationService;
import org.in.com.service.MenuService;
import org.in.com.service.NamespaceDeviceAuthService;
import org.in.com.service.NamespaceService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.TabletService;
import org.in.com.service.TransactionOTPService;
import org.in.com.service.UserCustomerService;
import org.in.com.service.UserLoginHistoryService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsShortURL;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenEncrypt;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

@Service
public class AuthImpl extends BaseImpl implements AuthService {

	private static String CUST_OTP_KEY = "UCUS";

	@Autowired
	MenuService menuService;
	@Autowired
	CommissionService commissionService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	EmailService emailService;
	@Autowired
	UserService userService;
	@Autowired
	NamespaceDeviceAuthService deviceAuthService;
	@Autowired
	TransactionOTPService otpService;
	@Autowired
	UserCustomerService userCustomerService;
	@Autowired
	TabletService tabletService;
	@Autowired
	BusVehicleDriverService driverService;
	@Autowired
	GroupService groupService;
	@Autowired
	IntegrationService integrationService;
	@Autowired
	BitsService bitsService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	UserLoginHistoryService userLoginHistoryService;
	@Autowired
	SlackService slack;

	// code added to handle exception
	public AuthDTO CheckAuthendtication(NamespaceDTO namespaceDTO, AuthenticationTypeEM authenticationType, String username, String password, DeviceMediumEM devicemedium, String userFirstName) {
		AuthDTO authDTO = new AuthDTO();
		try {
			PrivilegeDAO dao = new PrivilegeDAO();
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			if (namespaceDTO == null || namespaceDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			authDTO.setNativeNamespaceCode(namespaceDTO.getCode());
			authDTO.setDeviceMedium(devicemedium);

			boolean authenticationFlag = false;
			// Bits Username password validation
			if (authenticationType.getId() == AuthenticationTypeEM.BITS_USERNAME_PASSWORD.getId()) {
				UserDTO userDTO = dao.getAuthendtication(authDTO, username);

				if (userDTO.getIntegration() != null && userDTO.getIntegration().getId() != 0) {
					integrationService.get(authDTO, userDTO.getIntegration());
				}

				if (userDTO.getIntegration() == null || userDTO.getIntegration().getIntegrationtype() == null || userDTO.getIntegration().getIntegrationtype().getId() == IntegrationTypeEM.BITS.getId()) {
					if (userDTO == null || userDTO.getToken() == null) {
						throw new ServiceException(102);
					}
					if (userDTO.getActiveFlag() != 1 || userDTO.isOAuth()) {
						throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
					}
					if (userDTO.getToken().equals(TokenEncrypt.encryptString(password))) {
						authenticationFlag = true;
					}
				}
				else if (userDTO.getIntegration() != null && userDTO.getIntegration().getIntegrationtype() != null && userDTO.getIntegration().getIntegrationtype().getId() != IntegrationTypeEM.BITS.getId()) {
					UserDTO bitsUserDTO = new UserDTO();
					bitsUserDTO.setUsername(username);
					bitsUserDTO.setOldPassword(password);
					bitsUserDTO.setIntegration(userDTO.getIntegration());

					bitsUserDTO = bitsService.getAuthToken(bitsUserDTO.getIntegration(), bitsUserDTO);
					if (bitsUserDTO != null && StringUtil.isNotNull(bitsUserDTO.getToken())) {
						authDTO.setAuthToken(bitsUserDTO.getToken());
						authenticationFlag = true;
					}
					else {
						throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
					}
				}
				authDTO.setUserCode(userDTO.getCode());
			}
			// Bits SSO Login
			else if (authenticationType.getId() == AuthenticationTypeEM.SSO_FACEBOOK.getId() || authenticationType.getId() == AuthenticationTypeEM.SSO_GMAIL.getId()) {
				UserDTO userDTO = new UserDTO();
				userDTO.setUsername(username);
				userDTO.setName(userFirstName);
				userDTO.setToken(password);
				if (StringUtil.isNull(username) || StringUtil.isNull(userFirstName) || StringUtil.isNull(password)) {
					throw new ServiceException(ErrorCode.USER_INVALID_EMAIL_PASSWORD);
				}
				userService.findAndRegisterUser(authDTO, userDTO);

				if (userDTO.getId() != 0) {
					authenticationFlag = true;
					authDTO.setUserCode(userDTO.getCode());
				}
			}
			if (authenticationFlag) {
				if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
					// User Menu
					List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
					authDTO.setPrivileges(privilegesDTOlist);
				}
				if (StringUtil.isNull(authDTO.getAuthToken())) {
					TokenGenerator idGenerator = new TokenGenerator();
					authDTO.setAuthToken(idGenerator.getSessionTrackingId());
				}
				authDTO.setDeviceMedium(devicemedium);
				authDTO.setAuthenticationType(authenticationType);

				userLoginHistoryService.saveLoginEntry(authDTO);

				if (authDTO.getGroup() != null && authDTO.getGroup().getId() != 0) {
					authDTO.getUser().setGroup(groupService.getGroup(authDTO, authDTO.getGroup()));
				}
				if (authDTO.getUser().getOrganization() != null && authDTO.getUser().getOrganization().getId() != 0) {
					authDTO.getUser().setOrganization(getOrganizationDTObyId(authDTO, authDTO.getUser().getOrganization()));
				}
				AuthCache authCache = new AuthCache();
				authCache.putAuthDTO(authDTO);
			}
			else {
				// the below code has been handled in exception block
				throw new ServiceException(101);
			}
			// push Flutter Notification
			notificationPushService.pushUserNotification(authDTO, authDTO.getUser(), NotificationSubscriptionTypeEM.ALL_USER_LOGIN);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(500);
		}

		return authDTO;
	}

	public void switchNamespace(AuthDTO authDTO, NamespaceDTO namespaceDTO) {
		namespaceDTO = namespaceService.getNamespace(namespaceDTO);
		if (namespaceDTO == null || namespaceDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		namespaceService.checkUserNamespaceMapping(authDTO, namespaceDTO);
		if (namespaceDTO.getLookupId() == 0) {
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			AuthCache authCache = new AuthCache();
			authCache.putAuthDTO(authDTO);
		}
		else if (namespaceDTO.getLookupId() != 0) {
			NamespaceDTO namespace = getAliasNamespace(namespaceDTO.getCode());
			authDTO.setNamespaceCode(namespace.getCode());
			AuthCache authCache = new AuthCache();
			authCache.putAuthDTO(authDTO);
		}
	}

	public AuthDTO APIAuthendtication(String apiToken) {
		AuthDTO authDTO = null;
		try {
			AuthCache authCache = new AuthCache();
			authDTO = authCache.getAPIAuthDTO(apiToken);
			if (authDTO == null) {
				PrivilegeDAO dao = new PrivilegeDAO();
				authDTO = dao.getAPIAuthendtication(apiToken);
				if (authDTO == null || StringUtil.isNull(authDTO.getUserCode())) {
					throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
				}
				// User Menu
				if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
					List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
					authDTO.setPrivileges(privilegesDTOlist);
				}
				authDTO.setDeviceMedium(DeviceMediumEM.API_USER);
				authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_API_TOKEN);

				authDTO.setApiToken(apiToken);
				authDTO.setAuthToken(apiToken);
				authCache.putAuthDTO(authDTO);
			}
			if (StringUtil.isNull(authDTO.getUserCode())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
			}
			if (!authDTO.getUser().getApiToken().equals(apiToken)) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return authDTO;
	}

	public AuthDTO getAuthDTO(String authtoken) {
		AuthCache authCache = new AuthCache();
		AuthDTO authDTO = authCache.getAuthDTO(authtoken);
		if (authDTO == null) {
			if (authtoken.contains(Text.NAMESPACE_GUEST_USERNAME)) {
				String guestKey = null;
				for (Object key : EhcacheManager.getGuestAuthTokenEhCache().getKeys()) {
					Element guestElement = EhcacheManager.getGuestAuthTokenEhCache().get(key);
					if (guestElement != null && authtoken.trim().equals((String) guestElement.getObjectValue())) {
						guestKey = (String) key;
						break;
					}
				}
				if (StringUtil.isNotNull(guestKey)) {
					EhcacheManager.getGuestAuthTokenEhCache().remove(guestKey);
				}
			}
			else {
				throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
			}
		}
		if (authDTO == null || StringUtil.isNull(authDTO.getUserCode()) || StringUtil.isNull(authDTO.getNamespaceCode())) {
			authCache.clearAuthDTO(authtoken);
			throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
		}
		return authDTO;
	}

	//
	public AuthDTO getAuthDetails(String authtoken, String guestKey) {
		AuthCache authCache = new AuthCache();
		AuthDTO authDTO = authCache.getAuthDTO(authtoken);
		if (authDTO == null) {
			EhcacheManager.getGuestAuthTokenEhCache().remove(guestKey);
		}
		return authDTO;

	}

	public UserDTO getUserDTO(String authtoken) {
		AuthCache authCache = new AuthCache();
		AuthDTO authDTO = authCache.getAuthDTO(authtoken);
		if (authDTO != null) {
			return authDTO.getUser();
		}
		return null;
	}

	public void logout(String authtoken) {
		AuthCache authCache = new AuthCache();
		AuthDTO authDTO = authCache.getAuthDTO(authtoken);
		if (authDTO != null) {
			PrivilegeDAO privilegeDAO = new PrivilegeDAO();
			privilegeDAO.updateUserLoginSession(authDTO, authDTO, SessionStatusEM.SESSION_END_BY_USER);
		}
		authCache.clearAuthDTO(authtoken);
	}

	@Override
	public AuthDTO getGuestAuthendtication(String namespaceCode, DeviceMediumEM devicemedium) {
		AuthDTO authDTO = new AuthDTO();
		NamespaceDTO namespaceDTO = namespaceService.getNamespace(namespaceCode);
		try {
			if (namespaceDTO == null || namespaceDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			PrivilegeDAO dao = new PrivilegeDAO();
			UserDTO userDTO = dao.getAuthendtication(authDTO, Text.NAMESPACE_GUEST_USERNAME + "@" + namespaceDTO.getCode());
			if (userDTO == null || userDTO.getToken() == null) {
				throw new ServiceException(ErrorCode.USER_INVALID_EMAIL);
			}
			if (userDTO.getToken().equals(TokenEncrypt.encryptString(Text.NAMESPACE_GUEST_PASSWORD))) {
				authDTO.setUserCode(userDTO.getCode());
				TokenGenerator idGenerator = new TokenGenerator();
				authDTO.setAuthToken(namespaceDTO.getCode() + Text.NAMESPACE_GUEST_USERNAME + idGenerator.getSessionTrackingId());
				authDTO.setDeviceMedium(devicemedium);
				authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_GUEST);
				authDTO.setNativeNamespaceCode(authDTO.getNamespace().getCode());

				AuthCache authCache = new AuthCache();
				authCache.putAuthDTO(authDTO);
				String guestKey = StringUtil.removeSymbol(namespaceCode + devicemedium.getCode());
				Element element = new Element(guestKey, authDTO.getAuthToken());
				element.setTimeToIdle(0);
				element.setTimeToLive(0);
				EhcacheManager.getGuestAuthTokenEhCache().put(element);
			}
			else {
				// the below code has been handled in exception block
				throw new ServiceException(ErrorCode.USER_INVALID_EMAIL_PASSWORD);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			throw new ServiceException(500);
		}
		return authDTO;
	}

	@Override
	public AuthDTO getGuestAuthendticationV2(String namespaceCode, DeviceMediumEM devicemedium, String contextToken) {
		AuthDTO authDTO = new AuthDTO();
		NamespaceDTO namespaceDTO = namespaceService.getNamespace(namespaceCode);
		try {
			if (namespaceDTO == null || namespaceDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}
			if (StringUtil.isNull(namespaceDTO.getContextToken()) || !contextToken.equals(namespaceDTO.getContextToken())) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED, "Invalid context token");
			}
			authDTO = getGuestAuthendtication(namespaceCode, devicemedium);
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			throw new ServiceException(500);
		}
		return authDTO;
	}

	public UserDTO forgetPassword(AuthDTO authDTO, String username) {
		UserDTO userDTO = new UserDTO();
		userDTO.setUsername(username);
		userDTO = userService.getUserDTO(authDTO, userDTO);
		if (userDTO != null && StringUtil.isNotNull(userDTO.getEmail()) && StringUtil.isNotNull(userDTO.getCode()) && userDTO.getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
			// Generate password
			userDTO.setForgetToken(TokenEncrypt.encryptString(userDTO.getCode()));
			UserDAO dao = new UserDAO();
			// Update forget Token
			dao.updateForgetTokenPassword(authDTO, userDTO);

			// Send email Notifications
			emailService.sendForgetPasswordEmail(authDTO, userDTO);
		}
		else {
			throw new ServiceException(ErrorCode.USER_INVALID_EMAIL);
		}
		return userDTO;

	}

	public AuthDTO APIAuthendtication(String namespaceCode, String username, String apiToken) {

		AuthDTO authDTO = null;
		String invalidAPIAuthErrorCode = null;
		try {
			AuthCache authCache = new AuthCache();
			authDTO = authCache.getAPIAuthDTO(apiToken);
			if (authDTO == null) {
				// namespace alias concepts
				NamespaceDTO namespace = getAliasNamespace(namespaceCode);

				// Check any existing error found
				invalidAPIAuthErrorCode = authCache.checkInvalidAPIAuth(namespace.getCode(), username);
				if (StringUtil.isNotNull(invalidAPIAuthErrorCode)) {
					throw new ServiceException(ErrorCode.getErrorCode(invalidAPIAuthErrorCode));
				}

				PrivilegeDAO dao = new PrivilegeDAO();
				authDTO = dao.getAPIAuthendticationV3(namespace, username);
				if (authDTO == null || StringUtil.isNull(authDTO.getUserCode()) || StringUtil.isNull(authDTO.getApiToken()) || authDTO.getUser() == null) {
					throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
				}
				if (StringUtil.isNotNull(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE")) && !namespaceCode.equals(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE"))) {
					throw new ServiceException(ErrorCode.INVALID_OPERATOR);
				}
				if (!authDTO.getApiToken().equals(apiToken)) {
					throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
				}
				authDTO.setDeviceMedium(DeviceMediumEM.API_USER);
				authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_API_TOKEN);
				authDTO.getAdditionalAttribute().put(Text.ALIAS_NAMESPACE_CODE, namespace.getAliasCode());

				// User Menu
				if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
					List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
					authDTO.setPrivileges(privilegesDTOlist);
				}
				authDTO.setApiToken(apiToken);
				authDTO.setAuthToken(apiToken);
				// check sector permission
				checkSectorPermission(authDTO);
				authCache.putAuthDTO(authDTO);
			}
			if (StringUtil.isNull(authDTO.getUserCode())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
			}
			if (StringUtil.isNotNull(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE")) && !namespaceCode.equals(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE"))) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_OPERATOR);
			}
			if (!username.equals(authDTO.getUser().getUsername())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
			if (!apiToken.equals(authDTO.getUser().getApiToken())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
		}
		catch (ServiceException e) {
			if (!e.getErrorCode().getCode().equals(invalidAPIAuthErrorCode)) {
				AuthCache authCache = new AuthCache();
				authCache.putInvalidAPIAuth(namespaceCode, username, e.getErrorCode().getCode());
			}
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return authDTO;
	}

	public AuthDTO APIAuthendtication(String namespaceCode, String username, String apiToken, DeviceMediumEM deviceMedium) {
		AuthDTO authDTO = null;
		String invalidAPIAuthErrorCode = null;
		try {
			if (!deviceMedium.getCode().equals(DeviceMediumEM.API_USER.getCode()) && !deviceMedium.getCode().equals(DeviceMediumEM.WEB_API_USER.getCode())) {
				throw new ServiceException(ErrorCode.INVALID_DEVICE_MEDIUM);
			}
			AuthCache authCache = new AuthCache();
			authDTO = authCache.getAPIAuthDTO(apiToken);
			if (authDTO == null) {
				// namespace alias concepts
				NamespaceDTO namespace = getAliasNamespace(namespaceCode);

				// Check any existing error found
				invalidAPIAuthErrorCode = authCache.checkInvalidAPIAuth(namespace.getCode(), username);
				if (StringUtil.isNotNull(invalidAPIAuthErrorCode)) {
					throw new ServiceException(ErrorCode.getErrorCode(invalidAPIAuthErrorCode));
				}

				PrivilegeDAO dao = new PrivilegeDAO();
				authDTO = dao.getAPIAuthendticationV3(namespace, username);
				if (authDTO == null || StringUtil.isNull(authDTO.getUserCode()) || StringUtil.isNull(authDTO.getApiToken()) || authDTO.getUser() == null) {
					throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
				}
				if (StringUtil.isNotNull(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE")) && !namespaceCode.equals(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE"))) {
					throw new ServiceException(ErrorCode.INVALID_OPERATOR);
				}
				if (!authDTO.getApiToken().equals(apiToken)) {
					throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
				}
				authDTO.setDeviceMedium(deviceMedium);
				authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_API_TOKEN);
				authDTO.getAdditionalAttribute().put(Text.ALIAS_NAMESPACE_CODE, namespace.getAliasCode());

				// User Menu
				if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
					List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
					authDTO.setPrivileges(privilegesDTOlist);
				}
				authDTO.setApiToken(apiToken);
				authDTO.setAuthToken(apiToken);
				// check sector permission
				checkSectorPermission(authDTO);
				authCache.putAuthDTO(authDTO);
			}
			if (StringUtil.isNull(authDTO.getUserCode())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
			}
			if (StringUtil.isNotNull(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE")) && !namespaceCode.equals(authDTO.getUser().getAdditionalAttribute().get("ALIAS_NAMESPACE"))) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_OPERATOR);
			}
			if (!username.equals(authDTO.getUser().getUsername())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
			if (!apiToken.equals(authDTO.getUser().getApiToken())) {
				authCache.clearAuthDTO(apiToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
		}
		catch (ServiceException e) {
			slack.sendAlert(ApplicationConfig.getServerZoneCode() + Text.SINGLE_SPACE + namespaceCode + Text.SINGLE_SPACE + username + Text.SINGLE_SPACE + apiToken + Text.SINGLE_SPACE + deviceMedium.getCode() + " DL11 - API authentication failed" + e.getErrorCode().getCode());
			if (!e.getErrorCode().getCode().equals(invalidAPIAuthErrorCode)) {
				AuthCache authCache = new AuthCache();
				authCache.putInvalidAPIAuth(namespaceCode, username, e.getErrorCode().getCode());
			}
			System.out.println("DL11 - API authentication failed " + namespaceCode + username + apiToken + e.getErrorCode().getCode() + e.getErrorCode().getMessage());
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			System.out.println("DL12 - API authentication failed " + namespaceCode + username + apiToken);
			slack.sendAlert(ApplicationConfig.getServerZoneCode() + Text.SINGLE_SPACE + namespaceCode + Text.SINGLE_SPACE + username + Text.SINGLE_SPACE + apiToken + Text.SINGLE_SPACE + deviceMedium.getCode() + " DL12 - API authentication failed");
			e.printStackTrace();
		}
		return authDTO;
	}

	public AuthDTO getApiAuthendtication(String namespaceCode, String username, String apiToken, DeviceMediumEM deviceMedium) {
		AuthDTO authDTO = null;
		try {
			AuthCache authCache = new AuthCache();
			PrivilegeDAO dao = new PrivilegeDAO();
			authDTO = dao.getAPIAuthendticationV3(namespaceService.getNamespace(namespaceCode), username);

			if (authDTO == null || StringUtil.isNull(authDTO.getUserCode())) {
				throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
			}
			if (!namespaceCode.equals(authDTO.getNamespaceCode())) {
				throw new ServiceException(ErrorCode.INVALID_OPERATOR);
			}
			if (!username.equals(authDTO.getUser().getUsername())) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
			if (!authDTO.getApiToken().equals(apiToken)) {
				throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
			}
			authDTO.setDeviceMedium(deviceMedium);
			authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_API_TOKEN);

			// User Menu
			if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
				List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
				authDTO.setPrivileges(privilegesDTOlist);
			}
			TokenGenerator idGenerator = new TokenGenerator();
			authDTO.setAuthToken(idGenerator.getSessionTrackingId());
			authCache.putAuthDTO(authDTO);

		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
		}
		return authDTO;
	}

	public void removeUserAuthToken(AuthDTO authDTO, UserDTO userDTO) {
		for (Object key : EhcacheManager.getAuthTokenEhCache().getKeys()) {
			Element element = EhcacheManager.getAuthTokenEhCache().get(key);
			if (element != null) {
				AuthCacheDTO auth = (AuthCacheDTO) element.getObjectValue();
				if (userDTO.getCode().equals(auth.getUserCacheCode())) {
					EhcacheManager.getAuthTokenEhCache().remove(key);
					System.out.println("remove session : " + userDTO.getCode() + " - " + key);
				}
			}
		}
	}

	@Override
	public AuthDTO CheckDeviceAuthendtication(NamespaceDTO namespaceDTO, NamespaceDeviceDTO deviceDTO, AuthenticationTypeEM authenticationType, String username, String password, DeviceMediumEM devicemedium) {

		AuthDTO authDTO = new AuthDTO();
		try {
			PrivilegeDAO dao = new PrivilegeDAO();
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			if (namespaceDTO == null || namespaceDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			authDTO.setNativeNamespaceCode(namespaceDTO.getCode());

			boolean authenticationFlag = false;
			// Bits Username password validation
			if (authenticationType.getId() == AuthenticationTypeEM.BITS_USERNAME_PASSWORD.getId()) {
				UserDTO userDTO = dao.getAuthendtication(authDTO, username);
				if (userDTO == null || userDTO.getToken() == null) {
					throw new ServiceException(102);
				}
				if (userDTO.getActiveFlag() != 1) {
					throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
				}
				if (userDTO.getToken().equals(TokenEncrypt.encryptString(password))) {
					authenticationFlag = deviceAuthService.checkDeviceUserAuthendtication(authDTO, deviceDTO, userDTO);
				}
				authDTO.setUserCode(userDTO.getCode());
			}
			if (authenticationFlag) {
				if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
					// User Menu
					List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
					authDTO.setPrivileges(privilegesDTOlist);
				}
				TokenGenerator idGenerator = new TokenGenerator();
				authDTO.setAuthToken(idGenerator.getSessionTrackingId());
				authDTO.setDeviceMedium(devicemedium);
				authDTO.setAuthenticationType(authenticationType);

				dao.loginEntry(authDTO, Numeric.ZERO_INT);

				if (authDTO.getGroup() != null && authDTO.getGroup().getId() != 0) {
					authDTO.getUser().setGroup(groupService.getGroup(authDTO, authDTO.getGroup()));
				}
				if (authDTO.getUser().getOrganization() != null && authDTO.getUser().getOrganization().getId() != 0) {
					authDTO.getUser().setOrganization(getOrganizationDTObyId(authDTO, authDTO.getUser().getOrganization()));
				}
				AuthCache authCache = new AuthCache();
				authCache.putAuthDTO(authDTO);
			}
			else {
				// the below code has been handled in exception block
				throw new ServiceException(101);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(500);
		}

		return authDTO;
	}

	@Override
	public AuthDTO CheckTabletAuthendtication(NamespaceDTO namespaceDTO, TabletDTO tabletDTO, AuthenticationTypeEM authenticationType, String username, String deviceToken, DeviceMediumEM deviceMedium) {

		AuthDTO authDTO = new AuthDTO();
		try {
			PrivilegeDAO dao = new PrivilegeDAO();
			namespaceDTO = namespaceService.getNamespace(namespaceDTO);
			if (namespaceDTO == null || namespaceDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			authDTO.setNativeNamespaceCode(namespaceDTO.getCode());

			boolean authenticationFlag = false;
			// Bits Username password validation
			if (authenticationType.getId() == AuthenticationTypeEM.TABLET_POB.getId()) {
				UserDTO userDTO = dao.getAuthendtication(authDTO, username);
				if (userDTO == null || userDTO.getToken() == null) {
					throw new ServiceException(102);
				}
				if (userDTO.getActiveFlag() != 1) {
					throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
				}
				TabletDTO tablet = tabletService.getTablet(authDTO, tabletDTO.getCode());
				if (tablet == null || tablet.getActiveFlag() != 1 || StringUtil.isNull(tablet.getUser().getUsername())) {
					throw new ServiceException(ErrorCode.INVALID_DEVICE_CODE);
				}
				if (tablet.getUser().getUsername().equals(userDTO.getUsername()) && userDTO.getToken().equals(deviceToken) && userDTO.getId() == tablet.getUser().getId() && tablet.getId() != 0) {
					if (StringUtil.isNull(tablet.getBusVehicle().getCode())) {
						throw new ServiceException(ErrorCode.INVALID_VEHICLE_CODE);
					}
					authenticationFlag = true;
					tabletDTO.setBusVehicle(tablet.getBusVehicle());
					tabletDTO.setName(tablet.getName());
				}
				authDTO.setUserCode(userDTO.getCode());
			}
			else if (authenticationType.getId() == AuthenticationTypeEM.TABLET_POB_DRIVER.getId()) {
				UserDTO userDTO = dao.getAuthendtication(authDTO, username);
				if (userDTO == null || userDTO.getToken() == null) {
					throw new ServiceException(102);
				}
				if (userDTO.getActiveFlag() != 1) {
					throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
				}
				BusVehicleDriverDTO busVehicleDriver = new BusVehicleDriverDTO();
				busVehicleDriver.setCode(tabletDTO.getCode());
				driverService.get(authDTO, busVehicleDriver);
				if (busVehicleDriver.getActiveFlag() != 1 || StringUtil.isNull(busVehicleDriver.getUser().getUsername())) {
					throw new ServiceException(ErrorCode.INVALID_DEVICE_CODE);
				}
				if (busVehicleDriver.getUser().getUsername().equals(userDTO.getUsername()) && userDTO.getToken().equals(deviceToken) && userDTO.getId() == busVehicleDriver.getUser().getId() && busVehicleDriver.getId() != 0) {
					authenticationFlag = true;
					tabletDTO.setName(busVehicleDriver.getName());
				}
				authDTO.setUserCode(userDTO.getCode());
			}
			if (authenticationFlag) {
				TokenGenerator idGenerator = new TokenGenerator();
				authDTO.setAuthToken(idGenerator.getSessionTrackingId());
				authDTO.setDeviceMedium(deviceMedium);
				authDTO.setAuthenticationType(authenticationType);

				dao.loginEntry(authDTO, Numeric.ZERO_INT);

				if (authDTO.getUser().getGroup() != null && authDTO.getUser().getGroup().getId() != 0) {
					authDTO.getUser().setGroup(groupService.getGroup(authDTO, authDTO.getUser().getGroup()));
				}
				if (authDTO.getUser().getOrganization() != null && authDTO.getUser().getOrganization().getId() != 0) {
					authDTO.getUser().setOrganization(getOrganizationDTObyId(authDTO, authDTO.getUser().getOrganization()));
				}
				AuthCache authCache = new AuthCache();
				authCache.putAuthDTO(authDTO);
			}
			else {
				// the below code has been handled in exception block
				throw new ServiceException(ErrorCode.USER_INVALID_DEVICE_AUTH_TOKEN);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(500);
		}

		return authDTO;
	}

	@Override
	public void getLogoutSessionToken(AuthDTO authDTO, String sessiontoken) {
		AuthCache authCache = new AuthCache();
		AuthDTO sessionAuthDTO = authCache.getAuthDTO(sessiontoken);
		if (sessionAuthDTO != null && sessionAuthDTO.getNamespaceCode().equals(authDTO.getNamespaceCode()) && sessionAuthDTO.getUser().getUserRole().getId() != UserRoleEM.CUST_ROLE.getId()) {
			PrivilegeDAO privilegeDAO = new PrivilegeDAO();
			privilegeDAO.updateUserLoginSession(authDTO, sessionAuthDTO, SessionStatusEM.SESSION_TERMINATED_BY_USER);
			authCache.clearAuthDTO(sessiontoken);
		}
	}

	@Override
	public AuthDTO getVerifyPassword(AuthDTO authDTO, String password) {
		UserDTO userDTO = userService.getUserDTO(authDTO, authDTO.getUser());
		if (!userDTO.getToken().equals(TokenEncrypt.encryptString(password))) {
			throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN);
		}
		return authDTO;
	}

	@Override
	public void updateUserLoginSession(AuthDTO authDTO) {
		if (authDTO != null && StringUtil.isNotNull(authDTO.getAuthToken())) {
			PrivilegeDAO privilegeDAO = new PrivilegeDAO();
			privilegeDAO.updateUserLoginSession(authDTO, authDTO, SessionStatusEM.SESSION_END_BY_SYSTEM);
		}
	}

	@Override
	public void switchUser(AuthDTO authDTO, UserDTO user) {
		if (ApplicationConfig.getServerZoneCode().equals(authDTO.getNamespaceCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		user = getUserDTO(authDTO, user);
		if (user == null || user.getId() == Numeric.ZERO_INT || user.getActiveFlag() != Numeric.ONE_INT) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}

		// Validate Group Level Permission
		GroupDTO authUserGroup = groupService.getGroup(authDTO, authDTO.getGroup());
		GroupDTO userGroup = groupService.getGroup(authDTO, user.getGroup());
		if (userGroup.getLevel() > authUserGroup.getLevel()) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED, "Group level permission denied");
		}
		System.out.println(authDTO.getUserCode() + " is switched to " + user.getCode());

		UserDTO currentUser = authDTO.getUser();
		authDTO.setUserCode(user.getCode());
		authDTO.setUser(null);

		// Privileges for User
		List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
		authDTO.setPrivileges(privilegesDTOlist);

		AuthCache authCache = new AuthCache();
		authCache.putAuthDTO(authDTO);

		String events = currentUser.getName() + " switched to  " + user.getName();
		authDTO.getAdditionalAttribute().put("events", events);

		PrivilegeDAO dao = new PrivilegeDAO();
		dao.loginEntry(authDTO, Numeric.ZERO_INT);
	}

	@Override
	public void generateCustomerOTP(AuthDTO authDTO, String mobileNumber) {
		// Send OTP
		if (!StringUtil.isContains(Constants.DEFAULT_OTP_MOBILE_NUMBER, mobileNumber)) {
			putTransactionValidityCache(mobileNumber);
		}

		otpService.generateOTP(authDTO, CUST_OTP_KEY + mobileNumber, mobileNumber, true);
	}

	private void putTransactionValidityCache(String mobileNumber) {
		Element element = EhcacheManager.getTransactionValidityEhCache().get(mobileNumber);
		if (element == null) {
			element = new Element(mobileNumber, Numeric.ONE_INT);
			EhcacheManager.getTransactionValidityEhCache().put(element);
		}
		else if (element != null) {
			int otpCount = (int) element.getObjectValue();
			if (otpCount < Numeric.THREE_INT) {
				otpCount = otpCount + 1;

				element = new Element(mobileNumber, otpCount);
				EhcacheManager.getTransactionValidityEhCache().put(element);
			}
			else if (otpCount >= Numeric.THREE_INT) {
				throw new ServiceException(ErrorCode.OTP_TRANSACTION_COUNT_EXCEED);
			}
		}
	}

	@Override
	public AuthDTO getCustomerAuthendticationV2(AuthDTO auth, String mobileNumber, int otpNumber) {
		AuthDTO authDTO = new AuthDTO();
		try {
			boolean validateStatus = otpService.checkOTP(auth, CUST_OTP_KEY + mobileNumber, mobileNumber, otpNumber);
			if (!validateStatus) {
				throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
			}

			// Check user customer
			UserCustomerDTO userCustomer = userCustomerService.checkUserCustomer(auth, mobileNumber);

			// Add new customer
			if (userCustomer == null || userCustomer.getId() == 0) {
				UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setMobile(mobileNumber);
				userCustomerDTO.setActiveFlag(Numeric.ONE_INT);
				userCustomerService.saveUserCustomer(auth, userCustomerDTO);
				if (userCustomerDTO.getId() == 0) {
					throw new ServiceException(ErrorCode.USER_INVALID_EMAIL);
				}

				userCustomer = new UserCustomerDTO();
				userCustomer.setId(userCustomerDTO.getId());
			}

			authDTO.setNamespaceCode(auth.getNamespaceCode());
			authDTO.setNativeNamespaceCode(auth.getNativeNamespaceCode());
			authDTO.setDeviceMedium(auth.getDeviceMedium());
			PrivilegeDAO dao = new PrivilegeDAO();
			UserDTO userDTO = dao.getAuthendtication(authDTO, Text.NAMESPACE_GUEST_USERNAME + "@" + authDTO.getNamespaceCode());
			if (userDTO == null || userDTO.getToken() == null) {
				throw new ServiceException(ErrorCode.USER_INVALID_EMAIL);
			}
			authDTO.setUserCode(userDTO.getCode());
			TokenGenerator idGenerator = new TokenGenerator();
			authDTO.setAuthToken(idGenerator.getSessionTrackingId());
			authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_CUSTOMER);
			authDTO.setNativeNamespaceCode(authDTO.getNamespace().getCode());

			userCustomerService.generateUserCustomerAuth(authDTO, userCustomer);
			authDTO.setUserCustomer(userCustomer);

			// Put Cache
			AuthCache authCache = new AuthCache();
			authCache.putAuthDTO(authDTO);
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			throw new ServiceException(500);
		}
		return authDTO;
	}

	@Override
	public AuthDTO getCustomerAuthendticationV3(AuthDTO auth, UserCustomerDTO userCustomer) {
		AuthDTO authDTO = new AuthDTO();
		try {
			// Check user customer
			UserCustomerDTO userCustomerDTO = userCustomerService.checkUserCustomerAuthBySessionToken(auth, userCustomer);

			// Add new customer
			if (userCustomerDTO == null || userCustomerDTO.getId() == 0 || userCustomerDTO.getUserCustomerAuth().getId() == 0) {
				throw new ServiceException(ErrorCode.USER_INVALID_DEVICE_AUTH_TOKEN);
			}

			authDTO.setNamespaceCode(auth.getNamespaceCode());
			authDTO.setNativeNamespaceCode(auth.getNativeNamespaceCode());
			authDTO.setDeviceMedium(auth.getDeviceMedium());
			PrivilegeDAO dao = new PrivilegeDAO();
			UserDTO userDTO = dao.getAuthendtication(authDTO, Text.NAMESPACE_GUEST_USERNAME + "@" + authDTO.getNamespaceCode());
			if (userDTO == null || userDTO.getToken() == null) {
				throw new ServiceException(ErrorCode.USER_INVALID_EMAIL);
			}
			authDTO.setUserCode(userDTO.getCode());
			TokenGenerator idGenerator = new TokenGenerator();
			authDTO.setAuthToken(idGenerator.getSessionTrackingId());
			authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_CUSTOMER);
			authDTO.setNativeNamespaceCode(authDTO.getNamespace().getCode());
			authDTO.setUserCustomer(userCustomerDTO);

			// Put Cache
			AuthCache authCache = new AuthCache();
			authCache.putAuthDTO(authDTO);
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			throw new ServiceException(500);
		}
		return authDTO;
	}

	@Override
	public AuthDTO verifyAuthToken(IntegrationDTO integrationDTO, String authToken) {
		AuthDTO authDTO = new AuthDTO();
		try {
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			integrationService.getIntegrationV2(namespaceDTO, integrationDTO);

			namespaceDTO = getNamespaceDTObyId(namespaceDTO);
			if (namespaceDTO == null || namespaceDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}

			Map<String, String> auth = bitsService.verifyAuthToken(integrationDTO, authToken);

			authDTO.setNamespaceCode(namespaceDTO.getCode());
			authDTO.setNativeNamespaceCode(namespaceDTO.getCode());

			// Username password validation
			PrivilegeDAO dao = new PrivilegeDAO();
			UserDTO userDTO = dao.getAuthendtication(authDTO, auth.get("USERNAME"));
			if (userDTO == null || userDTO.getId() == 0 || userDTO.getIntegration().getId() == 0 || userDTO.getIntegration().getId() != integrationDTO.getId()) {
				throw new ServiceException(ErrorCode.USER_INVALID_AUTH_TOKEN, "username not found");
			}

			authDTO.setAuthToken(authToken);
			authDTO.setUserCode(userDTO.getCode());
			authDTO.setAuthenticationType(AuthenticationTypeEM.BITS_USERNAME_PASSWORD);
			authDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(auth.get("DEVICE_MEDIUM_CODE")));

			// User Menu
			List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, authDTO.getUser());
			authDTO.setPrivileges(privilegesDTOlist);

			dao.loginEntry(authDTO, Numeric.ZERO_INT);

			if (authDTO.getGroup() != null && authDTO.getGroup().getId() != 0) {
				authDTO.getUser().setGroup(groupService.getGroup(authDTO, authDTO.getGroup()));
			}
			if (authDTO.getUser().getOrganization() != null && authDTO.getUser().getOrganization().getId() != 0) {
				authDTO.getUser().setOrganization(getOrganizationDTObyId(authDTO, authDTO.getUser().getOrganization()));
			}

			AuthCache authCache = new AuthCache();
			authCache.putAuthDTO(authDTO);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(500);
		}
		return authDTO;
	}

	@Override
	public String generateBotOTP(AuthDTO authDTO) {
		JSONObject data = new JSONObject();
		data.put("authToken", authDTO.getAuthToken());
		data.put("url", ApplicationConfig.getServerZoneUrl() + "/busservices");
		data.put("userCode", authDTO.getNamespaceCode() + authDTO.getUserCode());
		data.put("deviceMedium", DeviceMediumEM.EZEEBOT_APP.getCode());
		data.put("name", authDTO.getUser().getName() + (StringUtil.isNotNull(authDTO.getUser().getLastname()) ? " " + authDTO.getUser().getLastname() : Text.EMPTY));
		data.put("namespaceCode", authDTO.getNamespaceCode());
		data.put("namespaceName", authDTO.getNamespace().getName());
		data.put("product", "Bits");
		data.put("logo", "https://web.cdn.ezeebus.com/product/bits/logo/bits-logo-png.png");
		String otp = BitsShortURL.getUrlshortenerOTP(data.toString(), BitsShortURL.TYPE.MXD);
		return otp;
	}

	public JSONObject getBotOTP(String otp) {
		JSONObject data = BitsShortURL.decryptShorternOTP(otp, BitsShortURL.TYPE.MXD, 1);

		if (data.getInt("status") == 0 || !data.has("data")) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}
		String content = data.getJSONObject("data").getString("content");
		JSONObject jsonContent = JSONObject.fromObject(content);
		return jsonContent;
	}

	@Override
	public NamespaceDTO getAliasNamespace(String namespaceCode) {
		NamespaceDTO nativeNamespace = namespaceService.getNamespace(namespaceCode);
		NamespaceDTO namespace = new NamespaceDTO();
		if (nativeNamespace.getLookupId() != 0) {
			NamespaceDTO lookupNamespace = new NamespaceDTO();
			lookupNamespace.setId(nativeNamespace.getLookupId());
			namespaceService.getNamespace(lookupNamespace);
			namespace.setId(lookupNamespace.getId());
			namespace.setCode(lookupNamespace.getCode());
			namespace.setActiveFlag(lookupNamespace.getActiveFlag());

			namespace.setName(nativeNamespace.getName());
			namespace.setLookupId(nativeNamespace.getLookupId());
			namespace.setAliasCode(nativeNamespace.getCode());
		}
		else if (nativeNamespace.getId() != 0) {
			namespace.setCode(nativeNamespace.getCode());
			namespace.setId(nativeNamespace.getId());
			namespace.setActiveFlag(nativeNamespace.getActiveFlag());
			namespace.setAliasCode(Text.EMPTY);
			namespace.setName(nativeNamespace.getName());
		}
		return namespace;
	}

	private void checkSectorPermission(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		authDTO.getAdditionalAttribute().put(Text.SECTOR, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
	}

}
