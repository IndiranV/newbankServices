package org.in.com.dao;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.UserRegistrationDTO;
import org.in.com.dto.UserSessionAuditDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.SessionStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.JSONUtil;
import org.in.com.utils.StringUtil;

public class UserDAO extends BaseDAO {
	public List<UserDTO> getUser(AuthDTO authDTO, UserDTO userDTO) {
		List<UserDTO> list = new ArrayList<UserDTO>();

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, username, user_group_id, organization_id, token, email, mobile, first_name, last_name, api_token, forget_token, user_role_id, payment_type, tags, additional_attribute, integration_id, active_flag FROM user WHERE code = ? AND namespace_id = ? AND active_flag = 1 AND user_role_id = ?");
			selectPS.setString(1, userDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, UserRoleEM.USER_ROLE.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDTO dto = new UserDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setUsername(selectRS.getString("username"));
				dto.setEmail(selectRS.getString("email"));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));
				dto.setGroup(groupDTO);

				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(selectRS.getInt("organization_id"));
				dto.setOrganization(organizationDTO);

				dto.setMobile(selectRS.getString("mobile"));
				dto.setName(selectRS.getString("first_name"));
				dto.setLastname(selectRS.getString("last_name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setApiToken(selectRS.getString("api_token"));
				dto.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
				dto.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("payment_type")));

				IntegrationDTO integration = new IntegrationDTO();
				integration.setId(selectRS.getInt("integration_id"));
				dto.setIntegration(integration);

				List<UserTagEM> userTags = convertUserTagList(selectRS.getString("tags"));
				dto.setUserTags(userTags);
				
				Map<String, String> additionalAttribute = convertAdditionalAttribute(selectRS.getString("additional_attribute"));
				dto.setAdditionalAttribute(additionalAttribute);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<UserDTO> getNamespaceUsers(AuthDTO authDTO) {
		List<UserDTO> list = new ArrayList<UserDTO>();
		Map<Integer, OrganizationDTO> orgMap = new HashMap<Integer, OrganizationDTO>();
		OrganizationDAO organizationDAO = new OrganizationDAO();

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			List<OrganizationDTO> Orglist = organizationDAO.getOrganizationDTO(connection, authDTO);
			for (OrganizationDTO organizationDTO : Orglist) {
				orgMap.put(organizationDTO.getId(), organizationDTO);
			}

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, username, user_group_id, organization_id, token, email, mobile, first_name, last_name, api_token, forget_token, user_role_id, payment_type, tags, additional_attribute, integration_id, contact_verified_flag, active_flag FROM user WHERE namespace_id = ? AND active_flag < 2 AND user_role_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, UserRoleEM.USER_ROLE.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDTO dto = new UserDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setUsername(selectRS.getString("username"));
				dto.setEmail(selectRS.getString("email"));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));

				int userOrgId = selectRS.getInt("organization_id");
				dto.setMobile(selectRS.getString("mobile"));
				dto.setName(selectRS.getString("first_name"));
				dto.setLastname(selectRS.getString("last_name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setApiToken(selectRS.getString("api_token"));
				dto.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
				if (userOrgId != 0 && orgMap.get(userOrgId) != null) {
					dto.setOrganization(orgMap.get(userOrgId));
				}
				dto.setGroup(groupDTO);
				dto.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("payment_type")));

				IntegrationDTO integration = new IntegrationDTO();
				integration.setId(selectRS.getInt("integration_id"));
				dto.setIntegration(integration);
				dto.setContactVerifiedFlag(selectRS.getString("contact_verified_flag"));

				List<UserTagEM> userTags = convertUserTagList(selectRS.getString("tags"));
				dto.setUserTags(userTags);
				
				Map<String, String> additionalAttribute = convertAdditionalAttribute(selectRS.getString("additional_attribute"));
				dto.setAdditionalAttribute(additionalAttribute);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public UserDTO getUsersDTO(Connection connection, int namespaceId, int userId) {
		UserDTO dto = new UserDTO();
		try {

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, username, user_group_id, organization_id, token, email, mobile, first_name, last_name, api_token, forget_token, user_role_id, tags, active_flag FROM user WHERE namespace_id = ? AND id = ? AND active_flag < 2");
			selectPS.setInt(1, namespaceId);
			selectPS.setInt(2, userId);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				dto.setCode(selectRS.getString("code"));
				dto.setUsername(selectRS.getString("username"));
				dto.setEmail(selectRS.getString("email"));
				dto.setMobile(selectRS.getString("mobile"));
				dto.setName(selectRS.getString("first_name"));
				dto.setLastname(selectRS.getString("last_name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setApiToken(selectRS.getString("api_token"));
				dto.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));

				List<UserTagEM> userTags = convertUserTagList(selectRS.getString("tags"));
				dto.setUserTags(userTags);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public void UserUID(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_USER_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}");
			termSt.setString(++pindex, userDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, userDTO.getUsername());
			termSt.setString(++pindex, userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
			termSt.setString(++pindex, userDTO.getOrganization() != null ? userDTO.getOrganization().getCode() : null);
			termSt.setString(++pindex, null);
			termSt.setString(++pindex, "NA");
			termSt.setString(++pindex, userDTO.getEmail());
			termSt.setString(++pindex, userDTO.getMobile());
			termSt.setString(++pindex, userDTO.getName());
			termSt.setString(++pindex, userDTO.getLastname());
			termSt.setInt(++pindex, userDTO.getUserRole() != null ? userDTO.getUserRole().getId() : authDTO.getUser().getUserRole().getId());
			termSt.setInt(++pindex, userDTO.getPaymentType().getId());
			termSt.setInt(++pindex, userDTO.getIntegration() != null ? userDTO.getIntegration().getId() : Numeric.ZERO_INT);
			termSt.setString(++pindex, userDTO.getUserTagCodes());
			termSt.setString(++pindex, getAdditionalAttribute(userDTO.getAdditionalAttribute()));
			termSt.setInt(++pindex, userDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				userDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
	
	public void saveVehicleDriverUser(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_BUS_VEHICLE_DRIVER_USER_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			termSt.setString(++pindex, userDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, userDTO.getUsername());
			termSt.setString(++pindex, userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
			termSt.setString(++pindex, userDTO.getOrganization() != null ? userDTO.getOrganization().getCode() : null);
			termSt.setString(++pindex, null);
			termSt.setString(++pindex, "NA");
			termSt.setString(++pindex, userDTO.getEmail());
			termSt.setString(++pindex, userDTO.getMobile());
			termSt.setString(++pindex, userDTO.getName());
			termSt.setString(++pindex, userDTO.getLastname());
			termSt.setInt(++pindex, userDTO.getUserRole() != null ? userDTO.getUserRole().getId() : authDTO.getUser().getUserRole().getId());
			termSt.setInt(++pindex, userDTO.getPaymentType().getId());
			termSt.setInt(++pindex, userDTO.getIntegration() != null ? userDTO.getIntegration().getId() : Numeric.ZERO_INT);
			termSt.setString(++pindex, userDTO.getUserTagCodes());
			termSt.setInt(++pindex, userDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				userDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<UserDTO> getAllUserInGroups(AuthDTO authDTO, GroupDTO groupDTO) {
		List<UserDTO> list = new ArrayList<UserDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT acc.id, acc.username, acc.code, acc.email, acc.first_name, acc.last_name, acc.payment_type, acc.tags, acc.additional_attribute, acc.active_flag FROM user acc, user_group grgr WHERE acc.namespace_id = ? AND grgr.id = acc.user_group_id AND grgr.active_flag < 2 AND grgr.code = ? AND acc.user_role_id = ? AND acc.active_flag < 2 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, groupDTO.getCode());
			selectPS.setInt(3, UserRoleEM.USER_ROLE.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDTO dto = new UserDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setUsername(selectRS.getString("username"));
				dto.setEmail(selectRS.getString("email"));
				dto.setName(selectRS.getString("first_name"));
				dto.setLastname(selectRS.getString("last_name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("acc.payment_type")));
				dto.setGroup(groupDTO);

				List<UserTagEM> userTags = convertUserTagList(selectRS.getString("acc.tags"));
				dto.setUserTags(userTags);
				
				Map<String, String> additionalAttribute = convertAdditionalAttribute(selectRS.getString("acc.additional_attribute"));
				dto.setAdditionalAttribute(additionalAttribute);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<UserDTO> getAllUserInOrg(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		List<UserDTO> list = new ArrayList<UserDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT acc.id, acc.username, acc.code, acc.email, acc.first_name, acc.last_name, acc.payment_type, acc.tags, acc.additional_attribute, acc.active_flag FROM user acc, organization grgr WHERE acc.namespace_id = ? AND grgr.id = acc.organization_id AND acc.user_role_id = 1 AND grgr.active_flag < 2 AND acc.active_flag < 2 AND grgr.code = ? AND acc.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, organizationDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDTO dto = new UserDTO();
				dto.setId(selectRS.getInt("acc.id"));
				dto.setCode(selectRS.getString("code"));
				dto.setUsername(selectRS.getString("username"));
				dto.setEmail(selectRS.getString("email"));
				dto.setName(selectRS.getString("first_name"));
				dto.setLastname(selectRS.getString("last_name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("acc.payment_type")));

				List<UserTagEM> userTags = convertUserTagList(selectRS.getString("acc.tags"));
				dto.setUserTags(userTags);
				
				Map<String, String> additionalAttribute = convertAdditionalAttribute(selectRS.getString("acc.additional_attribute"));
				dto.setAdditionalAttribute(additionalAttribute);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void resetUserProfilePassword(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user  SET user.token = ?, user.password_updated_at = NOW(), updated_by = ?,  updated_at = NOW() WHERE user.namespace_id = ? and  user.code = ?");
			preparedStatement.setString(1, userDTO.getToken());
			preparedStatement.setInt(2, authDTO.getUser().getId());
			preparedStatement.setInt(3, authDTO.getNamespace().getId());
			preparedStatement.setString(4, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
			// Add Audit Log
			addAuditLog(connection, authDTO, userDTO.getCode(), "user", "Password Resert", "NA");
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateAPITokenPassword(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user  SET user.api_token = ? WHERE user.namespace_id = ? and  user.code = ?");
			preparedStatement.setString(1, userDTO.getApiToken());
			preparedStatement.setInt(2, authDTO.getNamespace().getId());
			preparedStatement.setString(3, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateUserMobile(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user SET mobile = ?, contact_verified_flag = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ?");
			preparedStatement.setString(1, userDTO.getMobile());
			preparedStatement.setString(2, userDTO.getContactVerifiedFlag());
			preparedStatement.setInt(3, authDTO.getUser().getId());
			preparedStatement.setInt(4, authDTO.getNamespace().getId());
			preparedStatement.setString(5, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
			// Add Audit Log
			addAuditLog(connection, authDTO, userDTO.getCode(), "user", "Mobile Number Verified", "NA");
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void getUserEmailVerifyToken(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT id,code,email,first_name FROM user WHERE namespace_id = ? and  forget_token = ?)");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, userDTO.getToken());
			@Cleanup
			ResultSet status = preparedStatement.executeQuery();
			if (status.next()) {
				userDTO.setId(status.getInt("id"));
				userDTO.setCode(status.getString("code"));
				userDTO.setName(status.getString("first_name"));
				userDTO.setEmail(status.getString("email"));
			}

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateEmailVerify(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user SET email_verified = NOW(), forget_token = null WHERE  namespace_id = ? AND  code = ?)");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public UserDTO getUserDTO(NamespaceDTO namespaceDTO, String userCode) {
		UserDTO userDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, user_role_id, token, forget_token, payment_type, tags, additional_attribute, api_token, contact_verified_flag, password_updated_at, user_group_id, organization_id, active_flag FROM user WHERE namespace_id = ? AND code = ? AND active_flag < 2");
			selectPS.setInt(1, namespaceDTO.getId());
			selectPS.setString(2, userCode);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("id"));
				userDTO.setCode(selectRS.getString("code"));
				userDTO.setUsername(selectRS.getString("username"));
				userDTO.setEmail(selectRS.getString("email"));
				userDTO.setMobile(selectRS.getString("mobile"));
				userDTO.setName(selectRS.getString("first_name"));
				userDTO.setLastname(selectRS.getString("last_name"));
				userDTO.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
				userDTO.setActiveFlag(selectRS.getInt("active_flag"));
				userDTO.setToken(selectRS.getString("token"));
				userDTO.setForgetToken(selectRS.getString("forget_token"));
				userDTO.setApiToken(selectRS.getString("api_token"));
				userDTO.setContactVerifiedFlag(selectRS.getString("contact_verified_flag"));
				String passwordUpdateDate = selectRS.getString("password_updated_at");
				userDTO.setPasswordUpdatedAt(StringUtil.isNull(passwordUpdateDate) ? Text.EMPTY : new DateTime(passwordUpdateDate).format("YYYY-MM-DD hh:mm:ss"));
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));
				userDTO.setGroup(groupDTO);
				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(selectRS.getInt("organization_id"));
				userDTO.setOrganization(organizationDTO);
				userDTO.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("payment_type")));

				List<UserTagEM> userTags = convertUserTagList(selectRS.getString("tags"));
				userDTO.setUserTags(userTags);
				
				Map<String, String> additionalAttribute = convertAdditionalAttribute(selectRS.getString("additional_attribute"));
				userDTO.setAdditionalAttribute(additionalAttribute);
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userDTO;
	}

	public void getUserDTO(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (userDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, user_role_id, token, forget_token, api_token, contact_verified_flag, password_updated_at, payment_type, tags, additional_attribute, user_group_id, organization_id, integration_id, active_flag FROM user WHERE namespace_id = ? AND id = ? AND active_flag < 3");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, userDTO.getId());
			}
			else if (StringUtil.isNotNull(userDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, user_role_id, token, forget_token, api_token, contact_verified_flag, password_updated_at, payment_type, tags, additional_attribute, user_group_id, organization_id, integration_id, active_flag FROM user WHERE namespace_id = ? AND code = ? AND active_flag < 3");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, userDTO.getCode());
			}
			else if (StringUtil.isNotNull(userDTO.getUsername())) {
				selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, user_role_id, token, forget_token, api_token, contact_verified_flag, password_updated_at, payment_type, tags, additional_attribute, user_group_id, organization_id, integration_id, active_flag FROM user WHERE namespace_id = ? AND username = ? AND active_flag < 3");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, userDTO.getUsername());
			}
			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					userDTO.setId(selectRS.getInt("id"));
					userDTO.setCode(selectRS.getString("code"));
					userDTO.setUsername(selectRS.getString("username"));
					userDTO.setEmail(selectRS.getString("email"));
					userDTO.setName(selectRS.getString("first_name"));
					userDTO.setLastname(selectRS.getString("last_name"));
					userDTO.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
					userDTO.setMobile(selectRS.getString("mobile"));
					userDTO.setActiveFlag(selectRS.getInt("active_flag"));
					userDTO.setToken(selectRS.getString("token"));
					userDTO.setApiToken(selectRS.getString("api_token"));
					userDTO.setForgetToken(selectRS.getString("forget_token"));
					userDTO.setContactVerifiedFlag(selectRS.getString("contact_verified_flag"));
					String passwordUpdateDate = selectRS.getString("password_updated_at");
					userDTO.setPasswordUpdatedAt(StringUtil.isNull(passwordUpdateDate) ? Text.EMPTY : new DateTime(passwordUpdateDate).format("YYYY-MM-DD hh:mm:ss"));

					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(selectRS.getInt("user_group_id"));
					userDTO.setGroup(groupDTO);

					OrganizationDTO organizationDTO = new OrganizationDTO();
					organizationDTO.setId(selectRS.getInt("organization_id"));
					userDTO.setOrganization(organizationDTO);

					IntegrationDTO integration = new IntegrationDTO();
					integration.setId(selectRS.getInt("integration_id"));
					userDTO.setIntegration(integration);

					userDTO.setPaymentType(PaymentTypeEM.getPaymentTypeEM(selectRS.getInt("payment_type")));

					List<UserTagEM> userTags = convertUserTagList(selectRS.getString("tags"));
					userDTO.setUserTags(userTags);
					
					Map<String, String> additionalAttribute = convertAdditionalAttribute(selectRS.getString("additional_attribute"));
					userDTO.setAdditionalAttribute(additionalAttribute);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public BigDecimal getCurrentCreditBalace(AuthDTO authDTO, UserDTO userDTO) {
		BigDecimal currentBalance = BigDecimal.ZERO;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT current_balance FROM user_balance WHERE namespace_id = ? AND  user_id = ? ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				currentBalance = selectRS.getBigDecimal("current_balance");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return currentBalance;
	}

	public List<UserFeedbackDTO> getAllUserFeedback(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		List<UserFeedbackDTO> list = new ArrayList<UserFeedbackDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT code, name, email, contact_number, comments, feedback_at, active_flag FROM user_feedback WHERE namespace_id = ? AND feedback_at between ? AND ? AND active_flag = 1");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, fromDate.format("YYYY-MM-DD"));
			preparedStatement.setString(3, toDate.plusDays(1).format("YYYY-MM-DD"));
			@Cleanup
			ResultSet status = preparedStatement.executeQuery();
			while (status.next()) {
				UserFeedbackDTO userdto = new UserFeedbackDTO();
				userdto.setCode(status.getString("code"));
				userdto.setName(status.getString("name"));
				userdto.setEmail(status.getString("email"));
				userdto.setMobile(status.getString("contact_number"));
				userdto.setComments(status.getString("comments"));
				userdto.setFeedbackDate(status.getString("feedback_at"));
				userdto.setActiveFlag(status.getInt("active_flag"));
				list.add(userdto);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public UserFeedbackDTO getUserFeedback(AuthDTO authDTO, UserFeedbackDTO userFeedback) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT code, name, email, contact_number, comments, feedback_at, active_flag FROM user_feedback WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, userFeedback.getCode());
			@Cleanup
			ResultSet status = preparedStatement.executeQuery();
			if (status.next()) {
				userFeedback.setCode(status.getString("code"));
				userFeedback.setName(status.getString("name"));
				userFeedback.setEmail(status.getString("email"));
				userFeedback.setMobile(status.getString("contact_number"));
				userFeedback.setComments(status.getString("comments"));
				userFeedback.setFeedbackDate(status.getString("feedback_at"));
				userFeedback.setActiveFlag(status.getInt("active_flag"));
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userFeedback;
	}

	public void updateUserFeedbackComment(AuthDTO authDTO, UserFeedbackDTO userFeedback) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPs = connection.prepareStatement("UPDATE user_feedback SET comments = ?, updated_by = ?, updated_at = NOW() WHERE code = ? AND namespace_id = ?");
			selectPs.setString(1, userFeedback.convertCommentReply());
			selectPs.setInt(2, authDTO.getUser().getId());
			selectPs.setString(3, userFeedback.getCode());
			selectPs.setInt(4, authDTO.getNamespace().getId());
			selectPs.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public UserFeedbackDTO updateUserFeedback(AuthDTO authDTO, UserFeedbackDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_USER_FEEDBACK_IUD(?,?,?,?,?, ?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getTicketCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setString(++pindex, dto.getEmail());
			callableStatement.setString(++pindex, dto.getMobile());
			callableStatement.setString(++pindex, dto.convertCommentReply());
			callableStatement.setInt(++pindex, dto.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				dto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public void updateForgetTokenPassword(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user SET forget_token = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ?");
			preparedStatement.setString(1, userDTO.getForgetToken());
			preparedStatement.setInt(2, userDTO.getId());
			preparedStatement.setInt(3, authDTO.getNamespace().getId());
			preparedStatement.setString(4, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public UserDTO getUsersDTO(Connection connection, int userId) {
		UserDTO dto = new UserDTO();
		try {

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, username, first_name, user_role_id, active_flag FROM user WHERE id = ? AND active_flag < 2");
			selectPS.setInt(1, userId);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				dto.setCode(selectRS.getString("code"));
				dto.setUsername(selectRS.getString("username"));
				dto.setName(selectRS.getString("first_name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setUserRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public UserRegistrationDTO addUserRegistrationRequest(AuthDTO authDTO, UserRegistrationDTO registrationDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL  EZEE_SP_USER_REGISTRATION_REQUEST_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, registrationDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, registrationDTO.getName());
			callableStatement.setString(++pindex, registrationDTO.getOrganization());
			callableStatement.setString(++pindex, registrationDTO.getAddress());
			callableStatement.setString(++pindex, registrationDTO.getCity());
			callableStatement.setString(++pindex, registrationDTO.getState());
			callableStatement.setString(++pindex, registrationDTO.getEmail());
			callableStatement.setString(++pindex, registrationDTO.getMobile());
			callableStatement.setString(++pindex, registrationDTO.getComments());
			callableStatement.setInt(++pindex, registrationDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				registrationDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return registrationDTO;

	}

	public List<UserRegistrationDTO> getUserRegistrationRequest(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		List<UserRegistrationDTO> list = new ArrayList<UserRegistrationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT code, name, organization, address, city, state, email, mobile, comments, request_at, active_flag FROM user_registration_request WHERE namespace_id = ? AND request_at between ? AND ? AND active_flag = 1");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, fromDate.format("YYYY-MM-DD"));
			preparedStatement.setString(3, toDate.format("YYYY-MM-DD"));
			@Cleanup
			ResultSet status = preparedStatement.executeQuery();
			while (status.next()) {
				UserRegistrationDTO userdto = new UserRegistrationDTO();
				userdto.setCode(status.getString("code"));
				userdto.setName(status.getString("name"));
				userdto.setAddress(status.getString("address"));
				userdto.setCity(status.getString("city"));
				userdto.setState(status.getString("state"));
				userdto.setEmail(status.getString("email"));
				userdto.setMobile(status.getString("mobile"));
				userdto.setComments(status.getString("comments"));
				userdto.setOrganization(status.getString("organization"));
				userdto.setRequestDate(status.getString("request_at"));
				userdto.setActiveFlag(status.getInt("active_flag"));
				list.add(userdto);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public boolean checkUsername(AuthDTO authDTO, String username) {
		boolean isUsername = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM user WHERE namespace_id = ? AND username = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, username);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				isUsername = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return isUsername;
	}

	public boolean checkUserMobileNumber(AuthDTO authDTO, String mobileNumber) {
		boolean isMobileNumber = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM user WHERE namespace_id = ? AND mobile = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, mobileNumber);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				isMobileNumber = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return isMobileNumber;
	}

	public void appStoreUpdate(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_USER_APP_STORE_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?)}");
			termSt.setString(++pindex, userDTO.getAppStoreDetails().getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setInt(++pindex, userDTO.getAppStoreDetails().getDeviceMedium().getId());
			termSt.setString(++pindex, userDTO.getCode());
			termSt.setString(++pindex, userDTO.getAppStoreDetails().getModel());
			termSt.setString(++pindex, userDTO.getAppStoreDetails().getOs());
			termSt.setString(++pindex, userDTO.getAppStoreDetails().getUdid());
			termSt.setString(++pindex, userDTO.getAppStoreDetails().getGcmToken());
			termSt.setInt(++pindex, userDTO.getAppStoreDetails().getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				userDTO.getAppStoreDetails().setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			System.out.println(JSONUtil.objectToJson(authDTO));
			System.out.println(JSONUtil.objectToJson(userDTO));
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<AppStoreDetailsDTO> getAppStoreDetails(AuthDTO authDTO, UserDTO userDTO) {
		List<AppStoreDetailsDTO> storeList = new ArrayList<AppStoreDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, model, os, udid, gcm_token, device_medium_id, active_flag FROM user_app_store WHERE namespace_id = ? AND user_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AppStoreDetailsDTO userAppDetailsDTO = new AppStoreDetailsDTO();
				userAppDetailsDTO.setCode(selectRS.getString("code"));
				userAppDetailsDTO.setModel(selectRS.getString("model"));
				userAppDetailsDTO.setOs(selectRS.getString("os"));
				userAppDetailsDTO.setUdid(selectRS.getString("udid"));
				userAppDetailsDTO.setGcmToken(selectRS.getString("gcm_token"));
				userAppDetailsDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium_id")));
				userAppDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				storeList.add(userAppDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return storeList;
	}

	public List<AppStoreDetailsDTO> getAppStoreDetailsV2(AuthDTO authDTO, UserDTO userDTO, DeviceMediumEM deviceMedium) {
		List<AppStoreDetailsDTO> userAppDetailsList = new ArrayList<AppStoreDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, model, os, udid, gcm_token, device_medium_id, active_flag, updated_at FROM user_app_store WHERE namespace_id = ? AND user_id = ? AND device_medium_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());
			selectPS.setInt(3, deviceMedium.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AppStoreDetailsDTO userAppDetailsDTO = new AppStoreDetailsDTO();
				userAppDetailsDTO.setCode(selectRS.getString("code"));
				userAppDetailsDTO.setModel(selectRS.getString("model"));
				userAppDetailsDTO.setOs(selectRS.getString("os"));
				userAppDetailsDTO.setUdid(selectRS.getString("udid"));
				userAppDetailsDTO.setGcmToken(selectRS.getString("gcm_token"));
				userAppDetailsDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium_id")));
				userAppDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));

				AuditDTO auditDTO = new AuditDTO();
				String updateDate = selectRS.getString("updated_at");
				auditDTO.setUpdatedAt(new DateTime(updateDate).format("YYYY-MM-DD hh:mm:ss"));
				userAppDetailsDTO.setAudit(auditDTO);

				userAppDetailsList.add(userAppDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userAppDetailsList;
	}

	public void updateUserDetails(AuthDTO authDTO, UserDTO userDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user SET username = ?, user_group_id = ?, token = ?, email = ?, first_name = ?, user_role_id = ?, active_flag = ?, updated_by = ?, updated_at = NOW() WHERE user.namespace_id = ? and  user.code = ?");
			preparedStatement.setString(1, userDTO.getUsername());
			preparedStatement.setInt(2, userDTO.getGroup().getId());
			preparedStatement.setString(3, userDTO.getToken());
			preparedStatement.setString(4, userDTO.getEmail());
			preparedStatement.setString(5, userDTO.getName());
			preparedStatement.setInt(6, userDTO.getUserRole().getId());
			preparedStatement.setInt(7, userDTO.getActiveFlag());
			preparedStatement.setInt(8, authDTO.getUser().getId());
			preparedStatement.setInt(9, authDTO.getNamespace().getId());
			preparedStatement.setString(10, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
			// Add Audit Log
			addAuditLog(connection, authDTO, userDTO.getCode(), "user", "Password details updated", "NA");
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	public boolean updateLoginSessionDetails(AuthDTO authDTO, UserSessionAuditDTO sessionAuditDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE user_login_history SET ip_address = ? , latitude = ? , longitude = ? WHERE namespace_id = ? AND user_id = ? AND auth_token = ?");
			selectPS.setString(1, sessionAuditDTO.getIpAddress());
			selectPS.setString(2, sessionAuditDTO.getLatitude());
			selectPS.setString(3, sessionAuditDTO.getLongitude());
			selectPS.setInt(4, authDTO.getNamespace().getId());
			selectPS.setInt(5, authDTO.getUser().getId());
			selectPS.setString(6, authDTO.getAuthToken());

			selectPS.execute();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return true;
	}

	public List<UserSessionAuditDTO> getUserRecentSession(AuthDTO authDTO, UserDTO userDTO) {
		List<UserSessionAuditDTO> list = new ArrayList<UserSessionAuditDTO>();

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT login_privider,device_medium,session_started_at,session_end_at,auth_token,ip_address,latitude,longitude,active_flag FROM user_login_history WHERE namespace_id = ? AND user_id = ? ORDER BY id DESC LIMIT 3");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserSessionAuditDTO sessionAuditDTO = new UserSessionAuditDTO();
				sessionAuditDTO.setIpAddress(selectRS.getString("ip_address"));
				sessionAuditDTO.setLatitude(selectRS.getString("latitude"));
				sessionAuditDTO.setLongitude(selectRS.getString("longitude"));
				sessionAuditDTO.setSessionStartAt(selectRS.getString("session_started_at"));
				sessionAuditDTO.setSessionEndAt(selectRS.getString("session_end_at"));
				sessionAuditDTO.setSessionStatus(SessionStatusEM.getSessionStatusEMEM(selectRS.getInt("active_flag")));
				sessionAuditDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium")));
				list.add(sessionAuditDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	private List<UserTagEM> convertUserTagList(String tags) {
		List<UserTagEM> tagList = new ArrayList<>();
		if (StringUtil.isNotNull(tags)) {
			List<String> tagCodes = Arrays.asList(tags.split(Text.COMMA));
			if (tagCodes != null) {
				for (String tagCode : tagCodes) {
					UserTagEM userTagEM = UserTagEM.getUserTagEM(tagCode);
					if (StringUtil.isNull(tagCode) || userTagEM == null) {
						continue;
					}
					tagList.add(userTagEM);
				}
			}
		}
		return tagList;
	}

	public void getUserV2(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (userDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, tags, active_flag FROM user WHERE id = ? AND active_flag < 3");
				selectPS.setInt(1, userDTO.getId());
			}
			else if (StringUtil.isNotNull(userDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, tags, active_flag FROM user WHERE code = ? AND active_flag < 3");
				selectPS.setString(1, userDTO.getCode());
			}
			else if (StringUtil.isNotNull(userDTO.getUsername())) {
				selectPS = connection.prepareStatement("SELECT id, username, code, email, mobile, first_name, last_name, tags, active_flag FROM user WHERE username = ? AND active_flag < 3");
				selectPS.setString(1, userDTO.getUsername());
			}

			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					userDTO.setId(selectRS.getInt("id"));
					userDTO.setCode(selectRS.getString("code"));
					userDTO.setUsername(selectRS.getString("username"));
					userDTO.setEmail(selectRS.getString("email"));
					userDTO.setMobile(selectRS.getString("mobile"));
					userDTO.setName(selectRS.getString("first_name"));
					userDTO.setLastname(selectRS.getString("last_name"));
					List<UserTagEM> userTags = convertUserTagList(selectRS.getString("tags"));
					userDTO.setUserTags(userTags);
					userDTO.setActiveFlag(selectRS.getInt("active_flag"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}
	
	private String getAdditionalAttribute(Map<String, String> additionalAttribute) {
		StringBuilder details = new StringBuilder();
		if (additionalAttribute != null) {
			for (Map.Entry<String, String> map : additionalAttribute.entrySet()) {
				if (details.length() > 0) {
					details.append(Text.COMMA);
				}
				details.append(map.getKey());
				details.append(Text.COLON);
				details.append(map.getValue());
			}
		}
		
		String additionalDetails = details.toString();
		if (StringUtil.isNull(additionalDetails)) {
			additionalDetails =  Text.NA;
		}
		return additionalDetails;
	}
	
	private Map<String, String> convertAdditionalAttribute(String additionalStr) {
		Map<String, String> additionalAttribute = new HashMap<>();
		if (StringUtil.isNotNull(additionalStr)) {
			String[] additionldetails = additionalStr.split(Text.COMMA);
			for (String details : additionldetails){
				if (details.split("\\:").length != 2) {
					continue;
				}
				additionalAttribute.put(details.split("\\:")[0], details.split("\\:")[1]);
			}
		}
		return additionalAttribute;
	}
}
