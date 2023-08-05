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
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.exception.ServiceException;

public class ScheduleCategoryDAO {

	public List<ScheduleCategoryDTO> getAllCategory(AuthDTO authDTO) {
		List<ScheduleCategoryDTO> list = new ArrayList<ScheduleCategoryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, active_flag FROM schedule_category WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
				categoryDTO.setId(selectRS.getInt("id"));
				categoryDTO.setCode(selectRS.getString("code"));
				categoryDTO.setName(selectRS.getString("name"));
				categoryDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(categoryDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void update(AuthDTO authDTO, ScheduleCategoryDTO categoryDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("CALL EZEE_SP_SCHEDULE_CATEGORY_IUD(?,?,?,?,?, ?,?)");
			callableStatement.setString(++pindex, categoryDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, categoryDTO.getName());
			callableStatement.setInt(++pindex, categoryDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			categoryDTO.setCode(callableStatement.getString("pcrCode"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ScheduleCategoryDTO getCategory(AuthDTO authDTO, ScheduleCategoryDTO categoryDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (categoryDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag FROM schedule_category WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, categoryDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag FROM schedule_category WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, categoryDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				categoryDTO.setId(selectRS.getInt("id"));
				categoryDTO.setCode(selectRS.getString("code"));
				categoryDTO.setName(selectRS.getString("name"));
				categoryDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return categoryDTO;
	}
}
