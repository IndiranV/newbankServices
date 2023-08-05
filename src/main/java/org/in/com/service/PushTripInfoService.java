package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;

public interface PushTripInfoService {

	public void updateTripInfo(AuthDTO authDTO, TripChartDTO tripChartDTO);
	
	public void removeTripInfo(AuthDTO authDTO, TripDTO tripDTO);
}
