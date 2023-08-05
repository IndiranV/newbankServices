package org.in.com.controller.commerce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.aggregator.gps.TrackBusService;
import org.in.com.constants.Text;
import org.in.com.controller.commerce.io.BusIO;
import org.in.com.controller.commerce.io.BusSeatLayoutIO;
import org.in.com.controller.commerce.io.BusSeatTypeIO;
import org.in.com.controller.commerce.io.GpsIO;
import org.in.com.controller.commerce.io.OperatorIO;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.commerce.io.SeatStatusIO;
import org.in.com.controller.commerce.io.StageFareIO;
import org.in.com.controller.commerce.io.StageIO;
import org.in.com.controller.commerce.io.StationIO;
import org.in.com.controller.commerce.io.StationPointIO;
import org.in.com.controller.commerce.io.TicketDetailsIO;
import org.in.com.controller.commerce.io.TripIO;
import org.in.com.controller.commerce.io.TripStatusIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.GpsService;
import org.in.com.service.NamespaceService;
import org.in.com.service.StationPointService;
import org.in.com.service.TicketService;
import org.in.com.service.TripService;
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

@Controller
@RequestMapping(value = "/commerce/gps")
public class CommerceGpsController<T> extends BaseController {
	@Autowired
	GpsService gpsService;
	@Autowired
	TicketService ticketService;
	@Autowired
	TripService tripService;
	@Autowired
	BusService busService;
	@Autowired
	TrackBusService geoService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	StationPointService stationPointService;

	@RequestMapping(value = "/ticket/{ticketCode}/vehicle/location", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<GpsIO> getTicketGPSLoction(@PathVariable("ticketCode") String ticketCode) throws Exception {
		GpsIO gpsIO = new GpsIO();
		if (StringUtil.isNotNull(ticketCode)) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			AuthDTO authDTO = getNamespaceAuthDTO(ticketDTO);
			if (authDTO == null) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TripDTO tripDTO = gpsService.getTicketVechileLocation(authDTO, ticketDTO);
			// GPS Data
			if (tripDTO.getLocation() != null) {
				gpsIO.setLatitude(tripDTO.getLocation().getLatitude());
				gpsIO.setLongitude(tripDTO.getLocation().getLongitude());
				gpsIO.setAddress(tripDTO.getLocation().getAddress());
				gpsIO.setUpdatedTime(tripDTO.getLocation().getUpdatedTime());
				if (tripDTO.getLocation().getError() != null) {
					gpsIO.setErrorCode(tripDTO.getLocation().getError().getCode());
					gpsIO.setErrorDesc(tripDTO.getLocation().getError().getMessage());
				}
			}
			if (tripDTO.getTripInfo() != null) {
				gpsIO.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
				gpsIO.setDriverName(tripDTO.getTripInfo().getDriverName());
				gpsIO.setRemarks(tripDTO.getTripInfo().getRemarks());
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getBusVehicle() != null && tripDTO.getTripInfo().getBusVehicle().getId() != 0) {
					gpsIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
					gpsIO.setDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
					gpsIO.setVendorCode(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode());
				}
			}

			BusIO bus = new BusIO();
			if (tripDTO.getBus() != null) {
				bus.setCode(tripDTO.getBus().getCode());
				bus.setName(tripDTO.getBus().getName());
				bus.setCategoryCode(StringUtil.isNull(tripDTO.getBus().getCategoryCode()) ? Text.EMPTY : tripDTO.getBus().getCategoryCode());
				bus.setDisplayName(StringUtil.isNull(tripDTO.getBus().getDisplayName()) ? Text.EMPTY : tripDTO.getBus().getDisplayName());
			}
			gpsIO.setBus(bus);

			gpsIO.setTrackingCloseTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getDroppingPoint().getMinitues() + 60).format("YYYY-MM-DD hh:mm:ss"));
			// Ticket data
			StationIO fromStationIO = new StationIO();
			StationIO toStationIO = new StationIO();
			StationPointIO boardingPointIO = new StationPointIO();
			List<StationPointIO> boardingList = new ArrayList<StationPointIO>();

			// Mapping from station and boarding point
			boardingPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
			boardingPointIO.setName(ticketDTO.getBoardingPoint().getName());
			boardingPointIO.setAddress(ticketDTO.getBoardingPoint().getAddress());
			boardingPointIO.setLandmark(ticketDTO.getBoardingPoint().getLandmark());
			boardingPointIO.setNumber(ticketDTO.getBoardingPoint().getNumber());
			boardingPointIO.setLongitude(ticketDTO.getBoardingPoint().getLongitude());
			boardingPointIO.setLatitude(ticketDTO.getBoardingPoint().getLatitude());
			boardingPointIO.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			boardingList.add(boardingPointIO);

			fromStationIO.setCode(ticketDTO.getFromStation().getCode());
			fromStationIO.setName(ticketDTO.getFromStation().getName());
			fromStationIO.setStationPoint(boardingList);

			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
			gpsIO.setFromStation(fromStationIO);
			gpsIO.setToStation(toStationIO);

			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(authDTO.getNamespace().getCode());
			operatorIO.setName(authDTO.getNamespace().getName());
			gpsIO.setOperator(operatorIO);

			List<TicketDetailsIO> ticketDetails = new ArrayList<TicketDetailsIO>();
			if (ticketDTO.getTicketDetails() != null) {
				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					TicketDetailsIO ticketDetailsIO = new TicketDetailsIO();
					ticketDetailsIO.setSeatName(ticketDetailsDTO.getSeatName());
					ticketDetailsIO.setSeatCode(ticketDetailsDTO.getSeatCode());
					ticketDetailsIO.setSeatType(ticketDetailsDTO.getSeatType());

					SeatStatusIO ticketStatus = new SeatStatusIO();
					ticketStatus.setCode(ticketDetailsDTO.getTicketStatus().getCode());
					ticketStatus.setName(ticketDetailsDTO.getTicketStatus().getDescription());
					ticketDetailsIO.setSeatStatus(ticketStatus);

					ticketDetailsIO.setPassengerName(ticketDetailsDTO.getPassengerName());
					ticketDetailsIO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
					ticketDetailsIO.setPassengerGendar(ticketDetailsDTO.getSeatGendar().getCode());
					ticketDetailsIO.setSeatFare(ticketDetailsDTO.getSeatFare());
					ticketDetailsIO.setServiceTax(ticketDetailsDTO.getAcBusTax());
					ticketDetails.add(ticketDetailsIO);
				}
			}
			gpsIO.setTicketDetails(ticketDetails);

			// Trip data
			TripIO tripIO = new TripIO();

			List<StageIO> stageList = new ArrayList<>();
			// Sorting
			Comparator<StageDTO> comp = new BeanComparator("stageSequence");
			Collections.sort(tripDTO.getStageList(), comp);

			// Stage
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				StageIO stageIO = new StageIO();
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				toStation.setName(stageDTO.getToStation().getStation().getName());
				stageIO.setStageSequence(stageDTO.getStageSequence());
				toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				List<StageFareIO> stageFareList = new ArrayList<>();
				for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
					StageFareIO stageFareIO = new StageFareIO();
					stageFareIO.setFare(fareDTO.getFare());
					stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
					stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
					if (fareDTO.getGroup() != null) {
						stageFareIO.setGroupName(fareDTO.getGroup().getName());
					}
					stageFareList.add(stageFareIO);
				}
				stageIO.setStageFare(stageFareList);
				stageIO.setCode(stageDTO.getCode());
				List<StationPointIO> fromStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					fromStationPoint.add(pointIO);
				}
				List<StationPointIO> toStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					toStationPoint.add(pointIO);
				}
				fromStation.setStationPoint(fromStationPoint);
				toStation.setStationPoint(toStationPoint);
				stageIO.setFromStation(fromStation);
				stageIO.setToStation(toStation);
				stageList.add(stageIO);
			}
			tripIO.setStageList(stageList);
			tripIO.setTripCode(tripDTO.getCode());
			// Trip Status
			TripStatusIO tripStatusIO = new TripStatusIO();
			tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
			tripStatusIO.setName(tripDTO.getTripStatus().getName());
			tripIO.setTripStatus(tripStatusIO);
			gpsIO.setTrip(tripIO);
		}
		return ResponseIO.success(gpsIO);
	}

	@RequestMapping(value = "/trip/{tripCode}/vehicle/location", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<GpsIO> getTripGPSLoction(@PathVariable("tripCode") String tripCode) throws Exception {
		GpsIO gpsIO = new GpsIO();
		if (StringUtil.isNotNull(tripCode)) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			AuthDTO authDTO = getNamespaceAuthDTO(tripDTO);
			if (authDTO == null) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			tripDTO = gpsService.getTripVechileLocation(authDTO, tripDTO);
			// GPS Data
			if (tripDTO.getLocation() != null) {
				gpsIO.setLatitude(tripDTO.getLocation().getLatitude());
				gpsIO.setLongitude(tripDTO.getLocation().getLongitude());
				gpsIO.setAddress(tripDTO.getLocation().getAddress());
				gpsIO.setUpdatedTime(tripDTO.getLocation().getUpdatedTime());
				if (tripDTO.getLocation().getError() != null) {
					gpsIO.setErrorCode(tripDTO.getLocation().getError().getCode());
					gpsIO.setErrorDesc(tripDTO.getLocation().getError().getMessage());
				}
			}
			if (tripDTO.getTripInfo() != null) {
				gpsIO.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
				gpsIO.setDriverName(tripDTO.getTripInfo().getDriverName());
				gpsIO.setRemarks(tripDTO.getTripInfo().getRemarks());
				if (tripDTO.getTripInfo().getBusVehicle() != null) {
					gpsIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
					gpsIO.setDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
					gpsIO.setVendorCode(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode());
				}
			}
			ScheduleStationDTO tripToStation = null;

			for (ScheduleStationDTO scheduleStationDTO : tripDTO.getStationList()) {
				if (tripToStation == null) {
					tripToStation = scheduleStationDTO;
				}
				if (tripToStation.getStationSequence() < scheduleStationDTO.getStationSequence()) {
					tripToStation = scheduleStationDTO;
				}
			}

			BusIO bus = new BusIO();
			if (tripDTO.getBus() != null) {
				bus.setCode(tripDTO.getBus().getCode());
				bus.setName(tripDTO.getBus().getName());
				bus.setCategoryCode(StringUtil.isNull(tripDTO.getBus().getCategoryCode()) ? Text.EMPTY : tripDTO.getBus().getCategoryCode());
				bus.setDisplayName(StringUtil.isNull(tripDTO.getBus().getDisplayName()) ? Text.EMPTY : tripDTO.getBus().getDisplayName());
			}
			gpsIO.setBus(bus);

			gpsIO.setTrackingCloseTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripToStation.getMinitues() + 60).format("YYYY-MM-DD hh:mm:ss"));

			// Trip data
			TripIO tripIO = new TripIO();

			List<StageIO> stageList = new ArrayList<>();
			// Sorting
			Comparator<StageDTO> comp = new BeanComparator("stageSequence");
			Collections.sort(tripDTO.getStageList(), comp);

			// Stage
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				StageIO stageIO = new StageIO();
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				toStation.setName(stageDTO.getToStation().getStation().getName());
				stageIO.setStageSequence(stageDTO.getStageSequence());
				toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				List<StageFareIO> stageFareList = new ArrayList<>();
				for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
					StageFareIO stageFareIO = new StageFareIO();
					stageFareIO.setFare(fareDTO.getFare());
					stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
					stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
					if (fareDTO.getGroup() != null) {
						stageFareIO.setGroupName(fareDTO.getGroup().getName());
					}
					stageFareList.add(stageFareIO);
				}
				stageIO.setStageFare(stageFareList);
				stageIO.setCode(stageDTO.getCode());
				List<StationPointIO> fromStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					fromStationPoint.add(pointIO);
				}
				List<StationPointIO> toStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					toStationPoint.add(pointIO);
				}
				fromStation.setStationPoint(fromStationPoint);
				toStation.setStationPoint(toStationPoint);
				stageIO.setFromStation(fromStation);
				stageIO.setToStation(toStation);
				stageList.add(stageIO);
			}
			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(authDTO.getNamespace().getCode());
			operatorIO.setName(authDTO.getNamespace().getName());
			gpsIO.setOperator(operatorIO);

			tripIO.setStageList(stageList);
			tripIO.setTripCode(tripDTO.getCode());
			// Trip Status
			TripStatusIO tripStatusIO = new TripStatusIO();
			tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
			tripStatusIO.setName(tripDTO.getTripStatus().getName());
			tripIO.setTripStatus(tripStatusIO);
			gpsIO.setTrip(tripIO);
		}
		return ResponseIO.success(gpsIO);
	}

	@RequestMapping(value = "/bus/{busCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusIO> getBus(@PathVariable("busCode") String busCode) throws Exception {
		BusIO busIO = new BusIO();

		BusDTO busDTO = new BusDTO();
		busDTO.setCode(busCode);

		AuthDTO authDTO = getNamespaceAuthDTO(busDTO);
		if (authDTO == null) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}

		busDTO = busService.getBus(authDTO, busDTO);
		busIO.setCode(busDTO.getCode());
		busIO.setName(busDTO.getName());
		busIO.setCategoryCode(StringUtil.isNull(busDTO.getCategoryCode()) ? Text.EMPTY : busDTO.getCategoryCode());
		busIO.setDisplayName(StringUtil.isNull(busDTO.getDisplayName()) ? Text.EMPTY : busDTO.getDisplayName());
		busIO.setTotalSeatCount(busDTO.getBusSeatLayoutDTO().getList().size());

		List<BusSeatLayoutIO> seatLayoutList = new ArrayList<>();
		for (BusSeatLayoutDTO layoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
			BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
			layoutIO.setCode(layoutDTO.getCode());
			layoutIO.setSeatName(layoutDTO.getName());

			BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
			if (layoutDTO.getBusSeatType() != null) {
				seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
				seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
				layoutIO.setBusSeatType(seatTypeIO);
			}

			layoutIO.setColPos(layoutDTO.getColPos());
			layoutIO.setRowPos(layoutDTO.getRowPos());
			layoutIO.setLayer(layoutDTO.getLayer());
			layoutIO.setSequence(layoutDTO.getSequence());
			layoutIO.setOrientation(layoutDTO.getOrientation());
			layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
			seatLayoutList.add(layoutIO);
		}
		busIO.setSeatLayoutList(seatLayoutList);

		return ResponseIO.success(busIO);
	}

	@RequestMapping(value = "/trip/stage/{tripStageCode}/vehicle/location", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<GpsIO> getTripStageGPSLoction(@PathVariable("tripStageCode") String tripStageCode) throws Exception {
		GpsIO gpsIO = new GpsIO();
		try {
			if (StringUtil.isNull(tripStageCode)) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_STAGE_CODE);
			}
			Map<String, Integer> stageSplit = BitsUtil.getTripStage(tripStageCode);
			NamespaceDTO namespaceDTO = new NamespaceDTO();
			namespaceDTO.setId(stageSplit.get("namespace"));
			AuthDTO authDTO = new AuthDTO();
			authDTO.setNamespaceCode(namespaceService.getNamespace(namespaceDTO).getCode());
			if (StringUtil.isNull(authDTO.getNamespaceCode())) {
				System.out.println("NS Not found: " + namespaceDTO.getId());
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
			}
			TripDTO tripDTO = new TripDTO();
			SearchDTO searchDTO = new SearchDTO();
			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setId(stageSplit.get("fromStation"));
			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setId(stageSplit.get("toStation"));
			searchDTO.setFromStation(fromStationDTO);
			searchDTO.setToStation(toStationDTO);
			tripDTO.setSearch(searchDTO);
			tripDTO.setCode(stageSplit.get("namespace") + "N" + stageSplit.get("schedule") + "S" + stageSplit.get("tripDate") + "D");
			tripDTO.setTripDate(DateUtil.getUnCompressDate(stageSplit.get("tripDate").toString()));

			tripDTO = gpsService.getTripStageVechileLocation(authDTO, tripDTO);
			// GPS Data
			if (tripDTO.getLocation() != null) {
				gpsIO.setLatitude(tripDTO.getLocation().getLatitude());
				gpsIO.setLongitude(tripDTO.getLocation().getLongitude());
				gpsIO.setAddress(tripDTO.getLocation().getAddress());
				gpsIO.setUpdatedTime(tripDTO.getLocation().getUpdatedTime());
				if (tripDTO.getLocation().getError() != null) {
					gpsIO.setErrorCode(tripDTO.getLocation().getError().getCode());
					gpsIO.setErrorDesc(tripDTO.getLocation().getError().getMessage());
				}
			}
			if (tripDTO.getTripInfo() != null) {
				gpsIO.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
				gpsIO.setDriverName(tripDTO.getTripInfo().getDriverName());
				gpsIO.setRemarks(tripDTO.getTripInfo().getRemarks());
				if (tripDTO.getTripInfo().getBusVehicle() != null) {
					gpsIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
					gpsIO.setDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
					gpsIO.setVendorCode(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode());
				}
			}

			BusIO bus = new BusIO();
			if (tripDTO.getBus() != null) {
				bus.setCode(tripDTO.getBus().getCode());
				bus.setName(tripDTO.getBus().getName());
				bus.setCategoryCode(StringUtil.isNull(tripDTO.getBus().getCategoryCode()) ? Text.EMPTY : tripDTO.getBus().getCategoryCode());
				bus.setDisplayName(StringUtil.isNull(tripDTO.getBus().getDisplayName()) ? Text.EMPTY : tripDTO.getBus().getDisplayName());
			}
			gpsIO.setBus(bus);

			gpsIO.setTrackingCloseTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() + 60).format("YYYY-MM-DD hh:mm:ss"));

			// Trip data
			TripIO tripIO = new TripIO();

			// Stage
			StageDTO stageDTO = tripDTO.getStage();
			StationIO fromStation = new StationIO();
			StationIO toStation = new StationIO();
			fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
			fromStation.setName(stageDTO.getFromStation().getStation().getName());
			fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			toStation.setCode(stageDTO.getToStation().getStation().getCode());
			toStation.setName(stageDTO.getToStation().getStation().getName());
			toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			List<StationPointIO> fromStationPoint = new ArrayList<>();
			for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
				pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setAddress(pointDTO.getAddress());
				pointIO.setNumber(pointDTO.getNumber());
				fromStationPoint.add(pointIO);
			}

			List<StationPointIO> toStationPoint = new ArrayList<>();
			for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
				pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setAddress(pointDTO.getAddress());
				pointIO.setNumber(pointDTO.getNumber());
				toStationPoint.add(pointIO);
			}
			fromStation.setStationPoint(fromStationPoint);
			toStation.setStationPoint(toStationPoint);
			tripIO.setFromStation(fromStation);
			tripIO.setToStation(toStation);

			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(authDTO.getNamespace().getCode());
			operatorIO.setName(authDTO.getNamespace().getName());
			gpsIO.setOperator(operatorIO);

			tripIO.setTripCode(tripDTO.getCode());
			// Trip Status
			TripStatusIO tripStatusIO = new TripStatusIO();
			tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
			tripStatusIO.setName(tripDTO.getTripStatus().getName());
			tripIO.setTripStatus(tripStatusIO);
			gpsIO.setTrip(tripIO);
		}
		catch (ServiceException e) {
			System.out.println("tripStageCode SE: " + tripStageCode);
			throw e;
		}
		catch (Exception e) {
			System.out.println("tripStageCode E: " + tripStageCode);
			e.printStackTrace();
		}
		return ResponseIO.success(gpsIO);
	}

	@RequestMapping(value = "/vehicle/{deviceCode}/location/{vendorCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<GpsIO> getVehicleGPSLoction(@PathVariable("deviceCode") String deviceCode, @PathVariable("vendorCode") String vendorCode, String vehicleRegistrationeNumber) throws Exception {
		if (StringUtil.isNull(vendorCode) || StringUtil.isNull(deviceCode)) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		GPSLocationDTO locationDTO = geoService.getVehicleLocation("", GPSDeviceVendorEM.getGPSDeviceVendorEM(vendorCode), deviceCode, vehicleRegistrationeNumber);
		GpsIO gpsIO = new GpsIO();
		gpsIO.setLatitude(locationDTO.getLatitude());
		gpsIO.setLongitude(locationDTO.getLongitude());
		gpsIO.setUpdatedTime(locationDTO.getUpdatedTime());
		gpsIO.setAddress(locationDTO.getAddress());
		gpsIO.setSpeed(locationDTO.getSpeed());
		return ResponseIO.success(gpsIO);
	}

	@RequestMapping(value = "/vehicle/{operatorCode}/{vendorCode}/location/{deviceCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<GpsIO> getVehicleGEOLoction(@PathVariable("operatorCode") String operatorCode, @PathVariable("vendorCode") String vendorCode, @PathVariable("deviceCode") String deviceCode, String vehicleRegistrationeNumber) throws Exception {
		if (StringUtil.isNull(operatorCode) || StringUtil.isNull(vendorCode) || StringUtil.isNull(deviceCode)) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		GPSLocationDTO locationDTO = geoService.getVehicleLocation(operatorCode, GPSDeviceVendorEM.getGPSDeviceVendorEM(vendorCode), deviceCode, vehicleRegistrationeNumber);
		GpsIO gpsIO = new GpsIO();
		gpsIO.setLatitude(locationDTO.getLatitude());
		gpsIO.setLongitude(locationDTO.getLongitude());
		gpsIO.setUpdatedTime(locationDTO.getUpdatedTime());
		gpsIO.setRegistationNumber(locationDTO.getRegisterNumber());
		gpsIO.setSpeed(locationDTO.getSpeed());
		gpsIO.setAddress(locationDTO.getAddress());
		gpsIO.setIgnition(locationDTO.isIgnition() ? 1 : 0);
		gpsIO.setRoad(locationDTO.getRoad());
		gpsIO.setArea(locationDTO.getArea());
		gpsIO.setLandmark(locationDTO.getLandmark());
		gpsIO.setCity(locationDTO.getCity());
		gpsIO.setState(locationDTO.getState());
		gpsIO.setPostalCode(locationDTO.getPostalCode());
		return ResponseIO.success(gpsIO);
	}

	@RequestMapping(value = "/operator/{operatorCode}/stationpoint/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<StationPointIO> getStationPoint(@PathVariable("operatorCode") String operatorCode, @PathVariable("code") String code) throws Exception {
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(operatorCode);

		StationPointDTO stationPointDTO = new StationPointDTO();
		stationPointDTO.setCode(code);

		stationPointDTO = stationPointService.getStationPoint(authDTO, stationPointDTO);

		StationPointIO stationPoint = new StationPointIO();
		stationPoint.setLatitude(stationPointDTO.getLatitude() == null ? "" : stationPointDTO.getLatitude());
		stationPoint.setLongitude(stationPointDTO.getLongitude() == null ? "" : stationPointDTO.getLongitude());
		stationPoint.setCode(stationPointDTO.getCode());
		stationPoint.setName(stationPointDTO.getName());
		stationPoint.setLandmark(stationPointDTO.getLandmark());
		stationPoint.setAddress(stationPointDTO.getAddress());
		stationPoint.setNumber(stationPointDTO.getNumber());
		stationPoint.setMapUrl(stationPointDTO.getMapUrl());

		return ResponseIO.success(stationPoint);
	}
}
