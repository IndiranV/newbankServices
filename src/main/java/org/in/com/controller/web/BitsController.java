package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusTypeCategoryDetailsIO;
import org.in.com.controller.web.io.BusTypeCategoryIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.EventTriggerIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleCategoryIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.StageFareIO;
import org.in.com.controller.web.io.StageIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.controller.web.io.TicketIO;
import org.in.com.controller.web.io.TripIO;
import org.in.com.controller.web.io.TripInfoIO;
import org.in.com.controller.web.io.TripScheduleControlIO;
import org.in.com.controller.web.io.TripStatusIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.VehicleAttendantIO;
import org.in.com.controller.web.io.VehicleDriverIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusTypeCategoryDTO;
import org.in.com.dto.BusTypeCategoryDetailsDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.EventTriggerDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AttendantCategoryEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.BusService;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.NamespaceService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ReportQueryService;
import org.in.com.service.SearchService;
import org.in.com.service.SeatVisibilityReportService;
import org.in.com.service.StationService;
import org.in.com.service.TicketService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.TokenEncrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/bits")
public class BitsController extends BaseController {
	@Autowired
	StationService stationService;
	@Autowired
	BusService busService;
	@Autowired
	AuthService authService;
	@Autowired
	SearchService searchService;
	@Autowired
	UserService userService;
	@Autowired
	TicketService ticketService;
	@Autowired
	SeatVisibilityReportService seatVisibilityReportService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	BusVehicleDriverService driverService;
	@Autowired
	BusVehicleService busVehicleService;
	@Autowired
	ReportQueryService queryService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	BusBreakevenService busBreakevenService;
	@Autowired
	TripService tripService;

	@RequestMapping(value = "/stations", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getAllStations() throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		List<StationDTO> list = stationService.getAllStations();
		for (StationDTO stationDTO : list) {
			StationIO stationio = new StationIO();
			stationio.setCode(stationDTO.getCode());
			stationio.setName(stationDTO.getName());
			stationio.setActiveFlag(stationDTO.getActiveFlag());
			StateIO state = new StateIO();
			state.setCode(stationDTO.getState().getCode());
			state.setName(stationDTO.getState().getName());
			stationio.setState(state);
			stations.add(stationio);
		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/bustype/category", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusTypeCategoryIO>> getBusTypeCategory() throws Exception {
		List<BusTypeCategoryIO> busIOList = new ArrayList<BusTypeCategoryIO>();
		List<BusTypeCategoryDTO> list = busService.getBusTypeCategory();
		for (BusTypeCategoryDTO dto : list) {
			BusTypeCategoryIO seatTypeIO = new BusTypeCategoryIO();
			seatTypeIO.setCode(dto.getCode());
			seatTypeIO.setCode(dto.getName());
			seatTypeIO.setActiveFlag(dto.getActiveFlag());
			List<BusTypeCategoryDetailsIO> categoryList = new ArrayList<>();
			for (BusTypeCategoryDetailsDTO detailsDTO : dto.getCategoryList()) {
				BusTypeCategoryDetailsIO detailsIO = new BusTypeCategoryDetailsIO();
				detailsIO.setCode(detailsDTO.getCode());
				detailsIO.setName(detailsDTO.getName());
				categoryList.add(detailsIO);
			}
			seatTypeIO.setCategoryList(categoryList);

			busIOList.add(seatTypeIO);
		}
		return ResponseIO.success(busIOList);
	}

	@RequestMapping(value = "/{namespaceCode}/schedule/trip/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getActiveTripSchedule(@PathVariable("namespaceCode") String namespaceCode, @PathVariable("tripDate") String tripDate) throws Exception {
		List<TripIO> tripList = new ArrayList<>();
		try {
			AuthDTO authDTO = authService.getGuestAuthendtication(namespaceCode, DeviceMediumEM.WEB_USER);
			if (!DateUtil.isValidDate(tripDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(new DateTime(tripDate));
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
				schedule.setPnrStartCode(tripDTO.getSchedule().getPnrStartCode());
				schedule.setDayOfWeek(tripDTO.getSchedule().getDayOfWeek());
				if (tripDTO.getSchedule().getCategory() != null) {
					ScheduleCategoryIO categoryIO = new ScheduleCategoryIO();
					categoryIO.setCode(tripDTO.getSchedule().getCategory().getCode());
					categoryIO.setName(tripDTO.getSchedule().getCategory().getName());
					schedule.setCategory(categoryIO);
				}
				tripIO.setSchedule(schedule);

				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getReservableLayoutSeatCount());
				busIO.setCode(tripDTO.getBus().getCode());
				tripIO.setBus(busIO);
				tripIO.setBookedSeatCount(tripDTO.getBookedSeatCount());
				tripIO.setTotalBookedAmount(tripDTO.getTotalBookedAmount());

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
					fromStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					toStation.setCode(stageDTO.getToStation().getStation().getCode());
					toStation.setName(stageDTO.getToStation().getStation().getName());
					stageIO.setStageSequence(stageDTO.getStageSequence());
					toStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						fromStationPoint.add(pointIO);
					}
					List<StationPointIO> toStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
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

				DateTime now = DateUtil.NOW();
				StageStationDTO firstStationDTO = tripDTO.getStage().getFromStation();
				List<TripScheduleControlIO> statusList = new ArrayList<TripScheduleControlIO>();
				for (ScheduleControlDTO controlDTO : tripDTO.getSchedule().getControlList()) {
					TripScheduleControlIO control = new TripScheduleControlIO();
					if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(controlDTO.getGroup().getCode());
						groupIO.setActiveFlag(controlDTO.getGroup().getActiveFlag());
						groupIO.setName(controlDTO.getGroup().getName());
						control.setGroup(groupIO);
					}
					if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0) {
						StationIO fromStationIO = new StationIO();
						fromStationIO.setCode(controlDTO.getFromStation().getCode());
						fromStationIO.setActiveFlag(controlDTO.getFromStation().getActiveFlag());
						fromStationIO.setName(controlDTO.getFromStation().getName());
						control.setFromStation(fromStationIO);
						StationIO toStationIO = new StationIO();
						toStationIO.setCode(controlDTO.getToStation().getCode());
						toStationIO.setActiveFlag(controlDTO.getToStation().getActiveFlag());
						toStationIO.setName(controlDTO.getToStation().getName());
						control.setToStation(toStationIO);
					}
					if (controlDTO.getAllowBookingFlag() != 1) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStationDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStationDTO.getMinitues()));
					if (minutiesDiff >= controlDTO.getOpenMinitues()) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_YET_OPEN);
					}
					control.setOpenDate(DateUtil.minusMinituesToDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStationDTO.getMinitues()), Math.abs(controlDTO.getOpenMinitues())).format("YYYY-MM-DD hh:mm:ss"));
					if (controlDTO.getCloseMinitues() != -1 && minutiesDiff <= controlDTO.getCloseMinitues()) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStationDTO.getMinitues() - controlDTO.getCloseMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					// Identify Close time
					if (controlDTO.getCloseMinitues() == -1) {
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStationDTO.getMinitues() + firstStationDTO.getStationPoint().get(firstStationDTO.getStationPoint().size() - 1).getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					else {
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStationDTO.getMinitues() - controlDTO.getCloseMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					if (minutiesDiff <= controlDTO.getOpenMinitues()) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_OPEN);
					}

					if (tripDTO.getTripStatus() == null) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
					}
					control.setTripStatus(getTripStatus(tripDTO.getTripStatus()));

					statusList.add(control);
				}
				tripIO.setStatusList(statusList);
				List<EventTriggerIO> eventList = new ArrayList<EventTriggerIO>();
				for (EventTriggerDTO triggerDTO : tripDTO.getEventList()) {
					EventTriggerIO trigger = new EventTriggerIO();
					trigger.setCode(triggerDTO.getCode());
					trigger.setName(triggerDTO.getName());
					trigger.setEventTime(triggerDTO.getEventTime().format("YYYY-MM-DD hh:mm:ss"));
					BaseIO event = new BaseIO();
					event.setCode(triggerDTO.getTriggerType().getCode());
					event.setName(triggerDTO.getTriggerType().getName());
					trigger.setTriggerType(event);
					eventList.add(trigger);
				}
				tripIO.setEventList(eventList);
				// Copy Trip informations
				if (tripDTO.getTripInfo() != null) {
					TripInfoIO tripInfo = new TripInfoIO();
					tripInfo.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
					tripInfo.setDriverName(tripDTO.getTripInfo().getDriverName());
					tripInfo.setDriverName2(tripDTO.getTripInfo().getDriverName2());
					tripInfo.setDriverMobile2(tripDTO.getTripInfo().getDriverMobile2());
					tripInfo.setAttenderName(tripDTO.getTripInfo().getAttenderName());
					tripInfo.setAttenderMobile(tripDTO.getTripInfo().getAttenderMobile());
					tripInfo.setRemarks(tripDTO.getTripInfo().getRemarks());
					tripInfo.setNotificationStatus(tripDTO.getTripInfo().getNotificationStatusCodes().split(Text.COMMA));
					tripInfo.setTripStartDateTime(tripDTO.getTripInfo().getTripStartDateTime() != null ? tripDTO.getTripInfo().getTripStartDateTime().format(Text.DATE_TIME_DATE4J) : null);
					tripInfo.setTripCloseDateTime(tripDTO.getTripInfo().getTripCloseDateTime() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);
					if (tripDTO.getTripInfo().getBusVehicle() != null) {
						BusVehicleIO busVehicleIO = new BusVehicleIO();
						busVehicleIO.setName(tripDTO.getTripInfo().getBusVehicle().getName());
						busVehicleIO.setCode(tripDTO.getTripInfo().getBusVehicle().getCode());
						busVehicleIO.setRegistrationDate(tripDTO.getTripInfo().getBusVehicle().getRegistrationDate());
						busVehicleIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
						busVehicleIO.setLicNumber(tripDTO.getTripInfo().getBusVehicle().getLicNumber());
						busVehicleIO.setGpsDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());

						BaseIO gpsVendor = new BaseIO();
						gpsVendor.setCode(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null ? tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode() : null);
						gpsVendor.setName(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null ? tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getName() : null);
						busVehicleIO.setGpsDeviceVendor(gpsVendor);

						tripInfo.setBusVehicle(busVehicleIO);
					}

					if (tripDTO.getTripInfo().getPrimaryDriver() != null) {
						VehicleDriverIO primaryDriver = new VehicleDriverIO();
						primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
						primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
						tripInfo.setPrimaryDriver(primaryDriver);
					}
					if (tripDTO.getTripInfo().getSecondaryDriver() != null) {
						VehicleDriverIO secondaryDriver = new VehicleDriverIO();
						secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
						secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
						tripInfo.setSecondaryDriver(secondaryDriver);
					}
					if (tripDTO.getTripInfo().getAttendant() != null) {
						VehicleAttendantIO attendant = new VehicleAttendantIO();
						attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
						attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
						tripInfo.setAttendant(attendant);
					}

					tripIO.setTripInfo(tripInfo);
				}
				tripList.add(tripIO);
			} // Sorting Trips
			Collections.sort(tripList, new Comparator<TripIO>() {
				@Override
				public int compare(TripIO t1, TripIO t2) {
					return new CompareToBuilder().append(t1.getSchedule().getName(), t2.getSchedule().getName()).toComparison();
				}
			});
		}
		catch (ServiceException e) {
			System.out.println("Export Trip to S3 Service Exception:" + namespaceCode + " - " + tripDate + " - " + e.getErrorCode());
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			System.out.println("Export Trip to S3 :" + namespaceCode + " - " + tripDate);
			e.printStackTrace();
		}
		return ResponseIO.success(tripList);
	}

	private TripStatusIO getTripStatus(TripStatusEM statusEM) {
		TripStatusIO status = new TripStatusIO();
		status.setCode(statusEM.getCode());
		status.setName(statusEM.getName());
		return status;
	}

	@RequestMapping(value = "/{namespaceCode}/users", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getUsers(@PathVariable("namespaceCode") String namespaceCode) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		try {
			List<UserDTO> list = userService.getAll(authDTO);
			for (UserDTO userDTO : list) {
				UserIO userIO = new UserIO();
				userIO.setUsername(userDTO.getUsername());
				userIO.setEmail(userDTO.getEmail());
				userIO.setCode(userDTO.getCode());
				userIO.setName(userDTO.getName());
				userIO.setLastname(userDTO.getLastname());
				userIO.setMobile(userDTO.getMobile());
				userIO.setActiveFlag(userDTO.getActiveFlag());
				user.add(userIO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/{namespaceCode}/feedback/ticket/{travelDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketIO>> getTicketsForFeedback(@PathVariable("namespaceCode") String namespaceCode, @PathVariable("travelDate") String travelDate) throws Exception {
		List<TicketIO> tickets = new ArrayList<TicketIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setTripDate(new DateTime(travelDate));
		List<TicketDTO> list = ticketService.getTicketsForFeedback(authDTO, ticketDTO);
		for (TicketDTO dto : list) {
			TicketIO ticket = new TicketIO();
			ticket.setCode(dto.getCode());
			ticket.setPassegerMobleNo(dto.getPassengerMobile());
			ticket.setPassegerEmailId(dto.getPassengerEmailId());
			ticket.setDeviceMedium(dto.getDeviceMedium().getCode());
			tickets.add(ticket);
		}
		return ResponseIO.success(tickets);
	}

	@RequestMapping(value = "/occupancy/summary/{namespaceCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> sendOverallOccupancySummarySMS(@PathVariable("namespaceCode") String namespaceCode) throws Exception {
		AuthDTO authDTO = authService.getGuestAuthendtication(namespaceCode, DeviceMediumEM.WEB_USER);

		seatVisibilityReportService.sendOverallOccupancySummarySMS(authDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{accessToken}/{namespaceCode}/vehicle/driver", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleDriverIO>> getAllDrivers(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		basicValidation(accessToken, namespaceCode);

		List<VehicleDriverIO> vehicleDrivers = new ArrayList<VehicleDriverIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		List<BusVehicleDriverDTO> list = driverService.getAll(authDTO);
		for (BusVehicleDriverDTO driverDTO : list) {
			VehicleDriverIO vehicleDriver = new VehicleDriverIO();
			vehicleDriver.setCode(driverDTO.getCode());
			vehicleDriver.setName(driverDTO.getName());
			vehicleDriver.setLastName(driverDTO.getLastName());
			vehicleDriver.setDateOfBirth(driverDTO.getDateOfBirth());
			vehicleDriver.setBloodGroup(driverDTO.getBloodGroup());
			vehicleDriver.setLicenseNumber(driverDTO.getLicenseNumber());
			vehicleDriver.setBadgeNumber(driverDTO.getBadgeNumber());
			vehicleDriver.setLicenseExpiryDate(driverDTO.getLicenseExpiryDate());
			vehicleDriver.setQualification(driverDTO.getQualification());
			vehicleDriver.setEmployeeCode(driverDTO.getEmployeeCode());
			vehicleDriver.setMobileNumber(driverDTO.getMobileNumber());
			vehicleDriver.setEmergencyContactNumber(driverDTO.getEmergencyContactNumber());
			vehicleDriver.setAadharNo(driverDTO.getAadharNo());
			vehicleDriver.setJoiningDate(driverDTO.getJoiningDate());
			vehicleDriver.setRemarks(driverDTO.getRemarks());
			vehicleDriver.setActiveFlag(driverDTO.getActiveFlag());
			vehicleDrivers.add(vehicleDriver);
		}
		return ResponseIO.success(vehicleDrivers);
	}

	@RequestMapping(value = "/{namespaceCode}/vehicle/driver", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleDriverIO>> getDrivers(@PathVariable("namespaceCode") String namespaceCode) throws Exception {
		List<VehicleDriverIO> vehicleDrivers = new ArrayList<VehicleDriverIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);

		List<BusVehicleDriverDTO> list = driverService.getAll(authDTO);
		for (BusVehicleDriverDTO driverDTO : list) {
			VehicleDriverIO vehicleDriver = new VehicleDriverIO();
			vehicleDriver.setCode(driverDTO.getCode());
			vehicleDriver.setName(driverDTO.getName());
			vehicleDriver.setLastName(driverDTO.getLastName());
			vehicleDriver.setDateOfBirth(driverDTO.getDateOfBirth());
			vehicleDriver.setBloodGroup(driverDTO.getBloodGroup());
			vehicleDriver.setLicenseNumber(driverDTO.getLicenseNumber());
			vehicleDriver.setBadgeNumber(driverDTO.getBadgeNumber());
			vehicleDriver.setLicenseExpiryDate(driverDTO.getLicenseExpiryDate());
			vehicleDriver.setQualification(driverDTO.getQualification());
			vehicleDriver.setEmployeeCode(driverDTO.getEmployeeCode());
			vehicleDriver.setMobileNumber(driverDTO.getMobileNumber());
			vehicleDriver.setEmergencyContactNumber(driverDTO.getEmergencyContactNumber());
			vehicleDriver.setAadharNo(driverDTO.getAadharNo());
			vehicleDriver.setJoiningDate(driverDTO.getJoiningDate());
			vehicleDriver.setRemarks(driverDTO.getRemarks());
			vehicleDriver.setActiveFlag(driverDTO.getActiveFlag());
			vehicleDrivers.add(vehicleDriver);
		}
		return ResponseIO.success(vehicleDrivers);
	}

	@RequestMapping(value = "/{accessToken}/{namespaceCode}/bus/vehicle", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleIO>> getAllBusVehicle(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		basicValidation(accessToken, namespaceCode);

		List<BusVehicleIO> vehicleList = new ArrayList<BusVehicleIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		List<BusVehicleDTO> list = (List<BusVehicleDTO>) busVehicleService.getAll(authDTO);
		for (BusVehicleDTO busVehicleDTO : list) {
			BusVehicleIO busVehicleIO = new BusVehicleIO();
			busVehicleIO.setCode(busVehicleDTO.getCode());
			busVehicleIO.setName(busVehicleDTO.getName());

			BusIO busIO = new BusIO();
			busIO.setSeatCount(busVehicleDTO.getBus().getSeatLayoutCount());
			busIO.setDisplayName(busService.getBusCategoryByCode(busVehicleDTO.getBus().getCategoryCode()));
			busVehicleIO.setBus(busIO);

			BaseIO vehicleType = new BaseIO();
			vehicleType.setCode(busVehicleDTO.getVehicleType().getCode());
			vehicleType.setName(busVehicleDTO.getVehicleType().getName());
			busVehicleIO.setVehicleType(vehicleType);

			busVehicleIO.setRegistrationDate(busVehicleDTO.getRegistrationDate());
			busVehicleIO.setRegistationNumber(busVehicleDTO.getRegistationNumber());
			busVehicleIO.setLicNumber(busVehicleDTO.getLicNumber());
			busVehicleIO.setMobileNumber(busVehicleDTO.getMobileNumber());
			busVehicleIO.setActiveFlag(busVehicleDTO.getActiveFlag());
			vehicleList.add(busVehicleIO);
		}
		return ResponseIO.success(vehicleList);
	}

	@RequestMapping(value = "/{namespaceCode}/bus/vehicle", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleIO>> getBusVehicles(@PathVariable("namespaceCode") String namespaceCode) throws Exception {
		List<BusVehicleIO> vehicleList = new ArrayList<BusVehicleIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		List<BusVehicleDTO> list = (List<BusVehicleDTO>) busVehicleService.getAll(authDTO);
		for (BusVehicleDTO busVehicleDTO : list) {
			BusVehicleIO busVehicleIO = new BusVehicleIO();
			busVehicleIO.setCode(busVehicleDTO.getCode());
			busVehicleIO.setName(busVehicleDTO.getName());

			BaseIO vehicleType = new BaseIO();
			vehicleType.setCode(busVehicleDTO.getVehicleType().getCode());
			vehicleType.setName(busVehicleDTO.getVehicleType().getName());
			busVehicleIO.setVehicleType(vehicleType);

			BaseIO gpsDeviceVendor = new BaseIO();
			gpsDeviceVendor.setCode(busVehicleDTO.getDeviceVendor().getCode());
			gpsDeviceVendor.setName(busVehicleDTO.getDeviceVendor().getName());
			busVehicleIO.setGpsDeviceVendor(gpsDeviceVendor);

			busVehicleIO.setGpsDeviceCode(busVehicleDTO.getGpsDeviceCode());
			busVehicleIO.setRegistrationDate(busVehicleDTO.getRegistrationDate());
			busVehicleIO.setRegistationNumber(busVehicleDTO.getRegistationNumber());
			busVehicleIO.setLicNumber(busVehicleDTO.getLicNumber());
			busVehicleIO.setMobileNumber(busVehicleDTO.getMobileNumber());
			busVehicleIO.setActiveFlag(busVehicleDTO.getActiveFlag());
			vehicleList.add(busVehicleIO);
		}
		return ResponseIO.success(vehicleList);
	}

	@RequestMapping(value = "/{accessToken}/{namespaceCode}/users", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getUsers(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		basicValidation(accessToken, namespaceCode);

		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);

		try {
			List<UserDTO> list = userService.getAll(authDTO);
			for (UserDTO userDTO : list) {
				UserIO userIO = new UserIO();
				userIO.setUsername(userDTO.getUsername());
				userIO.setEmail(userDTO.getEmail());
				userIO.setCode(userDTO.getCode());
				userIO.setName(userDTO.getName());
				userIO.setLastname(userDTO.getLastname());
				userIO.setMobile(userDTO.getMobile());
				userIO.setActiveFlag(userDTO.getActiveFlag());

				GroupIO groupIO = new GroupIO();
				if (userDTO.getGroup() != null) {
					groupIO.setCode(userDTO.getGroup().getCode());
					groupIO.setName(userDTO.getGroup().getName());
					groupIO.setDecription(userDTO.getGroup().getDecription());
					groupIO.setLevel(userDTO.getGroup().getLevel());
				}
				userIO.setGroup(groupIO);
				user.add(userIO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/{accessToken}/{namespaceCode}/vehicle/attendant", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleAttendantIO>> getAllAttendant(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		basicValidation(accessToken, namespaceCode);

		List<VehicleAttendantIO> vehicleAttendantList = new ArrayList<VehicleAttendantIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		List<BusVehicleAttendantDTO> attendantDTOList = driverService.getAllAttendant(authDTO, AttendantCategoryEM.ATTENDANT);

		for (BusVehicleAttendantDTO vehicleAttendantDTO : attendantDTOList) {
			VehicleAttendantIO vehicleAttendant = new VehicleAttendantIO();
			vehicleAttendant.setCode(vehicleAttendantDTO.getCode());
			vehicleAttendant.setName(vehicleAttendantDTO.getName());
			vehicleAttendant.setAge(vehicleAttendantDTO.getAge());
			vehicleAttendant.setMobile(vehicleAttendantDTO.getMobile());
			vehicleAttendant.setAlternateMobile(vehicleAttendantDTO.getAlternateMobile());
			vehicleAttendant.setJoiningDate(vehicleAttendantDTO.getJoiningDate());
			vehicleAttendant.setAddress(vehicleAttendantDTO.getAddress());
			vehicleAttendant.setRemarks(vehicleAttendantDTO.getRemarks());
			vehicleAttendant.setActiveFlag(vehicleAttendantDTO.getActiveFlag());
			vehicleAttendantList.add(vehicleAttendant);
		}
		return ResponseIO.success(vehicleAttendantList);
	}

	@RequestMapping(value = "/{namespaceCode}/vehicle/attendant", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleAttendantIO>> getAttendants(@PathVariable("namespaceCode") String namespaceCode) throws Exception {
		List<VehicleAttendantIO> vehicleAttendantList = new ArrayList<VehicleAttendantIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		List<BusVehicleAttendantDTO> attendantDTOList = driverService.getAllAttendant(authDTO, AttendantCategoryEM.ATTENDANT);

		for (BusVehicleAttendantDTO vehicleAttendantDTO : attendantDTOList) {
			VehicleAttendantIO vehicleAttendant = new VehicleAttendantIO();
			vehicleAttendant.setCode(vehicleAttendantDTO.getCode());
			vehicleAttendant.setName(vehicleAttendantDTO.getName());
			vehicleAttendant.setAge(vehicleAttendantDTO.getAge());
			vehicleAttendant.setMobile(vehicleAttendantDTO.getMobile());
			vehicleAttendant.setAlternateMobile(vehicleAttendantDTO.getAlternateMobile());
			vehicleAttendant.setJoiningDate(vehicleAttendantDTO.getJoiningDate());
			vehicleAttendant.setAddress(vehicleAttendantDTO.getAddress());
			vehicleAttendant.setRemarks(vehicleAttendantDTO.getRemarks());
			vehicleAttendant.setActiveFlag(vehicleAttendantDTO.getActiveFlag());
			vehicleAttendantList.add(vehicleAttendant);
		}
		return ResponseIO.success(vehicleAttendantList);
	}

	@RequestMapping(value = "/{accessToken}/{namespaceCode}/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<OrganizationIO>> getOrganizations(@PathVariable("accessToken") String accessToken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		basicValidation(accessToken, namespaceCode);

		List<OrganizationIO> organizations = new ArrayList<OrganizationIO>();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		List<OrganizationDTO> list = (List<OrganizationDTO>) organizationService.getAll(authDTO);
		for (OrganizationDTO organizationDTO : list) {
			OrganizationIO organizaionIO = new OrganizationIO();
			organizaionIO.setCode(organizationDTO.getCode());
			organizaionIO.setName(organizationDTO.getName());
			organizaionIO.setShortCode(organizationDTO.getShortCode());
			organizaionIO.setAddress1(organizationDTO.getAddress1());
			organizaionIO.setAddress2(organizationDTO.getAddress2());
			organizaionIO.setContact(organizationDTO.getContact());
			organizaionIO.setPincode(organizationDTO.getPincode());
			organizaionIO.setLatitude(organizationDTO.getLatitude());
			organizaionIO.setLongitude(organizationDTO.getLongitude());

			StateIO stateIO = new StateIO();
			stateIO.setCode(organizationDTO.getStation().getState().getCode());
			stateIO.setName(organizationDTO.getStation().getState().getName());

			StationIO stationIO = new StationIO();
			stationIO.setName(organizationDTO.getStation().getName());
			stationIO.setCode(organizationDTO.getStation().getCode());
			stationIO.setState(stateIO);
			organizaionIO.setStation(stationIO);

			organizaionIO.setActiveFlag(organizationDTO.getActiveFlag());
			organizaionIO.setUserCount(organizationDTO.getUserCount());
			organizations.add(organizaionIO);
		}
		// Sort
		Comparator<OrganizationIO> comp = new BeanComparator("name");
		Collections.sort(organizations, comp);
		return ResponseIO.success(organizations);
	}

	@RequestMapping(value = "/{namespaceCode}/dynamic/map/{queryCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getDynamicReportMap(@PathVariable("namespaceCode") String namespaceCode, @PathVariable("queryCode") String queryCode, @RequestBody List<DBQueryParamDTO> paramList) throws Exception {
		List<Map<String, ?>> list = null;
		AuthDTO authDTO = authService.getGuestAuthendtication(namespaceCode, DeviceMediumEM.WEB_USER);
		DateTime now = DateUtil.NOW();
		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode(queryCode);
		queryService.get(authDTO, reportQueryDTO);
		if (paramList == null) {
			paramList = new ArrayList<DBQueryParamDTO>();
		}
		try {
			if (StringUtils.isNotEmpty(reportQueryDTO.getQuery())) {
				// Default Value
				if (reportQueryDTO.getQuery().contains(":namespaceId")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("namespaceId");
					namespaceParamDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
					paramList.add(namespaceParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":superNamespaceFlag")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("superNamespaceFlag");
					namespaceParamDTO.setValue(ApplicationConfig.getServerZoneCode().equals(authDTO.getNamespaceCode()) ? Numeric.ONE : Numeric.ZERO);
					paramList.add(namespaceParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":loginUserId")) {
					DBQueryParamDTO userParamDTO = new DBQueryParamDTO();
					userParamDTO.setParamName("loginUserId");
					userParamDTO.setValue(String.valueOf(authDTO.getUser().getId()));
					paramList.add(userParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":userCustomerId")) {
					DBQueryParamDTO userParamDTO = new DBQueryParamDTO();
					userParamDTO.setParamName("userCustomerId");
					userParamDTO.setValue(authDTO.getUserCustomer() != null ? String.valueOf(authDTO.getUserCustomer().getId()) : Numeric.ZERO);
					paramList.add(userParamDTO);
				}
				// Date range validation as per days limit
				BitsUtil.validateDateRange(paramList, reportQueryDTO.getDaysLimit());
				list = queryService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);
				reportQueryDTO.setDescription(ErrorCode.SUCCESS.getMessage());
			}
			else {
				reportQueryDTO.setDescription(ErrorCode.INVALID_CODE.getMessage());
			}
		}
		catch (ServiceException e) {
			reportQueryDTO.setDescription(e.getErrorCode() != null ? e.getErrorCode().getMessage() : Text.NA);
			throw e;
		}
		catch (Exception e) {
			reportQueryDTO.setDescription(e.getMessage());
			throw e;
		}
		finally {
			queryService.addReportQueryAuditLog(authDTO, reportQueryDTO, BitsUtil.convertParameterToString(paramList), DateUtil.getSecondsDifferent(now, DateUtil.NOW()), list != null ? list.size() : 0);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{namespaceCode}/breakeven/expense", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getBreakevenExpenses(@PathVariable("namespaceCode") String namespaceCode) throws Exception {
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		JSONArray expenses = busBreakevenService.getBreakevenExpenses(authDTO);
		return ResponseIO.success(expenses);
	}

	@RequestMapping(value = "/{namespaceCode}/trip/{tripCode}/breakeven/expense", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getTripBreakevenExpenses(@PathVariable("namespaceCode") String namespaceCode, @PathVariable("tripCode") String tripCode) throws Exception {
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		JSONArray expenses = tripService.getTripBreakevenExpenses(authDTO, tripDTO);
		return ResponseIO.success(expenses);
	}

	private void basicValidation(String accessToken, String password) {
		String token = TokenEncrypt.encryptString(password);
		if (!accessToken.equals(token)) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
	}
}
