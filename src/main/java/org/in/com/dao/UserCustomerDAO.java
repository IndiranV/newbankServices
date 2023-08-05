package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserCustomerAuthDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class UserCustomerDAO extends BaseDAO {

	public void updateUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_USER_CUSTOMER_IUD (?,?,?,?,?, ?,?,?,?,?, ?,?)}");
			termSt.setString(++pindex, userCustomerDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, userCustomerDTO.getEmail());
			termSt.setString(++pindex, userCustomerDTO.getMobile());
			termSt.setString(++pindex, userCustomerDTO.getName());
			termSt.setString(++pindex, userCustomerDTO.getLastname());
			termSt.setString(++pindex, userCustomerDTO.getWalletCode());
			termSt.setInt(++pindex, userCustomerDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser() != null ? authDTO.getUser().getId() : Numeric.ZERO_INT);
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				if (StringUtil.isNull(userCustomerDTO.getCode())) {
					userCustomerDTO.setId(termSt.getInt("pitUserCustomerId"));
				}
				userCustomerDTO.setCode(termSt.getString("pcrCode"));
				userCustomerDTO.setWalletCode(termSt.getString("pcrWalletCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public UserCustomerDTO checkUserCustomer(AuthDTO authDTO, String mobileNumber) {
		UserCustomerDTO userCustomerDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, email, mobile, first_name, last_name, wallet_code, active_flag FROM user_customer WHERE namespace_id = ? AND mobile = ? AND active_flag <= 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, mobileNumber);

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setId(selectRS.getInt("id"));
				userCustomerDTO.setCode(selectRS.getString("code"));
				userCustomerDTO.setEmail(selectRS.getString("email"));
				userCustomerDTO.setMobile(selectRS.getString("mobile"));
				userCustomerDTO.setName(selectRS.getString("first_name"));
				userCustomerDTO.setLastname(selectRS.getString("last_name"));
				userCustomerDTO.setWalletCode(selectRS.getString("wallet_code"));
				userCustomerDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userCustomerDTO;
	}

	public void getUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (userCustomerDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, email, mobile, first_name, last_name, wallet_code, active_flag FROM  user_customer WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, userCustomerDTO.getId());
			}
			else if (StringUtil.isNotNull(userCustomerDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, email, mobile, first_name, last_name, wallet_code, active_flag FROM  user_customer WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, userCustomerDTO.getCode());
			}
			else if (StringUtil.isNotNull(userCustomerDTO.getMobile())) {
				selectPS = connection.prepareStatement("SELECT id, code, email, mobile, first_name, last_name, wallet_code, active_flag FROM  user_customer WHERE namespace_id = ? AND mobile = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, userCustomerDTO.getMobile());
			}
			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					userCustomerDTO.setId(selectRS.getInt("id"));
					userCustomerDTO.setCode(selectRS.getString("code"));
					userCustomerDTO.setEmail(selectRS.getString("email"));
					userCustomerDTO.setMobile(selectRS.getString("mobile"));
					userCustomerDTO.setName(selectRS.getString("first_name"));
					userCustomerDTO.setLastname(selectRS.getString("last_name"));
					userCustomerDTO.setWalletCode(selectRS.getString("wallet_code"));
					userCustomerDTO.setActiveFlag(selectRS.getInt("active_flag"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void addAppStoreDetails(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO user_customer_app_store (namespace_id, user_customer_id, device_medium_id, model, os, udid, gcm_token, active_flag, updated_by, updated_at) VALUES (?,?,?,?,?,?,?,1,?,NOW())");
			ps.setInt(++pindex, authDTO.getNamespace().getId());
			ps.setInt(++pindex, userCustomer.getId());
			ps.setInt(++pindex, userCustomer.getAppStoreDetails().getDeviceMedium().getId());
			ps.setString(++pindex, userCustomer.getAppStoreDetails().getModel());
			ps.setString(++pindex, userCustomer.getAppStoreDetails().getOs());
			ps.setString(++pindex, userCustomer.getAppStoreDetails().getUdid());
			ps.setString(++pindex, userCustomer.getAppStoreDetails().getGcmToken());
			ps.setInt(++pindex, authDTO.getUser().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateUserAppStoreDetails(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user_customer_app_store SET model = ?, os = ?, udid = ?, gcm_token = ?, device_medium_id = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND user_customer_id = ? AND active_flag = 1");
			preparedStatement.setString(1, userCustomer.getAppStoreDetails().getModel());
			preparedStatement.setString(2, userCustomer.getAppStoreDetails().getOs());
			preparedStatement.setString(3, userCustomer.getAppStoreDetails().getUdid());
			preparedStatement.setString(4, userCustomer.getAppStoreDetails().getGcmToken());
			preparedStatement.setInt(5, userCustomer.getAppStoreDetails().getDeviceMedium().getId());
			preparedStatement.setInt(6, authDTO.getUser().getId());
			preparedStatement.setInt(7, authDTO.getNamespace().getId());
			preparedStatement.setInt(8, userCustomer.getId());
			int status = preparedStatement.executeUpdate();
			userCustomer.setActiveFlag(status);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	public boolean isUserAppStoreExist(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		boolean userAppExist = Text.FALSE;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM user_customer_app_store WHERE namespace_id = ? AND user_customer_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userCustomerDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userAppExist = Text.TRUE;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userAppExist;
	}

	public List<AppStoreDetailsDTO> getAppStoreDetails(AuthDTO authDTO, UserCustomerDTO userDTO) {
		List<AppStoreDetailsDTO> userAppDetailsList = new ArrayList<AppStoreDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT model, os, udid, gcm_token, device_medium_id, active_flag FROM user_customer_app_store WHERE namespace_id = ? AND user_customer_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AppStoreDetailsDTO userAppDetailsDTO = new AppStoreDetailsDTO();
				userAppDetailsDTO.setModel(selectRS.getString("model"));
				userAppDetailsDTO.setOs(selectRS.getString("os"));
				userAppDetailsDTO.setUdid(selectRS.getString("udid"));
				userAppDetailsDTO.setGcmToken(selectRS.getString("gcm_token"));
				userAppDetailsDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium_id")));
				userAppDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				userAppDetailsList.add(userAppDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userAppDetailsList;
	}

	public void addUserCustomerAuth(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		UserCustomerAuthDTO userCustomerAuthDTO = userCustomerDTO.getUserCustomerAuth();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO user_customer_auth(namespace_id, user_customer_id, device_medium_id, session_token, active_flag, updated_by, updated_at) VALUES(?,?,?,?, 1,?,NOW())");
			ps.setInt(++pindex, authDTO.getNamespace().getId());
			ps.setInt(++pindex, userCustomerDTO.getId());
			ps.setInt(++pindex, userCustomerAuthDTO.getDeviceMedium().getId());
			ps.setString(++pindex, userCustomerAuthDTO.getSessionToken());
			ps.setInt(++pindex, authDTO.getUser() != null ? authDTO.getUser().getId() : Numeric.ZERO_INT);
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateUserCustomerAuth(AuthDTO authDTO, UserCustomerAuthDTO userCustomerAuthDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user_customer_auth SET session_token = ?, updated_by = ?, updated_at = NOW() WHERE id = ? AND namespace_id = ?");
			preparedStatement.setString(1, userCustomerAuthDTO.getSessionToken());
			preparedStatement.setInt(2, authDTO.getUser().getId());
			preparedStatement.setInt(3, userCustomerAuthDTO.getId());
			preparedStatement.setInt(4, authDTO.getNamespace().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	public List<UserCustomerAuthDTO> getUserCustomerAuth(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		List<UserCustomerAuthDTO> userCustomerAuthList = new ArrayList<UserCustomerAuthDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, device_medium_id, session_token, active_flag FROM user_customer_auth WHERE namespace_id = ? AND user_customer_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userCustomer.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserCustomerAuthDTO customerAuth = new UserCustomerAuthDTO();
				customerAuth.setId(selectRS.getInt("id"));
				customerAuth.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium_id")));
				customerAuth.setSessionToken(selectRS.getString("session_token"));
				customerAuth.setActiveFlag(selectRS.getInt("active_flag"));
				userCustomerAuthList.add(customerAuth);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userCustomerAuthList;
	}

}
