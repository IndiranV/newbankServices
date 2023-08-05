package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.gps.TrackBusService;
import org.in.com.cache.TripVanInfoCache;
import org.in.com.constants.Text;
import org.in.com.dao.TripVanInfoDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.TripPickupVanChartDTO;
import org.in.com.dto.TripVanExceptionDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.service.BusService;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.BusVehicleVanPickupService;
import org.in.com.service.ScheduleService;
import org.in.com.service.StationService;
import org.in.com.service.TripVanInfoService;
import org.in.com.service.UserService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class TripVanInfoImpl extends TripVanInfoCache implements TripVanInfoService {

	@Autowired
	BusVehicleService busVehicleService;
	@Lazy
	@Autowired
	BusVehicleVanPickupService busVehicleVanPickupService;
	@Autowired
	BusVehicleDriverService driverService;
	@Autowired
	UserService userService;
	@Autowired
	TrackBusService trackbusService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	StationService stationService;
	@Autowired
	BusService busService;

	@Override
	public void updateTripVanInfo(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDAO tripVanInfoDAO = new TripVanInfoDAO();

		tripVanInfoDTO.setDriver(driverService.getBusVehicleDriver(authDTO, tripVanInfoDTO.getDriver()));
		tripVanInfoDTO.setVehicle(busVehicleService.getBusVehicles(authDTO, tripVanInfoDTO.getVehicle()));
		busVehicleVanPickupService.getBusVehicleVanPickup(authDTO, tripVanInfoDTO.getVanPickup());

		tripVanInfoDAO.updateTripVanInfo(authDTO, tripVanInfoDTO);

		/** Push GPS Trips */
		pushGeoTripDetails(authDTO, tripVanInfoDTO);
		
		// clear cache
		removeTripVanInfoCache(authDTO, tripVanInfoDTO);
	}

	@Async
	public void pushGeoTripDetails(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		SearchDTO search = new SearchDTO();
		search.setTravelDate(tripVanInfoDTO.getTripDate());

		TripPickupVanChartDTO tripPickupVanChart = busVehicleVanPickupService.getActiveVanPickupTripChart(authDTO, search, tripVanInfoDTO.getVanPickup());

		TripChartDTO tripChartDTO = new TripChartDTO();

		TripDTO trip = new TripDTO();
		trip.setCode(tripVanInfoDTO.getCode());
		trip.setBus(tripVanInfoDTO.getVehicle().getBus());
		trip.setTripDate(tripVanInfoDTO.getTripDate().getStartOfDay());
		trip.setStageList(new ArrayList<>());

		TripInfoDTO tripInfo = new TripInfoDTO();
		tripInfo.setDriverName(tripVanInfoDTO.getDriver().getName());
		tripInfo.setDriverMobile(tripVanInfoDTO.getDriver().getMobileNumber());
		tripInfo.setBusVehicle(tripVanInfoDTO.getVehicle());
		trip.setTripInfo(tripInfo);

		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(tripVanInfoDTO.getVanPickup().getCode());
		schedule.setName(tripVanInfoDTO.getVanPickup().getName());
		schedule.setServiceNumber(Text.NA);
		trip.setSchedule(schedule);

		List<ScheduleStationDTO> stations = new ArrayList<>();
		List<StationPointDTO> stationPoints = new ArrayList<>();
		int sequence = 1;
		for (TripChartDetailsDTO tripChartDetailsDTO : tripPickupVanChart.getTicketDetails()) {
			ScheduleStationDTO scheduleStationDTO = new ScheduleStationDTO();
			scheduleStationDTO.setStationSequence(sequence);
			scheduleStationDTO.setStation(tripChartDetailsDTO.getFromStation());
			stations.add(scheduleStationDTO);
			if (tripChartDetailsDTO.getBoardingPoint().getActiveFlag() == 5) {
				stationPoints.add(tripChartDetailsDTO.getBoardingPoint());
			}
			else if (tripChartDetailsDTO.getDroppingPoint().getActiveFlag() == 5) {
				stationPoints.add(tripChartDetailsDTO.getDroppingPoint());
			}
			sequence++;
		}
		trip.setStationList(stations);

		StageStationDTO fromStation = new StageStationDTO();
		fromStation.setStation(stationService.getStation(tripVanInfoDTO.getVanPickup().getStation()));
		fromStation.setStationPoint(stationPoints);

		StageStationDTO toStation = new StageStationDTO();
		toStation.setStation(new StationDTO());

		StageDTO stageDTO = new StageDTO();
		stageDTO.setFromStation(fromStation);
		stageDTO.setToStation(toStation);
		List<StageDTO> stages = new ArrayList<>();
		stages.add(stageDTO);
		trip.setStageList(stages);

		search.setFromStation(tripVanInfoDTO.getVanPickup().getStation());
		search.setToStation(tripVanInfoDTO.getVanPickup().getStation());
		trip.setSearch(search);
		tripChartDTO.setTrip(trip);
		tripChartDTO.setVendorList(getVendorList(authDTO));
		tripChartDTO.setTicketDetailsList(tripPickupVanChart.getTicketDetails());

		trackbusService.updateGeoTripDetails(authDTO, GPSDeviceVendorEM.EZEEGPS, tripChartDTO);
	}

	private List<String> getVendorList(AuthDTO authDTO) {
		Map<String, String> partnerMap = new HashMap<>();
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTripInfo()) {
			userDTO = userService.getUser(authDTO, userDTO);
			if (userDTO.getUserTags() == null || userDTO.getUserTags().isEmpty()) {
				continue;
			}
			for (UserTagEM userTag : userDTO.getUserTags()) {
				partnerMap.put(userTag.getCode(), userTag.getCode());
			}
		}
		return new ArrayList<String>(partnerMap.values());
	}

	@Override
	public TripVanInfoDTO getTripVanInfo(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDAO tripVanInfoDAO = new TripVanInfoDAO();
		TripVanInfoDTO vanInfo = tripVanInfoDAO.getTripVanInfo(authDTO, tripVanInfoDTO);

		if (StringUtil.isNotNull(vanInfo.getCode())) {
			vanInfo.setDriver(driverService.getBusVehicleDriver(authDTO, vanInfo.getDriver()));
			vanInfo.setVehicle(busVehicleService.getBusVehicles(authDTO, vanInfo.getVehicle()));
			vanInfo.getVehicle().setBus(busService.getBus(authDTO, vanInfo.getVehicle().getBus()));
			busVehicleVanPickupService.getBusVehicleVanPickup(authDTO, vanInfo.getVanPickup());
		}
		if (vanInfo.getTripVanException() != null && vanInfo.getTripVanException().getVanPickup() != null && vanInfo.getTripVanException().getSchedules() != null) {
			busVehicleVanPickupService.getBusVehicleVanPickup(authDTO, vanInfo.getTripVanException().getVanPickup());
			for (ScheduleDTO scheduleDTO : vanInfo.getTripVanException().getSchedules()) {
				scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			if (vanInfo.getTripVanException().getAudit() != null && vanInfo.getTripVanException().getAudit().getUser() != null && vanInfo.getTripVanException().getAudit().getUser().getId() != 0) {
				vanInfo.getTripVanException().getAudit().setUser(userService.getUser(authDTO, vanInfo.getTripVanException().getAudit().getUser()));
			}
		}
		return vanInfo;
	}

	@Override
	public void updateNotitficationStatus(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDAO tripVanInfoDAO = new TripVanInfoDAO();
		tripVanInfoDAO.updateNotificationStatus(authDTO, tripVanInfoDTO);

	}

	@Override
	public TripVanInfoDTO getTripVanInfoByCode(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDAO tripVanInfoDAO = new TripVanInfoDAO();
		TripVanInfoDTO tripVanInfo = tripVanInfoDAO.getTripVanInfoByCode(authDTO, tripVanInfoDTO);

		tripVanInfo.setDriver(driverService.getBusVehicleDriver(authDTO, tripVanInfo.getDriver()));
		tripVanInfo.setVanPickup(busVehicleVanPickupService.getBusVehicleVanPickup(authDTO, tripVanInfo.getVanPickup()));
		tripVanInfo.setVehicle(busVehicleService.getBusVehicles(authDTO, tripVanInfo.getVehicle()));
		return tripVanInfo;
	}

	@Override
	public void updateTripVanException(AuthDTO authDTO, TripVanExceptionDTO tripVanExceptionDTO) {
		TripVanInfoDAO tripVanInfoDAO = new TripVanInfoDAO();
		
		BusVehicleVanPickupDTO repoVanPickup = new BusVehicleVanPickupDTO();
		repoVanPickup.setCode(tripVanExceptionDTO.getVanPickup().getCode());
		busVehicleVanPickupService.getBusVehicleVanPickup(authDTO, repoVanPickup);
		tripVanExceptionDTO.getVanPickup().setId(repoVanPickup.getId());

		tripVanInfoDAO.updateTripVanException(authDTO, tripVanExceptionDTO);
		
		// clear cache
		removeTripVanExceptionCache(authDTO, tripVanExceptionDTO);
	}

	@Override
	public TripVanInfoDTO getTripVanInfoV2(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDTO tripVanInfo = getTripVanInfoCache(authDTO, tripVanInfoDTO);
		return tripVanInfo;
	}

}
