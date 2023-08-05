package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;

@Data
public class TicketMigrationDetailsIO {
	private String ticketCode;
	private List<String> seatCode;
	private List<String> migrationSeatCode;
}
