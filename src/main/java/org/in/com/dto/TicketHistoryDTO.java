package org.in.com.dto;

import org.in.com.dto.enumeration.TicketStatusEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketHistoryDTO extends BaseDTO<TicketHistoryDTO> {
	private UserDTO user;
	private TicketStatusEM status;
	private String addtionalEvent;
	private String log;
	private String datetime;
}
