package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.TripDTO;

public interface ScheduleTripSeatPreferenceService {

	public ScheduleSeatPreferenceDTO updateTripSeatPereference(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatPreferenceDTO preferenceDTO);

	public List<ScheduleSeatPreferenceDTO> getScheduleTripSeatPreference(AuthDTO authDTO, TripDTO tripDTO);

	public void removeTripSeatPereference(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatPreferenceDTO preferenceDTO);

}
