package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.in.com.aggregator.costiv.CostivService;
import org.in.com.aggregator.gps.TrackBusService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.BusCache;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.TripCache;
import org.in.com.cache.redis.RedisTripCacheService;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.AttendantCategoryEM;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.OverrideTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.PushTripInfoService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTagService;
import org.in.com.service.ScheduleTimeOverrideService;
import org.in.com.service.SectorService;
import org.in.com.service.StationService;
import org.in.com.service.TripHelperService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;
import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class TripImpl extends CacheCentral implements TripService {
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.TripImpl");

	private static final String TAX = "tax";
	private static final String EXPENSE = "expense";
	private static final String VALUE = "value";

	@Autowired
	SMSService smsService;
	@Autowired
	TrackBusService trackbusService;
	@Autowired
	BusVehicleService vehicleService;
	@Autowired
	RedisTripCacheService cacheService;
	@Autowired
	ScheduleBusService scheduleBusService;
	@Autowired
	ScheduleTimeOverrideService timeOverrideService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStageService scheduleStageService;
	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;
	@Autowired
	TripHelperService tripHelperService;
	@Autowired
	BusVehicleDriverService driverService;
	@Autowired
	CostivService costivService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleTagService scheduleTagService;
	@Autowired
	BusBreakevenService breakevenService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	PushTripInfoService pushTripInfoService;
	@Autowired
	SectorService sectorService;

	public List<TicketDetailsDTO> getBookedBlockedSeats(AuthDTO authDTO, TripDTO tripDTO) {
		List<TicketDetailsDTO> list = cacheService.getBookedBlockedSeatsCache(authDTO, tripDTO);

		if (list == null) {
			TripDAO tripDAO = new TripDAO();
			if (tripDTO.getId() == 0 && StringUtil.isNull(tripDTO.getCode())) {
				System.out.println("----------------error in getBookedBlockedSeats --------------- ");
			}
			list = tripDAO.getBookedBlockedSeats(authDTO, tripDTO);
			// Generate Trip Stage Code
			for (TicketDetailsDTO detailsDTO : list) {
				detailsDTO.setTripStageCode(tripDTO.getCode() + detailsDTO.getFromStation().getId() + "T" + detailsDTO.getToStation().getId());
			}
			// Update Trip stage seat details cache
			cacheService.putBookedBlockedSeatsCache(authDTO, tripDTO, list);

		}
		tripDTO.setTicketDetailsList(list);

		return list;
	}

	public void SaveBookedBlockedSeats(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.SaveBookedBlockedSeats(authDTO, ticketDTO);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	public List<TripDTO> saveTrip(AuthDTO authDTO, List<TripDTO> tripList) {
		List<TripDTO> activeList = new ArrayList<TripDTO>();

		TripDAO tripDAO = new TripDAO();
		TripCache tripCache = new TripCache();
		List<TripDTO> saveList = new ArrayList<TripDTO>();
		tripCache.CheckAndGetTripDTO(authDTO, tripList);

		for (TripDTO tripDTO : tripList) {
			if (tripDTO.getId() == 0) {
				saveList.add(tripDTO);
			}
			else if (tripDTO.getId() != 0) {
				activeList.add(tripDTO);
			}
		}
		if (!saveList.isEmpty()) {
			// Save in DB
			tripDAO.saveTripDTO(authDTO, saveList);
			// Update in Cache
			tripCache.putAllTripDTO(authDTO, saveList);
			activeList.addAll(saveList);
		}
		return activeList;
	}

	public TripDTO getTripDTO(AuthDTO authDTO, StageDTO stageDTO) {
		TripDAO tripDAO = new TripDAO();
		TripDTO tripDTO = tripDAO.getTripStageDetails(authDTO, stageDTO);
		if (tripDTO == null || tripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		return tripDTO;
	}

	public TripDTO getTripsByScheduleTripDate(AuthDTO authDTO, ScheduleDTO schedule) {
		if (schedule == null || schedule.getId() == 0 || StringUtil.isNull(schedule.getTripDate())) {
			throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, schedule.getId() + Text.HYPHEN + schedule.getCode() + Text.HYPHEN + schedule.getTripDate().format("YYYY-MM-DD"));
		}

		TripDAO tripDAO = new TripDAO();
		TripDTO tripDTO = tripDAO.getTripsByScheduleTripDate(authDTO, schedule);

		if (tripDTO == null || tripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE, schedule.getId() + Text.HYPHEN + schedule.getCode() + Text.HYPHEN + schedule.getTripDate().format("YYYY-MM-DD"));
		}
		return tripDTO;
	}

	public void UpdateTripStatus(AuthDTO authDTO, TripDTO tripDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripStatus(authDTO, tripDTO);

		notificationPushService.pushServiceUpdateNotification(authDTO, tripDTO);
		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, tripDTO);
		TripCache tripCache = new TripCache();
		tripCache.removeTrip(authDTO, tripDTO.getCode());
	}

	public void SaveBookedBlockedSeats(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.SaveBookedBlockedSeats(authDTO, ticketDTO, connection);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	public void updateTripSeatDetailsStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripSeatDetailsFlag(connection, authDTO, ticketDTO);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	public void updateTripSeatDetailsWithExtras(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripSeatDetailsWithExtras(connection, authDTO, ticketDTO);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	public void updateTripSeatDetailsWithExtrasV2(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripSeatDetailsWithExtrasV2(connection, authDTO, ticketDTO);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	@Override
	public void updateTripSeatDetailsWithExtras(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();

		Boolean isFound = null;
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
			TicketDetailsDTO seatDetailsDTO = iterator.next();

			isFound = false;
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				if (seatDetailsDTO.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
					seatDetailsDTO.getTicketExtra().setTravelStatus(ticketDetailsDTO.getTravelStatus());
					isFound = true;
					break;
				}
			}

			if (!isFound) {
				iterator.remove();
			}
		}

		TicketDTO ticket = new TicketDTO();
		ticket.setCode(ticketDTO.getCode());
		ticket.setTicketDetails(seatList);

		tripDAO.updateTripSeatDetailsWithExtras(authDTO, ticket);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	public TripChartDTO getTripChart(AuthDTO authDTO, TripDTO tripDTO) {
		TripChartDTO tripChartDTO = new TripChartDTO();
		try {
			tripDTO = getTrip(authDTO, tripDTO);
			if (tripDTO.getId() == 0 || tripDTO.getSchedule() == null || tripDTO.getSchedule().getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
			}
			// Apply sector validation
			SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);

			tripHelperService.getTripDetails(authDTO, tripDTO);

			// Apply Sector schedule, Station filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isScheduleExists(sector.getSchedule(), tripDTO.getSchedule()) == null && BitsUtil.isStationExists(sector.getStation(), tripDTO.getStationList()) == null) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}

			if (authDTO.getAdditionalAttribute().get(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_ALL.getActionCode()) != null) {
				DateTime now = DateTime.now(TimeZone.getDefault());
				int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getTripDateTimeV2());
				if (Integer.parseInt(authDTO.getAdditionalAttribute().get(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_ALL.getActionCode())) < minutiesDiff) {
					throw new ServiceException(ErrorCode.UNAUTHORIZED);
				}
			}
			TicketDAO dao = new TicketDAO();
			tripChartDTO = dao.getTicketForTripChart(authDTO, tripDTO);

			getTripInfo(authDTO, tripDTO);

			for (Iterator<TripChartDetailsDTO> iterator = tripChartDTO.getTicketDetailsList().iterator(); iterator.hasNext();) {
				TripChartDetailsDTO chartDetailsDTO = iterator.next();

				chartDetailsDTO.setFromStation(stationService.getStation(chartDetailsDTO.getFromStation()));
				chartDetailsDTO.setToStation(stationService.getStation(chartDetailsDTO.getToStation()));

				chartDetailsDTO.getUser().setGroup(groupService.getGroup(authDTO, chartDetailsDTO.getUser().getGroup()));

				if (StringUtil.isNotNull(chartDetailsDTO.getBookingType())) {
					chartDetailsDTO.setBookingType("Online");
				}
				if (chartDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					// Validate PBL Block Live Time
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setCode(chartDetailsDTO.getTicketCode());
					TicketExtraDTO ticketExtraDTO = dao.getTicketExtra(authDTO, ticketDTO);

					if (BitsUtil.validateBlockReleaseTime(ticketExtraDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), DateUtil.getDateTime(chartDetailsDTO.getTicketAt()))) {
						iterator.remove();
						continue;
					}
					chartDetailsDTO.setBookingType("To Pay");
				}
				// Update Seat fare with discount
				for (TicketAddonsDetailsDTO addonsDetailsDTO : chartDetailsDTO.getTicketAddonsDetailsList()) {
					if (addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.COUPON_DISCOUNT.getId() || addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.SCHEDULE_DISCOUNT.getId() || addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.DISCOUNT_AMOUNT.getId() || addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.OFFLINE_DISCOUNT.getId()) {
						chartDetailsDTO.setSeatFare(chartDetailsDTO.getSeatFare().subtract(addonsDetailsDTO.getValue()));
					}
					else if (addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.CUSTOMER_ID_PROOF.getId()) {
						chartDetailsDTO.setIdProof(addonsDetailsDTO.getRefferenceCode());
					}
				}
			}
			tripChartDTO.setTrip(tripDTO);
		}
		catch (ServiceException e) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripChartDTO;
	}

	public TripChartDTO getTripsForTripchart(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		TripDAO dao = new TripDAO();
		dao.getTripsForTripchart(authDTO, tripChartDTO);
		BusCache busCache = new BusCache();
		ScheduleCache scheduleCache = new ScheduleCache();
		for (TripDTO tripDTO : tripChartDTO.getTripList()) {
			tripDTO.setSchedule(scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule()));
			ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, tripDTO.getSchedule());
			tripDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
		}

		return tripChartDTO;
	}

	public TripChartDTO getTripsForTripchartV2(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		TripDAO dao = new TripDAO();
		dao.getTripsForTripchart(authDTO, tripChartDTO);
		BusCache busCache = new BusCache();
		ScheduleCache scheduleCache = new ScheduleCache();
		for (TripDTO tripDTO : tripChartDTO.getTripList()) {
			tripDTO.setSchedule(scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule()));
			ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, tripDTO.getSchedule());
			tripDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
			tripDTO.setTripInfo(getTripInfo(authDTO, tripDTO));
		}

		return tripChartDTO;
	}

	public void UpdateTripContact(AuthDTO authDTO, TripDTO tripDTO) {
		TRIP_INFO_LOGGER.info("UpdateTripContact - {} {}", authDTO.getNamespaceCode(), authDTO.getUser().getUsername(), tripDTO.getCode());

		tripDTO = getTrip(authDTO, tripDTO);

		StageStationDTO originStageStation = getTripStartCloseTime(authDTO, tripDTO);

		if (tripDTO.getTripInfo().getPrimaryDriver() != null && StringUtil.isNotNull(tripDTO.getTripInfo().getPrimaryDriver().getCode())) {
			tripDTO.getTripInfo().setPrimaryDriver(getVehicleDriverDTO(authDTO, tripDTO.getTripInfo().getPrimaryDriver()));
		}
		if (tripDTO.getTripInfo().getSecondaryDriver() != null && StringUtil.isNotNull(tripDTO.getTripInfo().getSecondaryDriver().getCode())) {
			tripDTO.getTripInfo().setSecondaryDriver(getVehicleDriverDTO(authDTO, tripDTO.getTripInfo().getSecondaryDriver()));
		}
		if (tripDTO.getTripInfo().getAttendant() != null && StringUtil.isNotNull(tripDTO.getTripInfo().getAttendant().getCode())) {
			tripDTO.getTripInfo().setAttendant(getVehicleAttendantDTO(authDTO, tripDTO.getTripInfo().getAttendant()));
		}
		if (tripDTO.getTripInfo().getCaptain() != null && StringUtil.isNotNull(tripDTO.getTripInfo().getCaptain().getCode())) {
			tripDTO.getTripInfo().setCaptain(getVehicleAttendantDTO(authDTO, tripDTO.getTripInfo().getCaptain()));
		}

		ScheduleDTO schedule = scheduleService.getSchedule(authDTO, tripDTO.getSchedule());
		tripDTO.getTripInfo().setScheduleTagList(schedule.getScheduleTagList());

		ScheduleBusDTO scheduleBus = scheduleBusService.getActiveScheduleBus(authDTO, schedule);
		breakevenService.processBreakevenToTripBreakeven(authDTO, scheduleBus, tripDTO, originStageStation.getStation());

		String extras = composeExtras(authDTO, tripDTO);
		tripDTO.getTripInfo().setExtras(extras);

		TripDAO tripDAO = new TripDAO();
		tripDAO.UpdateTripInfo(authDTO, tripDTO);
		DateTime updatedAt = DateUtil.NOW();
		// Reset Trip Info Cache
		TripCache tripCache = new TripCache();
		tripCache.removeTripInfo(authDTO, tripDTO);

		if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getBusVehicle() != null && StringUtil.isNotNull(tripDTO.getTripInfo().getBusVehicle().getCode())) {

			TripChartDTO tripChartDTO = getTripChart(authDTO, tripDTO);

			pushTripInfoService.updateTripInfo(authDTO, tripChartDTO);

			/** Update Vehicle Last Assigned Trip Date */
			tripDTO.getTripInfo().getBusVehicle().setLastAssignedDate(tripDTO.getTripDate());
			vehicleService.updateLastAssignedDate(authDTO, tripDTO.getTripInfo().getBusVehicle());

			/**
			 * Update Vehicle Primary/Secondary Driver Last Assigned Trip Date
			 */
			if (tripDTO.getTripInfo().getPrimaryDriverId() != 0) {
				tripDTO.getTripInfo().getPrimaryDriver().setLastAssignedDate(tripDTO.getTripDate());
				driverService.updateLastAssignedDate(authDTO, tripDTO.getTripInfo().getPrimaryDriver());
			}
			if (tripDTO.getTripInfo().getSecondaryDriverId() != 0) {
				tripDTO.getTripInfo().getSecondaryDriver().setLastAssignedDate(tripDTO.getTripDate());
				driverService.updateLastAssignedDate(authDTO, tripDTO.getTripInfo().getSecondaryDriver());
			}
		}
		else if (tripDTO.getTripInfo() == null || tripDTO.getTripInfo().getBusVehicle() == null || StringUtil.isNull(tripDTO.getTripInfo().getBusVehicle().getCode())) {
			pushTripInfoService.removeTripInfo(authDTO, tripDTO);
		}
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.VEHICLE_ASSIGNED)) {
			notificationPushService.pushVehicleAssignedNotification(authDTO, tripDTO, updatedAt);
		}
	}

	private StageStationDTO getTripStartCloseTime(AuthDTO authDTO, TripDTO tripDTO) {
		List<StageStationDTO> stageList = getScheduleTripStage(authDTO, tripDTO);
		DateTime destinationStationDateTime = BitsUtil.getDestinationStationTime(stageList, tripDTO.getTripDate());
		tripDTO.getTripInfo().setTripCloseDateTime(destinationStationDateTime);

		StageStationDTO originStageStation = BitsUtil.getOriginStageStation(stageList);

		DateTime startingStationDateTime = BitsUtil.getOriginStationPointDateTime(stageList, tripDTO.getTripDate());
		tripDTO.getTripInfo().setTripStartDateTime(startingStationDateTime);
		return originStageStation;
	}

	public void tripNotification(AuthDTO authDTO, TripDTO tripDTO) {
		String busNo = null;
		String contact = null;
		TripChartDTO tripChartDTO = new TripChartDTO();
		TicketDAO dao = new TicketDAO();
		tripChartDTO = dao.getTicketForTripChart(authDTO, tripDTO);
		for (TripChartDetailsDTO dto : tripChartDTO.getTicketDetailsList()) {
			smsService.sendTripNotification(authDTO, dto, busNo, contact);
		}
	}

	public TripDTO getTripDTO(AuthDTO authDTO, TripDTO tripDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.getTripDTO(authDTO, tripDTO);
		if (tripDTO == null || tripDTO.getId() == 0 || tripDTO.getSchedule() == null || tripDTO.getSchedule().getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		return tripDTO;
	}

	public TripDTO getTripDTOwithScheduleDetails(AuthDTO authDTO, TripDTO tripDTO) {
		TripDTO trip = getTrip(authDTO, tripDTO);
		if (trip == null || trip.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}

		if (trip.getSchedule() != null && trip.getSchedule().getId() != 0) {
			ScheduleCache cache = new ScheduleCache();
			trip.setSchedule(cache.getScheduleDTObyId(authDTO, trip.getSchedule()));
		}
		trip.setBus(getBusDTObyId(authDTO, trip.getBus()));
		return trip;
	}

	public TripInfoDTO getAllTripContact(AuthDTO authDTO) {
		TripDAO tripDAO = new TripDAO();
		return tripDAO.getAllTripContact(authDTO);
	}

	@Override
	public List<BusVehicleDriverDTO> getTripDrivers(AuthDTO authDTO) {
		List<BusVehicleDriverDTO> finalDrivers = new ArrayList<>();
		List<BusVehicleDriverDTO> drivers = driverService.getAll(authDTO);

		TripDAO tripDAO = new TripDAO();
		TripInfoDTO tripInfoDTO = tripDAO.getAllTripContact(authDTO);

		for (BusVehicleDriverDTO driverDTO : drivers) {
			boolean driverExist = Text.FALSE;

			for (TripInfoDTO tripInfo : tripInfoDTO.getList()) {
				if (StringUtil.isNull(tripInfo.getDriverName())) {
					continue;
				}
				double similarity = StringUtil.similarity(driverDTO.getName(), StringUtil.removeUnknownSymbol(tripInfo.getDriverName()));

				if (similarity < 0.5) {
					continue;
				}
				driverDTO.setMobileNumber(tripInfo.getDriverMobile());
				finalDrivers.add(driverDTO);
				driverExist = Text.TRUE;
			}

			if (!driverExist) {
				driverDTO.setMobileNumber(Text.NA);
				finalDrivers.add(driverDTO);
			}
		}
		return finalDrivers;
	}

	@Override
	public List<BusVehicleAttendantDTO> getTripAttenders(AuthDTO authDTO) {
		List<BusVehicleAttendantDTO> finalAttendants = new ArrayList<>();
		List<BusVehicleAttendantDTO> attendants = driverService.getAllAttendant(authDTO, AttendantCategoryEM.ATTENDANT);

		TripDAO tripDAO = new TripDAO();
		TripInfoDTO tripInfoDTO = tripDAO.getAllTripContact(authDTO);

		for (BusVehicleAttendantDTO attendantDTO : attendants) {
			boolean attendantExist = Text.FALSE;

			for (TripInfoDTO tripInfo : tripInfoDTO.getList()) {
				if (StringUtil.isNull(tripInfo.getDriverName())) {
					continue;
				}
				double similarity = StringUtil.similarity(attendantDTO.getName(), StringUtil.removeUnknownSymbol(tripInfo.getDriverName()));

				if (similarity < 0.5) {
					continue;
				}
				attendantDTO.setMobile(tripInfo.getDriverMobile());
				finalAttendants.add(attendantDTO);
				attendantExist = Text.TRUE;
			}

			if (!attendantExist) {
				attendantDTO.setMobile(Text.NA);
				finalAttendants.add(attendantDTO);
			}
		}
		return finalAttendants;
	}

	public TripInfoDTO getTripInfo(AuthDTO authDTO, TripDTO tripDTO) {
		TripCache tripCache = new TripCache();
		TripInfoDTO infoDTO = tripCache.getTripInfo(authDTO, tripDTO);
		if (infoDTO != null) {
			if (infoDTO.getBusVehicle() != null && infoDTO.getBusVehicle().getId() != 0) {
				infoDTO.setBusVehicle(vehicleService.getBusVehicles(authDTO, infoDTO.getBusVehicle()));
			}
			if (infoDTO.getPrimaryDriverId() != 0) {
				infoDTO.setPrimaryDriver(getVehicleDriverDTOById(authDTO, infoDTO.getPrimaryDriver()));
			}
			if (infoDTO.getSecondaryDriverId() != 0) {
				infoDTO.setSecondaryDriver(getVehicleDriverDTOById(authDTO, infoDTO.getSecondaryDriver()));
			}
			if (infoDTO.getAttendantId() != 0) {
				infoDTO.setAttendant(getVehicleAttendantDTOById(authDTO, infoDTO.getAttendant()));
			}
			if (infoDTO.getCaptainId() != 0) {
				infoDTO.setCaptain(getVehicleAttendantDTOById(authDTO, infoDTO.getCaptain()));
			}
		}
		return infoDTO;
	}

	public List<TripDTO> getTripInfoByTripDate(AuthDTO authDTO, DateTime tripDate) {
		TripDAO tripDAO = new TripDAO();
		List<TripDTO> list = tripDAO.getTripInfoDTOByTripDate(authDTO, tripDate);
		for (TripDTO tripDTO : list) {
			if (tripDTO != null && tripDTO.getTripInfo().getBusVehicle() != null && tripDTO.getTripInfo().getBusVehicle().getId() != 0) {
				tripDTO.getTripInfo().setBusVehicle(vehicleService.getBusVehicles(authDTO, tripDTO.getTripInfo().getBusVehicle()));
			}
			if (tripDTO.getTripInfo().getPrimaryDriverId() != 0) {
				tripDTO.getTripInfo().setPrimaryDriver(getVehicleDriverDTOById(authDTO, tripDTO.getTripInfo().getPrimaryDriver()));
			}
			if (tripDTO.getTripInfo().getSecondaryDriverId() != 0) {
				tripDTO.getTripInfo().setSecondaryDriver(getVehicleDriverDTOById(authDTO, tripDTO.getTripInfo().getSecondaryDriver()));
			}
			if (tripDTO.getTripInfo().getAttendantId() != 0) {
				tripDTO.getTripInfo().setAttendant(getVehicleAttendantDTOById(authDTO, tripDTO.getTripInfo().getAttendant()));
			}
			if (tripDTO.getTripInfo().getCaptainId() != 0) {
				tripDTO.getTripInfo().setCaptain(getVehicleAttendantDTOById(authDTO, tripDTO.getTripInfo().getCaptain()));
			}
		}
		return list;
	}

	public void updateTripJobStatus(AuthDTO authDTO, TripDTO tripDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripJobStatus(authDTO, tripDTO);

		TripCache tripCache = new TripCache();
		tripCache.removeTripInfo(authDTO, tripDTO);
	}

	public void updateTripTransferSeatDetails(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripTransferSeatDetails(authDTO, ticketDTO, connection);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	public TripDTO getTripStageDetails(AuthDTO authDTO, TripDTO tripStageDTO) {
		TripCache tripCache = new TripCache();
		TripDTO tripDTO = tripCache.getTripDTO(authDTO, tripStageDTO);
		if (tripDTO == null || tripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		if (tripStageDTO.getSearch() == null || tripStageDTO.getSearch().getFromStation() == null || tripStageDTO.getSearch().getToStation() == null || StringUtil.isNull(tripStageDTO.getSearch().getFromStation().getCode()) || StringUtil.isNull(tripStageDTO.getSearch().getToStation().getCode())) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_STAGE_CODE);
		}

		StageDTO stageDTO = new StageDTO();
		stageDTO.setTravelDate(tripStageDTO.getSearch().getTravelDate());
		StageStationDTO fromStageStationDTO = new StageStationDTO();
		fromStageStationDTO.setStation(stationService.getStation(tripStageDTO.getSearch().getFromStation()));
		StageStationDTO toStageStationDTO = new StageStationDTO();
		toStageStationDTO.setStation(stationService.getStation(tripStageDTO.getSearch().getToStation()));
		stageDTO.setFromStation(fromStageStationDTO);
		stageDTO.setToStation(toStageStationDTO);
		tripDTO.setStage(stageDTO);

		return tripDTO;

	}

	public void clearBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO) {
		cacheService.clearBookedBlockedSeatsCache(authDTO, tripDTO);

		// Update Trip Sync Time
		TripCache tripCache = new TripCache();
		tripCache.updateTripSyncTime(authDTO, tripDTO);
	}

	public List<TripDTO> getTripWiseBookedSeatCount(AuthDTO authDTO, UserDTO userDTO, ExtraCommissionDTO extraCommissionDTO) {
		TripDAO tripDAO = new TripDAO();
		return tripDAO.getTripWiseBookedSeatCount(authDTO, userDTO, extraCommissionDTO);
	}

	public List<TripDTO> getTripWiseBookedSeatCountV2(AuthDTO authDTO, UserDTO userDTO, DateTime fromDate, DateTime toDate, TicketStatusEM ticketStatus) {
		TripDAO tripDAO = new TripDAO();
		return tripDAO.getTripWiseBookedSeatCountV2(authDTO, userDTO, fromDate, toDate, ticketStatus);
	}

	@Override
	public void checkTripChartWithLatest(AuthDTO authDTO, TripDTO tripDTO, DateTime versionDatetime) {
		TripCache tripCache = new TripCache();
		tripDTO = tripCache.getTripDTO(authDTO, tripDTO);
		if (tripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		List<TicketDetailsDTO> list = getBookedBlockedSeats(authDTO, tripDTO);
		boolean foundNew = false;
		for (TicketDetailsDTO detailsDTO : list) {
			if (detailsDTO.getUpdatedAt().compareTo(versionDatetime) == 1) {
				foundNew = true;
				break;
			}
		}
		if (!foundNew) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
	}

	@Override
	public void checkRecentTickets(AuthDTO authDTO, TripDTO tripDTO, String syncTime) {
		TripCache tripCache = new TripCache();
		tripDTO = tripCache.getTripDTO(authDTO, tripDTO);
		if (tripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		if (StringUtil.isNotNull(tripDTO.getSyncTime()) && !syncTime.equals(tripDTO.getSyncTime())) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
	}

	@Override
	public void SaveTripHistory(AuthDTO authDTO, TripDTO tripDTO, String actionName, String logData) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.addAuditLog(authDTO, tripDTO.getCode(), "trip", actionName, logData);
	}

	@Override
	public void updateTripSeatDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDAO tripDAO = new TripDAO();
		tripDAO.updateTripSeatDetailsV2(authDTO, ticketDTO);

		// Clear Trip block Seats
		clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
	}

	@Override
	public List<StageStationDTO> getScheduleTripStage(AuthDTO authDTO, TripDTO tripDTO) {

		ScheduleCache scheduleCache = new ScheduleCache();

		ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
		scheduleDTO.setTripDate(tripDTO.getTripDate());
		tripDTO.setSchedule(scheduleDTO);

		List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverrideService.getByScheduleId(authDTO, tripDTO.getSchedule());

		// Stations
		List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, tripDTO.getSchedule());

		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
		for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
			ScheduleStationDTO stationDTO = iterator.next();
			// Exception and override
			for (Iterator<ScheduleStationDTO> OverrideIterator = stationDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
				ScheduleStationDTO overrideStationDTO = OverrideIterator.next();
				// common validations
				if (StringUtil.isNotNull(overrideStationDTO.getActiveFrom()) && !tripDTO.getTripDate().gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
					OverrideIterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(overrideStationDTO.getActiveTo()) && !tripDTO.getTripDate().lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
					OverrideIterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(overrideStationDTO.getDayOfWeek()) && overrideStationDTO.getDayOfWeek().length() != 7) {
					OverrideIterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(overrideStationDTO.getDayOfWeek()) && overrideStationDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
					OverrideIterator.remove();
					continue;
				}
				// Remove if Exceptions
				if (overrideStationDTO.getMinitues() == -1) {
					iterator.remove();
					break;
				}
				// Override, time should follow in same day
				stationDTO.setMinitues(overrideStationDTO.getMinitues());
			}

			stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
			stationMap.put(stationDTO.getStation().getId(), stationDTO);
		}

		// Station time override
		for (ScheduleTimeOverrideDTO overrideDTO : timeOverridelist) {
			ScheduleStationDTO reactionStationDTO = null;
			for (ScheduleStationDTO stationDTO : stationList) {
				if (overrideDTO.getStation().getId() == stationDTO.getStation().getId()) {
					stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));

					if (overrideDTO.isReactionFlag()) {
						reactionStationDTO = stationDTO;
					}
				}
			}
			for (ScheduleStationDTO stationDTO : stationList) {
				if (reactionStationDTO != null && stationDTO.getStationSequence() > reactionStationDTO.getStationSequence()) {
					stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));
				}
			}
		}

		// Schedule Station Point
		List<ScheduleStationPointDTO> stationPointList = scheduleCache.getScheduleStationPointDTO(authDTO, tripDTO.getSchedule());

		// Validate all stations Point
		for (Iterator<ScheduleStationPointDTO> iterator = stationPointList.iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO stationPointDTO = iterator.next();
			if (stationMap.get(stationPointDTO.getStation().getId()) == null) {
				iterator.remove();
				continue;
			}
			// common validations
			if (stationPointDTO.getActiveFrom() != null && !tripDTO.getTripDate().gteq(new DateTime(stationPointDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getActiveTo() != null && !tripDTO.getTripDate().lteq(new DateTime(stationPointDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getDayOfWeek() != null && stationPointDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (stationPointDTO.getDayOfWeek() != null && stationPointDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Exception and Override
			for (Iterator<ScheduleStationPointDTO> OverrideIterator = stationPointDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
				ScheduleStationPointDTO overrideScheduleStationPonitDTO = OverrideIterator.next();
				// common validations
				if (overrideScheduleStationPonitDTO.getActiveFrom() != null && !tripDTO.getTripDate().gteq(new DateTime(overrideScheduleStationPonitDTO.getActiveFrom()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getActiveTo() != null && !tripDTO.getTripDate().lteq(new DateTime(overrideScheduleStationPonitDTO.getActiveTo()))) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getDayOfWeek() != null && overrideScheduleStationPonitDTO.getDayOfWeek().length() != 7) {
					OverrideIterator.remove();
					continue;
				}
				if (overrideScheduleStationPonitDTO.getDayOfWeek() != null && overrideScheduleStationPonitDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
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
		Map<Integer, StageStationDTO> stageStationMap = new HashMap<>();

		for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
			ScheduleStationDTO stationDTO = iterator.next();
			StageStationDTO stageStationDTO = new StageStationDTO();
			stageStationDTO.setMinitues(stationDTO.getMinitues());
			stageStationDTO.setStationSequence(stationDTO.getStationSequence());
			stageStationDTO.setStation(stationDTO.getStation());
			stageStationDTO.setMobileNumber(stationDTO.getMobileNumber());
			stageStationMap.put(stationDTO.getStation().getId(), stageStationDTO);
		}
		for (Iterator<ScheduleStationPointDTO> iterator = stationPointList.iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO pointDTO = iterator.next();
			if (stationMap.get(pointDTO.getStation().getId()) != null && stageStationMap.get(pointDTO.getStation().getId()) != null) {
				StageStationDTO stageStationDTO = stageStationMap.get(pointDTO.getStation().getId());
				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setId(pointDTO.getStationPoint().getId());

				// Copy station Point from cache
				getStationPointDTObyId(authDTO, stationPointDTO);

				stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
				stationPointDTO.setMinitues(pointDTO.getMinitues());
				stationPointDTO.setBusVehicleVanPickup(pointDTO.getBusVehicleVanPickup());
				stageStationDTO.getStationPoint().add(stationPointDTO);
				stageStationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
			}
			else {
				iterator.remove();
				continue;
			}
		}
		return new ArrayList<StageStationDTO>(stageStationMap.values());
	}

	private int getStationTimeOverride(ScheduleTimeOverrideDTO timeOverrideDTO, int stationMinitues) {
		int finalStationMinitues = stationMinitues;
		if (timeOverrideDTO.getOverrideType().getId() == OverrideTypeEM.DECREASE_VALUE.getId()) {
			finalStationMinitues = stationMinitues - timeOverrideDTO.getOverrideMinutes();
		}
		else if (timeOverrideDTO.getOverrideType().getId() == OverrideTypeEM.INCREASE_VALUE.getId()) {
			finalStationMinitues = stationMinitues + timeOverrideDTO.getOverrideMinutes();
		}
		return finalStationMinitues;
	}

	@Override
	public JSONArray getActiveTripBusVehicles(AuthDTO authDTO, DateTime tripDate) {
		List<BusVehicleDTO> busVehicleList = vehicleService.getActiveBusVehicles(authDTO);
		TripDAO tripDAO = new TripDAO();
		JSONArray busVehicles = new JSONArray();

		for (BusVehicleDTO busVehicleDTO : busVehicleList) {
			JSONObject busVehicleJSON = new JSONObject();
			busVehicleJSON.put("code", busVehicleDTO.getCode());
			busVehicleJSON.put("name", busVehicleDTO.getName());
			busVehicleJSON.put("registrationNumber", busVehicleDTO.getRegistationNumber());

			JSONObject gpsDeviceVendorJSON = new JSONObject();
			gpsDeviceVendorJSON.put("code", busVehicleDTO.getDeviceVendor() != null && StringUtil.isNotNull(busVehicleDTO.getDeviceVendor().getCode()) ? busVehicleDTO.getDeviceVendor().getCode() : Text.EMPTY);
			gpsDeviceVendorJSON.put("name", busVehicleDTO.getDeviceVendor() != null && StringUtil.isNotNull(busVehicleDTO.getDeviceVendor().getName()) ? busVehicleDTO.getDeviceVendor().getName() : Text.EMPTY);
			busVehicleJSON.put("gpsDeviceVendor", gpsDeviceVendorJSON);

			List<TripDTO> list = tripDAO.getTripByTripDateAndVehicle(authDTO, tripDate, busVehicleDTO);
			JSONArray trips = new JSONArray();
			if (list != null && !list.isEmpty()) {
				for (TripDTO tripDTO : list) {
					JSONObject tripJSON = new JSONObject();
					tripJSON.put("code", tripDTO.getCode());
					tripJSON.put("tripDate", tripDTO.getTripDateTimeV2().format(Text.DATE_TIME_DATE4J));
					tripJSON.put("tripStatus", tripDTO.getTripStatus() != null ? tripDTO.getTripStatus().getName() : Text.EMPTY);
					tripJSON.put("tripDate", tripDTO.getTripInfo().getTripCloseDateTime() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);
					trips.add(tripJSON);
				}
			}
			busVehicleJSON.put("trips", trips);

			busVehicles.add(busVehicleJSON);
		}
		return busVehicles;
	}

	@Override
	public JSONArray getPreferredVehicles(AuthDTO authDTO, TripDTO tripDTO) {
		JSONArray busVehicles = new JSONArray();
		try {
			TripCache tripCache = new TripCache();
			tripCache.getTripDTO(authDTO, tripDTO);

			TripDAO tripDAO = new TripDAO();
			List<BusVehicleDTO> busVehicleList = tripDAO.getTripInfoBySchedule(authDTO, tripDTO);

			Map<String, BusVehicleDTO> busVehicleMap = new HashMap<>();
			for (BusVehicleDTO busVehicleDTO : busVehicleList) {
				if (busVehicleDTO.getDeviceVendor() != null && GPSDeviceVendorEM.EZEEGPS.getId() != busVehicleDTO.getDeviceVendor().getId()) {
					continue;
				}
				// if (tripDTO.getBus().getId() !=
				// busVehicleDTO.getBus().getId()) {
				// continue;
				// }

				GPSLocationDTO gpsLocationDTO = trackbusService.getVehicleLocation(authDTO.getNamespaceCode(), GPSDeviceVendorEM.EZEEGPS, busVehicleDTO.getGpsDeviceCode(), busVehicleDTO.getRegistationNumber());

				JSONObject busVehicleJSON = new JSONObject();
				busVehicleJSON.put("code", busVehicleDTO.getCode());
				busVehicleJSON.put("name", busVehicleDTO.getName());
				busVehicleJSON.put("registrationDate", busVehicleDTO.getRegistrationDate());
				busVehicleJSON.put("registrationNumber", busVehicleDTO.getRegistationNumber());
				busVehicleJSON.put("licNumber", busVehicleDTO.getLicNumber());
				busVehicleJSON.put("mobileNumber", busVehicleDTO.getMobileNumber());
				busVehicleJSON.put("gpsDeviceCode", busVehicleDTO.getGpsDeviceCode());

				JSONObject vehicleTypeJSON = new JSONObject();
				vehicleTypeJSON.put("code", busVehicleDTO.getVehicleType().getCode());
				vehicleTypeJSON.put("name", busVehicleDTO.getVehicleType().getName());
				busVehicleJSON.put("vehicleType", vehicleTypeJSON);

				JSONObject gpsDeviceVendorJSON = new JSONObject();
				gpsDeviceVendorJSON.put("code", busVehicleDTO.getDeviceVendor().getCode());
				gpsDeviceVendorJSON.put("name", busVehicleDTO.getDeviceVendor().getName());
				busVehicleJSON.put("gpsDeviceVendor", gpsDeviceVendorJSON);

				JSONObject gpsLocationJSON = new JSONObject();
				if (gpsLocationDTO != null) {
					gpsLocationJSON.put("latitude", gpsLocationDTO.getLatitude());
					gpsLocationJSON.put("longitude", gpsLocationDTO.getLongitude());
					gpsLocationJSON.put("speed", gpsLocationDTO.getSpeed());
					gpsLocationJSON.put("address", StringUtil.isNotNull(gpsLocationDTO.getAddress()) && gpsLocationDTO.getAddress().split(Text.COMMA).length >= 3 ? gpsLocationDTO.getAddress().split(Text.COMMA)[2] : gpsLocationDTO.getAddress());
					gpsLocationJSON.put("ignition", gpsLocationDTO.isIgnition() ? 1 : 0);
					gpsLocationJSON.put("updatedTime", gpsLocationDTO.getUpdatedTime());
				}
				busVehicleJSON.put("gpsLocation", gpsLocationJSON);

				busVehicleMap.put(busVehicleDTO.getCode(), busVehicleDTO);
				busVehicles.add(busVehicleJSON);
			}

			if (DateUtil.NOW().getStartOfDay().compareTo(tripDTO.getTripDate().getStartOfDay()) == 0) {
				List<BusVehicleDTO> busVehiclesList = vehicleService.getActiveBusVehicles(authDTO);
				for (BusVehicleDTO busVehicleDTO : busVehiclesList) {
					if (busVehicleMap.get(busVehicleDTO.getCode()) != null) {
						continue;
					}
					if (tripDTO.getBus().getId() != busVehicleDTO.getBus().getId()) {
						continue;
					}

					JSONObject busVehicleJSON = new JSONObject();
					busVehicleJSON.put("code", busVehicleDTO.getCode());
					busVehicleJSON.put("name", busVehicleDTO.getName());
					busVehicleJSON.put("registrationDate", busVehicleDTO.getRegistrationDate());
					busVehicleJSON.put("registrationNumber", busVehicleDTO.getRegistationNumber());
					busVehicleJSON.put("licNumber", busVehicleDTO.getLicNumber());
					busVehicleJSON.put("mobileNumber", busVehicleDTO.getMobileNumber());
					busVehicleJSON.put("gpsDeviceCode", busVehicleDTO.getGpsDeviceCode());

					JSONObject gpsDeviceVendorJSON = new JSONObject();
					gpsDeviceVendorJSON.put("code", busVehicleDTO.getDeviceVendor().getCode());
					gpsDeviceVendorJSON.put("name", busVehicleDTO.getDeviceVendor().getName());
					busVehicleJSON.put("gpsDeviceVendor", gpsDeviceVendorJSON);

					busVehicleJSON.put("gpsLocation", new JSONObject());
					busVehicles.add(busVehicleJSON);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return busVehicles;
	}

	@Override
	public TripDTO getTrip(AuthDTO authDTO, TripDTO tripDTO) {
		TripCache tripCache = new TripCache();
		tripCache.getTripDTO(authDTO, tripDTO);

		if (tripDTO == null || tripDTO.getId() == 0 || tripDTO.getSchedule() == null || tripDTO.getSchedule().getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		return tripDTO;
	}

	@Override
	public List<TripDTO> getTripsByTripDate(AuthDTO authDTO, DateTime tripDate) {
		TripDAO tripDAO = new TripDAO();
		return tripDAO.getTripsByTripDate(authDTO, tripDate);
	}

	@Override
	public List<String> getRelatedStageCodes(AuthDTO authDTO, TripDTO tripDTO, StationDTO fromStation, StationDTO toStation) {
		ScheduleCache scheduleCache = new ScheduleCache();
		tripDTO.setSchedule(scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule()));

		// Station
		List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDTO.getTripDate());

		// Validate all stations
		Map<Integer, StageStationDTO> stationMap = new HashMap<>();
		for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
			ScheduleStationDTO stationDTO = iterator.next();
			StageStationDTO stageStationDTO = new StageStationDTO();
			stageStationDTO.setMinitues(stationDTO.getMinitues());
			stageStationDTO.setStationSequence(stationDTO.getStationSequence());
			stageStationDTO.setStation(stationDTO.getStation());
			stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
		}

		// collect other stages list to check blocked/booked seat status
		List<ScheduleStageDTO> otherScheduleStageList = scheduleStageService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDTO.getTripDate());

		// other stage Filter, identify other stages
		for (Iterator<ScheduleStageDTO> itrStageDTO = otherScheduleStageList.iterator(); itrStageDTO.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = itrStageDTO.next();
			if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
				scheduleStageDTO.setFromStationSequence(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence());
				scheduleStageDTO.setToStationSequence(stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence());
			}
			else {
				itrStageDTO.remove();
			}
		}

		// Identify co-releated schedules stage,
		int fromStationSquence = stationMap.get(fromStation.getId()).getStationSequence();
		int toStationSquence = stationMap.get(toStation.getId()).getStationSequence();

		List<String> releatedStageCodeList = new ArrayList<>();
		for (Iterator<ScheduleStageDTO> itrStageDTO = otherScheduleStageList.iterator(); itrStageDTO.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = itrStageDTO.next();
			if (scheduleStageDTO.getToStationSequence() <= fromStationSquence) {
				itrStageDTO.remove();
				continue;
			}
			if (scheduleStageDTO.getFromStationSequence() >= toStationSquence) {
				itrStageDTO.remove();
				continue;
			}
			String stageCode = BitsUtil.getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, scheduleStageDTO);
			if (!releatedStageCodeList.contains(stageCode)) {
				releatedStageCodeList.add(stageCode);
			}
		}

		return releatedStageCodeList;
	}

	@Override
	public void updateTripExtras(AuthDTO authDTO, Map<String, String> additionalAttribute) {
		TripDTO trip = new TripDTO();
		trip.setCode(additionalAttribute.get("tripCode"));
		trip = getTrip(authDTO, trip);

		TripDAO tripDAO = new TripDAO();
		String dateTime = DateUtil.convertDateTime(DateUtil.NOW());
		additionalAttribute.put("dateTime", dateTime);

		StringBuilder extras = new StringBuilder();
		if ("START".equals(additionalAttribute.get("actionType"))) {
			extras.append(additionalAttribute.get("startOdometer")).append(Text.VERTICAL_BAR).append(dateTime).append("||");
		}
		else if ("END".equals(additionalAttribute.get("actionType"))) {
			tripDAO.getTripInfoDTO(authDTO, trip);

			if (trip.getTripInfo() == null) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL, "Trip Info is not updated!");
			}

			extras.append(StringUtil.split(trip.getTripInfo().getExtras(), Text.VERTICAL_BAR, 0)).append(Text.VERTICAL_BAR).append(StringUtil.split(trip.getTripInfo().getExtras(), Text.VERTICAL_BAR, 1)).append(Text.VERTICAL_BAR);
			extras.append(additionalAttribute.get("endOdometer")).append(Text.VERTICAL_BAR).append(dateTime).append(Text.VERTICAL_BAR);
		}

		tripDAO.updateTripExtras(authDTO, trip, extras.toString());

		// Reset Trip Info Cache
		TripCache tripCache = new TripCache();
		tripCache.removeTripInfo(authDTO, trip);

		// Update Odometer in Costiv
		costivService.updateTripOdometer(authDTO, additionalAttribute);
	}

	@Override
	public void saveTripIncomeExpense(AuthDTO authDTO, TripDTO tripDTO, Map<String, String> transactionDetails) {
		getTripInfo(authDTO, tripDTO);

		if (tripDTO.getTripInfo() == null || tripDTO.getTripInfo().getPrimaryDriverId() == 0) {
			throw new ServiceException(ErrorCode.TRIP_INFO_INVALID);
		}

		if ("FUELEXPENSE".equals(transactionDetails.get("expenseType"))) {
			costivService.saveFuelExpense(authDTO, tripDTO, transactionDetails);
		}
		else {
			costivService.saveTripIncomeExpense(authDTO, tripDTO, transactionDetails);
		}
	}

	public List<Map<String, Object>> getBreakevenDetails(AuthDTO authDTO, DateTime fromDate, DateTime toDate, ScheduleDTO scheduleDTO, BusVehicleDTO vehicleDTO) {
		List<Map<String, Object>> breakevenDetailList = new ArrayList<Map<String, Object>>();
		if (scheduleDTO != null && StringUtil.isNotNull(scheduleDTO.getCode())) {
			scheduleService.getSchedule(authDTO, scheduleDTO);
		}
		if (vehicleDTO != null && StringUtil.isNotNull(vehicleDTO.getCode())) {
			vehicleService.getBusVehicles(authDTO, vehicleDTO);
		}
		List<TripDTO> list = new ArrayList<TripDTO>();
		TripDAO tripDAO = new TripDAO();
		if (fromDate.gteq(DateUtil.getDateTime("2020-11-25 00:00:00")) || toDate.gteq(DateUtil.getDateTime("2020-11-25 00:00:00"))) {
			List<TripDTO> tripList = tripDAO.getTripBreakevenDetails(authDTO, fromDate, toDate, scheduleDTO, vehicleDTO);
			list.addAll(tripList);
		}
		if (fromDate.lt(DateUtil.getDateTime("2020-11-25 00:00:00")) || toDate.lt(DateUtil.getDateTime("2020-11-25 00:00:00"))) {
			List<TripDTO> tripList = tripDAO.getOldBreakevenDetails(authDTO, fromDate, toDate, scheduleDTO, vehicleDTO);
			list.addAll(tripList);
		}
		Map<String, TripDTO> tripMap = Maps.newHashMap();

		Map<String, String> fixedExpenseSubMap = Maps.newHashMap();
		Map<String, String> floatingExpenseSubMap = Maps.newHashMap();
		for (TripDTO tripDTO : list) {
			if (tripMap.get(tripDTO.getCode()) != null) {
				continue;
			}

			tripDTO.setSchedule(scheduleService.getSchedule(authDTO, tripDTO.getSchedule()));
			tripDTO.setBus(getBusDTObyId(authDTO, tripDTO.getBus()));
			tripDTO.getTripInfo().setBusVehicle(vehicleService.getBusVehicles(authDTO, tripDTO.getTripInfo().getBusVehicle()));

			Map<String, String> uniqueSeatMap = new HashMap<>();
			Map<String, String> uniqueCancelSeatMap = new HashMap<>();
			BigDecimal netRevenue = BigDecimal.ZERO;
			List<TicketDetailsDTO> ticketDetailList = getBookedBlockedSeats(authDTO, tripDTO);
			for (TicketDetailsDTO ticketDetails : ticketDetailList) {
				if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					uniqueSeatMap.put(ticketDetails.getSeatCode() + ticketDetails.getTicketCode(), ticketDetails.getSeatCode());
				}
				else if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
					uniqueCancelSeatMap.put(ticketDetails.getSeatCode() + ticketDetails.getTicketCode(), ticketDetails.getSeatCode());
				}
				netRevenue = netRevenue.add(ticketDetails.getNetAmount());
			}
			int bookedSeatCount = uniqueSeatMap.size();
			int cancelledSeatCount = uniqueCancelSeatMap.size();

			JSONObject json = tripDTO.getBreakeven();
			JSONObject detailsJson = json.getJSONObject("details");

			Map<String, Object> breakevenDetailMap = Maps.newHashMap();
			breakevenDetailMap.put("trip_code", tripDTO.getCode());
			breakevenDetailMap.put("trip_date", DateUtil.convertDate(tripDTO.getTripDate()));
			breakevenDetailMap.put("schedule_code", tripDTO.getSchedule().getCode());
			breakevenDetailMap.put("schedule_name", tripDTO.getSchedule().getName());
			breakevenDetailMap.put("service_number", tripDTO.getSchedule().getServiceNumber());
			breakevenDetailMap.put("registration_number", tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			breakevenDetailMap.put("net_revenue", netRevenue);
			breakevenDetailMap.put("booked_seat_count", bookedSeatCount);

			float distance = Float.parseFloat(json.getString("distance"));
			float mileage = detailsJson.has("mileage") ? Float.parseFloat(detailsJson.getString("mileage")) : 0;
			BigDecimal perLitreCost = StringUtil.getBigDecimalValue(json.getString("fuel"));

			BigDecimal fuelExpense = BigDecimal.ZERO;
			if (distance > 0) {
				float avgDistance = distance / mileage;
				fuelExpense = fuelExpense.add(StringUtil.getBigDecimalValue(String.valueOf(avgDistance)).multiply(perLitreCost));
			}

			BigDecimal seatCount = StringUtil.getBigDecimalValue(String.valueOf(tripDTO.getBus().getSeatLayoutCount()));
			Map<String, String> fixedExpenseMap = Maps.newHashMap();
			JSONArray taxJsonArray = detailsJson.has("tax") ? detailsJson.getJSONArray("tax") : new JSONArray();
			for (Object taxObject : taxJsonArray) {
				JSONObject taxJson = (JSONObject) taxObject;
				BigDecimal taxCost = BigDecimal.ZERO;
				BigDecimal value = taxJson.has(VALUE) ? StringUtil.getBigDecimalValue(taxJson.getString(VALUE)) : BigDecimal.ZERO;
				String type = taxJson.getString("type");

				if (type.equals("SEAT")) {
					taxCost = taxCost.add(value.multiply(seatCount));
				}
				else if (type.equals("BOSEAT") && bookedSeatCount > 0) {
					taxCost = taxCost.add(value.multiply(new BigDecimal(bookedSeatCount)));
				}
				else if (type.equals("KM")) {
					taxCost = taxCost.add(value.multiply(StringUtil.getBigDecimalValue(String.valueOf(distance))));
				}
				else if (type.equals("DAY")) {
					taxCost = taxCost.add(value);
				}
				if (fixedExpenseMap.get(taxJson.getString("name")) == null) {
					fixedExpenseMap.put(taxJson.getString("name"), String.valueOf(taxCost));
				}
				else {
					BigDecimal taxAmount = StringUtil.getBigDecimalValue(fixedExpenseMap.get(taxJson.getString("name")));
					taxAmount = taxAmount.add(taxCost);
					fixedExpenseMap.put(taxJson.getString("name"), String.valueOf(taxAmount.setScale(2, RoundingMode.HALF_UP)));
				}
			}

			Map<String, String> floatingExpenseMap = Maps.newHashMap();
			JSONArray expenseJsonArray = detailsJson.has("expense") ? detailsJson.getJSONArray("expense") : new JSONArray();
			for (Object expenseObject : expenseJsonArray) {
				JSONObject expenseJson = (JSONObject) expenseObject;

				BigDecimal expenseCost = BigDecimal.ZERO;
				BigDecimal value = expenseJson.has(VALUE) ? StringUtil.getBigDecimalValue(expenseJson.getString(VALUE)) : BigDecimal.ZERO;
				String type = expenseJson.getString("type");

				if (type.equals("SEAT")) {
					expenseCost = expenseCost.add(value.multiply(seatCount));
				}
				else if (type.equals("BOSEAT") && bookedSeatCount > 0) {
					expenseCost = expenseCost.add(value.multiply(new BigDecimal(bookedSeatCount)));
				}
				else if (type.equals("KM")) {
					expenseCost = expenseCost.add(value.multiply(StringUtil.getBigDecimalValue(String.valueOf(distance))));
				}
				else if (type.equals("DAY")) {
					expenseCost = expenseCost.add(value);
				}
				if (floatingExpenseMap.get(expenseJson.getString("name")) == null) {
					floatingExpenseMap.put(expenseJson.getString("name"), String.valueOf(expenseCost.setScale(2, RoundingMode.HALF_UP)));
				}
				else {
					BigDecimal expenseAmount = StringUtil.getBigDecimalValue(floatingExpenseMap.get(expenseJson.getString("name")));
					expenseAmount = expenseAmount.add(expenseCost);
					floatingExpenseMap.put(expenseJson.getString("name"), String.valueOf(expenseAmount.setScale(2, RoundingMode.HALF_UP)));
				}
			}

			breakevenDetailMap.put("seat_count", seatCount.intValue());
			breakevenDetailMap.put("fuel_price", String.valueOf(fuelExpense));
			breakevenDetailMap.put("mileage", detailsJson.getString("mileage"));
			breakevenDetailMap.put("fixed_expense", fixedExpenseMap);
			breakevenDetailMap.put("floating_expense", floatingExpenseMap);

			fixedExpenseSubMap.putAll(fixedExpenseMap);
			floatingExpenseSubMap.putAll(floatingExpenseMap);

			breakevenDetailList.add(breakevenDetailMap);
			tripMap.put(tripDTO.getCode(), tripDTO);
		}

		for (Map<String, Object> breakevenMap : breakevenDetailList) {
			Map<String, String> fixedExpenseMap = (Map<String, String>) breakevenMap.get("fixed_expense");
			Map<String, String> floatingExpenseMap = (Map<String, String>) breakevenMap.get("floating_expense");

			for (Entry<String, String> map : fixedExpenseSubMap.entrySet()) {
				if (fixedExpenseMap.get(map.getKey()) == null) {
					fixedExpenseMap.put(map.getKey(), Numeric.ZERO);
				}
			}
			for (Entry<String, String> map : floatingExpenseSubMap.entrySet()) {
				if (floatingExpenseMap.get(map.getKey()) == null) {
					floatingExpenseMap.put(map.getKey(), Numeric.ZERO);
				}
			}
		}
		return breakevenDetailList;
	}

	public JSONArray getTripBreakevenExpenses(AuthDTO authDTO, TripDTO tripDTO) {
		JSONArray breakevenExpenses = new JSONArray();
		try {
			tripDTO = getTrip(authDTO, tripDTO);
			tripDTO.setBus(getBusDTObyId(authDTO, tripDTO.getBus()));

			TripDAO tripDAO = new TripDAO();
			tripDAO.getTripBreakevenDetails(authDTO, tripDTO);

			if (tripDTO.getBreakeven() != null && tripDTO.getBreakeven().has("details") && tripDTO.getBreakeven().getJSONObject("details") != null) {
				Map<String, String> uniqueSeatMap = new HashMap<>();
				Map<String, String> uniqueCancelSeatMap = new HashMap<>();
				BigDecimal netRevenue = BigDecimal.ZERO;
				List<TicketDetailsDTO> ticketDetailList = getBookedBlockedSeats(authDTO, tripDTO);
				for (TicketDetailsDTO ticketDetails : ticketDetailList) {
					if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						uniqueSeatMap.put(ticketDetails.getSeatCode() + ticketDetails.getTicketCode(), ticketDetails.getSeatCode());
					}
					else if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						uniqueCancelSeatMap.put(ticketDetails.getSeatCode() + ticketDetails.getTicketCode(), ticketDetails.getSeatCode());
					}
					netRevenue = netRevenue.add(ticketDetails.getNetAmount());
				}
				int bookedSeatCount = uniqueSeatMap.size();

				JSONObject repoBreakeven = tripDTO.getBreakeven().getJSONObject("details");
				BigDecimal seatCount = StringUtil.getBigDecimalValue(String.valueOf(tripDTO.getBus().getSeatLayoutCount()));

				/** Tax */
				if (repoBreakeven.has(TAX) && !repoBreakeven.getJSONArray(TAX).isEmpty()) {
					for (Object taxObject : repoBreakeven.getJSONArray(TAX)) {
						JSONObject taxJson = (JSONObject) taxObject;

						BigDecimal taxCost = BigDecimal.ZERO;
						BigDecimal value = taxJson.has(VALUE) ? StringUtil.getBigDecimalValue(taxJson.getString(VALUE)) : BigDecimal.ZERO;
						String type = taxJson.getString("type");

						if (type.equals("SEAT")) {
							taxCost = taxCost.add(value.multiply(seatCount));
						}
						else if (type.equals("BOSEAT") && bookedSeatCount > 0) {
							taxCost = taxCost.add(value.multiply(new BigDecimal(bookedSeatCount)));
						}
						else if (type.equals("KM")) {
							taxCost = taxCost.add(value);
						}
						else if (type.equals("DAY")) {
							taxCost = taxCost.add(value);
						}

						JSONObject tax = new JSONObject();
						tax.put("amount", taxCost);
						tax.put("actionCode", StringUtil.substring(StringUtil.removeSymbol(taxJson.getString("name").replaceAll(" ", "")), 15).toUpperCase());
						tax.put("name", taxJson.getString("name"));
						tax.put("type", type);
						breakevenExpenses.add(tax);
					}
				}

				/** Expenses */
				if (repoBreakeven.has(EXPENSE) && !repoBreakeven.getJSONArray(EXPENSE).isEmpty()) {
					for (Object expenseObject : repoBreakeven.getJSONArray(EXPENSE)) {
						JSONObject expenseJson = (JSONObject) expenseObject;
						BigDecimal expenseCost = BigDecimal.ZERO;
						BigDecimal value = expenseJson.has(VALUE) ? StringUtil.getBigDecimalValue(expenseJson.getString(VALUE)) : BigDecimal.ZERO;
						String type = expenseJson.getString("type");

						if (type.equals("SEAT")) {
							expenseCost = expenseCost.add(value.multiply(seatCount));
						}
						else if (type.equals("BOSEAT") && bookedSeatCount > 0) {
							expenseCost = expenseCost.add(value.multiply(new BigDecimal(bookedSeatCount)));
						}
						else if (type.equals("KM")) {
							expenseCost = expenseCost.add(value);
						}
						else if (type.equals("DAY")) {
							expenseCost = expenseCost.add(value);
						}
						JSONObject expensesJson = new JSONObject();
						expensesJson.put("amount", expenseCost);
						expensesJson.put("actionCode", StringUtil.substring(StringUtil.removeSymbol(expenseJson.getString("name").replaceAll(" ", "")), 15).toUpperCase());
						expensesJson.put("name", expenseJson.getString("name"));
						expensesJson.put("type", type);
						breakevenExpenses.add(expensesJson);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return breakevenExpenses;
	}

	public Map<String, Map<String, String>> getTripDataCountDetails(AuthDTO authDTO, String fromDate, String toDate) {
		Map<String, Map<String, String>> resultMap = new HashMap<>();

		/** Get Redis Cache Data */
		Map<String, Map<String, String>> dataMap = getTripDataCountCache(authDTO);

		if (dataMap != null) {
			for (Entry<String, Map<String, String>> dataEntryMap : dataMap.entrySet()) {
				DateTime tripDate = DateUtil.getDateTime(dataEntryMap.getKey());
				if (!tripDate.gteq(new DateTime(fromDate)) || !tripDate.lteq(new DateTime(toDate))) {
					continue;
				}

				Map<String, String> tripEhCahceDataMap = getTripDateCountEhCache(authDTO, DateUtil.convertDate(tripDate));
				/** Override Eh Cache Data */
				if (tripEhCahceDataMap != null) {
					resultMap.put(dataEntryMap.getKey(), tripEhCahceDataMap);
				}
				else {
					resultMap.put(dataEntryMap.getKey(), dataEntryMap.getValue());
				}
			}
		}
		return resultMap;
	}

	public void putTripDataCountCache(AuthDTO authDTO, Map<String, Map<String, String>> dataMap) {
		cacheService.putTripDataCountCache(authDTO, dataMap);
	}

	@Override
	public Map<String, Map<String, String>> getTripDataCountCache(AuthDTO authDTO) {
		Map<String, Map<String, String>> dataMap = cacheService.getTripDataCountCache(authDTO);
		return dataMap;
	}

	/** Get Trip Data count from Eh Cachce */
	public Map<String, String> getTripDateCountEhCache(AuthDTO authDTO, String tripDate) {
		TripCache tripCache = new TripCache();
		Map<String, String> tripDataMap = tripCache.getTripDataCountEhCache(authDTO, tripDate);
		return tripDataMap;
	}

	private String composeExtras(AuthDTO authDTO, TripDTO tripDTO) {
		TripDTO repoTripDTO = new TripDTO();
		repoTripDTO.setCode(tripDTO.getCode());
		TripInfoDTO infoDTO = getTripInfo(authDTO, repoTripDTO);

		String extras = Text.NA;
		StringBuilder extrasDetails = new StringBuilder();

		if (infoDTO != null && StringUtil.isNotNull(infoDTO.getExtras()) && tripDTO.getTripInfo().getNotificationBusContact() != null) {
			extrasDetails.append(StringUtil.split(infoDTO.getExtras(), Text.VERTICAL_BAR, 0)).append(Text.VERTICAL_BAR);
			extrasDetails.append(StringUtil.split(infoDTO.getExtras(), Text.VERTICAL_BAR, 1)).append(Text.VERTICAL_BAR);
			extrasDetails.append(StringUtil.split(infoDTO.getExtras(), Text.VERTICAL_BAR, 2)).append(Text.VERTICAL_BAR);
			extrasDetails.append(StringUtil.split(infoDTO.getExtras(), Text.VERTICAL_BAR, 3)).append(Text.VERTICAL_BAR);
			extrasDetails.append(tripDTO.getTripInfo().getNotificationBusContact().getId());
		}
		else if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getNotificationBusContact() != null) {
			extrasDetails.append("||||").append(tripDTO.getTripInfo().getNotificationBusContact().getId());
		}
		if (StringUtil.isNotNull(extrasDetails.toString())) {
			extras = extrasDetails.toString();
		}
		return extras;
	}
}
