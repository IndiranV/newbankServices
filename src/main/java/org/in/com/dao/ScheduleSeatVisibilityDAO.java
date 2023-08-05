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

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;

public class ScheduleSeatVisibilityDAO {

	public List<ScheduleSeatVisibilityDTO> get(AuthDTO authDTO, ScheduleSeatVisibilityDTO seatVisibilityDTO) {
		List<ScheduleSeatVisibilityDTO> overrideList = new ArrayList<ScheduleSeatVisibilityDTO>();
		Map<Integer, ScheduleSeatVisibilityDTO> scheduleMap = new HashMap<Integer, ScheduleSeatVisibilityDTO>();
		try {
			UserDAO userDAO = new UserDAO();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.bus_id,from_station_id,to_station_id,seat.refference_id,seat.refference_type,seat.active_from,seat.active_to,seat.day_of_week,release_minutes,seat.visibility_type,seat.seat_codes,seat.additional_attribute,seat.lookup_id,seat.active_flag,seat.updated_by,seat.updated_at,sche.code,sche.name, seat.remarks FROM schedule_seat_visibility seat,schedule sche WHERE sche.id = seat.schedule_id AND seat.namespace_id = ? AND sche.namespace_id = ? AND sche.code = ? AND seat.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, seatVisibilityDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatVisibilityDTO visibilityDTO = new ScheduleSeatVisibilityDTO();
				visibilityDTO.setCode(selectRS.getString("code"));
				visibilityDTO.setId(selectRS.getInt("seat.id"));
				visibilityDTO.setActiveFlag(selectRS.getInt("active_flag"));

				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				fromStationDTO.setId(selectRS.getInt("from_station_id"));
				toStationDTO.setId(selectRS.getInt("to_station_id"));

				visibilityDTO.setLookupCode(selectRS.getString("lookup_id"));
				visibilityDTO.setActiveFrom(selectRS.getString("active_from"));
				visibilityDTO.setActiveTo(selectRS.getString("active_to"));
				visibilityDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				visibilityDTO.setReleaseMinutes(selectRS.getInt("release_minutes"));
				String busSeatCode = selectRS.getString("seat_codes");
				List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
				if (StringUtil.isNotNull(busSeatCode)) {
					String[] seatCodes = busSeatCode.split(Text.COMMA);
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
				visibilityDTO.setBus(busDTO);
				visibilityDTO.setRefferenceType(selectRS.getString("seat.refference_type"));
				visibilityDTO.setVisibilityType(selectRS.getString("seat.visibility_type"));
				visibilityDTO.setUpdatedAt(selectRS.getString("seat.updated_at"));
				visibilityDTO.setRemarks(selectRS.getString("seat.remarks"));
				UserDTO updatedByUserDTO = userDAO.getUsersDTO(connection, selectRS.getInt("seat.updated_by"));
				visibilityDTO.setUpdatedBy(updatedByUserDTO.getName());
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				scheduleDTO.setName(selectRS.getString("sche.name"));

				String reffernceIds = selectRS.getString("seat.refference_id");
				if (visibilityDTO.getRefferenceType().equals("GR")) {
					List<GroupDTO> groupList = convertGroupList(reffernceIds);
					visibilityDTO.setGroupList(groupList);
				}
				else if (visibilityDTO.getRefferenceType().equals("UR")) {
					List<UserDTO> userList = convertUserList(reffernceIds);
					visibilityDTO.setUserList(userList);
				}
				else if (visibilityDTO.getRefferenceType().equals("SG")) {
					List<RouteDTO> routeList = convertRouteList(reffernceIds);
					if (fromStationDTO.getId() != 0 && toStationDTO.getId() != 0) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setFromStation(fromStationDTO);
						routeDTO.setToStation(toStationDTO);
						routeList.add(routeDTO);
					}
					visibilityDTO.setRouteList(routeList);
					
					List<UserDTO> routeUsers = convertUserCodesToList(selectRS.getString("seat.additional_attribute"));
					visibilityDTO.setRouteUsers(routeUsers);
				}
				else if (visibilityDTO.getRefferenceType().equals("BR")) {
					List<OrganizationDTO> organizationList = convertOrganizationList(reffernceIds);
					visibilityDTO.setOrganizations(organizationList);
				}
				visibilityDTO.setSchedule(scheduleDTO);

				if (visibilityDTO.getLookupCode().equals("0")) {
					scheduleMap.put(visibilityDTO.getId(), visibilityDTO);
				}
				else {
					overrideList.add(visibilityDTO);
				}
			}
			for (ScheduleSeatVisibilityDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleSeatVisibilityDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleSeatVisibilityDTO>(scheduleMap.values());
	}

	public ScheduleSeatVisibilityDTO getIUD(AuthDTO authDTO, ScheduleSeatVisibilityDTO visibilityDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_SEAT_VISIBILITY_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?  ,?,?,?)}");
			for (ScheduleSeatVisibilityDTO dto : visibilityDTO.getList()) {
				pindex = 0;
				String referenceIds = null;
				if (dto.getRefferenceType().equals("GR")) {
					referenceIds = dto.getGroups();
				}
				else if (dto.getRefferenceType().equals("UR")) {
					referenceIds = dto.getUsers();
				}
				else if (dto.getRefferenceType().equals("SG")) {
					referenceIds = dto.getRouteStationList();
				}
				else if (dto.getRefferenceType().equals("BR")) {
					referenceIds = dto.getOrganizationIds();
				}
				StringBuilder seatCodes = new StringBuilder();
				if (dto.getBus() != null && dto.getBus().getBusSeatLayoutDTO() != null && dto.getBus().getBusSeatLayoutDTO().getList() != null) {
					for (BusSeatLayoutDTO layoutDTO : dto.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatCodes.length() > 0) {
							seatCodes.append(",");
						}
						seatCodes.append(layoutDTO.getCode());
					}
				}
				callableStatement.setString(++pindex, dto.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, dto.getSchedule().getCode());
				callableStatement.setString(++pindex, dto.getBus().getCode());
				callableStatement.setInt(++pindex, 0);
				callableStatement.setInt(++pindex, 0);
				callableStatement.setString(++pindex, referenceIds);
				callableStatement.setString(++pindex, dto.getRefferenceType());
				callableStatement.setString(++pindex, dto.getActiveFrom() != null ? dto.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, dto.getActiveTo() != null ? dto.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, dto.getDayOfWeek());
				callableStatement.setInt(++pindex, dto.getReleaseMinutes());
				callableStatement.setString(++pindex, dto.getVisibilityType());
				callableStatement.setString(++pindex, seatCodes.toString());
				callableStatement.setString(++pindex, dto.getRouteUsersCodes());
				callableStatement.setString(++pindex, dto.getLookupCode());
				callableStatement.setString(++pindex, dto.getRemarks());
				callableStatement.setInt(++pindex, dto.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					visibilityDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					visibilityDTO.setCode(callableStatement.getString("pcrCode"));
				}
				callableStatement.clearParameters();
				visibilityDTO.setSchedule(dto.getSchedule());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return visibilityDTO;
	}

	public List<ScheduleSeatVisibilityDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleSeatVisibilityDTO> overrideList = new ArrayList<ScheduleSeatVisibilityDTO>();
		Map<Integer, ScheduleSeatVisibilityDTO> scheduleMap = new HashMap<Integer, ScheduleSeatVisibilityDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.bus_id,from_station_id,to_station_id,seat.refference_id,seat.refference_type,seat.active_from,seat.active_to,seat.day_of_week,release_minutes,seat.visibility_type,seat.seat_codes,seat.additional_attribute,seat.lookup_id,seat.active_flag, seat.remarks, seat.updated_by, seat.updated_at  FROM schedule_seat_visibility seat WHERE seat.namespace_id = ? AND seat.active_flag  = 1 AND seat.schedule_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatVisibilityDTO visibilityDTO = new ScheduleSeatVisibilityDTO();
				visibilityDTO.setId(selectRS.getInt("seat.id"));
				visibilityDTO.setCode(selectRS.getString("code"));
				visibilityDTO.setActiveFlag(selectRS.getInt("active_flag"));

				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				fromStationDTO.setId(selectRS.getInt("from_station_id"));
				toStationDTO.setId(selectRS.getInt("to_station_id"));

				visibilityDTO.setLookupCode(selectRS.getString("lookup_id"));
				visibilityDTO.setActiveFrom(selectRS.getString("active_from"));
				visibilityDTO.setActiveTo(selectRS.getString("active_to"));
				visibilityDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				visibilityDTO.setRemarks(selectRS.getString("remarks"));
				visibilityDTO.setReleaseMinutes(selectRS.getInt("release_minutes"));
				visibilityDTO.setUpdatedBy(selectRS.getString("seat.updated_by"));
				visibilityDTO.setUpdatedAt(selectRS.getString("seat.updated_at"));

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
				visibilityDTO.setBus(busDTO);
				visibilityDTO.setRefferenceType(selectRS.getString("seat.refference_type"));
				visibilityDTO.setVisibilityType(selectRS.getString("seat.visibility_type"));

				String reffernceIds = selectRS.getString("seat.refference_id");
				if (visibilityDTO.getRefferenceType().equals("GR")) {
					List<GroupDTO> groupList = convertGroupList(reffernceIds);
					visibilityDTO.setGroupList(groupList);
				}
				else if (visibilityDTO.getRefferenceType().equals("UR")) {
					List<UserDTO> userList = convertUserList(reffernceIds);
					visibilityDTO.setUserList(userList);
				}
				else if (visibilityDTO.getRefferenceType().equals("SG")) {
					List<RouteDTO> routeList = convertRouteList(reffernceIds);
					if (fromStationDTO.getId() != 0 && toStationDTO.getId() != 0) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setFromStation(fromStationDTO);
						routeDTO.setToStation(toStationDTO);
						routeList.add(routeDTO);
					}
					visibilityDTO.setRouteList(routeList);
					
					List<UserDTO> routeUsers = convertUserCodesToList(selectRS.getString("seat.additional_attribute"));
					visibilityDTO.setRouteUsers(routeUsers);
				}
				else if (visibilityDTO.getRefferenceType().equals("BR")) {
					List<OrganizationDTO> organizationList = convertOrganizationList(reffernceIds);
					visibilityDTO.setOrganizations(organizationList);
				}

				visibilityDTO.setSchedule(scheduleDTO);

				if (visibilityDTO.getLookupCode().equals("0")) {
					scheduleMap.put(visibilityDTO.getId(), visibilityDTO);
				}
				else {
					overrideList.add(visibilityDTO);
				}
			}
			for (ScheduleSeatVisibilityDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleSeatVisibilityDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleSeatVisibilityDTO>(scheduleMap.values());

	}

	public ScheduleSeatVisibilityDTO getScheduleSeatVisibility(AuthDTO authDTO, ScheduleSeatVisibilityDTO seatVisibilityDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code, bus_id,from_station_id,to_station_id,refference_id,refference_type,active_from,active_to,day_of_week,release_minutes,visibility_type,seat_codes,additional_attribute,lookup_id,active_flag, remarks, updated_by FROM schedule_seat_visibility seat WHERE code = ? AND namespace_id = ? ");
			selectPS.setString(1, seatVisibilityDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				seatVisibilityDTO.setId(selectRS.getInt("id"));
				seatVisibilityDTO.setCode(selectRS.getString("code"));
				seatVisibilityDTO.setActiveFlag(selectRS.getInt("active_flag"));

				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				fromStationDTO.setId(selectRS.getInt("from_station_id"));
				toStationDTO.setId(selectRS.getInt("to_station_id"));

				seatVisibilityDTO.setLookupCode(selectRS.getString("lookup_id"));
				seatVisibilityDTO.setActiveFrom(selectRS.getString("active_from"));
				seatVisibilityDTO.setActiveTo(selectRS.getString("active_to"));
				seatVisibilityDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				seatVisibilityDTO.setRemarks(selectRS.getString("remarks"));
				seatVisibilityDTO.setReleaseMinutes(selectRS.getInt("release_minutes"));
				seatVisibilityDTO.setUpdatedBy(selectRS.getString("updated_by"));

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
				seatVisibilityDTO.setBus(busDTO);
				seatVisibilityDTO.setRefferenceType(selectRS.getString("refference_type"));
				seatVisibilityDTO.setVisibilityType(selectRS.getString("visibility_type"));

				String reffernceIds = selectRS.getString("seat.refference_id");
				if (seatVisibilityDTO.getRefferenceType().equals("GR")) {
					List<GroupDTO> groupList = convertGroupList(reffernceIds);
					seatVisibilityDTO.setGroupList(groupList);
				}
				else if (seatVisibilityDTO.getRefferenceType().equals("UR")) {
					List<UserDTO> userList = convertUserList(reffernceIds);
					seatVisibilityDTO.setUserList(userList);
				}
				else if (seatVisibilityDTO.getRefferenceType().equals("SG")) {
					List<RouteDTO> routeList = convertRouteList(reffernceIds);
					if (fromStationDTO.getId() != 0 && toStationDTO.getId() != 0) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setFromStation(fromStationDTO);
						routeDTO.setToStation(toStationDTO);
						routeList.add(routeDTO);
					}
					seatVisibilityDTO.setRouteList(routeList);
					
					List<UserDTO> routeUsers = convertUserCodesToList(selectRS.getString("seat.additional_attribute"));
					seatVisibilityDTO.setRouteUsers(routeUsers);
				}
				else if (seatVisibilityDTO.getRefferenceType().equals("BR")) {
					List<OrganizationDTO> organizationList = convertOrganizationList(reffernceIds);
					seatVisibilityDTO.setOrganizations(organizationList);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return seatVisibilityDTO;
	}

	private List<UserDTO> convertUserList(String userIds) {
		List<UserDTO> userList = new ArrayList<>();
		if (StringUtil.isNotNull(userIds)) {
			String[] userIdList = userIds.split(Text.COMMA);
			for (String userId : userIdList) {
				if (StringUtil.isNull(userId) || Numeric.ZERO.equals(userId)) {
					continue;
				}
				UserDTO userDTO = new UserDTO();
				userDTO.setId(Integer.valueOf(userId));
				userList.add(userDTO);
			}
		}
		return userList;
	}

	private List<UserDTO> convertUserCodesToList(String userCodes) {
		List<UserDTO> userList = new ArrayList<>();
		if (StringUtil.isNotNull(userCodes)) {
			String[] userCodeList = userCodes.split(Text.COMMA);
			for (String userCode : userCodeList) {
				if (StringUtil.isNull(userCode)) {
					continue;
				}
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(userCode);
				userList.add(userDTO);
			}
		}
		return userList;
	}
	
	private List<GroupDTO> convertGroupList(String groupIds) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groupIds)) {
			String[] groupIdList = groupIds.split(Text.COMMA);
			for (String groupId : groupIdList) {
				if (StringUtil.isNull(groupId) || Numeric.ZERO.equals(groupId)) {
					continue;
				}
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(Integer.valueOf(groupId));
				groupList.add(groupDTO);
			}
		}
		return groupList;
	}

	private List<RouteDTO> convertRouteList(String routes) {
		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (StringUtil.isNotNull(routes)) {
			for (String routeId : routes.split(Text.COMMA)) {
				if (StringUtil.isNull(routeId.trim()) || routeId.split(Text.UNDER_SCORE).length != 2) {
					continue;
				}
				int fromStationId = Integer.valueOf(routeId.split(Text.UNDER_SCORE)[0]);
				int toStationId = Integer.valueOf(routeId.split(Text.UNDER_SCORE)[1]);
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
	
	private List<OrganizationDTO> convertOrganizationList(String organizationIds) {
		List<OrganizationDTO> organizationList = new ArrayList<OrganizationDTO>();
		if (StringUtil.isNotNull(organizationIds)) {
			String[] organizationIdList = organizationIds.split(Text.COMMA);
			for (String organizationId : organizationIdList) {
				if (StringUtil.isNull(organizationId) || Numeric.ZERO.equals(organizationId)) {
					continue;
				}
				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(Integer.valueOf(organizationId));
				organizationList.add(organizationDTO);
			}
		}
		return organizationList;
	}
}
