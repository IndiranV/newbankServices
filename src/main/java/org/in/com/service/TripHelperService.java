package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.TripDTO;

public interface TripHelperService {

	public void getTripDetails(AuthDTO authDTO, TripDTO tripDTO);

	public List<TripDTO> ConvertScheduleToTrip(AuthDTO authDTO, SearchDTO searchDTO, List<ScheduleDTO> scheduleList);

}
