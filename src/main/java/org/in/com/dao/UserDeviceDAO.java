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
import org.in.com.dto.UserDeviceDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ServiceException;
import org.springframework.stereotype.Repository;

@Repository
public class UserDeviceDAO {

	public List<UserDeviceDTO> get(AuthDTO authDTO, DeviceMediumEM deviceMedium) {
		List<UserDeviceDTO> userDeviceDTOList = new ArrayList<UserDeviceDTO>();

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT device_code, unique_code, version, device_medium_id FROM user_device_register WHERE namespace_id = ? AND device_medium_id = ? AND active_flag = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, deviceMedium.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDeviceDTO dto = new UserDeviceDTO();
				dto.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium_id")));
				dto.setVersion(selectRS.getString("version"));
				dto.setDeviceCode(selectRS.getString("device_code"));
				dto.setUniqueCode(selectRS.getString("uniqueCode"));
				userDeviceDTOList.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return userDeviceDTOList;
	}

	public boolean registerUserDevice(AuthDTO authDTO, UserDeviceDTO userDeviceDTO) {
		boolean result = false;
		int pindex = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			CallableStatement termSt = connection.prepareCall("{call EZEE_SP_USER_DEVICE_IUD(?,?,?,?,? ,1 ,?,1,?)}");
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setString(++pindex, userDeviceDTO.getDeviceCode());
			termSt.setString(++pindex, userDeviceDTO.getUniqueCode());
			termSt.setInt(++pindex, userDeviceDTO.getDeviceMedium().getId());
			termSt.setString(++pindex, userDeviceDTO.getVersion());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				result = true;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return result;

	}

}
