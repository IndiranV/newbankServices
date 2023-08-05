package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketAddonsDetailsIO extends BaseIO {
	private String seatCode;
	private TicketStatusEM addonsStatus;
	private AddonsTypeEM addonsType;
	private BigDecimal value;

}
