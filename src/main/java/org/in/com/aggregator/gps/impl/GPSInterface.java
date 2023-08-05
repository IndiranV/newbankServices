package org.in.com.aggregator.gps.impl;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;

public interface GPSInterface {
	public GPSLocationDTO getVehicleLocation(String operatorCode, String deviceCode, String vehicleNumber);

	public void updateGeoTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO);

	public void removeGeoTripDetails(AuthDTO authDTO, TripDTO tripDTO);
	
	public List<GPSLocationDTO> getAllDevice(String apiKey);
}
