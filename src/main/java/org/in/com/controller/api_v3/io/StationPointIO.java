package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;

import lombok.Data;

@Data
public class StationPointIO {
	private String code;
	private String name;
	private String latitude;
	private String longitude;
	private String address;
	private String landmark;
	private String number;
	private String dateTime;
	private List<String> seatList;
	private BigDecimal additionalFare = BigDecimal.ZERO;
	private List<BaseIO> amenities;
}
