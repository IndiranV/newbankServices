package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserDetailsDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class UserDetailsDAO {

	public UserDetailsDTO updateUserDetails(AuthDTO authDTO, UserDetailsDTO userDetailsDTO) {
		UserDetailsDTO userDetails = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_USER_DETAILS_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, StringUtil.isNotNull(userDetailsDTO.getCode()) ? userDetailsDTO.getCode() : Text.NA);
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, userDetailsDTO.getUser() != null ? userDetailsDTO.getUser().getCode() : Text.NA);
			callableStatement.setInt(++pindex, userDetailsDTO.getStation() != null ? userDetailsDTO.getStation().getId() : Numeric.ZERO_INT);
			callableStatement.setInt(++pindex, userDetailsDTO.getState() != null ? userDetailsDTO.getState().getId() : Numeric.ZERO_INT);
			callableStatement.setString(++pindex, userDetailsDTO.getStationArea() != null ? userDetailsDTO.getStationArea().getCode() : Text.NA);
			callableStatement.setString(++pindex, userDetailsDTO.getAddress1());
			callableStatement.setString(++pindex, userDetailsDTO.getAddress2());
			callableStatement.setString(++pindex, userDetailsDTO.getLandmark());
			callableStatement.setString(++pindex, userDetailsDTO.getPincode());
			callableStatement.setInt(++pindex, userDetailsDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				userDetails = new UserDetailsDTO();
				userDetails.setCode(callableStatement.getString("pcrCode"));
				userDetails.setActiveFlag(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userDetails;
	}

	public UserDetailsDTO getUserDetails(AuthDTO authDTO, UserDTO userDTO) {
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ud.code, usr.code, usr.first_name, station_id, state_id, station_area_id, address1, address2, land_mark, pin_code, ud.active_flag FROM user_details ud, user usr WHERE ud.namespace_id = ? AND usr.namespace_id = ? AND usr.code = ? AND ud.user_id = usr.id AND ud.active_flag = 1 AND usr.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, userDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userDetailsDTO.setCode(selectRS.getString("ud.code"));
				userDetailsDTO.setAddress1(selectRS.getString("address1"));
				userDetailsDTO.setAddress2(selectRS.getString("address2"));
				userDetailsDTO.setLandmark(selectRS.getString("land_mark"));
				userDetailsDTO.setPincode(selectRS.getString("pin_code"));
				userDetailsDTO.setActiveFlag(selectRS.getInt("ud.active_flag"));

				userDTO = new UserDTO();
				userDTO.setCode(selectRS.getString("usr.code"));
				userDTO.setName(selectRS.getString("usr.first_name"));
				userDetailsDTO.setUser(userDTO);

				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("station_id"));
				userDetailsDTO.setStation(stationDTO);

				StateDTO stateDTO = new StateDTO();
				stateDTO.setId(selectRS.getInt("state_id"));
				userDetailsDTO.setState(stateDTO);

				StationAreaDTO stationAreaDTO = new StationAreaDTO();
				stationAreaDTO.setId(selectRS.getInt("station_area_id"));
				userDetailsDTO.setStationArea(stationAreaDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userDetailsDTO;
	}

}
