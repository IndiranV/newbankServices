package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.dp.DynamicPricingFactoryService;
import org.in.com.aggregator.mercservices.MercService;
import org.in.com.aggregator.redbus.DynamicFareService;
import org.in.com.aggregator.redbus.DynamicFareServiceImpl;
import org.in.com.aggregator.redbus.RedbusCommunicator;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.dto.ScheduleDynamicStageFareCacheDTO;
import org.in.com.cache.dto.ScheduleDynamicStageFareDetailsCacheDTO;
import org.in.com.cache.redis.RedisTripCacheService;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleDynamicStageFareDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuditService;
import org.in.com.service.BusService;
import org.in.com.service.IntegrationService;
import org.in.com.service.ReportQueryService;
import org.in.com.service.ScheduleControlService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ScheduleDynamicStageFareImpl extends CacheCentral implements ScheduleDynamicStageFareService {
	private static final String CACHEKEY = "DYC_";
	private static final String QUERY = "CALL EZEE_SP_RPT_BOOKED_TICKET_TRANSACTION(:namespaceId, :fromDate, :toDate, :scheduleCode)";
	private static final Logger DYNAMIC_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.dynamic.pricing");

	@Autowired
	DynamicFareService dynamicFareService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleControlService scheduleControlService;
	@Autowired
	StationService stationService;
	@Autowired
	ReportQueryService reportService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStageService scheduleStageService;
	@Autowired
	TripService tripService;
	@Autowired
	BusService busService;
	@Autowired
	RedisTripCacheService redisTripCacheService;
	@Autowired
	AuditService auditService;
	@Autowired
	IntegrationService integration;
	@Autowired
	DynamicPricingFactoryService dpFactoryService;
	@Autowired
	ScheduleDynamicStageFareDAO scheduleDynamicStageFareRepo;
	@Autowired
	MercService mercService;

	@Override
	public ScheduleDynamicStageFareDTO updateScheduleDynamicStageFareDetails(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare) {
		ScheduleDynamicStageFareDAO stageFareDAO = new ScheduleDynamicStageFareDAO();

		/** Add / Update / Remove Dynamic Fare */
		ScheduleDynamicStageFareDTO scheduleDynamicStageFareDTO = stageFareDAO.updateScheduleDynamicStageFare(authDTO, scheduleSeatFare);
		scheduleDynamicStageFareDTO.setStageFare(scheduleSeatFare.getStageFare());

		/** Add / Update / Remove Dynamic Minimum & Maximum Fare */
		stageFareDAO.updateScheduleDynamicStageFareDetails(authDTO, scheduleDynamicStageFareDTO);

		/** Activate / Add Via Route / Remove Via Route Dynamic Fare */
		if (BitsUtil.getDynamicPriceProvider(authDTO.getNamespace().getProfile().getDynamicPriceProviders(), DynamicPriceProviderEM.REDBUS) != null && scheduleSeatFare.getDynamicPriceProvider().getId() == DynamicPriceProviderEM.REDBUS.getId()) {
			dynamicFareProcess(authDTO, scheduleSeatFare, scheduleDynamicStageFareDTO);
		}
		/** Reset Schedule Cache */
		EhcacheManager.getScheduleEhCache().remove(CACHEKEY + scheduleSeatFare.getSchedule().getCode());
		return scheduleDynamicStageFareDTO;
	}

	@Override
	public void addScheduleDynamicPriceException(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare, TripDTO trip) {
		ScheduleDynamicStageFareDAO scheduleDynamicStageFareDAO = new ScheduleDynamicStageFareDAO();
		List<ScheduleDynamicStageFareDTO> scheduleDynamicStageFares = scheduleDynamicStageFareDAO.getScheduleSeatFareByScheduleId(authDTO, scheduleSeatFare.getSchedule());

		DateTime dateTime = DateUtil.getDateTime(scheduleSeatFare.getActiveFrom()).getStartOfDay();
		ScheduleDynamicStageFareDTO dynamicStageFareExceptionDTO = null;
		for (Iterator<ScheduleDynamicStageFareDTO> iterator = scheduleDynamicStageFares.iterator(); iterator.hasNext();) {
			ScheduleDynamicStageFareDTO dynamicStageFareDTO = iterator.next();

			if (StringUtil.isNull(dynamicStageFareDTO.getActiveFrom()) || StringUtil.isNull(dynamicStageFareDTO.getActiveTo()) || StringUtil.isNull(dynamicStageFareDTO.getDayOfWeek())) {
				iterator.remove();
				continue;
			}
			for (Iterator<ScheduleDynamicStageFareDTO> OverrideIterator = dynamicStageFareDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
				ScheduleDynamicStageFareDTO overrideStationDTO = OverrideIterator.next();

				if (DateUtil.getDateTime(overrideStationDTO.getActiveFrom()).getStartOfDay().compareTo(dateTime) == 0) {
					dynamicStageFareExceptionDTO = overrideStationDTO;
				}
			}
			/** Add Exception */
			if (dynamicStageFareExceptionDTO != null) {
				dynamicStageFareExceptionDTO.setStatus(scheduleSeatFare.getStatus());
				dynamicStageFareExceptionDTO.setActiveFlag(scheduleSeatFare.getActiveFlag());
				dynamicStageFareExceptionDTO.setSchedule(scheduleSeatFare.getSchedule());
			}
			else if (dynamicStageFareExceptionDTO == null) {
				dynamicStageFareExceptionDTO = new ScheduleDynamicStageFareDTO();
				dynamicStageFareExceptionDTO.setActiveFrom(scheduleSeatFare.getActiveFrom());
				dynamicStageFareExceptionDTO.setActiveTo(scheduleSeatFare.getActiveTo());
				dynamicStageFareExceptionDTO.setDayOfWeek(scheduleSeatFare.getDayOfWeek());
				dynamicStageFareExceptionDTO.setStatus(scheduleSeatFare.getActiveFlag());
				dynamicStageFareExceptionDTO.setActiveFlag(scheduleSeatFare.getActiveFlag());
				dynamicStageFareExceptionDTO.setLookupCode(dynamicStageFareDTO.getCode());
				dynamicStageFareExceptionDTO.setSchedule(scheduleSeatFare.getSchedule());
				dynamicStageFareExceptionDTO.setDynamicPriceProvider(dynamicStageFareDTO.getDynamicPriceProvider());
			}
		}
		scheduleDynamicStageFareDAO.updateScheduleDynamicStageFare(authDTO, dynamicStageFareExceptionDTO);

		auditService.addAuditLog(authDTO, trip.getCode(), "trip", "Dynamic Price", scheduleSeatFare.getActiveFlag() == 1 ? "Disable" : "Enabled");
		if (dynamicStageFareExceptionDTO.getDynamicPriceProvider().getId() == DynamicPriceProviderEM.REDBUS.getId()) {
			dynamicFareService.updateDynamicPriceStatus(authDTO, scheduleSeatFare);
		}

		/** Reset Schedule Cache */
		EhcacheManager.getScheduleEhCache().remove(CACHEKEY + scheduleSeatFare.getSchedule().getCode());

	}

	@Async
	public void dynamicFareProcess(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare, ScheduleDynamicStageFareDTO scheduleDynamicStageFareDTO) {
		try {
			/** Fetch Schedule Details */
			scheduleSeatFare.setSchedule(scheduleService.getSchedule(authDTO, scheduleSeatFare.getSchedule()));

			/** Fetch Schedule Stations Details */
			List<ScheduleStationDTO> stations = scheduleStationService.getByScheduleTripDate(authDTO, scheduleSeatFare.getSchedule(), DateUtil.getDateTime(scheduleSeatFare.getActiveFrom()));
			scheduleSeatFare.getSchedule().setStationList(stations);

			DateTime tripDate = DateUtil.getDateTime(scheduleSeatFare.getActiveFrom()).getStartOfDay();
			DateTime activeToDateTime = DateUtil.getDateTime(scheduleSeatFare.getActiveTo()).getStartOfDay();
			if (DateUtil.NOW().compareTo(tripDate) > 0 && activeToDateTime.compareTo(DateUtil.NOW()) > 0) {
				tripDate = DateUtil.NOW();
			}

			/** Fetch Activated Routes */
			Map<String, String> routes = dynamicFareService.getActiveRoutes(authDTO, scheduleSeatFare.getSchedule(), tripDate);

			List<ScheduleDynamicStageFareDetailsDTO> updateStages = null;
			if (!routes.isEmpty()) {
				/** Fetch Via Routes */
				updateStages = new ArrayList<>();

				for (ScheduleDynamicStageFareDetailsDTO scheduleStageDTO : scheduleSeatFare.getStageFare()) {
					String key = scheduleStageDTO.getFromStation().getCode() + Text.HYPHEN + scheduleStageDTO.getToStation().getCode() + Text.HYPHEN + scheduleSeatFare.getSchedule().getCode();
					if (routes.get(key) == null) {
						updateStages.add(scheduleStageDTO);
					}
				}
			}
			else if (scheduleSeatFare.getActiveFlag() == 1 && routes.isEmpty()) {
				/** RB Activate Routes */
				dynamicFareService.activateStageFareAPI(authDTO, scheduleSeatFare);

				if (scheduleSeatFare.getStatus() == Numeric.ONE_INT) {
					scheduleSeatFare.setCode(scheduleDynamicStageFareDTO.getCode());
					ScheduleDynamicStageFareDAO stageFareDAO = new ScheduleDynamicStageFareDAO();
					stageFareDAO.updateStatus(authDTO, scheduleSeatFare);
				}
			}

			/** RB Add / Remove Via Routes */
			if (scheduleSeatFare.getActiveFlag() == 1 && updateStages != null && !updateStages.isEmpty()) {
				/** RB Add Via Routes */
				dynamicFareService.updateScheduleStageFare(authDTO, scheduleSeatFare, updateStages);
			}
			else if (scheduleSeatFare.getActiveFlag() != 1 && updateStages != null && !updateStages.isEmpty()) {
				/** RB Remove Via Routes */
				dynamicFareService.removeScheduleStageFare(authDTO, scheduleSeatFare, updateStages);
			}

			/** Activate Services */
			openChart(authDTO, scheduleSeatFare.getSchedule(), DateUtil.getDateTime(scheduleSeatFare.getActiveFrom()).getStartOfDay());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openChart(AuthDTO authDTO, ScheduleDTO schedule, DateTime dateTime) {
		scheduleService.getSchedule(authDTO, schedule);

		// // Schedule Control
		ScheduleControlDTO scheduleControl = new ScheduleControlDTO();
		scheduleControl.setSchedule(schedule);
		List<ScheduleControlDTO> scheduleControlDTOList = scheduleControlService.get(authDTO, scheduleControl);
		scheduleControl = scheduleControlDTOList.get(Numeric.ZERO_INT);

		DateTime endDate = DateUtil.addMinituesToDate(dateTime, scheduleControl.getOpenMinitues());

		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.pushNewServiceAlert(authDTO.getNamespaceCode(), schedule, endDate);
	}

	@Override
	public void openChart(AuthDTO authDTO, DateTime dateTime) {
		ScheduleDynamicStageFareDAO scheduleDynamicStageFareDAO = new ScheduleDynamicStageFareDAO();
		List<ScheduleDynamicStageFareDTO> scheduleDynamicStageFares = scheduleDynamicStageFareDAO.getDynamicSchedules(authDTO, dateTime);
		for (ScheduleDynamicStageFareDTO scheduleDynamicStageFare : scheduleDynamicStageFares) {
			scheduleService.getSchedule(authDTO, scheduleDynamicStageFare.getSchedule());

			openChart(authDTO, scheduleDynamicStageFare.getSchedule(), dateTime.getStartOfDay());
		}
	}

	@Override
	public List<ScheduleDynamicStageFareDTO> getScheduleStageFare(AuthDTO authDTO, ScheduleDTO schedule) {
		ScheduleDynamicStageFareDAO stageFareDAO = new ScheduleDynamicStageFareDAO();
		List<ScheduleDynamicStageFareDTO> list = stageFareDAO.getScheduleSeatFareByScheduleId(authDTO, schedule);
		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
		if (!list.isEmpty()) {
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, schedule);
			for (ScheduleStationDTO stationDTO : stationList) {
				stationMap.put(stationDTO.getStation().getId(), stationDTO);
			}
		}

		for (ScheduleDynamicStageFareDTO scheduleDynamicStageFare : list) {
			for (Iterator<ScheduleDynamicStageFareDetailsDTO> itrStage = scheduleDynamicStageFare.getStageFare().iterator(); itrStage.hasNext();) {
				ScheduleDynamicStageFareDetailsDTO fareDetails = itrStage.next();
				if (stationMap.get(fareDetails.getFromStation().getId()) == null || stationMap.get(fareDetails.getToStation().getId()) == null) {
					itrStage.remove();
					continue;
				}

				fareDetails.setFromStation(stationService.getStation(fareDetails.getFromStation()));
				fareDetails.setToStation(stationService.getStation(fareDetails.getToStation()));
			}
		}
		return list;
	}

	@Override
	public ScheduleDynamicStageFareDetailsDTO getScheduleDynamicStageFare(AuthDTO authDTO, ScheduleDTO schedule, StationDTO fromStationDTO, StationDTO toStationDTO) {
		String key = CACHEKEY + schedule.getCode();
		List<ScheduleDynamicStageFareDTO> scheduleDynamicStageFareList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleDynamicStageFareCacheDTO> scheduleDynamicStageCacheList = (List<ScheduleDynamicStageFareCacheDTO>) element.getObjectValue();
			scheduleDynamicStageFareList = bindDynamicStageFareFromCacheObject(scheduleDynamicStageCacheList);
		}
		else if (schedule.getId() != 0) {
			ScheduleDynamicStageFareDAO dynamicStageFareDAO = new ScheduleDynamicStageFareDAO();
			scheduleDynamicStageFareList = dynamicStageFareDAO.getByScheduleId(authDTO, schedule);
			// Save to schedule station Point Cache
			List<ScheduleDynamicStageFareCacheDTO> scheduleSeatVisibilityCacheList = bindDynamicStageFareToCacheObject(scheduleDynamicStageFareList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Common validation
		for (Iterator<ScheduleDynamicStageFareDTO> iterator = scheduleDynamicStageFareList.iterator(); iterator.hasNext();) {
			ScheduleDynamicStageFareDTO dynamicStageFareDTO = iterator.next();
			DateTime dateTime = schedule.getTripDate();
			if (StringUtil.isNull(dynamicStageFareDTO.getActiveFrom()) || StringUtil.isNull(dynamicStageFareDTO.getActiveTo()) || StringUtil.isNull(dynamicStageFareDTO.getDayOfWeek())) {
				iterator.remove();
				continue;
			}
			// common validations
			if (!dateTime.gteq(new DateTime(dynamicStageFareDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (!dateTime.lteq(new DateTime(dynamicStageFareDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (dynamicStageFareDTO.getDayOfWeek() != null && dynamicStageFareDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (dynamicStageFareDTO.getDayOfWeek() != null && dynamicStageFareDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (dynamicStageFareDTO.getStageFare() == null || dynamicStageFareDTO.getStageFare().isEmpty()) {
				iterator.remove();
				continue;
			}
			// Override and Exceptions
			for (Iterator<ScheduleDynamicStageFareDTO> overrideItrControlDTO = dynamicStageFareDTO.getOverrideList().iterator(); overrideItrControlDTO.hasNext();) {
				ScheduleDynamicStageFareDTO overrideDTO = overrideItrControlDTO.next();
				// common validations
				if (overrideDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(overrideDTO.getActiveFrom()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(overrideDTO.getActiveTo()))) {
					overrideItrControlDTO.remove();
					continue;
				}
			}

			// Remove if Exceptions
			if (!dynamicStageFareDTO.getOverrideList().isEmpty()) {
				iterator.remove();
				// DP Fare status expose
				schedule.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "DPOFF");
				continue;
			}
			// Identify the Stage fare details
			if (fromStationDTO != null && toStationDTO != null) {
				for (Iterator<ScheduleDynamicStageFareDetailsDTO> fareDetailsIterator = dynamicStageFareDTO.getStageFare().iterator(); fareDetailsIterator.hasNext();) {
					ScheduleDynamicStageFareDetailsDTO fareDetailsDTO = fareDetailsIterator.next();
					if (fareDetailsDTO.getFromStation().getId() != fromStationDTO.getId() || fareDetailsDTO.getToStation().getId() != toStationDTO.getId()) {
						fareDetailsIterator.remove();
						continue;
					}
					if (fareDetailsDTO.getMinFare().compareTo(BigDecimal.ZERO) <= 0 || fareDetailsDTO.getMaxFare().compareTo(BigDecimal.ZERO) <= 0) {
						fareDetailsIterator.remove();
						continue;
					}
				}
			}

			// Remove if empty of Stage fare details
			if (dynamicStageFareDTO.getStageFare().isEmpty()) {
				iterator.remove();
				continue;
			}
		}
		ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = null;
		for (ScheduleDynamicStageFareDTO dynamicStageFareDTO : scheduleDynamicStageFareList) {
			for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : dynamicStageFareDTO.getStageFare()) {
				dynamicStageFareDetailsDTO = dynamicStageFareDetails;
				dynamicStageFareDetailsDTO.setList(dynamicStageFareDTO.getStageFare());
				dynamicStageFareDetailsDTO.setDynamicPriceProvider(dynamicStageFareDTO.getDynamicPriceProvider());
			}
		}

		return dynamicStageFareDetailsDTO;
	}

	// for search,busmap, get trips
	public ScheduleDynamicStageFareDetailsDTO getDynamicPricingTripStageFareDetails(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails) {
		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList = getDynamicPricingTripStageFareDetails(authDTO, schedule);
		// Identify the Stage fare details
		for (Iterator<ScheduleDynamicStageFareDetailsDTO> fareDetailsIterator = scheduleDynamicStageFareList.iterator(); fareDetailsIterator.hasNext();) {
			ScheduleDynamicStageFareDetailsDTO fareDetailsDTO = fareDetailsIterator.next();
			if (fareDetailsDTO.getFromStation().getId() != dynamicStageFareDetails.getFromStation().getId() || fareDetailsDTO.getToStation().getId() != dynamicStageFareDetails.getToStation().getId()) {
				fareDetailsIterator.remove();
				continue;
			}

			if (fareDetailsDTO.getTripDate().compareTo(schedule.getTripDate()) != 0) {
				fareDetailsIterator.remove();
				continue;
			}
			// If no trip fare found
			if (fareDetailsDTO.getSeatFare().isEmpty()) {
				fareDetailsIterator.remove();
				continue;
			}
		}

		ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetailsDTO = null;
		for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDTO : scheduleDynamicStageFareList) {
			dynamicStageTripFareDetailsDTO = dynamicStageFareDTO;
		}

		return dynamicStageTripFareDetailsDTO;
	}

	// To reduce the cache hit, fetch the respective DP trip fare, just filter
	// stage wise, this method only useful if required all DP fare of all stages
	// in a trip.
	public ScheduleDynamicStageFareDetailsDTO getDynamicPricingTripStageFareDetailsV2(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails, List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList) {
		// Fetch fare from Cache if not found
		if (scheduleDynamicStageFareList.isEmpty()) {
			List<ScheduleDynamicStageFareDetailsDTO> dpFareList = getDynamicPricingTripStageFareDetails(authDTO, schedule);
			scheduleDynamicStageFareList.addAll(dpFareList);
		}
		ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetailsDTO = null;

		// Identify the Stage fare details
		for (ScheduleDynamicStageFareDetailsDTO fareDetailsDTO : scheduleDynamicStageFareList) {
			if (fareDetailsDTO.getTripDate().compareTo(schedule.getTripDate()) != 0) {
				continue;
			}
			if (fareDetailsDTO.getFromStation().getId() == dynamicStageFareDetails.getFromStation().getId() && fareDetailsDTO.getToStation().getId() == dynamicStageFareDetails.getToStation().getId()) {
				// If no trip fare found
				if (!fareDetailsDTO.getSeatFare().isEmpty()) {
					dynamicStageTripFareDetailsDTO = fareDetailsDTO;
					break;
				}
			}
		}

		return dynamicStageTripFareDetailsDTO;
	}

	public Map<String, ScheduleDynamicStageFareDetailsDTO> getDynamicPricingTripStageFareDetailsV3(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails) {
		Map<String, ScheduleDynamicStageFareDetailsDTO> dpfareMap = new HashMap<String, ScheduleDynamicStageFareDetailsDTO>();
		List<ScheduleDynamicStageFareDetailsDTO> dpFareList = getDynamicPricingTripStageFareDetails(authDTO, schedule);
		Map<String, String> stageDPMap = new HashMap<String, String>();
		for (ScheduleDynamicStageFareDetailsDTO dpStage : dynamicStageFareDetails.getList()) {
			stageDPMap.put(dpStage.getFromStation().getId() + "_" + dpStage.getToStation().getId(), Text.NA);
		}
		// Identify the Stage fare details
		for (ScheduleDynamicStageFareDetailsDTO fareDetailsDTO : dpFareList) {
			// If no trip fare found
			if (!fareDetailsDTO.getSeatFare().isEmpty() && stageDPMap.get(fareDetailsDTO.getFromStation().getId() + "_" + fareDetailsDTO.getToStation().getId()) != null) {
				dpfareMap.put(fareDetailsDTO.getFromStation().getId() + "_" + fareDetailsDTO.getToStation().getId(), fareDetailsDTO);
			}
		}
		return dpfareMap;
	}

	private List<ScheduleDynamicStageFareDetailsDTO> getDynamicPricingTripStageFareDetails(AuthDTO authDTO, ScheduleDTO schedule) {
		String key = CACHEKEY + schedule.getCode() + Text.UNDER_SCORE + DateUtil.getCompressDate(schedule.getTripDate());
		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList = null;
		Element element = EhcacheManager.getScheduleTripDPEhCache().get(key);
		if (element != null) {
			List<ScheduleDynamicStageFareDetailsCacheDTO> scheduleDynamicStageCacheList = (List<ScheduleDynamicStageFareDetailsCacheDTO>) element.getObjectValue();
			scheduleDynamicStageFareList = bindDynamicStageFareDetailsFromCacheObject(scheduleDynamicStageCacheList);
		}
		else if (schedule.getId() != 0) {
			ScheduleDynamicStageFareDAO dynamicStageFareDAO = new ScheduleDynamicStageFareDAO();
			scheduleDynamicStageFareList = dynamicStageFareDAO.getDPTripStageFareDetails(authDTO, schedule);
			// Save to schedule station Point Cache
			List<ScheduleDynamicStageFareDetailsCacheDTO> scheduleDynamicStageFareDetailsCacheList = bindDynamicStageFareDetailsToCacheObject(scheduleDynamicStageFareList);
			element = new Element(key, scheduleDynamicStageFareDetailsCacheList);
			EhcacheManager.getScheduleTripDPEhCache().put(element);
		}
		return scheduleDynamicStageFareList;
	}

	private List<ScheduleDynamicStageFareDetailsCacheDTO> bindDynamicStageFareDetailsToCacheObject(List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList) {
		List<ScheduleDynamicStageFareDetailsCacheDTO> tripFareCacheList = new ArrayList<ScheduleDynamicStageFareDetailsCacheDTO>();
		for (ScheduleDynamicStageFareDetailsDTO tripFareDetails : scheduleDynamicStageFareList) {
			ScheduleDynamicStageFareDetailsCacheDTO tripFareCache = new ScheduleDynamicStageFareDetailsCacheDTO();
			tripFareCache.setFromStationId(tripFareDetails.getFromStation().getId());
			tripFareCache.setToStationId(tripFareDetails.getToStation().getId());
			List<String> fareList = new ArrayList<String>();
			for (BusSeatLayoutDTO layoutDTO : tripFareDetails.getSeatFare()) {
				fareList.add(layoutDTO.getName() + Text.COLON + layoutDTO.getFare());
			}
			tripFareCache.setTripDate(tripFareDetails.getTripDate().format("YYYY-MM-DD"));
			tripFareCache.setSeatNameFare(fareList);
			tripFareCacheList.add(tripFareCache);
		}
		return tripFareCacheList;
	}

	private List<ScheduleDynamicStageFareDetailsDTO> bindDynamicStageFareDetailsFromCacheObject(List<ScheduleDynamicStageFareDetailsCacheDTO> scheduleDynamicStageCacheList) {
		List<ScheduleDynamicStageFareDetailsDTO> tripFareList = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
		for (ScheduleDynamicStageFareDetailsCacheDTO tripFareDetailsCacheDTO : scheduleDynamicStageCacheList) {
			ScheduleDynamicStageFareDetailsDTO tripFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();
			List<BusSeatLayoutDTO> seatFare = new ArrayList<BusSeatLayoutDTO>();
			for (String seatNameFare : tripFareDetailsCacheDTO.getSeatNameFare()) {
				BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
				layoutDTO.setName(seatNameFare.split(Text.COLON)[0]);
				layoutDTO.setFare(new BigDecimal(seatNameFare.split(Text.COLON)[1]));
				seatFare.add(layoutDTO);
			}
			tripFareDetailsDTO.setSeatFare(seatFare);
			StationDTO fromStation = new StationDTO();
			fromStation.setId(tripFareDetailsCacheDTO.getFromStationId());
			StationDTO toStation = new StationDTO();
			toStation.setId(tripFareDetailsCacheDTO.getToStationId());
			tripFareDetailsDTO.setFromStation(fromStation);
			tripFareDetailsDTO.setToStation(toStation);
			tripFareDetailsDTO.setTripDate(new DateTime(tripFareDetailsCacheDTO.getTripDate()));
			tripFareList.add(tripFareDetailsDTO);
		}

		return tripFareList;
	}

	public List<ScheduleDynamicStageFareCacheDTO> bindDynamicStageFareToCacheObject(List<ScheduleDynamicStageFareDTO> scheduleDynamicStageFareList) {
		List<ScheduleDynamicStageFareCacheDTO> dynamicStageFareCacheList = new ArrayList<>();
		// copy to cache
		if (scheduleDynamicStageFareList != null && !scheduleDynamicStageFareList.isEmpty()) {
			for (ScheduleDynamicStageFareDTO dynamicStageFareDTO : scheduleDynamicStageFareList) {
				ScheduleDynamicStageFareCacheDTO dynamicStageFareCacheDTO = new ScheduleDynamicStageFareCacheDTO();

				dynamicStageFareCacheDTO.setCode(dynamicStageFareDTO.getCode());
				dynamicStageFareCacheDTO.setLookupCode(dynamicStageFareDTO.getLookupCode());
				dynamicStageFareCacheDTO.setActiveFrom(dynamicStageFareDTO.getActiveFrom());
				dynamicStageFareCacheDTO.setActiveTo(dynamicStageFareDTO.getActiveTo());
				dynamicStageFareCacheDTO.setDayOfWeek(dynamicStageFareDTO.getDayOfWeek());
				dynamicStageFareCacheDTO.setDynamicPriceProviderId(dynamicStageFareDTO.getDynamicPriceProvider().getId());

				List<ScheduleDynamicStageFareDetailsCacheDTO> fareDetailsList = new ArrayList<ScheduleDynamicStageFareDetailsCacheDTO>();
				for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO : dynamicStageFareDTO.getStageFare()) {
					ScheduleDynamicStageFareDetailsCacheDTO fareDetailsCacheDTO = new ScheduleDynamicStageFareDetailsCacheDTO();
					fareDetailsCacheDTO.setFromStationId(dynamicStageFareDetailsDTO.getFromStation().getId());
					fareDetailsCacheDTO.setToStationId(dynamicStageFareDetailsDTO.getToStation().getId());
					fareDetailsCacheDTO.setMinFare(dynamicStageFareDetailsDTO.getMinFare());
					fareDetailsCacheDTO.setMaxFare(dynamicStageFareDetailsDTO.getMaxFare());

					fareDetailsList.add(fareDetailsCacheDTO);
				}
				dynamicStageFareCacheDTO.setDynamicStageFare(fareDetailsList);

				// Override
				List<ScheduleDynamicStageFareCacheDTO> overrideList = new ArrayList<>();
				if (dynamicStageFareDTO.getOverrideList() != null && !dynamicStageFareDTO.getOverrideList().isEmpty()) {
					for (ScheduleDynamicStageFareDTO overRideCacheDTO : dynamicStageFareDTO.getOverrideList()) {
						ScheduleDynamicStageFareCacheDTO overrideDTO = new ScheduleDynamicStageFareCacheDTO();
						overrideDTO.setCode(overRideCacheDTO.getCode());
						overrideDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideList.add(overrideDTO);
					}
					dynamicStageFareCacheDTO.setOverrideList(overrideList);
				}
				dynamicStageFareCacheList.add(dynamicStageFareCacheDTO);
			}
		}
		return dynamicStageFareCacheList;
	}

	private List<ScheduleDynamicStageFareDTO> bindDynamicStageFareFromCacheObject(List<ScheduleDynamicStageFareCacheDTO> scheduleDynamicStageFareCacheList) {
		List<ScheduleDynamicStageFareDTO> scheduleDynamicStageFareList = new ArrayList<>();
		// copy from cache
		for (ScheduleDynamicStageFareCacheDTO stageFareCache : scheduleDynamicStageFareCacheList) {
			ScheduleDynamicStageFareDTO dynamicStageFare = new ScheduleDynamicStageFareDTO();
			dynamicStageFare.setCode(stageFareCache.getCode());
			dynamicStageFare.setLookupCode(stageFareCache.getLookupCode());
			dynamicStageFare.setActiveFrom(stageFareCache.getActiveFrom());
			dynamicStageFare.setActiveTo(stageFareCache.getActiveTo());
			dynamicStageFare.setDayOfWeek(stageFareCache.getDayOfWeek());
			dynamicStageFare.setDynamicPriceProvider(DynamicPriceProviderEM.getDynamicPriceProviderEM(stageFareCache.getDynamicPriceProviderId()));

			if (stageFareCache.getDynamicStageFare() != null && !stageFareCache.getDynamicStageFare().isEmpty()) {
				List<ScheduleDynamicStageFareDetailsDTO> dynamicStageFareDetailsList = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
				for (ScheduleDynamicStageFareDetailsCacheDTO detailsCacheDTO : stageFareCache.getDynamicStageFare()) {
					ScheduleDynamicStageFareDetailsDTO detailsDTO = new ScheduleDynamicStageFareDetailsDTO();
					StationDTO fromStation = new StationDTO();
					fromStation.setId(detailsCacheDTO.getFromStationId());
					StationDTO toStation = new StationDTO();
					toStation.setId(detailsCacheDTO.getToStationId());
					detailsDTO.setFromStation(fromStation);
					detailsDTO.setToStation(toStation);
					detailsDTO.setMinFare(detailsCacheDTO.getMinFare());
					detailsDTO.setMaxFare(detailsCacheDTO.getMaxFare());

					dynamicStageFareDetailsList.add(detailsDTO);
				}
				dynamicStageFare.setStageFare(dynamicStageFareDetailsList);
			}
			// Override
			List<ScheduleDynamicStageFareDTO> overrideList = new ArrayList<>();
			if (stageFareCache.getOverrideList() != null && !stageFareCache.getOverrideList().isEmpty()) {
				for (ScheduleDynamicStageFareCacheDTO overrideCacheDTO : stageFareCache.getOverrideList()) {
					ScheduleDynamicStageFareDTO overrideDTO = new ScheduleDynamicStageFareDTO();
					overrideDTO.setCode(overrideCacheDTO.getCode());
					overrideDTO.setActiveFrom(overrideCacheDTO.getActiveFrom());
					overrideDTO.setDayOfWeek(overrideCacheDTO.getDayOfWeek());
					overrideDTO.setActiveTo(overrideCacheDTO.getActiveTo());
					overrideList.add(overrideDTO);
				}
				dynamicStageFare.setOverrideList(overrideList);
			}
			scheduleDynamicStageFareList.add(dynamicStageFare);
		}
		return scheduleDynamicStageFareList;
	}

	public static boolean isValidDate(DateTime scheduleFromDate, DateTime scheduleToDate, DateTime dynamicFromDate) {
		boolean flag = false;
		if (dynamicFromDate.gteq(scheduleFromDate) && dynamicFromDate.lteq(scheduleToDate)) {
			flag = true;
		}
		return flag;
	}

	@Override
	public List<Map<String, ?>> getScheduleDynamicFareDetails(AuthDTO authDTO, ScheduleDTO schedule) {
		List<Map<String, ?>> result = new ArrayList<>();
		try {
			List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();

			DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
			namespaceParamDTO.setParamName("namespaceId");
			namespaceParamDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
			paramList.add(namespaceParamDTO);

			DBQueryParamDTO fromDateParam = new DBQueryParamDTO();
			fromDateParam.setParamName("fromDate");
			fromDateParam.setValue(schedule.getActiveFrom());
			paramList.add(fromDateParam);

			DBQueryParamDTO toDateParam = new DBQueryParamDTO();
			toDateParam.setParamName("toDate");
			toDateParam.setValue(schedule.getActiveTo());
			paramList.add(toDateParam);

			DBQueryParamDTO scheduleCodeParam = new DBQueryParamDTO();
			scheduleCodeParam.setParamName("scheduleCode");
			scheduleCodeParam.setValue(schedule.getCode());
			paramList.add(scheduleCodeParam);

			ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
			reportQueryDTO.setQuery(QUERY);

			result = reportService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return result;
	}

	// Store the DP fare change notify to redis cache for queue process
	// redbus DP only using it
	public void notifyFareChangeQueue(AuthDTO authDTO, ScheduleDTO schedule) {
		try {
			// allow past two days
			if (DateUtil.getDayDifferent(schedule.getTripDate(), DateUtil.NOW()) > 2) {
				throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
			}
			scheduleService.getSchedule(authDTO, schedule);
			/** Fetch Schedule Details */
			schedule = scheduleService.getActiveSchedule(authDTO, schedule);

			// :ToDo need to validate 15min once fare update of trip, validate
			// and add log

			// validate DP is active or not
			ScheduleDynamicStageFareDetailsDTO dynamicStageFare = getScheduleDynamicStageFare(authDTO, schedule, null, null);
			if (dynamicStageFare != null) {
				redisTripCacheService.putNotifyFareChangeRequest(authDTO, schedule);
			}
			else {
				throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE, "DP is not active");
			}
		}
		catch (ServiceException e) {
			DYNAMIC_LOGGER.error("schedule fare notify: {} {} {} {} {}", authDTO.getNamespaceCode(), schedule.getCode(), schedule.getTripDate(), e.getErrorCode(), e.getMessage());
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS, "please try again");
		}
	}

	// Process the DP fare change notify from redis queue process
	public void processFareChangeQueueJob(AuthDTO auth) {
		String requestProcessCode = TokenGenerator.generateCode("DPFUC");
		JSONArray requests = redisTripCacheService.getNotifyFareChangeRequest();
		DYNAMIC_LOGGER.info("{} DPFU Cron Queue size: {}", requestProcessCode, requests.size());
		for (Object requestJson : requests) {
			DYNAMIC_LOGGER.info("{} fare update process start: {}", requestProcessCode, requestJson);
			JSONObject request = (JSONObject) requestJson;

			AuthDTO authDTO = new AuthDTO();
			authDTO.setNamespaceCode(request.getString("namespaceCode"));
			authDTO.getAdditionalAttribute().put("requestProcessCode", requestProcessCode);

			ScheduleDTO schedule = new ScheduleDTO();
			schedule.setCode(request.getString("scheduleCode"));
			schedule.setTripDate(DateUtil.getDateTime(request.getString("tripDate")));

			DateTime scheduleTripDate = schedule.getTripDate();
			scheduleService.getSchedule(authDTO, schedule);
			schedule.setTripDate(scheduleTripDate);
			try {

				TripDTO tripDTO = tripService.getTripsByScheduleTripDate(authDTO, schedule);

				List<BusSeatLayoutDTO> seatFares = dynamicFareService.getStageFare(authDTO, schedule, scheduleTripDate);
				if (seatFares.isEmpty()) {
					DYNAMIC_LOGGER.error("{} DP fare is empty unable to process {} {}", requestProcessCode, schedule.getCode(), schedule.getTripDate());
					continue;
				}
				// Get Schedule Seat Fare
				List<ScheduleDynamicStageFareDTO> dynamicStageFareList = getScheduleStageFare(authDTO, schedule);

				List<ScheduleDynamicStageFareDetailsDTO> dynamicStageFareRepoList = new ArrayList<>();

				for (ScheduleDynamicStageFareDTO dynamicStageFareDTO : dynamicStageFareList) {
					DateTime fromDatetime = new DateTime(dynamicStageFareDTO.getActiveFrom() + " 00:00:00");
					DateTime toDateTime = new DateTime(dynamicStageFareDTO.getActiveTo() + " 23:59:59");

					// Validate Date time by Schedule Control
					if (!isValidDate(fromDatetime, toDateTime, scheduleTripDate.getStartOfDay())) {
						continue;
					}

					if (dynamicStageFareDTO.getDynamicPriceProvider().getId() != DynamicPriceProviderEM.REDBUS.getId()) {
						throw new ServiceException(ErrorCode.UNAUTHORIZED, "DP not enabled");
					}
					dynamicStageFareDTO.setSchedule(schedule);
					if (dynamicStageFareDTO.getStageFare().isEmpty()) {
						continue;
					}
					for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : dynamicStageFareDTO.getStageFare()) {
						dynamicStageFareDetails.setFromStation(stationService.getStation(dynamicStageFareDetails.getFromStation()));
						dynamicStageFareDetails.setToStation(stationService.getStation(dynamicStageFareDetails.getToStation()));

						// Get Stage Fare From API
						List<BusSeatLayoutDTO> fareList = getSeatFares(seatFares, schedule, dynamicStageFareDetails);
						if (fareList.isEmpty()) {
							continue;
						}

						dynamicStageFareDetails.setSeatFare(fareList);
						dynamicStageFareDetails.setTripDate(scheduleTripDate);
						dynamicStageFareDetails.setDynamicPriceProvider(dynamicStageFareDTO.getDynamicPriceProvider());
						dynamicStageFareRepoList.add(dynamicStageFareDetails);
					}
					scheduleDynamicStageFareRepo.updateScheduleDynamicStageFareMappping(authDTO, schedule, dynamicStageFareRepoList);
					DYNAMIC_LOGGER.info("{} fare save in repo {} {} Size:{}", requestProcessCode, schedule.getCode(), schedule.getTripDate(), dynamicStageFareDTO.getStageFare().size());
				}

				// push Merc Service
				if (Constants.MERC_SERVICE_FARE_INDEX.contains(authDTO.getNamespaceCode())) {
					mercService.indexFareHistory(authDTO, schedule, tripDTO, dynamicStageFareRepoList);
				}

				reloadTripDynamicPricingCache(authDTO, schedule);
				DYNAMIC_LOGGER.info("{} DPFU reloaded Trip DP Cache: {}", requestProcessCode, requestJson);
			}
			catch (ServiceException e) {
				DYNAMIC_LOGGER.error("{} {} Process queue error {}", requestProcessCode, requestJson, e.getErrorCode().toString());
			}
			catch (Exception e) {
				DYNAMIC_LOGGER.error("{} Process queue {}", requestProcessCode, requestJson);
				e.printStackTrace();
			}
			finally {
				DYNAMIC_LOGGER.info("{} remove Notify Fare Change Request: {}", requestProcessCode, requestJson);
				redisTripCacheService.removeNotifyFareChangeRequest(authDTO, schedule);
			}
			DYNAMIC_LOGGER.info("{} fare update process End: {}", requestProcessCode, requestJson);
		}
		DYNAMIC_LOGGER.info("{} DPFU Cron Queue completed", requestProcessCode);
	}

	// Sciative Integration
	public void notifyUpdateTripStageFareChange(AuthDTO authDTO, TripDTO tripDTO, List<StageDTO> stageList) {
		DYNAMIC_LOGGER.info("{} DP fare direct update: start {}", authDTO.getAdditionalAttribute().get("requestProcessCode"), tripDTO.getCode());
		tripService.getTrip(authDTO, tripDTO);
		DateTime scheduleTripDate = tripDTO.getTripDate();
		ScheduleDTO schedule = scheduleService.getSchedule(authDTO, tripDTO.getSchedule());
		schedule.setTripDate(scheduleTripDate);

		BusDTO bus = busService.getBus(authDTO, tripDTO.getBus());
		Map<String, BusSeatLayoutDTO> layoutMap = bus.getBusSeatLayoutMapFromList();
		List<ScheduleDynamicStageFareDetailsDTO> dynamicStageFareRepoList = new ArrayList<>();

		// Get Schedule Seat Fare
		List<ScheduleDynamicStageFareDTO> dynamicStageFareList = getScheduleStageFare(authDTO, schedule);

		for (StageDTO stage : stageList) {

			for (ScheduleDynamicStageFareDTO dynamicStageFareDTO : dynamicStageFareList) {
				DateTime fromDatetime = new DateTime(dynamicStageFareDTO.getActiveFrom());
				DateTime toDateTime = new DateTime(dynamicStageFareDTO.getActiveTo());

				dynamicStageFareDTO.setSchedule(schedule);
				if (!fromDatetime.lt(scheduleTripDate) && !toDateTime.gt(scheduleTripDate)) {
					continue;
				}
				if (dynamicStageFareDTO.getDynamicPriceProvider().getId() != DynamicPriceProviderEM.SCIATIVE.getId()) {
					throw new ServiceException(ErrorCode.UNAUTHORIZED, "DP not enabled");
				}
				if (dynamicStageFareDTO.getStageFare().isEmpty()) {
					continue;
				}
				for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : dynamicStageFareDTO.getStageFare()) {
					dynamicStageFareDetails.setFromStation(stationService.getStation(dynamicStageFareDetails.getFromStation()));
					dynamicStageFareDetails.setToStation(stationService.getStation(dynamicStageFareDetails.getToStation()));
					if (!dynamicStageFareDetails.getFromStation().getCode().equals(stage.getFromStation().getStation().getCode()) || !dynamicStageFareDetails.getToStation().getCode().equals(stage.getToStation().getStation().getCode())) {
						continue;
					}
					// Get Stage Fare From API
					List<BusSeatLayoutDTO> fareList = getSeatFares(stage, schedule, dynamicStageFareDetails, layoutMap);
					if (fareList.isEmpty()) {
						throw new ServiceException(ErrorCode.UPDATE_FAIL, "Seat Layout not matched");
					}
					if (fareList.size() != layoutMap.size()) {
						throw new ServiceException(ErrorCode.UPDATE_FAIL, "Some Seat details not matched or missed");
					}

					dynamicStageFareDetails.setSeatFare(fareList);
					dynamicStageFareDetails.setTripDate(scheduleTripDate);
					dynamicStageFareDetails.setDynamicPriceProvider(dynamicStageFareDTO.getDynamicPriceProvider());
					dynamicStageFareRepoList.add(dynamicStageFareDetails);
				}
			}
		}
		scheduleDynamicStageFareRepo.updateScheduleDynamicStageFareMappping(authDTO, schedule, dynamicStageFareRepoList);
		// push Merc Service
		if (Constants.MERC_SERVICE_FARE_INDEX.contains(authDTO.getNamespaceCode())) {
			mercService.indexFareHistory(authDTO, schedule, tripDTO, dynamicStageFareRepoList);
		}
		// reload cache
		reloadTripDynamicPricingCache(authDTO, schedule);
		DYNAMIC_LOGGER.info("{} DP fare direct update: End {}", authDTO.getAdditionalAttribute().get("requestProcessCode"), tripDTO.getCode());
	}

	// Redbus fare, direct response
	public JSONObject getScheduleStageTripDPRawFare(AuthDTO authDTO, ScheduleDTO schedule) {
		DateTime scheduleTripDate = schedule.getTripDate();
		JSONObject seatFares = dynamicFareService.getScheduleStageTripDPRawFare(authDTO, schedule, scheduleTripDate);
		return seatFares;
	}

	private List<BusSeatLayoutDTO> getSeatFares(List<BusSeatLayoutDTO> fareList, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails) {
		List<BusSeatLayoutDTO> finalList = new ArrayList<>();
		String routeId = dynamicStageFareDetails.getFromStation().getCode() + "-" + dynamicStageFareDetails.getToStation().getCode() + "-" + schedule.getCode();
		for (BusSeatLayoutDTO busSeatLayoutDTO : fareList) {
			if (!routeId.equals(busSeatLayoutDTO.getCode())) {
				continue;
			}

			if (busSeatLayoutDTO.getFare().compareTo(dynamicStageFareDetails.getMinFare()) < 0) {
				busSeatLayoutDTO.setFare(dynamicStageFareDetails.getMinFare());
			}
			else if (busSeatLayoutDTO.getFare().compareTo(dynamicStageFareDetails.getMaxFare()) > 0) {
				busSeatLayoutDTO.setFare(dynamicStageFareDetails.getMaxFare());
			}
			finalList.add(busSeatLayoutDTO);
		}
		return finalList;
	}

	private List<BusSeatLayoutDTO> getSeatFares(StageDTO stage, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails, Map<String, BusSeatLayoutDTO> layoutMap) {
		List<BusSeatLayoutDTO> finalList = new ArrayList<>();
		for (BusSeatLayoutDTO busSeatLayoutDTO : stage.getBus().getBusSeatLayoutDTO().getList()) {

			if (layoutMap.get(busSeatLayoutDTO.getCode()) == null || !layoutMap.get(busSeatLayoutDTO.getCode()).getName().equals(busSeatLayoutDTO.getName())) {
				continue;
			}
			if (busSeatLayoutDTO.getFare().compareTo(dynamicStageFareDetails.getMinFare()) < 0) {
				busSeatLayoutDTO.setFare(dynamicStageFareDetails.getMinFare());
			}
			else if (busSeatLayoutDTO.getFare().compareTo(dynamicStageFareDetails.getMaxFare()) > 0) {
				busSeatLayoutDTO.setFare(dynamicStageFareDetails.getMaxFare());
			}
			finalList.add(busSeatLayoutDTO);
		}
		return finalList;
	}

	private void reloadTripDynamicPricingCache(AuthDTO authDTO, ScheduleDTO schedule) {
		// Update cache
		String farekey = CACHEKEY + schedule.getCode() + Text.UNDER_SCORE + DateUtil.getCompressDate(schedule.getTripDate());
		DYNAMIC_LOGGER.info("{} DP fare Cache Reload: start {}", authDTO.getAdditionalAttribute().get("requestProcessCode"), farekey);

		// reload to cache
		ScheduleDynamicStageFareDAO dynamicStageFareDAO = new ScheduleDynamicStageFareDAO();
		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList = dynamicStageFareDAO.getDPTripStageFareDetails(authDTO, schedule);

		// Save to Cache
		List<ScheduleDynamicStageFareDetailsCacheDTO> scheduleDynamicStageFareDetailsCacheList = bindDynamicStageFareDetailsToCacheObject(scheduleDynamicStageFareList);
		Element element = new Element(farekey, scheduleDynamicStageFareDetailsCacheList);
		EhcacheManager.getScheduleTripDPEhCache().put(element);
		DYNAMIC_LOGGER.info("{} DP fare Cache Reload: End {}", authDTO.getAdditionalAttribute().get("requestProcessCode"), farekey);
	}

	@Async
	public void updateTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDTO tripDTO = tripService.getTrip(authDTO, ticketDTO.getTripDTO());

		ScheduleDTO schedule = scheduleService.getSchedule(authDTO, tripDTO.getSchedule());
		ScheduleDynamicStageFareDetailsDTO dynamicStageFare = getScheduleDynamicStageFare(authDTO, schedule, ticketDTO.getFromStation(), ticketDTO.getToStation());

		if (dynamicStageFare != null && dynamicStageFare.getDynamicPriceProvider().getId() != 0) {
			ticketDTO.getTripDTO().setSchedule(schedule);
			dynamicFareService.updateTicketStatus(authDTO, dynamicStageFare, ticketDTO);
		}
		else {
			DYNAMIC_LOGGER.error("DP push ticket event missed:{} {}", authDTO.getNamespaceCode(), ticketDTO.getCode());
		}
	}

	@Async
	public void updateSeatStatus(AuthDTO authDTO, ScheduleSeatVisibilityDTO scheduleSeatVisibility) {
		scheduleSeatVisibility.setSchedule(scheduleService.getSchedule(authDTO, scheduleSeatVisibility.getSchedule()));
		List<ScheduleStationDTO> stations = scheduleStationService.getByScheduleTripDate(authDTO, scheduleSeatVisibility.getSchedule(), DateUtil.getDateTime(scheduleSeatVisibility.getActiveFrom()));
		scheduleSeatVisibility.getSchedule().setStationList(stations);

		BusDTO busDTO = new BusDTO();
		busDTO.setCode(scheduleSeatVisibility.getBus().getCode());
		busDTO = getBusDTO(authDTO, busDTO);
		Map<String, BusSeatLayoutDTO> seatMap = busDTO.getBusSeatLayoutMapFromList();

		for (ScheduleSeatVisibilityDTO scheduleSeatVisibilityDTO : scheduleSeatVisibility.getList()) {
			for (BusSeatLayoutDTO busSeatLayoutDTO : scheduleSeatVisibility.getBus().getBusSeatLayoutDTO().getList()) {
				busSeatLayoutDTO.setName(seatMap.get(busSeatLayoutDTO.getCode()).getName());
			}
			if ("SG".equals(scheduleSeatVisibilityDTO.getRefferenceType())) {
				for (RouteDTO routeDTO : scheduleSeatVisibilityDTO.getRouteList()) {
					checkAndUpdateSeatStatus(authDTO, scheduleSeatVisibilityDTO, routeDTO.getFromStation(), routeDTO.getToStation());
				}
			}
			else {
				ScheduleStationDTO fromStation = BitsUtil.getOriginStation(scheduleSeatVisibilityDTO.getSchedule().getStationList());
				ScheduleStationDTO toStation = BitsUtil.getDestinationStation(scheduleSeatVisibilityDTO.getSchedule().getStationList());
				checkAndUpdateSeatStatus(authDTO, scheduleSeatVisibilityDTO, fromStation.getStation(), toStation.getStation());
			}
		}
	}

	private void checkAndUpdateSeatStatus(AuthDTO authDTO, ScheduleSeatVisibilityDTO scheduleSeatVisibility, StationDTO fromStation, StationDTO toStation) {
		ScheduleDynamicStageFareDetailsDTO dynamicStageFare = getScheduleDynamicStageFare(authDTO, scheduleSeatVisibility.getSchedule(), fromStation, toStation);
		if (dynamicStageFare != null) {
			DynamicFareServiceImpl dynamicFareServiceImpl = new DynamicFareServiceImpl();
			dynamicFareServiceImpl.updateSeatStatus(authDTO, fromStation, toStation, scheduleSeatVisibility);
		}
	}

	@Override
	public List<ScheduleDynamicStageFareDetailsDTO> getScheduleDynamicStageTripFareDetails(AuthDTO authDTO, ScheduleDTO schedule) {
		schedule = scheduleService.getSchedule(authDTO, schedule);

		ScheduleDynamicStageFareDAO stageFareDAO = new ScheduleDynamicStageFareDAO();
		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareDetails = stageFareDAO.getAllDPTripStageFareDetails(authDTO, schedule);
		for (ScheduleDynamicStageFareDetailsDTO fareDetails : scheduleDynamicStageFareDetails) {
			fareDetails.setFromStation(stationService.getStation(fareDetails.getFromStation()));
			fareDetails.setToStation(stationService.getStation(fareDetails.getToStation()));
		}
		return scheduleDynamicStageFareDetails;
	}

	public void notifyBusTypeChange(AuthDTO authDTO, ScheduleDTO schedule, ScheduleBusOverrideDTO busOverride) {
		schedule = scheduleService.getSchedule(authDTO, schedule);
		schedule.setTripDate(busOverride.getActiveFromDateTime());
		ScheduleDynamicStageFareDetailsDTO dynamicStageFare = getScheduleDynamicStageFare(authDTO, schedule, null, null);
		if (dynamicStageFare != null) {
			dpFactoryService.notifyBusTypeChange(authDTO, schedule, dynamicStageFare, busOverride);
		}
	}
}
