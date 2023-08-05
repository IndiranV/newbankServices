package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Numeric;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.TicketStatusEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketPhoneBookControlDTO extends BaseDTO<TicketPhoneBookControlDTO> {
	private GroupDTO group;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int allowMinutes;
	private int blockMinutes;
	private MinutesTypeEM blockMinutesType;// AM/PM/MIN
	private String lookupCode;
	private List<TicketPhoneBookControlDTO> overrideList = new ArrayList<TicketPhoneBookControlDTO>();

	private DateTypeEM dateType;
	private String refferenceType;
	private int maxSlabValueLimit;
	private SlabCalenderModeEM slabCalenderMode;
	private SlabCalenderTypeEM slabCalenderType;
	private SlabModeEM slabMode;
	private UserDTO userDTO;
	private List<ScheduleDTO> scheduleList;
	private List<RouteDTO> routeList;
	private TicketStatusEM ticketStatus;
	private int respectiveFlag;

	public DateTime getActiveFromDate() {
		return new DateTime(activeFrom);
	}

	public DateTime getActiveToDate() {
		return new DateTime(activeTo);
	}

	public int getTicketStatusId() {
		return ticketStatus != null ? ticketStatus.getId() : Numeric.ZERO_INT;
	}
}