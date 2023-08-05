package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripPickupVanChartDTO;
import org.in.com.dto.TripVanInfoDTO;

public interface BusVehicleVanPickupService {
	public void updateBusVehicleVanPickup(AuthDTO authDTO, BusVehicleVanPickupDTO vanRoute);

	public BusVehicleVanPickupDTO getBusVehicleVanPickup(AuthDTO authDTO, BusVehicleVanPickupDTO vanRoute);

	public List<BusVehicleVanPickupDTO> getAllBusVehicleVanPickup(AuthDTO authDTO);

	public List<BusVehicleVanPickupDTO> getByStationId(AuthDTO authDTO, StationDTO stationDTO);

	public List<TripVanInfoDTO> getActiveVanPickupTrips(AuthDTO authDTO, SearchDTO searchDTO);

	public TripPickupVanChartDTO getActiveVanPickupTripChart(AuthDTO authDTO, SearchDTO searchDTO, BusVehicleVanPickupDTO busVehicleVanPickup);
}
