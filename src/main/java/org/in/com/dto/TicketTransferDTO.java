package org.in.com.dto;

import java.util.List;
import java.util.Map;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketTransferDTO extends BaseDTO<TicketTransferDTO> {
	private StationDTO fromStation;
	private StationDTO toStation;
	private TripDTO transferTrip;
	private TripDTO trip;
	private DateTime travelDate;
	private Map<String, Boolean> additionalAttribute;
	private List<TicketTransferDetailsDTO> ticketTransferDetails;
}
