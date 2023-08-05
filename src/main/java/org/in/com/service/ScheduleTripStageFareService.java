package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.StationDTO;

import net.sf.json.JSONArray;

public interface ScheduleTripStageFareService {

	public List<ScheduleFareAutoOverrideDTO> getTripStageActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO);

	public List<ScheduleFareAutoOverrideDTO> getTripStageActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public void updateQuickFare(AuthDTO authDTO, ScheduleTripStageFareDTO quickFareOverrideDTO);

	public List<ScheduleTripStageFareDTO> getScheduleTripStageFare(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleFareAutoOverrideDTO> processScheduleTripStageFare(AuthDTO authDTO, ScheduleDTO schedule, List<ScheduleFareAutoOverrideDTO> tripStageFareList, StationDTO fromStation, StationDTO toStation);

	public List<ScheduleTripStageFareDTO> getScheduleTripStageFareV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate, String tripCode);

	public List<ScheduleTripStageFareDTO> getScheduleTripStageFares(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate);

	public void updateQuickFareV2(AuthDTO authDTO, List<ScheduleTripStageFareDTO> quickFareOverrides);

	/** Schedule Trip Stage Fare History*/
	public JSONArray getScheduleTripStageFareHistory(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate, String tripCode);
}
