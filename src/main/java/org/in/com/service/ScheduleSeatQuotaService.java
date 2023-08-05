package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.TripDTO;

public interface ScheduleSeatQuotaService {
	public List<ScheduleSeatVisibilityDTO> getTripSeatQuotaDetails(AuthDTO authDTO, TripDTO tripDTO);

	public ScheduleSeatVisibilityDTO addTripSeatQuotaDetails(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatVisibilityDTO seatVisibilityDTO);

}
