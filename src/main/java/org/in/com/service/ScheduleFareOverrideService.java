package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;

public interface ScheduleFareOverrideService {
	public List<ScheduleFareAutoOverrideDTO> get(AuthDTO authDTO, ScheduleFareAutoOverrideDTO dto);

	public ScheduleFareAutoOverrideDTO Update(AuthDTO authDTO, ScheduleFareAutoOverrideDTO dto);

	public void UpdateV2(AuthDTO authDTO, List<ScheduleFareAutoOverrideDTO> scheduleFareAutoOverrideList);

	public List<ScheduleFareAutoOverrideDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO);

	public List<ScheduleFareAutoOverrideDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	public List<ScheduleFareAutoOverrideDTO> getTripScheduleActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO, Map<Integer, GroupDTO> groupMap, List<BusSeatTypeEM> busSeatTypes);

	public List<ScheduleFareAutoOverrideDTO> getTripScheduleDateRangeActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime activeFromDate, DateTime activeToDate);

	public List<ScheduleFareAutoOverrideDTO> processTripScheduleActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<ScheduleFareAutoOverrideDTO> autoOverrideList, StationDTO fromStationDTO, StationDTO toStationDTO, Map<Integer, GroupDTO> groupMap, BusSeatTypeEM busSeatType);

}
