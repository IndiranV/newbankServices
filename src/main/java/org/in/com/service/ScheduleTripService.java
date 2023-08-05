package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;

public interface ScheduleTripService {

	public List<TripDTO> getAllTripDetails(AuthDTO authDTO, SectorDTO sector, SearchDTO searchDTO);

	public List<TripDTO> getStageTripList(AuthDTO authDTO, SectorDTO sector, SearchDTO searchDTO);

	public List<TripDTO> getScheduleTripList(AuthDTO authDTO, ScheduleDTO schedule, List<DateTime> tripDateList);

	public TripDTO getTripDetails(AuthDTO authDTO, TripDTO tripDTO);

	public TripDTO getTripStageDetails(AuthDTO authDTO, TripDTO tripDTO);

	public List<TripDTO> getTripByTripDateAndVehicle(AuthDTO authDTO, DateTime tripDate, BusVehicleDTO vehicle);

	public void validateSeatFareWithFareRule(AuthDTO authDTO, ScheduleSeatFareDTO scheduleSeatFareDTO);

	public List<TripDTO> getTripByTripDateAndDriver(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver, DateTime tripDate);

}
