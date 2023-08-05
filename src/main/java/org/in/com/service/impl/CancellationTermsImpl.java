package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.CancellationTermsCache;
import org.in.com.cache.ScheduleCache;
import org.in.com.dao.CancellationTermsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.ScheduleCancellationTermService;
import org.in.com.service.TripService;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class CancellationTermsImpl extends CancellationTermsCache implements CancellationTermsService {
	@Autowired
	TripService tripService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	ScheduleCancellationTermService scheduleTermService;
	@Autowired
	CancelTicketService cancelTicketService;
	private static final Logger logger = LoggerFactory.getLogger(CancellationTermsImpl.class);

	public List<CancellationTermDTO> get(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		CancellationTermsCache manager = new CancellationTermsCache();
		List<CancellationTermDTO> list = new ArrayList<>();
		cancellationTermDTO = manager.getCancellationTermDTO(authDTO, cancellationTermDTO);
		list.add(cancellationTermDTO);
		return list;
	}

	public List<CancellationTermDTO> getAll(AuthDTO authDTO) {
		CancellationTermsDAO termsDAO = new CancellationTermsDAO();
		return termsDAO.getAllCancellationTerms(authDTO);
	}

	public CancellationTermDTO Update(AuthDTO authDTO, CancellationTermDTO dto) {
		CancellationTermsDAO termsDAO = new CancellationTermsDAO();
		if (dto.getActiveFlag() != 1 && scheduleTermService.CheckCancellationTermUsed(authDTO, dto)) {
			throw new ServiceException(ErrorCode.CANCELLATION_USED_SCHEDULE);
		}
		termsDAO.getCancellationTermsIUD(authDTO, dto);
		removeCancellationTermDTO(authDTO, dto);
		return dto;
	}

	@Override
	public CancellationTermDTO getCancellationTermsByTripDTO(AuthDTO authDTO, UserDTO userDTO, TripDTO tripDTO) {
		ScheduleCancellationTermDTO cancellationTermDTO = null;
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDTO scheduleDTO = tripDTO.getSchedule();
		if (StringUtil.isNull(tripDTO.getSchedule().getCode())) {
			scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
		}
		List<ScheduleCancellationTermDTO> scheduleCancellationTermList = scheduleCache.getScheduleCancellationTermDTO(authDTO, scheduleDTO);
		for (Iterator<ScheduleCancellationTermDTO> iterator = scheduleCancellationTermList.iterator(); iterator.hasNext();) {
			ScheduleCancellationTermDTO termDTO = iterator.next();
			// Stage Date Time validations
			if (termDTO.getActiveFrom() != null && tripDTO.getTripDate().lt(new DateTime(termDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (termDTO.getActiveTo() != null && !tripDTO.getTripDate().lteq(new DateTime(termDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (termDTO.getDayOfWeek() != null && termDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (termDTO.getDayOfWeek() != null && termDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}

			if (termDTO.getCancellationTerm() == null || termDTO.getCancellationTerm().getId() == 0) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleCancellationTermDTO> overrideIterator = termDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleCancellationTermDTO overrideScheduleCancellationTermDTO = overrideIterator.next();
				if (tripDTO.getTripDate().lt(new DateTime(overrideScheduleCancellationTermDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDTO.getTripDate().lteq(new DateTime(overrideScheduleCancellationTermDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleCancellationTermDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleCancellationTermDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
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
			if ((termDTO.getGroup() == null || termDTO.getGroup().getId() == 0) && cancellationTermDTO == null) {
				cancellationTermDTO = termDTO;
			}
			else if (termDTO.getGroup().getId() == userDTO.getGroup().getId()) {
				cancellationTermDTO = termDTO;
			}
		}
		if (cancellationTermDTO == null) {
			throw new ServiceException(ErrorCode.CANCELLATION_TERM_NOT_FOUND_BOOKING_NOT_ALLOW);
		}
		// get actual Cancellation terms
		cancellationTermDTO.setCancellationTerm(getCancellationTermDTOById(authDTO, cancellationTermDTO.getCancellationTerm()));

		return cancellationTermDTO.getCancellationTerm();
	}

	public CancellationTermDTO getCancellationTermsById(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		CancellationTermsDAO termsDAO = new CancellationTermsDAO();
		return termsDAO.getCancellationTerms(authDTO, cancellationTermDTO);
	}

	@Override
	public void getCancellationTermGroupIdByGroupKey(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		getCancellationTermGroupIdByGroupKeyCache(authDTO, cancellationTermDTO);
	}

}
