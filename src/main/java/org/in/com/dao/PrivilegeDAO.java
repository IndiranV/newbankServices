package org.in.com.dao;

import hirondelle.date4j.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.SessionStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class PrivilegeDAO {

	public UserDTO getAuthendtication(AuthDTO authDTO, String username) {
		UserDTO userDTO = new UserDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ur.id, ur.token, ur.code, ur.api_token, ur.first_name,ur.last_name,ur.mobile,ur.oauth,  ur.email,ur.user_role_id,payment_type,active_flag,user_group_id,organization_id, integration_id FROM user ur WHERE ur.namespace_id = ? AND ur.username = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, username);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userDTO.setId(selectRS.getInt("ur.id"));
				userDTO.setUsername(username);
				userDTO.setToken(selectRS.getString("token"));
				userDTO.setCode(selectRS.getString("ur.code"));
				userDTO.setApiToken(selectRS.getString("ur.api_token"));
				userDTO.setEmail(selectRS.getString("ur.email"));
				userDTO.setName(selectRS.getString("ur.first_name"));
				userDTO.setLastname(selectRS.getString("ur.last_name"));
				userDTO.setMobile(selectRS.getString("ur.mobile"));
				userDTO.setOAuth(selectRS.getBoolean("ur.oauth"));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));
				userDTO.setGroup(groupDTO);

				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(selectRS.getInt("organization_id"));
				userDTO.setOrganization(organizationDTO);
				
				IntegrationDTO integration = new IntegrationDTO();
				integration.setId(selectRS.getInt("integration_id"));
				userDTO.setIntegration(integration);

				userDTO.setActiveFlag(selectRS.getInt("active_flag"));
				userDTO.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
				userDTO.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("payment_type")));
			}

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return userDTO;
	}

	public UserDTO getAuthendticationByCode(AuthDTO authDTO, String usercode) {
		UserDTO userDTO = new UserDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ur.id, ur.username, ur.token, ur.code, ur.api_token, ur.first_name,ur.last_name,ur.mobile,ur.oauth,  ur.email,ur.user_role_id,payment_type,active_flag,user_group_id,organization_id FROM user ur WHERE ur.namespace_id = ? AND ur.code = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, usercode);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userDTO.setId(selectRS.getInt("ur.id"));
				userDTO.setUsername(selectRS.getString("ur.username"));
				userDTO.setToken(selectRS.getString("token"));
				userDTO.setCode(selectRS.getString("ur.code"));
				userDTO.setApiToken(selectRS.getString("ur.api_token"));
				userDTO.setEmail(selectRS.getString("ur.email"));
				userDTO.setName(selectRS.getString("ur.first_name"));
				userDTO.setLastname(selectRS.getString("ur.last_name"));
				userDTO.setMobile(selectRS.getString("ur.mobile"));
				userDTO.setOAuth(selectRS.getBoolean("ur.oauth"));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));
				userDTO.setGroup(groupDTO);

				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(selectRS.getInt("organization_id"));
				userDTO.setOrganization(organizationDTO);

				userDTO.setActiveFlag(selectRS.getInt("active_flag"));
				userDTO.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
				userDTO.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("payment_type")));
			}

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return userDTO;
	}

	public boolean loginEntry(AuthDTO authDTO, int duplicateFlag) {
		DateTime now = DateUtil.NOW();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("INSERT INTO user_login_history(namespace_id,user_id,login_privider,device_medium, session_started_at, session_end_at,auth_token, ip_address, latitude, longitude, duplicate_flag, active_flag, updated_by, updated_at) VALUES(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?) ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getUser().getId());
			selectPS.setInt(3, authDTO.getAuthenticationType().getId());
			selectPS.setInt(4, authDTO.getDeviceMedium().getId());
			selectPS.setString(5, now.format("YYYY-MM-DD hh:mm:ss"));
			selectPS.setString(6, now.format("YYYY-MM-DD hh:mm:ss"));
			selectPS.setString(7, authDTO.getAuthToken());
			selectPS.setString(8, StringUtil.substring(getEvents(authDTO), 100));
			selectPS.setString(9, Text.NA);
			selectPS.setString(10, Text.NA);
			selectPS.setInt(11, duplicateFlag);
			selectPS.setInt(12, 1);
			selectPS.setInt(13, authDTO.getUser().getId());
			selectPS.setString(14, now.format("YYYY-MM-DD hh:mm:ss"));

			selectPS.execute();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return true;
	}

	public boolean updateUserLoginSession(AuthDTO authDTO, AuthDTO sessionAuthDTO, SessionStatusEM statusEM) {
		DateTime now = DateUtil.NOW();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE user_login_history SET session_end_at = ?, active_flag = ? , updated_by = ?, updated_at = ? WHERE namespace_id = ? AND user_id = ? AND auth_token = ? AND active_flag = 1");
			selectPS.setString(1, now.format("YYYY-MM-DD hh:mm:ss"));
			selectPS.setInt(2, statusEM.getId());
			selectPS.setInt(3, authDTO.getUser().getId());
			selectPS.setString(4, now.format("YYYY-MM-DD hh:mm:ss"));
			selectPS.setInt(5, authDTO.getNamespace().getId());
			selectPS.setInt(6, sessionAuthDTO.getUser().getId());
			selectPS.setString(7, sessionAuthDTO.getAuthToken());

			selectPS.execute();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return true;
	}

	public AuthDTO getAPIAuthendtication(String apiToken) {
		AuthDTO authDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ns.code, ur.code,api_token FROM user ur,namespace ns WHERE ns.id = ur.namespace_id AND ur.api_token = ? AND ur.active_flag = 1");
			selectPS.setString(1, apiToken);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				authDTO = new AuthDTO();
				authDTO.setNamespaceCode(selectRS.getString("ns.code"));
				authDTO.setNativeNamespaceCode(selectRS.getString("ns.code"));
				authDTO.setUserCode(selectRS.getString("ur.code"));
				authDTO.setApiToken(selectRS.getString("api_token"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return authDTO;
	}

	public AuthDTO getAPIAuthendticationV3(NamespaceDTO namespaceDTO, String username) {
		AuthDTO authDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,api_token FROM user WHERE namespace_id = ? AND username = ? AND active_flag = 1");
			selectPS.setInt(1, namespaceDTO.getId());
			selectPS.setString(2, username);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				authDTO = new AuthDTO();
				authDTO.setNamespaceCode(namespaceDTO.getCode());
				authDTO.setNativeNamespaceCode(namespaceDTO.getCode());
				authDTO.setUserCode(selectRS.getString("code"));
				authDTO.setApiToken(selectRS.getString("api_token"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return authDTO;
	}
	
	private String getEvents(AuthDTO authDTO) {
		String events = authDTO.getAdditionalAttribute().containsKey("events") ? authDTO.getAdditionalAttribute().get("events") : Text.NA;
		return events;
	}
}
