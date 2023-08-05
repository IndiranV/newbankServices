package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.CancellationTermsCache;
import org.in.com.cache.ScheduleCache;
import org.in.com.dao.ScheduleCancellationTermDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleCancellationTermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class ScheduleCancellationTermImpl extends CacheCentral implements ScheduleCancellationTermService {
	@Autowired
	ScheduleActivityService scheduleActivityService;
	@Autowired
	GroupService groupService;

	public List<ScheduleCancellationTermDTO> get(AuthDTO authDTO, ScheduleCancellationTermDTO dto) {
		ScheduleCancellationTermDAO dao = new ScheduleCancellationTermDAO();
		CancellationTermsCache termsCache = new CancellationTermsCache();
		List<ScheduleCancellationTermDTO> list = dao.get(authDTO, dto);
		for (ScheduleCancellationTermDTO scheduleCancellationTermDTO : list) {
			if (scheduleCancellationTermDTO.getGroup() != null) {
				scheduleCancellationTermDTO.setGroup(groupService.getGroup(authDTO, scheduleCancellationTermDTO.getGroup()));
			}
			scheduleCancellationTermDTO.setCancellationTerm(termsCache.getCancellationTermDTOById(authDTO, scheduleCancellationTermDTO.getCancellationTerm()));

			for (ScheduleCancellationTermDTO lookupScheduleCancellationTermDTO : scheduleCancellationTermDTO.getOverrideList()) {
				if (lookupScheduleCancellationTermDTO.getGroup() != null) {
					lookupScheduleCancellationTermDTO.setGroup(groupService.getGroup(authDTO, lookupScheduleCancellationTermDTO.getGroup()));
				}
				lookupScheduleCancellationTermDTO.setCancellationTerm(termsCache.getCancellationTermDTOById(authDTO, lookupScheduleCancellationTermDTO.getCancellationTerm()));
			}
		}
		return list;
	}

	public ScheduleCancellationTermDTO Update(AuthDTO authDTO, ScheduleCancellationTermDTO cancellationTermDTO) {
		ScheduleCancellationTermDAO dao = new ScheduleCancellationTermDAO();
		// Activity Activity Log
		scheduleActivityService.scheduleCancellationTermsActivity(authDTO, cancellationTermDTO);
		dao.getIUD(authDTO, cancellationTermDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		for (ScheduleCancellationTermDTO scheduleCancellationTerm : cancellationTermDTO.getList()) {
			scheduleCache.removeScheduleDTO(authDTO, scheduleCancellationTerm.getSchedule());
		}
		scheduleCache.removeScheduleDTO(authDTO, cancellationTermDTO.getSchedule());
		return cancellationTermDTO;
	}

	@Override
	public boolean CheckCancellationTermUsed(AuthDTO authDTO, CancellationTermDTO dto) {
		ScheduleCancellationTermDAO dao = new ScheduleCancellationTermDAO();
		return dao.CheckCancellationTermUsed(authDTO, dto);
	}

	public ScheduleCancellationTermDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCancellationTermDTO scheduleTermDTO = null;

		ScheduleCache scheduleCache = new ScheduleCache();
		List<ScheduleCancellationTermDTO> scheduleCancellationTermList = scheduleCache.getScheduleCancellationTermDTO(authDTO, scheduleDTO);
		for (Iterator<ScheduleCancellationTermDTO> iterator = scheduleCancellationTermList.iterator(); iterator.hasNext();) {
			ScheduleCancellationTermDTO termDTO = iterator.next();
			// Stage Date Time validations
			if (termDTO.getActiveFrom() != null && scheduleDTO.getTripDate().lt(new DateTime(termDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (termDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(termDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (termDTO.getDayOfWeek() != null && termDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (termDTO.getDayOfWeek() != null && termDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (termDTO.getGroup() != null && termDTO.getGroup().getId() != 0 && termDTO.getGroup().getId() != authDTO.getGroup().getId()) {
				iterator.remove();
				continue;
			}
			// Exceptions and Override
			for (Iterator<ScheduleCancellationTermDTO> overrideIterator = termDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleCancellationTermDTO overrideScheduleCancellationTermDTO = overrideIterator.next();
				if (scheduleDTO.getTripDate().lt(new DateTime(overrideScheduleCancellationTermDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(overrideScheduleCancellationTermDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleCancellationTermDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleCancellationTermDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// exceptions/Override for Group Level
				if (overrideScheduleCancellationTermDTO.getGroup().getId() != termDTO.getGroup().getId()) {
					overrideIterator.remove();
					continue;
				}
				// Apply Override
				termDTO.setCancellationTerm(overrideScheduleCancellationTermDTO.getCancellationTerm());
			}
		}

		// Filter for Group Level or User Level
		for (Iterator<ScheduleCancellationTermDTO> iterator = scheduleCancellationTermList.iterator(); iterator.hasNext();) {
			ScheduleCancellationTermDTO termDTO = iterator.next();
			if ((termDTO.getGroup() == null || termDTO.getGroup().getId() == 0) && scheduleTermDTO == null) {
				scheduleTermDTO = termDTO;
			}
			else if (termDTO.getGroup().getId() == authDTO.getGroup().getId()) {
				scheduleTermDTO = termDTO;
				break;
			}
		}
		return scheduleTermDTO;
	}

}
