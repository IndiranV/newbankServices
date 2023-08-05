package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface BusBreakevenService {

	public BusBreakevenSettingsDTO getBreakeven(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO);

	public void updateBreakevenSettings(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO);

	public BusBreakevenSettingsDTO getBreakevenSettings(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO);

	public List<BusBreakevenSettingsDTO> getAllBreakevenSettings(AuthDTO authDTO);

	public JSONObject processTripBreakeven(AuthDTO authDTO, ScheduleBusDTO scheduleBusDTO, TripDTO tripDTO, StationDTO fuelStation);

	public void processBreakevenToTripBreakeven(AuthDTO authDTO, ScheduleBusDTO scheduleBus, TripDTO tripDTO, StationDTO fuelStation);

	public Map<String, String> getAllStageFuelPrice(AuthDTO authDTO, DateTime fuelDate);

	public JSONArray getBreakevenExpenses(AuthDTO authDTO);

}
