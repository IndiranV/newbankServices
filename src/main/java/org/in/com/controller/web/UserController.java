package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.AppStoreDetailsIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NamespaceIO;
import org.in.com.controller.web.io.NamespaceProfileIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.PaymentTypeIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationAreaIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.controller.web.io.UserDetailsIO;
import org.in.com.controller.web.io.UserDeviceIO;
import org.in.com.controller.web.io.UserFeedbackIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.UserPaymentPreferencesIO;
import org.in.com.controller.web.io.UserRegistrationIO;
import org.in.com.controller.web.io.UserStationPointIO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserDetailsDTO;
import org.in.com.dto.UserDeviceDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.UserPaymentPreferencesDTO;
import org.in.com.dto.UserRegistrationDTO;
import org.in.com.dto.UserStationPointDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FrequencyModeEM;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.PreferenceTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.OrganizationService;
import org.in.com.service.StateService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.UserCustomerService;
import org.in.com.service.UserDeviceService;
import org.in.com.service.UserFeedbackService;
import org.in.com.service.UserPaymentPreferenceService;
import org.in.com.service.UserService;
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

import hirondelle.date4j.DateTime;

@Controller
@RequestMapping("/{authtoken}/user")
public class UserController extends BaseController {
	@Autowired
	UserService userService;
	@Autowired
	StationService stationService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	UserFeedbackService userFeedbackService;
	@Autowired
	UserPaymentPreferenceService preferenceService;
	@Autowired
	UserDeviceService deviceService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	UserCustomerService userCustomerService;
	@Autowired
	GroupService groupService;
	@Autowired
	StateService stateService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getAllUsers(@PathVariable("authtoken") String authtoken, String userTagCode, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserTagEM userTagDTO = UserTagEM.getUserTagEM(userTagCode);
			List<UserDTO> list = userService.getUsers(authDTO, userTagDTO);
			for (UserDTO userDTO : list) {
				if (activeFlag != -1 && activeFlag != userDTO.getActiveFlag()) {
					continue;
				}
				UserIO userio = new UserIO();
				userio.setUsername(userDTO.getUsername());
				userio.setEmail(userDTO.getEmail());
				userio.setCode(userDTO.getCode());
				userio.setName(userDTO.getName());
				userio.setLastname(userDTO.getLastname());
				userio.setMobile(userDTO.getMobile());
				userio.setActiveFlag(userDTO.getActiveFlag());

				PaymentTypeIO paymentType = new PaymentTypeIO();
				paymentType.setCode(userDTO.getPaymentType().getCode());
				paymentType.setName(userDTO.getPaymentType().getName());
				userio.setPaymentType(paymentType);

				GroupIO groupIO = new GroupIO();
				groupIO.setCode(userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
				groupIO.setName(userDTO.getGroup() != null ? userDTO.getGroup().getName() : null);
				groupIO.setDecription(userDTO.getGroup() != null ? userDTO.getGroup().getDecription() : null);
				groupIO.setLevel(userDTO.getGroup() != null ? userDTO.getGroup().getLevel() : null);
				userio.setGroup(groupIO);

				OrganizationIO organizationIO = new OrganizationIO();
				organizationIO.setCode(userDTO.getOrganization() != null ? userDTO.getOrganization().getCode() : null);
				organizationIO.setName(userDTO.getOrganization() != null ? userDTO.getOrganization().getName() : null);
				
				StationIO stationIO = new StationIO();
				if (userDTO.getOrganization() != null && userDTO.getOrganization().getStation() != null) {
					stationIO.setCode(userDTO.getOrganization().getStation().getCode());
					stationIO.setName(userDTO.getOrganization().getStation().getName());
				}
				organizationIO.setStation(stationIO);
				
				userio.setOrganization(organizationIO);

				if (userDTO.getIntegration() != null && userDTO.getIntegration().getIntegrationtype() != null) {
					BaseIO integrationType = new BaseIO();
					integrationType.setCode(userDTO.getIntegration().getIntegrationtype().getCode());
					integrationType.setName(userDTO.getIntegration().getIntegrationtype().getName());
					userio.setIntegrationType(integrationType);
				}

				List<BaseIO> userTags = new ArrayList<BaseIO>();
				for (UserTagEM userTagEM : userDTO.getUserTags()) {
					BaseIO userTag = new BaseIO();
					userTag.setCode(userTagEM.getCode());
					userTag.setName(userTagEM.getName());
					userTags.add(userTag);
				}
				userio.setUserTags(userTags);
				userio.setMobileVerifiedFlag(userDTO.getMobileVerifiedFlag());
				userio.setAdditionalAttribute(userDTO.getAdditionalAttribute());

				UserDetailsIO userDetailsIO = new UserDetailsIO();
				UserDetailsDTO userDetailsDTO = userDTO.getUserDetails();
				if (userDetailsDTO != null) {
					userDetailsIO.setCode(userDetailsDTO.getCode());
					userDetailsIO.setAddress1(userDetailsDTO.getAddress1());
					userDetailsIO.setAddress2(userDetailsDTO.getAddress2());
					userDetailsIO.setLandmark(userDetailsDTO.getLandmark());
					userDetailsIO.setPincode(userDetailsDTO.getPincode());
					userDetailsIO.setActiveFlag(userDetailsDTO.getActiveFlag());

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
				userio.setUserDetails(userDetailsIO);
				user.add(userio);
			}
			// Sorting
			Comparator<UserIO> comp = new BeanComparator("name");
			Collections.sort(user, comp);
		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/{userCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> getUser(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		UserIO user = new UserIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userCode);
		List<UserDTO> list = userService.get(authDTO, userDTO);
		for (UserDTO dto : list) {
			user.setUsername(dto.getUsername());
			user.setEmail(dto.getEmail());
			user.setCode(dto.getCode());
			user.setName(dto.getName());
			user.setLastname(dto.getLastname());
			user.setMobile(dto.getMobile());
			user.setActiveFlag(dto.getActiveFlag());

			PaymentTypeIO paymentType = new PaymentTypeIO();
			paymentType.setCode(dto.getPaymentType().getCode());
			paymentType.setName(dto.getPaymentType().getName());
			user.setPaymentType(paymentType);

			GroupIO groupIO = new GroupIO();
			groupIO.setCode(dto.getGroup() != null ? dto.getGroup().getCode() : null);
			groupIO.setName(dto.getGroup() != null ? dto.getGroup().getName() : null);
			groupIO.setLevel(dto.getGroup() != null ? dto.getGroup().getLevel() : null);
			groupIO.setDecription(dto.getGroup() != null ? dto.getGroup().getDecription() : null);
			user.setGroup(groupIO);

			OrganizationIO organizationIO = new OrganizationIO();
			if (dto.getOrganization() != null) {
				organizationIO.setCode(dto.getOrganization().getCode());
				organizationIO.setName(dto.getOrganization().getName());

				StationIO station = new StationIO();
				station.setName(dto.getOrganization().getStation().getName());
				station.setCode(dto.getOrganization().getStation().getCode());
				organizationIO.setStation(station);
			}
			user.setOrganization(organizationIO);

			if (dto.getIntegration() != null && dto.getIntegration().getIntegrationtype() != null) {
				BaseIO integrationType = new BaseIO();
				integrationType.setCode(dto.getIntegration().getIntegrationtype().getCode());
				integrationType.setName(dto.getIntegration().getIntegrationtype().getName());
				user.setIntegrationType(integrationType);
			}

			List<BaseIO> userTags = new ArrayList<BaseIO>();
			for (UserTagEM userTagEM : dto.getUserTags()) {
				BaseIO userTag = new BaseIO();
				userTag.setCode(userTagEM.getCode());
				userTag.setName(userTagEM.getName());
				userTags.add(userTag);
			}
			user.setUserTags(userTags);
			user.setAdditionalAttribute(dto.getAdditionalAttribute());
		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> getUpdateUID(@PathVariable("authtoken") String authtoken, @RequestBody UserIO userIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userIO.getCode());
			userDTO.setName(StringUtil.substring(userIO.getName(), 45));
			userDTO.setLastname(StringUtil.substring(userIO.getLastname(), 45));
			userDTO.setUsername(userIO.getUsername());
			if (StringUtil.isNull(userDTO.getUsername())) {
				throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
			}
			else if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && StringUtil.isNull(userDTO.getCode()) && StringUtil.isNotNull(userDTO.getUsername()) && !StringUtil.isValidUsername(userDTO.getUsername())) {
				throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
			}
			userDTO.setEmail(userIO.getEmail());
			userDTO.setMobile(userIO.getMobile());
			userDTO.setActiveFlag(userIO.getActiveFlag());

			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(userIO.getGroup() != null ? userIO.getGroup().getCode() : null);
			userDTO.setGroup(groupDTO);

			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(userIO.getOrganization() != null ? userIO.getOrganization().getCode() : null);
			userDTO.setOrganization(organizationDTO);

			if (userIO.getPaymentType() != null && StringUtil.isNotNull(userIO.getPaymentType().getCode())) {
				userDTO.setPaymentType(PaymentTypeEM.getPaymentTypeEM(userIO.getPaymentType().getCode()));
			}
			else {
				userDTO.setPaymentType(PaymentTypeEM.PAYMENT_GATEWAY_PAID);
			}

			List<UserTagEM> userTags = new ArrayList<UserTagEM>();
			if (userIO.getUserTags() != null) {
				for (BaseIO BaseIO : userIO.getUserTags()) {
					UserTagEM userTagEM = UserTagEM.getUserTagEM(BaseIO.getCode());
					if (userTagEM == null) {
						continue;
					}
					userTags.add(userTagEM);
				}
			}
			userDTO.setUserTags(userTags);

			IntegrationDTO integrationDTO = new IntegrationDTO();
			if (userIO.getIntegrationType() != null && StringUtil.isNotNull(userIO.getIntegrationType().getCode())) {
				integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(userIO.getIntegrationType().getCode()));
			}
			userDTO.setIntegration(integrationDTO);
			userDTO.setAdditionalAttribute(userIO.getAdditionalAttribute() != null ? userIO.getAdditionalAttribute() : new HashMap<String, String>());
			userService.Update(authDTO, userDTO);
			userIO.setCode(userDTO.getCode());
		}
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/group/{groupcode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getUserInGroup(@PathVariable("authtoken") String authtoken, @PathVariable("groupcode") String groupcode) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(groupcode);
			List<UserDTO> list = userService.get(authDTO, groupDTO);
			for (UserDTO userDTO : list) {
				UserIO userio = new UserIO();
				userio.setUsername(userDTO.getUsername());
				userio.setCode(userDTO.getCode());
				userio.setEmail(userDTO.getEmail());
				userio.setName(userDTO.getName());
				userio.setLastname(userDTO.getLastname());
				userio.setActiveFlag(userDTO.getActiveFlag());

				PaymentTypeIO paymentType = new PaymentTypeIO();
				paymentType.setCode(userDTO.getPaymentType().getCode());
				paymentType.setName(userDTO.getPaymentType().getName());
				userio.setPaymentType(paymentType);

				List<BaseIO> userTags = new ArrayList<BaseIO>();
				for (UserTagEM userTagEM : userDTO.getUserTags()) {
					BaseIO userTag = new BaseIO();
					userTag.setCode(userTagEM.getCode());
					userTag.setName(userTagEM.getName());
					userTags.add(userTag);
				}
				userio.setUserTags(userTags);

				user.add(userio);
			}
		}

		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/{usercode}/resetpassword", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> resetPassword(@PathVariable("authtoken") String authtoken, @PathVariable("usercode") String usercode, String newpassword) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserIO userIO = new UserIO();
		UserDTO userDTO = new UserDTO();
		userDTO.setCode(usercode);
		userDTO.setNewPassword(newpassword);
		userService.resetProfilePassword(authDTO, userDTO);
		userIO.setCode(userDTO.getCode());
		userIO.setActiveFlag(userDTO.getActiveFlag());
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/changepassword", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> ChangePassword(@PathVariable("authtoken") String authtoken, String oldpassword, String newpassword) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO userDTO = authDTO.getUser();
		UserIO userIO = new UserIO();
		userDTO.setOldPassword(oldpassword);
		userDTO.setNewPassword(newpassword);
		userService.changeProfilePassword(authDTO, userDTO);
		userIO.setCode(userDTO.getCode());
		userIO.setActiveFlag(userDTO.getActiveFlag());
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/profile/generate/otp", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> generateProfileOTP(@PathVariable("authtoken") String authtoken, @RequestParam(value = "mobileNumber") String mobileNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(mobileNumber)) {
			mobileNumber = authDTO.getUser().getMobile();
		}
		if (!StringUtil.isValidMobileNumber(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		userService.generateProfileOTP(authDTO, mobileNumber);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/profile/validate/otp/{otpNumber}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> validateProfileOTP(@PathVariable("authtoken") String authtoken, @PathVariable("otpNumber") int otpNumber, @RequestParam(value = "mobileNumber") String mobileNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(mobileNumber)) {
			mobileNumber = authDTO.getUser().getMobile();
		}
		if (!StringUtil.isValidMobileNumber(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}

		userService.validateProfileOTP(authDTO, mobileNumber, otpNumber);

		return ResponseIO.success();
	}

	@RequestMapping(value = "/apitokenUpdate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> updateAPITokenPassword(@PathVariable("authtoken") String authtoken, String tokenPassword) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO userDTO = authDTO.getUser();
		UserIO userIO = new UserIO();
		userDTO.setNewPassword(tokenPassword);
		userService.updateAPITokenPassword(authDTO, userDTO);
		userIO.setCode(userDTO.getCode());
		userIO.setActiveFlag(userDTO.getActiveFlag());
		userIO.setApiToken(userDTO.getApiToken());

		// update to session
		// authDTO.getUser().setApiToken(userDTO.getApiToken());
		// Element element = new Element(authDTO.getAuthToken(), authDTO);
		// EhcacheManager.getAuthTokenEhCache().put(element);

		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/organization/{organizationcode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getuserInOrganization(@PathVariable("authtoken") String authtoken, @PathVariable("organizationcode") String organizationcode) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(organizationcode);
			List<UserDTO> list = userService.get(authDTO, organizationDTO);
			for (UserDTO userDTO : list) {
				UserIO userio = new UserIO();
				userio.setCode(userDTO.getCode());
				userio.setUsername(userDTO.getUsername());
				userio.setEmail(userDTO.getEmail());
				userio.setName(userDTO.getName());
				userio.setLastname(userDTO.getLastname());
				userio.setActiveFlag(userDTO.getActiveFlag());

				List<BaseIO> userTags = new ArrayList<BaseIO>();
				for (UserTagEM userTagEM : userDTO.getUserTags()) {
					BaseIO userTag = new BaseIO();
					userTag.setCode(userTagEM.getCode());
					userTag.setName(userTagEM.getName());
					userTags.add(userTag);
				}
				userio.setUserTags(userTags);

				user.add(userio);
			}
		}

		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/preference/{userCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserPaymentPreferencesIO> getPreference(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		UserPaymentPreferencesIO preferencesIO = new UserPaymentPreferencesIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			UserPaymentPreferencesDTO preferencesDTO = new UserPaymentPreferencesDTO();
			preferencesDTO.setUser(userDTO);
			preferenceService.get(authDTO, preferencesDTO);
			if (preferencesDTO != null && preferencesDTO.getId() != 0) {
				preferencesIO.setPreferencesType(preferencesDTO.getPreferenceType().getCode());
				preferencesIO.setFrequencyMode(preferencesDTO.getFrequencyMode().getCode());
				preferencesIO.setDayOfMonth(preferencesDTO.getDayOfMonth());
				preferencesIO.setDayOfTime(preferencesDTO.getDayOfTime());
				preferencesIO.setDayOfWeek(preferencesDTO.getDayOfWeek());
				preferencesIO.setEmailAddress(preferencesDTO.getEmailAddress());
			}
		}
		return ResponseIO.success(preferencesIO);
	}

	@RequestMapping(value = "/preference/{userCode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserPaymentPreferencesIO> getPreference(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode, @RequestBody UserPaymentPreferencesIO paymentPreferences) throws Exception {
		UserPaymentPreferencesIO preferencesIO = new UserPaymentPreferencesIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			UserPaymentPreferencesDTO preferencesDTO = new UserPaymentPreferencesDTO();
			preferencesDTO.setUser(userDTO);
			preferencesDTO.setFrequencyMode(FrequencyModeEM.getFrequencyModeEM(paymentPreferences.getFrequencyMode()));
			preferencesDTO.setPreferenceType(PreferenceTypeEM.getPreferenceTypeEM(paymentPreferences.getPreferencesType()));
			preferencesDTO.setActiveFlag(paymentPreferences.getActiveFlag());
			preferencesDTO.setDayOfMonth(paymentPreferences.getDayOfMonth());
			preferencesDTO.setDayOfTime(paymentPreferences.getDayOfTime());
			preferencesDTO.setDayOfWeek(paymentPreferences.getDayOfWeek());
			preferencesDTO.setEmailAddress(paymentPreferences.getEmailAddress());
			preferenceService.Update(authDTO, preferencesDTO);

		}
		return ResponseIO.success(preferencesIO);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> getLogout(@PathVariable("authtoken") String authtoken) throws Exception {
		authService.logout(authtoken);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/logout/{sessiontoken}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> getLogoutSessionToken(@PathVariable("authtoken") String authtoken, @PathVariable("sessiontoken") String sessiontoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		authService.getLogoutSessionToken(authDTO, sessiontoken);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/emailverifyUpdate", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> emailverify(@PathVariable("authtoken") String authtoken, String emailverifyToken) throws Exception {
		UserIO userIO = new UserIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setToken(emailverifyToken);
			userService.updateEmailVerify(authDTO, userDTO);
			userIO.setCode(userDTO.getCode());
			userIO.setName(userDTO.getName());
			userIO.setEmail(userDTO.getEmail());
		}
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/profile/balance", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Double> getProfileBalance(@PathVariable("authtoken") String authtoken) throws Exception {
		BigDecimal balance = BigDecimal.ZERO;
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			balance = userService.getCurrentCreditBalace(authDTO, authDTO.getUser());
		}
		return ResponseIO.success(balance.doubleValue());

	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserIO> getProfile(@PathVariable("authtoken") String authtoken) throws Exception {
		UserIO userIO = new UserIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO userDTO = authDTO.getUser();
		userIO.setCode(userDTO.getCode());
		userIO.setName(userDTO.getName());
		userIO.setEmail(userDTO.getEmail());
		userIO.setLastname(userDTO.getLastname());
		userIO.setMobile(userDTO.getMobile());
		userIO.setUsername(userDTO.getUsername());
		userIO.setApiToken(StringUtil.isNotNull(userDTO.getApiToken()) ? userDTO.getApiToken() : "NA");
		userIO.setMobileVerifiedFlag(userDTO.getMobileVerifiedFlag());
		userIO.setPasswordUpdateAt(userDTO.getPasswordUpdatedAt());

		PaymentTypeIO paymentType = new PaymentTypeIO();
		paymentType.setCode(userDTO.getPaymentType().getCode());
		paymentType.setName(userDTO.getPaymentType().getName());
		userIO.setPaymentType(paymentType);

		// Migrate User Customer
		if (authDTO.getUserCustomer() != null && authDTO.getUserCustomer().getId() != 0) {
			UserCustomerDTO userCustomerDTO = userCustomerService.getUserCustomer(authDTO, authDTO.getUserCustomer());
			userIO.setCode(userCustomerDTO.getCode());
			userIO.setName(userCustomerDTO.getName());
			userIO.setEmail(userCustomerDTO.getEmail());
			userIO.setLastname(userCustomerDTO.getLastname());
			userIO.setMobile(userCustomerDTO.getMobile());
		}

		RoleIO roleIO = new RoleIO();
		if (userDTO.getGroup() != null) {
			GroupDTO groupDTO = groupService.getGroup(authDTO, userDTO.getGroup());
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(groupDTO.getCode());
			groupIO.setName(groupDTO.getName());
			groupIO.setLevel(groupDTO.getLevel());
			userIO.setGroup(groupIO);
		}
		roleIO.setCode(userDTO.getUserRole().getCode());
		roleIO.setName(userDTO.getUserRole().getName());
		userIO.setRole(roleIO);

		if (userDTO.getOrganization() != null && userDTO.getOrganization().getId() != 0) {
			OrganizationIO organization = new OrganizationIO();
			OrganizationDTO organizationDTO = organizationService.getOrganization(authDTO, userDTO.getOrganization());
			organization.setCode(organizationDTO.getCode());
			organization.setName(organizationDTO.getName());
			StationIO station = new StationIO();
			StationDTO stationDTO = stationService.getStation(organizationDTO.getStation());
			station.setCode(stationDTO.getCode());
			station.setName(stationDTO.getName());
			organization.setStation(station);
			userIO.setOrganization(organization);
		}

		userIO.setNativeNamespaceCode(authDTO.getNativeNamespaceCode());
		BigDecimal currnetBalance = userService.getCurrentCreditBalace(authDTO, userDTO);
		userIO.setCurrnetBalance(currnetBalance.doubleValue());
		NamespaceIO namespaceIO = new NamespaceIO();
		namespaceIO.setName(authDTO.getNamespace().getName());
		namespaceIO.setCode(authDTO.getNamespace().getCode());
		// profile Details
		NamespaceProfileIO profileIO = new NamespaceProfileIO();
		NamespaceProfileDTO namespaceProfileDTO = authDTO.getNamespace().getProfile();
		profileIO.setBoardingReportingMinitues(namespaceProfileDTO.getBoardingReportingMinitues());
		profileIO.setCancellationCommissionRevokeFlag(namespaceProfileDTO.isCancellationCommissionRevokeFlag());
		profileIO.setCancellationChargeTaxFlag(namespaceProfileDTO.isCancellationChargeTaxFlag());
		profileIO.setTravelStatusOpenMinutes(namespaceProfileDTO.getTravelStatusOpenMinutes());
		profileIO.setDateFormate(namespaceProfileDTO.getDateFormat());
		profileIO.setDomainURL(namespaceProfileDTO.getDomainURL());
		profileIO.setEmailNotificationFlag(namespaceProfileDTO.isEmailNotificationFlag());
		profileIO.setMaxSeatPerTransaction(namespaceProfileDTO.getMaxSeatPerTransaction());
		profileIO.setPhoneBookingTicketNotificationMinitues(namespaceProfileDTO.getPhoneBookingTicketNotificationMinitues());
		profileIO.setTimeFormat(namespaceProfileDTO.getTimeFormat());
		profileIO.setPnrStartCode(namespaceProfileDTO.getPnrStartCode());
		profileIO.setSeatBlockTime(namespaceProfileDTO.getSeatBlockTime());
		profileIO.setSendarMailName(namespaceProfileDTO.getSendarMailName());
		profileIO.setSendarSMSName(namespaceProfileDTO.getSendarSMSName());
		profileIO.setSmsNotificationFlagCode(namespaceProfileDTO.getSmsNotificationFlagCode());
		profileIO.setPaymentReceiptAcknowledgeProcess(namespaceProfileDTO.getPaymentReceiptAcknowledgeProcess());
		profileIO.setOtaPartnerCode(namespaceProfileDTO.getOtaPartnerCode());
		profileIO.setOtpVerifyGroupEnabled(namespaceProfileDTO.isOtpVerifyGroupEnabled(userDTO.getGroup()));
		profileIO.setExpirePasswordGroupEnabled(namespaceProfileDTO.isExpirePasswordGroupEnabled(userDTO.getGroup()));
		profileIO.setWhatsappNumber(namespaceProfileDTO.getWhatsappNumber());
		profileIO.setWhatsappUrl(namespaceProfileDTO.getWhatsappUrl());
		profileIO.setWhatsappDatetime(namespaceProfileDTO.getWhatsappDatetime());
		profileIO.setAliasNamespaceFlag(namespaceProfileDTO.isAliasNamespaceFlag());
		profileIO.setTicketRescheduleMaxCount(namespaceProfileDTO.getTicketRescheduleMaxCount());
		profileIO.setSearchPastDayCount(namespaceProfileDTO.getSearchPastDayCount());
		profileIO.setSupportNumber(namespaceProfileDTO.getSupportNumber());
		profileIO.setAllowDirectLogin(namespaceProfileDTO.getAllowDirectLogin());

		StateIO state = new StateIO();
		if (namespaceProfileDTO.getState().getId() != 0) {
			StateDTO stateDTO = stateService.getState(namespaceProfileDTO.getState());
			state.setCode(stateDTO.getCode());
			state.setName(stateDTO.getName());
		}
		profileIO.setState(state);
		
		namespaceIO.setNamespaceProfile(profileIO);
		userIO.setNamespace(namespaceIO);
		return ResponseIO.success(userIO);
	}

	@RequestMapping(value = "/feedback", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserFeedbackIO>> getAllUserFeedback(@PathVariable("authtoken") String authtoken, String fromDate, String toDate) throws Exception {
		List<UserFeedbackIO> userFeedbacklist = new ArrayList<UserFeedbackIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			DateTime fromStamp = new DateTime(fromDate);
			DateTime toStamp = new DateTime(toDate);
			List<UserFeedbackDTO> list = userFeedbackService.getAll(authDTO, fromStamp, toStamp);
			for (UserFeedbackDTO userfeedbackdto : list) {
				UserFeedbackIO userfeedbackIO = new UserFeedbackIO();
				userfeedbackIO.setCode(userfeedbackdto.getCode());
				userfeedbackIO.setTicketCode(userfeedbackdto.getTicketCode());
				userfeedbackIO.setName(userfeedbackdto.getName());
				userfeedbackIO.setEmail(userfeedbackdto.getEmail());
				userfeedbackIO.setMobile(userfeedbackdto.getMobile());
				userfeedbackIO.setComments(userfeedbackdto.getComment());
				userfeedbackIO.setReplyContent(userfeedbackdto.getReply());
				userfeedbackIO.setFeedbackDate(userfeedbackdto.getFeedbackDate());
				userfeedbackIO.setActiveFlag(userfeedbackdto.getActiveFlag());
				userFeedbacklist.add(userfeedbackIO);
			}
		}
		return ResponseIO.success(userFeedbacklist);
	}

	@RequestMapping(value = "/feedback/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserFeedbackIO> updateUserfeedback(@PathVariable("authtoken") String authtoken, @RequestBody UserFeedbackIO userFeedback) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserFeedbackIO userfeedbackIO = new UserFeedbackIO();
		if (authDTO != null) {
			UserFeedbackDTO userfeedbackDTO = new UserFeedbackDTO();
			userfeedbackDTO.setTicketCode(userFeedback.getTicketCode());
			userfeedbackDTO.setCode(userFeedback.getCode());
			userfeedbackDTO.setName(userFeedback.getName());
			userfeedbackDTO.setEmail(userFeedback.getEmail());
			userfeedbackDTO.setMobile(userFeedback.getMobile());
			userfeedbackDTO.setComments(userFeedback.getComments());
			userfeedbackDTO.setActiveFlag(userFeedback.getActiveFlag());
			userFeedbackService.Update(authDTO, userfeedbackDTO);
			userfeedbackIO.setCode(userfeedbackDTO.getCode());
			userfeedbackIO.setActiveFlag(userfeedbackDTO.getActiveFlag());
		}
		return ResponseIO.success(userfeedbackIO);

	}

	@RequestMapping(value = "/feedback/{refferencecode}/reply", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> replyToUserFeedback(@PathVariable("authtoken") String authtoken, @PathVariable("refferencecode") String refferencecode, @RequestParam(value = "notificationmode", required = true, defaultValue = "SMS") String notificationmode, String participantaddress, @RequestBody String content) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO response = new BaseIO();
		if (authDTO != null) {
			UserFeedbackDTO userfeedbackDTO = new UserFeedbackDTO();
			userfeedbackDTO.setCode(refferencecode);
			if (NotificationMediumEM.SMS.getCode().equals(notificationmode)) {
				userfeedbackDTO.setMobile(participantaddress);
			}
			else {
				userfeedbackDTO.setEmail(participantaddress);
			}
			userfeedbackDTO.setReplyContent(content);
			String responseLog = userFeedbackService.sendReplyToUserFeedback(authDTO, userfeedbackDTO);
			response.setName(responseLog);
		}
		return ResponseIO.success(response);

	}

	@RequestMapping(value = "/registration", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserRegistrationIO>> getAllUserRegistration(@PathVariable("authtoken") String authtoken, String fromDate, String toDate) throws Exception {
		List<UserRegistrationIO> userFeedbacklist = new ArrayList<UserRegistrationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			DateTime fromStamp = new DateTime(fromDate);
			DateTime toStamp = new DateTime(toDate);
			List<UserRegistrationDTO> list = userFeedbackService.getUserRegistrationRequest(authDTO, fromStamp, toStamp);
			for (UserRegistrationDTO registrationDTO : list) {
				UserRegistrationIO registration = new UserRegistrationIO();
				registration.setCode(registrationDTO.getCode());
				registration.setName(registrationDTO.getName());
				registration.setEmail(registrationDTO.getEmail());
				registration.setMobile(registrationDTO.getMobile());
				registration.setState(registrationDTO.getState());
				registration.setOrganization(registrationDTO.getOrganization());
				registration.setAddress(registrationDTO.getAddress());
				registration.setComments(registrationDTO.getComments());
				registration.setRequestDate(registrationDTO.getRequestDate());
				registration.setActiveFlag(registrationDTO.getActiveFlag());
				userFeedbacklist.add(registration);
			}
		}
		return ResponseIO.success(userFeedbacklist);
	}

	@RequestMapping(value = "/registration/request", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserRegistrationIO> addUserRegistration(@PathVariable("authtoken") String authtoken, @RequestBody UserRegistrationIO registration) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserRegistrationIO userRegistration = new UserRegistrationIO();
		if (authDTO != null) {
			UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
			registrationDTO.setCode(registration.getCode());
			registrationDTO.setName(registration.getName());
			registrationDTO.setEmail(registration.getEmail());
			registrationDTO.setMobile(registration.getMobile());
			registrationDTO.setCity(registration.getCity());
			registrationDTO.setComments(registration.getComments());
			registrationDTO.setAddress(registration.getAddress());
			registrationDTO.setState(registration.getState());
			registrationDTO.setOrganization(registration.getOrganization());
			registrationDTO.setActiveFlag(registration.getActiveFlag());
			userFeedbackService.addUserRegistrationRequest(authDTO, registrationDTO);
			userRegistration.setCode(registrationDTO.getCode());
			userRegistration.setActiveFlag(registrationDTO.getActiveFlag());
		}
		return ResponseIO.success(userRegistration);
	}

	@RequestMapping(value = "/device/register/add", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> addDeviceRegister(@PathVariable("authtoken") String authtoken, @RequestBody UserDeviceIO userDevice) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		if (StringUtil.isNotNull(userDevice.getDeviceCode()) && StringUtil.isNotNull(userDevice.getDeviceMedium().getCode()) && StringUtil.isNotNull(userDevice.getUniqueCode())) {
			UserDeviceDTO userDeviceDTO = new UserDeviceDTO(userDevice.getDeviceCode(), userDevice.getUniqueCode(), userDevice.getVersion(), DeviceMediumEM.getDeviceMediumEM(userDevice.getDeviceMedium().getCode()));
			deviceService.registerUserDevice(authDTO, userDeviceDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/device/{deviceMediumCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserDeviceIO>> getDeviceRegister(@PathVariable("authtoken") String authtoken, @PathVariable("deviceMediumCode") String deviceMediumCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<UserDeviceIO> list = new ArrayList<UserDeviceIO>();
		List<UserDeviceDTO> deviceList = deviceService.getUserDevice(authDTO, DeviceMediumEM.getDeviceMediumEM(deviceMediumCode));
		for (UserDeviceDTO deviceDTO : deviceList) {
			UserDeviceIO deviceIO = new UserDeviceIO();
			deviceIO.setDeviceCode(deviceDTO.getDeviceCode());
			deviceIO.setUniqueCode(deviceDTO.getUniqueCode());
			deviceIO.setVersion(deviceDTO.getVersion());
			BaseIO deviceMedium = new BaseIO();
			deviceMedium.setCode(deviceDTO.getDeviceMedium().getCode());
			deviceMedium.setName(deviceDTO.getDeviceMedium().getName());
			deviceIO.setDeviceMedium(deviceMedium);
			list.add(deviceIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{userCode}/specific/stationpoint/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateUserSpecificStationPoint(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode, @RequestBody UserStationPointIO userStationPointIO) throws Exception {
		BaseIO userStnpIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserStationPointDTO userStationPointDTO = new UserStationPointDTO();
			userStationPointDTO.setCode(userStationPointIO.getCode());
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			userStationPointDTO.setUser(userDTO);
			
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(userStationPointIO.getStation().getCode());
			
			List<StationPointDTO> stationPointList = new ArrayList<>();
			for (StationPointIO stationPointIO : userStationPointIO.getStation().getStationPoint()) {
				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setCode(stationPointIO.getCode());
				stationPointList.add(stationPointDTO);
			}
			stationDTO.setStationPoints(stationPointList);
			userStationPointDTO.setStation(stationDTO);

			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			for (GroupIO groupIO : userStationPointIO.getGroupList()) {
				GroupDTO group = new GroupDTO();
				group.setCode(groupIO.getCode());
				groupList.add(group);
			}

			userStationPointDTO.setGroupList(groupList);
			userStationPointDTO.setBoardingCommission(userStationPointIO.getBoardingCommission());
			userStationPointDTO.setActiveFlag(userStationPointIO.getActiveFlag());
			stationPointService.updateUserSpecificStationPoint(authDTO, userStationPointDTO);
			userStnpIO.setCode(userStationPointDTO.getCode());
			userStnpIO.setActiveFlag(userStationPointDTO.getActiveFlag());
		}
		return ResponseIO.success(userStnpIO);
	}

	@RequestMapping(value = "/{userCode}/specific/stationpoint", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserStationPointIO>> getUserSpecificStationPoint(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<UserStationPointIO> userStationPointList = new ArrayList<UserStationPointIO>();
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			List<UserStationPointDTO> list = stationPointService.getUserSpecificStationPoint(authDTO, userDTO, new StationDTO());
			for (UserStationPointDTO userStationPoint : list) {
				UserStationPointIO userStationPointIO = new UserStationPointIO();
				userStationPointIO.setCode(userStationPoint.getCode());
				StationIO station = new StationIO();
				station.setName(userStationPoint.getStation().getName());
				station.setCode(userStationPoint.getStation().getCode());
				
				List<StationPointIO> stationPointList = new ArrayList<>();
				for (StationPointDTO stationPointDTO : userStationPoint.getStation().getStationPoints()) {
					StationPointIO stationPointIO = new StationPointIO();
					stationPointIO.setCode(stationPointDTO.getCode());
					stationPointIO.setName(stationPointDTO.getName());
					stationPointIO.setAddress(stationPointDTO.getAddress());
					stationPointIO.setLandmark(stationPointDTO.getLandmark());
					stationPointList.add(stationPointIO);
				}
				station.setStationPoint(stationPointList);
				
				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : userStationPoint.getGroupList()) {
					GroupIO group = new GroupIO();
					group.setCode(groupDTO.getCode());
					group.setName(groupDTO.getName());
					group.setActiveFlag(groupDTO.getActiveFlag());
					groupList.add(group);
				}
				userStationPointIO.setStation(station);
				userStationPointIO.setGroupList(groupList);
				userStationPointIO.setBoardingCommission(userStationPoint.getBoardingCommission());
				userStationPointIO.setActiveFlag(userStationPoint.getActiveFlag());
				userStationPointList.add(userStationPointIO);
			}
		}
		return ResponseIO.success(userStationPointList);
	}

	@RequestMapping(value = "/{username}/check", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> getCheckUsername(@PathVariable("authtoken") String authtoken, @PathVariable("username") String username) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			userService.checkUsername(authDTO, username);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/app/store/details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AppStoreDetailsIO> appStoreUpdate(@PathVariable("authtoken") String authtoken, @RequestBody AppStoreDetailsIO storeDetails) throws Exception {
		AppStoreDetailsIO userAppDetails = new AppStoreDetailsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (storeDetails == null || StringUtil.isNull(storeDetails.getUdid()) || StringUtil.isNull(storeDetails.getGcmToken())) {
				throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
			}
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(authDTO.getUserCode());

			AppStoreDetailsDTO appStoreDetails = new AppStoreDetailsDTO();
			appStoreDetails.setUdid(storeDetails.getUdid());
			appStoreDetails.setGcmToken(storeDetails.getGcmToken());
			appStoreDetails.setModel(storeDetails.getModel());
			appStoreDetails.setOs(storeDetails.getOs());
			appStoreDetails.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(storeDetails.getDeviceMedium().getCode()));
			appStoreDetails.setActiveFlag(storeDetails.getActiveFlag());
			userDTO.setAppStoreDetails(appStoreDetails);

			userService.appStoreUpdate(authDTO, userDTO);
			userAppDetails.setCode(userDTO.getAppStoreDetails().getCode());
			userAppDetails.setActiveFlag(userDTO.getAppStoreDetails().getActiveFlag());
		}
		return ResponseIO.success(userAppDetails);
	}

	@RequestMapping(value = "/integration/type/{code}", method = { RequestMethod.GET }, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserIO>> getUsers(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		List<UserIO> users = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		IntegrationDTO integrationDTO = new IntegrationDTO();
		integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(code));

		List<UserDTO> list = userService.getUsers(authDTO, integrationDTO);
		for (UserDTO userDTO : list) {
			UserIO user = new UserIO();
			user.setUsername(userDTO.getUsername());
			user.setEmail(userDTO.getEmail());
			user.setCode(userDTO.getCode());
			user.setName(userDTO.getName());
			user.setLastname(userDTO.getLastname());
			user.setMobile(userDTO.getMobile());
			user.setActiveFlag(userDTO.getActiveFlag());

			GroupIO groupIO = new GroupIO();
			groupIO.setCode(userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
			groupIO.setName(userDTO.getGroup() != null ? userDTO.getGroup().getName() : null);
			user.setGroup(groupIO);

			users.add(user);
		}
		return ResponseIO.success(users);
	}

}
