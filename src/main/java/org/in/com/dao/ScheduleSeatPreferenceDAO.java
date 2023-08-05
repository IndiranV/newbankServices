package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleSeatPreferenceDAO {

	public List<ScheduleSeatPreferenceDTO> get(AuthDTO authDTO, ScheduleSeatPreferenceDTO seatVisibilityDTO) {
		List<ScheduleSeatPreferenceDTO> overrideList = new ArrayList<ScheduleSeatPreferenceDTO>();
		Map<Integer, ScheduleSeatPreferenceDTO> seatPreferenceMap = new HashMap<Integer, ScheduleSeatPreferenceDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.bus_id,seat.active_from,seat.active_to,seat.day_of_week,seat.preferenced_gender,seat.seat_codes,seat.group_codes,seat.lookup_id,seat.active_flag,seat.updated_by,seat.updated_at,sche.code,sche.name FROM schedule_seat_preferences seat,schedule sche WHERE sche.id = seat.schedule_id AND seat.namespace_id = ? AND sche.namespace_id = ? AND sche.code = ? AND seat.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, seatVisibilityDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO = new ScheduleSeatPreferenceDTO();
				scheduleSeatPreferenceDTO.setCode(selectRS.getString("code"));
				scheduleSeatPreferenceDTO.setId(selectRS.getInt("seat.id"));
				scheduleSeatPreferenceDTO.setActiveFlag(selectRS.getInt("active_flag"));
				scheduleSeatPreferenceDTO.setActiveFrom(selectRS.getString("active_from"));
				scheduleSeatPreferenceDTO.setActiveTo(selectRS.getString("active_to"));
				scheduleSeatPreferenceDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleSeatPreferenceDTO.setGendar(SeatGendarEM.getSeatGendarEM(selectRS.getInt("preferenced_gender")));
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
				scheduleSeatPreferenceDTO.setBus(busDTO);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("seat.group_codes"));
				scheduleSeatPreferenceDTO.setGroupList(groupList);
				scheduleSeatPreferenceDTO.setLookupCode(selectRS.getString("seat.lookup_id"));
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				scheduleDTO.setName(selectRS.getString("sche.name"));
				scheduleSeatPreferenceDTO.setSchedule(scheduleDTO);
				
				AuditDTO auditDTO = new AuditDTO();
				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(selectRS.getInt("seat.updated_by"));
				auditDTO.setUser(updatedBy);
				auditDTO.setUpdatedAt(selectRS.getString("seat.updated_at"));
				scheduleSeatPreferenceDTO.setAudit(auditDTO);
				
				if (scheduleSeatPreferenceDTO.getLookupCode().equals("0")) {
					seatPreferenceMap.put(scheduleSeatPreferenceDTO.getId(), scheduleSeatPreferenceDTO);
				}
				else {
					overrideList.add(scheduleSeatPreferenceDTO);
				}
			}
			for (ScheduleSeatPreferenceDTO overrideSeatPreferenceDTO : overrideList) {
				if (seatPreferenceMap.get(Integer.parseInt(overrideSeatPreferenceDTO.getLookupCode())) != null) {
					ScheduleSeatPreferenceDTO dto = seatPreferenceMap.get(Integer.parseInt(overrideSeatPreferenceDTO.getLookupCode()));
					dto.getOverrideList().add(overrideSeatPreferenceDTO);
					seatPreferenceMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleSeatPreferenceDTO>(seatPreferenceMap.values());
	}

	public ScheduleSeatPreferenceDTO getIUD(AuthDTO authDTO, ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO) {
		try {
			StringBuilder seatCodes = new StringBuilder();
			if (scheduleSeatPreferenceDTO.getBus() != null && scheduleSeatPreferenceDTO.getBus().getBusSeatLayoutDTO() != null && scheduleSeatPreferenceDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
				for (BusSeatLayoutDTO layoutDTO : scheduleSeatPreferenceDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (seatCodes.length() > 0) {
						seatCodes.append(",");
					}
					seatCodes.append(layoutDTO.getCode());
				}
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_SEAT_PREFERENCE_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,?)}");

			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getBus().getCode());
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getActiveFrom() != null ? scheduleSeatPreferenceDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getActiveTo() != null ? scheduleSeatPreferenceDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, scheduleSeatPreferenceDTO.getGendar() != null ? scheduleSeatPreferenceDTO.getGendar().getId() : -1);
			callableStatement.setString(++pindex, seatCodes.toString());
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getGroups());
			callableStatement.setString(++pindex, scheduleSeatPreferenceDTO.getLookupCode());
			callableStatement.setInt(++pindex, scheduleSeatPreferenceDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleSeatPreferenceDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				scheduleSeatPreferenceDTO.setCode(callableStatement.getString("pcrCode"));
			}
			callableStatement.clearParameters();
			scheduleSeatPreferenceDTO.setSchedule(scheduleSeatPreferenceDTO.getSchedule());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleSeatPreferenceDTO;
	}

	public List<ScheduleSeatPreferenceDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleSeatPreferenceDTO> overrideList = new ArrayList<ScheduleSeatPreferenceDTO>();
		Map<Integer, ScheduleSeatPreferenceDTO> seatPreferenceMap = new HashMap<Integer, ScheduleSeatPreferenceDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.bus_id,seat.active_from,seat.active_to,seat.day_of_week,seat.preferenced_gender,seat.seat_codes,seat.group_codes,seat.lookup_id,seat.active_flag FROM schedule_seat_preferences seat WHERE seat.namespace_id = ? AND seat.active_flag  = 1 AND seat.schedule_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO = new ScheduleSeatPreferenceDTO();
				scheduleSeatPreferenceDTO.setId(selectRS.getInt("seat.id"));
				scheduleSeatPreferenceDTO.setCode(selectRS.getString("code"));
				scheduleSeatPreferenceDTO.setActiveFlag(selectRS.getInt("active_flag"));
				scheduleSeatPreferenceDTO.setActiveFrom(selectRS.getString("active_from"));
				scheduleSeatPreferenceDTO.setActiveTo(selectRS.getString("active_to"));
				scheduleSeatPreferenceDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleSeatPreferenceDTO.setGendar(SeatGendarEM.getSeatGendarEM(selectRS.getInt("preferenced_gender")));
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
				scheduleSeatPreferenceDTO.setLookupCode(selectRS.getString("lookup_id"));
				List<GroupDTO> groupList = convertGroupList(selectRS.getString("seat.group_codes"));
				scheduleSeatPreferenceDTO.setGroupList(groupList);

				busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
				busDTO.setId(selectRS.getInt("bus_id"));
				scheduleSeatPreferenceDTO.setBus(busDTO);
				scheduleSeatPreferenceDTO.setSchedule(scheduleDTO);

				if (scheduleSeatPreferenceDTO.getLookupCode().equals("0")) {
					seatPreferenceMap.put(scheduleSeatPreferenceDTO.getId(), scheduleSeatPreferenceDTO);
				}
				else {
					overrideList.add(scheduleSeatPreferenceDTO);
				}
			}
			for (ScheduleSeatPreferenceDTO overrideSeatPreferenceDTO : overrideList) {
				if (seatPreferenceMap.get(Integer.parseInt(overrideSeatPreferenceDTO.getLookupCode())) != null) {
					ScheduleSeatPreferenceDTO dto = seatPreferenceMap.get(Integer.parseInt(overrideSeatPreferenceDTO.getLookupCode()));
					dto.getOverrideList().add(overrideSeatPreferenceDTO);
					seatPreferenceMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleSeatPreferenceDTO>(seatPreferenceMap.values());

	}

	private List<GroupDTO> convertGroupList(String groups) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groups)) {
			List<String> groupCodes = Arrays.asList(groups.split(Text.COMMA));
			if (groupCodes != null) {
				for (String groupCode : groupCodes) {
					if (StringUtil.isNull(groupCode)) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(groupCode);
					groupList.add(groupDTO);
				}
			}
		}
		return groupList;
	}

}
