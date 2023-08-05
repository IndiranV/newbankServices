package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.StateDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class StateDAO {
	public List<StateDTO> getAllStates() {
		List<StateDTO> list = new ArrayList<StateDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,name,active_flag FROM state where active_flag < 2 order by name");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StateDTO dto = new StateDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return list;
	}

	public StateDTO getStates(StateDTO state) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (state.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id,code,name,active_flag FROM state where id = ? AND active_flag = 1");
				selectPS.setInt(1, state.getId());
			}
			else if (StringUtil.isNotNull(state.getCode())) {
				selectPS = connection.prepareStatement("SELECT id,code,name,active_flag FROM state where code = ? AND  active_flag = 1");
				selectPS.setString(1, state.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				state.setId(selectRS.getInt("id"));
				state.setCode(selectRS.getString("code"));
				state.setName(selectRS.getString("name"));
				state.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return state;
	}
}
