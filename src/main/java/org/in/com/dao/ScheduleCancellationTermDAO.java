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
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ServiceException;

public class ScheduleCancellationTermDAO extends GroupDAO {
	public List<ScheduleCancellationTermDTO> get(AuthDTO authDTO, ScheduleCancellationTermDTO termDTO) {
		List<ScheduleCancellationTermDTO> overrideList = new ArrayList<ScheduleCancellationTermDTO>();
		Map<Integer, ScheduleCancellationTermDTO> scheduleMap = new HashMap<Integer, ScheduleCancellationTermDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT term.id,term.code,term.cancellation_policy_id , term.active_from,term.active_to,term.day_of_week,term.lookup_id,term.active_flag,sche.code,term.user_group_id  FROM schedule_cancellation_terms term,schedule sche WHERE sche.id = term.schedule_id AND term.namespace_id = ? AND sche.code = ? AND term.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, termDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleCancellationTermDTO scheduleCancellationTermDTO = new ScheduleCancellationTermDTO();
				scheduleCancellationTermDTO.setCode(selectRS.getString("term.code"));
				scheduleCancellationTermDTO.setId(selectRS.getInt("term.id"));
				scheduleCancellationTermDTO.setActiveFlag(selectRS.getInt("term.active_flag"));
				scheduleCancellationTermDTO.setActiveFrom(selectRS.getString("term.active_from"));
				scheduleCancellationTermDTO.setActiveTo(selectRS.getString("term.active_to"));
				scheduleCancellationTermDTO.setDayOfWeek(selectRS.getString("term.day_of_week"));
				scheduleCancellationTermDTO.setLookupCode(selectRS.getString("term.lookup_id"));
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("term.user_group_id"));
				scheduleCancellationTermDTO.setGroup(groupDTO);
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
				cancellationTermDTO.setId(selectRS.getInt("term.cancellation_policy_id"));
				scheduleCancellationTermDTO.setCancellationTerm(cancellationTermDTO);
				scheduleCancellationTermDTO.setSchedule(scheduleDTO);
				// Overrides
				if (scheduleCancellationTermDTO.getLookupCode().equals("0")) {
					scheduleMap.put(scheduleCancellationTermDTO.getId(), scheduleCancellationTermDTO);
				}
				else {
					overrideList.add(scheduleCancellationTermDTO);
				}
			}
			for (ScheduleCancellationTermDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleCancellationTermDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ScheduleCancellationTermDTO>(scheduleMap.values());
	}

	public ScheduleCancellationTermDTO getIUD(AuthDTO authDTO, ScheduleCancellationTermDTO termDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_SCHEDULE_CANCELLATION_TERMS_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?)}");
			for (ScheduleCancellationTermDTO pointDTO : termDTO.getList()) {
				pindex = 0;
				callableStatement.setString(++pindex, pointDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, pointDTO.getSchedule() != null ? pointDTO.getSchedule().getCode() : null);
				callableStatement.setString(++pindex, pointDTO.getCancellationTerm() != null ? pointDTO.getCancellationTerm().getCode() : null);
				callableStatement.setString(++pindex, pointDTO.getGroup() != null ? pointDTO.getGroup().getCode() : null);
				callableStatement.setString(++pindex, pointDTO.getActiveFrom() != null ? pointDTO.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, pointDTO.getActiveTo() != null ? pointDTO.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, pointDTO.getDayOfWeek());
				callableStatement.setString(++pindex, pointDTO.getLookupCode());
				callableStatement.setInt(++pindex, pointDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					termDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					termDTO.setCode(callableStatement.getString("pcrCode"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return termDTO;
	}

	public List<ScheduleCancellationTermDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleCancellationTermDTO> overrideList = new ArrayList<ScheduleCancellationTermDTO>();
		Map<Integer, ScheduleCancellationTermDTO> scheduleMap = new HashMap<Integer, ScheduleCancellationTermDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT term.id,term.code,term.cancellation_policy_id , term.active_from,term.active_to,term.day_of_week,term.lookup_id,term.active_flag,term.user_group_id  FROM schedule_cancellation_terms term  WHERE  term.namespace_id = ? AND term.schedule_id = ? AND term.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleCancellationTermDTO dto = new ScheduleCancellationTermDTO();
				dto.setId(selectRS.getInt("term.id"));
				dto.setCode(selectRS.getString("term.code"));
				dto.setActiveFlag(selectRS.getInt("term.active_flag"));
				dto.setLookupCode(selectRS.getString("term.lookup_id"));
				dto.setActiveFrom(selectRS.getString("term.active_from"));
				dto.setActiveTo(selectRS.getString("term.active_to"));
				dto.setDayOfWeek(selectRS.getString("term.day_of_week"));
				dto.setLookupCode(selectRS.getString("term.lookup_id"));
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("term.user_group_id"));
				dto.setGroup(groupDTO);
				CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
				cancellationTermDTO.setId(selectRS.getInt("term.cancellation_policy_id"));
				dto.setCancellationTerm(cancellationTermDTO);
				dto.setSchedule(scheduleDTO);
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleCancellationTermDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleCancellationTermDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return new ArrayList<ScheduleCancellationTermDTO>(scheduleMap.values());
	}

	public boolean CheckCancellationTermUsed(AuthDTO authDTO, CancellationTermDTO dto) {
		boolean status = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT  1  FROM schedule_cancellation_terms term,namespace_cancellation_terms nstr,schedule sche  WHERE  sche.id = term.schedule_id AND nstr.namespace_id = term.namespace_id AND term.namespace_id = ? AND term.cancellation_policy_id = nstr.id and nstr.code = ? AND term.active_flag < 2 AND sche.active_flag <= 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, dto.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				status = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return status;
	}
}
