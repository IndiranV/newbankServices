package org.in.com.controller.web.io;

import lombok.Data;

import org.in.com.controller.web.io.GroupIO;

@Data
public class TripScheduleControlIO {
	private GroupIO group;
	private String openDate;
	private String closeDate;
	private StationIO fromStation;
	private StationIO toStation;
	private TripStatusIO tripStatus;

}
