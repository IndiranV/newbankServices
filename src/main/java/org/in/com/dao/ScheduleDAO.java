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

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

public class ScheduleDAO {
	public List<ScheduleDTO> getAll(AuthDTO authDTO) {
		List<ScheduleDTO> overrideList = new ArrayList<ScheduleDTO>();
		Map<Integer, ScheduleDTO> scheduleMap = new HashMap<Integer, ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, pnr_start_code, day_of_week, prerequisites, category_id, lookup_id, active_flag FROM schedule WHERE namespace_id = ? AND active_flag  = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			// selectPS.setString(2, "000000");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO dto = new ScheduleDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				dto.setServiceNumber(selectRS.getString("service_number"));
				dto.setDisplayName(selectRS.getString("display_name"));
				dto.setPnrStartCode(selectRS.getString("pnr_start_code"));
				dto.setPreRequrities(selectRS.getString("prerequisites"));

				int catgeryId = selectRS.getInt("category_id");
				if (catgeryId != 0) {
					ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
					categoryDTO.setId(catgeryId);
					dto.setCategory(categoryDTO);
				}
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleDTO scheduleDTO = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					scheduleDTO.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(scheduleDTO.getId(), scheduleDTO);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleDTO>(scheduleMap.values());
	}

	public List<ScheduleDTO> get(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleDTO> list = new ArrayList<ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (scheduleDTO.getId() != 0) {
				selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week, prerequisites, category_id, lookup_id, tags, distance, sector_id, active_flag FROM schedule WHERE namespace_id = ? AND id = ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, scheduleDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week, prerequisites, category_id, lookup_id, tags, distance, sector_id, active_flag FROM schedule WHERE namespace_id = ? AND code = ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, scheduleDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				scheduleDTO.setId(selectRS.getInt("id"));
				scheduleDTO.setCode(selectRS.getString("code"));
				scheduleDTO.setName(selectRS.getString("name"));
				scheduleDTO.setActiveFlag(selectRS.getInt("active_flag"));
				scheduleDTO.setLookupCode(selectRS.getString("lookup_id"));
				scheduleDTO.setActiveFrom(selectRS.getString("active_from"));
				scheduleDTO.setActiveTo(selectRS.getString("active_to"));
				scheduleDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleDTO.setServiceNumber(selectRS.getString("service_number"));
				scheduleDTO.setDisplayName(selectRS.getString("display_name"));
				scheduleDTO.setApiDisplayName(selectRS.getString("api_display_name"));
				scheduleDTO.setPnrStartCode(selectRS.getString("pnr_start_code"));
				scheduleDTO.setPreRequrities(selectRS.getString("prerequisites"));
				int catgeryId = selectRS.getInt("category_id");
				if (catgeryId != 0) {
					ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
					categoryDTO.setId(catgeryId);
					scheduleDTO.setCategory(categoryDTO);
				}
				
				String scheduleTag = selectRS.getString("tags");
				List<ScheduleTagDTO> scheduleTagList = convertScheduleTagList(scheduleTag);
				scheduleDTO.setScheduleTagList(scheduleTagList);
				
				scheduleDTO.setDistance(selectRS.getFloat("distance"));
				
				String sectorIds = selectRS.getString("sector_id");
				List<SectorDTO> sectorList = convertSectorList(sectorIds);
				scheduleDTO.setSectorList(sectorList);
				
				@Cleanup
				PreparedStatement overrideSelectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, pnr_start_code, day_of_week, category_id, lookup_id, active_flag FROM schedule WHERE namespace_id = ? AND active_flag  = 1 AND lookup_id = ?");
				overrideSelectPS.setInt(1, authDTO.getNamespace().getId());
				overrideSelectPS.setInt(2, scheduleDTO.getId());
				@Cleanup
				ResultSet overrideSelectRS = overrideSelectPS.executeQuery();
				while (overrideSelectRS.next()) {
					ScheduleDTO overrideDto = new ScheduleDTO();
					overrideDto.setId(overrideSelectRS.getInt("id"));
					overrideDto.setCode(overrideSelectRS.getString("code"));
					overrideDto.setName(overrideSelectRS.getString("name"));
					overrideDto.setActiveFlag(overrideSelectRS.getInt("active_flag"));
					overrideDto.setLookupCode(overrideSelectRS.getString("lookup_id"));
					overrideDto.setActiveFrom(overrideSelectRS.getString("active_from"));
					overrideDto.setActiveTo(overrideSelectRS.getString("active_to"));
					overrideDto.setDayOfWeek(overrideSelectRS.getString("day_of_week"));
					overrideDto.setServiceNumber(overrideSelectRS.getString("service_number"));
					overrideDto.setDisplayName(overrideSelectRS.getString("display_name"));
					overrideDto.setPnrStartCode(overrideSelectRS.getString("pnr_start_code"));
					scheduleDTO.getOverrideList().add(overrideDto);
				}
				list.add(scheduleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public ScheduleDTO getIUD(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_SCHEDULE_IUD(?,?,?,?,? ,?,?,?,?,?, ?,?,?,?,? ,?,?,?,?)}");
			callableStatement.setString(++pindex, scheduleDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleDTO.getName());
			callableStatement.setString(++pindex, scheduleDTO.getActiveFrom());
			callableStatement.setString(++pindex, scheduleDTO.getActiveTo());
			callableStatement.setString(++pindex, scheduleDTO.getServiceNumber());
			callableStatement.setString(++pindex, StringUtil.substring(scheduleDTO.getDisplayName(), 120));
			callableStatement.setString(++pindex, StringUtil.substring(scheduleDTO.getApiDisplayName(), 120));
			callableStatement.setString(++pindex, scheduleDTO.getPnrStartCode());
			callableStatement.setString(++pindex, scheduleDTO.getDayOfWeek());
			callableStatement.setString(++pindex, scheduleDTO.getCategory() != null ? scheduleDTO.getCategory().getCode() : null);
			callableStatement.setString(++pindex, scheduleDTO.getLookupCode());
			callableStatement.setString(++pindex, scheduleDTO.getScheduleTagIds());
			callableStatement.setFloat(++pindex, scheduleDTO.getDistance());
			callableStatement.setString(++pindex, scheduleDTO.getSectorIds());
			callableStatement.setInt(++pindex, scheduleDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleDTO;
	}

	public ScheduleDTO getScheduleRefreshIUD(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_SCHEDULE_PREREQUISITES_IUD(?,?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleDTO.getCode());
			callableStatement.registerOutParameter(++pindex, Types.VARCHAR);
			callableStatement.execute();
			scheduleDTO.setPreRequrities(callableStatement.getString("pcrPreRequisites"));
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public List<ScheduleDTO> getClosedSchedule(AuthDTO authDTO) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week, category_id, lookup_id, tags, distance, sector_id, active_flag FROM schedule WHERE namespace_id = ?  AND lookup_id = 0 AND active_flag = 0");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO dto = new ScheduleDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));

				dto.setServiceNumber(selectRS.getString("service_number"));
				dto.setDisplayName(selectRS.getString("display_name"));
				dto.setApiDisplayName(selectRS.getString("api_display_name"));
				dto.setPnrStartCode(selectRS.getString("pnr_start_code"));
				int catgeryId = selectRS.getInt("category_id");
				if (catgeryId != 0) {
					ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
					categoryDTO.setId(catgeryId);
					dto.setCategory(categoryDTO);
				}
				String scheduleTag = selectRS.getString("tags");
				List<ScheduleTagDTO> scheduleTagList = convertScheduleTagList(scheduleTag);
				dto.setScheduleTagList(scheduleTagList);
				
				dto.setDistance(selectRS.getFloat("distance"));
				
				String sectorIds = selectRS.getString("sector_id");
				List<SectorDTO> sectorList = convertSectorList(sectorIds);
				dto.setSectorList(sectorList);
				scheduleList.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleList;
	}

	public List<ScheduleDTO> getExpireSchedule(AuthDTO authDTO) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week, category_id, lookup_id, tags, distance, sector_id, active_flag FROM schedule WHERE namespace_id = ? AND active_flag = 1 AND lookup_id = 0 AND  active_to < DATE(NOW()) AND prerequisites = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, "000000");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO dto = new ScheduleDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));

				dto.setServiceNumber(selectRS.getString("service_number"));
				dto.setDisplayName(selectRS.getString("display_name"));
				dto.setApiDisplayName(selectRS.getString("api_display_name"));
				dto.setPnrStartCode(selectRS.getString("pnr_start_code"));
				int catgeryId = selectRS.getInt("category_id");
				if (catgeryId != 0) {
					ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
					categoryDTO.setId(catgeryId);
					dto.setCategory(categoryDTO);
				}
				
				String scheduleTag = selectRS.getString("tags");
				List<ScheduleTagDTO> scheduleTagList = convertScheduleTagList(scheduleTag);
				dto.setScheduleTagList(scheduleTagList);
				
				dto.setDistance(selectRS.getFloat("distance"));
				
				String sectorIds = selectRS.getString("sector_id");
				List<SectorDTO> sectorList = convertSectorList(sectorIds);
				dto.setSectorList(sectorList);
				scheduleList.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleList;
	}

	public List<ScheduleDTO> getPartialSchedule(AuthDTO authDTO) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week, category_id, lookup_id, tags, distance, sector_id, active_flag FROM schedule sche WHERE  namespace_id = ? AND active_flag = 1 AND lookup_id = 0 AND  (prerequisites !='000000' OR NOT EXISTS(SELECT 1 FROM schedule_bus bus WHERE bus.schedule_id = sche.id AND bus.active_flag = 1) OR NOT EXISTS(SELECT 1 FROM schedule_cancellation_terms term WHERE term.schedule_id = sche.id AND term.active_flag = 1) OR NOT EXISTS(SELECT 1 FROM schedule_control con WHERE con.schedule_id = sche.id AND con.active_flag = 1) OR NOT EXISTS(SELECT 1 FROM schedule_stage stag WHERE stag.schedule_id = sche.id AND stag.active_flag = 1) OR NOT EXISTS(SELECT 1 FROM schedule_station stag WHERE stag.schedule_id = sche.id AND stag.active_flag = 1) OR NOT EXISTS(SELECT 1 FROM schedule_station_point stag WHERE stag.schedule_id = sche.id AND stag.active_flag = 1))");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO dto = new ScheduleDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));

				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));

				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));

				dto.setServiceNumber(selectRS.getString("service_number"));
				dto.setDisplayName(selectRS.getString("display_name"));
				dto.setApiDisplayName(selectRS.getString("api_display_name"));
				dto.setPnrStartCode(selectRS.getString("pnr_start_code"));
				int catgeryId = selectRS.getInt("category_id");
				if (catgeryId != 0) {
					ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
					categoryDTO.setId(catgeryId);
					dto.setCategory(categoryDTO);
				}
				
				String scheduleTag = selectRS.getString("tags");
				List<ScheduleTagDTO> scheduleTagList = convertScheduleTagList(scheduleTag);
				dto.setScheduleTagList(scheduleTagList);
				
				dto.setDistance(selectRS.getFloat("distance"));
				
				String sectorIds = selectRS.getString("sector_id");
				List<SectorDTO> sectorList = convertSectorList(sectorIds);
				dto.setSectorList(sectorList);
				scheduleList.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleList;
	}

	public List<ScheduleDTO> getActiveSchedule(AuthDTO authDTO, DateTime activeDate) {
		List<ScheduleDTO> list = new ArrayList<ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week,category_id,lookup_id, tags, distance, sector_id, active_flag FROM schedule sche WHERE  namespace_id = ? AND active_flag = 1 AND prerequisites = ? AND lookup_id = 0 AND  active_to >= ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, "000000");
			selectPS.setString(3, activeDate.format("YYYY-MM-DD"));
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO dto = new ScheduleDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				ScheduleDTO lookupDTO = new ScheduleDTO();
				lookupDTO.setId(selectRS.getInt("lookup_id"));
				// dto.setLookupDTO(lookupDTO);
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				dto.setServiceNumber(selectRS.getString("service_number"));
				dto.setDisplayName(selectRS.getString("display_name"));
				dto.setApiDisplayName(selectRS.getString("api_display_name"));
				dto.setPnrStartCode(selectRS.getString("pnr_start_code"));
				int catgeryId = selectRS.getInt("category_id");
				if (catgeryId != 0) {
					ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
					categoryDTO.setId(catgeryId);
					dto.setCategory(categoryDTO);
				}
				
				String scheduleTag = selectRS.getString("tags");
				List<ScheduleTagDTO> scheduleTagList = convertScheduleTagList(scheduleTag);
				dto.setScheduleTagList(scheduleTagList);
				
				dto.setDistance(selectRS.getFloat("distance"));
				
				String sectorIds = selectRS.getString("sector_id");
				List<SectorDTO> sectorList = convertSectorList(sectorIds);
				dto.setSectorList(sectorList);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return list;
	}

	public ScheduleDTO getActiveSchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id, code, name, active_from, active_to,  service_number, display_name, api_display_name, pnr_start_code, day_of_week, category_id, lookup_id, tags, distance, active_flag FROM schedule sche WHERE  code =? AND namespace_id = ? AND active_flag = 1 AND prerequisites = ? AND lookup_id = 0 AND  active_to >= DATE(NOW())");
			selectPS.setString(1, scheduleDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, "000000");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				scheduleDTO.setId(selectRS.getInt("id"));
				scheduleDTO.setCode(selectRS.getString("code"));
				scheduleDTO.setName(selectRS.getString("name"));
				scheduleDTO.setActiveFlag(selectRS.getInt("active_flag"));
				ScheduleDTO lookupDTO = new ScheduleDTO();
				lookupDTO.setId(selectRS.getInt("lookup_id"));
				// dto.setLookupDTO(lookupDTO);
				scheduleDTO.setActiveFrom(selectRS.getString("active_from"));
				scheduleDTO.setActiveTo(selectRS.getString("active_to"));
				scheduleDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleDTO.setServiceNumber(selectRS.getString("service_number"));
				scheduleDTO.setDisplayName(selectRS.getString("display_name"));
				scheduleDTO.setApiDisplayName(selectRS.getString("api_display_name"));
				scheduleDTO.setPnrStartCode(selectRS.getString("pnr_start_code"));
				
				String scheduleTag = selectRS.getString("tags");
				List<ScheduleTagDTO> scheduleTagList = convertScheduleTagList(scheduleTag);
				scheduleDTO.setScheduleTagList(scheduleTagList);
				
				scheduleDTO.setDistance(selectRS.getFloat("distance"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleDTO;
	}

	private List<ScheduleTagDTO> convertScheduleTagList(String tagIds) {
		List<ScheduleTagDTO> scheduleTagList = new ArrayList<ScheduleTagDTO>();
		if (StringUtil.isNotNull(tagIds)) {
			String[] scheduleTagIds = tagIds.split(Text.COMMA);
			for (String tagId : scheduleTagIds) {
				if (tagId.equals(Numeric.ZERO)) {
					continue;
				}
				ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
				scheduleTagDTO.setId(Integer.valueOf(tagId));
				scheduleTagList.add(scheduleTagDTO);
			}
		}
		return scheduleTagList;
	}

	private List<SectorDTO> convertSectorList(String sectorIds) {
		List<SectorDTO> sectorList = new ArrayList<SectorDTO>();
		if (StringUtil.isNotNull(sectorIds)) {
			String[] sectors = sectorIds.split(Text.COMMA);
			for (String sectorId : sectors) {
				if (sectorId.equals(Numeric.ZERO)) {
					continue;
				}
				SectorDTO sectorDTO = new SectorDTO();
				sectorDTO.setId(Integer.valueOf(sectorId));
				sectorList.add(sectorDTO);
			}
		}
		return sectorList;
	}

}
