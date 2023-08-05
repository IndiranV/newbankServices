package org.in.com.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.api.io.BusIO;
import org.in.com.controller.api.io.BusVehicleIO;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.api.io.ScheduleIO;
import org.in.com.controller.api.io.StageFareIO;
import org.in.com.controller.api.io.StageIO;
import org.in.com.controller.api.io.StationIO;
import org.in.com.controller.api.io.StationPointIO;
import org.in.com.controller.api.io.TripIO;
import org.in.com.controller.api.io.TripInfoIO;
import org.in.com.controller.api.io.TripStatusIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SearchService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/json/{operatorCode}/{username}/{apiToken}/trip")
public class ApiTripController extends BaseController {

	@Autowired
	AuthService authService;
	@Autowired
	TripService tripService;
	@Autowired
	SearchService searchService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleVisibilityService visibilityService;

	@RequestMapping(value = "/schedule/{tripDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getActiveTripSchedule(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<TripIO> tripList = new ArrayList<>();
		if (!DateUtil.isValidDate(tripDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(DateUtil.getDateTime(tripDate));
		if (DateUtil.getDayDifferent(searchDTO.getTravelDate(), DateUtil.NOW()) > 5) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE, "Last 5 days data only available!");
		}

		List<TripDTO> list = searchService.getAllTrips(authDTO, searchDTO);

		for (TripDTO tripDTO : list) {
			TripIO tripIO = new TripIO();

			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setName(tripDTO.getSchedule().getName());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			schedule.setActiveFrom(tripDTO.getSchedule().getActiveFrom());
			schedule.setActiveTo(tripDTO.getSchedule().getActiveTo());
			schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
			schedule.setDayOfWeek(tripDTO.getSchedule().getDayOfWeek());
			tripIO.setSchedule(schedule);

			// Bus
			BusIO busIO = new BusIO();
			busIO.setName(tripDTO.getBus().getName());
			busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
			busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
			busIO.setTotalSeatCount(tripDTO.getBus().getReservableLayoutSeatCount());
			busIO.setCode(tripDTO.getBus().getCode());
			tripIO.setBus(busIO);

			tripIO.setBookedAmount(tripDTO.getTotalBookedAmount());
			tripIO.setBookedSeatCount(tripDTO.getBookedSeatCount());
			tripIO.setCancelledAmount(tripDTO.getTotalCancelledAmount());
			tripIO.setCancelledSeatCount(tripDTO.getCancelledSeatCount());

			List<StageIO> stageList = new ArrayList<>();

			// Stage
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				StageIO stageIO = new StageIO();
				stageIO.setCode(stageDTO.getCode());

				StationIO fromStation = new StationIO();
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

				StationIO toStation = new StationIO();
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				toStation.setName(stageDTO.getToStation().getStation().getName());
				toStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

				stageIO.setStageSequence(stageDTO.getStageSequence());

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

				List<StationPointIO> fromStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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
					pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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
			if (tripDTO.getTripStatus() != null) {
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
			}
			tripIO.setTripStatus(tripStatusIO);

			// Copy Trip informations
			if (tripDTO.getTripInfo() != null) {
				TripInfoIO tripInfo = new TripInfoIO();
				tripInfo.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
				tripInfo.setDriverName(tripDTO.getTripInfo().getDriverName());
				tripInfo.setRemarks(tripDTO.getTripInfo().getRemarks());

				if (tripDTO.getTripInfo().getBusVehicle() != null) {
					BusVehicleIO busVehicleIO = new BusVehicleIO();
					busVehicleIO.setName(tripDTO.getTripInfo().getBusVehicle().getName());
					busVehicleIO.setCode(tripDTO.getTripInfo().getBusVehicle().getCode());
					busVehicleIO.setRegistrationDate(tripDTO.getTripInfo().getBusVehicle().getRegistrationDate());
					busVehicleIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
					busVehicleIO.setLicNumber(tripDTO.getTripInfo().getBusVehicle().getLicNumber());
					busVehicleIO.setGpsDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
					tripInfo.setBusVehicle(busVehicleIO);
				}
				tripIO.setTripInfo(tripInfo);
			}
			tripList.add(tripIO);
		}
		return ResponseIO.success(tripList);
	}
}
