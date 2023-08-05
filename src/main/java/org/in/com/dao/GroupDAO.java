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
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ServiceException;

public class GroupDAO {
	public List<GroupDTO> getAllGroups(AuthDTO authDTO) {
		List<GroupDTO> list = new ArrayList<GroupDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT grgr.id, grgr.code, grgr.name, grgr.description,grgr.user_role_id,grgr.level, grgr.color, grgr.active_flag FROM user_group grgr WHERE  grgr.active_flag < 2 AND grgr.namespace_id = ?");
			@Cleanup
			PreparedStatement selectUserC0untPS = connection.prepareStatement("SELECT COUNT(1) as usercount FROM user WHERE namespace_id = ? AND user_group_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("grgr.id"));
				groupDTO.setName(selectRS.getString("grgr.name"));
				groupDTO.setCode(selectRS.getString("grgr.code"));
				groupDTO.setDecription(selectRS.getString("grgr.description"));
				groupDTO.setColor(selectRS.getString("grgr.color"));
				groupDTO.setActiveFlag(selectRS.getInt("grgr.active_flag"));
				groupDTO.setLevel(selectRS.getInt("grgr.level"));
				groupDTO.setRole(UserRoleEM.getUserRoleEM(selectRS.getInt("grgr.user_role_id")));
				selectUserC0untPS.setInt(1, authDTO.getNamespace().getId());
				selectUserC0untPS.setInt(2, selectRS.getInt("grgr.id"));
				@Cleanup
				ResultSet selectgrgrRS = selectUserC0untPS.executeQuery();
				if (selectgrgrRS.next()) {
					groupDTO.setUserCount(selectgrgrRS.getInt("usercount"));
				}
				selectUserC0untPS.clearParameters();
				list.add(groupDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<GroupDTO> getAllActiveGroup(AuthDTO authDTO) {
		List<GroupDTO> list = new ArrayList<GroupDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT grgr.id, grgr.code, grgr.name, grgr.description,grgr.user_role_id,grgr.level, grgr.color, grgr.active_flag FROM user_group grgr WHERE  grgr.active_flag < 2 AND grgr.namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setName(selectRS.getString("grgr.name"));
				groupDTO.setCode(selectRS.getString("grgr.code"));
				groupDTO.setColor(selectRS.getString("grgr.color"));
				groupDTO.setDecription(selectRS.getString("grgr.description"));
				groupDTO.setActiveFlag(selectRS.getInt("grgr.active_flag"));
				groupDTO.setLevel(selectRS.getInt("grgr.level"));
				groupDTO.setRole(UserRoleEM.getUserRoleEM(selectRS.getInt("grgr.user_role_id")));
				list.add(groupDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public void getGroupDTO(AuthDTO authDTO, GroupDTO groupDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (groupDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name,user_role_id, description, level, color, active_flag FROM user_group  grgr WHERE  namespace_id = ? AND id = ? AND active_flag < 2 ");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, groupDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, code, name,user_role_id, description, level, color, active_flag FROM user_group  grgr WHERE  namespace_id = ? AND code = ? AND active_flag < 2 ");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, groupDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				groupDTO.setId(selectRS.getInt("id"));
				groupDTO.setName(selectRS.getString("name"));
				groupDTO.setCode(selectRS.getString("code"));
				groupDTO.setLevel(selectRS.getInt("level"));
				groupDTO.setColor(selectRS.getString("color"));
				groupDTO.setDecription(selectRS.getString("description"));
				groupDTO.setRole(UserRoleEM.getUserRoleEM(selectRS.getInt("user_role_id")));
				groupDTO.setActiveFlag(selectRS.getInt("level"));
				groupDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public GroupDTO getGroupIUD(AuthDTO authDTO, GroupDTO groupDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_USER_GROUP_IUD( ?,?,?,?,?, ?,?,?,?,?, ?)}");
			termSt.setString(++pindex, groupDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setInt(++pindex, groupDTO.getRole().getId());
			termSt.setString(++pindex, groupDTO.getName());
			termSt.setString(++pindex, groupDTO.getDecription());
			termSt.setString(++pindex, groupDTO.getColor());
			termSt.setInt(++pindex, groupDTO.getLevel());
			termSt.setInt(++pindex, groupDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				groupDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return groupDTO;
	}

	public GroupDTO getGroupMapUser(AuthDTO authDTO, GroupDTO groupDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user,user_group SET user.user_group_id = user_group.id WHERE user.namespace_id = ? and  user.namespace_id =user_group.namespace_id AND user_group.code = ? AND user.code = ?");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, groupDTO.getCode());
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
		return groupDTO;
	}
	
	public GroupDTO checkVehicleDriverGroup(AuthDTO authDTO) {
		GroupDTO group = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code FROM user_group WHERE namespace_id = ? AND name LIKE '%Vehicle Driver%' AND active_flag = 1 LIMIT 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				group = new GroupDTO();
				group.setCode(selectRS.getString("code"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return group;
	}

}
