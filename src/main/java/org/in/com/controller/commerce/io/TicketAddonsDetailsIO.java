package org.in.com.controller.commerce.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketAddonsDetailsIO extends BaseIO {
	private String seatCode;
	private SeatStatusIO addonStatus;
	private AddonTypeIO addonType;
	private BigDecimal value;

}
