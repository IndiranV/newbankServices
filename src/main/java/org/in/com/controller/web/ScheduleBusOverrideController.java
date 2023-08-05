package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleBusOverrideIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripDTO;
import org.in.com.service.ScheduleBusOverrideService;
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
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;

@Controller
@RequestMapping("/{authtoken}/schedule")
public class ScheduleBusOverrideController extends BaseController {
	@Autowired
	ScheduleBusOverrideService scheduleBusOverrideService;
	@Autowired
	TripService tripService;

	@RequestMapping(value = "/bus/override/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleBusOverrideIO> updateScheduleBusOverride(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleBusOverrideIO scheduleBusOverride) throws Exception {
		ScheduleBusOverrideIO scheduleBusOverrideIO = new ScheduleBusOverrideIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleBusOverrideDTO scheduleBusOverrideDTO = new ScheduleBusOverrideDTO();
			scheduleBusOverrideDTO.setCode(scheduleBusOverride.getCode());
			scheduleBusOverrideDTO.setActiveFrom(scheduleBusOverride.getActiveFrom());
			scheduleBusOverrideDTO.setActiveTo(scheduleBusOverride.getActiveTo());
			scheduleBusOverrideDTO.setDayOfWeek(scheduleBusOverride.getDayOfWeek());
			scheduleBusOverrideDTO.setLookupCode(scheduleBusOverride.getLookupCode());
			scheduleBusOverrideDTO.setActiveFlag(scheduleBusOverride.getActiveFlag());

			List<String> tripDates = new ArrayList<>();
			if (scheduleBusOverride.getTripDates() != null && !scheduleBusOverride.getTripDates().isEmpty()) {
				for (String tripDate : scheduleBusOverride.getTripDates()) {
					if (DateUtil.isValidDate(tripDate)) {
						tripDates.add(tripDate);
					}
				}
			}
			scheduleBusOverrideDTO.setTripDates(tripDates);
			ScheduleDTO schedule = new ScheduleDTO();
			schedule.setCode(scheduleBusOverride.getSchedule().getCode());
			scheduleBusOverrideDTO.setSchedule(schedule);

			BusDTO busDTO = new BusDTO();
			busDTO.setCode(scheduleBusOverride.getBus().getCode());
			busDTO.setCategoryCode(scheduleBusOverride.getBus().getCategoryCode());
			scheduleBusOverrideDTO.setBus(busDTO);

			NamespaceTaxDTO tax = new NamespaceTaxDTO();
			if (scheduleBusOverride.getTax() != null && StringUtil.isNotNull(scheduleBusOverride.getTax().getCode())) {
				tax.setCode(scheduleBusOverride.getTax().getCode());
			}
			scheduleBusOverrideDTO.setTax(tax);

			scheduleBusOverrideService.updateScheduleBusOverride(authDTO, scheduleBusOverrideDTO);
			scheduleBusOverrideIO.setCode(scheduleBusOverrideDTO.getCode());
			scheduleBusOverrideIO.setActiveFlag(scheduleBusOverrideDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleBusOverrideIO);
	}

	@RequestMapping(value = "/bus/override/{tripCode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleBusOverrideIO> updateScheduleBusOverrideForTripDate(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleBusOverrideIO scheduleBusOverride, @PathVariable("tripCode") String tripCode) throws Exception {
		ScheduleBusOverrideIO scheduleBusOverrideIO = new ScheduleBusOverrideIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleBusOverrideDTO scheduleBusOverrideDTO = new ScheduleBusOverrideDTO();
			scheduleBusOverrideDTO.setCode(scheduleBusOverride.getCode());
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			tripService.getTrip(authDTO, tripDTO);
			scheduleBusOverrideDTO.setActiveFrom(DateUtil.convertDate(tripDTO.getTripDate()));
			scheduleBusOverrideDTO.setActiveTo(DateUtil.convertDate(tripDTO.getTripDate()));
			scheduleBusOverrideDTO.setDayOfWeek(scheduleBusOverride.getDayOfWeek());
			scheduleBusOverrideDTO.setLookupCode(scheduleBusOverride.getLookupCode());
			scheduleBusOverrideDTO.setActiveFlag(scheduleBusOverride.getActiveFlag());

			List<String> tripDates = new ArrayList<>();
			if (scheduleBusOverride.getTripDates() != null && !scheduleBusOverride.getTripDates().isEmpty()) {
				for (String travelDate : scheduleBusOverride.getTripDates()) {
					if (DateUtil.isValidDate(travelDate)) {
						tripDates.add(travelDate);
					}
				}
			}
			scheduleBusOverrideDTO.setTripDates(tripDates);
			ScheduleDTO schedule = new ScheduleDTO();
			schedule.setCode(scheduleBusOverride.getSchedule().getCode());
			scheduleBusOverrideDTO.setSchedule(schedule);

			BusDTO busDTO = new BusDTO();
			busDTO.setCode(scheduleBusOverride.getBus().getCode());
			busDTO.setCategoryCode(scheduleBusOverride.getBus().getCategoryCode());
			scheduleBusOverrideDTO.setBus(busDTO);

			NamespaceTaxDTO tax = new NamespaceTaxDTO();
			if (scheduleBusOverride.getTax() != null && StringUtil.isNotNull(scheduleBusOverride.getTax().getCode())) {
				tax.setCode(scheduleBusOverride.getTax().getCode());
			}
			scheduleBusOverrideDTO.setTax(tax);

			scheduleBusOverrideService.updateScheduleBusOverride(authDTO, scheduleBusOverrideDTO);
			scheduleBusOverrideIO.setCode(scheduleBusOverrideDTO.getCode());
			scheduleBusOverrideIO.setActiveFlag(scheduleBusOverrideDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleBusOverrideIO);
	}

	@RequestMapping(value = "/{scheduleCode}/bus/override", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleBusOverrideIO>> getScheduleBusOverride(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		List<ScheduleBusOverrideIO> scheduleBusOverrideList = new ArrayList<ScheduleBusOverrideIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);
			List<ScheduleBusOverrideDTO> list = scheduleBusOverrideService.getScheduleBusOverride(authDTO, scheduleDTO);

			for (ScheduleBusOverrideDTO scheduleBusOverrideDTO : list) {
				ScheduleBusOverrideIO scheduleBusOverrideIO = new ScheduleBusOverrideIO();
				scheduleBusOverrideIO.setCode(scheduleBusOverrideDTO.getCode());
				scheduleBusOverrideIO.setActiveFrom(scheduleBusOverrideDTO.getActiveFrom());
				scheduleBusOverrideIO.setActiveTo(scheduleBusOverrideDTO.getActiveTo());
				scheduleBusOverrideIO.setDayOfWeek(scheduleBusOverrideDTO.getDayOfWeek());
				scheduleBusOverrideIO.setLookupCode(scheduleBusOverrideDTO.getLookupCode());
				scheduleBusOverrideIO.setActiveFlag(scheduleBusOverrideDTO.getActiveFlag());
				Collections.sort(scheduleBusOverrideDTO.getTripDates());
				scheduleBusOverrideIO.setTripDates(scheduleBusOverrideDTO.getTripDates());

				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(scheduleBusOverrideDTO.getSchedule().getCode());
				schedule.setName(scheduleBusOverrideDTO.getSchedule().getName());
				schedule.setServiceNumber(scheduleBusOverrideDTO.getSchedule().getServiceNumber());
				scheduleBusOverrideIO.setSchedule(schedule);

				BusIO bus = new BusIO();
				bus.setCode(scheduleBusOverrideDTO.getBus().getCode());
				bus.setName(scheduleBusOverrideDTO.getBus().getName());
				bus.setCategoryCode(scheduleBusOverrideDTO.getBus().getCategoryCode());
				bus.setTotalSeatCount(scheduleBusOverrideDTO.getBus().getReservableLayoutSeatCount());
				scheduleBusOverrideIO.setBus(bus);

				NamespaceTaxIO tax = new NamespaceTaxIO();
				if (scheduleBusOverrideDTO.getTax() != null && scheduleBusOverrideDTO.getTax().getActiveFlag() != Numeric.ZERO_INT) {
					tax.setActiveFlag(scheduleBusOverrideDTO.getTax().getActiveFlag());
					tax.setCgstValue(scheduleBusOverrideDTO.getTax().getCgstValue());
					tax.setCode(scheduleBusOverrideDTO.getTax().getCode());
					tax.setGstin(scheduleBusOverrideDTO.getTax().getGstin());
					tax.setName(scheduleBusOverrideDTO.getTax().getName());
					tax.setSacNumber(scheduleBusOverrideDTO.getTax().getSacNumber());
					tax.setSgstValue(scheduleBusOverrideDTO.getTax().getSgstValue());
					tax.setUgstValue(scheduleBusOverrideDTO.getTax().getUgstValue());
					tax.setIgstValue(scheduleBusOverrideDTO.getTax().getIgstValue());
					tax.setTradeName(scheduleBusOverrideDTO.getTax().getTradeName());
				}
				scheduleBusOverrideIO.setTax(tax);

				List<ScheduleBusOverrideIO> overrideList = new ArrayList<ScheduleBusOverrideIO>();
				for (ScheduleBusOverrideDTO overrideDTO : scheduleBusOverrideDTO.getOverrideList()) {
					ScheduleBusOverrideIO scheduleBusOverride = new ScheduleBusOverrideIO();
					scheduleBusOverride.setCode(overrideDTO.getCode());
					scheduleBusOverride.setActiveFrom(overrideDTO.getActiveFrom());
					scheduleBusOverride.setActiveTo(overrideDTO.getActiveTo());
					scheduleBusOverride.setDayOfWeek(overrideDTO.getDayOfWeek());
					scheduleBusOverrideIO.setTripDates(scheduleBusOverrideDTO.getTripDates());
					scheduleBusOverride.setLookupCode(overrideDTO.getLookupCode());
					scheduleBusOverride.setActiveFlag(overrideDTO.getActiveFlag());
					overrideList.add(scheduleBusOverride);
				}
				scheduleBusOverrideIO.setOverrideList(overrideList);
				scheduleBusOverrideList.add(scheduleBusOverrideIO);
				Collections.sort(scheduleBusOverrideList, new Comparator<ScheduleBusOverrideIO>() {
					@Override
					public int compare(ScheduleBusOverrideIO o1, ScheduleBusOverrideIO o2) {
						DateTime date1 = DateUtil.isValidDate(o1.getActiveFrom()) ? DateUtil.getDateTime(o1.getActiveFrom()) : DateUtil.getDateTime(o1.getTripDates().get(0));
						DateTime date2 = DateUtil.isValidDate(o2.getActiveFrom()) ? DateUtil.getDateTime(o2.getActiveFrom()) : DateUtil.getDateTime(o2.getTripDates().get(0));
						return new CompareToBuilder().append(date1, date2).toComparison();
					}
				});
			}
		}
		return ResponseIO.success(scheduleBusOverrideList);
	}
}
