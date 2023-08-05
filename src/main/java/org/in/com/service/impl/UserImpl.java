package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserSessionAuditDTO;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.GroupService;
import org.in.com.service.IntegrationService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.SectorService;
import org.in.com.service.TransactionOTPService;
import org.in.com.service.UserDetailsService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenEncrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class UserImpl extends CacheCentral implements UserService {

	private static String OTP_KEY = "USR";

	@Autowired
	EmailService emailService;
	@Autowired
	AuthService authService;
	@Autowired
	GroupService groupService;
	@Autowired
	IntegrationService integrationService;
	@Autowired
	BitsService bitsService;
	@Autowired
	TransactionOTPService otpService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	SectorService sectorService;
	@Autowired
	UserDetailsService userDetailsService;

	public UserDTO getUser(AuthDTO authDTO, UserDTO user) {
		UserDTO userDTO = null;
		if (user.getId() != 0) {
			userDTO = getUserDTOById(authDTO, user);
		}
		else if (StringUtil.isNotNull(user.getCode())) {
			userDTO = getUserDTO(authDTO.getNamespaceCode(), user.getCode());
		}
		return userDTO;
	}

	public UserDTO getUser(String namespaceCode, String userCode) {
		return getUserDTO(namespaceCode, userCode);
	}

	public List<UserDTO> get(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO dao = new UserDAO();
		List<UserDTO> list = dao.getUser(authDTO, userDTO);
		for (UserDTO dto : list) {
			if (dto.getGroup() != null && dto.getGroup().getId() != 0) {
				dto.setGroup(groupService.getGroup(authDTO, dto.getGroup()));
			}
			if (dto.getOrganization() != null && dto.getOrganization().getId() != 0) {
				dto.setOrganization(getOrganizationDTObyId(authDTO, dto.getOrganization()));
				if (dto.getOrganization().getStation().getId() != 0) {
					dto.getOrganization().setStation(getStationDTObyId(dto.getOrganization().getStation()));
				}
			}
			if (userDTO.getIntegration() != null && userDTO.getIntegration().getId() != 0) {
				integrationService.get(authDTO, userDTO.getIntegration());
			}
		}
		return list;
	}

	public List<UserDTO> getAll(AuthDTO authDTO) {
		SectorDTO sector = sectorService.getActiveUserSectorOrganization(authDTO);

		UserDAO dao = new UserDAO();
		List<UserDTO> list = dao.getNamespaceUsers(authDTO);
		for (Iterator<UserDTO> iterator = list.iterator(); iterator.hasNext();) {
			UserDTO userDTO = iterator.next();
			// Apply Sector User filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isOrganizationExists(sector.getOrganization(), userDTO.getOrganization()) == null) {
				iterator.remove();
				continue;
			}
			if (userDTO.getGroup() != null && userDTO.getGroup().getId() != 0) {
				userDTO.setGroup(groupService.getGroup(authDTO, userDTO.getGroup()));
			}
		}
		return list;
	}

	public UserDTO Update(AuthDTO authDTO, UserDTO dto) {
		boolean notificationFlag = StringUtil.isNull(dto.getCode());
		UserDAO dao = new UserDAO();

		// Always username in lower case
		if (StringUtil.isNotNull(dto.getUsername())) {
			dto.setUsername(StringUtil.trimAllSpaces(dto.getUsername()).toLowerCase());
		}
		if (authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId() && StringUtil.isNull(dto.getCode())) {
			throw new ServiceException(ErrorCode.INVALID_USERNAME);
		}
		if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && StringUtil.isNull(dto.getCode()) && StringUtil.isNotNull(dto.getUsername()) && (dto.getUsername().length() <= 4 || StringUtil.isNumeric(dto.getUsername()) || StringUtil.removeUnknownSymbol(dto.getUsername()).length() != dto.getUsername().length())) {
			throw new ServiceException(ErrorCode.INVALID_USERNAME);
		}
		if (dto.getIntegration() != null && dto.getIntegration().getIntegrationtype() != null) {
			dto.setIntegration(integrationService.getIntegration(authDTO, dto.getIntegration().getIntegrationtype(), Text.NA));
		}
		// User fcm notification
		if ((StringUtil.isNull(dto.getCode()) && dto.getActiveFlag() == 1) || (StringUtil.isNotNull(dto.getCode()) && dto.getActiveFlag() == 2)) {
			NotificationSubscriptionTypeEM notificationType = NotificationSubscriptionTypeEM.NEW_USER_UPDATE;
			if (dto.getActiveFlag() == 2) {
				notificationType = NotificationSubscriptionTypeEM.USER_DELETE;
			}
			notificationPushService.pushUserNotification(authDTO, dto, notificationType);
		}

		dao.UserUID(authDTO, dto);

		// clear Cache
		removeUserDTO(authDTO, dto);

		// remove login all user sessions
		if (dto.getActiveFlag() != 1) {
			authService.removeUserAuthToken(authDTO, dto);
		}
		if (notificationFlag && authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
			emailService.sendRegisterEmail(authDTO, dto);
		}

		return dto;
	}

	public List<UserDTO> get(AuthDTO authDTO, GroupDTO groupDTO) {
		UserDAO dao = new UserDAO();
		return dao.getAllUserInGroups(authDTO, groupDTO);
	}

	public List<UserDTO> get(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		UserDAO dao = new UserDAO();
		return dao.getAllUserInOrg(authDTO, organizationDTO);
	}

	public void resetProfilePassword(AuthDTO authDTO, UserDTO userDTO) {
		if (StringUtil.isNotNull(userDTO.getNewPassword())) {
			userDTO.setToken(TokenEncrypt.encryptString(userDTO.getNewPassword()));
		}
		UserDAO dao = new UserDAO();
		dao.resetUserProfilePassword(authDTO, userDTO);

		// Reset pwd - push fcm notification
		notificationPushService.pushUserNotification(authDTO, userDTO, NotificationSubscriptionTypeEM.USER_RESET_PASSWORD);

		// clear Cache
		removeUserDTO(authDTO, userDTO);
	}

	public void changeProfilePassword(AuthDTO authDTO, UserDTO userDTO) {
		UserDTO dbUserDTO = new UserDTO();
		dbUserDTO.setCode(userDTO.getCode());

		UserDAO userDAO = new UserDAO();
		userDAO.getUserDTO(authDTO, dbUserDTO);

		if (dbUserDTO.getToken().equals(TokenEncrypt.encryptString(userDTO.getOldPassword()))) {
			userDTO.setToken(TokenEncrypt.encryptString(userDTO.getNewPassword()));
			userDAO.resetUserProfilePassword(authDTO, userDTO);
		}
		else {
			throw new ServiceException(202);

		}
		// clear Cache
		removeUserDTO(authDTO, userDTO);
	}

	public void updateAPITokenPassword(AuthDTO authDTO, UserDTO userDTO) {
		if (StringUtil.isNull(userDTO.getApiToken())) {
			userDTO.setApiToken(TokenEncrypt.encryptString(userDTO.getCode() + userDTO.getNewPassword()));
		}
		UserDAO dao = new UserDAO();
		dao.updateAPITokenPassword(authDTO, userDTO);
		// clear Cache
		removeUserDTO(authDTO, userDTO);
	}

	public void updateEmailVerify(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO dao = new UserDAO();
		dao.getUserEmailVerifyToken(authDTO, userDTO);
		if (userDTO.getId() != 0) {
			dao.updateEmailVerify(authDTO, userDTO);
		}
		else {
			throw new ServiceException(204);

		}
	}

	public BigDecimal getCurrentCreditBalace(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO dao = new UserDAO();
		return dao.getCurrentCreditBalace(authDTO, userDTO);
	}

	@Override
	public UserDTO getUserDTO(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.getUserDTO(authDTO, userDTO);
		return userDTO;
	}

	@Override
	public UserDTO findAndRegisterUser(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.getUserDTO(authDTO, userDTO);
		if (userDTO.getId() == 0) {
			// Create new User with Cust Role
			AuthDTO forUserAuthDTO = authService.getGuestAuthendtication(authDTO.getNamespaceCode(), authDTO.getDeviceMedium());
			userDTO.setEmail(userDTO.getUsername());
			if (userDTO.getName() == null) {
				userDTO.setName(userDTO.getUsername());
			}
			UserDTO guestUserDTO = forUserAuthDTO.getUser();

			userDTO.setGroup(guestUserDTO.getGroup());
			userDTO.setUserRole(UserRoleEM.CUST_ROLE);
			userDTO.setPaymentType(PaymentTypeEM.PAYMENT_GATEWAY_PAID);
			userDTO.setActiveFlag(1);
			userDTO.setId(guestUserDTO.getId());

			userDAO.UserUID(forUserAuthDTO, userDTO);
			if (userDTO.getId() != 0) {
				userDTO.setId(0);
				userDTO.setUsername(null);
			}
			else {
				throw new ServiceException(ErrorCode.USER_INVALID_EMAIL);
			}
			userDAO.getUserDTO(authDTO, userDTO);
		}
		return userDTO;
	}

	@Override
	public void checkUsername(AuthDTO authDTO, String username) {
		UserDAO userDAO = new UserDAO();
		if (!StringUtil.isValidUsername(username) || !userDAO.checkUsername(authDTO, username)) {
			throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
		}
	}

	@Override
	public void appStoreUpdate(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.appStoreUpdate(authDTO, userDTO);
		// clear appstore cache
		removeAppStoreDetailsCache(authDTO, userDTO);
	}

	@Override
	public List<AppStoreDetailsDTO> getAppStoreDetails(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		return userDAO.getAppStoreDetails(authDTO, userDTO);
	}

	@Override
	public void updateUserDetails(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.updateUserDetails(authDTO, userDTO);
	}

	@Override
	public void sessionAuditUpdate(AuthDTO authDTO, UserSessionAuditDTO sessionAuditDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.updateLoginSessionDetails(authDTO, sessionAuditDTO);
	}

	@Override
	public List<UserSessionAuditDTO> getUserRecentSession(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		return userDAO.getUserRecentSession(authDTO, userDTO);
	}

	@Override
	public List<UserDTO> getUsers(AuthDTO authDTO, UserTagEM userTag) {
		SectorDTO sector = sectorService.getActiveUserSectorOrganization(authDTO);

		UserDAO dao = new UserDAO();
		List<UserDTO> list = dao.getNamespaceUsers(authDTO);
		for (Iterator<UserDTO> iterator = list.iterator(); iterator.hasNext();) {
			UserDTO userDTO = iterator.next();
			if (userDTO.getGroup() != null && userDTO.getGroup().getId() != 0) {
				userDTO.setGroup(groupService.getGroup(authDTO, userDTO.getGroup()));
			}
			// Apply Sector User filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isOrganizationExists(sector.getOrganization(), userDTO.getOrganization()) == null) {
				iterator.remove();
				continue;
			}

			// Validate User Tag
			if (userTag != null && !checkUserTag(userTag, userDTO.getUserTags())) {
				iterator.remove();
				continue;
			}

			if (userDTO.getIntegration().getId() != 0) {
				integrationService.get(authDTO, userDTO.getIntegration());
			}
			userDTO.setUserDetails(userDetailsService.getUserDetailsV2(authDTO, userDTO));
		}
		return list;
	}

	public boolean checkUserTag(UserTagEM userTag, List<UserTagEM> userTagList) {
		boolean isExist = Text.FALSE;
		for (UserTagEM userTagEM : userTagList) {
			if (userTag.getId() == userTagEM.getId()) {
				isExist = Text.TRUE;
				break;
			}
		}
		return isExist;
	}

	public List<UserDTO> getUsers(AuthDTO authDTO, IntegrationDTO integrationDTO) {
		integrationDTO = integrationService.getIntegration(authDTO, integrationDTO.getIntegrationtype(), Text.NA);
		List<UserDTO> users = bitsService.getUsers(authDTO, integrationDTO);
		return users;
	}

	@Override
	public void getUserV2(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.getUserV2(authDTO, userDTO);
	}

	private void changeMobileNumber(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		if (userDTO.getMobileVerifiedFlag() != Numeric.ONE_INT) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, "Mobile Number verified");
		}

		userDAO.updateUserMobile(authDTO, userDTO);

		// clear Cache
		removeUserDTO(authDTO, userDTO);
	}

	@Override
	public void generateProfileOTP(AuthDTO authDTO, String mobileNumber) {
		otpService.generateOTP(authDTO, OTP_KEY + authDTO.getUser().getCode(), mobileNumber, true);
	}

	@Override
	public void validateProfileOTP(AuthDTO authDTO, String mobileNumber, int otpNumber) {
		boolean validateStatus = otpService.checkOTP(authDTO, OTP_KEY + authDTO.getUser().getCode(), mobileNumber, otpNumber);
		if (!validateStatus) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}

		UserDTO userDTO = authDTO.getUser();
		userDTO.setMobile(mobileNumber);
		userDTO.setContactVerifiedFlag(Numeric.ONE + Numeric.ZERO);
		// update Mobile number
		changeMobileNumber(authDTO, userDTO);
	}

	@Override
	public Map<String, String> generateProfileOTPV2(AuthDTO authDTO, String mobileNumber) {
		String deviceModel = Text.EMPTY;
		boolean isVerifiedMobile = false;
		boolean isSubscriptionEnabled = false;
		if (authDTO.getUser().getMobileVerifiedFlag() == Numeric.ONE_INT) {
			isVerifiedMobile = true;
		}
		isSubscriptionEnabled = authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.OTP_LOGIN);

		if (isVerifiedMobile || isSubscriptionEnabled) {
			otpService.generateOTP(authDTO, OTP_KEY + authDTO.getUser().getCode(), mobileNumber, true);
			if (isSubscriptionEnabled) {
				List<AppStoreDetailsDTO> userAppList = getAppstoreByUserId(authDTO, authDTO.getUser());
				if (!userAppList.isEmpty()) {
					List<String> deviceModelList = userAppList.stream().map(user -> user.getModel()).collect(Collectors.toList());
					deviceModel = StringUtils.join(deviceModelList, ',');
				}
			}
		}
		if (!isVerifiedMobile && StringUtil.isNull(deviceModel)) {
			throw new ServiceException(ErrorCode.NO_PROVISION_TO_SEND_NOTIFICATION);
		}
		Map<String, String> dataMap = Maps.newHashMap();
		dataMap.put("MOBILE", isVerifiedMobile ? StringUtil.getMobileNumberMasking(mobileNumber) : Text.EMPTY);
		dataMap.put("EZBOT", deviceModel);
		return dataMap;
	}

	@Override
	public void validateProfileOTPV2(AuthDTO authDTO, String mobileNumber, int otpNumber) {
		boolean validateStatus = otpService.checkOTP(authDTO, OTP_KEY + authDTO.getUser().getCode(), mobileNumber, otpNumber);
		if (!validateStatus) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}
	}

	@Override
	public void saveVehicleDriverUser(AuthDTO authDTO, UserDTO userDTO) {
		UserDAO userDAO = new UserDAO();
		userDAO.saveVehicleDriverUser(authDTO, userDTO);
	}

	@Override
	public List<UserDTO> getAllUserV2(AuthDTO authDTO, UserTagEM userTag) {
		// TODO cache
		UserDAO dao = new UserDAO();
		List<UserDTO> list = dao.getNamespaceUsers(authDTO);
		for (Iterator<UserDTO> iterator = list.iterator(); iterator.hasNext();) {
			UserDTO userDTO = iterator.next();
			if (userDTO.getGroup() != null && userDTO.getGroup().getId() != 0) {
				userDTO.setGroup(groupService.getGroup(authDTO, userDTO.getGroup()));
			}
			// Validate User Tag
			if (userTag != null && !checkUserTag(userTag, userDTO.getUserTags())) {
				iterator.remove();
				continue;
			}
		}
		return list;
	}
}
