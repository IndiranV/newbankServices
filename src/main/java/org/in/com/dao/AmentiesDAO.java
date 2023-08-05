package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class AmentiesDAO {

	/**
	 * Here we are getting all the amenities
	 * 
	 * @param namespaceDTO
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AmenitiesDTO> getAllAmenties(AuthDTO authDTO) {

		List<AmenitiesDTO> list = new ArrayList<AmenitiesDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,active_flag FROM amenities where active_flag < 2");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AmenitiesDTO amentiesDTO = new AmenitiesDTO();
				amentiesDTO.setCode(selectRS.getString("code"));
				amentiesDTO.setName(selectRS.getString("name"));
				amentiesDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(amentiesDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	/**
	 * Here we are updating the Amenties based on the code
	 * 
	 * @param namespaceDTO
	 * @param amentiesDTO
	 * @return
	 */

	public AmenitiesDTO getAmentiesUpdate(AuthDTO authDTO, AmenitiesDTO amentiesDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_AMENITIES_IUD( ?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, amentiesDTO.getCode());
			callableStatement.setString(++pindex, amentiesDTO.getName());
			callableStatement.setInt(++pindex, amentiesDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				amentiesDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return amentiesDTO;
	}

	public List<AmenitiesDTO> getAllforZoneSync(String syncDate) {

		List<AmenitiesDTO> list = new ArrayList<AmenitiesDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,active_flag FROM amenities where DATE(updated_at) >= ?");
			selectPS.setString(1, syncDate);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AmenitiesDTO amentiesDTO = new AmenitiesDTO();
				amentiesDTO.setCode(selectRS.getString("code"));
				amentiesDTO.setName(selectRS.getString("name"));
				amentiesDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(amentiesDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public String getZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DATE_FORMAT(MAX(updated_at), '%Y-%m-%d %H:%i:%s') as zoneSyncDate FROM amenities");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2014-02-12 03:49:03";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	public List<AmenitiesDTO> updateZoneSync(AuthDTO authDTO, List<AmenitiesDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_AMENITIES_ZONESYNC( ?,?,?,?,?)}");
			for (AmenitiesDTO amentiesDTO : list) {
				int pindex = 0;
				callableStatement.setString(++pindex, amentiesDTO.getCode());
				callableStatement.setString(++pindex, amentiesDTO.getName());
				callableStatement.setInt(++pindex, amentiesDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}
}
