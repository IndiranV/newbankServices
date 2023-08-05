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

import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class ScheduleBusOverrideDAO {
	public void updateScheduleBusOverride(AuthDTO authDTO, ScheduleBusOverrideDTO scheduleBusOverride) {
		try {
			int pindex = Numeric.ZERO_INT;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("CALL EZEE_SP_SCHEDULE_BUS_OVERRIDE_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)");
			callableStatement.setString(++pindex, scheduleBusOverride.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleBusOverride.getSchedule().getCode());
			callableStatement.setString(++pindex, scheduleBusOverride.getActiveFrom());
			callableStatement.setString(++pindex, scheduleBusOverride.getActiveTo());
			callableStatement.setString(++pindex, scheduleBusOverride.getTripDatesToString());
			callableStatement.setString(++pindex, scheduleBusOverride.getDayOfWeek());
			callableStatement.setString(++pindex, scheduleBusOverride.getBus().getCode());
			callableStatement.setString(++pindex, scheduleBusOverride.getTax().getCode());
			callableStatement.setString(++pindex, scheduleBusOverride.getBus().getCategoryCode());
			callableStatement.setString(++pindex, scheduleBusOverride.getLookupCode());
			callableStatement.setInt(++pindex, scheduleBusOverride.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleBusOverride.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}
	}

	public List<ScheduleBusOverrideDTO> getBusOverrideBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleBusOverrideDTO> list = new ArrayList<ScheduleBusOverrideDTO>();
		Map<Integer, ScheduleBusOverrideDTO> scheduleMap = new HashMap<Integer, ScheduleBusOverrideDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (scheduleDTO.getId() > 0) {
				selectPS = connection.prepareStatement("SELECT sbd.id, sbd.code, sch.id, sch.code, sch.name, sch.service_number, sbd.active_from, sbd.active_to, sbd.trip_dates, sbd.day_of_week, sbd.bus_id, sbd.tax_id, sbd.category_code, sbd.lookup_id, sbd.active_flag FROM schedule_bus_override sbd, schedule sch WHERE sbd.namespace_id = ? AND sch.namespace_id = ? AND sch.id = ? AND sbd.schedule_id = sch.id AND sch.active_flag = 1 AND sbd.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setInt(3, scheduleDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT sbd.id, sbd.code, sch.id, sch.code, sch.name, sch.service_number, sbd.active_from, sbd.active_to, sbd.trip_dates, sbd.day_of_week, sbd.bus_id, sbd.tax_id, sbd.category_code, sbd.lookup_id, sbd.active_flag FROM schedule_bus_override sbd, schedule sch WHERE sbd.namespace_id = ? AND sch.namespace_id = ? AND sch.code = ? AND sbd.schedule_id = sch.id AND sch.active_flag = 1 AND sbd.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setString(3, scheduleDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleBusOverrideDTO scheduleBusOverride = new ScheduleBusOverrideDTO();
				scheduleBusOverride.setId(selectRS.getInt("sbd.id"));
				scheduleBusOverride.setCode(selectRS.getString("code"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("sch.id"));
				schedule.setCode(selectRS.getString("sch.code"));
				schedule.setName(selectRS.getString("sch.name"));
				schedule.setServiceNumber(selectRS.getString("sch.service_number"));
				scheduleBusOverride.setSchedule(schedule);

				scheduleBusOverride.setActiveFrom(selectRS.getString("active_from"));
				scheduleBusOverride.setActiveTo(selectRS.getString("active_to"));
				scheduleBusOverride.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleBusOverride.setTripDates(getTripDates(selectRS.getString("sbd.trip_dates")));

				BusDTO bus = new BusDTO();
				bus.setId(selectRS.getInt("bus_id"));
				bus.setCategoryCode(selectRS.getString("category_code"));
				scheduleBusOverride.setBus(bus);

				NamespaceTaxDTO tax = new NamespaceTaxDTO();
				tax.setId(selectRS.getInt("sbd.tax_id"));
				scheduleBusOverride.setTax(tax);

				scheduleBusOverride.setLookupCode(selectRS.getString("lookup_id"));
				scheduleBusOverride.setActiveFlag(selectRS.getInt("active_flag"));

				if (scheduleBusOverride.getLookupCode().equals("0")) {
					scheduleMap.put(scheduleBusOverride.getId(), scheduleBusOverride);
				}
				else {
					list.add(scheduleBusOverride);
				}
			}
			for (ScheduleBusOverrideDTO overrideScheduleDTO : list) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleBusOverrideDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ScheduleBusOverrideDTO>(scheduleMap.values());
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

	public List<ScheduleBusOverrideDTO> getBusOverrideByBus(AuthDTO authDTO, BusDTO busDTO) {
		List<ScheduleBusOverrideDTO> list = new ArrayList<ScheduleBusOverrideDTO>();
		Map<Integer, ScheduleBusOverrideDTO> scheduleMap = new HashMap<Integer, ScheduleBusOverrideDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT sbd.id, sbd.code, sch.id, sch.code, sch.name, sch.active_from, sch.active_to, sch.day_of_week, sch.service_number, sbd.active_from, sbd.active_to, sbd.trip_dates, sbd.day_of_week, sbd.lookup_id, sbd.active_flag FROM schedule_bus_override sbd, schedule sch, bus bs WHERE sbd.namespace_id = ? AND sch.namespace_id = sbd.namespace_id AND bs.namespace_id = sbd.namespace_id AND bs.code = ? AND sbd.bus_id = bs.id AND sbd.schedule_id = sch.id AND sch.prerequisites = '000000' AND sch.active_flag < 2 AND sbd.active_flag = 1 AND bs.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, busDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleBusOverrideDTO scheduleBusOverride = new ScheduleBusOverrideDTO();
				scheduleBusOverride.setId(selectRS.getInt("sbd.id"));
				scheduleBusOverride.setCode(selectRS.getString("sbd.code"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("sch.id"));
				schedule.setCode(selectRS.getString("sch.code"));
				schedule.setName(selectRS.getString("sch.name"));
				schedule.setActiveFrom(selectRS.getString("sch.active_from"));
				schedule.setActiveTo(selectRS.getString("sch.active_to"));
				schedule.setDayOfWeek(selectRS.getString("sch.day_of_week"));
				schedule.setServiceNumber(selectRS.getString("sch.service_number"));
				scheduleBusOverride.setSchedule(schedule);

				scheduleBusOverride.setActiveFrom(selectRS.getString("sbd.active_from"));
				scheduleBusOverride.setActiveTo(selectRS.getString("sbd.active_to"));
				scheduleBusOverride.setTripDates(getTripDates(selectRS.getString("sbd.trip_dates")));
				scheduleBusOverride.setDayOfWeek(selectRS.getString("sbd.day_of_week"));
				scheduleBusOverride.setLookupCode(selectRS.getString("sbd.lookup_id"));
				scheduleBusOverride.setActiveFlag(selectRS.getInt("sbd.active_flag"));

				if (scheduleBusOverride.getLookupCode().equals("0")) {
					scheduleMap.put(scheduleBusOverride.getId(), scheduleBusOverride);
				}
				else {
					list.add(scheduleBusOverride);
				}
			}
			for (ScheduleBusOverrideDTO overrideScheduleDTO : list) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleBusOverrideDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ScheduleBusOverrideDTO>(scheduleMap.values());
	}
}
