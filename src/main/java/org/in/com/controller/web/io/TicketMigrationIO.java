package org.in.com.controller.web.io;

import java.util.List;

import org.in.com.dto.BaseDTO;
import org.in.com.dto.StationDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketMigrationIO extends BaseDTO<TicketMigrationIO> {
	private StationDTO fromStation;
	private StationDTO toStation;
	private String travelDate;
	private String tripCode;
	private List<TicketMigrationDetailsIO> ticketMigrationDetails;

}
