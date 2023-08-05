package org.in.com.controller.app.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingIO extends BaseIO {
	private List<TicketIO> ticket;
	private boolean roundTripFlag = false;
}
