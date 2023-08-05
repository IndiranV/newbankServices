package org.in.com.dao;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Component;

@Component
public class ScheduleDynamicStageFareDAO {
	public ScheduleDynamicStageFareDTO updateScheduleDynamicStageFare(AuthDTO authDTO, ScheduleDynamicStageFareDTO dynamicStageFareDTO) {
		ScheduleDynamicStageFareDTO scheduleDynamicStageFareDTO = new ScheduleDynamicStageFareDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_DYNAMIC_STAGE_FARE_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?)}");
			callableStatement.setString(++pindex, dynamicStageFareDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dynamicStageFareDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, dynamicStageFareDTO.getActiveFrom() != null ? dynamicStageFareDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, dynamicStageFareDTO.getActiveTo() != null ? dynamicStageFareDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, dynamicStageFareDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, dynamicStageFareDTO.getStatus());
			callableStatement.setInt(++pindex, dynamicStageFareDTO.getDynamicPriceProvider().getId());
			callableStatement.setString(++pindex, dynamicStageFareDTO.getLookupCode());
			callableStatement.setInt(++pindex, dynamicStageFareDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0 || StringUtil.isNull(dynamicStageFareDTO.getCode())) {
				scheduleDynamicStageFareDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleDynamicStageFareDTO;
	}

	public List<ScheduleDynamicStageFareDTO> getScheduleSeatFareByScheduleId(AuthDTO authDTO, ScheduleDTO schedule) {
		List<ScheduleDynamicStageFareDTO> overrideList = new ArrayList<ScheduleDynamicStageFareDTO>();
		Map<Integer, ScheduleDynamicStageFareDTO> scheduleMap = new HashMap<Integer, ScheduleDynamicStageFareDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (schedule.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, active_from, active_to, day_of_week, status, dynamic_price_provider_id, lookup_id, active_flag FROM schedule_dynamic_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, schedule.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT fare.id, fare.code, fare.active_from, fare.active_to, fare.day_of_week, fare.status, fare.dynamic_price_provider_id, fare.lookup_id, fare.active_flag FROM schedule_dynamic_stage_fare fare,schedule sche WHERE fare.namespace_id = ? AND fare.namespace_id = sche.namespace_id AND fare.schedule_id = sche.id AND sche.code = ? AND fare.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, schedule.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDynamicStageFareDTO stageFareDTO = new ScheduleDynamicStageFareDTO();
				stageFareDTO.setId(selectRS.getInt("id"));
				stageFareDTO.setCode(selectRS.getString("code"));
				stageFareDTO.setActiveFrom(selectRS.getString("active_from"));
				stageFareDTO.setActiveTo(selectRS.getString("active_to"));
				stageFareDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				stageFareDTO.setStatus(selectRS.getInt("status"));
				stageFareDTO.setLookupCode(selectRS.getString("lookup_id"));
				stageFareDTO.setActiveFlag(selectRS.getInt("active_flag"));
				stageFareDTO.setDynamicPriceProvider(DynamicPriceProviderEM.getDynamicPriceProviderEM(selectRS.getInt("dynamic_price_provider_id")));

				// Exception is disabled
				if (stageFareDTO.getStatus() != 1 && !stageFareDTO.getLookupCode().equals("0")) {
					continue;
				}

				List<ScheduleDynamicStageFareDetailsDTO> list = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
				@Cleanup
				PreparedStatement detailsPS = connection.prepareStatement("SELECT min_fare, max_fare, from_station_id, to_station_id, active_flag FROM schedule_dynamic_stage_fare_details WHERE namespace_id = ? AND schedule_dynamic_stage_fare_id = ? AND active_flag = 1");
				detailsPS.setInt(1, authDTO.getNamespace().getId());
				detailsPS.setInt(2, stageFareDTO.getId());

				@Cleanup
				ResultSet detailsRS = detailsPS.executeQuery();
				while (detailsRS.next()) {
					ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();
					dynamicStageFareDetailsDTO.setMinFare(new BigDecimal(detailsRS.getString("min_fare")));
					dynamicStageFareDetailsDTO.setMaxFare(new BigDecimal(detailsRS.getString("max_fare")));

					StationDTO fromStation = new StationDTO();
					fromStation.setId(detailsRS.getInt("from_station_id"));
					dynamicStageFareDetailsDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setId(detailsRS.getInt("to_station_id"));
					dynamicStageFareDetailsDTO.setToStation(toStation);

					dynamicStageFareDetailsDTO.setActiveFlag(detailsRS.getInt("active_flag"));
					list.add(dynamicStageFareDetailsDTO);
				}

				stageFareDTO.setStageFare(list);
				if (stageFareDTO.getLookupCode().equals("0")) {
					scheduleMap.put(stageFareDTO.getId(), stageFareDTO);
				}
				else {
					overrideList.add(stageFareDTO);
				}
			}
			for (ScheduleDynamicStageFareDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleDynamicStageFareDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleDynamicStageFareDTO>(scheduleMap.values());
	}

	public void updateScheduleDynamicStageFareDetails(AuthDTO authDTO, ScheduleDynamicStageFareDTO dynamicStageFare) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : dynamicStageFare.getStageFare()) {
				pindex = 0;
				@Cleanup
				CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_DYNAMIC_STAGE_FARE_DETAILS_IUD(?,?,?,?, ?,?,?,?,?, ?)}");
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, dynamicStageFare.getCode());
				callableStatement.setBigDecimal(++pindex, dynamicStageFareDetails.getMinFare());
				callableStatement.setBigDecimal(++pindex, dynamicStageFareDetails.getMaxFare());
				callableStatement.setString(++pindex, dynamicStageFareDetails.getFromStation().getCode());
				callableStatement.setString(++pindex, dynamicStageFareDetails.getToStation().getCode());
				callableStatement.setInt(++pindex, dynamicStageFareDetails.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void addcheduleDynamicStageFareMap(AuthDTO authDTO, ScheduleDTO scheduleDTO, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails, String fares, DateTime dateTime) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO schedule_dynamic_trip_stage_fare (namespace_id, schedule_id, from_station_id, to_station_id, trip_date, fare_group, active_flag, updated_by, updated_at) VALUES (?,?,?,?,?, ?,1,?,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setInt(++pindex, authDTO.getNamespace().getId());
			ps.setInt(++pindex, scheduleDTO.getId());
			ps.setInt(++pindex, dynamicStageFareDetails.getFromStation().getId());
			ps.setInt(++pindex, dynamicStageFareDetails.getToStation().getId());
			ps.setString(++pindex, dateTime.format("YYYY-MM-DD"));
			ps.setString(++pindex, fares);
			ps.setInt(++pindex, 0);
			ps.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public List<Map<String, String>> getScheduleDynamicTripStageFare(AuthDTO authDTO, ScheduleDTO schedule) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, trip_date, fare FROM schedule_dynamic_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, schedule.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				Map<String, String> dynamicStageFare = new HashMap<String, String>();
				dynamicStageFare.put("fromStationId", String.valueOf(selectRS.getInt("from_station_id")));
				dynamicStageFare.put("toStationId", String.valueOf(selectRS.getInt("from_station_id")));
				dynamicStageFare.put("tripDate", String.valueOf(selectRS.getString("trip_date")));
				dynamicStageFare.put("fare", String.valueOf(selectRS.getString("fare")));
				list.add(dynamicStageFare);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return list;
	}

	public Map<String, String> getDynamicFare(AuthDTO authDTO) {
		Map<String, String> routeFareMap = new HashMap<String, String>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dsf.schedule_id, dsfd.from_station_id, dsfd.to_station_id, dsfd.fare_group, dsfd.trip_date FROM schedule_dynamic_stage_fare dsf, schedule_dynamic_trip_stage_fare dsfd WHERE dsf.namespace_id = ? AND dsf.active_flag = 1 AND dsfd.schedule_id = dsf.schedule_id AND dsfd.namespace_id = dsf.namespace_id AND dsfd.active_flag = 1 AND dsf.status = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StringBuilder key = new StringBuilder();
				key.append(selectRS.getInt("dsfd.from_station_id"));
				key.append("_");
				key.append(selectRS.getInt("dsfd.to_station_id"));
				key.append("_");
				key.append(selectRS.getInt("dsf.schedule_id"));

				StringBuilder value = new StringBuilder();
				value.append(key);
				value.append("_");
				value.append(selectRS.getString("dsfd.fare_group"));
				value.append("_");
				value.append(selectRS.getString("dsfd.trip_date"));
				value.append("_");

				routeFareMap.put(key.toString(), value.toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return routeFareMap;
	}

	public List<ScheduleDynamicStageFareDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO schedule) {
		List<ScheduleDynamicStageFareDTO> overrideList = new ArrayList<ScheduleDynamicStageFareDTO>();
		Map<Integer, ScheduleDynamicStageFareDTO> scheduleMap = new HashMap<Integer, ScheduleDynamicStageFareDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, active_from, active_to, day_of_week, status, dynamic_price_provider_id, lookup_id, active_flag FROM schedule_dynamic_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, schedule.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDynamicStageFareDTO stageFareDTO = new ScheduleDynamicStageFareDTO();
				stageFareDTO.setId(selectRS.getInt("id"));
				stageFareDTO.setCode(selectRS.getString("code"));
				stageFareDTO.setActiveFrom(selectRS.getString("active_from"));
				stageFareDTO.setActiveTo(selectRS.getString("active_to"));
				stageFareDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				stageFareDTO.setStatus(selectRS.getInt("status"));
				stageFareDTO.setLookupCode(selectRS.getString("lookup_id"));
				stageFareDTO.setActiveFlag(selectRS.getInt("active_flag"));
				stageFareDTO.setDynamicPriceProvider(DynamicPriceProviderEM.getDynamicPriceProviderEM(selectRS.getInt("dynamic_price_provider_id")));

				// Exception is disabled
				if (stageFareDTO.getStatus() != 1 && !stageFareDTO.getLookupCode().equals("0")) {
					continue;
				}
				if (stageFareDTO.getLookupCode().equals("0")) {
					List<ScheduleDynamicStageFareDetailsDTO> list = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
					@Cleanup
					PreparedStatement detailsPS = connection.prepareStatement("SELECT min_fare, max_fare, from_station_id, to_station_id, active_flag FROM schedule_dynamic_stage_fare_details WHERE namespace_id = ? AND schedule_dynamic_stage_fare_id = ? AND active_flag = 1");
					detailsPS.setInt(1, authDTO.getNamespace().getId());
					detailsPS.setInt(2, stageFareDTO.getId());

					@Cleanup
					ResultSet detailsRS = detailsPS.executeQuery();
					while (detailsRS.next()) {
						ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();
						dynamicStageFareDetailsDTO.setMinFare(new BigDecimal(detailsRS.getString("min_fare")));
						dynamicStageFareDetailsDTO.setMaxFare(new BigDecimal(detailsRS.getString("max_fare")));

						StationDTO fromStation = new StationDTO();
						fromStation.setId(detailsRS.getInt("from_station_id"));
						dynamicStageFareDetailsDTO.setFromStation(fromStation);

						StationDTO toStation = new StationDTO();
						toStation.setId(detailsRS.getInt("to_station_id"));
						dynamicStageFareDetailsDTO.setToStation(toStation);

						dynamicStageFareDetailsDTO.setActiveFlag(detailsRS.getInt("active_flag"));

						list.add(dynamicStageFareDetailsDTO);
					}

					stageFareDTO.setStageFare(list);
				}
				if (stageFareDTO.getLookupCode().equals("0")) {
					scheduleMap.put(stageFareDTO.getId(), stageFareDTO);
				}
				else {
					overrideList.add(stageFareDTO);
				}
			}
			for (ScheduleDynamicStageFareDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleDynamicStageFareDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleDynamicStageFareDTO>(scheduleMap.values());
	}

	// get all fare details, reporting
	public List<ScheduleDynamicStageFareDetailsDTO> getAllDPTripStageFareDetails(AuthDTO authDTO, ScheduleDTO schedule) {
		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareDetails = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, trip_date, fare_group, active_flag FROM schedule_dynamic_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, schedule.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();

				StationDTO fromStation = new StationDTO();
				fromStation.setId(selectRS.getInt("from_station_id"));
				dynamicStageFareDetailsDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(selectRS.getInt("to_station_id"));
				dynamicStageFareDetailsDTO.setToStation(toStation);

				dynamicStageFareDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));

				dynamicStageFareDetailsDTO.setTripDate(new DateTime(selectRS.getString("trip_date")));
				String seatFareGroup = selectRS.getString("fare_group");
				List<BusSeatLayoutDTO> seatFareList = new ArrayList<BusSeatLayoutDTO>();
				for (String seatFare : seatFareGroup.split(",")) {
					BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
					layoutDTO.setName(seatFare.split(":")[0]);
					layoutDTO.setFare(new BigDecimal(seatFare.split(":")[1]));
					seatFareList.add(layoutDTO);
				}
				dynamicStageFareDetailsDTO.setSeatFare(seatFareList);
				scheduleDynamicStageFareDetails.add(dynamicStageFareDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleDynamicStageFareDetails;
	}

	// for specific trip Date, cached
	public List<ScheduleDynamicStageFareDetailsDTO> getDPTripStageFareDetails(AuthDTO authDTO, ScheduleDTO schedule) {
		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareDetails = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, trip_date, fare_group, active_flag FROM schedule_dynamic_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND trip_date = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, schedule.getId());
			selectPS.setString(3, schedule.getTripDate().format("YYYY-MM-DD"));

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();

				StationDTO fromStation = new StationDTO();
				fromStation.setId(selectRS.getInt("from_station_id"));
				dynamicStageFareDetailsDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(selectRS.getInt("to_station_id"));
				dynamicStageFareDetailsDTO.setToStation(toStation);

				dynamicStageFareDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));

				dynamicStageFareDetailsDTO.setTripDate(new DateTime(selectRS.getString("trip_date")));
				String seatFareGroup = selectRS.getString("fare_group");
				List<BusSeatLayoutDTO> seatFareList = new ArrayList<BusSeatLayoutDTO>();
				for (String seatFare : seatFareGroup.split(",")) {
					BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
					layoutDTO.setName(seatFare.split(":")[0]);
					layoutDTO.setFare(new BigDecimal(seatFare.split(":")[1]));
					seatFareList.add(layoutDTO);
				}
				dynamicStageFareDetailsDTO.setSeatFare(seatFareList);

				scheduleDynamicStageFareDetails.add(dynamicStageFareDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleDynamicStageFareDetails;
	}

	public void updateStatus(AuthDTO authDTO, ScheduleDynamicStageFareDTO dynamicStageFareDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE schedule_dynamic_stage_fare SET status = ? WHERE code = ? AND namespace_id = ?");
			ps.setInt(1, dynamicStageFareDTO.getStatus());
			ps.setString(2, dynamicStageFareDTO.getCode());
			ps.setInt(3, authDTO.getNamespace().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public boolean existDynamicRoute(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStation, StationDTO toStation, DateTime tripDate) {
		boolean isExist = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM schedule_dynamic_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND from_station_id = ? AND to_station_id = ? AND trip_date = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			selectPS.setInt(3, fromStation.getId());
			selectPS.setInt(4, toStation.getId());
			selectPS.setString(5, tripDate.format("YYYY-MM-DD"));

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				isExist = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return isExist;
	}

	public void updateScheduleDynamicStageFareMappping(AuthDTO authDTO, ScheduleDTO schedule, List<ScheduleDynamicStageFareDetailsDTO> dynamicStageFareDetailsList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement checkExistPS = connection.prepareStatement("SELECT 1 FROM schedule_dynamic_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND from_station_id = ? AND to_station_id = ? AND trip_date = ? AND active_flag = 1");
			@Cleanup
			PreparedStatement updatePS = connection.prepareStatement("UPDATE schedule_dynamic_trip_stage_fare SET fare_group = ?, updated_at = NOW() WHERE namespace_id = ? AND schedule_id = ? AND from_station_id = ? AND to_station_id = ? AND trip_date = ? AND active_flag = 1");
			@Cleanup
			PreparedStatement insertPS = connection.prepareStatement("INSERT INTO schedule_dynamic_trip_stage_fare (namespace_id, schedule_id, from_station_id, to_station_id, trip_date, fare_group, active_flag, updated_by, updated_at) VALUES (?,?,?,?,?, ?,1,0,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);

			for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : dynamicStageFareDetailsList) {

				if (dynamicStageFareDetails.getSeatFare().isEmpty()) {
					continue;
				}
				// Check DP stage fare if exists or Not
				StringBuilder seatFare = new StringBuilder();
				for (BusSeatLayoutDTO busSeatLayoutDTO : dynamicStageFareDetails.getSeatFare()) {
					seatFare.append(busSeatLayoutDTO.getName());
					seatFare.append(":");
					seatFare.append(busSeatLayoutDTO.getFare());
					seatFare.append(",");
				}
				checkExistPS.setInt(1, authDTO.getNamespace().getId());
				checkExistPS.setInt(2, schedule.getId());
				checkExistPS.setInt(3, dynamicStageFareDetails.getFromStation().getId());
				checkExistPS.setInt(4, dynamicStageFareDetails.getToStation().getId());
				checkExistPS.setString(5, schedule.getTripDate().format("YYYY-MM-DD"));

				boolean isExist = false;
				@Cleanup
				ResultSet selectRS = checkExistPS.executeQuery();
				if (selectRS.next()) {
					isExist = true;
				}
				checkExistPS.clearParameters();

				if (isExist) { // update DP stage fare if exists
					updatePS.setString(1, seatFare.toString());
					updatePS.setInt(2, authDTO.getNamespace().getId());
					updatePS.setInt(3, schedule.getId());
					updatePS.setInt(4, dynamicStageFareDetails.getFromStation().getId());
					updatePS.setInt(5, dynamicStageFareDetails.getToStation().getId());
					updatePS.setString(6, schedule.getTripDate().format("YYYY-MM-DD"));
					updatePS.addBatch();
					updatePS.clearParameters();
				}
				else { // insert DP stage fare if not exists
					insertPS.setInt(1, authDTO.getNamespace().getId());
					insertPS.setInt(2, schedule.getId());
					insertPS.setInt(3, dynamicStageFareDetails.getFromStation().getId());
					insertPS.setInt(4, dynamicStageFareDetails.getToStation().getId());
					insertPS.setString(5, schedule.getTripDate().format("YYYY-MM-DD"));
					insertPS.setString(6, seatFare.toString());
					insertPS.addBatch();
					insertPS.clearParameters();

				}

			}
			int[] insertresults = insertPS.executeBatch();
			int[] updateresults = updatePS.executeBatch();
//			System.out.println("insertresults" + insertresults.length + updateresults.length);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
	}

	public List<ScheduleDynamicStageFareDTO> getDynamicSchedules(AuthDTO authDTO, DateTime dateTime) {
		List<ScheduleDynamicStageFareDTO> overrideList = new ArrayList<ScheduleDynamicStageFareDTO>();
		Map<Integer, ScheduleDynamicStageFareDTO> scheduleDynamicStageFareMap = new HashMap<Integer, ScheduleDynamicStageFareDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT schedule_id, active_from, active_to, lookup_id, status_id FROM schedule_dynamic_stage_fare WHERE namespace_id = ? AND active_from >= ? AND active_to <= ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, DateUtil.convertDate(dateTime));
			selectPS.setString(3, DateUtil.convertDate(dateTime));

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDynamicStageFareDTO stageFareDTO = new ScheduleDynamicStageFareDTO();
				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("schedule_id"));
				stageFareDTO.setSchedule(schedule);

				stageFareDTO.setActiveFrom(selectRS.getString("active_from"));
				stageFareDTO.setActiveTo(selectRS.getString("active_to"));
				stageFareDTO.setLookupCode(selectRS.getString("lookup_id"));
				stageFareDTO.setStatus(selectRS.getInt("status_id"));

				if (stageFareDTO.getLookupCode().equals("0")) {
					scheduleDynamicStageFareMap.put(stageFareDTO.getId(), stageFareDTO);
				}
				else {
					overrideList.add(stageFareDTO);
				}
			}
			for (ScheduleDynamicStageFareDTO overrideScheduleDTO : overrideList) {
				if (scheduleDynamicStageFareMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleDynamicStageFareDTO dto = scheduleDynamicStageFareMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleDynamicStageFareMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleDynamicStageFareDTO>(scheduleDynamicStageFareMap.values());
	}
}
