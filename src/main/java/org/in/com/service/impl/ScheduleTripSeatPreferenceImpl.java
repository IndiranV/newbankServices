package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.BusCache;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.TripDTO;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleSeatPreferenceService;
import org.in.com.service.ScheduleTripSeatPreferenceService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleTripSeatPreferenceImpl extends BusCache implements ScheduleTripSeatPreferenceService {

	@Autowired
	TripService tripService;
	@Autowired
	ScheduleSeatPreferenceService seatPreferenceService;
	@Autowired
	GroupService groupService;

	@Override
	public ScheduleSeatPreferenceDTO updateTripSeatPereference(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatPreferenceDTO preferenceDTO) {
		ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO = new ScheduleSeatPreferenceDTO();
		try {
			// Trip
			tripService.getTrip(authDTO, tripDTO);

			// Schedule
			ScheduleCache scheduleCache = new ScheduleCache();
			scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
			preferenceDTO.setSchedule(tripDTO.getSchedule());

			// Bus
			getBusDTObyId(authDTO, tripDTO.getBus());

			preferenceDTO.getBus().setCode(tripDTO.getBus().getCode());
			preferenceDTO.setActiveFrom(tripDTO.getTripDate().format(Text.DATE_DATE4J));
			preferenceDTO.setActiveTo(tripDTO.getTripDate().format(Text.DATE_DATE4J));
			preferenceDTO.setDayOfWeek("1111111");
			preferenceDTO.setLookupCode(Text.EMPTY);

			scheduleSeatPreferenceDTO = seatPreferenceService.Update(authDTO, preferenceDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return scheduleSeatPreferenceDTO;
	}

	@Override
	public List<ScheduleSeatPreferenceDTO> getScheduleTripSeatPreference(AuthDTO authDTO, TripDTO tripDTO) {
		// Trip
		tripService.getTrip(authDTO, tripDTO);

		// Schedule
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());

		tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());

		List<ScheduleSeatPreferenceDTO> seatPreferenceDTOList = seatPreferenceService.getByScheduleId(authDTO, tripDTO.getSchedule());

		BusCache cache = new BusCache();
		for (Iterator<ScheduleSeatPreferenceDTO> iterartor = seatPreferenceDTOList.iterator(); iterartor.hasNext();) {
			ScheduleSeatPreferenceDTO seatPreferenceDTO = iterartor.next();
			if (tripDTO.getBus().getId() != seatPreferenceDTO.getBus().getId()) {
				iterartor.remove();
				continue;
			}

			if (seatPreferenceDTO.getBus() != null) {
				BusDTO busDTO = new BusDTO();
				busDTO.setId(seatPreferenceDTO.getBus().getId());
				busDTO = cache.getBusDTObyId(authDTO, busDTO);
				seatPreferenceDTO.getBus().setDisplayName(busDTO.getDisplayName());
				seatPreferenceDTO.getBus().setCategoryCode(busDTO.getCategoryCode());
				seatPreferenceDTO.getBus().setName(busDTO.getName());
				seatPreferenceDTO.getBus().setCode(busDTO.getCode());
				seatPreferenceDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(busDTO.getBusSeatLayoutDTO().getList(), seatPreferenceDTO.getBus().getBusSeatLayoutDTO().getList()));
			}

			for (GroupDTO groupDTO : seatPreferenceDTO.getGroupList()) {
				groupService.getGroup(authDTO, groupDTO);
			}

			// Override
			for (ScheduleSeatPreferenceDTO seatOverrideDTO : seatPreferenceDTO.getOverrideList()) {
				if (seatOverrideDTO.getBus() != null) {
					BusDTO overrideBusDTO = new BusDTO();
					overrideBusDTO.setId(seatOverrideDTO.getBus().getId());
					overrideBusDTO = cache.getBusDTObyId(authDTO, overrideBusDTO);
					seatOverrideDTO.getBus().setDisplayName(overrideBusDTO.getDisplayName());
					seatOverrideDTO.getBus().setCategoryCode(overrideBusDTO.getCategoryCode());
					seatOverrideDTO.getBus().setName(overrideBusDTO.getName());
					seatOverrideDTO.getBus().setCode(overrideBusDTO.getCode());
					seatOverrideDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(overrideBusDTO.getBusSeatLayoutDTO().getList(), seatOverrideDTO.getBus().getBusSeatLayoutDTO().getList()));
				}
				for (GroupDTO groupDTO : seatOverrideDTO.getGroupList()) {
					groupService.getGroup(authDTO, groupDTO);
				}
			}
		}
		return seatPreferenceDTOList;
	}

	private List<BusSeatLayoutDTO> filterByCode(List<BusSeatLayoutDTO> Orglist, List<BusSeatLayoutDTO> fillerlist) {
		List<BusSeatLayoutDTO> list = new ArrayList<>();
		for (BusSeatLayoutDTO fitterdto : fillerlist) {
			for (BusSeatLayoutDTO dto : Orglist) {
				if (dto.getCode().equals(fitterdto.getCode())) {
					list.add(dto);
					break;
				}
			}
		}
		return list;
	}

	@Override
	public void removeTripSeatPereference(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatPreferenceDTO preferenceDTO) {
		// Trip
		tripService.getTrip(authDTO, tripDTO);

		// Schedule
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
		preferenceDTO.setSchedule(tripDTO.getSchedule());
		
		// save as Exception
		List<ScheduleSeatPreferenceDTO> seatPreferenceList = seatPreferenceService.get(authDTO, preferenceDTO);
		if (seatPreferenceList != null) {
			for (ScheduleSeatPreferenceDTO seatPreferenceDTO : seatPreferenceList) {
				if (preferenceDTO.getCode().equals(seatPreferenceDTO.getCode())) {
					preferenceDTO.setCode(Text.EMPTY);
					preferenceDTO.setBus(seatPreferenceDTO.getBus());
					preferenceDTO.setActiveFrom(DateUtil.convertDate(tripDTO.getTripDate()));
					preferenceDTO.setActiveTo(DateUtil.convertDate(tripDTO.getTripDate()));
					preferenceDTO.setDayOfWeek(seatPreferenceDTO.getDayOfWeek());
					preferenceDTO.setGendar(null);
					preferenceDTO.setGroupList(seatPreferenceDTO.getGroupList());
					preferenceDTO.setLookupCode(seatPreferenceDTO.getCode());
					preferenceDTO.setActiveFlag(Numeric.ONE_INT);
					break;
				}
			}
		}

		seatPreferenceService.Update(authDTO, preferenceDTO);
	}

}
