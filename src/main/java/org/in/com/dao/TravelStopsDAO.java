package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import com.google.common.base.Joiner;

public class TravelStopsDAO {

	public List<TravelStopsDTO> getStop(AuthDTO authDTO, TravelStopsDTO dto) {
		List<TravelStopsDTO> list = new ArrayList<TravelStopsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, amenities, rest_room, minutes, landmark, latitude, longitude, active_flag FROM travel_stops WHERE code  = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, dto.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				TravelStopsDTO stopDTO = new TravelStopsDTO();
				stopDTO.setCode(selectRS.getString("code"));
				stopDTO.setName(selectRS.getString("name"));
				String amenities = selectRS.getString("amenities");
				stopDTO.setAmenities(Arrays.asList(amenities.split(",")));
				stopDTO.setRestRoom(selectRS.getString("rest_room"));
				stopDTO.setMinutes(selectRS.getInt("minutes"));
				stopDTO.setLandmark(selectRS.getString("landmark"));
				stopDTO.setLatitude(selectRS.getString("latitude"));
				stopDTO.setLongitude(selectRS.getString("longitude"));
				stopDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(stopDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<TravelStopsDTO> getAllStop(AuthDTO authDTO) {
		List<TravelStopsDTO> list = new ArrayList<TravelStopsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, amenities, rest_room, minutes, landmark, latitude, longitude, active_flag FROM travel_stops WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TravelStopsDTO stopDTO = new TravelStopsDTO();
				stopDTO.setCode(selectRS.getString("code"));
				stopDTO.setName(selectRS.getString("name"));
				String amenities = selectRS.getString("amenities");
				stopDTO.setAmenities(Arrays.asList(amenities.split(",")));
				stopDTO.setRestRoom(selectRS.getString("rest_room"));
				stopDTO.setMinutes(selectRS.getInt("minutes"));
				stopDTO.setLandmark(selectRS.getString("landmark"));
				stopDTO.setLatitude(selectRS.getString("latitude"));
				stopDTO.setLongitude(selectRS.getString("longitude"));
				stopDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(stopDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public TravelStopsDTO update(AuthDTO authDTO, TravelStopsDTO stopsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TRAVEL_STOPS_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?)}");
			callableStatement.setString(++pindex, stopsDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, stopsDTO.getName());
			callableStatement.setString(++pindex, Joiner.on(",").join(stopsDTO.getAmenities()));
			callableStatement.setString(++pindex, stopsDTO.getRestRoom());
			callableStatement.setInt(++pindex, stopsDTO.getMinutes());
			callableStatement.setString(++pindex, stopsDTO.getLandmark());
			callableStatement.setString(++pindex, stopsDTO.getLatitude());
			callableStatement.setString(++pindex, stopsDTO.getLongitude());
			callableStatement.setInt(++pindex, stopsDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stopsDTO.setCode(callableStatement.getString("pcrCode"));
				stopsDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return stopsDTO;
	}

	public List<TravelStopsDTO> getScheduleStop(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<TravelStopsDTO> list = new ArrayList<TravelStopsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stg.code, stg.name,  stg.amenities, rest_room, stg.minutes, sstg.travel_minutes, sstg.station_id, sstg.remarks, stg.landmark, stg.latitude, stg.longitude, stg.active_flag FROM travel_stops stg, schedule_travel_stops sstg, schedule sche WHERE sche.code = ? AND stg.namespace_id = ? AND stg.id = sstg.travel_stops_id AND sche.id = sstg.schedule_id AND stg.active_flag = 1 AND sstg.active_flag = 1");
			selectPS.setString(1, scheduleDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TravelStopsDTO stopDTO = new TravelStopsDTO();
				stopDTO.setCode(selectRS.getString("stg.code"));
				stopDTO.setName(selectRS.getString("stg.name"));
				String amenities = selectRS.getString("amenities");
				stopDTO.setAmenities(Arrays.asList(amenities.split(",")));
				stopDTO.setRestRoom(selectRS.getString("rest_room"));
				stopDTO.setMinutes(selectRS.getInt("stg.minutes"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("station_id"));
				stopDTO.setStation(stationDTO);
				stopDTO.setLandmark(selectRS.getString("stg.landmark"));
				stopDTO.setLatitude(selectRS.getString("stg.latitude"));
				stopDTO.setLongitude(selectRS.getString("stg.longitude"));
				stopDTO.setTravelMinutes(selectRS.getInt("sstg.travel_minutes"));
				stopDTO.setRemarks(selectRS.getString("sstg.remarks"));
				stopDTO.setActiveFlag(selectRS.getInt("stg.active_flag"));
				list.add(stopDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public TravelStopsDTO mapScheduleStops(AuthDTO authDTO, ScheduleDTO scheduleDTO, TravelStopsDTO stopDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_TRAVEL_STOPS_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setString(++pindex, scheduleDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, stopDTO.getCode());
			callableStatement.setInt(++pindex, stopDTO.getStation().getId());
			callableStatement.setInt(++pindex, stopDTO.getTravelMinutes());
			callableStatement.setString(++pindex, StringUtil.substring(stopDTO.getRemarks(), 240));
			callableStatement.setInt(++pindex, stopDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stopDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return stopDTO;
	}

}
