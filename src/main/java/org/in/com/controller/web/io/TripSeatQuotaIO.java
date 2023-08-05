package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripSeatQuotaIO extends BaseIO {
	private TripIO trip;
	private UserIO user;
	private StationIO fromStation;
	private StationIO toStation;
	private int relaseMinutes;
	private String remarks;
	private List<TicketDetailsIO> quotaDetails;
	private TicketDetailsIO quotaSeat;
	private UserIO updatedUser;
	private String updatedAt;
}
