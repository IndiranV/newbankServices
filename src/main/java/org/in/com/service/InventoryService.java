package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;

public interface InventoryService {

	public List<TripDTO> getScheduleTripStageList(AuthDTO authDTO, DateTime tripDate, List<String> scheduleCodes);

}
