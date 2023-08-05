package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.StateDTO;

import hirondelle.date4j.DateTime;

public interface CalendarAnnouncementService extends BaseService<CalendarAnnouncementDTO> {

	public Map<String, List<Map<String, Object>>> getActiveCalendarAnnouncement(AuthDTO authDTO, DateTime fromDate, DateTime toDate);
	
	public List<CalendarAnnouncementDTO> getAllCalendarAnnouncementForZoneSync(AuthDTO authDTO, String syncDate);
	
	public List<CalendarAnnouncementDTO> getAllCalendarAnnouncement(AuthDTO authDTO, List<StateDTO> stateList);
}
