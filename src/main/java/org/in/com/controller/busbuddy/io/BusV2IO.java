package org.in.com.controller.busbuddy.io;

import java.util.List;

import lombok.Data;

@Data
public class BusV2IO {
	private String categoryCode;
	private String busType;
	private String name;
	private String code;
	private int totalSeatCount;
	private List<BusSeatLayoutV2IO> seatLayout;
}
