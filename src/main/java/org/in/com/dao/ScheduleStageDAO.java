package org.in.com.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class ScheduleStageDAO extends GroupDAO {

	public List<ScheduleStageDTO> get(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleStageDTO> overrideList = new ArrayList<ScheduleStageDTO>();
		Map<Integer, ScheduleStageDTO> scheduleMap = new HashMap<Integer, ScheduleStageDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT fare.id,fare.user_group_id,fare.code,fare.from_station_id,fare.to_station_id,fare.bus_seat_type_id,fare.bus_seat_type_fare,fare,fare.active_from,fare.active_to,fare.day_of_week,fare.lookup_id,fare.active_flag,sche.id,sche.code,fare.fare  FROM schedule_stage fare, schedule sche WHERE sche.id = fare.schedule_id AND fare.namespace_id = ? AND sche.namespace_id = ? AND sche.code = ? AND fare.active_flag IN (1,6)");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, scheduleDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStageDTO dto = new ScheduleStageDTO();
				dto.setId(selectRS.getInt("fare.id"));
				dto.setCode(selectRS.getString("fare.code"));
				dto.setActiveFlag(selectRS.getInt("fare.active_flag"));
				dto.setActiveFlag(1);
				dto.setFare(selectRS.getDouble("fare.fare"));
				dto.setLookupCode(selectRS.getString("fare.lookup_id"));
				dto.setActiveFrom(selectRS.getString("fare.active_from"));
				dto.setActiveTo(selectRS.getString("fare.active_to"));
				dto.setDayOfWeek(selectRS.getString("fare.day_of_week"));
				ScheduleDTO schedule = new ScheduleDTO();
				StationDTO FromStationDTO = new StationDTO();
				StationDTO ToStationDTO = new StationDTO();
				FromStationDTO.setId(selectRS.getInt("fare.from_station_id"));
				ToStationDTO.setId(selectRS.getInt("fare.to_station_id"));
				dto.setFromStation(FromStationDTO);
				dto.setToStation(ToStationDTO);
				schedule.setId(selectRS.getInt("sche.id"));
				schedule.setCode(selectRS.getString("sche.code"));
				// update parent reff
				scheduleDTO.setId(selectRS.getInt("sche.id"));

				int busSeatTypeId = selectRS.getInt("fare.bus_seat_type_id");
				dto.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeId));

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("fare.bus_seat_type_fare"));
				dto.setBusSeatTypeFare(busSeatTypeFareList);

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("fare.user_group_id"));
				dto.setGroup(groupDTO);
				dto.setSchedule(schedule);
				// Overrides
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleStageDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleStageDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleStageDTO>(scheduleMap.values());
	}

	public ScheduleStageDTO getIUD(AuthDTO authDTO, ScheduleStageDTO scheduleStageFareDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_SCHEDULE_STAGE_IUD(?,?,?,?,? ,?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			for (ScheduleStageDTO fareDTO : scheduleStageFareDTO.getList()) {
				pindex = 0;
				callableStatement.setString(++pindex, fareDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, fareDTO.getSchedule().getCode());
				callableStatement.setInt(++pindex, fareDTO.getFromStation().getId());
				callableStatement.setInt(++pindex, fareDTO.getToStation().getId());
				callableStatement.setString(++pindex, fareDTO.getGroup().getCode());
				callableStatement.setInt(++pindex, 0);
				callableStatement.setDouble(++pindex, BigDecimal.ZERO.doubleValue());
				callableStatement.setString(++pindex, getBusSeatTypeFareDetails(fareDTO));
				callableStatement.setString(++pindex, fareDTO.getActiveFrom() != null ? fareDTO.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, fareDTO.getActiveTo() != null ? fareDTO.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, fareDTO.getDayOfWeek());
				callableStatement.setString(++pindex, fareDTO.getLookupCode());
				callableStatement.setInt(++pindex, fareDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					fareDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					fareDTO.setCode(callableStatement.getString("pcrCode"));
				}
				scheduleStageFareDTO.setSchedule(fareDTO.getSchedule());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleStageFareDTO;
	}

	// get all trips
	public List<ScheduleStageDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleStageDTO> overrideList = new ArrayList<ScheduleStageDTO>();
		List<ScheduleStageDTO> stageFareList = new ArrayList<ScheduleStageDTO>();
		List<ScheduleStageDTO> fareList = new ArrayList<ScheduleStageDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT fare.id,fare.user_group_id,fare.code,fare.from_station_id,fare.to_station_id,fare.bus_seat_type_id,fare,fare.active_from,fare.active_to,fare.day_of_week,fare.lookup_id,fare.active_flag,fare.fare, fare.bus_seat_type_fare FROM schedule_stage fare WHERE fare.namespace_id = ? AND fare.schedule_id = ? AND fare.active_flag IN (1,6)");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStageDTO dto = new ScheduleStageDTO();
				dto.setId(selectRS.getInt("fare.id"));
				dto.setCode(selectRS.getString("fare.code"));
				dto.setActiveFlag(selectRS.getInt("fare.active_flag"));
				dto.setActiveFlag(1);
				dto.setFare(selectRS.getDouble("fare.fare"));
				dto.setLookupCode(selectRS.getString("fare.lookup_id"));
				dto.setActiveFrom(selectRS.getString("fare.active_from"));
				dto.setActiveTo(selectRS.getString("fare.active_to"));
				dto.setDayOfWeek(selectRS.getString("fare.day_of_week"));
				StationDTO FromStationDTO = new StationDTO();
				StationDTO ToStationDTO = new StationDTO();
				FromStationDTO.setId(selectRS.getInt("fare.from_station_id"));
				ToStationDTO.setId(selectRS.getInt("fare.to_station_id"));
				dto.setFromStation(FromStationDTO);
				dto.setToStation(ToStationDTO);

				int busSeatTypeId = selectRS.getInt("fare.bus_seat_type_id");
				dto.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeId));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("fare.user_group_id"));
				dto.setGroup(groupDTO);

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("fare.bus_seat_type_fare"));
				dto.setBusSeatTypeFare(busSeatTypeFareList);

				dto.setSchedule(scheduleDTO);
				if (StringUtil.isNull(dto.getActiveFrom())) {
					dto.setActiveFrom(scheduleDTO.getActiveFrom());
					dto.setActiveTo(scheduleDTO.getActiveTo());
				}
				if (StringUtil.isNull(dto.getDayOfWeek())) {
					dto.setDayOfWeek(scheduleDTO.getDayOfWeek());
				}

				if (dto.getLookupCode().equals("0")) {
					stageFareList.add(dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			Map<Integer, List<ScheduleStageDTO>> scheduleStageFareMap = convertScheduleStageFares(stageFareList);
			for (ScheduleStageDTO overrideScheduleDTO : overrideList) {
				List<ScheduleStageDTO> scheduleStageFares = scheduleStageFareMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
				if (scheduleStageFares != null && !scheduleStageFares.isEmpty()) {
					for (ScheduleStageDTO dto : scheduleStageFares) {
						dto.getOverrideList().add(overrideScheduleDTO);
					}
				}
			}

			for (Entry<Integer, List<ScheduleStageDTO>> entry : scheduleStageFareMap.entrySet()) {
				List<ScheduleStageDTO> stageFares = entry.getValue();
				fareList.addAll(stageFares);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return fareList;
	}

	// getSearch Result
	public List<ScheduleStageDTO> getStageByStationID(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation) {
		List<ScheduleStageDTO> fareList = new ArrayList<ScheduleStageDTO>();
		List<ScheduleStageDTO> overrideList = new ArrayList<ScheduleStageDTO>();
		Map<Integer, ScheduleStageDTO> scheduleMap = new HashMap<Integer, ScheduleStageDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			StringBuilder stageFilter = new StringBuilder();
			if (fromStation.getId() != 0) {
				stageFilter.append(" AND from_station_id = ").append(fromStation.getId());
			}
			if (toStation.getId() != 0) {
				stageFilter.append(" AND to_station_id = ").append(toStation.getId());
			}

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT fare.id,fare.schedule_id,fare.user_group_id,fare.code,fare.from_station_id,fare.to_station_id,fare.bus_seat_type_id,fare,fare.active_from,fare.active_to,fare.day_of_week,fare.lookup_id,fare.active_flag,fare.fare, fare.bus_seat_type_fare  FROM schedule_stage fare WHERE fare.namespace_id = ? AND fare.active_flag  = 1 " + stageFilter.toString());
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStageDTO dto = new ScheduleStageDTO();
				dto.setId(selectRS.getInt("fare.id"));
				dto.setCode(selectRS.getString("fare.code"));
				dto.setActiveFlag(selectRS.getInt("fare.active_flag"));

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(selectRS.getInt("fare.schedule_id"));
				dto.setSchedule(scheduleDTO);

				dto.setFare(selectRS.getDouble("fare.fare"));
				dto.setLookupCode(selectRS.getString("fare.lookup_id"));
				dto.setActiveFrom(selectRS.getString("fare.active_from"));
				dto.setActiveTo(selectRS.getString("fare.active_to"));
				dto.setDayOfWeek(selectRS.getString("fare.day_of_week"));

				StationDTO FromStationDTO = new StationDTO();
				StationDTO ToStationDTO = new StationDTO();
				FromStationDTO.setId(selectRS.getInt("fare.from_station_id"));
				ToStationDTO.setId(selectRS.getInt("fare.to_station_id"));
				dto.setFromStation(FromStationDTO);
				dto.setToStation(ToStationDTO);

				int busSeatTypeId = selectRS.getInt("fare.bus_seat_type_id");
				dto.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeId));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("fare.user_group_id"));
				dto.setGroup(groupDTO);

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("fare.bus_seat_type_fare"));
				dto.setBusSeatTypeFare(busSeatTypeFareList);

				// Change Sche Fare to ScheV2 fare
				if (busSeatTypeFareList.isEmpty() && busSeatTypeId != 0) {
					dto.setBusSeatTypeFare(convertBusSeatTypeFareList(dto.getBusSeatType(), dto.getFare()));
				}

				if (dto.getLookupCode().equals("0") && (dto.getFare() != 0 || !busSeatTypeFareList.isEmpty())) {
					scheduleMap.put(dto.getId(), dto);
				}
				else if (!dto.getLookupCode().equals("0")) {
					overrideList.add(dto);
				}
			}
			Map<Integer, List<ScheduleStageDTO>> scheduleStageFareMap = convertScheduleStageFares(new ArrayList<ScheduleStageDTO>(scheduleMap.values()));
			for (ScheduleStageDTO overrideScheduleDTO : overrideList) {
				List<ScheduleStageDTO> scheduleStageFares = scheduleStageFareMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
				if (scheduleStageFares != null && !scheduleStageFares.isEmpty()) {
					for (ScheduleStageDTO dto : scheduleStageFares) {
						dto.getOverrideList().add(overrideScheduleDTO);
					}
				}
			}

			for (Entry<Integer, List<ScheduleStageDTO>> entry : scheduleStageFareMap.entrySet()) {
				List<ScheduleStageDTO> stageFares = entry.getValue();
				fareList.addAll(stageFares);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return fareList;
	}

	public List<ScheduleStageDTO> getScheduleStageByStationID(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStation, StationDTO toStation) {

		List<ScheduleStageDTO> overrideList = new ArrayList<ScheduleStageDTO>();
		Map<Integer, ScheduleStageDTO> scheduleMap = new HashMap<Integer, ScheduleStageDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT fare.id,fare.schedule_id,fare.user_group_id,fare.code,fare.from_station_id,fare.to_station_id,fare.bus_seat_type_id,fare,fare.active_from,fare.active_to,fare.day_of_week,fare.lookup_id,fare.active_flag,fare.fare, fare.bus_seat_type_fare FROM schedule_stage fare WHERE fare.namespace_id = ? AND fare.schedule_id = ? AND fare.active_flag  = 1 AND fare.from_station_id = ? AND fare.to_station_id = ? ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			selectPS.setInt(3, fromStation.getId());
			selectPS.setInt(4, toStation.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStageDTO stageDTO = new ScheduleStageDTO();
				stageDTO.setId(selectRS.getInt("fare.id"));
				stageDTO.setCode(selectRS.getString("fare.code"));
				stageDTO.setActiveFlag(selectRS.getInt("fare.active_flag"));
				stageDTO.setSchedule(scheduleDTO);
				stageDTO.setFare(selectRS.getDouble("fare.fare"));
				stageDTO.setLookupCode(selectRS.getString("fare.lookup_id"));
				stageDTO.setActiveFrom(selectRS.getString("fare.active_from"));
				stageDTO.setActiveTo(selectRS.getString("fare.active_to"));
				stageDTO.setDayOfWeek(selectRS.getString("fare.day_of_week"));
				// dto.setLookupDTO(lookupDTO);
				StationDTO FromStationDTO = new StationDTO();
				StationDTO ToStationDTO = new StationDTO();
				FromStationDTO.setId(selectRS.getInt("fare.from_station_id"));
				ToStationDTO.setId(selectRS.getInt("fare.to_station_id"));
				stageDTO.setFromStation(FromStationDTO);
				stageDTO.setToStation(ToStationDTO);

				int busSeatTypeId = selectRS.getInt("fare.bus_seat_type_id");
				stageDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeId));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("fare.user_group_id"));
				stageDTO.setGroup(groupDTO);

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("fare.bus_seat_type_fare"));
				stageDTO.setBusSeatTypeFare(busSeatTypeFareList);

				if (stageDTO.getLookupCode().equals("0")) {
					scheduleMap.put(stageDTO.getId(), stageDTO);
				}
				else {
					overrideList.add(stageDTO);
				}
				for (ScheduleStageDTO overrideScheduleDTO : overrideList) {
					if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
						ScheduleStageDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
						dto.getOverrideList().add(overrideScheduleDTO);
						scheduleMap.put(dto.getId(), dto);
					}
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleStageDTO>(scheduleMap.values());

	}

	public String getBusSeatTypeFareDetails(ScheduleStageDTO scheduleStageDTO) {
		StringBuilder seatTypeFareBuilder = new StringBuilder();
		Map<String, BusSeatTypeEM> stagefareMap = new HashMap<String, BusSeatTypeEM>();
		if (scheduleStageDTO.getBusSeatTypeFare() != null && !scheduleStageDTO.getBusSeatTypeFare().isEmpty()) {
			for (BusSeatTypeFareDTO seatTypeFare : scheduleStageDTO.getBusSeatTypeFare()) {
				if (stagefareMap.get(seatTypeFare.getBusSeatType().getCode()) != null) {
					continue;
				}
				seatTypeFareBuilder.append(seatTypeFare.getBusSeatType().getId());
				seatTypeFareBuilder.append(Text.COLON);
				seatTypeFareBuilder.append(seatTypeFare.getFare());
				seatTypeFareBuilder.append(Text.COMMA);
				stagefareMap.put(seatTypeFare.getBusSeatType().getCode(), seatTypeFare.getBusSeatType());
			}
		}
		else if (scheduleStageDTO.getBusSeatType() != null && scheduleStageDTO.getFare() != 0 && stagefareMap.get(scheduleStageDTO.getBusSeatType().getCode()) == null) {
			seatTypeFareBuilder.append(scheduleStageDTO.getBusSeatType().getId());
			seatTypeFareBuilder.append(Text.COLON);
			seatTypeFareBuilder.append(scheduleStageDTO.getFare());
			seatTypeFareBuilder.append(Text.COMMA);
		}
		else {
			seatTypeFareBuilder.append(Text.NA);
		}
		return seatTypeFareBuilder.toString();
	}

	private List<BusSeatTypeFareDTO> convertBusSeatTypeFareList(BusSeatTypeEM seatType, double fare) {
		List<BusSeatTypeFareDTO> busSeatTypeFareList = new ArrayList<>();
		BusSeatTypeFareDTO busSeatTypeFareDTO = new BusSeatTypeFareDTO();
		busSeatTypeFareDTO.setBusSeatType(seatType);
		busSeatTypeFareDTO.setFare(BigDecimal.valueOf(fare));
		busSeatTypeFareList.add(busSeatTypeFareDTO);
		return busSeatTypeFareList;
	}

	private List<BusSeatTypeFareDTO> convertBusSeatTypeFareList(String seatTypeFareDetails) {
		List<BusSeatTypeFareDTO> busSeatTypeFareList = new ArrayList<>();
		if (StringUtil.isNotNull(seatTypeFareDetails)) {
			List<String> busSeatTypefare = Arrays.asList(seatTypeFareDetails.split(Text.COMMA));
			for (String seatTypeFare : busSeatTypefare) {
				BusSeatTypeFareDTO busSeatTypeFareDTO = new BusSeatTypeFareDTO();
				busSeatTypeFareDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(Integer.parseInt(seatTypeFare.split(Text.COLON)[0])));
				busSeatTypeFareDTO.setFare(new BigDecimal(seatTypeFare.split(Text.COLON)[1]));
				busSeatTypeFareList.add(busSeatTypeFareDTO);
			}
		}
		return busSeatTypeFareList;
	}

	private Map<Integer, List<ScheduleStageDTO>> convertScheduleStageFares(List<ScheduleStageDTO> fareList) {
		Map<Integer, List<ScheduleStageDTO>> scheduleStageFareMap = new HashMap<>();
		try {
			for (ScheduleStageDTO scheduleStageFareDTO : fareList) {
				Map<String, ScheduleStageDTO> stageFareMap = new HashMap<>();
				for (BusSeatTypeFareDTO seatTypeFareDTO : scheduleStageFareDTO.getBusSeatTypeFare()) {
					BigDecimal fare = seatTypeFareDTO.getFare().setScale(0, BigDecimal.ROUND_DOWN);
					if (stageFareMap.get(seatTypeFareDTO.getBusSeatType().getId() + Text.UNDER_SCORE + fare) != null) {
						continue;
					}
					ScheduleStageDTO stageFareDTO = new ScheduleStageDTO();
					stageFareDTO.setId(scheduleStageFareDTO.getId());
					stageFareDTO.setCode(scheduleStageFareDTO.getCode());
					stageFareDTO.setActiveFlag(scheduleStageFareDTO.getActiveFlag());
					stageFareDTO.setLookupCode(scheduleStageFareDTO.getLookupCode());
					stageFareDTO.setActiveFrom(scheduleStageFareDTO.getActiveFrom());
					stageFareDTO.setActiveTo(scheduleStageFareDTO.getActiveTo());
					stageFareDTO.setDayOfWeek(scheduleStageFareDTO.getDayOfWeek());
					stageFareDTO.setFromStation(scheduleStageFareDTO.getFromStation());
					stageFareDTO.setToStation(scheduleStageFareDTO.getToStation());
					stageFareDTO.setGroup(scheduleStageFareDTO.getGroup());
					stageFareDTO.setBusSeatTypeFare(scheduleStageFareDTO.getBusSeatTypeFare());
					stageFareDTO.setSchedule(scheduleStageFareDTO.getSchedule());
					stageFareDTO.setActiveFrom(scheduleStageFareDTO.getActiveFrom());
					stageFareDTO.setActiveTo(scheduleStageFareDTO.getActiveTo());
					stageFareDTO.setDayOfWeek(scheduleStageFareDTO.getDayOfWeek());

					stageFareDTO.setFare(seatTypeFareDTO.getFare().doubleValue());
					stageFareDTO.setBusSeatType(seatTypeFareDTO.getBusSeatType());

					stageFareMap.put(seatTypeFareDTO.getBusSeatType().getId() + Text.UNDER_SCORE + fare, stageFareDTO);
				}

				if (scheduleStageFareDTO.getBusSeatTypeFare().isEmpty()) {
					stageFareMap.put(scheduleStageFareDTO.getBusSeatType().getId() + Text.UNDER_SCORE + scheduleStageFareDTO.getFare(), scheduleStageFareDTO);
				}

				scheduleStageFareMap.put(scheduleStageFareDTO.getId(), new ArrayList<>(stageFareMap.values()));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleStageFareMap;
	}

}
