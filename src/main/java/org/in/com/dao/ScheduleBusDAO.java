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

import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleBusDAO {
	public List<ScheduleBusDTO> get(AuthDTO authDTO, ScheduleBusDTO scheduleBusDTO) {
		List<ScheduleBusDTO> scheduleBusList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT scbs.id,scbs.code,scbs.bus_id, scbs.tax_id, scbs.breakeven_settings_id, scbs.distance, scbs.amenities_code,scbs.active_flag FROM schedule_bus scbs,schedule sche WHERE  scbs.schedule_id = sche.id AND scbs.namespace_id = ? AND sche.code = ?  AND scbs.active_flag  = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleBusDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleBusDTO dto = new ScheduleBusDTO();
				dto.setId(selectRS.getInt("scbs.id"));
				dto.setCode(selectRS.getString("scbs.code"));
				dto.setActiveFlag(selectRS.getInt("scbs.active_flag"));
				
				NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
				taxDTO.setId(selectRS.getInt("scbs.tax_id"));
				dto.setTax(taxDTO);
				
				BusBreakevenSettingsDTO breakevenSettings = new BusBreakevenSettingsDTO();
				breakevenSettings.setId(selectRS.getInt("scbs.breakeven_settings_id"));
				dto.setBreakevenSettings(breakevenSettings);
				dto.setDistance(selectRS.getFloat("scbs.distance"));
				
				String amenities = selectRS.getString("scbs.amenities_code");
				if (StringUtil.isNotNull(amenities)) {
					String[] amentiesArray = amenities.split(",");
					List<AmenitiesDTO> amenitieslist = new ArrayList<>();
					for (String value : amentiesArray) {
						AmenitiesDTO amentiesDTO = new AmenitiesDTO();
						amentiesDTO.setCode(value);
						amenitieslist.add(amentiesDTO);
					}
					dto.setAmentiesList(amenitieslist);
				}
				BusDTO busDTO = new BusDTO();
				dto.setSchedule(scheduleBusDTO.getSchedule());
				busDTO.setId(selectRS.getInt("scbs.bus_id"));
				dto.setBus(busDTO);
				scheduleBusList.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleBusList;
	}

	public ScheduleBusDTO getIUD(AuthDTO authDTO, ScheduleBusDTO busDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			StringBuilder amentiesCodes = new StringBuilder();
			for (AmenitiesDTO amentiesDTO : busDTO.getAmentiesList()) {
				if (amentiesCodes.length() > 0) {
					amentiesCodes.append(",");
				}
				amentiesCodes.append(amentiesDTO.getCode());
			}
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL  EZEE_SP_SCHEDULE_BUS_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, busDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, busDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, busDTO.getBus().getCode());
			callableStatement.setString(++pindex, busDTO.getTax().getCode());
			callableStatement.setInt(++pindex, busDTO.getBreakevenSettings().getId());
			callableStatement.setFloat(++pindex, busDTO.getDistance());
			callableStatement.setString(++pindex, amentiesCodes.toString());
			callableStatement.setInt(++pindex, busDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				busDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				busDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return busDTO;
	}

	public ScheduleBusDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleBusDTO scheduleBusDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT  scbs.id,scbs.code,scbs.bus_id,scbs.tax_id,scbs.breakeven_settings_id,scbs.distance,scbs.amenities_code, scbs.active_flag FROM schedule_bus scbs WHERE scbs.namespace_id = ? AND  scbs.schedule_id = ? AND scbs.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				scheduleBusDTO = new ScheduleBusDTO();
				scheduleBusDTO.setId(selectRS.getInt("scbs.id"));
				scheduleBusDTO.setCode(selectRS.getString("scbs.code"));
				scheduleBusDTO.setActiveFlag(selectRS.getInt("scbs.active_flag"));
				BusDTO busDTO = new BusDTO();
				scheduleBusDTO.setSchedule(scheduleDTO);
				busDTO.setId(selectRS.getInt("scbs.bus_id"));
				scheduleBusDTO.setBus(busDTO);
				
				NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
				taxDTO.setId(selectRS.getInt("scbs.tax_id"));
				scheduleBusDTO.setTax(taxDTO);
				
				BusBreakevenSettingsDTO breakevenSettings = new BusBreakevenSettingsDTO();
				breakevenSettings.setId(selectRS.getInt("scbs.breakeven_settings_id"));
				scheduleBusDTO.setBreakevenSettings(breakevenSettings);
				
				scheduleBusDTO.setDistance(selectRS.getFloat("scbs.distance"));
				String amenities = selectRS.getString("scbs.amenities_code");
				List<AmenitiesDTO> amenitieslist = new ArrayList<>();
				if (StringUtil.isNotNull(amenities)) {
					String[] amentiesArray = amenities.split(",");
					for (String value : amentiesArray) {
						AmenitiesDTO amentiesDTO = new AmenitiesDTO();
						amentiesDTO.setCode(value);
						amenitieslist.add(amentiesDTO);
					}
				}
				scheduleBusDTO.setAmentiesList(amenitieslist);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleBusDTO;
	}

	public boolean CheckBusmapUsed(AuthDTO authDTO, BusDTO dto) {
		boolean status = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT scbs.namespace_id, scbs.id,scbs.code,scbs.bus_id,scbs.amenities_code, scbs.active_flag FROM schedule sche, schedule_bus scbs,bus bs WHERE bs.namespace_id = ? AND scbs.namespace_id = bs.namespace_id  AND  scbs.namespace_id = sche.namespace_id  AND  scbs.schedule_id = sche.id AND  scbs.bus_id = bs.id   AND bs.code = ? AND scbs.active_flag < 2 AND sche.active_flag < 2");
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

	public int checkScheduleBusChange(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		int ticketCount = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT  1 as count FROM ticket WHERE namespace_id = ? AND trip_date >= DATE(NOW()) AND schedule_id = ? AND active_flag = 1 AND ticket_status_id IN (1, 5) LIMIT 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				ticketCount = selectRS.getInt("count");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketCount;
	}

	public Map<String, ScheduleDTO> getScheduleByBus(AuthDTO authDTO, BusDTO busDTO) {
		Map<String, ScheduleDTO> scheduleMap = new HashMap<String, ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT sche.id, sche.code, sche.name, sche.active_from, sche.active_to, sche.day_of_week, sche.service_number, sche.active_flag FROM schedule sche, schedule_bus scbs, bus bs WHERE bs.namespace_id = ? AND scbs.namespace_id = bs.namespace_id AND scbs.namespace_id = sche.namespace_id AND scbs.schedule_id = sche.id AND sche.prerequisites = '000000' AND scbs.bus_id = bs.id AND bs.code = ? AND scbs.active_flag = 1 AND sche.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, busDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(selectRS.getInt("sche.id"));
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				scheduleDTO.setName(selectRS.getString("sche.name"));
				scheduleDTO.setActiveFrom(selectRS.getString("sche.active_from")); 
				scheduleDTO.setActiveTo(selectRS.getString("sche.active_to"));
				scheduleDTO.setDayOfWeek(selectRS.getString("sche.day_of_week"));
				scheduleDTO.setServiceNumber(selectRS.getString("sche.service_number"));
				scheduleDTO.setActiveFlag(selectRS.getInt("sche.active_flag"));
				
				scheduleMap.put(scheduleDTO.getCode(), scheduleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleMap;
	}

}
