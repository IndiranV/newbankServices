package org.in.com.service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;

public interface FareRuleService extends BaseService<FareRuleDTO> {
	public FareRuleDTO getFareRule(AuthDTO authDTO, FareRuleDTO fareRuleDTO);

	public void updateFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule);

	public FareRuleDTO getFareRuleDetails(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation);

	public JSONArray getFareRuleDetailsBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public FareRuleDTO getFareRuleDetailsByFareRule(AuthDTO authDTO, FareRuleDTO fareRuleDTO, StationDTO fromStation, StationDTO toStationDTO);

	public FareRuleDetailsDTO getStageFareRuleDetails(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStation, StationDTO toStationDTO);

	public FareRuleDetailsDTO getFareRuleDetails(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStationDTO, StationDTO toStationDTO);

	public StageDTO getFareRuleByRoute(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStationDTO, StationDTO toStationDTO, BusDTO busDTO);

	public void syncVertexFareRule(AuthDTO authDTO, FareRuleDTO fareRule, DateTime lastSyncDate);
	
	public List<FareRuleDetailsDTO> getZoneSyncFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule, String syncDate);
	
	public JSONArray getLowFareStages(AuthDTO authDTO, ScheduleDTO scheduleDTO);
	
	public void applyFareRulesInStages(AuthDTO authDTO, ScheduleDTO scheduleDTO);
	
	public void applyChangeOfScheduleBusInStages(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, ScheduleDTO scheduleDTO);
	
	public void applyScheduleBusOverrideInQuickFare(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, ScheduleBusOverrideDTO scheduleBusOverride);
}
