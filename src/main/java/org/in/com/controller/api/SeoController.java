package org.in.com.controller.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.config.ApplicationConfig;
import org.in.com.controller.api.io.BusIO;
import org.in.com.controller.api.io.BusSeatLayoutIO;
import org.in.com.controller.api.io.BusSeatTypeIO;
import org.in.com.controller.api.io.OrganizationIO;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.api.io.ScheduleIO;
import org.in.com.controller.api.io.StageFareIO;
import org.in.com.controller.api.io.StationIO;
import org.in.com.controller.api.io.StationPointIO;
import org.in.com.controller.api.io.TripIO;
import org.in.com.controller.api.io.TripStatusIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.OrganizationService;
import org.in.com.service.SearchService;
import org.in.com.service.SeoService;
import org.in.com.service.StationService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;

@Controller
@RequestMapping("/api/json/{accessToken}/seo/{namespaceCode}")
public class SeoController extends BaseController {
	@Autowired
	SeoService seoService;
	@Autowired
	StationService stationService;
	@Autowired
	BusService busService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	SearchService searchService;
	@Autowired
	protected AuthService authService;

	@RequestMapping(value = "/station/{stationCode}/point", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<StationPointIO>> getStationPoint(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("stationCode") String stationCode) throws Exception {
		if (StringUtil.isNull(accessToken) || StringUtil.isNull(stationCode)) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);

		StationDTO station = new StationDTO();
		station.setCode(stationCode);

		List<StationPointIO> pointList = new ArrayList<StationPointIO>();
		List<StationPointDTO> list = seoService.getScheduleStationPoint(authDTO, station);
		for (StationPointDTO stationPointDTO : list) {
			StationPointIO stationPointIO = new StationPointIO();
			stationPointIO.setCode(stationPointDTO.getCode());
			stationPointIO.setName(stationPointDTO.getName());
			stationPointIO.setLandmark(stationPointDTO.getLandmark());
			stationPointIO.setAddress(stationPointDTO.getAddress());
			stationPointIO.setNumber(stationPointDTO.getNumber());
			stationPointIO.setLatitude(stationPointDTO.getLatitude());
			stationPointIO.setLongitude(stationPointDTO.getLongitude());
			pointList.add(stationPointIO);
		}
		return ResponseIO.success(pointList);
	}

	@RequestMapping(value = "/station", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<StationIO>> getActiveStation(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);

		List<StationDTO> list = stationService.getCommerceStation(authDTO);
		for (StationDTO stationDTO : list) {
			StationIO stationio = new StationIO();
			stationio.setCode(stationDTO.getCode());
			stationio.setName(stationDTO.getName());

			// StateIO state = new StateIO();
			// state.setCode(stationDTO.getState().getCode());
			// state.setName(stationDTO.getState().getName());
			// stationio.setState(state);
			stations.add(stationio);
		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/route", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getRoutes(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);

		List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
		if (authDTO != null) {
			Map<String, List<String>> routeList = stationService.getCommerceRoutes(authDTO);
			if (routeList != null) {
				for (Map.Entry<String, List<String>> entry : routeList.entrySet()) {
					for (String toStation : entry.getValue()) {
						Map<String, String> routeMap = new HashMap<String, String>();
						routeMap.put(entry.getKey(), toStation);
						listMap.add(routeMap);
					}
				}
			}
		}
		return ResponseIO.success(listMap);
	}

	@RequestMapping(value = "/bustype", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<BusIO>> getBusType(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);

		List<BusIO> busIOList = new ArrayList<BusIO>();
		List<BusDTO> list = busService.getAll(authDTO);

		for (BusDTO dto : list) {
			BusIO busIO = new BusIO();
			busIO.setCode(dto.getCode());
			busIO.setName(dto.getName());
			busIO.setBusType(busService.getBusCategoryUsingEM(dto.getCategoryCode()));
			busIO.setCategoryCode(dto.getCategoryCode());
			busIO.setDisplayName(dto.getDisplayName());
			busIO.setTotalSeatCount(dto.getSeatCount());
			busIOList.add(busIO);
		}
		return ResponseIO.success(busIOList);
	}

	@RequestMapping(value = "/organization", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<OrganizationIO>> getOrganization(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		AuthDTO authDTO = validateMandatory(accessToken, namespaceCode);

		List<OrganizationIO> organizations = new ArrayList<OrganizationIO>();

		List<OrganizationDTO> list = organizationService.getAll(authDTO);
		for (OrganizationDTO organizationDTO : list) {
			OrganizationIO organizaionio = new OrganizationIO();
			organizaionio.setCode(organizationDTO.getCode());
			organizaionio.setName(organizationDTO.getName());
			organizaionio.setAddress1(organizationDTO.getAddress1());
			organizaionio.setAddress2(organizationDTO.getAddress2());
			organizaionio.setContact(organizationDTO.getContact());
			StationIO stationIO = new StationIO();

			// StateIO stateIO = new StateIO();
			// stateIO.setCode(organizationDTO.getStation().getState().getCode());
			// stateIO.setName(organizationDTO.getStation().getState().getName());
			// stationIO.setState(stateIO);

			stationIO.setName(organizationDTO.getStation().getName());
			stationIO.setCode(organizationDTO.getStation().getCode());
			organizaionio.setStation(stationIO);

			organizations.add(organizaionio);
		}
		return ResponseIO.success(organizations);
	}

	@RequestMapping(value = "/search/{fromCode}/{toCode}/{tripDate}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripIO>> getSearchInventory(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("fromCode") String fromCode, @PathVariable("toCode") String toCode, @PathVariable("tripDate") String tripDate) throws Exception {
		validateMandatory(accessToken, namespaceCode);

		AuthDTO authDTO = authService.getGuestAuthendtication(namespaceCode, DeviceMediumEM.WEB_USER);

		if (!DateUtil.isValidDate(tripDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		DateTime tripTravelDate = new DateTime(tripDate);
		if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) < -10) {
			throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
		}
		if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) > authDTO.getNamespace().getProfile().getAdvanceBookingDays()) {
			throw new ServiceException(ErrorCode.MAX_ADVANCE_BOOKING_DAYS, authDTO.getNamespace().getProfile().getAdvanceBookingDays() + " days");
		}
		if (StringUtil.isNull(fromCode) || StringUtil.isNull(toCode)) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(tripTravelDate);
		StationDTO fromStationDTO = new StationDTO();
		fromStationDTO.setCode(fromCode);
		StationDTO toStationDTO = new StationDTO();
		toStationDTO.setCode(toCode);
		searchDTO.setFromStation(fromStationDTO);
		searchDTO.setToStation(toStationDTO);

		List<TripIO> tripList = new ArrayList<>();
		List<TripDTO> list = searchService.getSearch(authDTO, searchDTO);
		for (TripDTO tripDTO : list) {
			TripIO tripIO = new TripIO();
			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setName(tripDTO.getSchedule().getName());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
			tripIO.setSchedule(schedule);

			// Trip Status
			TripStatusIO tripStatusIO = new TripStatusIO();
			tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
			tripStatusIO.setName(tripDTO.getTripStatus().getName());
			tripIO.setTripStatus(tripStatusIO);

			int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
			int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
			tripIO.setTravelTime(Hours + " : " + Minutes);

			// Bus
			BusIO busIO = new BusIO();
			busIO.setName(tripDTO.getBus().getName());
			busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
			busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
			busIO.setTotalSeatCount(tripDTO.getBus().getBusSeatLayoutDTO().getList().size());
			List<AmenitiesIO> amenities = new ArrayList<AmenitiesIO>();
			for (AmenitiesDTO amenitiesDTO : tripDTO.getAmenities()) {
				AmenitiesIO amenitiesIO = new AmenitiesIO();
				amenitiesIO.setCode(amenitiesDTO.getCode());
				amenitiesIO.setName(amenitiesDTO.getName());
				amenitiesIO.setActiveFlag(amenitiesDTO.getActiveFlag());
				amenities.add(amenitiesIO);
			}
			tripIO.setAmenities(amenities);

			Map<String, Integer> availableMap = new HashMap<String, Integer>();
			Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();
			for (BusSeatLayoutDTO layoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
				BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
				layoutIO.setCode(layoutDTO.getCode());
				layoutIO.setSeatName(layoutDTO.getName());
				layoutIO.setColPos(layoutDTO.getColPos());
				layoutIO.setRowPos(layoutDTO.getRowPos());
				layoutIO.setLayer(layoutDTO.getLayer());

				BusSeatTypeIO seatStatus = new BusSeatTypeIO();
				seatStatus.setCode(layoutDTO.getBusSeatType().getCode());
				seatStatus.setName(layoutDTO.getBusSeatType().getName());
				layoutIO.setBusSeatType(seatStatus);

				layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
				if (layoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
					availableMap.put(layoutDTO.getBusSeatType().getCode(), availableMap.get(layoutDTO.getBusSeatType().getCode()) == null ? 1 : availableMap.get(layoutDTO.getBusSeatType().getCode()) + 1);

					// Schedule Seat Fare
					if (layoutDTO.getFare() != null) {
						if (seatFareMap.get(layoutDTO.getBusSeatType().getCode()) == null) {
							List<BusSeatLayoutDTO> seatFareList = new ArrayList<BusSeatLayoutDTO>();
							seatFareList.add(layoutDTO);
							seatFareMap.put(layoutDTO.getBusSeatType().getCode(), seatFareList);
						}
						else if (seatFareMap.get(layoutDTO.getBusSeatType().getCode()) != null) {
							List<BusSeatLayoutDTO> seatFareList = seatFareMap.get(layoutDTO.getBusSeatType().getCode());
							seatFareList.add(layoutDTO);
							seatFareMap.put(layoutDTO.getBusSeatType().getCode(), seatFareList);
						}
					}
				}
			}

			tripIO.setBus(busIO);
			tripIO.setTripCode(tripDTO.getCode());
			tripIO.setTravelDate(tripDate);
			if (tripDTO.getStage() != null) {
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(tripDTO.getStage().getFromStation().getStation().getCode());
				fromStation.setName(tripDTO.getStage().getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(tripDTO.getStage().getToStation().getStation().getCode());
				toStation.setName(tripDTO.getStage().getToStation().getStation().getName());
				toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				List<StageFareIO> stageFareList = new ArrayList<>();
				int availableSeatCount = 0;
				for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
					StageFareIO stageFareIO = new StageFareIO();
					stageFareIO.setFare(fareDTO.getFare());
					stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
					stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
					if (fareDTO.getGroup() != null) {
						stageFareIO.setGroupName(fareDTO.getGroup().getName());
					}
					if (availableMap.get(fareDTO.getBusSeatType().getCode()) != null) {
						stageFareIO.setAvailableSeatCount(availableMap.get(fareDTO.getBusSeatType().getCode()));
					}
					// Schedule Seat Fare
					if (seatFareMap.get(fareDTO.getBusSeatType().getCode()) != null) {
						List<BusSeatLayoutDTO> seatFareList = seatFareMap.get(fareDTO.getBusSeatType().getCode());
						Map<BigDecimal, Integer> seatFareCount = new HashMap<BigDecimal, Integer>();
						for (BusSeatLayoutDTO layoutDTO : seatFareList) {
							if (seatFareCount.get(layoutDTO.getFare()) != null) {
								seatFareCount.put(layoutDTO.getFare(), seatFareCount.get(layoutDTO.getFare()) + 1);
							}
							else {
								seatFareCount.put(layoutDTO.getFare(), 1);
							}
						}
						List<BigDecimal> fareList = new ArrayList<BigDecimal>(seatFareCount.keySet());
						for (BigDecimal fare : fareList) {
							StageFareIO seatFareIO = new StageFareIO();
							seatFareIO.setFare(fare);
							seatFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
							seatFareIO.setSeatName(fareDTO.getBusSeatType().getName());
							seatFareIO.setAvailableSeatCount(seatFareCount.get(fare));
							stageFareIO.setAvailableSeatCount(stageFareIO.getAvailableSeatCount() - seatFareCount.get(fare));
							stageFareList.add(seatFareIO);
						}
					}
					// remove multiple fare if avl is 0
					if (stageFareIO.getAvailableSeatCount() == 0 && !stageFareList.isEmpty()) {
						continue;
					}
					stageFareList.add(stageFareIO);
					availableSeatCount = stageFareIO.getAvailableSeatCount() + availableSeatCount;
				}
				// Sorting
				Comparator<StageFareIO> comp = new BeanComparator("fare");
				Collections.sort(stageFareList, comp);

				tripIO.setStageFare(stageFareList);
				tripIO.setAvailableSeatCount(availableSeatCount);
				tripIO.setTravelStopCount(tripDTO.getTravelStopCount());

				List<StationPointIO> fromStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					if (pointDTO.getCreditDebitFlag().equals("Cr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripDTO.getStage().getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					pointIO.setLatitude(pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					fromStationPoint.add(pointIO);
				}
				List<StationPointIO> toStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					if (pointDTO.getCreditDebitFlag().equals("Cr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripDTO.getStage().getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					pointIO.setLatitude(pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					toStationPoint.add(pointIO);
				}
				// Sorting
				Comparator<StationPointIO> timeSort = new BeanComparator("dateTime");
				Collections.sort(fromStationPoint, timeSort);
				Collections.sort(toStationPoint, timeSort);

				fromStation.setStationPoint(fromStationPoint);
				toStation.setStationPoint(toStationPoint);

				tripIO.setFromStation(fromStation);
				tripIO.setToStation(toStation);
			}
			// Sorting
			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(tripDTO.getStationList(), comp);

			List<StationIO> viaStation = new ArrayList<StationIO>();
			for (ScheduleStationDTO stationDTO : tripDTO.getStationList()) {
				if (stationDTO.getActiveFlag() == 1) {
					StationIO station = new StationIO();
					station.setName(stationDTO.getStation().getName());
					station.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stationDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					viaStation.add(station);
				}
			}

			tripIO.setViaStations(viaStation);
			tripList.add(tripIO);
		}
		// Sorting Trips
		Collections.sort(tripList, new Comparator<TripIO>() {
			@Override
			public int compare(TripIO t1, TripIO t2) {
				return new CompareToBuilder().append(t2.getTripStatus().getCode(), t1.getTripStatus().getCode()).append(t2.getAvailableSeatCount() > 0 ? 1 : 0, t1.getAvailableSeatCount() > 0 ? 1 : 0).append(t1.getFromStation().getDateTime(), t2.getFromStation().getDateTime()).toComparison();
			}
		});

		return ResponseIO.success(tripList);
	}

	private AuthDTO validateMandatory(String accessToken, String namespaceCode) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (StringUtil.isNull(namespaceCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		return authDTO;
	}
}
