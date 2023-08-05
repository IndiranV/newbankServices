package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.enumeration.CalendarAnnouncementCategoryEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

public class CalendarAnnouncementDAO {

	public void updateCalendarAnnouncement(AuthDTO authDTO, CalendarAnnouncementDTO calendarAnnouncementDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_CALENDAR_ANNOUNCEMENT_IUD(?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			pindex = 0;
			callableStatement.setString(++pindex, calendarAnnouncementDTO.getCode());
			callableStatement.setString(++pindex, calendarAnnouncementDTO.getName());
			callableStatement.setInt(++pindex, calendarAnnouncementDTO.getCategory().getId());
			callableStatement.setString(++pindex, getStateIds(calendarAnnouncementDTO.getStates()));
			callableStatement.setString(++pindex, calendarAnnouncementDTO.getActiveFrom());
			callableStatement.setString(++pindex, calendarAnnouncementDTO.getActiveTo());
			callableStatement.setString(++pindex, calendarAnnouncementDTO.getDayOfWeek());
			callableStatement.setString(++pindex, getDateString(calendarAnnouncementDTO.getDates()));
			callableStatement.setInt(++pindex, calendarAnnouncementDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				calendarAnnouncementDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
	
	public List<CalendarAnnouncementDTO> getAllCalendarAnnouncement(AuthDTO authDTO) {
		List<CalendarAnnouncementDTO> calendarAnnouncementList = new ArrayList<CalendarAnnouncementDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, category_id, state_id, active_from, active_to, day_of_week, dates, active_flag FROM calendar_announcement WHERE active_flag < 2");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				CalendarAnnouncementDTO calendarAnnouncementDTO = new CalendarAnnouncementDTO();
				calendarAnnouncementDTO.setCode(selectRS.getString("code"));
				calendarAnnouncementDTO.setName(selectRS.getString("name"));
				calendarAnnouncementDTO.setActiveFrom(selectRS.getString("active_from"));
				calendarAnnouncementDTO.setActiveTo(selectRS.getString("active_to"));
				calendarAnnouncementDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				calendarAnnouncementDTO.setCategory(CalendarAnnouncementCategoryEM.getCategoryEM(selectRS.getInt("category_id")));
				
				List<StateDTO> stateList = convertStateList(selectRS.getString("state_id"));
				calendarAnnouncementDTO.setStates(stateList);
				
				List<DateTime> dateList = convertDateList(selectRS.getString("dates"));
				calendarAnnouncementDTO.setDates(dateList);
				
				calendarAnnouncementDTO.setActiveFlag(selectRS.getInt("active_flag"));
				calendarAnnouncementList.add(calendarAnnouncementDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return calendarAnnouncementList;
	}
	
	public List<CalendarAnnouncementDTO> getAllCalendarAnnouncementforZoneSync(String syncDate) {
		List<CalendarAnnouncementDTO> calendarAnnouncementList = new ArrayList<CalendarAnnouncementDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, category_id, state_id, active_from, active_to, day_of_week, dates, active_flag FROM calendar_announcement WHERE DATE(updated_at) >= ?");
			selectPS.setString(1, syncDate);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				CalendarAnnouncementDTO calendarAnnouncementDTO = new CalendarAnnouncementDTO();
				calendarAnnouncementDTO.setCode(selectRS.getString("code"));
				calendarAnnouncementDTO.setName(selectRS.getString("name"));
				calendarAnnouncementDTO.setActiveFrom(selectRS.getString("active_from"));
				calendarAnnouncementDTO.setActiveTo(selectRS.getString("active_to"));
				calendarAnnouncementDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				calendarAnnouncementDTO.setCategory(CalendarAnnouncementCategoryEM.getCategoryEM(selectRS.getInt("category_id")));
				
				List<StateDTO> stateList = convertStateList(selectRS.getString("state_id"));
				calendarAnnouncementDTO.setStates(stateList);
				
				List<DateTime> dateList = convertDateList(selectRS.getString("dates"));
				calendarAnnouncementDTO.setDates(dateList);
				
				calendarAnnouncementDTO.setActiveFlag(selectRS.getInt("active_flag"));
				calendarAnnouncementList.add(calendarAnnouncementDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return calendarAnnouncementList;
	}
	
	public String getZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DATE(MAX(updated_at)) as zoneSyncDate FROM calendar_announcement");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2022-10-20";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}
	
	public List<CalendarAnnouncementDTO> updateZoneSync(AuthDTO authDTO, List<CalendarAnnouncementDTO> list) {
		try {
			int batchCount = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_CALENDAR_ANNOUNCEMENT_ZONESYNC(?,?,?,?,?, ?,?,?,?,?)}");
			for (CalendarAnnouncementDTO calendarAnnouncementDTO : list) {
				try {
					int pindex = 0;
					callableStatement.setString(++pindex, calendarAnnouncementDTO.getCode());
					callableStatement.setString(++pindex, calendarAnnouncementDTO.getName());
					callableStatement.setInt(++pindex, calendarAnnouncementDTO.getCategory() != null ? calendarAnnouncementDTO.getCategory().getId() : 0);
					callableStatement.setString(++pindex, getStateIds(calendarAnnouncementDTO.getStates()));
					callableStatement.setString(++pindex, calendarAnnouncementDTO.getActiveFrom());
					callableStatement.setString(++pindex, calendarAnnouncementDTO.getActiveTo());
					callableStatement.setString(++pindex, calendarAnnouncementDTO.getDayOfWeek());
					callableStatement.setString(++pindex, getDateString(calendarAnnouncementDTO.getDates()));
					callableStatement.setInt(++pindex, calendarAnnouncementDTO.getActiveFlag());
					callableStatement.setInt(++pindex, authDTO.getUser().getId());
					callableStatement.addBatch();
					batchCount++;

					if (batchCount > 100) {
						callableStatement.executeBatch();
						callableStatement.clearBatch();
						callableStatement.clearParameters();
						batchCount = 0;
					}

				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("Calendar Announcement ZoneSync : " + calendarAnnouncementDTO.getCode() + "-" + calendarAnnouncementDTO.getName());
				}
			}
			if (batchCount > 0) {
				callableStatement.executeBatch();
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}
	
	private String getDateString(List<DateTime> dateList) {
		StringBuilder dates = new StringBuilder();
		if (dateList != null) {
			for (DateTime datetime : dateList) {
				if (dates.length() > 0) {
					dates.append(",");
				}
				dates.append(DateUtil.convertDate(datetime));
			}
		}
		return dates.toString();
	}
	
	private String getStateIds(List<StateDTO> stateList) {
		StringBuilder dates = new StringBuilder();
		if (stateList != null) {
			for (StateDTO stateDTO : stateList) {
				if (stateDTO.getId() == 0) {
					continue;
				}
				if (dates.length() > 0) {
					dates.append(",");
				}
				dates.append(stateDTO.getId());
			}
		}
		return dates.toString();
	}
	
	private List<StateDTO> convertStateList(String stateIds) {
		List<StateDTO> stateList = new ArrayList<>();
		if (StringUtil.isNotNull(stateIds)) {
			for (String stateId : stateIds.split(Text.COMMA)) {
				if (StringUtil.isNull(stateId) || stateId.equals(Numeric.ZERO)) {
					continue;
				}
				StateDTO stateDTO = new StateDTO();
				stateDTO.setId(Integer.valueOf(stateId));
				stateList.add(stateDTO);
			}
		}
		return stateList;
	}
	
	private List<DateTime> convertDateList(String dates) {
		List<DateTime> dateList = new ArrayList<>();
		if (StringUtil.isNotNull(dates)) {
			for (String date : dates.split(Text.COMMA)) {
				if (!DateUtil.isValidDate(date)) {
					continue;
				}
				dateList.add(DateUtil.getDateTime(date));
			}
		}
		return dateList;
	}
}
