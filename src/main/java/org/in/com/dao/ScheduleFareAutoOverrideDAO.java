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

import lombok.Cleanup;

import org.apache.commons.beanutils.BeanUtils;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.FareOverrideModeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class ScheduleFareAutoOverrideDAO {

	public List<ScheduleFareAutoOverrideDTO> get(AuthDTO authDTO, ScheduleFareAutoOverrideDTO seatVisibilityDTO) {
		List<ScheduleFareAutoOverrideDTO> overrideList = new ArrayList<ScheduleFareAutoOverrideDTO>();
		Map<Integer, ScheduleFareAutoOverrideDTO> scheduleMap = new HashMap<Integer, ScheduleFareAutoOverrideDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT sche.id, seat.id, seat.code, seat.active_from, seat.active_to, seat.day_of_week, seat.from_station_id, seat.to_station_id, seat.user_group_id, seat.route_station_id, seat.override_minutes, seat.fare, seat.bus_seat_type_id, bus_seat_type_fare, seat.lookup_id, seat.tag, seat.fare_override_mode_id, seat.active_flag, sche.code, sche.name, seat.updated_by, seat.updated_at FROM schedule_fare_auto_override seat,schedule sche WHERE sche.id = seat.schedule_id AND seat.namespace_id = ? AND sche.namespace_id = ? AND sche.code = ? AND seat.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, seatVisibilityDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = new ScheduleFareAutoOverrideDTO();
				fareOverrideDTO.setCode(selectRS.getString("code"));
				fareOverrideDTO.setId(selectRS.getInt("seat.id"));
				fareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				fareOverrideDTO.setLookupCode(selectRS.getString("lookup_id"));
				fareOverrideDTO.setActiveFrom(selectRS.getString("active_from"));
				fareOverrideDTO.setActiveTo(selectRS.getString("active_to"));
				fareOverrideDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				fareOverrideDTO.setOverrideMinutes(selectRS.getInt("override_minutes"));
				fareOverrideDTO.setTag(selectRS.getString("seat.tag"));
				fareOverrideDTO.setFareOverrideMode(FareOverrideModeEM.getFareOverrideModeEM(selectRS.getInt("seat.fare_override_mode_id")));
				seatVisibilityDTO.getSchedule().setId(selectRS.getInt("sche.id"));

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				fareOverrideDTO.setGroupList(groupList);

				List<RouteDTO> routeList = converRouteList(selectRS.getString("route_station_id"));
				fareOverrideDTO.setRouteList(routeList);

				List<BusSeatTypeEM> busSeatTypeList = convertBusSeatTypeList(selectRS.getString("bus_seat_type_id"));
				fareOverrideDTO.setBusSeatType(busSeatTypeList);

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("bus_seat_type_fare"));
				fareOverrideDTO.setBusSeatTypeFare(busSeatTypeFareList);

				fareOverrideDTO.setFare(selectRS.getBigDecimal("fare"));

				// Change Sche Fare to ScheV2 fare
				if (busSeatTypeFareList.isEmpty() && !StringUtil.isNotNull(busSeatTypeList)) {
					fareOverrideDTO.setBusSeatTypeFare(convertBusSeatTypeFareList(busSeatTypeList, fareOverrideDTO.getFare()));
				}

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("seat.updated_by"));
				AuditDTO auditDTO = new AuditDTO();
				auditDTO.setUpdatedAt(selectRS.getString("seat.updated_at"));
				auditDTO.setUser(user);
				fareOverrideDTO.setAudit(auditDTO);

				if (fareOverrideDTO.getLookupCode().equals("0")) {
					scheduleMap.put(fareOverrideDTO.getId(), fareOverrideDTO);
				}
				else {
					overrideList.add(fareOverrideDTO);
				}
			}
			for (ScheduleFareAutoOverrideDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleFareAutoOverrideDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleFareAutoOverrideDTO>(scheduleMap.values());

	}

	public ScheduleFareAutoOverrideDTO getIUD(AuthDTO authDTO, ScheduleFareAutoOverrideDTO visibilityDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_FARE_AUTO_OVERRIDE_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?)}");

			callableStatement.setString(++pindex, visibilityDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, visibilityDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, visibilityDTO.getActiveFrom() != null ? visibilityDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, visibilityDTO.getActiveTo() != null ? visibilityDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, visibilityDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, visibilityDTO.getOverrideMinutes());
			callableStatement.setString(++pindex, visibilityDTO.getGroups());
			callableStatement.setString(++pindex, visibilityDTO.getRouteStationList());
			callableStatement.setBigDecimal(++pindex, BigDecimal.ZERO);
			callableStatement.setString(++pindex, Text.NA);
			callableStatement.setString(++pindex, visibilityDTO.getBusSeatTypeFareDetails());
			callableStatement.setString(++pindex, visibilityDTO.getLookupCode());
			callableStatement.setString(++pindex, visibilityDTO.getTag());
			callableStatement.setInt(++pindex, visibilityDTO.getFareOverrideMode() != null ? visibilityDTO.getFareOverrideMode().getId() : FareOverrideModeEM.SEARCH_FARE.getId());
			callableStatement.setInt(++pindex, visibilityDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				visibilityDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				visibilityDTO.setCode(callableStatement.getString("pcrCode"));
			}
			callableStatement.clearParameters();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return visibilityDTO;
	}

	public void UpdateIUDV2(AuthDTO authDTO, List<ScheduleFareAutoOverrideDTO> scheduleFareAutoOverrideList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_FARE_AUTO_OVERRIDE_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?)}");
			for (ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : scheduleFareAutoOverrideList) {
				pindex = 0;
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getSchedule().getCode());
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getActiveFrom() != null ? fareAutoOverrideDTO.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getActiveTo() != null ? fareAutoOverrideDTO.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getDayOfWeek());
				callableStatement.setInt(++pindex, fareAutoOverrideDTO.getOverrideMinutes());
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getGroups());
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getRouteStationList());
				callableStatement.setBigDecimal(++pindex, BigDecimal.ZERO);
				callableStatement.setString(++pindex, Text.NA);
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getBusSeatTypeFareDetails());
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getLookupCode());
				callableStatement.setString(++pindex, fareAutoOverrideDTO.getTag());
				callableStatement.setInt(++pindex, fareAutoOverrideDTO.getFareOverrideMode() != null ? fareAutoOverrideDTO.getFareOverrideMode().getId() : FareOverrideModeEM.SEARCH_FARE.getId());
				callableStatement.setInt(++pindex, fareAutoOverrideDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					fareAutoOverrideDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					fareAutoOverrideDTO.setCode(callableStatement.getString("pcrCode"));
				}
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<ScheduleFareAutoOverrideDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleFareAutoOverrideDTO> overrideList = new ArrayList<ScheduleFareAutoOverrideDTO>();
		List<ScheduleFareAutoOverrideDTO> fareAutoOverrides = new ArrayList<>();
		List<ScheduleFareAutoOverrideDTO> fareAutoOverrideList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id, seat.code, seat.active_from, seat.active_to, seat.day_of_week, seat.override_minutes, seat.user_group_id, seat.route_station_id, seat.fare, bus_seat_type_id, bus_seat_type_fare, seat.lookup_id, seat.tag, seat.fare_override_mode_id, seat.active_flag FROM schedule_fare_auto_override seat WHERE seat.namespace_id = ? AND seat.active_flag  = 1 AND seat.schedule_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = new ScheduleFareAutoOverrideDTO();
				fareOverrideDTO.setId(selectRS.getInt("seat.id"));
				fareOverrideDTO.setCode(selectRS.getString("code"));
				fareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				fareOverrideDTO.setLookupCode(selectRS.getString("lookup_id"));
				fareOverrideDTO.setActiveFrom(selectRS.getString("active_from"));
				fareOverrideDTO.setActiveTo(selectRS.getString("active_to"));
				fareOverrideDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				fareOverrideDTO.setOverrideMinutes(selectRS.getInt("override_minutes"));
				fareOverrideDTO.setTag(selectRS.getString("seat.tag"));
				fareOverrideDTO.setFareOverrideMode(FareOverrideModeEM.getFareOverrideModeEM(selectRS.getInt("seat.fare_override_mode_id")));

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				fareOverrideDTO.setGroupList(groupList);

				List<RouteDTO> routeList = converRouteList(selectRS.getString("route_station_id"));
				fareOverrideDTO.setRouteList(routeList);

				List<BusSeatTypeEM> busSeatTypeList = convertBusSeatTypeList(selectRS.getString("bus_seat_type_id"));
				fareOverrideDTO.setBusSeatType(busSeatTypeList);

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("bus_seat_type_fare"));
				fareOverrideDTO.setBusSeatTypeFare(busSeatTypeFareList);

				fareOverrideDTO.setFare(selectRS.getBigDecimal("fare"));

				if (fareOverrideDTO.getLookupCode().equals("0")) {
					fareAutoOverrides.add(fareOverrideDTO);
				}
				else {
					for (BusSeatTypeFareDTO seatTypeFareDTO : fareOverrideDTO.getBusSeatTypeFare()) {
						if (seatTypeFareDTO.getFare().compareTo(BigDecimal.ZERO) < 0) {
							fareOverrideDTO.setFare(StringUtil.getBigDecimalValue("-1"));
							break;
						}
					}
					overrideList.add(fareOverrideDTO);
				}
			}

			Map<Integer, List<ScheduleFareAutoOverrideDTO>> scheduleFareAutoOverrides = convertScheduleFareAutoOverrides(fareAutoOverrides);
			for (ScheduleFareAutoOverrideDTO overrideScheduleDTO : overrideList) {
				List<ScheduleFareAutoOverrideDTO> scheduleFareOverrides = scheduleFareAutoOverrides.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
				if (scheduleFareOverrides != null && !scheduleFareOverrides.isEmpty()) {
					for (ScheduleFareAutoOverrideDTO dto : scheduleFareOverrides) {
						dto.getOverrideList().add(overrideScheduleDTO);
					}
				}
			}

			for (Entry<Integer, List<ScheduleFareAutoOverrideDTO>> entry : scheduleFareAutoOverrides.entrySet()) {
				List<ScheduleFareAutoOverrideDTO> scheduleFareOverrides = entry.getValue();
				fareAutoOverrideList.addAll(scheduleFareOverrides);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return fareAutoOverrideList;
	}

	// only for Get Search fare asitis
	public List<ScheduleFareAutoOverrideDTO> getByScheduleIdV2(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleFareAutoOverrideDTO> fareAutoOverrides = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id, seat.code, seat.active_from, seat.active_to, seat.day_of_week, seat.override_minutes, seat.user_group_id, seat.route_station_id, seat.fare, bus_seat_type_id, bus_seat_type_fare, seat.lookup_id, seat.tag, seat.fare_override_mode_id, seat.active_flag FROM schedule_fare_auto_override seat WHERE seat.namespace_id = ? AND seat.active_flag  = 1 AND seat.schedule_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = new ScheduleFareAutoOverrideDTO();
				fareOverrideDTO.setId(selectRS.getInt("seat.id"));
				fareOverrideDTO.setCode(selectRS.getString("code"));
				fareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				fareOverrideDTO.setLookupCode(selectRS.getString("lookup_id"));
				fareOverrideDTO.setActiveFrom(selectRS.getString("active_from"));
				fareOverrideDTO.setActiveTo(selectRS.getString("active_to"));
				fareOverrideDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				fareOverrideDTO.setOverrideMinutes(selectRS.getInt("override_minutes"));
				fareOverrideDTO.setTag(selectRS.getString("seat.tag"));
				fareOverrideDTO.setFareOverrideMode(FareOverrideModeEM.getFareOverrideModeEM(selectRS.getInt("seat.fare_override_mode_id")));

				if (fareOverrideDTO.getFareOverrideMode().getId() != FareOverrideModeEM.SEARCH_FARE.getId()) {
					continue;
				}

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				fareOverrideDTO.setGroupList(groupList);

				List<RouteDTO> routeList = converRouteList(selectRS.getString("route_station_id"));
				fareOverrideDTO.setRouteList(routeList);

				List<BusSeatTypeFareDTO> busSeatTypeFareList = convertBusSeatTypeFareList(selectRS.getString("bus_seat_type_fare"));
				fareOverrideDTO.setBusSeatTypeFare(busSeatTypeFareList);

				if (!fareOverrideDTO.getLookupCode().equals("0")) {
					continue;
				}
				fareAutoOverrides.add(fareOverrideDTO);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return fareAutoOverrides;
	}

	public void updateSearchFareAutoOverride(AuthDTO authDTO, ScheduleTripStageFareDTO quickFareOverrideDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_TRIP_STAGE_FARE_IUD(?,?,?,?,? ,?,?,?,?)}");

			callableStatement.setString(++pindex, quickFareOverrideDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, quickFareOverrideDTO.getFareDetails());
			callableStatement.setString(++pindex, quickFareOverrideDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, DateUtil.convertDate(quickFareOverrideDTO.getSchedule().getTripDate()));
			callableStatement.setInt(++pindex, quickFareOverrideDTO.getActiveFlag());
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

	public void updateQuickFare(AuthDTO authDTO, List<ScheduleTripStageFareDTO> scheduleTripStageFares) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE schedule_trip_stage_fare SET active_flag = 0 WHERE code = ? AND active_flag = 1");
			for (ScheduleTripStageFareDTO quickFareOverrideDTO : scheduleTripStageFares) {
				selectPS.setString(1, quickFareOverrideDTO.getCode());
				selectPS.addBatch();
			}
			selectPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public ScheduleTripStageFareDTO getSearchScheduleFareAutoOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
		try {
			if (scheduleDTO.getId() == 0 || scheduleDTO.getTripDate() == null) {
				System.out.println("Error QICKFRE01" + scheduleDTO.getCode() + " " + scheduleDTO.getId() + " " + authDTO.getNamespaceCode());
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, route_fare_details, schedule_id, trip_date, active_flag FROM schedule_trip_stage_fare WHERE code = ? AND active_flag = 1");
			selectPS.setString(1, BitsUtil.getGeneratedTripCodeV2(authDTO, scheduleDTO, scheduleDTO.getTripDate()));

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				quickFareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				if (quickFareOverrideDTO.getActiveFlag() == 1) {
					quickFareOverrideDTO.setId(selectRS.getInt("id"));
					quickFareOverrideDTO.setCode(selectRS.getString("code"));
					quickFareOverrideDTO.setTripDate(selectRS.getString("trip_date"));
					quickFareOverrideDTO.setFareDetails(selectRS.getString("route_fare_details"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return quickFareOverrideDTO;
	}

	public ScheduleTripStageFareDTO getSearchScheduleFareAutoOverride(AuthDTO authDTO, ScheduleTripStageFareDTO quickFareOverrideDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, route_fare_details, schedule_id, trip_date, active_flag FROM schedule_trip_stage_fare WHERE code = ? AND active_flag = 1");
			selectPS.setString(1, quickFareOverrideDTO.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				quickFareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				if (quickFareOverrideDTO.getActiveFlag() == 1) {
					quickFareOverrideDTO.setId(selectRS.getInt("id"));
					quickFareOverrideDTO.setCode(selectRS.getString("code"));
					quickFareOverrideDTO.setTripDate(selectRS.getString("trip_date"));
					quickFareOverrideDTO.setFareDetails(selectRS.getString("route_fare_details"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return quickFareOverrideDTO;
	}

	public List<ScheduleTripStageFareDTO> getScheduleTripStageFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate, String tripCode) {
		List<ScheduleTripStageFareDTO> list = new ArrayList<ScheduleTripStageFareDTO>();
		try {
			String scheduleId = "";
			if (scheduleDTO != null && scheduleDTO.getId() != 0) {
				scheduleId = " AND schedule_id = " + scheduleDTO.getId();
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(tripCode)) {
				selectPS = connection.prepareStatement("SELECT id, code, route_fare_details, schedule_id, trip_date, updated_by, updated_at, active_flag FROM schedule_trip_stage_fare WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, tripCode);
			}
			else if (DateUtil.isValidDateV2(fromDate) && DateUtil.isValidDateV2(toDate)) {
				selectPS = connection.prepareStatement("SELECT id, code, route_fare_details, schedule_id, trip_date, updated_by, updated_at, active_flag FROM schedule_trip_stage_fare WHERE namespace_id = ? " + scheduleId + " AND trip_date BETWEEN ? AND ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, fromDate);
				selectPS.setString(3, toDate);
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
				quickFareOverrideDTO.setId(selectRS.getInt("id"));
				quickFareOverrideDTO.setCode(selectRS.getString("code"));
				quickFareOverrideDTO.setTripDate(selectRS.getString("trip_date"));
				quickFareOverrideDTO.setFareDetails(selectRS.getString("route_fare_details"));
				quickFareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("schedule_id"));
				quickFareOverrideDTO.setSchedule(schedule);

				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(selectRS.getInt("updated_by"));

				AuditDTO audit = new AuditDTO();
				audit.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("updated_at")).format("YYYY-MM-DD hh:mm:ss"));
				audit.setUser(updatedBy);
				quickFareOverrideDTO.setAudit(audit);

				list.add(quickFareOverrideDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<ScheduleTripStageFareDTO> getScheduleTripStageFares(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate) {
		List<ScheduleTripStageFareDTO> scheduleTripStageFares = new ArrayList<ScheduleTripStageFareDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;

			if (DateUtil.isValidDateV2(fromDate) && DateUtil.isValidDateV2(toDate)) {
				selectPS = connection.prepareStatement("SELECT id, code, route_fare_details, schedule_id, trip_date, updated_by, updated_at, active_flag FROM schedule_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND trip_date >= ? AND trip_date <= ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, scheduleDTO.getId());
				selectPS.setString(3, fromDate);
				selectPS.setString(4, toDate);
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, code, route_fare_details, schedule_id, trip_date, updated_by, updated_at, active_flag FROM schedule_trip_stage_fare WHERE namespace_id = ? AND schedule_id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, scheduleDTO.getId());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
				quickFareOverrideDTO.setId(selectRS.getInt("id"));
				quickFareOverrideDTO.setCode(selectRS.getString("code"));
				quickFareOverrideDTO.setTripDate(selectRS.getString("trip_date"));
				quickFareOverrideDTO.setFareDetails(selectRS.getString("route_fare_details"));
				quickFareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("schedule_id"));
				quickFareOverrideDTO.setSchedule(schedule);

				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(selectRS.getInt("updated_by"));

				AuditDTO audit = new AuditDTO();
				audit.setUpdatedAt(DateUtil.convertDateTime(DateUtil.getDateTime(selectRS.getString("updated_at"))));
				audit.setUser(updatedBy);
				quickFareOverrideDTO.setAudit(audit);

				scheduleTripStageFares.add(quickFareOverrideDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleTripStageFares;
	}
	
	public void saveScheduleTripStageFareLog(AuthDTO authDTO, List<ScheduleTripStageFareDTO> fareList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			
			int index = 0;
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("INSERT INTO schedule_trip_stage_fare_log (namespace_id, trip_code, schedule_id, from_station_id, to_station_id, trip_date, event, log1, active_flag, updated_by, updated_at) VALUES(?,?,?,?,? ,?,?,?,1,? ,NOW())");
			for (ScheduleTripStageFareDTO tripStageDfareDTO : fareList) {
				index = 0;
				psTrip.setInt(++index, authDTO.getNamespace().getId());
				psTrip.setString(++index, tripStageDfareDTO.getCode());
				psTrip.setInt(++index, tripStageDfareDTO.getSchedule().getId());
				psTrip.setInt(++index, tripStageDfareDTO.getRoute().getFromStation().getId());
				psTrip.setInt(++index, tripStageDfareDTO.getRoute().getToStation().getId());
				psTrip.setString(++index, tripStageDfareDTO.getTripDate());
				psTrip.setString(++index, tripStageDfareDTO.getName());
				psTrip.setString(++index, tripStageDfareDTO.getFareDetails());
				psTrip.setInt(++index, authDTO.getUser().getId());
				psTrip.addBatch();
			}
			psTrip.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public List<ScheduleTripStageFareDTO> getScheduleTripStageFareHistory(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate, String tripCode) {
		List<ScheduleTripStageFareDTO> list = new ArrayList<ScheduleTripStageFareDTO>();
		try {
			String scheduleId = "";
			if (scheduleDTO != null && scheduleDTO.getId() != 0) {
				scheduleId = " AND schedule_id = " + scheduleDTO.getId();
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(tripCode)) {
				selectPS = connection.prepareStatement("SELECT id, trip_code, schedule_id, from_station_id, to_station_id, trip_date, event, log1, updated_by, updated_at, active_flag FROM schedule_trip_stage_fare_log WHERE namespace_id = ? AND trip_code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, tripCode);
			}
			else if (DateUtil.isValidDateV2(fromDate) && DateUtil.isValidDateV2(toDate)) {
				selectPS = connection.prepareStatement("SELECT id, trip_code, schedule_id, from_station_id, to_station_id, trip_date, event, log1, updated_by, updated_at, active_flag FROM schedule_trip_stage_fare_log WHERE namespace_id = ? " + scheduleId + " AND trip_date BETWEEN ? AND ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, fromDate);
				selectPS.setString(3, toDate);
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
				quickFareOverrideDTO.setId(selectRS.getInt("id"));
				quickFareOverrideDTO.setCode(selectRS.getString("trip_code"));
				quickFareOverrideDTO.setTripDate(selectRS.getString("trip_date"));
				quickFareOverrideDTO.setFareDetails(selectRS.getString("log1"));
				quickFareOverrideDTO.setActiveFlag(selectRS.getInt("active_flag"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("schedule_id"));
				quickFareOverrideDTO.setSchedule(schedule);
				
				StationDTO fromStation = new StationDTO();
				fromStation.setId(selectRS.getInt("from_station_id"));
				
				StationDTO toStation = new StationDTO();
				toStation.setId(selectRS.getInt("to_station_id"));

				RouteDTO routeDTO = new RouteDTO();
				routeDTO.setFromStation(fromStation);
				routeDTO.setToStation(toStation);
				quickFareOverrideDTO.setRoute(routeDTO);
				
				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(selectRS.getInt("updated_by"));

				AuditDTO audit = new AuditDTO();
				audit.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("updated_at")).format("YYYY-MM-DD hh:mm:ss"));
				audit.setUser(updatedBy);
				quickFareOverrideDTO.setAudit(audit);

				list.add(quickFareOverrideDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	private Map<Integer, List<ScheduleFareAutoOverrideDTO>> convertScheduleFareAutoOverrides(List<ScheduleFareAutoOverrideDTO> overrideList) {
		Map<Integer, List<ScheduleFareAutoOverrideDTO>> scheduleFareAutoOverrides = new HashMap<>();
		try {
			for (ScheduleFareAutoOverrideDTO scheduleFareAutoOverrideDTO : overrideList) {
				Map<BigDecimal, ScheduleFareAutoOverrideDTO> fareOverrideMap = new HashMap<>();
				for (BusSeatTypeFareDTO seatTypeFareDTO : scheduleFareAutoOverrideDTO.getBusSeatTypeFare()) {
					BigDecimal fare = seatTypeFareDTO.getFare().setScale(0, BigDecimal.ROUND_DOWN);
					if (fareOverrideMap.get(fare) == null) {
						ScheduleFareAutoOverrideDTO scheduleFareAutoOverride = new ScheduleFareAutoOverrideDTO();
						BeanUtils.copyProperties(scheduleFareAutoOverride, scheduleFareAutoOverrideDTO);

						scheduleFareAutoOverride.setFare(seatTypeFareDTO.getFare());

						List<BusSeatTypeEM> busSeatTypes = new ArrayList<>();
						busSeatTypes.add(seatTypeFareDTO.getBusSeatType());
						scheduleFareAutoOverride.setBusSeatType(busSeatTypes);

						fareOverrideMap.put(fare, scheduleFareAutoOverride);
					}
					else {
						ScheduleFareAutoOverrideDTO fareAutoOverrideDTO = fareOverrideMap.get(fare);
						fareAutoOverrideDTO.getBusSeatType().add(seatTypeFareDTO.getBusSeatType());
						fareOverrideMap.put(fare, fareAutoOverrideDTO);
					}
				}

				if (scheduleFareAutoOverrideDTO.getBusSeatTypeFare().isEmpty()) {
					fareOverrideMap.put(scheduleFareAutoOverrideDTO.getFare(), scheduleFareAutoOverrideDTO);
				}

				scheduleFareAutoOverrides.put(scheduleFareAutoOverrideDTO.getId(), new ArrayList<>(fareOverrideMap.values()));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleFareAutoOverrides;
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

	private List<BusSeatTypeEM> convertBusSeatTypeList(String seatTypes) {
		List<BusSeatTypeEM> busSeatTypeList = new ArrayList<>();
		if (StringUtil.isNotNull(seatTypes)) {
			List<String> busSeatTypeIds = Arrays.asList(seatTypes.split(Text.COMMA));
			if (busSeatTypeIds != null) {
				for (String busSeatTypeId : busSeatTypeIds) {
					if (StringUtil.isNull(busSeatTypeId) || busSeatTypeId.equals(Numeric.ZERO)) {
						continue;
					}
					busSeatTypeList.add(BusSeatTypeEM.getBusSeatTypeEM(Integer.valueOf(busSeatTypeId)));
				}
			}
		}
		return busSeatTypeList;
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

	private List<BusSeatTypeFareDTO> convertBusSeatTypeFareList(List<BusSeatTypeEM> busSeatTypeList, BigDecimal fare) {
		List<BusSeatTypeFareDTO> busSeatTypeFareList = new ArrayList<>();
		for (BusSeatTypeEM seatType : busSeatTypeList) {
			BusSeatTypeFareDTO busSeatTypeFareDTO = new BusSeatTypeFareDTO();
			busSeatTypeFareDTO.setBusSeatType(seatType);
			busSeatTypeFareDTO.setFare(fare);
			busSeatTypeFareList.add(busSeatTypeFareDTO);
		}
		return busSeatTypeFareList;
	}

}
