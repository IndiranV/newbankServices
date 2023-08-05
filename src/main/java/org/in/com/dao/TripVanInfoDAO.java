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
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripVanExceptionDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class TripVanInfoDAO {

	public void updateTripVanInfo(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		try {
			int pindex = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			String code = authDTO.getNamespace().getId() + "N" + tripVanInfoDTO.getVanPickup().getId() + "V" + DateUtil.getCompressDate(tripVanInfoDTO.getTripDate()) + "D";
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TRIP_VAN_INFO_IUD(?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, code);
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, tripVanInfoDTO.getVehicle().getId());
			callableStatement.setInt(++pindex, tripVanInfoDTO.getDriver().getId());
			callableStatement.setString(++pindex, DateUtil.convertDate(tripVanInfoDTO.getTripDate()));
			callableStatement.setInt(++pindex, tripVanInfoDTO.getVanPickup().getId());
			callableStatement.setString(++pindex, tripVanInfoDTO.getMobileNumber());
			callableStatement.setString(++pindex, Text.NA);
			callableStatement.setInt(++pindex, tripVanInfoDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			System.out.println(callableStatement.getInt("pitRowCount"));
			if (callableStatement.getInt("pitRowCount") > 0) {
				tripVanInfoDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public TripVanInfoDTO getTripVanInfo(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDTO tripVanInfo = new TripVanInfoDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (tripVanInfoDTO.getVanPickup().getId() != 0) {
				selectPS = connection.prepareStatement("SELECT code, vehicle_id, trip_date, driver_id, van_pickup_id, mobile_number, notification_status, active_flag FROM trip_van_info WHERE namespace_id = ? AND van_pickup_id = ? AND trip_date = ? AND driver_id > 0 AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, tripVanInfoDTO.getVanPickup().getId());
				selectPS.setString(3, DateUtil.convertDate(tripVanInfoDTO.getTripDate()));
			}
			else {
				selectPS = connection.prepareStatement("SELECT info.code, vehicle_id, trip_date, driver_id, van_pickup_id, mobile_number, notification_status, info.active_flag FROM trip_van_info info, bus_vehicle_pickup_van pick WHERE pick.namespace_id = ? AND info.namespace_id = pick.namespace_id AND pick.code = ? AND trip_date = ? AND info.van_pickup_id = pick.id AND info.driver_id > 0 AND pick.active_flag = 1 AND info.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, tripVanInfoDTO.getVanPickup().getCode());
				selectPS.setString(3, DateUtil.convertDate(tripVanInfoDTO.getTripDate()));
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				tripVanInfo.setCode(selectRS.getString("code"));

				BusVehicleDriverDTO driverDTO = new BusVehicleDriverDTO();
				driverDTO.setId(selectRS.getInt("driver_id"));
				tripVanInfo.setDriver(driverDTO);

				tripVanInfo.setTripDate(DateUtil.getDateTime(selectRS.getString("trip_date")));

				BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
				vanPickupDTO.setId(selectRS.getInt("van_pickup_id"));
				tripVanInfo.setVanPickup(vanPickupDTO);

				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				busVehicleDTO.setId(selectRS.getInt("vehicle_id"));
				tripVanInfo.setVehicle(busVehicleDTO);

				tripVanInfo.setMobileNumber(selectRS.getString("mobile_number"));
				tripVanInfo.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getString("notification_status")));
				tripVanInfo.setActiveFlag(selectRS.getInt("active_flag"));
			}
			
			TripVanExceptionDTO traipVanException = getTripVanException(connection, authDTO, tripVanInfoDTO);
			tripVanInfo.setTripVanException(traipVanException);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripVanInfo;
	}
	
	public TripVanExceptionDTO getTripVanException(Connection connection, AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanExceptionDTO tripVanException = null;
		try {
			@Cleanup
			PreparedStatement selectPS = null;
			if (tripVanInfoDTO.getVanPickup().getId() != 0) {
				selectPS = connection.prepareStatement("SELECT code, trip_date, exception_schedule_code, van_pickup_id, active_flag, updated_by, updated_at FROM trip_van_info WHERE namespace_id = ? AND van_pickup_id = ? AND trip_date = ? AND (driver_id = -1 OR (exception_schedule_code != 'NA' AND exception_schedule_code != '')) AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, tripVanInfoDTO.getVanPickup().getId());
				selectPS.setString(3, DateUtil.convertDate(tripVanInfoDTO.getTripDate()));
			}
			else {
				selectPS = connection.prepareStatement("SELECT info.code, trip_date, van_pickup_id, info.exception_schedule_code, info.active_flag, info.updated_by, info.updated_at FROM trip_van_info info, bus_vehicle_pickup_van pick WHERE pick.namespace_id = ? AND info.namespace_id = pick.namespace_id AND pick.code = ? AND trip_date = ? AND info.van_pickup_id = pick.id AND (info.driver_id = -1 OR (info.exception_schedule_code != 'NA' AND info.exception_schedule_code != '')) AND pick.active_flag = 1 AND info.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, tripVanInfoDTO.getVanPickup().getCode());
				selectPS.setString(3, DateUtil.convertDate(tripVanInfoDTO.getTripDate()));
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				tripVanException =  new TripVanExceptionDTO();
				tripVanException.setCode(selectRS.getString("code"));
				tripVanException.setTripDate(DateUtil.getDateTime(selectRS.getString("trip_date")));
				
				BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
				vanPickupDTO.setId(selectRS.getInt("van_pickup_id"));
				tripVanException.setVanPickup(vanPickupDTO);
				
				tripVanException.setSchedules(convertScheduleList(selectRS.getString("exception_schedule_code")));
				
				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(selectRS.getInt("updated_by"));
				
				AuditDTO audit = new AuditDTO();
				audit.setUser(updatedBy);
				audit.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("updated_at")).format("YYYY-MM-DD hh:mm:ss"));
				tripVanException.setAudit(audit);
				tripVanException.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripVanException;
	}

	public TripVanInfoDTO getTripVanInfoByCode(AuthDTO authDTO, TripVanInfoDTO tripVanInfo) {
		TripVanInfoDTO tripVanInfoDTO = new TripVanInfoDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, mobile_number, vehicle_id, driver_id, van_pickup_id, notification_status FROM trip_van_info WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, tripVanInfo.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				tripVanInfoDTO.setCode(selectRS.getString("code"));
				tripVanInfoDTO.setMobileNumber(selectRS.getString("mobile_number"));

				BusVehicleDTO vehicleDTO = new BusVehicleDTO();
				vehicleDTO.setId(selectRS.getInt("vehicle_id"));
				tripVanInfoDTO.setVehicle(vehicleDTO);

				BusVehicleDriverDTO driverDTO = new BusVehicleDriverDTO();
				driverDTO.setId(selectRS.getInt("driver_id"));
				tripVanInfoDTO.setDriver(driverDTO);

				BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
				vanPickupDTO.setId(selectRS.getInt("van_pickup_id"));
				tripVanInfoDTO.setVanPickup(vanPickupDTO);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tripVanInfoDTO;
	}

	public void updateNotificationStatus(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_van_info SET notification_status = ? WHERE code = ? AND active_flag = 1");
			ps.setString(1, tripVanInfoDTO.getNotificationType().getCode());
			ps.setString(2, tripVanInfoDTO.getCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateTripVanException(AuthDTO authDTO, TripVanExceptionDTO tripVanExceptionDTO) {
		try {
			int pindex = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			
			StringBuilder scheduleCodes = new StringBuilder();
			if (tripVanExceptionDTO.getActiveFlag() != 1 || (tripVanExceptionDTO.getSchedules() != null && tripVanExceptionDTO.getSchedules().isEmpty())) {
				scheduleCodes.append("NA");
			}
			else if (tripVanExceptionDTO.getSchedules() != null && !tripVanExceptionDTO.getSchedules().isEmpty()){
				for (ScheduleDTO schedule : tripVanExceptionDTO.getSchedules()) {
					scheduleCodes.append(",");
					scheduleCodes.append(schedule.getCode());
				}
			}

			String code = authDTO.getNamespace().getId() + "N" + tripVanExceptionDTO.getVanPickup().getId() + "V" + DateUtil.getCompressDate(tripVanExceptionDTO.getTripDate()) + "D";
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TRIP_VAN_INFO_IUD(?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, code);
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.setInt(++pindex, -1);
			callableStatement.setString(++pindex, DateUtil.convertDate(tripVanExceptionDTO.getTripDate()));
			callableStatement.setInt(++pindex, tripVanExceptionDTO.getVanPickup().getId());
			callableStatement.setString(++pindex, Text.EMPTY);
			callableStatement.setString(++pindex, scheduleCodes.toString());
			callableStatement.setInt(++pindex, tripVanExceptionDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				tripVanExceptionDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
	
	private List<ScheduleDTO> convertScheduleList(String scheduleCodes) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		if (StringUtil.isNotNull(scheduleCodes)) {
			String[] scheduleCode = scheduleCodes.split(Text.COMMA);

			for (String schedule : scheduleCode) {
				if (StringUtil.isNull(schedule)) {
					continue;
				}

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(schedule);
				scheduleList.add(scheduleDTO);
			}
		}
		return scheduleList;
	}

}
