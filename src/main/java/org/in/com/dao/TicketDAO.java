package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketRefundDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

public class TicketDAO extends BaseDAO {

	public TicketDTO showTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = null;
			if (StringUtil.isNull(ticketDTO.getPassengerEmailId())) {
				ps = connection.prepareStatement("SELECT id,code,booking_code,user_id,trip_date,travel_minutes,boarding_point_id,boarding_point_minutes,dropping_point_id,dropping_point_minutes,from_station_id,to_station_id,trip_code,trip_stage_code,mobile_number,email_id,service_number,reporting_minutes,journey_type,device_medium,ticket_status_id,bus_id,tax_id,lookup_id, payment_gateway_partner_code,remarks,ticket_at,additional_attribute,active_flag,updated_by,updated_at FROM ticket WHERE  namespace_id = ? AND code = ? AND active_flag = 1");
				ps.setInt(1, authDTO.getNamespace().getId());
				ps.setString(2, ticketDTO.getCode());
			}
			else {
				ps = connection.prepareStatement("SELECT id,code,booking_code,user_id,trip_date,travel_minutes,boarding_point_id,boarding_point_minutes,dropping_point_id,dropping_point_minutes,from_station_id,to_station_id,trip_code,trip_stage_code,mobile_number,email_id,service_number,reporting_minutes,journey_type,device_medium,ticket_status_id,bus_id,tax_id,lookup_id,payment_gateway_partner_code,remarks,ticket_at,additional_attribute,active_flag,updated_by,updated_at FROM ticket WHERE  namespace_id = ? AND  code = ? AND email_id = ? AND active_flag = 1");
				ps.setInt(1, authDTO.getNamespace().getId());
				ps.setString(2, ticketDTO.getCode());
				ps.setString(3, ticketDTO.getPassengerEmailId());
			}

			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketDTO.setId(rs.getInt("id"));
				ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				ticketDTO.setBookingCode(rs.getString("booking_code"));
				ticketDTO.setCode(rs.getString("code"));
				ticketDTO.setTravelMinutes(rs.getInt("travel_minutes"));
				TripDTO tripDTO = new TripDTO();
				BusDTO busDTO = new BusDTO();
				StageDTO stageDTO = new StageDTO();
				stageDTO.setCode(rs.getString("trip_stage_code"));
				tripDTO.setStage(stageDTO);
				tripDTO.setCode(rs.getString("trip_code"));
				ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
				busDTO.setId(rs.getInt("bus_id"));
				tripDTO.setBus(busDTO);

				NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
				taxDTO.setId(rs.getInt("tax_id"));
				ticketDTO.setTax(taxDTO);

				ticketDTO.setLookupId(rs.getInt("lookup_id"));
				ticketDTO.setTripDTO(tripDTO);
				ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
				ticketDTO.setPassengerEmailId(rs.getString("email_id"));
				ticketDTO.setServiceNo(rs.getString("service_number"));
				UserDTO ticketUserDTO = new UserDTO();
				ticketUserDTO.setId(rs.getInt("user_id"));
				ticketDTO.setTicketUser(ticketUserDTO);
				ticketDTO.setReportingMinutes(rs.getInt("reporting_minutes"));
				ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(rs.getString("journey_type")));
				ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				// ticketDTO.setPaymentGatewayPartnerCode(rs.getString("payment_gateway_partner_code"));
				ticketDTO.setRemarks(rs.getString("remarks"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));
				ticketDTO.setActiveFlag(rs.getInt("active_flag"));

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(rs.getInt("from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				ticketDTO.setFromStation(fromStationDTO);
				toStationDTO.setId(rs.getInt("to_station_id"));
				ticketDTO.setToStation(toStationDTO);

				// get boarding and dropping points
				StationPointDAO spDAO = new StationPointDAO();
				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setId(rs.getInt("boarding_point_id"));
				spDAO.getStationPointbyId(connection, stationPointDTO);
				stationPointDTO.setMinitues(rs.getInt("boarding_point_minutes"));

				ticketDTO.setBoardingPoint(stationPointDTO);
				StationPointDTO ToStationPointDTO = new StationPointDTO();
				ToStationPointDTO.setId(rs.getInt("dropping_point_id"));
				spDAO.getStationPointbyId(connection, ToStationPointDTO);
				ToStationPointDTO.setMinitues(rs.getInt("dropping_point_minutes"));
				ticketDTO.setDroppingPoint(ToStationPointDTO);

				convertTicketFromAdditionalAtribute(ticketDTO, rs.getString("additional_attribute"));

				// get Ticket Details and ticket status details
				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				@Cleanup
				PreparedStatement cancelDetailsPS = connection.prepareStatement("select cancellation_charges,refund_amount,active_flag from  ticket_cancel_detail where ticket_id = ? AND ticket_detail_id = ? AND active_flag = 1");
				@Cleanup
				PreparedStatement pdps = connection.prepareStatement("SELECT id,ticket_id,seat_name,seat_code,seat_type,ticket_status_id,travel_status_id,passenger_name,passenger_age,seat_gender,seat_fare,ac_bus_tax,active_flag,updated_by,updated_at FROM ticket_detail WHERE ticket_id = ? and active_flag = 1");
				pdps.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet pdrs = pdps.executeQuery();
				while (pdrs.next()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setId(pdrs.getInt("id"));
					tdDTO.setSeatName(pdrs.getString("seat_name"));
					tdDTO.setSeatCode(pdrs.getString("seat_code"));
					tdDTO.setSeatType(pdrs.getString("seat_type"));
					tdDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(pdrs.getInt("ticket_status_id")));
					tdDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(pdrs.getInt("travel_status_id")));
					tdDTO.setPassengerName(pdrs.getString("passenger_name"));
					tdDTO.setPassengerAge(pdrs.getInt("passenger_age"));
					tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(pdrs.getInt("seat_gender")));
					tdDTO.setSeatFare(pdrs.getBigDecimal("seat_fare"));
					tdDTO.setAcBusTax(pdrs.getBigDecimal("ac_bus_tax"));
					tdDTO.setActiveFlag(pdrs.getInt("active_flag"));
					// Mapping Cancellation details
					if (tdDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
						cancelDetailsPS.setInt(1, ticketDTO.getId());
						cancelDetailsPS.setInt(2, tdDTO.getId());
						@Cleanup
						ResultSet cancelDetailsRS = cancelDetailsPS.executeQuery();
						if (cancelDetailsRS.next()) {
							tdDTO.setCancellationCharges(cancelDetailsRS.getBigDecimal("cancellation_charges"));
							tdDTO.setRefundAmount(cancelDetailsRS.getBigDecimal("refund_amount"));
						}
					}
					ticketDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(ticketDetails);
				// Ticket Addons
				// get Ticket Addons Details
				List<TicketAddonsDetailsDTO> ticketAddonsDetailsList = new ArrayList<TicketAddonsDetailsDTO>();
				@Cleanup
				PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT tad.id,td.seat_code,tad.ticket_addons_type_id,tad.ticket_status_id,tad.refference_id,tad.reference_code,tad.value,tad.active_flag FROM ticket_addons_detail tad,  ticket_detail td WHERE tad.ticket_id = td.ticket_id and tad.ticket_detail_id = td.id AND tad.active_flag = td.active_flag AND tad.ticket_id = ? and tad.active_flag = 1");
				ticketAddonsPS.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
				while (ticketAddonsRS.next()) {
					TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
					addonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
					addonsDetailsDTO.setSeatCode(ticketAddonsRS.getString("seat_code"));
					addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
					addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
					addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
					addonsDetailsDTO.setRefferenceCode(ticketAddonsRS.getString("reference_code"));
					addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
					addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
					ticketAddonsDetailsList.add(addonsDetailsDTO);
				}
				ticketDTO.setTicketAddonsDetails(ticketAddonsDetailsList);

				// Mapping ticket transaction details
				@Cleanup
				PreparedStatement cancelTransactionsPS = connection.prepareStatement("SELECT charge_amount,cancel_commission,refund_amount,refund_status, remarks FROM ticket_cancel_transaction WHERE ticket_id = ? AND ticket_transaction_id = ? AND active_flag = 1");
				List<TicketTransactionDTO> XactionList = new ArrayList<>();
				@Cleanup
				PreparedStatement XactionPS = connection.prepareStatement("SELECT id,transaction_type_id,transaction_seat_count,commission_amount,transaction_mode_id,transaction_amount,addons_amount,ac_bus_tax,tds_tax,payment_transaction_id,active_flag from ticket_transaction where ticket_id = ?");
				XactionPS.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet xActionRS = XactionPS.executeQuery();
				while (xActionRS.next()) {
					TicketTransactionDTO transDto = new TicketTransactionDTO();
					PaymentTransactionDTO payment = new PaymentTransactionDTO();
					transDto.setId(xActionRS.getInt("id"));
					transDto.setTransactionType(TransactionTypeEM.getTransactionTypeEM(xActionRS.getInt("transaction_type_id")));
					transDto.setTransSeatCount(xActionRS.getInt("transaction_seat_count"));
					transDto.setTransactionMode(TransactionModeEM.getTransactionModeEM(xActionRS.getInt("transaction_mode_id")));
					transDto.setTransactionAmount(xActionRS.getBigDecimal("transaction_amount"));
					transDto.setCommissionAmount(xActionRS.getBigDecimal("commission_amount"));
					transDto.setAddonsAmount(xActionRS.getBigDecimal("addons_amount"));
					transDto.setAcBusTax(xActionRS.getBigDecimal("ac_bus_tax"));
					transDto.setTdsTax(xActionRS.getBigDecimal("tds_tax"));
					payment.setId(xActionRS.getInt("payment_transaction_id"));
					transDto.setPaymentTrans(payment);
					transDto.setActiveFlag(xActionRS.getInt("active_flag"));
					// Mapping Cancellation details
					if (transDto.getTransactionType().getId() == TransactionTypeEM.TICKETS_CANCEL.getId()) {
						cancelTransactionsPS.setInt(1, ticketDTO.getId());
						cancelTransactionsPS.setInt(2, transDto.getId());
						@Cleanup
						ResultSet cancelTransactionsRS = cancelTransactionsPS.executeQuery();
						if (cancelTransactionsRS.next()) {
							transDto.setCancellationChargeAmount(cancelTransactionsRS.getBigDecimal("charge_amount"));
							transDto.setCancellationCommissionAmount(cancelTransactionsRS.getBigDecimal("cancel_commission"));
							transDto.setRefundAmount(cancelTransactionsRS.getBigDecimal("refund_amount"));
							transDto.setRemarks(cancelTransactionsRS.getString("remarks"));
						}
					}
					XactionList.add(transDto);
				}
				TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
				ticketTransactionDTO.setList(XactionList);
				ticketDTO.setTicketXaction(ticketTransactionDTO);
			}
			else {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}

			// pickup related Ticket Code
			if (ticketDTO.getLookupId() != 0 && ticketDTO.getId() != ticketDTO.getLookupId()) {
				PreparedStatement lookupTicket = connection.prepareStatement("SELECT code FROM ticket WHERE id = ?");
				lookupTicket.setInt(1, ticketDTO.getLookupId());
				ResultSet lookupRS = lookupTicket.executeQuery();
				if (lookupRS.next()) {
					ticketDTO.setRelatedTicketCode(lookupRS.getString("code"));
				}
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketDTO;
	}

	public List<TicketAddonsDetailsDTO> getTicketAddonsDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketAddonsDetailsDTO> ticketAddonsDetailsList = new ArrayList<TicketAddonsDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id FROM ticket WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, ticketDTO.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				@Cleanup
				PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT tad.id,td.seat_code,tad.ticket_addons_type_id,tad.ticket_status_id,tad.refference_id,tad.reference_code,tad.value,tad.active_flag FROM ticket_addons_detail tad,  ticket_detail td WHERE tad.ticket_id = td.ticket_id and tad.ticket_detail_id = td.id AND tad.active_flag = td.active_flag AND tad.ticket_id = ? and tad.active_flag = 1");
				ticketAddonsPS.setInt(1, rs.getInt("id"));

				@Cleanup
				ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
				while (ticketAddonsRS.next()) {
					TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
					addonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
					addonsDetailsDTO.setSeatCode(ticketAddonsRS.getString("seat_code"));
					addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
					addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
					addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
					addonsDetailsDTO.setRefferenceCode(ticketAddonsRS.getString("reference_code"));
					addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
					addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
					ticketAddonsDetailsList.add(addonsDetailsDTO);
				}
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketAddonsDetailsList;
	}

	/**
	 * @Desc Insert the values into ticket table while blocking
	 * @param authDTO
	 * @param ticketDTO
	 * @throws Exception
	 */
	public TicketDTO insertTicket(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, Connection connection, String auditEvent) throws Exception {
		try {
			int psCount = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket(code, booking_code, namespace_id, user_id, for_user_id, trip_date, travel_minutes, from_station_id, to_station_id, boarding_point_id, boarding_point_minutes, dropping_point_id, dropping_point_minutes, schedule_id, trip_code, trip_stage_code, mobile_number, email_id, service_number, cancellation_policy_id, reporting_minutes, journey_type, device_medium, ticket_status_id, bus_id, tax_id, lookup_id, payment_gateway_partner_code, remarks, ticket_at, additional_attribute, active_flag, updated_by, updated_at) VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,NOW() ,?,1,?,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString(++psCount, ticketDTO.getCode());
			ps.setString(++psCount, bookingDTO.getCode());
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setInt(++psCount, ticketDTO.getTicketUser().getId());
			ps.setInt(++psCount, ticketDTO.getTicketForUser().getId());
			ps.setString(++psCount, ticketDTO.getTripDate().format("YYYY-MM-DD"));
			ps.setInt(++psCount, ticketDTO.getTravelMinutes());
			ps.setInt(++psCount, ticketDTO.getFromStation().getId());
			ps.setInt(++psCount, ticketDTO.getToStation().getId());
			ps.setInt(++psCount, ticketDTO.getBoardingPoint().getId());
			ps.setInt(++psCount, ticketDTO.getBoardingPoint().getMinitues());
			ps.setInt(++psCount, ticketDTO.getDroppingPoint().getId());
			ps.setInt(++psCount, ticketDTO.getDroppingPoint().getMinitues());
			ps.setInt(++psCount, ticketDTO.getTripDTO().getSchedule().getId());
			ps.setString(++psCount, ticketDTO.getTripDTO().getCode());
			ps.setString(++psCount, ticketDTO.getTripDTO().getStage().getCode());
			ps.setString(++psCount, ticketDTO.getPassengerMobile());
			ps.setString(++psCount, ticketDTO.getPassengerEmailId());
			ps.setString(++psCount, StringUtil.substring(ticketDTO.getServiceNo(), 25));
			ps.setInt(++psCount, ticketDTO.getCancellationTerm().getId());
			ps.setInt(++psCount, ticketDTO.getReportingMinutes());
			ps.setString(++psCount, ticketDTO.getJourneyType().getCode());
			ps.setInt(++psCount, ticketDTO.getDeviceMedium().getId());
			ps.setInt(++psCount, ticketDTO.getTicketStatus().getId());
			ps.setInt(++psCount, ticketDTO.getTripDTO().getBus().getId());
			ps.setInt(++psCount, ticketDTO.getTripDTO().getSchedule().getTax().getId());
			ps.setInt(++psCount, ticketDTO.getLookupId());
			// Id for blocking status
			ps.setString(++psCount, bookingDTO.getPaymentGatewayPartnerCode());
			ps.setString(++psCount, ticketDTO.getRemarks());
			ps.setString(++psCount, getAdditionalAtribute(ticketDTO));
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
			@Cleanup
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				ticketDTO.setId(rs.getInt(1));
				bookingDTO.setId(rs.getInt(1));
			}
			else {
				ticketDTO.setCode(null);
				throw new ServiceException();
			} // Insert Ticket Audit Table
			insertTicketAudit(authDTO, ticketDTO, connection, auditEvent + "Create Ticket", false);
		}
		catch (ServiceException e) {
			ticketDTO.setCode(null);
			throw e;
		}
		catch (SQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
		}
		catch (Exception e) {
			System.out.println(authDTO.getNamespaceCode() + " " + ticketDTO.getCode() + " " + ticketDTO.getPassengerMobile() + " " + ticketDTO.getDeviceMedium().getCode());
			ticketDTO.setCode(null);
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);

		}
		return ticketDTO;
	}

	public TicketDTO insertTicketExtras(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection) throws Exception {
		if (ticketDTO.getTicketExtra() != null && ticketDTO.getTicketExtra().isExtraExists()) {
			try {
				int psCount = 0;
				@Cleanup
				PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_extras(namespace_id, ticket_id, sequence_number, block_release_minutes, link_pay, phone_book_payment_status, offline_ticket_code, active_flag, updated_by, updated_at) VALUES (?,?,?,?,?, ?,?,1,?,NOW())");
				ps.setInt(++psCount, authDTO.getNamespace().getId());
				ps.setInt(++psCount, ticketDTO.getId());
				ps.setInt(++psCount, ticketDTO.getTicketExtra().getSequenceNumber());
				ps.setInt(++psCount, ticketDTO.getTicketExtra().getBlockReleaseMinutes());
				ps.setString(++psCount, StringUtil.isNotNull(ticketDTO.getTicketExtra().getLinkPay()) ? ticketDTO.getTicketExtra().getLinkPay() : Text.NA);
				ps.setInt(++psCount, ticketDTO.getTicketExtra().getPhoneBookPaymentStatus());
				ps.setString(++psCount, StringUtil.isNotNull(ticketDTO.getTicketExtra().getOfflineTicketCode()) ? ticketDTO.getTicketExtra().getOfflineTicketCode() : Text.NA);
				ps.setInt(++psCount, authDTO.getUser().getId());
				ps.executeUpdate();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ticketDTO;
	}

	public TicketDTO insertTicketExtrasV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			insertTicketExtras(authDTO, ticketDTO, connection);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ticketDTO;
	}

	public TicketDTO updateTicketExtra(AuthDTO authDTO, TicketDTO ticketDTO) throws Exception {
		if (ticketDTO.getTicketExtra() != null) {
			try {
				@Cleanup
				Connection connection = ConnectDAO.getConnection();
				int psCount = 0;
				@Cleanup
				PreparedStatement ps = connection.prepareStatement("UPDATE ticket_extras SET link_pay = ? WHERE namespace_id = ? AND ticket_id = ?");
				ps.setString(++psCount, StringUtil.isNotNull(ticketDTO.getTicketExtra().getLinkPay()) ? ticketDTO.getTicketExtra().getLinkPay() : Text.NA);
				ps.setInt(++psCount, authDTO.getNamespace().getId());
				ps.setInt(++psCount, ticketDTO.getId());
				ps.executeUpdate();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ticketDTO;
	}

	public void updatePhoneBookTicketPaymentStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int psCount = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket_extras SET phone_book_payment_status = ? WHERE namespace_id = ? AND ticket_id = ?");
			ps.setInt(++psCount, ticketDTO.getTicketExtra().getPhoneBookPaymentStatus());
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setInt(++psCount, ticketDTO.getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertTicketDetails(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection) throws Exception {
		try {
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				int psCount = 0;
				@Cleanup
				PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_detail (ticket_id, seat_code, seat_name, seat_type, ticket_status_id, travel_status_id, passenger_name, passenger_age, seat_gender, seat_fare, ac_bus_tax, active_flag, updated_by, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,1,?,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
				ps.setInt(++psCount, ticketDTO.getId());
				ps.setString(++psCount, ticketDetailsDTO.getSeatCode());
				ps.setString(++psCount, ticketDetailsDTO.getSeatName());
				ps.setString(++psCount, ticketDetailsDTO.getSeatTypeName());
				ps.setInt(++psCount, ticketDetailsDTO.getTicketStatus().getId());
				ps.setInt(++psCount, TravelStatusEM.YET_BOARD.getId());
				// status for blocking
				ps.setString(++psCount, ticketDetailsDTO.getPassengerName());
				ps.setInt(++psCount, ticketDetailsDTO.getPassengerAge());
				ps.setInt(++psCount, ticketDetailsDTO.getSeatGendar().getId());
				ps.setBigDecimal(++psCount, ticketDetailsDTO.getSeatFare());
				ps.setBigDecimal(++psCount, ticketDetailsDTO.getAcBusTax());
				ps.setInt(++psCount, authDTO.getUser().getId());
				ps.executeUpdate();
				@Cleanup
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					ticketDetailsDTO.setId(rs.getInt(1));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void insertTicketAddonsDetails(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection) throws Exception {
		try {
			if (ticketDTO.getTicketAddonsDetails() != null && !ticketDTO.getTicketAddonsDetails().isEmpty()) {
				for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
					int psCount = 0;
					@Cleanup
					PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_addons_detail (ticket_id,ticket_detail_id,ticket_addons_type_id,ticket_status_id,refference_id,reference_code,value,active_flag,updated_by,updated_at) VALUES (?,?,?,?,?,  ?,?,1,?,now())");
					ps.setInt(++psCount, ticketDTO.getId());
					ps.setInt(++psCount, addonsDetailsDTO.getTicketDetailsId(ticketDTO.getTicketDetails()));
					ps.setInt(++psCount, addonsDetailsDTO.getAddonsType().getId());
					ps.setInt(++psCount, addonsDetailsDTO.getTicketStatus().getId());
					ps.setInt(++psCount, addonsDetailsDTO.getRefferenceId());
					ps.setString(++psCount, addonsDetailsDTO.getRefferenceCode());
					ps.setBigDecimal(++psCount, addonsDetailsDTO.getValue());
					ps.setInt(++psCount, authDTO.getUser().getId());
					ps.executeUpdate();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected void insertTicketAudit(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection, String actionEvent, boolean isEditedTicket) {
		try {
			int psCount = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO audit_ticket_log (namespace_id, ticket_code, user_id, ticket_status_id, event,device_medium, active_flag,updated_by,updated_at) VALUES (?,?,?,?,?,?,1,?,NOW())");
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setString(++psCount, ticketDTO.getCode());
			ps.setInt(++psCount, ticketDTO.getTicketUser().getId());
			ps.setInt(++psCount, isEditedTicket && ticketDTO.getTicketStatus() != null ? TicketStatusEM.EDIT_TICKET.getId() : ticketDTO.getTicketStatus().getId());
			ps.setString(++psCount, StringUtil.isNull(actionEvent) ? ticketDTO.getTicketEvent() : StringUtil.substring(actionEvent, 240));
			ps.setInt(++psCount, authDTO.getDeviceMedium().getId());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void insertTicketAuditLog(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection, String actionEvent) {
		insertTicketAudit(authDTO, ticketDTO, connection, actionEvent, false);
	}

	public TicketAddonsDetailsDTO checkTicketUsed(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketAddonsDetailsDTO ticketAddonsDetailsDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT id FROM ticket_addons_detail WHERE reference_code = ? AND active_flag = 1");
			ticketAddonsPS.setString(1, ticketDTO.getCode());
			@Cleanup
			ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
			if (ticketAddonsRS.next()) {
				ticketAddonsDetailsDTO = new TicketAddonsDetailsDTO();
				ticketAddonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketAddonsDetailsDTO;
	}

	public TicketExtraDTO getTicketExtra(AuthDTO authDTO, int ticketId) {
		TicketExtraDTO ticketExtraDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT block_release_minutes, sequence_number, link_pay, phone_book_payment_status, offline_ticket_code FROM ticket_extras WHERE namespace_id = ? AND ticket_id = ? AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, ticketId);

			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketExtraDTO = new TicketExtraDTO();
				ticketExtraDTO.setBlockReleaseMinutes(rs.getInt("block_release_minutes"));
				ticketExtraDTO.setSequenceNumber(rs.getInt("sequence_number"));
				ticketExtraDTO.setLinkPay(rs.getString("link_pay"));
				ticketExtraDTO.setPhoneBookPaymentStatus(rs.getInt("phone_book_payment_status"));
				ticketExtraDTO.setOfflineTicketCode(rs.getString("offline_ticket_code"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketExtraDTO;
	}

	public TicketExtraDTO getTicketExtra(AuthDTO authDTO, TicketDTO ticket) {
		TicketExtraDTO ticketExtraDTO = new TicketExtraDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT tex.block_release_minutes, tex.sequence_number, tex.link_pay, tex.offline_ticket_code FROM ticket tck, ticket_extras tex WHERE tex.namespace_id = ? AND tck.code = ? AND tex.ticket_id = tck.id AND tex.active_flag = 1 AND tck.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, ticket.getCode());

			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketExtraDTO.setBlockReleaseMinutes(rs.getInt("tex.block_release_minutes"));
				ticketExtraDTO.setSequenceNumber(rs.getInt("tex.sequence_number"));
				ticketExtraDTO.setLinkPay(rs.getString("tex.link_pay"));
				ticketExtraDTO.setOfflineTicketCode(rs.getString("tex.offline_ticket_code"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketExtraDTO;
	}

	public void getTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = null;
			if (StringUtil.isNotNull(ticketDTO.getBookingCode())) {
				ps = connection.prepareStatement("SELECT id,code,booking_code,namespace_id,trip_date,travel_minutes,boarding_point_id,ticket_at,boarding_point_minutes,dropping_point_id,dropping_point_minutes,from_station_id,to_station_id,trip_code,trip_stage_code,mobile_number,email_id,service_number,journey_type,device_medium,for_user_id,user_id,bus_id,tax_id,ticket_status_id,lookup_id,additional_attribute,remarks,active_flag,updated_at,updated_by FROM ticket WHERE booking_code = ? AND namespace_id = ? and active_flag = 1");
				ps.setString(1, ticketDTO.getBookingCode());
			}
			else if (ticketDTO.getId() != 0) {
				ps = connection.prepareStatement("SELECT id,code,booking_code,namespace_id,trip_date,travel_minutes,boarding_point_id,ticket_at,boarding_point_minutes,dropping_point_id,dropping_point_minutes,from_station_id,to_station_id,trip_code,trip_stage_code,mobile_number,email_id,service_number,journey_type,device_medium,for_user_id,user_id,bus_id,tax_id,ticket_status_id,lookup_id,additional_attribute,remarks,active_flag,updated_at,updated_by FROM ticket WHERE id = ? AND namespace_id = ? and active_flag = 1");
				ps.setInt(1, ticketDTO.getId());
			}
			else {
				ps = connection.prepareStatement("SELECT id,code,booking_code,namespace_id,trip_date,travel_minutes,boarding_point_id,ticket_at,boarding_point_minutes,dropping_point_id,dropping_point_minutes,from_station_id,to_station_id,trip_code,trip_stage_code,mobile_number,email_id,service_number,journey_type,device_medium,for_user_id,user_id,bus_id,tax_id,ticket_status_id,lookup_id,additional_attribute,remarks,active_flag,updated_at,updated_by FROM ticket WHERE code = ? AND namespace_id = ? and active_flag = 1");
				ps.setString(1, ticketDTO.getCode());
			}
			ps.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketDTO.setId(rs.getInt("id"));
				ticketDTO.setCode(rs.getString("code"));
				ticketDTO.setBookingCode(rs.getString("booking_code"));
				ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				ticketDTO.setTravelMinutes(rs.getInt("travel_minutes"));
				ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
				ticketDTO.setServiceNo(rs.getString("service_number"));
				ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(rs.getString("journey_type")));
				ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				ticketDTO.setPassengerEmailId(rs.getString("email_id"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));

				NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
				taxDTO.setId(rs.getInt("tax_id"));
				ticketDTO.setTax(taxDTO);

				convertTicketFromAdditionalAtribute(ticketDTO, rs.getString("additional_attribute"));

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(rs.getInt("from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(rs.getInt("to_station_id"));
				ticketDTO.setFromStation(fromStationDTO);
				ticketDTO.setToStation(toStationDTO);
				// get boarding and dropping points

				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setId(rs.getInt("boarding_point_id"));
				stationPointDTO.setMinitues(rs.getInt("boarding_point_minutes"));
				ticketDTO.setBoardingPoint(stationPointDTO);
				StationPointDTO ToStationPointDTO = new StationPointDTO();
				ToStationPointDTO.setId(rs.getInt("dropping_point_id"));
				ToStationPointDTO.setMinitues(rs.getInt("dropping_point_minutes"));
				ticketDTO.setDroppingPoint(ToStationPointDTO);
				UserDTO ticketUserDTO = new UserDTO();
				ticketUserDTO.setId(rs.getInt("user_id"));
				ticketDTO.setTicketUser(ticketUserDTO);
				UserDTO ticketForUserDTO = new UserDTO();
				ticketForUserDTO.setId(rs.getInt("for_user_id"));
				ticketDTO.setTicketForUser(ticketForUserDTO);
				ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
				ticketDTO.setActiveFlag(rs.getInt("active_flag"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("updated_at")));
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("trip_code"));
				StageDTO stageDTO = new StageDTO();
				stageDTO.setCode(rs.getString("trip_stage_code"));
				tripDTO.setStage(stageDTO);
				BusDTO busDTO = new BusDTO();
				busDTO.setId(rs.getInt("bus_id"));
				tripDTO.setBus(busDTO);
				ticketDTO.setLookupId(rs.getInt("lookup_id"));
				ticketDTO.setTripDTO(tripDTO);
				ticketDTO.setRemarks(rs.getString("remarks"));
				// get Passenger Details
				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				@Cleanup
				PreparedStatement pdps = connection.prepareStatement("SELECT id,ticket_id,seat_name,seat_code,seat_type,ticket_status_id,travel_status_id,passenger_name,passenger_age,seat_gender,seat_fare,ac_bus_tax,active_flag,updated_by,updated_at FROM ticket_detail WHERE ticket_id = ? and active_flag = 1");
				pdps.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet pdrs = pdps.executeQuery();
				while (pdrs.next()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setId(pdrs.getInt("id"));
					tdDTO.setSeatName(pdrs.getString("seat_name"));
					tdDTO.setSeatCode(pdrs.getString("seat_code"));
					tdDTO.setSeatType(pdrs.getString("seat_type"));
					tdDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(pdrs.getInt("ticket_status_id")));
					tdDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(pdrs.getInt("travel_status_id")));
					tdDTO.setPassengerName(pdrs.getString("passenger_name"));
					tdDTO.setPassengerAge(pdrs.getInt("passenger_age"));
					tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(pdrs.getInt("seat_gender")));
					tdDTO.setSeatFare(pdrs.getBigDecimal("seat_fare"));
					tdDTO.setAcBusTax(pdrs.getBigDecimal("ac_bus_tax"));
					tdDTO.setActiveFlag(pdrs.getInt("active_flag"));
					ticketDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(ticketDetails);

				// get Ticket Addons Details
				List<TicketAddonsDetailsDTO> ticketAddonsDetailsList = new ArrayList<TicketAddonsDetailsDTO>();
				@Cleanup
				PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT tad.id,td.seat_code,tad.ticket_addons_type_id,tad.ticket_status_id,tad.refference_id,tad.reference_code,tad.value,tad.active_flag FROM ticket_addons_detail tad,  ticket_detail td WHERE tad.ticket_id = td.ticket_id and tad.ticket_detail_id = td.id AND tad.active_flag = td.active_flag AND tad.ticket_id = ? and tad.active_flag = 1");
				ticketAddonsPS.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
				while (ticketAddonsRS.next()) {
					TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
					addonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
					addonsDetailsDTO.setSeatCode(ticketAddonsRS.getString("seat_code"));
					addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
					addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
					addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
					addonsDetailsDTO.setRefferenceCode(ticketAddonsRS.getString("reference_code"));
					addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
					addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
					ticketAddonsDetailsList.add(addonsDetailsDTO);
				}
				ticketDTO.setTicketAddonsDetails(ticketAddonsDetailsList);
			}
			else {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
		}
		catch (ServiceException e) {
			System.out.println("PNR01:" + authDTO.getNamespaceCode() + " - " + ticketDTO.getCode() + " - " + ticketDTO.getBookingCode());
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public List<TicketDTO> getTicketStatusByBookingCode(AuthDTO authDTO, String bookingCode) {
		List<TicketDTO> ticketList = new ArrayList<TicketDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,booking_code,namespace_id,trip_date,travel_minutes,boarding_point_id,ticket_at,boarding_point_minutes,dropping_point_id,dropping_point_minutes,from_station_id,to_station_id,trip_code,trip_stage_code,mobile_number,email_id,service_number,journey_type,device_medium,for_user_id,user_id,bus_id,ticket_status_id,additional_attribute,active_flag,updated_at,updated_by FROM ticket WHERE booking_code = ? AND namespace_id = ? and active_flag = 1");
			ps.setString(1, bookingCode);
			ps.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setId(rs.getInt("id"));
				ticketDTO.setCode(rs.getString("code"));
				ticketDTO.setBookingCode(rs.getString("booking_code"));
				ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				ticketDTO.setTravelMinutes(rs.getInt("travel_minutes"));
				ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
				ticketDTO.setServiceNo(rs.getString("service_number"));
				ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(rs.getString("journey_type")));
				ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				ticketDTO.setPassengerEmailId(rs.getString("email_id"));
				ticketDTO.setServiceNo(rs.getString("service_number"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));

				convertTicketFromAdditionalAtribute(ticketDTO, rs.getString("additional_attribute"));

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(rs.getInt("from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(rs.getInt("to_station_id"));
				ticketDTO.setFromStation(fromStationDTO);
				ticketDTO.setToStation(toStationDTO);
				// get boarding and dropping points

				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setId(rs.getInt("boarding_point_id"));
				stationPointDTO.setMinitues(rs.getInt("boarding_point_minutes"));
				ticketDTO.setBoardingPoint(stationPointDTO);
				StationPointDTO ToStationPointDTO = new StationPointDTO();
				ToStationPointDTO.setId(rs.getInt("dropping_point_id"));
				stationPointDTO.setMinitues(rs.getInt("dropping_point_minutes"));
				ticketDTO.setDroppingPoint(ToStationPointDTO);
				UserDTO TicketUserDTO = new UserDTO();
				TicketUserDTO.setId(rs.getInt("user_id"));
				ticketDTO.setTicketUser(TicketUserDTO);
				UserDTO TicketForUserDTO = new UserDTO();
				TicketForUserDTO.setId(rs.getInt("for_user_id"));
				ticketDTO.setTicketForUser(TicketForUserDTO);
				ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
				ticketDTO.setActiveFlag(rs.getInt("active_flag"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("updated_at")));
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("trip_code"));
				StageDTO stageDTO = new StageDTO();
				stageDTO.setCode(rs.getString("trip_stage_code"));
				tripDTO.setStage(stageDTO);
				BusDTO busDTO = new BusDTO();
				busDTO.setId(rs.getInt("bus_id"));
				tripDTO.setBus(busDTO);
				ticketDTO.setTripDTO(tripDTO);
				// get Passenger Details
				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				@Cleanup
				PreparedStatement pdps = connection.prepareStatement("SELECT id,ticket_id,seat_name,seat_code,seat_type,ticket_status_id,travel_status_id,passenger_name,passenger_age,seat_gender,seat_fare,ac_bus_tax,active_flag,updated_by,updated_at FROM ticket_detail WHERE ticket_id = ? and active_flag = 1");
				pdps.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet pdrs = pdps.executeQuery();
				while (pdrs.next()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setId(pdrs.getInt("id"));
					tdDTO.setSeatName(pdrs.getString("seat_name"));
					tdDTO.setSeatCode(pdrs.getString("seat_code"));
					tdDTO.setSeatType(pdrs.getString("seat_type"));
					tdDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(pdrs.getInt("ticket_status_id")));
					tdDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(pdrs.getInt("travel_status_id")));
					tdDTO.setPassengerName(pdrs.getString("passenger_name"));
					tdDTO.setPassengerAge(pdrs.getInt("passenger_age"));
					tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(pdrs.getInt("seat_gender")));
					tdDTO.setSeatFare(pdrs.getBigDecimal("seat_fare"));
					tdDTO.setAcBusTax(pdrs.getBigDecimal("ac_bus_tax"));
					tdDTO.setActiveFlag(pdrs.getInt("active_flag"));
					ticketDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(ticketDetails);

				// get Ticket Addons Details
				List<TicketAddonsDetailsDTO> ticketAddonsDetailsList = new ArrayList<TicketAddonsDetailsDTO>();
				@Cleanup
				PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT tad.id,td.seat_code,tad.ticket_addons_type_id,tad.ticket_status_id,tad.refference_id,tad.value,tad.active_flag FROM ticket_addons_detail tad,  ticket_detail td WHERE tad.ticket_id = td.ticket_id and tad.ticket_detail_id = td.id AND tad.active_flag = td.active_flag AND tad.ticket_id = ? and tad.active_flag = 1");
				ticketAddonsPS.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
				while (ticketAddonsRS.next()) {
					TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
					addonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
					addonsDetailsDTO.setSeatCode(ticketAddonsRS.getString("seat_code"));
					addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
					addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
					addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
					addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
					addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
					ticketAddonsDetailsList.add(addonsDetailsDTO);
				}
				ticketDTO.setTicketAddonsDetails(ticketAddonsDetailsList);
				ticketList.add(ticketDTO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketList;
	}

	public TicketDTO getTicketStatusV2(AuthDTO authDTO, String ticketCode) {
		TicketDTO ticketDTO = new TicketDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,boarding_point_id,ticket_at,boarding_point_minutes,dropping_point_id,dropping_point_minutes,user_id,for_user_id,trip_stage_code, device_medium, mobile_number, service_number, trip_code, from_station_id, to_station_id, ticket_status_id, remarks, active_flag, updated_at FROM ticket WHERE code = ? AND namespace_id = ? and active_flag = 1");
			ps.setString(1, ticketCode);
			ps.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketDTO.setId(rs.getInt("id"));
				ticketDTO.setCode(rs.getString("code"));
				ticketDTO.setBookingCode(rs.getString("booking_code"));
				ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
				ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));
				ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
				ticketDTO.setServiceNo(rs.getString("service_number"));

				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("trip_code"));
				StageDTO stageDTO = new StageDTO();
				stageDTO.setCode(rs.getString("trip_stage_code"));
				tripDTO.setStage(stageDTO);
				ticketDTO.setTripDTO(tripDTO);

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(rs.getInt("from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(rs.getInt("to_station_id"));
				ticketDTO.setFromStation(fromStationDTO);
				ticketDTO.setToStation(toStationDTO);

				// get boarding and dropping points
				StationPointDTO stationPointDTO = new StationPointDTO();
				stationPointDTO.setId(rs.getInt("boarding_point_id"));
				stationPointDTO.setMinitues(rs.getInt("boarding_point_minutes"));
				ticketDTO.setBoardingPoint(stationPointDTO);
				StationPointDTO ToStationPointDTO = new StationPointDTO();
				ToStationPointDTO.setId(rs.getInt("dropping_point_id"));
				ToStationPointDTO.setMinitues(rs.getInt("dropping_point_minutes"));
				ticketDTO.setDroppingPoint(ToStationPointDTO);

				UserDTO ticketUserDTO = new UserDTO();
				ticketUserDTO.setId(rs.getInt("user_id"));
				ticketDTO.setTicketUser(ticketUserDTO);

				UserDTO ticketForUserDTO = new UserDTO();
				ticketForUserDTO.setId(rs.getInt("for_user_id"));
				ticketDTO.setTicketForUser(ticketForUserDTO);

				ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
				ticketDTO.setActiveFlag(rs.getInt("active_flag"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));
				ticketDTO.setRemarks(rs.getString("remarks"));

				// get Passenger Details
				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				@Cleanup
				PreparedStatement pdps = connection.prepareStatement("SELECT id,seat_name,seat_code,seat_fare,passenger_name,ticket_status_id,travel_status_id,active_flag FROM ticket_detail WHERE ticket_id = ? and active_flag = 1");
				pdps.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet pdrs = pdps.executeQuery();
				while (pdrs.next()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setId(pdrs.getInt("id"));
					tdDTO.setSeatName(pdrs.getString("seat_name"));
					tdDTO.setSeatCode(pdrs.getString("seat_code"));
					tdDTO.setSeatFare(pdrs.getBigDecimal("seat_fare"));
					tdDTO.setPassengerName(pdrs.getString("passenger_name"));
					tdDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(pdrs.getInt("ticket_status_id")));
					tdDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(pdrs.getInt("travel_status_id")));
					tdDTO.setActiveFlag(pdrs.getInt("active_flag"));
					ticketDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(ticketDetails);

				// get Ticket Addons Details
				List<TicketAddonsDetailsDTO> ticketAddonsDetailsList = new ArrayList<TicketAddonsDetailsDTO>();
				@Cleanup
				PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT tad.id,td.seat_code,tad.ticket_addons_type_id,tad.ticket_status_id,tad.refference_id,tad.value,tad.active_flag FROM ticket_addons_detail tad,  ticket_detail td WHERE tad.ticket_id = td.ticket_id and tad.ticket_detail_id = td.id AND tad.active_flag = td.active_flag AND tad.ticket_id = ? and tad.active_flag = 1");
				ticketAddonsPS.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
				while (ticketAddonsRS.next()) {
					TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
					addonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
					addonsDetailsDTO.setSeatCode(ticketAddonsRS.getString("seat_code"));
					addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
					addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
					addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
					addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
					addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
					ticketAddonsDetailsList.add(addonsDetailsDTO);
				}
				ticketDTO.setTicketAddonsDetails(ticketAddonsDetailsList);
			}
			else {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketDTO;
	}

	public boolean checkDuplicateTicketCodeEntry(AuthDTO authDTO, String ticketCode) {
		boolean status = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM ticket WHERE code = ? AND namespace_id = ?");
			ps.setString(1, ticketCode);
			ps.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				status = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			status = true;
		}
		return status;
	}

	public boolean checkSeatDuplicateEntry(AuthDTO authDTO, TicketDTO ticketDTO) {
		boolean status = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psSeatCode = connection.prepareStatement("SELECT tck.id, tdt.ticket_status_id, tdt.travel_status_id, tdt.updated_at FROM ticket tck,ticket_detail tdt WHERE tck.namespace_id = ? AND tck.id = tdt.ticket_id AND tck.trip_code = ? AND tck.trip_stage_code = ? AND  tdt.seat_code = ?");
			@Cleanup
			PreparedStatement psSeatName = connection.prepareStatement("SELECT tck.id, tdt.ticket_status_id, tdt.travel_status_id, tdt.updated_at FROM ticket tck,ticket_detail	tdt WHERE tck.namespace_id = ? AND tck.id = tdt.ticket_id AND tck.trip_code = ? AND tck.trip_stage_code = ? AND tdt.seat_name = ? ");
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				for (String coRelatedStageCode : ticketDTO.getTripDTO().getReleatedStageCodeList()) {
					psSeatCode.setInt(1, authDTO.getNamespace().getId());
					psSeatCode.setString(2, ticketDTO.getTripDTO().getCode());
					psSeatCode.setString(3, coRelatedStageCode);
					psSeatCode.setString(4, detailsDTO.getSeatCode());
					@Cleanup
					ResultSet rsSeatCode = psSeatCode.executeQuery();
					while (rsSeatCode.next()) {
						TicketStatusEM statusDTO = TicketStatusEM.getTicketStatusEM(rsSeatCode.getInt("tdt.ticket_status_id"));
						TravelStatusEM travelStatus = TravelStatusEM.getTravelStatusEM(rsSeatCode.getInt("tdt.travel_status_id"));
						DateTime createdAt = new DateTime(rsSeatCode.getString("tdt.updated_at"));
						int ticketId = rsSeatCode.getInt("tck.id");
						if (statusDTO == null) {
							status = true;
							break;
						}
						else if ((statusDTO.getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || statusDTO.getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) && travelStatus.getId() != TravelStatusEM.NOT_TRAVELED.getId()) {
							status = true;
							break;
						}
						else if (ticketId != ticketDTO.getId() && statusDTO.getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(createdAt, DateUtil.NOW()) < authDTO.getNamespace().getProfile().getSeatBlockTime()) {
							status = true;
							break;
						}
					}
					psSeatName.setInt(1, authDTO.getNamespace().getId());
					psSeatName.setString(2, ticketDTO.getTripDTO().getCode());
					psSeatName.setString(3, coRelatedStageCode);
					psSeatName.setString(4, detailsDTO.getSeatName());

					@Cleanup
					ResultSet rsSeatName = psSeatName.executeQuery();
					while (rsSeatName.next()) {
						TicketStatusEM statusDTO = TicketStatusEM.getTicketStatusEM(rsSeatName.getInt("tdt.ticket_status_id"));
						TravelStatusEM travelStatus = TravelStatusEM.getTravelStatusEM(rsSeatName.getInt("tdt.travel_status_id"));
						DateTime createdAt = new DateTime(rsSeatName.getString("tdt.updated_at"));
						int ticketId = rsSeatName.getInt("tck.id");
						if (statusDTO == null) {
							status = true;
							break;
						}
						else if ((statusDTO.getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || statusDTO.getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) && travelStatus.getId() != TravelStatusEM.NOT_TRAVELED.getId()) {
							status = true;
							break;
						}
						else if (ticketId != ticketDTO.getId() && statusDTO.getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(createdAt, DateUtil.NOW()) < authDTO.getNamespace().getProfile().getSeatBlockTime()) {
							status = true;
							break;
						}
					}
				}
				psSeatCode.clearParameters();
				psSeatName.clearParameters();
				if (status) {
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(" ERROR Releated StageCode List Not Found: " + ticketDTO.getCode() + " " + ticketDTO.getTripDTO() != null ? ticketDTO.getTripDTO().getCode() : Text.EMPTY + " " + ticketDTO.getTripDTO() == null ? Text.EMPTY : ticketDTO.getTripDTO().getStage() != null ? ticketDTO.getTripDTO().getStage().getCode() : Text.EMPTY + " " + DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"));
			status = true;
		}
		return status;
	}

	public List<TicketDetailsDTO> checkSeatDuplicateEntryV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketDetailsDTO> ticketDetailsList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psSeatCode = connection.prepareStatement("SELECT tck.id, tdt.ticket_status_id, tdt.travel_status_id, tdt.updated_at FROM ticket tck,ticket_detail tdt WHERE tck.namespace_id = ? AND tck.id = tdt.ticket_id AND tck.trip_code = ? AND tck.trip_stage_code = ? AND  tdt.seat_code = ?");
			@Cleanup
			PreparedStatement psSeatName = connection.prepareStatement("SELECT tck.id, tdt.ticket_status_id, tdt.travel_status_id, tdt.updated_at FROM ticket tck,ticket_detail	tdt WHERE tck.namespace_id = ? AND tck.id = tdt.ticket_id AND tck.trip_code = ? AND tck.trip_stage_code = ? AND tdt.seat_name = ? ");
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				for (String coRelatedStageCode : ticketDTO.getTripDTO().getReleatedStageCodeList()) {
					psSeatCode.setInt(1, authDTO.getNamespace().getId());
					psSeatCode.setString(2, ticketDTO.getTripDTO().getCode());
					psSeatCode.setString(3, coRelatedStageCode);
					psSeatCode.setString(4, detailsDTO.getSeatCode());
					@Cleanup
					ResultSet rsSeatCode = psSeatCode.executeQuery();
					while (rsSeatCode.next()) {
						TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
						ticketDetailsDTO.setTicketId(rsSeatCode.getInt("tck.id"));
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rsSeatCode.getInt("tdt.ticket_status_id")));
						ticketDetailsDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(rsSeatCode.getInt("tdt.travel_status_id")));
						ticketDetailsDTO.setUpdatedAt(DateUtil.getDateTime(rsSeatCode.getString("tdt.updated_at")));
						ticketDetailsList.add(ticketDetailsDTO);
					}

					psSeatName.setInt(1, authDTO.getNamespace().getId());
					psSeatName.setString(2, ticketDTO.getTripDTO().getCode());
					psSeatName.setString(3, coRelatedStageCode);
					psSeatName.setString(4, detailsDTO.getSeatName());

					@Cleanup
					ResultSet rsSeatName = psSeatName.executeQuery();
					while (rsSeatName.next()) {
						TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
						ticketDetailsDTO.setTicketId(rsSeatName.getInt("tck.id"));
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rsSeatName.getInt("tdt.ticket_status_id")));
						ticketDetailsDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(rsSeatName.getInt("tdt.travel_status_id")));
						ticketDetailsDTO.setUpdatedAt(DateUtil.getDateTime(rsSeatName.getString("tdt.updated_at")));
						ticketDetailsList.add(ticketDetailsDTO);
					}
				}
				psSeatCode.clearParameters();
				psSeatName.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(" ERROR Releated StageCode List Not Found: " + ticketDTO.getCode() + " " + ticketDTO.getTripDTO() != null ? ticketDTO.getTripDTO().getCode() : Text.EMPTY + " " + ticketDTO.getTripDTO() == null ? Text.EMPTY : ticketDTO.getTripDTO().getStage() != null ? ticketDTO.getTripDTO().getStage().getCode() : Text.EMPTY + " " + DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"));
			throw new ServiceException(e.getMessage());
		}
		return ticketDetailsList;
	}

	public void UpdateTicketStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET ticket_status_id = ?, mobile_number = ?, email_id = ? WHERE id = ? AND namespace_id = ?");
			ps.setInt(1, ticketDTO.getTicketStatus().getId());
			ps.setString(2, ticketDTO.getPassengerMobile());
			ps.setString(3, ticketDTO.getPassengerEmailId());
			ps.setInt(4, ticketDTO.getId());
			ps.setInt(5, authDTO.getNamespace().getId());
			ps.executeUpdate();
			@Cleanup
			PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET ticket_status_id = ?, active_flag = ?, updated_at = NOW() WHERE ticket_id = ? AND seat_code = ?");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsPS.setInt(1, ticketDetailsDTO.getTicketStatus().getId());
				ticketDetailsPS.setInt(2, ticketDetailsDTO.getActiveFlag());
				ticketDetailsPS.setInt(3, ticketDTO.getId());
				ticketDetailsPS.setString(4, ticketDetailsDTO.getSeatCode());
				ticketDetailsPS.addBatch();
			}
			ticketDetailsPS.executeBatch();
			@Cleanup
			PreparedStatement ticketAddonsDetailsPS = connection.prepareStatement("UPDATE ticket_addons_detail SET ticket_status_id = ?, active_flag = ?, updated_at = NOW() WHERE ticket_id = ? AND ticket_detail_id = ?");
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
				ticketAddonsDetailsPS.setInt(1, addonsDetailsDTO.getTicketStatus().getId());
				ticketAddonsDetailsPS.setInt(2, addonsDetailsDTO.getActiveFlag());
				ticketAddonsDetailsPS.setInt(3, ticketDTO.getId());
				ticketAddonsDetailsPS.setInt(4, addonsDetailsDTO.getTicketDetailsId(ticketDTO.getTicketDetails()));
				ticketAddonsDetailsPS.addBatch();
			}
			ticketAddonsDetailsPS.executeBatch();

			String event = "";
			Map<String, TicketStatusEM> ticketStatusMap = Maps.newHashMap();
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId()) {
				event = ticketDTO.getRemarks() + " ";
			}
			else {
				event = getTicketEvent(ticketDTO, ticketStatusMap);
			}
			String confirmedBy = "";
			if (authDTO.getAdditionalAttribute().containsKey(Text.PHONE_BOOK_CONFIRMED_BY) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				confirmedBy = ", Confirmed by : " + authDTO.getAdditionalAttribute().get(Text.PHONE_BOOK_CONFIRMED_BY);
			}
			if (authDTO.getAdditionalAttribute().containsKey(Text.PHONE_BOOK_AUTO_CANCEL) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
				confirmedBy = " - Auto Cancel";
			}
			if (authDTO.getAdditionalAttribute().containsKey("CAN_MY_ACC") && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get("CAN_MY_ACC")) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				confirmedBy = " [Cancel to my account]";
			}
			if (authDTO.getAdditionalAttribute().containsKey("OVERRIDE_REFUND") && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get("OVERRIDE_REFUND")) && ticketDTO.getCancellationOverideRefundAmount() != null) {
				// Override Refund Amount
				String overrideValue = ticketDTO.isCancellationOveridePercentageFlag() && ticketDTO.getCancellationOverideValue() != null ? (" (" + String.valueOf(ticketDTO.getCancellationOverideValue()) + "%)") : Text.EMPTY;

				insertTicketAudit(authDTO, ticketDTO, connection, event + "Update Ticket Status" + confirmedBy + " with override refund Rs." + ticketDTO.getCancellationOverideRefundAmount() + overrideValue, false);
			}
			else {
				insertTicketAudit(authDTO, ticketDTO, connection, event + "Update Ticket Status" + confirmedBy, false);
			}
			if (!ticketStatusMap.isEmpty() && ticketStatusMap.get("TICKET_STATUS") != null) {
				ticketDTO.setTicketStatus(ticketStatusMap.get("TICKET_STATUS"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public void updateTicketStatusV2(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET ticket_status_id = ?, mobile_number = ?, email_id = ? WHERE id = ? AND namespace_id = ?");
			ps.setInt(1, ticketDTO.getTicketStatus().getId());
			ps.setString(2, ticketDTO.getPassengerMobile());
			ps.setString(3, ticketDTO.getPassengerEmailId());
			ps.setInt(4, ticketDTO.getId());
			ps.setInt(5, authDTO.getNamespace().getId());
			ps.executeUpdate();
			@Cleanup
			PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET ticket_status_id = ?,seat_fare = ?, active_flag = ?, updated_at = NOW() WHERE ticket_id = ? AND seat_code = ?");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsPS.setInt(1, ticketDetailsDTO.getTicketStatus().getId());
				ticketDetailsPS.setBigDecimal(2, ticketDetailsDTO.getSeatFare());
				ticketDetailsPS.setInt(3, ticketDetailsDTO.getActiveFlag());
				ticketDetailsPS.setInt(4, ticketDTO.getId());
				ticketDetailsPS.setString(5, ticketDetailsDTO.getSeatCode());
				ticketDetailsPS.addBatch();
			}
			ticketDetailsPS.executeBatch();
			@Cleanup
			PreparedStatement ticketAddonsDetailsPS = connection.prepareStatement("UPDATE ticket_addons_detail SET ticket_status_id = ?, active_flag = ?, updated_at = NOW() WHERE ticket_id = ? AND ticket_detail_id = ?");
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
				ticketAddonsDetailsPS.setInt(1, addonsDetailsDTO.getTicketStatus().getId());
				ticketAddonsDetailsPS.setInt(2, addonsDetailsDTO.getActiveFlag());
				ticketAddonsDetailsPS.setInt(3, ticketDTO.getId());
				ticketAddonsDetailsPS.setInt(4, addonsDetailsDTO.getTicketDetailsId(ticketDTO.getTicketDetails()));
				ticketAddonsDetailsPS.addBatch();
			}
			ticketAddonsDetailsPS.executeBatch();

			String event = "";
			Map<String, TicketStatusEM> ticketStatusMap = Maps.newHashMap();
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId()) {
				event = ticketDTO.getRemarks() + " ";
			}
			else {
				event = getTicketEvent(ticketDTO, ticketStatusMap);
			}
			String confirmedBy = "";
			if (authDTO.getAdditionalAttribute().containsKey(Text.PHONE_BOOK_CONFIRMED_BY) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				confirmedBy = ", Confirmed by : " + authDTO.getAdditionalAttribute().get(Text.PHONE_BOOK_CONFIRMED_BY);
			}
			if (authDTO.getAdditionalAttribute().containsKey(Text.PHONE_BOOK_AUTO_CANCEL) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
				confirmedBy = " - Auto Cancel";
			}
			if (authDTO.getAdditionalAttribute().containsKey("CAN_MY_ACC") && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get("CAN_MY_ACC")) && ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				confirmedBy = " [Cancel to my account]";
			}
			if (authDTO.getAdditionalAttribute().containsKey("OVERRIDE_REFUND") && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get("OVERRIDE_REFUND")) && ticketDTO.getCancellationOverideRefundAmount() != null) {
				// Override Refund Amount
				String overrideValue = ticketDTO.isCancellationOveridePercentageFlag() && ticketDTO.getCancellationOverideValue() != null ? (" (" + String.valueOf(ticketDTO.getCancellationOverideValue()) + "%)") : Text.EMPTY;

				insertTicketAudit(authDTO, ticketDTO, connection, event + "Update Ticket Status" + confirmedBy + " with override refund Rs." + ticketDTO.getCancellationOverideRefundAmount() + overrideValue, false);
			}
			else {
				insertTicketAudit(authDTO, ticketDTO, connection, event + "Update Ticket Status" + confirmedBy, false);
			}
			if (!ticketStatusMap.isEmpty() && ticketStatusMap.get("TICKET_STATUS") != null) {
				ticketDTO.setTicketStatus(ticketStatusMap.get("TICKET_STATUS"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void UpdateTicketDetailsStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET ticket_status_id = ? WHERE id = ? AND namespace_id = ?");
			ps.setInt(1, ticketDTO.getTicketStatus().getId());
			ps.setInt(2, ticketDTO.getId());
			ps.setInt(3, authDTO.getNamespace().getId());
			ps.executeUpdate();
			@Cleanup
			PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET seat_name = ?,seat_code = ?,ticket_status_id = ? WHERE ticket_id = ? AND id = ?");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsPS.setString(1, ticketDetailsDTO.getSeatName());
				ticketDetailsPS.setString(2, ticketDetailsDTO.getSeatCode());
				ticketDetailsPS.setInt(3, ticketDetailsDTO.getTicketStatus().getId());
				ticketDetailsPS.setInt(4, ticketDTO.getId());
				ticketDetailsPS.setInt(5, ticketDetailsDTO.getId());
				ticketDetailsPS.addBatch();
			}
			ticketDetailsPS.executeBatch();

			@Cleanup
			PreparedStatement ticketAddonsDetailsPS = connection.prepareStatement("UPDATE ticket_addons_detail SET ticket_status_id = ?, active_flag = ? WHERE ticket_id = ? AND ticket_detail_id = ?");
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
				ticketAddonsDetailsPS.setInt(1, addonsDetailsDTO.getTicketStatus().getId());
				ticketAddonsDetailsPS.setInt(2, addonsDetailsDTO.getActiveFlag());
				ticketAddonsDetailsPS.setInt(3, ticketDTO.getId());
				ticketAddonsDetailsPS.setInt(4, addonsDetailsDTO.getTicketDetailsId(ticketDTO.getTicketDetails()));
				ticketAddonsDetailsPS.addBatch();
			}
			ticketAddonsDetailsPS.executeBatch();

			insertTicketAudit(authDTO, ticketDTO, connection, "Update Ticket Details Status", false);

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	/**
	 * @DESC Check the seat status for pending order
	 * @param authDTO
	 * @param ticketDTO
	 * @return true if the seat is available to confirm else return false
	 */
	public boolean checkSeatStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		boolean isValidTicket = true;
		try {
			String seatCode = null;
			for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {
				if (seatCode == null) {
					seatCode = "'" + dto.getSeatCode().split("-")[1] + "'";
				}
				else {
					seatCode = seatCode + "'" + dto.getSeatCode().split("-")[1] + "'";
				}

			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM trip_seat_detail WHERE namespace_id=? AND trip_code=? AND seat_code IN (" + seatCode + ")");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, ticketDTO.getTripDTO().getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				isValidTicket = false;
			}
		}
		catch (ArrayIndexOutOfBoundsException aiobe) {
			throw new ServiceException(ErrorCode.PENDING_ORDER_CONFIRMATION_SEAT_COUNT_MISMATCH);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return isValidTicket;
	}

	public TripChartDTO getTicketForTripChart(AuthDTO authDTO, TripDTO tripDTO) {
		TripChartDTO tripChartDTO = new TripChartDTO();
		List<TripChartDetailsDTO> tripChartDetailsDTOList = new ArrayList<TripChartDetailsDTO>();
		boolean isAllowCancelSeats = tripDTO.getAdditionalAttributes().containsKey(Text.TRIP_CHART_VIEW_CANCEL_SEATS_FLAG);
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT tck.id, tck.code,tck.trip_date, tck.mobile_number, tck.service_number, device_medium, tck.boarding_point_id, tck.boarding_point_minutes,tck.dropping_point_id,tck.dropping_point_minutes, tck.travel_minutes,tck.ticket_at,tck.trip_stage_code,tck.from_station_id,tck.to_station_id,remarks,tck.additional_attribute,tck.updated_at,tkd.id, tkd.seat_name, tkd.seat_code, tkd.passenger_name,tkd.passenger_age,tkd.seat_gender,tkd.seat_fare, tkd.ac_bus_tax, usr.id, usr.code, usr.username,usr.first_name,usr.last_name,usr.user_role_id, usr.user_group_id,tkd.ticket_status_id,tkd.travel_status_id,tkd.updated_at, payment_gateway_partner_code FROM ticket tck,ticket_detail tkd,user usr WHERE tck.namespace_id = ? and usr.namespace_id = ? AND tck.id = tkd.ticket_id AND usr.id = tck.user_id AND tck.trip_code = ? AND tck.active_flag = 1 AND tkd.active_flag = 1");
			ticketPS.setInt(1, authDTO.getNamespace().getId());
			ticketPS.setInt(2, authDTO.getNamespace().getId());
			ticketPS.setString(3, tripDTO.getCode());

			@Cleanup
			PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT  ticket_addons_type_id, ticket_status_id, refference_id, reference_code, value, active_flag FROM ticket_addons_detail  tad WHERE  ticket_id = ? and ticket_detail_id = ? AND active_flag = 1");

			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();

			while (ticketRS.next()) {
				int ticketId = ticketRS.getInt("tck.id");
				int ticketDetailsId = ticketRS.getInt("tkd.id");
				TripChartDetailsDTO tripchartDetailsdto = new TripChartDetailsDTO();
				tripchartDetailsdto.setTicketCode(ticketRS.getString("tck.code"));
				tripchartDetailsdto.setTripDate(ticketRS.getString("trip_date"));
				tripchartDetailsdto.setPassengerMobile(ticketRS.getString("tck.mobile_number"));
				tripchartDetailsdto.setServiceNumber(ticketRS.getString("tck.service_number"));
				StationPointDTO boardingPointDTO = new StationPointDTO();
				boardingPointDTO.setId(ticketRS.getInt("tck.boarding_point_id"));
				boardingPointDTO.setMinitues(ticketRS.getInt("tck.boarding_point_minutes"));
				tripchartDetailsdto.setBoardingPoint(boardingPointDTO);
				StationPointDTO droppingPointDTO = new StationPointDTO();
				droppingPointDTO.setId(ticketRS.getInt("tck.dropping_point_id"));
				droppingPointDTO.setMinitues(ticketRS.getInt("tck.dropping_point_minutes"));
				tripchartDetailsdto.setDroppingPoint(droppingPointDTO);
				tripchartDetailsdto.setTicketAt(new DateTime(ticketRS.getString("tck.ticket_at")).format("YYYY-MM-DD hh:mm:ss"));
				tripchartDetailsdto.setTripStageCode(ticketRS.getString("trip_stage_code"));
				tripchartDetailsdto.setRemarks(StringUtil.isNull(ticketRS.getString("remarks"), Text.NA));
				tripchartDetailsdto.setTicketUpdatedAt(new DateTime(ticketRS.getString("tck.updated_at")));
				DeviceMediumEM deviceMedium = DeviceMediumEM.getDeviceMediumEM(ticketRS.getInt("device_medium"));
				if (deviceMedium.getId() == DeviceMediumEM.API_USER.getId() && tripchartDetailsdto.getRemarks().length() < 20) {
					tripchartDetailsdto.setRemarks(Text.EMPTY);
				}
				tripchartDetailsdto.setDeviceMedium(deviceMedium);
				tripchartDetailsdto.setSeatName(ticketRS.getString("tkd.seat_name"));
				tripchartDetailsdto.setSeatCode(ticketRS.getString("tkd.seat_code"));
				tripchartDetailsdto.setPassengerName(ticketRS.getString("tkd.passenger_name"));
				tripchartDetailsdto.setPassengerAge(ticketRS.getInt("tkd.passenger_age"));
				tripchartDetailsdto.setAcBusTax(ticketRS.getBigDecimal("tkd.ac_bus_tax"));
				tripchartDetailsdto.setSeatFare(ticketRS.getBigDecimal("tkd.seat_fare"));
				tripchartDetailsdto.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketRS.getInt("tkd.ticket_status_id")));
				tripchartDetailsdto.setTravelStatus(TravelStatusEM.getTravelStatusEM(ticketRS.getInt("tkd.travel_status_id")));
				SeatGendarEM seatgenderdto = SeatGendarEM.getSeatGendarEM(ticketRS.getInt("tkd.seat_gender"));
				tripchartDetailsdto.setTravelMinutes(ticketRS.getInt("tck.travel_minutes"));
				tripchartDetailsdto.setSeatGendar(seatgenderdto);
				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(ticketRS.getInt("tck.from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(ticketRS.getInt("tck.to_station_id"));
				tripchartDetailsdto.setFromStation(fromStationDTO);
				tripchartDetailsdto.setToStation(toStationDTO);
				UserDTO userDTO = new UserDTO();
				GroupDTO groupDTO = new GroupDTO();
				userDTO.setId(ticketRS.getInt("usr.id"));
				userDTO.setCode(ticketRS.getString("usr.code"));
				userDTO.setUsername(ticketRS.getString("usr.username"));
				userDTO.setName(ticketRS.getString("usr.first_name"));
				userDTO.setLastname(ticketRS.getString("usr.last_name"));
				userDTO.setUserRole(UserRoleEM.getUserRoleEM(ticketRS.getInt("usr.user_role_id")));
				tripchartDetailsdto.setTicketSeatUpdatedAt(new DateTime(ticketRS.getString("tkd.updated_at")));
				tripchartDetailsdto.setBookingType(ticketRS.getString("payment_gateway_partner_code"));
				groupDTO.setId(ticketRS.getInt("usr.user_group_id"));
				userDTO.setGroup(groupDTO);
				tripchartDetailsdto.setUser(userDTO);
				tripchartDetailsdto.setTripCode(tripDTO.getCode());
				convertTicketFromAdditionalAtributeV2(tripchartDetailsdto, ticketRS.getString("tck.additional_attribute"));
				if (tripchartDetailsdto.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || tripchartDetailsdto.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || (isAllowCancelSeats && (tripchartDetailsdto.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || tripchartDetailsdto.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()))) {
					ticketAddonsPS.setInt(1, ticketId);
					ticketAddonsPS.setInt(2, ticketDetailsId);
					List<TicketAddonsDetailsDTO> addonsDetailsList = new ArrayList<>();
					@Cleanup
					ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
					while (ticketAddonsRS.next()) {
						TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
						addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
						addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
						addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
						addonsDetailsDTO.setRefferenceCode(ticketAddonsRS.getString("reference_code"));
						addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
						addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
						addonsDetailsList.add(addonsDetailsDTO);
					}
					tripchartDetailsdto.setTicketAddonsDetailsList(addonsDetailsList);
					tripChartDetailsDTOList.add(tripchartDetailsdto);
				}
			}
			tripChartDTO.setTicketDetailsList(tripChartDetailsDTOList);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripChartDTO;

	}

	public List<TicketDTO> findTicket(AuthDTO authDTO, TicketDTO ticket, UserDTO userDTO) {
		List<TicketDTO> list = new ArrayList<>();
		try {
			Map<String, String> ticketCodeMap = new HashMap<String, String>();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			if (StringUtil.isNotNull(ticket.getCode())) {
				@Cleanup
				PreparedStatement ps = null;
				if (userDTO != null && userDTO.getId() != 0) {
					ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,travel_minutes,user_id,from_station_id,to_station_id,bus_id,device_medium,ticket_status_id,lookup_id,trip_code,trip_stage_code,mobile_number,email_id,journey_type,ticket_at,active_flag FROM ticket WHERE namespace_id = ? AND user_id = ? AND code = ?");
					ps.setInt(1, authDTO.getNamespace().getId());
					ps.setInt(2, userDTO.getId());
					ps.setString(3, ticket.getCode());
				}
				else {
					ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,travel_minutes,user_id,from_station_id,to_station_id,bus_id,device_medium,ticket_status_id,lookup_id,trip_code,trip_stage_code,mobile_number,email_id,journey_type,ticket_at,active_flag FROM ticket WHERE namespace_id = ? AND code = ?");
					ps.setInt(1, authDTO.getNamespace().getId());
					ps.setString(2, ticket.getCode());
				}
				@Cleanup
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setId(rs.getInt("id"));
					ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
					ticketDTO.setBookingCode(rs.getString("booking_code"));
					ticketDTO.setCode(rs.getString("code"));
					ticketDTO.setTravelMinutes(rs.getInt("travel_minutes"));
					ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
					ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
					ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
					ticketDTO.setPassengerEmailId(rs.getString("email_id"));
					ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(rs.getString("journey_type")));
					ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));
					ticketDTO.setActiveFlag(rs.getInt("active_flag"));
					ticketDTO.setLookupId(rs.getInt("lookup_id"));

					UserDTO ticketUserDTO = new UserDTO();
					ticketUserDTO.setId(rs.getInt("user_id"));
					ticketDTO.setTicketUser(ticketUserDTO);

					TripDTO tripDTO = new TripDTO();
					StageDTO stageDTO = new StageDTO();
					stageDTO.setCode(rs.getString("trip_stage_code"));
					tripDTO.setStage(stageDTO);
					tripDTO.setCode(rs.getString("trip_code"));
					ticketDTO.setTripDTO(tripDTO);

					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setId(rs.getInt("from_station_id"));
					StationDTO toStationDTO = new StationDTO();
					ticketDTO.setFromStation(fromStationDTO);
					toStationDTO.setId(rs.getInt("to_station_id"));
					ticketDTO.setToStation(toStationDTO);

					// pickup related Ticket Code
					if (ticketDTO.getLookupId() != 0 && ticketDTO.getId() != ticketDTO.getLookupId()) {
						@Cleanup
						PreparedStatement lookupTicket = connection.prepareStatement("SELECT code FROM ticket WHERE id = ? AND namespace_id = ? AND active_flag = 1");
						lookupTicket.setInt(1, ticketDTO.getLookupId());
						lookupTicket.setInt(2, authDTO.getNamespace().getId());
						@Cleanup
						ResultSet lookupRS = lookupTicket.executeQuery();
						if (lookupRS.next()) {
							ticketDTO.setRelatedTicketCode(lookupRS.getString("code"));
						}
					}

					ticketCodeMap.put(ticketDTO.getCode(), ticketDTO.getCode());
					list.add(ticketDTO);
				}
			}
			if (StringUtil.isNotNull(ticket.getPassengerMobile())) {
				@Cleanup
				PreparedStatement ps = null;
				if (userDTO != null && userDTO.getId() != 0) {
					ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,travel_minutes,user_id,from_station_id,to_station_id,bus_id,trip_code,trip_stage_code,mobile_number,email_id,journey_type,device_medium,ticket_status_id,ticket_at,active_flag FROM ticket WHERE namespace_id = ? AND user_id = ? AND mobile_number = ? ");
					ps.setInt(1, authDTO.getNamespace().getId());
					ps.setInt(2, userDTO.getId());
					ps.setString(3, ticket.getPassengerMobile());
				}
				else {
					ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,travel_minutes,user_id,from_station_id,to_station_id,bus_id,trip_code,trip_stage_code,mobile_number,email_id,journey_type,device_medium,ticket_status_id,ticket_at,active_flag FROM ticket WHERE namespace_id = ? AND mobile_number = ? ");
					ps.setInt(1, authDTO.getNamespace().getId());
					ps.setString(2, ticket.getPassengerMobile());

				}
				@Cleanup
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setCode(rs.getString("code"));
					{
						// remove duplicate ticket
						if (ticketCodeMap.get(ticketDTO.getCode()) != null) {
							continue;
						}
					}
					ticketDTO.setId(rs.getInt("id"));
					ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
					ticketDTO.setBookingCode(rs.getString("booking_code"));
					ticketDTO.setTravelMinutes(rs.getInt("travel_minutes"));
					ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
					ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
					ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
					ticketDTO.setPassengerEmailId(rs.getString("email_id"));
					ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(rs.getString("journey_type")));
					ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));
					ticketDTO.setActiveFlag(rs.getInt("active_flag"));

					UserDTO ticketUserDTO = new UserDTO();
					ticketUserDTO.setId(rs.getInt("user_id"));
					ticketDTO.setTicketUser(ticketUserDTO);

					TripDTO tripDTO = new TripDTO();
					StageDTO stageDTO = new StageDTO();
					stageDTO.setCode(rs.getString("trip_stage_code"));
					tripDTO.setStage(stageDTO);
					tripDTO.setCode(rs.getString("trip_code"));
					ticketDTO.setTripDTO(tripDTO);

					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setId(rs.getInt("from_station_id"));
					StationDTO toStationDTO = new StationDTO();
					ticketDTO.setFromStation(fromStationDTO);
					toStationDTO.setId(rs.getInt("to_station_id"));
					ticketDTO.setToStation(toStationDTO);

					ticketCodeMap.put(ticketDTO.getCode(), ticketDTO.getCode());
					list.add(ticketDTO);
				}
			}
			if (StringUtil.isNotNull(ticket.getPassengerEmailId())) {
				@Cleanup
				PreparedStatement ps = null;
				if (userDTO != null && userDTO.getId() != 0) {
					ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,travel_minutes,user_id,from_station_id,to_station_id,bus_id,trip_code,trip_stage_code,mobile_number,email_id,journey_type,device_medium,ticket_status_id,ticket_at,active_flag FROM ticket WHERE namespace_id = ? AND user_id = ? AND email_id = ? ");
					ps.setInt(1, authDTO.getNamespace().getId());
					ps.setInt(2, userDTO.getId());
					ps.setString(3, ticket.getPassengerEmailId());
				}
				else {
					ps = connection.prepareStatement("SELECT id,code,booking_code,trip_date,travel_minutes,user_id,from_station_id,to_station_id,bus_id,trip_code,trip_stage_code,mobile_number,email_id,journey_type,device_medium,ticket_status_id,ticket_at,active_flag FROM ticket WHERE namespace_id = ? AND email_id = ? ");
					ps.setInt(1, authDTO.getNamespace().getId());
					ps.setString(2, ticket.getPassengerEmailId());
				}
				@Cleanup
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setCode(rs.getString("code"));
					{
						// remove duplicate ticket
						if (ticketCodeMap.get(ticketDTO.getCode()) != null) {
							continue;
						}
					}
					ticketDTO.setId(rs.getInt("id"));
					ticketDTO.setTripDate(new DateTime(rs.getString("trip_date")));
					ticketDTO.setBookingCode(rs.getString("booking_code"));
					ticketDTO.setTravelMinutes(rs.getInt("travel_minutes"));
					ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
					ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
					ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
					ticketDTO.setPassengerEmailId(rs.getString("email_id"));
					ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(rs.getString("journey_type")));
					ticketDTO.setTicketAt(new DateTime(rs.getString("ticket_at")));
					ticketDTO.setActiveFlag(rs.getInt("active_flag"));

					UserDTO ticketUserDTO = new UserDTO();
					ticketUserDTO.setId(rs.getInt("user_id"));
					ticketDTO.setTicketUser(ticketUserDTO);

					TripDTO tripDTO = new TripDTO();
					StageDTO stageDTO = new StageDTO();
					stageDTO.setCode(rs.getString("trip_stage_code"));
					tripDTO.setStage(stageDTO);
					tripDTO.setCode(rs.getString("trip_code"));
					ticketDTO.setTripDTO(tripDTO);

					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setId(rs.getInt("from_station_id"));
					StationDTO toStationDTO = new StationDTO();
					ticketDTO.setFromStation(fromStationDTO);
					toStationDTO.setId(rs.getInt("to_station_id"));
					ticketDTO.setToStation(toStationDTO);

					ticketCodeMap.put(ticketDTO.getCode(), ticketDTO.getCode());
					list.add(ticketDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return list;
	}

	public TicketDTO getAutoPassengerDetails(AuthDTO authDTO, String mobileNumber, int seatCount) {

		TicketDTO ticketDTO = new TicketDTO();
		try {
			List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = null;
			ps = connection.prepareStatement("SELECT email_id, passenger_name,passenger_age,seat_gender FROM ticket tck,ticket_detail tkd WHERE tck.namespace_id = ? AND tck.id = tkd.ticket_id AND tck.mobile_number = ? ORDER BY tck.id DESC LIMIT ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, mobileNumber);
			ps.setInt(3, seatCount);
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ticketDTO.setPassengerEmailId(rs.getString("email_id"));
				TicketDetailsDTO tdDTO = new TicketDetailsDTO();
				tdDTO.setPassengerName(rs.getString("passenger_name"));
				tdDTO.setPassengerAge(rs.getInt("passenger_age"));
				tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(rs.getInt("seat_gender")));
				ticketDetails.add(tdDTO);
			}
			ticketDTO.setTicketDetails(ticketDetails);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return ticketDTO;
	}

	public List<TicketDTO> getPhoneBookingTickets(AuthDTO authDTO, DateTime fromDate, DateTime toDate, UserDTO userDTO) {
		List<TicketDTO> phoneTicketsList = new ArrayList<TicketDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = null;
			if (userDTO != null && userDTO.getId() != 0) {
				ticketPS = connection.prepareStatement("SELECT id, code FROM ticket WHERE namespace_id = ? AND user_id = ? AND ticket_status_id = 5 AND trip_date >= ? AND trip_date <= ? AND active_flag = 1");
				ticketPS.setInt(1, authDTO.getNamespace().getId());
				ticketPS.setInt(2, userDTO.getId());
				ticketPS.setString(3, DateUtil.convertDate(fromDate));
				ticketPS.setString(4, DateUtil.convertDate(toDate));
			}
			else {
				ticketPS = connection.prepareStatement("SELECT id, code FROM ticket WHERE namespace_id = ? AND ticket_status_id = 5 AND trip_date >= ? AND trip_date <= ? AND active_flag = 1");
				ticketPS.setInt(1, authDTO.getNamespace().getId());
				ticketPS.setString(2, DateUtil.convertDate(fromDate));
				ticketPS.setString(3, DateUtil.convertDate(toDate));
			}

			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();

			while (ticketRS.next()) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setId(ticketRS.getInt("id"));
				ticketDTO.setCode(ticketRS.getString("code"));
				// Get ticket details for the PNR
				getTicketStatus(authDTO, ticketDTO);
				phoneTicketsList.add(ticketDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return phoneTicketsList;
	}

	public List<TripDTO> getTripsForPhoneBlockForceRelease(AuthDTO authDTO, DateTime travelDate) {
		List<TripDTO> phoneBlockTrips = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT DISTINCT trip_code FROM ticket WHERE namespace_id = ? AND ticket_status_id = 5 AND trip_date <= ? AND active_flag = 1");
			ticketPS.setInt(1, authDTO.getNamespace().getId());
			ticketPS.setString(2, travelDate.format("YYYY-MM-DD"));

			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			while (ticketRS.next()) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(ticketRS.getString("trip_code"));
				phoneBlockTrips.add(tripDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return phoneBlockTrips;
	}

	public TicketRefundDTO getRefundTicket(AuthDTO authDTO, TicketRefundDTO ticketRefundDTO) {

		List<TicketRefundDTO> refundList = new ArrayList<TicketRefundDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT tck.code,booking_code,trip_date,from_station_id,to_station_id,travel_minutes,mobile_number,tctr.updated_at,tctr.code,email_id,refund_amount,ticket_at,tctr.transaction_seat_count FROM ticket_cancel_transaction tkct,ticket tck, ticket_transaction tctr WHERE tck.namespace_id = tctr.namespace_id AND tctr.namespace_id = ?  AND tkct.ticket_transaction_id = tctr.id AND tctr.ticket_id = tck.id AND tkct.refund_status = ? ");
			ticketPS.setInt(1, authDTO.getNamespace().getId());
			ticketPS.setInt(2, ticketRefundDTO.getRefundStatus().getId());
			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			while (ticketRS.next()) {
				TicketRefundDTO refundDTO = new TicketRefundDTO();
				refundDTO.setTicketCode(ticketRS.getString("tck.code"));
				refundDTO.setBookingCode(ticketRS.getString("booking_code"));
				refundDTO.setTransactionCode(ticketRS.getString("tctr.code"));
				refundDTO.setTripDate(new DateTime(ticketRS.getString("trip_date")));
				refundDTO.setTravelMinutes(ticketRS.getInt("travel_minutes"));

				refundDTO.setPassegerMobleNo(ticketRS.getString("mobile_number"));
				refundDTO.setPassegerEmailId(ticketRS.getString("email_id"));
				refundDTO.setTotalRefundAmount(ticketRS.getBigDecimal("refund_amount"));
				refundDTO.setBookedAt(ticketRS.getString("ticket_at"));
				refundDTO.setCanncelledAt(ticketRS.getString("tctr.updated_at"));

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(ticketRS.getInt("from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(ticketRS.getInt("to_station_id"));

				refundDTO.setFromStation(fromStationDTO);
				refundDTO.setToStation(toStationDTO);
				refundDTO.setBookingCode(ticketRS.getString("booking_code"));
				refundDTO.setSeatCount(ticketRS.getInt("tctr.transaction_seat_count"));
				refundList.add(refundDTO);
			}

			ticketRefundDTO.setList(refundList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ticketRefundDTO;
	}

	public TicketRefundDTO updateRefundTicketStatus(AuthDTO authDTO, TicketRefundDTO ticketRefundDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int ticketId = 0, ticketTransactionId = 0;
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT ticket_id, id FROM ticket_transaction WHERE namespace_id = ? AND code = ?");
			ticketPS.setInt(1, authDTO.getNamespace().getId());
			ticketPS.setString(2, ticketRefundDTO.getTransactionCode());
			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			if (ticketRS.next()) {
				ticketId = ticketRS.getInt("ticket_id");
				ticketTransactionId = ticketRS.getInt("id");
				@Cleanup
				PreparedStatement refundPS = connection.prepareStatement("UPDATE ticket_cancel_transaction SET refund_status = ?, updated_by = ?, updated_at = NOW() WHERE ticket_transaction_id = ? AND ticket_id = ? ");
				refundPS.setInt(1, ticketRefundDTO.getRefundStatus().getId());
				refundPS.setInt(2, authDTO.getUser().getId());
				refundPS.setInt(3, ticketTransactionId);
				refundPS.setInt(4, ticketId);
				refundPS.execute();
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketRefundDTO;
	}

	public void getTicketTripDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT id,code,trip_date,travel_minutes,from_station_id,to_station_id,boarding_point_id,boarding_point_minutes,dropping_point_id,dropping_point_minutes,trip_code,ticket_status_id,active_flag FROM ticket WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			ticketPS.setString(1, ticketDTO.getCode());
			ticketPS.setInt(2, authDTO.getNamespace().getId());

			@Cleanup
			PreparedStatement ticketDetailsPs = null;
			@Cleanup
			ResultSet ticketDetailsRs = null;

			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			StationPointDAO stationPointDAO = new StationPointDAO();
			if (ticketRS.next()) {
				ticketDTO.setId(ticketRS.getInt("id"));
				ticketDTO.setCode(ticketRS.getString("code"));
				ticketDTO.setTripDate(new DateTime(ticketRS.getString("trip_date")));
				ticketDTO.setTravelMinutes(ticketRS.getInt("travel_minutes"));
				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(ticketRS.getInt("from_station_id"));
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(ticketRS.getInt("to_station_id"));
				ticketDTO.setFromStation(fromStationDTO);
				ticketDTO.setToStation(toStationDTO);

				// get boarding and dropping points
				StationPointDTO fromStationPoint = new StationPointDTO();
				fromStationPoint.setId(ticketRS.getInt("boarding_point_id"));
				stationPointDAO.getStationPointbyId(connection, fromStationPoint);
				fromStationPoint.setMinitues(ticketRS.getInt("boarding_point_minutes"));
				ticketDTO.setBoardingPoint(fromStationPoint);

				StationPointDTO toStationPoint = new StationPointDTO();
				toStationPoint.setId(ticketRS.getInt("dropping_point_id"));
				stationPointDAO.getStationPointbyId(connection, toStationPoint);
				toStationPoint.setMinitues(ticketRS.getInt("dropping_point_minutes"));
				ticketDTO.setDroppingPoint(toStationPoint);

				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(ticketRS.getString("trip_code"));
				ticketDTO.setTripDTO(tripDTO);
				ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketRS.getInt("ticket_status_id")));
				ticketDTO.setActiveFlag(ticketRS.getInt("active_flag"));

				List<TicketDetailsDTO> ticketDetails = new ArrayList<>();

				ticketDetailsPs = connection.prepareStatement("SELECT id,ticket_id,seat_name,seat_code,seat_type,ticket_status_id,travel_status_id,passenger_name,passenger_age,seat_gender,seat_fare,ac_bus_tax,active_flag,updated_by,updated_at FROM ticket_detail WHERE ticket_id = ? and active_flag = 1");
				ticketDetailsPs.setInt(1, ticketDTO.getId());

				ticketDetailsRs = ticketDetailsPs.executeQuery();
				while (ticketDetailsRs.next()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setSeatName(ticketDetailsRs.getString("seat_name"));
					tdDTO.setSeatCode(ticketDetailsRs.getString("seat_code"));
					tdDTO.setSeatType(ticketDetailsRs.getString("seat_type"));
					tdDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketDetailsRs.getInt("ticket_status_id")));
					tdDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(ticketDetailsRs.getInt("travel_status_id")));
					tdDTO.setPassengerName(ticketDetailsRs.getString("passenger_name"));
					tdDTO.setPassengerAge(ticketDetailsRs.getInt("passenger_age"));
					tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(ticketDetailsRs.getInt("seat_gender")));
					tdDTO.setSeatFare(ticketDetailsRs.getBigDecimal("seat_fare"));
					tdDTO.setAcBusTax(ticketDetailsRs.getBigDecimal("ac_bus_tax"));
					tdDTO.setActiveFlag(ticketDetailsRs.getInt("active_flag"));
					ticketDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(ticketDetails);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public NamespaceDTO getNamespace(TicketDTO ticketDTO) {
		NamespaceDTO namespaceDTO = new NamespaceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT  namespace_id, device_medium FROM ticket WHERE code = ?");
			ticketPS.setString(1, ticketDTO.getCode());
			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			if (ticketRS.next()) {
				namespaceDTO.setId(ticketRS.getInt("namespace_id"));
				ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(ticketRS.getInt("device_medium")));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return namespaceDTO;
	}

	public List<TicketDTO> findTicketbyMobileCouponHistory(AuthDTO authDTO, String passengerMobile, String coupon) {
		List<TicketDTO> ticketList = new ArrayList<TicketDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = null;
			ticketPS = connection.prepareStatement("SELECT tck.code, tck.ticket_status_id, tck.active_flag FROM ticket tck, addons_discount_criteria crit, addons_discount_coupon coup, ticket_addons_detail addon WHERE crit.namespace_id = tck.namespace_id AND crit.namespace_id = coup.namespace_id AND tck.namespace_id = ? AND addon.ticket_id = tck.id AND addon.active_flag = 1 AND tck.mobile_number = ? AND tck.active_flag = 1 AND addon.refference_id = crit.id AND crit.addons_discount_coupon_id = coup.id AND coup.coupon = ? AND coup.active_flag = 1");
			ticketPS.setInt(1, authDTO.getNamespace().getId());
			ticketPS.setString(2, passengerMobile);
			ticketPS.setString(3, coupon);
			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			while (ticketRS.next()) {
				TicketDTO ticket = new TicketDTO();
				ticket.setCode(ticketRS.getString("code"));
				ticket.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketRS.getInt("ticket_status_id")));
				ticket.setActiveFlag(ticketRS.getInt("active_flag"));
				ticketList.add(ticket);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ticketList;
	}

	public void updateTicketLookup(AuthDTO authDTO, BookingDTO bookingDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET lookup_id = ? WHERE id = ?");
			for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
				ps.setInt(1, ticketDTO.getLookupId());
				ps.setInt(2, ticketDTO.getId());
				ps.executeUpdate();
				ps.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTravelStatus(AuthDTO authDTO, TicketDTO ticketDTO, TravelStatusEM travelStatus) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET ticket_detail.travel_status_id = ?, ticket_detail.updated_by = ?, ticket_detail.updated_at = NOW() WHERE ticket_detail.id = ? AND ticket_detail.ticket_id = ? AND active_flag = 1");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsPS.setInt(1, ticketDetailsDTO.getTravelStatus().getId());
				ticketDetailsPS.setInt(2, authDTO.getUser().getId());
				ticketDetailsPS.setInt(3, ticketDetailsDTO.getId());
				ticketDetailsPS.setInt(4, ticketDTO.getId());
				ticketDetailsPS.addBatch();
			}
			ticketDetailsPS.executeBatch();

			TicketDTO ticket = getTicketStatus(ticketDTO, travelStatus);
			insertTicketAudit(authDTO, ticket, connection, "Update Ticket Travel Status - " + ticket.getSeatNames(), false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTicketRemarks(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET remarks = ? WHERE id = ? AND code = ? AND namespace_id= ?");
			ps.setString(1, ticketDTO.getRemarks());
			ps.setInt(2, ticketDTO.getId());
			ps.setString(3, ticketDTO.getCode());
			ps.setInt(4, authDTO.getNamespace().getId());
			ps.executeUpdate();
			ps.clearParameters();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTicketForUser(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET user_id = ?, for_user_id = ? WHERE id = ? AND code = ? AND namespace_id = ?");
			ps.setInt(1, ticketDTO.getTicketUser().getId());
			ps.setInt(2, ticketDTO.getTicketForUser().getId());
			ps.setInt(3, ticketDTO.getId());
			ps.setString(4, ticketDTO.getCode());
			ps.setInt(5, authDTO.getNamespace().getId());
			ps.executeUpdate();
			ps.clearParameters();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TicketDTO> getTicketsForFeedback(AuthDTO authDTO, TicketDTO ticket) {
		List<TicketDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT code, mobile_number, email_id, device_medium FROM ticket WHERE namespace_id = ? AND trip_date = ? AND ticket_status_id = 1 AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, ticket.getTripDate().format("YYYY-MM-DD"));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(rs.getString("code"));
				ticketDTO.setPassengerMobile(rs.getString("mobile_number"));
				ticketDTO.setPassengerEmailId(rs.getString("email_id"));
				ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				list.add(ticketDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return list;
	}

	public List<TicketDTO> getTicketByMobile(AuthDTO authDTO, String mobileNumber) {
		List<TicketDTO> ticketList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DISTINCT tssd.ticket_code, trp.code AS trip_code, trp.trip_date FROM trip trp, trip_stage_seat_detail tssd WHERE tssd.namespace_id = ? AND trp.namespace_id = ? AND trp.id = tssd.trip_id AND tssd.contact_number = ? AND trp.trip_date BETWEEN ? AND ? AND tssd.ticket_status_id IN (1,5) AND tssd.active_flag = 1 AND trp.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, mobileNumber);
			selectPS.setString(4, DateUtil.minusDaysToDate(DateUtil.NOW(), Numeric.ONE_INT).format(Text.DATE_DATE4J));
			selectPS.setString(5, DateUtil.addDaysToDate(DateUtil.NOW(), Numeric.ONE_INT).format(Text.DATE_DATE4J));

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(selectRS.getString("tssd.ticket_code"));
				ticketDTO.setTripDate(new DateTime(selectRS.getString("trp.trip_date")));

				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(selectRS.getString("trip_code"));
				ticketDTO.setTripDTO(tripDTO);

				ticketList.add(ticketDTO);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketList;
	}

	public boolean isServiceFirstTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		boolean isExist = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT COUNT(id) AS ticket_count FROM ticket WHERE namespace_id = ? AND trip_code = ? AND active_flag = 1 GROUP BY trip_code");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, ticketDTO.getTripDTO().getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int count = rs.getInt("ticket_count");
				if (count > 1) {
					return false;
				}
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return isExist;
	}

	private TicketDTO getTicketStatus(TicketDTO ticketDTO, TravelStatusEM travelStatus) {
		TicketDTO ticket = new TicketDTO();
		ticket = ticketDTO;
		TicketStatusEM ticketStatus = null;
		if (TravelStatusEM.BOARDED.getId() == travelStatus.getId()) {
			ticketStatus = TicketStatusEM.TICKET_BOARDED;
		}
		else if (TravelStatusEM.NOT_BOARDED.getId() == travelStatus.getId()) {
			ticketStatus = TicketStatusEM.TICKET_NOT_BOARDED;
		}
		else if (TravelStatusEM.NOT_TRAVELED.getId() == travelStatus.getId()) {
			ticketStatus = TicketStatusEM.TICKET_NOT_TRAVEL;
		}
		else if (TravelStatusEM.YET_BOARD.getId() == travelStatus.getId()) {
			ticketStatus = TicketStatusEM.TICKET_YET_BOARD;
		}
		else if (TravelStatusEM.TRAVELED.getId() == travelStatus.getId()) {
			ticketStatus = TicketStatusEM.TICKET_TRAVELED;
		}
		ticket.setTicketStatus(ticketStatus);
		return ticket;
	}

	private String getTicketEvent(TicketDTO ticketDTO, Map<String, TicketStatusEM> ticketStatusMap) {
		StringBuilder events = new StringBuilder();
		TicketDetailsDTO ticketDetails = Iterables.getFirst(ticketDTO.getTicketDetails(), null);
		if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && ticketDetails != null && ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
			events.append(ticketDTO.getSeatNames() + " - ");

			ticketStatusMap.put("TICKET_STATUS", ticketDTO.getTicketStatus());
			ticketDTO.setTicketStatus(TicketStatusEM.TICKET_PARTIAL_CANCELLED);
		}
		return events.toString();
	}

	public void findTicketByLookupId(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code FROM ticket WHERE namespace_id = ? AND lookup_id = ? AND ticket_status_id != 6 AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, ticketDTO.getLookupId());

			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticketDTO.setId(rs.getInt("id"));
				ticketDTO.setCode(rs.getString("code"));
			}
			else {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
		}
		catch (ServiceException e) {
			System.out.println("PNR01:" + authDTO.getNamespaceCode() + " - " + ticketDTO.getCode() + " - " + ticketDTO.getBookingCode());
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<TicketDTO> findTicketByLookupIdV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketDTO> tickets = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code FROM ticket WHERE namespace_id = ? AND lookup_id = ? AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, ticketDTO.getLookupId());

			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TicketDTO ticket = new TicketDTO();
				ticket.setId(rs.getInt("id"));
				ticket.setCode(rs.getString("code"));
				tickets.add(ticket);
			}
		}
		catch (ServiceException e) {
			System.out.println("PNR01:" + authDTO.getNamespaceCode() + " - " + ticketDTO.getCode() + " - " + ticketDTO.getBookingCode());
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tickets;
	}

	public Map<Integer, Map<String, Integer>> getPastDaysBookedRoute(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		Map<Integer, Map<String, Integer>> topRoute = new HashMap<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT from_station_id, to_station_id, COUNT(id) AS route_count FROM ticket WHERE namespace_id = ? AND trip_date >= ? AND trip_date <= ? AND ticket_status_id NOT IN (3,4,5,6) AND active_flag = 1 GROUP BY from_station_id,to_station_id ORDER BY from_station_id,to_station_id ");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, DateUtil.convertDate(fromDate));
			ps.setString(3, DateUtil.convertDate(toDate));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int fromStationId = rs.getInt("from_station_id");
				int toStationId = rs.getInt("to_station_id");
				int routeCount = rs.getInt("route_count");

				if (topRoute.get(fromStationId) == null) {
					Map<String, Integer> topRouteMap = new HashMap<>();
					topRouteMap.put(fromStationId + Text.UNDER_SCORE + toStationId, routeCount);
					topRoute.put(fromStationId, topRouteMap);
				}
				else {
					Map<String, Integer> topRouteMap = topRoute.get(fromStationId);
					topRouteMap.put(fromStationId + Text.UNDER_SCORE + toStationId, routeCount);
					topRoute.put(fromStationId, topRouteMap);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return topRoute;
	}

	public List<TicketDTO> getTicketDetails(AuthDTO authDTO, List<GroupDTO> groups, List<TicketStatusEM> ticketStatusList, DateTime fromDate, DateTime toDate) {
		List<TicketDTO> ticketList = new ArrayList<>();
		try {
			StringBuilder groupFilter = new StringBuilder();
			int groupCount = groups.size();
			int count = 1;
			for (GroupDTO groupDTO : groups) {
				groupFilter.append(groupDTO.getId());
				if (groupCount > 1 && count != groupCount) {
					groupFilter.append(Text.COMMA);
				}
				count = count + 1;
			}

			StringBuilder ticketStatusFilter = new StringBuilder();
			int ticketStatusCount = ticketStatusList.size();
			count = 1;
			for (TicketStatusEM ticketStatusEM : ticketStatusList) {
				ticketStatusFilter.append(ticketStatusEM.getId());
				if (ticketStatusCount > 1 && count != ticketStatusCount) {
					ticketStatusFilter.append(Text.COMMA);
				}
				count = count + 1;
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT tck.id, tck.code, tck.booking_code, tck.trip_date, tck.travel_minutes, tck.trip_code, tck.ticket_at, tck.ticket_status_id, tck.mobile_number, tck.email_id, tck.user_id, tck.active_flag FROM ticket tck, user usr WHERE tck.namespace_id = ? AND tck.namespace_id = usr.namespace_id AND tck.user_id = usr.id AND usr.user_group_id IN (" + groupFilter + ") AND tck.ticket_status_id IN (" + ticketStatusFilter + ") AND tck.trip_date BETWEEN ? AND ? AND tck.active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, DateUtil.convertDate(fromDate));
			ps.setString(3, DateUtil.convertDate(toDate));

			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setId(rs.getInt("id"));
				ticketDTO.setCode(rs.getString("tck.code"));
				ticketDTO.setBookingCode(rs.getString("tck.booking_code"));
				ticketDTO.setTripDate(new DateTime(rs.getString("tck.trip_date")));
				ticketDTO.setTravelMinutes(rs.getInt("tck.travel_minutes"));
				ticketDTO.setTicketAt(new DateTime(rs.getString("tck.ticket_at")));
				ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(rs.getInt("ticket_status_id")));
				ticketDTO.setPassengerMobile(rs.getString("tck.mobile_number"));
				ticketDTO.setPassengerEmailId(rs.getString("tck.email_id"));

				UserDTO ticketUser = new UserDTO();
				ticketUser.setId(rs.getInt("tck.user_id"));
				ticketDTO.setTicketUser(ticketUser);

				ticketDTO.setActiveFlag(rs.getInt("tck.active_flag"));

				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(rs.getString("tck.trip_code"));
				ticketDTO.setTripDTO(tripDTO);

				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				@Cleanup
				PreparedStatement pdps = connection.prepareStatement("SELECT id,ticket_id,seat_name,seat_code,seat_type,ticket_status_id,travel_status_id,passenger_name,passenger_age,seat_gender,seat_fare,ac_bus_tax,active_flag,updated_by,updated_at FROM ticket_detail WHERE ticket_id = ? and active_flag = 1");
				pdps.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet pdrs = pdps.executeQuery();
				while (pdrs.next()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setId(pdrs.getInt("id"));
					tdDTO.setSeatName(pdrs.getString("seat_name"));
					tdDTO.setSeatCode(pdrs.getString("seat_code"));
					tdDTO.setSeatType(pdrs.getString("seat_type"));
					tdDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(pdrs.getInt("ticket_status_id")));
					tdDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(pdrs.getInt("travel_status_id")));
					tdDTO.setPassengerName(pdrs.getString("passenger_name"));
					tdDTO.setPassengerAge(pdrs.getInt("passenger_age"));
					tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(pdrs.getInt("seat_gender")));
					tdDTO.setSeatFare(pdrs.getBigDecimal("seat_fare"));
					tdDTO.setAcBusTax(pdrs.getBigDecimal("ac_bus_tax"));
					tdDTO.setActiveFlag(pdrs.getInt("active_flag"));
					ticketDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(ticketDetails);

				List<TicketAddonsDetailsDTO> ticketAddonsDetailsList = new ArrayList<TicketAddonsDetailsDTO>();
				@Cleanup
				PreparedStatement ticketAddonsPS = connection.prepareStatement("SELECT tad.id,td.seat_code,tad.ticket_addons_type_id,tad.ticket_status_id,tad.refference_id,tad.reference_code,tad.value,tad.active_flag FROM ticket_addons_detail tad,  ticket_detail td WHERE tad.ticket_id = td.ticket_id and tad.ticket_detail_id = td.id AND tad.active_flag = td.active_flag AND tad.ticket_id = ? and tad.active_flag = 1");
				ticketAddonsPS.setInt(1, ticketDTO.getId());
				@Cleanup
				ResultSet ticketAddonsRS = ticketAddonsPS.executeQuery();
				while (ticketAddonsRS.next()) {
					TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
					addonsDetailsDTO.setId(ticketAddonsRS.getInt("id"));
					addonsDetailsDTO.setSeatCode(ticketAddonsRS.getString("seat_code"));
					addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(ticketAddonsRS.getInt("ticket_addons_type_id")));
					addonsDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketAddonsRS.getInt("ticket_status_id")));
					addonsDetailsDTO.setRefferenceId(ticketAddonsRS.getInt("refference_id"));
					addonsDetailsDTO.setRefferenceCode(ticketAddonsRS.getString("reference_code"));
					addonsDetailsDTO.setValue(ticketAddonsRS.getBigDecimal("value"));
					addonsDetailsDTO.setActiveFlag(ticketAddonsRS.getInt("active_flag"));
					ticketAddonsDetailsList.add(addonsDetailsDTO);
				}
				ticketDTO.setTicketAddonsDetails(ticketAddonsDetailsList);

				ticketList.add(ticketDTO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ticketList;

	}

	public List<TicketDTO> getTicketsByTripDate(AuthDTO authDTO, String tripDate) {
		List<TicketDTO> tickets = new ArrayList<TicketDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement psTrip = connection.prepareStatement("SELECT tck.code AS ticket_code, tck.from_station_id, tck.to_station_id, tck.schedule_id, tdt.seat_gender, tdt.seat_name, tdt.seat_code, tdt.seat_fare, tdt.ticket_status_id, tdt.travel_status_id, tck.boarding_point_id, tck.dropping_point_id, tck.ticket_at, tdt.passenger_name,tdt.passenger_age,tck.mobile_number, tck.user_id, tdt.updated_at FROM ticket tck, ticket_detail tdt WHERE tck.namespace_id = ? AND tdt.ticket_id = tck.id AND tck.active_flag = tdt.active_flag AND tck.trip_date = ? AND tdt.ticket_status_id IN (1, 2, 5) AND tck.active_flag = 1");
			psTrip.setInt(1, authDTO.getNamespace().getId());
			psTrip.setString(2, tripDate);
			@Cleanup
			ResultSet resultSet = psTrip.executeQuery();
			while (resultSet.next()) {
				TicketDTO ticketDTO = new TicketDTO();

				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setTicketCode(resultSet.getString("ticket_code"));

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(resultSet.getInt("tck.from_station_id"));
				ticketDetailsDTO.setFromStation(fromStationDTO);

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(resultSet.getInt("tck.to_station_id"));
				ticketDetailsDTO.setToStation(toStationDTO);

				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("tck.user_id"));
				ticketDetailsDTO.setUser(userDTO);

				StationPointDTO boardingPoint = new StationPointDTO();
				boardingPoint.setId(resultSet.getInt("tck.boarding_point_id"));
				ticketDTO.setBoardingPoint(boardingPoint);

				StationPointDTO droppingPoint = new StationPointDTO();
				droppingPoint.setId(resultSet.getInt("tck.dropping_point_id"));
				ticketDTO.setDroppingPoint(droppingPoint);

				ticketDetailsDTO.setSeatCode(resultSet.getString("tdt.seat_code"));
				ticketDetailsDTO.setSeatName(resultSet.getString("tdt.seat_name"));
				ticketDetailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(resultSet.getInt("tdt.seat_gender")));
				ticketDetailsDTO.setSeatFare(resultSet.getBigDecimal("tdt.seat_fare"));
				ticketDetailsDTO.setPassengerName(resultSet.getString("tdt.passenger_name"));
				ticketDetailsDTO.setPassengerAge(resultSet.getInt("tdt.passenger_age"));
				ticketDetailsDTO.setContactNumber(resultSet.getString("tck.mobile_number"));
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(resultSet.getInt("tdt.ticket_status_id")));
				ticketDetailsDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(resultSet.getInt("tdt.travel_status_id")));
				ticketDetailsDTO.setTicketAt(DateUtil.getDateTime(resultSet.getString("tck.ticket_at")));
				ticketDetailsDTO.setUpdatedAt(DateUtil.getDateTime(resultSet.getString("tdt.updated_at")));
				ticketDetailsDTO.setScheduleId(resultSet.getInt("tck.schedule_id"));

				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				ticketDetails.add(ticketDetailsDTO);
				ticketDTO.setTicketDetails(ticketDetails);
				tickets.add(ticketDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tickets;
	}

	private String getAdditionalAtribute(TicketDTO ticketDTO) {
		StringBuilder additionalAttribute = new StringBuilder();
		additionalAttribute.append(ticketDTO.getCancellationTerm().getPolicyGroupId()).append(Text.VERTICAL_BAR);

		if (ticketDTO.getScheduleTicketTransferTerms() == null) {
			additionalAttribute.append(Numeric.ZERO);
		}
		else {
			additionalAttribute.append(ticketDTO.getScheduleTicketTransferTerms().getId());
		}
		additionalAttribute.append(Text.VERTICAL_BAR);
		additionalAttribute.append(StringUtil.isNotNull(ticketDTO.getAlternateMobile()) ? ticketDTO.getAlternateMobile() : Text.EMPTY);
		return additionalAttribute.toString();
	}

	private void convertTicketFromAdditionalAtribute(TicketDTO ticketDTO, String additionalAttribute) {
		if (StringUtil.isNotNull(additionalAttribute)) {
			String[] additionalAttributes = additionalAttribute.split("\\|");

			CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
			cancellationTermDTO.setPolicyGroupId(Integer.valueOf(additionalAttributes[Numeric.ZERO_INT]));
			ticketDTO.setCancellationTerm(cancellationTermDTO);

			ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms = new ScheduleTicketTransferTermsDTO();
			scheduleTicketTransferTerms.setId(Integer.valueOf(additionalAttributes[Numeric.ONE_INT]));
			ticketDTO.setScheduleTicketTransferTerms(scheduleTicketTransferTerms);

			ticketDTO.setAlternateMobile(additionalAttributes.length > 2 ? additionalAttributes[Numeric.TWO_INT] : Text.EMPTY);
		}
	}

	private void convertTicketFromAdditionalAtributeV2(TripChartDetailsDTO tripChartDetails, String additionalAttribute) {
		if (StringUtil.isNotNull(additionalAttribute)) {
			String[] additionalAttributes = additionalAttribute.split("\\|");
			tripChartDetails.setAlternateMobile(additionalAttributes.length > 2 ? additionalAttributes[Numeric.TWO_INT] : Text.EMPTY);
		}
	}

	/** Past 3 Months */
	public List<TicketDTO> getTicketByUserCustomer(AuthDTO auth, UserCustomerDTO userCustomer) {
		List<TicketDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, code, ticket_at, boarding_point_id, dropping_point_id, dropping_point_minutes, trip_date, travel_minutes, boarding_point_minutes, bus_id, ticket_status_id, trip_code, user_id, from_station_id, to_station_id, journey_type, mobile_number, email_id FROM ticket WHERE namespace_id = ? AND ticket_status_id IN (1,2,5,6) AND trip_date >= SUBDATE(CURDATE(), INTERVAL 3 MONTH) AND active_flag = 1 AND (mobile_number = ? OR for_user_id = ?)  limit 30");
			preparedStatement.setInt(1, auth.getNamespace().getId());
			preparedStatement.setString(2, userCustomer.getMobile());
			preparedStatement.setInt(3, userCustomer.getId());

			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();

			@Cleanup
			PreparedStatement ticketDetailsPS = null;
			@Cleanup
			ResultSet ticketDetailsRS = null;

			while (resultSet.next()) {
				TicketDTO ticket = new TicketDTO();
				ticket.setId(resultSet.getInt("id"));
				ticket.setCode(resultSet.getString("code"));
				ticket.setTicketAt(DateUtil.getDateTime(resultSet.getString("ticket_at")));
				ticket.setTripDate(DateUtil.getDateTime(resultSet.getString("trip_date")));
				ticket.setTravelMinutes(resultSet.getInt("travel_minutes"));
				ticket.setJourneyType(JourneyTypeEM.getJourneyTypeEM(resultSet.getString("journey_type")));
				ticket.setPassengerMobile(resultSet.getString("mobile_number"));
				ticket.setPassengerEmailId(resultSet.getString("email_id"));
				ticket.setTicketStatus(TicketStatusEM.getTicketStatusEM(resultSet.getInt("ticket_status_id")));

				UserDTO ticketUser = new UserDTO();
				ticketUser.setId(resultSet.getInt("user_id"));
				ticket.setTicketUser(ticketUser);

				TripDTO trip = new TripDTO();
				trip.setCode(resultSet.getString("trip_code"));
				BusDTO bus = new BusDTO();
				bus.setId(resultSet.getInt("bus_id"));
				trip.setBus(bus);
				ticket.setTripDTO(trip);

				StationDTO fromStation = new StationDTO();
				fromStation.setId(resultSet.getInt("from_station_id"));
				ticket.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(resultSet.getInt("to_station_id"));
				ticket.setToStation(toStation);

				StationPointDTO fromStationPoint = new StationPointDTO();
				fromStationPoint.setMinitues(resultSet.getInt("boarding_point_minutes"));
				fromStationPoint.setId(resultSet.getInt("boarding_point_id"));
				ticket.setBoardingPoint(fromStationPoint);

				StationPointDTO toStationPoint = new StationPointDTO();
				toStationPoint.setMinitues(resultSet.getInt("dropping_point_minutes"));
				toStationPoint.setId(resultSet.getInt("dropping_point_id"));
				ticket.setDroppingPoint(toStationPoint);

				ticketDetailsPS = connection.prepareStatement("SELECT seat_name, seat_code, seat_fare, ac_bus_tax, active_flag FROM ticket_detail WHERE ticket_id = ? AND active_flag = 1");
				ticketDetailsPS.setInt(1, ticket.getId());
				ticketDetailsRS = ticketDetailsPS.executeQuery();

				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				while (ticketDetailsRS.next()) {
					TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
					ticketDetailsDTO.setSeatName(ticketDetailsRS.getString("seat_name"));
					ticketDetailsDTO.setSeatFare(ticketDetailsRS.getBigDecimal("seat_fare"));
					ticketDetailsDTO.setAcBusTax(ticketDetailsRS.getBigDecimal("ac_bus_tax"));
					ticketDetails.add(ticketDetailsDTO);
				}
				ticket.setTicketDetails(ticketDetails);
				list.add(ticket);
			}
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return list;
	}

	/** Recent ticket */
	public List<TicketDTO> getBookedTicketByUser(AuthDTO auth, UserDTO userDTO) {
		List<TicketDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, code, ticket_at, boarding_point_id, dropping_point_id, dropping_point_minutes, trip_date, travel_minutes, boarding_point_minutes, bus_id, ticket_status_id, trip_code, user_id, from_station_id, to_station_id, journey_type, mobile_number, email_id FROM ticket WHERE namespace_id = ?  AND user_id = ? AND ticket_status_id IN (1,2,5,6) AND trip_date >= SUBDATE(CURDATE(), INTERVAL 3 MONTH) AND active_flag = 1 limit 10");
			preparedStatement.setInt(1, auth.getNamespace().getId());
 			preparedStatement.setInt(2, userDTO.getId());

			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();

			@Cleanup
			PreparedStatement ticketDetailsPS = null;
			@Cleanup
			ResultSet ticketDetailsRS = null;

			while (resultSet.next()) {
				TicketDTO ticket = new TicketDTO();
				ticket.setId(resultSet.getInt("id"));
				ticket.setCode(resultSet.getString("code"));
				ticket.setTicketAt(DateUtil.getDateTime(resultSet.getString("ticket_at")));
				ticket.setTripDate(DateUtil.getDateTime(resultSet.getString("trip_date")));
				ticket.setTravelMinutes(resultSet.getInt("travel_minutes"));
				ticket.setJourneyType(JourneyTypeEM.getJourneyTypeEM(resultSet.getString("journey_type")));
				ticket.setPassengerMobile(resultSet.getString("mobile_number"));
				ticket.setPassengerEmailId(resultSet.getString("email_id"));
				ticket.setTicketStatus(TicketStatusEM.getTicketStatusEM(resultSet.getInt("ticket_status_id")));

				UserDTO ticketUser = new UserDTO();
				ticketUser.setId(resultSet.getInt("user_id"));
				ticket.setTicketUser(ticketUser);

				TripDTO trip = new TripDTO();
				trip.setCode(resultSet.getString("trip_code"));
				BusDTO bus = new BusDTO();
				bus.setId(resultSet.getInt("bus_id"));
				trip.setBus(bus);
				ticket.setTripDTO(trip);

				StationDTO fromStation = new StationDTO();
				fromStation.setId(resultSet.getInt("from_station_id"));
				ticket.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(resultSet.getInt("to_station_id"));
				ticket.setToStation(toStation);

				StationPointDTO fromStationPoint = new StationPointDTO();
				fromStationPoint.setMinitues(resultSet.getInt("boarding_point_minutes"));
				fromStationPoint.setId(resultSet.getInt("boarding_point_id"));
				ticket.setBoardingPoint(fromStationPoint);

				StationPointDTO toStationPoint = new StationPointDTO();
				toStationPoint.setMinitues(resultSet.getInt("dropping_point_minutes"));
				toStationPoint.setId(resultSet.getInt("dropping_point_id"));
				ticket.setDroppingPoint(toStationPoint);

				ticketDetailsPS = connection.prepareStatement("SELECT seat_name, seat_code, seat_fare, ac_bus_tax, active_flag FROM ticket_detail WHERE ticket_id = ? AND active_flag = 1");
				ticketDetailsPS.setInt(1, ticket.getId());
				ticketDetailsRS = ticketDetailsPS.executeQuery();

				List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
				while (ticketDetailsRS.next()) {
					TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
					ticketDetailsDTO.setSeatName(ticketDetailsRS.getString("seat_name"));
					ticketDetailsDTO.setSeatFare(ticketDetailsRS.getBigDecimal("seat_fare"));
					ticketDetailsDTO.setAcBusTax(ticketDetailsRS.getBigDecimal("ac_bus_tax"));
					ticketDetails.add(ticketDetailsDTO);
				}
				ticket.setTicketDetails(ticketDetails);
				list.add(ticket);
			}
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return list;
	}
}
