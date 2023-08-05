package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ServiceException;

public class BusVehicleVanPickupDAO {

	public BusVehicleVanPickupDTO getBusVehicleVanPickup(AuthDTO authDTO, BusVehicleVanPickupDTO vanRoute) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (vanRoute.getId() > 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, station_id, trip_status_id, seat_count, active_flag FROM bus_vehicle_pickup_van WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, vanRoute.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, code, name, station_id, trip_status_id, seat_count, active_flag FROM bus_vehicle_pickup_van WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, vanRoute.getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				vanRoute.setId(selectRS.getInt("id"));
				vanRoute.setName(selectRS.getString("name"));
				vanRoute.setCode(selectRS.getString("code"));

				StationDTO station = new StationDTO();
				station.setId(selectRS.getInt("station_id"));
				vanRoute.setStation(station);

				vanRoute.setTripStatus(TripStatusEM.getTripStatusEM(selectRS.getInt("trip_status_id")));
				vanRoute.setSeatCount(selectRS.getInt("seat_count"));
				vanRoute.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return vanRoute;
	}

	public void updateBusVehicleVanPickup(AuthDTO authDTO, BusVehicleVanPickupDTO vanRoute) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_BUS_VEHICLE_PICKUP_VAN_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			termSt.setString(++pindex, vanRoute.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, vanRoute.getName());
			termSt.setString(++pindex, vanRoute.getStation().getCode());
			termSt.setInt(++pindex, vanRoute.getTripStatus().getId());
			termSt.setInt(++pindex, vanRoute.getSeatCount());
			termSt.setInt(++pindex, vanRoute.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				vanRoute.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public List<BusVehicleVanPickupDTO> getAllBusVehicleVanPickup(AuthDTO authDTO) {
		List<BusVehicleVanPickupDTO> list = new ArrayList<BusVehicleVanPickupDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, station_id, trip_status_id, seat_count, active_flag FROM bus_vehicle_pickup_van WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleVanPickupDTO vanRoute = new BusVehicleVanPickupDTO();
				vanRoute.setName(selectRS.getString("name"));
				vanRoute.setCode(selectRS.getString("code"));

				StationDTO station = new StationDTO();
				station.setId(selectRS.getInt("station_id"));
				vanRoute.setStation(station);

				vanRoute.setTripStatus(TripStatusEM.getTripStatusEM(selectRS.getInt("trip_status_id")));
				vanRoute.setSeatCount(selectRS.getInt("seat_count"));
				vanRoute.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(vanRoute);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<BusVehicleVanPickupDTO> getActiveVanPickupTrips(AuthDTO authDTO) {
		List<BusVehicleVanPickupDTO> list = new ArrayList<BusVehicleVanPickupDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, station_id, trip_status_id, seat_count, active_flag FROM bus_vehicle_pickup_van WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleVanPickupDTO vanRoute = new BusVehicleVanPickupDTO();
				vanRoute.setId(selectRS.getInt("id"));
				vanRoute.setName(selectRS.getString("name"));
				vanRoute.setCode(selectRS.getString("code"));

				StationDTO station = new StationDTO();
				station.setId(selectRS.getInt("station_id"));
				vanRoute.setStation(station);

				vanRoute.setTripStatus(TripStatusEM.getTripStatusEM(selectRS.getInt("trip_status_id")));
				vanRoute.setSeatCount(selectRS.getInt("seat_count"));
				vanRoute.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(vanRoute);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<BusVehicleVanPickupDTO> getByStationId(AuthDTO authDTO, StationDTO stationDTO) {
		List<BusVehicleVanPickupDTO> list = new ArrayList<BusVehicleVanPickupDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, active_flag FROM bus_vehicle_pickup_van WHERE namespace_id = ? AND station_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, stationDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusVehicleVanPickupDTO vanRoute = new BusVehicleVanPickupDTO();
				vanRoute.setName(selectRS.getString("name"));
				vanRoute.setCode(selectRS.getString("code"));
				vanRoute.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(vanRoute);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public boolean checkVanPickupUsed(AuthDTO authDTO, BusVehicleVanPickupDTO vehicleVanPickup) {
		boolean status = Text.FALSE;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM schedule_station_point ssp, bus_vehicle_pickup_van van, schedule sche WHERE sche.namespace_id = ssp.namespace_id AND van.namespace_id = sche.namespace_id AND sche.id = ssp.schedule_id AND van.id = ssp.bus_vehicle_pickup_van_id AND ssp.namespace_id = ? AND van.code = ? AND sche.active_flag = 1 AND ssp.active_flag = 1 AND van.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, vehicleVanPickup.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				status = Text.TRUE;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return status;
	}
}
