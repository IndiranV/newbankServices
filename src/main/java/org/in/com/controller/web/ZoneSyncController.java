package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CalendarAnnouncementIO;
import org.in.com.controller.web.io.FareRuleDetailsIO;
import org.in.com.controller.web.io.MenuEventIO;
import org.in.com.controller.web.io.MenuIO;
import org.in.com.controller.web.io.ReportQueryIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationAreaIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationOtaPartnerIO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ZoneSyncService;
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
@RequestMapping("/{authtoken}/zone")
public class ZoneSyncController extends BaseController {
	@Autowired
	ZoneSyncService syncService;

	@RequestMapping(value = "/station/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<StationIO>> zoneSyncStation(@PathVariable("authtoken") String authtoken, String bitsAuthtoken) throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<StationDTO> list = syncService.zoneSyncStation(authDTO, bitsAuthtoken);
		for (StationDTO stationDTO : list) {
			StationIO stationio = new StationIO();
			stationio.setCode(stationDTO.getCode());
			stationio.setName(stationDTO.getName());
			stationio.setLatitude(StringUtil.isNull(stationDTO.getLatitude(), Text.EMPTY));
			stationio.setLongitude(StringUtil.isNull(stationDTO.getLongitude(), Text.EMPTY));
			stationio.setRadius(stationDTO.getRadius());
			stationio.setActiveFlag(stationDTO.getActiveFlag());
			stations.add(stationio);
		}

		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/amenities/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<AmenitiesIO>> zoneSyncAmenities(@PathVariable("authtoken") String authtoken, String bitsAuthtoken) throws Exception {
		List<AmenitiesIO> amenties = new ArrayList<AmenitiesIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<AmenitiesDTO> list = syncService.zoneSyncAmenities(authDTO, bitsAuthtoken);
		for (AmenitiesDTO amentiesDTO : list) {
			AmenitiesIO amentiesio = new AmenitiesIO();
			amentiesio.setCode(amentiesDTO.getCode());
			amentiesio.setName(amentiesDTO.getName());
			amentiesio.setActiveFlag(amentiesDTO.getActiveFlag());
			amentiesio.setCode(amentiesDTO.getCode());
			amenties.add(amentiesio);
		}
		return ResponseIO.success(amenties);
	}

	@RequestMapping(value = "/report/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ReportQueryIO>> zoneSyncReportQuery(@PathVariable("authtoken") String authtoken, String bitsAuthtoken) throws Exception {
		List<ReportQueryIO> list = new ArrayList<ReportQueryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ReportQueryDTO> DTOList = syncService.zoneSyncReportQuery(authDTO, bitsAuthtoken);
		for (ReportQueryDTO queryDTO : DTOList) {
			ReportQueryIO queryIO = new ReportQueryIO();
			queryIO.setCode(queryDTO.getCode());
			queryIO.setName(queryDTO.getName());
			queryIO.setDescription(queryDTO.getDescription());
			queryIO.setQuery(queryDTO.getQuery());
			queryIO.setDaysLimit(queryDTO.getDaysLimit());
			queryIO.setActiveFlag(queryDTO.getActiveFlag());
			list.add(queryIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/menu/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<MenuIO>> zoneSyncMenu(@PathVariable("authtoken") String authtoken, String bitsAuthtoken) throws Exception {
		List<MenuIO> menuList = new ArrayList<MenuIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<MenuDTO> list = syncService.zoneSyncMenu(authDTO, bitsAuthtoken);
		for (MenuDTO menuDTO : list) {
			MenuIO menuio = new MenuIO();
			menuio.setCode(menuDTO.getCode());
			menuio.setName(menuDTO.getName());
			menuio.setLink(menuDTO.getLink());
			BaseIO severity = new BaseIO();
			severity.setCode(menuDTO.getSeverity().getCode());
			severity.setName(menuDTO.getSeverity().getName());
			menuio.setSeverity(severity);
			menuio.setDisplayFlag(menuDTO.getDisplayFlag());
			menuio.setActionCode(menuDTO.getActionCode());
			menuio.setActionCode(menuDTO.getActionCode());
			List<String> tagList = new ArrayList<>();
			for (String tag : menuDTO.getTagList()) {
				tagList.add(tag);
			}
			menuio.setTagList(tagList);
			BaseIO productIO = new BaseIO();
			productIO.setCode(menuDTO.getProductType().getCode());
			productIO.setName(menuDTO.getProductType().getName());
			productIO.setActiveFlag(1);
			menuio.setProductType(productIO);

			if (menuDTO.getLookup() != null) {
				MenuIO lookup = new MenuIO();
				lookup.setCode(menuDTO.getLookup().getCode());
				lookup.setName(menuDTO.getLookup().getName());
				lookup.setLink(menuDTO.getLookup().getLink());
				menuio.setLookup(lookup);
			}
			List<MenuEventIO> eventList = new ArrayList<MenuEventIO>();
			for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
				MenuEventIO eventIO = new MenuEventIO();
				eventIO.setCode(eventDTO.getCode());
				BaseIO eventSeverity = new BaseIO();
				eventSeverity.setCode(eventDTO.getSeverity().getCode());
				eventSeverity.setName(eventDTO.getSeverity().getName());
				eventIO.setSeverity(eventSeverity);
				eventIO.setActiveFlag(eventDTO.getActiveFlag());
				eventIO.setName(eventDTO.getName());
				eventIO.setOperationCode(eventDTO.getOperationCode());
				eventIO.setAttr1Value(eventDTO.getAttr1Value());
				eventIO.setPermissionType(MenuEventDTO.getPermission(eventDTO.getPermissionFlag()));
				if (StringUtil.isNull(eventIO.getName())) {
					continue;
				}
				eventList.add(eventIO);
			}
			menuio.setEventList(eventList);
			menuio.setActiveFlag(menuDTO.getActiveFlag());
			menuList.add(menuio);
		}
		return ResponseIO.success(menuList);
	}

	@RequestMapping(value = "/fare/rule/{ruleCode}/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<FareRuleDetailsIO>> zoneSyncFareRuleDetails(@PathVariable("authtoken") String authtoken, @PathVariable("ruleCode") String ruleCode, String bitsAuthtoken) throws Exception {
		List<FareRuleDetailsIO> fareRuleDetailsList = new ArrayList<FareRuleDetailsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(ruleCode)) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		fareRuleDTO.setCode(ruleCode);

		List<FareRuleDetailsDTO> list = syncService.zoneSyncFareRuleDetails(authDTO, bitsAuthtoken, fareRuleDTO);
		for (FareRuleDetailsDTO fareRuleDetailsDTO : list) {
			FareRuleDetailsIO fareRuleDetails = new FareRuleDetailsIO();

			StationIO fromStation = new StationIO();
			fromStation.setCode(fareRuleDetailsDTO.getFromStation().getCode());
			fromStation.setName(fareRuleDetailsDTO.getFromStation().getName());
			fareRuleDetails.setFromStation(fromStation);

			StationIO toStation = new StationIO();
			toStation.setCode(fareRuleDetailsDTO.getToStation().getCode());
			toStation.setName(fareRuleDetailsDTO.getToStation().getName());
			fareRuleDetails.setToStation(toStation);

			fareRuleDetails.setDistance(fareRuleDetailsDTO.getDistance());

			fareRuleDetails.setNonAcSeaterMaxFare(fareRuleDetailsDTO.getNonAcSeaterMaxFare());
			fareRuleDetails.setNonAcSeaterMinFare(fareRuleDetailsDTO.getNonAcSeaterMinFare());

			fareRuleDetails.setAcSeaterMinFare(fareRuleDetailsDTO.getAcSeaterMinFare());
			fareRuleDetails.setAcSeaterMaxFare(fareRuleDetailsDTO.getAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleSeaterMinFare(fareRuleDetailsDTO.getMultiAxleSeaterMinFare());
			fareRuleDetails.setMultiAxleSeaterMaxFare(fareRuleDetailsDTO.getMultiAxleSeaterMaxFare());

			fareRuleDetails.setNonAcSleeperLowerMinFare(fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
			fareRuleDetails.setNonAcSleeperLowerMaxFare(fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());

			fareRuleDetails.setNonAcSleeperUpperMinFare(fareRuleDetailsDTO.getNonAcSleeperUpperMinFare());
			fareRuleDetails.setNonAcSleeperUpperMaxFare(fareRuleDetailsDTO.getNonAcSleeperUpperMaxFare());

			fareRuleDetails.setAcSleeperLowerMinFare(fareRuleDetailsDTO.getAcSleeperLowerMinFare());
			fareRuleDetails.setAcSleeperLowerMaxFare(fareRuleDetailsDTO.getAcSleeperLowerMaxFare());

			fareRuleDetails.setAcSleeperUpperMinFare(fareRuleDetailsDTO.getAcSleeperUpperMinFare());
			fareRuleDetails.setAcSleeperUpperMaxFare(fareRuleDetailsDTO.getAcSleeperUpperMaxFare());

			fareRuleDetails.setBrandedAcSleeperMinFare(fareRuleDetailsDTO.getBrandedAcSleeperMinFare());
			fareRuleDetails.setBrandedAcSleeperMaxFare(fareRuleDetailsDTO.getBrandedAcSleeperMaxFare());

			fareRuleDetails.setSingleAxleAcSeaterMinFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMinFare());
			fareRuleDetails.setSingleAxleAcSeaterMaxFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleAcSleeperMinFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMinFare());
			fareRuleDetails.setMultiAxleAcSleeperMaxFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMaxFare());

			fareRuleDetails.setActiveFlag(fareRuleDetailsDTO.getActiveFlag());
			fareRuleDetailsList.add(fareRuleDetails);
		}
		return ResponseIO.success(fareRuleDetailsList);

	}

	@RequestMapping(value = "/ota/station/sync", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationOtaPartnerIO>> updateStationOtaPartner(@PathVariable("authtoken") String authtoken, String bitsAuthtoken) throws Exception {
		List<StationOtaPartnerIO> stationOtaPartners = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<StationOtaPartnerDTO> list = syncService.zoneSyncStationOtaPartner(authDTO, bitsAuthtoken);
		for (StationOtaPartnerDTO stationOtaPartnerDTO2 : list) {

			List<BaseIO> stations = new ArrayList<>();
			for (StationDTO stationDTO : stationOtaPartnerDTO2.getStations()) {
				BaseIO stationio = new BaseIO();
				stationio.setCode(stationDTO.getCode());
				stationio.setName(stationDTO.getName());
				stationio.setActiveFlag(stationDTO.getActiveFlag());
				stations.add(stationio);
			}

			StationOtaPartnerIO stationOtaPartnerIO = new StationOtaPartnerIO();
			stationOtaPartnerIO.setCode(stationOtaPartnerDTO2.getCode());
			stationOtaPartnerIO.setOtaStationCode(stationOtaPartnerDTO2.getOtaStationCode());
			stationOtaPartnerIO.setOtaStationName(stationOtaPartnerDTO2.getOtaStationName());

			BaseIO state = new BaseIO();
			state.setCode(stationOtaPartnerDTO2.getState().getCode());
			state.setName(stationOtaPartnerDTO2.getState().getName());
			stationOtaPartnerIO.setState(state);

			stationOtaPartnerIO.setActiveFlag(stationOtaPartnerDTO2.getActiveFlag());
			stationOtaPartnerIO.setStations(stations);

			BaseIO otaPartner = new BaseIO();
			otaPartner.setCode(stationOtaPartnerDTO2.getOtaPartner().getCode());
			otaPartner.setName(stationOtaPartnerDTO2.getOtaPartner().getVendorKey());
			stationOtaPartnerIO.setOtaPartner(otaPartner);

			stationOtaPartners.add(stationOtaPartnerIO);
		}

		return ResponseIO.success(stationOtaPartners);
	}

	@RequestMapping(value = "/station/area/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<StationAreaIO>> zoneSyncStationArea(@PathVariable("authtoken") String authtoken, String bitsAuthtoken) throws Exception {
		List<StationAreaIO> stationAreas = new ArrayList<StationAreaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<StationAreaDTO> list = syncService.zoneSyncStationArea(authDTO, bitsAuthtoken);
		for (StationAreaDTO stationArea : list) {
			StationAreaIO stationAreaIO = new StationAreaIO();
			stationAreaIO.setCode(stationArea.getCode());
			stationAreaIO.setName(stationArea.getName());
			stationAreaIO.setLatitude(StringUtil.isNull(stationArea.getLatitude(), Text.EMPTY));
			stationAreaIO.setLongitude(StringUtil.isNull(stationArea.getLongitude(), Text.EMPTY));
			stationAreaIO.setRadius(stationArea.getRadius());

			StationIO stationIO = new StationIO();
			stationIO.setCode(stationArea.getStation().getCode());
			stationIO.setName(stationArea.getStation().getName());
			stationIO.setActiveFlag(stationArea.getStation().getActiveFlag());
			stationAreaIO.setStation(stationIO);

			stationAreaIO.setActiveFlag(stationArea.getActiveFlag());
			stationAreas.add(stationAreaIO);
		}
		return ResponseIO.success(stationAreas);
	}

	@RequestMapping(value = "/calendar/announcement/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<CalendarAnnouncementIO>> zoneSyncCalendarAnnouncement(@PathVariable("authtoken") String authtoken, String bitsAccessToken) throws Exception {
		List<CalendarAnnouncementIO> announcementList = new ArrayList<CalendarAnnouncementIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<CalendarAnnouncementDTO> list = syncService.zoneSyncCalendarAnnouncement(authDTO, bitsAccessToken);
		for (CalendarAnnouncementDTO calendatAnouncementDTO : list) {
			CalendarAnnouncementIO calendarAnouncementIO = new CalendarAnnouncementIO();
			calendarAnouncementIO.setCode(calendatAnouncementDTO.getCode());
			calendarAnouncementIO.setName(calendatAnouncementDTO.getName());
			calendarAnouncementIO.setActiveFrom(calendatAnouncementDTO.getActiveFrom());
			calendarAnouncementIO.setActiveTo(calendatAnouncementDTO.getActiveTo());
			calendarAnouncementIO.setDayOfWeek(calendatAnouncementDTO.getDayOfWeek());

			BaseIO categoryIO = new BaseIO();
			categoryIO.setCode(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getCode() : null);
			categoryIO.setName(calendatAnouncementDTO.getCategory() != null ? calendatAnouncementDTO.getCategory().getName() : null);
			calendarAnouncementIO.setCategory(categoryIO);

			List<StateIO> stateList = new ArrayList<StateIO>();
			for (StateDTO stateDTO : calendatAnouncementDTO.getStates()) {
				StateIO stateIO = new StateIO();
				stateIO.setCode(stateDTO.getCode());
				stateIO.setName(stateDTO.getName());
				stateList.add(stateIO);
			}
			calendarAnouncementIO.setStates(stateList);

			List<String> dateList = new ArrayList<>();
			for (DateTime date : calendatAnouncementDTO.getDates()) {
				dateList.add(DateUtil.convertDate(date));
			}
			calendarAnouncementIO.setDates(dateList);

			calendarAnouncementIO.setActiveFlag(calendatAnouncementDTO.getActiveFlag());
			announcementList.add(calendarAnouncementIO);
		}
		return ResponseIO.success(announcementList);
	}
}
