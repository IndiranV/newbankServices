package org.in.com.controller.api.io;

import java.util.List;

import lombok.Data;

@Data
public class BusIO {
	private String code;
	private String busType;
	private String categoryCode;
	private String displayName;
	private String name;
	private int totalSeatCount;
	private List<BusSeatLayoutIO> seatLayoutList;
	private BusIO busIO;

}
