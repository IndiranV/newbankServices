package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

public class TicketEditDAO extends TicketDAO {
	public void editMobileNumber(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET mobile_number = ?, updated_at = NOW() WHERE namespace_id = ? AND id = ?");
			ps.setString(1, ticketDTO.getPassengerMobile());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, ticketDTO.getId());
			ps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, event, true);

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void editEmailId(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET email_id = ?, updated_at = NOW() WHERE namespace_id = ? AND id = ?");
			ps.setString(1, ticketDTO.getPassengerEmailId());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, ticketDTO.getId());
			ps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, event, true);

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void editBoardingPoint(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET boarding_point_id = ?, boarding_point_minutes = ?, updated_at = NOW() WHERE namespace_id = ? AND id = ?");
			ps.setInt(1, ticketDTO.getBoardingPoint().getId());
			ps.setInt(2, ticketDTO.getBoardingPoint().getMinitues());
			ps.setInt(3, authDTO.getNamespace().getId());
			ps.setInt(4, ticketDTO.getId());
			ps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, event, true);

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void editDropingPoint(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET dropping_point_id = ?, dropping_point_minutes = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ?");
			ps.setInt(1, ticketDTO.getDroppingPoint().getId());
			ps.setInt(2, ticketDTO.getDroppingPoint().getMinitues());
			ps.setInt(3, authDTO.getNamespace().getId());
			ps.setString(4, ticketDTO.getCode());
			ps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, event, true);

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void editPassengerDetails(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			if (ticketDTO.getId() != 0 && ticketDTO.getTicketDetails() != null && !ticketDTO.getTicketDetails().isEmpty()) {
				@Cleanup
				PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET seat_gender = ?, passenger_age = ?, passenger_name = ?, updated_at = NOW() WHERE ticket_id = ? AND id = ?");
				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					ticketDetailsPS.setInt(1, ticketDetailsDTO.getSeatGendar().getId());
					ticketDetailsPS.setInt(2, ticketDetailsDTO.getPassengerAge());
					ticketDetailsPS.setString(3, ticketDetailsDTO.getPassengerName());
					ticketDetailsPS.setInt(4, ticketDTO.getId());
					ticketDetailsPS.setInt(5, ticketDetailsDTO.getId());
					ticketDetailsPS.executeUpdate();
				}
				insertTicketAudit(authDTO, ticketDTO, connection, event, true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public void editChangeSeat(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			if (ticketDTO.getId() != 0 && ticketDTO.getTicketDetails() != null && !ticketDTO.getTicketDetails().isEmpty()) {
				DateTime updatedAt = DateUtil.NOW();
				@Cleanup
				PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET seat_name = ?, seat_code = ?, updated_at = ? WHERE ticket_id = ? AND id = ?");
				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					ticketDetailsPS.setString(1, ticketDetailsDTO.getSeatName());
					ticketDetailsPS.setString(2, ticketDetailsDTO.getSeatCode());
					ticketDetailsPS.setString(3, DateUtil.convertDateTime(updatedAt));
					ticketDetailsPS.setInt(4, ticketDTO.getId());
					ticketDetailsPS.setInt(5, ticketDetailsDTO.getId());
					ticketDetailsPS.executeUpdate();
				}
				ticketDTO.setUpdatedAt(updatedAt);
				insertTicketAudit(authDTO, ticketDTO, connection, event, true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public void editCustomerIdProof(AuthDTO authDTO, TicketDTO ticketDTO, TicketAddonsDetailsDTO ticketAddonsDetails, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_addons_detail SET reference_code = ?, updated_at = NOW() WHERE ticket_id = ? AND id = ?");
			ticketDetailsPS.setString(1, ticketAddonsDetails.getRefferenceCode());
			ticketDetailsPS.setInt(2, ticketDTO.getId());
			ticketDetailsPS.setInt(3, ticketAddonsDetails.getId());
			ticketDetailsPS.executeUpdate();
			insertTicketAudit(authDTO, ticketDTO, connection, event, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public void addCustomerIdProof(AuthDTO authDTO, TicketDTO ticketDTO, TicketAddonsDetailsDTO ticketAddonsDetails, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int psCount = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_addons_detail (ticket_id,ticket_detail_id,ticket_addons_type_id,ticket_status_id,refference_id,reference_code,value,active_flag,updated_by,updated_at) VALUES (?,?,?,?,?,  ?,?,1,?,NOW())");
			ps.setInt(++psCount, ticketDTO.getId());
			ps.setInt(++psCount, ticketAddonsDetails.getTicketDetailsId(ticketDTO.getTicketDetails()));
			ps.setInt(++psCount, ticketAddonsDetails.getAddonsType().getId());
			ps.setInt(++psCount, ticketAddonsDetails.getTicketStatus().getId());
			ps.setInt(++psCount, ticketAddonsDetails.getRefferenceId());
			ps.setString(++psCount, ticketAddonsDetails.getRefferenceCode());
			ps.setBigDecimal(++psCount, ticketAddonsDetails.getValue());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
			insertTicketAudit(authDTO, ticketDTO, connection, event, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void editMigrateTicket(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection, String event) {
		try {
			if (ticketDTO.getId() != 0) {
				int index = 0;
				@Cleanup
				PreparedStatement ticketPS = connection.prepareStatement("UPDATE ticket SET from_station_id = ?, to_station_id = ?, travel_minutes = ?, boarding_point_id = ?, boarding_point_minutes = ?, dropping_point_id = ?, dropping_point_minutes = ?, bus_id = ?, schedule_id = ?, trip_code = ?, trip_stage_code = ?, service_number = ?, reporting_minutes = ? , remarks = ?, updated_at = NOW() WHERE id = ?");
				ticketPS.setInt(++index, ticketDTO.getFromStation().getId());
				ticketPS.setInt(++index, ticketDTO.getToStation().getId());
				ticketPS.setInt(++index, ticketDTO.getTravelMinutes());
				ticketPS.setInt(++index, ticketDTO.getBoardingPoint().getId());
				ticketPS.setInt(++index, ticketDTO.getBoardingPoint().getMinitues());
				ticketPS.setInt(++index, ticketDTO.getDroppingPoint().getId());
				ticketPS.setInt(++index, ticketDTO.getDroppingPoint().getMinitues());
				ticketPS.setInt(++index, ticketDTO.getTripDTO().getBus().getId());
				ticketPS.setInt(++index, ticketDTO.getTripDTO().getSchedule().getId());
				ticketPS.setString(++index, ticketDTO.getTripDTO().getCode());
				ticketPS.setString(++index, ticketDTO.getTripDTO().getStage().getCode());
				ticketPS.setString(++index, ticketDTO.getServiceNo());
				ticketPS.setInt(++index, ticketDTO.getReportingMinutes());
				ticketPS.setString(++index, StringUtil.substring(ticketDTO.getRemarks(), 240));
				ticketPS.setInt(++index, ticketDTO.getId());
				ticketPS.executeUpdate();

				if (ticketDTO.getId() != 0 && ticketDTO.getTicketDetails() != null && !ticketDTO.getTicketDetails().isEmpty()) {
					@Cleanup
					PreparedStatement ticketDetailsPS = connection.prepareStatement("UPDATE ticket_detail SET seat_name = ?, seat_code = ?, updated_at = NOW() WHERE ticket_id = ? AND id = ?");
					for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
						ticketDetailsPS.setString(1, ticketDetailsDTO.getSeatName());
						ticketDetailsPS.setString(2, ticketDetailsDTO.getSeatCode());
						ticketDetailsPS.setInt(3, ticketDTO.getId());
						ticketDetailsPS.setInt(4, ticketDetailsDTO.getId());
						ticketDetailsPS.executeUpdate();
					}
				}
			}
			insertTicketAudit(authDTO, ticketDTO, connection, "Migrate Ticket " + event, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public void editRemarks(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET remarks = ?, updated_at = NOW() WHERE namespace_id = ? AND id = ?");
			ps.setString(1, ticketDTO.getRemarks());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, ticketDTO.getId());
			ps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, event, true);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void editTicketExtra(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket_extras SET block_release_minutes = ?, updated_at = NOW() WHERE namespace_id = ? AND ticket_id = ? AND active_flag = 1");
			ps.setInt(1, ticketDTO.getTicketExtra().getBlockReleaseMinutes());
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, ticketDTO.getId());
			ps.executeUpdate();

			@Cleanup
			PreparedStatement tps = connection.prepareStatement("UPDATE trip_stage_seat_detail SET extras = ?, updated_at = NOW() WHERE namespace_id = ? AND ticket_code = ? AND active_flag = 1");
			// tps.setString(1, ticketDTO.convertTicketExtra());
			tps.setInt(2, authDTO.getNamespace().getId());
			tps.setString(3, ticketDTO.getCode());
			tps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, "edit ticket extra release minutes", true);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void editAlternateMobileNumber(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE ticket SET additional_attribute = ?, updated_at = NOW() WHERE namespace_id = ? AND id = ?");
			ps.setString(1, getAdditionalAtribute(ticketDTO));
			ps.setInt(2, authDTO.getNamespace().getId());
			ps.setInt(3, ticketDTO.getId());
			ps.executeUpdate();

			insertTicketAudit(authDTO, ticketDTO, connection, event, true);

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
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
}
