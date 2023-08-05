package org.in.com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserSessionAuditDTO;
import org.in.com.dto.enumeration.UserTagEM;

public interface UserService extends BaseService<UserDTO> {
	public List<UserDTO> get(AuthDTO authDTO, GroupDTO groupDTO);

	public UserDTO getUser(AuthDTO authDTO, UserDTO user);

	public UserDTO getUser(String namespaceCode, String userCode);

	public List<UserDTO> getUsers(AuthDTO authDTO, UserTagEM userTag);

	public List<UserDTO> get(AuthDTO authDTO, OrganizationDTO organizationDTO);

	public void resetProfilePassword(AuthDTO authDTO, UserDTO userDTO);

	public void changeProfilePassword(AuthDTO authDTO, UserDTO userDTO);

	public void updateAPITokenPassword(AuthDTO authDTO, UserDTO userDTO);

	public void updateEmailVerify(AuthDTO authDTO, UserDTO userDTO);

	public BigDecimal getCurrentCreditBalace(AuthDTO authDTO, UserDTO userDTO);

	public UserDTO getUserDTO(AuthDTO authDTO, UserDTO userDTO);

	public UserDTO findAndRegisterUser(AuthDTO authDTO, UserDTO userDTO);

	public void checkUsername(AuthDTO authDTO, String username);

	public void appStoreUpdate(AuthDTO authDTO, UserDTO userDTO);

	public void updateUserDetails(AuthDTO authDTO, UserDTO userDTO);

	public void sessionAuditUpdate(AuthDTO authDTO, UserSessionAuditDTO sessionAuditDTO);

	public List<UserSessionAuditDTO> getUserRecentSession(AuthDTO authDTO, UserDTO userDTO);

	public List<AppStoreDetailsDTO> getAppStoreDetails(AuthDTO authDTO, UserDTO userDTO);

	public List<UserDTO> getUsers(AuthDTO authDTO, IntegrationDTO integrationDTO);

	public void getUserV2(AuthDTO authDTO, UserDTO userDTO);

	public void generateProfileOTP(AuthDTO authDTO, String mobileNumber);

	public void validateProfileOTP(AuthDTO authDTO, String mobileNumber, int otpNumber);

	public Map<String, String> generateProfileOTPV2(AuthDTO authDTO, String mobileNumber);
	
	public void validateProfileOTPV2(AuthDTO authDTO, String mobileNumber, int otpNumber);
	
	public void saveVehicleDriverUser(AuthDTO authDTO, UserDTO userDTO);
	
	public List<UserDTO> getAllUserV2(AuthDTO authDTO, UserTagEM userTag);
}
