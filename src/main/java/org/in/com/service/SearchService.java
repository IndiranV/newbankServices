package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;

public interface SearchService {

	public List<TripDTO> getSearch(AuthDTO authDTO, SearchDTO searchDTO);

	public List<TripDTO> getAllTrips(AuthDTO authDTO, SearchDTO searchDTO);

	public List<TripDTO> getScheduleTripList(AuthDTO authDTO, ScheduleDTO schedule, List<DateTime> tripDateList);

	public void pushTripsDetails(AuthDTO authDTO, List<NamespaceDTO> namespaces, DateTime startDate, int days);

	public void disableInActiveRoutes(AuthDTO authDTO, List<NamespaceDTO> namespaces);

	public void pushInventoryChangesEvent(AuthDTO authDTO, TripDTO tripDTO);

	public void pushInventoryChangesEvent(AuthDTO authDTO, ScheduleDTO scheduleDTO);
	
	public void updateTripDataCount(AuthDTO authDTO, int days);

}
