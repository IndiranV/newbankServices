package org.in.com.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.VehicleTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;
import net.sf.json.JSONObject;

public class TripDAO extends BaseDAO {

	Logger logger = LoggerFactory.getLogger(TripDAO.class);

	public void saveTripStageDTO(AuthDTO authDTO, Connection connection, SearchDTO searchDTO, TripDTO tripDTO) {
		try {

			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TRIP_STAGE_IUD(?,?,?,?,? ,?,?,?,?,?)}");
			pindex = 0;
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, tripDTO.getId());
			callableStatement.setString(++pindex, tripDTO.getStage().getCode());
			callableStatement.setString(++pindex, searchDTO.getTravelDate().format("YYYY-MM-DD"));
			callableStatement.setInt(++pindex, searchDTO.getFromStation().getId());
			callableStatement.setInt(++pindex, searchDTO.getToStation().getId());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			tripDTO.getStage().setId(callableStatement.getInt("pitStageId"));

			callableStatement.clearParameters();
		}
		catch (SQLIntegrityConstraintViolationException e) {
			System.out.println("MICVE01 : " + authDTO.getNamespaceCode() + " - " + authDTO.getUser().getUsername());
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred while saving the trip stage");
		}
	}

	public void saveTripDTO(AuthDTO authDTO, List<TripDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TRIP_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?)}");
			for (TripDTO tripDTO : list) {
				if (tripDTO.getId() != 0) {
					// make assumption to identify cache status
					tripDTO.setActiveFlag(-1);
					continue;
				}
				pindex = 0;
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, tripDTO.getCode());
				callableStatement.setInt(++pindex, tripDTO.getSchedule().getId());
				callableStatement.setInt(++pindex, tripDTO.getBus().getId());
				callableStatement.setString(++pindex, tripDTO.getTripDate().format("YYYY-MM-DD"));
				callableStatement.setInt(++pindex, tripDTO.getTripOriginMinutes());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.registerOutParameter(++pindex, Types.VARCHAR);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				tripDTO.setId(callableStatement.getInt("pitTripId"));
				int tripCloseflag = callableStatement.getInt("pitTripStatusFlag");
				String remarks = callableStatement.getString("pcrRemarks");
				if (TripStatusEM.getTripStatusEM(tripCloseflag).getId() == TripStatusEM.TRIP_CLOSED.getId() || TripStatusEM.getTripStatusEM(tripCloseflag).getId() == TripStatusEM.TRIP_CANCELLED.getId()) {
					tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(tripCloseflag));
				}
				tripDTO.setRemarks(remarks);
				callableStatement.clearParameters();
			}
		}
		catch (SQLIntegrityConstraintViolationException e) {
			System.out.println("MICVE02 : " + authDTO.getNamespaceCode() + " - " + authDTO.getUser().getUsername());
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred while inserting the trip details" + e.getMessage());
		}
	}

	public TripDTO getTripStageDetails(AuthDTO authDTO, StageDTO stageDTO) {
		TripDTO tripDTO = new TripDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("SELECT tr.id,trd.id, tr.code,tr.schedule_id,travel_date, trip_date,trip_minutes,from_station_id,to_station_id,trip_status_flag FROM trip tr,trip_stage trd WHERE  tr.id =trd.trip_id AND tr.namespace_id = trd.namespace_id AND trd.namespace_id = ? AND trd.code = ?");
			psTrip.setInt(1, authDTO.getNamespace().getId());
			psTrip.setString(2, stageDTO.getCode());
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			if (resultSet.next()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(resultSet.getInt("schedule_id"));
				tripDTO.setSchedule(scheduleDTO);
				tripDTO.setId(resultSet.getInt("tr.id"));
				stageDTO.setId(resultSet.getInt("trd.id"));
				tripDTO.setCode(resultSet.getString("tr.code"));
				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				stageDTO.setTravelDate(new DateTime(resultSet.getString("travel_date")));
				tripDTO.setTripDate(new DateTime(resultSet.getString("trip_date")));
				tripDTO.setTripMinutes(resultSet.getInt("trip_minutes"));
				scheduleDTO.setTripDate(tripDTO.getTripDate());
				fromStationDTO.setId(resultSet.getInt("from_station_id"));
				toStationDTO.setId(resultSet.getInt("to_station_id"));
				StageStationDTO fromStageStationDTO = new StageStationDTO();
				fromStageStationDTO.setStation(fromStationDTO);
				StageStationDTO toStageStationDTO = new StageStationDTO();
				toStageStationDTO.setStation(toStationDTO);
				stageDTO.setFromStation(fromStageStationDTO);
				stageDTO.setToStation(toStageStationDTO);
				SearchDTO searchDTO = new SearchDTO();
				searchDTO.setFromStation(fromStationDTO);
				searchDTO.setToStation(toStationDTO);
				searchDTO.setTravelDate(stageDTO.getTravelDate());
				tripDTO.setSearch(searchDTO);
				tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(resultSet.getInt("trip_status_flag")));
			}
			tripDTO.setStage(stageDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred while getting the trip details: " + e.getMessage());
		}
		return tripDTO;
	}

	public void SaveBookedBlockedSeats(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			SaveBookedBlockedSeats(authDTO, ticketDTO, connection);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
		}
	}

	public void SaveBookedBlockedSeats(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection) {
		try {
			String stationPoint = BitsUtil.convertStationPoint(ticketDTO.getBoardingPoint(), ticketDTO.getDroppingPoint());

			int index = 0;
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("INSERT INTO trip_stage_seat_detail (namespace_id,trip_id,trip_stage_id,from_station_id,to_station_id,seat_gendar,seat_name,seat_code,seat_fare,contact_number,passenger_name,passenger_age,user_id,ticket_code,boarding_point_name,station_point,extras,ticket_status_id,ticket_at,active_flag,updated_by,updated_at) VALUES(?,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,?,?, ?,NOW(),1,?,NOW())");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				psTrip.setInt(++index, authDTO.getNamespace().getId());
				psTrip.setInt(++index, ticketDTO.getTripDTO().getId());
				psTrip.setInt(++index, ticketDTO.getTripDTO().getStage().getId());
				psTrip.setInt(++index, ticketDTO.getFromStation().getId());
				psTrip.setInt(++index, ticketDTO.getToStation().getId());
				psTrip.setInt(++index, ticketDetailsDTO.getSeatGendar().getId());
				psTrip.setString(++index, ticketDetailsDTO.getSeatName());
				psTrip.setString(++index, ticketDetailsDTO.getSeatCode());
				psTrip.setBigDecimal(++index, ticketDTO.getActualBookedSeatFare(ticketDetailsDTO));
				psTrip.setString(++index, ticketDTO.getPassengerMobile());
				psTrip.setString(++index, StringUtil.substring(ticketDetailsDTO.getPassengerName(), 40));
				psTrip.setInt(++index, ticketDetailsDTO.getPassengerAge());
				psTrip.setInt(++index, ticketDTO.getTicketUser().getId());
				psTrip.setString(++index, ticketDTO.getCode());
				psTrip.setString(++index, ticketDTO.getBoardingPoint().getName());
				psTrip.setString(++index, stationPoint);
				psTrip.setString(++index, convertTicketExtraToString(ticketDTO, ticketDetailsDTO));
				psTrip.setInt(++index, ticketDetailsDTO.getTicketStatus().getId());
				psTrip.setInt(++index, authDTO.getUser().getId());
				psTrip.addBatch();
			}
			psTrip.executeBatch();
		}
		catch (Exception e) {
			System.out.println("ticketDTO.getBoardingPoint() - " + ticketDTO.toJSON().toString());
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
		}

	}

	public List<TicketDetailsDTO> getBookedBlockedSeats(AuthDTO authDTO, TripDTO tripDTO) {
		List<TicketDetailsDTO> list = new ArrayList<TicketDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = null;
			if (tripDTO.getId() != 0) {
				psTrip = connection.prepareStatement("SELECT tst.code, from_station_id, to_station_id, seat_gendar, seat_name, seat_code, seat_fare, ticket_code, ticket_status_id, boarding_point_name, station_point, extras, ticket_at, passenger_name, passenger_age, contact_number, user_id, ssd.updated_at FROM trip_stage_seat_detail ssd, trip tst  WHERE tst.namespace_id = ssd.namespace_id AND ssd.trip_id = tst.id AND tst.active_flag  = ssd.active_flag AND tst.namespace_id = ? AND tst.id = ? AND tst.active_flag = 1");
				psTrip.setInt(1, authDTO.getNamespace().getId());
				psTrip.setInt(2, tripDTO.getId());
			}
			else if (StringUtil.isNotNull(tripDTO.getCode())) {
				psTrip = connection.prepareStatement("SELECT tst.code, from_station_id, to_station_id, seat_gendar, seat_name, seat_code, seat_fare, ticket_code, ticket_status_id, boarding_point_name, station_point, extras, ticket_at, passenger_name, passenger_age, contact_number, user_id, ssd.updated_at FROM trip_stage_seat_detail ssd, trip tst  WHERE tst.namespace_id = ssd.namespace_id AND ssd.trip_id = tst.id AND tst.active_flag  = ssd.active_flag AND tst.namespace_id = ? AND tst.code = ? AND tst.active_flag = 1");
				psTrip.setInt(1, authDTO.getNamespace().getId());
				psTrip.setString(2, tripDTO.getCode());
			}
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			while (resultSet.next()) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setTicketCode(resultSet.getString("ticket_code"));
				ticketDetailsDTO.setBoardingPointName(resultSet.getString("boarding_point_name"));
				ticketDetailsDTO.setStationPoint(resultSet.getString("station_point"));
				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				fromStationDTO.setId(resultSet.getInt("from_station_id"));
				ticketDetailsDTO.setFromStation(fromStationDTO);
				toStationDTO.setId(resultSet.getInt("to_station_id"));
				ticketDetailsDTO.setToStation(toStationDTO);
				ticketDetailsDTO.setSeatCode(resultSet.getString("seat_code"));
				ticketDetailsDTO.setSeatName(resultSet.getString("seat_name"));
				ticketDetailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(resultSet.getInt("seat_gendar")));
				ticketDetailsDTO.setSeatFare(resultSet.getBigDecimal("seat_fare"));
				ticketDetailsDTO.setPassengerName(resultSet.getString("passenger_name"));
				ticketDetailsDTO.setPassengerAge(resultSet.getInt("passenger_age"));
				ticketDetailsDTO.setContactNumber(resultSet.getString("contact_number"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				ticketDetailsDTO.setUser(userDTO);
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(resultSet.getInt("ticket_status_id")));
				ticketDetailsDTO.setTicketAt(new DateTime(resultSet.getString("ticket_at")));
				ticketDetailsDTO.setUpdatedAt(new DateTime(resultSet.getString("updated_at")));

				String extras = resultSet.getString("extras");
				ticketDetailsDTO.setNetAmount(getNetAmount(extras));

				TicketExtraDTO ticketExtra = convertStringToTicketExtra(extras, ticketDetailsDTO);
				ticketDetailsDTO.setTicketExtra(ticketExtra);

				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_CANCELLED.getId()) {
					continue;
				}
				list.add(ticketDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred while getting the booked blocked seats");
		}
		return list;
	}

	public List<TicketDetailsDTO> getTripStageSeatsDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketDetailsDTO> list = new ArrayList<TicketDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("SELECT  id,seat_gendar,seat_name,seat_code,ticket_code,seat_fare,ticket_status_id,boarding_point_name,station_point,passenger_name,passenger_age,contact_number,user_id, extras, active_flag FROM trip_stage_seat_detail  WHERE  namespace_id = ? AND  ticket_code = ? AND  active_flag = 1");
			psTrip.setInt(1, authDTO.getNamespace().getId());
			psTrip.setString(2, ticketDTO.getCode());
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			while (resultSet.next()) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setId(resultSet.getInt("id"));
				ticketDetailsDTO.setActiveFlag(resultSet.getInt("active_flag"));
				ticketDetailsDTO.setTicketCode(resultSet.getString("ticket_code"));
				ticketDetailsDTO.setBoardingPointName(resultSet.getString("boarding_point_name"));
				ticketDetailsDTO.setStationPoint(resultSet.getString("station_point"));
				ticketDetailsDTO.setSeatCode(resultSet.getString("seat_code"));
				ticketDetailsDTO.setSeatName(resultSet.getString("seat_name"));
				ticketDetailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(resultSet.getInt("seat_gendar")));
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(resultSet.getInt("ticket_status_id")));
				ticketDetailsDTO.setSeatFare(resultSet.getBigDecimal("seat_fare"));
				ticketDetailsDTO.setPassengerName(resultSet.getString("passenger_name"));
				ticketDetailsDTO.setPassengerAge(resultSet.getInt("passenger_age"));
				ticketDetailsDTO.setContactNumber(resultSet.getString("contact_number"));

				TicketExtraDTO ticketExtra = convertStringToTicketExtra(resultSet.getString("extras"), ticketDetailsDTO);
				ticketDetailsDTO.setTicketExtra(ticketExtra);

				list.add(ticketDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred while getting the booked blocked seats");
		}
		return list;
	}

	public void updateTripStatus(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("UPDATE trip set trip_status_flag = ?, remarks = ?, updated_by = ?, updated_at = NOW()  WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			psTrip.setInt(1, tripDTO.getTripStatus().getId());
			psTrip.setString(2, StringUtil.substring(tripDTO.getRemarks(), 120));
			psTrip.setInt(3, authDTO.getUser().getId());
			psTrip.setInt(4, authDTO.getNamespace().getId());
			psTrip.setString(5, tripDTO.getCode());
			psTrip.execute();

			// Add Audit Log
			addAuditLog(connection, authDTO, tripDTO.getCode(), "trip", "Trip Status", tripDTO.getTripStatus() != null ? "Trip: " + tripDTO.getTripStatus().getName() : "NA");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTripSeatDetailsFlag(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int index = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail set ticket_status_id = ?, contact_number = ?, active_flag = ?, updated_by = ?,updated_at= NOW() WHERE namespace_id = ? AND ticket_code = ? AND seat_code = ? AND active_flag = 1");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				ps.setInt(++index, ticketDetailsDTO.getTicketStatus().getId());
				ps.setString(++index, ticketDTO.getPassengerMobile());
				ps.setInt(++index, ticketDetailsDTO.getActiveFlag());
				ps.setInt(++index, authDTO.getUser().getId());
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setString(++index, ticketDTO.getCode());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateTripSeatDetailsWithExtras(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int index = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail set ticket_status_id = ?, extras = ?, active_flag = ?, updated_by = ?,updated_at= NOW() WHERE namespace_id = ? AND ticket_code = ? AND seat_code = ? AND active_flag = 1");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				ps.setInt(++index, ticketDetailsDTO.getTicketStatus().getId());
				ps.setString(++index, convertTicketExtraToStringUpdate(ticketDetailsDTO.getTicketExtra(), ticketDetailsDTO.getTravelStatus()));
				ps.setInt(++index, ticketDetailsDTO.getActiveFlag());
				ps.setInt(++index, authDTO.getUser().getId());
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setString(++index, ticketDTO.getCode());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateTripSeatDetailsWithExtrasV2(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int index = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail set ticket_status_id = ?,seat_fare = ?, extras = ?, active_flag = ?, updated_by = ?,updated_at= NOW() WHERE namespace_id = ? AND ticket_code = ? AND seat_code = ? AND active_flag = 1");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				ps.setInt(++index, ticketDetailsDTO.getTicketStatus().getId());
				ps.setBigDecimal(++index, ticketDetailsDTO.getSeatFare());
				ps.setString(++index, convertTicketExtraToStringUpdate(ticketDetailsDTO.getTicketExtra(), ticketDetailsDTO.getTravelStatus()));
				ps.setInt(++index, ticketDetailsDTO.getActiveFlag());
				ps.setInt(++index, authDTO.getUser().getId());
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setString(++index, ticketDTO.getCode());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateTripSeatDetailsV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int index = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail SET active_flag = ?, updated_by = ?,updated_at= NOW() WHERE namespace_id = ? AND trip_id = ? AND ticket_code = ? AND seat_code = ? AND active_flag <= 2");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				ps.setInt(++index, ticketDetailsDTO.getActiveFlag());
				ps.setInt(++index, authDTO.getUser().getId());
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setInt(++index, ticketDTO.getTripDTO().getId());
				ps.setString(++index, ticketDTO.getCode());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTripSeatDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int index = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail SET seat_gendar = ?, seat_name = ?, seat_code = ?, passenger_name = ?, passenger_age = ?, contact_number = ?, boarding_point_name = ?, station_point = ?, extras = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND id = ?");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				ps.setInt(++index, ticketDetailsDTO.getSeatGendar().getId());
				ps.setString(++index, ticketDetailsDTO.getSeatName());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.setString(++index, StringUtil.substring(ticketDetailsDTO.getPassengerName(), 40));
				ps.setInt(++index, ticketDetailsDTO.getPassengerAge());
				ps.setString(++index, ticketDetailsDTO.getContactNumber());
				ps.setString(++index, ticketDetailsDTO.getBoardingPointName());
				ps.setString(++index, ticketDetailsDTO.getStationPoint());
				ps.setString(++index, convertTicketExtraToStringUpdate(ticketDetailsDTO.getTicketExtra(), ticketDetailsDTO.getTicketExtra().getTravelStatus()));
				ps.setInt(++index, authDTO.getUser().getId());
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setInt(++index, ticketDetailsDTO.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			System.out.println(ticketDTO.toJSON());
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateTripTransferSeatDetails(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection) {
		try {
			int index = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail SET trip_id = ?, trip_stage_id= ?, from_station_id = ?, to_station_id = ?, seat_name = ?, seat_code = ?, boarding_point_name = ?, station_point = ?, extras = ?, updated_by = ?, updated_at= NOW() WHERE namespace_id = ? AND id = ?");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				index = 0;
				ps.setInt(++index, ticketDTO.getTripDTO().getId());
				ps.setInt(++index, ticketDTO.getTripDTO().getStage().getId());
				ps.setInt(++index, ticketDTO.getFromStation().getId());
				ps.setInt(++index, ticketDTO.getToStation().getId());
				ps.setString(++index, ticketDetailsDTO.getSeatName());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.setString(++index, ticketDetailsDTO.getBoardingPointName());
				ps.setString(++index, ticketDetailsDTO.getStationPoint());
				ps.setString(++index, convertTicketExtraToStringUpdate(ticketDetailsDTO.getTicketExtra(), ticketDetailsDTO.getTravelStatus()));
				ps.setInt(++index, authDTO.getUser().getId());
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setInt(++index, ticketDetailsDTO.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public TripChartDTO getTripsForTripchart(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		try {
			List<TripDTO> tripList = new ArrayList<TripDTO>();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT t.id, t.code, t.schedule_id,t.trip_status_flag FROM trip t WHERE t.namespace_id = ? AND t.trip_date = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tripChartDTO.getTripDate().toString());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO dto = new TripDTO();
				dto.setId(rs.getInt("t.id"));
				dto.setTripStatus(TripStatusEM.getTripStatusEM(rs.getInt("t.trip_status_flag")));
				dto.setCode(rs.getString("t.code"));
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(rs.getInt("t.schedule_id"));
				dto.setSchedule(scheduleDTO);
				dto.setTripDate(tripChartDTO.getTripDate());
				tripList.add(dto);
			}
			tripChartDTO.setTripList(tripList);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tripChartDTO;
	}

	public TripDTO getTripDTO(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,schedule_id,bus_id,trip_date,trip_minutes,trip_status_flag, remarks FROM trip tr WHERE tr.namespace_id = ? AND tr.code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tripDTO.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tripDTO.setId(rs.getInt("id"));
				tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(rs.getInt("trip_status_flag")));
				tripDTO.setCode(rs.getString("code"));
				BusDTO busDTO = new BusDTO();
				busDTO.setId(rs.getInt("bus_id"));
				tripDTO.setBus(busDTO);
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(rs.getInt("schedule_id"));
				tripDTO.setSchedule(scheduleDTO);
				tripDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				tripDTO.setTripMinutes(rs.getInt("trip_minutes"));
				tripDTO.setRemarks(rs.getString("remarks"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tripDTO;
	}

	public TripDTO getTripsByScheduleTripDate(AuthDTO authDTO, ScheduleDTO schedule) {
		TripDTO tripDTO = new TripDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,schedule_id,bus_id,trip_date,trip_minutes,trip_status_flag, remarks FROM trip WHERE namespace_id = ? AND schedule_id = ? AND trip_date = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, schedule.getId());
			ps.setString(3, schedule.getTripDate().format("YYYY-MM-DD"));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tripDTO.setId(rs.getInt("id"));
				tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(rs.getInt("trip_status_flag")));
				tripDTO.setCode(rs.getString("code"));
				BusDTO busDTO = new BusDTO();
				busDTO.setId(rs.getInt("bus_id"));
				tripDTO.setBus(busDTO);
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(rs.getInt("schedule_id"));
				tripDTO.setSchedule(scheduleDTO);
				tripDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				tripDTO.setTripMinutes(rs.getInt("trip_minutes"));
				tripDTO.setRemarks(rs.getString("remarks"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tripDTO;
	}

	public void UpdateTripInfo(AuthDTO authDTO, TripDTO dto) {
		try {
			if (dto.getTripInfo() != null) {
				@Cleanup
				Connection connection = ConnectDAO.getConnection();
				int pindex = 0;
				@Cleanup
				CallableStatement callableStatement = connection.prepareCall("{CALL  EZEE_SP_TRIP_INFO_IUD(?,?,?,?,? ,?,?,?,?,?, ?,?,?,?,? ,?,?,?,?,?, ?)}");
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, dto.getCode());
				callableStatement.setString(++pindex, dto.getTripInfo().getTripStartDateTime() != null ? dto.getTripInfo().getTripStartDateTime().format(Text.DATE_TIME_DATE4J) : null);
				callableStatement.setString(++pindex, dto.getTripInfo().getTripCloseDateTime() != null ? dto.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);
				callableStatement.setString(++pindex, dto.getTripInfo().getDriverName());
				callableStatement.setString(++pindex, dto.getTripInfo().getDriverMobile());
				callableStatement.setString(++pindex, dto.getTripInfo().getSecondaryDriverDetails());
				callableStatement.setInt(++pindex, dto.getTripInfo().getPrimaryDriverId());
				callableStatement.setInt(++pindex, dto.getTripInfo().getSecondaryDriverId());
				callableStatement.setInt(++pindex, dto.getTripInfo().getAttendantId());
				callableStatement.setInt(++pindex, dto.getTripInfo().getCaptainId());
				callableStatement.setString(++pindex, dto.getTripInfo().getRemarks());
				callableStatement.setString(++pindex, dto.getTripInfo().getScheduleTagIds());
				callableStatement.setString(++pindex, dto.getTripInfo().getExtras());
				callableStatement.setString(++pindex, dto.getTripInfo().getBusVehicle() != null ? dto.getTripInfo().getBusVehicle().getCode() : null);
				callableStatement.setDouble(++pindex, dto.getTripInfo().getTripBreakeven().getFuelPrice());
				callableStatement.setInt(++pindex, dto.getTripInfo().getTripBreakeven().getId());
				callableStatement.setFloat(++pindex, dto.getTripInfo().getDistance());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.executeUpdate();

				// Activity Log
				StringBuilder activityLog = new StringBuilder();
				if (dto.getTripInfo().getBusVehicle() != null && StringUtil.isNotNull(dto.getTripInfo().getBusVehicle().getRegistationNumber()) && StringUtil.isNotNull(dto.getTripInfo().getDriverName()) && StringUtil.isNotNull(dto.getTripInfo().getDriverMobile())) {
					activityLog.append(dto.getTripInfo().getBusVehicle() != null ? "Vehicle: " + dto.getTripInfo().getBusVehicle().getRegistationNumber() + ", " : Text.EMPTY);
					activityLog.append("Driver: ");
					activityLog.append(dto.getTripInfo().getDriverName());
					activityLog.append(", Driver contact: ");
					activityLog.append(dto.getTripInfo().getDriverMobile());
				}
				else {
					activityLog.append("Unassigned");
				}

				// Add Audit Log
				addAuditLog(connection, authDTO, dto.getCode(), "trip", "Vehicle and Driver Update", activityLog.toString());
			}
		}
		catch (Exception e) {
			System.out.println(dto.getCode() + " " + dto.getTripInfo().getDriverName() + " " + dto.getTripInfo().getDriverMobile() + " " + dto.getTripInfo().getBusVehicle() != null ? dto.getTripInfo().getBusVehicle().getCode() : null);
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public TripInfoDTO getAllTripContact(AuthDTO authDTO) {
		TripInfoDTO dto = new TripInfoDTO();
		List<TripInfoDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT driver_name, driver_mobile FROM trip_info  WHERE namespace_id = ? AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripInfoDTO infoDTO = new TripInfoDTO();
				infoDTO.setDriverName(rs.getString("driver_name"));
				infoDTO.setDriverMobile(rs.getString("driver_mobile"));
				list.add(infoDTO);
			}
			dto.setList(list);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public TripInfoDTO getTripInfoDTO(AuthDTO authDTO, TripDTO tripDTO) {
		TripInfoDTO infoDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = null;

			if (tripDTO.getId() != 0) {
				ps = connection.prepareStatement("SELECT driver_name,driver_mobile,driver_details,info.trip_start_time,info.trip_close_time,bus_vehicle_id, info.primary_driver_id, info.secondary_driver_id, info.attendant_id, info.captain_id, notification_status, info.remarks, extras FROM trip_info info WHERE namespace_id = ? AND info.trip_id = ? and active_flag = 1");
				ps.setInt(1, authDTO.getNamespace().getId());
				ps.setInt(2, tripDTO.getId());
			}
			else if (StringUtil.isNotNull(tripDTO.getCode())) {
				ps = connection.prepareStatement("SELECT driver_name,driver_mobile,driver_details,info.trip_start_time,info.trip_close_time,bus_vehicle_id, info.primary_driver_id, info.secondary_driver_id, info.attendant_id, info.captain_id, notification_status, info.remarks, extras FROM trip_info info,trip tr WHERE tr.namespace_id = ? AND tr.id  = info.trip_id AND tr.active_flag = info.active_flag AND tr.active_flag = 1 AND  tr.code = ?");
				ps.setInt(1, authDTO.getNamespace().getId());
				ps.setString(2, tripDTO.getCode());
			}
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				infoDTO = new TripInfoDTO();
				infoDTO.setDriverName(rs.getString("driver_name"));
				infoDTO.setDriverMobile(rs.getString("driver_mobile"));
				convertDriverDetails(infoDTO, rs.getString("driver_details"));

				BusVehicleDriverDTO primaryDriver = new BusVehicleDriverDTO();
				primaryDriver.setId(rs.getInt("info.primary_driver_id"));
				infoDTO.setPrimaryDriver(primaryDriver);

				BusVehicleDriverDTO secondaryDriver = new BusVehicleDriverDTO();
				secondaryDriver.setId(rs.getInt("info.secondary_driver_id"));
				infoDTO.setSecondaryDriver(secondaryDriver);

				BusVehicleAttendantDTO attendant = new BusVehicleAttendantDTO();
				attendant.setId(rs.getInt("info.attendant_id"));
				infoDTO.setAttendant(attendant);

				BusVehicleAttendantDTO captain = new BusVehicleAttendantDTO();
				captain.setId(rs.getInt("info.captain_id"));
				infoDTO.setCaptain(captain);

				String tripStartTime = rs.getString("info.trip_start_time");
				infoDTO.setTripStartDateTime(StringUtil.isNotNull(tripStartTime) ? new DateTime(tripStartTime) : null);
				String tripCloseTime = rs.getString("info.trip_close_time");
				infoDTO.setTripCloseDateTime(StringUtil.isNotNull(tripCloseTime) ? new DateTime(tripCloseTime) : null);
				infoDTO.setRemarks(rs.getString("info.remarks"));
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				String notificationType = rs.getString("notification_status");
				if (StringUtil.isNotNull(notificationType)) {
					List<NotificationTypeEM> notificationTypeList = new ArrayList<NotificationTypeEM>();
					for (String type : notificationType.split(",")) {
						if (NotificationTypeEM.getNotificationTypeEM(type) != null) {
							notificationTypeList.add(NotificationTypeEM.getNotificationTypeEM(type));
						}
					}
					infoDTO.setNotificationStatus(notificationTypeList);
				}
				busVehicleDTO.setId(rs.getInt("bus_vehicle_id"));
				infoDTO.setBusVehicle(busVehicleDTO);

				infoDTO.setExtras(rs.getString("extras"));

				tripDTO.setTripInfo(infoDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return infoDTO;
	}

	public void convertDriverDetails(TripInfoDTO infoDTO, String driverDetail) {
		if (StringUtil.isValidJSON(driverDetail)) {
			JSONObject driverAttenderJSON = JSONObject.fromObject(driverDetail);
			infoDTO.setDriverName2(driverAttenderJSON.getString("dn"));
			infoDTO.setDriverMobile2(driverAttenderJSON.getString("dm"));
			infoDTO.setAttenderName(driverAttenderJSON.getString("an"));
			infoDTO.setAttenderMobile(driverAttenderJSON.getString("am"));
			infoDTO.setCaptainName(driverAttenderJSON.has("cn") ? driverAttenderJSON.getString("cn") : Text.NA);
			infoDTO.setCaptainMobile(driverAttenderJSON.has("cm") ? driverAttenderJSON.getString("cm") : Text.NA);
		}
	}

	public List<TripDTO> getTripInfoDTOByTripDate(AuthDTO authDTO, DateTime tripDate) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT tr.code, driver_name, driver_mobile, driver_details, info.trip_close_time, info.primary_driver_id, info.secondary_driver_id, info.attendant_id, info.captain_id, bus_vehicle_id, notification_status, info.remarks, info.extras FROM trip_info info,trip tr WHERE tr.namespace_id = ? AND tr.id  = info.trip_id AND tr.active_flag = info.active_flag AND tr.active_flag = 1 AND  tr.trip_date = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tripDate.format("YYYY-MM-DD"));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO tripDTO = new TripDTO();
				TripInfoDTO infoDTO = new TripInfoDTO();
				tripDTO.setCode(rs.getString("tr.code"));
				infoDTO.setDriverName(rs.getString("driver_name"));
				infoDTO.setDriverMobile(rs.getString("driver_mobile"));
				convertDriverDetails(infoDTO, rs.getString("driver_details"));
				infoDTO.setRemarks(rs.getString("info.remarks"));

				BusVehicleDriverDTO primaryDriverDTO = new BusVehicleDriverDTO();
				primaryDriverDTO.setId(rs.getInt("info.primary_driver_id"));
				infoDTO.setPrimaryDriver(primaryDriverDTO);

				BusVehicleDriverDTO secondaryDriverDTO = new BusVehicleDriverDTO();
				secondaryDriverDTO.setId(rs.getInt("info.secondary_driver_id"));
				infoDTO.setSecondaryDriver(secondaryDriverDTO);

				BusVehicleAttendantDTO attendantDTO = new BusVehicleAttendantDTO();
				attendantDTO.setId(rs.getInt("info.attendant_id"));
				infoDTO.setAttendant(attendantDTO);

				BusVehicleAttendantDTO captainDTO = new BusVehicleAttendantDTO();
				captainDTO.setId(rs.getInt("info.captain_id"));
				infoDTO.setCaptain(captainDTO);

				String tripCloseTime = rs.getString("info.trip_close_time");
				infoDTO.setTripCloseDateTime(StringUtil.isNotNull(tripCloseTime) ? new DateTime(tripCloseTime) : null);
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				String notificationType = rs.getString("notification_status");
				if (StringUtil.isNotNull(notificationType)) {
					List<NotificationTypeEM> notificationTypeList = new ArrayList<NotificationTypeEM>();
					for (String type : notificationType.split(",")) {
						if (NotificationTypeEM.getNotificationTypeEM(type) != null) {
							notificationTypeList.add(NotificationTypeEM.getNotificationTypeEM(type));
						}
					}
					infoDTO.setNotificationStatus(notificationTypeList);
				}
				busVehicleDTO.setId(rs.getInt("bus_vehicle_id"));
				infoDTO.setBusVehicle(busVehicleDTO);
				infoDTO.setExtras(rs.getString("info.extras"));
				tripDTO.setTripInfo(infoDTO);
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<BusVehicleDTO> getTripInfoBySchedule(AuthDTO authDTO, TripDTO trip) {
		List<BusVehicleDTO> busVehicleList = new ArrayList<BusVehicleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT bv.id, bv.code, bv.name, bv.bus_id, bv.vehicle_type, bv.registation_date, bv.registation_number, bv.lic_number, bv.gps_device_code, bv.gps_device_vendor_id, bv.mobile_number FROM trip_info info, trip tr, bus_vehicle bv WHERE tr.namespace_id = ? AND bv.namespace_id = ? AND bv.id = info.bus_vehicle_id AND tr.schedule_id = ? AND tr.id  = info.trip_id AND tr.trip_date >= ? AND tr.trip_date <= ? AND tr.active_flag = info.active_flag AND bv.active_flag = 1 AND tr.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, trip.getSchedule().getId());
			ps.setString(4, DateUtil.minusDaysToDate(trip.getTripDate(), 10).format("YYYY-MM-DD"));
			ps.setString(5, trip.getTripDate().format("YYYY-MM-DD"));
			@Cleanup
			ResultSet selectRS = ps.executeQuery();
			while (selectRS.next()) {
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				busVehicleDTO.setId(selectRS.getInt("bv.id"));
				busVehicleDTO.setCode(selectRS.getString("bv.code"));
				busVehicleDTO.setName(selectRS.getString("bv.name"));

				BusDTO busDTO = new BusDTO();
				busDTO.setId(selectRS.getInt("bv.bus_id"));
				busVehicleDTO.setBus(busDTO);

				busVehicleDTO.setVehicleType(VehicleTypeEM.getVehicleTypeEM(selectRS.getInt("bv.vehicle_type")));
				busVehicleDTO.setRegistrationDate(selectRS.getString("bv.registation_date"));
				busVehicleDTO.setRegistationNumber(selectRS.getString("bv.registation_number"));
				busVehicleDTO.setLicNumber(selectRS.getString("bv.lic_number"));
				busVehicleDTO.setGpsDeviceCode(selectRS.getString("bv.gps_device_code"));
				busVehicleDTO.setMobileNumber(selectRS.getString("bv.mobile_number"));
				busVehicleDTO.setDeviceVendor(GPSDeviceVendorEM.getGPSDeviceVendorEM(selectRS.getInt("gps_device_vendor_id")));

				busVehicleList.add(busVehicleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return busVehicleList;
	}

	public List<TripDTO> getTripByTripDateAndVehicle(AuthDTO authDTO, DateTime tripDate, BusVehicleDTO vehicle) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT tr.id, tr.code, trip_minutes, trip_status_flag, driver_name, driver_mobile, info.trip_close_time, bus_vehicle_id, info.primary_driver_id, info.secondary_driver_id, info.attendant_id, info.captain_id, notification_status, info.extras, info.remarks FROM trip_info info,trip tr,bus_vehicle busv WHERE busv.namespace_id = ? AND tr.namespace_id = ? AND tr.id  = info.trip_id AND tr.active_flag = info.active_flag AND tr.active_flag = 1 AND  tr.trip_date = ? AND bus_vehicle_id = busv.id AND busv.code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setString(3, tripDate.format("YYYY-MM-DD"));
			ps.setString(4, vehicle.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO tripDTO = new TripDTO();
				TripInfoDTO infoDTO = new TripInfoDTO();
				tripDTO.setId(rs.getInt("tr.id"));
				tripDTO.setCode(rs.getString("tr.code"));
				tripDTO.setTripMinutes(rs.getInt("trip_minutes"));
				tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(rs.getInt("trip_status_flag")));
				tripDTO.setTripDate(tripDate);
				infoDTO.setDriverName(rs.getString("driver_name"));
				infoDTO.setDriverMobile(rs.getString("driver_mobile"));
				infoDTO.setRemarks(rs.getString("info.remarks"));

				String tripCloseTime = rs.getString("info.trip_close_time");
				infoDTO.setTripCloseDateTime(StringUtil.isNotNull(tripCloseTime) ? new DateTime(tripCloseTime) : null);
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				String notificationType = rs.getString("notification_status");
				if (StringUtil.isNotNull(notificationType)) {
					List<NotificationTypeEM> notificationTypeList = new ArrayList<NotificationTypeEM>();
					for (String type : notificationType.split(",")) {
						if (NotificationTypeEM.getNotificationTypeEM(type) != null) {
							notificationTypeList.add(NotificationTypeEM.getNotificationTypeEM(type));
						}
					}
					infoDTO.setNotificationStatus(notificationTypeList);
				}
				busVehicleDTO.setId(rs.getInt("bus_vehicle_id"));
				infoDTO.setBusVehicle(busVehicleDTO);

				BusVehicleDriverDTO primaryDriver = new BusVehicleDriverDTO();
				primaryDriver.setId(rs.getInt("info.primary_driver_id"));
				infoDTO.setPrimaryDriver(primaryDriver);

				BusVehicleDriverDTO secondaryDriver = new BusVehicleDriverDTO();
				secondaryDriver.setId(rs.getInt("info.secondary_driver_id"));
				infoDTO.setSecondaryDriver(secondaryDriver);

				BusVehicleAttendantDTO attendant = new BusVehicleAttendantDTO();
				attendant.setId(rs.getInt("info.attendant_id"));
				infoDTO.setAttendant(attendant);

				BusVehicleAttendantDTO captain = new BusVehicleAttendantDTO();
				captain.setId(rs.getInt("info.captain_id"));
				infoDTO.setCaptain(captain);

				infoDTO.setExtras(rs.getString("info.extras"));

				tripDTO.setTripInfo(infoDTO);
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<TripDTO> getTripByTripDateAndDriver(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver, DateTime tripDate) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT tr.id, tr.code, trip_minutes, trip_status_flag, driver_name, driver_mobile, info.trip_start_time, info.trip_close_time, bus_vehicle_id, info.primary_driver_id, info.secondary_driver_id, info.attendant_id, info.captain_id, notification_status, info.extras, info.remarks FROM trip_info info, trip tr WHERE tr.namespace_id = ? AND tr.namespace_id = info.namespace_id AND tr.id = info.trip_id AND (info.primary_driver_id = ? OR info.secondary_driver_id = ?) AND tr.trip_date = ? AND tr.active_flag = info.active_flag AND tr.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, busVehicleDriver.getId());
			ps.setInt(3, busVehicleDriver.getId());
			ps.setString(4, tripDate.format("YYYY-MM-DD"));

			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO tripDTO = new TripDTO();
				TripInfoDTO infoDTO = new TripInfoDTO();
				tripDTO.setId(rs.getInt("tr.id"));
				tripDTO.setCode(rs.getString("tr.code"));
				tripDTO.setTripMinutes(rs.getInt("trip_minutes"));
				tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(rs.getInt("trip_status_flag")));
				tripDTO.setTripDate(tripDate);
				infoDTO.setDriverName(rs.getString("driver_name"));
				infoDTO.setDriverMobile(rs.getString("driver_mobile"));
				infoDTO.setRemarks(rs.getString("info.remarks"));

				String tripStartTime = rs.getString("info.trip_start_time");
				infoDTO.setTripStartDateTime(StringUtil.isNotNull(tripStartTime) ? new DateTime(tripStartTime) : null);
				String tripCloseTime = rs.getString("info.trip_close_time");
				infoDTO.setTripCloseDateTime(StringUtil.isNotNull(tripCloseTime) ? new DateTime(tripCloseTime) : null);
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				String notificationType = rs.getString("notification_status");
				if (StringUtil.isNotNull(notificationType)) {
					List<NotificationTypeEM> notificationTypeList = new ArrayList<NotificationTypeEM>();
					for (String type : notificationType.split(",")) {
						if (NotificationTypeEM.getNotificationTypeEM(type) != null) {
							notificationTypeList.add(NotificationTypeEM.getNotificationTypeEM(type));
						}
					}
					infoDTO.setNotificationStatus(notificationTypeList);
				}
				busVehicleDTO.setId(rs.getInt("bus_vehicle_id"));
				infoDTO.setBusVehicle(busVehicleDTO);

				BusVehicleDriverDTO primaryDriver = new BusVehicleDriverDTO();
				primaryDriver.setId(rs.getInt("info.primary_driver_id"));
				infoDTO.setPrimaryDriver(primaryDriver);

				BusVehicleDriverDTO secondaryDriver = new BusVehicleDriverDTO();
				secondaryDriver.setId(rs.getInt("info.secondary_driver_id"));
				infoDTO.setSecondaryDriver(secondaryDriver);

				BusVehicleAttendantDTO attendant = new BusVehicleAttendantDTO();
				attendant.setId(rs.getInt("info.attendant_id"));
				infoDTO.setAttendant(attendant);

				BusVehicleAttendantDTO captain = new BusVehicleAttendantDTO();
				captain.setId(rs.getInt("info.captain_id"));
				infoDTO.setCaptain(captain);

				infoDTO.setExtras(rs.getString("info.extras"));

				tripDTO.setTripInfo(infoDTO);
				tripDTO.setActiveFlag(Numeric.ONE_INT);
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NamespaceDTO getNamespace(TripDTO tripDTO) {
		NamespaceDTO namespaceDTO = new NamespaceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT namespace_id FROM trip  WHERE  code = ?");
			ps.setString(1, tripDTO.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				namespaceDTO.setId(rs.getInt("namespace_id"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return namespaceDTO;
	}

	public void updateTripJobStatus(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = null;
			if (tripDTO.getId() != 0) {
				ps = connection.prepareStatement("UPDATE trip_info SET notification_status = ? WHERE  namespace_id = ?  AND trip_id = ?");
				ps.setString(1, tripDTO.getTripInfo().getNotificationStatusCodes());
				ps.setInt(2, authDTO.getNamespace().getId());
				ps.setInt(3, tripDTO.getId());
			}
			else {
				ps = connection.prepareStatement("INSERT INTO trip_info(namespace_id,trip_id,notification_status,active_flag,updated_by,updated_at) SELECT namespace_id,id,?,active_flag,updated_by,NOW() FROM trip WHERE namespace_id = ? AND code = ?");
				ps.setString(1, tripDTO.getTripInfo().getNotificationStatusCodes());
				ps.setInt(2, authDTO.getNamespace().getId());
				ps.setString(3, tripDTO.getCode());
			}
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TripDTO> getTripWiseBookedSeatCount(AuthDTO authDTO, UserDTO userDTO, ExtraCommissionDTO extraCommissionDTO) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("SELECT trip_date,COUNT(*) as seat_count,SUM(seat_fare) as seat_amount FROM trip,trip_stage_seat_detail WHERE trip.id = trip_stage_seat_detail.trip_id AND trip.trip_date >= ? AND trip.trip_date <= ? AND trip_stage_seat_detail.namespace_id = ? AND trip.namespace_id = ? AND  user_id = ? AND trip_stage_seat_detail.ticket_status_id = 1 AND trip.trip_status_flag != 4 AND trip.active_flag = 1 AND trip_stage_seat_detail.active_flag = 1 GROUP BY trip_date");
			psTrip.setString(1, extraCommissionDTO.getActiveFrom());
			psTrip.setString(2, extraCommissionDTO.getActiveTo());
			psTrip.setInt(3, authDTO.getNamespace().getId());
			psTrip.setInt(4, authDTO.getNamespace().getId());
			psTrip.setInt(5, userDTO.getId());
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			while (resultSet.next()) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setTripDate(new DateTime(resultSet.getString("trip_date")));
				tripDTO.setBookedSeatCount(resultSet.getInt("seat_count"));
				tripDTO.setId(resultSet.getInt("seat_amount"));
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;

	}

	public List<TripDTO> getTripWiseBookedSeatCountV2(AuthDTO authDTO, UserDTO userDTO, DateTime fromDate, DateTime toDate, TicketStatusEM ticketStatus) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("SELECT trip.code, trip_date, COUNT(*) as seat_count,SUM(seat_fare) as seat_amount FROM trip,trip_stage_seat_detail WHERE trip.id = trip_stage_seat_detail.trip_id AND trip.trip_date >= ? AND trip.trip_date <= ? AND trip_stage_seat_detail.namespace_id = ? AND trip.namespace_id = ? AND  user_id = ? AND trip_stage_seat_detail.ticket_status_id = ? AND trip.trip_status_flag != 4 AND trip.active_flag = 1 AND trip_stage_seat_detail.active_flag = 1 GROUP BY trip.id");
			psTrip.setString(1, fromDate.format("YYYY-MM-DD"));
			psTrip.setString(2, toDate.format("YYYY-MM-DD"));
			psTrip.setInt(3, authDTO.getNamespace().getId());
			psTrip.setInt(4, authDTO.getNamespace().getId());
			psTrip.setInt(5, userDTO.getId());
			psTrip.setInt(6, ticketStatus.getId());
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			while (resultSet.next()) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(resultSet.getString("trip.code"));
				tripDTO.setTripDate(new DateTime(resultSet.getString("trip_date")));
				tripDTO.setBookedSeatCount(resultSet.getInt("seat_count"));
				tripDTO.setId(resultSet.getInt("seat_amount"));
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;

	}

	public TripInfoDTO getAllocatedVehicle(AuthDTO authDTO, DateTime tripDate) {
		TripInfoDTO infoDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT driver_name, driver_mobile, bus_vehicle_id, trip_info.trip_close_time, notification_status, trip_info.remarks FROM trip, trip_info WHERE trip_info.namespace_id = trip.namespace_id AND trip.namespace_id = ? AND trip_info.trip_id = trip.id AND trip_info.active_flag = 1 AND trip.trip_date = ? AND trip.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tripDate.format("YYYY-MM-DD"));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				infoDTO = new TripInfoDTO();
				infoDTO.setDriverName(rs.getString("driver_name"));
				infoDTO.setDriverMobile(rs.getString("driver_mobile"));
				infoDTO.setRemarks(rs.getString("remarks"));

				String tripCloseTime = rs.getString("info.trip_close_time");
				infoDTO.setTripCloseDateTime(StringUtil.isNotNull(tripCloseTime) ? new DateTime(tripCloseTime) : null);
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				String notificationType = rs.getString("notification_status");
				if (StringUtil.isNotNull(notificationType)) {
					List<NotificationTypeEM> notificationTypeList = new ArrayList<NotificationTypeEM>();
					for (String type : notificationType.split(",")) {
						if (NotificationTypeEM.getNotificationTypeEM(type) != null) {
							notificationTypeList.add(NotificationTypeEM.getNotificationTypeEM(type));
						}
					}
					infoDTO.setNotificationStatus(notificationTypeList);
				}
				busVehicleDTO.setId(rs.getInt("bus_vehicle_id"));
				infoDTO.setBusVehicle(busVehicleDTO);
				// tripDTO.setTripInfoDTO(infoDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return infoDTO;
	}

	public List<TripDTO> getTripsByTripDate(AuthDTO authDTO, DateTime tripDate) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT code, trip_date, trip_minutes, trip_status_flag FROM trip WHERE namespace_id = ? AND trip_date = ? AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, tripDate.format("YYYY-MM-DD"));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("code"));
				tripDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				tripDTO.setTripMinutes(rs.getInt("trip_minutes"));
				tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(rs.getInt("trip_status_flag")));
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<TripDTO> getTripBreakevenDetails(AuthDTO authDTO, DateTime fromDate, DateTime toDate, ScheduleDTO schedule, BusVehicleDTO vehicleDTO) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			String scheduleIdVehicleId = "";
			if (schedule != null && schedule.getId() != 0) {
				scheduleIdVehicleId = " AND trp.schedule_id = " + schedule.getId();
			}
			if (vehicleDTO != null && vehicleDTO.getId() != 0) {
				scheduleIdVehicleId = scheduleIdVehicleId + " AND bus_vehicle_id = " + vehicleDTO.getId();
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT trp.code, trp.trip_date, trp.schedule_id, trp.bus_id, info.bus_vehicle_id, info.fuel_price, info.distance, even.breakeven_details FROM trip trp, trip_info info, trip_breakeven even WHERE trp.namespace_id = ? AND trp.namespace_id = info.namespace_id AND even.namespace_id = info.namespace_id AND trp.id = info.trip_id" + scheduleIdVehicleId + " AND info.trip_breakeven_id = even.id AND trp.trip_date BETWEEN ? AND ? AND trp.active_flag = 1 AND info.active_flag = 1 AND even.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, DateUtil.convertDate(fromDate));
			ps.setString(3, DateUtil.convertDate(toDate));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("trp.code"));
				tripDTO.setTripDate(new DateTime(rs.getString("trp.trip_date")));

				BusDTO bus = new BusDTO();
				bus.setId(rs.getInt("trp.bus_id"));
				tripDTO.setBus(bus);

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(rs.getInt("trp.schedule_id"));
				tripDTO.setSchedule(scheduleDTO);

				BusVehicleDTO busvehicleDTO = new BusVehicleDTO();
				busvehicleDTO.setId(rs.getInt("info.bus_vehicle_id"));

				TripInfoDTO tripInfo = new TripInfoDTO();
				tripInfo.setBusVehicle(busvehicleDTO);
				tripDTO.setTripInfo(tripInfo);

				JSONObject breakevenJson = new JSONObject();
				breakevenJson.put("fuel", rs.getString("info.fuel_price"));
				breakevenJson.put("distance", rs.getString("info.distance"));
				breakevenJson.put("details", rs.getString("even.breakeven_details"));
				tripDTO.setBreakeven(breakevenJson);
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<TripDTO> getOldBreakevenDetails(AuthDTO authDTO, DateTime fromDate, DateTime toDate, ScheduleDTO schedule, BusVehicleDTO vehicleDTO) {
		List<TripDTO> list = new ArrayList<TripDTO>();
		try {
			String scheduleIdVehicleId = "";
			if (schedule != null && schedule.getId() != 0) {
				scheduleIdVehicleId = " AND trp.schedule_id = " + schedule.getId();
			}
			if (vehicleDTO != null && vehicleDTO.getId() != 0) {
				scheduleIdVehicleId = scheduleIdVehicleId + " AND bus_vehicle_id = " + vehicleDTO.getId();
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT trp.code, trp.trip_date, trp.schedule_id, trp.bus_id, info.bus_vehicle_id, info.distance, even.name, even.breakeven_details FROM trip trp, trip_info info, bus_breakeven_settings even, schedule_bus bus WHERE trp.namespace_id = ? AND trp.namespace_id = info.namespace_id AND info.namespace_id = even.namespace_id AND even.namespace_id = bus.namespace_id AND trp.id = info.trip_id" + scheduleIdVehicleId + " AND bus.schedule_id = trp.schedule_id AND bus.breakeven_settings_id = even.id AND info.trip_breakeven_id = 0 AND trp.trip_date BETWEEN ? AND ? AND trp.active_flag = 1 AND info.active_flag = 1 AND even.active_flag = 1 AND bus.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, DateUtil.convertDate(fromDate));
			ps.setString(3, DateUtil.convertDate(toDate));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("trp.code"));
				tripDTO.setTripDate(new DateTime(rs.getString("trp.trip_date")));

				BusDTO bus = new BusDTO();
				bus.setId(rs.getInt("trp.bus_id"));
				tripDTO.setBus(bus);

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(rs.getInt("trp.schedule_id"));
				tripDTO.setSchedule(scheduleDTO);

				BusVehicleDTO busvehicleDTO = new BusVehicleDTO();
				busvehicleDTO.setId(rs.getInt("info.bus_vehicle_id"));

				TripInfoDTO tripInfo = new TripInfoDTO();
				tripInfo.setBusVehicle(busvehicleDTO);
				tripDTO.setTripInfo(tripInfo);

				JSONObject breakevenJson = new JSONObject();
				breakevenJson.put("fuel", "70");
				breakevenJson.put("distance", rs.getString("info.distance"));
				breakevenJson.put("details", rs.getString("even.breakeven_details"));
				tripDTO.setBreakeven(breakevenJson);
				list.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void getTripBreakevenDetails(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT info.bus_vehicle_id, info.fuel_price, info.distance, even.breakeven_details FROM trip_info info, trip_breakeven even WHERE info.namespace_id = ? AND even.namespace_id = info.namespace_id AND info.trip_id = ? AND info.trip_breakeven_id = even.id AND info.active_flag = 1 AND even.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, tripDTO.getId());

			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				BusVehicleDTO busvehicleDTO = new BusVehicleDTO();
				busvehicleDTO.setId(rs.getInt("info.bus_vehicle_id"));

				TripInfoDTO tripInfo = new TripInfoDTO();
				tripInfo.setBusVehicle(busvehicleDTO);
				tripDTO.setTripInfo(tripInfo);

				JSONObject breakevenJson = new JSONObject();
				breakevenJson.put("fuel", rs.getString("info.fuel_price"));
				breakevenJson.put("distance", rs.getString("info.distance"));
				breakevenJson.put("details", rs.getString("even.breakeven_details"));
				tripDTO.setBreakeven(breakevenJson);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private TicketExtraDTO convertStringToTicketExtra(String extra, TicketDetailsDTO ticketDetailsDTO) {
		TicketExtraDTO ticketExtraDTO = new TicketExtraDTO();
		if (StringUtil.isNotNull(extra)) {
			String[] ticketExtras = extra.split(Text.COMMA);
			ticketExtraDTO.setBlockReleaseMinutes(StringUtil.getIntegerValue(ticketExtras[Numeric.ZERO_INT]));
			ticketExtraDTO.setTicketAt(DateUtil.getDateTime(ticketExtras[Numeric.ONE_INT]));
			ticketExtraDTO.setNetAmount(StringUtil.getBigDecimalValue(ticketExtras[Numeric.TWO_INT]));

			if (ticketExtras.length > Numeric.THREE_INT && StringUtil.isNotNull(ticketExtras[Numeric.THREE_INT]) && ticketExtras[Numeric.THREE_INT].length() >= Numeric.SIX_INT) {
				String ticketEditDetails = ticketExtras[Numeric.THREE_INT];
				ticketExtraDTO.setEditBoardingPoint(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.ZERO_INT))));
				ticketExtraDTO.setEditDroppingPoint(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.ONE_INT))));
				ticketExtraDTO.setEditPassengerDetails(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.TWO_INT))));
				ticketExtraDTO.setEditChangeSeat(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.THREE_INT))));
				ticketExtraDTO.setEditMobileNumber(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.FOUR_INT))));
				ticketExtraDTO.setTicketTransfer(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.FIVE_INT))));
			}
			String offlineTicketCode = Text.NA;
			if (ticketExtras.length > Numeric.FOUR_INT && StringUtil.isNotNull(ticketExtras[Numeric.FOUR_INT]) && !ticketExtras[Numeric.FOUR_INT].equals(Text.HYPHEN)) {
				offlineTicketCode = ticketExtras[Numeric.FOUR_INT];
			}
			ticketExtraDTO.setOfflineTicketCode(offlineTicketCode);
			ticketExtraDTO.setAcBusTax(ticketExtras.length > Numeric.FIVE_INT ? StringUtil.getBigDecimalValue(ticketExtras[Numeric.FIVE_INT]) : BigDecimal.ZERO);
			ticketExtraDTO.setTicketTransferMinutes(ticketExtras.length > Numeric.SIX_INT ? Integer.valueOf(ticketExtras[Numeric.SIX_INT]) : 0);
			ticketExtraDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(ticketExtras.length > Numeric.SEVEN_INT ? Integer.valueOf(ticketExtras[Numeric.SEVEN_INT]) : 1));
			ticketDetailsDTO.setAcBusTax(ticketExtraDTO.getAcBusTax());
		}
		return ticketExtraDTO;
	}

	private BigDecimal getNetAmount(String extra) {
		BigDecimal actualBookedAmount = BigDecimal.ZERO;
		if (StringUtil.isNotNull(extra) && extra.split(Text.COMMA).length >= 3) {
			String[] ticketExtras = extra.split(Text.COMMA);
			actualBookedAmount = StringUtil.getBigDecimalValue(ticketExtras[Numeric.TWO_INT]);
		}
		return actualBookedAmount;
	}

	public void updateTripExtras(AuthDTO authDTO, TripDTO tripDTO, String extras) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_info SET extras = ? WHERE namespace_id = ? AND trip_id = ?");
			ps.setString(1, extras);
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, tripDTO.getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String convertTicketExtraToString(TicketDTO ticket, TicketDetailsDTO ticketDetailsDTO) {
		StringBuilder ticketExtras = new StringBuilder();
		String ticketEditDetails = "000000";
		if (ticket.getTicketExtra() != null) {
			ticketExtras.append(ticket.getTicketExtra().getBlockReleaseMinutes()).append(Text.COMMA);
			ticketEditDetails = ticket.getTicketExtra().getTicketEditDetails();
		}
		else if (ticket.getTicketExtra() == null) {
			ticketExtras.append(Numeric.ZERO_INT).append(Text.COMMA);
		}
		ticketExtras.append(DateUtil.convertDateTime(ticket.getTicketAt())).append(Text.COMMA).append(ticketDetailsDTO.getNetAmount());
		ticketExtras.append(Text.COMMA).append(ticketEditDetails);

		String offlineTicketCode = Text.HYPHEN;
		if (ticketDetailsDTO.getTicketExtra() != null && StringUtil.isNotNull(ticketDetailsDTO.getTicketExtra().getOfflineTicketCode())) {
			offlineTicketCode = ticketDetailsDTO.getTicketExtra().getOfflineTicketCode();
		}
		ticketExtras.append(Text.COMMA).append(offlineTicketCode).append(Text.COMMA).append(ticketDetailsDTO.getAcBusTax().setScale(2, BigDecimal.ROUND_CEILING));
		ticketExtras.append(Text.COMMA).append(ticket.getScheduleTicketTransferTerms() != null ? ticket.getScheduleTicketTransferTerms().getMinutes() : 0);
		ticketExtras.append(Text.COMMA).append(ticketDetailsDTO.getTravelStatus() != null ? ticketDetailsDTO.getTravelStatus().getId() : 1);
		return ticketExtras.toString();
	}

	private String convertTicketExtraToStringUpdate(TicketExtraDTO ticketExtra, TravelStatusEM travelStatus) {
		StringBuilder ticketExtras = new StringBuilder();
		ticketExtras.append(ticketExtra.getBlockReleaseMinutes()).append(Text.COMMA).append(DateUtil.convertDateTime(ticketExtra.getTicketAt())).append(Text.COMMA);
		ticketExtras.append(ticketExtra.getNetAmount()).append(Text.COMMA).append(ticketExtra.getTicketEditDetails());

		String offlineTicketCode = Text.HYPHEN;
		if (StringUtil.isNotNull(ticketExtra.getOfflineTicketCode())) {
			offlineTicketCode = ticketExtra.getOfflineTicketCode();
		}
		ticketExtras.append(Text.COMMA).append(offlineTicketCode).append(Text.COMMA).append(ticketExtra.getAcBusTax().setScale(2, BigDecimal.ROUND_CEILING));
		ticketExtras.append(Text.COMMA).append(ticketExtra.getTicketTransferMinutes()).append(Text.COMMA).append(travelStatus != null ? travelStatus.getId() : 1);
		return ticketExtras.toString();
	}

	/** To be removed */
	public void updateTripSeatDetailsWithExtras(AuthDTO authDTO, TicketDetailsDTO ticketDetailsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail set extras = ? WHERE namespace_id = ? AND ticket_code = ? AND seat_code = ? AND active_flag = 1");
			int index = 0;
			ps.setString(++index, convertTicketExtraToStringUpdate(ticketDetailsDTO.getTicketExtra(), ticketDetailsDTO.getTravelStatus()));
			ps.setInt(++index, authDTO.getNamespace().getId());
			ps.setString(++index, ticketDetailsDTO.getTicketCode());
			ps.setString(++index, ticketDetailsDTO.getSeatCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateTripSeatDetailsWithExtras(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE trip_stage_seat_detail SET extras = ? WHERE namespace_id = ? AND ticket_code = ? AND seat_code = ? AND active_flag = 1");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				int index = 0;
				ps.setString(++index, convertTicketExtraToStringUpdate(ticketDetailsDTO.getTicketExtra(), ticketDetailsDTO.getTicketExtra().getTravelStatus()));
				ps.setInt(++index, authDTO.getNamespace().getId());
				ps.setString(++index, ticketDTO.getCode());
				ps.setString(++index, ticketDetailsDTO.getSeatCode());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<TicketDetailsDTO> getBookedBlockedSeatsV2(AuthDTO authDTO, String tripDate) {
		List<TicketDetailsDTO> list = new ArrayList<TicketDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("SELECT tst.code, ssd.from_station_id, ssd.to_station_id, tck.schedule_id, seat_gendar, seat_name, seat_code, seat_fare, ticket_code, ssd.ticket_status_id, boarding_point_name, station_point, ssd.extras, ssd.ticket_at, ssd.passenger_name, ssd.passenger_age, contact_number, ssd.user_id, ssd.updated_at FROM trip_stage_seat_detail ssd, ticket tck, trip tst  WHERE tst.namespace_id = ssd.namespace_id AND tst.namespace_id = tck.namespace_id AND tck.code = ssd.ticket_code AND ssd.trip_id = tst.id AND tst.active_flag = ssd.active_flag AND tst.namespace_id = ? AND tst.trip_date = ? AND tst.active_flag = 1");
			psTrip.setInt(1, authDTO.getNamespace().getId());
			psTrip.setString(2, tripDate);
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			while (resultSet.next()) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setTicketCode(resultSet.getString("ticket_code"));
				ticketDetailsDTO.setBoardingPointName(resultSet.getString("boarding_point_name"));
				ticketDetailsDTO.setStationPoint(resultSet.getString("station_point"));
				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				fromStationDTO.setId(resultSet.getInt("ssd.from_station_id"));
				ticketDetailsDTO.setFromStation(fromStationDTO);
				toStationDTO.setId(resultSet.getInt("ssd.to_station_id"));
				ticketDetailsDTO.setToStation(toStationDTO);
				ticketDetailsDTO.setSeatCode(resultSet.getString("seat_code"));
				ticketDetailsDTO.setSeatName(resultSet.getString("seat_name"));
				ticketDetailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(resultSet.getInt("seat_gendar")));
				ticketDetailsDTO.setSeatFare(resultSet.getBigDecimal("seat_fare"));
				ticketDetailsDTO.setPassengerName(resultSet.getString("passenger_name"));
				ticketDetailsDTO.setPassengerAge(resultSet.getInt("passenger_age"));
				ticketDetailsDTO.setContactNumber(resultSet.getString("contact_number"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				ticketDetailsDTO.setUser(userDTO);
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(resultSet.getInt("ticket_status_id")));
				ticketDetailsDTO.setTicketAt(new DateTime(resultSet.getString("ticket_at")));
				ticketDetailsDTO.setUpdatedAt(new DateTime(resultSet.getString("updated_at")));
				ticketDetailsDTO.setScheduleId(resultSet.getInt("schedule_id"));

				String extras = resultSet.getString("extras");
				ticketDetailsDTO.setNetAmount(getNetAmount(extras));

				TicketExtraDTO ticketExtra = convertStringToTicketExtra(extras, ticketDetailsDTO);
				ticketDetailsDTO.setTicketExtra(ticketExtra);

				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_CANCELLED.getId()) {
					continue;
				}
				list.add(ticketDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
