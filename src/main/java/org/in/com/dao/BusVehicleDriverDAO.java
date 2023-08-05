package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AttendantCategoryEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class BusVehicleDriverDAO {

	public List<BusVehicleDriverDTO> getAllDriver(AuthDTO authDTO) {
		List<BusVehicleDriverDTO> vehicleDriverList = new ArrayList<BusVehicleDriverDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, last_name, user_id, date_of_birth, blood_group, license_no, badge_number, license_expiry_date, qualification, employee_code, mobile_number, emergency_contact, aadhar_no, joining_date, last_assigned_date, assigned_trips_count, remarks, active_flag FROM bus_vehicle_driver WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleDriverDTO vehicleDriver = new BusVehicleDriverDTO();
				vehicleDriver.setCode(selectRS.getString("code"));
				vehicleDriver.setName(selectRS.getString("name"));
				vehicleDriver.setLastName(selectRS.getString("last_name"));

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("user_id"));
				vehicleDriver.setUser(user);

				vehicleDriver.setDateOfBirth(selectRS.getString("date_of_birth"));
				vehicleDriver.setBloodGroup(selectRS.getString("blood_group"));
				vehicleDriver.setLicenseNumber(selectRS.getString("license_no"));
				vehicleDriver.setBadgeNumber(selectRS.getString("badge_number"));
				vehicleDriver.setLicenseExpiryDate(selectRS.getString("license_expiry_date"));
				vehicleDriver.setQualification(selectRS.getString("qualification"));
				vehicleDriver.setEmployeeCode(selectRS.getString("employee_code"));
				vehicleDriver.setMobileNumber(selectRS.getString("mobile_number"));
				vehicleDriver.setEmergencyContactNumber(selectRS.getString("emergency_contact"));
				vehicleDriver.setAadharNo(selectRS.getString("aadhar_no"));
				vehicleDriver.setJoiningDate(selectRS.getString("joining_date"));
				vehicleDriver.setLastAssignedDate(DateUtil.getDateTime(selectRS.getString("last_assigned_date")));
				vehicleDriver.setAssignedTripsCount(selectRS.getInt("assigned_trips_count"));
				vehicleDriver.setRemarks(selectRS.getString("remarks"));
				vehicleDriver.setActiveFlag(selectRS.getInt("active_flag"));
				vehicleDriverList.add(vehicleDriver);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return vehicleDriverList;

	}

	public BusVehicleDriverDTO getDriver(AuthDTO authDTO, BusVehicleDriverDTO vehicleDriver) {
		BusVehicleDriverDTO busVehicleDriverDTO = new BusVehicleDriverDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(vehicleDriver.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, last_name, user_id, date_of_birth, blood_group, license_no, badge_number, license_expiry_date, qualification, employee_code, mobile_number, emergency_contact, aadhar_no, joining_date, last_assigned_date, remarks, active_flag FROM bus_vehicle_driver WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, vehicleDriver.getCode());
			}
			else if (vehicleDriver.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, last_name, user_id, date_of_birth, blood_group, license_no, badge_number, license_expiry_date, qualification, employee_code, mobile_number, emergency_contact, aadhar_no, joining_date, last_assigned_date, remarks, active_flag FROM bus_vehicle_driver WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, vehicleDriver.getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				busVehicleDriverDTO.setId(selectRS.getInt("id"));
				busVehicleDriverDTO.setCode(selectRS.getString("code"));
				busVehicleDriverDTO.setName(selectRS.getString("name"));
				busVehicleDriverDTO.setLastName(selectRS.getString("last_name"));

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("user_id"));
				busVehicleDriverDTO.setUser(user);
				vehicleDriver.setUser(user);

				busVehicleDriverDTO.setDateOfBirth(selectRS.getString("date_of_birth"));
				busVehicleDriverDTO.setBloodGroup(selectRS.getString("blood_group"));
				busVehicleDriverDTO.setLicenseNumber(selectRS.getString("license_no"));
				busVehicleDriverDTO.setBadgeNumber(selectRS.getString("badge_number"));
				busVehicleDriverDTO.setLicenseExpiryDate(selectRS.getString("license_expiry_date"));
				busVehicleDriverDTO.setQualification(selectRS.getString("qualification"));
				busVehicleDriverDTO.setEmployeeCode(selectRS.getString("employee_code"));
				busVehicleDriverDTO.setMobileNumber(selectRS.getString("mobile_number"));
				busVehicleDriverDTO.setEmergencyContactNumber(selectRS.getString("emergency_contact"));
				busVehicleDriverDTO.setAadharNo(selectRS.getString("aadhar_no"));
				busVehicleDriverDTO.setJoiningDate(selectRS.getString("joining_date"));
				busVehicleDriverDTO.setLastAssignedDate(DateUtil.getDateTime(selectRS.getString("last_assigned_date")));
				busVehicleDriverDTO.setRemarks(selectRS.getString("remarks"));
				busVehicleDriverDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return busVehicleDriverDTO;

	}

	public void getBusVehicleDriver(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriverDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(busVehicleDriverDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, last_name, user_id, date_of_birth, blood_group, license_no, badge_number, license_expiry_date, qualification, employee_code, mobile_number, emergency_contact, aadhar_no, joining_date, last_assigned_date, remarks, active_flag FROM bus_vehicle_driver WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, busVehicleDriverDTO.getCode());
			}
			else if (busVehicleDriverDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, last_name, user_id, date_of_birth, blood_group, license_no, badge_number, license_expiry_date, qualification, employee_code, mobile_number, emergency_contact, aadhar_no, joining_date, last_assigned_date, remarks, active_flag FROM bus_vehicle_driver WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, busVehicleDriverDTO.getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				busVehicleDriverDTO.setId(selectRS.getInt("id"));
				busVehicleDriverDTO.setCode(selectRS.getString("code"));
				busVehicleDriverDTO.setName(selectRS.getString("name"));
				busVehicleDriverDTO.setLastName(selectRS.getString("last_name"));

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("user_id"));
				busVehicleDriverDTO.setUser(user);

				busVehicleDriverDTO.setDateOfBirth(selectRS.getString("date_of_birth"));
				busVehicleDriverDTO.setBloodGroup(selectRS.getString("blood_group"));
				busVehicleDriverDTO.setLicenseNumber(selectRS.getString("license_no"));
				busVehicleDriverDTO.setBadgeNumber(selectRS.getString("badge_number"));
				busVehicleDriverDTO.setLicenseExpiryDate(selectRS.getString("license_expiry_date"));
				busVehicleDriverDTO.setQualification(selectRS.getString("qualification"));
				busVehicleDriverDTO.setEmployeeCode(selectRS.getString("employee_code"));
				busVehicleDriverDTO.setMobileNumber(selectRS.getString("mobile_number"));
				busVehicleDriverDTO.setEmergencyContactNumber(selectRS.getString("emergency_contact"));
				busVehicleDriverDTO.setAadharNo(selectRS.getString("aadhar_no"));
				busVehicleDriverDTO.setJoiningDate(selectRS.getString("joining_date"));
				busVehicleDriverDTO.setLastAssignedDate(DateUtil.getDateTime(selectRS.getString("last_assigned_date")));
				busVehicleDriverDTO.setRemarks(selectRS.getString("remarks"));
				busVehicleDriverDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateDriver(AuthDTO authDTO, BusVehicleDriverDTO driverDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_BUS_VEHICLE_DRIVER_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,0,?)}");
			termSt.setString(++pindex, driverDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, driverDTO.getName());
			termSt.setString(++pindex, driverDTO.getLastName());
			termSt.setString(++pindex, driverDTO.getDateOfBirth());
			termSt.setString(++pindex, driverDTO.getBloodGroup());
			termSt.setString(++pindex, driverDTO.getLicenseNumber());
			termSt.setString(++pindex, driverDTO.getBadgeNumber());
			termSt.setString(++pindex, driverDTO.getLicenseExpiryDate());
			termSt.setString(++pindex, driverDTO.getQualification());
			termSt.setString(++pindex, StringUtil.isNull(driverDTO.getEmployeeCode()) ? Text.NA : driverDTO.getEmployeeCode());
			termSt.setString(++pindex, driverDTO.getMobileNumber());
			termSt.setString(++pindex, driverDTO.getEmergencyContactNumber());
			termSt.setString(++pindex, StringUtil.isNull(StringUtils.deleteWhitespace(driverDTO.getAadharNo()), Text.NA));
			termSt.setString(++pindex, driverDTO.getJoiningDate());
			termSt.setString(++pindex, driverDTO.getRemarks());
			termSt.setInt(++pindex, driverDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				driverDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateAttendant(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_BUS_VEHICLE_ATTENDANT_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			termSt.setString(++pindex, attendantDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setInt(++pindex, attendantDTO.getCategory() != null ? attendantDTO.getCategory().getId() : 1);
			termSt.setString(++pindex, attendantDTO.getName());
			termSt.setInt(++pindex, attendantDTO.getAge());
			termSt.setString(++pindex, attendantDTO.getMobile());
			termSt.setString(++pindex, attendantDTO.getAlternateMobile());
			termSt.setString(++pindex, attendantDTO.getJoiningDate());
			termSt.setString(++pindex, attendantDTO.getAddress());
			termSt.setString(++pindex, attendantDTO.getRemarks());
			termSt.setInt(++pindex, attendantDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.setInt(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				attendantDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<BusVehicleAttendantDTO> getAllAttendant(AuthDTO authDTO, AttendantCategoryEM catagory) {
		List<BusVehicleAttendantDTO> vehicleAttendantList = new ArrayList<BusVehicleAttendantDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (catagory != null) {
				selectPS = connection.prepareStatement("SELECT code, name, category_id, age, mobile, alternate_mobile, joining_date, address, remarks, active_flag FROM bus_vehicle_attendant WHERE namespace_id = ? AND category_id = ? AND active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, catagory.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT code, name, category_id, age, mobile, alternate_mobile, joining_date, address, remarks, active_flag FROM bus_vehicle_attendant WHERE namespace_id = ? AND active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleAttendantDTO vehicleAttendant = new BusVehicleAttendantDTO();
				vehicleAttendant.setCode(selectRS.getString("code"));
				vehicleAttendant.setName(selectRS.getString("name"));
				vehicleAttendant.setAge(selectRS.getInt("age"));
				vehicleAttendant.setMobile(selectRS.getString("mobile"));
				vehicleAttendant.setAlternateMobile(selectRS.getString("alternate_mobile"));
				vehicleAttendant.setJoiningDate(selectRS.getString("joining_date"));
				vehicleAttendant.setAddress(selectRS.getString("address"));
				vehicleAttendant.setRemarks(selectRS.getString("remarks"));
				vehicleAttendant.setActiveFlag(selectRS.getInt("active_flag"));
				vehicleAttendant.setCategory(AttendantCategoryEM.getCategoryEM(selectRS.getInt("category_id")));
				vehicleAttendantList.add(vehicleAttendant);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return vehicleAttendantList;
	}

	public BusVehicleAttendantDTO getAttendant(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO) {
		BusVehicleAttendantDTO vehicleAttendantDTO = new BusVehicleAttendantDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(attendantDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, age, mobile, alternate_mobile, joining_date, address, remarks, active_flag FROM bus_vehicle_attendant WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, attendantDTO.getCode());
			}
			else if (attendantDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, age, mobile, alternate_mobile, joining_date, address, remarks, active_flag FROM bus_vehicle_attendant WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, attendantDTO.getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				vehicleAttendantDTO.setId(selectRS.getInt("id"));
				vehicleAttendantDTO.setCode(selectRS.getString("code"));
				vehicleAttendantDTO.setName(selectRS.getString("name"));
				vehicleAttendantDTO.setAge(selectRS.getInt("age"));
				vehicleAttendantDTO.setMobile(selectRS.getString("mobile"));
				vehicleAttendantDTO.setAlternateMobile(selectRS.getString("alternate_mobile"));
				vehicleAttendantDTO.setJoiningDate(selectRS.getString("joining_date"));
				vehicleAttendantDTO.setAddress(selectRS.getString("address"));
				vehicleAttendantDTO.setRemarks(selectRS.getString("remarks"));
				vehicleAttendantDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return vehicleAttendantDTO;
	}

	public void updateDriverUserMap(AuthDTO authDTO, BusVehicleDriverDTO driverDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bus_vehicle_driver, user SET bus_vehicle_driver.user_id = user.id WHERE user.namespace_id = ? AND user.namespace_id = bus_vehicle_driver.namespace_id AND bus_vehicle_driver.code = ? AND user.code = ?");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, driverDTO.getCode());
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
	}

	public void updateUser(AuthDTO authDTO, UserDTO userDTO, BusVehicleDriverDTO driverDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE bus_vehicle_driver SET user_id = ? WHERE namespace_id = ? AND code = ?");
			ps.setInt(1, userDTO.getId());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setString(3, driverDTO.getCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateLastAssignedDate(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE bus_vehicle_driver SET last_assigned_date = ? WHERE namespace_id = ? AND id = ?");
			selectPS.setString(1, busVehicleDriver.getLastAssignedDateToString());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, busVehicleDriver.getId());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UPDATE_FAIL, "Unable to update last assigned date in vehicle driver");
		}
	}

	public void updateAssignedTripsCount(AuthDTO authDTO, List<BusVehicleDriverDTO> busVehicleDrivers) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE bus_vehicle_driver SET assigned_trips_count = ? WHERE namespace_id = ? AND code = ?");
			for (BusVehicleDriverDTO busVehicleDriver : busVehicleDrivers) {
				selectPS.setInt(1, busVehicleDriver.getAssignedTripsCount());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setString(3, busVehicleDriver.getCode());
				selectPS.addBatch();
			}
			selectPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UPDATE_FAIL, "Unable to update trip assigned count in vehicle driver");
		}
	}
}