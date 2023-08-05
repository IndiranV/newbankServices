package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class TabletDAO extends BaseDAO {

	public void saveTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_TABLET_IUD(?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, tabletDTO.getCode());
			callableStatement.setString(++pindex, tabletDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, tabletDTO.getUser().getId());
			callableStatement.setString(++pindex, StringUtil.isNull(tabletDTO.getMobileNumber(), "00"));
			callableStatement.setString(++pindex, tabletDTO.getModel());
			callableStatement.setString(++pindex, StringUtil.substring(tabletDTO.getVersion(), 15));
			callableStatement.setString(++pindex, StringUtil.substring(tabletDTO.getRemarks(), 200));
			callableStatement.setInt(++pindex, tabletDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public Collection<TabletDTO> getAll(AuthDTO authDTO) {
		Collection<TabletDTO> list = new ArrayList<TabletDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, mobile_number, mobile_verify_flag, bus_vehicle_id, model, version, remarks, active_flag FROM namespace_tablet WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TabletDTO tabletDTO = new TabletDTO();
				tabletDTO.setCode(selectRS.getString("code"));
				tabletDTO.setName(selectRS.getString("name"));
				tabletDTO.setMobileNumber(selectRS.getString("mobile_number"));
				tabletDTO.setMobileVerifyFlag(selectRS.getInt("mobile_verify_flag"));
				tabletDTO.setModel(selectRS.getString("model"));
				tabletDTO.setVersion(selectRS.getString("version"));
				tabletDTO.setRemarks(selectRS.getString("remarks"));
				tabletDTO.setActiveFlag(selectRS.getInt("active_flag"));

				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				busVehicleDTO.setId(selectRS.getInt("bus_vehicle_id"));
				tabletDTO.setBusVehicle(busVehicleDTO);
				list.add(tabletDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void tabletVehicleMapping(AuthDTO authDTO, TabletDTO tabletDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE namespace_tablet SET bus_vehicle_id = ?, updated_by = ? WHERE code = ? AND namespace_id = ? AND active_flag = 1;");
			ps.setInt(1, tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getId() : 0);
			ps.setInt(2, authDTO.getUser().getId());
			ps.setString(3, tabletDTO.getCode());
			ps.setInt(4, authDTO.getNamespace().getId());
			ps.executeUpdate();
			// Add Audit Log
			addAuditLog(connection, authDTO, tabletDTO.getCode(), "namespace_tablet", tabletDTO.getBusVehicle() != null ? String.valueOf(tabletDTO.getBusVehicle().getId()) : "NA", tabletDTO.toString());

			System.out.println("Tablet - " + tabletDTO.getCode() + " - Vehicle - " + String.valueOf(tabletDTO.getBusVehicle().getId()) + " - Mapping  Updated By" + authDTO.getNamespace().getId() + "-" + authDTO.getUser().getId());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public TabletDTO getTablet(AuthDTO authDTO, String tabletCode) {
		TabletDTO tabletDTO = new TabletDTO();

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, user_id, mobile_number, bus_vehicle_id, model, version, remarks, active_flag FROM namespace_tablet WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, tabletCode);
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				tabletDTO.setId(selectRS.getInt("id"));
				tabletDTO.setCode(selectRS.getString("code"));
				tabletDTO.setName(selectRS.getString("name"));
				tabletDTO.setActiveFlag(selectRS.getInt("active_flag"));
				tabletDTO.setMobileNumber(selectRS.getString("mobile_number"));
				tabletDTO.setModel(selectRS.getString("model"));
				tabletDTO.setVersion(selectRS.getString("version"));
				tabletDTO.setRemarks(selectRS.getString("remarks"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("user_id"));
				tabletDTO.setUser(userDTO);

				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				busVehicleDTO.setId(selectRS.getInt("bus_vehicle_id"));
				tabletDTO.setBusVehicle(busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tabletDTO;
	}

	public List<TabletDTO> getTablet(AuthDTO authDTO, BusVehicleDTO busVehicle) {
		List<TabletDTO> tabletList = new ArrayList<TabletDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, mobile_number, mobile_verify_flag, user_id, bus_vehicle_id, model, version, remarks, active_flag FROM namespace_tablet WHERE namespace_id = ? AND bus_vehicle_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, busVehicle.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TabletDTO tabletDTO = new TabletDTO();
				tabletDTO.setId(selectRS.getInt("id"));
				tabletDTO.setCode(selectRS.getString("code"));
				tabletDTO.setName(selectRS.getString("name"));
				tabletDTO.setMobileNumber(selectRS.getString("mobile_number"));
				tabletDTO.setMobileVerifyFlag(selectRS.getInt("mobile_verify_flag"));
				tabletDTO.setModel(selectRS.getString("model"));
				tabletDTO.setVersion(selectRS.getString("version"));
				tabletDTO.setRemarks(selectRS.getString("remarks"));
				tabletDTO.setActiveFlag(selectRS.getInt("active_flag"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("user_id"));
				tabletDTO.setUser(userDTO);
				tabletList.add(tabletDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tabletList;

	}

	public void deleteTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE namespace_tablet SET active_flag = 2  WHERE namespace_id = ? AND code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tabletDTO.getCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public void updateTablet(AuthDTO authDTO, TabletDTO tabletDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE namespace_tablet SET name = ?, mobile_number = ?, model = ?, version = ?, remarks = ? WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			ps.setString(1, tabletDTO.getName());
			ps.setString(2, tabletDTO.getMobileNumber());
			ps.setString(3, tabletDTO.getModel());
			ps.setString(4, StringUtil.substring(tabletDTO.getVersion(), 15));
			ps.setString(5, StringUtil.substring(tabletDTO.getRemarks(), 200));
			ps.setInt(6, authDTO.getNamespace().getId());
			ps.setString(7, tabletDTO.getCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public void releaseUserFromTablet(AuthDTO authDTO, UserDTO userDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE namespace_tablet SET user_id = 0, active_flag = 0  WHERE namespace_id = ? AND user_id = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, authDTO.getUser().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public void verifyMobile(AuthDTO authDTO, TabletDTO tabletDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE namespace_tablet SET mobile_verify_flag = 1 WHERE namespace_id = ? AND code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tabletDTO.getCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}
}
