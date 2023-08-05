package org.in.com.service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.TicketStatusEM;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

public interface TripService {

	public List<TripDTO> saveTrip(AuthDTO authDTO, List<TripDTO> tripList);

	public List<TicketDetailsDTO> getBookedBlockedSeats(AuthDTO authDTO, TripDTO tripDTO);

	public void SaveBookedBlockedSeats(AuthDTO authDTO, TicketDTO ticketDTO);

	public void clearBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO);

	public void SaveBookedBlockedSeats(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void UpdateTripStatus(AuthDTO authDTO, TripDTO tripDTO);

	public TripDTO getTripDTO(AuthDTO authDTO, TripDTO tripDTO);

	public TripDTO getTripsByScheduleTripDate(AuthDTO authDTO, ScheduleDTO schedule);

	public TripDTO getTrip(AuthDTO authDTO, TripDTO tripDTO);

	public TripDTO getTripDTOwithScheduleDetails(AuthDTO authDTO, TripDTO tripDTO);

	public void updateTripSeatDetailsStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTripSeatDetailsWithExtras(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTripSeatDetailsWithExtrasV2(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTripSeatDetailsWithExtras(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTripSeatDetails(AuthDTO authDTO, TicketDTO ticketDTO);

	public TripChartDTO getTripChart(AuthDTO authDTO, TripDTO tripDTO);

	public TripChartDTO getTripsForTripchart(AuthDTO authDTO, TripChartDTO tripChartDTO);

	public TripChartDTO getTripsForTripchartV2(AuthDTO authDTO, TripChartDTO tripChartDTO);

	public void UpdateTripContact(AuthDTO authDTO, TripDTO tripDTO);

	public void updateTripJobStatus(AuthDTO authDTO, TripDTO tripDTO);

	public void tripNotification(AuthDTO authDTO, TripDTO tripDTO);

	public TripInfoDTO getAllTripContact(AuthDTO authDTO);

	public List<BusVehicleDriverDTO> getTripDrivers(AuthDTO authDTO);

	public List<BusVehicleAttendantDTO> getTripAttenders(AuthDTO authDTO);

	public TripInfoDTO getTripInfo(AuthDTO authDTO, TripDTO tripDTO);

	public void updateTripTransferSeatDetails(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public TripDTO getTripStageDetails(AuthDTO authDTO, TripDTO tripStageDTO);

	public List<TripDTO> getTripWiseBookedSeatCount(AuthDTO authDTO, UserDTO userDTO, ExtraCommissionDTO extraCommissionDTO);

	public List<TripDTO> getTripWiseBookedSeatCountV2(AuthDTO authDTO, UserDTO userDTO, DateTime fromDate, DateTime toDate, TicketStatusEM ticketStatus);

	public List<TripDTO> getTripInfoByTripDate(AuthDTO authDTO, DateTime tripDate);

	public List<StageStationDTO> getScheduleTripStage(AuthDTO authDTO, TripDTO tripDTO);

	public void checkTripChartWithLatest(AuthDTO authDTO, TripDTO tripDTO, DateTime versionDatetime);

	public void checkRecentTickets(AuthDTO authDTO, TripDTO tripDTO, String syncTime);

	public void SaveTripHistory(AuthDTO authDTO, TripDTO tripDTO, String actionName, String logData);

	public JSONArray getActiveTripBusVehicles(AuthDTO authDTO, DateTime tripDate);

	public JSONArray getPreferredVehicles(AuthDTO authDTO, TripDTO tripDTO);

	public List<TripDTO> getTripsByTripDate(AuthDTO authDTO, DateTime tripDate);

	public List<String> getRelatedStageCodes(AuthDTO authDTO, TripDTO tripDTO, StationDTO fromStation, StationDTO toStation);

	public void updateTripExtras(AuthDTO authDTO, Map<String, String> additionalAttribute);

	public void saveTripIncomeExpense(AuthDTO authDTO, TripDTO tripDTO, Map<String, String> transactionDetails);

	public List<Map<String, Object>> getBreakevenDetails(AuthDTO authDTO, DateTime fromDate, DateTime toDate, ScheduleDTO schedule, BusVehicleDTO busVehicle);

	public JSONArray getTripBreakevenExpenses(AuthDTO authDTO, TripDTO tripDTO);

	public Map<String, Map<String, String>> getTripDataCountCache(AuthDTO authDTO);

	public Map<String, Map<String, String>> getTripDataCountDetails(AuthDTO authDTO, String fromDate, String toDate);

	public void putTripDataCountCache(AuthDTO authDTO, Map<String, Map<String, String>> dataMap);

}
