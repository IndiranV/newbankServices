package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;

public interface DynamicPricingService {

	public List<TripDTO> getDateWiseDPTripList(AuthDTO authDTO, List<DateTime> tripDateList, String filterType, String[] scheduleCode);

	public List<TripDTO> getBookedBlockedTickets(AuthDTO authDTO, List<String> tripCodeList, DateTime syncTime);

}
