package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.in.com.cache.CalendarAnnouncementCache;
import org.in.com.constants.Numeric;
import org.in.com.dao.CalendarAnnouncementDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.service.CalendarAnnouncementService;
import org.in.com.service.StateService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StreamUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class CalendarAnnouncementServiceImpl extends CalendarAnnouncementCache implements CalendarAnnouncementService {

	@Autowired
	StateService stateService;
	@Autowired
	StationService stationService;
	
	@Override
	public List<CalendarAnnouncementDTO> get(AuthDTO authDTO, CalendarAnnouncementDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CalendarAnnouncementDTO> getAll(AuthDTO authDTO) {
		return null;
	}
	
	public List<CalendarAnnouncementDTO> getAllCalendarAnnouncement(AuthDTO authDTO, List<StateDTO> stateList) {
		for (StateDTO stateDTO : stateList) {
			StateDTO sateDTO2 = stateService.getState(stateDTO);
			stateDTO.setId(sateDTO2.getId());
		}
		CalendarAnnouncementDAO dao = new CalendarAnnouncementDAO();
		List<CalendarAnnouncementDTO> calendarAnnouncementList = dao.getAllCalendarAnnouncement(authDTO);
		for (Iterator<CalendarAnnouncementDTO> announcementItr = calendarAnnouncementList.iterator(); announcementItr.hasNext();) {
			CalendarAnnouncementDTO annnouncementDTO = announcementItr.next();
			for (StateDTO sateDTO : annnouncementDTO.getStates()) {
				StateDTO sateDTO2 = stateService.getState(sateDTO);
				sateDTO.setCode(sateDTO2.getCode());
				sateDTO.setName(sateDTO2.getName());
			}
			
			List<StateDTO> repoStates = getMatchingStates(annnouncementDTO.getStates(), stateList);
			if (!annnouncementDTO.getStates().isEmpty() && !stateList.isEmpty() && repoStates.isEmpty()) {
				announcementItr.remove();
				continue;
			}
		}
		return calendarAnnouncementList;
	}

	@Override
	public CalendarAnnouncementDTO Update(AuthDTO authDTO, CalendarAnnouncementDTO dto) {
		CalendarAnnouncementDAO dao = new CalendarAnnouncementDAO();
		for (StateDTO sateDTO : dto.getStates()) {
			StateDTO sateDTO2 = stateService.getState(sateDTO);
			sateDTO.setId(sateDTO2.getId());
		}
		dao.updateCalendarAnnouncement(authDTO, dto);
		
		removeCalendarAnnouncement(authDTO);
		return dto;
	}

	@Override
	public List<CalendarAnnouncementDTO> getAllCalendarAnnouncementForZoneSync(AuthDTO authDTO, String syncDate) {
		CalendarAnnouncementDAO dao = new CalendarAnnouncementDAO();
		List<CalendarAnnouncementDTO> calendarAnnouncementList = dao.getAllCalendarAnnouncementforZoneSync(syncDate);
		for (CalendarAnnouncementDTO annnouncementDTO : calendarAnnouncementList) {
			for (StateDTO sateDTO : annnouncementDTO.getStates()) {
				StateDTO sateDTO2 = stateService.getState(sateDTO);
				sateDTO.setCode(sateDTO2.getCode());
				sateDTO.setName(sateDTO2.getName());
			}
		}
		return calendarAnnouncementList;
	}

	@Override
	public Map<String, List<Map<String, Object>>> getActiveCalendarAnnouncement(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
		
		List<DateTime> totalDates = DateUtil.getDateList(fromDate, toDate);
		List<StateDTO> namespaceStates = getUniqueNamespaceStates(authDTO);
		
		CalendarAnnouncementCache calendarAnnouncementCache = new CalendarAnnouncementCache();
		List<CalendarAnnouncementDTO> calendarAnnouncementList = calendarAnnouncementCache.getCalendarAnnouncements(authDTO);
		
		for (DateTime tripDate : totalDates) {
			for (Iterator<CalendarAnnouncementDTO> calendarItr = calendarAnnouncementList.iterator(); calendarItr.hasNext();) {
				CalendarAnnouncementDTO calendarAnnouncement = calendarItr.next();
				if (calendarAnnouncement.getActiveFlag() != Numeric.ONE_INT) {
					continue;
				}
				if (StringUtil.isNotNull(calendarAnnouncement.getActiveFrom()) && !tripDate.gteq(new DateTime(calendarAnnouncement.getActiveFrom()))) {
					continue;
				}
				if (StringUtil.isNotNull(calendarAnnouncement.getActiveTo()) && !tripDate.lteq(new DateTime(calendarAnnouncement.getActiveTo()))) {
					continue;
				}
				if (StringUtil.isNotNull(calendarAnnouncement.getDayOfWeek()) && calendarAnnouncement.getDayOfWeek().length() != 7) {
					continue;
				}
				if (StringUtil.isNotNull(calendarAnnouncement.getDayOfWeek()) && calendarAnnouncement.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					continue;
				}
				if (calendarAnnouncement.getDates() != null && !calendarAnnouncement.getDates().isEmpty() && !calendarAnnouncement.getDates().stream().anyMatch(date -> date.compareTo(tripDate) == 0)) {
					continue;
				}
				
				Map<String, Object> dataMap = new HashMap<>();
				dataMap.put("name", calendarAnnouncement.getName());
				
				Map<String, String> categoryMap = new HashMap<>();
				categoryMap.put("code", calendarAnnouncement.getCategory().getCode());
				categoryMap.put("name", calendarAnnouncement.getCategory().getName());
				dataMap.put("category", categoryMap);
				
				String mapKey = DateUtil.convertDate(tripDate);
				
				if (calendarAnnouncement.getStates().isEmpty()) {
					dataMap.put("state", new ArrayList<Map<String, String>>());
					
					if (resultMap.get(mapKey) != null) {
						List<Map<String, Object>> dataMapList = resultMap.get(mapKey);
						dataMapList.add(dataMap);
						resultMap.put(mapKey, dataMapList);
					}
					else {
						List<Map<String, Object>> dataMapList = new ArrayList<>();
						dataMapList.add(dataMap);
						resultMap.put(mapKey, dataMapList);
					}
				}
				else if (!calendarAnnouncement.getStates().isEmpty()) {
					List<StateDTO> stateList = getMatchingStates(calendarAnnouncement.getStates(), namespaceStates);
					if (stateList.isEmpty()) {
						continue;
					}
					List<Map<String, String>> stateMapList = new ArrayList<>();
					for (StateDTO stateDTO : stateList) {
						Map<String, String> stateMap = new HashMap<>();
						stateMap.put("code", stateDTO.getCode());
						stateMap.put("name", stateDTO.getName());
						stateMapList.add(stateMap);
					}
					dataMap.put("state", stateMapList);
					
					if (resultMap.get(mapKey) != null) {
						List<Map<String, Object>> dataMapList = resultMap.get(mapKey);
						dataMapList.add(dataMap);
						resultMap.put(mapKey, dataMapList);
					}
					else {
						List<Map<String, Object>> dataMapList = new ArrayList<>();
						dataMapList.add(dataMap);
						resultMap.put(mapKey, dataMapList);
					}
				}
			}
		}
		
		// sorting based on date
//		List<Map.Entry<String, List<Map<String, Object>>>> entries = new ArrayList<>(dataEntryMap.entrySet());
//		Collections.sort(entries, new Comparator<Map.Entry<String, List<Map<String, Object>>>>() {
//			@Override
//			public int compare(Map.Entry<String, List<Map<String, Object>>> o1, Map.Entry<String, List<Map<String, Object>>> o2) {
//				return new CompareToBuilder().append(new DateTime(o1.getKey()), new DateTime(o2.getKey())).toComparison();
//			}
//		});
//		
//		for (Map.Entry<String, List<Map<String, Object>>> entry : entries) {
//			finalDataMap.put(entry.getKey(), entry.getValue());
//		}
		return resultMap;
	}
	
	public static List<StateDTO> getMatchingStates(List<StateDTO> repoStates, List<StateDTO> states) {
		Map<String, StateDTO> stateMap = new HashMap<>();
		if (repoStates != null && states != null) {
			for (StateDTO state : states) {
				for (StateDTO repoState : repoStates) {
					if (state.getId() != repoState.getId()) {
						continue;
					}
					stateMap.put(state.getCode(), state);
				}
			}
		}
		return new ArrayList<StateDTO>(stateMap.values());
	}
	
	private List<StateDTO> getUniqueNamespaceStates(AuthDTO authDTO) {
		List<StateDTO> namespaceStates = new ArrayList<StateDTO>();

		List<StationDTO> stationList = stationService.getCommerceStation(authDTO);
		List<String> statCodes = stationList.stream().filter(StreamUtil.distinctByKey(stn -> stn.getState().getCode())).map(stn -> stn.getState().getCode()).collect(Collectors.toList());
	
		if (authDTO.getNamespace().getProfile().getState() != null && !statCodes.contains(authDTO.getNamespace().getProfile().getState().getCode())) {
			statCodes.add(authDTO.getNamespace().getProfile().getState().getCode());
		}
		for (String stateCode : statCodes) {
			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(stateCode);
			stateDTO = stateService.getState(stateDTO);
			namespaceStates.add(stateDTO);
		}
		return namespaceStates;
	}
}
