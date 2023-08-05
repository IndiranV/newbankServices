package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;
import net.sf.json.JSONObject;

public class BusBreakevenDAO {

	public List<BusBreakevenSettingsDTO> getAllBreakevenSettings(AuthDTO authDTO) {
		List<BusBreakevenSettingsDTO> list = new ArrayList<BusBreakevenSettingsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, bus_id, breakeven_details, active_flag FROM bus_breakeven_settings WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusBreakevenSettingsDTO breakevenSettingsDTO = new BusBreakevenSettingsDTO();
				breakevenSettingsDTO.setCode(selectRS.getString("code"));
				breakevenSettingsDTO.setName(selectRS.getString("name"));

				BusDTO bus = new BusDTO();
				bus.setId(selectRS.getInt("bus_id"));
				breakevenSettingsDTO.setBus(bus);

				breakevenSettingsDTO.setBreakevenDetails(JSONObject.fromObject(selectRS.getString("breakeven_details")));
				breakevenSettingsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(breakevenSettingsDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public BusBreakevenSettingsDTO getBreakevenSettingsDetails(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(breakevenSettingsDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, bus_id, breakeven_details, active_flag FROM bus_breakeven_settings WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, breakevenSettingsDTO.getCode());
			}
			else if (breakevenSettingsDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, bus_id, breakeven_details, active_flag FROM bus_breakeven_settings WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, breakevenSettingsDTO.getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				breakevenSettingsDTO.setId(selectRS.getInt("id"));
				breakevenSettingsDTO.setCode(selectRS.getString("code"));
				breakevenSettingsDTO.setName(selectRS.getString("name"));

				BusDTO bus = new BusDTO();
				bus.setId(selectRS.getInt("bus_id"));
				breakevenSettingsDTO.setBus(bus);

				breakevenSettingsDTO.setBreakevenDetails(JSONObject.fromObject(selectRS.getString("breakeven_details")));
				breakevenSettingsDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return breakevenSettingsDTO;
	}

	public BusBreakevenSettingsDTO updateBreakevenSettings(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_BUS_BREAKEVEN_SETTINGS_IUD(?,?,?,?,? ,?,?,?,?)}");
			callableStatement.setString(++pindex, breakevenSettingsDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, breakevenSettingsDTO.getName());
			callableStatement.setInt(++pindex, breakevenSettingsDTO.getBus().getId());
			callableStatement.setString(++pindex, breakevenSettingsDTO.getBreakeven());
			callableStatement.setInt(++pindex, breakevenSettingsDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				breakevenSettingsDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return breakevenSettingsDTO;
	}

	public int updateTripBreakeven(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettings) {
		int tripBreakevenId = 0;
		try {
			if (StringUtil.isNotNull(breakevenSettings.getBreakevenKey())) {
				@Cleanup
				Connection connection = ConnectDAO.getConnection();
				int pindex = 0;
				@Cleanup
				CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TRIP_BREAKEVEN_IUD(?,?,?,?,? ,?,?,?)}");
				callableStatement.setString(++pindex, breakevenSettings.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, breakevenSettings.getBreakevenKey());
				callableStatement.setString(++pindex, breakevenSettings.getBreakeven());
				callableStatement.setInt(++pindex, breakevenSettings.getActiveFlag());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					breakevenSettings.setId(callableStatement.getInt("pitId"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tripBreakevenId;
	}

}
