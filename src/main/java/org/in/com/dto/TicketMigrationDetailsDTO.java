package org.in.com.dto;

import lombok.Data;

@Data
public class TicketMigrationDetailsDTO {

	private TicketDTO ticket;
	private TicketDTO migrationTicket;
	private StationPointDTO boardingPoint;
	private StationPointDTO droppingPoint;
}
