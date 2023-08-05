package org.in.com.dao;

import hirondelle.date4j.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.utils.StringUtil;

public class TripSeatQuotaDAO extends BaseDAO {

	public void updateTripSeatQuota(AuthDTO authDTO, TripSeatQuotaDTO tripSeatQuotaDTO) {
		try {
			int pindex = Numeric.ZERO_INT;
			String seatNames = Text.EMPTY;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("INSERT INTO trip_seat_quota_details(namespace_id, trip_id, seat_code, seat_fare, ac_bus_tax, seat_name, seat_gendar, user_id, from_station_id, to_station_id, release_minutes, remarks, active_flag, updated_by, updated_at)VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW());");
			List<TicketDetailsDTO> ticketDetails = tripSeatQuotaDTO.getTrip().getTicketDetailsList();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
				selectPS.setInt(++pindex, authDTO.getNamespace().getId());
				selectPS.setInt(++pindex, tripSeatQuotaDTO.getTrip().getId());
				selectPS.setString(++pindex, ticketDetailsDTO.getSeatCode());
				selectPS.setBigDecimal(++pindex, ticketDetailsDTO.getSeatFare());
				selectPS.setBigDecimal(++pindex, ticketDetailsDTO.getAcBusTax());
				selectPS.setString(++pindex, ticketDetailsDTO.getSeatName());
				selectPS.setInt(++pindex, ticketDetailsDTO.getSeatGendar().getId());
				selectPS.setInt(++pindex, tripSeatQuotaDTO.getUser().getId());
				selectPS.setInt(++pindex, tripSeatQuotaDTO.getFromStation().getId());
				selectPS.setInt(++pindex, tripSeatQuotaDTO.getToStation().getId());
				selectPS.setInt(++pindex, tripSeatQuotaDTO.getRelaseMinutes());
				selectPS.setString(++pindex, tripSeatQuotaDTO.getRemarks());
				selectPS.setInt(++pindex, tripSeatQuotaDTO.getActiveFlag());
				selectPS.setInt(++pindex, authDTO.getUser().getId());
				selectPS.execute();
				selectPS.clearParameters();
				pindex = Numeric.ZERO_INT;
				seatNames = seatNames + ticketDetailsDTO.getSeatName() + Text.COMMA + Text.SINGLE_SPACE;
			}
			addAuditLog(connection, authDTO, tripSeatQuotaDTO.getTrip().getCode(), "trip", "Quota Seat Added ", StringUtil.substring(seatNames, seatNames.length() - 1));

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TripSeatQuotaDTO> getAllTripSeatQuota(AuthDTO authDTO, TripDTO tripDTO) {
		List<TripSeatQuotaDTO> tripSeatQuotaList = new ArrayList<TripSeatQuotaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (tripDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, trip_id, seat_code, seat_fare, ac_bus_tax, seat_name, seat_gendar, user_id, from_station_id, to_station_id, release_minutes, remarks, active_flag, updated_by, updated_at FROM trip_seat_quota_details WHERE namespace_id = ? AND trip_id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, tripDTO.getId());
			}
			else if (StringUtil.isNotNull(tripDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT tsqd.id, trip_id, seat_code, seat_fare, ac_bus_tax, seat_name, seat_gendar, user_id, from_station_id, to_station_id, release_minutes, tsqd.remarks, tsqd.active_flag, tsqd.updated_by, tsqd.updated_at FROM trip_seat_quota_details tsqd, trip WHERE tsqd.namespace_id = ? AND tsqd.trip_id = trip.id AND trip.code = ? AND tsqd.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, tripDTO.getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TripSeatQuotaDTO tripSeatQuotaDTO = new TripSeatQuotaDTO();
				tripSeatQuotaDTO.setId(selectRS.getInt("id"));
				TicketDetailsDTO ticketDetails = new TicketDetailsDTO();
				ticketDetails.setSeatCode(selectRS.getString("seat_code"));
				ticketDetails.setSeatFare(selectRS.getBigDecimal("seat_fare"));
				ticketDetails.setAcBusTax(selectRS.getBigDecimal("ac_bus_tax"));
				ticketDetails.setSeatName(selectRS.getString("seat_name"));
				ticketDetails.setSeatGendar(SeatGendarEM.getSeatGendarEM(selectRS.getInt("seat_gendar")));
				tripSeatQuotaDTO.setSeatDetails(ticketDetails);

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("user_id"));
				tripSeatQuotaDTO.setUser(user);

				StationDTO fromStation = new StationDTO();
				fromStation.setId(selectRS.getInt("from_station_id"));
				tripSeatQuotaDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(selectRS.getInt("to_station_id"));
				tripSeatQuotaDTO.setToStation(toStation);

				tripSeatQuotaDTO.setRelaseMinutes(selectRS.getInt("release_minutes"));
				tripSeatQuotaDTO.setRemarks(selectRS.getString("remarks"));
				tripSeatQuotaDTO.setActiveFlag(selectRS.getInt("active_flag"));
				tripSeatQuotaDTO.setTrip(tripDTO);
				tripSeatQuotaDTO.setUpdatedAt(new DateTime(selectRS.getString("updated_at")).format(Text.DATE_TIME_DATE4J));

				UserDTO updateUser = new UserDTO();
				updateUser.setId(selectRS.getInt("updated_by"));
				tripSeatQuotaDTO.setUpdatedBy(updateUser);

				tripSeatQuotaList.add(tripSeatQuotaDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripSeatQuotaList;
	}

	public void deleteTripSeatQuotaDetails(AuthDTO authDTO, List<TripSeatQuotaDTO> seatQuotaList) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE trip_seat_quota_details SET active_flag = ? WHERE id = ?");
			for (TripSeatQuotaDTO tripSeatQuotaDTO : seatQuotaList) {
				selectPS.setInt(1, tripSeatQuotaDTO.getActiveFlag());
				selectPS.setInt(2, tripSeatQuotaDTO.getId());
				selectPS.addBatch();
				addAuditLog(connection, authDTO, tripSeatQuotaDTO.getTrip().getCode(), "trip_seat_quota_details", "Quota Seat Released ", tripSeatQuotaDTO.getSeatDetails().getSeatName());
			}
			selectPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
