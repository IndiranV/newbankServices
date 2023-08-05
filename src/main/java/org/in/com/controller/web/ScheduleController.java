package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.commerce.io.StageFareIO;
import org.in.com.controller.commerce.io.StageIO;
import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.controller.web.io.AuditIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusBreakevenSettingsIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusSeatLayoutIO;
import org.in.com.controller.web.io.BusSeatTypeFareIO;
import org.in.com.controller.web.io.BusSeatTypeIO;
import org.in.com.controller.web.io.BusVehicleVanPickupIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.controller.web.io.GalleryIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.ScheduleBookGenderRestrictionIO;
import org.in.com.controller.web.io.ScheduleBusIO;
import org.in.com.controller.web.io.ScheduleCancellationTermIO;
import org.in.com.controller.web.io.ScheduleCategoryIO;
import org.in.com.controller.web.io.ScheduleControlIO;
import org.in.com.controller.web.io.ScheduleDiscountIO;
import org.in.com.controller.web.io.ScheduleFareAutoOverrideIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.ScheduleNotificationIO;
import org.in.com.controller.web.io.ScheduleSeatAutoReleaseIO;
import org.in.com.controller.web.io.ScheduleSeatFareIO;
import org.in.com.controller.web.io.ScheduleSeatPreferenceIO;
import org.in.com.controller.web.io.ScheduleSeatVisibilityIO;
import org.in.com.controller.web.io.ScheduleStageIO;
import org.in.com.controller.web.io.ScheduleStationIO;
import org.in.com.controller.web.io.ScheduleStationPointIO;
import org.in.com.controller.web.io.ScheduleTagIO;
import org.in.com.controller.web.io.ScheduleTimeOverrideIO;
import org.in.com.controller.web.io.ScheduleVirtualSeatBlockIO;
import org.in.com.controller.web.io.SectorIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.controller.web.io.TravelStopsIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GalleryDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBookGenderRestrictionDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleNotificationDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.ScheduleVirtualSeatBlockDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareOverrideModeEM;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.OverrideTypeEM;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.StationPointAmenitiesEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.GalleryImageService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.ScheduleBookGenderRestrictionService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleCancellationTermService;
import org.in.com.service.ScheduleCategoryService;
import org.in.com.service.ScheduleControlService;
import org.in.com.service.ScheduleDiscountService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleNotificationService;
import org.in.com.service.ScheduleSeatAutoReleaseService;
import org.in.com.service.ScheduleSeatFareService;
import org.in.com.service.ScheduleSeatPreferenceService;
import org.in.com.service.ScheduleSeatVisibilityService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTimeOverrideService;
import org.in.com.service.ScheduleVirtualSeatBlockService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SeatVisibilityReportService;
import org.in.com.service.SectorService;
import org.in.com.service.TravelStopsService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/schedule")
public class ScheduleController extends BaseController {

	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleBusService scheduleBusService;
	@Autowired
	ScheduleDiscountService discountService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStationPointService schedulePointService;
	@Autowired
	ScheduleStageService scheduleStageService;
	@Autowired
	ScheduleCancellationTermService termService;
	@Autowired
	ScheduleControlService controlService;
	@Autowired
	ScheduleSeatVisibilityService seatVisibilityService;
	@Autowired
	ScheduleVisibilityService visibilityService;
	@Autowired
	ScheduleSeatPreferenceService seatPreferenceService;
	@Autowired
	ScheduleSeatAutoReleaseService scheduleSeatAutoReleaseService;
	@Autowired
	ScheduleSeatFareService seatFareService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	ScheduleTimeOverrideService timeOverrideService;
	@Autowired
	GalleryImageService galleryImageService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleNotificationService notificationService;
	@Autowired
	TravelStopsService stopService;
	@Autowired
	ScheduleCategoryService scheduleCategoryService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	ScheduleVirtualSeatBlockService virtualSeatBlockService;
	@Autowired
	ScheduleBookGenderRestrictionService scheduleBookGenderRestrictionService;
	@Autowired
	SeatVisibilityReportService seatVisibilityReportService;
	@Autowired
	SectorService sectorService;

	@RequestMapping(value = "/{scheduleCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleIO>> getSchedule(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		List<ScheduleIO> schedule = new ArrayList<ScheduleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO dto = new ScheduleDTO();
			dto.setCode(scheduleCode);
			List<ScheduleDTO> list = scheduleService.get(authDTO, dto);
			for (ScheduleDTO scheduleDTO : list) {
				ScheduleIO scheduleio = new ScheduleIO();
				scheduleio.setCode(scheduleDTO.getCode());
				scheduleio.setName(scheduleDTO.getName());
				scheduleio.setServiceNumber(scheduleDTO.getServiceNumber());
				scheduleio.setActiveFrom(scheduleDTO.getActiveFrom());
				scheduleio.setActiveTo(scheduleDTO.getActiveTo());
				scheduleio.setDisplayName(scheduleDTO.getDisplayName());
				scheduleio.setApiDisplayName(scheduleDTO.getApiDisplayName());
				scheduleio.setActiveFlag(scheduleDTO.getActiveFlag());
				scheduleio.setPnrStartCode(scheduleDTO.getPnrStartCode());
				// scheduleio.setTicketRACLimit(scheduleDTO.getTicketRACLimit());
				// scheduleio.setServiceTax(scheduleDTO.getAcBusTax());
				// scheduleio.setMobileTicketFlag(scheduleDTO.getMobileTicketFlag());
				scheduleio.setDayOfWeek(scheduleDTO.getDayOfWeek());

				if (scheduleDTO.getCategory() != null) {
					ScheduleCategoryIO categoryIO = new ScheduleCategoryIO();
					categoryIO.setCode(scheduleDTO.getCategory().getCode());
					categoryIO.setName(scheduleDTO.getCategory().getName());
					scheduleio.setCategory(categoryIO);
				}
				List<ScheduleTagIO> scheudleTagList = new ArrayList<>();
				for (ScheduleTagDTO scheduleTag : scheduleDTO.getScheduleTagList()) {
					ScheduleTagIO tagsIO = new ScheduleTagIO();
					tagsIO.setCode(scheduleTag.getCode());
					tagsIO.setName(scheduleTag.getName());
					scheudleTagList.add(tagsIO);
				}
				scheduleio.setScheduleTagList(scheudleTagList);
				scheduleio.setDistance(scheduleDTO.getDistance());

				List<SectorIO> sectorList = new ArrayList<SectorIO>();
				for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
					SectorIO sectorIO = new SectorIO();
					sectorIO.setCode(sectorDTO.getCode());
					sectorIO.setName(sectorDTO.getName());
					sectorList.add(sectorIO);
				}
				scheduleio.setSectorList(sectorList);

				List<ScheduleIO> overrideScheduleioList = new ArrayList<ScheduleIO>();
				if (!scheduleDTO.getOverrideList().isEmpty()) {
					for (ScheduleDTO lookUpscheduleDTO : scheduleDTO.getOverrideList()) {
						ScheduleIO lookupioList = new ScheduleIO();
						lookupioList.setCode(lookUpscheduleDTO.getCode());
						lookupioList.setName(lookUpscheduleDTO.getName());
						lookupioList.setActiveFrom(lookUpscheduleDTO.getActiveFrom());
						lookupioList.setActiveTo(lookUpscheduleDTO.getActiveTo());
						lookupioList.setActiveFlag(lookUpscheduleDTO.getActiveFlag());
						lookupioList.setDayOfWeek(lookUpscheduleDTO.getDayOfWeek());
						overrideScheduleioList.add(lookupioList);
					}
				}
				scheduleio.setOverrideList(overrideScheduleioList);
				schedule.add(scheduleio);
			}
		}
		return ResponseIO.success(schedule);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleIO> updateSchedule(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleIO schedule) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleIO scheduleIO = new ScheduleIO();
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedule.getCode());
			scheduleDTO.setName(schedule.getName());
			scheduleDTO.setActiveFrom(schedule.getActiveFrom());
			scheduleDTO.setActiveTo(schedule.getActiveTo());
			scheduleDTO.setDayOfWeek(schedule.getDayOfWeek());
			scheduleDTO.setDisplayName(schedule.getDisplayName());
			scheduleDTO.setApiDisplayName(schedule.getApiDisplayName());
			scheduleDTO.setServiceNumber(schedule.getServiceNumber());
			scheduleDTO.setPnrStartCode(schedule.getPnrStartCode());
			// scheduleDTO.setAcBusTax(schedule.getServiceTax());

			ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
			categoryDTO.setCode(schedule.getCategory() != null ? schedule.getCategory().getCode() : null);
			scheduleDTO.setCategory(categoryDTO);

			scheduleDTO.setLookupCode(schedule.getLookupCode());
			if (scheduleDTO.getLookupCode() != null) {
				scheduleDTO.setName("NA");
				scheduleDTO.setServiceNumber("NA");
				scheduleDTO.setDisplayName("NA");
				scheduleDTO.setPnrStartCode("NA");
			}
			if (schedule.getScheduleTagList() != null) {
				List<ScheduleTagDTO> scheduleTagList = new ArrayList<ScheduleTagDTO>();
				for (ScheduleTagIO scheduleTagIO : schedule.getScheduleTagList()) {
					if (StringUtil.isNull(scheduleTagIO.getCode())) {
						continue;
					}
					ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
					scheduleTagDTO.setCode(scheduleTagIO.getCode());
					scheduleTagList.add(scheduleTagDTO);
				}
				scheduleDTO.setScheduleTagList(scheduleTagList);
			}
			if (schedule.getSectorList() != null) {
				List<SectorDTO> sectorList = new ArrayList<SectorDTO>();
				for (SectorIO sectorIO : schedule.getSectorList()) {
					if (StringUtil.isNull(sectorIO.getCode())) {
						continue;
					}
					SectorDTO sectorDTO = new SectorDTO();
					sectorDTO.setCode(sectorIO.getCode());
					sectorList.add(sectorDTO);
				}
				scheduleDTO.setSectorList(sectorList);
			}
			scheduleDTO.setDistance(schedule.getDistance());
			scheduleDTO.setActiveFlag(schedule.getActiveFlag());
			scheduleService.Update(authDTO, scheduleDTO);
			scheduleIO.setName(scheduleDTO.getName());
			scheduleIO.setCode(scheduleDTO.getCode());
			scheduleIO.setActiveFlag(scheduleDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleIO);

	}

	@RequestMapping(value = "/bus/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleBusIO>> getBus(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleBusIO> stageIOList = new ArrayList<ScheduleBusIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			ScheduleBusDTO dto = new ScheduleBusDTO();
			dto.setSchedule(scheduleDTO);
			List<ScheduleBusDTO> list = scheduleBusService.get(authDTO, dto);
			for (ScheduleBusDTO busDTO : list) {
				ScheduleBusIO scheduleBusIO = new ScheduleBusIO();
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleBusIO.setCode(busDTO.getCode());
				scheduleBusIO.setName(busDTO.getName());
				scheduleBusIO.setActiveFlag(busDTO.getActiveFlag());
				scheduleIO.setCode(busDTO.getSchedule().getCode());
				scheduleBusIO.setSchedule(scheduleIO);

				BusIO busIO = new BusIO();
				List<AmenitiesIO> amentiesList = new ArrayList<AmenitiesIO>();
				if (busDTO.getAmentiesList() != null && !busDTO.getAmentiesList().isEmpty()) {
					for (AmenitiesDTO amentiesDTO : busDTO.getAmentiesList()) {
						AmenitiesIO amenitiesIO = new AmenitiesIO();
						amenitiesIO.setCode(amentiesDTO.getCode());
						amenitiesIO.setName(amentiesDTO.getName());
						amentiesList.add(amenitiesIO);
					}
				}
				if (busDTO.getBus() != null && busDTO.getBus().getBusSeatLayoutDTO() != null && busDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
					List<BusSeatLayoutIO> layoutIOList = new ArrayList<BusSeatLayoutIO>();
					for (BusSeatLayoutDTO layoutDTO : busDTO.getBus().getBusSeatLayoutDTO().getList()) {
						BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
						layoutIO.setCode(layoutDTO.getCode());
						layoutIO.setSeatName(layoutDTO.getName());
						BusSeatTypeIO busSeatTypeIO = new BusSeatTypeIO();
						busSeatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
						busSeatTypeIO.setName(layoutDTO.getBusSeatType().getName());
						layoutIO.setBusSeatType(busSeatTypeIO);
						layoutIO.setColPos(layoutDTO.getColPos());
						layoutIO.setRowPos(layoutDTO.getRowPos());
						layoutIO.setLayer(layoutDTO.getLayer());
						layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
						layoutIOList.add(layoutIO);
					}
					scheduleBusIO.setBusSeatLayout(layoutIOList);
				}
				scheduleBusIO.setAmenities(amentiesList);
				busIO.setCode(busDTO.getBus().getCode());
				busIO.setCategoryCode(busDTO.getBus().getCategoryCode());
				busIO.setDisplayName(busDTO.getBus().getDisplayName());
				busIO.setName(busDTO.getBus().getName());
				scheduleBusIO.setBus(busIO);

				NamespaceTaxIO tax = new NamespaceTaxIO();
				if (busDTO.getTax().getActiveFlag() != Numeric.ZERO_INT) {
					tax.setActiveFlag(busDTO.getTax().getActiveFlag());
					tax.setCgstValue(busDTO.getTax().getCgstValue());
					tax.setCode(busDTO.getTax().getCode());
					tax.setGstin(busDTO.getTax().getGstin());
					tax.setName(busDTO.getTax().getName());
					tax.setSacNumber(busDTO.getTax().getSacNumber());
					tax.setSgstValue(busDTO.getTax().getSgstValue());
					tax.setUgstValue(busDTO.getTax().getUgstValue());
					tax.setIgstValue(busDTO.getTax().getIgstValue());
					tax.setTradeName(busDTO.getTax().getTradeName());
				}
				scheduleBusIO.setTax(tax);

				BusBreakevenSettingsIO breakevenSettings = new BusBreakevenSettingsIO();
				if (busDTO.getBreakevenSettings() != null) {
					breakevenSettings.setCode(busDTO.getBreakevenSettings().getCode());
					breakevenSettings.setName(busDTO.getBreakevenSettings().getName());
					scheduleBusIO.setBreakevenSettings(breakevenSettings);
				}
				scheduleBusIO.setDistance(busDTO.getDistance());
				// Override
				stageIOList.add(scheduleBusIO);
			}
		}
		return ResponseIO.success(stageIOList);
	}

	@RequestMapping(value = "/bus/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleBusIO> updateScheduleBus(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleBusIO scheduleBusIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ScheduleBusDTO> busDTOList = new ArrayList<ScheduleBusDTO>();
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			ScheduleBusDTO scheduleBusDTO = new ScheduleBusDTO();
			scheduleBusDTO.setCode(scheduleBusIO.getCode());
			scheduleDTO.setCode(scheduleBusIO.getSchedule() != null ? scheduleBusIO.getSchedule().getCode() : null);
			scheduleBusDTO.setActiveFlag(scheduleBusIO.getActiveFlag());
			scheduleBusDTO.setSchedule(scheduleDTO);
			scheduleBusDTO.setDistance(scheduleBusIO.getDistance());

			NamespaceTaxDTO tax = new NamespaceTaxDTO();
			tax.setCode(scheduleBusIO.getTax() != null ? scheduleBusIO.getTax().getCode() : null);
			scheduleBusDTO.setTax(tax);

			BusBreakevenSettingsDTO breakevenSettings = new BusBreakevenSettingsDTO();
			breakevenSettings.setCode(scheduleBusIO.getBreakevenSettings() != null ? scheduleBusIO.getBreakevenSettings().getCode() : null);
			scheduleBusDTO.setBreakevenSettings(breakevenSettings);

			List<AmenitiesDTO> amentiesList = new ArrayList<AmenitiesDTO>();
			BusDTO busDTO = new BusDTO();
			if (scheduleBusIO.getAmenities() != null) {
				for (AmenitiesIO amenitiesIO : scheduleBusIO.getAmenities()) {
					AmenitiesDTO amentiesDTO = new AmenitiesDTO();
					amentiesDTO.setCode(amenitiesIO.getCode());
					amentiesList.add(amentiesDTO);
				}
			}
			busDTO.setCode(scheduleBusIO.getBus() != null ? scheduleBusIO.getBus().getCode() : null);
			scheduleBusDTO.setBus(busDTO);
			scheduleBusDTO.setAmentiesList(amentiesList);
			busDTOList.add(scheduleBusDTO);
			scheduleBusService.Update(authDTO, scheduleBusDTO);
			scheduleBusIO.setCode(busDTO.getCode());
			scheduleBusIO.setActiveFlag(busDTO.getActiveFlag());

		}
		return ResponseIO.success(scheduleBusIO);
	}

	@RequestMapping(value = "/bus/{scheduleCode}/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> checkScheduleBusChange(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);
		scheduleBusService.checkScheduleBusmapChange(authDTO, scheduleDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/station/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleStationIO>> getScheduleStation(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleStationIO> stationIOList = new ArrayList<ScheduleStationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			ScheduleStationDTO dto = new ScheduleStationDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleStationDTO> list = scheduleStationService.get(authDTO, dto);
			// Sorting
			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(list, comp);

			for (ScheduleStationDTO stationDTO : list) {
				ScheduleStationIO scheduleStationIO = new ScheduleStationIO();
				StationIO stationIO = new StationIO();
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(stationDTO.getSchedule().getCode());
				stationIO.setCode(stationDTO.getStation().getCode());
				stationIO.setName(stationDTO.getStation().getName());
				stationIO.setLatitude(StringUtil.isNull(stationDTO.getStation().getLatitude(), Numeric.ZERO));
				stationIO.setLongitude(StringUtil.isNull(stationDTO.getStation().getLongitude(), Numeric.ZERO));
				scheduleStationIO.setStation(stationIO);
				scheduleStationIO.setSchedule(scheduleIO);
				scheduleStationIO.setCode(stationDTO.getCode());
				scheduleStationIO.setName(stationDTO.getName());
				scheduleStationIO.setActiveFrom(stationDTO.getActiveFrom());
				scheduleStationIO.setActiveTo(stationDTO.getActiveTo());
				scheduleStationIO.setActiveFlag(stationDTO.getActiveFlag());
				scheduleStationIO.setMinitues(stationDTO.getMinitues());
				scheduleStationIO.setDayOfWeek(stationDTO.getDayOfWeek());
				scheduleStationIO.setStationSequence(stationDTO.getStationSequence());
				scheduleStationIO.setMobileNumber(stationDTO.getMobileNumber());
				// override
				List<ScheduleStationIO> lookupStageIOList = new ArrayList<ScheduleStationIO>();

				for (ScheduleStationDTO lookupStationDTO : stationDTO.getOverrideList()) {
					ScheduleStationIO lookupScheduleStationIO = new ScheduleStationIO();
					StationIO lookupStationIO = new StationIO();
					ScheduleIO lookupScheduleIO = new ScheduleIO();
					lookupScheduleIO.setCode(lookupStationDTO.getSchedule().getCode());
					lookupStationIO.setCode(lookupStationDTO.getStation().getCode());
					lookupStationIO.setName(lookupStationDTO.getStation().getName());
					lookupScheduleStationIO.setStation(lookupStationIO);
					lookupScheduleStationIO.setSchedule(lookupScheduleIO);
					lookupScheduleStationIO.setCode(lookupStationDTO.getCode());
					lookupScheduleStationIO.setName(lookupStationDTO.getName());
					lookupScheduleStationIO.setActiveFrom(lookupStationDTO.getActiveFrom());
					lookupScheduleStationIO.setActiveTo(lookupStationDTO.getActiveTo());
					lookupScheduleStationIO.setActiveFlag(lookupStationDTO.getActiveFlag());
					lookupScheduleStationIO.setMinitues(lookupStationDTO.getMinitues());
					lookupScheduleStationIO.setDayOfWeek(lookupStationDTO.getDayOfWeek());
					lookupScheduleStationIO.setStationSequence(lookupStationDTO.getStationSequence());
					lookupScheduleStationIO.setMobileNumber(lookupStationDTO.getMobileNumber());
					lookupStageIOList.add(lookupScheduleStationIO);
				}
				scheduleStationIO.setOverrideList(lookupStageIOList);
				stationIOList.add(scheduleStationIO);
			}
		}
		return ResponseIO.success(stationIOList);
	}

	@RequestMapping(value = "/station/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleStationIO> updateScheduleStation(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleStationIO> stageIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleStationIO scheduleStageIO = new ScheduleStationIO();
		List<ScheduleStationDTO> stageDTOList = new ArrayList<ScheduleStationDTO>();
		if (authDTO != null) {
			for (ScheduleStationIO scheduleStationIO : stageIOList) {
				ScheduleStationDTO stationDTO = new ScheduleStationDTO();
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				stationDTO.setCode(scheduleStationIO.getCode());
				stationDTO.setName(scheduleStationIO.getName());
				stationDTO.setActiveFrom(scheduleStationIO.getActiveFrom());
				stationDTO.setActiveTo(scheduleStationIO.getActiveTo());
				stationDTO.setDayOfWeek(scheduleStationIO.getDayOfWeek());
				stationDTO.setMinitues(scheduleStationIO.getMinitues());
				stationDTO.setStationSequence(scheduleStationIO.getStationSequence());
				stationDTO.setMobileNumber(StringUtil.isNull(scheduleStationIO.getMobileNumber(), Text.NA));
				stationDTO.setLookupCode(scheduleStationIO.getLookupCode());
				scheduleDTO.setCode(scheduleStationIO.getSchedule() != null ? scheduleStationIO.getSchedule().getCode() : null);
				stationDTO.setActiveFlag(scheduleStationIO.getActiveFlag());
				StationDTO station = new StationDTO();
				station.setCode(scheduleStationIO.getStation() != null ? scheduleStationIO.getStation().getCode() : null);
				stationDTO.setStation(station);
				stationDTO.setSchedule(scheduleDTO);
				// Delete Operation
				if (stationDTO.getActiveFlag() != 1 && StringUtil.isNotNull(stationDTO.getCode())) {
					stationDTO.setName("DEFAULT");
					stationDTO.setActiveFrom("2014-06-06");
					stationDTO.setActiveTo("2014-06-06");
					stationDTO.setDayOfWeek("0000000");
				}
				stageDTOList.add(stationDTO);
			}
			ScheduleStationDTO stageDTO = new ScheduleStationDTO();
			stageDTO.setList(stageDTOList);
			scheduleStationService.Update(authDTO, stageDTO);
			scheduleStageIO.setCode(stageDTO.getCode());
			scheduleStageIO.setActiveFlag(stageDTO.getActiveFlag());

		}
		return ResponseIO.success(scheduleStageIO);
	}

	@RequestMapping(value = "/{scheduleCode}/station/{stationCode}/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> checkScheduleStation(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode, @PathVariable("stationCode") String stationCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);

		StationDTO station = new StationDTO();
		station.setCode(stationCode);

		scheduleStationService.isStationUsed(authDTO, scheduleDTO, station);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/station/point/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleStationPointIO>> getScheduleStationPoint(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleStationPointIO> pointIOlist = new ArrayList<ScheduleStationPointIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleStationPointDTO dto = new ScheduleStationPointDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleStationPointDTO> list = schedulePointService.get(authDTO, dto);
			// Sorting
			Comparator<ScheduleStationPointDTO> comp = new BeanComparator("minitues");
			Collections.sort(list, comp);

			for (ScheduleStationPointDTO pointDTO : list) {
				ScheduleStationPointIO scheduleStationPointIO = new ScheduleStationPointIO();
				StationPointIO pointIO = new StationPointIO();
				StationIO stationIO = new StationIO();
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(pointDTO.getSchedule().getCode());
				stationIO.setCode(pointDTO.getStation().getCode());
				stationIO.setName(pointDTO.getStation().getName());
				scheduleStationPointIO.setStation(stationIO);
				scheduleStationPointIO.setSchedule(scheduleIO);
				pointIO.setCode(pointDTO.getStationPoint().getCode());
				pointIO.setName(pointDTO.getStationPoint().getName());
				pointIO.setAddress(pointDTO.getStationPoint().getAddress());
				pointIO.setLandmark(pointDTO.getStationPoint().getLandmark());
				pointIO.setLatitude(pointDTO.getStationPoint().getLatitude());
				pointIO.setLongitude(pointDTO.getStationPoint().getLongitude());
				pointIO.setNumber(pointDTO.getStationPoint().getNumber());

				List<BaseIO> amenities = new ArrayList<>();
				for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getStationPoint().getAmenities())) {
					BaseIO amenititesIO = new BaseIO();
					amenititesIO.setCode(amenitiesEM.getCode());
					amenititesIO.setName(amenitiesEM.getName());
					amenities.add(amenititesIO);
				}
				pointIO.setAmenities(amenities);
				scheduleStationPointIO.setStationPoint(pointIO);

				if (pointDTO.getBusVehicleVanPickup() != null) {
					BusVehicleVanPickupIO vanRoute = new BusVehicleVanPickupIO();
					vanRoute.setCode(pointDTO.getBusVehicleVanPickup().getCode());
					vanRoute.setName(pointDTO.getBusVehicleVanPickup().getName());
					scheduleStationPointIO.setVanRoute(vanRoute);
				}

				scheduleStationPointIO.setCode(pointDTO.getCode());
				scheduleStationPointIO.setMinitues(pointDTO.getMinitues());
				scheduleStationPointIO.setDayOfWeek(pointDTO.getDayOfWeek());
				scheduleStationPointIO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
				scheduleStationPointIO.setActiveFrom(pointDTO.getActiveFrom());
				scheduleStationPointIO.setActiveTo(pointDTO.getActiveTo());
				scheduleStationPointIO.setBoardingFlag(pointDTO.getBoardingFlag());
				scheduleStationPointIO.setDroppingFlag(pointDTO.getDroppingFlag());
				scheduleStationPointIO.setActiveFlag(pointDTO.getActiveFlag());
				scheduleStationPointIO.setFare(pointDTO.getFare());
				scheduleStationPointIO.setMobileNumber(pointDTO.getMobileNumber());
				scheduleStationPointIO.setAddress(pointDTO.getAddress());

				List<BaseIO> schedulePointAmenities = new ArrayList<>();
				for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
					BaseIO amenititesIO = new BaseIO();
					amenititesIO.setCode(amenitiesEM.getCode());
					amenititesIO.setName(amenitiesEM.getName());
					schedulePointAmenities.add(amenititesIO);
				}
				scheduleStationPointIO.setAmenities(schedulePointAmenities);
				// Override
				if (!pointDTO.getOverrideList().isEmpty()) {
					List<ScheduleStationPointIO> overridePointIOlist = new ArrayList<ScheduleStationPointIO>();
					for (ScheduleStationPointDTO lookupPointDTO : pointDTO.getOverrideList()) {
						ScheduleStationPointIO lookupScheduleStationPointIO = new ScheduleStationPointIO();
						StationPointIO lookupPointIO = new StationPointIO();
						StationIO lookupStationIO = new StationIO();
						ScheduleIO lookupScheduleIO = new ScheduleIO();
						lookupScheduleIO.setCode(lookupPointDTO.getSchedule().getCode());
						lookupStationIO.setCode(lookupPointDTO.getStation().getCode());
						lookupStationIO.setName(lookupPointDTO.getStation().getName());
						lookupScheduleStationPointIO.setStation(lookupStationIO);
						lookupScheduleStationPointIO.setSchedule(lookupScheduleIO);
						lookupPointIO.setCode(lookupPointDTO.getStationPoint().getCode());
						lookupPointIO.setName(lookupPointDTO.getStationPoint().getName());
						lookupPointIO.setAddress(lookupPointDTO.getStationPoint().getAddress());
						lookupPointIO.setLandmark(lookupPointDTO.getStationPoint().getLandmark());
						lookupPointIO.setLatitude(lookupPointDTO.getStationPoint().getLatitude());
						lookupPointIO.setLongitude(lookupPointDTO.getStationPoint().getLongitude());
						lookupPointIO.setNumber(lookupPointDTO.getStationPoint().getNumber());

						List<BaseIO> lookupAmenities = new ArrayList<>();
						for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(lookupPointDTO.getStationPoint().getAmenities())) {
							BaseIO amenititesIO = new BaseIO();
							amenititesIO.setCode(amenitiesEM.getCode());
							amenititesIO.setName(amenitiesEM.getName());
							lookupAmenities.add(amenititesIO);
						}
						lookupPointIO.setAmenities(lookupAmenities);

						lookupScheduleStationPointIO.setStationPoint(lookupPointIO);
						lookupScheduleStationPointIO.setCode(lookupPointDTO.getCode());
						lookupScheduleStationPointIO.setMinitues(lookupPointDTO.getMinitues());
						lookupScheduleStationPointIO.setDayOfWeek(lookupPointDTO.getDayOfWeek());
						lookupScheduleStationPointIO.setCreditDebitFlag(lookupPointDTO.getCreditDebitFlag());
						lookupScheduleStationPointIO.setActiveFrom(lookupPointDTO.getActiveFrom());
						lookupScheduleStationPointIO.setActiveTo(lookupPointDTO.getActiveTo());
						lookupScheduleStationPointIO.setFare(lookupPointDTO.getFare());
						lookupScheduleStationPointIO.setMobileNumber(lookupPointDTO.getMobileNumber());
						lookupScheduleStationPointIO.setAddress(lookupPointDTO.getAddress());

						List<BaseIO> lookupSchedulePointAmenities = new ArrayList<>();
						for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(lookupPointDTO.getAmenities())) {
							BaseIO amenititesIO = new BaseIO();
							amenititesIO.setCode(amenitiesEM.getCode());
							amenititesIO.setName(amenitiesEM.getName());
							lookupSchedulePointAmenities.add(amenititesIO);
						}
						lookupScheduleStationPointIO.setAmenities(lookupSchedulePointAmenities);
						overridePointIOlist.add(lookupScheduleStationPointIO);
					}
					scheduleStationPointIO.setOverrideList(overridePointIOlist);
				}
				pointIOlist.add(scheduleStationPointIO);
			}
		}
		return ResponseIO.success(pointIOlist);
	}

	@RequestMapping(value = "/station/point/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleStationPointIO> updateScheduleSationPoint(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleStationPointIO> pointIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleStationPointIO stationPointIO = new ScheduleStationPointIO();
		List<ScheduleStationPointDTO> stageDTOList = new ArrayList<ScheduleStationPointDTO>();
		if (authDTO != null) {
			for (ScheduleStationPointIO pointIO : pointIOList) {
				ScheduleStationPointDTO scheduleStationPointDTO = new ScheduleStationPointDTO();
				StationPointDTO pointDTO = new StationPointDTO();
				pointDTO.setCode(pointIO.getStationPoint().getCode());
				scheduleStationPointDTO.setStationPoint(pointDTO);
				StationDTO stationDTO = new StationDTO();
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(pointIO.getSchedule().getCode());
				stationDTO.setCode(pointIO.getStation().getCode());
				scheduleStationPointDTO.setSchedule(scheduleDTO);
				scheduleStationPointDTO.setStation(stationDTO);
				scheduleStationPointDTO.setCode(pointIO.getCode());
				scheduleStationPointDTO.setMinitues(pointIO.getMinitues());
				scheduleStationPointDTO.setCreditDebitFlag(pointIO.getCreditDebitFlag());
				scheduleStationPointDTO.setDayOfWeek(pointIO.getDayOfWeek());
				scheduleStationPointDTO.setActiveFrom(pointIO.getActiveFrom());
				scheduleStationPointDTO.setActiveTo(pointIO.getActiveTo());
				scheduleStationPointDTO.setLookupCode(pointIO.getLookupCode());
				scheduleStationPointDTO.setActiveFlag(pointIO.getActiveFlag());
				scheduleStationPointDTO.setFare(pointIO.getFare());
				scheduleStationPointDTO.setMobileNumber(StringUtil.isNull(pointIO.getMobileNumber(), Text.NA));
				scheduleStationPointDTO.setAddress(StringUtil.isNull(pointIO.getAddress(), Text.NA));

				List<StationPointAmenitiesEM> amenitiesList = new ArrayList<>();
				if (pointIO.getAmenities() != null) {
					for (BaseIO amenitiesIO : pointIO.getAmenities()) {
						StationPointAmenitiesEM amenitiesEM = StationPointAmenitiesEM.getStationPointAmenitiesEM(amenitiesIO.getCode());
						if (amenitiesEM == null) {
							continue;
						}
						amenitiesList.add(amenitiesEM);
					}
				}
				scheduleStationPointDTO.setAmenities(StationPointAmenitiesEM.getStationPointAmenitiesCodes(amenitiesList));

				// Station Point Additional fare max limit 300
				if (scheduleStationPointDTO.getFare() != null && scheduleStationPointDTO.getFare().compareTo(Numeric.THREE_HUNDRED) > 0) {
					scheduleStationPointDTO.setFare(Numeric.THREE_HUNDRED);
				}

				if (pointIO.getVanRoute() != null) {
					BusVehicleVanPickupDTO vanRouteDTO = new BusVehicleVanPickupDTO();
					vanRouteDTO.setCode(pointIO.getVanRoute().getCode());
					scheduleStationPointDTO.setBusVehicleVanPickup(vanRouteDTO);
				}

				scheduleStationPointDTO.setBoardingDroppingFlag(String.valueOf(pointIO.getBoardingFlag()) + String.valueOf(pointIO.getDroppingFlag()));
				if (pointIO.getBoardingFlag() == 0 && pointIO.getDroppingFlag() == 0) {
					scheduleStationPointDTO.setBoardingDroppingFlag("11");
				}

				// Delete Operation
				if (scheduleStationPointDTO.getActiveFlag() != 1 && StringUtil.isNotNull(scheduleStationPointDTO.getCode())) {
					scheduleStationPointDTO.setName("DEFAULT");
					scheduleStationPointDTO.setActiveFrom("2014-06-06");
					scheduleStationPointDTO.setActiveTo("2014-06-06");
					scheduleStationPointDTO.setDayOfWeek("0000000");
				}
				stageDTOList.add(scheduleStationPointDTO);
			}
			ScheduleStationPointDTO stageDTO = new ScheduleStationPointDTO();
			stageDTO.setList(stageDTOList);
			schedulePointService.Update(authDTO, stageDTO);
			stationPointIO.setCode(stageDTO.getCode());
			stationPointIO.setActiveFlag(stageDTO.getActiveFlag());
		}
		return ResponseIO.success(stationPointIO);
	}
	
	@RequestMapping(value = "/van/pickup/station/{stationCode}/points", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleVanPickupIO>> getVanPickupStationPoints(@PathVariable("authtoken") String authtoken, @PathVariable("stationCode") String stationCode) throws Exception {
		List<BusVehicleVanPickupIO> vanPickuPoints = new ArrayList<BusVehicleVanPickupIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		
		StationDTO stationDTO = new StationDTO();
		stationDTO.setCode(stationCode);
		List<BusVehicleVanPickupDTO> vanPickupList = schedulePointService.getVanPickupStationPoints(authDTO, stationDTO);
		
		for (BusVehicleVanPickupDTO vanPickupDTO : vanPickupList) {
			BusVehicleVanPickupIO vanPickupIO = new BusVehicleVanPickupIO();
			vanPickupIO.setCode(vanPickupDTO.getCode());
			vanPickupIO.setName(vanPickupDTO.getName());
			
			StationIO vanPickupStationIO = new StationIO();
			vanPickupStationIO.setCode(vanPickupDTO.getStation().getCode());
			vanPickupStationIO.setName(vanPickupDTO.getStation().getName());
			
			List<StationPointIO> stationPointIOList = new ArrayList<>();
			for (StationPointDTO stationPointDTO : vanPickupDTO.getStation().getStationPoints()) {
				StationPointIO stationPointIO = new StationPointIO();
				StationIO stationIO = new StationIO();
				stationIO.setName(stationPointDTO.getStation().getName());
				stationIO.setCode(stationPointDTO.getStation().getCode());
				stationPointIO.setLatitude(stationPointDTO.getLatitude() == null ? "" : stationPointDTO.getLatitude());
				stationPointIO.setLongitude(stationPointDTO.getLongitude() == null ? "" : stationPointDTO.getLongitude());
				stationPointIO.setCode(stationPointDTO.getCode());
				stationPointIO.setName(stationPointDTO.getName());
				stationPointIO.setLandmark(stationPointDTO.getLandmark());
				stationPointIO.setAddress(stationPointDTO.getAddress());
				stationPointIO.setNumber(stationPointDTO.getNumber());
				stationPointIO.setMapUrl(stationPointDTO.getMapUrl());
				List<BaseIO> amentiesList = new ArrayList<BaseIO>();
				for (StationPointAmenitiesEM amentiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(stationPointDTO.getAmenities())) {
					BaseIO amenitiesIO = new BaseIO();
					amenitiesIO.setCode(amentiesEM.getCode());
					amenitiesIO.setName(amentiesEM.getName());
					amentiesList.add(amenitiesIO);
				}
				stationPointIO.setAmenities(amentiesList);
				stationPointIO.setActiveFlag(stationPointDTO.getActiveFlag());
				stationPointIO.setStation(stationIO);
				stationPointIOList.add(stationPointIO);
			}
			vanPickupStationIO.setStationPoint(stationPointIOList);
			vanPickupIO.setStation(vanPickupStationIO);
			vanPickuPoints.add(vanPickupIO);
		}
		return ResponseIO.success(vanPickuPoints);
	}

	@RequestMapping(value = "/stage/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleStageIO>> getScheduleStage(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleStageIO> stageIOList = new ArrayList<ScheduleStageIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			List<ScheduleStageDTO> list = scheduleStageService.get(authDTO, scheduleDTO);
			// Sorting Stage
			Collections.sort(list, new Comparator<ScheduleStageDTO>() {
				@Override
				public int compare(ScheduleStageDTO t1, ScheduleStageDTO t2) {
					return new CompareToBuilder().append(t1.getFromStationSequence(), t2.getFromStationSequence()).append(t2.getToStationSequence(), t1.getToStationSequence()).toComparison();
				}
			});

			for (ScheduleStageDTO stageDTO : list) {
				ScheduleStageIO stageIO = new ScheduleStageIO();
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(stageDTO.getSchedule().getCode());
				stageIO.setCode(stageDTO.getCode());
				stageIO.setName(stageDTO.getName());
				stageIO.setActiveFrom(stageDTO.getActiveFrom());
				stageIO.setActiveTo(stageDTO.getActiveTo());
				stageIO.setActiveFlag(stageDTO.getActiveFlag());
				stageIO.setDayOfWeek(stageDTO.getDayOfWeek());
				StationIO fromStationIO = new StationIO();
				StationIO toStationIO = new StationIO();
				fromStationIO.setCode(stageDTO.getFromStation().getCode());
				fromStationIO.setName(stageDTO.getFromStation().getName());
				toStationIO.setCode(stageDTO.getToStation().getCode());
				toStationIO.setName(stageDTO.getToStation().getName());
				stageIO.setFromStation(fromStationIO);
				stageIO.setToStation(toStationIO);
				// Bus Seat Type wise fare
				List<BusSeatTypeFareIO> busSeatTypeFareList = new ArrayList<>();
				if (!stageDTO.getBusSeatTypeFare().isEmpty()) {
					for (BusSeatTypeFareDTO seatTypefare : stageDTO.getBusSeatTypeFare()) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(seatTypefare.getFare());
						busSeatTypeFare.setSeatType(seatTypefare.getBusSeatType().getCode());
						busSeatTypeFareList.add(busSeatTypeFare);
					}
				}
				if (stageDTO.getBusSeatTypeFare().isEmpty() && stageDTO.getFare() != 0) {
					BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
					busSeatTypeFare.setSeatType(stageDTO.getBusSeatType().getCode());
					busSeatTypeFare.setFare(BigDecimal.valueOf(stageDTO.getFare()));
					busSeatTypeFareList.add(busSeatTypeFare);
				}
				stageIO.setBusSeatTypeFare(busSeatTypeFareList);

				BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
				if (stageDTO.getBusSeatType() != null && BusSeatTypeEM.ALL_BUS_SEAT_TYPE.getId() != stageDTO.getBusSeatType().getId()) {
					seatTypeIO.setCode(stageDTO.getBusSeatType().getCode());
					seatTypeIO.setName(stageDTO.getBusSeatType().getName());
					stageIO.setBusSeatType(seatTypeIO);
					stageIO.setFare(stageDTO.getFare());
				}
				else {
					BusSeatTypeFareIO busSeatTypeFare = busSeatTypeFareList.get(0);
					seatTypeIO.setCode(busSeatTypeFare.getSeatType());
					seatTypeIO.setName(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeFare.getSeatType()).getName());
					stageIO.setBusSeatType(seatTypeIO);
					stageIO.setFare(busSeatTypeFare.getFare().doubleValue());
				}
				if (stageDTO.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(stageDTO.getGroup().getCode());
					groupIO.setName(stageDTO.getGroup().getName());
					groupIO.setLevel(stageDTO.getGroup().getLevel());
					stageIO.setGroup(groupIO);
				}
				stageIO.setSchedule(scheduleIO);

				// Override
				List<ScheduleStageIO> LookupStageIOList = new ArrayList<ScheduleStageIO>();

				for (ScheduleStageDTO lookupStageDTO : stageDTO.getOverrideList()) {
					ScheduleStageIO lookupStageIO = new ScheduleStageIO();
					ScheduleIO lookupScheduleIO = new ScheduleIO();
					lookupScheduleIO.setCode(lookupStageDTO.getSchedule().getCode());
					lookupStageIO.setSchedule(lookupScheduleIO);

					lookupStageIO.setCode(lookupStageDTO.getCode());
					lookupStageIO.setName(lookupStageDTO.getName());
					lookupStageIO.setActiveFrom(lookupStageDTO.getActiveFrom());
					lookupStageIO.setActiveTo(lookupStageDTO.getActiveTo());
					lookupStageIO.setActiveFlag(lookupStageDTO.getActiveFlag());
					lookupStageIO.setDayOfWeek(lookupStageDTO.getDayOfWeek());
					StationIO lookupFromStationIO = new StationIO();
					StationIO lookupToStationIO = new StationIO();
					lookupFromStationIO.setCode(lookupStageDTO.getFromStation().getCode());
					lookupFromStationIO.setName(lookupStageDTO.getFromStation().getName());
					lookupToStationIO.setCode(lookupStageDTO.getToStation().getCode());
					lookupToStationIO.setName(lookupStageDTO.getToStation().getName());
					lookupStageIO.setFromStation(lookupFromStationIO);
					lookupStageIO.setToStation(lookupToStationIO);

					List<BusSeatTypeFareIO> busSeatTypeFareLookupList = new ArrayList<>();
					if (!lookupStageDTO.getBusSeatTypeFare().isEmpty()) {
						for (BusSeatTypeFareDTO seatTypefare : lookupStageDTO.getBusSeatTypeFare()) {
							BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
							busSeatTypeFare.setFare(seatTypefare.getFare());
							busSeatTypeFare.setSeatType(seatTypefare.getBusSeatType().getCode());
							busSeatTypeFareLookupList.add(busSeatTypeFare);
						}
					}
					if (lookupStageDTO.getBusSeatTypeFare().isEmpty() && lookupStageDTO.getFare() != 0) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(BigDecimal.valueOf(lookupStageDTO.getFare()));
						busSeatTypeFare.setSeatType(lookupStageDTO.getBusSeatType().getCode());
						busSeatTypeFareLookupList.add(busSeatTypeFare);
					}
					lookupStageIO.setBusSeatTypeFare(busSeatTypeFareLookupList);
					BusSeatTypeIO lookupSeatTypeIO = new BusSeatTypeIO();
					if (lookupStageDTO.getBusSeatType() != null && BusSeatTypeEM.ALL_BUS_SEAT_TYPE.getId() != lookupStageDTO.getBusSeatType().getId()) {
						lookupSeatTypeIO.setCode(lookupStageDTO.getBusSeatType().getCode());
						lookupSeatTypeIO.setName(lookupStageDTO.getBusSeatType().getName());
						lookupStageIO.setBusSeatType(lookupSeatTypeIO);
						lookupStageIO.setFare(lookupStageDTO.getFare());
					}
					else {
						BusSeatTypeFareIO busSeatTypeFareLookup = busSeatTypeFareLookupList.get(0);
						seatTypeIO.setCode(busSeatTypeFareLookup.getSeatType());
						seatTypeIO.setName(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeFareLookup.getSeatType()).getName());
						lookupStageIO.setBusSeatType(seatTypeIO);
						lookupStageIO.setFare(busSeatTypeFareLookup.getFare().doubleValue());
					}
					if (lookupStageDTO.getGroup() != null) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(lookupStageDTO.getGroup().getCode());
						groupIO.setName(lookupStageDTO.getGroup().getName());
						groupIO.setLevel(stageDTO.getGroup().getLevel());
						lookupStageIO.setGroup(groupIO);
					}
					LookupStageIOList.add(lookupStageIO);
				}
				stageIO.setOverrideList(LookupStageIOList);
				stageIOList.add(stageIO);
			}
		}
		return ResponseIO.success(stageIOList);
	}

	@RequestMapping(value = "/stage/v2/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleStageIO>> getScheduleStageV2(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleStageIO> stageIOList = new ArrayList<ScheduleStageIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			List<ScheduleStageDTO> list = scheduleStageService.getScheduleStageV2(authDTO, scheduleDTO);
			// Sorting Stage
			Collections.sort(list, new Comparator<ScheduleStageDTO>() {
				@Override
				public int compare(ScheduleStageDTO t1, ScheduleStageDTO t2) {
					return new CompareToBuilder().append(t1.getFromStationSequence(), t2.getFromStationSequence()).append(t2.getToStationSequence(), t1.getToStationSequence()).toComparison();
				}
			});

			for (ScheduleStageDTO stageDTO : list) {
				ScheduleStageIO stageIO = new ScheduleStageIO();
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(stageDTO.getSchedule().getCode());
				stageIO.setCode(stageDTO.getCode());
				stageIO.setName(stageDTO.getName());
				stageIO.setActiveFrom(stageDTO.getActiveFrom());
				stageIO.setActiveTo(stageDTO.getActiveTo());
				stageIO.setActiveFlag(stageDTO.getActiveFlag());
				stageIO.setDayOfWeek(stageDTO.getDayOfWeek());
				StationIO fromStationIO = new StationIO();
				StationIO toStationIO = new StationIO();
				fromStationIO.setCode(stageDTO.getFromStation().getCode());
				fromStationIO.setName(stageDTO.getFromStation().getName());
				toStationIO.setCode(stageDTO.getToStation().getCode());
				toStationIO.setName(stageDTO.getToStation().getName());
				stageIO.setFromStation(fromStationIO);
				stageIO.setToStation(toStationIO);
				// Bus Seat Type wise fare
				List<BusSeatTypeFareIO> busSeatTypeFareList = new ArrayList<>();
				if (!stageDTO.getBusSeatTypeFare().isEmpty()) {
					for (BusSeatTypeFareDTO seatTypefare : stageDTO.getBusSeatTypeFare()) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(seatTypefare.getFare());
						busSeatTypeFare.setSeatType(seatTypefare.getBusSeatType().getCode());
						busSeatTypeFareList.add(busSeatTypeFare);
					}
				}
				if (stageDTO.getBusSeatTypeFare().isEmpty() && stageDTO.getFare() != 0) {
					BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
					busSeatTypeFare.setSeatType(stageDTO.getBusSeatType().getCode());
					busSeatTypeFare.setFare(BigDecimal.valueOf(stageDTO.getFare()));
					busSeatTypeFareList.add(busSeatTypeFare);
				}
				stageIO.setBusSeatTypeFare(busSeatTypeFareList);

				if (stageDTO.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(stageDTO.getGroup().getCode());
					groupIO.setName(stageDTO.getGroup().getName());
					groupIO.setLevel(stageDTO.getGroup().getLevel());
					stageIO.setGroup(groupIO);
				}
				stageIO.setSchedule(scheduleIO);

				// Override
				List<ScheduleStageIO> LookupStageIOList = new ArrayList<ScheduleStageIO>();

				for (ScheduleStageDTO lookupStageDTO : stageDTO.getOverrideList()) {
					ScheduleStageIO lookupStageIO = new ScheduleStageIO();
					ScheduleIO lookupScheduleIO = new ScheduleIO();
					lookupScheduleIO.setCode(lookupStageDTO.getSchedule().getCode());
					lookupStageIO.setSchedule(lookupScheduleIO);

					lookupStageIO.setCode(lookupStageDTO.getCode());
					lookupStageIO.setName(lookupStageDTO.getName());
					lookupStageIO.setActiveFrom(lookupStageDTO.getActiveFrom());
					lookupStageIO.setActiveTo(lookupStageDTO.getActiveTo());
					lookupStageIO.setActiveFlag(lookupStageDTO.getActiveFlag());
					lookupStageIO.setDayOfWeek(lookupStageDTO.getDayOfWeek());
					StationIO lookupFromStationIO = new StationIO();
					StationIO lookupToStationIO = new StationIO();
					lookupFromStationIO.setCode(lookupStageDTO.getFromStation().getCode());
					lookupFromStationIO.setName(lookupStageDTO.getFromStation().getName());
					lookupToStationIO.setCode(lookupStageDTO.getToStation().getCode());
					lookupToStationIO.setName(lookupStageDTO.getToStation().getName());
					lookupStageIO.setFromStation(lookupFromStationIO);
					lookupStageIO.setToStation(lookupToStationIO);

					List<BusSeatTypeFareIO> busSeatTypeFareLookupList = new ArrayList<>();
					if (!lookupStageDTO.getBusSeatTypeFare().isEmpty()) {
						for (BusSeatTypeFareDTO seatTypefare : lookupStageDTO.getBusSeatTypeFare()) {
							BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
							busSeatTypeFare.setFare(seatTypefare.getFare());
							busSeatTypeFare.setSeatType(seatTypefare.getBusSeatType().getCode());
							busSeatTypeFareLookupList.add(busSeatTypeFare);
						}
					}
					if (lookupStageDTO.getBusSeatTypeFare().isEmpty() && lookupStageDTO.getFare() != 0) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(BigDecimal.valueOf(lookupStageDTO.getFare()));
						busSeatTypeFare.setSeatType(lookupStageDTO.getBusSeatType().getCode());
						busSeatTypeFareLookupList.add(busSeatTypeFare);
					}
					lookupStageIO.setBusSeatTypeFare(busSeatTypeFareLookupList);

					if (lookupStageDTO.getGroup() != null) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(lookupStageDTO.getGroup().getCode());
						groupIO.setName(lookupStageDTO.getGroup().getName());
						groupIO.setLevel(stageDTO.getGroup().getLevel());
						lookupStageIO.setGroup(groupIO);
					}
					LookupStageIOList.add(lookupStageIO);
				}
				stageIO.setOverrideList(LookupStageIOList);
				stageIOList.add(stageIO);
			}
		}
		return ResponseIO.success(stageIOList);
	}

	@RequestMapping(value = "/stage/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleStageIO> updateScheduleStage(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleStageIO> stageIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleStageIO scheduleStageIO = new ScheduleStageIO();
		List<ScheduleStageDTO> stageDTOList = new ArrayList<ScheduleStageDTO>();
		if (authDTO != null) {
			for (ScheduleStageIO stageIO : stageIOList) {
				ScheduleStageDTO stageDTO = new ScheduleStageDTO();
				stageDTO.setCode(stageIO.getCode());
				stageDTO.setName(stageIO.getName());
				stageDTO.setActiveFrom(stageIO.getActiveFrom());
				stageDTO.setDayOfWeek(stageIO.getDayOfWeek());
				stageDTO.setActiveTo(stageIO.getActiveTo());
				stageDTO.setLookupCode(stageIO.getLookupCode());
				stageDTO.setActiveFlag(stageIO.getActiveFlag());
				stageDTO.setFare(stageIO.getFare());
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(stageIO.getSchedule().getCode());
				StationDTO FromstationDTO = new StationDTO();
				StationDTO TostationDTO = new StationDTO();
				FromstationDTO.setCode(stageIO.getFromStation().getCode());
				TostationDTO.setCode(stageIO.getToStation().getCode());
				stageDTO.setFromStation(FromstationDTO);
				stageDTO.setToStation(TostationDTO);
				stageDTO.setSchedule(scheduleDTO);
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(stageIO.getGroup() != null ? stageIO.getGroup().getCode() : null);
				stageDTO.setGroup(groupDTO);
				stageDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageIO.getBusSeatType().getCode()));
				stageDTOList.add(stageDTO);
			}
			ScheduleStageDTO stageDTO = new ScheduleStageDTO();
			stageDTO.setList(stageDTOList);
			scheduleStageService.Update(authDTO, stageDTO);
			scheduleStageIO.setCode(stageDTO.getList().get(0).getCode());
			scheduleStageIO.setActiveFlag(stageDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleStageIO);
	}

	@RequestMapping(value = "/stage/v2/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleStageIO> updateScheduleStageV2(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleStageIO> stageIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleStageIO scheduleStageIO = new ScheduleStageIO();
		List<ScheduleStageDTO> stageDTOList = new ArrayList<ScheduleStageDTO>();
		if (authDTO != null) {
			for (ScheduleStageIO stageIO : stageIOList) {
				ScheduleStageDTO stageDTO = new ScheduleStageDTO();
				stageDTO.setCode(stageIO.getCode());
				stageDTO.setName(stageIO.getName());
				stageDTO.setActiveFrom(stageIO.getActiveFrom());
				stageDTO.setDayOfWeek(stageIO.getDayOfWeek());
				stageDTO.setActiveTo(stageIO.getActiveTo());
				stageDTO.setLookupCode(stageIO.getLookupCode());
				stageDTO.setActiveFlag(stageIO.getActiveFlag());

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(stageIO.getSchedule().getCode());
				StationDTO FromstationDTO = new StationDTO();
				StationDTO TostationDTO = new StationDTO();
				FromstationDTO.setCode(stageIO.getFromStation().getCode());
				TostationDTO.setCode(stageIO.getToStation().getCode());
				stageDTO.setFromStation(FromstationDTO);
				stageDTO.setToStation(TostationDTO);
				stageDTO.setSchedule(scheduleDTO);

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(stageIO.getGroup() != null ? stageIO.getGroup().getCode() : null);
				stageDTO.setGroup(groupDTO);

				List<BusSeatTypeFareDTO> seatTypeFareList = new ArrayList<>();
				if (stageIO.getFare() != 0 && stageIO.getBusSeatType() != null) {
					BusSeatTypeFareDTO busSeatTypeFare = new BusSeatTypeFareDTO();
					busSeatTypeFare.setFare(BigDecimal.valueOf(stageIO.getFare()));
					busSeatTypeFare.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageIO.getBusSeatType().getCode()));
					seatTypeFareList.add(busSeatTypeFare);
				}

				if (stageIO.getBusSeatTypeFare() != null) {
					for (BusSeatTypeFareIO seatTypefare : stageIO.getBusSeatTypeFare()) {
						if (seatTypefare.getFare().compareTo(BigDecimal.ZERO) == 0) {
							throw new ServiceException(ErrorCode.TRANSACTION_AMOUNT_INVALID, "Fare cannot be Rs.0");
						}
						BusSeatTypeFareDTO busSeatTypeFare = new BusSeatTypeFareDTO();
						busSeatTypeFare.setFare(seatTypefare.getFare());
						busSeatTypeFare.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(seatTypefare.getSeatType()));
						seatTypeFareList.add(busSeatTypeFare);
					}
				}
				stageDTO.setBusSeatTypeFare(seatTypeFareList);
				stageDTOList.add(stageDTO);
			}
			ScheduleStageDTO stageDTO = new ScheduleStageDTO();
			stageDTO.setList(stageDTOList);
			scheduleStageService.Update(authDTO, stageDTO);
			scheduleStageIO.setCode(stageDTO.getList().get(0).getCode());
			scheduleStageIO.setActiveFlag(stageDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleStageIO);
	}

	@RequestMapping(value = "/cancellationterm/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleCancellationTermIO>> getScheduleCancellationterm(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleCancellationTermIO> stageIOList = new ArrayList<ScheduleCancellationTermIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleCancellationTermDTO dto = new ScheduleCancellationTermDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleCancellationTermDTO> list = termService.get(authDTO, dto);
			for (ScheduleCancellationTermDTO stageDTO : list) {
				ScheduleCancellationTermIO termIO = new ScheduleCancellationTermIO();
				GroupIO groupIO = new GroupIO();
				if (stageDTO.getGroup() != null) {
					groupIO.setCode(stageDTO.getGroup().getCode());
					groupIO.setName(stageDTO.getGroup().getName());
					groupIO.setLevel(stageDTO.getGroup().getLevel());
					termIO.setGroup(groupIO);
				}
				if (stageDTO.getSchedule() != null) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(stageDTO.getSchedule().getCode());
					scheduleIO.setName(stageDTO.getSchedule().getName());
					termIO.setSchedule(scheduleIO);
				}
				if (stageDTO.getCancellationTerm() != null) {
					CancellationTermIO cancellationTermIO = new CancellationTermIO();
					cancellationTermIO.setCode(stageDTO.getCancellationTerm().getCode());
					cancellationTermIO.setName(stageDTO.getCancellationTerm().getName());
					termIO.setCancellationTerm(cancellationTermIO);
				}
				termIO.setCode(stageDTO.getCode());
				termIO.setActiveFrom(stageDTO.getActiveFrom());
				termIO.setActiveTo(stageDTO.getActiveTo());
				termIO.setDayOfWeek(stageDTO.getDayOfWeek());
				termIO.setActiveFlag(stageDTO.getActiveFlag());

				// Override
				List<ScheduleCancellationTermIO> OverrideStageIOList = new ArrayList<ScheduleCancellationTermIO>();

				for (ScheduleCancellationTermDTO OverrideCancellationTermDTO : stageDTO.getOverrideList()) {
					ScheduleCancellationTermIO lookupTermIO = new ScheduleCancellationTermIO();
					GroupIO lookupGroupIO = new GroupIO();
					if (OverrideCancellationTermDTO.getGroup() != null) {
						lookupGroupIO.setCode(OverrideCancellationTermDTO.getGroup().getCode());
						lookupGroupIO.setName(OverrideCancellationTermDTO.getGroup().getName());
						lookupTermIO.setGroup(lookupGroupIO);
					}
					if (OverrideCancellationTermDTO.getSchedule() != null) {
						ScheduleIO scheduleIO = new ScheduleIO();
						scheduleIO.setCode(OverrideCancellationTermDTO.getSchedule().getCode());
						scheduleIO.setName(OverrideCancellationTermDTO.getSchedule().getName());
						lookupTermIO.setSchedule(scheduleIO);
					}
					if (OverrideCancellationTermDTO.getCancellationTerm() != null) {
						CancellationTermIO cancellationTermIO = new CancellationTermIO();
						cancellationTermIO.setCode(OverrideCancellationTermDTO.getCancellationTerm().getCode());
						cancellationTermIO.setName(OverrideCancellationTermDTO.getCancellationTerm().getName());
						lookupTermIO.setCancellationTerm(cancellationTermIO);
					}
					lookupTermIO.setCode(OverrideCancellationTermDTO.getCode());
					lookupTermIO.setActiveFrom(OverrideCancellationTermDTO.getActiveFrom());
					lookupTermIO.setActiveTo(OverrideCancellationTermDTO.getActiveTo());
					lookupTermIO.setDayOfWeek(OverrideCancellationTermDTO.getDayOfWeek());
					lookupTermIO.setActiveFlag(OverrideCancellationTermDTO.getActiveFlag());
					OverrideStageIOList.add(lookupTermIO);
				}
				termIO.setOverrideList(OverrideStageIOList);
				stageIOList.add(termIO);
			}
		}
		return ResponseIO.success(stageIOList);
	}

	@RequestMapping(value = "/cancellationterm/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleCancellationTermIO> updateScheduleCancellationterm(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleCancellationTermIO> stageIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleCancellationTermIO scheduleStageIO = new ScheduleCancellationTermIO();
		List<ScheduleCancellationTermDTO> stageDTOList = new ArrayList<ScheduleCancellationTermDTO>();
		if (authDTO != null) {
			for (ScheduleCancellationTermIO stageIO : stageIOList) {
				ScheduleCancellationTermDTO stageDTO = new ScheduleCancellationTermDTO();
				stageDTO.setCode(stageIO.getCode());
				stageDTO.setActiveFrom(stageIO.getActiveFrom());
				stageDTO.setActiveTo(stageIO.getActiveTo());
				if (stageIO.getGroup() != null) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(stageIO.getGroup().getCode());
					stageDTO.setGroup(groupDTO);
				}
				if (stageIO.getCancellationTerm() != null) {
					CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
					cancellationTermDTO.setCode(stageIO.getCancellationTerm().getCode());
					stageDTO.setCancellationTerm(cancellationTermDTO);
				}
				if (stageIO.getSchedule() != null) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(stageIO.getSchedule().getCode());
					stageDTO.setSchedule(scheduleDTO);
				}
				stageDTO.setLookupCode(stageIO.getLookupCode());

				stageDTO.setActiveFlag(stageIO.getActiveFlag());
				stageDTO.setDayOfWeek(stageIO.getDayOfWeek());
				stageDTOList.add(stageDTO);
			}
			ScheduleCancellationTermDTO stageDTO = new ScheduleCancellationTermDTO();
			stageDTO.setList(stageDTOList);
			termService.Update(authDTO, stageDTO);
			scheduleStageIO.setCode(stageDTO.getCode());
			scheduleStageIO.setActiveFlag(stageDTO.getActiveFlag());

		}
		return ResponseIO.success(scheduleStageIO);

	}

	@RequestMapping(value = "/control/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleControlIO>> getScheduleStageControl(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleControlIO> controlIOList = new ArrayList<ScheduleControlIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleControlDTO dto = new ScheduleControlDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleControlDTO> list = controlService.get(authDTO, dto);

			// Sorting Trips
			Collections.sort(list, new Comparator<ScheduleControlDTO>() {
				public int compare(ScheduleControlDTO t1, ScheduleControlDTO t2) {
					return new CompareToBuilder().append(t1.getGroup().getId(), t2.getGroup().getId()).append(t1.getFromStation() == null ? 0 : 1, t2.getFromStation() == null ? 0 : 1).toComparison();
				}
			});

			for (ScheduleControlDTO controlDTO : list) {
				ScheduleControlIO stageIO = new ScheduleControlIO();
				stageIO.setCode(controlDTO.getCode());
				stageIO.setName(controlDTO.getName());
				stageIO.setActiveFrom(controlDTO.getActiveFrom());
				stageIO.setActiveTo(controlDTO.getActiveTo());
				stageIO.setActiveFlag(controlDTO.getActiveFlag());
				stageIO.setCloseMinitues(controlDTO.getCloseMinitues());
				stageIO.setOpenMinitues(controlDTO.getOpenMinitues());
				stageIO.setDayOfWeek(controlDTO.getDayOfWeek());
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(controlDTO.getSchedule().getCode());
				stageIO.setAllowBookingFlag(controlDTO.getAllowBookingFlag());
				// Stage based booking open/close time
				if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0) {
					StationIO fromStationIO = new StationIO();
					StationIO toStationIO = new StationIO();
					fromStationIO.setCode(controlDTO.getFromStation().getCode());
					fromStationIO.setName(controlDTO.getFromStation().getName());
					toStationIO.setCode(controlDTO.getToStation().getCode());
					toStationIO.setName(controlDTO.getToStation().getName());
					stageIO.setFromStation(fromStationIO);
					stageIO.setToStation(toStationIO);
				}
				stageIO.setSchedule(scheduleIO);
				if (controlDTO.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(controlDTO.getGroup().getCode());
					groupIO.setName(controlDTO.getGroup().getName());
					groupIO.setLevel(controlDTO.getGroup().getLevel());
					stageIO.setGroup(groupIO);
				}

				// override
				List<ScheduleControlIO> overrideIOList = new ArrayList<ScheduleControlIO>();

				for (ScheduleControlDTO lookupControlDTO : controlDTO.getOverrideList()) {
					ScheduleControlIO lookupStageIO = new ScheduleControlIO();
					lookupStageIO.setCode(lookupControlDTO.getCode());
					lookupStageIO.setName(lookupControlDTO.getName());
					lookupStageIO.setActiveFrom(lookupControlDTO.getActiveFrom());
					lookupStageIO.setActiveTo(lookupControlDTO.getActiveTo());
					lookupStageIO.setActiveFlag(lookupControlDTO.getActiveFlag());
					lookupStageIO.setCloseMinitues(lookupControlDTO.getCloseMinitues());
					lookupStageIO.setOpenMinitues(lookupControlDTO.getOpenMinitues());
					lookupStageIO.setDayOfWeek(lookupControlDTO.getDayOfWeek());
					ScheduleIO lookupScheduleIO = new ScheduleIO();
					lookupScheduleIO.setCode(lookupControlDTO.getSchedule().getCode());
					lookupStageIO.setAllowBookingFlag(lookupControlDTO.getAllowBookingFlag());
					lookupStageIO.setSchedule(lookupScheduleIO);
					if (lookupControlDTO.getGroup() != null) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(lookupControlDTO.getGroup().getCode());
						groupIO.setName(lookupControlDTO.getGroup().getName());
						groupIO.setLevel(lookupControlDTO.getGroup().getLevel());
						lookupStageIO.setGroup(groupIO);
					}
					overrideIOList.add(lookupStageIO);
				}
				stageIO.setOverrideList(overrideIOList);

				controlIOList.add(stageIO);
			}
		}
		return ResponseIO.success(controlIOList);
	}

	@RequestMapping(value = "/control/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleControlIO> updateScheduleStageControl(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleControlIO> stageIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleControlIO scheduleStageIO = new ScheduleControlIO();
		List<ScheduleControlDTO> stageDTOList = new ArrayList<ScheduleControlDTO>();
		if (authDTO != null) {
			for (ScheduleControlIO stageIO : stageIOList) {
				ScheduleControlDTO stageDTO = new ScheduleControlDTO();
				stageDTO.setCode(stageIO.getCode());
				stageDTO.setName(stageIO.getName());
				stageDTO.setActiveFrom(stageIO.getActiveFrom());
				stageDTO.setActiveTo(stageIO.getActiveTo());
				stageDTO.setOpenMinitues(stageIO.getOpenMinitues());
				stageDTO.setCloseMinitues(stageIO.getCloseMinitues());
				stageDTO.setDayOfWeek(stageIO.getDayOfWeek());
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(stageIO.getSchedule().getCode());
				// Stage based booking open/close time
				if (stageIO.getFromStation() != null && stageIO.getToStation() != null && StringUtil.isNotNull(stageIO.getFromStation().getCode()) && StringUtil.isNotNull(stageIO.getToStation().getCode())) {
					StationDTO FromstationDTO = new StationDTO();
					StationDTO TostationDTO = new StationDTO();
					FromstationDTO.setCode(stageIO.getFromStation().getCode());
					TostationDTO.setCode(stageIO.getToStation().getCode());
					stageDTO.setFromStation(FromstationDTO);
					stageDTO.setToStation(TostationDTO);
				}
				stageDTO.setSchedule(scheduleDTO);
				stageDTO.setAllowBookingFlag(stageIO.getAllowBookingFlag());
				if (stageIO.getGroup() != null) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(stageIO.getGroup().getCode());
					stageDTO.setGroup(groupDTO);
				}

				stageDTO.setLookupCode(stageIO.getLookupCode());
				stageDTO.setActiveFlag(stageIO.getActiveFlag());
				// Delete Operation
				if (stageDTO.getActiveFlag() != 1 && StringUtil.isNotNull(stageDTO.getCode())) {
					stageDTO.setName("DEFAULT");
					stageDTO.setActiveFrom("2014-06-06");
					stageDTO.setActiveTo("2014-06-06");
					stageDTO.setDayOfWeek("0000000");
				}
				if (StringUtil.isNotNull(stageIO.getDayOfWeek()) && stageIO.getDayOfWeek().length() != 7) {
					throw new ServiceException(ErrorCode.INVALID_DAYOFFWEEK, stageIO.getDayOfWeek() + " Should be 7 leter");
				}
				stageDTOList.add(stageDTO);
			}
			ScheduleControlDTO stageDTO = new ScheduleControlDTO();
			stageDTO.setList(stageDTOList);
			controlService.Update(authDTO, stageDTO);
			scheduleStageIO.setCode(stageDTO.getCode());
			scheduleStageIO.setActiveFlag(stageDTO.getActiveFlag());

		}
		return ResponseIO.success(scheduleStageIO);

	}

	@RequestMapping(value = "/seatvisibility/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleSeatVisibilityIO>> getScheduleSeatVisibility(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleSeatVisibilityIO> stvisibilityIOList = new ArrayList<ScheduleSeatVisibilityIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleSeatVisibilityDTO dto = new ScheduleSeatVisibilityDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleSeatVisibilityDTO> list = seatVisibilityService.get(authDTO, dto);
			for (ScheduleSeatVisibilityDTO visibilityDTO : list) {
				if (!new DateTime(visibilityDTO.getActiveTo()).plusDays(1).isAfterNow()) {
					continue;
				}
				ScheduleSeatVisibilityIO visibilityIO = new ScheduleSeatVisibilityIO();
				visibilityIO.setCode(visibilityDTO.getCode());
				visibilityIO.setName(visibilityDTO.getName());
				visibilityIO.setActiveFrom(visibilityDTO.getActiveFrom());
				visibilityIO.setActiveTo(visibilityDTO.getActiveTo());
				visibilityIO.setActiveFlag(visibilityDTO.getActiveFlag());
				visibilityIO.setDayOfWeek(visibilityDTO.getDayOfWeek());
				visibilityIO.setRemarks(visibilityDTO.getRemarks());
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(visibilityDTO.getSchedule().getCode());
				visibilityIO.setVisibilityType(visibilityDTO.getVisibilityType());
				visibilityIO.setUpdatedAt(visibilityDTO.getUpdatedAt());
				visibilityIO.setUpdatedBy(visibilityDTO.getUpdatedBy());
				visibilityIO.setSchedule(scheduleIO);
				visibilityIO.setRoleType(visibilityDTO.getRefferenceType());
				visibilityIO.setReleaseMinutes(visibilityDTO.getReleaseMinutes());

				// Stage based booking open/close time
				if (visibilityDTO.getBus() != null) {
					BusIO busIO = new BusIO();
					busIO.setCode(visibilityDTO.getBus().getCode());
					busIO.setName(visibilityDTO.getBus().getName());
					busIO.setCategoryCode(visibilityDTO.getBus().getCategoryCode() == null ? "" : visibilityDTO.getBus().getCategoryCode());
					busIO.setDisplayName(visibilityDTO.getBus().getDisplayName() == null ? "" : visibilityDTO.getBus().getDisplayName());
					visibilityIO.setBus(busIO);
				}
				List<BusSeatLayoutIO> layoutIOList = new ArrayList<BusSeatLayoutIO>();
				for (BusSeatLayoutDTO layoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
					BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
					layoutIO.setCode(layoutDTO.getCode());
					layoutIO.setSeatName(layoutDTO.getName());
					// BusSeatTypeIO busSeatTypeIO = new BusSeatTypeIO();
					// busSeatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
					// busSeatTypeIO.setName(layoutDTO.getBusSeatType().getName());
					// layoutIO.setBusSeatType(busSeatTypeIO);
					layoutIO.setColPos(layoutDTO.getColPos());
					layoutIO.setRowPos(layoutDTO.getRowPos());
					layoutIO.setLayer(layoutDTO.getLayer());
					layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
					layoutIOList.add(layoutIO);
				}
				visibilityIO.setBusSeatLayout(layoutIOList);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				if (visibilityDTO.getGroupList() != null) {
					for (GroupDTO groupDTO : visibilityDTO.getGroupList()) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(groupDTO.getCode());
						groupIO.setName(groupDTO.getName());
						groupIO.setLevel(groupDTO.getLevel());
						visibilityIO.setGroup(groupIO);
						groupList.add(groupIO);
					}
				}
				visibilityIO.setGroupList(groupList);

				List<UserIO> userList = new ArrayList<UserIO>();
				if (visibilityDTO.getUserList() != null) {
					for (UserDTO userDTO : visibilityDTO.getUserList()) {
						UserIO userIO = new UserIO();
						userIO.setCode(userDTO.getCode());
						userIO.setName(userDTO.getName());
						visibilityIO.setUser(userIO);
						userList.add(userIO);
					}
				}
				visibilityIO.setUserList(userList);

				List<RouteIO> routeList = new ArrayList<RouteIO>();
				if (visibilityDTO.getRouteList() != null) {
					for (RouteDTO routeDTO : visibilityDTO.getRouteList()) {
						RouteIO route = new RouteIO();

						StationIO fromStation = new StationIO();
						fromStation.setCode(routeDTO.getFromStation().getCode());
						fromStation.setName(routeDTO.getFromStation().getName());
						route.setFromStation(fromStation);

						StationIO toStation = new StationIO();
						toStation.setCode(routeDTO.getToStation().getCode());
						toStation.setName(routeDTO.getToStation().getName());
						route.setToStation(toStation);

						visibilityIO.setFromStation(fromStation);
						visibilityIO.setToStation(toStation);
						routeList.add(route);
					}

					List<UserIO> routeUsers = new ArrayList<UserIO>();
					if (visibilityDTO.getRouteUsers() != null) {
						for (UserDTO userDTO2 : visibilityDTO.getRouteUsers()) {
							UserIO userIO2 = new UserIO();
							userIO2.setCode(userDTO2.getCode());
							userIO2.setName(userDTO2.getName());
							routeUsers.add(userIO2);
						}
					}
					visibilityIO.setRouteUsers(routeUsers);
				}
				visibilityIO.setRouteList(routeList);

				List<OrganizationIO> organizationList = new ArrayList<OrganizationIO>();
				if (visibilityDTO.getOrganizations() != null) {
					for (OrganizationDTO organizationDTO : visibilityDTO.getOrganizations()) {
						OrganizationIO organizationIO = new OrganizationIO();
						organizationIO.setCode(organizationDTO.getCode());
						organizationIO.setName(organizationDTO.getName());
						organizationIO.setShortCode(organizationDTO.getShortCode());

						StationIO orgStationIO = new StationIO();
						if (organizationDTO.getStation() != null) {
							orgStationIO.setCode(organizationDTO.getStation().getCode());
							orgStationIO.setName(organizationDTO.getStation().getName());
						}
						organizationIO.setStation(orgStationIO);
						organizationList.add(organizationIO);
					}
				}
				visibilityIO.setOrganizations(organizationList);

				// Override
				List<ScheduleSeatVisibilityIO> overrideSeatVisibilityIOList = new ArrayList<ScheduleSeatVisibilityIO>();
				for (ScheduleSeatVisibilityDTO overrideVisibilityDTO : visibilityDTO.getOverrideList()) {
					if (!new DateTime(visibilityDTO.getActiveTo()).plusDays(1).isAfterNow()) {
						continue;
					}
					ScheduleSeatVisibilityIO overrideVisibilityIO = new ScheduleSeatVisibilityIO();
					overrideVisibilityIO.setCode(overrideVisibilityDTO.getCode());
					overrideVisibilityIO.setName(overrideVisibilityDTO.getName());
					overrideVisibilityIO.setActiveFrom(overrideVisibilityDTO.getActiveFrom());
					overrideVisibilityIO.setActiveTo(overrideVisibilityDTO.getActiveTo());
					overrideVisibilityIO.setActiveFlag(overrideVisibilityDTO.getActiveFlag());
					overrideVisibilityIO.setDayOfWeek(overrideVisibilityDTO.getDayOfWeek());
					overrideVisibilityIO.setReleaseMinutes(overrideVisibilityDTO.getReleaseMinutes());
					overrideVisibilityIO.setUpdatedAt(overrideVisibilityDTO.getUpdatedAt());
					overrideVisibilityIO.setUpdatedBy(overrideVisibilityDTO.getUpdatedBy());

					ScheduleIO overrideScheduleIO = new ScheduleIO();
					overrideScheduleIO.setCode(overrideVisibilityDTO.getSchedule().getCode());
					overrideVisibilityIO.setVisibilityType(overrideVisibilityDTO.getVisibilityType());
					overrideVisibilityIO.setSchedule(overrideScheduleIO);
					overrideVisibilityIO.setRoleType(overrideVisibilityDTO.getRefferenceType());
					if (overrideVisibilityDTO.getBus() != null) {
						BusIO busIO = new BusIO();
						busIO.setCode(dto.getCode());
						busIO.setName(dto.getName());
						busIO.setCategoryCode(overrideVisibilityDTO.getBus().getCategoryCode() == null ? "" : overrideVisibilityDTO.getBus().getCategoryCode());
						busIO.setDisplayName(overrideVisibilityDTO.getBus().getDisplayName() == null ? "" : overrideVisibilityDTO.getBus().getDisplayName());
						overrideVisibilityIO.setBus(busIO);
					}
					List<BusSeatLayoutIO> lookupLayoutIOList = new ArrayList<BusSeatLayoutIO>();
					for (BusSeatLayoutDTO layoutDTO : overrideVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
						BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
						layoutIO.setCode(layoutDTO.getCode());
						layoutIO.setSeatName(layoutDTO.getName());
						// BusSeatTypeIO busSeatTypeIO = new BusSeatTypeIO();
						// busSeatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
						// busSeatTypeIO.setName(layoutDTO.getBusSeatType().getName());
						// layoutIO.setBusSeatType(busSeatTypeIO);
						layoutIO.setColPos(layoutDTO.getColPos());
						layoutIO.setRowPos(layoutDTO.getRowPos());
						layoutIO.setLayer(layoutDTO.getLayer());
						layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
						lookupLayoutIOList.add(layoutIO);
					}
					overrideVisibilityIO.setBusSeatLayout(lookupLayoutIOList);
					List<GroupIO> groupIOList = new ArrayList<GroupIO>();
					if (overrideVisibilityDTO.getGroupList() != null) {
						for (GroupDTO groupDTO : overrideVisibilityDTO.getGroupList()) {
							GroupIO groupIO = new GroupIO();
							groupIO.setCode(groupDTO.getCode());
							groupIO.setName(groupDTO.getName());
							groupIO.setLevel(groupDTO.getLevel());
							overrideVisibilityIO.setGroup(groupIO);
							groupIOList.add(groupIO);
						}
						overrideVisibilityIO.setGroupList(groupIOList);
					}
					List<UserIO> userIOList = new ArrayList<UserIO>();
					if (overrideVisibilityDTO.getUserList() != null) {
						for (UserDTO userDTO : overrideVisibilityDTO.getUserList()) {
							UserIO userIO = new UserIO();
							userIO.setCode(userDTO.getCode());
							userIO.setName(userDTO.getName());
							overrideVisibilityIO.setUser(userIO);
							userIOList.add(userIO);
						}
						overrideVisibilityIO.setUserList(userIOList);
					}
					List<RouteIO> routeIOList = new ArrayList<RouteIO>();
					if (overrideVisibilityDTO.getRouteList() != null) {
						for (RouteDTO routeDTO : overrideVisibilityDTO.getRouteList()) {
							RouteIO routeIO = new RouteIO();

							StationIO fromStation = new StationIO();
							fromStation.setCode(routeDTO.getFromStation().getCode());
							fromStation.setName(routeDTO.getFromStation().getName());
							routeIO.setFromStation(fromStation);

							StationIO toStation = new StationIO();
							toStation.setCode(routeDTO.getToStation().getCode());
							toStation.setName(routeDTO.getToStation().getName());
							routeIO.setToStation(toStation);
							overrideVisibilityIO.setFromStation(fromStation);
							overrideVisibilityIO.setToStation(toStation);
							routeIOList.add(routeIO);
						}
						overrideVisibilityIO.setRouteList(routeIOList);
					}

					List<OrganizationIO> overrideOrganizationList = new ArrayList<OrganizationIO>();
					if (overrideVisibilityDTO.getOrganizations() != null) {
						for (OrganizationDTO overrideOrganizationDTO : overrideVisibilityDTO.getOrganizations()) {
							OrganizationIO overrideOrganizationIO = new OrganizationIO();
							overrideOrganizationIO.setCode(overrideOrganizationDTO.getCode());
							overrideOrganizationIO.setName(overrideOrganizationDTO.getName());
							overrideOrganizationIO.setShortCode(overrideOrganizationDTO.getShortCode());

							StationIO overrideOrgStationIO = new StationIO();
							if (overrideOrganizationDTO.getStation() != null) {
								overrideOrgStationIO.setCode(overrideOrganizationDTO.getStation().getCode());
								overrideOrgStationIO.setName(overrideOrganizationDTO.getStation().getName());
							}
							overrideOrganizationIO.setStation(overrideOrgStationIO);
							overrideOrganizationList.add(overrideOrganizationIO);
						}
						overrideVisibilityIO.setOrganizations(overrideOrganizationList);
					}
					overrideSeatVisibilityIOList.add(overrideVisibilityIO);
				}
				visibilityIO.setOverrideList(overrideSeatVisibilityIOList);
				stvisibilityIOList.add(visibilityIO);
			}
		}
		return ResponseIO.success(stvisibilityIOList);
	}

	@RequestMapping(value = "/seatvisibility/{travelDate}/v2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getScheduleSeatVisibilityV2(@PathVariable("authtoken") String authtoken, @PathVariable("travelDate") String travelDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(DateUtil.getDateTime(travelDate).getStartOfDay());
		List<Map<String, String>> result = seatVisibilityReportService.getAllScheduleVisibility(authDTO, searchDTO);

		return ResponseIO.success(result);
	}

	@RequestMapping(value = "/seatvisibility/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleSeatVisibilityIO> updateScheduleSeatVisibility(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleSeatVisibilityIO> stageIOList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleSeatVisibilityIO scheduleStageIO = new ScheduleSeatVisibilityIO();
		List<ScheduleSeatVisibilityDTO> stageDTOList = new ArrayList<ScheduleSeatVisibilityDTO>();
		if (authDTO != null) {
			for (ScheduleSeatVisibilityIO visibilityIO : stageIOList) {
				ScheduleSeatVisibilityDTO visibilityDTO = new ScheduleSeatVisibilityDTO();
				visibilityDTO.setCode(visibilityIO.getCode());
				visibilityDTO.setName(visibilityIO.getName());
				visibilityDTO.setRemarks(visibilityIO.getRemarks());
				visibilityDTO.setActiveFrom(visibilityIO.getActiveFrom());
				visibilityDTO.setActiveTo(visibilityIO.getActiveTo());
				List<BusSeatLayoutDTO> layoutList = new ArrayList<>();
				if (visibilityIO.getBusSeatLayout() != null) {
					for (BusSeatLayoutIO layoutIO : visibilityIO.getBusSeatLayout()) {
						BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
						layoutDTO.setCode(layoutIO.getCode());
						layoutDTO.setName(layoutIO.getSeatName());
						layoutList.add(layoutDTO);
					}
				}
				visibilityDTO.setRefferenceType(visibilityIO.getRoleType());
				visibilityDTO.setDayOfWeek(visibilityIO.getDayOfWeek());
				visibilityDTO.setReleaseMinutes(visibilityIO.getReleaseMinutes());

				// Stage based
				List<RouteDTO> routeList = new ArrayList<RouteDTO>();
				if (visibilityIO.getFromStation() != null && visibilityIO.getToStation() != null && StringUtil.isNotNull(visibilityIO.getFromStation().getCode()) && StringUtil.isNotNull(visibilityIO.getToStation().getCode())) {
					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setCode(visibilityIO.getFromStation().getCode());

					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setCode(visibilityIO.getToStation().getCode());

					RouteDTO existRoute = new RouteDTO();
					existRoute.setFromStation(fromStationDTO);
					existRoute.setToStation(toStationDTO);
					routeList.add(existRoute);
				}
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(visibilityIO.getSchedule().getCode());
				visibilityDTO.setSchedule(scheduleDTO);
				visibilityDTO.setVisibilityType(visibilityIO.getVisibilityType());

				// Group
				List<GroupDTO> groupList = new ArrayList<GroupDTO>();
				if (visibilityIO.getRoleType().equals("GR") && visibilityIO.getGroup() != null && StringUtil.isNotNull(visibilityIO.getGroup().getCode())) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(visibilityIO.getGroup().getCode());
					groupList.add(groupDTO);
				}
				if (visibilityIO.getRoleType().equals("GR") && visibilityIO.getGroupList() != null) {
					for (GroupIO group : visibilityIO.getGroupList()) {
						if (StringUtil.isNull(group.getCode())) {
							continue;
						}
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setCode(group.getCode());
						groupList.add(groupDTO);
					}
				}

				// User
				List<UserDTO> userList = new ArrayList<UserDTO>();
				if (visibilityIO.getRoleType().equals("UR") && visibilityIO.getUser() != null && StringUtil.isNotNull(visibilityIO.getUser().getCode())) {
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(visibilityIO.getUser().getCode());
					userList.add(userDTO);
				}
				if (visibilityIO.getRoleType().equals("UR") && visibilityIO.getUserList() != null) {
					for (UserIO user : visibilityIO.getUserList()) {
						if (StringUtil.isNull(user.getCode())) {
							continue;
						}
						UserDTO userDTO = new UserDTO();
						userDTO.setCode(user.getCode());
						userList.add(userDTO);
					}
				}

				visibilityDTO.setGroupList(groupList);
				visibilityDTO.setUserList(userList);

				if (visibilityIO.getRoleType().equals("SG") && visibilityIO.getRouteList() != null) {
					for (RouteIO route : visibilityIO.getRouteList()) {
						if (route.getFromStation() == null || route.getToStation() == null || StringUtil.isNull(route.getFromStation().getCode()) || StringUtil.isNull(route.getToStation().getCode())) {
							continue;
						}
						RouteDTO routeDTO = new RouteDTO();
						StationDTO fromStationDTO = new StationDTO();
						fromStationDTO.setCode(route.getFromStation().getCode());
						routeDTO.setFromStation(fromStationDTO);

						StationDTO toStationDTO = new StationDTO();
						toStationDTO.setCode(route.getToStation().getCode());
						routeDTO.setToStation(toStationDTO);

						routeList.add(routeDTO);
					}
				}
				visibilityDTO.setRouteList(routeList);

				List<UserDTO> routeUsers = new ArrayList<>();
				if (visibilityIO.getRouteUsers() != null) {
					for (UserIO routeUserIO : visibilityIO.getRouteUsers()) {
						if (StringUtil.isNull(routeUserIO.getCode())) {
							continue;
						}
						UserDTO userDTO = new UserDTO();
						userDTO.setCode(routeUserIO.getCode());
						routeUsers.add(userDTO);
					}
				}
				visibilityDTO.setRouteUsers(routeUsers);

				List<OrganizationDTO> organizationList = new ArrayList<OrganizationDTO>();
				if (visibilityIO.getRoleType().equals("BR") && visibilityIO.getOrganizations() != null) {
					for (OrganizationIO organizationIO : visibilityIO.getOrganizations()) {
						if (StringUtil.isNull(organizationIO.getCode())) {
							continue;
						}
						OrganizationDTO organizationDTO = new OrganizationDTO();
						organizationDTO.setCode(organizationIO.getCode());
						organizationList.add(organizationDTO);
					}
				}
				visibilityDTO.setOrganizations(organizationList);

				if (visibilityIO.getBus() != null) {
					BusDTO busDTO = new BusDTO();
					busDTO.setCode(visibilityIO.getBus().getCode());
					BusSeatLayoutDTO busSeatLayoutDTO = new BusSeatLayoutDTO();
					busSeatLayoutDTO.setList(layoutList);
					busDTO.setBusSeatLayoutDTO(busSeatLayoutDTO);
					visibilityDTO.setBus(busDTO);
				}
				visibilityDTO.setLookupCode(visibilityIO.getLookupCode());
				visibilityDTO.setActiveFlag(visibilityIO.getActiveFlag());
				visibilityDTO.setRefferenceType(visibilityIO.getRoleType());
				stageDTOList.add(visibilityDTO);
			}
			ScheduleSeatVisibilityDTO stageDTO = new ScheduleSeatVisibilityDTO();
			stageDTO.setList(stageDTOList);

			// Validate Edit Permission
			checkVisibilityEditPermission(authDTO);

			seatVisibilityService.Update(authDTO, stageDTO);
			scheduleStageIO.setCode(stageDTO.getCode());
			scheduleStageIO.setActiveFlag(stageDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleStageIO);
	}

	private void checkVisibilityEditPermission(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SEAT_VISIBILITY_EDIT_RIGHTS_USER);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		Map<String, String> additionalAttribute = new HashMap<>();
		additionalAttribute.put(Text.SEAT_VISIBILITY_EDIT_RIGHTS, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
		authDTO.setAdditionalAttribute(additionalAttribute);
	}

	@RequestMapping(value = "/seatpreference/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleSeatPreferenceIO>> getScheduleSeatPreference(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleSeatPreferenceIO> stvisibilityIOList = new ArrayList<ScheduleSeatPreferenceIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleSeatPreferenceDTO dto = new ScheduleSeatPreferenceDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleSeatPreferenceDTO> list = seatPreferenceService.get(authDTO, dto);
			for (ScheduleSeatPreferenceDTO visibilityDTO : list) {
				if (!new DateTime(visibilityDTO.getActiveTo()).plusDays(1).isAfterNow()) {
					continue;
				}
				ScheduleSeatPreferenceIO visibilityIO = new ScheduleSeatPreferenceIO();
				visibilityIO.setCode(visibilityDTO.getCode());
				visibilityIO.setName(visibilityDTO.getName());
				visibilityIO.setActiveFrom(visibilityDTO.getActiveFrom());
				visibilityIO.setActiveTo(visibilityDTO.getActiveTo());
				visibilityIO.setActiveFlag(visibilityDTO.getActiveFlag());
				visibilityIO.setDayOfWeek(visibilityDTO.getDayOfWeek());

				if (visibilityDTO.getGendar() != null) {
					visibilityIO.setPreferenceGendar(visibilityDTO.getGendar().getCode());
				}

				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(visibilityDTO.getSchedule().getCode());
				visibilityIO.setSchedule(scheduleIO);
				if (visibilityDTO.getBus() != null) {
					BusIO busIO = new BusIO();
					busIO.setCode(visibilityDTO.getBus().getCode());
					busIO.setName(visibilityDTO.getBus().getName());
					busIO.setCategoryCode(visibilityDTO.getBus().getCategoryCode() == null ? "" : visibilityDTO.getBus().getCategoryCode());
					busIO.setDisplayName(visibilityDTO.getBus().getDisplayName() == null ? "" : visibilityDTO.getBus().getDisplayName());
					visibilityIO.setBus(busIO);
				}
				List<BusSeatLayoutIO> layoutIOList = new ArrayList<BusSeatLayoutIO>();
				for (BusSeatLayoutDTO layoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
					BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
					layoutIO.setCode(layoutDTO.getCode());
					layoutIO.setSeatName(layoutDTO.getName());
					layoutIO.setColPos(layoutDTO.getColPos());
					layoutIO.setRowPos(layoutDTO.getRowPos());
					layoutIO.setLayer(layoutDTO.getLayer());
					layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
					layoutIOList.add(layoutIO);
				}
				visibilityIO.setBusSeatLayout(layoutIOList);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : visibilityDTO.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupList.add(groupIO);
				}

				visibilityIO.setGroupList(groupList);

				List<ScheduleSeatPreferenceIO> overrrideSeatPreferenceIOList = new ArrayList<ScheduleSeatPreferenceIO>();
				for (ScheduleSeatPreferenceDTO overrideSeat : visibilityDTO.getOverrideList()) {
					ScheduleSeatPreferenceIO overrideSeatPreferenceIO = new ScheduleSeatPreferenceIO();
					overrideSeatPreferenceIO.setCode(overrideSeat.getCode());
					overrideSeatPreferenceIO.setName(overrideSeat.getName());
					overrideSeatPreferenceIO.setActiveFrom(overrideSeat.getActiveFrom());
					overrideSeatPreferenceIO.setActiveTo(overrideSeat.getActiveTo());
					overrideSeatPreferenceIO.setActiveFlag(overrideSeat.getActiveFlag());
					overrideSeatPreferenceIO.setDayOfWeek(overrideSeat.getDayOfWeek());
					overrideSeatPreferenceIO.setPreferenceGendar(overrideSeat.getGendar() != null ? overrideSeat.getGendar().getCode() : Text.NA);
					if (overrideSeat.getBus() != null) {
						BusIO busIO = new BusIO();
						busIO.setCode(overrideSeat.getBus().getCode());
						busIO.setName(overrideSeat.getBus().getName());
						busIO.setCategoryCode(overrideSeat.getBus().getCategoryCode() == null ? "" : overrideSeat.getBus().getCategoryCode());
						busIO.setDisplayName(overrideSeat.getBus().getDisplayName() == null ? "" : overrideSeat.getBus().getDisplayName());
						overrideSeatPreferenceIO.setBus(busIO);
					}
					List<BusSeatLayoutIO> overrideLayoutIOList = new ArrayList<BusSeatLayoutIO>();
					for (BusSeatLayoutDTO layoutDTO : overrideSeat.getBus().getBusSeatLayoutDTO().getList()) {
						BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
						layoutIO.setCode(layoutDTO.getCode());
						layoutIO.setSeatName(layoutDTO.getName());
						layoutIO.setColPos(layoutDTO.getColPos());
						layoutIO.setRowPos(layoutDTO.getRowPos());
						layoutIO.setLayer(layoutDTO.getLayer());
						layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
						overrideLayoutIOList.add(layoutIO);
					}
					overrideSeatPreferenceIO.setBusSeatLayout(overrideLayoutIOList);

					List<GroupIO> overrideGroupList = new ArrayList<GroupIO>();
					for (GroupDTO groupDTO : overrideSeat.getGroupList()) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(groupDTO.getCode());
						groupIO.setName(groupDTO.getName());
						overrideGroupList.add(groupIO);
					}
					overrideSeatPreferenceIO.setGroupList(overrideGroupList);
					
					AuditIO auditIO = new AuditIO();
					if (overrideSeat.getAudit() != null) {
						UserIO updatedBy = new UserIO();
						updatedBy.setCode(overrideSeat.getAudit().getUser() != null ? overrideSeat.getAudit().getUser().getCode() : Text.EMPTY);
						updatedBy.setName(overrideSeat.getAudit().getUser() != null ? overrideSeat.getAudit().getUser().getName() : Text.EMPTY);
						auditIO.setUser(updatedBy);
						auditIO.setUpdatedAt(overrideSeat.getAudit().getUpdatedAt());
					}
					overrideSeatPreferenceIO.setAudit(auditIO);
					
					overrrideSeatPreferenceIOList.add(overrideSeatPreferenceIO);
				}
				visibilityIO.setOverrideList(overrrideSeatPreferenceIOList);
				
				AuditIO auditIO = new AuditIO();
				if (visibilityDTO.getAudit() != null) {
					UserIO updatedBy = new UserIO();
					updatedBy.setCode(visibilityDTO.getAudit().getUser() != null ? visibilityDTO.getAudit().getUser().getCode() : Text.EMPTY);
					updatedBy.setName(visibilityDTO.getAudit().getUser() != null ? visibilityDTO.getAudit().getUser().getName() : Text.EMPTY);
					auditIO.setUser(updatedBy);
					auditIO.setUpdatedAt(visibilityDTO.getAudit().getUpdatedAt());
				}
				visibilityIO.setAudit(auditIO);
				stvisibilityIOList.add(visibilityIO);
			}
		}
		return ResponseIO.success(stvisibilityIOList);
	}

	@RequestMapping(value = "/seatpreference/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleSeatPreferenceIO> updateScheduleSeatPreference(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleSeatPreferenceIO seatPreference) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleSeatPreferenceIO preferenceIO = new ScheduleSeatPreferenceIO();
		if (authDTO != null) {
			ScheduleSeatPreferenceDTO preferenceDTO = new ScheduleSeatPreferenceDTO();
			preferenceDTO.setCode(seatPreference.getCode());
			preferenceDTO.setActiveFrom(seatPreference.getActiveFrom());
			preferenceDTO.setActiveTo(seatPreference.getActiveTo());
			List<BusSeatLayoutDTO> layoutList = new ArrayList<>();
			if (seatPreference.getBusSeatLayout() != null) {
				for (BusSeatLayoutIO layoutIO : seatPreference.getBusSeatLayout()) {
					BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
					layoutDTO.setCode(layoutIO.getCode());
					layoutDTO.setName(layoutIO.getSeatName());
					layoutList.add(layoutDTO);
				}
			}
			if (seatPreference.getSchedule() == null) {
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_CODE);
			}
			preferenceDTO.setDayOfWeek(seatPreference.getDayOfWeek());
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(seatPreference.getSchedule().getCode());
			preferenceDTO.setSchedule(scheduleDTO);
			preferenceDTO.setGendar(StringUtil.isNotNull(seatPreference.getPreferenceGendar()) ? SeatGendarEM.getSeatGendarEM(seatPreference.getPreferenceGendar()) : null);
			if (seatPreference.getBus() != null) {
				BusDTO busDTO = new BusDTO();
				busDTO.setCode(seatPreference.getBus().getCode());
				BusSeatLayoutDTO busSeatLayoutDTO = new BusSeatLayoutDTO();
				busSeatLayoutDTO.setList(layoutList);
				busDTO.setBusSeatLayoutDTO(busSeatLayoutDTO);
				preferenceDTO.setBus(busDTO);
			}

			List<GroupDTO> groupList = new ArrayList<>();
			if (seatPreference.getGroupList() != null) {
				for (GroupIO group : seatPreference.getGroupList()) {
					if (StringUtil.isNull(group.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}
			preferenceDTO.setGroupList(groupList);
			preferenceDTO.setLookupCode(seatPreference.getLookupCode());
			preferenceDTO.setActiveFlag(seatPreference.getActiveFlag());
			seatPreferenceService.Update(authDTO, preferenceDTO);
			preferenceIO.setCode(preferenceDTO.getCode());
			preferenceIO.setActiveFlag(preferenceDTO.getActiveFlag());

		}
		return ResponseIO.success(preferenceIO);
	}

	@RequestMapping(value = "/{scheduleCode}/refresh", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<String> getScheduleRefresh(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO dto = new ScheduleDTO();
			dto.setCode(scheduleCode);
			scheduleService.getRefresh(authDTO, dto);
			if (dto.getPreRequrities() == null || !dto.getPreRequrities().equals("000000")) {
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA);
			}
		}
		return ResponseIO.success("Successfully Created");
	}

	@RequestMapping(value = "/cache/{scheduleCode}/clear", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<String> clearScheduleCache(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO dto = new ScheduleDTO();
			dto.setCode(scheduleCode);
			scheduleService.clearScheduleCache(authDTO, dto);
		}
		return ResponseIO.success("Schedule Cache Cleared");
	}

	@RequestMapping(value = "/prerequisites/{condition}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleIO>> getPrerequisites(@PathVariable("authtoken") String authtoken, @PathVariable("condition") String condition) throws Exception {
		List<ScheduleIO> PrerequisitesList = new ArrayList<ScheduleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			// Sector Permission
			checkSectorPermission(authDTO);

			List<ScheduleDTO> list = null;
			if ("closed".equals(condition)) {
				list = scheduleService.getClosed(authDTO);
			}
			else if ("none".equals(condition)) {
				list = scheduleService.getActive(authDTO, DateUtil.NOW());
			}
			else if ("partial".equals(condition)) {
				list = scheduleService.getPartial(authDTO);
			}
			else if ("expire".equals(condition)) {
				list = scheduleService.getExpire(authDTO);
			}
			else {
				list = new ArrayList<>();
			}
			List<ScheduleDTO> finalList = new ArrayList<>();

			boolean scheduleVisibilityCheck = getPrivilege(authDTO, MenuEventEM.BOOKING_SCHEDULE_VIEW_RIGHTS_ALL);
			if (scheduleVisibilityCheck) {
				List<ScheduleDTO> rightsSchedule = visibilityService.getUserActiveSchedule(authDTO);
				for (ScheduleDTO rightsscheduleDTO : rightsSchedule) {
					for (Iterator<ScheduleDTO> scheIterator = list.iterator(); scheIterator.hasNext();) {
						ScheduleDTO scheduleDTO = scheIterator.next();
						if (scheduleDTO.getId() == rightsscheduleDTO.getId()) {
							finalList.add(scheduleDTO);
							break;
						}
					}
				}
			}
			else {
				finalList.addAll(list);
			}
			// Sorting
			Comparator<ScheduleDTO> comp = new BeanComparator("name");
			Collections.sort(finalList, comp);

			for (ScheduleDTO scheduleDTO : finalList) {
				ScheduleIO scheduleio = new ScheduleIO();
				scheduleio.setCode(scheduleDTO.getCode());
				scheduleio.setName(scheduleDTO.getName());
				scheduleio.setServiceNumber(scheduleDTO.getServiceNumber());
				scheduleio.setActiveFrom(scheduleDTO.getActiveFrom());
				scheduleio.setActiveTo(scheduleDTO.getActiveTo());
				scheduleio.setDisplayName(scheduleDTO.getDisplayName());
				scheduleio.setActiveFlag(scheduleDTO.getActiveFlag());
				scheduleio.setPnrStartCode(scheduleDTO.getPnrStartCode());
				scheduleio.setDayOfWeek(scheduleDTO.getDayOfWeek());

				if (scheduleDTO.getCategory() != null) {
					ScheduleCategoryIO categoryIO = new ScheduleCategoryIO();
					categoryIO.setCode(scheduleDTO.getCategory().getCode());
					categoryIO.setName(scheduleDTO.getCategory().getName());
					categoryIO.setActiveFlag(scheduleDTO.getCategory().getActiveFlag());
					scheduleio.setCategory(categoryIO);
				}
				if (scheduleDTO.getScheduleBus() != null && scheduleDTO.getScheduleBus().getBus() != null) {
					BusIO busIO = new BusIO();
					busIO.setCode(scheduleDTO.getScheduleBus().getBus().getCode());
					busIO.setCategoryCode(scheduleDTO.getScheduleBus().getBus().getCategoryCode());
					busIO.setDisplayName(scheduleDTO.getScheduleBus().getBus().getDisplayName());
					busIO.setName(scheduleDTO.getScheduleBus().getBus().getName());
					busIO.setBusType(BitsUtil.getBusCategoryUsingEM(scheduleDTO.getScheduleBus().getBus().getCategoryCode()));
					busIO.setSeatCount(scheduleDTO.getScheduleBus().getBus().getReservableLayoutSeatCount());
					scheduleio.setBus(busIO);
				}
				List<ScheduleTagIO> scheudleTagList = new ArrayList<>();
				for (ScheduleTagDTO scheduleTag : scheduleDTO.getScheduleTagList()) {
					ScheduleTagIO scheduleTagIO = new ScheduleTagIO();
					scheduleTagIO.setCode(scheduleTag.getCode());
					scheduleTagIO.setName(scheduleTag.getName());
					scheudleTagList.add(scheduleTagIO);
				}
				scheduleio.setScheduleTagList(scheudleTagList);
				scheduleio.setDistance(scheduleDTO.getDistance());

				List<SectorIO> sectorList = new ArrayList<SectorIO>();
				for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
					SectorIO sectorIO = new SectorIO();
					sectorIO.setCode(sectorDTO.getCode());
					sectorIO.setName(sectorDTO.getName());
					sectorList.add(sectorIO);
				}
				scheduleio.setSectorList(sectorList);

				List<StageIO> stageList = new ArrayList<>();
				// Stage
				if (scheduleDTO.getStageList() != null) {
					Map<String, String> stageMap = new HashMap<>();
					// From Station
					for (StageDTO stageDTO : scheduleDTO.getStageList()) {
						StageIO stageIO = new StageIO();
						org.in.com.controller.commerce.io.StationIO fromStation = new org.in.com.controller.commerce.io.StationIO();
						fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
						fromStation.setName(stageDTO.getFromStation().getStation().getName());
						fromStation.setDateTime("" + stageDTO.getFromStation().getMinitues());
						if (stageMap.get(fromStation.getCode()) != null) {
							continue;
						}
						stageMap.put(fromStation.getCode(), fromStation.getCode());
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
						List<org.in.com.controller.commerce.io.StationPointIO> fromStationPoint = new ArrayList<>();
						for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
							org.in.com.controller.commerce.io.StationPointIO pointIO = new org.in.com.controller.commerce.io.StationPointIO();
							pointIO.setDateTime("" + (stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()));
							pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
							pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
							pointIO.setCode(pointDTO.getCode());
							pointIO.setName(pointDTO.getName());
							pointIO.setLandmark(pointDTO.getLandmark());
							pointIO.setAddress(pointDTO.getAddress());
							pointIO.setNumber(pointDTO.getNumber());
							pointIO.setVanRouteEnabledFlag(pointDTO.getVanRouteEnabledFlag());
							fromStationPoint.add(pointIO);
						}
						stageIO.setStageSequence(stageDTO.getFromStation().getStationSequence());
						fromStation.setStationPoint(fromStationPoint);
						stageIO.setFromStation(fromStation);
						stageList.add(stageIO);
					}
					// To stations
					for (StageDTO stageDTO : scheduleDTO.getStageList()) {
						StageIO stageIO = new StageIO();
						org.in.com.controller.commerce.io.StationIO toStation = new org.in.com.controller.commerce.io.StationIO();
						toStation.setCode(stageDTO.getToStation().getStation().getCode());
						toStation.setName(stageDTO.getToStation().getStation().getName());
						toStation.setDateTime("" + stageDTO.getToStation().getMinitues());
						if (stageMap.get(toStation.getCode()) != null) {
							continue;
						}
						stageMap.put(toStation.getCode(), toStation.getCode());
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
						List<org.in.com.controller.commerce.io.StationPointIO> fromStationPoint = new ArrayList<>();
						for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
							org.in.com.controller.commerce.io.StationPointIO pointIO = new org.in.com.controller.commerce.io.StationPointIO();
							pointIO.setDateTime("" + (stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()));
							pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
							pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
							pointIO.setCode(pointDTO.getCode());
							pointIO.setName(pointDTO.getName());
							pointIO.setLandmark(pointDTO.getLandmark());
							pointIO.setAddress(pointDTO.getAddress());
							pointIO.setNumber(pointDTO.getNumber());
							pointIO.setVanRouteEnabledFlag(pointDTO.getVanRouteEnabledFlag());
							fromStationPoint.add(pointIO);
						}
						List<org.in.com.controller.commerce.io.StationPointIO> toStationPoint = new ArrayList<>();
						for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
							org.in.com.controller.commerce.io.StationPointIO pointIO = new org.in.com.controller.commerce.io.StationPointIO();
							pointIO.setDateTime("" + (stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()));
							pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
							pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
							pointIO.setCode(pointDTO.getCode());
							pointIO.setName(pointDTO.getName());
							pointIO.setLandmark(pointDTO.getLandmark());
							pointIO.setAddress(pointDTO.getAddress());
							pointIO.setNumber(pointDTO.getNumber());
							pointIO.setVanRouteEnabledFlag(pointDTO.getVanRouteEnabledFlag());
							toStationPoint.add(pointIO);
						}
						stageIO.setStageSequence(stageDTO.getToStation().getStationSequence());
						toStation.setStationPoint(toStationPoint);
						stageIO.setFromStation(toStation);
						stageList.add(stageIO);
					}
					scheduleio.setStageList(stageList);
					// Sorting
					Comparator<StageIO> compStage = new BeanComparator("stageSequence");
					Collections.sort(stageList, compStage);
				}
				PrerequisitesList.add(scheduleio);
			}

		}
		return ResponseIO.success(PrerequisitesList);
	}

	private void checkSectorPermission(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		Map<String, String> additionalAttribute = new HashMap<>();
		additionalAttribute.put(Text.SECTOR, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
		authDTO.setAdditionalAttribute(additionalAttribute);
	}

	@RequestMapping(value = "/seat/auto/release", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleSeatAutoReleaseIO>> getScheduleSeatAutoRelease(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleSeatAutoReleaseIO> releaseIOlist = new ArrayList<ScheduleSeatAutoReleaseIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<ScheduleSeatAutoReleaseDTO> list = scheduleSeatAutoReleaseService.getAllScheduleSeatAutoRelease(authDTO);
		for (ScheduleSeatAutoReleaseDTO releaseDTO : list) {
			ScheduleSeatAutoReleaseIO scheduleSeatAutoReleaseIO = new ScheduleSeatAutoReleaseIO();
			scheduleSeatAutoReleaseIO.setCode(releaseDTO.getCode());
			scheduleSeatAutoReleaseIO.setReleaseMinutes(releaseDTO.getReleaseMinutes());
			scheduleSeatAutoReleaseIO.setDayOfWeek(releaseDTO.getDayOfWeek());
			scheduleSeatAutoReleaseIO.setActiveFrom(releaseDTO.getActiveFrom());
			scheduleSeatAutoReleaseIO.setActiveTo(releaseDTO.getActiveTo());

			scheduleSeatAutoReleaseIO.setMinutesType(releaseDTO.getMinutesTypeEM().getCode());
			scheduleSeatAutoReleaseIO.setReleaseMode(releaseDTO.getReleaseModeEM().getCode());
			scheduleSeatAutoReleaseIO.setReleaseType(releaseDTO.getReleaseTypeEM().getCode());

			scheduleSeatAutoReleaseIO.setActiveFlag(releaseDTO.getActiveFlag());

			List<GroupIO> groups = new ArrayList<>();
			for (GroupDTO groupDTO : releaseDTO.getGroups()) {
				GroupIO groupIO = new GroupIO();
				groupIO.setCode(groupDTO.getCode());
				groupIO.setName(groupDTO.getName());
				groupIO.setLevel(groupDTO.getLevel());
				groups.add(groupIO);
			}
			scheduleSeatAutoReleaseIO.setGroups(groups);

			List<ScheduleIO> schedules = new ArrayList<>();
			for (ScheduleDTO scheduleDTO : releaseDTO.getSchedules()) {
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(scheduleDTO.getCode());
				scheduleIO.setName(scheduleDTO.getName());
				scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
				schedules.add(scheduleIO);
			}
			scheduleSeatAutoReleaseIO.setSchedules(schedules);

			// Override
			if (!releaseDTO.getOverrideList().isEmpty()) {
				List<ScheduleSeatAutoReleaseIO> overrideAutoReleaseIOlist = new ArrayList<ScheduleSeatAutoReleaseIO>();
				for (ScheduleSeatAutoReleaseDTO overrideAutoReleaseDTO : releaseDTO.getOverrideList()) {
					ScheduleSeatAutoReleaseIO overrideAutoReleaseIO = new ScheduleSeatAutoReleaseIO();

					List<GroupIO> overrideGroups = new ArrayList<>();
					for (GroupDTO groupDTO : overrideAutoReleaseDTO.getGroups()) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(groupDTO.getCode());
						groupIO.setName(groupDTO.getName());
						groupIO.setLevel(groupDTO.getLevel());
						overrideGroups.add(groupIO);
					}
					overrideAutoReleaseIO.setGroups(overrideGroups);

					List<ScheduleIO> overrideSchedules = new ArrayList<>();
					for (ScheduleDTO scheduleDTO : overrideAutoReleaseDTO.getSchedules()) {
						ScheduleIO scheduleIO = new ScheduleIO();
						scheduleIO.setCode(scheduleDTO.getCode());
						scheduleIO.setName(scheduleDTO.getName());
						scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
						overrideSchedules.add(scheduleIO);
					}
					overrideAutoReleaseIO.setSchedules(overrideSchedules);

					overrideAutoReleaseIO.setCode(overrideAutoReleaseDTO.getCode());

					overrideAutoReleaseIO.setReleaseMinutes(overrideAutoReleaseDTO.getReleaseMinutes());
					// overrideAutoReleaseIO.setReleaseMode(overrideAutoReleaseDTO.getReleaseModeEM().getCode());
					// overrideAutoReleaseIO.setReleaseType(overrideAutoReleaseDTO.getReleaseTypeEM().getCode());
					overrideAutoReleaseIO.setDayOfWeek(overrideAutoReleaseDTO.getDayOfWeek());
					// overrideAutoReleaseIO.setMinutesType(overrideAutoReleaseDTO.getMinutesTypeEM().getCode());

					overrideAutoReleaseIO.setActiveFrom(overrideAutoReleaseDTO.getActiveFrom());
					overrideAutoReleaseIO.setActiveTo(overrideAutoReleaseDTO.getActiveTo());
					overrideAutoReleaseIO.setActiveFlag(overrideAutoReleaseDTO.getActiveFlag());
					overrideAutoReleaseIOlist.add(overrideAutoReleaseIO);
				}
				scheduleSeatAutoReleaseIO.setOverrideList(overrideAutoReleaseIOlist);
			}
			releaseIOlist.add(scheduleSeatAutoReleaseIO);
		}
		return ResponseIO.success(releaseIOlist);
	}

	@RequestMapping(value = "/seat/auto/release/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleSeatAutoReleaseIO> updateScheduleSeatAutoRelease(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleSeatAutoReleaseIO seatAutoRelease) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleSeatAutoReleaseIO seatAutoReleaseIO = new ScheduleSeatAutoReleaseIO();
		if (authDTO != null) {
			ScheduleSeatAutoReleaseDTO releaseDTO = new ScheduleSeatAutoReleaseDTO();
			releaseDTO.setCode(seatAutoRelease.getCode());

			List<GroupDTO> groups = new ArrayList<>();
			if (seatAutoRelease.getGroups() != null) {
				for (GroupIO group : seatAutoRelease.getGroups()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groups.add(groupDTO);
				}
			}
			releaseDTO.setGroups(groups);

			List<ScheduleDTO> schedules = new ArrayList<>();
			if (seatAutoRelease.getSchedules() != null) {
				for (ScheduleIO scheduleIO : seatAutoRelease.getSchedules()) {
					ScheduleDTO schedule = new ScheduleDTO();
					schedule.setCode(scheduleIO.getCode());
					schedules.add(schedule);
				}
			}

			releaseDTO.setSchedules(schedules);
			releaseDTO.setReleaseMinutes(seatAutoRelease.getReleaseMinutes());
			releaseDTO.setReleaseModeEM(ReleaseModeEM.getReleaseModeEM(seatAutoRelease.getReleaseMode()));
			releaseDTO.setReleaseTypeEM(ReleaseTypeEM.getReleaseTypeEM(seatAutoRelease.getReleaseType()));
			releaseDTO.setMinutesTypeEM(MinutesTypeEM.getMinutesTypeEM(seatAutoRelease.getMinutesType()));
			releaseDTO.setDayOfWeek(seatAutoRelease.getDayOfWeek());
			releaseDTO.setActiveFrom(seatAutoRelease.getActiveFrom());
			releaseDTO.setActiveTo(seatAutoRelease.getActiveTo());
			releaseDTO.setLookupCode(seatAutoRelease.getLookupCode());
			releaseDTO.setActiveFlag(seatAutoRelease.getActiveFlag());
			scheduleSeatAutoReleaseService.Update(authDTO, releaseDTO);
			seatAutoReleaseIO.setCode(releaseDTO.getCode());
			seatAutoReleaseIO.setActiveFlag(releaseDTO.getActiveFlag());
		}
		return ResponseIO.success(seatAutoReleaseIO);
	}

	@RequestMapping(value = "/visibility/{scheduleCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserIO>> getScheduleVisibility(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<UserIO> userList = new ArrayList<>();
		if (authDTO != null) {

			List<UserDTO> dtoList = visibilityService.get(authDTO, scheduleCode);
			for (UserDTO userDTO : dtoList) {
				UserIO user = new UserIO();
				user.setName(userDTO.getName());
				user.setCode(userDTO.getCode());
				user.setActiveFlag(userDTO.getActiveFlag());
				userList.add(user);
			}
		}
		return ResponseIO.success(userList);
	}

	@RequestMapping(value = "/visibility/{scheduleCode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateScheduleVisibility(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode, @RequestBody List<UserIO> userList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<UserDTO> userDTOList = new ArrayList<>();
			if (userList != null && !userList.isEmpty()) {
				for (UserIO user : userList) {
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(user.getCode());
					userDTOList.add(userDTO);
				}
				boolean status = visibilityService.Update(authDTO, scheduleCode, userDTOList);
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/discount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleDiscountIO>> getScheduleDiscount(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleDiscountIO> discountIOList = new ArrayList<ScheduleDiscountIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleDiscountDTO> list = discountService.get(authDTO);
			for (ScheduleDiscountDTO discountDTO : list) {
				if (!new DateTime(discountDTO.getActiveTo()).plusDays(1).isAfterNow()) {
					continue;
				}
				ScheduleDiscountIO discountIO = new ScheduleDiscountIO();
				discountIO.setCode(discountDTO.getCode());
				discountIO.setName(discountDTO.getName());
				discountIO.setActiveFrom(discountDTO.getActiveFrom());
				discountIO.setActiveTo(discountDTO.getActiveTo());
				discountIO.setAfterBookingMinutes(discountDTO.getAfterBookingMinutes());
				discountIO.setActiveFlag(discountDTO.getActiveFlag());
				discountIO.setDayOfWeek(discountDTO.getDayOfWeek());
				discountIO.setDiscountValue(discountDTO.getDiscountValue());
				discountIO.setPercentageFlag(discountDTO.getPercentageFlag());
				discountIO.setFemaleDiscountFlag(discountDTO.getFemaleDiscountFlag());
				discountIO.setAdvanceBookingDays(discountDTO.getAdvanceBookingDays());

				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO schedule : discountDTO.getScheduleList()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(schedule.getCode());
					scheduleIO.setName(schedule.getName());
					scheduleIO.setServiceNumber(schedule.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				discountIO.setScheduleList(scheduleList);

				BaseIO authenticationType = new BaseIO();
				authenticationType.setCode(discountDTO.getAuthenticationType().getCode());
				authenticationType.setName(discountDTO.getAuthenticationType().getName());
				discountIO.setAuthenticationType(authenticationType);

				BaseIO deviceMedium = new BaseIO();
				deviceMedium.setCode(discountDTO.getDeviceMedium().getCode());
				deviceMedium.setName(discountDTO.getDeviceMedium().getName());
				discountIO.setDeviceMedium(deviceMedium);
				discountIO.setDateType(discountDTO.getDateType().getCode());

				List<GroupIO> groupList = new ArrayList<>();
				for (GroupDTO groupDTO : discountDTO.getGroupList()) {
					GroupIO group = new GroupIO();
					group.setCode(groupDTO.getCode());
					group.setName(groupDTO.getName());
					groupList.add(group);
				}
				discountIO.setGroupList(groupList);

				// Override
				List<ScheduleDiscountIO> overrideIOList = new ArrayList<ScheduleDiscountIO>();
				for (ScheduleDiscountDTO overrideVisibilityDTO : discountDTO.getOverrideList()) {
					if (!new DateTime(discountDTO.getActiveTo()).plusDays(1).isAfterNow()) {
						continue;
					}
					ScheduleDiscountIO overrideIO = new ScheduleDiscountIO();
					overrideIO.setCode(overrideVisibilityDTO.getCode());
					overrideIO.setName(overrideVisibilityDTO.getName());
					overrideIO.setActiveFrom(overrideVisibilityDTO.getActiveFrom());
					overrideIO.setActiveTo(overrideVisibilityDTO.getActiveTo());
					overrideIO.setActiveFlag(overrideVisibilityDTO.getActiveFlag());
					overrideIO.setDayOfWeek(overrideVisibilityDTO.getDayOfWeek());

					List<ScheduleIO> scheduleOverrideList = new ArrayList<>();
					for (ScheduleDTO schedule : overrideVisibilityDTO.getScheduleList()) {
						ScheduleIO scheduleIO = new ScheduleIO();
						scheduleIO.setCode(schedule.getCode());
						scheduleIO.setName(schedule.getName());
						scheduleIO.setServiceNumber(schedule.getServiceNumber());
						scheduleOverrideList.add(scheduleIO);
					}
					overrideIO.setScheduleList(scheduleOverrideList);

					List<GroupIO> groupOverrideList = new ArrayList<>();
					for (GroupDTO groupDTO : overrideVisibilityDTO.getGroupList()) {
						GroupIO group = new GroupIO();
						group.setCode(groupDTO.getCode());
						group.setName(groupDTO.getName());
						groupOverrideList.add(group);
					}
					overrideIO.setGroupList(groupOverrideList);

					overrideIOList.add(overrideIO);
				}
				discountIO.setOverrideList(overrideIOList);
				discountIOList.add(discountIO);
			}
		}
		return ResponseIO.success(discountIOList);
	}

	@RequestMapping(value = "/discount/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleDiscountIO> updateScheduleDiscount(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleDiscountIO discount) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDiscountIO scheduleDiscountIO = new ScheduleDiscountIO();
		if (authDTO != null) {
			ScheduleDiscountDTO discountDTO = new ScheduleDiscountDTO();
			discountDTO.setCode(discount.getCode());
			discountDTO.setName(discount.getName());
			discountDTO.setActiveFrom(discount.getActiveFrom());
			discountDTO.setActiveTo(discount.getActiveTo());
			discountDTO.setAfterBookingMinutes(discount.getAfterBookingMinutes());
			discountDTO.setDayOfWeek(discount.getDayOfWeek());
			discountDTO.setDiscountValue(discount.getDiscountValue());
			discountDTO.setPercentageFlag(discount.getPercentageFlag());
			discountDTO.setAuthenticationType(AuthenticationTypeEM.getAuthenticationTypeEM(discount.getAuthenticationType() != null ? discount.getAuthenticationType().getCode() : "ALL"));
			discountDTO.setFemaleDiscountFlag(discount.getFemaleDiscountFlag());
			discountDTO.setAdvanceBookingDays(discount.getAdvanceBookingDays());
			if (StringUtil.isNotNull(discount.getDateType())) {
				discountDTO.setDateType(DateTypeEM.getDateTypeEM(discount.getDateType()));
			}
			discountDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(discount.getDeviceMedium() != null ? discount.getDeviceMedium().getCode() : "ALL"));

			List<ScheduleDTO> scheduleList = new ArrayList<>();
			if (discount.getScheduleList() != null) {
				for (ScheduleIO schedule : discount.getScheduleList()) {
					ScheduleDTO scheduleDTO1 = new ScheduleDTO();
					scheduleDTO1.setCode(schedule.getCode());
					scheduleList.add(scheduleDTO1);
				}
			}
			discountDTO.setScheduleList(scheduleList);

			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			if (discount.getGroupList() != null) {
				for (GroupIO GroupIO : discount.getGroupList()) {
					GroupDTO group = new GroupDTO();
					group.setCode(GroupIO.getCode());
					groupList.add(group);
				}
			}
			discountDTO.setGroupList(groupList);

			discountDTO.setLookupCode(discount.getLookupCode());
			discountDTO.setActiveFlag(discount.getActiveFlag());
			discountService.Update(authDTO, discountDTO);
			scheduleDiscountIO.setCode(discountDTO.getCode());
			scheduleDiscountIO.setActiveFlag(discountDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleDiscountIO);
	}

	@RequestMapping(value = "/seatFare/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleSeatFareIO>> getScheduleSeatFare(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleSeatFareIO> seatFareList = new ArrayList<ScheduleSeatFareIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleSeatFareDTO dto = new ScheduleSeatFareDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleSeatFareDTO> list = seatFareService.get(authDTO, dto);
			for (ScheduleSeatFareDTO seatFareDTO : list) {
				ScheduleSeatFareIO seatFareIO = new ScheduleSeatFareIO();

				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(seatFareDTO.getSchedule().getCode());

				seatFareIO.setCode(seatFareDTO.getCode());
				seatFareIO.setDayOfWeek(seatFareDTO.getDayOfWeek());
				seatFareIO.setActiveFrom(seatFareDTO.getActiveFrom());
				seatFareIO.setActiveTo(seatFareDTO.getActiveTo());
				seatFareIO.setSeatFare(seatFareDTO.getSeatFare());
				seatFareIO.setFareOverrideType(seatFareDTO.getFareOverrideType() != null ? seatFareDTO.getFareOverrideType().getCode() : "");
				seatFareIO.setFareType(seatFareDTO.getFareType() != null ? seatFareDTO.getFareType().getCode() : "");
				seatFareIO.setActiveFlag(seatFareDTO.getActiveFlag());

				if (seatFareDTO.getBus() != null) {
					BusIO busIO = new BusIO();
					busIO.setCode(dto.getCode());
					busIO.setName(dto.getName());
					busIO.setCategoryCode(seatFareDTO.getBus().getCategoryCode() == null ? "" : seatFareDTO.getBus().getCategoryCode());
					busIO.setDisplayName(seatFareDTO.getBus().getDisplayName() == null ? "" : seatFareDTO.getBus().getDisplayName());
					seatFareIO.setBus(busIO);
				}
				List<BusSeatLayoutIO> layoutIOList = new ArrayList<BusSeatLayoutIO>();
				for (BusSeatLayoutDTO layoutDTO : seatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
					BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
					layoutIO.setCode(layoutDTO.getCode());
					layoutIO.setSeatName(layoutDTO.getName());
					BusSeatTypeIO busSeatTypeIO = new BusSeatTypeIO();
					busSeatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
					busSeatTypeIO.setName(layoutDTO.getBusSeatType().getName());
					layoutIO.setBusSeatType(busSeatTypeIO);
					layoutIO.setColPos(layoutDTO.getColPos());
					layoutIO.setRowPos(layoutDTO.getRowPos());
					layoutIO.setLayer(layoutDTO.getLayer());
					layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
					layoutIOList.add(layoutIO);
				}
				seatFareIO.setBusSeatLayout(layoutIOList);

				List<RouteIO> routeList = new ArrayList<RouteIO>();
				for (RouteDTO routeDTO : seatFareDTO.getRoutes()) {
					RouteIO route = new RouteIO();
					StationIO fromStation = new StationIO();
					fromStation.setCode(routeDTO.getFromStation().getCode());
					fromStation.setName(routeDTO.getFromStation().getName());
					route.setFromStation(fromStation);
					seatFareIO.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(routeDTO.getToStation().getCode());
					toStation.setName(routeDTO.getToStation().getName());
					route.setToStation(toStation);
					seatFareIO.setToStation(toStation);
					routeList.add(route);
				}

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : seatFareDTO.getGroups()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupIO.setLevel(groupDTO.getLevel());
					groupList.add(groupIO);
					seatFareIO.setGroup(groupIO);
				}

				seatFareIO.setRouteList(routeList);
				seatFareIO.setGroupList(groupList);

				// Override
				if (!seatFareDTO.getOverrideList().isEmpty()) {
					List<ScheduleSeatFareIO> overrideAutoReleaseIOlist = new ArrayList<ScheduleSeatFareIO>();
					for (ScheduleSeatFareDTO overrideSeatFareDTO : seatFareDTO.getOverrideList()) {
						ScheduleSeatFareIO overrideSeatFareIO = new ScheduleSeatFareIO();
						overrideSeatFareIO.setCode(overrideSeatFareDTO.getCode());
						overrideSeatFareIO.setDayOfWeek(overrideSeatFareDTO.getDayOfWeek());
						overrideSeatFareIO.setActiveFrom(overrideSeatFareDTO.getActiveFrom());
						overrideSeatFareIO.setActiveTo(overrideSeatFareDTO.getActiveTo());
						overrideSeatFareIO.setActiveFlag(overrideSeatFareDTO.getActiveFlag());
						overrideSeatFareIO.setSeatFare(overrideSeatFareDTO.getSeatFare());
						overrideAutoReleaseIOlist.add(overrideSeatFareIO);
					}
					seatFareIO.setOverrideList(overrideAutoReleaseIOlist);
				}
				seatFareList.add(seatFareIO);
			}
		}
		return ResponseIO.success(seatFareList);
	}

	@RequestMapping(value = "/seatFare/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleSeatFareIO> updateScheduleSeatFare(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleSeatFareIO seatFare) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleSeatFareIO seatFareIO = new ScheduleSeatFareIO();
		if (authDTO != null) {
			ScheduleSeatFareDTO seatFareDTO = new ScheduleSeatFareDTO();
			if (seatFare.getSchedule() != null) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(seatFare.getSchedule().getCode());
				seatFareDTO.setSchedule(scheduleDTO);
			}
			seatFareDTO.setCode(seatFare.getCode());
			seatFareDTO.setSeatFare(seatFare.getSeatFare());
			seatFareDTO.setFareType(FareTypeEM.getFareTypeEM(seatFare.getFareType()));
			seatFareDTO.setFareOverrideType(FareOverrideTypeEM.getFareOverrideTypeEM(seatFare.getFareOverrideType()));

			List<BusSeatLayoutDTO> layoutList = new ArrayList<>();
			if (seatFare.getBusSeatLayout() != null) {
				for (BusSeatLayoutIO layoutIO : seatFare.getBusSeatLayout()) {
					BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
					layoutDTO.setCode(layoutIO.getCode());
					layoutList.add(layoutDTO);
				}
				if (layoutList.size() > 15) {
					throw new ServiceException(ErrorCode.SEAT_NOT_ALLOWED_ABOVE_20);
				}
			}
			List<GroupDTO> groupList = new ArrayList<>();
			if (seatFare.getGroup() != null && StringUtil.isNotNull(seatFare.getGroup().getCode())) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(seatFare.getGroup().getCode());
				groupList.add(groupDTO);
			}

			if (seatFare.getGroupList() != null) {
				for (GroupIO group : seatFare.getGroupList()) {
					if (StringUtil.isNull(group.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}
			seatFareDTO.setGroups(groupList);

			if (seatFare.getBus() != null) {
				BusDTO busDTO = new BusDTO();
				busDTO.setCode(seatFare.getBus().getCode());
				BusSeatLayoutDTO busSeatLayoutDTO = new BusSeatLayoutDTO();
				busSeatLayoutDTO.setList(layoutList);
				busDTO.setBusSeatLayoutDTO(busSeatLayoutDTO);
				seatFareDTO.setBus(busDTO);
			}
			List<RouteDTO> routeList = new ArrayList<RouteDTO>();
			if (seatFare.getFromStation() != null && StringUtil.isNotNull(seatFare.getFromStation().getCode()) && seatFare.getToStation() != null && StringUtil.isNotNull(seatFare.getToStation().getCode())) {
				RouteDTO routeDTO = new RouteDTO();

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setCode(seatFare.getFromStation().getCode().trim());
				routeDTO.setFromStation(fromStationDTO);

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setCode(seatFare.getToStation().getCode().trim());
				routeDTO.setToStation(toStationDTO);

				routeList.add(routeDTO);
			}

			if (seatFare.getRouteList() != null && !seatFare.getRouteList().isEmpty()) {
				for (RouteIO route : seatFare.getRouteList()) {
					if (route.getFromStation() != null && StringUtil.isNotNull(route.getFromStation().getCode()) && route.getToStation() != null && StringUtil.isNotNull(route.getToStation().getCode())) {
						RouteDTO routeDTO = new RouteDTO();
						StationDTO fromStationDTO = new StationDTO();
						fromStationDTO.setCode(route.getFromStation().getCode().trim());

						StationDTO toStationDTO = new StationDTO();
						toStationDTO.setCode(route.getToStation().getCode().trim());
						routeDTO.setFromStation(fromStationDTO);
						routeDTO.setToStation(toStationDTO);
						routeList.add(routeDTO);
					}
				}
			}
			seatFareDTO.setRoutes(routeList);

			seatFareDTO.setDayOfWeek(seatFare.getDayOfWeek());
			seatFareDTO.setActiveFrom(seatFare.getActiveFrom());
			seatFareDTO.setActiveTo(seatFare.getActiveTo());
			seatFareDTO.setLookupCode(seatFare.getLookupCode());
			seatFareDTO.setActiveFlag(seatFare.getActiveFlag());
			seatFareService.Update(authDTO, seatFareDTO);
			seatFareIO.setCode(seatFareDTO.getCode());
			seatFareIO.setActiveFlag(seatFareDTO.getActiveFlag());
		}
		return ResponseIO.success(seatFareIO);
	}

	@RequestMapping(value = "/fareAutoOverride/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleFareAutoOverrideIO>> getFareAutoOverride(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleFareAutoOverrideIO> releaseIOlist = new ArrayList<ScheduleFareAutoOverrideIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleFareAutoOverrideDTO dto = new ScheduleFareAutoOverrideDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleFareAutoOverrideDTO> list = fareOverrideService.get(authDTO, dto);
			// Sorting Trips
			Collections.sort(list, new Comparator<ScheduleFareAutoOverrideDTO>() {
				public int compare(ScheduleFareAutoOverrideDTO t1, ScheduleFareAutoOverrideDTO t2) {
					return new CompareToBuilder().append(t2.getAudit().getUpdatedAt(), t1.getAudit().getUpdatedAt()).toComparison();
				}
			});
			for (ScheduleFareAutoOverrideDTO fareAutoDTO : list) {
				ScheduleFareAutoOverrideIO fareAutoIO = new ScheduleFareAutoOverrideIO();

				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(schedulecode);

				fareAutoIO.setCode(fareAutoDTO.getCode());
				fareAutoIO.setDayOfWeek(fareAutoDTO.getDayOfWeek());
				fareAutoIO.setActiveFrom(fareAutoDTO.getActiveFrom());
				fareAutoIO.setActiveTo(fareAutoDTO.getActiveTo());
				fareAutoIO.setTag(fareAutoDTO.getTag());

				BaseIO fareMode = new BaseIO();
				fareMode.setCode(fareAutoDTO.getFareOverrideMode().getCode());
				fareMode.setName(fareAutoDTO.getFareOverrideMode().getName());
				fareAutoIO.setFareOverrideMode(fareMode);

				fareAutoIO.setOverrideMinutes(fareAutoDTO.getOverrideMinutes());

				// Bus Seat Type wise fare
				List<BusSeatTypeFareIO> busSeatTypeFareList = new ArrayList<>();
				if (!fareAutoDTO.getBusSeatTypeFare().isEmpty()) {
					for (BusSeatTypeFareDTO seatTypefare : fareAutoDTO.getBusSeatTypeFare()) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(seatTypefare.getFare());
						busSeatTypeFare.setSeatType(seatTypefare.getBusSeatType().getCode());
						busSeatTypeFareList.add(busSeatTypeFare);
					}
				}
				else if (fareAutoDTO.getBusSeatTypeFare().isEmpty() && !fareAutoDTO.getBusSeatType().isEmpty()) {
					for (BusSeatTypeEM busSeatTypeEM : fareAutoDTO.getBusSeatType()) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(fareAutoDTO.getFare());
						busSeatTypeFare.setSeatType(busSeatTypeEM.getCode());
						busSeatTypeFareList.add(busSeatTypeFare);
					}
				}
				fareAutoIO.setBusSeatTypeFare(busSeatTypeFareList);

				List<RouteIO> routeList = new ArrayList<RouteIO>();
				for (RouteDTO routeDTO : fareAutoDTO.getRouteList()) {
					RouteIO route = new RouteIO();
					StationIO fromStation = new StationIO();
					fromStation.setCode(routeDTO.getFromStation().getCode());
					fromStation.setName(routeDTO.getFromStation().getName());
					route.setFromStation(fromStation);
					fareAutoIO.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(routeDTO.getToStation().getCode());
					toStation.setName(routeDTO.getToStation().getName());
					route.setToStation(toStation);
					fareAutoIO.setToStation(toStation);
					routeList.add(route);
				}

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : fareAutoDTO.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupIO.setLevel(groupDTO.getLevel());
					groupList.add(groupIO);
				}

				fareAutoIO.setRouteList(routeList);
				fareAutoIO.setGroupList(groupList);

				UserIO user = new UserIO();
				user.setCode(fareAutoDTO.getAudit().getUser().getCode());
				user.setName(fareAutoDTO.getAudit().getUser().getName());
				fareAutoIO.setUser(user);
				fareAutoIO.setUpdateAt(fareAutoDTO.getAudit().getUpdatedAt());
				fareAutoIO.setActiveFlag(fareAutoDTO.getActiveFlag());

				// Override
				List<ScheduleFareAutoOverrideIO> overrideAutoReleaseIOlist = new ArrayList<ScheduleFareAutoOverrideIO>();
				for (ScheduleFareAutoOverrideDTO overrideAutoReleaseDTO : fareAutoDTO.getOverrideList()) {
					ScheduleFareAutoOverrideIO overrideAutoReleaseIO = new ScheduleFareAutoOverrideIO();

					overrideAutoReleaseIO.setCode(overrideAutoReleaseDTO.getCode());

					List<RouteIO> overrideRouteList = new ArrayList<RouteIO>();
					for (RouteDTO routeDTO : overrideAutoReleaseDTO.getRouteList()) {
						RouteIO route = new RouteIO();
						StationIO fromStation = new StationIO();
						fromStation.setCode(routeDTO.getFromStation().getCode());
						fromStation.setName(routeDTO.getFromStation().getName());
						route.setFromStation(fromStation);
						overrideAutoReleaseIO.setFromStation(fromStation);

						StationIO toStation = new StationIO();
						toStation.setCode(routeDTO.getToStation().getCode());
						toStation.setName(routeDTO.getToStation().getName());
						route.setToStation(toStation);
						overrideAutoReleaseIO.setToStation(toStation);
						overrideRouteList.add(route);
					}

					List<GroupIO> overrideGroupList = new ArrayList<GroupIO>();
					for (GroupDTO groupDTO : overrideAutoReleaseDTO.getGroupList()) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(groupDTO.getCode());
						groupIO.setName(groupDTO.getName());
						groupIO.setLevel(groupDTO.getLevel());
						overrideGroupList.add(groupIO);
					}
					overrideAutoReleaseIO.setGroupList(overrideGroupList);
					overrideAutoReleaseIO.setRouteList(overrideRouteList);

					// Bus Seat Type wise fare
					List<BusSeatTypeFareIO> overrideSeatTypeFareList = new ArrayList<>();
					for (BusSeatTypeFareDTO seatTypefare : overrideAutoReleaseDTO.getBusSeatTypeFare()) {
						BusSeatTypeFareIO busSeatTypeFare = new BusSeatTypeFareIO();
						busSeatTypeFare.setFare(seatTypefare.getFare());
						busSeatTypeFare.setSeatType(seatTypefare.getBusSeatType().getCode());
						overrideSeatTypeFareList.add(busSeatTypeFare);
					}
					overrideAutoReleaseIO.setBusSeatTypeFare(overrideSeatTypeFareList);

					overrideAutoReleaseIO.setActiveFrom(overrideAutoReleaseDTO.getActiveFrom());
					overrideAutoReleaseIO.setActiveTo(overrideAutoReleaseDTO.getActiveTo());
					overrideAutoReleaseIO.setDayOfWeek(overrideAutoReleaseDTO.getDayOfWeek());
					overrideAutoReleaseIO.setTag(overrideAutoReleaseDTO.getTag());

					BaseIO fareOverrideMode = new BaseIO();
					fareOverrideMode.setCode(overrideAutoReleaseDTO.getFareOverrideMode().getCode());
					fareOverrideMode.setName(overrideAutoReleaseDTO.getFareOverrideMode().getName());
					overrideAutoReleaseIO.setFareOverrideMode(fareOverrideMode);

					overrideAutoReleaseIO.setActiveFlag(overrideAutoReleaseDTO.getActiveFlag());

					overrideAutoReleaseIOlist.add(overrideAutoReleaseIO);
				}
				fareAutoIO.setOverrideList(overrideAutoReleaseIOlist);
				releaseIOlist.add(fareAutoIO);
			}
		}
		return ResponseIO.success(releaseIOlist);
	}

	@RequestMapping(value = "/fareAutoOverride/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleFareAutoOverrideIO> updateFareAutoOverride(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleFareAutoOverrideIO fareAutoOverride) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleFareAutoOverrideIO scheduleFareAutoOverrideIO = new ScheduleFareAutoOverrideIO();
		if (authDTO != null) {
			ScheduleFareAutoOverrideDTO scheduleFareAutoOverrideDTO = new ScheduleFareAutoOverrideDTO();
			if (fareAutoOverride.getSchedule() != null) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(fareAutoOverride.getSchedule().getCode());
				scheduleFareAutoOverrideDTO.setSchedule(scheduleDTO);
			}

			List<GroupDTO> groupList = new ArrayList<>();
			if (fareAutoOverride.getGroup() != null && StringUtil.isNotNull(fareAutoOverride.getGroup().getCode())) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(fareAutoOverride.getGroup().getCode());
				groupList.add(groupDTO);
			}

			if (fareAutoOverride.getGroupList() != null) {
				for (GroupIO group : fareAutoOverride.getGroupList()) {
					if (StringUtil.isNull(group.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}

			scheduleFareAutoOverrideDTO.setGroupList(groupList);
			scheduleFareAutoOverrideDTO.setCode(fareAutoOverride.getCode());
			scheduleFareAutoOverrideDTO.setOverrideMinutes(fareAutoOverride.getOverrideMinutes());
			scheduleFareAutoOverrideDTO.setTag(Text.SCHEDULE);
			scheduleFareAutoOverrideDTO.setFareOverrideMode(FareOverrideModeEM.SCHEDULE_FARE);

			// Bus Seat Type wise fare
			List<BusSeatTypeFareDTO> seatTypeFareList = new ArrayList<>();
			if (fareAutoOverride.getBusSeatTypeFare() != null) {
				for (BusSeatTypeFareIO seatTypefare : fareAutoOverride.getBusSeatTypeFare()) {
					if (seatTypefare.getFare().compareTo(BigDecimal.ZERO) == 0) {
						throw new ServiceException(ErrorCode.TRANSACTION_AMOUNT_INVALID, "Fare cannot be Rs.0");
					}
					BusSeatTypeFareDTO busSeatTypeFare = new BusSeatTypeFareDTO();
					busSeatTypeFare.setFare(seatTypefare.getFare());
					busSeatTypeFare.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(seatTypefare.getSeatType()));
					seatTypeFareList.add(busSeatTypeFare);
				}
			}
			scheduleFareAutoOverrideDTO.setBusSeatTypeFare(seatTypeFareList);
			if (!seatTypeFareList.isEmpty()) {
				scheduleFareAutoOverrideDTO.setFareOverrideMode(FareOverrideModeEM.SCHEDULE_FARE_V2);
			}

			if (fareAutoOverride.getActiveFlag() == 1 && (fareAutoOverride.getDayOfWeek() == null || fareAutoOverride.getDayOfWeek().length() != 7)) {
				throw new ServiceException(ErrorCode.INVALID_DAYOFFWEEK, fareAutoOverride.getDayOfWeek());
			}

			List<RouteDTO> routeList = new ArrayList<RouteDTO>();
			if (fareAutoOverride.getFromStation() != null && StringUtil.isNotNull(fareAutoOverride.getFromStation().getCode()) && fareAutoOverride.getToStation() != null && StringUtil.isNotNull(fareAutoOverride.getToStation().getCode())) {
				RouteDTO routeDTO = new RouteDTO();

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setCode(fareAutoOverride.getFromStation().getCode().trim());
				routeDTO.setFromStation(fromStationDTO);

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setCode(fareAutoOverride.getToStation().getCode().trim());
				routeDTO.setToStation(toStationDTO);

				routeList.add(routeDTO);
			}

			if (fareAutoOverride.getRouteList() != null && !fareAutoOverride.getRouteList().isEmpty()) {
				for (RouteIO route : fareAutoOverride.getRouteList()) {
					if (route.getFromStation() != null && StringUtil.isNotNull(route.getFromStation().getCode()) && route.getToStation() != null && StringUtil.isNotNull(route.getToStation().getCode())) {
						RouteDTO routeDTO = new RouteDTO();
						StationDTO fromStationDTO = new StationDTO();
						fromStationDTO.setCode(route.getFromStation().getCode().trim());

						StationDTO toStationDTO = new StationDTO();
						toStationDTO.setCode(route.getToStation().getCode().trim());
						routeDTO.setFromStation(fromStationDTO);
						routeDTO.setToStation(toStationDTO);
						routeList.add(routeDTO);
					}
				}
			}
			scheduleFareAutoOverrideDTO.setRouteList(routeList);

			scheduleFareAutoOverrideDTO.setDayOfWeek(fareAutoOverride.getDayOfWeek());
			scheduleFareAutoOverrideDTO.setActiveFrom(fareAutoOverride.getActiveFrom());
			scheduleFareAutoOverrideDTO.setActiveTo(fareAutoOverride.getActiveTo());
			scheduleFareAutoOverrideDTO.setLookupCode(fareAutoOverride.getLookupCode());
			scheduleFareAutoOverrideDTO.setActiveFlag(fareAutoOverride.getActiveFlag());
			fareOverrideService.Update(authDTO, scheduleFareAutoOverrideDTO);
			scheduleFareAutoOverrideIO.setCode(scheduleFareAutoOverrideDTO.getCode());
			scheduleFareAutoOverrideIO.setActiveFlag(scheduleFareAutoOverrideDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleFareAutoOverrideIO);
	}

	@RequestMapping(value = "/timeOverride/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleTimeOverrideIO>> getTimeOverride(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleTimeOverrideIO> releaseIOlist = new ArrayList<ScheduleTimeOverrideIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleTimeOverrideDTO dto = new ScheduleTimeOverrideDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleTimeOverrideDTO> list = timeOverrideService.get(authDTO, dto);
			for (ScheduleTimeOverrideDTO releaseDTO : list) {
				ScheduleTimeOverrideIO timeIO = new ScheduleTimeOverrideIO();

				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(schedulecode);

				timeIO.setCode(releaseDTO.getCode());
				timeIO.setDayOfWeek(releaseDTO.getDayOfWeek());
				timeIO.setActiveFrom(releaseDTO.getActiveFrom());
				timeIO.setActiveTo(releaseDTO.getActiveTo());

				timeIO.setOverrideType(releaseDTO.getOverrideType().getCode());
				timeIO.setOverrideMinutes(releaseDTO.getOverrideMinutes());
				timeIO.setReactionFlag(releaseDTO.isReactionFlag());
				if (releaseDTO.getStation() != null && releaseDTO.getStation().getId() != 0) {
					StationIO fromStationIO = new StationIO();
					fromStationIO.setCode(releaseDTO.getStation().getCode());
					fromStationIO.setName(releaseDTO.getStation().getName());
					timeIO.setStation(fromStationIO);
				}
				timeIO.setActiveFlag(releaseDTO.getActiveFlag());

				UserIO updatedUser = new UserIO();
				updatedUser.setCode(releaseDTO.getUpdatedUser().getCode());
				updatedUser.setName(releaseDTO.getUpdatedUser().getName());
				timeIO.setUpdatedUser(updatedUser);
				timeIO.setUpdatedAt(releaseDTO.getUpdatedAt().format(Text.DATE_TIME_DATE4J));

				// Override
				if (!releaseDTO.getOverrideList().isEmpty()) {
					List<ScheduleTimeOverrideIO> overrideAutoReleaseIOlist = new ArrayList<ScheduleTimeOverrideIO>();
					for (ScheduleTimeOverrideDTO overrideAutoReleaseDTO : releaseDTO.getOverrideList()) {
						ScheduleTimeOverrideIO overrideAutoReleaseIO = new ScheduleTimeOverrideIO();

						overrideAutoReleaseIO.setCode(overrideAutoReleaseDTO.getCode());

						overrideAutoReleaseIO.setOverrideType(overrideAutoReleaseDTO.getOverrideType().getCode());
						overrideAutoReleaseIO.setReactionFlag(overrideAutoReleaseDTO.isReactionFlag());
						overrideAutoReleaseIO.setOverrideMinutes(overrideAutoReleaseDTO.getOverrideMinutes());

						overrideAutoReleaseIO.setActiveFrom(overrideAutoReleaseDTO.getActiveFrom());
						overrideAutoReleaseIO.setActiveTo(overrideAutoReleaseDTO.getActiveTo());
						overrideAutoReleaseIO.setDayOfWeek(overrideAutoReleaseDTO.getDayOfWeek());
						overrideAutoReleaseIO.setActiveFlag(overrideAutoReleaseDTO.getActiveFlag());

						UserIO overrideUpdatedUser = new UserIO();
						overrideUpdatedUser.setCode(overrideAutoReleaseDTO.getUpdatedUser().getCode());
						overrideUpdatedUser.setName(overrideAutoReleaseDTO.getUpdatedUser().getName());
						overrideAutoReleaseIO.setUpdatedUser(updatedUser);

						overrideAutoReleaseIO.setUpdatedAt(overrideAutoReleaseDTO.getUpdatedAt().format(Text.DATE_TIME_DATE4J));
						overrideAutoReleaseIOlist.add(overrideAutoReleaseIO);
					}
					timeIO.setOverrideList(overrideAutoReleaseIOlist);
				}
				releaseIOlist.add(timeIO);
			}
		}
		return ResponseIO.success(releaseIOlist);
	}

	@RequestMapping(value = "/timeOverride/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleTimeOverrideIO> updateTimeOverride(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleTimeOverrideIO seatAutoRelease) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleTimeOverrideIO seatAutoReleaseIO = new ScheduleTimeOverrideIO();
		if (authDTO != null) {
			ScheduleTimeOverrideDTO releaseDTO = new ScheduleTimeOverrideDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			if (seatAutoRelease.getSchedule() != null) {
				scheduleDTO.setCode(seatAutoRelease.getSchedule().getCode());
			}
			releaseDTO.setSchedule(scheduleDTO);
			releaseDTO.setCode(seatAutoRelease.getCode());

			releaseDTO.setOverrideMinutes(seatAutoRelease.getOverrideMinutes());
			releaseDTO.setOverrideType(OverrideTypeEM.getOverrideTypeEM(seatAutoRelease.getOverrideType()));
			releaseDTO.setReactionFlag(seatAutoRelease.isReactionFlag());

			if (seatAutoRelease.getStation() != null && StringUtil.isNotNull(seatAutoRelease.getStation().getCode())) {
				StationDTO FromstationDTO = new StationDTO();
				FromstationDTO.setCode(seatAutoRelease.getStation().getCode().trim());
				releaseDTO.setStation(FromstationDTO);
			}

			releaseDTO.setDayOfWeek(seatAutoRelease.getDayOfWeek());
			releaseDTO.setActiveFrom(seatAutoRelease.getActiveFrom());
			releaseDTO.setActiveTo(seatAutoRelease.getActiveTo());
			releaseDTO.setLookupCode(seatAutoRelease.getLookupCode());
			releaseDTO.setActiveFlag(seatAutoRelease.getActiveFlag());

			timeOverrideService.Update(authDTO, releaseDTO);
			seatAutoReleaseIO.setCode(releaseDTO.getCode());
			seatAutoReleaseIO.setActiveFlag(releaseDTO.getActiveFlag());
		}
		return ResponseIO.success(seatAutoReleaseIO);
	}

	@RequestMapping(value = "/gallery/image/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<GalleryIO>> getGalleryImage(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<GalleryIO> releaseIOlist = new ArrayList<GalleryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			List<GalleryDTO> list = galleryImageService.getScheduleGallery(authDTO, scheduleDTO);
			for (GalleryDTO galleryDTO : list) {
				GalleryIO gallery = new GalleryIO();
				gallery.setCode(galleryDTO.getCode());
				gallery.setName(galleryDTO.getName());
				gallery.setActiveFlag(galleryDTO.getActiveFlag());
				releaseIOlist.add(gallery);
			}
		}
		return ResponseIO.success(releaseIOlist);
	}

	@RequestMapping(value = "/gallery/image/{schedulecode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> addGalleryImage(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode, @RequestBody GalleryIO gallery) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			GalleryDTO galleryDTO = new GalleryDTO();
			galleryDTO.setCode(gallery.getCode());
			galleryDTO.setActiveFlag(gallery.getActiveFlag());
			galleryImageService.mapScheduleGallery(authDTO, scheduleDTO, galleryDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/getScheduleNotifications/{scheduleCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleNotificationIO>> getScheduleNotifications(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		List<ScheduleNotificationIO> list = new ArrayList<ScheduleNotificationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleNotificationDTO dto = new ScheduleNotificationDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);
			dto.setSchedule(scheduleDTO);
			List<ScheduleNotificationDTO> list1 = notificationService.getAllNotifications(authDTO, scheduleDTO);
			for (ScheduleNotificationDTO scheduleNotificationDTO : list1) {
				ScheduleNotificationIO scheduleNotificationIO = new ScheduleNotificationIO();
				scheduleNotificationIO.setCode(scheduleNotificationDTO.getCode());
				scheduleNotificationIO.setMobileNumber(scheduleNotificationDTO.getMobileNumber());
				scheduleNotificationIO.setMinutes(scheduleNotificationDTO.getMinutes());
				list.add(scheduleNotificationIO);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/updateScheduleNotification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleNotificationIO> getUpdateScheduleNotification(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleNotificationIO scheduleNotificationIO) throws Exception {
		ScheduleNotificationIO notificationIO = new ScheduleNotificationIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleNotificationDTO dto = new ScheduleNotificationDTO();
			dto.setCode(scheduleNotificationIO.getCode());
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleNotificationIO.getSchedule().getCode());
			dto.setMobileNumber(scheduleNotificationIO.getMobileNumber());
			dto.setMinutes(scheduleNotificationIO.getMinutes());
			dto.setActiveFlag(scheduleNotificationIO.getActiveFlag());
			dto.setSchedule(scheduleDTO);
			notificationService.getNotificationUID(authDTO, dto);
			notificationIO.setCode(dto.getCode());
			notificationIO.setActiveFlag(dto.getActiveFlag());
		}
		return ResponseIO.success(notificationIO);
	}

	@RequestMapping(value = "/travel/stops/{schedulecode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> addScheduleStops(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode, @RequestBody TravelStopsIO stopIO) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stopIO.getStations().getCode());
			TravelStopsDTO dto = new TravelStopsDTO();
			dto.setCode(stopIO.getCode());
			dto.setTravelMinutes(stopIO.getTravelMinutes());
			dto.setActiveFlag(stopIO.getActiveFlag());
			dto.setStation(stationDTO);
			dto.setRemarks(stopIO.getRemarks());
			stopService.mapScheduleStops(authDTO, scheduleDTO, dto);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/travel/stops/{schedulecode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TravelStopsIO>> getScheduleStopItems(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) {
		List<TravelStopsIO> stopsList = new ArrayList<TravelStopsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			List<TravelStopsDTO> list = stopService.getScheduleStop(authDTO, scheduleDTO);
			for (TravelStopsDTO dto : list) {
				TravelStopsIO stops = new TravelStopsIO();
				stops.setCode(dto.getCode());
				stops.setName(dto.getName());
				StationIO stationIO = new StationIO();
				stationIO.setCode(dto.getStation().getCode());
				stops.setStations(stationIO);
				stops.setAmenities(dto.getAmenities());
				stops.setRestRoom(dto.getRestRoom());
				stops.setTravelMinutes(dto.getTravelMinutes());
				stops.setMinutes(dto.getMinutes());
				stops.setLandmark(dto.getLandmark());
				stops.setLatitude(dto.getLatitude());
				stops.setLongitude(dto.getLongitude());
				stops.setRemarks(dto.getRemarks());
				stops.setActiveFlag(dto.getActiveFlag());
				stopsList.add(stops);
			}
		}
		return ResponseIO.success(stopsList);
	}

	@RequestMapping(value = "/clone/{schedulecode}/[{entityList}]", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getScheduleClone(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode, @PathVariable String[] entityList, String cloneType) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO base = new BaseIO();
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			if (entityList == null || entityList.length == 0) {
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA);
			}
			List<String> entity = Arrays.asList(entityList);
			if (StringUtil.isNotNull(cloneType) && "REVERSE".equals(cloneType)) {
				scheduleDTO = scheduleService.reverseClone(authDTO, scheduleDTO, entity);
			}
			else {
				scheduleDTO = scheduleService.clone(authDTO, scheduleDTO, entity);
			}
			base.setCode(scheduleDTO.getCode());
		}
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/clone/{schedulecode}/{clonetype}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getScheduleCloneV2(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode, @PathVariable("clonetype") String cloneType, String entity) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO base = new BaseIO();
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			List<String> entityList = Arrays.asList(entity.split(Text.COMMA));
			if (StringUtil.isNull(entityList)) {
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA);
			}
			if (StringUtil.isNotNull(cloneType) && "RETURN".equalsIgnoreCase(cloneType)) {
				scheduleDTO = scheduleService.reverseClone(authDTO, scheduleDTO, entityList);
			}
			else if (StringUtil.isNotNull(cloneType) && "ONWARD".equalsIgnoreCase(cloneType)) {
				scheduleDTO = scheduleService.clone(authDTO, scheduleDTO, entityList);
			}
			base.setCode(scheduleDTO.getCode());
		}
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/category/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleCategoryIO> updateCategory(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleCategoryIO scheduleCategoryIO) throws Exception {
		ScheduleCategoryDTO dto = new ScheduleCategoryDTO();
		ScheduleCategoryIO category = new ScheduleCategoryIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(scheduleCategoryIO.getName())) {
				throw new ServiceException(ErrorCode.NOT_NULL_DATA_FOR_PERSITS);
			}
			dto.setCode(scheduleCategoryIO.getCode());
			dto.setName(scheduleCategoryIO.getName());
			dto.setActiveFlag(scheduleCategoryIO.getActiveFlag());
			scheduleCategoryService.Update(authDTO, dto);
			category.setCode(dto.getCode());
			category.setActiveFlag(dto.getActiveFlag());
		}
		return ResponseIO.success(category);
	}

	@RequestMapping(value = "/category", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleCategoryIO>> getAllCategory(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleCategoryIO> categories = new ArrayList<ScheduleCategoryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleCategoryDTO> list = scheduleCategoryService.getAll(authDTO);
			for (ScheduleCategoryDTO dto : list) {
				ScheduleCategoryIO scheduleCategoryIO = new ScheduleCategoryIO();
				scheduleCategoryIO.setCode(dto.getCode());
				scheduleCategoryIO.setName(dto.getName());
				scheduleCategoryIO.setActiveFlag(dto.getActiveFlag());
				categories.add(scheduleCategoryIO);
			}
		}
		return ResponseIO.success(categories);
	}

	@RequestMapping(value = "/tax", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<NamespaceTaxIO>> getAllTAX(@PathVariable("authtoken") String authtoken) throws Exception {
		List<NamespaceTaxIO> taxlist = new ArrayList<NamespaceTaxIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<NamespaceTaxDTO> list = taxService.getAll(authDTO);
			for (NamespaceTaxDTO taxDTO : list) {
				if (taxDTO.getProductType().getId() != ProductTypeEM.BITS.getId()) {
					continue;
				}
				NamespaceTaxIO tax = new NamespaceTaxIO();
				tax.setActiveFlag(taxDTO.getActiveFlag());
				tax.setCode(taxDTO.getCode());
				tax.setGstin(taxDTO.getGstin());
				tax.setName(taxDTO.getName());
				tax.setSacNumber(taxDTO.getSacNumber());
				tax.setCgstValue(taxDTO.getCgstValue());
				tax.setSgstValue(taxDTO.getSgstValue());
				tax.setUgstValue(taxDTO.getUgstValue());
				tax.setIgstValue(taxDTO.getIgstValue());
				tax.setTradeName(taxDTO.getTradeName());
				taxlist.add(tax);
			}
		}
		return ResponseIO.success(taxlist);
	}

	@RequestMapping(value = "/station/point/exception/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleStationPointIO> updateScheduleSationPointException(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleStationPointIO stationPoint) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleStationPointIO stationPointIO = new ScheduleStationPointIO();
		if (authDTO != null) {
			ScheduleStationPointDTO stationPointDTO = new ScheduleStationPointDTO();

			List<String> tripDates = new ArrayList<>();
			if (stationPoint.getTripDates() != null && !stationPoint.getTripDates().isEmpty()) {
				for (String tripDate : stationPoint.getTripDates()) {
					if (DateUtil.isValidDate(tripDate)) {
						tripDates.add(tripDate);
					}
				}
			}

			List<StationPointDTO> stationPointList = new ArrayList<StationPointDTO>();
			for (StationPointIO point : stationPoint.getStationPointList()) {
				StationPointDTO pointDTO = new StationPointDTO();
				pointDTO.setCode(point.getCode());
				stationPointList.add(pointDTO);
			}
			if (stationPointList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}

			List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
			if (stationPoint.getScheduleList() != null && !stationPoint.getScheduleList().isEmpty()) {
				for (ScheduleIO schedule : stationPoint.getScheduleList()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(schedule.getCode());
					scheduleList.add(scheduleDTO);
				}
			}

			stationPointDTO.setStationPointList(stationPointList);
			stationPointDTO.setScheduleList(scheduleList);

			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stationPoint.getStation().getCode());
			stationPointDTO.setStation(stationDTO);
			stationPointDTO.setCode(stationPoint.getCode());
			stationPointDTO.setDayOfWeek(stationPoint.getDayOfWeek());
			stationPointDTO.setTripDates(tripDates);
			stationPointDTO.setReleaseMinutes(stationPoint.getReleaseMinutes());
			stationPointDTO.setActiveFrom(stationPoint.getActiveFrom());
			stationPointDTO.setActiveTo(stationPoint.getActiveTo());
			stationPointDTO.setActiveFlag(stationPoint.getActiveFlag());
			stationPointDTO.setBoardingDroppingFlag(String.valueOf(stationPoint.getBoardingFlag()) + String.valueOf(stationPoint.getDroppingFlag()));
			if (stationPoint.getBoardingFlag() == 0 && stationPoint.getDroppingFlag() == 0) {
				stationPointDTO.setBoardingDroppingFlag("10");
			}
			stationPointDTO.setStationPointType(StringUtil.isNull(stationPoint.getStationPointType(), "REG"));

			schedulePointService.updateScheduleStationPointException(authDTO, stationPointDTO);
			stationPointIO.setCode(stationPointDTO.getCode());
			stationPointIO.setActiveFlag(stationPointDTO.getActiveFlag());
		}
		return ResponseIO.success(stationPointIO);
	}

	@RequestMapping(value = "/station/point/exception", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleStationPointIO>> getScheduleStationPointException(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleStationPointIO> pointIOlist = new ArrayList<ScheduleStationPointIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleStationPointDTO> list = schedulePointService.getScheduleStationPointException(authDTO);

			for (ScheduleStationPointDTO pointDTO : list) {
				ScheduleStationPointIO scheduleStationPointIO = new ScheduleStationPointIO();

				StationIO stationIO = new StationIO();
				stationIO.setCode(pointDTO.getStation().getCode());
				stationIO.setName(pointDTO.getStation().getName());
				scheduleStationPointIO.setStation(stationIO);

				List<StationPointIO> stationPointList = new ArrayList<StationPointIO>();
				for (StationPointDTO stationPointDTO : pointDTO.getStationPointList()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setCode(stationPointDTO.getCode());
					pointIO.setName(stationPointDTO.getName());
					stationPointList.add(pointIO);
				}
				scheduleStationPointIO.setStationPointList(stationPointList);

				List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
				for (ScheduleDTO scheduleDTO : pointDTO.getScheduleList()) {
					ScheduleIO schedule = new ScheduleIO();
					schedule.setCode(scheduleDTO.getCode());
					schedule.setName(scheduleDTO.getName());
					schedule.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(schedule);
				}
				scheduleStationPointIO.setScheduleList(scheduleList);

				scheduleStationPointIO.setCode(pointDTO.getCode());
				scheduleStationPointIO.setDayOfWeek(pointDTO.getDayOfWeek());
				scheduleStationPointIO.setReleaseMinutes(pointDTO.getReleaseMinutes());
				scheduleStationPointIO.setActiveFrom(pointDTO.getActiveFrom());
				scheduleStationPointIO.setActiveTo(pointDTO.getActiveTo());
				scheduleStationPointIO.setTripDates(pointDTO.getTripDates());
				scheduleStationPointIO.setBoardingFlag(pointDTO.getBoardingFlag());
				scheduleStationPointIO.setDroppingFlag(pointDTO.getDroppingFlag());
				scheduleStationPointIO.setStationPointType(pointDTO.getStationPointType());
				scheduleStationPointIO.setActiveFlag(pointDTO.getActiveFlag());
				pointIOlist.add(scheduleStationPointIO);
			}
		}
		return ResponseIO.success(pointIOlist);
	}

	@RequestMapping(value = "/virtual/seat/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleVirtualSeatBlockIO> updateScheduleVirtualSeatBlock(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleVirtualSeatBlockIO scheduleVirtualSeatBlock) throws Exception {
		ScheduleVirtualSeatBlockIO virtualSeatBlock = new ScheduleVirtualSeatBlockIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlockDTO = new ScheduleVirtualSeatBlockDTO();
			scheduleVirtualSeatBlockDTO.setCode(scheduleVirtualSeatBlock.getCode());
			scheduleVirtualSeatBlockDTO.setActiveFlag(scheduleVirtualSeatBlock.getActiveFlag());
			scheduleVirtualSeatBlockDTO.setActiveFrom(scheduleVirtualSeatBlock.getActiveFrom());
			scheduleVirtualSeatBlockDTO.setActiveTo(scheduleVirtualSeatBlock.getActiveTo());
			scheduleVirtualSeatBlockDTO.setDayOfWeek(scheduleVirtualSeatBlock.getDayOfWeek());
			scheduleVirtualSeatBlockDTO.setLookupCode(scheduleVirtualSeatBlock.getLookupCode());
			scheduleVirtualSeatBlockDTO.setRefreshMinutes(scheduleVirtualSeatBlock.getRefreshMinutes());

			List<String> occuapancyblockPercentage = new ArrayList<String>();
			for (String range : scheduleVirtualSeatBlock.getOccuapancyblockPercentage()) {
				occuapancyblockPercentage.add(range);
			}
			// Exception record
			if (StringUtil.isNull(scheduleVirtualSeatBlockDTO.getLookupCode())) {
				occuapancyblockPercentage.add("NA");
			}
			scheduleVirtualSeatBlockDTO.setOccuapancyblockPercentage(occuapancyblockPercentage);

			List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
			if (scheduleVirtualSeatBlock.getScheduleList() != null && !scheduleVirtualSeatBlock.getScheduleList().isEmpty()) {
				for (ScheduleIO schedule : scheduleVirtualSeatBlock.getScheduleList()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(schedule.getCode());
					scheduleList.add(scheduleDTO);
				}
			}
			scheduleVirtualSeatBlockDTO.setScheduleList(scheduleList);

			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			if (scheduleVirtualSeatBlock.getGroupList() != null && !scheduleVirtualSeatBlock.getGroupList().isEmpty()) {
				for (GroupIO group : scheduleVirtualSeatBlock.getGroupList()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}
			scheduleVirtualSeatBlockDTO.setUserGroupList(groupList);

			virtualSeatBlockService.updateScheduleVirtualSeatBlock(authDTO, scheduleVirtualSeatBlockDTO);
			virtualSeatBlock.setCode(scheduleVirtualSeatBlockDTO.getCode());
			virtualSeatBlock.setRefreshMinutes(scheduleVirtualSeatBlockDTO.getRefreshMinutes());
			virtualSeatBlock.setActiveFlag(scheduleVirtualSeatBlockDTO.getActiveFlag());
		}
		return ResponseIO.success(virtualSeatBlock);
	}

	@RequestMapping(value = "/virtual/seat", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleVirtualSeatBlockIO>> getScheduleVirtualSeatBlock(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleVirtualSeatBlockIO> scheduleVirtualSeatBlockList = new ArrayList<ScheduleVirtualSeatBlockIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleVirtualSeatBlockDTO> list = virtualSeatBlockService.getScheduleVirtualSeatBlock(authDTO);
			for (ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlockDTO : list) {
				ScheduleVirtualSeatBlockIO virtualSeatBlock = new ScheduleVirtualSeatBlockIO();
				virtualSeatBlock.setCode(scheduleVirtualSeatBlockDTO.getCode());
				virtualSeatBlock.setActiveFrom(scheduleVirtualSeatBlockDTO.getActiveFrom());
				virtualSeatBlock.setActiveTo(scheduleVirtualSeatBlockDTO.getActiveTo());
				virtualSeatBlock.setDayOfWeek(scheduleVirtualSeatBlockDTO.getDayOfWeek());
				virtualSeatBlock.setRefreshMinutes(scheduleVirtualSeatBlockDTO.getRefreshMinutes());
				virtualSeatBlock.setActiveFlag(scheduleVirtualSeatBlockDTO.getActiveFlag());

				List<String> occuapancyblockPercentage = new ArrayList<String>();
				for (String range : scheduleVirtualSeatBlockDTO.getOccuapancyblockPercentage()) {
					occuapancyblockPercentage.add(range);
				}
				virtualSeatBlock.setOccuapancyblockPercentage(occuapancyblockPercentage);

				// Schedule
				List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
				for (ScheduleDTO scheduleDTO : scheduleVirtualSeatBlockDTO.getScheduleList()) {
					ScheduleIO schedule = new ScheduleIO();
					schedule.setCode(scheduleDTO.getCode());
					schedule.setName(scheduleDTO.getName());
					schedule.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(schedule);
				}
				virtualSeatBlock.setScheduleList(scheduleList);

				// User Group
				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : scheduleVirtualSeatBlockDTO.getUserGroupList()) {
					GroupIO group = new GroupIO();
					group.setCode(groupDTO.getCode());
					group.setName(groupDTO.getName());
					groupList.add(group);
				}
				virtualSeatBlock.setGroupList(groupList);

				List<ScheduleVirtualSeatBlockIO> exceptionList = new ArrayList<ScheduleVirtualSeatBlockIO>();
				if (!scheduleVirtualSeatBlockDTO.getExceptionList().isEmpty()) {
					for (ScheduleVirtualSeatBlockDTO exceptionDTO : scheduleVirtualSeatBlockDTO.getExceptionList()) {
						ScheduleVirtualSeatBlockIO scheduleVirtualSeatBlockException = new ScheduleVirtualSeatBlockIO();
						scheduleVirtualSeatBlockException.setCode(exceptionDTO.getCode());
						scheduleVirtualSeatBlockException.setActiveFrom(exceptionDTO.getActiveFrom());
						scheduleVirtualSeatBlockException.setActiveTo(exceptionDTO.getActiveTo());
						scheduleVirtualSeatBlockException.setDayOfWeek(exceptionDTO.getDayOfWeek());
						scheduleVirtualSeatBlockException.setRefreshMinutes(exceptionDTO.getRefreshMinutes());
						scheduleVirtualSeatBlockException.setActiveFlag(exceptionDTO.getActiveFlag());
						exceptionList.add(scheduleVirtualSeatBlockException);
					}
					virtualSeatBlock.setExceptionList(exceptionList);
				}
				scheduleVirtualSeatBlockList.add(virtualSeatBlock);
			}
		}
		return ResponseIO.success(scheduleVirtualSeatBlockList);
	}

	@RequestMapping(value = "/book/gender/restriction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleBookGenderRestrictionIO>> getScheduleGenderRestriction(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleBookGenderRestrictionIO> scheduleBookGenderRestrictionList = new ArrayList<ScheduleBookGenderRestrictionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleBookGenderRestrictionDTO> list = scheduleBookGenderRestrictionService.getAll(authDTO);
			for (ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO : list) {
				ScheduleBookGenderRestrictionIO scheduleBookGenderRestriction = new ScheduleBookGenderRestrictionIO();
				scheduleBookGenderRestriction.setCode(scheduleBookGenderRestrictionDTO.getCode());
				scheduleBookGenderRestriction.setDayOfWeek(scheduleBookGenderRestrictionDTO.getDayOfWeek());
				scheduleBookGenderRestriction.setFemaleSeatCount(scheduleBookGenderRestrictionDTO.getFemaleSeatCount());
				scheduleBookGenderRestriction.setSeatTypeGroupModel(scheduleBookGenderRestrictionDTO.getSeatTypeGroupModel() == 2 ? "ALL" : "SEAT_TYPE");

				List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
				List<GroupIO> groupList = new ArrayList<GroupIO>();

				for (ScheduleDTO scheduleDTO : scheduleBookGenderRestrictionDTO.getScheduleList()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}

				for (GroupDTO groupDTO : scheduleBookGenderRestrictionDTO.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupList.add(groupIO);
				}
				scheduleBookGenderRestriction.setReleaseMinutes(scheduleBookGenderRestrictionDTO.getReleaseMinutes());
				scheduleBookGenderRestriction.setScheduleList(scheduleList);
				scheduleBookGenderRestriction.setGroupList(groupList);
				scheduleBookGenderRestriction.setActiveFlag(scheduleBookGenderRestrictionDTO.getActiveFlag());
				scheduleBookGenderRestrictionList.add(scheduleBookGenderRestriction);
			}
		}
		return ResponseIO.success(scheduleBookGenderRestrictionList);
	}

	@RequestMapping(value = "/book/gender/restriction/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleBookGenderRestrictionIO> updateScheduleGenderRestriction(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleBookGenderRestrictionIO scheduleBookGenderRestriction) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleBookGenderRestrictionIO scheduleBookGenderRestrictionIO = new ScheduleBookGenderRestrictionIO();
		if (authDTO != null) {
			ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO = new ScheduleBookGenderRestrictionDTO();
			scheduleBookGenderRestrictionDTO.setCode(scheduleBookGenderRestriction.getCode());
			scheduleBookGenderRestrictionDTO.setDayOfWeek(scheduleBookGenderRestriction.getDayOfWeek());
			scheduleBookGenderRestrictionDTO.setReleaseMinutes(scheduleBookGenderRestriction.getReleaseMinutes());
			scheduleBookGenderRestrictionDTO.setFemaleSeatCount(scheduleBookGenderRestriction.getFemaleSeatCount());
			scheduleBookGenderRestrictionDTO.setSeatTypeGroupModel("ALL".equals(scheduleBookGenderRestriction.getSeatTypeGroupModel()) ? 2 : 1);

			List<ScheduleDTO> scheduleList = new ArrayList<>();
			for (ScheduleIO scheduleIO : scheduleBookGenderRestriction.getScheduleList()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleIO.getCode());
				scheduleList.add(scheduleDTO);
			}

			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			for (GroupIO group : scheduleBookGenderRestriction.getGroupList()) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(group.getCode());
				groupList.add(groupDTO);
			}

			scheduleBookGenderRestrictionDTO.setGroupList(groupList);
			scheduleBookGenderRestrictionDTO.setScheduleList(scheduleList);
			scheduleBookGenderRestrictionDTO.setActiveFlag(scheduleBookGenderRestriction.getActiveFlag());
			scheduleBookGenderRestrictionService.Update(authDTO, scheduleBookGenderRestrictionDTO);
			scheduleBookGenderRestrictionIO.setCode(scheduleBookGenderRestrictionDTO.getCode());
			scheduleBookGenderRestrictionIO.setActiveFlag(scheduleBookGenderRestrictionDTO.getActiveFlag());

		}
		return ResponseIO.success(scheduleBookGenderRestrictionIO);

	}

	@RequestMapping(value = "/sector", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<SectorIO>> getUserSectors(@PathVariable("authtoken") String authtoken) throws Exception {
		List<SectorIO> sectorList = new ArrayList<SectorIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		validateSectorPermission(authDTO);

		List<SectorDTO> list = sectorService.getUserSectors(authDTO);
		for (SectorDTO sectorDTO : list) {
			SectorIO sectorIO = new SectorIO();
			sectorIO.setCode(sectorDTO.getCode());
			sectorIO.setName(sectorDTO.getName());
			sectorIO.setActiveFlag(sectorDTO.getActiveFlag());
			sectorList.add(sectorIO);
		}
		return ResponseIO.success(sectorList);
	}

	@RequestMapping(value = "/bus/{busCode}/utilize", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleIO>> getScheduleByBus(@PathVariable("authtoken") String authtoken, @PathVariable("busCode") String busCode) throws Exception {
		List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(busCode)) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(busCode);
		List<ScheduleDTO> list = scheduleBusService.getScheduleByBus(authDTO, busDTO);

		for (ScheduleDTO scheduleDTO : list) {
			ScheduleIO scheduleIO = new ScheduleIO();
			scheduleIO.setCode(scheduleDTO.getCode());
			scheduleIO.setName(scheduleDTO.getName());
			scheduleIO.setActiveFrom(scheduleDTO.getActiveFrom());
			scheduleIO.setActiveTo(scheduleDTO.getActiveTo());
			scheduleIO.setDayOfWeek(scheduleDTO.getDayOfWeek());
			scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
			scheduleIO.setActiveFlag(scheduleDTO.getActiveFlag());
			scheduleList.add(scheduleIO);
		}

		return ResponseIO.success(scheduleList);
	}

	private void validateSectorPermission(AuthDTO authDTO) {
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		Map<String, String> additionalAttribute = new HashMap<>();
		additionalAttribute.put(Text.SECTOR, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
		authDTO.setAdditionalAttribute(additionalAttribute);
	}
}
