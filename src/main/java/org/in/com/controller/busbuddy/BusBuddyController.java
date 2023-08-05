package org.in.com.controller.busbuddy;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.aggregator.costiv.CostivService;
import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.busbuddy.io.BaseIO;
import org.in.com.controller.busbuddy.io.BusIO;
import org.in.com.controller.busbuddy.io.BusSeatLayoutIO;
import org.in.com.controller.busbuddy.io.BusSeatLayoutV2IO;
import org.in.com.controller.busbuddy.io.BusV2IO;
import org.in.com.controller.busbuddy.io.PaymentTransactionIO;
import org.in.com.controller.busbuddy.io.ResponseIO;
import org.in.com.controller.busbuddy.io.RouteIO;
import org.in.com.controller.busbuddy.io.ScheduleIO;
import org.in.com.controller.busbuddy.io.StageIO;
import org.in.com.controller.busbuddy.io.StationIO;
import org.in.com.controller.busbuddy.io.StationPointIO;
import org.in.com.controller.busbuddy.io.TripChartDetailsIO;
import org.in.com.controller.busbuddy.io.TripChartIO;
import org.in.com.controller.busbuddy.io.TripChartV2IO;
import org.in.com.controller.busbuddy.io.TripIO;
import org.in.com.controller.busbuddy.io.TripTransactionIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.StageFareIO;
import org.in.com.controller.web.io.TripInfoIO;
import org.in.com.controller.web.io.VehicleAttendantIO;
import org.in.com.controller.web.io.VehicleDriverIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.NamespaceTabletSettingsService;
import org.in.com.service.NotificationService;
import org.in.com.service.PaymentTransactionService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.TicketService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/{authtoken}/busbuddy")
public class BusBuddyController extends BaseController {

	@Autowired
	TripService tripService;
	@Autowired
	TicketService ticketService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	PaymentTransactionService paymentTransactionService;
	@Autowired
	CostivService costivService;
	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	BusService busService;
	@Autowired
	NamespaceTabletSettingsService tabletSettingsService;

	@RequestMapping(value = "/{deviceCode}/trip/list/{tripDate}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripIO>> getTripsForTripchart(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		List<TripIO> tripList = new ArrayList<>();
		TripChartDTO tripChartDTO = new TripChartDTO();
		tripChartDTO.setTripDate(new DateTime(tripDate));

		tripService.getTripsForTripchartV2(authDTO, tripChartDTO);
		for (TripDTO tripDTO : tripChartDTO.getTripList()) {
			TripIO tripIO = new TripIO();
			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setName(tripDTO.getSchedule().getName());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
			tripIO.setSchedule(schedule);

			// Bus
			BusIO busIO = new BusIO();
			busIO.setName(tripDTO.getBus().getName());
			busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
			busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
			tripIO.setBus(busIO);
			tripIO.setTripCode(tripDTO.getCode());

			TripInfoIO tripInfo = new TripInfoIO();
			if (tripDTO.getTripInfo() != null && StringUtil.isNotNull(tripDTO.getTripInfo().getExtras())) {
				tripInfo.setStartOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 0));
				tripInfo.setStartDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 1));
				tripInfo.setEndOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 2));
				tripInfo.setEndDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 3));
			}
			tripIO.setTripInfo(tripInfo);

			tripList.add(tripIO);
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/list/{tripDate}/vehicle/{vehicleCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripChartIO>> getTripsByTripDateVehicle(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate, @PathVariable("vehicleCode") String vehicleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripChartIO> tripList = new ArrayList<>();
		BusVehicleDTO vehicle = new BusVehicleDTO();
		vehicle.setCode(vehicleCode);
		DateTime tripDateTime = new DateTime(tripDate);
		try {
			List<TripDTO> list = scheduleTripService.getTripByTripDateAndVehicle(authDTO, tripDateTime, vehicle);
			for (TripDTO tripDTO : list) {
				TripChartIO tripChartIO = new TripChartIO();
				TripIO tripIO = new TripIO();

				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				tripIO.setSchedule(schedule);
				BusIO bus = new BusIO();
				bus.setCode(tripDTO.getBus().getCode());
				tripIO.setBus(bus);
				tripIO.setSyncTime(tripDTO.getSyncTime());

				// Stage
				StageDTO stageDTO = tripDTO.getStage();
				RouteIO stageIO = new RouteIO();
				stageIO.setCode(stageDTO.getCode());

				BaseIO fromStation = new BaseIO();
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				stageIO.setFromStation(fromStation);

				BaseIO toStation = new BaseIO();
				toStation.setName(stageDTO.getToStation().getStation().getName());
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				stageIO.setToStation(toStation);

				tripIO.setRoute(stageIO);

				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTripStartDate(tripDTO.getTripStartTime().format("YYYY-MM-DD hh:mm:ss"));
				tripIO.setTripCloseTime(tripDTO.getTripCloseTime().format("YYYY-MM-DD hh:mm:ss"));

				TripInfoIO tripInfo = new TripInfoIO();
				VehicleDriverIO primaryDriver = new VehicleDriverIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getPrimaryDriver() != null && tripDTO.getTripInfo().getPrimaryDriver().getId() != 0) {
					primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
					primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
					primaryDriver.setMobileNumber(tripDTO.getTripInfo().getPrimaryDriver().getMobileNumber());
				}
				tripInfo.setPrimaryDriver(primaryDriver);
				VehicleDriverIO secondaryDriver = new VehicleDriverIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getSecondaryDriver() != null && tripDTO.getTripInfo().getSecondaryDriver().getId() != 0) {
					secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
					secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
					secondaryDriver.setMobileNumber(tripDTO.getTripInfo().getSecondaryDriver().getMobileNumber());
				}
				tripInfo.setSecondaryDriver(secondaryDriver);
				VehicleAttendantIO attendant = new VehicleAttendantIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getAttendant() != null && tripDTO.getTripInfo().getAttendant().getId() != 0) {
					attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
					attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
					attendant.setMobile(tripDTO.getTripInfo().getAttendant().getMobile());
				}
				tripInfo.setAttendant(attendant);

				VehicleAttendantIO captain = new VehicleAttendantIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getCaptain() != null && tripDTO.getTripInfo().getCaptain().getId() != 0) {
					captain.setCode(tripDTO.getTripInfo().getCaptain().getCode());
					captain.setName(tripDTO.getTripInfo().getCaptain().getName());
					captain.setMobile(tripDTO.getTripInfo().getCaptain().getMobile());
				}
				tripInfo.setCaptain(captain);
				
				if (StringUtil.isNotNull(tripDTO.getTripInfo().getExtras())) {
					tripInfo.setStartOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 0));
					tripInfo.setStartDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 1));
					tripInfo.setEndOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 2));
					tripInfo.setEndDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 3));
				}

				tripIO.setTripInfo(tripInfo);

				tripChartIO.setTrip(tripIO);
				tripList.add(tripChartIO);
			}
		}
		catch (ServiceException e) {
			System.out.println("VehicleCode VE001: " + vehicleCode + "  - " + tripDate);
			throw e;
		}
		catch (Exception e) {
			System.out.println("VehicleCode VE002: " + vehicleCode + "  - " + tripDate);
			e.printStackTrace();
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/{deviceCode}/trip/chart/{tripCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripChartIO> getTripPassengerDetails(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("tripCode") String tripCode) throws Exception {
		TripChartIO tripchart = new TripChartIO();

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		TripChartDTO tripChartDTO = tripService.getTripChart(authDTO, tripDTO);

		if (tripChartDTO.getTrip().getStageList().isEmpty()) {
			throw new ServiceException(ErrorCode.TRIP_NOT_AVAILABLE);
		}
		TripDTO trip = tripChartDTO.getTrip();
		tripchart.setDatetime(trip.getTripOriginTime());
		tripchart.setTripCode(trip.getCode());
		tripchart.setTripBusCode(tripDTO.getBus().getCode());
		tripchart.setSyncTime(trip.getSyncTime());

		// Sorting
		Comparator<StageDTO> stageComp = new BeanComparator("stageSequence");
		Collections.sort(trip.getStageList(), stageComp);

		StageDTO fromStage = trip.getStageList().get(0);
		StageDTO toStage = trip.getStageList().get(trip.getStageList().size() - 1);
		tripchart.setFromTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), fromStage.getFromStation().getMinitues())));
		tripchart.setEndTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), toStage.getToStation().getMinitues())));

		Map<Integer, StationPointDTO> boardingStationPointMap = new HashMap<Integer, StationPointDTO>();
		Map<Integer, StationPointDTO> droppingStationPointMap = new HashMap<Integer, StationPointDTO>();

		Map<String, StationPointIO> boardingPointMap = new HashMap<String, StationPointIO>();
		Map<String, StationPointIO> droppingPointMap = new HashMap<String, StationPointIO>();

		List<StageIO> stageList = new ArrayList<>();
		for (StageDTO stageDTO : trip.getStageList()) {
			StageIO stageIO = new StageIO();
			stageIO.setCode(stageDTO.getCode());
			stageIO.setStageSequence(stageDTO.getStageSequence());

			StationIO fromStation = new StationIO();
			fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
			fromStation.setName(stageDTO.getFromStation().getStation().getName());
			fromStation.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

			StationIO toStation = new StationIO();
			toStation.setCode(stageDTO.getToStation().getStation().getCode());
			toStation.setName(stageDTO.getToStation().getStation().getName());
			toStation.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

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

			for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setStageName(stageDTO.getFromStation().getStation().getName());

				boardingStationPointMap.put(pointDTO.getId(), pointDTO);
				boardingPointMap.put(pointIO.getCode(), pointIO);
			}

			for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}

				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setStageName(stageDTO.getToStation().getStation().getName());

				droppingStationPointMap.put(pointDTO.getId(), pointDTO);
				droppingPointMap.put(pointIO.getCode(), pointIO);
			}
			stageIO.setFromStation(fromStation);
			stageIO.setToStation(toStation);
			stageList.add(stageIO);
		}

		tripchart.setStages(stageList);

		ScheduleIO schedule = new ScheduleIO();
		schedule.setCode(trip.getSchedule().getCode());
		schedule.setName(trip.getSchedule().getName());
		schedule.setServiceNumber(trip.getSchedule().getServiceNumber());
		tripchart.setSchedule(schedule);

		Map<String, List<TripChartDetailsIO>> boardingTicketMap = new HashMap<String, List<TripChartDetailsIO>>();
		Map<String, List<TripChartDetailsIO>> droppingTicketMap = new HashMap<String, List<TripChartDetailsIO>>();
		Map<String, List<TripChartDetailsDTO>> seatTicketMap = new HashMap<String, List<TripChartDetailsDTO>>();
		for (TripChartDetailsDTO chartDTO : tripChartDTO.getTicketDetailsList()) {
			TripChartDetailsIO tripChartDetails = new TripChartDetailsIO();
			tripChartDetails.setTicketCode(chartDTO.getTicketCode());
			tripChartDetails.setSeatCode(chartDTO.getSeatCode());
			tripChartDetails.setSeatName(chartDTO.getSeatName());
			tripChartDetails.setPassengerMobile(chartDTO.getPassengerMobile());
			tripChartDetails.setPassengerName(chartDTO.getPassengerName());
			tripChartDetails.setPassengerAge(chartDTO.getPassengerAge());
			tripChartDetails.setGender(chartDTO.getSeatGendar().getCode());
			tripChartDetails.setRemarks(StringUtil.isNotNull(chartDTO.getRemarks()) ? chartDTO.getRemarks().replaceAll("null", "").replaceAll("-", "") : "");
			tripChartDetails.setTravelStatusCode(chartDTO.getTravelStatus().getCode());
			tripChartDetails.setSeatFare(chartDTO.getSeatFare().add(chartDTO.getAcBusTax()).setScale(0, RoundingMode.CEILING));
			tripChartDetails.setAcBusTax(chartDTO.getAcBusTax());
			tripChartDetails.setBookedDate(chartDTO.getTicketAt().toString());
			tripChartDetails.setBookedType(chartDTO.getBookingType());

			StationIO fromStationIO = new StationIO();
			fromStationIO.setCode(chartDTO.getFromStation().getCode());
			fromStationIO.setName(chartDTO.getFromStation().getName());
			tripChartDetails.setFromStation(fromStationIO);

			StationIO toStationIO = new StationIO();
			toStationIO.setCode(chartDTO.getToStation().getCode());
			toStationIO.setName(chartDTO.getToStation().getName());
			tripChartDetails.setToStation(toStationIO);

			BaseIO userIO = new BaseIO();
			userIO.setCode(chartDTO.getUser().getCode());
			userIO.setName(chartDTO.getUser().getName());
			tripChartDetails.setBookedBy(userIO);

			StationPointIO boardingStationPoint = new StationPointIO();
			if (boardingStationPointMap.get(chartDTO.getBoardingPoint().getId()) != null) {
				StationPointDTO stationPointDTO = boardingStationPointMap.get(chartDTO.getBoardingPoint().getId());
				boardingStationPoint.setName(stationPointDTO.getName());
				boardingStationPoint.setCode(stationPointDTO.getCode());
			}
			else {
				boardingStationPoint.setName("Others");
				boardingStationPoint.setCode("Others");
				boardingStationPoint.setStageName(chartDTO.getFromStation().getName());
				boardingStationPoint.setLandmark(Text.EMPTY);
				boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			}
			tripChartDetails.setBoardingPoint(boardingStationPoint);

			if (boardingPointMap.get(boardingStationPoint.getCode()) == null) {
				boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				boardingPointMap.put(boardingStationPoint.getCode(), boardingStationPoint);
			}

			StationPointIO droppingStationPoint = new StationPointIO();
			if (droppingStationPointMap.get(chartDTO.getDroppingPoint().getId()) != null) {
				StationPointDTO stationPointDTO = droppingStationPointMap.get(chartDTO.getDroppingPoint().getId());
				droppingStationPoint.setName(stationPointDTO.getName());
				droppingStationPoint.setCode(stationPointDTO.getCode());
			}
			else {
				droppingStationPoint.setName("Others");
				droppingStationPoint.setCode("Others");
				droppingStationPoint.setStageName(chartDTO.getToStation().getName());
				droppingStationPoint.setLandmark(Text.EMPTY);
				droppingStationPoint.setDateTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), toStage.getToStation().getMinitues())));
			}
			tripChartDetails.setDroppingPoint(droppingStationPoint);

			if (droppingPointMap.get(droppingStationPoint.getCode()) == null) {
				droppingPointMap.put(droppingStationPoint.getCode(), droppingStationPoint);
			}

			if (boardingTicketMap.get(boardingStationPoint.getCode()) == null) {
				List<TripChartDetailsIO> ticketDetailsList = new ArrayList<>();
				ticketDetailsList.add(tripChartDetails);
				boardingTicketMap.put(boardingStationPoint.getCode(), ticketDetailsList);
			}
			else {
				List<TripChartDetailsIO> ticketDetailsList = boardingTicketMap.get(boardingStationPoint.getCode());
				ticketDetailsList.add(tripChartDetails);
				boardingTicketMap.put(boardingStationPoint.getCode(), ticketDetailsList);
			}

			if (droppingTicketMap.get(droppingStationPoint.getCode()) == null) {
				List<TripChartDetailsIO> ticketDetailsList = new ArrayList<>();
				ticketDetailsList.add(tripChartDetails);
				droppingTicketMap.put(droppingStationPoint.getCode(), ticketDetailsList);
			}
			else {
				List<TripChartDetailsIO> ticketDetailsList = droppingTicketMap.get(droppingStationPoint.getCode());
				ticketDetailsList.add(tripChartDetails);
				droppingTicketMap.put(droppingStationPoint.getCode(), ticketDetailsList);
			}
			// vacantSeats Logic
			List<TripChartDetailsDTO> ticketDetailsList = seatTicketMap.get(chartDTO.getSeatCode()) != null ? seatTicketMap.get(chartDTO.getSeatCode()) : new ArrayList<TripChartDetailsDTO>();
			ticketDetailsList.add(chartDTO);
			seatTicketMap.put(chartDTO.getSeatCode(), ticketDetailsList);
		}
		// Compose vacant Seats Logic
		List<BusSeatLayoutIO> vacantSeats = new ArrayList<>();
		for (BusSeatLayoutDTO layoutDTO : trip.getBus().getBusSeatLayoutDTO().getList()) {
			List<TripChartDetailsDTO> ticketDetailsList = seatTicketMap.get(layoutDTO.getCode()) != null ? seatTicketMap.get(layoutDTO.getCode()) : new ArrayList<TripChartDetailsDTO>();
			if (!isVacantSeats(ticketDetailsList)) {
				BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
				layoutIO.setSeatCode(layoutDTO.getCode());
				layoutIO.setSeatName(layoutDTO.getName());
				vacantSeats.add(layoutIO);
			}
		}

		tripchart.setVacantSeats(vacantSeats);
		List<StationPointIO> boardingPoints = new ArrayList<StationPointIO>();
		List<StationPointIO> boardingStationPoints = new ArrayList<>(boardingPointMap.values());
		for (StationPointIO stationPointIO : boardingStationPoints) {
			StationPointIO boardingPoint = new StationPointIO();
			boardingPoint.setCode(stationPointIO.getCode());
			boardingPoint.setName(stationPointIO.getName());
			boardingPoint.setLandmark(stationPointIO.getLandmark());
			boardingPoint.setStageName(stationPointIO.getStageName());
			boardingPoint.setDateTime(stationPointIO.getDateTime());
			if (boardingTicketMap.get(stationPointIO.getCode()) != null) {
				boardingPoint.setSeats(boardingTicketMap.get(stationPointIO.getCode()));
			}
			boardingPoints.add(boardingPoint);
		}

		// Sorting Bus Seat Layout
		Comparator<StationPointIO> boardingComp = new BeanComparator("dateTime");
		Collections.sort(boardingPoints, boardingComp);

		List<StationPointIO> droppingPoints = new ArrayList<StationPointIO>();
		List<StationPointIO> droppingStationPoints = new ArrayList<>(droppingPointMap.values());
		for (StationPointIO stationPointIO : droppingStationPoints) {
			StationPointIO droppingPoint = new StationPointIO();
			droppingPoint.setCode(stationPointIO.getCode());
			droppingPoint.setName(stationPointIO.getName());
			droppingPoint.setLandmark(stationPointIO.getLandmark());
			droppingPoint.setStageName(stationPointIO.getStageName());
			droppingPoint.setDateTime(stationPointIO.getDateTime());
			if (droppingTicketMap.get(stationPointIO.getCode()) != null) {
				droppingPoint.setSeats(droppingTicketMap.get(stationPointIO.getCode()));
			}
			droppingPoints.add(droppingPoint);
		}

		// Sorting Bus Seat Layout
		Comparator<StationPointIO> droppingComp = new BeanComparator("dateTime");
		Collections.sort(droppingPoints, droppingComp);

		tripchart.setBoardingPoints(boardingPoints);
		tripchart.setDroppingPoints(droppingPoints);

		return ResponseIO.success(tripchart);
	}

	@RequestMapping(value = "/list/{tripDate}/vehicle/{vehicleCode}/v2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripIO>> getTripsByTripDateVehicleV2(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate, @PathVariable("vehicleCode") String vehicleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripIO> tripList = new ArrayList<>();
		BusVehicleDTO vehicle = new BusVehicleDTO();
		vehicle.setCode(vehicleCode);
		DateTime tripDateTime = new DateTime(tripDate);
		try {
			List<TripDTO> list = scheduleTripService.getTripByTripDateAndVehicle(authDTO, tripDateTime, vehicle);
			for (TripDTO tripDTO : list) {
				TripIO tripIO = new TripIO();

				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				tripIO.setSchedule(schedule);
				BusIO bus = new BusIO();
				bus.setCode(tripDTO.getBus().getCode());
				tripIO.setBus(bus);
				tripIO.setSyncTime(tripDTO.getSyncTime());

				// Stage
				StageDTO stageDTO = tripDTO.getStage();
				RouteIO stageIO = new RouteIO();
				stageIO.setCode(stageDTO.getCode());

				BaseIO fromStation = new BaseIO();
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				stageIO.setFromStation(fromStation);

				BaseIO toStation = new BaseIO();
				toStation.setName(stageDTO.getToStation().getStation().getName());
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				stageIO.setToStation(toStation);

				tripIO.setRoute(stageIO);

				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTripStartDate(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD hh:mm:ss"));
				tripIO.setTripCloseTime(tripDTO.getTripInfo() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);

				TripInfoIO tripInfo = new TripInfoIO();
				VehicleDriverIO primaryDriver = new VehicleDriverIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getPrimaryDriver() != null && tripDTO.getTripInfo().getPrimaryDriver().getId() != 0) {
					primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
					primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
					primaryDriver.setMobileNumber(tripDTO.getTripInfo().getPrimaryDriver().getMobileNumber());
				}
				tripInfo.setPrimaryDriver(primaryDriver);
				VehicleDriverIO secondaryDriver = new VehicleDriverIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getSecondaryDriver() != null && tripDTO.getTripInfo().getSecondaryDriver().getId() != 0) {
					secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
					secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
					secondaryDriver.setMobileNumber(tripDTO.getTripInfo().getSecondaryDriver().getMobileNumber());
				}
				tripInfo.setSecondaryDriver(secondaryDriver);
				VehicleAttendantIO attendant = new VehicleAttendantIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getAttendant() != null && tripDTO.getTripInfo().getAttendant().getId() != 0) {
					attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
					attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
					attendant.setMobile(tripDTO.getTripInfo().getAttendant().getMobile());
				}
				tripInfo.setAttendant(attendant);
				
				VehicleAttendantIO captain = new VehicleAttendantIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getCaptain() != null && tripDTO.getTripInfo().getCaptain().getId() != 0) {
					captain.setCode(tripDTO.getTripInfo().getCaptain().getCode());
					captain.setName(tripDTO.getTripInfo().getCaptain().getName());
					captain.setMobile(tripDTO.getTripInfo().getCaptain().getMobile());
				}
				tripInfo.setCaptain(captain);

				if (StringUtil.isNotNull(tripDTO.getTripInfo().getExtras())) {
					tripInfo.setStartOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 0));
					tripInfo.setStartDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 1));
					tripInfo.setEndOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 2));
					tripInfo.setEndDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 3));
				}

				tripIO.setTripInfo(tripInfo);
				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			System.out.println("VehicleCode VE001: " + vehicleCode + "  - " + tripDate);
			throw e;
		}
		catch (Exception e) {
			System.out.println("VehicleCode VE002: " + vehicleCode + "  - " + tripDate);
			e.printStackTrace();
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/{deviceCode}/trip/chart/{tripCode}/v2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripChartV2IO> getTripPassengerDetailsV2(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("tripCode") String tripCode) throws Exception {
		TripChartV2IO tripchart = new TripChartV2IO();

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		TripChartDTO tripChartDTO = tripService.getTripChart(authDTO, tripDTO);

		if (tripChartDTO.getTrip().getStageList().isEmpty()) {
			throw new ServiceException(ErrorCode.TRIP_NOT_AVAILABLE);
		}
		TripDTO trip = tripChartDTO.getTrip();
		tripchart.setDatetime(trip.getTripOriginTime());
		tripchart.setTripCode(trip.getCode());
		tripchart.setTripBusCode(tripDTO.getBus().getCode());
		tripchart.setSyncTime(trip.getSyncTime());

		// Sorting
		Comparator<StageDTO> stageComp = new BeanComparator("stageSequence");
		Collections.sort(trip.getStageList(), stageComp);

		StageDTO fromStage = trip.getStageList().get(0);
		// Sorting
		Comparator<StationPointDTO> compFromPoint = new BeanComparator("minitues");
		Collections.sort(fromStage.getFromStation().getStationPoint(), compFromPoint);
		StationPointDTO fromStageStationPoint = fromStage.getFromStation().getStationPoint().get(0);
		StageDTO toStage = trip.getStageList().get(trip.getStageList().size() - 1);

		Comparator<StationPointDTO> compToPoint = new BeanComparator("minitues").reversed();
		Collections.sort(toStage.getToStation().getStationPoint(), compToPoint);
		StationPointDTO toStageStationPoint = toStage.getToStation().getStationPoint().get(0);
		if (fromStageStationPoint.getCreditDebitFlag().equals("Cr")) {
			tripchart.setFromTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), fromStage.getFromStation().getMinitues() + fromStageStationPoint.getMinitues())));
		}
		else if (fromStageStationPoint.getCreditDebitFlag().equals("Dr")) {
			tripchart.setFromTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), fromStage.getFromStation().getMinitues() - fromStageStationPoint.getMinitues())));
		}

		if (toStageStationPoint.getCreditDebitFlag().equals("Cr")) {
			tripchart.setEndTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), toStage.getToStation().getMinitues() + toStageStationPoint.getMinitues())));
		}
		else if (toStageStationPoint.getCreditDebitFlag().equals("Dr")) {
			tripchart.setEndTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), toStage.getToStation().getMinitues() - toStageStationPoint.getMinitues())));
		}

		Map<Integer, StationPointIO> boardingPointMap = new HashMap<Integer, StationPointIO>();
		Map<Integer, StationPointIO> droppingPointMap = new HashMap<Integer, StationPointIO>();

		List<StageIO> stageList = new ArrayList<>();
		for (StageDTO stageDTO : trip.getStageList()) {
			StageIO stageIO = new StageIO();
			stageIO.setCode(stageDTO.getCode());
			stageIO.setStageSequence(stageDTO.getStageSequence());

			StationIO fromStation = new StationIO();
			fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
			fromStation.setName(stageDTO.getFromStation().getStation().getName());
			fromStation.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

			StationIO toStation = new StationIO();
			toStation.setCode(stageDTO.getToStation().getStation().getCode());
			toStation.setName(stageDTO.getToStation().getStation().getName());
			toStation.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

			List<StationPointIO> boardingPointList = new ArrayList<StationPointIO>();
			for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setStageName(stageDTO.getFromStation().getStation().getName());
				pointIO.setLongitude(pointDTO.getLongitude());
				pointIO.setLatitude(pointDTO.getLatitude());

				boardingPointList.add(pointIO);
				boardingPointMap.put(pointDTO.getId(), pointIO);
			}
			fromStation.setStationPoint(boardingPointList);

			List<StationPointIO> droppingPointList = new ArrayList<StationPointIO>();
			for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), stageDTO.getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}

				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setStageName(stageDTO.getToStation().getStation().getName());
				pointIO.setLongitude(pointDTO.getLongitude());
				pointIO.setLatitude(pointDTO.getLatitude());
				droppingPointList.add(pointIO);

				droppingPointMap.put(pointDTO.getId(), pointIO);
			}
			toStation.setStationPoint(droppingPointList);

			stageIO.setFromStation(fromStation);
			stageIO.setToStation(toStation);
			stageList.add(stageIO);
		}

		tripchart.setStages(stageList);

		ScheduleIO schedule = new ScheduleIO();
		schedule.setCode(trip.getSchedule().getCode());
		schedule.setName(trip.getSchedule().getName());
		schedule.setServiceNumber(trip.getSchedule().getServiceNumber());
		tripchart.setSchedule(schedule);
		List<TripChartDetailsIO> ticketDetailsList = new ArrayList<>();

		for (TripChartDetailsDTO chartDTO : tripChartDTO.getTicketDetailsList()) {
			TripChartDetailsIO tripChartDetails = new TripChartDetailsIO();
			tripChartDetails.setTicketCode(chartDTO.getTicketCode());
			tripChartDetails.setSeatCode(chartDTO.getSeatCode());
			tripChartDetails.setSeatName(chartDTO.getSeatName());
			tripChartDetails.setPassengerMobile(chartDTO.getPassengerMobile());
			tripChartDetails.setPassengerName(chartDTO.getPassengerName());
			tripChartDetails.setPassengerAge(chartDTO.getPassengerAge());
			tripChartDetails.setGender(chartDTO.getSeatGendar().getCode());
			tripChartDetails.setRemarks(StringUtil.isNotNull(chartDTO.getRemarks()) ? chartDTO.getRemarks().replaceAll("null", "").replaceAll("-", "") : "");
			tripChartDetails.setTravelStatusCode(chartDTO.getTravelStatus().getCode());
			tripChartDetails.setSeatFare(chartDTO.getSeatFare().add(chartDTO.getAcBusTax()).setScale(0, RoundingMode.CEILING));
			tripChartDetails.setAcBusTax(chartDTO.getAcBusTax());
			tripChartDetails.setBookedDate(chartDTO.getTicketAt().toString());
			tripChartDetails.setBookedType(StringUtil.isNotNull(chartDTO.getBookingType()) ? chartDTO.getBookingType() : "Online");
			tripChartDetails.setTicketStatusCode(chartDTO.getTicketStatus().getCode());
			tripChartDetails.setUpdatedAt(chartDTO.getTicketUpdatedAt().compareTo(chartDTO.getTicketSeatUpdatedAt()) == 1 ? chartDTO.getTicketUpdatedAt().format("YYYY-MM-DD hh:mm:ss") : chartDTO.getTicketSeatUpdatedAt().format("YYYY-MM-DD hh:mm:ss"));
			StationIO fromStationIO = new StationIO();
			fromStationIO.setCode(chartDTO.getFromStation().getCode());
			fromStationIO.setName(chartDTO.getFromStation().getName());
			tripChartDetails.setFromStation(fromStationIO);

			StationIO toStationIO = new StationIO();
			toStationIO.setCode(chartDTO.getToStation().getCode());
			toStationIO.setName(chartDTO.getToStation().getName());
			tripChartDetails.setToStation(toStationIO);

			BaseIO userIO = new BaseIO();
			userIO.setCode(chartDTO.getUser().getCode());
			userIO.setName(chartDTO.getUser().getName());
			tripChartDetails.setBookedBy(userIO);

			StationPointIO boardingStationPoint = new StationPointIO();
			if (boardingPointMap.get(chartDTO.getBoardingPoint().getId()) != null) {
				StationPointIO stationPointDTO = boardingPointMap.get(chartDTO.getBoardingPoint().getId());
				boardingStationPoint.setName(stationPointDTO.getName());
				boardingStationPoint.setCode(stationPointDTO.getCode());
			}
			else {
				boardingStationPoint.setName("Others");
				boardingStationPoint.setCode("Others");
				boardingStationPoint.setStageName(chartDTO.getFromStation().getName());
				boardingStationPoint.setLandmark(Text.EMPTY);
				boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			}
			tripChartDetails.setBoardingPoint(boardingStationPoint);

			StationPointIO droppingStationPoint = new StationPointIO();
			if (droppingPointMap.get(chartDTO.getDroppingPoint().getId()) != null) {
				StationPointIO stationPoint = droppingPointMap.get(chartDTO.getDroppingPoint().getId());
				droppingStationPoint.setName(stationPoint.getName());
				droppingStationPoint.setCode(stationPoint.getCode());
			}
			else {
				droppingStationPoint.setName("Others");
				droppingStationPoint.setCode("Others");
				droppingStationPoint.setStageName(chartDTO.getToStation().getName());
				droppingStationPoint.setLandmark(Text.EMPTY);
				droppingStationPoint.setDateTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(trip.getTripDate(), toStage.getToStation().getMinitues())));
			}
			tripChartDetails.setDroppingPoint(droppingStationPoint);

			ticketDetailsList.add(tripChartDetails);
		}
		tripchart.setTicketDetails(ticketDetailsList);
		return ResponseIO.success(tripchart);
	}

	@RequestMapping(value = "/{deviceCode}/trip/bus/layout/{busCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BusV2IO> getTripBusLayout(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("busCode") String busCode) throws Exception {
		List<BusSeatLayoutV2IO> layoutIOList = new ArrayList<BusSeatLayoutV2IO>();

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(busCode);

		Collection<BusSeatLayoutDTO> list = (Collection<BusSeatLayoutDTO>) busService.getBusLayout(authDTO, busDTO);
		BusV2IO bus = new BusV2IO();
		bus.setCode(busCode);
		int reservationSeatCount = 0;
		for (BusSeatLayoutDTO layoutDTO : list) {
			BusSeatLayoutV2IO layoutIO = new BusSeatLayoutV2IO();
			layoutIO.setSeatCode(layoutDTO.getCode());
			layoutIO.setSeatName(layoutDTO.getName());
			BaseIO seatTypeIO = new BaseIO();
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
			if (layoutDTO.getBusSeatType().isReservation()) {
				reservationSeatCount++;
			}
			layoutIOList.add(layoutIO);
		}
		bus.setSeatLayout(layoutIOList);
		bus.setTotalSeatCount(reservationSeatCount);
		return ResponseIO.success(bus);
	}

	private boolean isVacantSeats(List<TripChartDetailsDTO> ticketDetailsList) {
		for (TripChartDetailsDTO detailsDTO : ticketDetailsList) {
			if (StringUtil.isNotNull(detailsDTO.getTicketCode()) && detailsDTO.getTravelStatus().getId() != TravelStatusEM.NOT_TRAVELED.getId()) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping(value = "/{deviceCode}/trip/{tripCode}/check/anychange/{syncTime}/v2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> checkRecentTickets(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("tripCode") String tripCode, @PathVariable("syncTime") String syncTime) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		tripService.checkRecentTickets(authDTO, tripDTO, syncTime);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{deviceCode}/ticket/{ticketCode}/travel/status/{travelStatus}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTicketTravelStatus(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("ticketCode") String ticketCode, @PathVariable("travelStatus") String travelStatus, String seatCodeList, String syncId, String remarks) throws Exception {
		BaseIO response = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
		for (int i = 0; i < seatCodeList.split(",").length; i++) {
			TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
			ticketDetailsDTO.setSeatCode(seatCodeList.split(",")[i]);
			ticketDetailsDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(travelStatus));
			ticketDetailsDTO.setActiveFlag(Numeric.ONE_INT);
			ticketDetails.add(ticketDetailsDTO);
		}
		ticketDTO.setTicketDetails(ticketDetails);
		ticketDTO.setRemarks(remarks);
		ticketService.updateTravelStatus(authDTO, ticketDTO);
		response.setCode(syncId);
		response.setActiveFlag(1);
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/{deviceCode}/notification/{ticketCode}/afterboard", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> busbuddyAfterboard(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("ticketCode") String ticketCode, String vehicleNumber, String mobileNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		if (StringUtil.isNull(mobileNumber) || StringUtil.isNull(vehicleNumber)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		if (EhcacheManager.getFreshRequestEhCache().get("afterboard" + authDTO.getUserCode() + ticketCode) == null) {
			notificationService.firebusbuddyAfterboard(authDTO, ticketCode, vehicleNumber, mobileNumber);
			Element element = new Element("afterboard" + authDTO.getUserCode() + ticketCode, ticketCode);
			EhcacheManager.getFreshRequestEhCache().put(element);
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{deviceCode}/commerce/schedule/{tripCode}/stage", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StageIO>> getStageByStageCode(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("tripCode") String tripCode) throws Exception {
		List<StageIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		tripDTO = tripService.getTripDTO(authDTO, tripDTO);

		List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
		// Sorting
		Comparator<StageStationDTO> comp = new BeanComparator("stationSequence");
		Collections.sort(stageList, comp);

		for (StageStationDTO stageStationDTO : stageList) {
			StageIO stageIO = new StageIO();
			StationIO fromStation = new StationIO();
			fromStation.setCode(stageStationDTO.getStation().getCode());
			fromStation.setName(stageStationDTO.getStation().getName());
			stageIO.setStageSequence(stageStationDTO.getStationSequence());
			fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageStationDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

			List<StationPointIO> fromStationPoint = new ArrayList<>();
			for (StationPointDTO pointDTO : stageStationDTO.getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageStationDTO.getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageStationDTO.getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				fromStationPoint.add(pointIO);
			}
			fromStation.setStationPoint(fromStationPoint);
			stageIO.setFromStation(fromStation);
			list.add(stageIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{deviceCode}/sync", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTabletSyncTime(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, String syncTime) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(syncTime)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, syncTime);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{deviceCode}/acknowledge/{ackType}/details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateAcknowledge(@PathVariable("authtoken") String authtoken, @PathVariable("deviceCode") String deviceCode, @PathVariable("ackType") String ackType, HttpServletRequest request) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// Update Sync Time
		putTabletSyncTime(authDTO.getNamespaceCode(), deviceCode, DateUtil.convertDateTime(DateUtil.NOW()));

		Map<String, String> responseParam = new HashMap<String, String>();
		for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
			String fieldName = en.nextElement();
			String fieldValue = request.getParameter(fieldName);
			responseParam.put(fieldName, fieldValue);
		}
		System.out.println(authDTO.getNamespaceCode() + " - " + deviceCode + " - " + responseParam.toString());
		return ResponseIO.success();
	}

	@RequestMapping(value = "/zone/{zoneCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTabletSyncTime(@PathVariable("authtoken") String authtoken, @PathVariable("zoneCode") String zoneCode) throws Exception {
		BaseIO zone = new BaseIO();

		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(zoneCode);
		if (namespaceZone != null) {
			zone.setCode(namespaceZone.getCode());
			zone.setName(namespaceZone.getDomainURL());
			zone.setActiveFlag(Numeric.ONE_INT);
		}
		return ResponseIO.success(zone);
	}

	private void putTabletSyncTime(String namespaceCode, String deviceCode, String syncTime) {
		String key = Text.BUS_BUDDY_SYNC + Text.UNDER_SCORE + namespaceCode + Text.UNDER_SCORE + deviceCode;

		Element element = new Element(key, DateUtil.convertDateTime(DateUtil.getDateTime(syncTime)));
		EhcacheManager.getBusBuddyEhCache().put(element);
	}

	@RequestMapping(value = "/payment/{paymentCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> getUserPaymentTransaction(@PathVariable("authtoken") String authtoken, @PathVariable("paymentCode") String paymentCode) throws Exception {
		PaymentTransactionIO transactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
		paymentTransactionDTO.setCode(paymentCode);
		paymentTransactionDTO = paymentTransactionService.getPaymentTransaction(authDTO, paymentTransactionDTO);
		if (paymentTransactionDTO.getId() != 0) {
			transactionIO.setCode(paymentTransactionDTO.getCode());
			transactionIO.setRemarks(paymentTransactionDTO.getRemarks());
			transactionIO.setAmountReceivedDate(paymentTransactionDTO.getAmountReceivedDate());
			transactionIO.setName(paymentTransactionDTO.getName());
			transactionIO.setAmount(paymentTransactionDTO.getTransactionAmount());
			transactionIO.setTransactionDate(paymentTransactionDTO.getTransactionDate());

			// Transaction Mode
			BaseIO modeIO = new BaseIO();
			modeIO.setCode(paymentTransactionDTO.getTransactionMode().getCode());
			modeIO.setName(paymentTransactionDTO.getTransactionMode().getName());
			transactionIO.setTransactionMode(modeIO);

			// Transaction Type
			BaseIO typeIO = new BaseIO();
			typeIO.setCode(paymentTransactionDTO.getTransactionType().getCode());
			typeIO.setName(paymentTransactionDTO.getTransactionType().getName());
			transactionIO.setTransactionType(typeIO);

			// Ack status
			BaseIO acknowledgeStatus = new BaseIO();
			acknowledgeStatus.setCode(paymentTransactionDTO.getPaymentAcknowledge().getCode());
			acknowledgeStatus.setName(paymentTransactionDTO.getPaymentAcknowledge().getName());
			transactionIO.setAcknowledgeStatus(acknowledgeStatus);

			// User
			BaseIO userIO = new BaseIO();
			userIO.setCode(paymentTransactionDTO.getUser().getCode());
			userIO.setName(paymentTransactionDTO.getUser().getName());
			transactionIO.setUser(userIO);

			// Payment Handle User
			BaseIO handlerUserIO = new BaseIO();
			handlerUserIO.setCode(paymentTransactionDTO.getPaymentHandledByUser().getCode());
			handlerUserIO.setName(paymentTransactionDTO.getPaymentHandledByUser().getName());
			transactionIO.setPaymentHandledBy(handlerUserIO);

			List<PaymentTransactionIO> partialPaymentList = new ArrayList<PaymentTransactionIO>();
			for (PaymentTransactionDTO partialPaymentTransaction : paymentTransactionDTO.getPartialPaymentPaidList()) {
				PaymentTransactionIO partialPayment = new PaymentTransactionIO();
				partialPayment.setCode(partialPaymentTransaction.getCode());
				partialPayment.setRemarks(partialPaymentTransaction.getRemarks());
				partialPayment.setAmountReceivedDate(partialPaymentTransaction.getAmountReceivedDate());
				partialPayment.setName(partialPaymentTransaction.getName());
				partialPayment.setAmount(partialPaymentTransaction.getTransactionAmount());
				partialPayment.setTransactionDate(partialPaymentTransaction.getTransactionDate());

				// Transaction Mode
				BaseIO transactionMode = new BaseIO();
				transactionMode.setCode(partialPaymentTransaction.getTransactionMode().getCode());
				transactionMode.setName(partialPaymentTransaction.getTransactionMode().getName());
				partialPayment.setTransactionMode(transactionMode);

				// Transaction Type
				BaseIO transactionType = new BaseIO();
				transactionType.setCode(partialPaymentTransaction.getTransactionType().getCode());
				transactionType.setName(partialPaymentTransaction.getTransactionType().getName());
				partialPayment.setTransactionType(transactionType);

				// Ack status
				BaseIO acknowledgeStatusIO = new BaseIO();
				acknowledgeStatusIO.setCode(partialPaymentTransaction.getPaymentAcknowledge().getCode());
				acknowledgeStatusIO.setName(partialPaymentTransaction.getPaymentAcknowledge().getName());
				partialPayment.setAcknowledgeStatus(acknowledgeStatusIO);

				// User
				BaseIO user = new BaseIO();
				user.setCode(partialPaymentTransaction.getUser().getCode());
				user.setName(partialPaymentTransaction.getUser().getName());
				partialPayment.setUser(user);

				// Payment Handle User
				BaseIO handlerUser = new BaseIO();
				handlerUser.setCode(partialPaymentTransaction.getPaymentHandledByUser().getCode());
				handlerUser.setName(partialPaymentTransaction.getPaymentHandledByUser().getName());
				partialPayment.setPaymentHandledBy(handlerUser);

				partialPaymentList.add(partialPayment);
			}
			transactionIO.setPartialPaymentList(partialPaymentList);
		}
		return ResponseIO.success(transactionIO);
	}

	@RequestMapping(value = "/trip/{tripCode}/payment/{paymentCode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> saveAgentColletion(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("paymentCode") String paymentCode, @RequestParam(defaultValue = "NA") String remarks) throws Exception {
		BaseIO response = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
		paymentTransactionDTO.setCode(paymentCode);
		paymentTransactionDTO = paymentTransactionService.getPaymentTransaction(authDTO, paymentTransactionDTO);

		if (paymentTransactionDTO.getId() == 0 || paymentTransactionDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_REJECT.getId()) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}

		Map<String, String> transactionDetails = new HashMap<>();
		transactionDetails.put("amount", String.valueOf(paymentTransactionDTO.getTransactionAmount()));
		transactionDetails.put("expenseType", "AGNTCOLN");
		transactionDetails.put("remarks", remarks);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		// Agent Collection
		tripService.saveTripIncomeExpense(authDTO, tripDTO, transactionDetails);
		response.setCode(transactionDetails.get("transactionCode"));
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/trip/{tripCode}/income/expense/{type}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripTransactionIO> saveTripIncomeExpense(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("type") String type, @RequestBody TripTransactionIO tripIncomeExpense) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		Map<String, String> transactionDetails = new HashMap<>();
		transactionDetails.put("transactionCode", tripIncomeExpense.getCode());
		transactionDetails.put("expenseType", type);
		transactionDetails.put("remarks", tripIncomeExpense.getRemarks());
		transactionDetails.put("litres", String.valueOf(tripIncomeExpense.getLitres()));
		transactionDetails.put("pricePerLitre", String.valueOf(tripIncomeExpense.getPricePerLitre()));
		transactionDetails.put("billNumber", tripIncomeExpense.getBillNumber());
		transactionDetails.put("amount", tripIncomeExpense.getAmount() != null ? tripIncomeExpense.getAmount().toString() : Numeric.ZERO);
		transactionDetails.put("odometer", String.valueOf(tripIncomeExpense.getOdometer()));
		transactionDetails.put("paymentContact", tripIncomeExpense.getPaymentContact());
		transactionDetails.put("vendorContact", tripIncomeExpense.getVendorContact());

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		tripService.saveTripIncomeExpense(authDTO, tripDTO, transactionDetails);
		tripIncomeExpense.setCode(transactionDetails.get("transactionCode"));

		return ResponseIO.success(tripIncomeExpense);
	}

	@RequestMapping(value = "/trip/{tripCode}/income/expenses", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> getTripIncomeExpense(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, String expenseType) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		JSONArray response = new JSONArray();
		if (StringUtil.isNotNull(expenseType) && "FUELEXPENSE".equals(expenseType)) {
			response = costivService.getFuelExpenses(authDTO, tripDTO);
		}
		else {
			response = costivService.getTripIncomeExpenses(authDTO, tripDTO);
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/expense/types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> getExpenseTypes(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		JSONArray response = costivService.getExpenseTypes(authDTO);
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/phone/ticket/{ticketCode}/payment/status/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updatePhoneBookTicketPaymentStatus(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestParam(defaultValue = "0", required = true) int paymentStatus, @RequestParam(required = true) String paymentMode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO.setRemarks(paymentMode);

		TicketExtraDTO ticketExtra = new TicketExtraDTO();
		ticketExtra.setPhoneBookPaymentStatus(paymentStatus);
		ticketDTO.setTicketExtra(ticketExtra);

		ticketService.updatePhoneBookTicketPaymentStatus(authDTO, ticketDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/contacts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> getContacts(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		JSONArray response = costivService.getContacts(authDTO);
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public JSONObject getTabletSettings(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		JSONObject response = tabletSettingsService.getNamespaceTabletSettingJson(authDTO);
		return response;
	}

	@RequestMapping(value = "/driver/{driverCode}/trip/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getTripByTripDateAndDriver(@PathVariable("authtoken") String authtoken, @PathVariable("driverCode") String driverCode, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripIO> tripList = new ArrayList<>();
		try {
			BusVehicleDriverDTO busVehicleDriver = new BusVehicleDriverDTO();
			busVehicleDriver.setCode(driverCode);

			DateTime tripDateTime = new DateTime(tripDate);

			List<TripDTO> list = scheduleTripService.getTripByTripDateAndDriver(authDTO, busVehicleDriver, tripDateTime);
			for (TripDTO tripDTO : list) {

				if (tripDTO.getActiveFlag() != Numeric.ONE_INT) {
					continue;
				}
				TripIO tripIO = new TripIO();

				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				tripIO.setSchedule(schedule);

				BusIO bus = new BusIO();
				bus.setCode(tripDTO.getBus().getCode());
				tripIO.setBus(bus);

				tripIO.setSyncTime(tripDTO.getSyncTime());

				// Stage
				StageDTO stageDTO = tripDTO.getStage();
				RouteIO stageIO = new RouteIO();
				stageIO.setCode(stageDTO.getCode());

				BaseIO fromStation = new BaseIO();
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				stageIO.setFromStation(fromStation);

				BaseIO toStation = new BaseIO();
				toStation.setName(stageDTO.getToStation().getStation().getName());
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				stageIO.setToStation(toStation);

				tripIO.setRoute(stageIO);

				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTripStartDate(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD hh:mm:ss"));
				tripIO.setTripCloseTime(tripDTO.getTripInfo() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);

				TripInfoIO tripInfo = new TripInfoIO();
				VehicleDriverIO primaryDriver = new VehicleDriverIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getPrimaryDriver() != null && tripDTO.getTripInfo().getPrimaryDriver().getId() != 0) {
					primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
					primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
					primaryDriver.setMobileNumber(tripDTO.getTripInfo().getPrimaryDriver().getMobileNumber());
				}
				tripInfo.setPrimaryDriver(primaryDriver);

				VehicleDriverIO secondaryDriver = new VehicleDriverIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getSecondaryDriver() != null && tripDTO.getTripInfo().getSecondaryDriver().getId() != 0) {
					secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
					secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
					secondaryDriver.setMobileNumber(tripDTO.getTripInfo().getSecondaryDriver().getMobileNumber());
				}
				tripInfo.setSecondaryDriver(secondaryDriver);

				VehicleAttendantIO attendant = new VehicleAttendantIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getAttendant() != null && tripDTO.getTripInfo().getAttendant().getId() != 0) {
					attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
					attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
					attendant.setMobile(tripDTO.getTripInfo().getAttendant().getMobile());
				}
				tripInfo.setAttendant(attendant);
				
				VehicleAttendantIO captain = new VehicleAttendantIO();
				if (tripDTO.getTripInfo() != null && tripDTO.getTripInfo().getCaptain() != null && tripDTO.getTripInfo().getCaptain().getId() != 0) {
					captain.setCode(tripDTO.getTripInfo().getCaptain().getCode());
					captain.setName(tripDTO.getTripInfo().getCaptain().getName());
					captain.setMobile(tripDTO.getTripInfo().getCaptain().getMobile());
				}
				tripInfo.setCaptain(captain);

				if (StringUtil.isNotNull(tripDTO.getTripInfo().getExtras())) {
					tripInfo.setStartOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 0));
					tripInfo.setStartDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 1));
					tripInfo.setEndOdometer(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 2));
					tripInfo.setEndDateTime(StringUtil.split(tripDTO.getTripInfo().getExtras(), Text.VERTICAL_BAR, 3));
				}

				tripIO.setTripInfo(tripInfo);
				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			System.out.println("DriverCode DR001: " + driverCode + "  - " + tripDate);
			throw e;
		}
		catch (Exception e) {
			System.out.println("DriverCode DR002: " + driverCode + "  - " + tripDate);
			e.printStackTrace();
		}
		return ResponseIO.success(tripList);
	}

}
