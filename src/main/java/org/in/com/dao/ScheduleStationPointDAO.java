package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class ScheduleStationPointDAO {

	public List<ScheduleStationPointDTO> get(AuthDTO authDTO, ScheduleStationPointDTO scheduleStationPointDTO) {
		List<ScheduleStationPointDTO> overrideList = new ArrayList<ScheduleStationPointDTO>();
		Map<Integer, ScheduleStationPointDTO> scheduleMap = new HashMap<Integer, ScheduleStationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT   point.id,  point.code,  point.station_id,  point.station_point_id, point.bus_vehicle_pickup_van_id, ress.code, sche.code, ress.name,ress.address,ress.landmark,ress.contact_number,ress.amenities_code,ress.latitude,ress.longitude, point.minitues,  point.credit_debit_flag,  point.active_from,  point.active_to,  point.day_of_week,  point.lookup_id, point.boarding_dropping_flag, point.fare, point.mobile_number, point.amenities_code, point.address, point.active_flag FROM schedule_station_point point,  schedule sche,  station_point ress WHERE sche.id = point.schedule_id    AND sche.namespace_id = point.namespace_id    AND sche.namespace_id = ress.namespace_id    AND ress.id = point.station_point_id    AND sche.namespace_id = ?    AND sche.code = ?    AND  sche.active_flag < 2    AND point.active_flag < 2  ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleStationPointDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStationPointDTO dto = new ScheduleStationPointDTO();
				dto.setId(selectRS.getInt("point.id"));
				dto.setCode(selectRS.getString("point.code"));
				dto.setBoardingDroppingFlag(selectRS.getString("point.boarding_dropping_flag"));
				dto.setActiveFlag(selectRS.getInt("point.active_flag"));
				dto.setLookupCode(selectRS.getString("point.lookup_id"));
				dto.setActiveFrom(selectRS.getString("point.active_from"));
				dto.setActiveTo(selectRS.getString("point.active_to"));
				dto.setDayOfWeek(selectRS.getString("point.day_of_week"));
				StationDTO stationDTO = new StationDTO();
				StationPointDTO pointDTO = new StationPointDTO();
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				stationDTO.setId(selectRS.getInt("point.station_id"));
				pointDTO.setId(selectRS.getInt("point.station_point_id"));
				pointDTO.setCode(selectRS.getString("ress.code"));
				pointDTO.setName(selectRS.getString("ress.name"));
				pointDTO.setAddress(selectRS.getString("ress.address"));
				pointDTO.setLandmark(selectRS.getString("ress.landmark"));
				pointDTO.setNumber(selectRS.getString("ress.contact_number"));
				pointDTO.setLatitude(selectRS.getString("ress.latitude"));
				pointDTO.setLongitude(selectRS.getString("ress.longitude"));
				pointDTO.setAmenities(selectRS.getString("ress.amenities_code"));
				dto.setSchedule(scheduleDTO);
				dto.setStationPoint(pointDTO);

				BusVehicleVanPickupDTO vanRouteDTO = new BusVehicleVanPickupDTO();
				vanRouteDTO.setId(selectRS.getInt("point.bus_vehicle_pickup_van_id"));
				dto.setBusVehicleVanPickup(vanRouteDTO);

				dto.setStation(stationDTO);
				dto.setCreditDebitFlag(selectRS.getString("point.credit_debit_flag"));
				dto.setMinitues(selectRS.getInt("point.minitues"));
				dto.setFare(selectRS.getBigDecimal("point.fare"));
				dto.setMobileNumber(selectRS.getString("point.mobile_number"));
				dto.setAddress(selectRS.getString("point.address"));
				dto.setAmenities(selectRS.getString("point.amenities_code"));

				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleStationPointDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleStationPointDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ScheduleStationPointDTO>(scheduleMap.values());
	}

	public ScheduleStationPointDTO getIUD(AuthDTO authDTO, ScheduleStationPointDTO stationPointDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_STATION_POINT_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?)}");
			for (ScheduleStationPointDTO pointDTO : stationPointDTO.getList()) {
				pindex = 0;
				callableStatement.setString(++pindex, pointDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, pointDTO.getSchedule().getCode());
				callableStatement.setInt(++pindex, pointDTO.getStation().getId());
				callableStatement.setString(++pindex, pointDTO.getStationPoint().getCode());
				callableStatement.setString(++pindex, pointDTO.getBusVehicleVanPickup() != null ? pointDTO.getBusVehicleVanPickup().getCode() : "NA");
				callableStatement.setInt(++pindex, pointDTO.getMinitues());
				callableStatement.setString(++pindex, pointDTO.getCreditDebitFlag());
				callableStatement.setString(++pindex, pointDTO.getActiveFrom() != null ? pointDTO.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, pointDTO.getActiveTo() != null ? pointDTO.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, pointDTO.getDayOfWeek());
				callableStatement.setString(++pindex, pointDTO.getLookupCode());
				callableStatement.setString(++pindex, pointDTO.getBoardingDroppingFlag());
				callableStatement.setBigDecimal(++pindex, pointDTO.getFare());
				callableStatement.setString(++pindex, pointDTO.getMobileNumber());
				callableStatement.setString(++pindex, pointDTO.getAmenities());
				callableStatement.setString(++pindex, pointDTO.getAddress());
				callableStatement.setInt(++pindex, pointDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					stationPointDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					stationPointDTO.setCode(callableStatement.getString("pcrCode"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return stationPointDTO;
	}

	// get All Trips
	public List<ScheduleStationPointDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleStationPointDTO> overrideList = new ArrayList<ScheduleStationPointDTO>();
		Map<Integer, ScheduleStationPointDTO> scheduleMap = new HashMap<Integer, ScheduleStationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT point.id,  point.code,  point.station_id,  point.station_point_id, point.bus_vehicle_pickup_van_id, point.minitues,  point.credit_debit_flag,  point.active_from,  point.active_to,  point.day_of_week,  point.lookup_id, point.boarding_dropping_flag, point.fare, point.mobile_number, point.amenities_code, point.address, point.active_flag  FROM schedule_station_point point   WHERE  point.namespace_id = ?  AND  point.schedule_id = ? AND point.active_flag  = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStationPointDTO dto = new ScheduleStationPointDTO();
				dto.setId(selectRS.getInt("point.id"));
				dto.setCode(selectRS.getString("point.code"));
				dto.setBoardingDroppingFlag(selectRS.getString("point.boarding_dropping_flag"));
				dto.setActiveFlag(selectRS.getInt("point.active_flag"));
				dto.setLookupCode(selectRS.getString("point.lookup_id"));
				dto.setActiveFrom(selectRS.getString("point.active_from"));
				dto.setActiveTo(selectRS.getString("point.active_to"));
				dto.setDayOfWeek(selectRS.getString("point.day_of_week"));
				StationDTO stationDTO = new StationDTO();
				StationPointDTO pointDTO = new StationPointDTO();
				stationDTO.setId(selectRS.getInt("point.station_id"));
				pointDTO.setId(selectRS.getInt("point.station_point_id"));
				dto.setSchedule(scheduleDTO);
				dto.setStationPoint(pointDTO);
				dto.setStation(stationDTO);

				BusVehicleVanPickupDTO vanRouteDTO = new BusVehicleVanPickupDTO();
				vanRouteDTO.setId(selectRS.getInt("point.bus_vehicle_pickup_van_id"));
				dto.setBusVehicleVanPickup(vanRouteDTO);

				dto.setCreditDebitFlag(selectRS.getString("point.credit_debit_flag"));
				dto.setMinitues(selectRS.getInt("point.minitues"));
				dto.setFare(selectRS.getBigDecimal("point.fare"));
				dto.setMobileNumber(selectRS.getString("point.mobile_number"));
				dto.setAddress(selectRS.getString("point.address"));
				dto.setAmenities(selectRS.getString("point.amenities_code"));

				if (StringUtil.isNull(dto.getActiveFrom())) {
					dto.setActiveFrom(scheduleDTO.getActiveFrom());
					dto.setActiveTo(scheduleDTO.getActiveTo());
				}
				if (StringUtil.isNull(dto.getDayOfWeek())) {
					dto.setDayOfWeek(scheduleDTO.getDayOfWeek());
				}
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleStationPointDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleStationPointDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleStationPointDTO>(scheduleMap.values());
	}
	
	public List<BusVehicleVanPickupDTO> getVanPickupStationPoint(AuthDTO authDTO, StationDTO stationDTO) {
		List<BusVehicleVanPickupDTO> list = new ArrayList<BusVehicleVanPickupDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT vanp.code, vanp.name, poit.code,poit.name,poit.address,poit.landmark,poit.contact_number,poit.latitude,poit.longitude,poit.map_url,poit.amenities_code,poit.active_flag, vanp.code, vanp.name FROM schedule sch, schedule_station_point sspt, station_point poit, bus_vehicle_pickup_van vanp WHERE sspt.namespace_id = ? AND sspt.station_id = ? AND sspt.namespace_id = poit.namespace_id AND sch.namespace_id = sspt.namespace_id AND sspt.namespace_id = vanp.namespace_id AND sch.id = sspt.schedule_id AND vanp.id = sspt.bus_vehicle_pickup_van_id AND sspt.station_id = poit.station_id AND sspt.station_point_id = poit.id AND poit.active_flag < 2 AND sch.active_flag = 1 AND sspt.active_flag = 1 AND vanp.active_flag = 1 GROUP BY vanp.id, poit.id");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, stationDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleVanPickupDTO vanPickupDTO =  new BusVehicleVanPickupDTO();
				vanPickupDTO.setCode(selectRS.getString("vanp.code"));
				vanPickupDTO.setName(selectRS.getString("vanp.name"));
				
				StationDTO stationDTO2 = new StationDTO();
				stationDTO2.setCode(stationDTO.getCode());
				stationDTO2.setName(stationDTO.getName());
				
				List<StationPointDTO> stationPointList = new ArrayList<>();
				StationPointDTO dto = new StationPointDTO();
				dto.setStation(stationDTO);
				dto.setCode(selectRS.getString("poit.code"));
				dto.setName(selectRS.getString("poit.name"));
				dto.setAddress(selectRS.getString("poit.address"));
				dto.setLandmark(selectRS.getString("poit.landmark"));
				dto.setNumber(selectRS.getString("poit.contact_number"));
				dto.setLatitude(selectRS.getString("poit.latitude"));
				dto.setLongitude(selectRS.getString("poit.longitude"));
				dto.setMapUrl(selectRS.getString("poit.map_url"));
				dto.setAmenities(selectRS.getString("poit.amenities_code"));
				dto.setActiveFlag(selectRS.getInt("poit.active_flag"));
				stationPointList.add(dto);
				stationDTO2.setStationPoints(stationPointList);
				vanPickupDTO.setStation(stationDTO2);
				list.add(vanPickupDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public boolean CheckStationPointUsed(AuthDTO authDTO, StationPointDTO dto) {
		boolean status = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT sche.code FROM schedule_station_point point , station_point ress,schedule sche WHERE   sche.namespace_id = ress.namespace_id    AND  point.namespace_id = ress.namespace_id    AND sche.id = point.schedule_id AND ress.id = point.station_point_id   AND point.namespace_id = ?  AND  ress.code = ?  AND sche.active_flag  = 1 AND ress.active_flag  = 1 AND point.active_flag  = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, dto.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				status = true;
			}
			@Cleanup
			PreparedStatement selectExcepPS = connection.prepareStatement("SELECT code FROM schedule_station_point_exception point  WHERE   namespace_id = ?  AND station_point_code LIKE ? AND  active_flag  = 1 ");
			selectExcepPS.setInt(1, authDTO.getNamespace().getId());
			selectExcepPS.setString(2, '%' + dto.getCode() + '%');
			@Cleanup
			ResultSet selectExceptionRS = selectExcepPS.executeQuery();
			if (selectExceptionRS.next()) {
				status = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return status;
	}

	public void updateScheduleStationPointException(AuthDTO authDTO, ScheduleStationPointDTO stationPointDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			StringBuilder stationPointCodes = new StringBuilder();
			for (StationPointDTO pointDTO : stationPointDTO.getStationPointList()) {
				stationPointCodes.append(",");
				stationPointCodes.append(pointDTO.getCode());
			}

			StringBuilder scheduleCodes = new StringBuilder();
			if (stationPointDTO.getScheduleList().isEmpty()) {
				scheduleCodes.append("NA");
			}
			else {
				for (ScheduleDTO schedule : stationPointDTO.getScheduleList()) {
					scheduleCodes.append(",");
					scheduleCodes.append(schedule.getCode());
				}
			}
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_STATION_POINT_EXCEPTION_IUD(?,?,?,?,?, ?,?,?,?,?,? ,?,?,?,?,?)}");
			pindex = 0;
			callableStatement.setString(++pindex, stationPointDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, stationPointDTO.getStation().getCode());
			callableStatement.setString(++pindex, stationPointCodes.toString());
			callableStatement.setString(++pindex, scheduleCodes.length() < 10 ? "NA" : scheduleCodes.toString());
			callableStatement.setString(++pindex, StringUtil.isNull(stationPointDTO.getActiveFrom(), Text.NA));
			callableStatement.setString(++pindex, StringUtil.isNull(stationPointDTO.getActiveTo(), Text.NA));
			callableStatement.setString(++pindex, stationPointDTO.getTripDatesToString());
			callableStatement.setString(++pindex, stationPointDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, stationPointDTO.getReleaseMinutes());
			callableStatement.setString(++pindex, stationPointDTO.getBoardingDroppingFlag());
			callableStatement.setString(++pindex, stationPointDTO.getStationPointType());
			callableStatement.setInt(++pindex, stationPointDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stationPointDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				stationPointDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<ScheduleStationPointDTO> getScheduleStationPointException(AuthDTO authDTO) {
		List<ScheduleStationPointDTO> stationPointExceptionList = new ArrayList<ScheduleStationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stpe.station_id, stpe.code, stpe.active_from, stpe.active_to, stpe.trip_dates, stpe.day_of_week, stpe.release_minutes, stpe.boarding_dropping_flag, stpe.station_point_type, stpe.station_point_code, stpe.schedule_code, stpe.active_flag FROM schedule_station_point_exception stpe WHERE stpe.namespace_id = ? AND stpe.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStationPointDTO stationPointDTO = new ScheduleStationPointDTO();
				stationPointDTO.setCode(selectRS.getString("stpe.code"));
				stationPointDTO.setActiveFlag(selectRS.getInt("stpe.active_flag"));
				stationPointDTO.setActiveFrom(selectRS.getString("stpe.active_from"));
				stationPointDTO.setActiveTo(selectRS.getString("stpe.active_to"));
				stationPointDTO.setDayOfWeek(selectRS.getString("stpe.day_of_week"));
				stationPointDTO.setReleaseMinutes(selectRS.getInt("stpe.release_minutes"));
				stationPointDTO.setBoardingDroppingFlag(selectRS.getString("stpe.boarding_dropping_flag"));
				stationPointDTO.setStationPointType(selectRS.getString("stpe.station_point_type"));

				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("stpe.station_id"));
				stationPointDTO.setStation(stationDTO);

				String stationPointCodes = selectRS.getString("stpe.station_point_code");

				StationPointDTO pointDTO = new StationPointDTO();
				pointDTO.setCode(stationPointCodes);
				stationPointDTO.setStationPoint(pointDTO);

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(selectRS.getString("stpe.schedule_code"));
				stationPointDTO.setSchedule(scheduleDTO);

				stationPointDTO.setTripDates(getTripDates(selectRS.getString("stpe.trip_dates")));
				stationPointExceptionList.add(stationPointDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return stationPointExceptionList;
	}

	/*
	 * All station point in schedule active for station
	 * Used for SEO
	 */
	public List<StationPointDTO> getScheduleStationPoint(AuthDTO authDTO, StationDTO station) {
		List<StationPointDTO> list = new ArrayList<StationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DISTINCT pint.code, pint.name, pint.address, pint.landmark, pint.contact_number, pint.latitude, pint.longitude FROM station_point pint, schedule sche, schedule_station stat, schedule_station_point spnt WHERE pint.namespace_id = ? AND pint.namespace_id = sche.namespace_id AND stat.namespace_id = pint.namespace_id AND spnt.namespace_id = sche.namespace_id AND stat.station_id = ? AND stat.station_id = pint.station_id AND sche.active_flag = 1 AND pint.active_flag = 1 AND stat.active_flag = 1 AND spnt.active_flag = 1 AND sche.id = stat.schedule_id AND stat.station_id = pint.station_id AND spnt.station_point_id = pint.id AND spnt.schedule_id = sche.id AND sche.lookup_id = 0");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, station.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {

				StationPointDTO point = new StationPointDTO();
				point.setCode(selectRS.getString("pint.code"));
				point.setName(selectRS.getString("pint.name"));
				point.setAddress(selectRS.getString("pint.address"));
				point.setLandmark(selectRS.getString("pint.landmark"));
				point.setNumber(selectRS.getString("pint.contact_number"));
				point.setLatitude(selectRS.getString("pint.latitude"));
				point.setLongitude(selectRS.getString("pint.longitude"));
				list.add(point);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	private List<String> getTripDates(String tripDates) {
		List<String> tripDateTimes = new ArrayList<>();
		if (StringUtil.isNotNull(tripDates)) {
			for (String tripdate : tripDates.split(",")) {
				if (DateUtil.isValidDate(tripdate)) {
					tripDateTimes.add(tripdate);
				}
			}
		}
		return tripDateTimes;
	}
}
