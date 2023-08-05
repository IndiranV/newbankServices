package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.CalendarAnnouncementCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.CalendarAnnouncementDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.enumeration.CalendarAnnouncementCategoryEM;
import org.in.com.utils.DateUtil;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

public class CalendarAnnouncementCache {
	
	private static String CACHEKEY = "CALENDAR";
	
	public List<CalendarAnnouncementDTO> getCalendarAnnouncements(AuthDTO authDTO) {
		List<CalendarAnnouncementCacheDTO> calendarAnnouncementCacheList = null;
		List<CalendarAnnouncementDTO> calendarAnnouncementList = null;
		Element element = EhcacheManager.getCalendarAnnouncementEhCache().get(CACHEKEY);
		if (element != null) {
			calendarAnnouncementCacheList = (List<CalendarAnnouncementCacheDTO>) element.getObjectValue();
			calendarAnnouncementList = bindFromCacheObject(calendarAnnouncementCacheList);
		}
		else {
			CalendarAnnouncementDAO dao  = new CalendarAnnouncementDAO();
			calendarAnnouncementList = dao.getAllCalendarAnnouncement(authDTO);
			if (calendarAnnouncementList != null && !calendarAnnouncementList.isEmpty()) {
				calendarAnnouncementCacheList = bindToCacheObject(calendarAnnouncementList);
				element = new Element(CACHEKEY, calendarAnnouncementCacheList);
				EhcacheManager.getCalendarAnnouncementEhCache().put(element);
			}
		}
		return calendarAnnouncementList;
	}

	private List<CalendarAnnouncementDTO> bindFromCacheObject(List<CalendarAnnouncementCacheDTO> cacheList) {
		List<CalendarAnnouncementDTO> calendarAnnouncementList = new ArrayList<CalendarAnnouncementDTO>();
		for (CalendarAnnouncementCacheDTO cacheDTO : cacheList) {
			CalendarAnnouncementDTO calendarAnnouncementDTO = new CalendarAnnouncementDTO();
			calendarAnnouncementDTO.setCode(cacheDTO.getCode());
			calendarAnnouncementDTO.setName(cacheDTO.getName());
			calendarAnnouncementDTO.setActiveFrom(cacheDTO.getActiveFrom());
			calendarAnnouncementDTO.setActiveTo(cacheDTO.getActiveTo());
			calendarAnnouncementDTO.setDayOfWeek(cacheDTO.getDayOfWeek());
			calendarAnnouncementDTO.setCategory(CalendarAnnouncementCategoryEM.getCategoryEM(cacheDTO.getCategoryCode()));
			
			List<StateDTO> states = new ArrayList<>();
			for (Integer stateId : cacheDTO.getStateId()) {
				if (stateId == 0) {
					continue;
				}
				StateDTO stateDTO = new StateDTO();
				stateDTO.setId(stateId);
				states.add(stateDTO);
			}
			calendarAnnouncementDTO.setStates(states);
			
			List<DateTime> dateList = new ArrayList<>();
			for (String date : cacheDTO.getDates()) {
				if (!DateUtil.isValidDate(date)) {
					continue;
				}
				dateList.add(DateUtil.getDateTime(date));
			}
			calendarAnnouncementDTO.setDates(dateList);
			calendarAnnouncementDTO.setActiveFlag(cacheDTO.getActiveFlag());
			calendarAnnouncementList.add(calendarAnnouncementDTO);
		}
		return calendarAnnouncementList;
	}

	private List<CalendarAnnouncementCacheDTO> bindToCacheObject(List<CalendarAnnouncementDTO> bannerList) {
		List<CalendarAnnouncementCacheDTO> cacheList = new ArrayList<CalendarAnnouncementCacheDTO>();
		for (CalendarAnnouncementDTO calendarAnnouncementDTO : bannerList) {
			CalendarAnnouncementCacheDTO cacheDTO = new CalendarAnnouncementCacheDTO();
			cacheDTO.setCode(calendarAnnouncementDTO.getCode());
			cacheDTO.setName(calendarAnnouncementDTO.getName());
			cacheDTO.setActiveFrom(calendarAnnouncementDTO.getActiveFrom());
			cacheDTO.setActiveTo(calendarAnnouncementDTO.getActiveTo());
			cacheDTO.setDayOfWeek(calendarAnnouncementDTO.getDayOfWeek());
			cacheDTO.setCategoryCode(calendarAnnouncementDTO.getCategory() != null ? calendarAnnouncementDTO.getCategory().getCode() : Text.NA);
			
			List<String> dateList = new ArrayList<>();
			for (DateTime date : calendarAnnouncementDTO.getDates()) {
				dateList.add(DateUtil.convertDate(date));
			}
			cacheDTO.setDates(dateList);
			
			List<Integer> stateIdList = new ArrayList<>();
			for (StateDTO stateDTO : calendarAnnouncementDTO.getStates()) {
				stateIdList.add(stateDTO.getId());
			}
			cacheDTO.setStateId(stateIdList);
			cacheDTO.setActiveFlag(calendarAnnouncementDTO.getActiveFlag());
			cacheList.add(cacheDTO);
		}
		return cacheList;
	}

	public void removeCalendarAnnouncement(AuthDTO authDTO) {
		EhcacheManager.getCalendarAnnouncementEhCache().removeAll();
	}
}
