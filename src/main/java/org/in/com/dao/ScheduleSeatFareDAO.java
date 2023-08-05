package org.in.com.dao;

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

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleSeatFareDAO {

	public List<ScheduleSeatFareDTO> get(AuthDTO authDTO, ScheduleSeatFareDTO seatVisibilityDTO) {
		List<ScheduleSeatFareDTO> overrideList = new ArrayList<ScheduleSeatFareDTO>();
		Map<Integer, ScheduleSeatFareDTO> scheduleMap = new HashMap<Integer, ScheduleSeatFareDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.bus_id,seat.active_from,seat.active_to,seat.day_of_week,seat.route_station_id,seat.user_group_id,seat.seat_fare,seat.fare_type_id,fare_override_type_id, seat.seat_codes,seat.lookup_id,seat.active_flag,sche.code,sche.name FROM schedule_seat_fare seat,schedule sche WHERE sche.id = seat.schedule_id AND seat.namespace_id = ? AND sche.namespace_id = ? AND sche.code = ? AND seat.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, seatVisibilityDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatFareDTO seatFareDTO = new ScheduleSeatFareDTO();
				seatFareDTO.setCode(selectRS.getString("code"));
				seatFareDTO.setId(selectRS.getInt("seat.id"));
				seatFareDTO.setLookupCode(selectRS.getString("lookup_id"));
				seatFareDTO.setActiveFlag(selectRS.getInt("active_flag"));
				seatFareDTO.setActiveFrom(selectRS.getString("active_from"));
				seatFareDTO.setActiveTo(selectRS.getString("active_to"));
				seatFareDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				seatFareDTO.setRoutes(converRouteList(selectRS.getString("route_station_id")));
				seatFareDTO.setGroups(convertGroupList(selectRS.getString("user_group_id")));
				seatFareDTO.setSeatFare(selectRS.getBigDecimal("seat_fare"));
				seatFareDTO.setFareType(FareTypeEM.getFareTypeEM(selectRS.getInt("fare_type_id")));
				seatFareDTO.setFareOverrideType(FareOverrideTypeEM.getFareOverrideTypeEM(selectRS.getInt("fare_override_type_id")));
				
				String busSeatCode = selectRS.getString("seat_codes");
				List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
				if (StringUtil.isNotNull(busSeatCode)) {
					String[] seatCodes = busSeatCode.split(",");
					for (String seatCode : seatCodes) {
						if (StringUtil.isNotNull(seatCode)) {
							BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
							busSeatTypeDTO.setCode(seatCode);
							seatlist.add(busSeatTypeDTO);
						}
					}
				}
				BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
				busSeatTypeDTO.setList(seatlist);
				BusDTO busDTO = new BusDTO();
				busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
				busDTO.setId(selectRS.getInt("bus_id"));
				seatFareDTO.setBus(busDTO);
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				scheduleDTO.setName(selectRS.getString("sche.name"));
				seatFareDTO.setSchedule(scheduleDTO);
				if (seatFareDTO.getLookupCode().equals("0")) {
					scheduleMap.put(seatFareDTO.getId(), seatFareDTO);
				}
				else {
					overrideList.add(seatFareDTO);
				}
			}
			for (ScheduleSeatFareDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleSeatFareDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleSeatFareDTO>(scheduleMap.values());

	}

	public ScheduleSeatFareDTO getIUD(AuthDTO authDTO, ScheduleSeatFareDTO seatFareDTO) {
		try {
			StringBuilder seatCodes = new StringBuilder();
			if (seatFareDTO.getBus() != null && seatFareDTO.getBus().getBusSeatLayoutDTO() != null && seatFareDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
				for (BusSeatLayoutDTO layoutDTO : seatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (seatCodes.length() > 0) {
						seatCodes.append(",");
					}
					if (seatCodes.length() > 240) {
						continue;
					}
					seatCodes.append(layoutDTO.getCode());
				}
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_SEAT_FARE_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?)}");

			callableStatement.setString(++pindex, seatFareDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, seatFareDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, seatFareDTO.getBus().getCode());
			callableStatement.setString(++pindex, seatFareDTO.getActiveFrom() != null ? seatFareDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, seatFareDTO.getActiveTo() != null ? seatFareDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, seatFareDTO.getDayOfWeek());
			callableStatement.setString(++pindex, seatFareDTO.getRouteStationList());
			callableStatement.setString(++pindex, seatFareDTO.getGroupIds());
			callableStatement.setBigDecimal(++pindex, seatFareDTO.getSeatFare());
			callableStatement.setInt(++pindex, seatFareDTO.getFareType() != null ? seatFareDTO.getFareType().getId() : 0);
			callableStatement.setInt(++pindex, seatFareDTO.getFareOverrideType() != null ? seatFareDTO.getFareOverrideType().getId() : 0);
			callableStatement.setString(++pindex, seatCodes.toString());
			callableStatement.setString(++pindex, seatFareDTO.getLookupCode());
			callableStatement.setInt(++pindex, seatFareDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				seatFareDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				seatFareDTO.setCode(callableStatement.getString("pcrCode"));
			}
			callableStatement.clearParameters();
			seatFareDTO.setSchedule(seatFareDTO.getSchedule());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return seatFareDTO;
	}

	public List<ScheduleSeatFareDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleSeatFareDTO> overrideList = new ArrayList<ScheduleSeatFareDTO>();
		Map<Integer, ScheduleSeatFareDTO> scheduleMap = new HashMap<Integer, ScheduleSeatFareDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.bus_id,seat.active_from,seat.active_to,seat.day_of_week,seat.route_station_id,seat.user_group_id,seat.seat_fare,seat.fare_type_id,fare_override_type_id,seat.seat_codes,seat.lookup_id,seat.active_flag FROM schedule_seat_fare seat WHERE seat.namespace_id = ? AND seat.active_flag  = 1 AND seat.schedule_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatFareDTO seatFareDTO = new ScheduleSeatFareDTO();
				seatFareDTO.setId(selectRS.getInt("seat.id"));
				seatFareDTO.setCode(selectRS.getString("code"));
				seatFareDTO.setActiveFlag(selectRS.getInt("active_flag"));
				seatFareDTO.setLookupCode(selectRS.getString("lookup_id"));
				seatFareDTO.setActiveFrom(selectRS.getString("active_from"));
				seatFareDTO.setActiveTo(selectRS.getString("active_to"));
				seatFareDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				seatFareDTO.setRoutes(converRouteList(selectRS.getString("route_station_id")));
				seatFareDTO.setGroups(convertGroupList(selectRS.getString("user_group_id")));
				seatFareDTO.setSeatFare(selectRS.getBigDecimal("seat_fare"));
				seatFareDTO.setFareType(FareTypeEM.getFareTypeEM(selectRS.getInt("fare_type_id")));
				seatFareDTO.setFareOverrideType(FareOverrideTypeEM.getFareOverrideTypeEM(selectRS.getInt("fare_override_type_id")));
				
				String busSeatCode = selectRS.getString("seat_codes");
				List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
				if (StringUtil.isNotNull(busSeatCode)) {
					String[] seatCodes = busSeatCode.split(",");
					for (String seatCode : seatCodes) {
						if (StringUtil.isNotNull(seatCode)) {
							BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
							busSeatTypeDTO.setCode(seatCode);
							seatlist.add(busSeatTypeDTO);
						}
					}
				}
				BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
				busSeatTypeDTO.setList(seatlist);
				BusDTO busDTO = new BusDTO();
				busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
				busDTO.setId(selectRS.getInt("bus_id"));
				seatFareDTO.setBus(busDTO);
				seatFareDTO.setSchedule(scheduleDTO);

				if (seatFareDTO.getLookupCode().equals("0")) {
					scheduleMap.put(seatFareDTO.getId(), seatFareDTO);
				}
				else {
					overrideList.add(seatFareDTO);
				}
			}
			for (ScheduleSeatFareDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleSeatFareDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleSeatFareDTO>(scheduleMap.values());

	}

	private List<GroupDTO> convertGroupList(String groups) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groups)) {
			List<String> groupIds = Arrays.asList(groups.split(Text.COMMA));
			if (groupIds != null) {
				for (String groupId : groupIds) {
					if (StringUtil.isNull(groupId) || groupId.equals(Numeric.ZERO)) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(Integer.valueOf(groupId));
					groupList.add(groupDTO);
				}
			}
		}
		return groupList;
	}

	private List<RouteDTO> converRouteList(String routes) {
		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (StringUtil.isNotNull(routes)) {
			for (String stationId : routes.split(",")) {
				if (StringUtil.isNull(stationId.trim())) {
					continue;
				}
				int fromStationId = Integer.valueOf(stationId.split("_")[0]);
				int toStationId = Integer.valueOf(stationId.split("_")[1]);
				if (fromStationId == 0 || toStationId == 0) {
					continue;
				}
				RouteDTO routeDTO = new RouteDTO();
				StationDTO fromStation = new StationDTO();
				fromStation.setId(fromStationId);
				routeDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(toStationId);
				routeDTO.setToStation(toStation);
				routeList.add(routeDTO);
			}
		}
		return routeList;
	}
}
