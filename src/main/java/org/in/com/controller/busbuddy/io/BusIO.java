package org.in.com.controller.busbuddy.io;

import java.util.List;

import lombok.Data;

@Data
public class BusIO {
	private String categoryCode;
	private String displayName;
	private String busType;
	private String name;
	private String code;
	private int totalSeatCount;
	private List<BusSeatLayoutIO> seatLayoutList;
}
