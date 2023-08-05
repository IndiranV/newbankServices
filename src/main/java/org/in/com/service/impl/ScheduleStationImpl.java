package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.cache.ScheduleCache;
import org.in.com.dao.ScheduleStationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleAuditLogService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class ScheduleStationImpl extends BaseImpl implements ScheduleStationService {
	@Autowired
	ScheduleActivityService scheduleActivityService;
	@Autowired
	StationService stationService;
	@Autowired
	ScheduleAuditLogService scheduleAuditLogService;

	public List<ScheduleStationDTO> get(AuthDTO authDTO, ScheduleStationDTO dto) {
		ScheduleStationDAO dao = new ScheduleStationDAO();
		List<ScheduleStationDTO> list = dao.get(authDTO, dto);
		for (ScheduleStationDTO stationDTO : list) {
			stationDTO.setStation(stationService.getStation(stationDTO.getStation()));
			for (ScheduleStationDTO overrideStationDTO : stationDTO.getOverrideList()) {
				overrideStationDTO.setStation(stationService.getStation(stationDTO.getStation()));
			}
		}
		return list;
	}

	public ScheduleStationDTO Update(AuthDTO authDTO, ScheduleStationDTO dto) {
		ScheduleStationDAO dao = new ScheduleStationDAO();
		for (ScheduleStationDTO stationDTO : dto.getList()) {
			if (StringUtil.isNotNull(stationDTO.getStation().getCode())) {
				stationDTO.setStation(stationService.getStation(stationDTO.getStation()));

				// Check Is Advance Booking Exist For Station
				if (stationDTO.getActiveFlag() != 1 && StringUtil.isNull(stationDTO.getLookupCode())) {
					isStationUsed(authDTO, stationDTO.getSchedule(), stationDTO.getStation());
				}

				// Activity Activity Log
				scheduleActivityService.scheduleStationActivity(authDTO, stationDTO);
			}
			if (StringUtil.isNotNull(stationDTO.getCode())) {
				scheduleAuditLogService.updateScheduleStationAudit(authDTO, stationDTO);
			}
		}
		dao.getIUD(authDTO, dto);
		ScheduleCache scheduleCache = new ScheduleCache();
		for (ScheduleStationDTO stationDTO : dto.getList()) {
			scheduleCache.removeScheduleDTO(authDTO, stationDTO.getSchedule());
		}
		return dto;
	}

	@Override
	public List<ScheduleStationDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		return null;
	}

	public List<ScheduleStationDTO> getScheduleStation(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCache scheduleCache = new ScheduleCache();
		List<ScheduleStationDTO> stationDTOList = scheduleCache.getScheduleStation(authDTO, scheduleDTO);
		if (stationDTOList == null && scheduleDTO.getId() != 0) {
			ScheduleStationDAO stationDAO = new ScheduleStationDAO();
			stationDTOList = stationDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleCache.putScheduleStation(authDTO, scheduleDTO, stationDTOList);
		}
		return stationDTOList;
	}

	public List<ScheduleStationDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		List<ScheduleStationDTO> stationList = getScheduleStation(authDTO, scheduleDTO);
		if (StringUtil.isNull(scheduleDTO.getTripDate())) {
			scheduleDTO.setTripDate(tripDate);
		}
		// Validate all stations
		for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
			ScheduleStationDTO stationDTO = iterator.next();
			// common validations
			if (stationDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(stationDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (stationDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(stationDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (stationDTO.getDayOfWeek() != null && stationDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (stationDTO.getDayOfWeek() != null && stationDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			int overrideRecentDays = 0;
			// Exception and override
			for (Iterator<ScheduleStationDTO> OverrideIterator = stationDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
				ScheduleStationDTO overrideStationDTO = OverrideIterator.next();
				// common validations
				if (overrideStationDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideStationDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().length() != 7) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					OverrideIterator.remove();
					continue;
				}
				// Remove if Exceptions
				if (overrideStationDTO.getMinitues() == -1) {
					stationDTO.setActiveFlag(-1);
					// iterator.remove();
					break;
				}
				// Override, time should follow in same day
				if (stationDTO.getMinitues() < 1440 && overrideStationDTO.getMinitues() >= 1440) {
					OverrideIterator.remove();
					continue;
				} // Second day
				else if (stationDTO.getMinitues() < 2880 && overrideStationDTO.getMinitues() >= 2880) {
					OverrideIterator.remove();
					continue;
				} // Third day
				else if (stationDTO.getMinitues() < 4320 && overrideStationDTO.getMinitues() >= 4320) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideRecentDays == 0 || DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) <= overrideRecentDays) {
					stationDTO.setMinitues(overrideStationDTO.getMinitues());
					overrideRecentDays = DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) + 1;
				}
			}
			stationDTO.setStation(stationService.getStation(stationDTO.getStation()));
		}
		return stationList;
	}

	@Override
	public void isStationUsed(AuthDTO authDTO, ScheduleDTO schedule, StationDTO station) {
		// Get Schedule Details
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.getScheduleDTO(authDTO, schedule);
		// Get Station Details
		getStationDTO(station);

		// Check Is Advance Booking Exist For Station
		ScheduleStationDAO scheduleStationDAO = new ScheduleStationDAO();
		boolean stationUsed = scheduleStationDAO.isStationUsed(authDTO, schedule, station);

		if (stationUsed) {
			throw new ServiceException(ErrorCode.SCHEDULE_STATION_USED_TICKET);
		}
	}
}
