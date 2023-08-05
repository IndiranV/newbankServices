package org.in.com.controller.commerce.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.UserIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusSeatLayoutIO extends BaseIO {

	private BusSeatTypeIO busSeatType;
	private SeatStatusIO seatStatus;
	private SeatGendarStatusIO seatGendarStatus;
	private int rowPos;
	private int colPos;
	private int layer;
	private int sequence;
	private int orientation;
	private String seatName;
	private BigDecimal seatFare;
	private BigDecimal discountFare;
	private BigDecimal serviceTax;

	private String passengerName;
	private String remarks;
	private String boardingPointName;
	private JSONObject stationPoint;
	private int passengerAge;
	private String contactNumber;
	private String ticketCode;
	private String updatedAt;
	private String releaseAt;
	private String route;
	private RouteIO routes;
	private UserIO user;
	private GroupIO group;
	private OrganizationIO organization;
}
