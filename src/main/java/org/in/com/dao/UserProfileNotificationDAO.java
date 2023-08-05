package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserProfileNotificationDTO;
import org.in.com.exception.ServiceException;

public class UserProfileNotificationDAO {

	public List<UserProfileNotificationDTO> getNotification(AuthDTO authDTO) {
		List<UserProfileNotificationDTO> list = new ArrayList<UserProfileNotificationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, message, active_from, comment_flag,refference_id,refference_type FROM user_profile_notification upn WHERE namespace_id = ? AND upn.active_flag = 1 AND NOT EXISTS (SELECT 1 FROM user_profile_notification_like nlike WHERE nlike.namespace_id = ? AND user_profile_notification_id = upn.id AND user_id = ?)");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getUser().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserProfileNotificationDTO dto = new UserProfileNotificationDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setMessage(selectRS.getString("message"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setCommentFlag(selectRS.getInt("comment_flag"));
				String refferenceType = selectRS.getString("refference_type");
				int refferenceId = selectRS.getInt("refference_id");
				if (refferenceType.equals("GR") && refferenceId == authDTO.getGroup().getId()) {
					list.add(dto);
				}
				else if (refferenceType.equals("UR") && refferenceId == authDTO.getUser().getId()) {
					list.add(dto);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<UserProfileNotificationDTO> getAllNotification(AuthDTO authDTO) {
		List<UserProfileNotificationDTO> list = new ArrayList<UserProfileNotificationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,namespace_id,message,active_from,active_to,comment_flag, refference_type  ,refference_id , active_flag  FROM user_profile_notification WHERE namespace_id = ? AND  active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserProfileNotificationDTO dto = new UserProfileNotificationDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setMessage(selectRS.getString("message"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setCommentFlag(selectRS.getInt("comment_flag"));
				if (selectRS.getString("refference_type").equals("UR")) {
					UserDTO userDTO = new UserDTO();
					userDTO.setId(selectRS.getInt("refference_id"));
					dto.setUser(userDTO);
				}
				if (selectRS.getString("refference_type").equals("GR")) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(selectRS.getInt("refference_id"));
					dto.setGroup(groupDTO);
				}
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void update(AuthDTO authDTO, UserProfileNotificationDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_USER_PROFILE_NOTIFICATION_IUD( ?,?,?,?,?, ?,?,?,?,?, ?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setString(++pindex, dto.getMessage());
			callableStatement.setString(++pindex, dto.getActiveFrom());
			callableStatement.setString(++pindex, dto.getActiveTo());
			callableStatement.setInt(++pindex, dto.getCommentFlag());
			String refferenceCode = dto.getGroup() != null && dto.getGroup().getCode() != null ? dto.getGroup().getCode() : dto.getUser() != null && dto.getUser().getCode() != null ? dto.getUser().getCode() : "";
			String refferenceType = dto.getGroup() != null && dto.getGroup().getCode() != null ? "GR" : dto.getUser() != null && dto.getUser().getCode() != null ? "UR" : "NA";
			callableStatement.setString(++pindex, refferenceCode);
			callableStatement.setString(++pindex, refferenceType);
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
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateLike(AuthDTO authDTO, UserProfileNotificationDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_USER_PROFILE_NOTIFICATION_LIKE_IUD(?,?,?,?,?,?,?,?,?)}");
			callableStatement.setString(++pindex, null);
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getComments());
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
	}

}
