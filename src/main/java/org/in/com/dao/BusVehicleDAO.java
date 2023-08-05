package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.VehicleTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class BusVehicleDAO {

	public List<BusVehicleDTO> getAllBusVehicles(AuthDTO authDTO) {
		List<BusVehicleDTO> list = new ArrayList<BusVehicleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT bv.id, bv.code,bv.name,bv.bus_id,bv.vehicle_type,bv.registation_date,bv.registation_number,bv.lic_number,bv.gps_device_code,bv.gps_device_vendor_id, bv.mobile_number, bv.last_assigned_date, bv.active_flag  FROM bus_vehicle bv  WHERE bv.namespace_id = ? AND  bv.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleDTO dto = new BusVehicleDTO();
				dto.setId(selectRS.getInt("bv.id"));
				dto.setCode(selectRS.getString("bv.code"));
				dto.setName(selectRS.getString("bv.name"));

				BusDTO busDTO = new BusDTO();
				busDTO.setId(selectRS.getInt("bv.bus_id"));
				dto.setBus(busDTO);

				dto.setVehicleType(VehicleTypeEM.getVehicleTypeEM(selectRS.getInt("bv.vehicle_type")));
				dto.setRegistrationDate(selectRS.getString("bv.registation_date"));
				dto.setRegistationNumber(selectRS.getString("bv.registation_number"));
				dto.setLicNumber(selectRS.getString("bv.lic_number"));
				dto.setGpsDeviceCode(selectRS.getString("bv.gps_device_code"));
				dto.setDeviceVendor(GPSDeviceVendorEM.getGPSDeviceVendorEM(selectRS.getInt("gps_device_vendor_id")));
				dto.setMobileNumber(selectRS.getString("bv.mobile_number"));
				dto.setLastAssignedDate(DateUtil.getDateTime(selectRS.getString("bv.last_assigned_date")));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public BusVehicleDTO getBusVehicles(AuthDTO authDTO, BusVehicleDTO vehicleDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (vehicleDTO != null && vehicleDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT bv.id, bv.code,bv.name,bv.bus_id,bv.vehicle_type,bv.registation_date,bv.registation_number,bv.lic_number,bv.gps_device_code, bv.gps_device_vendor_id, bv.mobile_number, bv.last_assigned_date, bv.active_flag  FROM bus_vehicle bv  WHERE bv.namespace_id=? AND id = ? AND  bv.active_flag  = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, vehicleDTO.getId());
			}
			else if (vehicleDTO != null && StringUtil.isNotNull(vehicleDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT bv.id, bv.code,bv.name,bv.bus_id,bv.vehicle_type,bv.registation_date,bv.registation_number,bv.lic_number,bv.gps_device_code,bv.gps_device_vendor_id, bv.mobile_number, bv.last_assigned_date, bv.active_flag  FROM bus_vehicle bv  WHERE bv.namespace_id=? AND code = ? AND  bv.active_flag  = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, vehicleDTO.getCode());
			}
			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					vehicleDTO.setId(selectRS.getInt("bv.id"));
					vehicleDTO.setCode(selectRS.getString("bv.code"));
					vehicleDTO.setName(selectRS.getString("bv.name"));

					BusDTO busDTO = new BusDTO();
					busDTO.setId(selectRS.getInt("bv.bus_id"));
					vehicleDTO.setBus(busDTO);

					vehicleDTO.setVehicleType(VehicleTypeEM.getVehicleTypeEM(selectRS.getInt("bv.vehicle_type")));
					vehicleDTO.setRegistrationDate(selectRS.getString("bv.registation_date"));
					vehicleDTO.setRegistationNumber(selectRS.getString("bv.registation_number"));
					vehicleDTO.setLicNumber(selectRS.getString("bv.lic_number"));
					vehicleDTO.setGpsDeviceCode(selectRS.getString("bv.gps_device_code"));
					vehicleDTO.setMobileNumber(selectRS.getString("bv.mobile_number"));
					vehicleDTO.setLastAssignedDate(DateUtil.getDateTime(selectRS.getString("bv.last_assigned_date")));
					vehicleDTO.setActiveFlag(selectRS.getInt("active_flag"));
					vehicleDTO.setDeviceVendor(GPSDeviceVendorEM.getGPSDeviceVendorEM(selectRS.getInt("gps_device_vendor_id")));
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return vehicleDTO;
	}

	public void updateBusVehicle(AuthDTO authDTO, BusVehicleDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_BUS_VEHICLE_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setString(++pindex, dto.getBus().getCode());
			callableStatement.setInt(++pindex, dto.getVehicleType().getId());
			callableStatement.setString(++pindex, dto.getRegistrationDate());
			callableStatement.setString(++pindex, dto.getRegistationNumber());
			callableStatement.setString(++pindex, dto.getLicNumber());
			callableStatement.setString(++pindex, dto.getGpsDeviceCode());
			callableStatement.setInt(++pindex, dto.getDeviceVendor().getId());
			callableStatement.setString(++pindex, dto.getMobileNumber());
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

	public void updateLastAssignedDate(AuthDTO authDTO, BusVehicleDTO busVehicle) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE bus_vehicle SET last_assigned_date = ? WHERE namespace_id = ? AND code = ?");
			selectPS.setString(1, busVehicle.getLastAssignedDateToString());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, busVehicle.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UPDATE_FAIL, "Unable to update last assigned date in vehicle");
		}
	}
}
