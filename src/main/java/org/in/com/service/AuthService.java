package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;

import net.sf.json.JSONObject;

public interface AuthService {
	public AuthDTO CheckAuthendtication(NamespaceDTO namespaceDTO, AuthenticationTypeEM authenticationType, String username, String password, DeviceMediumEM devicemedium, String userFirstName);

	public AuthDTO getGuestAuthendtication(String namespaceCode, DeviceMediumEM devicemedium);

	public AuthDTO getGuestAuthendticationV2(String namespaceCode, DeviceMediumEM devicemedium, String contextToken);

	public void switchNamespace(AuthDTO authDTO, NamespaceDTO namespaceDTO);

	public AuthDTO APIAuthendtication(String apiToken);

	public AuthDTO APIAuthendtication(String operatorCode, String username, String apiToken);

	public AuthDTO APIAuthendtication(String operatorCode, String username, String apiToken, DeviceMediumEM deviceMedium);

	public UserDTO forgetPassword(AuthDTO authDTO, String username);

	public void removeUserAuthToken(AuthDTO authDTO, UserDTO userDTO);

	public AuthDTO CheckDeviceAuthendtication(NamespaceDTO namespaceDTO, NamespaceDeviceDTO deviceDTO, AuthenticationTypeEM bitsUsernamePassword, String username, String password, DeviceMediumEM onlineUser);

	public AuthDTO CheckTabletAuthendtication(NamespaceDTO namespaceDTO, TabletDTO tabletDTO, AuthenticationTypeEM authenticationType, String username, String deviceToken, DeviceMediumEM deviceMedium);

	public AuthDTO getAuthDTO(String authtoken);

	public AuthDTO getAuthDetails(String authtoken, String guestKey);

	public UserDTO getUserDTO(String authtoken);

	public void logout(String authtoken);

	public void getLogoutSessionToken(AuthDTO authDTO, String sessiontoken);

	public AuthDTO getVerifyPassword(AuthDTO authDTO, String password);

	public void updateUserLoginSession(AuthDTO authDTO);

	public void switchUser(AuthDTO authDTO, UserDTO user);

	public AuthDTO getApiAuthendtication(String namespaceCode, String username, String apiToken, DeviceMediumEM deviceMedium);

	public void generateCustomerOTP(AuthDTO authDTO, String mobileNumber);

	public AuthDTO getCustomerAuthendticationV2(AuthDTO authDTO, String mobileNumber, int otpNumber);

	public AuthDTO getCustomerAuthendticationV3(AuthDTO auth, UserCustomerDTO userCustomer);

	public AuthDTO verifyAuthToken(IntegrationDTO integrationDTO, String authToken);

	public String generateBotOTP(AuthDTO authDTO);

	public JSONObject getBotOTP(String otp);

	public NamespaceDTO getAliasNamespace(String namespaceCode);

}