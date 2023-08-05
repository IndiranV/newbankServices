package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleStationPointDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.service.BusVehicleVanPickupService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleAuditLogService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TripVanInfoService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class ScheduleStationPointImpl extends BaseImpl implements ScheduleStationPointService {
	@Autowired
	BusVehicleVanPickupService vanRouteService;
	@Autowired
	ScheduleActivityService scheduleActivityService;
	@Autowired
	StationService stationService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	ScheduleAuditLogService scheduleAuditLogService;
	@Autowired
	TripVanInfoService tripVanInfoService;

	public List<ScheduleStationPointDTO> get(AuthDTO authDTO, ScheduleStationPointDTO dto) {
		ScheduleStationPointDAO dao = new ScheduleStationPointDAO();
		List<ScheduleStationPointDTO> list = dao.get(authDTO, dto);
		for (ScheduleStationPointDTO stationDTO : list) {
			stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
			if (stationDTO.getBusVehicleVanPickup() != null && stationDTO.getBusVehicleVanPickup().getId() > 0) {
				stationDTO.setBusVehicleVanPickup(vanRouteService.getBusVehicleVanPickup(authDTO, stationDTO.getBusVehicleVanPickup()));
			}
			for (ScheduleStationPointDTO overrideStationDTO : stationDTO.getOverrideList()) {
				overrideStationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
			}
		}
		return list;
	}

	public ScheduleStationPointDTO Update(AuthDTO authDTO, ScheduleStationPointDTO dto) {
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleStationPointDAO dao = new ScheduleStationPointDAO();
		for (ScheduleStationPointDTO stationDTO : dto.getList()) {
			stationDTO.setStation(stationService.getStation(stationDTO.getStation()));

			// Activity Activity Log
			scheduleActivityService.scheduleStationPointActivity(authDTO, stationDTO);

			if (StringUtil.isNotNull(stationDTO.getCode())) {
				scheduleAuditLogService.updateScheduleStationPointAudit(authDTO, stationDTO);
			}
			scheduleCache.removeScheduleDTO(authDTO, stationDTO.getSchedule());
		}
		dao.getIUD(authDTO, dto);
		return dto;
	}

	@Override
	public boolean CheckStationPointUsed(AuthDTO authDTO, StationPointDTO dto) {
		ScheduleStationPointDAO dao = new ScheduleStationPointDAO();
		return dao.CheckStationPointUsed(authDTO, dto);
	}

	public List<ScheduleStationPointDTO> getScheduleStationPoint(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCache scheduleCache = new ScheduleCache();
		List<ScheduleStationPointDTO> scheduleStationPointDTOList = scheduleCache.getScheduleStationPoint(authDTO, scheduleDTO);
		if (scheduleStationPointDTOList == null) {
			ScheduleStationPointDAO scheduleStationPointDAO = new ScheduleStationPointDAO();
			scheduleStationPointDTOList = scheduleStationPointDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleCache.putScheduleStationPoint(authDTO, scheduleDTO, scheduleStationPointDTOList);
		}
		return scheduleStationPointDTOList;

	}

	@Override
	public List<ScheduleStationPointDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		List<ScheduleStationPointDTO> stationPointDTOList = getScheduleStationPoint(authDTO, scheduleDTO);

		// Validate all stations Point
		for (Iterator<ScheduleStationPointDTO> iterator = stationPointDTOList.iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO stationPointDTO = iterator.next();
			// common validations
			if (stationPointDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(stationPointDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(stationPointDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getDayOfWeek() != null && stationPointDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getDayOfWeek() != null && stationPointDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Exception and Override
			for (Iterator<ScheduleStationPointDTO> OverrideIterator = stationPointDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
				ScheduleStationPointDTO overrideScheduleStationPonitDTO = OverrideIterator.next();
				// common validations
				if (overrideScheduleStationPonitDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(overrideScheduleStationPonitDTO.getActiveFrom()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(overrideScheduleStationPonitDTO.getActiveTo()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getDayOfWeek() != null && overrideScheduleStationPonitDTO.getDayOfWeek().length() != 7) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getDayOfWeek() != null && overrideScheduleStationPonitDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					OverrideIterator.remove();
					continue;
				}
				// Remove if Exceptions
				if (overrideScheduleStationPonitDTO.getMinitues() == -1) {
					// iterator.remove();
					stationPointDTO.setActiveFlag(0);
					break;
				}
				// Override
				stationPointDTO.setMinitues(stationPointDTO.getMinitues() + overrideScheduleStationPonitDTO.getMinitues());
				stationPointDTO.setCreditDebitFlag(overrideScheduleStationPonitDTO.getCreditDebitFlag());
			}
		}
		return stationPointDTOList;
	}

	@Override
	public void updateScheduleStationPointException(AuthDTO authDTO, ScheduleStationPointDTO stationPointDTO) {
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleStationPointDAO dao = new ScheduleStationPointDAO();
		stationPointDTO.setStation(stationService.getStation(stationPointDTO.getStation()));
		dao.updateScheduleStationPointException(authDTO, stationPointDTO);
		scheduleCache.removeScheduleStationPointException(authDTO);
	}

	public List<ScheduleStationPointDTO> getScheduleStationPointException(AuthDTO authDTO) {
		ScheduleStationPointDAO stationPointDAO = new ScheduleStationPointDAO();
		List<ScheduleStationPointDTO> stationList = stationPointDAO.getScheduleStationPointException(authDTO);
		for (ScheduleStationPointDTO stationPointDTO : stationList) {
			List<StationPointDTO> pointList = new ArrayList<StationPointDTO>();
			stationPointDTO.setStation(stationService.getStation(stationPointDTO.getStation()));
			for (String code : stationPointDTO.getStationPoint().getCode().split(",")) {
				if (StringUtil.isNull(code)) {
					continue;
				}
				StationPointDTO pointDTO = new StationPointDTO();
				pointDTO.setCode(code);
				stationPointService.getStationPoint(authDTO, pointDTO);
				if (!pointDTO.isActive()) {
					continue;
				}
				pointList.add(pointDTO);
			}
			stationPointDTO.setStationPointList(pointList);

			List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
			if (stationPointDTO.getSchedule() != null && StringUtil.isNotNull(stationPointDTO.getSchedule().getCode())) {
				ScheduleCache scheduleCache = new ScheduleCache();
				for (String scheduleCode : stationPointDTO.getSchedule().getCode().split(",")) {
					if (StringUtil.isNull(scheduleCode)) {
						continue;
					}
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(scheduleCode);
					scheduleCache.getScheduleDTO(authDTO, scheduleDTO);
					scheduleList.add(scheduleDTO);
				}
			}
			stationPointDTO.setScheduleList(scheduleList);
		}
		return stationList;
	}

	@Override
	public List<ScheduleStationPointDTO> getActiveScheduleStationPointList(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, Map<Integer, ScheduleStationDTO> stationMap) {
		ScheduleCache scheduleCache = new ScheduleCache();
		List<ScheduleStationPointDTO> stationPointDTOList = getScheduleStationPoint(authDTO, scheduleDTO);
		List<ScheduleStationPointDTO> stationPointExceptionList = scheduleCache.getScheduleStationPointException(authDTO);
		if (stationPointExceptionList == null) {
			System.out.println("Null Pointer error: stationPointExceptionList");
		}
		DateTime tripDate = scheduleDTO.getTripDate();
		DateTime now = DateUtil.NOW();
		String tripdate = DateUtil.convertDate(tripDate);

		// Validate all stations Point exception
		for (Iterator<ScheduleStationPointDTO> iterator = stationPointExceptionList.iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO exceptionPointDTO = iterator.next();
			ScheduleStationDTO scheduleStation = stationMap.get(exceptionPointDTO.getStation().getId());
			if (scheduleStation == null || StringUtil.isNull(scheduleStation.getCode())) {
				iterator.remove();
				continue;
			}

			if (exceptionPointDTO.getBoardingFlag() == 1 && exceptionPointDTO.getDroppingFlag() == 1 && searchDTO.getFromStation().getId() != exceptionPointDTO.getStation().getId() && searchDTO.getToStation().getId() != exceptionPointDTO.getStation().getId()) {
				iterator.remove();
				continue;
			}
			else if (exceptionPointDTO.getBoardingFlag() == 1 && exceptionPointDTO.getDroppingFlag() == 0 && searchDTO.getFromStation().getId() != exceptionPointDTO.getStation().getId()) {
				iterator.remove();
				continue;
			}
			else if (exceptionPointDTO.getBoardingFlag() == 0 && exceptionPointDTO.getDroppingFlag() == 1 && searchDTO.getToStation().getId() != exceptionPointDTO.getStation().getId()) {
				iterator.remove();
				continue;
			}

			if (StringUtil.isNotNull(exceptionPointDTO.getActiveFrom()) && !tripDate.gteq(new DateTime(exceptionPointDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (StringUtil.isNotNull(exceptionPointDTO.getActiveTo()) && !tripDate.lteq(new DateTime(exceptionPointDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (StringUtil.isNotNull(exceptionPointDTO.getActiveFrom()) && StringUtil.isNotNull(exceptionPointDTO.getActiveTo()) && exceptionPointDTO.getDayOfWeek() != null && exceptionPointDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (StringUtil.isNotNull(exceptionPointDTO.getActiveFrom()) && StringUtil.isNotNull(exceptionPointDTO.getActiveTo()) && exceptionPointDTO.getDayOfWeek() != null && exceptionPointDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (!exceptionPointDTO.getTripDates().isEmpty() && !exceptionPointDTO.getTripDates().contains(tripdate)) {
				iterator.remove();
				continue;
			}

			if (exceptionPointDTO.getSchedule() != null && StringUtil.isNotNull(exceptionPointDTO.getSchedule().getCode()) && !exceptionPointDTO.getSchedule().getCode().equals("NA") && !exceptionPointDTO.getSchedule().getCode().contains(scheduleDTO.getCode())) {
				iterator.remove();
				continue;
			}
			DateTime tripDateTime = DateUtil.addMinituesToDate(tripDate, scheduleStation.getMinitues());
			Integer minutes = DateUtil.getMinutiesDifferent(now, tripDateTime);
			if (exceptionPointDTO.getReleaseMinutes() > 0 && exceptionPointDTO.getReleaseMinutes() >= minutes) {
				iterator.remove();
				continue;
			}
		}
		// Validate all stations Point
		for (Iterator<ScheduleStationPointDTO> iterator = stationPointDTOList.iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO stationPointDTO = iterator.next();

			if (stationMap.get(stationPointDTO.getStation().getId()) == null) {
				iterator.remove();
				continue;
			}
			// Apply Extra Schedule Station Point Exception
			if (!stationPointExceptionList.isEmpty()) {
				String stationPointCode = stationPointService.getStationPoint(authDTO, stationPointDTO.getStationPoint()).getCode();
				for (ScheduleStationPointDTO exception : stationPointExceptionList) {
					// Regular station point exception validation
					if (exception.getStationPointType().equals("REG") && StringUtil.isNotNull(stationPointCode) && StringUtil.isNotNull(exception.getStationPoint().getCode()) && exception.getStationPoint().getCode().contains(stationPointCode)) {
						stationPointDTO.setActiveFlag(Numeric.ZERO_INT);
						break;
					}
					// Van pickup/drop station point exception validation
					else if (exception.getStationPointType().equals("VAN") && stationPointDTO.getVanRouteEnabledFlag() == Numeric.ONE_INT && StringUtil.isNotNull(stationPointCode) && StringUtil.isNotNull(exception.getStationPoint().getCode()) && exception.getStationPoint().getCode().contains(stationPointCode)) {
						stationPointDTO.setActiveFlag(Numeric.ZERO_INT);
						break;
					}
                    // Override Station Point Name Exception validation
					else if (exception.getStationPointType().equals("ONM") && stationPointDTO.getVanRouteEnabledFlag() == Numeric.ONE_INT && StringUtil.isNotNull(stationPointCode) && StringUtil.isNotNull(exception.getStationPoint().getCode()) && exception.getStationPoint().getCode().contains(stationPointCode)) {
						stationPointDTO.setBusVehicleVanPickup(new BusVehicleVanPickupDTO());
					}
				}
			}

			// Van Pickup Exception validation
			if (stationPointDTO.getActiveFlag() == Numeric.ONE_INT && stationPointDTO.getBusVehicleVanPickup() != null && stationPointDTO.getBusVehicleVanPickup().getId() != 0) {
				TripVanInfoDTO tripVanInfoDTO = new TripVanInfoDTO();
				tripVanInfoDTO.setTripDate(tripDate);
				tripVanInfoDTO.setVanPickup(stationPointDTO.getBusVehicleVanPickup());
				tripVanInfoDTO = tripVanInfoService.getTripVanInfoV2(authDTO, tripVanInfoDTO);

				if (tripVanInfoDTO != null && tripVanInfoDTO.getTripVanException() != null && StringUtil.isNotNull(tripVanInfoDTO.getTripVanException().getCode())) {
					if (BitsUtil.isScheduleExists(tripVanInfoDTO.getTripVanException().getSchedules(), scheduleDTO.getCode()) != null) {
						stationPointDTO.setActiveFlag(Numeric.ZERO_INT);
					}
				}
			}
			if (!stationPointDTO.isActive()) {
				iterator.remove();
				continue;
			}
			// common validations
			if (stationPointDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(stationPointDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(stationPointDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getDayOfWeek() != null && stationPointDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getDayOfWeek() != null && stationPointDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if ((stationPointDTO.getStation().getId() == searchDTO.getFromStation().getId() && stationPointDTO.getBoardingFlag() != 1) || (stationPointDTO.getStation().getId() == searchDTO.getToStation().getId() && stationPointDTO.getDroppingFlag() != 1)) {
				iterator.remove();
				continue;
			}
			// Exception and Override
			for (Iterator<ScheduleStationPointDTO> OverrideIterator = stationPointDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
				ScheduleStationPointDTO overrideScheduleStationPonitDTO = OverrideIterator.next();
				// common validations
				if (overrideScheduleStationPonitDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(overrideScheduleStationPonitDTO.getActiveFrom()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(overrideScheduleStationPonitDTO.getActiveTo()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getDayOfWeek() != null && overrideScheduleStationPonitDTO.getDayOfWeek().length() != 7) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getDayOfWeek() != null && overrideScheduleStationPonitDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					OverrideIterator.remove();
					continue;
				}
				// Remove if Exceptions
				if (overrideScheduleStationPonitDTO.getMinitues() == -1) {
					iterator.remove();
					break;
				}
				// Override
				stationPointDTO.setMinitues(stationPointDTO.getMinitues() + overrideScheduleStationPonitDTO.getMinitues());
				stationPointDTO.setCreditDebitFlag(overrideScheduleStationPonitDTO.getCreditDebitFlag());
			}
		}
		return stationPointDTOList;
	}

	@Override
	public List<BusVehicleVanPickupDTO> getVanPickupStationPoints(AuthDTO authDTO, StationDTO stationDTO) {
		stationService.getStation(stationDTO);
		ScheduleStationPointDAO dao = new ScheduleStationPointDAO();
		List<BusVehicleVanPickupDTO> list = dao.getVanPickupStationPoint(authDTO, stationDTO);

		Map<String, List<StationPointDTO>> stationPointMap = new HashMap<String, List<StationPointDTO>>();
		Map<String, BusVehicleVanPickupDTO> vanPickupMap = new HashMap<String, BusVehicleVanPickupDTO>();
		for (BusVehicleVanPickupDTO vanPickupDTO : list) {

			List<StationPointDTO> stationPointList = new ArrayList<>();
			stationPointList = vanPickupDTO.getStation().getStationPoints();
			if (stationPointMap.get(vanPickupDTO.getCode()) != null) {
				List<StationPointDTO> repoStationPointList = stationPointMap.get(vanPickupDTO.getCode());
				stationPointList.addAll(repoStationPointList);
			}
			stationPointMap.put(vanPickupDTO.getCode(), stationPointList);

			if (vanPickupMap.isEmpty() || vanPickupMap.get(vanPickupDTO.getCode()) == null) {
				vanPickupMap.put(vanPickupDTO.getCode(), vanPickupDTO);
			}
		}

		List<BusVehicleVanPickupDTO> finalVanPickupPoints = new ArrayList<>();
		for (Map.Entry<String, List<StationPointDTO>> stationPointDataMap : stationPointMap.entrySet()) {
			String key = stationPointDataMap.getKey();
			List<StationPointDTO> stationPoints = stationPointDataMap.getValue();

			@SuppressWarnings("unchecked")
			Comparator<StationPointDTO> comp = new BeanComparator("name");
			Collections.sort(stationPoints, comp);

			BusVehicleVanPickupDTO vanPickupDTO = vanPickupMap.get(key);
			vanPickupDTO.getStation().setStationPoints(stationPoints);
			finalVanPickupPoints.add(vanPickupDTO);
		}
		return finalVanPickupPoints;
	}
}
