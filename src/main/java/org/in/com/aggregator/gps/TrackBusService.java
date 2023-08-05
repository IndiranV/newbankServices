package org.in.com.aggregator.gps;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;

public interface TrackBusService {

	public GPSLocationDTO getVehicleLocation(String namespaceCode, GPSDeviceVendorEM gpsDeviceVendorEM, String deviceCode, String vehicleRegistrationeNumber);

	public void updateGeoTripDetails(AuthDTO authDTO, GPSDeviceVendorEM deviceVendor, TripChartDTO tripChartDTO);

	public void removeGeoTripDetails(AuthDTO authDTO, GPSDeviceVendorEM deviceVendor, TripDTO tripDTO);

}
