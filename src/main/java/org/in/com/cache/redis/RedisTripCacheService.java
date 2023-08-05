package org.in.com.cache.redis;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;

import net.sf.json.JSONArray;

public interface RedisTripCacheService {
	public List<TicketDetailsDTO> getBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO);

	public void putBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO, List<TicketDetailsDTO> list);

	public void clearBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO);

	public List<ExtraCommissionDTO> getAllExtraCommissionCache(AuthDTO authDTO);

	public void putgetAllExtraCommissionCache(AuthDTO authDTO, List<ExtraCommissionDTO> list);

	public void clearAllExtraCommissionCache(AuthDTO authDTO);

	public void putNotifyFareChangeRequest(AuthDTO authDTO, ScheduleDTO schedule);

	public JSONArray getNotifyFareChangeRequest();
	
	public void removeNotifyFareChangeRequest(AuthDTO authDTO, ScheduleDTO schedule);
	
	public Map<String, Map<String, String>> getTripDataCountCache(AuthDTO authDTO);
	
	public void putTripDataCountCache(AuthDTO authDTO, Map<String, Map<String, String>> dataMap);

}
