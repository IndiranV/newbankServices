package org.in.com.dto;

import java.util.List;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketMigrationDTO extends BaseDTO<TicketMigrationDTO> {
	private StationDTO fromStation;
	private StationDTO toStation;
	private TripDTO trip;
	private DateTime travelDate;
	private List<TicketMigrationDetailsDTO> ticketMigrationDetails;

}
