package org.in.com.service.impl;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.orbit.OrbitService;
import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.AmentiesDAO;
import org.in.com.dao.CalendarAnnouncementDAO;
import org.in.com.dao.FareRuleDAO;
import org.in.com.dao.MenuDAO;
import org.in.com.dao.ReportQueryDAO;
import org.in.com.dao.StationDAO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AmentiesService;
import org.in.com.service.MenuService;
import org.in.com.service.StateService;
import org.in.com.service.StationService;
import org.in.com.service.ZoneSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

@Service
public class ZoneSyncImpl implements ZoneSyncService {
	@Autowired
	BitsService bitsService;
	@Autowired
	MenuService menuService;
	@Autowired
	StationService stationService;
	@Autowired
	StateService stateService;
	@Autowired
	AmentiesService amentiesService;
	@Autowired
	OrbitService orbitService;

	@Override
	public List<AmenitiesDTO> zoneSyncAmenities(AuthDTO authDTO, String bitsAuthtoken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		AmentiesDAO amentiesDAO = new AmentiesDAO();
		String syncDate = amentiesDAO.getZoneSyncDate(authDTO);
		List<AmenitiesDTO> list = bitsService.getZoneSyncAmenties(authDTO, bitsAuthtoken, syncDate);
		amentiesDAO.updateZoneSync(authDTO, list);
		amentiesService.reloadAmenties();

		return list;
	}

	@Override
	public List<StationDTO> zoneSyncStation(AuthDTO authDTO, String bitsAuthtoken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		StationDAO stationDAO = new StationDAO();
		String syncDate = stationDAO.getZoneSyncDate(authDTO);
		List<StationDTO> list = bitsService.getZoneSyncStation(authDTO, bitsAuthtoken, syncDate);
		stationDAO.updateZoneSync(authDTO, list);
		for (StationDTO stationDTO : list) {
			EhcacheManager.getStationEhCache().remove(stationDTO.getCode());
		}
		return list;
	}

	@Override
	public List<MenuDTO> zoneSyncMenu(AuthDTO authDTO, String bitsAuthtoken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		MenuDAO menuDAO = new MenuDAO();
		String syncDate = menuDAO.getMenuZoneSyncDate(authDTO);
		List<MenuDTO> list = bitsService.getZoneSyncMenu(authDTO, bitsAuthtoken, syncDate);
		menuDAO.updateMenuZoneSync(authDTO, list);
		for (MenuDTO menuDTO : list) {
			menuDAO.updateMenuEventZoneSync(authDTO, menuDTO, menuDTO.getMenuEvent().getList());
		}
		menuService.reload();
		return list;
	}

	@Override
	public List<ReportQueryDTO> zoneSyncReportQuery(AuthDTO authDTO, String bitsAuthtoken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		String syncDate = queryDAO.getZoneSyncDate(authDTO);
		List<ReportQueryDTO> list = bitsService.getZoneSyncReportQuery(authDTO, bitsAuthtoken, syncDate);
		queryDAO.updateZoneSync(authDTO, list);
		return list;
	}

	@Override
	public List<FareRuleDetailsDTO> zoneSyncFareRuleDetails(AuthDTO authDTO, String bitsAuthtoken, FareRuleDTO fareRule) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		fareRuleDAO.getFareRule(authDTO, fareRule);
		if (fareRule.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Fare rule code");
		}

		String syncDate = fareRuleDAO.getZoneSyncDate(authDTO, fareRule);
		List<FareRuleDetailsDTO> farelist = bitsService.getZoneSyncFareRuleDetails(authDTO, bitsAuthtoken, fareRule.getCode(), syncDate);
		for (FareRuleDetailsDTO fareRuleDetailsDTO : farelist) {
			// get from station
			fareRuleDetailsDTO.setFromStation(stationService.getStation(fareRuleDetailsDTO.getFromStation()));
			// get to station
			fareRuleDetailsDTO.setToStation(stationService.getStation(fareRuleDetailsDTO.getToStation()));
		}
		authDTO.getAdditionalAttribute().put(Text.FARE_RULE_SYNC_FLAG, Numeric.ONE);
		Iterable<List<FareRuleDetailsDTO>> batchFareRules = Iterables.partition(farelist, 100);
		for (List<FareRuleDetailsDTO> list : batchFareRules) {
			fareRule.setFareRuleDetails(list);
			fareRuleDAO.updateFareRuleDetails(authDTO, fareRule);
		}
		return farelist;
	}

	@Override
	public List<StationOtaPartnerDTO> zoneSyncStationOtaPartner(AuthDTO authDTO, String bitsAuthtoken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		StationDAO stationDAO = new StationDAO();
		String syncDate = stationDAO.getStationOtaZoneSyncDate(authDTO);
		List<StationOtaPartnerDTO> list = bitsService.getZoneSyncStationOtaPartner(authDTO, bitsAuthtoken, syncDate);

		Iterable<List<StationOtaPartnerDTO>> stationOtaPartners = Iterables.partition(list, 500);
		for (List<StationOtaPartnerDTO> otaPartnerList : stationOtaPartners) {
			for (StationOtaPartnerDTO stationOtaPartner : otaPartnerList) {
				stationOtaPartner.setState(stateService.getState(stationOtaPartner.getState()));
				for (StationDTO station : stationOtaPartner.getStations()) {
					try {
						stationService.getStation(station);
					}
					catch (Exception e) {
						System.out.println("SYNC OTA STATION ERROR-" + station.getCode());
					}
				}
			}
			stationDAO.updateStationOtaZoneSync(authDTO, otaPartnerList);
		}
		return list;
	}

	@Override
	public List<StationAreaDTO> zoneSyncStationArea(AuthDTO authDTO, String bitsAuthtoken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		StationDAO stationDAO = new StationDAO();
		String syncDate = stationDAO.getStationAreaZoneSyncDate(authDTO);
		List<StationAreaDTO> list = bitsService.getZoneSyncStationArea(authDTO, bitsAuthtoken, syncDate);

		Iterable<List<StationAreaDTO>> batchStationAreaList = Iterables.partition(list, 100);
		for (List<StationAreaDTO> stationAreaList : batchStationAreaList) {
			stationDAO.updateStationAreaZoneSync(authDTO, stationAreaList);
		}
		return list;
	}

	@Override
	public List<StationAreaDTO> syncStationArea(AuthDTO authDTO) {
		StationDAO stationDAO = new StationDAO();
		String syncDate = stationDAO.getStationAreaZoneSyncDate(authDTO);
		List<StationAreaDTO> list = orbitService.syncStationArea(authDTO, syncDate);

		Iterable<List<StationAreaDTO>> batchStationAreaList = Iterables.partition(list, 100);
		for (List<StationAreaDTO> stationAreaList : batchStationAreaList) {
			stationDAO.updateStationAreaZoneSync(authDTO, stationAreaList);
		}
		return list;
	}

	@Override
	public List<CalendarAnnouncementDTO> zoneSyncCalendarAnnouncement(AuthDTO authDTO, String bitsAccessToken) {
		if (ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		CalendarAnnouncementDAO calendarAnnouncementDAO = new CalendarAnnouncementDAO();
		String syncDate = calendarAnnouncementDAO.getZoneSyncDate(authDTO);
		List<CalendarAnnouncementDTO> list = bitsService.getZoneSyncCalendarAnnouncement(authDTO, bitsAccessToken, syncDate);
		
		Iterable<List<CalendarAnnouncementDTO>> batchCalendarAnnouncementList = Iterables.partition(list, 100);
		for (List<CalendarAnnouncementDTO> calendarAnnouncementList : batchCalendarAnnouncementList) {
			for (CalendarAnnouncementDTO calendarAnnouncementDTO : calendarAnnouncementList) {
				for (StateDTO stateDTO : calendarAnnouncementDTO.getStates()) {
					StateDTO state = stateService.getState(stateDTO);
					stateDTO.setId(state.getId());
				}
			}
			calendarAnnouncementDAO.updateZoneSync(authDTO, calendarAnnouncementList);
		}
		return list;
	}
}
