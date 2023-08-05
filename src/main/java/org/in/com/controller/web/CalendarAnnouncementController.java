package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CalendarAnnouncementIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.enumeration.CalendarAnnouncementCategoryEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.CalendarAnnouncementService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;

@Controller
@RequestMapping("{authtoken}/calendar/announcement")
public class CalendarAnnouncementController extends BaseController {

	@Autowired
	CalendarAnnouncementService calendarAnnouncementService;
	
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CalendarAnnouncementIO> updateCalendarAnnouncement(@PathVariable("authtoken") String authtoken, @RequestBody CalendarAnnouncementIO announcementIO) {
		CalendarAnnouncementIO calendarAnnouncementIO = new CalendarAnnouncementIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		CalendarAnnouncementDTO calendatAnouncementDTO = new CalendarAnnouncementDTO();
		calendatAnouncementDTO.setCode(announcementIO.getCode());
		calendatAnouncementDTO.setName(announcementIO.getName());
		calendatAnouncementDTO.setActiveFrom(announcementIO.getActiveFrom());
		calendatAnouncementDTO.setActiveTo(announcementIO.getActiveTo());
		calendatAnouncementDTO.setDayOfWeek(announcementIO.getDayOfWeek());
		calendatAnouncementDTO.setCategory(announcementIO.getCategory() != null ? CalendarAnnouncementCategoryEM.getCategoryEM(announcementIO.getCategory().getCode()) : null);
		
		List<StateDTO> stateList = new ArrayList<>();
		if (announcementIO.getStates() != null) {
			for (StateIO stateIO : announcementIO.getStates()) {
				if (StringUtil.isNull(stateIO.getCode())) {
					continue;
				}
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(stateIO.getCode());
				stateList.add(stateDTO);
			}
		}
		calendatAnouncementDTO.setStates(stateList);
		
		List<DateTime> dateList = new ArrayList<>();
		if (announcementIO.getDates() != null) {
			for (String date : announcementIO.getDates() ) {
				if (!DateUtil.isValidDate(date)) {
					continue;	
				}
				dateList.add(DateUtil.getDateTime(date));
			}
		}
		calendatAnouncementDTO.setDates(dateList);
		calendatAnouncementDTO.setActiveFlag(announcementIO.getActiveFlag());
		calendarAnnouncementService.Update(authDTO, calendatAnouncementDTO);
		calendarAnnouncementIO.setCode(calendatAnouncementDTO.getCode());
		return ResponseIO.success(calendarAnnouncementIO);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CalendarAnnouncementIO>> getCalendarAnnouncement(@PathVariable("authtoken") String authtoken, String stateCodes) {
		List<CalendarAnnouncementIO> announcementList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		
		List<StateDTO> states = new ArrayList<>();
		if (StringUtil.isNotNull(stateCodes)) {
			for (String stateCode : stateCodes.split(Text.COMMA)) {
				if (StringUtil.isNull(stateCode)) {
					continue;
				}
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(stateCode);
				states.add(stateDTO);
			}
		}
		
		List<CalendarAnnouncementDTO> list = calendarAnnouncementService.getAllCalendarAnnouncement(authDTO, states);
		for (CalendarAnnouncementDTO calendatAnouncementDTO : list) {
			CalendarAnnouncementIO calendarAnouncementIO = new CalendarAnnouncementIO();
			calendarAnouncementIO.setCode(calendatAnouncementDTO.getCode());
			calendarAnouncementIO.setName(calendatAnouncementDTO.getName());
			calendarAnouncementIO.setActiveFrom(calendatAnouncementDTO.getActiveFrom());
			calendarAnouncementIO.setActiveTo(calendatAnouncementDTO.getActiveTo());
			calendarAnouncementIO.setDayOfWeek(calendatAnouncementDTO.getDayOfWeek());
			
			BaseIO categoryIO = new BaseIO();
			categoryIO.setCode(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getCode() : null);
			categoryIO.setName(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getName() : null);
			calendarAnouncementIO.setCategory(categoryIO);
			
			List<StateIO> stateList = new ArrayList<StateIO>();
			for (StateDTO stateDTO : calendatAnouncementDTO.getStates()) {
				StateIO stateIO = new StateIO();
				stateIO.setCode(stateDTO.getCode());
				stateIO.setName(stateDTO.getName());
				stateList.add(stateIO);
			}
			calendarAnouncementIO.setStates(stateList);
			
			List<String> dateList = new ArrayList<>();
			for (DateTime date : calendatAnouncementDTO.getDates() ) {
				dateList.add(DateUtil.convertDate(date));
			}
			calendarAnouncementIO.setDates(dateList);
			
			calendarAnouncementIO.setActiveFlag(calendatAnouncementDTO.getActiveFlag());
			announcementList.add(calendarAnouncementIO);
		}
		return ResponseIO.success(announcementList);
	}
	
	@RequestMapping(value = "/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CalendarAnnouncementIO>> getCalendarAnnouncementForZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) {
		List<CalendarAnnouncementIO> announcementList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		
		List<CalendarAnnouncementDTO> list = calendarAnnouncementService.getAllCalendarAnnouncementForZoneSync(authDTO, syncDate);
		for (CalendarAnnouncementDTO calendatAnouncementDTO : list) {
			CalendarAnnouncementIO calendarAnouncementIO = new CalendarAnnouncementIO();
			calendarAnouncementIO.setCode(calendatAnouncementDTO.getCode());
			calendarAnouncementIO.setName(calendatAnouncementDTO.getName());
			calendarAnouncementIO.setActiveFrom(calendatAnouncementDTO.getActiveFrom());
			calendarAnouncementIO.setActiveTo(calendatAnouncementDTO.getActiveTo());
			calendarAnouncementIO.setDayOfWeek(calendatAnouncementDTO.getDayOfWeek());
			
			BaseIO categoryIO = new BaseIO();
			categoryIO.setCode(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getCode() : null);
			categoryIO.setName(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getName() : null);
			calendarAnouncementIO.setCategory(categoryIO);
			
			List<StateIO> stateList = new ArrayList<StateIO>();
			for (StateDTO stateDTO : calendatAnouncementDTO.getStates()) {
				StateIO stateIO = new StateIO();
				stateIO.setCode(stateDTO.getCode());
				stateIO.setName(stateDTO.getName());
				stateList.add(stateIO);
			}
			calendarAnouncementIO.setStates(stateList);
			
			List<String> dateList = new ArrayList<>();
			for (DateTime date : calendatAnouncementDTO.getDates() ) {
				dateList.add(DateUtil.convertDate(date));
			}
			calendarAnouncementIO.setDates(dateList);
			
			calendarAnouncementIO.setActiveFlag(calendatAnouncementDTO.getActiveFlag());
			announcementList.add(calendarAnouncementIO);
		}
		return ResponseIO.success(announcementList);
	}
	
	@RequestMapping(value = "/active", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<Map<String, Object>>>> getActiveCalendarAnnouncement(@PathVariable("authtoken") String authtoken, String fromDate, String toDate) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
		DateTime fromDatetime = DateUtil.getDateTime(fromDate);
		DateTime toDatetime = DateUtil.getDateTime(toDate);
		
		Map<String, List<Map<String, Object>>> dataMap = calendarAnnouncementService.getActiveCalendarAnnouncement(authDTO, fromDatetime, toDatetime);
		return ResponseIO.success(dataMap);
	}
}
