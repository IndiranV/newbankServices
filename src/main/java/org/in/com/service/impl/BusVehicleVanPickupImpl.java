package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.in.com.cache.StationCache;
import org.in.com.constants.Text;
import org.in.com.dao.BusVehicleVanPickupDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripPickupVanChartDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusVehicleVanPickupService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.SectorService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.service.TripVanInfoService;
import org.in.com.service.helper.TripHelperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class BusVehicleVanPickupImpl extends StationCache implements BusVehicleVanPickupService {
	@Autowired
	StationService stationService;
	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	SectorService sectorService;
	@Autowired
	TripService tripService;
	@Lazy
	@Autowired
	TripVanInfoService tripVanInfoService;
	@Autowired
	StationPointService stationPointService;

	@Override
	public void updateBusVehicleVanPickup(AuthDTO authDTO, BusVehicleVanPickupDTO vanRoute) {
		BusVehicleVanPickupDAO busVehicleVanDAO = new BusVehicleVanPickupDAO();
		if (vanRoute.getActiveFlag() != 1 && busVehicleVanDAO.checkVanPickupUsed(authDTO, vanRoute)) {
			throw new ServiceException(ErrorCode.VAN_PICKUP_STATION_POINT_USED_SCHEDULE);
		}
		busVehicleVanDAO.updateBusVehicleVanPickup(authDTO, vanRoute);
	}

	@Override
	public BusVehicleVanPickupDTO getBusVehicleVanPickup(AuthDTO authDTO, BusVehicleVanPickupDTO vanRoute) {
		BusVehicleVanPickupDAO busVehicleVanDAO = new BusVehicleVanPickupDAO();
		busVehicleVanDAO.getBusVehicleVanPickup(authDTO, vanRoute);
		vanRoute.setStation(stationService.getStation(vanRoute.getStation()));
		return vanRoute;
	}

	@Override
	public List<BusVehicleVanPickupDTO> getAllBusVehicleVanPickup(AuthDTO authDTO) {
		BusVehicleVanPickupDAO busVehicleVanDAO = new BusVehicleVanPickupDAO();
		List<BusVehicleVanPickupDTO> list = busVehicleVanDAO.getAllBusVehicleVanPickup(authDTO);
		for (BusVehicleVanPickupDTO busVehicleVanPickupDTO : list) {
			busVehicleVanPickupDTO.setStation(stationService.getStation(busVehicleVanPickupDTO.getStation()));
		}
		return list;
	}

	@Override
	public List<BusVehicleVanPickupDTO> getByStationId(AuthDTO authDTO, StationDTO stationDTO) {
		BusVehicleVanPickupDAO busVehicleVanDAO = new BusVehicleVanPickupDAO();
		stationDTO = stationService.getStation(stationDTO);
		return busVehicleVanDAO.getByStationId(authDTO, stationDTO);
	}

	@Override
	public List<TripVanInfoDTO> getActiveVanPickupTrips(AuthDTO authDTO, SearchDTO searchDTO) {
		BusVehicleVanPickupDAO busVehicleVanDAO = new BusVehicleVanPickupDAO();
		List<BusVehicleVanPickupDTO> vanPickupTrips = busVehicleVanDAO.getActiveVanPickupTrips(authDTO);
		if (vanPickupTrips.isEmpty()) {
			throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE);
		}

		SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);

		List<TripDTO> scheduleTrips = scheduleTripService.getAllTripDetails(authDTO, sector, searchDTO);

		Map<String, List<StationPointDTO>> vanPickupPoints = new HashMap<>();
		Map<String, List<ScheduleDTO>> vanPickupSchedules = new HashMap<>();
		List<String> stationPointCodes = new ArrayList<>();
		List<String> tripCodes = new ArrayList<>();
		List<TripChartDetailsDTO> tripChartTickets = new ArrayList<>();
		TripHelperServiceImpl tripHelperServiceImpl = new TripHelperServiceImpl();

		for (TripDTO tripDTO : scheduleTrips) {
			boolean isVanPickup = false;
			tripDTO.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, tripDTO.getSchedule(), tripDTO));
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				for (StationPointDTO stationPointDTO : stageDTO.getFromStation().getStationPoint()) {
					if (stationPointDTO.getBusVehicleVanPickup() == null || stationPointDTO.getBusVehicleVanPickup().getId() == 0) {
						continue;
					}
					String key = stageDTO.getFromStation().getStation().getId() + "_" + stationPointDTO.getBusVehicleVanPickup().getId();
					if (vanPickupPoints.get(key) == null) {
						List<StationPointDTO> stationPoints = new ArrayList<>();
						stationPoints.add(stationPointDTO);
						vanPickupPoints.put(key, stationPoints);
						stationPointCodes.add(stationPointDTO.getCode());
						tripCodes.add(tripDTO.getCode() + "_" + stationPointDTO.getBusVehicleVanPickup().getId() + "_" + stationPointDTO.getCode());
					}
					else {
						List<StationPointDTO> stationPoints = vanPickupPoints.get(key);
						stationPoints.add(stationPointDTO);
						vanPickupPoints.put(key, stationPoints);
						stationPointCodes.add(stationPointDTO.getCode());
						tripCodes.add(tripDTO.getCode() + "_" + stationPointDTO.getBusVehicleVanPickup().getId() + "_" + stationPointDTO.getCode());
					}
					if (vanPickupSchedules.get(key) == null) {
						List<ScheduleDTO> schedules = new ArrayList<>();
						schedules.add(tripDTO.getSchedule());
						vanPickupSchedules.put(key, schedules);
					}
					else {
						List<ScheduleDTO> schedules = vanPickupSchedules.get(key);
						schedules.add(tripDTO.getSchedule());
						vanPickupSchedules.put(key, schedules);
					}
					isVanPickup = true;
				}
				for (StationPointDTO stationPointDTO : stageDTO.getToStation().getStationPoint()) {
					if (stationPointDTO.getBusVehicleVanPickup() == null || stationPointDTO.getBusVehicleVanPickup().getId() == 0) {
						continue;
					}
					String key = stageDTO.getToStation().getStation().getId() + "_" + stationPointDTO.getBusVehicleVanPickup().getId();
					if (vanPickupPoints.get(key) == null) {
						List<StationPointDTO> stationPoints = new ArrayList<>();
						stationPoints.add(stationPointDTO);
						vanPickupPoints.put(key, stationPoints);
						stationPointCodes.add(stationPointDTO.getCode());
						tripCodes.add(tripDTO.getCode() + "_" + stationPointDTO.getBusVehicleVanPickup().getId() + "_" + stationPointDTO.getCode());
					}
					else {
						List<StationPointDTO> stationPoints = vanPickupPoints.get(key);
						stationPoints.add(stationPointDTO);
						vanPickupPoints.put(key, stationPoints);
						stationPointCodes.add(stationPointDTO.getCode());
						tripCodes.add(tripDTO.getCode() + "_" + stationPointDTO.getBusVehicleVanPickup().getId() + "_" + stationPointDTO.getCode());
					}
					if (vanPickupSchedules.get(key) == null) {
						List<ScheduleDTO> schedules = new ArrayList<>();
						schedules.add(tripDTO.getSchedule());
						vanPickupSchedules.put(key, schedules);
					}
					else {
						List<ScheduleDTO> schedules = vanPickupSchedules.get(key);
						schedules.add(tripDTO.getSchedule());
						vanPickupSchedules.put(key, schedules);
					}
					isVanPickup = true;
				}
			}
			if (isVanPickup) {
				TicketDAO dao = new TicketDAO();
				TripChartDTO tripChartDTO = dao.getTicketForTripChart(authDTO, tripDTO);
				tripChartDTO.setTrip(tripDTO);
				tripChartTickets.addAll(tripChartDTO.getTicketDetailsList());
			}
		}

		List<TripVanInfoDTO> trips = new ArrayList<>();
		for (Iterator<BusVehicleVanPickupDTO> iterator = vanPickupTrips.iterator(); iterator.hasNext();) {
			BusVehicleVanPickupDTO busVehicleVanPickupDTO = iterator.next();
			busVehicleVanPickupDTO.setStation(stationService.getStation(busVehicleVanPickupDTO.getStation()));

			List<StationPointDTO> stationPoints = vanPickupPoints.get(busVehicleVanPickupDTO.getStation().getId() + "_" + busVehicleVanPickupDTO.getId());
			if (stationPoints == null || stationPoints.isEmpty()) {
				iterator.remove();
				continue;
			}
			List<ScheduleDTO> schedules = vanPickupSchedules.get(busVehicleVanPickupDTO.getStation().getId() + "_" + busVehicleVanPickupDTO.getId());
			List<ScheduleDTO> uniqueschedules = new ArrayList<>(schedules.stream().collect(Collectors.toMap(ScheduleDTO::getId, sch1 -> sch1, (sch1, sch2) -> sch1)).values());
			busVehicleVanPickupDTO.setSchedules(uniqueschedules);

			List<StationPointDTO> uniqueStationPoints = new ArrayList<>(stationPoints.stream().collect(Collectors.toMap(StationPointDTO::getCode, sch1 -> sch1, (sch1, sch2) -> sch1)).values());

			if (!tripChartTickets.isEmpty()) {
				Map<String, List<TripChartDetailsDTO>> vanPickupPointTicketMap = getVanPickupTicketBooking(authDTO, busVehicleVanPickupDTO, tripChartTickets, stationPointCodes);
				for (StationPointDTO stationPointDTO : uniqueStationPoints) {
					List<TripChartDetailsDTO> tripChartDetails = vanPickupPointTicketMap.get(busVehicleVanPickupDTO.getCode() + Text.UNDER_SCORE + stationPointDTO.getCode());
					if (tripChartDetails != null) {
						for (Iterator<TripChartDetailsDTO> ticketIterator = tripChartDetails.iterator(); ticketIterator.hasNext();) {
							TripChartDetailsDTO chartDetailsDTO = ticketIterator.next();
							String boardingKey = chartDetailsDTO.getTripCode() + "_" + busVehicleVanPickupDTO.getId() + "_" + chartDetailsDTO.getBoardingPoint().getCode();
							String droppingKey = chartDetailsDTO.getTripCode() + "_" + busVehicleVanPickupDTO.getId() + "_" + chartDetailsDTO.getDroppingPoint().getCode();
							if (!tripCodes.contains(boardingKey) && !tripCodes.contains(droppingKey)) {
								ticketIterator.remove();
								continue;
							}
						}
						stationPointDTO.setSeatCount(tripChartDetails.size());
					}
				}
			}
			busVehicleVanPickupDTO.getStation().setStationPoints(uniqueStationPoints);

			TripVanInfoDTO tripVanInfo = new TripVanInfoDTO();
			tripVanInfo.setTripDate(searchDTO.getTravelDate());
			tripVanInfo.setVanPickup(busVehicleVanPickupDTO);
			tripVanInfo = tripVanInfoService.getTripVanInfo(authDTO, tripVanInfo);
			tripVanInfo.setVanPickup(busVehicleVanPickupDTO);
			trips.add(tripVanInfo);
		}
		return trips;
	}

	private Map<String, List<TripChartDetailsDTO>> getVanPickupTicketBooking(AuthDTO authDTO, BusVehicleVanPickupDTO busVehicleVanPickup, List<TripChartDetailsDTO> tripChartTickets, List<String> stationPointCodes) {
		Map<String, List<TripChartDetailsDTO>> vanPickupPointTicketMap = new HashMap<>();

		for (TripChartDetailsDTO chartDetailsDTO : tripChartTickets) {
			chartDetailsDTO.setBoardingPoint(stationPointService.getStationPoint(authDTO, chartDetailsDTO.getBoardingPoint()));
			chartDetailsDTO.setDroppingPoint(stationPointService.getStationPoint(authDTO, chartDetailsDTO.getDroppingPoint()));
			chartDetailsDTO.setFromStation(stationService.getStation(chartDetailsDTO.getFromStation()));
			chartDetailsDTO.setToStation(stationService.getStation(chartDetailsDTO.getToStation()));

			if (!stationPointCodes.contains(chartDetailsDTO.getBoardingPoint().getCode()) && !stationPointCodes.contains(chartDetailsDTO.getDroppingPoint().getCode())) {
				continue;
			}

			if (stationPointCodes.contains(chartDetailsDTO.getBoardingPoint().getCode())) {
				String key = busVehicleVanPickup.getCode() + Text.UNDER_SCORE + chartDetailsDTO.getBoardingPoint().getCode();
				if (vanPickupPointTicketMap.get(key) == null) {
					List<TripChartDetailsDTO> tickets = new ArrayList<>();
					tickets.add(chartDetailsDTO);
					vanPickupPointTicketMap.put(key, tickets);
				}
				else {
					List<TripChartDetailsDTO> tickets = vanPickupPointTicketMap.get(key);
					tickets.add(chartDetailsDTO);
					vanPickupPointTicketMap.put(key, tickets);
				}
			}
			else if (stationPointCodes.contains(chartDetailsDTO.getDroppingPoint().getCode())) {
				String key = busVehicleVanPickup.getCode() + Text.UNDER_SCORE + chartDetailsDTO.getDroppingPoint().getCode();
				if (vanPickupPointTicketMap.get(key) == null) {
					List<TripChartDetailsDTO> tickets = new ArrayList<>();
					tickets.add(chartDetailsDTO);
					vanPickupPointTicketMap.put(key, tickets);
				}
				else {
					List<TripChartDetailsDTO> tickets = vanPickupPointTicketMap.get(key);
					tickets.add(chartDetailsDTO);
					vanPickupPointTicketMap.put(key, tickets);
				}
			}

		}
		return vanPickupPointTicketMap;
	}

	@Override
	public TripPickupVanChartDTO getActiveVanPickupTripChart(AuthDTO authDTO, SearchDTO searchDTO, BusVehicleVanPickupDTO busVehicleVanPickup) {
		BusVehicleVanPickupDAO busVehicleVanDAO = new BusVehicleVanPickupDAO();
		busVehicleVanDAO.getBusVehicleVanPickup(authDTO, busVehicleVanPickup);

		TripPickupVanChartDTO tripPickupVanChart = new TripPickupVanChartDTO();

		SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);

		List<TripDTO> scheduleTrips = scheduleTripService.getAllTripDetails(authDTO, sector, searchDTO);
		List<Integer> stationPointIds = new ArrayList<>();
		TripHelperServiceImpl tripHelperServiceImpl = new TripHelperServiceImpl();
		List<ScheduleDTO> schedules = new ArrayList<>();
		for (TripDTO tripDTO : scheduleTrips) {
			boolean isVanPickup = false;
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				for (StationPointDTO stationPointDTO : stageDTO.getFromStation().getStationPoint()) {
					if (stationPointDTO.getBusVehicleVanPickup() == null || stationPointDTO.getBusVehicleVanPickup().getId() == 0 || busVehicleVanPickup.getId() != stationPointDTO.getBusVehicleVanPickup().getId()) {
						continue;
					}
					stationPointIds.add(stationPointDTO.getId());
					isVanPickup = true;
				}
				for (StationPointDTO stationPointDTO : stageDTO.getToStation().getStationPoint()) {
					if (stationPointDTO.getBusVehicleVanPickup() == null || stationPointDTO.getBusVehicleVanPickup().getId() == 0 || busVehicleVanPickup.getId() != stationPointDTO.getBusVehicleVanPickup().getId()) {
						continue;
					}
					stationPointIds.add(stationPointDTO.getId());
					isVanPickup = true;
				}
			}
			if (isVanPickup) {
				tripDTO.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, tripDTO.getSchedule(), tripDTO));
				schedules.add(tripDTO.getSchedule());

				TicketDAO dao = new TicketDAO();
				TripChartDTO tripChartDTO = dao.getTicketForTripChart(authDTO, tripDTO);

				for (Iterator<TripChartDetailsDTO> iterator = tripChartDTO.getTicketDetailsList().iterator(); iterator.hasNext();) {
					TripChartDetailsDTO chartDetailsDTO = iterator.next();
					if (!stationPointIds.contains(chartDetailsDTO.getBoardingPoint().getId()) && !stationPointIds.contains(chartDetailsDTO.getDroppingPoint().getId())) {
						iterator.remove();
						continue;
					}
					chartDetailsDTO.setFromStation(stationService.getStation(chartDetailsDTO.getFromStation()));
					chartDetailsDTO.setToStation(stationService.getStation(chartDetailsDTO.getToStation()));

					int boardingMinutes = chartDetailsDTO.getBoardingPoint().getMinitues();
					int droppingMinutes = chartDetailsDTO.getDroppingPoint().getMinitues();
					chartDetailsDTO.setBoardingPoint(stationPointService.getStationPoint(authDTO, chartDetailsDTO.getBoardingPoint()));
					chartDetailsDTO.setDroppingPoint(stationPointService.getStationPoint(authDTO, chartDetailsDTO.getDroppingPoint()));
					chartDetailsDTO.getBoardingPoint().setMinitues(boardingMinutes);
					chartDetailsDTO.getDroppingPoint().setMinitues(droppingMinutes);

					if (stationPointIds.contains(chartDetailsDTO.getBoardingPoint().getId())) {
						chartDetailsDTO.getBoardingPoint().setActiveFlag(5);
					}
					else if (stationPointIds.contains(chartDetailsDTO.getDroppingPoint().getId())) {
						chartDetailsDTO.getDroppingPoint().setActiveFlag(5);
					}
				}
				tripPickupVanChart.getTicketDetails().addAll(tripChartDTO.getTicketDetailsList());
			}
		}

		TripVanInfoDTO tripVanInfo = new TripVanInfoDTO();
		tripVanInfo.setTripDate(searchDTO.getTravelDate());

		List<ScheduleDTO> uniqueschedules = new ArrayList<>(schedules.stream().collect(Collectors.toMap(ScheduleDTO::getId, sch1 -> sch1, (sch1, sch2) -> sch1)).values());
		busVehicleVanPickup.setSchedules(uniqueschedules);

		tripVanInfo.setVanPickup(busVehicleVanPickup);
		tripVanInfo = tripVanInfoService.getTripVanInfo(authDTO, tripVanInfo);
		tripVanInfo.setVanPickup(busVehicleVanPickup);
		tripPickupVanChart.setTripVanInfo(tripVanInfo);
		return tripPickupVanChart;
	}
}
