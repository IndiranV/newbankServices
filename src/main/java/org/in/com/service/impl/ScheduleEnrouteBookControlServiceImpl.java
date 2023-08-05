package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleEnrouteBookControlCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleEnrouteBookControlDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleEnrouteBookControlDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.enumeration.EnRouteTypeEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.ScheduleEnrouteBookControlService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleEnrouteBookControlServiceImpl extends CacheCentral implements ScheduleEnrouteBookControlService {
	private static String CACHEKEY = "SCH_EN_ROT_";
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	StationService stationService;

	@Override
	public List<ScheduleEnrouteBookControlDTO> getAll(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleDTO = scheduleCache.getScheduleDTO(authDTO, scheduleDTO);

		ScheduleEnrouteBookControlDAO scheduleEnrouteBookControlDAO = new ScheduleEnrouteBookControlDAO();
		List<ScheduleEnrouteBookControlDTO> list = scheduleEnrouteBookControlDAO.getAllScheduleEnrouteBookControl(authDTO, scheduleDTO);

		for (ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl : list) {
			for (StageDTO stageDTO : scheduleEnrouteBookControl.getStageList()) {
				if (stageDTO != null && stageDTO.getFromStation() != null && stageDTO.getToStation() != null) {
					stageDTO.getFromStation().setStation(stationService.getStation(stageDTO.getFromStation().getStation()));
					stageDTO.getToStation().setStation(stationService.getStation(stageDTO.getToStation().getStation()));
				}
			}
		}
		return list;
	}

	@Override
	public ScheduleEnrouteBookControlDTO Update(AuthDTO authDTO, ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl) {
		ScheduleEnrouteBookControlDAO scheduleEnrouteBookControlDAO = new ScheduleEnrouteBookControlDAO();
		for (StageDTO stageDTO : scheduleEnrouteBookControl.getStageList()) {
			StationDTO fromStationDTO = stageDTO.getFromStation().getStation();
			StationDTO toStationDTO = stageDTO.getToStation().getStation();

			stageDTO.getFromStation().setStation(getStationDTO(fromStationDTO));
			stageDTO.getToStation().setStation(getStationDTO(toStationDTO));
		}
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleEnrouteBookControl.setSchedule(scheduleCache.getScheduleDTO(authDTO, scheduleEnrouteBookControl.getSchedule()));
		scheduleEnrouteBookControlDAO.updateScheduleEnrouteBookControl(authDTO, scheduleEnrouteBookControl);

		EhcacheManager.getScheduleEhCache().remove(CACHEKEY + scheduleEnrouteBookControl.getSchedule().getCode());
		return scheduleEnrouteBookControl;
	}

	public List<ScheduleEnrouteBookControlDTO> getScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList = new ArrayList<ScheduleEnrouteBookControlDTO>();
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleEnrouteBookControlCacheDTO> scheduleEnrouteBookControlCacheList = (List<ScheduleEnrouteBookControlCacheDTO>) element.getObjectValue();
			scheduleEnrouteBookControlList = bindEnrouteBookControlFromCacheObject(scheduleEnrouteBookControlCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleEnrouteBookControlDAO scheduleEnrouteBookControlDAO = new ScheduleEnrouteBookControlDAO();
			scheduleEnrouteBookControlList = scheduleEnrouteBookControlDAO.getBySchedule(authDTO, scheduleDTO);
			List<ScheduleEnrouteBookControlCacheDTO> scheduleEnrouteBookControlCacheList = bindEnrouteBookControlToCacheObject(scheduleEnrouteBookControlList);
			element = new Element(key, scheduleEnrouteBookControlCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return scheduleEnrouteBookControlList;
	}

	@Override
	public void applyScheduleEnrouteBookControl(AuthDTO authDTO, TripDTO tripDTO) {
		ScheduleDTO scheduleDTO = tripDTO.getSchedule();
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList = new ArrayList<ScheduleEnrouteBookControlDTO>();
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleEnrouteBookControlCacheDTO> scheduleEnrouteBookControlCacheList = (List<ScheduleEnrouteBookControlCacheDTO>) element.getObjectValue();
			scheduleEnrouteBookControlList = bindEnrouteBookControlFromCacheObject(scheduleEnrouteBookControlCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleEnrouteBookControlDAO scheduleEnrouteBookControlDAO = new ScheduleEnrouteBookControlDAO();
			scheduleEnrouteBookControlList = scheduleEnrouteBookControlDAO.getBySchedule(authDTO, scheduleDTO);
			List<ScheduleEnrouteBookControlCacheDTO> scheduleEnrouteBookControlCacheList = bindEnrouteBookControlToCacheObject(scheduleEnrouteBookControlList);
			element = new Element(key, scheduleEnrouteBookControlCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		for (Iterator<ScheduleEnrouteBookControlDTO> iterator = scheduleEnrouteBookControlList.iterator(); iterator.hasNext();) {
			ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO = iterator.next();
			if (scheduleEnrouteBookControlDTO.getDayOfWeek() != null && scheduleEnrouteBookControlDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (scheduleEnrouteBookControlDTO.getDayOfWeek() != null && scheduleEnrouteBookControlDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (scheduleEnrouteBookControlDTO.getStageList() == null) {
				iterator.remove();
				continue;
			}
			StageDTO tripStageDTO = tripDTO.getStage();
			for (Iterator<StageDTO> stageIterator = scheduleEnrouteBookControlDTO.getStageList().iterator(); stageIterator.hasNext();) {
				StageDTO stageDTO = stageIterator.next();
				if (tripStageDTO.getFromStation().getStation().getId() != stageDTO.getFromStation().getStation().getId() || tripStageDTO.getToStation().getStation().getId() != stageDTO.getToStation().getStation().getId()) {
					stageIterator.remove();
					continue;
				}
			}
			if (scheduleEnrouteBookControlDTO.getStageList().isEmpty() || scheduleEnrouteBookControlDTO.getEnRouteType() == null || scheduleEnrouteBookControlDTO.getEnRouteType().getId() == Numeric.ZERO_INT) {
				iterator.remove();
				continue;
			}

			if (scheduleEnrouteBookControlDTO.getEnRouteType().getId() == EnRouteTypeEM.OPEN_ON_PREVIOUS_STAGE.getId()) {
				Map<String, TicketDetailsDTO> ticketDetailsMap = new HashMap<String, TicketDetailsDTO>();
				for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						boolean isExist = false;
						for (String relatedStageCode : tripDTO.getReleatedStageCodeList()) {
							if (ticketDetailsDTO.getTripStageCode().equals(relatedStageCode)) {
								isExist = true;
								break;
							}
						}
						if (!isExist) {
							ticketDetailsMap.put(ticketDetailsDTO.getSeatCode(), ticketDetailsDTO);
						}
					}
				}

				for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (ticketDetailsMap != null && ticketDetailsMap.get(seatLayoutDTO.getCode()) == null && (seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId())) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
					}
				}
			}
			else if (scheduleEnrouteBookControlDTO.getEnRouteType().getId() == EnRouteTypeEM.OPEN_ON_TRIP_TIME.getId()) {
				if (scheduleEnrouteBookControlDTO.getStageList() != null && !scheduleEnrouteBookControlDTO.getStageList().isEmpty()) {
					if (scheduleEnrouteBookControlDTO.getReleaseMinutes() != -1) {
						DateTime dateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), scheduleEnrouteBookControlDTO.getReleaseMinutes());
						if (dateTime.lteq(DateUtil.NOW())) {
							iterator.remove();
							continue;
						}
					}
					for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
						}
					}
				}
			}
		}
	}

	private List<ScheduleEnrouteBookControlDTO> bindEnrouteBookControlFromCacheObject(List<ScheduleEnrouteBookControlCacheDTO> scheduleEnrouteBookControlCacheList) {
		List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList = new ArrayList<ScheduleEnrouteBookControlDTO>();
		for (ScheduleEnrouteBookControlCacheDTO scheduleEnrouteBookControlCache : scheduleEnrouteBookControlCacheList) {
			ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO = new ScheduleEnrouteBookControlDTO();
			scheduleEnrouteBookControlDTO.setCode(scheduleEnrouteBookControlCache.getCode());
			scheduleEnrouteBookControlDTO.setReleaseMinutes(scheduleEnrouteBookControlCache.getReleaseMinutes());
			scheduleEnrouteBookControlDTO.setDayOfWeek(scheduleEnrouteBookControlCache.getDayOfWeek());
			scheduleEnrouteBookControlDTO.setEnRouteType(EnRouteTypeEM.getEnRouteTypeEM(scheduleEnrouteBookControlCache.getEnRouteTypeId()));
			scheduleEnrouteBookControlDTO.setActiveFlag(scheduleEnrouteBookControlCache.getActiveFlag());

			List<StageDTO> stageList = new ArrayList<StageDTO>();
			for (StageDTO stageDTO : scheduleEnrouteBookControlCache.getStageList()) {
				if (stageDTO != null && stageDTO.getFromStation() != null && stageDTO.getToStation() != null) {
					stageDTO.getFromStation().setStation(stationService.getStation(stageDTO.getFromStation().getStation()));
					stageDTO.getToStation().setStation(stationService.getStation(stageDTO.getToStation().getStation()));
					stageList.add(stageDTO);
				}
			}
			scheduleEnrouteBookControlDTO.setStageList(stageList);
			scheduleEnrouteBookControlList.add(scheduleEnrouteBookControlDTO);
		}
		return scheduleEnrouteBookControlList;
	}

	private List<ScheduleEnrouteBookControlCacheDTO> bindEnrouteBookControlToCacheObject(List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList) {
		List<ScheduleEnrouteBookControlCacheDTO> scheduleEnrouteBookControlCacheList = new ArrayList<ScheduleEnrouteBookControlCacheDTO>();
		for (ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO : scheduleEnrouteBookControlList) {
			ScheduleEnrouteBookControlCacheDTO scheduleEnrouteBookControlCache = new ScheduleEnrouteBookControlCacheDTO();
			scheduleEnrouteBookControlCache.setCode(scheduleEnrouteBookControlDTO.getCode());
			scheduleEnrouteBookControlCache.setReleaseMinutes(scheduleEnrouteBookControlDTO.getReleaseMinutes());
			scheduleEnrouteBookControlCache.setDayOfWeek(scheduleEnrouteBookControlDTO.getDayOfWeek());
			scheduleEnrouteBookControlCache.setEnRouteTypeId(scheduleEnrouteBookControlDTO.getEnRouteType().getId());
			scheduleEnrouteBookControlCache.setActiveFlag(scheduleEnrouteBookControlDTO.getActiveFlag());

			List<StageDTO> stageList = new ArrayList<StageDTO>();
			for (StageDTO stageDTO : scheduleEnrouteBookControlDTO.getStageList()) {
				if (stageDTO != null && stageDTO.getFromStation() != null && stageDTO.getToStation() != null) {
					stageDTO.getFromStation().setStation(stationService.getStation(stageDTO.getFromStation().getStation()));
					stageDTO.getToStation().setStation(stationService.getStation(stageDTO.getToStation().getStation()));
					stageList.add(stageDTO);
				}
			}
			scheduleEnrouteBookControlCache.setStageList(stageList);
			scheduleEnrouteBookControlCacheList.add(scheduleEnrouteBookControlCache);
		}
		return scheduleEnrouteBookControlCacheList;
	}

	@Override
	public ScheduleEnrouteBookControlDTO getScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl) {
		ScheduleEnrouteBookControlDAO scheduleEnrouteBookControlDAO = new ScheduleEnrouteBookControlDAO();
		scheduleEnrouteBookControlDAO.getScheduleEnrouteBookControl(authDTO, scheduleEnrouteBookControl);

		for (StageDTO stageDTO : scheduleEnrouteBookControl.getStageList()) {
			if (stageDTO != null && stageDTO.getFromStation() != null && stageDTO.getToStation() != null) {
				stageDTO.getFromStation().setStation(stationService.getStation(stageDTO.getFromStation().getStation()));
				stageDTO.getToStation().setStation(stationService.getStation(stageDTO.getToStation().getStation()));
			}
		}

		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleEnrouteBookControl.setSchedule(scheduleCache.getScheduleDTO(authDTO, scheduleEnrouteBookControl.getSchedule()));
		return scheduleEnrouteBookControl;
	}

	@Override
	public void applyScheduleEnrouteBookControl(AuthDTO authDTO, TripDTO trip, List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList, List<TicketDetailsDTO> ticketDetails, List<TripSeatQuotaDTO> tripSeatQuatoList) {

		for (ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO : scheduleEnrouteBookControlList) {
			if (scheduleEnrouteBookControlDTO.getDayOfWeek() != null && scheduleEnrouteBookControlDTO.getDayOfWeek().length() != 7) {
				continue;
			}
			if (scheduleEnrouteBookControlDTO.getDayOfWeek() != null && scheduleEnrouteBookControlDTO.getDayOfWeek().substring(trip.getTripDate().getWeekDay() - 1, trip.getTripDate().getWeekDay()).equals("0")) {
				continue;
			}
			if (scheduleEnrouteBookControlDTO.getStageList() == null) {
				continue;
			}
			StageDTO tripStageDTO = trip.getStage();
			for (Iterator<StageDTO> stageIterator = scheduleEnrouteBookControlDTO.getStageList().iterator(); stageIterator.hasNext();) {
				StageDTO stageDTO = stageIterator.next();
				if (tripStageDTO.getFromStation().getStation().getId() != stageDTO.getFromStation().getStation().getId() || tripStageDTO.getToStation().getStation().getId() != stageDTO.getToStation().getStation().getId()) {
					continue;
				}
			}
			if (scheduleEnrouteBookControlDTO.getStageList().isEmpty() || scheduleEnrouteBookControlDTO.getEnRouteType() == null || scheduleEnrouteBookControlDTO.getEnRouteType().getId() == Numeric.ZERO_INT) {
				continue;
			}

			if (scheduleEnrouteBookControlDTO.getEnRouteType().getId() == EnRouteTypeEM.OPEN_ON_PREVIOUS_STAGE.getId()) {
				Map<String, TicketDetailsDTO> ticketDetailsMap = new HashMap<String, TicketDetailsDTO>();
				for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						boolean isExist = false;
						for (String relatedStageCode : trip.getReleatedStageCodeList()) {
							if (ticketDetailsDTO.getTripStageCode().equals(relatedStageCode)) {
								isExist = true;
								break;
							}
						}
						if (!isExist) {
							ticketDetailsMap.put(ticketDetailsDTO.getSeatCode(), ticketDetailsDTO);
						}
					}
				}

				for (BusSeatLayoutDTO seatLayoutDTO : trip.getBus().getBusSeatLayoutDTO().getList()) {
					if (ticketDetailsMap != null && ticketDetailsMap.get(seatLayoutDTO.getCode()) == null && (seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId())) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
					}
				}
			}
			else if (scheduleEnrouteBookControlDTO.getEnRouteType().getId() == EnRouteTypeEM.OPEN_ON_TRIP_TIME.getId()) {
				if (scheduleEnrouteBookControlDTO.getStageList() != null && !scheduleEnrouteBookControlDTO.getStageList().isEmpty()) {
					if (scheduleEnrouteBookControlDTO.getReleaseMinutes() != -1) {
						DateTime dateTime = DateUtil.addMinituesToDate(trip.getTripDate(), scheduleEnrouteBookControlDTO.getReleaseMinutes());
						if (dateTime.lteq(DateUtil.NOW())) {
							continue;
						}
					}
					for (BusSeatLayoutDTO seatLayoutDTO : trip.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
						}
					}
				}
			}
		}

	}

}
